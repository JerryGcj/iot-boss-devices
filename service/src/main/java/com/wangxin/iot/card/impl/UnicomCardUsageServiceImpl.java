package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.IUnicomCardUsageService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.domain.IotUnicomCardUsage;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.IotUnicomCardUsageMapper;
import com.wangxin.iot.mapper.IotUnicomRefCardCostMapper;
import com.wangxin.iot.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/7/22
 */
@Service
public class UnicomCardUsageServiceImpl implements IUnicomCardUsageService {
    /**
     * 联通计费周期开始的时间
     */
    private static final int UNICOM_PERIOD_BEGIN = 27;
    @Autowired
    IotUnicomCardUsageMapper iotUnicomCardUsageMapper;
    @Autowired
    IotUnicomRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void updateUsage(Map map) {
        LocalDate now = LocalDate.now();
        String iccid = (String)map.get("iccid");
        //本周期内的流量用量
        BigDecimal monthToDateUsage = new BigDecimal(map.get("monthToDateUsage").toString());
        //获取当前是几号
        int dayOfMonth = now.getDayOfMonth();
        //设置一个内存标志位，判断今天有没有插入过
        String str = now.toString()+"-"+iccid;
        boolean contains = redisUtil.sHasKey(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
        //如果统计的时间是27号，那么是账单开始的第一天。
        if(dayOfMonth == UNICOM_PERIOD_BEGIN){
            if(contains){
                //已经有记录了，更新即可
                iotUnicomCardUsageMapper.updateUsage(monthToDateUsage,iccid,now);
            }else{
                //没记录，插入
                redisUtil.sSet(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
                IotUnicomCardUsage iotUnicomCardUsage = new IotUnicomCardUsage();
                iotUnicomCardUsage.setIccid(iccid);
                iotUnicomCardUsage.setDate(LocalDate.now());
                iotUnicomCardUsage.setCardUsage(monthToDateUsage);
                iotUnicomCardUsage.setCreateDate(new Date());
                iotUnicomCardUsageMapper.insert(iotUnicomCardUsage);
            }
        }else{
            /**
                返回的是周期内的累计使用流量
             */
            //这个周期开始的第一天是上个月的27号
            LocalDate lastMonth27 = LocalDate.of(now.getMonthValue() == 1 ? now.getYear()-1 : now.getYear(), now.minusMonths(1).getMonth(), UNICOM_PERIOD_BEGIN);
            //如果统计月份是27号以后，那么就应该用当月27号作为开始计费周期
            if(dayOfMonth > UNICOM_PERIOD_BEGIN){
                lastMonth27 = LocalDate.of(now.getYear(), now.getMonth(), UNICOM_PERIOD_BEGIN);
            }
            //27号到昨天总用量
            BigDecimal currentPeriodUsage = iotUnicomCardUsageMapper.getCurrentPeriodUsage(iccid, lastMonth27,now.minusDays(1));
            BigDecimal realUsage = currentPeriodUsage == null ? BigDecimal.ZERO : currentPeriodUsage;
            //今天的用量
            BigDecimal todayUsage = monthToDateUsage.subtract(realUsage);
            if(contains){
                //已经有记录了，更新即可
                iotUnicomCardUsageMapper.updateUsage(todayUsage,iccid,now);
            }else{
                //没记录，插入
                redisUtil.sSet(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
                IotUnicomCardUsage iotUnicomCardUsage = new IotUnicomCardUsage();
                iotUnicomCardUsage.setIccid(iccid);
                iotUnicomCardUsage.setDate(now);
                iotUnicomCardUsage.setCardUsage(todayUsage);
                iotUnicomCardUsage.setCreateDate(new Date());
                iotUnicomCardUsageMapper.insert(iotUnicomCardUsage);
            }
        }

    }


    @Override
    public void updateUsage(String iccid, Map map) {
        if(CollectionUtils.isEmpty(map)){
            return;
        }
        //更新usage表
        map.forEach((k,v)-> {
            QueryWrapper<IotUnicomCardUsage> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("iccid",iccid);
            queryWrapper.eq("date",k);
            //设置一个内存标志位，判断今天有没有插入过
            String str = k+"-"+iccid;
            boolean contains = redisUtil.sHasKey(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
            //已经有记录了，更新即可
            if(contains){
                iotUnicomCardUsageMapper.updateUsage((BigDecimal)v,iccid,(LocalDate)k);
            }else{
                //没记录，插入
                redisUtil.sSet(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
                IotUnicomCardUsage iotUnicomCardUsage = new IotUnicomCardUsage();
                iotUnicomCardUsage.setIccid(iccid);
                iotUnicomCardUsage.setDate((LocalDate) k);
                iotUnicomCardUsage.setCardUsage((BigDecimal)v);
                iotUnicomCardUsage.setCreateDate(new Date());
                iotUnicomCardUsageMapper.insert(iotUnicomCardUsage);
            }
        });
    }

    @Override
    public void syncRefUsage(RefCardModel refCardModel) {
        try {
            //生效的加油包
            if(refCardModel.getCostType().equals(1)){
                refCardModel =  iotRefCardCostMapper.getBasicRefByOilWithParentId(refCardModel);
            }
            BigDecimal periodUsage = iotUnicomCardUsageMapper.getPeriodUsage(refCardModel);
            if(periodUsage != null){
                IotUnicomRefCardCost iotRefCardCost = new IotUnicomRefCardCost();
                iotRefCardCost.setPushDailyTime(refCardModel.getPushDailyTime());
                iotRefCardCost.setId(refCardModel.getId());
                iotRefCardCost.setUsaged(periodUsage);
                iotRefCardCost.setUpdateTime(new Date());
                iotRefCardCostMapper.updateById(iotRefCardCost);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @Override
    public void saveUsage(String iccid, LocalDate activeDate,BigDecimal currentUsage) {

        IotUnicomCardUsage cardUsage = new IotUnicomCardUsage();
        cardUsage.setIccid(iccid);
        cardUsage.setDate(activeDate);
        cardUsage.setCreateDate(new Date());
        cardUsage.setCardUsage(currentUsage);
        iotUnicomCardUsageMapper.insert(cardUsage);
    }
}
