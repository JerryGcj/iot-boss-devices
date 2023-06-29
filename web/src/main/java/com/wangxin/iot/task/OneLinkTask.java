package com.wangxin.iot.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ICardUsageService;
import com.wangxin.iot.card.IIotCardSeparateService;
import com.wangxin.iot.constants.CardCostActiveEnum;
import com.wangxin.iot.constants.CardStatusEnum;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.OrderUpstreamMapper;
import com.wangxin.iot.mobile.OneLinkServiceImpl;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.*;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.rest.base.Result;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by 18765 on 2020/1/8 10:11
 */
@Component
@Slf4j
public class OneLinkTask {
    /**
     * 1代表1kb
     */
    private static final BigDecimal _1000GB = new BigDecimal(1*1024*1024*1000);
    @Autowired
    IIotCardSeparateService iotCardSeparateService;
    @Autowired
    IotRefCardCostMapper iotRefCardCostMapper;

    @Autowired
    Executor executor;

    @Autowired
    ICardUsageService cardUsageService;

    @Autowired
    IotTemplateFactory iotTemplateFactory;

    @Autowired
    CardInformationMapper cardInformationMapper;

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    OrderUpstreamMapper orderUpstreamMapper;

    @Autowired
    OrderMapper orderMapper;
    @Autowired
    CardCostMonitorTask cardCostMonitorTask;

    @Autowired
    RedisUtil redisUtil;
    /**
     * 每天凌晨零点执行一次，查询我们平台不是正常，移动平台正常的卡，给停用掉
     */
    @Scheduled(cron = "0 0 0 * * ?")
    public void syncCardStatus(){
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        //我们平台所有不正常的卡
        List<String> iccidsExcludeState = cardInformationMapper.getIccidsExcludeState(3);
        iccidsExcludeState.forEach(item-> threadPoolExecutor.execute(()-> {
            ThirdService thirdService = iotTemplateFactory.getExecutorThridService(item);
            IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(item);
            if(thirdService instanceof OneLinkServiceImpl){
                OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
                String cardStatus = oneLinkService.getCardStatus(item, operatorTemplate);
                //移动是正常的，停机处理
                if("3".equals(cardStatus)){
                    Map map = new HashMap(2);
                    map.put("iccid",item);
                    map.put("status","4");
                    boolean success = oneLinkService.modifyCard(map, operatorTemplate);
                    if(success){
                        log.info("iccid:{}与移动平台状态不一致，停机成功",item);
                        UpdateWrapper<CardInformation> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("iccid",item);
                        CardInformation cardInformation = new CardInformation();
                        cardInformation.setCardState("4");
                        this.cardInformationMapper.update(cardInformation,updateWrapper);
                    }
                }
            }

        }));
    }



    /**
     * 将日用量的数据同步到ref中，ref是停复机的参照值
     */
    @Scheduled(initialDelay = 1000*60*25,fixedDelay = 1000*60*60)
    public void syncRefUsage(){
        log.info("同步日流量表到ref开始");
        List<RefCardModel> needSyncCard = iotRefCardCostMapper.getNeedSync("1");
        if(!CollectionUtils.isEmpty(needSyncCard)){
            needSyncCard.forEach(item->cardUsageService.syncRefUsage(item));
        }
    }
    /**
     * 多线程同步卡用量
     */
    @Scheduled(initialDelay = 1000*60*15,fixedDelay = 1000*60*60)
    public void syncUsage(){
        List<String> iccidByOperationId = cardInformationMapper.getIccidsByActiveCost("1");
        log.info("同步onelink卡用量开始,已激活卡：{}",iccidByOperationId.size());
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        iccidByOperationId.forEach(item-> threadPoolExecutor.execute(() -> {
            OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)iotTemplateFactory.getExecutorThridService(item);
            IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(item);
            BigDecimal usage = oneLinkService.getUsaged(item, operatorTemplate);
            if(usage != null && usage.compareTo(BigDecimal.ZERO)>0){
                cardUsageService.updateUsage(item, LocalDate.now(),usage);
            }
            log.info("同步onelink卡用量：  核心线程数：{},完成任务数：{}缓冲区大小,{}",
                    threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount(),threadPoolExecutor.getThreadPoolExecutor().getQueue().size());
        }));

    }
    /**
     * 10分钟执行一次，调用移动(source=0)失败的重新处理。
     */
    @Scheduled(initialDelay = 1000*60*30,fixedDelay = 1000*60*15)
    public void retry(){
        int retryCount = 15;
        log.info("查询调用移动接口失败的卡，重新操作");
        List<OrderUpstream> record = orderUpstreamMapper.getRecord(0,retryCount);
        if(!CollectionUtils.isEmpty(record)){
            record.forEach(item->{
                //这种状态的没必要处理，处理也是失败的
                if(!item.getErrorMsg().contains("12026")){
                    Map map = new HashMap(3);
                    map.put("action","3");
                    map.put("id",item.getId());
                    map.put("iccid",item.getIccid());
                    map.put("flag","task");
                    map.put("status",item.getMirror().split(";")[0]);
                    ThirdService thirdService = iotTemplateFactory.getExecutorThridService(item.getIccid());
                    IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(item.getIccid());
                    Integer status=0;
                    if(thirdService instanceof OneLinkServiceImpl){
                        OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
                        if(oneLinkService.modifyCard(map, operatorTemplate)){
                            //修改成功后，修改db中的状态
                            String mirror = item.getMirror().split(";")[0];
                            UpdateWrapper<CardInformation> updateWrapper = new UpdateWrapper<>();
                            updateWrapper.eq("iccid",item.getIccid());
                            CardInformation cardInformation = new CardInformation();
                            if("1;2".equals(mirror)){
                                cardInformation.setCardState("3");
                            }else{
                                cardInformation.setCardState(mirror);
                            }
                            this.cardInformationMapper.update(cardInformation, updateWrapper);
                            status = 1;
                        }else{
                            //第四次失败后，状态改为失败丢弃
                            if(item.getRetryCount() == retryCount-1){
                                status = 2;
                            }
                        }
                    }
                    //调用次数累加
                    orderUpstreamMapper.incrRetryCount(item.getId(),status,item.getRetryCount());
                }
            });
        }
    }


    /**
     * 10min 执行一次
     * 定时处理订单状态是处理中的订单，查询移动平台是否实名，已实名的自动配置套餐
     */
    @Scheduled(initialDelay = 1000*60*10,fixedDelay = 1000*60*10)
    public void syncRealNameStatus(){
        QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("pay_state", 4)
                .eq("order_state", 2)
                .eq("operator_type", 1)
                .gt("create_time","2022-10-01")
                .orderByDesc("create_time");
        List<Order> orders = orderMapper.selectList(queryWrapper);
        if(CollectionUtils.isNotEmpty(orders)){
            orders.forEach(item->{
                log.info("处理中的订单iccid：{}，开始执行：PayOrderId：{}", item.getIccid(), item.getPayOrderId());
                IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(item.getIccid());
                //根据iccid,查找对应的通道
                ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
                Result result = thirdService.realNameStatus(item.getIccid(),item.getId(),templateByOperation);
                log.info("处理中的订单iccid：{}，PayOrderId：{}，处理结果：{}", item.getIccid(), item.getPayOrderId(), result.getMsg());
                Order order = new Order();
                order.setId(item.getId());
                if(result.getCode()==0){
                    //移动已实名，订单改成功
                    order.setOrderState(3);
                    orderMapper.updateById(order);
                }
                if(result.getCode()==2){
                    //移动已销户，订单改失败
                    order.setOrderState(4);
                    order.setNote(result.getMsg());
                    orderMapper.updateById(order);
                }
            });
        }
    }

    /**
     * 卡自动复机
     */
    @Scheduled(initialDelay = 1000*60*10,fixedDelay = 1000*60*60)
    public void autoRecovery(){
        List<Map<String,String>> unActiveCard = iotRefCardCostMapper.getUnActiveCard(0);
        if(CollectionUtils.isNotEmpty(unActiveCard)){
            for (Map<String,String> item:unActiveCard) {
                log.info("符合自动复机的卡："+item);
                String iccid = item.get("card_iccid");
                String cardId = item.get("cardId");
                String refId = item.get("refId");
                String simStatus = String.valueOf(item.get("card_state"));
                //卡状态不是激活，那么调用接口激活
                if(!CardStatusEnum.ACTIVATED.getCode().equals(simStatus)){
                    log.info("卡号：{}，已停机，有待生效的套餐，自动生效",iccid);
                    cardCostMonitorTask.updateStatus(iccid,cardId,refId,null, "3",CardCostActiveEnum.ACTIVED);
                }else{
                    //是否有生效中的套餐，改为已失效
                    Integer activeCount = iotRefCardCostMapper.getInAdvancePackageCount(iccid, CardCostActiveEnum.ACTIVED.getActive(),0);
                    if(activeCount > 0){
                        log.info("卡号：{},状态是：{}，有生效中的套餐，设置为到期失效",iccid,simStatus);
                        IotRefCardCost updateRefCardCost = new IotRefCardCost();
                        updateRefCardCost.setActive(CardCostActiveEnum.OVERTIME_DISABLED.getActive());
                        updateRefCardCost.setUpdateTime(new Date());
                        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("card_iccid",iccid);
                        queryWrapper.eq("active",CardCostActiveEnum.ACTIVED.getActive());
                        iotRefCardCostMapper.update(updateRefCardCost,queryWrapper);
                    }
                    log.info("卡：{}未生效的套餐id：{}，改为生效中",iccid,refId);
                    IotRefCardCost updateRefCardCost = new IotRefCardCost();
                    updateRefCardCost.setId(refId);
                    updateRefCardCost.setActive(CardCostActiveEnum.ACTIVED.getActive());
                    iotRefCardCostMapper.updateById(updateRefCardCost);
                }
            }
        }
    }

    /**
     * 每天早上6.30，查询一下我们平台正常，移动平台停机的卡--->复机，异常停机卡记录
     */
    @Scheduled(cron = "0 30 6 * * ?")
    public void syncStopReasonAndRestartCard(){
        iotCardSeparateService.syncStopReasonAndRestartCard(null);
    }

    /**
     * 每天凌晨1点，将昨天用量的内存标志位清除，防止垃圾数据填满redis
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void removeUpdateUsageContainer(){
        log.info("移动---昨天用量的内存标志位清除");
        Date yesterday = DateUtils.addDays2Date(DateUtils.getCurrentDate(), -1);
        String stringYesterday = DateUtils.formatDateToString(yesterday);
        Map<Object, Object> keys = redisUtil.hmget(RedisKeyConstants.MOBILE_UPDATE_USAGE_CONTAINER.getMessage());
        if(CollectionUtils.isNotEmpty(keys)){
            keys.forEach((k,v)->{
                if(k.toString().contains(stringYesterday)){
                    redisUtil.hdel(RedisKeyConstants.MOBILE_UPDATE_USAGE_CONTAINER.getMessage(),k);
                }
            });
        }
    }

    /**
     * 多线程同步卡信息（激活时间，msisdn）
     */
    @Scheduled(initialDelay = 1000*60*15,fixedDelay = 1000*60*60)
    public void syncActiveDate(){
        List<String> iccidByOperationId = cardInformationMapper.getIccidsByActiveDate();
        log.info("同步onelink卡信息开始,已激活卡：{}",iccidByOperationId.size());
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        iccidByOperationId.forEach(item-> {
            threadPoolExecutor.execute(() -> {
                QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("card_iccid",item).orderByAsc("create_time");
                List<IotRefCardCost> list = iotRefCardCostMapper.selectList(queryWrapper);
                if(CollectionUtils.isNotEmpty(list)){
                    CardInformation cardInformation = new CardInformation();
                    cardInformation.setActivationTime(list.get(0).getCreateTime());
                    UpdateWrapper<CardInformation> wrapper = new UpdateWrapper<>();
                    wrapper.eq("iccid", item);
                    cardInformationMapper.update(cardInformation, wrapper);
                }
                /*ThirdService thirdService = iotTemplateFactory.getExecutorThridService(item);
                IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(item);
                if(thirdService instanceof OneLinkServiceImpl){
                    OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
                    Map result = oneLinkService.sendReq(item, operatorTemplate);
                    if(result != null&& StringUtils.isEmpty(result.get("error").toString())){
                        String status = result.get("status").toString();
                        String msisdn = result.get("msisdn").toString();
                        Date activationTime = (Date)result.get("activationTime");
                        CardInformation cardInformation = new CardInformation();
                        cardInformation.setMsisdn(msisdn);
                        cardInformation.setCardState(status);
                        cardInformation.setActivationTime(activationTime);
                        UpdateWrapper<CardInformation> wrapper = new UpdateWrapper<>();
                        wrapper.eq("iccid", item);
                        cardInformationMapper.update(cardInformation, wrapper);
                    }
                }*/
                log.info("同步onelink卡信息：  核心线程数：{},完成任务数：{}缓冲区大小,{}",
                        threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount(),threadPoolExecutor.getThreadPoolExecutor().getQueue().size());
            });
        });
    }

    /**
     * 每天凌晨三点执行一次，查询我们平台有生效中的套餐的卡，移动平台不正常的卡，去激活
     */
//    @Scheduled(cron = "0 0 3 * * ?")
//    public void activeCardStatus(){
//        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
//        //我们平台所有有生效中的套餐的卡
//        List<String> iccidsExcludeState = cardInformationMapper.getIccidsByActiveCost("1");
//        iccidsExcludeState.forEach(item-> threadPoolExecutor.execute(()-> {
//            ThirdService thirdService = iotTemplateFactory.getExecutorThridService(item);
//            IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(item);
//            if(thirdService instanceof OneLinkServiceImpl){
//                OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
//                String cardStatus = oneLinkService.getCardStatus(item, operatorTemplate);
//                //移动是停机的，激活处理
//                if("4".equals(cardStatus)){
//                    Map map = new HashMap(2);
//                    map.put("iccid",item);
//                    map.put("status","3");
//                    boolean success = oneLinkService.modifyCard(map, operatorTemplate);
//                    if(success){
//                        log.info("iccid：{}，我方正常，移动停机，激活成功",item);
//                    }
//                }
//            }
//
//        }));
//    }
}
