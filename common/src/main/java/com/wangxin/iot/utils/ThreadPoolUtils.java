package com.wangxin.iot.utils;

import lombok.Data;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by 18765 on 2020/1/6 16:33
 */
@Data
public class ThreadPoolUtils {

    private ThreadPoolExecutor threadPoolExecutor;
    private ThreadPoolUtils(){
        threadPoolExecutor = new ThreadPoolExecutor(3,5,0L, TimeUnit.MILLISECONDS,new LinkedBlockingQueue<>());
    }
    private static class ThreadPoolHolder{
        private static ThreadPoolUtils instance = new ThreadPoolUtils();
    }
    public static ThreadPoolUtils getInstance(){
        return ThreadPoolHolder.instance;
    }
}
