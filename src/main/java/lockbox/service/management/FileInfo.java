package lockbox.service.management;

public class FileInfo {

    private String fileName;
    private long fileSize;
    private boolean passwordProtected;

    FileInfo(String fileName, long fileSize, boolean passwordProtected) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.setPasswordProtected(passwordProtected);
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public boolean isPasswordProtected() {
        return passwordProtected;
    }

    public void setPasswordProtected(boolean passwordProtected) {
        this.passwordProtected = passwordProtected;
    }
}
