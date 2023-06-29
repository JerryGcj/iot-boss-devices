package com.wangxin.iot.mobile;

import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.rest.base.Result;

import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/3/11
 */
public interface ThirdService {
      /**
       * 订购套餐
       * @param reqParam
       * @param iotOperatorTemplate
       * @return
       */
      Result placeOrderCost(Map<String, String> reqParam, IotOperatorTemplate iotOperatorTemplate);

      /**
       * 发送请求，返回用量和状态
       * @param iccid
       */
      Map sendReq(String iccid, IotOperatorTemplate iotOperatorTemplate);

      /**
       * 更新卡，具体实现通道类，抽象类中返回了false
       * @param reqParam
       * @return
       */
      boolean modifyCard(Map<String, String> reqParam, IotOperatorTemplate iotOperatorTemplate);

      /**
       * 获取卡详情
       * @param iccid
       * @param iotOperatorTemplate
       * @return
       */
      Map getCardDetails(String iccid, IotOperatorTemplate iotOperatorTemplate);

      /**
       * 查询实名状态
       * @param iccid
       * @param iotOperatorTemplate
       * @return
       */
      Result realNameStatus(String iccid, String id, IotOperatorTemplate iotOperatorTemplate);
}
