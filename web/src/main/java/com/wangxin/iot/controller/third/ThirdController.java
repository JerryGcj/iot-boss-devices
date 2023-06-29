package com.wangxin.iot.controller.third;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ICardInformationService;
import com.wangxin.iot.card.IIotCardSeparateService;
import com.wangxin.iot.card.IOrderPackageService;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.mobile.OneLinkServiceImpl;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.rest.base.CodeMsg;
import com.wangxin.iot.rest.base.Result;
import com.wangxin.iot.utils.Frequently;
import com.wangxin.iot.utils.StringUtil;
import com.wangxin.iot.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Date;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/3/11
 */
@Slf4j
@RestController
@RequestMapping("third")
public class ThirdController {
    @Autowired
    IOrderPackageService orderPackageService;
    @Autowired
    CardInformationMapper cardInformationMapper;
    @Autowired
    IotRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    IotTemplateFactory iotTemplateFactory;
    @Autowired
    ICardInformationService cardInformationService;
    @Autowired
    IIotCardSeparateService cardSeparateService;

    @Deprecated
    @RequestMapping("apiPlaceOrder")
    public Result apiPlaceOrder(@RequestBody Map<String,String> param){
        log.info("后台手动订购套餐开始，{}",param);
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        orderPackageService.apiPlaceOrder(param);
        return Result.success(null);
    }
    @RequestMapping("modifyCardStatus")
    public Result modifyCardStatus(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("修改卡状态，{}",param);
        //若目标状态是 4:停机 5:已销号 6:机卡分离 7:高危禁用，则统一停机
        String goalState = param.get("goalState");

        String state = goalState;
        if("4".equals(goalState) ||
                "5".equals(goalState) ||
                "6".equals(goalState) ||
                "7".equals(goalState)){
            param.put("status","4");
        }else if("2".equals(goalState)){
            state = "3";
            param.put("status",goalState);
        }else{
            param.put("status",goalState);
        }
        String originState = param.get("originState");
        //原始状态为1，则目标状态为2
        if("1".equals(originState)){
            param.put("status","2");
        }
        String iccid = param.get("iccid");
        param.put("action","0");
        /**
         * 机卡分离不用请求移动接口了
         */
        if(goalState.equals("6")){
            int count = cardInformationService.updateCardStatus(state,iccid);
            if(count == 1){
                return Result.success(null);
            }
        }
        IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(iccid);
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
        //目前只有移动池通道支持修改卡状态
        if(thirdService instanceof OneLinkServiceImpl){
            OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
            boolean flag = oneLinkService.modifyCard(param, templateByOperation);
            //移动通道的机卡分离直接修改即可
            if(flag || goalState.equals("5") || goalState.equals("6") || goalState.equals("7")){
                int count = cardInformationService.updateCardStatus(state,iccid);
                if(count == 1){
                    return Result.success(null);
                }
            }
            return Result.fail(new CodeMsg(1,"修改失败"));
        }
        return Result.fail(new CodeMsg(1,"该通道暂不能够修改卡状态"));
    }
    @RequestMapping("placeOrderCost")
    public Result placeOrderCost(@RequestBody Map<String,String> param){
        //校验数据正确性。
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("订购套餐:{}",param);
        String iccid = param.get("iccid");
        IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(iccid);
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
        //根据iccid,查找对应的通道，
        return thirdService.placeOrderCost(param,templateByOperation);
    }

    @RequestMapping("queryCardDetail")
    public Result queryCardDetail(@RequestBody Map<String,String> param) {
        // 校验数据
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }

        String iccid = param.get("iccid");
        IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(iccid);
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
        // 查询用量和状态
        Map result = thirdService.sendReq(iccid, templateByOperation);
        if(StringUtil.isNotEmpty(result.get("error").toString())) {
            return Result.fail(new CodeMsg(1, result.get("message").toString()));
        }
//        String data = result.get("data").toString();
//        BigDecimal use = new BigDecimal(data);
        String status = result.get("status").toString();
        String msisdn = result.get("msisdn").toString();
        Date activationTime = (Date)result.get("activationTime");
//        String smsState = result.get("smsState").toString();

        // 修改iot_card_information表
        CardInformation cardInformation = new CardInformation();
//        cardInformation.setTrafficUse(use);
        cardInformation.setMsisdn(msisdn);
        cardInformation.setCardState(status);
        cardInformation.setActivationTime(activationTime);
//        cardInformation.setSmsState(smsState);

        UpdateWrapper<CardInformation> wrapper = new UpdateWrapper<>();
        wrapper.eq("iccid", iccid);
        cardInformationMapper.update(cardInformation, wrapper);

        // 修改iot_ref_card_cost表
        //TODO(状态变更把用量干掉，暂时不统计)
//        IotRefCardCost iotRefCardCost = new IotRefCardCost();
//        iotRefCardCost.setUsaged(use);
//        UpdateWrapper<IotRefCardCost> wrapper2 = new UpdateWrapper<>();
//        wrapper2.eq("card_iccid", iccid);
//        wrapper2.eq("active", 1);
//        iotRefCardCostMapper.update(iotRefCardCost, wrapper2);

        return Result.success(null);
    }

    @RequestMapping("realNameStatus")
    public Result realNameStatus(@RequestBody Map<String,String> param){
        //校验数据正确性。
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("查询实名状态：{}",param);
        String iccid = param.get("iccid");
        String id = param.get("id");
        IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(iccid);
        //根据iccid,查找对应的通道
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);

        return thirdService.realNameStatus(iccid,id,templateByOperation);
    }

    @RequestMapping("getRealNameUrl")
    public Result getRealNameUrl(@RequestBody Map<String,String> param){
        //校验数据正确性。
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        String iccid = param.get("iccid");
        String url = "";
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(iccid);
        IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(iccid);
        if(thirdService instanceof OneLinkServiceImpl){
            OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
            url = oneLinkService.realNameReg(iccid, operatorTemplate);
        }
        return Result.success(url);
    }

    @RequestMapping("getCardStopReason")
    public Result getCardStopReason(String iccid){
        //校验数据正确性。
        if(StringUtil.isEmpty(iccid)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        ThirdService thirdService = iotTemplateFactory.getExecutorThridService(iccid);
        IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(iccid);
        if(thirdService instanceof OneLinkServiceImpl){
            OneLinkServiceImpl oneLinkService = (OneLinkServiceImpl)thirdService;
//            Map simStopReason = oneLinkService.getSimStopReason(iccid, operatorTemplate);
//            String stopReason = (String)simStopReason.get("stopReason");
//            if("020000000000".equals(stopReason)){
//                oneLinkService.getCardBindStatusByMsisdn(iccid)
//            }
            return Result.success(oneLinkService.getSimStopReason(iccid, operatorTemplate));
        }
        return Result.fail(CodeMsg.SERVER_ERROR);
    }

    /**
     * 同步我们平台有套餐激活的卡，在移动停机的
     * @return
     */
    @RequestMapping("syncCardStopReason")
    public Result syncCardStopReason(){

        String ip = StringUtils.getRemoteAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
        //一小时只能访问一次
        if(Frequently.isLimit(ip,3600L)){
            return Result.fail(new CodeMsg(500, "操作频繁，请稍后再试"));
        }
        cardSeparateService.syncStopReasonAndRestartCard(null);
        return Result.success("操作成功，请一分钟后查看最新数据！");
    }
}
