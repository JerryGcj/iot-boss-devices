package com.wangxin.iot.mq.listener;

import com.rabbitmq.client.Channel;
import com.wangxin.iot.config.RabbitConfig;
import com.wangxin.iot.mq.consumer.SpeedValueConsumer;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SpeedValueListener {

    @Autowired
    private SpeedValueConsumer speedValueConsumer;

    @RabbitListener(queues = RabbitConfig.SELF_LIMITING_SPEED_QUEUE_NAME)
    public void consume(Message message, Channel channel) throws IOException {
        //基于spring的代理实现
        speedValueConsumer.consume(message,channel);
    }

}
