package com.xuecheng.learning.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Qualifier;
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
public class PayNotifyConfig {

    //交换机
    public static final String PAY_NOTIFY_EXCHANGE = "pay_notify_exchange";
    public static final String PAY_PROCESS_QUEUE = "pay_process_queue";
    public static final String PAY_PROCESS_KEY = "pay_process_key";

    @Bean(PAY_NOTIFY_EXCHANGE)
    public DirectExchange pay_notify_exchange() {
        return new DirectExchange(PAY_NOTIFY_EXCHANGE);
    }
    //支付处理队列
    @Bean(PAY_PROCESS_QUEUE)
    public Queue pay_process_queue() {
        return new Queue(PAY_PROCESS_QUEUE);
    }

    //交换机和支付通知队列绑定
    @Bean
    public Binding binding_pay_process_queue(@Qualifier(PAY_PROCESS_QUEUE) Queue queue, @Qualifier(PAY_NOTIFY_EXCHANGE) DirectExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(PAY_PROCESS_KEY);
    }
}
