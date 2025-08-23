package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.SysLog;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志服务接口
 */
public interface SysLogService {

    /**
     * 分页查询系统日志
     *
     * @param current 当前页
     * @param size 每页大小
     * @param level 日志级别
     * @param keyword 关键词
     * @param module 操作模块
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 分页结果
     */
    IPage<SysLog> getLogPage(Integer current, Integer size, String level, String keyword,
                             String module, LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 根据ID获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    SysLog getLogById(Long id);

    /**
     * 保存系统日志
     *
     * @param sysLog 日志对象
     * @return 是否成功
     */
    boolean saveLog(SysLog sysLog);

    /**
     * 批量保存系统日志
     *
     * @param logs 日志列表
     * @return 是否成功
     */
    boolean batchSaveLog(List<SysLog> logs);

    /**
     * 获取日志统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    Map<String, Object> getLogStatistics(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 清理过期日志
     *
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanExpiredLogs(int days);

    /**
     * 删除指定日志
     *
     * @param ids 日志ID列表
     * @return 是否成功
     */
    boolean deleteLogs(List<Long> ids);

    /**
     * 记录用户操作日志
     *
     * @param userId 用户ID
     * @param username 用户名
     * @param module 操作模块
     * @param action 操作类型
     * @param message 日志内容
     * @param requestUri 请求URI
     * @param requestMethod 请求方法
     * @param clientIp 客户端IP
     */
    void logUserAction(Long userId, String username, String module, String action,
                       String message, String requestUri, String requestMethod, String clientIp);

    /**
     * 记录系统日志
     *
     * @param level 日志级别
     * @param message 日志内容
     * @param logger 日志来源
     * @param exception 异常信息
     */
    void logSystem(String level, String message, String logger, String exception);
}