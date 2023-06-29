package com.wangxin.iot.unicom.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.config.IotGatewayApiConfig;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.model.IccidOperatorModel;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.unicom.IoTGatewayClient;
import com.wangxin.iot.unicom.request.CommonJsonRequest;
import com.wangxin.iot.unicom.response.CommonJsonResponse;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.MD5Util;
import com.wangxin.iot.utils.StringUtils;
import com.wangxin.iot.utils.wechat.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.DependsOn;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@DependsOn("applicationContextUtil")
@Slf4j
public class IoTGatewayApi implements InitializingBean{

	public static final Map<String, String> ICCID_OPERATOR_CACHE = new ConcurrentHashMap<>();
	@Autowired
	IotUnicomCardInfoMapper iotUnicomCardInfoMapper;

	@Autowired
	private RestTemplate restTemplate;


	@Override
	public void afterPropertiesSet(){
		//初始化所有卡通道
		List<IccidOperatorModel> operator = iotUnicomCardInfoMapper.getOperator();
		operator.stream().forEach(item->{
			ICCID_OPERATOR_CACHE.putIfAbsent(item.getIccid(),item.getOperatorId());
		});
		log.info("联通所有卡对应通道放入缓存");
	}

	public HttpHeaders generatorSign() {
		HttpHeaders headers = new HttpHeaders();
		String nonce = WXPayUtil.generateNonceStr();
		String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(new Date());
		String sign = MD5Util.md5Encrypt32Lower(("appid=gxdxyl030349cus&timestamp="+timestamp+"&nonce="+nonce+"&appkey=ojges#76sds"));
		headers.add("appid","gxdxyl030349cus");
		headers.add("timestamp",timestamp);
		headers.add("nonce",nonce);
		headers.add("sign",sign);
		return headers;
	}
	/**wsEditTerminal
	 * 更改设备属性：
	 * 属性值（targetValue）：
	 * 属性类型（changeType）
	 * 1 - 设备ID (也称为"Terminal ID"): 文字最多50字符
	 * 2 - Modem ID: 文字最多40字符
	 * 3 - SIM卡状态: "0": 可测试,"1": 可激活,2": 已激活,"3": 已停用,"4": 已失效,"5"": 已清除,"6": 已更换,"7": 库存,"8": 开始
	 * 4 - 资费计划名
	 * 6 - 客户名
	 * 7 - 用量限额重置
	 *     "DEFAULT": 无;
	 *     "TEMPORARY_OVERRIDE": 当前周期;
	 *     "PERMANENT_OVERRIDE": 正在进行
	 * 17 - 账户自定义1
	 * 18 - 账户自定义2
	 * 19 - 账户自定义3
	 * 21 - Secure SIM Username copy rule:
	 *     ""F"" :  Unknown
	 *     ""N"" :  Not enabled
	 *     ""O"" :  Once
	 *     ""A"" ""  Always
	 *     ""L"" :  Locked
	 * 22 - Secure SIM Password copy rule:
	 *     ""F"" :  Unknown
	 *     ""N"" :  Not enabled
	 *     ""O"" :  Once
	 *     ""A"" ""  Always
	 *     ""L"" :  Locked
	 * 28 - REAL_NAME_STATUS
	 * 实名制状态(0-不需实名未实名,1-需要实名未实名,2-需要实名已实名,3-不需要实名已实名).
	 * 42 - 运营商自定义1
	 * 43 - 运营商自定义2
	 * 44 - 运营商自定义3
	 * 45 - 运营商自定义4
	 * 46 - 运营商自定义5
	 * 47 - Customer Custom 1
	 * 48 - Customer Custom 2
	 * 49 - Customer Custom 3
	 * 50 - Customer Custom 4
	 * 51 - Customer Custom 5
	 * 68 - Sale Date: 格式yyyy-MM-dd
	 * 73 - 账户自定义4
	 * 74 - 账户自定义5
	 * 75 - 账户自定义6
	 * 76 - 账户自定义7
	 * 77 - 账户自定义8
	 * 78 - 账户自定义9
	 * 79 - 账户自定义10
	 * @param businessParams
	 * @return
	 */
	public CommonJsonResponse wsEditTerminal(Map<String,Object> businessParams){
		//生效情况默认同步
//		String asynchronous = StringUtils.isEmpty(businessParams.get("asynchronous").toString()) ? "0" : businessParams.get("effectiveDate").toString();
		businessParams.put("asynchronous","0");
//		String effectiveDate = StringUtils.isEmpty(businessParams.get("effectiveDate").toString()) ? DateUtils.getCurrentDateString(null) : businessParams.get("effectiveDate").toString();
		//生效时间不传默认是当前时间。
		businessParams.put("effectiveDate", DateUtils.getCurrentDateString(null));
		//拿到request对象
		CommonJsonRequest commonJsonRequest = this.assembleReq(businessParams);
		commonJsonRequest.setApiName("wsEditTerminal/V1/1Main");
		//设置请求参数
		commonJsonRequest.setParams(businessParams);
		return this.launchReq(commonJsonRequest);
	}
	/**
	 * 实名认证状态,sb接口，传19位iccid
	 * @param iccid
	 * @return
	 */
	public boolean realNameStatusQuery(String iccid){
		HashMap params = new HashMap();
		params.put("iccid", iccid);
		CommonJsonRequest commonJsonRequest = this.assembleReq(params);
		commonJsonRequest.setApiName("realNameStatusQuery/V1/1Main");
		commonJsonRequest.setParams(params);
		log.info("请求报文：{}",commonJsonRequest.getReqText());
		CommonJsonResponse commonJsonResponse = this.launchReq(commonJsonRequest);
		if(commonJsonResponse != null){
			Map<String, Object> data = commonJsonResponse.getData();
			String rspCode = data.get("rspcode").toString();
			return rspCode.equals("0001")?true: false;
		}
		return false;
	}

	/**
	 * 新实名认证状态查询,sb接口，传19位iccid
	 * @param iccid
	 * @return
	 */
	public boolean newRealNameStatusQuery(String iccid){
		HttpHeaders httpHeaders = this.generatorSign();
		httpHeaders.setContentType(MediaType.APPLICATION_JSON);
		HashMap params = new HashMap();
		params.put("iccid", iccid);
		log.info("卡号 {} 实名状态查询请求报文，{}", iccid, params);
		String response = restTemplate.postForObject("https://smz.cuiot.cn/smz/newapi/query/queryByIccid", new HttpEntity<>(params, httpHeaders), String.class);
		log.info("卡号 {} 实名状态查询返回，{}", iccid, response);
		if(!StringUtils.isEmpty(response)){
			JSONObject jsonObject = JSONObject.parseObject(response);
			if(jsonObject != null && jsonObject.getInteger("code")== 30005){
				return true;
			}
		}

		return false;
	}

	/**
	 * 根据卡号和周期，获取设备使用流量
	 * @param businessParams
	 * @return
	 */
	public CommonJsonResponse wsGetTerminalUsageDataDetails(Map<String,String> businessParams){
		HashMap params = new HashMap();
		params.put("iccid", businessParams.get("iccid"));
		String cycleStartDate = businessParams.get("cycleStartDate") == null ? DateUtils.getCurrentDateString(null) : businessParams.get("cycleStartDate");
		params.put("cycleStartDate",cycleStartDate);
		//拿到request对象
		CommonJsonRequest commonJsonRequest = this.assembleReq(params);
		commonJsonRequest.setApiName("wsGetTerminalUsageDataDetails/V1/1Main");
		//设置请求参数
		commonJsonRequest.setParams(params);
		//发送请求，返回结果
		return this.launchReq(commonJsonRequest);
	}
	/**
	 * 根据设备卡号，获取详情
	 * "SIM卡状态:
	 *     ""0"": 可测试,
	 *    ""1"": 可激活,
	 *     ""2"": 已激活,
	 *     ""3"": 已停用,
	 *     ""4"": 已失效,
	 *     ""5"""": 已清除,
	 *     ""6"": 已更换,
	 *     ""7"": 库存,
	 *     ""8"": 开始"
	 * @param businessParams
	 * @return
	 */
	public CommonJsonResponse wsGetTerminalDetails(Map businessParams){
		HashMap params = new HashMap();
		params.put("iccids", businessParams.get("iccids"));
		//拿到request对象
		CommonJsonRequest commonJsonRequest = this.assembleReq(params);
		//设置请求参数
		commonJsonRequest.setParams(params);
		commonJsonRequest.setApiName("wsGetTerminalDetails/V1/1Main");
		//发送请求，返回结果
		return this.launchReq(commonJsonRequest);
	}

	/**
	 * 返回联通资费计划内的总用量，只有上个周期结束了，该方法才会返回总用量
	 * @param businessParams
	 * @return
	 */
	public CommonJsonResponse getTerminalUsage(Map businessParams){
		HashMap params = new HashMap();
		params.put("iccid", businessParams.get("iccid"));
		//这个字段代表上个周期的时间，202007，
		params.put("billingCycle", businessParams.get("billingCycle"));
		//拿到request对象
		CommonJsonRequest commonJsonRequest = this.assembleReq(params);
		//设置请求参数
		commonJsonRequest.setParams(params);
		commonJsonRequest.setApiName("wsGetTerminalUsage/V1/1Main");
		//发送请求，返回结果
		return this.launchReq(commonJsonRequest);
	}
	public CommonJsonResponse wsGetProvisioningStatus(String iccid,String changeType){
		HashMap params = new HashMap();
		params.put("iccid",iccid);
		//这个字段代表上个周期的时间，202007，
		params.put("changeType", StringUtils.isEmpty(changeType)?"3":changeType);
		//拿到request对象
		CommonJsonRequest commonJsonRequest = this.assembleReq(params);
		//设置请求参数
		commonJsonRequest.setParams(params);
		commonJsonRequest.setApiName("wsStatus/V1/1Main");
		//发送请求，返回结果
		return this.launchReq(commonJsonRequest);
	}

	/**
	 * 更改与给定设备相关联的通信计划
	 * @param businessParams
	 * @return
	 */
	public CommonJsonResponse editNetworkAccessConfig(Map<String,Object> businessParams){
		//生效情况默认同步
		businessParams.put("asynchronous","0");
		//生效时间不传默认是当前时间。
		businessParams.put("effectiveDate", DateUtils.getCurrentDateString(null));
		businessParams.put("iccid",businessParams.get("iccid"));
		businessParams.put("nacId",businessParams.get("nacId"));
		CommonJsonRequest commonJsonRequest = this.assembleReq(businessParams);
		commonJsonRequest.setApiName("wsEditNetworkAccessConfig/V1/1Main");
		//设置请求参数
		commonJsonRequest.setParams(businessParams);
		return this.launchReq(commonJsonRequest);
	}

	/**
	 * 装备参数
	 * @param params
	 * @return
	 */
	public CommonJsonRequest assembleReq(Map params){
		Object iccid = params.get("iccid");
		if(iccid == null){
			List iccids;
			if(params.get("iccids") instanceof String[]){
				iccids = Arrays.asList((String[])params.get("iccids"));
			}else{
				iccids = (List)params.get("iccids");
			}
			iccid = iccids.get(0);
		}
		String operatorId = ICCID_OPERATOR_CACHE.get(String.valueOf(iccid));
		if(operatorId == null){
			QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
			queryWrapper.eq("iccid",iccid);
			IotUnicomCardInfo iotUnicomCardInfo = this.iotUnicomCardInfoMapper.selectOne(queryWrapper);
			ICCID_OPERATOR_CACHE.putIfAbsent(iotUnicomCardInfo.getIccid(), iotUnicomCardInfo.getOperatorId());
			operatorId = iotUnicomCardInfo.getOperatorId();
		}
		IotOperatorTemplate templateByOperation = CacheComponent.getInstance().getTemplateByOperation(operatorId);
		IotGatewayApiConfig iotGatewayApiConfig = JSON.parseObject(templateByOperation.getTemplate(), IotGatewayApiConfig.class);
		CommonJsonRequest request = new CommonJsonRequest();
		request.setApiVer(iotGatewayApiConfig.getVersion());
		params.put("messageId", "1");
		params.put("version", iotGatewayApiConfig.getParamVersion());
		params.put("openId", iotGatewayApiConfig.getOpenId());
		return request;

	}

	/**
	 * 发送请求
	 * @param commonJsonRequest
	 * @return
	 */
	public CommonJsonResponse launchReq(CommonJsonRequest commonJsonRequest){
		try {
			Map<String, Object> params = commonJsonRequest.getParams();
			if(CollectionUtils.isEmpty(params)){
				return null;
			}
			Object iccid = params.get("iccid");
			if(iccid == null){
				List iccids;
				if(params.get("iccids") instanceof String[]){
					iccids = Arrays.asList((String[])params.get("iccids"));
				}else{
					iccids = (List)params.get("iccids");
				}
				iccid = iccids.get(0);
			}
			String operatorId = ICCID_OPERATOR_CACHE.get(String.valueOf(iccid));
			if(operatorId == null){
				QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
				queryWrapper.eq("iccid",iccid);
				IotUnicomCardInfo iotUnicomCardInfo = this.iotUnicomCardInfoMapper.selectOne(queryWrapper);
				ICCID_OPERATOR_CACHE.putIfAbsent(iotUnicomCardInfo.getIccid(), iotUnicomCardInfo.getOperatorId());
				operatorId = iotUnicomCardInfo.getOperatorId();
			}
			IotOperatorTemplate templateByOperation = CacheComponent.getInstance().getTemplateByOperation(operatorId);
			IotGatewayApiConfig iotGatewayApiConfig = JSON.parseObject(templateByOperation.getTemplate(), IotGatewayApiConfig.class);
			IoTGatewayClient ioTGatewayClient = new IoTGatewayClient(iotGatewayApiConfig.getServerUrl(),iotGatewayApiConfig.getAppId(),iotGatewayApiConfig.getAppSecret());
			return ioTGatewayClient.execute(commonJsonRequest);
		} catch (Exception e) {
			log.error("请求url {} 出现异常：{}", commonJsonRequest.getApiName() ,e);
			e.printStackTrace();
		}
		return null;
	}

}
