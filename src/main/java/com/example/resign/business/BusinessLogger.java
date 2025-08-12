package com.example.resign.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 业务日志工具类，用于记录业务操作日志
 */
public class BusinessLogger {

    private static final Logger businessLogger = LoggerFactory.getLogger("com.example.resign.business");
    
    /**
     * 记录任务创建日志
     * 
     * @param taskId 任务ID
     * @param appType 应用类型
     * @param originalPackageUrl 原始包URL
     */
    public static void logTaskCreation(String taskId, String appType, String originalPackageUrl) {
        businessLogger.info("Task created: ID={}, Type={}, Package={}", taskId, appType, originalPackageUrl);
    }
    
    /**
     * 记录任务状态变更日志
     * 
     * @param taskId 任务ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     */
    public static void logTaskStatusChange(String taskId, String oldStatus, String newStatus) {
        businessLogger.info("Task status changed: ID={}, From={}, To={}", taskId, oldStatus, newStatus);
    }
    
    /**
     * 记录任务处理开始日志
     * 
     * @param taskId 任务ID
     */
    public static void logTaskProcessingStart(String taskId) {
        businessLogger.info("Task processing started: ID={}", taskId);
    }
    
    /**
     * 记录任务处理完成日志
     * 
     * @param taskId 任务ID
     * @param success 是否成功
     * @param duration 处理时长（毫秒）
     */
    public static void logTaskProcessingComplete(String taskId, boolean success, long duration) {
        businessLogger.info("Task processing completed: ID={}, Success={}, Duration={}ms", 
                taskId, success, duration);
    }
    
    /**
     * 记录任务处理失败日志
     * 
     * @param taskId 任务ID
     * @param errorMessage 错误信息
     */
    public static void logTaskProcessingFailure(String taskId, String errorMessage) {
        businessLogger.error("Task processing failed: ID={}, Error={}", taskId, errorMessage);
    }
    
    /**
     * 记录任务重试日志
     * 
     * @param taskId 任务ID
     * @param retryCount 重试次数
     */
    public static void logTaskRetry(String taskId, int retryCount) {
        businessLogger.info("Task retry: ID={}, RetryCount={}", taskId, retryCount);
    }
    
    /**
     * 记录文件操作日志
     * 
     * @param operation 操作类型（上传、下载、删除等）
     * @param fileUrl 文件URL
     * @param success 是否成功
     */
    public static void logFileOperation(String operation, String fileUrl, boolean success) {
        businessLogger.info("File operation: Operation={}, URL={}, Success={}", 
                operation, fileUrl, success);
    }
    
    /**
     * 记录回调通知日志
     * 
     * @param taskId 任务ID
     * @param callbackUrl 回调URL
     * @param success 是否成功
     */
    public static void logCallbackNotification(String taskId, String callbackUrl, boolean success) {
        businessLogger.info("Callback notification: TaskID={}, URL={}, Success={}", 
                taskId, callbackUrl, success);
    }
}