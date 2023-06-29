package com.wangxin.iot.utils;

import com.wangxin.iot.constants.SendSMSConstants;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/11/11
 */
@Slf4j
public class SendSmsUtil {
    /**
     * 发送短信工具类
     * @param
     * @param content
     */
    public static String sendSms(String content){
       return SendSmsUtil.sendSms(SendSMSConstants.PHONES.getValue(), content);
    }
    public static String sendSms(String mobile,String content){
        try{
            String ts = String.valueOf(System.currentTimeMillis());
            String sign = MD5Util.md5Encrypt32Lower(SendSMSConstants.USERID.getValue()+ts+SendSMSConstants.APIKEY.getValue());
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("userid", SendSMSConstants.USERID.getValue());
            params.put("ts",ts);
            params.put("sign",sign);
            params.put("mobile", mobile);
            params.put("msgcontent", content);
            return HttpClientHelper.post(SendSMSConstants.SENDURL.getValue(), params, "UTF-8");
        }catch (Exception e){
            log.error("发送预警短信  {}  请求异常：{}", content, e);
            return null;
        }
    }
}
