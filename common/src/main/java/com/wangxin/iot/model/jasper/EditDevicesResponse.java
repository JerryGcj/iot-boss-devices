package com.wangxin.iot.model.jasper;

import lombok.Data;

/**
 * 联通Jasper 编辑设备详情接口响应参数
 * @author wx
 *
 */
@Data
public class EditDevicesResponse extends CommonResponse {
	/**
	 * Control Center 将更新的设备的唯一标识符。
	 */
	private String iccid;

}
