package com.wangxin.iot.config;

import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/7/16
 * @Desc:电信通道配置类
 */
@Data
public class TelecomGatewayApiConfig {
    private  String buyUrl;
    private  String userId;
    private  String appKey;
    private  String password;
}
