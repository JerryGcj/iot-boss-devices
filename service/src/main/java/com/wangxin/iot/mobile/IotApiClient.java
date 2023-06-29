package com.wangxin.iot.mobile;

import com.wangxin.iot.config.TemplateConfig;

import java.util.List;

/**
 * Created by 18765 on 2020/1/2 10:27
 */
public interface IotApiClient {

    /**
     * 调用请求
     */
    void callWebService(List<String> iccids,TemplateConfig config);
}
