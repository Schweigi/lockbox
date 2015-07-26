package lockbox.controller;

import lockbox.service.management.FileDownload;
import lockbox.service.management.FileManagementService;
import lockbox.service.management.exception.EncryptionException;
import lockbox.service.management.exception.InvalidPasswordException;
import lockbox.service.management.exception.LinkExpiredException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
public class FileHandlerController {

    @Autowired
    FileManagementService managementService;

    @RequestMapping(value = "/files", method = RequestMethod.POST)
    public void addFile(@RequestParam(required = false) String password,
                        @RequestParam("file") MultipartFile file) throws Exception {

        String publicId = managementService.upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize(), password);
        System.out.println(publicId);
    }

    @RequestMapping(value = "/files/{publicId}", method = RequestMethod.GET)
    public void getFile(@PathVariable String publicId,
                        @RequestHeader(value = "Authorization", required = false) String password,
                        HttpServletResponse response) throws Exception {
        try {
            prepareFileDownload(response, publicId, password);
        } catch(InvalidPasswordException e) {

        } catch(LinkExpiredException e) {

        }
    }

    @RequestMapping(value = "/files/{publicId}", method = RequestMethod.POST)
    public void getFileFromForm(@PathVariable String publicId,
                                @RequestParam(required = false) String password,
                                HttpServletResponse response) throws Exception {
        try {
            prepareFileDownload(response, publicId, password);
        } catch(InvalidPasswordException e) {

        } catch(LinkExpiredException e) {

        }
    }

    private void prepareFileDownload(HttpServletResponse response, String publicId, String password) throws IOException, LinkExpiredException, InvalidPasswordException, EncryptionException {
        FileDownload fileDownload = managementService.download(publicId, password);
        response.setContentType(fileDownload.getFileType());
        response.setContentLength((int) fileDownload.getFileSize());
        response.setHeader("content-disposition", "attachment; filename=" + fileDownload.getFileName());
        IOUtils.copy(fileDownload.getStream(), response.getOutputStream());
        response.flushBuffer();
    }
}
