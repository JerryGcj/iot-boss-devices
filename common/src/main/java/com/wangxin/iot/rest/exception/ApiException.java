package com.wangxin.iot.rest.exception;

import java.text.MessageFormat;

/**
 * @Description :  API 异常类
 * @author: 张闻帅
 * @version: V1.0
 * @Date: 2019/08/10
 */
public class ApiException extends RuntimeException {
    private int httpCode;
    private int retCode;
    private String retInfo;
    private String retMsg;

    public ApiException(HttpError httpError) {
        this.httpCode = httpError.getHttpCode();
        this.retCode = httpError.getRetCode();
        this.retMsg = httpError.getRetMessage();
        this.retInfo = httpError.getRetInfo();
    }

    public ApiException(HttpError httpError, String... msg) {
        this.httpCode = httpError.getHttpCode();
        this.retCode = httpError.getRetCode();
        this.retMsg = MessageFormat.format(httpError.getRetMessage(), msg);
        this.retInfo = MessageFormat.format(httpError.getRetInfo(), msg);
    }

    public int getHttpCode() {
        return httpCode;
    }

    public int getRetCode() {
        return retCode;
    }

    public String getRetInfo() {
        return retInfo;
    }

    public String getRetMessage() {
        return retMsg;
    }
}
