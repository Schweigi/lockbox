package lockbox.domain.repository;

import lockbox.domain.model.SharedLinkModel;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.Instant;
import java.util.List;

public interface SharedLinkRepository extends MongoRepository<SharedLinkModel, String> {

    SharedLinkModel findByPublicId(String publicId);
    List<SharedLinkModel> findByCreatedBefore(Instant created);
}
