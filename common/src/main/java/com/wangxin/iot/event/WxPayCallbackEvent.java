package com.wangxin.iot.event;

import org.springframework.context.ApplicationEvent;

/**
 * @author: yanwin
 * @Date: 2020/4/14
 */
public class WxPayCallbackEvent extends ApplicationEvent {

    public WxPayCallbackEvent(Object source) {
        super(source);
    }
}
