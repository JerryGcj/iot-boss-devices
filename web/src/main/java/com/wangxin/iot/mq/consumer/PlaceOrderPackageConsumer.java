package com.wangxin.iot.mq.consumer;

import com.alibaba.fastjson.JSON;
import com.rabbitmq.client.Channel;
import com.wangxin.iot.card.IIotCardOrderService;
import com.wangxin.iot.card.IOrderPackageService;
import com.wangxin.iot.constants.OrderStatus;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.model.IotCardOrder;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.mq.BaseConsumer;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
/**
 * @author: yanwin
 * @Date: 2020/8/13
 */
@Component
@Slf4j
public class PlaceOrderPackageConsumer implements BaseConsumer {

    @Autowired
    IOrderPackageService orderPackageService;
    @Autowired
    IIotCardOrderService cardOrderService;
    @Autowired
    IotRefCardCostMapper refCardCostMapper;
    @Autowired
    CardInformationMapper cardInformationMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void consume(Message message, Channel channel){
        String toDo = new String(message.getBody());
        String replace = toDo.replace("\"", "");
        log.info("mq接收到订购套餐请求，orderId : {}",replace);
        IotCardOrder cardOrder = cardOrderService.getById(replace);
        if(null!=cardOrder){
            cardOrderService.unicomOrderPackage(cardOrder);
        }else{
            Order order = orderPackageService.getById(replace);
            //不是处理中，就说明处理过了
            if(order.getOrderState() == OrderStatus.doing.getCode()||order.getOrderState() == OrderStatus.create.getCode()){
                //移动
                if(order.getOperatorType()==1){
                    String realNameInfo = cardInformationMapper.getRealNameByIccid(order.getIccid());
                    if("1".equals(realNameInfo)){
                        orderPackageService.wechatOrderPackage(order);
                    }else{
                        //把未实名的订单存redis,待实名认证后，订购套餐。
                        redisUtil.hset(RedisKeyConstants.UN_REAL_NAME_ORDER.getMessage(),order.getIccid()+"_"+order.getId(), JSON.toJSONString(order));
                    }
                }
                if(order.getOperatorType()==2){
                    orderPackageService.unicomOrderPackage(order);
                }
                if(order.getOperatorType()==3){
                    //电信
                    orderPackageService.telecomOrderPackage(order);
                }
            }else{
                log.info("mq订购套餐请求已处理，不在处理，orderId : {}",replace);
            }
        }
    }
}
