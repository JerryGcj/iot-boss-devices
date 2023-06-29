package com.wangxin.iot.unicom.internal.parser.json;

import com.wangxin.iot.unicom.ApiException;
import com.wangxin.iot.unicom.IoTGatewayParser;
import com.wangxin.iot.unicom.IoTGatewayResponse;
import com.wangxin.iot.unicom.internal.mapping.Converter;

public class ObjectJsonParser<T extends IoTGatewayResponse> implements IoTGatewayParser<T> {

	private Class<T> clazz;
	private boolean simplify;

	public ObjectJsonParser(Class<T> clazz) {
		this.clazz = clazz;
	}

	public ObjectJsonParser(Class<T> clazz, boolean simplify) {
		this.clazz = clazz;
		this.simplify = simplify;
	}

	public T parse(String rsp) throws ApiException {
		Converter converter;
		if (this.simplify) {
			converter = new SimplifyJsonConverter();
		} else {
			converter = new JsonConverter();
		}
		return converter.toResponse(rsp, clazz);
	}

	public Class<T> getResponseClass() {
		return clazz;
	}

}
