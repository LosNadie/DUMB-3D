package com.dumb.controller.admin;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.result.ApiResult;
import com.dumb.service.FileStorageService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {
    private final FileStorageService fileStorageService;

    public AdminFileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ApiResult<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            return ApiResult.success(fileStorageService.saveUploadedImage(file));
        } catch (IOException e) {
            String msg = e.getMessage() == null ? "封面上传失败" : e.getMessage();
            if (msg.contains("支持")) {
                return ApiResult.fail(ResultCodeEnum.PARAM_ERROR, msg);
            }
            if (msg.contains("不能为空")) {
                return ApiResult.fail(ResultCodeEnum.PARAM_ERROR, msg);
            }
            return ApiResult.fail(ResultCodeEnum.SERVER_ERROR, msg);
        }
    }
}
