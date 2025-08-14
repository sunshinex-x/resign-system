package com.example.resign.config;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * MinIO初始化组件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MinioInitializer implements CommandLineRunner {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    @Override
    public void run(String... args) throws Exception {
        try {
            initializeBucket();
        } catch (Exception e) {
            log.error("MinIO初始化失败，将使用本地文件存储作为备用方案", e);
        }
    }

    /**
     * 初始化MinIO存储桶
     */
    private void initializeBucket() {
        try {
            String bucketName = minioConfig.getBucketName();
            log.info("开始初始化MinIO存储桶: {}", bucketName);

            // 检查存储桶是否存在
            boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );

            if (!bucketExists) {
                // 创建存储桶
                minioClient.makeBucket(
                    MakeBucketArgs.builder()
                        .bucket(bucketName)
                        .build()
                );
                log.info("MinIO存储桶创建成功: {}", bucketName);

                // 设置存储桶策略为公共读取
                setBucketPolicy(bucketName);
            } else {
                log.info("MinIO存储桶已存在: {}", bucketName);
            }

            // 测试连接
            testConnection(bucketName);
            
        } catch (Exception e) {
            log.error("MinIO存储桶初始化失败", e);
            throw new RuntimeException("MinIO初始化失败: " + e.getMessage());
        }
    }

    /**
     * 设置存储桶策略
     */
    private void setBucketPolicy(String bucketName) {
        try {
            // 设置公共读取策略
            String policy = """
                {
                  "Version": "2012-10-17",
                  "Statement": [
                    {
                      "Effect": "Allow",
                      "Principal": {
                        "AWS": ["*"]
                      },
                      "Action": ["s3:GetObject"],
                      "Resource": ["arn:aws:s3:::%s/*"]
                    }
                  ]
                }
                """.formatted(bucketName);

            minioClient.setBucketPolicy(
                SetBucketPolicyArgs.builder()
                    .bucket(bucketName)
                    .config(policy)
                    .build()
            );
            
            log.info("MinIO存储桶策略设置成功: {}", bucketName);
        } catch (Exception e) {
            log.warn("设置MinIO存储桶策略失败，文件可能无法公开访问: {}", e.getMessage());
        }
    }

    /**
     * 测试MinIO连接
     */
    private void testConnection(String bucketName) {
        try {
            boolean exists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                    .bucket(bucketName)
                    .build()
            );
            
            if (exists) {
                log.info("MinIO连接测试成功，存储桶可正常访问: {}", bucketName);
            } else {
                log.error("MinIO连接测试失败，存储桶不存在: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("MinIO连接测试失败", e);
        }
    }
}