package com.example.resign.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 任务证书关联实体
 */
@Data
@TableName("task_certificate")
public class TaskCertificate {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 证书ID
     */
    private Long certificateId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}