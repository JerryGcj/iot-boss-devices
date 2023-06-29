package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotShareProfitsService;
import com.wangxin.iot.card.IIotTelecomCardInfoService;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.card.IotTelecomRefCardCostService;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.mapper.IotTelecomRefCardCostMapper;
import com.wangxin.iot.mapper.StandardCostMapper;
import com.wangxin.iot.model.StandardCost;
import com.wangxin.iot.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: iot_telecom_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
@Service
public class TelecomCardInfoServiceImpl extends ServiceImpl<IotTelecomCardInfoMapper, IotTelecomCardInfo> implements IIotTelecomCardInfoService {

    @Autowired
    StandardCostMapper standardCostMapper;
    @Autowired
    IotTelecomRefCardCostMapper telecomRefCardCostMapper;
    @Autowired
    ITelecomGatewayService telecomGatewayService;
    @Autowired
    IIotShareProfitsService shareProfitsService;

    @Override
    @Async
    public void simStatusChange(Map simStateChange) {
        Map data = (Map)simStateChange.get("data");
        String iccid = (String) data.get("iccid");
        Integer currentState = (Integer)data.get("currentState");
        IotTelecomCardInfo iotUnicomCardInfo = new IotTelecomCardInfo();
        iotUnicomCardInfo.setSimStatus(currentState);
        //卡状态变更
        iotUnicomCardInfo.setSimStatusChangeTime(new Date());
        UpdateWrapper<IotTelecomCardInfo> objectUpdateWrapper = new UpdateWrapper<>();
        objectUpdateWrapper.eq("iccid",iccid);
        this.baseMapper.update(iotUnicomCardInfo,objectUpdateWrapper);
    }

    @Override
    public void activePackage(String accessNumber){
        QueryWrapper<IotTelecomRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_number",accessNumber).eq("free_type","1").eq("active",0);
        IotTelecomRefCardCost refCardCost = telecomRefCardCostMapper.selectOne(queryWrapper);
        if(null!=refCardCost){
            StandardCost standardCost = standardCostMapper.selectById(refCardCost.getCostId());
            if(null!=standardCost){
                IotTelecomRefCardCost newRefCardCost = new IotTelecomRefCardCost();
                newRefCardCost.setId(refCardCost.getId());
                newRefCardCost.setActive(1);
                newRefCardCost.setValidStart(new Date());
                newRefCardCost.setValidEnd(DateUtils.addDays2Date(new Date(),standardCost.getPeriodOfValidity()));
                telecomRefCardCostMapper.updateById(newRefCardCost);
                //看看卡是什么状态
                IotTelecomCardInfo telecomCardInfo = this.getOne(new QueryWrapper<IotTelecomCardInfo>().eq("access_number",accessNumber));
                if(telecomCardInfo.getSimStatus()==5){
                    //去激活卡
                    Map<String,String> map= new HashMap<>(3);
                    map.put("access_number",accessNumber);
                    map.put("method","disabledNumber");
                    map.put("orderTypeId","20");
                    telecomGatewayService.updateCardStatus(map);
                }
                //扣除佣金
                shareProfitsService.recycleCommission(refCardCost);
            }
        }
    }
}
