package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.model.Order;

import java.util.Map;

/**
 * Created by 18765 on 2020/1/4 11:22
 */

public interface IOrderPackageService extends IService<Order> {
   /**
    * 电信订购套餐
    * @param order
    */
   void telecomOrderPackage(Order order);
   /**
    * 后台接口手动订购套餐
    * @param reqParam
    */
   void apiPlaceOrder(Map<String, String> reqParam);

   /**
    * 移动公众号订购套餐
    * @param order
    */
   void wechatOrderPackage(Order order);

   /**
    * 联通订购套餐
    * @param order
    */
   void unicomOrderPackage(Order order);
}
