package lockbox.service.storage;

import com.mongodb.gridfs.GridFSDBFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.InputStream;

@Service
public class MongoDbFileStorageService implements FileStorageService {

    @Autowired
    private GridFsOperations operations;

    @Override
    public String save(InputStream inputStream) {
        return operations.store(inputStream, (Object)null).getId().toString();
    }

    @Override
    public InputStream load(String id) throws FileNotFoundException {
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        GridFSDBFile file = operations.findOne(query);

        if (file == null) {
            throw new FileNotFoundException();
        } else {
            return file.getInputStream();
        }
    }

    @Override
    public void delete(String id) {
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        operations.delete(query);
    }
}
