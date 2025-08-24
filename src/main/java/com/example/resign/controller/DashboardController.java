package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.example.resign.model.common.Result;

import com.example.resign.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 看板控制器
 * 提供看板统计数据和最近任务列表
 */
@Tag(name = "看板管理", description = "看板统计数据和最近任务相关接口")
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 获取任务统计数据
     *
     * @return 任务统计结果
     */
    @Operation(summary = "获取任务统计数据", description = "获取各状态下的重签名任务统计数据")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/stats")
    public Result<Map<String, Long>> getTaskStats() {
        Map<String, Long> stats = dashboardService.getTaskStats();
        return Result.success(stats);
    }

    /**
     * 获取最近任务列表
     *
     * @param page 页码，默认1
     * @param size 每页大小，默认5
     * @return 最近任务列表
     */
    @Operation(summary = "获取最近任务列表", description = "获取最近创建的任务列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/recent-tasks")
    public Result<IPage<Object>> getRecentTasks(
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(defaultValue = "10") Integer size) {
        IPage<Object> recentTasks = dashboardService.getRecentTasks(page, size);
        return Result.success(recentTasks);
    }

    /**
     * 获取任务类型分布统计
     *
     * @return 任务类型分布数据
     */
    @Operation(summary = "获取任务类型分布", description = "获取iOS、Android任务数量分布")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/task-type-distribution")
    public Result<Map<String, Long>> getTaskTypeDistribution() {
        Map<String, Long> distribution = dashboardService.getTaskTypeDistribution();
        return Result.success(distribution);
    }

    /**
     * 获取近7天任务趋势
     *
     * @return 近7天任务趋势数据
     */
    @Operation(summary = "获取近7天任务趋势", description = "获取近7天每日任务创建和完成情况")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/task-trend")
    public Result<Map<String, Object>> getTaskTrend() {
        Map<String, Object> trend = dashboardService.getTaskTrend();
        return Result.success(trend);
    }
}