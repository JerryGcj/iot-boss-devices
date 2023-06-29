package com.wangxin.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by 18765 on 2020/1/2 11:32
 */
@Data
@Component
@ConfigurationProperties(prefix = "rest-union-config")
public class RestUnionConfigProerties {
    private String ratePlanUrl;
    private String updateDeviceStatusUrl;
    /**
     * 获取短信的发送详情
     */
    private String smsDetailUrl;
    //发送短信url
    private String deviceSendSmsUrl;
    //设备用量url
    private String deviceUsagesUrl;
    //设备详情url
    private String deviceDetailUrl;
    private String username;
    private String apiKey;
}
