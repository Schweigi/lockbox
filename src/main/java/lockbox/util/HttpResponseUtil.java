package lockbox.util;

import org.apache.commons.io.IOUtils;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

public class HttpResponseUtil {

    public void writeFile(HttpServletResponse response, InputStream fileStream, String fileName, String contentType, int fileSize) throws IOException {
        response.setContentType(contentType);
        response.setContentLength(fileSize);
        response.setHeader("content-disposition", "attachment; filename=" + fileName);
        IOUtils.copy(fileStream, response.getOutputStream());
        response.flushBuffer();
    }
}
