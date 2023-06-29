package com.wangxin.iot.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Set;

/**
 * @author: yanwin
 * @Date: 2020/3/16
 */
public class OneLinkUtils {
    public static String getTransid(String appid){
        return appid+DateUtils.getCurrentDateString("yyyyMMddHHmmss")+"10000001";
//        return appid+DateUtils.getCurrentDateString("YYYYMMDDHHMMSS")+"10000001";
    }
    public static String buildUrl(Map<String, String> paramMap){
        String url = null;
        StringBuffer urlString = new StringBuffer();
        urlString.append(paramMap.get("url")).append(paramMap.get("version")).append(paramMap.get("apiName"));
        paramMap.remove("url");
        paramMap.remove("version");
        paramMap.remove("apiName");
        if (!paramMap.isEmpty()) {
            // 参数列表不为空，地址尾部增加'?'
            urlString.append('?');
            // 拼接参数
            Set<Map.Entry<String, String>> entrySet = paramMap.entrySet();
            for (Map.Entry<String, String> entry : entrySet) {
                try {
                    urlString.append(entry.getKey()).append('=').append(URLEncoder.encode(entry.getValue(), "UTF-8")).append('&');
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
            // 去掉最后一个字符“&”
            url = urlString.substring(0, urlString.length() - 1);
        }
        return url;
    }
}
