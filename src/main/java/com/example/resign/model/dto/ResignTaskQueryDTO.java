package com.example.resign.model.dto;

import lombok.Data;

/**
 * 重签名任务查询条件DTO
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
     * 应用类型：IOS、ANDROID、HARMONY
     */
    private String appType;

    /**
     * 任务状态：PENDING、PROCESSING、SUCCESS、FAILED
     */
    private String status;

    /**
     * 任务ID
     */
    private String taskId;

    /**
     * 开始时间
     */
    private String startTime;

    /**
     * 结束时间
     */
    private String endTime;
}