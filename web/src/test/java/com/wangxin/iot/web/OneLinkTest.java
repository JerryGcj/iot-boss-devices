package com.wangxin.iot.web;

import com.wangxin.iot.WebApplication;
import com.wangxin.iot.card.ICardInformationService;
import com.wangxin.iot.card.ICardUsageService;
import com.wangxin.iot.card.IotRefCardCostService;
import com.wangxin.iot.domain.IotCardSeparateEntity;
import com.wangxin.iot.mobile.OneLinkServiceImpl;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.task.OneLinkTask;
import com.wangxin.iot.utils.StringUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class OneLinkTest {

    @Autowired
    ICardUsageService cardUsageService;

    @Autowired
    IotRefCardCostService iotRefCardCostService;
    @Autowired
    IotTemplateFactory iotTemplateFactory;
    @Autowired
    Executor executor;
    @Autowired
    ICardInformationService cardInformationService;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    RedisUtil redisUtil;

    IotOperatorTemplate templateByType;

    OneLinkServiceImpl oneLinkServiceImpl;
    @Autowired
    OneLinkTask oneLinkTask;
    @Test
    public void test(){
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        //线程安全
        CopyOnWriteArrayList<IotCardSeparateEntity> cardSeparateEntities = new CopyOnWriteArrayList<>();
        for (int i = 0; i < 100; i++) {
            threadPoolExecutor.execute(() -> {
                try {
                    System.out.println(Thread.currentThread().getName());
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
        }
        threadPoolExecutor.getThreadPoolExecutor().shutdown();
        while (true){
            boolean terminated = threadPoolExecutor.getThreadPoolExecutor().isTerminated();
            System.out.println("22222");
            System.out.println(terminated);
            //任务已执行完
            if(terminated){
                System.out.println("1111");
                break;
            }
        }
    }

    @Test
    public void realNameTask(){
        oneLinkTask.syncRealNameStatus();
    }
    @Test
    public void getSimStopReason(){
        oneLinkServiceImpl.getSimStopReason("898604720521D0223898",templateByType);
    }
    @Before
    public void before(){
         templateByType = CacheComponent.getInstance().getTemplateByType("oneLinkServiceImpl1");
         oneLinkServiceImpl = (OneLinkServiceImpl)applicationContext.getBean("oneLinkServiceImpl");
    }
    @Test
    public void getCardBindStatusByMsisdn(){
        oneLinkServiceImpl.getCardBindStatusByMsisdn("1440679871439",templateByType);
    }
    @Test
    public void simRegionLimitArea(){
        List<String> objects = Arrays.asList();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        List<String> result = new ArrayList<>();
        objects.forEach(item->{
            threadPoolExecutor.execute(() -> {
                Map map = oneLinkServiceImpl.riskSceneCardList(templateByType, item);
                String status = (String)map.get("status");
                if("0".equals(status)){
                    result.add(item);
                }
            });
        });
        threadPoolExecutor.getThreadPoolExecutor().shutdown();
        while (true){
            if(threadPoolExecutor.getThreadPoolExecutor().isTerminated()){
                result.forEach(System.out::println);
                break;
            }
        }
    }
    @Test
    public void getToken(){
        System.out.println(oneLinkServiceImpl.getToken(templateByType));
    }
    @Test
    public void cardDeatils(){
        List<String> objects = Arrays.asList();
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        List<String> result2 = new ArrayList<>();
        List<String> result4 = new ArrayList<>();
        objects.forEach(item->{
            threadPoolExecutor.execute(() -> {
                Map map = oneLinkServiceImpl.getCardStatusByMsisdn(item,templateByType);
            });
        });
        threadPoolExecutor.getThreadPoolExecutor().shutdown();
        while (true){
            if(threadPoolExecutor.getThreadPoolExecutor().isTerminated()){
                result2.forEach(System.out::println);
                System.out.println("result4:");
                result4.forEach(System.out::println);
                break;
            }
        }
    }
    @Test
    public void activeCard(){
        Map map = new HashMap();
        map.put("iccid","898604670521D0000492");
        map.put("status","3");
        oneLinkServiceImpl.modifyCard(map,templateByType);
    }
    @Test
    public void getUsaged(){
        System.out.println(oneLinkServiceImpl.getUsaged("898604670521D0000303",templateByType));
    }

    @Test
    public void realNameReg(){
        System.out.println(oneLinkServiceImpl.realNameReg("898604B30522D0395436",templateByType));
    }
    @Test
    public void changeHistory(){
        System.out.println(oneLinkServiceImpl.getChangeHistory("898604821221D0121000",templateByType));
    }

    @Test
    public void  getCardStatus(){
        String cardStatus = oneLinkServiceImpl.getCardStatus("89860472052070268243", templateByType);
    }

    @Test
    public void getDailyUsage(){
        Map map = new HashMap();
        map.put("apiName","/ec/query/sim-data-usage-daily/batch");
        map.put("iccids","89860467051970221025");
        map.put("queryDate","20200529");
        Map call = oneLinkServiceImpl.call(map, this.templateByType);
    }
    @Test
    public void getSimDataMargin(){
        Map map = new HashMap();
        map.put("apiName","/ec/query/sim-data-margin");
        map.put("iccid","89860421151981790002");
        Map call = oneLinkServiceImpl.call(map, this.templateByType);
    }
    @Test
    public void groupQuery(){
//        Map map = new HashMap();
//        map.put("apiName","/ec/query/group-info");
//        map.put("pageSize","50");
//        map.put("startNum","1");
//        Map call = oneLinkServiceImpl.call(map, this.templateByType);
        oneLinkServiceImpl.getFlowPoolUsage("3511000010624085",templateByType);
    }

    /**
     * 批量更新卡状态
     */
    @Test
    public void batchUpdateCardStatus(){
        Stream<String> stringStream = Stream.of( );
        stringStream.forEach(item-> executor.execute(()->{
            Map map = new HashMap();
            map.put("status","3");
            map.put("iccid",item);
            boolean flag = oneLinkServiceImpl.modifyCard(map, templateByType);
            if(flag){
                System.out.println("修改卡状态成功，"+item);
            }
        }));
    }


    @Test
    public void simChangeHistory(){
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        List<String> strings = Arrays.asList("");
        Set<String> finalResult = Collections.synchronizedSet(new HashSet<>());
        strings.forEach(item->
                executor.execute(()->{
                    Map map = new HashMap();
                    map.put("apiName","/ec/query/sim-change-history");
                    map.put("iccid",item);
                    Map call = oneLinkServiceImpl.call(map, this.templateByType);
                    if(call.get("status").equals("0")){
                        List result = (List) call.get("result");
                        Map changeHistoryList = (Map)result.get(0);
                        List<Map> changeHistory = (List)changeHistoryList.get("changeHistoryList");
                        c: for (Map change : changeHistory) {
                            String changeDate = (String) change.get("changeDate");
                            String descStatus = (String) change.get("descStatus");
                            String targetStatus = (String) change.get("targetStatus");
                            if(StringUtils.isNotEmpty(changeDate) ){
                                String substring = changeDate.substring(0, 8);
                                if(substring.contains("2020-12")){
                                    if("4".equals(descStatus) && "4".equals(targetStatus)){
                                        continue c;
                                    }
                                    if("4".equals(descStatus) || "2".equals(targetStatus) || "2".equals(descStatus) || "4".equals(targetStatus)){
                                        finalResult.add(item);
                                        break c;
                                    }
                                }

                            }
                        }
                    }
                    log.info("批量查询变更历史：活跃线程数：{}，  核心线程数：{},完成任务数：{}缓冲区大小,{}",
                            threadPoolExecutor.getActiveCount(),threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount(),threadPoolExecutor.getThreadPoolExecutor().getQueue().size());

                }));
        threadPoolExecutor.getThreadPoolExecutor().shutdown();
        while (true){
            if(threadPoolExecutor.getThreadPoolExecutor().isTerminated()){
                System.out.println("正常收费的卡 ,"+finalResult.size());
                finalResult.forEach(System.out::println);
//                System.out.println("停机保号费用的卡");
//                stop.forEach(System.out::println);
                break;
            }
        }
    }
    @Test
    public void batchQueryLocation(){
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        //新疆地区的区号
        List<String> strings = Arrays.asList(
//                "0692",
//                "0691",
//
//                "0771",
//                "0772",
//                "0773",
//                "0774",
//                "0779",
//                "0777",
//                "0775",
//                "0776",
//                "0774",
//                "0778",
//                //西藏
//                "0871",
//                "0874",
//                "0877",
//                "0875",
//                "0870",rest
//                "0888",
//                "0879",
//                "0883",
//                "0878",
//                "0873",
//                "0876",
//                "0872",
//                "0886",
//                "0887",
//                "0891",
//                "0895",
//                "0893",
//                "0892",
//                "0896",
//                "0897",
//                "0894",
//                "0595",
//                "0596",
//                "0597",
//                "0712",
//                "0728",
//                "0775",
//                "0771",
//                "0898",
//                "0412",
//                "0396",
//                "0738",
//                "0838",
//                "0668",
//                "0768",
//                "0793",
//                "0314",
                //以下是新疆
                "0991",
                "0990",
                "0995",
                "0902",
                "0994",
                "0909",
                "0996",
                "0997",
                "0908",
                "0998",
                "0903",
                "0999",
                "0901",
                "0906",
                "0993");
        Stream<String> stringStream = Stream.of("1440679901996",
                "1440679901997",
                "1440679901992",
                "1440679901998",
                "1440679901993",
                "1440679901992",
                "1440721768112");
        Map finalResult = new HashMap();
        stringStream.forEach(item->
            executor.execute(()->{
                Map map = new HashMap();
                map.put("apiName","/ec/query/district-position-location-message");
                map.put("msisdn",item);
                Map call = oneLinkServiceImpl.call(map, this.templateByType);
                if(call.get("status").equals("0")){
                    List result = (List) call.get("result");
                    Map<String, String> map2 = (Map) result.get(0);
                    String cityCode = map2.get("cityCode").trim();
                    System.out.println("卡号："+item+"城市："+cityCode);
//                    if(strings.contains(cityCode)){
//                        finalResult.put(item,cityCode);
//                    }
                }
                log.info("批量查询高危地区：活跃线程数：{}，  核心线程数：{},完成任务数：{}缓冲区大小,{}",
                        threadPoolExecutor.getActiveCount(),threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount(),threadPoolExecutor.getThreadPoolExecutor().getQueue().size());

            }));
        threadPoolExecutor.getThreadPoolExecutor().shutdown();
//        while (true){
//            if(threadPoolExecutor.getThreadPoolExecutor().isTerminated()){
//                System.out.println("fuck");
//                finalResult.forEach((k,v)-> System.out.println("卡号："+k+"\t地区："+v));
//                break;
//            }
//        }


    }


    /**
     * 查询物联网卡在指定月份使用量。
     */
    @Test
    public void monthUsage(){
       List<String> stringStream = Stream.of("").collect(Collectors.toList());
       BigDecimal bigDecimal = BigDecimal.ZERO;
       while (true){
            List<String> collect = stringStream.stream().limit(100).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                String join = String.join("_", collect);
                Map map = new HashMap();
                map.put("apiName","/ec/query/sim-data-usage-monthly/batch");
                //要查询的月份
                map.put("queryDate","202203");
                map.put("iccids",join);
                Map call = oneLinkServiceImpl.call(map, this.templateByType);
                if(call.get("status").equals("0")){
                    List<Map> result = (List<Map>) call.get("result");
                    List<Map> dataAmountList = (List<Map>) result.get(0).get("dataAmountList");
                    for (int i = 0; i < dataAmountList.size() ; i++) {
                        String dataAmount = (String)dataAmountList.get(i).get("dataAmount");
                        if(StringUtils.isNotEmpty(dataAmount)){
                           try {
                               //出了异常就捕获一下不处理，不影响大局
                               BigDecimal bigDecimal1 = new BigDecimal(dataAmount).divide(new BigDecimal(1024));
                               bigDecimal = bigDecimal.add(bigDecimal1);
                           }catch (Exception e){

                           }
                        }
                    }
                }
                //处理结果
                stringStream.removeAll(collect);
            }else{
                break;
            }
        }
        //所有激活卡月份总用量
       System.out.println(bigDecimal);

    }


}
