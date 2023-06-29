package com.wangxin.iot.rest.exception;

/**
 * @Description :  异常信息接口
 * @author: Mark (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018/08/10
 */
public interface HttpError {
    /**
     * 获得HTTPCode
     * @return httpCode
     */
    int getHttpCode();

    /**
     * 获得企业自定义编号
     * @return 自定义编码
     */
    int getRetCode();

    /**
     * 获得简要信息
     * @return 简要信息
     */
    String getRetInfo();

    /**
     * 获得详细信息
     * @return 详细信息
     */
    String getRetMessage();
}
