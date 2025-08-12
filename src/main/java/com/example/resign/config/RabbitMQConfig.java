package com.example.resign.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 重签名任务队列名称
     */
    public static final String RESIGN_TASK_QUEUE = "resign.task.queue";

    /**
     * 重签名任务交换机名称
     */
    public static final String RESIGN_TASK_EXCHANGE = "resign.task.exchange";

    /**
     * 重签名任务路由键
     */
    public static final String RESIGN_TASK_ROUTING_KEY = "resign.task";

    /**
     * 重签名任务死信队列名称
     */
    public static final String RESIGN_TASK_DLQ = "resign.task.dlq";

    /**
     * 重签名任务死信交换机名称
     */
    public static final String RESIGN_TASK_DLX = "resign.task.dlx";

    /**
     * 重签名任务死信路由键
     */
    public static final String RESIGN_TASK_DL_ROUTING_KEY = "resign.task.dl";

    /**
     * 创建重签名任务队列
     *
     * @return Queue
     */
    @Bean
    public Queue resignTaskQueue() {
        return QueueBuilder.durable(RESIGN_TASK_QUEUE)
                .withArgument("x-dead-letter-exchange", RESIGN_TASK_DLX)
                .withArgument("x-dead-letter-routing-key", RESIGN_TASK_DL_ROUTING_KEY)
                .build();
    }

    /**
     * 创建重签名任务交换机
     *
     * @return DirectExchange
     */
    @Bean
    public DirectExchange resignTaskExchange() {
        return new DirectExchange(RESIGN_TASK_EXCHANGE);
    }

    /**
     * 绑定重签名任务队列和交换机
     *
     * @param resignTaskQueue    重签名任务队列
     * @param resignTaskExchange 重签名任务交换机
     * @return Binding
     */
    @Bean
    public Binding resignTaskBinding(Queue resignTaskQueue, DirectExchange resignTaskExchange) {
        return BindingBuilder.bind(resignTaskQueue).to(resignTaskExchange).with(RESIGN_TASK_ROUTING_KEY);
    }

    /**
     * 创建重签名任务死信队列
     *
     * @return Queue
     */
    @Bean
    public Queue resignTaskDLQ() {
        return QueueBuilder.durable(RESIGN_TASK_DLQ).build();
    }

    /**
     * 创建重签名任务死信交换机
     *
     * @return DirectExchange
     */
    @Bean
    public DirectExchange resignTaskDLX() {
        return new DirectExchange(RESIGN_TASK_DLX);
    }

    /**
     * 绑定重签名任务死信队列和死信交换机
     *
     * @param resignTaskDLQ 重签名任务死信队列
     * @param resignTaskDLX 重签名任务死信交换机
     * @return Binding
     */
    @Bean
    public Binding resignTaskDLBinding(Queue resignTaskDLQ, DirectExchange resignTaskDLX) {
        return BindingBuilder.bind(resignTaskDLQ).to(resignTaskDLX).with(RESIGN_TASK_DL_ROUTING_KEY);
    }

    /**
     * 消息转换器
     *
     * @return MessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * RabbitTemplate配置
     *
     * @param connectionFactory 连接工厂
     * @param messageConverter  消息转换器
     * @return RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, MessageConverter messageConverter) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter);
        return rabbitTemplate;
    }
}