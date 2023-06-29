package com.wangxin.iot.constants;

/**
 * @author: yanwin
 * @Date: 2020/3/2
 */
public enum  TemplateConstants {
    TELECOM_HZC("telecom_hzc"),
    TELECOM_TYS("telecom_tys"),
    IOTGATEWAY("iotGateway"),
    THRID_HU("thrid_hu"),
    ONE_LINK_API_CONFIG("oneLinkServiceImpl1"),
    THRID_JH("thrid_jh"),
    CU("cu");

    private String message;

    TemplateConstants(String message){
        this.message=message;
    }

    public String getMessage(){
        return this.message;
    }
}
