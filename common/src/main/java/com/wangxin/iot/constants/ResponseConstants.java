package com.wangxin.iot.constants;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 接口状态
 */

public enum ResponseConstants {

	SUCCESS_CODE("0000","操作成功"),

	FAIL_CODE1("0001","缺失参数"),

	FAIL_CODE2("0002","卡号不存在"),

	FAIL_CODE3("0003","签名校验失败"),

	FAIL_CODE4("0004","商户不存在"),

	FAIL_CODE5("0005","商户状态异常"),

	FAIL_CODE6("0006","订单号不能超过50位"),

	FAIL_CODE7("0007","套餐编码无效"),

	FAIL_CODE8("0008","订单不存在"),

	FAIL_CODE9("0009","IP白名单限制"),

	FAIL_CODE10("0010","商户订单号重复"),

	FAIL_CODE11("0011","订单保存失败"),

	FAIL_CODE12("0012","账户余额不足"),

	FAIL_CODE13("0013","参数格式错误"),

	FAIL_CODE14("0014","时间戳已过期"),

	FAIL_CODE15("0015","时间戳格式错误，应为正整数"),

	FAIL_CODE16("0016","查询频繁"),

	FAIL_CODE17("0017","暂无可充值套餐"),

	FAIL_CODE18("0018","充值卡数量不能超过100"),

	FAIL_CODE19("0019","扣款失败"),

	FAIL_CODE20("0020","系统繁忙");

	/**
	 * 返回码
	 */
	private String code;

	/**
	 * 描述
	 */
	private String msg;

	ResponseConstants(String code, String msg) {
		this.code=code;
		this.msg = msg;
	}

	public String getMsg() {
		return this.msg;
	}

	public String getCode() {
		return this.code;
	}

}
