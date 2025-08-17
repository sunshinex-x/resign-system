package com.example.resign.model.vo;

import lombok.Data;

/**
 * 包信息视图对象
 */
@Data
public class PackageInfoVO {

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 包名/Bundle ID
     */
    private String packageName;

    /**
     * 版本号
     */
    private String versionName;

    /**
     * 版本代码
     */
    private String versionCode;

    /**
     * 应用图标URL
     */
    private String iconUrl;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 最小SDK版本
     */
    private String minSdkVersion;

    /**
     * 目标SDK版本
     */
    private String targetSdkVersion;

    /**
     * 签名信息
     */
    private String signature;

    /**
     * 权限列表
     */
    private java.util.List<String> permissions;
}