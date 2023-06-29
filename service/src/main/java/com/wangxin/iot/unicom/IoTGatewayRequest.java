package com.wangxin.iot.unicom;

import java.util.Map;

public interface IoTGatewayRequest<T extends IoTGatewayResponse> {

	String getContentType();

	String getApiName();

	String getApiVer();

	String getReqText();

	/**
	 * 获取所有的Key-Value形式的文本请求参数集合。其中：
	 * <ul>
	 * <li>Key: 请求参数名</li>
	 * <li>Value: 请求参数值</li>
	 * </ul>
	 *
	 * @return 文本请求参数集合
	 */
	Map<String, Object> getParams();


	/**
	 * 获取具体响应实现类的定义。
	 */
	Class<T> getResponseClass();


	/**
	 * 客户端参数检查，减少服务端无效调用。
	 */
	void check() throws ApiRuleException;

	void execProcessBeforeReqSend(Object[] params);

}
