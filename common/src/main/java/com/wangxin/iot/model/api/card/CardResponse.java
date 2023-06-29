package com.wangxin.iot.model.api.card;

import lombok.Data;

@Data
public class CardResponse {

    /**
     * 查询结果状态码
     */
    private String code = SUCESS_CODE;
    /**
     * 查询结果状态消息
     */
    private String msg = SUCESS_MSG;
    private Response3 data;
    public static final String SUCESS_CODE="0000";
    public static final String SUCESS_MSG="成功";



    @Override
    public String toString() {
        return "CardResponse [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
