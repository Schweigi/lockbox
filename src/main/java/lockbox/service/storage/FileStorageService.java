package lockbox.service.storage;

import java.io.FileNotFoundException;
import java.io.InputStream;

public interface FileStorageService {

    String save(InputStream inputStream);
    InputStream load(String id) throws FileNotFoundException;
    void delete(String id);
}
