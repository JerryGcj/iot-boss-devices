package com.wangxin.iot.constants;

import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/1/15
 */
@Data
public class UnionRestErrorConstants {
    public static final UnionRestErrorConstants LICENCE_ERROR =
            new UnionRestErrorConstants("10000001","Invalid credentials");
    public static final UnionRestErrorConstants API_VERSION_ERROR =
            new UnionRestErrorConstants("10000024","Invalid apiVersion");
    public static final UnionRestErrorConstants AREA_ERROR =
            new UnionRestErrorConstants("10000031","Invalid area");
    public static final UnionRestErrorConstants ICCID_ERROR =
            new UnionRestErrorConstants("20000001","Resource not found - Invalid ICCID");
    public static final UnionRestErrorConstants SERVER_ERROR =
            new UnionRestErrorConstants("30000001","server error");
    /**
     * 凭据无效
     */
    private String errorCode;
    private String errorMessage;

    public UnionRestErrorConstants(String errorCode, String errorMessage) {
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }
}
