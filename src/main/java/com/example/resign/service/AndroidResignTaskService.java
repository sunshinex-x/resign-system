package com.example.resign.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidResignTask;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

/**
 * Android重签名任务Service接口
 */
public interface AndroidResignTaskService {

    /**
     * 创建重签名任务
     *
     * @param apkFile APK文件
     * @param signConfigId 签名配置ID
     * @param packageName 包名（可选，用于修改包名）
     * @param appName 应用名称（可选，用于修改应用名称）
     * @param versionName 版本名称（可选，用于修改版本名称）
     * @param versionCode 版本号（可选，用于修改版本号）
     * @param signatureVersion 签名版本（v1, v2, v1+v2）
     * @param callbackUrl 回调URL
     * @param description 任务描述
     * @return 创建的任务
     */
    AndroidResignTask createResignTask(MultipartFile apkFile, Long signConfigId,
                                     String packageName, String appName, String versionName,
                                     Integer versionCode, String signatureVersion,
                                     String callbackUrl, String description);

    /**
     * 执行重签名任务
     *
     * @param taskId 任务ID
     * @return 是否成功启动任务
     */
    boolean executeResignTask(Long taskId);

    /**
     * 根据ID获取任务
     *
     * @param taskId 任务ID
     * @return 任务信息
     */
    AndroidResignTask getTaskById(Long taskId);

    /**
     * 分页获取任务列表
     *
     * @param page 页码
     * @param size 每页大小
     * @param status 任务状态
     * @param certificateId 证书ID
     * @param appName 应用名称
     * @param createBy 创建人
     * @return 任务列表
     */
    Page<AndroidResignTask> getTaskList(int page, int size, String status, Long signConfigId,
                                       String appName, String createBy);

    /**
     * 根据状态获取任务列表
     *
     * @param status 任务状态
     * @return 任务列表
     */
    List<AndroidResignTask> getTasksByStatus(String status);

    /**
     * 获取待处理任务列表
     *
     * @return 待处理任务列表
     */
    List<AndroidResignTask> getPendingTasks();

    /**
     * 更新任务状态
     *
     * @param taskId 任务ID
     * @param status 新状态
     * @param failureReason 失败原因
     * @return 是否更新成功
     */
    boolean updateTaskStatus(Long taskId, String status, String failureReason);

    /**
     * 重试任务
     *
     * @param taskId 任务ID
     * @return 是否重试成功
     */
    boolean retryTask(Long taskId);

    /**
     * 取消任务
     *
     * @param taskId 任务ID
     * @return 是否取消成功
     */
    boolean cancelTask(Long taskId);

    /**
     * 删除任务
     *
     * @param taskId 任务ID
     * @return 是否删除成功
     */
    boolean deleteTask(Long taskId);

    /**
     * 解析APK文件信息
     *
     * @param apkFile APK文件
     * @return APK信息
     */
    Map<String, Object> parseApkInfo(MultipartFile apkFile);

    /**
     * 验证包名格式
     *
     * @param packageName 包名
     * @return 是否有效
     */
    boolean validatePackageName(String packageName);

    /**
     * 获取任务统计信息
     *
     * @return 统计信息
     */
    Map<String, Object> getTaskStatistics();

    /**
     * 处理待处理任务
     */
    void processPendingTasks();

    /**
     * 清理过期任务文件
     *
     * @param daysToKeep 保留天数
     */
    void cleanupExpiredTasks(int daysToKeep);

    /**
     * 下载重签名后的APK文件
     *
     * @param taskId 任务ID
     * @return 文件字节数组
     */
    byte[] downloadResignedApk(Long taskId);

    /**
     * 获取任务处理进度
     *
     * @param taskId 任务ID
     * @return 进度信息
     */
    Map<String, Object> getTaskProgress(Long taskId);

    /**
     * 验证签名版本
     *
     * @param signatureVersion 签名版本
     * @return 是否有效
     */
    boolean validateSignatureVersion(String signatureVersion);
}