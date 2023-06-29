package com.wangxin.iot.constants;

/**
 * @Description : 成功信息 常量
 * @author: Mark (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018/08/10
 */
public enum SuccessConstants {
    /**
     * 成功常量
     */
    SUCCESS(10000000, "操作成功","OPERATE_SUCCESS",200);

    private int retCode;

    private String retMsg;

    private  String retInfo;

    private  int httpCode;

    SuccessConstants(int retCode, String retMsg, String retInfo, int httpCode) {
        this.retCode = retCode;
        this.retMsg = retMsg;
        this.retInfo=retInfo;
        this.httpCode=httpCode;
    }

    public int getHttpCode() {
        return httpCode;
    }

    public void setHttpCode(int httpCode) {
        this.httpCode = httpCode;
    }

    public int getRetCode() {
        return retCode;
    }

    public void setRetCode(int retCode) {
        this.retCode = retCode;
    }

    public String getRetMsg() {
        return retMsg;
    }

    public void setRetMsg(String retMsg) {
        this.retMsg = retMsg;
    }

    public String getRetInfo() {
        return retInfo;
    }

    public void setRetInfo(String retInfo) {
        this.retInfo = retInfo;
    }
}
