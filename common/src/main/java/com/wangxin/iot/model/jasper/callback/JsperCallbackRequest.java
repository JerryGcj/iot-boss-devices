package com.wangxin.iot.model.jasper.callback;

import lombok.Data;

/**
 * 联通Jasper推送请求参数
 *
 * @author wx
 * @date 2020/1/3
 */
@Data
public class JsperCallbackRequest {
    /**
     * 事件id
     */
    private String eventId;

    /**
     * 事件类型
     */
    private String eventType;

    /**
     * 请求时间
     */
    private String timestamp;

    /**
     * 签名
     */
    private String signature;

    /**
     * 事件类型特定的 XML 内容
     */
    private String data;
}
