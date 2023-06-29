package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.model.Order;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 */
public interface IotTelecomRefCardCostService extends IService<IotTelecomRefCardCost> {


    void saveRefWithOrder(Order order);

    void activePackage(String accessNumber);
}
