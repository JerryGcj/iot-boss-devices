package com.wangxin.iot.model.third.hu;

import org.apache.commons.codec.digest.DigestUtils;
import java.util.*;

/**
 * 请求工具类
 *
 * @author wx
 * @date 2020/3/2
 */
public class HuRequestUtil {
    public static void main(String[] args) {
        HuApiConfig apiConfig = new HuApiConfig();
        apiConfig.setAppId("test");
        apiConfig.setAppSecret("7Z51sQDreqeOWupwpKoq6fQwFYdPvBq3");
        apiConfig.setVersion("1.0.0");
        System.out.print(getParams(apiConfig));
    }

    /**
     * 获取请求参数
     * @param apiConfig
     * @return
     */
    public static String getParams(HuApiConfig apiConfig) {
        StringBuffer sb = new StringBuffer();
        // 通用参数
        Map<String, Object> map = new HashMap<>();
        map.put("app_id", apiConfig.getAppId());
        map.put("nonce", "aabbccdd");
        map.put("timestamp", System.currentTimeMillis());
        map.put("version", apiConfig.getVersion());
        // 获取签名
        String sign = getSign(map, apiConfig.getAppSecret());
        map.put("sign", sign);

        boolean flag = false;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
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
