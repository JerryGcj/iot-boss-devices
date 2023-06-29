package com.wangxin.iot.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private CachingConnectionFactory connectionFactory;


    @Bean
    public RabbitTemplate rabbitTemplate() {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(converter());

        // 消息是否成功发送到Exchange
        rabbitTemplate.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                log.info("消息成功发送到Exchange");
//                String msgId = correlationData.getId();
//                msgLogService.updateStatus(msgId, Constant.MsgLogStatus.DELIVER_SUCCESS);
            } else {
                log.info("消息发送到Exchange失败, {}, cause: {}", correlationData, cause);
            }
        });

        // 触发setReturnCallback回调必须设置mandatory=true, 否则Exchange没有找到Queue就会丢弃掉消息, 而不会触发回调
        rabbitTemplate.setMandatory(true);
        // 消息是否从Exchange路由到Queue, 注意: 这是一个失败回调, 只有消息从Exchange路由到Queue失败才会回调这个方法
        rabbitTemplate.setReturnCallback((message, replyCode, replyText, exchange, routingKey) -> {
            log.info("消息从Exchange路由到Queue失败: exchange: {}, route: {}, replyCode: {}, replyText: {}, message: {}", exchange, routingKey, replyCode, replyText, message);
        });

        return rabbitTemplate;
    }

    @Bean
    public Jackson2JsonMessageConverter converter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 订购套餐
     */
    public static final String PLACE_ORDER_QUEUE_NAME = "place_order_douyin.queue";
    public static final String PLACE_ORDER_EXCHANGE_NAME = "place_order_douyin.exchange";
    public static final String PLACE_ORDER_ROUTING_KEY_NAME = "place_order_douyin.routing.key";

    @Bean
    public Queue orderQueue() {
        return new Queue(PLACE_ORDER_QUEUE_NAME, true);
    }
    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(PLACE_ORDER_EXCHANGE_NAME, true, false);
    }
    @Bean
    public Binding orderBinding() {
        return BindingBuilder.bind(orderQueue()).to(orderExchange()).with(PLACE_ORDER_ROUTING_KEY_NAME);
    }

    public static final String SELF_LIMITING_SPEED_QUEUE_NAME = "self_limiting_speed.queue";
    public static final String SELF_LIMITING_SPEED_EXCHANGE_NAME = "self_limiting_speed.exchange";
    public static final String SELF_LIMITING_SPEED_ROUTING_KEY_NAME = "self_limiting_speed.routing.key";

    @Bean
    public Queue speedQueue() {
        return new Queue(SELF_LIMITING_SPEED_QUEUE_NAME, true);
    }
    @Bean
    public DirectExchange speedExchange() {
        return new DirectExchange(SELF_LIMITING_SPEED_EXCHANGE_NAME, true, false);
    }
    @Bean
    public Binding speedBinding() {
        return BindingBuilder.bind(speedQueue()).to(speedExchange()).with(SELF_LIMITING_SPEED_ROUTING_KEY_NAME);
    }

    /**
     * 流量校验
     */
    public static final String FLOW_QUEUE_NAME = "flow.queue";
    public static final String FLOW_EXCHANGE_NAME = "flow.exchange";
    public static final String FLOW_ROUTING_KEY_NAME = "flow.routing.key";

    @Bean
    public Queue mailQueue() {
        return new Queue(FLOW_QUEUE_NAME, true);
    }

    @Bean
    public DirectExchange mailExchange() {
        return new DirectExchange(FLOW_EXCHANGE_NAME, true, false);
    }

    @Bean
    public Binding mailBinding() {
        return BindingBuilder.bind(mailQueue()).to(mailExchange()).with(FLOW_ROUTING_KEY_NAME);
    }

    /**
     * 套餐退款
     */
    public static final String PACKAGE_REFUND_QUEUE_NAME = "package_refund.queue";
    public static final String PACKAGE_REFUND_EXCHANGE_NAME = "package_refund.exchange";
    public static final String PACKAGE_REFUND_ROUTING_KEY_NAME = "package_refund.routing.key";

    @Bean
    public Queue packageRefundQueue() {
        return new Queue(PACKAGE_REFUND_QUEUE_NAME, true);
    }
    @Bean
    public DirectExchange packageRefundExchange() {
        return new DirectExchange(PACKAGE_REFUND_EXCHANGE_NAME, true, false);
    }
    @Bean
    public Binding packageRefundBinding() {
        return BindingBuilder.bind(packageRefundQueue()).to(packageRefundExchange()).with(PACKAGE_REFUND_ROUTING_KEY_NAME);
    }
    /**
     * 手动拉取抖店订单
     */
    public static final String DOUDIAN_ORDER_QUEUE_NAME = "doudian_order.queue";
    public static final String DOUDIAN_ORDER_EXCHANGE_NAME = "doudian_order.exchange";
    public static final String DOUDIAN_ORDER_ROUTING_KEY_NAME = "doudian_order.routing.key";

    @Bean
    public Queue doudianOrderQueue() {
        return new Queue(DOUDIAN_ORDER_QUEUE_NAME, true);
    }
    @Bean
    public DirectExchange doudianOrderExchange() {
        return new DirectExchange(DOUDIAN_ORDER_EXCHANGE_NAME, true, false);
    }
    @Bean
    public Binding doudianOrderBinding() {
        return BindingBuilder.bind(doudianOrderQueue()).to(doudianOrderExchange()).with(DOUDIAN_ORDER_ROUTING_KEY_NAME);
    }
}
