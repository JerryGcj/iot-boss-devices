package com.wangxin.iot.utils;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.util.CollectionUtils;

import java.util.*;

/**
 * 请求工具类
 *
 * @author wx
 * @date 2020/3/2
 */
public class RequestSecretUtil {
    /**
     * 获取请求参数
     * @param
     * @return
     */
    public static String getParams(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        boolean flag = false;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if(flag) {
                sb.append("&");
            } else {
                sb.append("?");
                flag = true;
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }
    /**
     * 获取请求参数
     * @param
     * @return
     */
    public static String getParams(Map<String, Object> params, String appSecret) {
        StringBuilder sb = new StringBuilder();
        String sign = getSign(params, appSecret);
        params.put("sign",sign);
        boolean flag = false;
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if(flag) {
                sb.append("&");
            } else {
                sb.append("?");
                flag = true;
            }
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    /**
     * 获取签名
     * @param params
     * @param appSecret
     * @return
     */
    private static String getSign(Map<String, Object> params, String appSecret) {
        String paramsStr = getParamsByAsc(params);
        paramsStr += appSecret;
        return DigestUtils.md5Hex(paramsStr);

    }

    /**
     * 签名参数的值按照字典顺序排序
     * @param params
     * @return
     */
    private static String getParamsByAsc(Map<String, Object> params) {
        List<Map.Entry<String, Object>> entryList = new ArrayList<>(params.entrySet());
        Collections.sort(entryList,  Comparator.comparing(Map.Entry<String, Object>::getKey));
        StringBuffer param = new StringBuffer();
        for (Map.Entry<String, Object> entry : entryList) {
            param.append(entry.getValue());
        }
        return param.toString();
    }
}
