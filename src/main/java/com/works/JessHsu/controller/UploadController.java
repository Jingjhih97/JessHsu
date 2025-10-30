package com.works.JessHsu.controller;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import net.coobird.thumbnailator.Thumbnails;
import net.coobird.thumbnailator.tasks.UnsupportedFormatException;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

  private final String uploadDir = "uploads";
  // 只允許常見、穩定格式，避免 HEIC/WEBP
  private static final List<String> ALLOWED_TYPES = List.of(
      "image/jpeg", "image/jpg", "image/png", "image/gif"
  );

  public record UploadResp(String url, String originalUrl, String thumbnailUrl) {}

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UploadResp upload(@RequestPart("file") @NotNull MultipartFile file) throws Exception {
    if (file.isEmpty()) throw new IllegalArgumentException("空檔案");
    var contentType = file.getContentType();
    if (contentType == null || ALLOWED_TYPES.stream().noneMatch(contentType::equalsIgnoreCase)) {
      throw new IllegalArgumentException("僅支援上傳 JPG / PNG / GIF 圖片");
    }

    Files.createDirectories(Path.of(uploadDir));

    // 原始副檔名（原檔保留原始），導出版本一律 jpg
    String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (ext == null || ext.isBlank()) ext = "jpg";
    ext = ext.toLowerCase();

    String base = "%d-%s".formatted(Instant.now().toEpochMilli(), UUID.randomUUID());

    // 1) 存原檔（保留原格式）
    String originalName = base + "." + ext;
    Path originalPath = Path.of(uploadDir, originalName);
    try (InputStream in = file.getInputStream()) {
      Files.copy(in, originalPath, StandardCopyOption.REPLACE_EXISTING);
    }

    // 2) 產 web 版（固定輸出 jpg）
    String webName = base + "-web.jpg";
    Path webPath = Path.of(uploadDir, webName);
    try (InputStream in = Files.newInputStream(originalPath)) {
      Thumbnails.of(in)
          .size(1600, 1600)
          .outputFormat("jpg")
          .outputQuality(0.85)
          .toFile(webPath.toFile());
    } catch (UnsupportedFormatException e) {
      // 刪掉無效原檔，回報更友善訊息
      Files.deleteIfExists(originalPath);
      throw new IllegalArgumentException("圖片格式不支援或檔案已損毀，請改用 JPG / PNG / GIF。");
    }

    // 3) 產縮圖（固定輸出 jpg）
    String thumbName = base + "-thumb.jpg";
    Path thumbPath = Path.of(uploadDir, thumbName);
    try (InputStream in = Files.newInputStream(originalPath)) {
      Thumbnails.of(in)
          .size(400, 400)
          .outputFormat("jpg")
          .outputQuality(0.8)
          .toFile(thumbPath.toFile());
    }

    // 4) 相對路徑
    String originalRel  = "/uploads/" + originalName;
    String webRel       = "/uploads/" + webName;
    String thumbnailRel = "/uploads/" + thumbName;

    // 5) 絕對 URL 回前端
    String originalUrl  = ServletUriComponentsBuilder.fromCurrentContextPath().path(originalRel).toUriString();
    String url          = ServletUriComponentsBuilder.fromCurrentContextPath().path(webRel).toUriString();
    String thumbnailUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(thumbnailRel).toUriString();

    return new UploadResp(url, originalUrl, thumbnailUrl);
  }
}