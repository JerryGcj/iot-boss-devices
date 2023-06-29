package com.wangxin.iot.card.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotUnicomCardInfoService;
import com.wangxin.iot.card.IUnicomCardUsageService;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomFlowPool;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.mapper.IotUnicomFlowPoolMapper;
import com.wangxin.iot.mapper.IotUnicomRefCardCostMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.RealNameSystem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: iot_union_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
@Service
@Slf4j
public class IotUnicomCardInfoServiceImpl extends ServiceImpl<IotUnicomCardInfoMapper, IotUnicomCardInfo> implements IIotUnicomCardInfoService {
    @Autowired
    IUnicomCardUsageService unicomCardUsageService;
    @Autowired
    IotUnicomRefCardCostMapper iotUnicomRefCardCostMapper;
    @Autowired
    IotUnicomCardInfoMapper iotUnicomCardInfoMapper;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    IotUnicomFlowPoolMapper unicomFlowPoolMapper;
    @Override
    @Async
    public void simStatusChange(Map params) {
        JSONObject jsonObject = JSON.parseObject(params.get("data").toString());
        String iccid = jsonObject.getString("iccid");
        String currentState = jsonObject.getString("currentState");
        IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
        iotUnicomCardInfo.setSimStatus(Integer.valueOf(currentState));
        //卡状态变更
        iotUnicomCardInfo.setSimStatusChangeTime(new Date());
        UpdateWrapper<IotUnicomCardInfo> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("iccid",iccid);
        int update = this.baseMapper.update(iotUnicomCardInfo, objectUpdateWrapper);
        if(update == 1){
            log.info("推送修改卡:{}状态:{}成功",iccid,currentState);
        }
    }

    @Override
    @Async
    public void dataUsage24Change(Map params) {
        LocalDate localDate = LocalDate.now();
        //是否是月初的两天
        boolean earlyMonth = localDate.getDayOfMonth() + 1 <= 3;
        //月末是那天
        LocalDate lastDayOfMonth = localDate.with(TemporalAdjusters.lastDayOfMonth());
        boolean isContinue = localDate.getDayOfMonth() + 2 >= lastDayOfMonth.getDayOfMonth();
        //此处丢弃月末和月初两天的推送，联通平台有bug
        if(isContinue || earlyMonth){
            return;
        }
        JSONObject jsonObject = JSON.parseObject(params.get("data").toString());
        String iccid = (String) jsonObject.get("iccid");
        Long dailyDataUsage = Long.parseLong(jsonObject.get("dailyDataUsage").toString());
        BigDecimal bigDecimal = new BigDecimal(dailyDataUsage);
        //推送返回的是bit,转换成MB
        BigDecimal usage = bigDecimal.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
        Map map = new HashMap(1);
        map.put(LocalDate.now(),usage);
        //更新用量
        unicomCardUsageService.updateUsage(iccid,map);
        //生效中的基础套餐
        RefCardModel basicRef = this.iotUnicomRefCardCostMapper.getBasicRef(iccid);
        if(basicRef != null){
            basicRef.setPushDailyTime(new Date());
            //同步到ref表中
            unicomCardUsageService.syncRefUsage(basicRef);
        }
    }

    @Override
    public void dataUsageCycle(Map ctdUsage) {
        JSONObject jsonObject = JSON.parseObject(ctdUsage.get("data").toString());
        String iccid = (String) jsonObject.get("iccid");
        Long dailyDataUsage = Long.parseLong(jsonObject.get("dataUsage").toString());
        BigDecimal bigDecimal = new BigDecimal(dailyDataUsage);
        //推送返回的是bit,转换成MB
        BigDecimal usage = bigDecimal.divide(new BigDecimal(1024 * 1024), 2, BigDecimal.ROUND_HALF_UP);
        UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
        IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
        iotUnicomCardInfo.setMonthToDateUsage(usage);
        iotUnicomCardInfo.setDataUsageChangeTime(new Date());
        updateWrapper.eq("iccid",iccid);
        iotUnicomCardInfoMapper.update(iotUnicomCardInfo,updateWrapper);
    }

    @Override
    public void realNameStatus(Map realNameStatus) {
        JSONObject jsonObject = JSON.parseObject(realNameStatus.get("data").toString());
        String iccid = (String) jsonObject.get("iccid");
        QueryWrapper<IotUnicomCardInfo> cardInfoQueryWrapper = new QueryWrapper<>();
        cardInfoQueryWrapper.eq("iccid", iccid);
        IotUnicomCardInfo cardInfo = iotUnicomCardInfoMapper.selectOne(cardInfoQueryWrapper);
        if(null!=cardInfo){
            QueryWrapper<RealNameSystem> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("iccid", iccid).eq("status", "1");
            RealNameSystem realName = realNameSystemMapper.selectOne(queryWrapper);
            if(null==realName){
                RealNameSystem realNameSystem = new RealNameSystem();
                realNameSystem.setIccid(iccid);
                realNameSystem.setUserId(cardInfo.getUserId());
                realNameSystem.setUserCompany(cardInfo.getUserCompany());
                realNameSystem.setMsisdn(cardInfo.getMsisdn());
                realNameSystem.setOperatorType(2);
                realNameSystem.setStatus("1");
                realNameSystem.setCreateTime(new Date());
                realNameSystemMapper.insert(realNameSystem);
            }
        }
    }

    @Override
    public void flowPoolUsage(Map flowPool) {
        String currentMonth = LocalDate.now().toString().substring(0, 7);
        QueryWrapper<IotUnicomFlowPool> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("statistics_month",currentMonth);
        IotUnicomFlowPool iotUnicomFlowPool = this.unicomFlowPoolMapper.selectOne(queryWrapper);
        if(CollectionUtils.isEmpty(flowPool)){
            return;
        }
        Map map = (Map)flowPool.get("data");
        BigDecimal per =new BigDecimal(1024*1024*1024);
        BigDecimal usage = new BigDecimal(map.get("totalActualZoneUsage").toString());
        BigDecimal total = new BigDecimal(map.get("totalIncludedZoneUsage").toString());
        //insert
        if(iotUnicomFlowPool == null){
            IotUnicomFlowPool iotUnicomFlowPool1 = new IotUnicomFlowPool();
            iotUnicomFlowPool1.setCreateTime(new Date());
            iotUnicomFlowPool1.setTotal(total.divide(per));
            iotUnicomFlowPool1.setStatisticsMonth(currentMonth);
            iotUnicomFlowPool1.setUsaged(usage.divide(per).setScale(2,BigDecimal.ROUND_HALF_UP));
            this.unicomFlowPoolMapper.insert(iotUnicomFlowPool1);
        }else{
            //update
            iotUnicomFlowPool.setUsaged(usage.divide(per));
            iotUnicomFlowPool.setTotal(total.divide(per));
            this.unicomFlowPoolMapper.updateById(iotUnicomFlowPool);
        }
    }

}
