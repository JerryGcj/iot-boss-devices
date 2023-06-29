package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotRechargeOrderService;
import com.wangxin.iot.constants.PayStatus;
import com.wangxin.iot.domain.IotRechargeOrder;
import com.wangxin.iot.mapper.IotRechargeOrderMapper;
import com.wangxin.iot.rest.base.Result;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @Description: iot_recharge_order
 * @Author: jeecg-boot
 * @Date:   2021-06-24
 * @Version: V1.0
 */
@Service
public class IotRechargeOrderServiceImpl extends ServiceImpl<IotRechargeOrderMapper, IotRechargeOrder> implements IIotRechargeOrderService {

    @Override
    public Result<?> saveRechargeOrder(IotRechargeOrder order) {

        IotRechargeOrder rechargeOrder = new IotRechargeOrder();
        BeanUtils.copyProperties(order,rechargeOrder);
        rechargeOrder.setCreateTime(new Date());
        //未支付
        rechargeOrder.setStatus(PayStatus.unpay.getCode());
        rechargeOrder.setCreateBy(order.getOpenId());
        return Result.success(null);
    }
}
