package lockbox.controller;

import lockbox.controller.model.ApiErrorModel;
import lockbox.controller.model.ApiSuccessModel;
import lockbox.service.management.FileDownload;
import lockbox.service.management.FileManagementService;
import lockbox.service.management.exception.InvalidPasswordException;
import lockbox.service.management.exception.LinkExpiredException;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
public class ApiController {

    @Autowired
    FileManagementService managementService;

    @RequestMapping(value = "/files", method = RequestMethod.POST)
    public ApiSuccessModel upload(@RequestParam(required = false) String password,
                                  @RequestParam("file") MultipartFile file,
                                  HttpServletRequest request) throws Exception {

        String publicId = managementService.upload(file.getInputStream(), file.getOriginalFilename(), file.getContentType(), file.getSize(), password);
        return new ApiSuccessModel(request.getRequestURL()+"/"+publicId);
    }

    @RequestMapping(value = "/files/{publicId:[a-z0-9]{16}}", method = RequestMethod.GET)
    public Object download(@PathVariable String publicId,
                           @RequestHeader(value = "Authorization", required = false) String password,
                           HttpServletResponse response) throws Exception {
        try {
            FileDownload fileDownload = managementService.download(publicId, password);
            response.setContentType(fileDownload.getFileType());
            response.setContentLength((int) fileDownload.getFileSize());
            response.setHeader("content-disposition", "attachment; filename=" + fileDownload.getFileName());
            IOUtils.copy(fileDownload.getStream(), response.getOutputStream());
            response.flushBuffer();
            return null;
        } catch(InvalidPasswordException e) {
            return new ApiErrorModel("Invalid Password");
        } catch(LinkExpiredException e) {
            return new ApiErrorModel("Expired");
        }
    }
}
