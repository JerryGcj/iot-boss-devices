package com.wangxin.iot.task.xxl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.sun.mail.util.QEncoderStream;
import com.wangxin.iot.card.IStandardCostService;
import com.wangxin.iot.card.ITelecomCardUsageService;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.card.IotTelecomRefCardCostService;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.IotTelecomCardUsage;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.mapper.IotTelecomRefCardCostMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.IotRefCardCost;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.model.StandardCost;
import com.wangxin.iot.telecom.api.TelecomGatewayApi;
import com.wangxin.iot.utils.redis.RedisUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author: yanwin
 * @Date: 2021/1/20
 */
@Component
@Slf4j
public class TelecomXxlJob {
    @Autowired
    IotTelecomCardInfoMapper iotTelecomCardInfoMapper;
    @Autowired
    ITelecomGatewayService iTelecomGatewayService;
    @Autowired
    TelecomGatewayApi telecomGatewayApi;
    @Autowired
    IotTelecomRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    IStandardCostService standardCostService;
    @Autowired
    IotTelecomRefCardCostService refCardCostService;
    @Autowired
    ITelecomCardUsageService telecomCardUsageService;
    @Autowired
    RedisUtil redisUtil;
    /**
     * 查询卡实名状态
     * @param params
     * @return
     */
    @XxlJob("realNameStatus")
    public ReturnT realNameStatus(String params){
        XxlJobLogger.log("自动查询卡实名状态任务开始-----");
        QueryWrapper<RealNameSystem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operator_type",3).ne("status","1");
        List<RealNameSystem> realNameSystems = realNameSystemMapper.selectList(queryWrapper);
        if(CollectionUtils.isNotEmpty(realNameSystems)){
            realNameSystems.forEach(item -> {
                XxlJobLogger.log("iccid：{}，接入号：{}，即将开始查询实名状态---",item.getIccid(),item.getMsisdn());
                iTelecomGatewayService.realNameStatus(item.getMsisdn());
            });
        }else{
            XxlJobLogger.log("没有未实名的卡");
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 查询卡主状态，同步我方状态及激活时间
     * @param params
     * @return
     */
    @XxlJob("queryTelecomCardStatus")
    public ReturnT syncTelecomCardStatus(String params){
        QueryWrapper<IotTelecomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("sim_status", 1,2,3);
        List<IotTelecomCardInfo> iotTelecomCardInfos = iotTelecomCardInfoMapper.selectList(queryWrapper);
        XxlJobLogger.log("电信自动同步状态及激活时间任务开始----- 卡实体：{}", iotTelecomCardInfos);
        List<String> accessNumbers = iotTelecomCardInfos.stream().map(IotTelecomCardInfo::getAccessNumber).collect(Collectors.toList());
        if(CollectionUtils.isNotEmpty(accessNumbers)){
            try{
                accessNumbers.forEach(accessNumber->{
                    Map newMap = new HashMap();
                    newMap.put("method","queryCardMainStatus");
                    newMap.put("access_number",accessNumber);
                    iTelecomGatewayService.mainStatus(newMap);
                });
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 我们平台是停机，电信平台是激活的卡，设置成停机状态
     * @param params
     * @return
     */
    @XxlJob("syncTelecomCardStatus")
    public ReturnT syncCardStatus(String params){
        if(StringUtils.isEmpty(params)){
            XxlJobLogger.log("参数为空，不执行");
            return ReturnT.SUCCESS;
        }
//        ourStatus=5,upstreamStatus=4
        String[] split = params.split(",");
        String ourStatus = split[0].split("=")[1];
        String upstreamStatus = split[1].split("=")[1];
        List<String> accessNumbers = iotTelecomCardInfoMapper.getIccidByStatus(Integer.valueOf(ourStatus));
        Set<String> set = new HashSet<>();
        if(CollectionUtils.isNotEmpty(accessNumbers)){
            try {
                accessNumbers.forEach(accessNumber->{
                    Map newMap = new HashMap(2);
                    newMap.put("method","queryCardMainStatus");
                    newMap.put("access_number",accessNumber);
                    String status = iTelecomGatewayService.mainStatus(newMap);
                    if(status.equals(upstreamStatus)){
                        set.add(accessNumber);
                    }
                });
            }catch (Exception e){
                e.printStackTrace();
                return ReturnT.FAIL;
            }
        }
        XxlJobLogger.log("prefect ending");
        set.forEach(item->{
            XxlJobLogger.log("电信状态不一致的item："+item);
            Map businessMap = new HashMap(3);
            //发生位置是xxl-job定时任务
            businessMap.put("action","4");
            businessMap.put("method","disabledNumber");
            businessMap.put("acctCd","");
            businessMap.put("access_number",item);
            businessMap.put("orderTypeId",19);
            //操作卡
            iTelecomGatewayService.updateCardStatus(businessMap);
        });
        return ReturnT.SUCCESS;
    }

    /**
     * 到达每天用量的卡，申请停机
     * @param params
     * @return
     */
    @XxlJob("dailyLimit")
    public ReturnT dailyLimit(String params){
        //找出配置了的套餐
        List<StandardCost> dailyLimitCost = standardCostService.getDailyLimitCost();
        if(CollectionUtils.isEmpty(dailyLimitCost)){
            XxlJobLogger.log("没有配置每日限额套餐");
            return  ReturnT.SUCCESS;
        }
        Map<String, BigDecimal> costAndUsageRef = dailyLimitCost.stream().collect(Collectors.toMap(StandardCost::getId, StandardCost::getDailyLimit));
        List<String> costIds = dailyLimitCost.stream().map(item -> item.getId()).collect(Collectors.toList());
        QueryWrapper<IotTelecomRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("cost_id",costIds);
        queryWrapper.gt("valid_end",new Date());
        queryWrapper.lt("valid_start",new Date());
        List<IotTelecomRefCardCost> list = this.refCardCostService.list(queryWrapper);
        //卡号和日用量限制的对应关系
        Map<String, BigDecimal> iccidAndUsageRef = list.stream().collect(Collectors.toMap(IotTelecomRefCardCost::getAccessNumber, o -> costAndUsageRef.get(o.getCostId())));
        if(CollectionUtils.isEmpty(list)){
            XxlJobLogger.log("没有符合限额套餐的卡");
            return  ReturnT.SUCCESS;
        }
        List<String> iccids = list.stream().map(IotTelecomRefCardCost::getAccessNumber).collect(Collectors.toList());
        //配置了日用量套餐的卡号，遍历
        iccids.forEach(item->{
            QueryWrapper<IotTelecomCardUsage> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("access_number",item);
            queryWrapper1.eq("date",LocalDate.now());
            IotTelecomCardUsage usage = this.telecomCardUsageService.getOne(queryWrapper1);
            if(usage ==null){
                return;
            }
            //日用量限额
            BigDecimal dailyLimit = iccidAndUsageRef.get(item);
            if(dailyLimit.subtract(new BigDecimal("1024")).compareTo(usage.getCardUsage())<=0){
                //申请停机
                Map businessMap = new HashMap(3);
                //发生位置是xxl-job定时任务
                businessMap.put("action","4");
                businessMap.put("method","disabledNumber");
                businessMap.put("acctCd","");
                businessMap.put("access_number",item);
                businessMap.put("orderTypeId",19);
                //操作卡
                boolean success = iTelecomGatewayService.updateCardStatus(businessMap);
                //存redis,第二天早上重新激活
                if(success){
                    redisUtil.sSet("daily_limit_card", item);
                }
            }
        });
        return ReturnT.SUCCESS;

    }
    @XxlJob("recoverDailyLimit")
    public ReturnT recoverDailyLimit(String params){
        Set<Object> dailyLimitCard = redisUtil.sGet("daily_limit_card");
        if(CollectionUtils.isEmpty(dailyLimitCard)){
            XxlJobLogger.log("没有每日限额的卡需要复机");
        }
        dailyLimitCard.forEach(item->{
            //申请停机
            Map businessMap = new HashMap(4);
            //发生位置是xxl-job定时任务
            businessMap.put("action","4");
            businessMap.put("method","disabledNumber");
            businessMap.put("acctCd","");
            businessMap.put("access_number",item);
            businessMap.put("orderTypeId",20);
            //操作卡
            boolean success = iTelecomGatewayService.updateCardStatus(businessMap);
            redisUtil.setRemove("daily_limit_card", item);
        });
        return ReturnT.SUCCESS;
    }
}
