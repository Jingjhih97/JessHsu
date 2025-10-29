package com.works.JessHsu.controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder; // ⭐ 新增

import net.coobird.thumbnailator.Thumbnails;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

  private final String uploadDir = "uploads";

  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public UploadResp upload(@RequestPart("file") MultipartFile file) throws Exception {
    if (file.isEmpty()) throw new IllegalArgumentException("空檔案");
    if (file.getContentType() == null || !file.getContentType().startsWith("image/")) {
      throw new IllegalArgumentException("只允許上傳圖片");
    }

    Files.createDirectories(Path.of(uploadDir));
    String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
    if (ext == null || ext.isBlank()) ext = "jpg";

    String base = "%d-%s".formatted(Instant.now().toEpochMilli(), UUID.randomUUID());

    // 1) 存原檔
    String originalName = base + "." + ext;
    Path originalPath = Path.of(uploadDir, originalName);
    Files.copy(file.getInputStream(), originalPath, StandardCopyOption.REPLACE_EXISTING);

    // 2) web 版
    String webName = base + "-web." + ext;
    Path webPath = Path.of(uploadDir, webName);
    try (var in = Files.newInputStream(originalPath); var out = Files.newOutputStream(webPath)) {
      Thumbnails.of(in).size(1600, 1600).outputQuality(0.85).toOutputStream(out);
    }

    // 3) 縮圖
    String thumbName = base + "-thumb." + ext;
    Path thumbPath = Path.of(uploadDir, thumbName);
    try (var in = Files.newInputStream(originalPath); var out = Files.newOutputStream(thumbPath)) {
      Thumbnails.of(in).size(400, 400).outputQuality(0.8).toOutputStream(out);
    }

    // 4) 建立「相對路徑」
    String originalRel  = "/uploads/" + originalName;
    String webRel       = "/uploads/" + webName;
    String thumbnailRel = "/uploads/" + thumbName;

    // 5) 轉為「絕對 URL」
    String originalUrl  = ServletUriComponentsBuilder.fromCurrentContextPath().path(originalRel).toUriString();
    String url          = ServletUriComponentsBuilder.fromCurrentContextPath().path(webRel).toUriString();
    String thumbnailUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path(thumbnailRel).toUriString();

    return new UploadResp(url, originalUrl, thumbnailUrl);
  }

  public record UploadResp(String url, String originalUrl, String thumbnailUrl) {}
}