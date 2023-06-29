package com.wangxin.iot.constants;

/**
 * @Description : 公共常量枚举
 * @author: Mark (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018/10/17
 */
public enum ComConstants {
	/**
	 * 短信类 计数器KEY
	 */
	COUNTER_SMS_KEY("sms_key"),

	COUNTER_SMS_SEND_KEY("sms_key"),

	;

	ComConstants(String message){this.message=message;}
	private String message;
	public String getMessage(){
		return this.message;
	}
}
