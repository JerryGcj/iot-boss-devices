package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.model.Card;
import com.wangxin.iot.model.IotRefCardCost;
import com.wangxin.iot.model.Order;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 */
public interface IotRefCardCostService extends IService<IotRefCardCost> {
    /**
     * 根据上游返回数据，更新卡用量
     * @param card
     * @return
     */
    boolean updateUsaged(Card card);

    void saveWithOrder(Order order);

    void saveRefWithOrder(Order order);

}
