package com.wangxin.iot.telecom.api;

import com.alibaba.fastjson.JSON;
import com.wangxin.iot.config.TelecomGatewayApiConfig;
import com.wangxin.iot.constants.TemplateConstants;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.other.TelecomChannelFactory;
import com.wangxin.iot.utils.DesUtils;
import com.wangxin.iot.utils.RequestSecretUtil;
import com.wangxin.iot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Component
@DependsOn("applicationContextUtil")
@Slf4j
public class TelecomGatewayApi {

	ThreadLocal<TelecomGatewayApiConfig> threadLocal = ThreadLocal.withInitial(()->{
		IotOperatorTemplate iotGateway = CacheComponent.getInstance().getTemplateByType(TemplateConstants.TELECOM_TYS.getMessage());
		return JSON.parseObject(iotGateway.getTemplate(), TelecomGatewayApiConfig.class);
	});
	@Autowired
	TelecomChannelFactory telecomChannelFactory;
	@Autowired
	RestTemplate restTemplate;
	/**
	 * 装备参数
	 * @param params
	 * @return
	 */
	private String assembleReq(Map<String,String> params){
		Map<String,Object> request = new HashMap<>();
		request.putAll(params);
		String access_number = params.get("access_number");
		String iccid = params.get("iccid");
		//先根据accessNumber查，没有根据iccid,在没有用默认的
		if(StringUtils.isNotEmpty(access_number)){
			threadLocal.set(telecomChannelFactory.getOperatorTemplate(access_number, null));
		}else if(StringUtils.isNotEmpty(iccid)){
			threadLocal.set(telecomChannelFactory.getOperatorTemplate(null, iccid));
		}
		//key值指密钥，由电信提供，每个客户都有对应的key值，key值平均分为三段如下：
		String appKey = threadLocal.get().getAppKey();
		String key1 = appKey.substring(0,3);
		String key2 = appKey.substring(3,6);
		String key3 = appKey.substring(6,9);
		//密码
		String password = threadLocal.get().getPassword();
		String passwordEnc = DesUtils.strEnc(password,key1,key2,key3);
		//用户名
		String userId = threadLocal.get().getUserId();
		this.removeOverParams(params);
		String[] arr = new String[params.size()+2];
		//将map中的值都放在string数组中
		Collection<String> values = params.values();
		Object[] objects = values.toArray();
		for (int i = 0; i < objects.length; i++) {
			arr[i] = (String) objects[i];
		}
		//用户名
		arr[values.size()] = userId;
		arr[values.size()+1] = password;
		String sign = DesUtils.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3);
		request.put("user_id",userId);
		request.put("password",passwordEnc);
		request.put("sign",sign);
		String unionParams = RequestSecretUtil.getParams(request);
		String serverUrl = threadLocal.get().getBuyUrl();
		return serverUrl+unionParams;
	}
	private void  removeOverParams(Map reqMap){
		reqMap.remove("needDtl");
	}
	/**
	 * 发送请求
	 * @param params
	 * @return
	 */
	public String launchReq(Map params){
		HttpHeaders headers = new HttpHeaders();
		HttpEntity<String> request = new HttpEntity<>(null,headers);
		ResponseEntity<String> response = null;
		String url = null;
		try {
			url = this.assembleReq(params);
			log.info("接入号：{}，请求地址：{}，参数：{}",params.get("access_number"),url,params.toString());
			response =  restTemplate.postForEntity(url, request, String.class);
			log.info("接入号：{}，接口返回：{}",params.get("access_number"),response.getBody());
		}catch (HttpClientErrorException e){
			log.error("接入号：{}，请求接口失败：{},原因：{}",params.get("access_number"),url,e.getResponseBodyAsString());
			e.printStackTrace();
			return null;
		}catch (Exception e){
			log.error("接入号：{}，请求接口失败：{},\n原因：{}",params.get("access_number"),url,e.getMessage());
			e.printStackTrace();
			return null;
		}finally {
			threadLocal.remove();
		}
		return  response.getBody();
	}

}
