package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotCardSeparateService;
import com.wangxin.iot.domain.IotCardSeparateEntity;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotCardSeparateMapper;
import com.wangxin.iot.mobile.OneLinkServiceImpl;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.other.IotTemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * Created by 18765 on 2020/1/4 11:23
 */
@Service
@Slf4j
public class IotIotCardSeparateServiceImpl extends ServiceImpl<IotCardSeparateMapper,IotCardSeparateEntity> implements IIotCardSeparateService {

    @Autowired
    Executor executor;
    @Autowired
    IotTemplateFactory iotTemplateFactory;
    @Autowired
    CardInformationMapper cardInformationMapper;
    @Override
    @Async
    public void syncStopReasonAndRestartCard(List<Map<String,String>> lists) {
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        if(CollectionUtils.isEmpty(lists)){
            //有套餐我们平台正常的卡
            lists = cardInformationMapper.getMsisdnIccidByActiveCost("1");
        }
        log.info("同步异常停机任务开始，我们平台正常的卡数量：{}",lists.size());
        //首先把表里数据清除，获取最新的数据
        this.baseMapper.delete(null);
        //线程安全
        lists.forEach(item-> {
            threadPoolExecutor.execute(() -> {
                String iccid = item.get("iccid");
                ThirdService thirdService = iotTemplateFactory.getExecutorThridService(iccid);
                IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(iccid);
                if(thirdService instanceof OneLinkServiceImpl){
                    OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
                    Map result = oneLinkService.getSimStopReason(iccid, operatorTemplate);
                    if(result != null){
                        String status = result.get("stopReason").toString();
                        //不是未停机状态的卡
                        if(!"000000000000".equals(status)){
                            IotCardSeparateEntity iotCardSeparateEntity = new IotCardSeparateEntity();
                            iotCardSeparateEntity.setIccid(iccid);
                            iotCardSeparateEntity.setMsisdn(item.get("msisdn"));
                            iotCardSeparateEntity.setStopReason(status);
                            iotCardSeparateEntity.setCreateTime(new Date());
                            this.save(iotCardSeparateEntity);
                        }
                        //主动申请停机的卡
                        if("000000002000".equals(status)){
                            Map map = new HashMap(2);
                            map.put("iccid",iccid);
                            map.put("status","3");
                            boolean success = oneLinkService.modifyCard(map, operatorTemplate);
                            if(success){
                                log.info("iccid：{}，我方正常，移动停机，激活成功",item);
                                UpdateWrapper<CardInformation> cardInformationUpdateWrapper = new UpdateWrapper<>();
                                cardInformationUpdateWrapper.eq("iccid", iccid);
                                CardInformation cardInformation = new CardInformation();
                                cardInformation.setCardState("3");
                                this.cardInformationMapper.update(cardInformation, cardInformationUpdateWrapper);
                            }
                        }
                    }
                }
            });
        });

    }
}
