package com.wangxin.iot.web;

import com.alibaba.fastjson.JSON;
import com.wangxin.iot.WebApplication;
import com.wangxin.iot.card.ICustomerSalesDiscountService;
import com.wangxin.iot.card.IOrderPackageService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.event.WxPayCallbackEvent;
import com.wangxin.iot.mail.IMailService;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.CardMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.utils.redis.RedisUtil;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class RedisTest {
    @Autowired
    CardInformationMapper cardInformationMapper;

    /**
     * 注入发送邮件的接口
     */
    @Autowired
    private IMailService mailService;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    CardMapper cardMapper;
    @Autowired
    ICustomerSalesDiscountService customerSalesDiscountService;
    @Autowired
    IOrderPackageService orderPackageService;
    @Autowired
    private RedisUtil redisUtil;

    @Test
    public void callWebServiceMany(){
        int count =0;
        int temp = 0;
        while (true){
            count= temp*50;

            System.out.println(count+"\t"+ LocalDateTime.now().toLocalTime());

            try {
                temp++;
                TimeUnit.MINUTES.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    @Test
    public void callWebServiceOne(){


    }
    @Test
    public void redisUtil(){
//        List<Object> y = redisUtil.lBatchPop("y", 0, 3);
//        y.forEach(item-> System.out.println(item));
//        List iccids = cardMapper.temp(0,50);
//        redisUtil.lSetList(RedisKeyConstants.TASK_ACTIVE_ERROR.getMessage(), iccids);
        System.out.println(redisUtil.get("yanwin"));
        System.out.println(redisUtil.hasKey("yanwin"));
    }
    @Test
    public void batchPop() throws Exception{
        List list = new ArrayList<>();

        for (int i = 0; i <50 ; i++) {
            list.add(i+"");
        }
        redisUtil.lSetList(RedisKeyConstants.TASK_ACTIVE.getMessage(), list);
        TimeUnit.SECONDS.sleep(30);
        List ids = redisUtil.lBatchPop("task_active", 0, 10);
        while (CollectionUtils.isNotEmpty(ids)){
            try {
                ids.forEach(System.out::println);
                TimeUnit.SECONDS.sleep(1);
                ids = redisUtil.lBatchPop("task_active", 0, 10);
                System.out.println("弹栈："+ids.size());
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    @Test
    public void redisHget(){
        Object hget = redisUtil.hget("iot_device_cache", "89860619130010006178");
        System.out.println(hget);
    }
    @Test
    public void eventPublish() {
        Order order = orderMapper.selectById("ca28fb16a9c606011dca1da38fd2e587");
        applicationContext.publishEvent(new WxPayCallbackEvent(order));
    }
    @Test
    public void adjustUserBalance(){
        Order order = orderMapper.selectById("1b6b76be89a09926d5788c3ef1069650");
        customerSalesDiscountService.updateUserBalance(order);
    }
    @Test
    public void shareProfit(){
        Order order = orderMapper.selectById("19f5b0416e4e66b59b1a90218bb817af");
        //orderPackageService.telecomOrderPackage(order);
        redisUtil.hset(RedisKeyConstants.UN_REAL_NAME_ORDER.getMessage(),order.getIccid()+"_"+order.getId(), JSON.toJSONString(order));
    }
    @Test
    public void getUserScrete(){
        Map<String, String> tcymtx = customerSalesDiscountService.getUserScreat("tcymtx");
        tcymtx.forEach((k,v)-> System.out.println("k:" +k+"v:" +v));
    }

    @Test
    public void getWechatPayKey() {
        System.out.println(CacheComponent.getInstance().getKeyByMchId("1587872721"));
    }


    @Test
    public void sendmailHtml(){
        mailService.sendHtmlMail("18765907950@163.com,yanweiqd@gmail.com","主题：你好html邮件","<h1>内容：第一封html邮件</h1>");
    }



    @Test
    public void test(){
//        oneLinkTask.syncRefUsage();
//        cardInformationMapper.getAll();
    }

    @Test
    public void getChildUsernameByCurrentUser(){
        List list = redisUtil.setBatchPop(RedisKeyConstants.NEED_SYNC_CARD.getMessage(), 50);
        System.out.println("几个数据：：："+list.size());
    }
}
