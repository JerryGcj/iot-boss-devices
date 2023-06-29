package com.wangxin.iot.model.third.hu;

import lombok.Data;

/**
 * 接口配置参数
 *
 * @author wx
 * @date 2020/3/2
 */
@Data
public class OneLinkApiConfig {

    private String url;
    /**
     * token地址前缀
     */
    private String tokenUrl;

    /**
     * 系统分配的AppId
     */
    private String appId;

    /**
     * 接口版本号 1.0.0
     */
    private String version;

    /**
     * 密码
     */
    private String password;

}
