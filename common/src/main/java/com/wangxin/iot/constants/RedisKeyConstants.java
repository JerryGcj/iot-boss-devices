package com.wangxin.iot.constants;

/**
 * @Description : Redis Key 维护
 * @author: winner (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018/08/23
 */
public enum RedisKeyConstants {
	/**
	 * 卡信息缓存
	 */
	IOT_DEVICE_CACHE("iot_device_cache"),

	/**
	 * 定时任务要处理的卡信息
	 */
	TASK_CARD_SLOWLY("task_card_slowly"),

	/**
	 * 定时任务要及时处理和失败处理的卡信息
	 */
	TASK_CARD_TIMELY("task_card_timely"),
	/**
	 *  活动初始化 相关活动信息，KEY 的后置模块命名
	 *  例：[CCB2012312_INIT]
	 */
	ICCID_BLACK_LIST("iccid_black_list"),
	ICCID_USAGE("iccid_usage"),
	ONE_LINK_TOKEN("one_link_token"),
	TASK_ACTIVE_ERROR("task_active_error"),
	TASK_ACTIVE("task_active"),
	ICCID_TEMPLATE("iccid_template"),

	REAL_NAME_CHANNEL("real_name_channel"),
	UN_REAL_NAME_ORDER("un_real_name_order"),
	/**
	 * 卡导入时，同步卡详情的队列
	 */
	NEED_SYNC_CARD("need_sync_card"),
	/**
	 * 导入完成，发步到该订阅模型中
	 */
	NEED_SYNC_CHANNEL("need_sync_channel"),
	/**
	 * 联通卡查询订单支付成功
	 */
	UNICOM_ORDER_PROFITS_CHANNEL("unicom_order_profits_channel"),
	/**
	 * 内存用量标志位
	 */
	UPDATE_USAGE_CONTAINER("update_usage_container"),
	/**
	 * 电信卡容器
	 */
	TELECOM_NEED_SYNC_CHANNEL("telecom_need_sync_channel"),
	MOBILE_UPDATE_USAGE_CONTAINER("mobile_update_usage_container"),
	/**
	 * 导入的电信卡iccid
	 */
	TELECOM_NEED_SYNC_ACCESSNUMBER("telecom_need_sync_accessNumber"),
	TELECOM_EQUIPMENT_CHANNEL("telecom_equipment_channel"),
	TELECOM_EQUIPMENT_ACCESSNUMBER("telecom_equipment_accessNumber");
	private String message;
	RedisKeyConstants(String message) {
		this.message=message;
	}
	public String getMessage(){
		return this.message;
	}
}
