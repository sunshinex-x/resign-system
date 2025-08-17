package com.example.resign.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

/**
 * 重签名任务创建DTO（新版本）
 */
@Data
public class ResignTaskCreateV2DTO {
    
    /**
     * 原始包文件
     */
    private MultipartFile originalPackageFile;
    
    /**
     * 证书文件
     */
    private MultipartFile certificateFile;
    
    /**
     * 证书密码
     */
    private String certificatePassword;
    
    /**
     * Bundle ID和Profile文件映射
     * key: bundleId, value: profile文件
     */
    private Map<String, MultipartFile> bundleProfileMap;
    
    /**
     * 回调地址
     */
    private String callbackUrl;
    
    /**
     * 任务描述
     */
    private String description;
}