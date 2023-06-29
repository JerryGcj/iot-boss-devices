package com.wangxin.iot.utils;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * Created by 18765 on 2020/1/2 13:56
 */
@Component
public class ApplicationContextUtil implements ApplicationContextAware {
    public static ApplicationContext applicationContext = null;
    @Override
    public void setApplicationContext(ApplicationContext args){
        applicationContext = args;
    }
}
