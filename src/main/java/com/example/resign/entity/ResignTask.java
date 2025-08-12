package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 重签名任务实体类
 */
@Data
@TableName("resign_task")
public class ResignTask {

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
     * 应用类型：IOS、ANDROID、HARMONY
     */
    private String appType;

    /**
     * 原始安装包URL
     */
    private String originalPackageUrl;

    /**
     * 证书URL
     */
    private String certificateUrl;

    /**
     * 证书密码
     */
    private String certificatePassword;

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
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 回调URL
     */
    private String callbackUrl;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}