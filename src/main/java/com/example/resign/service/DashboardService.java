package com.example.resign.service;

import com.baomidou.mybatisplus.core.metadata.IPage;

import java.util.Map;

/**
 * 看板服务接口
 */
public interface DashboardService {

    /**
     * 获取任务统计数据
     *
     * @return 任务统计结果
     */
    Map<String, Long> getTaskStats();

    /**
     * 获取最近任务列表
     *
     * @param page 页码
     * @param size 每页大小
     * @return 最近任务列表
     */
    IPage<Object> getRecentTasks(Integer page, Integer size);

    /**
     * 获取任务类型分布统计
     *
     * @return 任务类型分布数据
     */
    Map<String, Long> getTaskTypeDistribution();

    /**
     * 获取近7天任务趋势
     *
     * @return 近7天任务趋势数据
     */
    Map<String, Object> getTaskTrend();
}