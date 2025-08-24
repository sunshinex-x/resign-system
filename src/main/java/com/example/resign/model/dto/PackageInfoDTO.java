package com.example.resign.model.dto;

import lombok.Data;

import java.util.List;

/**
 * 应用包信息DTO
 */
@Data
public class PackageInfoDTO {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 包名/Bundle ID
     */
    private String packageName;

    /**
     * 版本名称
     */
    private String versionName;

    /**
     * 版本代码
     */
    private String versionCode;

    /**
     * 应用类型
     */
    private String appType;

    /**
     * Bundle ID列表（iOS可能有多个）
     */
    private List<String> bundleIds;

    /**
     * 最小SDK版本
     */
    private String minSdkVersion;

    /**
     * 目标SDK版本
     */
    private String targetSdkVersion;

    /**
     * 权限列表
     */
    private List<String> permissions;

    /**
     * 应用图标（Base64编码）
     */
    private String iconBase64;

    /**
     * 文件大小
     */
    private Long fileSize;

    /**
     * 原始签名信息
     */
    private String originalSignature;
}