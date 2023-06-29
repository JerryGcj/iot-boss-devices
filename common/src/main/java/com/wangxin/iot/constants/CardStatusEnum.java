package com.wangxin.iot.constants;

/**
 * 平台card状态枚举
 */
public enum CardStatusEnum {
	/**
	 * 可测试
	 */
	TEST_READY("TEST_READY", 0),

	/**
	 * 可激活
	 */
	ACTIVAT_REDAY("ACTIVAT_REDAY", 1),

	/**
	 * 已激活
	 */
	ACTIVATED("ACTIVATED", 2),

	/**
	 * 停机
	 */
	DEACTIVATED("DEACTIVATED", 3),

	/**
	 * 已失效
	 */
	FAILURE("FAILURE", 4),

	/**
	 * 已清除
	 */
	RETIRED("RETIRED", 5),

	/**
	 * 已更换
	 */
	REPLACE("REPLACE", 6),

	/**
	 *库存inventory
	 */
	INVENTORY("INVENTORY", 7),

	/**
	 * 开始
	 */
	START("START", 8);


	/**
	 * 卡状态
	 */
	private String status;

	/**
	 * 卡状态code
	 */
	private Integer code;

	CardStatusEnum(String status, Integer code) {
		this.status=status;
		this.code = code;
	}

	public String getStatus() {
		return this.status;
	}
	public Integer getCodeInt(){
		return this.code;
	}
	public String getCode() {
		return String.valueOf(code);
	}

	/**
	 * 将通道的card状态转化为平台的card状态
	 * @param cardStatus
	 * @return
	 */
	public String convertStatus(CardStatus cardStatus) {
		CardStatusEnum[] values = CardStatusEnum.values();
		for (CardStatusEnum value : values) {
			String status = value.getStatus();
			if (status.equals(cardStatus.getStatus())) {
				return value.getCode();
			}
		}
		return null;
	}
	public static String getOwnCodeWithOnelink(Integer code){
		String cardState = null;
		switch (code){
			case 1:
				// 待激活--待激活
				cardState = "0";
				break;
			case 2:
				// 已激活--正常
				cardState = "3";
				break;
			case 4:
				// 停机--停机
				cardState = "4";
				break;
			case 6:
				// 已停用--停机
				cardState = "2";
				break;
			case 7:
				// 库存--沉默期
				cardState = "1";
				break;
			case 9:
				// 已销户--已销号
				cardState = "5";
				break;
			default:
				cardState = "6";
				break;
		}
		return cardState;
	}
}
