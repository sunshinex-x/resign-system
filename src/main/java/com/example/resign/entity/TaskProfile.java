package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务Profile关联实体
 */
@Data
@TableName("task_profile")
public class TaskProfile {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * Profile ID
     */
    private Long profileId;

    /**
     * Bundle ID
     */
    private String bundleId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}