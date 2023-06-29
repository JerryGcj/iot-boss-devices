package com.wangxin.iot.unicom.internal.parser.xml;


import com.wangxin.iot.unicom.ApiException;
import com.wangxin.iot.unicom.IoTGatewayParser;
import com.wangxin.iot.unicom.IoTGatewayResponse;
import com.wangxin.iot.unicom.internal.mapping.Converter;

/**
 * 单个JSON对象解释器。
 *
 * @author carver.gu
 * @since 1.0, Apr 11, 2010
 */
public class ObjectXmlParser<T extends IoTGatewayResponse> implements IoTGatewayParser<T> {

	private Class<T> clazz;

	public ObjectXmlParser(Class<T> clazz) {
		this.clazz = clazz;
	}

	@Override
	public T parse(String rsp) throws ApiException {
		Converter converter = new XmlConverter();
		return converter.toResponse(rsp, clazz);
	}

	@Override
	public Class<T> getResponseClass() {
		return clazz;
	}

}
