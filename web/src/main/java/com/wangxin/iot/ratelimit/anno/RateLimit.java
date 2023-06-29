package com.wangxin.iot.ratelimit.anno;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by 18765 on 2020/1/13 17:32
 * @author yanwin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {
    /**
     * 默认10分钟允许访问一次
     * @return
     */
    int count() default 1;

    /**
     *
     * @return
     */
    int seconds() default 600;
}
