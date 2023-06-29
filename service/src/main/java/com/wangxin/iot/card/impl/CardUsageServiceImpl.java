package com.wangxin.iot.card.impl;

import com.wangxin.iot.card.ICardUsageService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.CardUsageMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.model.CardUsage;
import com.wangxin.iot.model.IotRefCardCost;
import com.wangxin.iot.utils.StringUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/5/24
 */
@Service
public class CardUsageServiceImpl implements ICardUsageService {

    @Autowired
    CardUsageMapper cardUsageMapper;
    @Autowired
    IotRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    RedisUtil redisUtil;
    @Override
    @Async
    public void syncRefUsage(RefCardModel refCardModel) {
        if(refCardModel.getCostType().equals(1)){
            refCardModel =  iotRefCardCostMapper.getBasicRefByOil(refCardModel);
        }
        BigDecimal periodUsage = cardUsageMapper.getPeriodUsage(refCardModel);
        if(periodUsage == null){
            periodUsage = BigDecimal.ZERO;
        }
        IotRefCardCost iotRefCardCost = new IotRefCardCost();
        iotRefCardCost.setId(refCardModel.getId());
        iotRefCardCost.setUsaged(periodUsage);
        iotRefCardCostMapper.updateById(iotRefCardCost);

    }

    @Override
    public BigDecimal getPeriodUsage(RefCardModel refCardModel) {
        return cardUsageMapper.getPeriodUsage(refCardModel);
    }

    @Override
    public void saveUsage(String iccid, LocalDate activeDate,BigDecimal currentUsage) {

        CardUsage cardUsage = new CardUsage();
        cardUsage.setIccid(iccid);
        cardUsage.setDate(activeDate);
        cardUsage.setCreateDate(new Date());
        cardUsage.setCardUsage(currentUsage);
        cardUsageMapper.insert(cardUsage);
    }
    @Override
    public void updateUsage(String iccid, LocalDate localDate,BigDecimal currentUsage) {
        LocalDate monthFirstDay = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
        //设置一个内存标志位，判断今天有没有插入过
        String str = localDate.toString()+"_"+iccid;
        boolean contains = redisUtil.hHasKey(RedisKeyConstants.MOBILE_UPDATE_USAGE_CONTAINER.getMessage(),str);
        //今天第一次插入
        if(!contains){
            String currentMonthSumUsage = "";
            BigDecimal realUsage=currentUsage;
            //不是1号，返回用量-查询当月的总用量=今天的用量。
            if(!localDate.isEqual(monthFirstDay)){
                BigDecimal currentMonthUsage = cardUsageMapper.getCurrentMonthUsage(iccid, localDate.minusDays(1));
                if(currentMonthUsage != null){
                    currentMonthSumUsage = currentMonthUsage.toString() ;
                    realUsage = currentUsage.subtract(currentMonthUsage);
                 }
            }else{
                //是1号，那么当月总用量等于当前的用量
                currentMonthSumUsage = currentUsage.toString();
            }
            this.saveUsage(iccid,localDate,realUsage);
            //没记录，插入
            redisUtil.hset(RedisKeyConstants.MOBILE_UPDATE_USAGE_CONTAINER.getMessage(),str,currentMonthSumUsage);
        }else {
            //若是1号，则直接更新即可
            if(localDate.isEqual(monthFirstDay)){
                cardUsageMapper.updateUsage(currentUsage,iccid,localDate);
                return;
            }
            BigDecimal currentMonthUsage = BigDecimal.ZERO;
            //当月截止到前一天的总用量A，当前用量-A=今天的
            String currentMonthSumUsage = (String)redisUtil.hget(RedisKeyConstants.MOBILE_UPDATE_USAGE_CONTAINER.getMessage(), str);
            if(StringUtils.isNotEmpty(currentMonthSumUsage)){
                currentMonthUsage = new BigDecimal(currentMonthSumUsage);
            }
            //实际用量
            BigDecimal realUsage = currentUsage.subtract(currentMonthUsage);
            cardUsageMapper.updateUsage(realUsage,iccid,localDate);
        }
    }
}
