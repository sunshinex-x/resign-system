package com.example.resign.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件上传配置
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "file.upload")
public class FileUploadConfig {

    /**
     * 头像文件最大大小（字节）
     */
    private long avatarMaxSize = 2 * 1024 * 1024; // 2MB

    /**
     * 普通文件最大大小（字节）
     */
    private long fileMaxSize = 10 * 1024 * 1024; // 10MB

    /**
     * 安装包文件最大大小（字节）
     */
    private long packageMaxSize = 500 * 1024 * 1024; // 500MB

    /**
     * 允许的图片文件类型
     */
    private String[] allowedImageTypes = {
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    };

    /**
     * 允许的安装包文件类型
     */
    private String[] allowedPackageTypes = {
        "application/vnd.android.package-archive", // APK
        "application/octet-stream", // IPA, HAP
        "application/zip"
    };

    /**
     * 获取格式化的文件大小
     */
    public String getFormattedSize(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return String.format("%.2f KB", bytes / 1024.0);
        } else {
            return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
        }
    }

    /**
     * 检查是否为允许的图片类型
     */
    public boolean isAllowedImageType(String contentType) {
        if (contentType == null) return false;
        for (String type : allowedImageTypes) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为允许的安装包类型
     */
    public boolean isAllowedPackageType(String contentType) {
        if (contentType == null) return false;
        for (String type : allowedPackageTypes) {
            if (type.equals(contentType)) {
                return true;
            }
        }
        return false;
    }
}