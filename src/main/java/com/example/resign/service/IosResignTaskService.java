package com.example.resign.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosResignTask;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * iOS重签名任务Service接口
 */
public interface IosResignTaskService {

    /**
     * 创建iOS重签名任务
     */
    IosResignTask createResignTask(MultipartFile ipaFile, Long certificateId, 
                                 String bundleId, String appName, String appVersion, 
                                 String buildVersion, String callbackUrl, String description);

    /**
     * 执行重签名任务
     */
    boolean executeResignTask(Long taskId);

    /**
     * 根据ID获取任务信息
     */
    IosResignTask getTaskById(Long taskId);

    /**
     * 分页查询任务列表
     */
    Page<IosResignTask> getTaskList(int page, int size, String status, Long certificateId, 
                                  String appName, String createBy);

    /**
     * 根据状态查询任务列表
     */
    List<IosResignTask> getTasksByStatus(String status);

    /**
     * 获取待处理的任务列表
     */
    List<IosResignTask> getPendingTasks();

    /**
     * 更新任务状态
     */
    boolean updateTaskStatus(Long taskId, String status, String failureReason);

    /**
     * 重试失败的任务
     */
    boolean retryTask(Long taskId);

    /**
     * 取消任务
     */
    boolean cancelTask(Long taskId);

    /**
     * 删除任务
     */
    boolean deleteTask(Long taskId);

    /**
     * 解析IPA文件信息
     */
    Map<String, Object> parseIpaInfo(MultipartFile ipaFile);

    /**
     * 验证Bundle ID格式
     */
    boolean validateBundleId(String bundleId);

    /**
     * 获取任务统计信息
     */
    Map<String, Object> getTaskStatistics();

    /**
     * 批量处理待处理任务
     */
    void processPendingTasks();

    /**
     * 清理过期任务文件
     */
    void cleanupExpiredTasks(int daysToKeep);

    /**
     * 下载重签名后的IPA文件
     */
    byte[] downloadResignedIpa(Long taskId);

    /**
     * 获取任务处理进度
     */
    Map<String, Object> getTaskProgress(Long taskId);
}