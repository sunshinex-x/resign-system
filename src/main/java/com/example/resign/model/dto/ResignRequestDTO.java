package com.example.resign.model.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * 重签名请求DTO
 */
@Data
public class ResignRequestDTO {

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 应用类型
     */
    private String appType;

    /**
     * 原始应用包文件
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
     * Profile文件列表（iOS和鸿蒙）
     */
    private List<MultipartFile> profileFiles;

    /**
     * Bundle ID与Profile文件的映射
     */
    private Map<String, MultipartFile> bundleIdProfileMap;

    /**
     * Bundle ID列表
     */
    private List<String> bundleIds;

    /**
     * 输出文件名
     */
    private String outputFileName;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;
}