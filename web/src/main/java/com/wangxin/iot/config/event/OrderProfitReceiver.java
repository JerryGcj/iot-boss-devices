package com.wangxin.iot.config.event;

import com.wangxin.iot.event.WxPayCallbackEvent;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.utils.ApplicationContextUtil;
import lombok.extern.slf4j.Slf4j;

/**
 * @author: yanwin
 * @Date: 2020/5/25
 * @Desc:订单支付状态不确定，通过查询成功后，发送到此订阅模型。处理分润
 */
@Slf4j
public class OrderProfitReceiver {

    public void receiveMessage(String orderId){
        log.info("redis subscrible order_profits_channel 订单：{} 通过查询确实支付成功，进行分润",orderId);
        OrderMapper orderMapper = (OrderMapper) ApplicationContextUtil.applicationContext.getBean("orderMapper");
        Order order = orderMapper.selectById(orderId);
        if(order != null){
            ApplicationContextUtil.applicationContext.publishEvent(new WxPayCallbackEvent(order));
        }
    }
}
