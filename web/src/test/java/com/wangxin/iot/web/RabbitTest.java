package com.wangxin.iot.web;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.wangxin.iot.WebApplication;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.utils.UUIDUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by 18765 on 2020/1/2 14:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class RabbitTest {
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Test
    public void publish(){
        RealNameSystem nameSystem = realNameSystemMapper.selectById("eec59cedfffcf809267519d8f01e4db0");
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(nameSystem);
        //rabbitTemplate.convertAndSend("realName.exchange","realName.routing.key", jsonObject);
        System.out.println("当前时间："+new Date());
        String msg = "这是一条延迟消息4332";
        rabbitTemplate.convertAndSend("delayed.exchange"
                , "delayed.routingkey", "这是一条延迟消息1", message -> {
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setHeader("x-delay", 60000);
                    return message;
                });
        rabbitTemplate.convertAndSend("delayed.exchange"
                , "delayed.routingkey", "这是一条延迟消息2", message -> {
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setHeader("x-delay", 50000);
                    return message;
                });
        rabbitTemplate.convertAndSend("delayed.exchange"
                , "delayed.routingkey", "这是一条延迟消息3", message -> {
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setHeader("x-delay", 40000);
                    return message;
                });
        rabbitTemplate.convertAndSend("delayed.exchange"
                , "delayed.routingkey", "这是一条延迟消息4", message -> {
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setHeader("x-delay", 30000);
                    return message;
                });
        rabbitTemplate.convertAndSend("delayed.exchange"
                , "delayed.routingkey", "这是一条延迟消息5", message -> {
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setHeader("x-delay", 25000);
                    return message;
                });
        rabbitTemplate.convertAndSend("delayed.exchange"
                , "delayed.routingkey", "这是一条延迟消息6", message -> {
                    //设置发送消息的延迟时间
                    message.getMessageProperties().setHeader("x-delay", 15000);
                    return message;
                });
    }

}
