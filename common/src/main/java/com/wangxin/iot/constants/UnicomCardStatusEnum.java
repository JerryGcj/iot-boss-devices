package com.wangxin.iot.constants;

/**
 * 平台card状态枚举
 */
public enum UnicomCardStatusEnum {
	/**
	 * 可测试
	 */
	TEST_READY("test_ready", 0),

	/**
	 * 可激活
	 */
	ACTIVE_REDAY("active_reday", 1),

	/**
	 * 已激活
	 */
	ACTIVE("active_alreday", 2),

	/**
	 * 已停用
	 */
	STOP("stop", 3),

	/**
	 * 已失效
	 */
	DISABLED("disabled", 4),

	/**
	 * 已清除
	 */
	CLEANED("cleaned", 5),

	/**
	 * 已更换
	 */
	EXCHANGED("exchanged", 6),

	/**
	 * 库存
	 */
	STORE("store", 7),

	/**
	 * 开始
	 */
	START("start", 8);



	/**
	 * 卡状态
	 */
	private String status;

	/**
	 * 卡状态code
	 */
	private Integer code;

	UnicomCardStatusEnum(String status, Integer code) {
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
		UnicomCardStatusEnum[] values = UnicomCardStatusEnum.values();
		for (UnicomCardStatusEnum value : values) {
			String status = value.getStatus();
			if (status.equals(cardStatus.getStatus())) {
				return value.getCode();
			}
		}
		return null;
	}

}
