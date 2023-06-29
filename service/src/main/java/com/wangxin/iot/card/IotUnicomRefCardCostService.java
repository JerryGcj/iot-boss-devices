package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.model.IotCardOrder;
import com.wangxin.iot.model.Order;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 */
public interface IotUnicomRefCardCostService extends IService<IotUnicomRefCardCost> {


    void saveRefWithOrder(Order order);

    void saveRefWithCardOrder(IotCardOrder cardOrder);

    void activePackage(String iccid);
}
