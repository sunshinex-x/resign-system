package com.example.resign.service;

import com.example.resign.config.RabbitMQConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 重签名任务消息服务
 * 用于发送任务消息到RabbitMQ队列
 */
@Slf4j
@Service
public class ResignTaskMessageService {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 发送iOS重签名任务消息
     * 
     * @param taskId 任务ID
     */
    public void sendIosResignTaskMessage(Long taskId) {
        sendResignTaskMessage("IOS", taskId);
    }

    /**
     * 发送Android重签名任务消息
     * 
     * @param taskId 任务ID
     */
    public void sendAndroidResignTaskMessage(Long taskId) {
        sendResignTaskMessage("ANDROID", taskId);
    }

    /**
     * 发送重签名任务消息到队列
     * 
     * @param appType 应用类型（IOS/ANDROID）
     * @param taskId 任务ID
     */
    private void sendResignTaskMessage(String appType, Long taskId) {
        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("appType", appType);
            messageData.put("taskId", taskId);
            messageData.put("timestamp", System.currentTimeMillis());
            
            String messageJson = objectMapper.writeValueAsString(messageData);
            
            rabbitTemplate.convertAndSend(
                RabbitMQConfig.RESIGN_TASK_EXCHANGE,
                RabbitMQConfig.RESIGN_TASK_ROUTING_KEY,
                messageJson
            );
            
            log.info("发送{}重签名任务消息成功: taskId={}", appType, taskId);
            
        } catch (Exception e) {
            log.error("发送{}重签名任务消息失败: taskId={}", appType, taskId, e);
            throw new RuntimeException("发送重签名任务消息失败", e);
        }
    }
}