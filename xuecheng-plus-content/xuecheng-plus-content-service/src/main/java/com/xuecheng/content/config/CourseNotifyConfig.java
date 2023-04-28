package com.xuecheng.content.config;

import com.alibaba.fastjson.JSON;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CourseNotifyConfig implements ApplicationContextAware {

    //课程发布通知交换机
    public static final String COURSE_NOTIFY_EXCHANGE = "course_notify_exchange";
    //通知minio队列
    public static final String MINIO_NOTIFY_QUEUE = "minio_notify_queue";
    //通知es队列
    public static final String ES_NOTIFY_QUEUE = "es_notify_queue";
    //通知redis队列
    public static final String REDIS_NOTIFY_QUEUE = "redis_notify_queue";

    public static final String COURSE_ROUTING_KEY = "course_routing_key";


    @Bean(COURSE_NOTIFY_EXCHANGE)
    public DirectExchange courseNotifyExchange(){
        return new DirectExchange(COURSE_NOTIFY_EXCHANGE);
    }

    @Bean(MINIO_NOTIFY_QUEUE)
    public Queue minioNotifyQueue(){
        return new Queue(MINIO_NOTIFY_QUEUE);
    }

    @Bean(ES_NOTIFY_QUEUE)
    public Queue esNotifyQueue(){
        return new Queue(ES_NOTIFY_QUEUE);
    }

    @Bean(REDIS_NOTIFY_QUEUE)
    public Queue redisNotifyQueue(){
        return new Queue(REDIS_NOTIFY_QUEUE);
    }

    @Bean
    Binding binding_minio(@Qualifier(MINIO_NOTIFY_QUEUE) Queue queue, @Qualifier(COURSE_NOTIFY_EXCHANGE) DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(COURSE_ROUTING_KEY);
    }

    @Bean
    Binding binding_es(@Qualifier(ES_NOTIFY_QUEUE) Queue queue, @Qualifier(COURSE_NOTIFY_EXCHANGE) DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(COURSE_ROUTING_KEY);
    }

    @Bean
    Binding binding_redis(@Qualifier(REDIS_NOTIFY_QUEUE) Queue queue, @Qualifier(COURSE_NOTIFY_EXCHANGE) DirectExchange exchange){
        return BindingBuilder.bind(queue).to(exchange).with(COURSE_ROUTING_KEY);
    }
    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        //消息处理service
        MqMessageService mqMessageService = applicationContext.getBean(MqMessageService.class);
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("消息发送失败，应答码{}，原因{}，交换机{}，路由键{},消息{}",
                    replyCode, replyText, exchange, routingKey, message.toString());
            MqMessage mqMessage = JSON.parseObject(message.toString(), MqMessage.class);
            //消息重发
            mqMessageService.addMessage(mqMessage.getMessageType(),mqMessage.getBusinessKey1(),mqMessage.getBusinessKey2(),mqMessage.getBusinessKey3());
        });
    }
}
