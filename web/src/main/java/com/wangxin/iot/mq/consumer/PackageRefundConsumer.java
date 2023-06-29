package com.wangxin.iot.mq.consumer;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.rabbitmq.client.Channel;
import com.wangxin.iot.card.*;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.mq.BaseConsumer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/8/13
 */
@Component
@Slf4j
public class PackageRefundConsumer implements BaseConsumer {

    @Autowired
    private IUnicomGatewayService iUnicomGatewayService;
    @Autowired
    private IIotUnicomCardInfoService unicomCardInfoService;
    @Autowired
    private IotUnicomRefCardCostService unicomRefCardCostService;

    @Override
    public void consume(Message message, Channel channel){
        String toDo = new String(message.getBody());
        String replace = toDo.replace("\"", "");
        log.info("mq接收到套餐退款请求，refId : {}",replace);
        IotUnicomRefCardCost unicomRefCardCost = null;
        IotUnicomRefCardCost refCardCost = new IotUnicomRefCardCost();
        if(replace.contains(",")){
            unicomRefCardCost = unicomRefCardCostService.getById(replace.split(",")[0]);
            //将套餐退款状态改为退款成功
            refCardCost.setRefundStatus(replace.split(",")[1]);
            refCardCost.setActive(4);
            refCardCost.setValidEnd(new Date());
            if(unicomRefCardCost.getActive()==0){
                refCardCost.setValidEnd(unicomRefCardCost.getValidStart());
            }
            refCardCost.setId(replace.split(",")[0]);
        }else{
            unicomRefCardCost = unicomRefCardCostService.getById(replace);
            //将套餐退款状态改为退款中
            refCardCost.setRefundStatus("0");
            refCardCost.setId(replace);
        }
        unicomRefCardCostService.updateById(refCardCost);
        if(null!=unicomRefCardCost){
            if(unicomRefCardCost.getActive()==1){
                //给卡停机
                Map businessMap = new HashMap(3);
                businessMap.put("action","5");
                businessMap.put("goalState","3");
                businessMap.put("iccid",unicomRefCardCost.getIccid());
                //操作卡
                iUnicomGatewayService.updateCardStatus(businessMap);
            }
        }
    }
}
