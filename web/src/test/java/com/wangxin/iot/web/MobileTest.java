package com.wangxin.iot.web;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wangxin.iot.WebApplication;
import com.wangxin.iot.card.IIotRefundRecordService;
import com.wangxin.iot.card.IOrderPackageService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.mobile.OneLinkServiceImpl;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.IotRefundRecord;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.task.CardCostMonitorTask;
import com.wangxin.iot.task.RealNameStatusTask;
import com.wangxin.iot.utils.redis.RedisUtil;
import net.bytebuddy.asm.Advice;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

/**
 * Created by 18765 on 2020/1/2 14:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class MobileTest {
    @Autowired
    CardInformationMapper cardInformationMapper;
    @Autowired
    IOrderPackageService orderPackageService;
    @Autowired
    IotRefCardCostMapper refCardCostMapper;
    @Autowired
    RealNameStatusTask realNameStatusTask;
    @Autowired
    CardCostMonitorTask cardCostMonitorTask;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    Executor executor;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    IIotRefundRecordService refundRecordService;
    IotOperatorTemplate templateByType;

    OneLinkServiceImpl oneLinkServiceImpl;
    /*@Before
    public void before(){
        templateByType = CacheComponent.getInstance().getTemplateByType("oneLinkServiceImpl1");
        oneLinkServiceImpl = (OneLinkServiceImpl)applicationContext.getBean("oneLinkServiceImpl");
    }*/


    /**
     * 批量更新卡状态
     */
    @Test
    public void batchUpdateCardStatus(){
        Stream<String> stringStream = Stream.of(
        );

        stringStream.forEach(item-> executor.execute(()->{
            Map map = new HashMap();
            map.put("status","4");
            map.put("iccid",item);
            boolean flag = oneLinkServiceImpl.modifyCard(map, templateByType);
            if(flag){
                System.out.println("修改卡状态成功，"+item);
                UpdateWrapper<CardInformation> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("iccid",item);
                CardInformation cardInformation = new CardInformation();
                cardInformation.setCardState("4");
                cardInformationMapper.update(cardInformation, updateWrapper);
            }
        }));
    }

    @Test
    public void activeCard(){
        Map map = new HashMap();
        map.put("iccid","898604B30522C0106153");
        map.put("status","4");
        oneLinkServiceImpl.modifyCard(map,templateByType);
    }
    @Test
    public void test(){
        Order order = orderPackageService.getById("a954614b92e126a7c8bff64d30f21b46");
        if(order.getOperatorType()==1){
            Integer realNameInfo = refCardCostMapper.getRealNameInfo(order.getIccid(), "1");
            if(realNameInfo != 0){
                //orderPackageService.wechatOrderPackage(order);
            }else{
                //把未实名的订单存redis,待实名认证后，订购套餐。
                redisUtil.hset(RedisKeyConstants.UN_REAL_NAME_ORDER.getMessage(),order.getIccid()+"_"+order.getId(), JSON.toJSONString(order));
            }
        }
    }

    @Test
    public void realNameStatus(){
        //realNameStatusTask.mobileRealNameQuery();
        cardCostMonitorTask.cardAutoStopMonitor();
//        IotRefundRecord refundRecord = refundRecordService.getById("1576857316720701441");
//        refundRecordService.automation(refundRecord);
    }
}
