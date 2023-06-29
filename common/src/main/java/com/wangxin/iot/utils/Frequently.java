package com.wangxin.iot.utils;

import java.util.HashMap;
import java.util.Map;

public class Frequently {

    private static Map<String,Long[]> countMap = new HashMap<String,Long[]>();

    /**
     * 访问是否频繁
     * @param userName 用户名
     * @return boolean true/false
     */
    public static boolean isLimit(String userName){
        boolean b=false;
        Long[] key = countMap.get(userName);
        if(key != null){//不是第一次访问
            long time  = System.currentTimeMillis()/1000 - key[0];
            if(time<60){//一分钟内
                long count = key[1];
                if(count >= 1){
                    b=true;
                }else{
                    countMap.put(userName, new Long[]{key[0],key[1]+1});
                }
            }else{
                countMap.put(userName, new Long[]{System.currentTimeMillis()/1000, 1L});
            }
        }else{//第一次访问
            countMap.put(userName, new Long[]{System.currentTimeMillis()/1000, 1L});
        }
        return b;
    }
    public static boolean isLimit(String userName,Long timeInterval){
        boolean b=false;
        Long[] key = countMap.get(userName);
        if(key != null){//不是第一次访问
            long time  = System.currentTimeMillis()/1000 - key[0];
            if(time<timeInterval){//一分钟内
                long count = key[1];
                if(count >= 1){
                    b=true;
                }else{
                    countMap.putIfAbsent(userName, new Long[]{key[0],key[1]+1});
                }
            }else{
                countMap.putIfAbsent(userName, new Long[]{System.currentTimeMillis()/1000, 1L});
            }
        }else{//第一次访问
            countMap.putIfAbsent(userName, new Long[]{System.currentTimeMillis()/1000, 1L});
        }
        return b;
    }
}
