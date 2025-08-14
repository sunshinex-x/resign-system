package com.example.resign.model.dto;

import lombok.Data;

/**
 * 重签名任务查询DTO
 */
@Data
public class ResignTaskQueryDTO {

    /**
     * 当前页码
     */
    private Integer current = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 应用类型
     */
    private String appType;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;

    /**
     * 创建人
     */
    private String createBy;
}