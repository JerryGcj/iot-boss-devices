package com.wangxin.iot.model.api.product;

import lombok.Data;

import java.util.List;

@Data
public class ProductResponse {

    /**
     * 查询结果状态码
     */
    private String code = SUCESS_CODE;
    /**
     * 查询结果状态消息
     */
    private String msg = SUCESS_MSG;
    private List<Response2> data;
    public static final String SUCESS_CODE="0000";
    public static final String SUCESS_MSG="成功";



    @Override
    public String toString() {
        return "ProductResponse [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
