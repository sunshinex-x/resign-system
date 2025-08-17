package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 任务Bundle ID和Profile文件关联表
 */
@Data
@TableName("task_bundle_profile")
public class TaskBundleProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * Bundle ID
     */
    private String bundleId;

    /**
     * Profile文件URL
     */
    private String profileUrl;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 版本号
     */
    private String version;
}