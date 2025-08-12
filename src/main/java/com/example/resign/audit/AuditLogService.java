package com.example.resign.audit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 审计日志服务，用于记录系统中的重要操作
 */
@Service
public class AuditLogService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("com.example.resign.audit");
    
    /**
     * 记录用户操作日志
     * 
     * @param userId 用户ID
     * @param action 操作类型
     * @param resource 资源对象
     * @param resourceId 资源ID
     * @param details 详细信息
     */
    public void logUserAction(String userId, String action, String resource, String resourceId, String details) {
        String logMessage = String.format("User [%s] performed [%s] on %s [%s]: %s", 
                userId, action, resource, resourceId, details);
        auditLogger.info(logMessage);
    }
    
    /**
     * 记录系统操作日志
     * 
     * @param action 操作类型
     * @param resource 资源对象
     * @param resourceId 资源ID
     * @param details 详细信息
     */
    public void logSystemAction(String action, String resource, String resourceId, String details) {
        String logMessage = String.format("System performed [%s] on %s [%s]: %s", 
                action, resource, resourceId, details);
        auditLogger.info(logMessage);
    }
    
    /**
     * 记录任务状态变更
     * 
     * @param taskId 任务ID
     * @param oldStatus 旧状态
     * @param newStatus 新状态
     * @param reason 变更原因
     */
    public void logTaskStatusChange(String taskId, String oldStatus, String newStatus, String reason) {
        String logMessage = String.format("Task [%s] status changed from [%s] to [%s]. Reason: %s", 
                taskId, oldStatus, newStatus, reason);
        auditLogger.info(logMessage);
    }
    
    /**
     * 记录安全相关事件
     * 
     * @param userId 用户ID
     * @param action 操作类型
     * @param result 结果
     * @param details 详细信息
     */
    public void logSecurityEvent(String userId, String action, String result, String details) {
        String logMessage = String.format("Security event: User [%s] attempted [%s], Result: [%s], Details: %s", 
                userId, action, result, details);
        auditLogger.info(logMessage);
    }
}