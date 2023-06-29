package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotCardWechatRelationService;
import com.wangxin.iot.card.IIotShareProfitsService;
import com.wangxin.iot.card.IIotTelecomCardInfoService;
import com.wangxin.iot.card.IStandardCostService;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.mapper.IotTelecomRefCardCostMapper;
import com.wangxin.iot.mapper.ShareProfitsMapper;
import com.wangxin.iot.model.IotCardWechatRelation;
import com.wangxin.iot.model.ShareProfits;
import com.wangxin.iot.model.StandardCost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;


/**
 * @Description: 分润明细
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Service
@Slf4j
public class IotShareProfitsServiceImpl extends ServiceImpl<ShareProfitsMapper, ShareProfits> implements IIotShareProfitsService {

    @Autowired
    private IotTelecomRefCardCostMapper telecomRefCardCostMapper;
    @Autowired
    private IStandardCostService standardCostService;
    @Autowired
    private IotTelecomCardInfoMapper telecomCardInfoMapper;
    @Autowired
    private IIotCardWechatRelationService cardWechatRelationService;

    @Override
    public void recycleCommission(IotTelecomRefCardCost refCardCost) {
        if(null!=refCardCost){
            StandardCost standardCost = standardCostService.getById(refCardCost.getCostId());
            QueryWrapper<IotTelecomCardInfo> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("iccid",refCardCost.getIccid());
            IotTelecomCardInfo telecomCardInformation = telecomCardInfoMapper.selectOne(queryWrapper);
            QueryWrapper<IotCardWechatRelation> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("iccid",refCardCost.getIccid());
            IotCardWechatRelation cardWechatRelation = cardWechatRelationService.getOne(queryWrapper1);
            ShareProfits shopProfits = new ShareProfits();
            shopProfits.setHigherAgentId(telecomCardInformation.getUserId());
            shopProfits.setHigherAgent(telecomCardInformation.getUserCompany());
            shopProfits.setIccid(refCardCost.getIccid());
            if(null!=cardWechatRelation){
                shopProfits.setMobile(cardWechatRelation.getMobile());
            }
            shopProfits.setPackageName(refCardCost.getCostName());
            shopProfits.setPackageId(refCardCost.getCostId());
            shopProfits.setPackageMoney(standardCost.getStandardRates());
            shopProfits.setPurchaseQuantity(1);
            shopProfits.setStatus("0");
            shopProfits.setShareStatus("0");
            shopProfits.setRemark("试用套餐激活扣费");
            shopProfits.setOperatorType(standardCost.getOperatorType());
            shopProfits.setShareMoney(standardCost.getStandardRates().negate());
            shopProfits.setCreateTime(new Date());
            this.save(shopProfits);
        }
    }
}
