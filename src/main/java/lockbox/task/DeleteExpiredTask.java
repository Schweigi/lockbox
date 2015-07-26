package lockbox.task;

import lockbox.domain.model.SharedLinkModel;
import lockbox.domain.repository.SharedLinkRepository;
import lockbox.service.storage.FileStorageService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class DeleteExpiredTask {

    private static final String MAX_AGE = "lockbox.files.maxage";
    private static final int ONE_HOUR = 3600 * 1000;

    @Autowired
    private Environment env;

    @Autowired
    private Logger logger;

    @Autowired
    private SharedLinkRepository sharedLinkRepository;

    @Autowired
    private FileStorageService storageService;

    @Scheduled(fixedRate = ONE_HOUR)
    public void deleteExpired() {
        logger.info("Run delete expired task");

        int maxAge = Integer.parseInt(env.getProperty(MAX_AGE));
        Instant expired = Instant.now().minus(maxAge, ChronoUnit.HOURS);
        List<SharedLinkModel> links = sharedLinkRepository.findByCreatedBefore(expired);

        for(SharedLinkModel link: links) {
            storageService.delete(link.getStorageId());
            sharedLinkRepository.delete(link.getId());
        }
    }
}
