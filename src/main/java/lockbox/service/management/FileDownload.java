package lockbox.service.management;

import java.io.InputStream;

public class FileDownload {

    private InputStream stream;
    private String fileName;
    private String fileType;
    private long fileSize;

    FileDownload(InputStream stream, String fileName, String fileType, long fileSize) {
        this.stream = stream;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
    }

    public InputStream getStream() {
        return stream;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public long getFileSize() {
        return fileSize;
    }
}
