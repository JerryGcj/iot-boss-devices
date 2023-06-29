package com.wangxin.iot.model.jasper;

import com.wangxin.iot.utils.StringUtil;
import lombok.Data;

/**
 * 联通Jasper公共响应参数
 * @author wx
 *
 */
@Data
public class CommonResponse {
	/**
	 * 错误代码
	 */
	private String errorCode;
	
	/**
	 * 错误信息
	 */
	private String errorMessage;

	public Boolean isSuccess() {
		if (StringUtil.isEmpty(errorCode)) {
			return Boolean.TRUE;
		}
		return Boolean.FALSE;
	}
}
