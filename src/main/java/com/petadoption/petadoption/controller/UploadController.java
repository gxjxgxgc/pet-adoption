package com.petadoption.petadoption.controller;

import com.petadoption.petadoption.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@RestController
@RequestMapping("/upload")
@Tag(name = "文件上传", description = "文件上传相关接口")
public class UploadController {

    @Value("${upload.path:C:/Users/user/Desktop/IDEA/pet-adoption/uploads}")
    private String uploadPath;

    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>(Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    ));

    private long getMaxFileSize() {
        Object val = AdminController.getSetting("maxFileSize");
        if (val instanceof Number) {
            return ((Number) val).longValue() * 1024 * 1024;
        }
        return 5242880L;
    }

    private int getMaxFileCount() {
        Object val = AdminController.getSetting("maxFileCount");
        if (val instanceof Number) {
            return ((Number) val).intValue();
        }
        return 5;
    }

    @PostMapping
    @Operation(summary = "上传文件", description = "上传图片文件，支持JPEG、PNG、GIF、WEBP格式")
    public ResponseEntity<ApiResponse<Map<String, String>>> uploadFile(
            @RequestParam("file") MultipartFile file) {
        
        if (file.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "上传文件不能为空"));
        }

        if (file.getSize() > getMaxFileSize()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "文件大小超过限制，最大允许" + (getMaxFileSize() / 1024 / 1024) + "MB"));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "不支持的文件类型，仅支持JPEG、PNG、GIF、WEBP格式"));
        }

        try {
            String originalFilename = file.getOriginalFilename();
            String fileExtension = getFileExtension(originalFilename);
            String newFilename = generateFilename(fileExtension);
            
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath();
            log.info("上传目录绝对路径: {}", uploadDir);
            
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
                log.info("创建上传目录: {}", uploadDir);
            }
            
            Path filePath = uploadDir.resolve(newFilename);
            file.transferTo(filePath.toFile());
            
            String fileUrl = "/uploads/" + newFilename;
            
            Map<String, String> result = new HashMap<>();
            result.put("url", fileUrl);
            result.put("filename", newFilename);
            result.put("originalFilename", originalFilename);
            result.put("size", String.valueOf(file.getSize()));
            
            log.info("文件上传成功: {} -> {}", originalFilename, filePath);
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "文件上传失败: " + e.getMessage()));
        }
    }

    @PostMapping("/multiple")
    @Operation(summary = "批量上传文件", description = "批量上传多个图片文件")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> uploadMultipleFiles(
            @RequestParam("files") MultipartFile[] files) {
        
        if (files == null || files.length == 0) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "上传文件不能为空"));
        }

        if (files.length > getMaxFileCount()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error(400, "最多同时上传" + getMaxFileCount() + "个文件"));
        }

        List<Map<String, String>> results = new ArrayList<>();
        
        try {
            Path uploadDir = Paths.get(uploadPath).toAbsolutePath();
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
            
            for (MultipartFile file : files) {
                if (!file.isEmpty()) {
                    try {
                        String originalFilename = file.getOriginalFilename();
                        String fileExtension = getFileExtension(originalFilename);
                        String newFilename = generateFilename(fileExtension);
                        
                        Path filePath = uploadDir.resolve(newFilename);
                        file.transferTo(filePath.toFile());
                        
                        String fileUrl = "/uploads/" + newFilename;
                        
                        Map<String, String> fileInfo = new HashMap<>();
                        fileInfo.put("url", fileUrl);
                        fileInfo.put("filename", newFilename);
                        fileInfo.put("originalFilename", originalFilename);
                        fileInfo.put("size", String.valueOf(file.getSize()));
                        
                        results.add(fileInfo);
                        
                    } catch (IOException e) {
                        log.error("文件上传失败: {}", file.getOriginalFilename(), e);
                    }
                }
            }
            
            return ResponseEntity.ok(ApiResponse.success(results));
            
        } catch (IOException e) {
            log.error("创建上传目录失败", e);
            return ResponseEntity.internalServerError()
                    .body(ApiResponse.error(500, "创建上传目录失败: " + e.getMessage()));
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }

    private String generateFilename(String extension) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        return timestamp + "_" + uuid + extension;
    }
}
