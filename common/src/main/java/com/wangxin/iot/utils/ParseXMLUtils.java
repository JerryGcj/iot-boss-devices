package com.wangxin.iot.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.xml.sax.InputSource;

public class ParseXMLUtils {

	public static List<Map<String, String>> parse(String xmlDoc) throws JDOMException, IOException {
        // 创建一个新的字符串     
        StringReader xmlString = new StringReader(xmlDoc);     
        // 创建新的输入源SAX 解析器将使用 InputSource 对象来确定如何读取 XML 输入     
        InputSource source = new InputSource(xmlString);     
        // 创建一个新的SAXBuilder     
        SAXBuilder saxb = new SAXBuilder();
        List<Map<String, String>> result = null;
		//Map<String, String> map = new HashMap<String, String>();
		result = new ArrayList<Map<String, String>>();
		// 通过输入源构造一个Document
		Document doc = saxb.build(source);
		// 取的根元素
		Element root = doc.getRootElement();

		// 得到根元素所有子元素的集合
		List<Element> node = root.getChildren();
		Element et = null;
		for (int i = 0; i < node.size(); i++) {
			et = (Element) node.get(i);// 循环依次得到子元素
			List<Element> subNode = et.getChildren(); // 得到内层子节点
			Map<String, String> map = new HashMap<String, String>();
			map.put(et.getName(), et.getText());
			Element subEt = null;
			for (int j = 0; j < subNode.size(); j++) {
				subEt = (Element) subNode.get(j); // 循环依次得到子元素
				map.put(subEt.getName(), subEt.getText()); // 装入到Map中
			}
			// Map获取到值时才装入
			if (map.size() > 0) {
				result.add(map);
			}
		}
        return result;
    }
	
	public static void main(String[] args) throws JDOMException, IOException {
		String xml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
				"<SvcCont xmlns=\"\">\n" +
				"      <RESULT>\n" +
				"            <activeTime>2017-05-18</activeTime>\n" +
				"          <prodStatusName>在用</prodStatusName>\n" +
				"            <certNumber>370205621219253</certNumber>\n" +
				"            <number>1064950046512</number>\n" +
				"      </RESULT>\n" +
				"        <resultCode>0</resultCode>\n" +
				"      <resultMsg>处理成功！</resultMsg>\n" +
				"<GROUP_TRANSACTIONID>1111111111201803150000300002</GROUP_TRANSACTIONID>\n" +
				"  </SvcCont>";
				//"<?xml version=\"1.0\" encoding=\"utf-8\" ?><report><item><msgid>49173846</msgid><phone>18396872245</phone><reporttime>2018-04-16 18:36:31</reporttime><state>0</state><stateinfo>Retrieved--1000</stateinfo></item><item><msgid>49174404</msgid><phone>18396872245</phone><reporttime>2018-04-16 18:36:32</reporttime><state>0</state><stateinfo>Retrieved--1000</stateinfo></item></report>";
		List<Map<String, String>> list = parse(xml);

		for(int i=0;i<list.size();i++){
			String msgid = "";
			String phone = "";
			String state = "";
			String reporttime = "";
			Map<String, String> map = list.get(i);
			Iterator<String> iters = map.keySet().iterator();
			while(iters.hasNext()){
				String key = iters.next().toString();
				if(key=="resultCode"){
					msgid = map.get(key).toString();
				}else if(key=="prodStatusName"){
					phone = map.get(key).toString();
				}else if(key=="certNumber"){
					state = map.get(key).toString();
				}else if(key=="reporttime"){
					reporttime = map.get(key).toString();
				}

			}
			System.out.println(msgid);
			System.out.println(phone);
			System.out.println(state);
		}
	}
}
