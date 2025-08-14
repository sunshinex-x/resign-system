package com.example.resign.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 重签名任务创建DTO（文件上传）
 */
@Data
public class ResignTaskCreateDTO {

    /**
     * 应用类型
     */
    private String appType;

    /**
     * 原始安装包文件
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
     * 任务描述
     */
    private String description;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 描述文件列表（iOS）
     */
    private List<MultipartFile> profileFiles;

    /**
     * Bundle ID列表（iOS）
     */
    private List<String> bundleIds;
}