package com.wangxin.iot.card;


import com.wangxin.iot.model.Order;

import java.util.Map;

/**
 * @Description: 客户销售折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
public interface ICustomerSalesDiscountService{
    void updateUserBalance(Order order);

    Map<String,String> getUserScreat(String username);
}
