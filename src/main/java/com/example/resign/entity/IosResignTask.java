package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * iOS重签名任务实体类
 */
@Data
@TableName("ios_resign_task")
public class IosResignTask {

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
     * 原始IPA文件URL
     */
    private String originalIpaUrl;

    /**
     * 使用的iOS证书ID
     */
    private Long certificateId;

    /**
     * Bundle ID配置信息（JSON格式，key是bundleId，value是profileId）
     */
    private String bundleConfig;

    /**
     * 应用名称（从IPA解析）
     */
    private String appName;

    /**
     * 应用版本（从IPA解析）
     */
    private String appVersion;

    /**
     * 构建版本（从IPA解析）
     */
    private String appBuild;

    /**
     * 所有Bundle ID列表（JSON数组格式）
     */
    private String bundleIds;

    /**
     * 应用权限信息（JSON格式）
     */
    private String permissions;

    /**
     * 重签名后的IPA文件URL
     */
    private String resignedIpaUrl;

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