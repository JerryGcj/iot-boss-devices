package com.wangxin.iot.ratelimit.interceptor;

import com.alibaba.fastjson.JSON;
import com.wangxin.iot.helper.IpHelper;
import com.wangxin.iot.ratelimit.anno.RateLimit;
import com.wangxin.iot.rest.base.CodeMsg;
import com.wangxin.iot.rest.base.Result;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.lang.reflect.Method;

/**
 * Created by 18765 on 2020/1/13 17:37
 */
@Slf4j
@Component
public class RateLimitInterceptor implements HandlerInterceptor {
    @Autowired
    private RedisUtil redisUtil;
    /**
     * 请求前拦截
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HandlerMethod handlerMethod = (HandlerMethod)handler;
        String ipAddr = IpHelper.getIpAddr(request);
        //如果方法上有这个注解
        Method method = handlerMethod.getMethod();
        RateLimit rateLimit = method.getAnnotation(RateLimit.class);
        if(rateLimit != null){
            String key = ipAddr+"_"+request.getRequestURI();
            int seconds = rateLimit.seconds();
            int definedCount = rateLimit.count();
            String realCount = (String)redisUtil.get(key);
            //该周期内第一次访问
            if(StringUtils.isEmpty(realCount)){
                //设置初始值并设置过期时间 秒
                redisUtil.set(key,"1");
                redisUtil.expire(key,seconds);
                return true;
            }
            long incrCount = redisUtil.incr(key, 1);
            if(incrCount>Long.valueOf(definedCount)){
                log.info("ip:{},请求过于频繁,请求次数：{}",ipAddr,incrCount);
                this.render(response,JSON.toJSONString(Result.fail(new CodeMsg(1,"访问过于频繁"))));
                return false;
            }
        }
        return true;
    }
    private void render(HttpServletResponse response, String msg)throws Exception {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream out = response.getOutputStream();
        out.write(msg.getBytes("UTF-8"));
        out.flush();
        out.close();
    }
}
