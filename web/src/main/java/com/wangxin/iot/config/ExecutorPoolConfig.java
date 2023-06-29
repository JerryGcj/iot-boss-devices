package com.wangxin.iot.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by wangc on 08/06/2017.
 */
@EnableAsync
@Configuration
public class ExecutorPoolConfig  {
    private static final Logger log = LoggerFactory.getLogger(ExecutorPoolConfig.class);

    @Bean
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor threadPool = new ThreadPoolTaskExecutor();
        threadPool.initialize(); // 初始化
        //当前线程数
        threadPool.setCorePoolSize(20);
        // 最大线程数
        threadPool.setMaxPoolSize(50);
        //线程池所使用的缓冲队列长度
        threadPool.setQueueCapacity(2000);
        //等待任务在关机时完成--表明等待所有线程执行完
        threadPool.setWaitForTasksToCompleteOnShutdown(true);
        // 等待时间 （默认为0，此时立即停止），并没等待xx秒后强制停止
        threadPool.setAwaitTerminationSeconds(60 * 15);
        //  线程名称前缀
        threadPool.setThreadNamePrefix("iot_boss_async_");
        //满了让调用者去处理
        threadPool.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        return threadPool;
    }
}
