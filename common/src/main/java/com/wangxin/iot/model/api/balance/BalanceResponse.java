package com.wangxin.iot.model.api.balance;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class BalanceResponse implements Serializable {

    /**
     * 查询结果状态码
     */
    private String code = SUCESS_CODE;
    /**
     * 查询结果状态消息
     */
    private String msg = SUCESS_MSG;
    private BigDecimal data;
    public static final String SUCESS_CODE="0000";
    public static final String SUCESS_MSG="成功";



    @Override
    public String toString() {
        return "BalanceResponse [code=" + code + ", msg=" + msg + ", data=" + data + "]";
    }
}
