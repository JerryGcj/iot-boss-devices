package com.wangxin.iot.config;

import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/7/16
 */
@Data
public class IotGatewayApiConfig {
    private  String serverUrl;
    private  String appId;
    private  String appSecret;
    private  String openId;
    private  String version;
    private  String paramVersion;
}
