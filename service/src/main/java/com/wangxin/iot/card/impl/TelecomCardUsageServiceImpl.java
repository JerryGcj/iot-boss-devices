package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.ITelecomCardUsageService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.domain.IotTelecomCardUsage;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.IotTelecomRefCardCostMapper;
import com.wangxin.iot.mapper.IotTelocomCardUsageMapper;
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
public class TelecomCardUsageServiceImpl extends ServiceImpl<IotTelocomCardUsageMapper,IotTelecomCardUsage> implements ITelecomCardUsageService {
    /**
     * 电信计费周期开始的时间
     */
    private static final int TELECOM_PERIOD_BEGIN = 1;
    @Autowired
    IotTelocomCardUsageMapper iotTelocomCardUsageMapper;
    @Autowired
    IotTelecomRefCardCostMapper iotTelecomRefCardCostMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public void updateUsage(Map<String,String> map) {
        LocalDate now = LocalDate.now();
        String iccid = map.get("iccid");
        BigDecimal currentDayUsage = new BigDecimal(map.get("totalUsage"));
        String accessNumber = map.get("accessNumber");
        //设置一个内存标志位，判断今天有没有插入过
        String str = now.toString()+"-"+accessNumber;
        boolean contains = redisUtil.sHasKey(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
        //如果统计的时间是1号，那么是账单开始的第一天。
        if(contains){
            //已经有记录了，更新即可
            iotTelocomCardUsageMapper.updateUsage(currentDayUsage,iccid,now);
        }else{
            redisUtil.sSet(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),str);
            IotTelecomCardUsage iotTelecomCardUsage = new IotTelecomCardUsage();
            iotTelecomCardUsage.setIccid(iccid);
            iotTelecomCardUsage.setAccessNumber(map.get("accessNumber"));
            iotTelecomCardUsage.setDate(LocalDate.now());
            iotTelecomCardUsage.setCardUsage(currentDayUsage);
            iotTelecomCardUsage.setCreateDate(new Date());
            //没记录，插入
            iotTelocomCardUsageMapper.insert(iotTelecomCardUsage);
        }
    }

    @Override
    public void syncRefUsage(RefCardModel refCardModel) {
        try {
            //生效的加油包
            if(refCardModel.getCostType().equals(1)){
                refCardModel =  iotTelecomRefCardCostMapper.getBasicRefByOilWithParentId(refCardModel);
            }
            BigDecimal periodUsage = iotTelocomCardUsageMapper.getPeriodUsage(refCardModel);
            if(periodUsage != null){
                IotTelecomRefCardCost iotRefCardCost = new IotTelecomRefCardCost();
                iotRefCardCost.setPushDailyTime(refCardModel.getPushDailyTime());
                iotRefCardCost.setId(refCardModel.getId());
                iotRefCardCost.setUsaged(periodUsage);
                iotRefCardCost.setUpdateTime(new Date());
                iotTelecomRefCardCostMapper.updateById(iotRefCardCost);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
