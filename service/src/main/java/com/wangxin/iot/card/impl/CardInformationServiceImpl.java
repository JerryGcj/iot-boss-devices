package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ICardInformationService;
import com.wangxin.iot.config.TemplateConfig;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mobile.IotApiClient;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 */
@Service
@Slf4j
public class CardInformationServiceImpl implements ICardInformationService {

    @Autowired
    private IotApiClient iotApiClient;
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    private CardInformationMapper cardInformationMapper;
    @Override
    public int updateCardStatus(String status, String iccid) {
        CardInformation cardInformation = new CardInformation();
        cardInformation.setCardState(status);
        cardInformation.setUpdateDate(DateUtils.getCurrentDate());
        cardInformation.setNote("");
        //cardInformation.setActivationTime(DateUtils.getCurrentDate());
        int effectCount = cardInformationMapper.update(cardInformation, new UpdateWrapper<CardInformation>().eq("iccid", iccid));
        if(effectCount == 1){
            log.info("修改卡状态成功，iccid:{}",iccid);
        }
        return effectCount;
    }

    @Override
    public void syncCardUsaged(List iccids, TemplateConfig templateConfig) {
        if(CollectionUtils.isEmpty(iccids)){
            return;
        }
        if(iccids.size()>50){
            redisUtil.lSetList(RedisKeyConstants.TASK_ACTIVE.getMessage(), iccids);
            List ids = redisUtil.lBatchPop(RedisKeyConstants.TASK_ACTIVE.getMessage(), 0, 49);
            while (CollectionUtils.isNotEmpty(ids)){
                try {
                    //调用是async的
                    iotApiClient.callWebService(ids,templateConfig);
                    //一秒查一次
                    TimeUnit.SECONDS.sleep(1);
                    //获取下一次要查询的iccid
                    ids = redisUtil.lBatchPop("task_active", 0, 49);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        // 要查询的卡号小于50直接查
        iotApiClient.callWebService(iccids,templateConfig);
    }
}
