package com.wangxin.iot.mq.consumer;

import com.rabbitmq.client.Channel;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.mq.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author: yanwin
 * @Date: 2020/8/13
 */
@Component
@Slf4j
public class RegulateFlowConsumer implements BaseConsumer {

    @Autowired
    IUnicomGatewayService unicomGatewayService;

    @Override
    public void consume(Message message, Channel channel) throws IOException {
        String toDo = new String(message.getBody());
        String replace = toDo.replace("\"", "");
        log.info("接收到流量较正 flow 队列中的消息：{}",replace);
        Set<String> iccids = new HashSet<>();
        if(replace.contains(",")){
            iccids.addAll(Arrays.asList(replace.split(",")));
        }else{
            iccids.add(replace);
        }
        unicomGatewayService.regulateFlow(iccids);
    }
}
