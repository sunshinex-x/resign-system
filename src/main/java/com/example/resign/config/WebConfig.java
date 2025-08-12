package com.example.resign.config;

import com.example.resign.audit.AuditLogInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类，用于注册拦截器等Web相关配置
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private AuditLogInterceptor auditLogInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册审计日志拦截器，拦截所有API请求
        registry.addInterceptor(auditLogInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/health", "/api/metrics");
    }
}