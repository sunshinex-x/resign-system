package com.example.resign.controller;

import com.example.resign.model.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Tag(name = "健康检查", description = "系统健康状态检查相关接口")
@Slf4j
@RestController
@RequestMapping("/api/health")
@RequiredArgsConstructor
public class HealthController {

    private final DataSource dataSource;

    /**
     * 系统健康检查
     */
    @Operation(summary = "系统健康检查", description = "检查系统各组件的健康状态")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "健康检查完成")
    })
    @GetMapping("/check")
    public Result<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        // 检查数据库连接
        try (Connection connection = dataSource.getConnection()) {
            health.put("database", "UP");
            health.put("dbUrl", connection.getMetaData().getURL());
        } catch (Exception e) {
            log.error("数据库连接检查失败", e);
            health.put("database", "DOWN");
            health.put("dbError", e.getMessage());
        }
        
        // 检查应用状态
        health.put("application", "UP");
        health.put("timestamp", System.currentTimeMillis());
        
        return Result.success(health);
    }

    /**
     * 简单的ping接口
     */
    @Operation(summary = "Ping接口", description = "简单的连通性测试接口")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "连接正常")
    })
    @GetMapping("/ping")
    public Result<String> ping() {
        return Result.success("pong");
    }
}