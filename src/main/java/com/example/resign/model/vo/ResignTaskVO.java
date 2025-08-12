package com.example.resign.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重签名任务视图对象
 */
@Data
public class ResignTaskVO {

    /**
     * 任务唯一标识
     */
    private String taskId;

    /**
     * 应用类型：IOS、ANDROID、HARMONY
     */
    private String appType;

    /**
     * 原始安装包URL
     */
    private String originalPackageUrl;

    /**
     * 重签名后的安装包URL
     */
    private String resignedPackageUrl;

    /**
     * 任务状态：PENDING、PROCESSING、SUCCESS、FAILED
     */
    private String status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}