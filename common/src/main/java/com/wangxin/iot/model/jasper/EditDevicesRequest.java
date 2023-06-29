package com.wangxin.iot.model.jasper;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

/**
 * 联通Jasper 编辑设备详情接口请求参数
 * @author wx
 *
 */
@Data
public class EditDevicesRequest {
	@JSONField(serialize = false)
	private String iccid;
	/**
	 * （可选）更改将生效的日期（请参阅日期格式）。如果省略此参数，更改将立即生效。
	 */
	private String effectiveDate;
	
	/**
	 * 设备的 SIM 卡状态。有关有效值的列表，请参阅 SIM 卡状态值。
	 */
	private String status;
	
	/**
	 * 资费计划的名称。
	 */
	private String ratePlan;
	
	/**
	 * 通信计划的名称。
	 */
	private String communicationPlan;
	
	/**
	 * 客户的名称。客户用户无法更改该值。
	 */
	private String customer;
	
	/**
	 * 账户或客户可为设备分配的可选标识符。
	 */
	private String deviceID;
	
	/**
	 * 调制解调器 ID 号。
	 */
	private String modemID;
	
	/**
	 * 确定设备在达到资费计划中定义的流量上限时的行为。有效值为：
	 * DEFAULT。设备不能超过流量上限。
	 * TEMPORARY_OVERRIDE。设备可以使用任何数量的流量，直到当前计费周期结束，此时 Control Center 将开始实施资费计划中设置的流量上限。
	 * PERMANENT_OVERRIDE。设备可以使用任何数量的流量，不考虑资费计划中定义的流量上限。
	 */
	private String overageLimitOverride;

}
