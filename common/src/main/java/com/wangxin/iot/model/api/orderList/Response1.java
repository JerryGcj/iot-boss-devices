package com.wangxin.iot.model.api.orderList;

import com.wangxin.iot.model.api.order.OrderResponse;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class Response1 implements Serializable {

    /**
     * 提交时间
     */
    private String gmtCreate;
    /**
     * 平台流水号
     */
    private String bizOrderNo;
    /**
     * 商户流水号
     */
    private String outTradeNo;

    private List<SonOrderResponse> subBizOrderResponseList;


    @Override
    public String toString() {
        return "OrderResponse [gmtCreate=" + gmtCreate + ", bizOrderNo=" + bizOrderNo + ", " +
                "outTradeNo=" + outTradeNo + ", subBizOrderResponseList="+subBizOrderResponseList+"]";
    }
}
