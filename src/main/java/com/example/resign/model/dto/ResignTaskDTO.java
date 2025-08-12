package com.example.resign.model.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 重签名任务数据传输对象
 */
@Data
public class ResignTaskDTO {

    /**
     * 应用类型：IOS、ANDROID、HARMONY
     */
    @NotBlank(message = "应用类型不能为空")
    private String appType;

    /**
     * 原始安装包URL
     */
    @NotBlank(message = "原始安装包URL不能为空")
    private String originalPackageUrl;

    /**
     * 证书URL
     */
    @NotBlank(message = "证书URL不能为空")
    private String certificateUrl;

    /**
     * 证书密码
     */
    private String certificatePassword;

    /**
     * 回调URL
     */
    private String callbackUrl;
}