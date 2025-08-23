package com.example.resign.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.resign.entity.AndroidResignTask;
import com.example.resign.model.common.Result;
import com.example.resign.service.AndroidResignTaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Android重签名任务Controller
 */
@Slf4j
@RestController
@RequestMapping("/api/android/resign")
@Tag(name = "Android重签名任务", description = "Android应用重签名任务管理接口")
public class AndroidResignTaskController {

    @Autowired
    private AndroidResignTaskService resignTaskService;

    /**
     * 创建重签名任务
     */
    @Operation(summary = "创建Android重签名任务", description = "上传APK文件并创建重签名任务")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "任务创建成功"),
        @ApiResponse(responseCode = "400", description = "请求参数错误"),
        @ApiResponse(responseCode = "500", description = "服务器内部错误")
    })
    @PostMapping("/tasks")
    public Result<AndroidResignTask> createResignTask(
            @Parameter(description = "APK文件", required = true) @RequestParam("apkFile") MultipartFile apkFile,
            @Parameter(description = "证书ID", required = true) @RequestParam("certificateId") Long certificateId,
            @Parameter(description = "包名") @RequestParam(value = "packageName", required = false) String packageName,
            @Parameter(description = "应用名称") @RequestParam(value = "appName", required = false) String appName,
            @Parameter(description = "版本名称") @RequestParam(value = "versionName", required = false) String versionName,
            @Parameter(description = "版本号") @RequestParam(value = "versionCode", required = false) Integer versionCode,
            @Parameter(description = "签名版本", example = "v1+v2") @RequestParam(value = "signatureVersion", defaultValue = "v1+v2") String signatureVersion,
            @Parameter(description = "回调URL") @RequestParam(value = "callbackUrl", required = false) String callbackUrl,
            @Parameter(description = "任务描述") @RequestParam(value = "description", required = false) String description) {
        
        try {
            AndroidResignTask task = resignTaskService.createResignTask(
                apkFile, certificateId, packageName, appName, versionName, 
                versionCode, signatureVersion, callbackUrl, description
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
    @PostMapping("/tasks/{taskId}/execute")
    public Result<Void> executeResignTask(@PathVariable Long taskId) {
        
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
     * 获取任务详情
     */
    @GetMapping("/tasks/{taskId}")
    public Result<AndroidResignTask> getTaskById(@PathVariable Long taskId) {
        
        try {
            AndroidResignTask task = resignTaskService.getTaskById(taskId);
            
            if (task != null) {
                return Result.success("获取任务详情成功", task);
            } else {
                return Result.error(404, "任务不存在");
            }
            
        } catch (Exception e) {
            log.error("获取任务详情失败", e);
            return Result.error("获取任务详情失败: " + e.getMessage());
        }
    }

    /**
     * 分页获取任务列表
     */
    @GetMapping("/tasks")
    public Result<Page<AndroidResignTask>> getTaskList(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "certificateId", required = false) Long certificateId,
            @RequestParam(value = "appName", required = false) String appName,
            @RequestParam(value = "createBy", required = false) String createBy) {
        
        try {
            Page<AndroidResignTask> taskPage = resignTaskService.getTaskList(
                page, size, status, certificateId, appName, createBy
            );
            
            return Result.success("获取任务列表成功", taskPage);
            
        } catch (Exception e) {
            log.error("获取任务列表失败", e);
            return Result.error("获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 根据状态获取任务列表
     */
    @GetMapping("/tasks/status/{status}")
    public Result<List<AndroidResignTask>> getTasksByStatus(@PathVariable String status) {
        
        try {
            List<AndroidResignTask> tasks = resignTaskService.getTasksByStatus(status);
            
            return Result.success("获取任务列表成功", tasks);
            
        } catch (Exception e) {
            log.error("根据状态获取任务列表失败", e);
            return Result.error("根据状态获取任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取待处理任务列表
     */
    @GetMapping("/tasks/pending")
    public Result<List<AndroidResignTask>> getPendingTasks() {
        
        try {
            List<AndroidResignTask> tasks = resignTaskService.getPendingTasks();
            
            return Result.success("获取待处理任务列表成功", tasks);
            
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
     * 解析APK文件信息
     */
    @PostMapping("/parse")
    public Result<Map<String, Object>> parseApkInfo(
            @RequestParam("apkFile") MultipartFile apkFile) {
        
        try {
            Map<String, Object> apkInfo = resignTaskService.parseApkInfo(apkFile);
            
            return Result.success("解析APK文件信息成功", apkInfo);
            
        } catch (Exception e) {
            log.error("解析APK文件信息失败", e);
            return Result.error("解析APK文件信息失败: " + e.getMessage());
        }
    }

    /**
     * 验证包名
     */
    @PostMapping("/validate-package")
    public Result<Map<String, Object>> validatePackageName(
            @RequestParam("packageName") String packageName) {
        
        try {
            boolean isValid = resignTaskService.validatePackageName(packageName);
            
            return Result.success("验证包名成功", Map.of("isValid", isValid));
            
        } catch (Exception e) {
            log.error("验证包名失败", e);
            return Result.error("验证包名失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/statistics")
    public Result<Map<String, Object>> getTaskStatistics() {
        
        try {
            Map<String, Object> statistics = resignTaskService.getTaskStatistics();
            
            return Result.success("获取任务统计信息成功", statistics);
            
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
            
            return Result.success("获取任务处理进度成功", progress);
            
        } catch (Exception e) {
            log.error("获取任务处理进度失败", e);
            return Result.error("获取任务处理进度失败: " + e.getMessage());
        }
    }

    /**
     * 下载重签名后的APK文件
     */
    @GetMapping("/tasks/{taskId}/download")
    public Result<byte[]> downloadResignedApk(@PathVariable Long taskId) {
        try {
            byte[] fileData = resignTaskService.downloadResignedApk(taskId);
            
            if (fileData != null) {
                return Result.success(fileData);
            } else {
                return Result.error("文件不存在");
            }
            
        } catch (Exception e) {
            log.error("下载重签名文件失败", e);
            return Result.error("下载重签名文件失败: " + e.getMessage());
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