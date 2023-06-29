package com.wangxin.iot.unicom.request;


import com.wangxin.iot.unicom.ApiRuleException;
import com.wangxin.iot.unicom.BaseIoTGatewayRequest;
import com.wangxin.iot.unicom.internal.utils.ApaasSignUtil;
import com.wangxin.iot.unicom.internal.utils.json.JSONWriter;
import com.wangxin.iot.unicom.response.CommonJsonResponse;

import java.util.Map;

public class CommonJsonRequest extends BaseIoTGatewayRequest<CommonJsonResponse> {

	private Map<String, Object> params;

	@Override
	public Class<CommonJsonResponse> getResponseClass() {
		return CommonJsonResponse.class;
	}

	@Override
	public void check() throws ApiRuleException {

	}
	@Override
	public void execProcessBeforeReqSend(Object[] params) {
		Map<String, Object> map = (Map<String, Object>) params[0];
		this.setTransId((String) map.get(ApaasSignUtil._trans_id));
		this.setReqText(new JSONWriter().write(map));
	}

	@Override
	public Map<String, Object> getParams() {
		return params;
	}

	public void setParams(Map<String, Object> params) {
		this.params = params;
	}


}
