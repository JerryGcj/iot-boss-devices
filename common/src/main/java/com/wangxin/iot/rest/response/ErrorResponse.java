package com.wangxin.iot.rest.response;

import com.wangxin.iot.rest.exception.ApiException;
import com.wangxin.iot.rest.exception.HttpError;

import java.text.MessageFormat;

/**
 * @ClassName : ErrorResponse
 * @Description : 异常信息封装类
 * @author: Mark
 * @version:V1.0
 * @Date: 2018-8-13
 */
public class ErrorResponse {
    private int httpCode;
    private int retCode;
    private String retMsg;
    private String retInfo;

    public ErrorResponse(int errorCode, String errorMessage) {
        this.retCode = errorCode;
        this.retMsg = errorMessage;
    }

    public ErrorResponse(HttpError httpError, String... msg) {
        this.retCode = httpError.getRetCode();
        this.retMsg = MessageFormat.format(httpError.getRetMessage(), msg);
    }

    public ErrorResponse(HttpError httpError) {
        this.httpCode=httpError.getHttpCode();
        this.retCode = httpError.getRetCode();
        this.retMsg = httpError.getRetMessage();
        this.retInfo = httpError.getRetInfo();
    }

    public ErrorResponse(ApiException apiException) {
        this.httpCode=apiException.getHttpCode();
        this.retCode = apiException.getRetCode();
        this.retMsg = apiException.getRetMessage();
        this.retInfo = apiException.getRetInfo();
    }

    public int getHttpCode(){return httpCode;}
    public int getRetCode() {
        return retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public String getRetInfo() {
        return retInfo;
    }



}
