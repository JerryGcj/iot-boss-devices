package com.wangxin.iot.constants;

/**
 * Created by 18765 on 2020/1/3 19:31
 */
public enum WechatPayConstants {
    ACTIVE("active"),

    UN_ACTIVE("unActive");

    private String message;

    WechatPayConstants(String message){
        this.message=message;
    }

    public String getMessage(){
        return this.message;
    }
}
