package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotRechargeOrder;
import com.wangxin.iot.rest.base.Result;

/**
 * @Description: iot_recharge_order
 * @Author: jeecg-boot
 * @Date:   2021-06-24
 * @Version: V1.0
 */
public interface IIotRechargeOrderService extends IService<IotRechargeOrder> {
    Result<?> saveRechargeOrder(IotRechargeOrder order);
}
