package com.wangxin.iot.model.jasper;

import lombok.Data;

/**
 * 联通Jasper 获取设备用量接口响应参数
 * @author wx
 *
 */
@Data
public class CtdUsagesResponse extends CommonResponse {
	/**
	 * 设备的 ICCID。
	 */
	private String iccid;
	
	/**
	 * 设备的 IMSI。
	 */
	private String imsi;
	
	/**
	 * 设备的 MSISDN 或电话号码。
	 */
	private String msisdn;
	
	/**
	 * 设备的 IMEI。
	 */
	private String imei;
	
	/**
	 * 设备的 SIM 卡状态。
	 */
	private String status;
	
	/**
	 * 与设备关联的资费计划的名称。
	 */
	private String ratePlan;
	
	/**
	 * 与设备关联的通信计划的名称。
	 */
	private String communicationPlan;
	
	/**
	 * 自计费周期开始后使用的流量（以字节计）。
	 */
	private Integer ctdDataUsage;
	
	/**
	 * 自计费周期开始后的移动台始发消息和移动台终止消息的计数。
	 */
	private Integer ctdSMSUsage;
	
	/**
	 * 自计费周期开始后使用的通话秒数。
	 */
	private Integer ctdVoiceUsage;
	
	/**
	 * 自计费周期开始后的数据会话数量。
	 */
	private Integer ctdSessionCount;
	
	/**
	 * True/False 值，指示设备是否达到资费计划中设置的流量上限。
	 */
	private Boolean overageLimitReached;
	
	/**
	 * 指示设备能否超过资费计划中指定的流量上限。可能的值有：
	 * DEFAULT。设备不能超过流量上限。
	 * TEMPORARY_OVERRIDE。设备可以使用任何数量的流量，直到当前计费周期结束，此时 Control Center 将开始实施资费计划中设置的流量上限。
	 * PERMANENT_OVERRIDE。设备可以使用任何数量的流量，不考虑资费计划中定义的流量上限。
	 */
	private String overageLimitOverride;

}
