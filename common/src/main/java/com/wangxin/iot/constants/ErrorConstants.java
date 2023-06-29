package com.wangxin.iot.constants;

import com.wangxin.iot.rest.exception.HttpError;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description :  异常信息 常量
 * @author: 张闻帅
 * @version: V1.0
 * @Date: 2019/08/21
 */
@Slf4j
public enum ErrorConstants implements HttpError {
    /**
     * 系统错误，统一 10010000
     */
    SYS_ERR(500, 10010000, "SYS_ERROR", "系统繁忙,请稍候重试"),

    RESET_CONSUMERS_ERROR(500,10151001,"RESET_CONSUMERS_ERROR","重置指定队列消费者数量失败"),
    RESTART_LISTENER_ERROR(500,10151002,"RESTART_LISTENER_ERROR","重启队列监听失败"),
    STOP_LISTENER_ERROR(500,10151003,"STOP_LISTENER_ERROR","停止指定队列消费者数量失败"),
    ;

    private int httpCode;
    private int retCode;
    private String retInfo;
    private String retMsg;

    ErrorConstants(int httpCode, int retCode, String retInfo, String retMessage) {
        this.httpCode = httpCode;
        this.retCode = retCode;
        this.retMsg = retMessage;
        this.retInfo = retInfo;
    }

    @Override
    public int getHttpCode() {
        return httpCode;
    }

    @Override
    public int getRetCode() {
        return retCode;
    }

    @Override
    public String getRetInfo() {
        return this.retInfo;
    }

    @Override
    public String getRetMessage() {
        return retMsg;
    }


    protected static Map<String, ErrorConstants> msgToErrorConstantsMap = new HashMap<>();
    protected static Map<String, ErrorConstants> infoToErrorConstantsMap = new HashMap<>();

    static {
        for(ErrorConstants errorConstants : ErrorConstants.values()) {
            String retMsg = errorConstants.retMsg;
            String retInfo = errorConstants.retInfo;
            if(msgToErrorConstantsMap.containsKey(retMsg)) {
                log.error("ErrorConstants RetMsg Cannot Be Repeated And RetMsg Is "+ retMsg);
                throw new RuntimeException("ErrorConstants RetMsg Cannot Be Repeated");
            }
            if(infoToErrorConstantsMap.containsKey(retInfo)) {
                log.error("ErrorConstants RetInfo Cannot Be Repeated And RetInfo Is "+ retInfo + "; retCode:" + errorConstants.getRetCode());
                throw new RuntimeException("ErrorConstants RetInfo Cannot Be Repeated");
            }
            msgToErrorConstantsMap.put(retMsg, errorConstants);
            infoToErrorConstantsMap.put(retInfo, errorConstants);
        }
    }

    public static ErrorConstants valueOfRetMsg(String retMsg) {
        return msgToErrorConstantsMap.get(retMsg);
    }
}
