package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.*;
import com.wangxin.iot.constants.TelecomCardStatusEnum;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.mapper.*;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: yanwin
 * @Date: 2020/5/25
 */
@Service
@Slf4j
public class OrderPackageServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderPackageService {
    @Autowired
    ICardInformationService cardInformationService;
    @Autowired
    ICardUsageService cardUsageService;
    @Autowired
    IotRefCardCostService iotRefCardCostService;
    @Autowired
    IotTemplateFactory iotTemplateFactory;
    @Autowired
    CardInformationMapper cardInformationMapper;
    @Autowired
    IotRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    IotTelecomCardInfoMapper iotTelecomCardInfoMapper;
    @Autowired
    IotTelecomRefCardCostService iotTelecomRefCardCostService;
    @Autowired
    ITelecomGatewayService telecomGatewayService;
    @Autowired
    IotUnicomCardInfoMapper unicomCardInfoMapper;
    @Autowired
    IotUnicomRefCardCostService unicomRefCardCostService;
    @Autowired
    IUnicomGatewayService iIoTGatewayApiService;

    @Override
    public void telecomOrderPackage(Order order) {
        QueryWrapper<IotTelecomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iccid", order.getIccid());
        IotTelecomCardInfo cardInfo = iotTelecomCardInfoMapper.selectOne(queryWrapper);
        if(null!=cardInfo){
            //看看要不要调接口激活卡
            if(cardInfo.getSimStatus()!=1&&cardInfo.getSimStatus()!=4&&cardInfo.getSimStatus()!=6){
                log.info("接入号：{}，订购了套餐但不是激活状态，自动激活",order.getAccessNumber());
                Map businessMap = new HashMap();
                //发生位置是公众号订购
                businessMap.put("action","2");
                //卡是初始状态，测试激活（没测试套餐），则调用 活卡激活接口
                if(cardInfo.getSimStatus().intValue() == TelecomCardStatusEnum.ACTIVETEST.getIntCode().intValue()){
                    businessMap.put("method","requestServActive");
                }else if(cardInfo.getSimStatus().intValue() == TelecomCardStatusEnum.CLEANED.getIntCode().intValue()){
                    //停机 --->在用 orderTypeId =20
                    businessMap.put("method","disabledNumber");
                    businessMap.put("acctCd","");
                    businessMap.put("orderTypeId",20);
                }
                businessMap.put("access_number",order.getAccessNumber());
                //激活卡
                telecomGatewayService.updateCardStatus(businessMap);
            }
        }
        //存流量关系表
        this.iotTelecomRefCardCostService.saveRefWithOrder(order);
    }


    /**
     * 后台接口新增套餐，并激活卡
     * @param reqParam
     */
    @Override
    @Deprecated
    public void apiPlaceOrder(Map<String, String> reqParam) {
        Order order = new Order();
        order.setPaymentChannel("3");
        //手动设定的初始用量
        order.setNote(reqParam.get("initUsaged"));
        //套餐开始时间
        order.setCreateTime(DateUtils.formatStringToDate(reqParam.get("start")));
        //结束时间
        order.setUpdateDate(DateUtils.formatStringToDate(reqParam.get("end")));
        //卡号
        order.setIccid(reqParam.get("iccid"));
        order.setPackageId(reqParam.get("packageId"));
        //根据iccid,查找对应的通道
        IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(order.getIccid());
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
        try {
            String cardState = reqParam.get("cardState");
            String goalStatus="";
            if(cardState.equals("4")){
                goalStatus = "3";
            }
            reqParam.put("status",goalStatus);
            boolean flag = thirdService.modifyCard(reqParam, templateByOperation);
            if(flag){
                int count = cardInformationService.updateCardStatus("3",reqParam.get("iccid"));
                if(count==1){
                    log.info("接口订购套餐激活卡成功:{}",reqParam.get("iccid"));
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        //订购基础套餐，insert
        iotRefCardCostService.saveWithOrder(order);

    }

    @Override
    public void wechatOrderPackage(Order order){
        QueryWrapper<CardInformation> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iccid",order.getIccid());
        CardInformation cardInformation = cardInformationMapper.selectOne(queryWrapper);
        //决定要不要调用移动接口激活卡
        if(!"3".equals(cardInformation.getCardState())){
            //根据iccid,查找对应的通道
            IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(order.getIccid());
            ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
            //封装参数
            HashMap<String, String> orderMap = new HashMap<>(5);
            orderMap.put("iccid",order.getIccid());
                //移动流量池
            orderMap.put("orderId",order.getOrderId());
            orderMap.put("orderStatus",order.getOrderState().toString());
            //卡是停机状态，则申请复机
            if(cardInformation.getCardState().equals("4")){
                orderMap.put("status","3");
            }else{
                orderMap.put("status","1");
            }
            try {
                orderMap.put("action","2");
                //激活卡，修改订单状态
                thirdService.placeOrderCost(orderMap, templateByOperation);
            }catch (Exception e){
                log.error("调用oneLink失败:iccid:{}",order.getIccid());
                e.printStackTrace();
            }
        }
        //存流量关系表
        this.iotRefCardCostService.saveRefWithOrder(order);
    }

    @Override
    public void unicomOrderPackage(Order order) {
        QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iccid", order.getIccid());
        IotUnicomCardInfo cardInfo = unicomCardInfoMapper.selectOne(queryWrapper);
        /*if(null!=cardInfo){
            if(cardInfo.getSimStatus()!=2){
                log.info("卡号：iccid：{}，订购了套餐但不是激活状态，自动激活",order.getIccid());
                Map businessMap = new HashMap(3);
                //发生位置是公众号
                businessMap.put("action","2");
                businessMap.put("goalState","2");
                businessMap.put("iccid",order.getIccid());
                //激活卡
                iIoTGatewayApiService.updateCardStatus(businessMap);
            }
        }*/
        //存流量关系表
        if(order.getBuyNumber() >1){
            for (int i = 0; i < order.getBuyNumber(); i++) {
                //存流量关系表
                unicomRefCardCostService.saveRefWithOrder(order);
                try {
                    //此处睡眠0.2秒，是为了套餐开始时间错开，这样季包其余的active就是未生效状态
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            unicomRefCardCostService.saveRefWithOrder(order);
        }
    }
}
