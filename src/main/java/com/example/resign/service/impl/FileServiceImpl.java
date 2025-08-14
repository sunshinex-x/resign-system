package com.example.resign.service.impl;

import cn.hutool.core.io.IoUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpUtil;
import com.example.resign.config.MinioConfig;
import com.example.resign.service.FileService;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType) {
        try {
            // 检查存储桶是否存在，不存在则创建
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder()
                        .bucket(minioConfig.getBucketName())
                        .build());
            }

            // 设置文件元数据
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", contentType);

            // 上传文件
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .stream(inputStream, -1, 10485760)
                    .contentType(contentType)
                    .headers(headers)
                    .build());

            // 返回文件访问URL
            return getFileUrl(objectName);
        } catch (Exception e) {
            log.error("上传文件到MinIO失败", e);
            throw new RuntimeException("上传文件失败: " + e.getMessage());
        } finally {
            IoUtil.close(inputStream);
        }
    }

    @Override
    public InputStream downloadFileFromUrl(String fileUrl) {
        try {
            // 从URL下载文件
            byte[] bytes = HttpUtil.downloadBytes(fileUrl);
            return new ByteArrayInputStream(bytes);
        } catch (Exception e) {
            log.error("从URL下载文件失败: {}", fileUrl, e);
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public InputStream downloadFile(String objectName) {
        try {
            // 从MinIO下载文件
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            log.error("从MinIO下载文件失败: {}", objectName, e);
            throw new RuntimeException("下载文件失败: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String objectName) {
        try {
            // 删除MinIO中的文件
            minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .build());
            return true;
        } catch (Exception e) {
            log.error("删除MinIO文件失败: {}", objectName, e);
            return false;
        }
    }

    @Override
    public String getFileUrl(String objectName) {
        try {
            String urlMode = minioConfig.getUrlMode();
            String fileUrl;
            
            switch (urlMode) {
                case "direct":
                    // 直接访问MinIO URL（需要MinIO配置公共访问权限）
                    fileUrl = minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;
                    log.debug("生成直接访问URL: {}", fileUrl);
                    break;
                    
                case "presigned":
                    // 预签名URL（适用于需要临时访问权限的场景）
                    fileUrl = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .method(Method.GET)
                            .expiry(7 * 24 * 60 * 60)
                            .build());
                    log.debug("生成预签名URL: {}", fileUrl);
                    break;
                    
                case "proxy":
                default:
                    // 使用后端代理访问（推荐，解决CORS问题）
                    fileUrl = "/api/files/" + objectName;
                    log.debug("生成代理访问URL: {}", fileUrl);
                    break;
            }
            
            return fileUrl;
        } catch (Exception e) {
            log.error("获取文件URL失败: {}", objectName, e);
            throw new RuntimeException("获取文件URL失败: " + e.getMessage());
        }
    }

    @Override
    public String getObjectNameFromUrl(String fileUrl) {
        // 从URL中提取对象名称
        if (StrUtil.isBlank(fileUrl)) {
            return null;
        }

        try {
            // 如果是MinIO的预签名URL，需要解析出对象名称
            if (fileUrl.contains(minioConfig.getEndpoint())) {
                // MinIO预签名URL格式: http://localhost:9000/bucket-name/object-name?X-Amz-Algorithm=...
                String urlWithoutParams = fileUrl.split("\\?")[0]; // 移除查询参数
                String bucketPrefix = "/" + minioConfig.getBucketName() + "/";
                
                int bucketIndex = urlWithoutParams.indexOf(bucketPrefix);
                if (bucketIndex != -1) {
                    // 提取bucket名称后的完整对象路径
                    return urlWithoutParams.substring(bucketIndex + bucketPrefix.length());
                }
            }
            
            // 如果无法解析，返回null
            log.warn("无法从URL中提取对象名称: {}", fileUrl);
            return null;
        } catch (Exception e) {
            log.error("解析对象名称失败: {}", fileUrl, e);
            return null;
        }
    }
}