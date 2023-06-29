package com.wangxin.iot.constants;

/**
 * 平台card状态枚举
 */
public enum TelecomCardStatusEnum {

	/**
	 * 可激活
	 */
	ACTIVE_READY("active_ready", 1),

	/**
	 * 测试激活
	 */
	ACTIVETEST("active_test", 2),

	/**
	 * 测试去激活
	 */
	ACTIVETOTEST("active_to_test", 3),

	/**
	 * 在用
	 */
	ACTIVE("active", 4),

	/**
	 * 停机
	 */
	CLEANED("stop", 5),

	/**
	 * 运营商管理状态
	 */
	EXCHANGED("exchanged", 6),

	/**
	 * 拆机
	 */
	STORE("store", 7);



	/**
	 * 卡状态
	 */
	private String status;

	/**
	 * 卡状态code
	 */
	private Integer code;

	TelecomCardStatusEnum(String status, Integer code) {
		this.status=status;
		this.code = code;
	}

	public String getStatus() {
		return this.status;
	}

	public String getCode() {
		return String.valueOf(code);
	}
	public Integer getIntCode() {
		return code;
	}
	/**
	 * 将通道的card状态转化为平台的card状态
	 * @param cardStatus
	 * @return
	 */
	public String convertStatus(CardStatus cardStatus) {
		TelecomCardStatusEnum[] values = TelecomCardStatusEnum.values();
		for (TelecomCardStatusEnum value : values) {
			String status = value.getStatus();
			if (status.equals(cardStatus.getStatus())) {
				return value.getCode();
			}
		}
		return null;
	}

}
