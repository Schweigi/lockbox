package lockbox.controller;


import lockbox.controller.model.UploadFormModel;
import lockbox.service.management.FileDownload;
import lockbox.service.management.FileInfo;
import lockbox.service.management.FileManagementService;
import lockbox.service.management.exception.InvalidPasswordException;
import lockbox.service.management.exception.LinkExpiredException;
import lockbox.util.HttpResponseUtil;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class WebController extends WebMvcConfigurerAdapter {

    @Autowired
    FileManagementService managementService;

    @Autowired
    HttpResponseUtil httpResponseUtil;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String showUpload(UploadFormModel uploadFormModel) {
        return "upload";
    }

    @RequestMapping(value = "/", method = RequestMethod.POST)
    public String uploadFile(@Valid UploadFormModel uploadFormModel, Model model, BindingResult bindingResult) throws Exception {
        if (bindingResult.hasErrors()) {
            return "upload";
        } else if (uploadFormModel.getFile().isEmpty()) {
            bindingResult.addError(new ObjectError("file", "No file specified"));
            return "upload";
        } else {
            MultipartFile file = uploadFormModel.getFile();
            String publicId = managementService.upload(
                    file.getInputStream(),
                    file.getOriginalFilename(),
                    file.getContentType(),
                    file.getSize(),
                    uploadFormModel.getPassword());

            model.addAttribute("publicId", publicId);
            return "success";
        }
    }

    @RequestMapping(value = "/{publicId:[a-z0-9]{16}}", method = RequestMethod.GET)
    public String showDownload(@PathVariable String publicId, Model model) {
        try {
            FileInfo fileInfo = managementService.info(publicId);
            model.addAttribute("file", fileInfo);
            return "download";
        } catch(LinkExpiredException e) {
            return "expired";
        }
    }

    @RequestMapping(value = "/{publicId:[a-z0-9]{16}}", method = RequestMethod.POST)
    public String download(@PathVariable String publicId,
                         @RequestParam(required = false) String password,
                         Model model,
                         HttpServletResponse response) throws Exception {
        try {
            FileDownload fileDownload = managementService.download(publicId, password);
            httpResponseUtil.writeFile(
                    response,
                    fileDownload.getStream(),
                    fileDownload.getFileName(),
                    fileDownload.getFileType(),
                    (int) fileDownload.getFileSize());

            return null;
        } catch(InvalidPasswordException e) {
            FileInfo fileInfo = managementService.info(publicId);
            model.addAttribute("file", fileInfo);
            model.addAttribute("pwError", true);
            return "download";
        } catch(LinkExpiredException e) {
            return "expired";
        }
    }
}
