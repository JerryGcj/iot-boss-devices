package com.wangxin.iot.model.third.hu;

import com.alibaba.fastjson.JSON;
import lombok.Data;

/**
 * 接口配置参数
 *
 * @author wx
 * @date 2020/3/2
 */
@Data
public class JhApiConfig {
    /**
     * 接口地址
     */
    private String url;

    /**
     * 系统分配的AppId
     */
    private String traderId;

    /**
     * 接口版本号 1.0.0
     */
    private String version;

    /**
     * 秘钥
     */
    private String appSecret;

}
