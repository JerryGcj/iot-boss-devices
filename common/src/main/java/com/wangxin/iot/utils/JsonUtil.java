package com.wangxin.iot.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SimplePropertyPreFilter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JsonUtil {
    private static final SimplePropertyPreFilter filter = new SimplePropertyPreFilter();

    static {
        filter.getExcludes().add("apiMethod");
        filter.getExcludes().add("responseClass");
    }

    public static String toJson(Object o) {

        try {
            return JSON.toJSONString(o, filter);
        } catch (Exception e) {
            return "";
        }
    }

    public static JSONObject fromJson(String json) {
        return JSON.parseObject(json);
    }

    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return JSON.parseObject(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("paser json error " + json, e);
        }
    }

    /**
     * json转Map对象
     * @param json
     * @return
     */
    public static Map<String, String> parseRequestJson(String json){
        Map<String, String> result = new HashMap<>();
        try {
            Map<String, Object> map = JSON.parseObject(json, HashMap.class);
            for (Map.Entry<String, Object> entry : map.entrySet()) {
                result.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        } catch (Exception e) {
            log.error("解析传入的json出现未知错误{}", e.getMessage());
            return null;
        }
        return result;
    }
}
