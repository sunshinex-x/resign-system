package com.example.resign.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.ResignTask;
import com.example.resign.model.common.Result;
import com.example.resign.model.dto.ResignTaskDTO;
import com.example.resign.model.dto.ResignTaskQueryDTO;
import com.example.resign.model.vo.ResignTaskVO;
import com.example.resign.service.ResignTaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 重签名任务控制器
 */
@RestController
@RequestMapping("/api/resign")
@RequiredArgsConstructor
public class ResignTaskController {

    private final ResignTaskService resignTaskService;

    /**
     * 创建重签名任务
     *
     * @param resignTaskDTO 任务信息
     * @return 任务视图对象
     */
    @PostMapping("/tasks")
    public Result<ResignTaskVO> createTask(@Valid @RequestBody ResignTaskDTO resignTaskDTO) {
        ResignTaskVO task = resignTaskService.createTask(resignTaskDTO);
        return Result.success(task);
    }

    /**
     * 查询任务
     *
     * @param taskId 任务ID
     * @return 任务视图对象
     */
    @GetMapping("/tasks/{taskId}")
    public Result<ResignTaskVO> getTask(@PathVariable String taskId) {
        ResignTaskVO task = resignTaskService.getTaskById(taskId);
        return Result.success(task);
    }

    /**
     * 重试任务
     *
     * @param taskId 任务ID
     * @return 是否重试成功
     */
    @PutMapping("/tasks/{taskId}/retry")
    public Result<Boolean> retryTask(@PathVariable String taskId) {
        boolean result = resignTaskService.retryTask(taskId);
        return Result.success(result);
    }
    
    /**
     * 分页查询任务列表
     *
     * @param current 当前页码
     * @param size 每页大小
     * @param appType 应用类型，可为空
     * @param status 任务状态，可为空
     * @return 分页结果
     */
    @GetMapping("/tasks")
    public Result<IPage<ResignTaskVO>> pageTask(
            @RequestParam(defaultValue = "1") Integer current,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String appType,
            @RequestParam(required = false) String status) {
        Page<ResignTask> page = new Page<>(current, size);
        IPage<ResignTaskVO> result = resignTaskService.pageTask(page, appType, status);
        return Result.success(result);
    }
    
    /**
     * 高级分页查询任务列表
     *
     * @param queryDTO 查询条件
     * @return 分页结果
     */
    @PostMapping("/tasks/search")
    public Result<IPage<ResignTaskVO>> pageTaskAdvanced(@RequestBody ResignTaskQueryDTO queryDTO) {
        IPage<ResignTaskVO> result = resignTaskService.pageTaskAdvanced(queryDTO);
        return Result.success(result);
    }
    
    /**
     * 统计各状态任务数量
     *
     * @return 状态统计结果
     */
    @GetMapping("/tasks/stats")
    public Result<Map<String, Long>> countTaskByStatus() {
        Map<String, Long> result = resignTaskService.countTaskByStatus();
        return Result.success(result);
    }
    
    /**
     * 批量删除任务
     *
     * @param taskIds 任务ID列表
     * @return 是否删除成功
     */
    @DeleteMapping("/tasks/batch")
    public Result<Boolean> batchDeleteTasks(@RequestBody List<String> taskIds) {
        boolean result = resignTaskService.batchDeleteTasks(taskIds);
        return Result.success(result);
    }
}