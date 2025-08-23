package com.example.resign.controller;

import com.example.resign.service.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStream;

/**
 * 文件代理控制器
 * 用于代理访问MinIO中的文件，解决CORS和权限问题
 */
@Tag(name = "文件代理", description = "文件代理访问相关接口")
@Slf4j
@RestController
@RequestMapping("/api/files")
@RequiredArgsConstructor
public class FileProxyController {

    private final FileService fileService;

    /**
     * 代理访问头像文件
     */
    @Operation(summary = "获取头像文件", description = "代理访问用户头像文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文件获取成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    @GetMapping("/avatars/{fileName}")
    public ResponseEntity<byte[]> getAvatar(
            @Parameter(description = "头像文件名", example = "avatar.jpg") @PathVariable String fileName) {
        return getFile("avatars/" + fileName);
    }

    /**
     * 代理访问任意文件
     */
    @Operation(summary = "获取文件", description = "代理访问指定文件夹下的文件")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "文件获取成功"),
        @ApiResponse(responseCode = "404", description = "文件不存在")
    })
    @GetMapping("/{folder}/{fileName}")
    public ResponseEntity<byte[]> getFile(
            @Parameter(description = "文件夹名称", example = "certificates") @PathVariable String folder,
            @Parameter(description = "文件名", example = "cert.p12") @PathVariable String fileName) {
        return getFile(folder + "/" + fileName);
    }

    /**
     * 通用文件访问方法
     */
    private ResponseEntity<byte[]> getFile(String objectName) {
        try {
            log.info("代理访问文件: {}", objectName);
            
            // 从MinIO下载文件
            InputStream inputStream = fileService.downloadFile(objectName);
            byte[] fileBytes = inputStream.readAllBytes();
            inputStream.close();
            
            log.info("成功读取文件: {}, 大小: {} bytes", objectName, fileBytes.length);

            // 根据文件扩展名设置Content-Type
            String contentType = getContentType(objectName);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileBytes.length);
            
            // 设置缓存控制
            headers.setCacheControl("public, max-age=86400"); // 缓存1天
            
            return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
            
        } catch (Exception e) {
            log.error("代理访问文件失败: {}", objectName, e);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * 根据文件扩展名获取Content-Type
     */
    private String getContentType(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        
        return switch (extension) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "gif" -> "image/gif";
            case "webp" -> "image/webp";
            case "svg" -> "image/svg+xml";
            case "pdf" -> "application/pdf";
            case "txt" -> "text/plain";
            case "json" -> "application/json";
            default -> "application/octet-stream";
        };
    }
}