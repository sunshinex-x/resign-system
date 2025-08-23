package com.example.resign.controller;

import com.example.resign.model.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 系统信息控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Tag(name = "系统信息管理", description = "系统信息查询相关接口")
public class SystemInfoController {

    @Value("${spring.application.name:resign-system}")
    private String applicationName;

    @Value("${server.port:8080}")
    private String serverPort;

    /**
     * 获取系统信息
     */
    @Operation(summary = "获取系统信息", description = "获取系统运行状态和基本信息")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "获取成功")
    })
    @GetMapping("/info")
    public Result<Map<String, Object>> getSystemInfo() {
        Map<String, Object> systemInfo = new HashMap<>();
        
        // 应用信息
        Map<String, Object> appInfo = new HashMap<>();
        appInfo.put("name", applicationName);
        appInfo.put("version", "1.0.0");
        appInfo.put("port", serverPort);
        appInfo.put("startTime", getApplicationStartTime());
        appInfo.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        systemInfo.put("application", appInfo);
        
        // 系统信息
        Map<String, Object> osInfo = new HashMap<>();
        Properties props = System.getProperties();
        osInfo.put("name", props.getProperty("os.name"));
        osInfo.put("version", props.getProperty("os.version"));
        osInfo.put("arch", props.getProperty("os.arch"));
        systemInfo.put("os", osInfo);
        
        // Java信息
        Map<String, Object> javaInfo = new HashMap<>();
        javaInfo.put("version", props.getProperty("java.version"));
        javaInfo.put("vendor", props.getProperty("java.vendor"));
        javaInfo.put("home", props.getProperty("java.home"));
        systemInfo.put("java", javaInfo);
        
        // 内存信息
        Map<String, Object> memoryInfo = getMemoryInfo();
        systemInfo.put("memory", memoryInfo);
        
        // 运行时信息
        Map<String, Object> runtimeInfo = getRuntimeInfo();
        systemInfo.put("runtime", runtimeInfo);
        
        return Result.success(systemInfo);
    }
    
    /**
     * 获取应用启动时间
     */
    private String getApplicationStartTime() {
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            long startTime = runtimeMXBean.getStartTime();
            LocalDateTime startDateTime = LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(startTime), 
                java.time.ZoneId.systemDefault()
            );
            return startDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        } catch (Exception e) {
            log.warn("获取应用启动时间失败", e);
            return "未知";
        }
    }
    
    /**
     * 获取内存信息
     */
    private Map<String, Object> getMemoryInfo() {
        Map<String, Object> memoryInfo = new HashMap<>();
        try {
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            
            // 堆内存
            long heapUsed = memoryMXBean.getHeapMemoryUsage().getUsed();
            long heapMax = memoryMXBean.getHeapMemoryUsage().getMax();
            long heapCommitted = memoryMXBean.getHeapMemoryUsage().getCommitted();
            
            memoryInfo.put("heapUsed", formatBytes(heapUsed));
            memoryInfo.put("heapMax", formatBytes(heapMax));
            memoryInfo.put("heapCommitted", formatBytes(heapCommitted));
            memoryInfo.put("heapUsedPercent", String.format("%.2f%%", (double) heapUsed / heapMax * 100));
            
            // 非堆内存
            long nonHeapUsed = memoryMXBean.getNonHeapMemoryUsage().getUsed();
            long nonHeapMax = memoryMXBean.getNonHeapMemoryUsage().getMax();
            long nonHeapCommitted = memoryMXBean.getNonHeapMemoryUsage().getCommitted();
            
            memoryInfo.put("nonHeapUsed", formatBytes(nonHeapUsed));
            memoryInfo.put("nonHeapMax", nonHeapMax > 0 ? formatBytes(nonHeapMax) : "无限制");
            memoryInfo.put("nonHeapCommitted", formatBytes(nonHeapCommitted));
            
        } catch (Exception e) {
            log.warn("获取内存信息失败", e);
            memoryInfo.put("error", "获取内存信息失败");
        }
        return memoryInfo;
    }
    
    /**
     * 获取运行时信息
     */
    private Map<String, Object> getRuntimeInfo() {
        Map<String, Object> runtimeInfo = new HashMap<>();
        try {
            RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
            
            runtimeInfo.put("uptime", formatDuration(runtimeMXBean.getUptime()));
            runtimeInfo.put("vmName", runtimeMXBean.getVmName());
            runtimeInfo.put("vmVersion", runtimeMXBean.getVmVersion());
            runtimeInfo.put("vmVendor", runtimeMXBean.getVmVendor());
            
            // 处理器信息
            int processors = Runtime.getRuntime().availableProcessors();
            runtimeInfo.put("processors", processors);
            
        } catch (Exception e) {
            log.warn("获取运行时信息失败", e);
            runtimeInfo.put("error", "获取运行时信息失败");
        }
        return runtimeInfo;
    }
    
    /**
     * 格式化字节数
     */
    private String formatBytes(long bytes) {
        if (bytes < 1024) {
            return bytes + " B";
        }
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.2f %sB", bytes / Math.pow(1024, exp), pre);
    }
    
    /**
     * 格式化持续时间
     */
    private String formatDuration(long millis) {
        long seconds = millis / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return String.format("%d天 %d小时 %d分钟", days, hours % 24, minutes % 60);
        } else if (hours > 0) {
            return String.format("%d小时 %d分钟", hours, minutes % 60);
        } else if (minutes > 0) {
            return String.format("%d分钟 %d秒", minutes, seconds % 60);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}