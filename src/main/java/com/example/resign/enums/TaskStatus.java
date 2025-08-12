package com.example.resign.enums;

import lombok.Getter;

/**
 * 任务状态枚举
 */
@Getter
public enum TaskStatus {
    
    PENDING("PENDING", "等待处理"),
    PROCESSING("PROCESSING", "处理中"),
    SUCCESS("SUCCESS", "处理成功"),
    FAILED("FAILED", "处理失败");
    
    private final String status;
    private final String description;
    
    TaskStatus(String status, String description) {
        this.status = status;
        this.description = description;
    }
    
    public static TaskStatus fromString(String status) {
        for (TaskStatus taskStatus : TaskStatus.values()) {
            if (taskStatus.getStatus().equalsIgnoreCase(status)) {
                return taskStatus;
            }
        }
        throw new IllegalArgumentException("Unknown task status: " + status);
    }
}