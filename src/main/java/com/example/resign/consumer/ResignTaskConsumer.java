package com.example.resign.consumer;

import com.example.resign.config.RabbitMQConfig;
import com.example.resign.entity.AndroidResignTask;
import com.example.resign.entity.IosResignTask;
import com.example.resign.service.AndroidResignTaskService;
import com.example.resign.service.IosResignTaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 重签名任务消息队列消费者
 * 用于异步处理重签名任务
 */
@Slf4j
@Component
public class ResignTaskConsumer {

    @Autowired
    private AndroidResignTaskService androidResignTaskService;

    @Autowired
    private IosResignTaskService iosResignTaskService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 处理重签名任务消息
     * 
     * @param message 消息内容
     */
    @RabbitListener(queues = RabbitMQConfig.RESIGN_TASK_QUEUE)
    public void processResignTask(Message message) {
        try {
            String messageBody = new String(message.getBody());
            log.info("收到重签名任务消息: {}", messageBody);
            
            // 解析消息内容
            Map<String, Object> taskData = objectMapper.readValue(messageBody, Map.class);
            String appType = (String) taskData.get("appType");
            Long taskId = Long.valueOf(taskData.get("taskId").toString());
            
            if ("IOS".equalsIgnoreCase(appType)) {
                // 处理iOS重签名任务
                processIosResignTask(taskId);
            } else if ("ANDROID".equalsIgnoreCase(appType)) {
                // 处理Android重签名任务
                processAndroidResignTask(taskId);
            } else {
                log.error("不支持的应用类型: {}", appType);
            }
            
        } catch (Exception e) {
            log.error("处理重签名任务消息失败", e);
            throw new RuntimeException("处理重签名任务消息失败", e);
        }
    }

    /**
     * 处理iOS重签名任务
     * 
     * @param taskId 任务ID
     */
    private void processIosResignTask(Long taskId) {
        try {
            log.info("开始处理iOS重签名任务: {}", taskId);
            
            // 获取任务信息
            IosResignTask task = iosResignTaskService.getTaskById(taskId);
            if (task == null) {
                log.error("iOS重签名任务不存在: {}", taskId);
                return;
            }
            
            // 执行重签名
            iosResignTaskService.executeResignTask(taskId);
            
            log.info("iOS重签名任务处理完成: {}", taskId);
            
        } catch (Exception e) {
            log.error("处理iOS重签名任务失败: {}", taskId, e);
            // 更新任务状态为失败
            try {
                iosResignTaskService.updateTaskStatus(taskId, "FAILED", e.getMessage());
            } catch (Exception updateException) {
                log.error("更新iOS任务状态失败: {}", taskId, updateException);
            }
        }
    }

    /**
     * 处理Android重签名任务
     * 
     * @param taskId 任务ID
     */
    private void processAndroidResignTask(Long taskId) {
        try {
            log.info("开始处理Android重签名任务: {}", taskId);
            
            // 获取任务信息
            AndroidResignTask task = androidResignTaskService.getTaskById(taskId);
            if (task == null) {
                log.error("Android重签名任务不存在: {}", taskId);
                return;
            }
            
            // 执行重签名
            androidResignTaskService.executeResignTask(taskId);
            
            log.info("Android重签名任务处理完成: {}", taskId);
            
        } catch (Exception e) {
            log.error("处理Android重签名任务失败: {}", taskId, e);
            // 更新任务状态为失败
            try {
                androidResignTaskService.updateTaskStatus(taskId, "FAILED", e.getMessage());
            } catch (Exception updateException) {
                log.error("更新Android任务状态失败: {}", taskId, updateException);
            }
        }
    }
}