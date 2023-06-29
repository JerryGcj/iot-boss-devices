package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotCardOrderService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.card.IotUnicomRefCardCostService;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.mapper.IotCardOrderMapper;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.model.IotCardOrder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @Description: electron_channel_order_pay
 * @Author: jeecg-boot
 * @Date:   2021-09-29
 * @Version: V1.0
 */
@Service
@Slf4j
public class IotCardOrderServiceImpl extends ServiceImpl<IotCardOrderMapper, IotCardOrder> implements IIotCardOrderService {

    @Autowired
    private IotUnicomCardInfoMapper unicomCardInfoMapper;
    @Autowired
    private IotUnicomRefCardCostService unicomRefCardCostService;
    @Autowired
    private IUnicomGatewayService iIoTGatewayApiService;

    @Override
    public void unicomOrderPackage(IotCardOrder cardOrder) {
        QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
        if(cardOrder.getIccid().length()==10){
            queryWrapper.eq("custom_iccid", cardOrder.getIccid());
        }
        if(cardOrder.getIccid().length()==19){
            queryWrapper.eq("virtual_iccid", cardOrder.getIccid());
        }
        if(cardOrder.getIccid().length()==20){
            queryWrapper.eq("iccid", cardOrder.getIccid());
        }
        IotUnicomCardInfo cardInfo = unicomCardInfoMapper.selectOne(queryWrapper);
        if(null!=cardInfo){
            if(cardInfo.getSimStatus()!=2){
                log.info("卡号：iccid：{}，订购了套餐但不是激活状态，自动激活",cardOrder.getIccid());
                Map businessMap = new HashMap(3);
                //发生位置是公众号
                businessMap.put("action","2");
                businessMap.put("goalState","2");
                businessMap.put("iccid",cardInfo.getIccid());
                //激活卡
                iIoTGatewayApiService.updateCardStatus(businessMap);
            }
        }
        //存流量关系表
        unicomRefCardCostService.saveRefWithCardOrder(cardOrder);
    }
}
