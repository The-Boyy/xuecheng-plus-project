package com.xuecheng.orders.config;

import com.alibaba.fastjson.JSON;
import com.xuecheng.messagesdk.model.po.MqMessage;
import com.xuecheng.messagesdk.service.MqMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Mr.M
 * @version 1.0
 * @description TODO
 * @date 2023/2/23 16:59
 */
@Slf4j
@Configuration
public class PayNotifyConfig implements ApplicationContextAware {

    //交换机
    public static final String PAY_NOTIFY_EXCHANGE = "pay_notify_exchange";
    //支付结果通知消息类型
    public static final String PAY_NOTIFY = "pay_notify";
    public static final String PAY_PROCESS_QUEUE = "pay_process_queue";
    public static final String PAY_PROCESS_FAIL_QUEUE = "pay_process_fail_queue";
    public static final String PAY_PROCESS_KEY = "pay_process_key";
    public static final String PAY_PROCESS_FAIL_KEY = "pay_process_fail_key";
    //声明交换机
    @Bean(PAY_NOTIFY_EXCHANGE)
    public DirectExchange pay_notify_exchange() {
        return new DirectExchange(PAY_NOTIFY_EXCHANGE);
    }
    //支付处理队列
    @Bean(PAY_PROCESS_QUEUE)
    public Queue pay_process_queue() {
        return new Queue(PAY_PROCESS_QUEUE);
    }

    //支付处理失败队列
    @Bean(PAY_PROCESS_FAIL_QUEUE)
    public Queue pay_process_fail_queue() {
        return new Queue(PAY_PROCESS_FAIL_QUEUE);
    }

    //交换机和支付通知队列绑定
    @Bean
    public Binding binding_pay_process_queue(@Qualifier(PAY_PROCESS_QUEUE) Queue queue, @Qualifier(PAY_NOTIFY_EXCHANGE) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PAY_PROCESS_KEY);
    }
    @Bean
    public Binding binding_pay_process_fail_queue(@Qualifier(PAY_PROCESS_FAIL_QUEUE) Queue queue, @Qualifier(PAY_NOTIFY_EXCHANGE) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PAY_PROCESS_FAIL_KEY);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 获取RabbitTemplate
        RabbitTemplate rabbitTemplate = applicationContext.getBean(RabbitTemplate.class);
        //消息处理service
        MqMessageService mqMessageService = applicationContext.getBean(MqMessageService.class);
        // 设置ReturnCallback
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            // 投递失败，记录日志
            log.info("消息发送失败，应答码{}，原因{}，交换机{}，路由键{},消息{}",
                    replyCode, replyText, exchange, routingKey, message.toString());
            MqMessage mqMessage = JSON.parseObject(message.toString(), MqMessage.class);
            //将消息再添加到消息表
            mqMessageService.addMessage(mqMessage.getMessageType(),mqMessage.getBusinessKey1(),mqMessage.getBusinessKey2(),mqMessage.getBusinessKey3());
        });
    }
}
