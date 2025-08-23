package com.example.resign.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.IosResignTask;
import com.example.resign.model.common.Result;
import com.example.resign.service.IosResignTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * iOS重签名任务Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/ios/resign")
@Tag(name = "iOS重签名任务", description = "iOS应用重签名任务管理接口")
public class IosResignTaskController {

    @Autowired
    private IosResignTaskService resignTaskService;

    /**
     * 创建重签名任务
     */
    @Operation(summary = "创建iOS重签名任务", description = "上传IPA文件并创建重签名任务")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "任务创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/tasks")
    public Result<IosResignTask> createResignTask(
            @Parameter(description = "IPA文件", required = true) @RequestParam("ipaFile") MultipartFile ipaFile,
            @Parameter(description = "证书ID", required = true) @RequestParam("certificateId") Long certificateId,
            @Parameter(description = "Bundle ID") @RequestParam(value = "bundleId", required = false) String bundleId,
            @Parameter(description = "应用名称") @RequestParam(value = "appName", required = false) String appName,
            @Parameter(description = "应用版本") @RequestParam(value = "appVersion", required = false) String appVersion,
            @Parameter(description = "构建版本") @RequestParam(value = "buildVersion", required = false) String buildVersion,
            @Parameter(description = "回调URL") @RequestParam(value = "callbackUrl", required = false) String callbackUrl,
            @Parameter(description = "任务描述") @RequestParam(value = "description", required = false) String description) {
        
        try {
            IosResignTask task = resignTaskService.createResignTask(
                ipaFile, certificateId, bundleId, appName, appVersion, 
                buildVersion, callbackUrl, description
            );
            
            return Result.success("重签名任务创建成功", task);
            
        } catch (Exception e) {
            log.error("创建重签名任务失败", e);
            return Result.error("创建重签名任务失败: " + e.getMessage());
        }
    }

    /**
     * 执行重签名任务
     */
    @Operation(summary = "执行重签名任务", description = "开始执行指定的重签名任务")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "任务执行成功"),
        @ApiResponse(responseCode = "404", description = "任务不存在"),
        @ApiResponse(responseCode = "400", description = "任务状态不允许执行")
    })
    @PostMapping("/tasks/{taskId}/execute")
    public Result<Void> executeResignTask(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        
        try {
            boolean success = resignTaskService.executeResignTask(taskId);
            
            if (success) {
                return Result.success("重签名任务已开始执行", null);
            } else {
                return Result.error("重签名任务执行失败");
            }
            
        } catch (Exception e) {
            log.error("执行重签名任务失败", e);
            return Result.error("执行重签名任务失败: " + e.getMessage());
        }
    }

    /**
     * 根据ID获取任务详情
     */
    @Operation(summary = "获取任务详情", description = "根据任务ID获取重签名任务的详细信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功"),
        @ApiResponse(responseCode = "404", description = "任务不存在")
    })
    @GetMapping("/tasks/{taskId}")
    public Result<IosResignTask> getTaskById(
            @Parameter(description = "任务ID", required = true) @PathVariable Long taskId) {
        
        try {
            IosResignTask task = resignTaskService.getTaskById(taskId);
            
            if (task != null) {
                return Result.success(task);
            } else {
                return Result.error(404, "任务不存在");
            }
            
        } catch (Exception e) {
            log.error("获取任务详情失败", e);
            return Result.error("获取任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询任务列表
     */
    @Operation(summary = "分页查询任务列表", description = "根据条件分页查询重签名任务列表")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查询成功")
    })
    @GetMapping("/tasks")
    public Result<Page<IosResignTask>> getTaskList(
            @Parameter(description = "页码", example = "1") @RequestParam(value = "page", defaultValue = "1") int page,
            @Parameter(description = "每页大小", example = "10") @RequestParam(value = "size", defaultValue = "10") int size,
            @Parameter(description = "任务状态") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "证书ID") @RequestParam(value = "certificateId", required = false) Long certificateId,
            @Parameter(description = "应用名称") @RequestParam(value = "appName", required = false) String appName,
            @Parameter(description = "创建人") @RequestParam(value = "createBy", required = false) String createBy) {
        
        try {
            Page<IosResignTask> taskPage = resignTaskService.getTaskList(
                page, size, status, certificateId, appName, createBy
            );
            
            return Result.success(taskPage);
            
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return Result.error("获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取任务列表
     */
    @GetMapping("/tasks/status/{status}")
    public Result<List<IosResignTask>> getTasksByStatus(@PathVariable String status) {
        
        try {
            List<IosResignTask> tasks = resignTaskService.getTasksByStatus(status);
            
            return Result.success(tasks);
            
        } catch (Exception e) {
            log.error("根据状态获取任务列表失败", e);
            return Result.error("根据状态获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理任务列表
     */
    @GetMapping("/tasks/pending")
    public Result<List<IosResignTask>> getPendingTasks() {
        
        try {
            List<IosResignTask> tasks = resignTaskService.getPendingTasks();
            
            return Result.success(tasks);
            
        } catch (Exception e) {
            log.error("获取待处理任务列表失败", e);
            return Result.error("获取待处理任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 重试任务
     */
    @PostMapping("/tasks/{taskId}/retry")
    public Result<Void> retryTask(@PathVariable Long taskId) {
        
        try {
            boolean success = resignTaskService.retryTask(taskId);
            
            if (success) {
                return Result.success("任务重试成功", null);
            } else {
                return Result.error("任务重试失败");
            }
            
        } catch (Exception e) {
            log.error("重试任务失败", e);
            return Result.error("重试任务失败: " + e.getMessage());
        }
    }

    /**
     * 取消任务
     */
    @PostMapping("/tasks/{taskId}/cancel")
    public Result<Void> cancelTask(@PathVariable Long taskId) {
        
        try {
            boolean success = resignTaskService.cancelTask(taskId);
            
            if (success) {
                return Result.success("任务取消成功", null);
            } else {
                return Result.error("任务取消失败");
            }
            
        } catch (Exception e) {
            log.error("取消任务失败", e);
            return Result.error("取消任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/tasks/{taskId}")
    public Result<Void> deleteTask(@PathVariable Long taskId) {
        
        try {
            boolean success = resignTaskService.deleteTask(taskId);
            
            if (success) {
                return Result.success("任务删除成功", null);
            } else {
                return Result.error("任务删除失败");
            }
            
        } catch (Exception e) {
            log.error("删除任务失败", e);
            return Result.error("删除任务失败: " + e.getMessage());
        }
    }

    /**
     * 解析IPA文件信息
     */
    @PostMapping("/parse")
    public Result<Map<String, Object>> parseIpaInfo(
            @RequestParam("ipaFile") MultipartFile ipaFile) {
        
        try {
            Map<String, Object> ipaInfo = resignTaskService.parseIpaInfo(ipaFile);
            return Result.success(ipaInfo);
            
        } catch (Exception e) {
            log.error("解析IPA文件信息失败", e);
            return Result.error("解析IPA文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getTaskStatistics() {
        try {
            Map<String, Object> statistics = resignTaskService.getTaskStatistics();
            return Result.success(statistics);
            
        } catch (Exception e) {
            log.error("获取任务统计信息失败", e);
            return Result.error("获取任务统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务处理进度
     */
    @GetMapping("/tasks/{taskId}/progress")
    public Result<Map<String, Object>> getTaskProgress(@PathVariable Long taskId) {
        try {
            Map<String, Object> progress = resignTaskService.getTaskProgress(taskId);
            return Result.success(progress);
            
        } catch (Exception e) {
            log.error("获取任务处理进度失败", e);
            return Result.error("获取任务处理进度失败: " + e.getMessage());
        }
    }

    /**
     * 下载重签名后的IPA文件
     */
    @GetMapping("/tasks/{taskId}/download")
    public ResponseEntity<byte[]> downloadResignedIpa(@PathVariable Long taskId) {
        try {
            byte[] fileData = resignTaskService.downloadResignedIpa(taskId);
            
            if (fileData != null) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "resigned_app.ipa");
                headers.setContentLength(fileData.length);
                
                return new ResponseEntity<>(fileData, headers, HttpStatus.OK);
            } else {
                return ResponseEntity.notFound().build();
            }
            
        } catch (Exception e) {
            log.error("下载重签名文件失败", e);
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * 处理待处理任务
     */
    @PostMapping("/process-pending")
    public Result<Void> processPendingTasks() {
        try {
            resignTaskService.processPendingTasks();
            return Result.success();
            
        } catch (Exception e) {
            log.error("处理待处理任务失败", e);
            return Result.error("处理待处理任务失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期任务
     */
    @PostMapping("/cleanup")
    public Result<Void> cleanupExpiredTasks(
            @RequestParam(value = "daysToKeep", defaultValue = "30") int daysToKeep) {
        
        try {
            resignTaskService.cleanupExpiredTasks(daysToKeep);
            return Result.success();
            
        } catch (Exception e) {
            log.error("清理过期任务失败", e);
            return Result.error("清理过期任务失败: " + e.getMessage());
        }
    }
}