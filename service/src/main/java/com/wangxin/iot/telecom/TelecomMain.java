package com.wangxin.iot.telecom;

import com.wangxin.iot.utils.DesUtils;
import com.wangxin.iot.utils.RequestSecretUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/11/2
 */
public class TelecomMain {
    /**
     * 接口调用示例代码(Java)
     * @Title: demo.java
     * @date 2017年1月23日 下午13:19:22
     * @version V1.0
     * @Copyright 中国电信股份有限公司物联网分公司
     * @description API开发手册登录物联网自管理门户->在线文档查看
     */
    public static void main(String[] args) {
//        String[] params = {"queryIMSI","1410326832530"};
//        String[] params = {"queryBindDetection","1410317813236"};
//        String[] params = {"queryTraffic","1410317813236"};
//        String[] params = {"getTelephone","8986112121503935231"};
        String[] params = {"queryTrafficOfToday","1410348000030"};
        System.out.println(transfer(params));
    }
    public static String transfer(String[] args){
        //key值指密钥，由电信提供，每个客户都有对应的key值，key值平均分为三段如下：
//        String key1 = "xg5";
//        String key2 = "aGd";
//        String key3 = "il2";
//        1cly4kKHJ
        String key1 = "N9C";
        String key2 = "1KH";
        String key3 = "rTT";
        //密码
        String password = "DCw013Wz1g5VSX4K";
//        String password = "hze35Y2PYj2zF8L5";
        String passwordEnc = DesUtils.strEnc(password,key1,key2,key3);
        //接口名-套餐使用量查询
        String method = args[0];
        String access_number= args[1] ;
        //用户名
//        String user_id = "W68b8siQTUue24FU57jnXCG5yx8qbNP9";
        String user_id = "Kyt76HwMgf67ki4cV0x9R660FSrlK2N8";
        String[] arr = {access_number,user_id,method,password};


        String sign = DesUtils.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3);

        Map<String,Object> request = new HashMap<>();
        request.put("method",method);
//        request.put("iccid", access_number);
        request.put("access_number", access_number);
        request.put("user_id",user_id);
        request.put("sign",sign);
//        request.put("needDtl","1");
        request.put("password",passwordEnc);
        String buyUrl = "http://api.ct10649.com:9001/m2m_ec/query.do";
        String params = RequestSecretUtil.getParams(request);
        return buyUrl+params;
    }
    public static void test(){
        //key值指密钥，由电信提供，每个客户都有对应的key值，key值平均分为三段如下：
        String key1 = "xg5";
        String key2 = "aGd";
        String key3 = "il2";
        //密码
        String password = "hze35Y2PYj2zF8L5";
        String passwordEnc = DesUtils.strEnc(password,key1,key2,key3);
        //接口名-套餐使用量查询
        String method = "queryCardMainStatus";
        String access_number="1064912823278";
        //用户名
        String user_id = "W68b8siQTUue24FU57jnXCG5yx8qbNP9";
        String[] arr = {access_number,user_id,method,password};


        String sign = DesUtils.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3);

        Map<String,Object> request = new HashMap<>();
        request.put("method",method);
        request.put("access_number", access_number);
        request.put("user_id",user_id);
        request.put("sign",sign);
        request.put("password",passwordEnc);
        String buyUrl = "http://api.ct10649.com:9001/m2m_ec/query.do";
        String params = RequestSecretUtil.getParams(request);
        System.out.println(buyUrl+params);
    }
    public static void test2(){
        //key值指密钥，由电信提供，每个客户都有对应的key值，key值平均分为三段如下：
        String key1 = "xg5";
        String key2 = "aGd";
        String key3 = "il2";
        //密码
        String password = "hze35Y2PYj2zF8L5";
        String passwordEnc = DesUtils.strEnc(password,key1,key2,key3);
        //接口名-套餐使用量查询
        String method = "realNameQueryIot";
        String access_number="1064912823278";
        //用户名
        String user_id = "W68b8siQTUue24FU57jnXCG5yx8qbNP9";
        String[] arr = {access_number,user_id,method,password};


        String sign = DesUtils.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3);

        Map<String,Object> request = new HashMap<>();
        request.put("method",method);
        request.put("access_number", access_number);
        request.put("user_id",user_id);
        request.put("sign",sign);
        request.put("password",passwordEnc);
        String buyUrl = "http://api.ct10649.com:9001/m2m_ec/query.do";
        String params = RequestSecretUtil.getParams(request);
        System.out.println(buyUrl+params);
    }
    public static void disabledNumber(){
        //key值指密钥，由电信提供，每个客户都有对应的key值，key值平均分为三段如下：
        String key1 = "xg5";
        String key2 = "aGd";
        String key3 = "il2";
        //密码
        String password = "hze35Y2PYj2zF8L5";
        String passwordEnc = DesUtils.strEnc(password,key1,key2,key3);
        //接口名-套餐使用量查询
        String method = "disabledNumber";
        String access_number = "1064912823278";
        //用户名
        String user_id = "W68b8siQTUue24FU57jnXCG5yx8qbNP9";
        String orderTypeId = "20";
        String acctCd = "";
        String[] arr = {user_id,access_number,orderTypeId,acctCd,method,password};


        String sign = DesUtils.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3);

        Map<String,Object> request = new HashMap<>();
        request.put("access_number",access_number);
        request.put("method",method);
        request.put("orderTypeId",orderTypeId);
        request.put("acctCd",acctCd);
        request.put("user_id",user_id);
        request.put("sign",sign);
        request.put("password",passwordEnc);
        String buyUrl = "http://api.ct10649.com:9001/m2m_ec/query.do";
        String params = RequestSecretUtil.getParams(request);
        System.out.println(buyUrl+params);
    }
    public void demo(){
        //具体接口参数需参照自管理门户在线文档
        String access_number="14914000000";  //物联网卡号(149或10649号段)
        String user_id = "test";     //用户名
        String password = "test";    //密码
        String method = "queryPakage";  //接口名-套餐使用量查询

        String[] arr = {access_number,user_id,password,method}; //加密数组，数组所需参数根据对应的接口文档

        //key值指密钥，由电信提供，每个客户都有对应的key值，key值平均分为三段如下：
        String key1 = "abc";
        String key2 = "def";
        String key3 = "ghi";

        DesUtils des = new DesUtils(); //加密工具类实例化
        String passwordEnc = des.strEnc(password,key1,key2,key3);  //密码加密
        System.out.println("密码加密结果:"+passwordEnc);
        //密码加密结果：441894168BD86A2CC

        String sign = des.strEnc(DesUtils.naturalOrdering(arr),key1,key2,key3); //生成sign加密值
        System.out.println("sign加密结果:"+sign);
        //sign加密结果：45E8B9924DE397A8F7E5764767810CF774CC7E1685BA702C9C4C367EFDAE5D932B37C0C8F0F8EB0CAD6372289F407CA941894168BD86A2CC32E5804EA05BAA5099649468B9418E52

        String passwordDec = des.strDec(passwordEnc,key1,key2,key3);//密码解密
        System.out.println("密码解密结果:"+passwordDec);
        //密码解密结果 :test

        String signDec = des.strDec(sign,key1,key2,key3); //sign解密
        System.out.println("sign解密结果:"+signDec);
        //sign解密结果：14914000000,queryPakage,test,test
    }
}
