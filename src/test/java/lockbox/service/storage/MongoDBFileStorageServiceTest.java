package lockbox.service.storage;

import com.mongodb.DBObject;
import com.mongodb.gridfs.GridFSDBFile;
import lockbox.Application;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import java.io.*;
import java.nio.charset.Charset;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
public class MongoDBFileStorageServiceTest {

    private static final String EXPECTED = "Hello World";

    @Autowired
    MongoDbFileStorageService fileStorageService;

    @Autowired
    private GridFsOperations operations;

    @Test
    public void storeFile() throws IOException {
        // Prepare
        InputStream inputStream = IOUtils.toInputStream(EXPECTED, Charset.forName("UTF-8"));

        // Execute
        String id = fileStorageService.save(inputStream);

        // Assert
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        GridFSDBFile file = operations.findOne(query);
        Assert.assertNotNull(file);

        String actual = IOUtils.toString(file.getInputStream(), Charset.forName("UTF-8"));
        Assert.assertEquals(EXPECTED, actual);
    }

    @Test
    public void loadExistingFile() throws IOException {
        // Prepare
        InputStream inputStream = IOUtils.toInputStream(EXPECTED, Charset.forName("UTF-8"));
        String id = operations.store(inputStream, (DBObject)null).getId().toString();

        // Execute
        InputStream fileStream = fileStorageService.load(id);

        // Assert
        String actual = IOUtils.toString(fileStream, Charset.forName("UTF-8"));
        Assert.assertEquals(EXPECTED, actual);
    }

    @Test(expected = FileNotFoundException.class)
    public void loadNonExistingFile() throws FileNotFoundException {
        fileStorageService.load("empty");
    }

    @Test
    public void deleteExistingFile() throws FileNotFoundException {
        // Prepare
        InputStream inputStream = IOUtils.toInputStream(EXPECTED, Charset.forName("UTF-8"));
        String id = operations.store(inputStream, (DBObject)null).getId().toString();

        // Execute
        fileStorageService.delete(id);

        // Assert
        Query query = new Query().addCriteria(Criteria.where("_id").is(id));
        GridFSDBFile file = operations.findOne(query);
        Assert.assertNull(file);
    }

    @Test
    public void deleteNonExistingFile() throws FileNotFoundException {
        fileStorageService.delete("empty");
    }
}
