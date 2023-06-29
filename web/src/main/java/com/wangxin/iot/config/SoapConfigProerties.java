package com.wangxin.iot.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Created by 18765 on 2020/1/2 11:32
 */
@Data
@Component
@ConfigurationProperties(prefix = "soap-config")
public class SoapConfigProerties {
    private String username;
    private String password;
    private String namespaceUrl;
    private String prefix;
    private String licenseKey;
    private String url;
}
