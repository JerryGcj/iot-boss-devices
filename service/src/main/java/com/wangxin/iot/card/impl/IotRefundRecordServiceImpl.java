package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotRefundRecordService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.card.IotRefCardCostService;
import com.wangxin.iot.card.IotUnicomRefCardCostService;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.mapper.*;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.*;
import com.wangxin.iot.other.IotTemplateFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Description: iot_refund_record
 * @Author: jeecg-boot
 * @Date:   2021-07-13
 * @Version: V1.0
 */
@Service
@Slf4j
public class IotRefundRecordServiceImpl extends ServiceImpl<IotRefundRecordMapper, IotRefundRecord> implements IIotRefundRecordService {

    @Autowired
    private IUnicomGatewayService iUnicomGatewayService;
    @Autowired
    private IotCardWechatRelationMapper cardWechatRelationMapper;
    @Autowired
    private IotRefCardCostService refCardCostService;
    @Autowired
    private CardInformationMapper cardInformationMapper;
    @Autowired
    private IotUnicomRefCardCostService unicomRefCardCostService;
    @Autowired
    private IotUnicomCardInfoMapper unicomCardInfoMapper;
    @Autowired
    private IotTemplateFactory iotTemplateFactory;

    @Override
    public void automation(IotRefundRecord iotRefundRecord) {
        //余额清零及解绑
        IotCardWechatRelation cardWechatRelation = new IotCardWechatRelation();
        cardWechatRelation.setVirtualIccid("");
        cardWechatRelation.setIccid("");
        cardWechatRelation.setAccessNumber("");
        cardWechatRelation.setRefundSwitch(false);
        cardWechatRelation.setDelFlag("1");
        cardWechatRelation.setAccount(BigDecimal.ZERO);
        UpdateWrapper<IotCardWechatRelation> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("open_id",iotRefundRecord.getOpenId()).eq("operator_type",iotRefundRecord.getOperatorType());
        cardWechatRelationMapper.update(cardWechatRelation,updateWrapper);
        //卡停机，将套餐改为退款失效，回收到废卡回收账户
        if("1".equals(iotRefundRecord.getOperatorType())){
            //套餐改退款失效
            List<IotRefCardCost> list = refCardCostService.list(new QueryWrapper<IotRefCardCost>().eq("card_iccid",iotRefundRecord.getIccid()));
            if(CollectionUtils.isNotEmpty(list)){
                List<IotRefCardCost> updateList = new ArrayList<>();
                list.forEach(ref -> {
                    if(ref.getActive()==0||ref.getActive()==1){
                        IotRefCardCost refCardCost = new IotRefCardCost();
                        refCardCost.setRefundStatus("1");
                        refCardCost.setActive(4);
                        refCardCost.setValidEnd(new Date());
                        if(ref.getActive()==0){
                            refCardCost.setValidEnd(ref.getValidStart());
                        }
                        refCardCost.setId(ref.getId());
                        updateList.add(refCardCost);
                    }
                });
                if(CollectionUtils.isNotEmpty(updateList)){
                    refCardCostService.updateBatchById(updateList);
                }
            }
            //给卡停机
            CardInformation card = cardInformationMapper.selectOne(new QueryWrapper<CardInformation>().eq("iccid",iotRefundRecord.getIccid()));
            if("3".equals(card.getCardState())){
                IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(iotRefundRecord.getIccid());
                ThirdService executorThridService = iotTemplateFactory.getExecutorThridService(operatorTemplate);
                Map<String, String> paramMap = new HashMap();
                paramMap.put("iccid", iotRefundRecord.getIccid());
                paramMap.put("status","4");
                paramMap.put("action","1");
                boolean response = executorThridService.modifyCard(paramMap, operatorTemplate);
            }
            //分配到废卡回收账户下
            CardInformation cardInformation = new CardInformation();
            cardInformation.setCustomerId("caf26dfd81e9917d162685c192f92c08");
            cardInformation.setUserName("recycle");
            cardInformation.setCustomerName("废卡回收");
            cardInformationMapper.update(cardInformation,new UpdateWrapper<CardInformation>().eq("iccid",iotRefundRecord.getIccid()));
        }
        if("2".equals(iotRefundRecord.getOperatorType())){
            //套餐改退款失效
            List<IotUnicomRefCardCost> list = unicomRefCardCostService.list(new QueryWrapper<IotUnicomRefCardCost>().eq("iccid",iotRefundRecord.getIccid()));
            if(CollectionUtils.isNotEmpty(list)){
                List<IotUnicomRefCardCost> updateList = new ArrayList<>();
                list.forEach(ref -> {
                    if(ref.getActive()==0||ref.getActive()==1){
                        IotUnicomRefCardCost refCardCost = new IotUnicomRefCardCost();
                        refCardCost.setRefundStatus("1");
                        refCardCost.setActive(4);
                        refCardCost.setValidEnd(new Date());
                        if(ref.getActive()==0){
                            refCardCost.setValidEnd(ref.getValidStart());
                        }
                        refCardCost.setId(ref.getId());
                        updateList.add(refCardCost);
                    }
                });
                if(CollectionUtils.isNotEmpty(updateList)){
                    unicomRefCardCostService.updateBatchById(updateList);
                }
            }
            //给卡停机
            IotUnicomCardInfo iotUnicomCardInfo = unicomCardInfoMapper.selectOne(new QueryWrapper<IotUnicomCardInfo>().eq("iccid",iotRefundRecord.getIccid()));
            if(iotUnicomCardInfo.getSimStatus()==2){
                Map businessMap = new HashMap(3);
                businessMap.put("action","5");
                businessMap.put("goalState","3");
                businessMap.put("iccid",iotRefundRecord.getIccid());
                //操作卡
                iUnicomGatewayService.updateCardStatus(businessMap);
            }
            //分配到废卡回收账户下
            IotUnicomCardInfo unicomCardInfo = new IotUnicomCardInfo();
            unicomCardInfo.setUserId("caf26dfd81e9917d162685c192f92c08");
            unicomCardInfo.setUserName("recycle");
            unicomCardInfo.setUserCompany("废卡回收");
            unicomCardInfoMapper.update(unicomCardInfo,new UpdateWrapper<IotUnicomCardInfo>().eq("iccid",iotRefundRecord.getIccid()));
        }
    }
}
