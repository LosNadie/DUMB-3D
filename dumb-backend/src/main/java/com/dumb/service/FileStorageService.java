package com.dumb.service;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {

    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_REMOTE_IMAGE_BYTES = 10L * 1024 * 1024;
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) Cursor-Dumb/1.0";

    private final HttpClient httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(60))
        .followRedirects(HttpClient.Redirect.NORMAL)
        .build();

    public String saveUploadedImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IOException("文件不能为空");
        }
        String originalFilename = file.getOriginalFilename();
        String ext = normalizeExtension(StringUtils.getFilenameExtension(originalFilename));
        if (!isSupportedExtension(ext)) {
            throw new IOException("仅支持 JPG/PNG/WEBP 格式");
        }
        Path target = prepareTargetPath(ext);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        return toPublicPath(target);
    }

    public String downloadRemoteImage(String imageUrl) throws IOException, InterruptedException {
        if (!StringUtils.hasText(imageUrl)) {
            throw new IOException("图片地址不能为空");
        }
        URI uri = URI.create(imageUrl.trim());
        String scheme = uri.getScheme();
        if (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme)) {
            throw new IOException("仅支持 HTTP/HTTPS 图片地址");
        }

        HttpRequest request = HttpRequest.newBuilder(uri)
            .timeout(Duration.ofSeconds(30))
            .header("Accept", "image/*")
            .header("User-Agent", USER_AGENT)
            .GET()
            .build();
        HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());
        if (response.statusCode() < 200 || response.statusCode() >= 300) {
            throw new IOException("下载图片失败，状态码: " + response.statusCode());
        }
        byte[] bytes = response.body();
        if (bytes == null || bytes.length == 0) {
            throw new IOException("图片内容为空");
        }
        if (bytes.length > MAX_REMOTE_IMAGE_BYTES) {
            throw new IOException("图片过大，超过 10MB");
        }
        String ext = resolveExtension(uri, response.headers().firstValue("Content-Type").orElse(""));
        if (!isSupportedExtension(ext)) {
            throw new IOException("不支持的图片格式");
        }
        Path target = prepareTargetPath(ext);
        Files.write(target, bytes);
        return toPublicPath(target);
    }

    private static Path prepareTargetPath(String ext) throws IOException {
        Path uploadPath = Paths.get(UPLOAD_DIR).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);
        String filename = UUID.randomUUID() + "." + ext;
        return uploadPath.resolve(filename);
    }

    private static String toPublicPath(Path target) {
        return "/uploads/" + target.getFileName();
    }

    private static boolean isSupportedExtension(String ext) {
        return StringUtils.hasText(ext) && ALLOWED_EXTENSIONS.contains(ext);
    }

    private static String normalizeExtension(String ext) {
        if (!StringUtils.hasText(ext)) {
            return "";
        }
        return ext.toLowerCase(Locale.ROOT).trim();
    }

    private static String resolveExtension(URI uri, String contentType) {
        String byContentType = extensionByContentType(contentType);
        if (StringUtils.hasText(byContentType)) {
            return byContentType;
        }
        String path = uri.getPath();
        return normalizeExtension(StringUtils.getFilenameExtension(path));
    }

    private static String extensionByContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "";
        }
        String ct = contentType.toLowerCase(Locale.ROOT);
        if (ct.contains("image/jpeg") || ct.contains("image/jpg")) {
            return "jpg";
        }
        if (ct.contains("image/png")) {
            return "png";
        }
        if (ct.contains("image/webp")) {
            return "webp";
        }
        return "";
    }
}
