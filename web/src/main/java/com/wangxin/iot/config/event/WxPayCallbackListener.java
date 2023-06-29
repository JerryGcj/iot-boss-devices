package com.wangxin.iot.config.event;

import com.wangxin.iot.card.ICustomerSalesDiscountService;
import com.wangxin.iot.card.IotTelecomRefCardCostService;
import com.wangxin.iot.event.WxPayCallbackEvent;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * @author: yanwin
 * @Date: 2020/4/14
 * @desc:公众号订购套餐事件监听
 */
@Component
@Slf4j
public class WxPayCallbackListener implements ApplicationListener<WxPayCallbackEvent> {
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ICustomerSalesDiscountService customerSalesDiscountService;
    @Autowired
    IotRefCardCostMapper iotRefCardCostMapper;

    @Autowired
    IotTelecomRefCardCostService telecomRefCardCostService;

    @Override
    public void onApplicationEvent(WxPayCallbackEvent event) {
        Order order = (Order)event.getSource();
        log.info("spring监听到订单事件：{}",order.toString());
        //接口或者公众号订购的，走这个逻辑
        //联通订购套餐

    }
}
