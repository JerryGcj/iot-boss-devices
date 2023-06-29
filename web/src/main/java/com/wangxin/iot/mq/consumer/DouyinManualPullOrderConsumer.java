package com.wangxin.iot.mq.consumer;

import com.rabbitmq.client.Channel;
import com.wangxin.iot.card.IDouyinGatewayApiService;
import com.wangxin.iot.mq.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author: gcj
 * @Date: 2020/8/13
 */
@Component
@Slf4j
public class DouyinManualPullOrderConsumer implements BaseConsumer {

    @Autowired
    private IDouyinGatewayApiService douyinGatewayApiService;

    @Override
    public void consume(Message message, Channel channel) throws IOException {
        String orderId = new String(message.getBody());
        String realOrderId = orderId.replace("\"", "");
        log.info("mq接收抖音拉取订单请求，map : {}",realOrderId);
        douyinGatewayApiService.pullOneOrder("37636259",realOrderId);
    }
}
