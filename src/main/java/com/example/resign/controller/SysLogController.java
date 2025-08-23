package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.resign.model.common.Result;
import com.example.resign.entity.SysLog;
import com.example.resign.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 系统日志管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system/logs")
@RequiredArgsConstructor
public class SysLogController {

    private final SysLogService sysLogService;

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
    @GetMapping
    public Result<IPage<SysLog>> getLogList(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "20") Integer size,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String module,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            IPage<SysLog> page = sysLogService.getLogPage(current, size, level, keyword, module, startTime, endTime);
            return Result.success(page);
        } catch (Exception e) {
            log.error("查询系统日志失败", e);
            return Result.error("查询系统日志失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取日志详情
     *
     * @param id 日志ID
     * @return 日志详情
     */
    @GetMapping("/{id}")
    public Result<SysLog> getLogById(@PathVariable Long id) {
        try {
            SysLog sysLog = sysLogService.getLogById(id);
            if (sysLog == null) {
                return Result.error("日志不存在");
            }
            return Result.success(sysLog);
        } catch (Exception e) {
            log.error("获取日志详情失败", e);
            return Result.error("获取日志详情失败: " + e.getMessage());
        }
    }

    /**
     * 获取日志统计信息
     *
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计结果
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getLogStatistics(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        
        try {
            // 如果没有指定时间范围，默认查询最近7天
            if (startTime == null) {
                startTime = LocalDateTime.now().minusDays(7);
            }
            if (endTime == null) {
                endTime = LocalDateTime.now();
            }
            
            Map<String, Object> statistics = sysLogService.getLogStatistics(startTime, endTime);
            return Result.success(statistics);
        } catch (Exception e) {
            log.error("获取日志统计失败", e);
            return Result.error("获取日志统计失败: " + e.getMessage());
        }
    }

    /**
     * 批量删除日志
     *
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @DeleteMapping
    public Result<Void> deleteLogs(@RequestBody List<Long> ids) {
        try {
            boolean success = sysLogService.deleteLogs(ids);
            if (success) {
                return Result.success();
            } else {
                return Result.error("删除日志失败");
            }
        } catch (Exception e) {
            log.error("删除日志失败", e);
            return Result.error("删除日志失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期日志
     *
     * @param days 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/cleanup")
    public Result<Integer> cleanupLogs(@RequestParam(defaultValue = "30") Integer days) {
        try {
            int cleanedCount = sysLogService.cleanExpiredLogs(days);
            return Result.success("成功清理 " + cleanedCount + " 条过期日志", cleanedCount);
        } catch (Exception e) {
            log.error("清理过期日志失败", e);
            return Result.error("清理过期日志失败: " + e.getMessage());
        }
    }

    /**
     * 手动记录系统日志
     *
     * @param sysLog 日志对象
     * @return 保存结果
     */
    @PostMapping
    public Result<Void> saveLog(@RequestBody SysLog sysLog) {
        try {
            boolean success = sysLogService.saveLog(sysLog);
            if (success) {
                return Result.success();
            } else {
                return Result.error("保存日志失败");
            }
        } catch (Exception e) {
            log.error("保存日志失败", e);
            return Result.error("保存日志失败: " + e.getMessage());
        }
    }
}