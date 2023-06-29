package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.model.IotCardOrder;

/**
 * @Description: electron_channel_order_pay
 * @Author: jeecg-boot
 * @Date:   2021-09-29
 * @Version: V1.0
 */
public interface IIotCardOrderService extends IService<IotCardOrder> {

    /**
     * 联通订购套餐
     * @param cardOrder
     */
    void unicomOrderPackage(IotCardOrder cardOrder);
}
