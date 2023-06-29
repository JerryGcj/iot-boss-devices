package com.wangxin.iot.constants;

/**
 * Created by 18765 on 2020/1/3 19:31
 */
public enum SendSMSConstants {
    /**
     * 短信下发地址
     */
    SENDURL("http://115.28.96.167:8081/api/sms/send"),

    /**
     * 短信用户名
     */
    USERID("351430"),

    /**
     * 短信密钥
     */
    APIKEY("7ba3dcbd20cc40fd93c5d5fe25586858"),

    /**
     * 接收号码
     */
    PHONES("15650017861,18765907950");

    private String value;

    SendSMSConstants(String value){
        this.value=value;
    }

    public String getValue(){
        return this.value;
    }
}
