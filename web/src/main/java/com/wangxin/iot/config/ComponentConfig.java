package com.wangxin.iot.config;

import com.wangxin.iot.ratelimit.interceptor.RateLimitInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

/**
 * Created by 18765 on 2020/1/13 18:41
 */
@Component
@Slf4j
public class ComponentConfig extends WebMvcConfigurationSupport {
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        log.info("频率拦截器注入到容器中-------------");
        registry.addInterceptor(rateLimitInterceptor).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
}
