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
            // 获取文件的预签名URL，有效期7天
            return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .bucket(minioConfig.getBucketName())
                    .object(objectName)
                    .method(Method.GET)
                    .expiry(7 * 24 * 60 * 60)
                    .build());
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

        // 如果是MinIO的URL，直接从路径中提取对象名称
        if (fileUrl.contains(minioConfig.getEndpoint())) {
            String[] parts = fileUrl.split("/");
            // 最后一个部分是对象名称
            return parts[parts.length - 1];
        }

        // 如果是外部URL，生成一个唯一的对象名称
        String extension = StrUtil.subAfter(fileUrl, ".", true);
        return IdUtil.fastSimpleUUID() + "." + extension;
    }
}