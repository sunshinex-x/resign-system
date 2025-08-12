package com.example.resign.audit;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.UUID;

/**
 * 审计日志拦截器，用于捕获HTTP请求并记录审计信息
 */
@Component
public class AuditLogInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogInterceptor.class);
    
    @Autowired
    private AuditLogService auditLogService;
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 生成请求ID
        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);
        
        // 获取客户端IP
        String clientIp = getClientIp(request);
        MDC.put("clientIp", clientIp);
        
        // 获取用户ID（如果有认证系统）
        String userId = getUserId(request);
        MDC.put("userId", userId != null ? userId : "anonymous");
        
        // 获取操作类型
        String action = request.getMethod() + " " + request.getRequestURI();
        MDC.put("action", action);
        
        // 记录请求开始
        logger.info("Request started: {} {} from IP: {}", request.getMethod(), request.getRequestURI(), clientIp);
        
        return true;
    }
    
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        // 可以在这里添加额外的处理逻辑
    }
    
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        // 记录请求结束
        logger.info("Request completed: {} {} with status: {}", request.getMethod(), request.getRequestURI(), response.getStatus());
        
        // 如果发生异常，记录错误信息
        if (ex != null) {
            logger.error("Request error: {}", ex.getMessage(), ex);
        }
        
        // 清理MDC
        MDC.clear();
    }
    
    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果是多级代理，取第一个IP地址
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
    
    /**
     * 获取用户ID，根据实际认证系统修改
     */
    private String getUserId(HttpServletRequest request) {
        // 这里根据实际认证系统获取用户ID
        // 例如，从请求头、Session或JWT令牌中获取
        return request.getHeader("X-User-ID");
    }
}