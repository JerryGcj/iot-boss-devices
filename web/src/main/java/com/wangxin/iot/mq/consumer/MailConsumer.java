package com.wangxin.iot.mq.consumer;

import com.rabbitmq.client.Channel;
import com.wangxin.iot.mq.BaseConsumer;
import com.wangxin.iot.mq.helper.MessageHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: yanwin
 * @Date: 2020/8/10
 */
@Component
@Slf4j
public class MailConsumer implements BaseConsumer {
    @Override
    public void consume(Message message, Channel channel) throws IOException {
        String str = new String(message.getBody());
//        String s = MessageHelper.msgToObj(message, String.class);
//        System.out.println(new String((message.getBody())));
        log.info("收到消息: {}",str);
    }
}
