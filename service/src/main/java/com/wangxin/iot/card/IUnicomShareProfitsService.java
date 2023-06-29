package com.wangxin.iot.card;

import com.wangxin.iot.model.Order;

/**
 * @author: yanwin
 * @Date: 2020/4/27
 * @Desc:分润服务
 */
public interface IUnicomShareProfitsService {
    /**
     * 给代理商分配利润
     * @param order
     */
    void shareProfit(Order order);
}
