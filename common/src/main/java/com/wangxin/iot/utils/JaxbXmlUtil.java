package com.wangxin.iot.utils;

import java.io.StringReader;
import java.io.StringWriter;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Jaxb工具类 xml和java类相互转换
 *
 */
public class JaxbXmlUtil {
	public static final String DEFAULT_ENCODING = "UTF-8";

	/**
	 * pojo转换成xml 默认编码UTF-8
	 *
	 * @param obj 待转化的对象
	 * @return xml格式字符串
	 * @throws Exception JAXBException
	 */
	public static String convertToXml(Object obj) throws Exception {
		return convertToXml(obj, DEFAULT_ENCODING);
	}

	/**
	 * pojo转换成xml
	 *
	 * @param obj      待转化的对象
	 * @param encoding 编码
	 * @return xml格式字符串
	 * @throws Exception JAXBException
	 */
	public static String convertToXml(Object obj, String encoding) throws Exception {
		String result = null;

		JAXBContext context = JAXBContext.newInstance(obj.getClass());
		Marshaller marshaller = context.createMarshaller();
		// 格式化xml
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.setProperty(Marshaller.JAXB_ENCODING, encoding);
//		marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);

		StringWriter writer = new StringWriter();
		marshaller.marshal(obj, writer);
		result = writer.toString();

		return result;
	}

	/**
	 * xml转换成JavaBean
	 *
	 * @param xml xml格式字符串
	 * @param t   待转化的对象
	 * @return 转化后的对象
	 * @throws Exception JAXBException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T convertToJavaBean(String xml, Class<T> t) throws Exception {
		T obj = null;
		JAXBContext context = JAXBContext.newInstance(t);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		obj = (T) unmarshaller.unmarshal(new StringReader(xml));
		return obj;
	}
}
