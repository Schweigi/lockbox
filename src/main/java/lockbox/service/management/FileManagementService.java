package lockbox.service.management;

import lockbox.domain.model.SharedLinkModel;
import lockbox.domain.repository.SharedLinkRepository;
import lockbox.service.management.exception.EncryptionException;
import lockbox.service.management.exception.InvalidPasswordException;
import lockbox.service.management.exception.LinkExpiredException;
import lockbox.service.storage.FileStorageService;
import lockbox.util.EncryptionKey;
import lockbox.util.EncryptionUtil;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;

@Service
public class FileManagementService {

    @Autowired
    Logger logger;

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileStorageService storageService;

    @Autowired
    private EncryptionUtil encryptionUtil;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public String upload(InputStream inputStream, String fileName, String fileType, long fileSize, String password) throws EncryptionException {
        try {
            EncryptionKey key = encryptionUtil.generateKey();
            InputStream encryptedStream = encryptionUtil.encryptStream(inputStream, key);
            String storedFileId = storageService.save(encryptedStream);
            SharedLinkModel sharedLink = saveSharedLink(fileName, fileType, fileSize, password, storedFileId, key);

            logger.info(String.format("Upload %s with id %s", fileName, sharedLink.getId()));

            return sharedLink.getPublicId();
        } catch (NoSuchAlgorithmException|InvalidKeyException|NoSuchPaddingException|InvalidAlgorithmParameterException e) {
            throw new EncryptionException(e);
        }
    }

    private SharedLinkModel saveSharedLink(String fileName, String fileType, long fileSize, String password, String storageId, EncryptionKey key) {
        String hashedPassword = password == null || "".equals(password) ? null: passwordEncoder.encode(password);
        String publicId = KeyGenerators.string().generateKey();

        SharedLinkModel metaData = new SharedLinkModel();
        metaData.setCreated(Instant.now());
        metaData.setPublicId(publicId);
        metaData.setStorageId(storageId);
        metaData.setFileName(fileName);
        metaData.setFileType(fileType);
        metaData.setFileSize(fileSize);
        metaData.setPassword(hashedPassword);
        metaData.setEncryptionKey(key.getData());

        return sharedLinkRepository.save(metaData);
    }

    public FileInfo info(String publicId) throws LinkExpiredException {
        SharedLinkModel sharedLink = sharedLinkRepository.findByPublicId(publicId);

        if (sharedLink == null) {
            logger.info(String.format("Link %s is expired", publicId));
            throw new LinkExpiredException();
        } else {
            logger.info(String.format("Info %s", sharedLink.getId()));

            return new FileInfo(sharedLink.getFileName(), sharedLink.getFileSize(), sharedLink.getPassword() != null);
        }
    }

    public FileDownload download(String publicId, String password) throws InvalidPasswordException, LinkExpiredException, EncryptionException {
        SharedLinkModel sharedLink = sharedLinkRepository.findByPublicId(publicId);

        if (sharedLink == null) {
            logger.info(String.format("Link %s is expired", publicId));
            throw new LinkExpiredException();
        } else if (sharedLink.getPassword() != null && (password == null || !passwordEncoder.matches(password, sharedLink.getPassword()))) {
            logger.info(String.format("Invalid password for download %s", sharedLink.getId()));
            throw new InvalidPasswordException();
        } else {
            try {
                EncryptionKey key = encryptionUtil.generateKey(sharedLink.getEncryptionKey());
                InputStream encryptedStream = storageService.load(sharedLink.getStorageId());
                InputStream stream = encryptionUtil.decryptStream(encryptedStream, key);

                logger.info(String.format("Download %s", sharedLink.getId()));

                return new FileDownload(stream, sharedLink.getFileName(), sharedLink.getFileType(), sharedLink.getFileSize());
            } catch(FileNotFoundException e) {
                logger.info(String.format("File for download %s is missing", sharedLink.getId()));
                throw new LinkExpiredException();
            } catch (NoSuchAlgorithmException|InvalidKeyException|NoSuchPaddingException|InvalidAlgorithmParameterException e) {
                throw new EncryptionException(e);
            }
        }
    }
}
