package com.wangxin.iot.model.api.order;

import lombok.Data;

import java.io.Serializable;

@Data
public class Response implements Serializable {

    /**
     * 查询结果状态码
     */
    private String code = SUCESS_CODE;
    /**
     * 查询结果状态消息
     */
    private String msg = SUCESS_MSG;
    private OrderResponse data;
    public static final String SUCESS_CODE="0000";
    public static final String SUCESS_MSG="成功";



    @Override
    public String toString() {
        return "OrderResponse [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
