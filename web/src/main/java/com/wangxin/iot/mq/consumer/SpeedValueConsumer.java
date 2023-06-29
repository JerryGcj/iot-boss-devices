package com.wangxin.iot.mq.consumer;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.mq.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * @author: gcj
 * @Date: 2020/8/13
 */
@Component
@Slf4j
public class SpeedValueConsumer implements BaseConsumer {

    @Autowired
    private ITelecomGatewayService telecomGatewayService;

    @Override
    public void consume(Message message, Channel channel) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JavaType javaType = mapper.getTypeFactory().constructMapType(Map.class,String.class,String.class);
        Map<String,String> map = mapper.readValue(message.getBody(),javaType);
        log.info("mq接收到自主限速请求，map : {}",map);
        telecomGatewayService.setSpeedValue(map);
    }
}
