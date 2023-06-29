package com.wangxin.iot.utils;

import java.util.UUID;

/**
 * @author: yanwin
 * @Date: 2020/4/13
 */
public class UUIDUtil {
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-", "");
    }
}
