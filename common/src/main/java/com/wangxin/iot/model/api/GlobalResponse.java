package com.wangxin.iot.model.api;

import lombok.Data;

@Data
public class GlobalResponse {

    private String token;

    private long expireDate;

    //-- 返回代码定义 --//
    public static final String FAIL_CODE1="0001";		//缺失参数
    public static final String FAIL_CODE2="0002";		//卡号不存在
    public static final String FAIL_CODE3="0003";		//签名校验失败
    public static final String FAIL_CODE4="0004";		//商户不存在
    public static final String FAIL_CODE5="0005";		//商户状态异常
    public static final String FAIL_CODE6="0006";		//订单号不能超过50位
    public static final String FAIL_CODE7="0007";		//套餐编码无效
    public static final String FAIL_CODE8="0008";		//订单不存在
    public static final String FAIL_CODE9="0009";		//IP白名单限制
    public static final String FAIL_CODE10="0010";		//商户订单号重复
    public static final String FAIL_CODE11="0011";		//订单保存失败
    public static final String FAIL_CODE12="0012";		//账户余额不足
    public static final String FAIL_CODE13="0013";		//参数格式错误
    public static final String FAIL_CODE14="0014";      //时间戳已过期
    public static final String FAIL_CODE15="0015";		//系统繁忙

    public static final String FAIL_HINT1="缺失参数";
    public static final String FAIL_HINT2="卡号不存在";
    public static final String FAIL_HINT3="签名校验失败";
    public static final String FAIL_HINT4="商户不存在";
    public static final String FAIL_HINT5="商户状态异常";
    public static final String FAIL_HINT6="订单号不能超过50位";
    public static final String FAIL_HINT7="套餐编码无效";
    public static final String FAIL_HINT8="订单不存在";
    public static final String FAIL_HINT9="IP白名单限制";
    public static final String FAIL_HINT10="商户订单号重复";
    public static final String FAIL_HINT11="订单保存失败";
    public static final String FAIL_HINT12="账户余额不足";
    public static final String FAIL_HINT13="参数格式错误";
    public static final String FAIL_HINT14="时间戳已过期";
    public static final String FAIL_HINT15="系统繁忙";
}
