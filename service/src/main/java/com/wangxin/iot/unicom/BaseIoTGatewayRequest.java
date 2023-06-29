package com.wangxin.iot.unicom;

public abstract class BaseIoTGatewayRequest<T extends IoTGatewayResponse> implements IoTGatewayRequest<T> {

	protected String apiName;
	protected String apiVer;
	protected String apiType;
	protected String reqText;
	protected String transId;

	@Override
	public String getContentType(){
		if(apiType == null){
			return null;
		}
		if(apiType.equals(Constants.API_TYPE_JSON)){
			return "application/json";
		}else{
			return "text/xml";
		}
	}
	@Override
	public String getApiName() {
		return apiName;
	}
	@Override
	public String getApiVer() {
		return apiVer;
	}
	@Override
	public String getReqText() {
		return reqText;
	}
	public String getTransId() {
        return transId;
    }

    public void setTransId(String transId){
        this.transId = transId;
    }


	public void setApiName(String apiName) {
		this.apiName = apiName;
	}



	public void setApiVer(String apiVer) {
		this.apiVer = apiVer;
	}

	public String getApiType() {
		return apiType;
	}

	public void setApiType(String apiType) {
		this.apiType = apiType;
	}



	public void setReqText(String reqText) {
		this.reqText = reqText;
	}
}
