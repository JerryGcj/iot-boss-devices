package com.wangxin.iot.web;

import com.wangxin.iot.WebApplication;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.task.TelecomCardCostMonitorTask;
import com.wangxin.iot.task.TelecomCommonTask;
import com.wangxin.iot.task.xxl.TelecomXxlJob;
import com.wangxin.iot.telecom.api.TelecomGatewayApi;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by 18765 on 2020/1/2 14:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class TelecomTest {
    @Autowired
    TelecomGatewayApi telecomGatewayApi;
    @Autowired
    ITelecomGatewayService telecomGatewayService;
    @Autowired
    TelecomCardCostMonitorTask telecomCardCostMonitorTask;
    @Autowired
    TelecomCommonTask telecomCommonTask;
    @Autowired
    TelecomXxlJob telecomXxlJob;
    @Test
    public void xxlTest(){
        telecomXxlJob.recoverDailyLimit(null);
    }
    @Test
    public void test1() throws Exception {
        Map<String,String> map = new HashMap<>();
//        map.put("iccid","8986031641200752175");
//        map.put("method","queryTraffic");
//        map.put("needDtl","1");
//        map.put("iccid","8986031641200752175");

        map.put("access_number","1410367063706");
        map.put("method","queryTrafficByDate");
        map.put("startDate","20211017");
        map.put("endDate","20211021");
        map.put("needDtl","1");
        //telecomGatewayService.acquireAccessNumber("8986112121503935231");
//        System.out.println(s);

//        map.put("orderTypeId","20");
//        map.put("acctCd","");
        String result = telecomGatewayApi.launchReq(map);
        System.out.println(result);
//        Map<String, String> notifyMap = WXPayUtil.xmlToMap(result);
//        notifyMap.forEach((k,v)->{
//            System.out.println(k+"\t"+v);
//        });
//        Map<String,String> map = new HashMap<>();
//        map.put("method","getTelephone");
//        map.put("iccid","8986112022503440211");
//        telecomGatewayService.acquireAccessNumber("8986112022503440211");
//        Map<String,String> reqMap = new HashMap<>();
//        reqMap.put("method","queryTraffic");
//        reqMap.put("access_number","1410317812896");
//        reqMap.put("needDtl","1");
        //this.telecomGatewayApi.launchReq(reqMap);
    }
    @Test
    public void syncCardUsage(){
        telecomCommonTask.syncUsage();
    }

    //监控套餐
    @Test
    public void cardAutoStopMonitor(){
        telecomCardCostMonitorTask.cardAutoStopMonitor();
    }

    //自动复机
    @Test
    public void cardAutoStartMonitor(){
        telecomCardCostMonitorTask.cardAutoStartMonitor();
    }
}
