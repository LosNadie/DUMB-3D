package com.dumb.controller.admin;

import com.dumb.common.enums.ResultCodeEnum;
import com.dumb.common.result.ApiResult;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/files")
public class AdminFileController {
    private static final String UPLOAD_DIR = "uploads";
    private static final String JPG = "jpg";
    private static final String JPEG = "jpeg";
    private static final String PNG = "png";

    @PostMapping("/upload")
    public ApiResult<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return ApiResult.fail(ResultCodeEnum.PARAM_ERROR, "文件不能为空");
        }

        String originalFilename = file.getOriginalFilename();
        String ext = StringUtils.getFilenameExtension(originalFilename);
        if (ext == null) {
            return ApiResult.fail(ResultCodeEnum.PARAM_ERROR, "仅支持 JPG/PNG 格式");
        }

        String lowerExt = ext.toLowerCase(Locale.ROOT);
        boolean supported = JPG.equals(lowerExt) || JPEG.equals(lowerExt) || PNG.equals(lowerExt);
        if (!supported) {
            return ApiResult.fail(ResultCodeEnum.PARAM_ERROR, "仅支持 JPG/PNG 格式");
        }

        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String newFilename = UUID.randomUUID() + "." + lowerExt;
        Path target = uploadPath.resolve(newFilename);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        return ApiResult.success("/uploads/" + newFilename);
    }
}
