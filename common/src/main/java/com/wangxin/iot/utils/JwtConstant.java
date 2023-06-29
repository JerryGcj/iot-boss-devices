package com.wangxin.iot.utils;

/**
 * Created by wangc on 15/06/2017.
 */
public class JwtConstant {
    /**
     * JWT constant
     */
    public static final String JWT_ID = "jwt";
    public static final String JWT_SECRET = "hong1mu2zhi3ruan4jian5";
    public static final int JWT_TTL = 60 * 60 * 1000;  //millisecond
    public static final int JWT_REFRESH_INTERVAL = 55 * 60 * 1000;  //millisecond
    public static final int JWT_REFRESH_TTL = 12 * 60 * 60 * 1000;  //millisecond
}
