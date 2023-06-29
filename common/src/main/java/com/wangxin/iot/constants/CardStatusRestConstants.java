package com.wangxin.iot.constants;

import java.util.stream.Stream;

/**
 * 联通jsper接口 card状态
 */
public enum CardStatusRestConstants {

	/**
	 * 可测试
	 */
	TEST_READY("TEST_READY", 1),

	/**
	 * 库存
	 */
	INVENTORY("INVENTORY", 2),

	/**
	 * 可激活
	 */
	ACTIVATION_READY("ACTIVATION_READY", 3),

	/**
	 * 已激活
	 */
	ACTIVATED("ACTIVATED", 4),

	/**
	 * 已停用
	 */
	DEACTIVATED("DEACTIVATED", 5),

	/**
	 * 已失效
	 */
	RETIRED("RETIRED", 6);


	/**
	 * 卡状态
	 */
	private String status;

	/**
	 * 卡状态code
	 */
	private Integer code;

	CardStatusRestConstants(String status, Integer code) {
		this.status=status;
		this.code = code;
	}

	public String getStatus() {
		return this.status;
	}

	public String getCode() {
		return String.valueOf(code);
	}

	public static String getStatus(Integer state){
		return Stream.of(CardStatusRestConstants.values()).filter(item->item.code == state).findFirst().get().getStatus();
	}

}
