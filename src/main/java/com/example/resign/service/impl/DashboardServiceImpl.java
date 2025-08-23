package com.example.resign.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidResignTask;
import com.example.resign.entity.IosResignTask;
import com.example.resign.service.AndroidResignTaskService;
import com.example.resign.service.DashboardService;
import com.example.resign.service.IosResignTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 看板服务实现类
 */
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final AndroidResignTaskService androidResignTaskService;
    private final IosResignTaskService iosResignTaskService;

    @Override
    public Map<String, Long> getTaskStats() {
        // 合并Android和iOS任务统计数据
        Map<String, Object> androidStats = androidResignTaskService.getTaskStatistics();
        Map<String, Object> iosStats = iosResignTaskService.getTaskStatistics();
        
        Map<String, Long> totalStats = new HashMap<>();
        totalStats.put("totalCount", getLongValue(androidStats, "totalCount") + getLongValue(iosStats, "totalCount"));
        totalStats.put("pendingCount", getLongValue(androidStats, "pendingCount") + getLongValue(iosStats, "pendingCount"));
        totalStats.put("processingCount", getLongValue(androidStats, "processingCount") + getLongValue(iosStats, "processingCount"));
        totalStats.put("successCount", getLongValue(androidStats, "successCount") + getLongValue(iosStats, "successCount"));
        totalStats.put("failedCount", getLongValue(androidStats, "failedCount") + getLongValue(iosStats, "failedCount"));
        
        return totalStats;
    }
    
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }

    @Override
    public IPage<Object> getRecentTasks(Integer page, Integer size) {
        // 由于没有统一的任务表，这里返回模拟数据
        // 实际项目中可以考虑创建统一的任务视图或者合并两个服务的数据
        Page<Object> pageResult = new Page<>(page, size);
        pageResult.setTotal(0);
        pageResult.setRecords(new ArrayList<>());
        
        return pageResult;
    }

    @Override
    public Map<String, Long> getTaskTypeDistribution() {
        Map<String, Long> distribution = new HashMap<>();
        
        // 这里需要根据实际的任务类型字段来统计
        // 由于当前ResignTask可能没有appType字段，我们先返回模拟数据
        // 实际项目中应该根据数据库中的appType字段进行统计
        distribution.put("IOS", 40L);
        distribution.put("ANDROID", 35L);
        distribution.put("HARMONYOS", 25L);
        
        return distribution;
    }

    @Override
    public Map<String, Object> getTaskTrend() {
        Map<String, Object> trend = new HashMap<>();
        
        // 获取近7天的日期
        List<String> dates = new ArrayList<>();
        List<Long> totalTasks = new ArrayList<>();
        List<Long> successTasks = new ArrayList<>();
        List<Long> failedTasks = new ArrayList<>();
        
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
        
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            dates.add(date.format(formatter));
            
            // 这里应该查询数据库获取实际数据
            // 目前返回模拟数据
            totalTasks.add((long) (Math.random() * 50 + 10));
            successTasks.add((long) (Math.random() * 40 + 5));
            failedTasks.add((long) (Math.random() * 10 + 1));
        }
        
        trend.put("dates", dates);
        trend.put("totalTasks", totalTasks);
        trend.put("successTasks", successTasks);
        trend.put("failedTasks", failedTasks);
        
        return trend;
    }
}