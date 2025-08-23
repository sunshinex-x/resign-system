package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Android重签名任务实体类
 */
@Data
@TableName("android_resign_task")
public class AndroidResignTask {

    /**
     * 任务ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务唯一标识
     */
    private String taskId;

    /**
     * 原始APK文件URL
     */
    private String originalApkUrl;

    /**
     * 使用的Android证书ID
     */
    private Long certificateId;

    /**
     * 应用名称（从APK解析）
     */
    private String appName;

    /**
     * 应用版本（从APK解析）
     */
    private String appVersion;

    /**
     * 包名（从APK解析）
     */
    private String packageName;

    /**
     * 应用权限信息（JSON格式）
     */
    private String permissions;

    /**
     * 重签名后的APK文件URL
     */
    private String resignedApkUrl;

    /**
     * 任务状态：PENDING, PROCESSING, SUCCESS, FAILED
     */
    private String status;

    /**
     * 失败原因
     */
    private String failReason;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 处理耗时（毫秒）
     */
    private Long processingTime;

    /**
     * 原始文件大小（字节）
     */
    private Long originalFileSize;

    /**
     * 重签名后文件大小（字节）
     */
    private Long resignedFileSize;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 更新人
     */
    private String updateBy;
}