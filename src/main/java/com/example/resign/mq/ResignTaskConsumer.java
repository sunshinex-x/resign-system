package com.example.resign.mq;

import cn.hutool.core.bean.BeanUtil;
import com.example.resign.config.RabbitMQConfig;
import com.example.resign.entity.ResignTask;
import com.example.resign.model.vo.ResignTaskVO;
import com.example.resign.service.ResignTaskService;
import com.rabbitmq.client.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 重签名任务消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ResignTaskConsumer {

    private final ResignTaskService resignTaskService;

    /**
     * 处理重签名任务
     *
     * @param taskId  任务ID
     * @param message 消息
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.RESIGN_TASK_QUEUE)
    public void processResignTask(String taskId, Message message, Channel channel) {
        log.info("收到重签名任务: {}", taskId);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 查询任务
            ResignTaskVO taskVO = resignTaskService.getTaskById(taskId);
            ResignTask task = new ResignTask();
            BeanUtil.copyProperties(taskVO, task);

            if (task == null) {
                log.error("任务不存在: {}", taskId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 处理任务
            resignTaskService.processTask(task);

            // 确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理重签名任务失败: {}", taskId, e);
            try {
                // 拒绝消息并重新入队
                channel.basicNack(deliveryTag, false, true);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }

    /**
     * 处理死信队列中的任务
     *
     * @param taskId  任务ID
     * @param message 消息
     * @param channel 通道
     */
    @RabbitListener(queues = RabbitMQConfig.RESIGN_TASK_DLQ)
    public void processDLQ(String taskId, Message message, Channel channel) {
        log.info("收到死信队列任务: {}", taskId);
        long deliveryTag = message.getMessageProperties().getDeliveryTag();

        try {
            // 查询任务
            ResignTaskVO taskVO = resignTaskService.getTaskById(taskId);
            ResignTask task = new ResignTask();
            BeanUtil.copyProperties(taskVO, task);

            if (task == null) {
                log.error("任务不存在: {}", taskId);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 更新任务状态为失败
            resignTaskService.updateTaskStatus(taskId, "FAILED", null, "任务处理超时或失败");

            // 确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("处理死信队列任务失败: {}", taskId, e);
            try {
                // 拒绝消息但不重新入队
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                log.error("拒绝消息失败", ex);
            }
        }
    }
}