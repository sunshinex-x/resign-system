package com.example.resign.controller;

import com.example.resign.config.MinioConfig;
import com.example.resign.model.common.Result;
import io.minio.BucketExistsArgs;
import io.minio.GetObjectArgs;
import io.minio.ListObjectsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.messages.Item;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.io.InputStream;

/**
 * MinIO管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/minio")
@RequiredArgsConstructor
public class MinioController {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 检查MinIO连接状态
     */
    @GetMapping("/health")
    public Result<Map<String, Object>> checkHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String bucketName = minioConfig.getBucketName();
            
            // 检查存储桶是否存在
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            result.put("connected", true);
            result.put("endpoint", minioConfig.getEndpoint());
            result.put("bucketName", bucketName);
            result.put("bucketExists", bucketExists);
            
            if (bucketExists) {
                // 统计文件数量
                int fileCount = 0;
                try {
                    Iterable<io.minio.Result<Item>> objects = minioClient.listObjects(
                        ListObjectsArgs.builder()
                            .bucket(bucketName)
                            .maxKeys(1000)
                            .build()
                    );
                    
                    for (io.minio.Result<Item> object : objects) {
                        fileCount++;
                    }
                } catch (Exception e) {
                    log.warn("统计文件数量失败: {}", e.getMessage());
                }
                
                result.put("fileCount", fileCount);
            }
            
            return Result.success(result);
            
        } catch (Exception e) {
            log.error("MinIO健康检查失败", e);
            result.put("connected", false);
            result.put("error", e.getMessage());
            return Result.errorWithData("MinIO连接失败: " + e.getMessage(), result);
        }
    }

    /**
     * 列出存储桶中的文件
     */
    @GetMapping("/files")
    public Result<List<Map<String, Object>>> listFiles() {
        try {
            String bucketName = minioConfig.getBucketName();
            List<Map<String, Object>> files = new ArrayList<>();
            
            Iterable<io.minio.Result<Item>> objects = minioClient.listObjects(
                ListObjectsArgs.builder()
                    .bucket(bucketName)
                    .maxKeys(100)
                    .build()
            );
            
            for (io.minio.Result<Item> result : objects) {
                Item item = result.get();
                Map<String, Object> fileInfo = new HashMap<>();
                fileInfo.put("objectName", item.objectName());
                fileInfo.put("size", item.size());
                
                // 安全处理lastModified，避免空指针异常
                try {
                    fileInfo.put("lastModified", item.lastModified() != null ? 
                        item.lastModified() : null);
                } catch (Exception e) {
                    log.warn("获取文件修改时间失败: {}", item.objectName());
                    fileInfo.put("lastModified", null);
                }
                
                fileInfo.put("etag", item.etag());
                files.add(fileInfo);
            }
            
            return Result.success(files);
            
        } catch (Exception e) {
            log.error("列出文件失败", e);
            return Result.error("获取文件列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取MinIO配置信息
     */
    @GetMapping("/config")
    public Result<Map<String, Object>> getConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", minioConfig.getEndpoint());
        config.put("bucketName", minioConfig.getBucketName());
        config.put("accessKey", minioConfig.getAccessKey());
        config.put("urlMode", minioConfig.getUrlMode());
        // 不返回secretKey，保护敏感信息
        return Result.success(config);
    }

    /**
     * 测试文件下载
     */
    @GetMapping("/test-download/{objectName}")
    public Result<Map<String, Object>> testDownload(@PathVariable String objectName) {
        try {
            // 替换路径分隔符
            String fullObjectName = objectName.replace("-", "/");
            log.info("测试下载文件: {}", fullObjectName);
            
            // 检查文件是否存在
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build()
            );
            
            if (!exists) {
                return Result.error("存储桶不存在");
            }
            
            // 尝试下载文件
            try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(fullObjectName)
                    .build()
            )) {
                byte[] data = inputStream.readAllBytes();
                
                Map<String, Object> result = new HashMap<>();
                result.put("objectName", fullObjectName);
                result.put("fileSize", data.length);
                result.put("exists", true);
                result.put("downloadUrl", "/api/files/" + fullObjectName);
                
                return Result.success(result);
            }
            
        } catch (Exception e) {
            log.error("测试下载文件失败: {}", objectName, e);
            return Result.error("下载测试失败: " + e.getMessage());
        }
    }

    /**
     * 手动创建存储桶
     */
    @PostMapping("/create-bucket")
    public Result<String> createBucket() {
        try {
            String bucketName = minioConfig.getBucketName();
            
            // 检查存储桶是否已存在
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (bucketExists) {
                return Result.success("存储桶已存在: " + bucketName);
            }
            
            // 创建存储桶
            minioClient.makeBucket(
                MakeBucketArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            log.info("手动创建MinIO存储桶成功: {}", bucketName);
            return Result.success("存储桶创建成功: " + bucketName);
            
        } catch (Exception e) {
            log.error("手动创建存储桶失败", e);
            return Result.error("创建存储桶失败: " + e.getMessage());
        }
    }
}