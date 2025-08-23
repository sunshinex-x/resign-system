package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.resign.entity.SysLog;
import com.example.resign.mapper.SysLogMapper;
import com.example.resign.service.SysLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统日志服务实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SysLogServiceImpl extends ServiceImpl<SysLogMapper, SysLog> implements SysLogService {

    private final SysLogMapper sysLogMapper;

    @Override
    public IPage<SysLog> getLogPage(Integer current, Integer size, String level, String keyword,
                                    String module, LocalDateTime startTime, LocalDateTime endTime) {
        Page<SysLog> page = new Page<>(current, size);
        return sysLogMapper.selectLogPage(page, level, keyword, module, startTime, endTime);
    }

    @Override
    public SysLog getLogById(Long id) {
        return sysLogMapper.selectById(id);
    }

    @Override
    public boolean saveLog(SysLog sysLog) {
        if (sysLog.getCreateTime() == null) {
            sysLog.setCreateTime(LocalDateTime.now());
        }
        return sysLogMapper.insert(sysLog) > 0;
    }

    @Override
    public boolean batchSaveLog(List<SysLog> logs) {
        if (logs == null || logs.isEmpty()) {
            return true;
        }
        
        // 设置创建时间
        LocalDateTime now = LocalDateTime.now();
        logs.forEach(log -> {
            if (log.getCreateTime() == null) {
                log.setCreateTime(now);
            }
        });
        
        return sysLogMapper.batchInsert(logs) > 0;
    }

    @Override
    public Map<String, Object> getLogStatistics(LocalDateTime startTime, LocalDateTime endTime) {
        List<Map<String, Object>> statistics = sysLogMapper.selectLogStatistics(startTime, endTime);
        
        Map<String, Object> result = new HashMap<>();
        Map<String, Integer> levelCounts = new HashMap<>();
        
        int totalCount = 0;
        for (Map<String, Object> stat : statistics) {
            String level = (String) stat.get("level");
            Integer count = ((Number) stat.get("count")).intValue();
            levelCounts.put(level, count);
            totalCount += count;
        }
        
        result.put("totalCount", totalCount);
        result.put("levelCounts", levelCounts);
        result.put("debugCount", levelCounts.getOrDefault("DEBUG", 0));
        result.put("infoCount", levelCounts.getOrDefault("INFO", 0));
        result.put("warnCount", levelCounts.getOrDefault("WARN", 0));
        result.put("errorCount", levelCounts.getOrDefault("ERROR", 0));
        
        return result;
    }

    @Override
    public int cleanExpiredLogs(int days) {
        LocalDateTime beforeTime = LocalDateTime.now().minusDays(days);
        return sysLogMapper.deleteLogsBefore(beforeTime);
    }

    @Override
    public boolean deleteLogs(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return true;
        }
        return sysLogMapper.deleteBatchIds(ids) > 0;
    }

    @Override
    public void logUserAction(Long userId, String username, String module, String action,
                              String message, String requestUri, String requestMethod, String clientIp) {
        SysLog sysLog = new SysLog();
        sysLog.setLevel("INFO");
        sysLog.setMessage(message);
        sysLog.setLogger("UserAction");
        sysLog.setUserId(userId);
        sysLog.setUsername(username);
        sysLog.setModule(module);
        sysLog.setAction(action);
        sysLog.setRequestUri(requestUri);
        sysLog.setRequestMethod(requestMethod);
        sysLog.setClientIp(clientIp);
        sysLog.setCreateTime(LocalDateTime.now());
        
        try {
            saveLog(sysLog);
        } catch (Exception e) {
            log.error("保存用户操作日志失败", e);
        }
    }

    @Override
    public void logSystem(String level, String message, String logger, String exception) {
        SysLog sysLog = new SysLog();
        sysLog.setLevel(StringUtils.hasText(level) ? level.toUpperCase() : "INFO");
        sysLog.setMessage(message);
        sysLog.setLogger(logger);
        sysLog.setException(exception);
        sysLog.setCreateTime(LocalDateTime.now());
        
        try {
            saveLog(sysLog);
        } catch (Exception e) {
            log.error("保存系统日志失败", e);
        }
    }
}