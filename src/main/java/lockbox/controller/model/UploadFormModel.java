package lockbox.controller.model;

import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;

public class UploadFormModel {

    @NotNull
    private MultipartFile file;

    private String password;


    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
