package com.wangxin.iot.constants;

/**
 * @Description : 队列常量
 * @author: Mark (majianyou@wxdata.cn)
 * @version:V1.0
 * @Date: 2018/09/28
 */
public enum MqConstants {
	/**
	 * 物联网卡 队列名称
	 */
	CARD_QUEUE_NAME("card.message"),
	/**
	 * 物联网卡 转发器名称
	 */
	CARD_EXCHANGE_NAME("card.exchange"),

	CARD_EXCHANGE_NORMAL_ROUTING_KEY("card.exchange.normal"),
	/**
	 * 物联网卡 死信队列名称
	 */
	CARD_DEAD_QUEUE_NAME("card.dead.message"),
	/**
	 * 物联网卡 死信队列转发器名称
	 */
	CARD_DEAD_EXCHANGE_NAME("card.dead.exchange"),

	CARD_EXCHANGE_DEAD_ROUTING_KEY("card.exchange.dead");

	MqConstants(String message){
		this.message=message;
	}

	private String message;

	public String getMessage(){
		return this.message;
	}
}
