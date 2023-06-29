package com.wangxin.iot.model.jasper;

import lombok.Data;

/**
 * 联通Jasper 获取设备详情接口响应参数
 * @author wx
 *
 */
@Data
public class GetDevicesDetailResponse extends CommonResponse {
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
	 * 与此设备关联的客户名称（如果有，通常是企业或业务部门）。
	 */
	private String customer;
	
	/**
	 * 与此设备关联的人员（如果有）的 ID。
	 */
	private String endConsumerId;
	
	/**
	 * 首次激活设备的日期。请参阅日期格式了解详细信息。
	 */
	private String dateActivated;
	
	/**
	 * 上次更新设备信息的日期。请参阅日期格式了解详细信息。
	 */
	private String dateUpdated;
	
	/**
	 * 设备 SIM 卡从运营商库存转移到企业账户的日期。请参阅日期格式了解详细信息。
	 */
	private String dateShipped;
	
	/**
	 * 与设备关联的企业账户的 ID。
	 */
	private String accountId;
	
	/**
	 * 如果与设备关联的通信计划使用固定 IP 地址，则为与此设备关联的 IP 地址。如果通信计划使用动态 IP 地址，则此字段将为空。
	 */
	private String fixedIPAddress;
	
	/**
	 * 运营商添加的有关设备的信息。
	 */
	private String simNotes;
	
	/**
	 * 账户或客户可为设备分配的可选标识符。
	 */
	private String deviceID;
	
	/**
	 * 标识设备用于传输数据的调制解调器。
	 */
	private String modemID;
	
	/**
	 * 对于利用 Control Center 的全球 SIM 卡功能的企业，该值指示设备是使用主要运营商的主 SIM 卡还是合作伙伴运营商的虚拟订购。
	 */
	private String globalSIMType;

}
