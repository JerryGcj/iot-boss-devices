package com.wangxin.iot.mq.listener;

import com.rabbitmq.client.Channel;
import com.wangxin.iot.config.RabbitConfig;
import com.wangxin.iot.mq.consumer.PackageRefundConsumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class PackageRefundListener {

    @Autowired
    private PackageRefundConsumer packageRefundConsumer;

    @RabbitListener(queues = RabbitConfig.PACKAGE_REFUND_QUEUE_NAME)
    public void consume(Message message, Channel channel) throws IOException {
        //基于spring的代理实现
        packageRefundConsumer.consume(message,channel);
    }

}
