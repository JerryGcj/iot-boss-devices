package com.wangxin.iot.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

/**
 * @author: yanwin
 * @Date: 2020/3/28
 */
@Component
public class ScheduleConfig implements SchedulingConfigurer{

    @Autowired
    Executor executor;
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(Executors.newScheduledThreadPool(5));
    }

}
