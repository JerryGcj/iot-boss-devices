/*
 * Copyright (c) 2017-2018,Cardinal Operations and/or its affiliates. All rights reserved.
 * CARDINAL OPERATIONS PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 */

package com.wangxin.iot.config;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

/**
 * @Description : Web接口数据打印日志
 * @author: Mark (mjianyou@wxdata.cn)
 * @version:V1.0
 * @Date: 2018/08/10
 */
@Aspect
//@Component
@Slf4j
public class WebLogAop {

    @Pointcut("execution(public *  com.wangxin.iot.controller..*Controller.*(..)) "
                    + "&& !execution(public * com.wangxin.iot.controller.base.BaseController.*(..))")
    public void controllerMethodPointcut(){}

    @Before("controllerMethodPointcut()")
    public void doBefore(JoinPoint joinPoint){
        // 接收到请求，记录请求内容
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        // 记录下请求内容
        log.info("URL : " + request.getRequestURL().toString());
        log.info("HTTP_METHOD : " + request.getMethod());
        log.info("IP : " + request.getRemoteAddr());
        log.info("CLASS_METHOD : " + joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        log.info("ARGS : " + Arrays.toString(joinPoint.getArgs()));
    }
}
