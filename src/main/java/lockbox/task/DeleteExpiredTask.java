package lockbox.task;

import lockbox.domain.model.SharedLinkModel;
import lockbox.domain.repository.SharedLinkRepository;
import lockbox.service.storage.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DeleteExpiredTask {

    public static final String MAX_AGE = "lockbox.files.maxage";

    @Autowired
    Environment env;

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileStorageService storageService;

    @Scheduled(fixedRate = 3600 * 1000)
    public void deleteExpired() {
        int maxAge = Integer.parseInt(env.getProperty(MAX_AGE));
        Instant expired = Instant.now().minus(maxAge, ChronoUnit.HOURS);
        List<SharedLinkModel> links = sharedLinkRepository.findByCreatedBefore(expired);

        for(SharedLinkModel link: links) {
            storageService.delete(link.getStorageId());
            sharedLinkRepository.delete(link.getId());
        }
    }
}
