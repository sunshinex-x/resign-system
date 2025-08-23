package com.example.resign.controller;

import com.example.resign.config.MinioConfig;
import com.example.resign.model.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
@Tag(name = "系统配置管理", description = "系统配置管理相关接口")
public class SystemConfigController {

    private final MinioConfig minioConfig;

    // 重签名配置
    @Value("${resign.temp-dir:/tmp/resign}")
    private String tempDir;
    
    @Value("${resign.tools.ios.sign-tool:/usr/bin/codesign}")
    private String iosSignTool;
    
    @Value("${resign.tools.ios.verify-tool:/usr/bin/codesign}")
    private String iosVerifyTool;
    
    @Value("${resign.tools.android.sign-tool:/usr/local/bin/apksigner}")
    private String androidSignTool;
    
    @Value("${resign.tools.android.zipalign-tool:/usr/local/bin/zipalign}")
    private String androidZipalignTool;
    
    @Value("${resign.task.retry-count:3}")
    private Integer retryCount;
    
    @Value("${resign.task.timeout-seconds:1800}")
    private Integer timeoutSeconds;

    // RabbitMQ配置
    @Value("${spring.rabbitmq.host:localhost}")
    private String rabbitmqHost;
    
    @Value("${spring.rabbitmq.port:5672}")
    private Integer rabbitmqPort;
    
    @Value("${spring.rabbitmq.username:guest}")
    private String rabbitmqUsername;
    
    @Value("${spring.rabbitmq.virtual-host:/}")
    private String rabbitmqVirtualHost;

    /**
     * 获取重签名配置
     */
    @Operation(summary = "获取重签名配置", description = "获取重签名工具配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/resign")
    public Result<Map<String, Object>> getResignConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("tempDir", tempDir);
        
        Map<String, Object> tools = new HashMap<>();
        Map<String, Object> ios = new HashMap<>();
        ios.put("signTool", iosSignTool);
        ios.put("verifyTool", iosVerifyTool);
        
        Map<String, Object> android = new HashMap<>();
        android.put("signTool", androidSignTool);
        android.put("zipalignTool", androidZipalignTool);
        
        tools.put("ios", ios);
        tools.put("android", android);
        config.put("tools", tools);
        
        Map<String, Object> task = new HashMap<>();
        task.put("retryCount", retryCount);
        task.put("timeoutSeconds", timeoutSeconds);
        config.put("task", task);
        
        return Result.success(config);
    }

    /**
     * 保存重签名配置
     */
    @Operation(summary = "保存重签名配置", description = "保存重签名工具配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "保存成功")
    })
    @PostMapping("/resign")
    public Result<Boolean> saveResignConfig(@RequestBody Map<String, Object> config) {
        // 这里可以实现配置保存逻辑，比如更新配置文件或数据库
        log.info("保存重签名配置: {}", config);
        return Result.success(true);
    }

    /**
     * 获取MinIO配置
     */
    @Operation(summary = "获取MinIO配置", description = "获取MinIO对象存储配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/minio")
    public Result<Map<String, Object>> getMinioConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("endpoint", minioConfig.getEndpoint());
        config.put("bucketName", minioConfig.getBucketName());
        config.put("accessKey", minioConfig.getAccessKey());
        config.put("urlMode", minioConfig.getUrlMode());
        // 不返回secretKey，保护敏感信息
        return Result.success(config);
    }

    /**
     * 保存MinIO配置
     */
    @Operation(summary = "保存MinIO配置", description = "保存MinIO对象存储配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "保存成功")
    })
    @PostMapping("/minio")
    public Result<Boolean> saveMinioConfig(@RequestBody Map<String, Object> config) {
        // 这里可以实现配置保存逻辑
        log.info("保存MinIO配置: {}", config);
        return Result.success(true);
    }

    /**
     * 测试MinIO连接
     */
    @Operation(summary = "测试MinIO连接", description = "测试MinIO服务连接状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "测试成功")
    })
    @PostMapping("/minio/test")
    public Result<Boolean> testMinioConnection(@RequestBody Map<String, Object> config) {
        // 这里可以实现MinIO连接测试逻辑
        log.info("测试MinIO连接: {}", config);
        return Result.success(true);
    }

    /**
     * 获取RabbitMQ配置
     */
    @Operation(summary = "获取RabbitMQ配置", description = "获取RabbitMQ消息队列配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/rabbitmq")
    public Result<Map<String, Object>> getRabbitConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put("host", rabbitmqHost);
        config.put("port", rabbitmqPort);
        config.put("username", rabbitmqUsername);
        config.put("virtualHost", rabbitmqVirtualHost);
        // 不返回password，保护敏感信息
        return Result.success(config);
    }

    /**
     * 保存RabbitMQ配置
     */
    @Operation(summary = "保存RabbitMQ配置", description = "保存RabbitMQ消息队列配置信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "保存成功")
    })
    @PostMapping("/rabbitmq")
    public Result<Boolean> saveRabbitConfig(@RequestBody Map<String, Object> config) {
        // 这里可以实现配置保存逻辑
        log.info("保存RabbitMQ配置: {}", config);
        return Result.success(true);
    }

    /**
     * 测试RabbitMQ连接
     */
    @Operation(summary = "测试RabbitMQ连接", description = "测试RabbitMQ服务连接状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "测试成功")
    })
    @PostMapping("/rabbitmq/test")
    public Result<Boolean> testRabbitConnection(@RequestBody Map<String, Object> config) {
        // 这里可以实现RabbitMQ连接测试逻辑
        log.info("测试RabbitMQ连接: {}", config);
        return Result.success(true);
    }
}