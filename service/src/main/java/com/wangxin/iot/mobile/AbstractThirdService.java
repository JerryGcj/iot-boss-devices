package com.wangxin.iot.mobile;

import com.alibaba.fastjson.JSON;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.third.hu.OneLinkApiConfig;
import com.wangxin.iot.utils.OneLinkUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/3/11
 */
@Slf4j
public abstract class AbstractThirdService implements ThirdService {

    @Autowired
    protected RestTemplate restTemplate;

    @Override
    public boolean modifyCard(Map<String, String> reqParam, IotOperatorTemplate iotOperatorTemplate) {
        return false;
    }


    @Override
    public Map getCardDetails(String iccid, IotOperatorTemplate iotOperatorTemplate) {
        return null;
    }

    protected HttpHeaders getHeader(HttpHeaders httpHeaders, IotOperatorTemplate iotOperatorTemplate){
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        return httpHeaders;
    }
    protected String getToken(IotOperatorTemplate iotOperatorTemplate){
        return "";
    }
    protected Map<String, String> getCommonReqParam(Map<String, String> reqParam, IotOperatorTemplate iotOperatorTemplate){
        OneLinkApiConfig oneLinkApiConfig = JSON.parseObject(iotOperatorTemplate.getTemplate(), OneLinkApiConfig.class);
        reqParam.put("token",this.getToken(iotOperatorTemplate));
        reqParam.put("transid", OneLinkUtils.getTransid(oneLinkApiConfig.getAppId()));
        reqParam.put("url",oneLinkApiConfig.getUrl());
        reqParam.put("version",oneLinkApiConfig.getVersion());
        return reqParam;
    }
    public  Map call(Map<String,String> reqParam, IotOperatorTemplate iotOperatorTemplate){
        reqParam = getCommonReqParam(reqParam,iotOperatorTemplate);
        String url = OneLinkUtils.buildUrl(reqParam);
        log.info("请求url:{}",url);
        Map call = null;
        try {
            call = restTemplate.getForObject(url, Map.class);
        }catch (Exception e){
            log.error("请求异常，url:{}",url);
            e.printStackTrace();
        }
        log.info("接口返回:{}",call);
        return call;
    }
    public <T,E> T call(String url, E reqParam, Class<T> clazz, IotOperatorTemplate iotOperatorTemplate){
        HttpHeaders headers = new HttpHeaders();
        //老胡接口请求token时不设置请求头
        if(!url.contains("login")){
            this.getHeader(headers,iotOperatorTemplate);
        }
        HttpEntity<String> request = new HttpEntity<>(JSON.toJSONString(reqParam),headers);
        ResponseEntity<String> response = null;
        try {
            response =  restTemplate.postForEntity(url, request , String.class);
            log.info("请求地址：{}\n接口返回：{}",url,response.getBody());
        }catch (HttpClientErrorException e){
            log.error("请求接口失败：{},原因：{}",url,e.getResponseBodyAsString());
            e.printStackTrace();
            return null;
        }catch (Exception e){
            log.error("请求接口失败：{},\n原因：{}",url,e.getMessage());
            e.printStackTrace();
            return null;
        }
        return  JSON.parseObject(response.getBody(), clazz);
    }

}
