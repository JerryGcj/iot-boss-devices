package com.wangxin.iot.model.api.orderList;

import com.wangxin.iot.model.api.order.OrderResponse;
import lombok.Data;

import java.io.Serializable;

@Data
public class SonOrderResponse implements Serializable {

    private String subBizOrderNo;

    private int status;

    private String iccid;

    @Override
    public String toString() {
        return "SonOrderResponse [subBizOrderNo=" + subBizOrderNo + ", iccid=" + iccid + ", status=" + status + "]";
    }
}
