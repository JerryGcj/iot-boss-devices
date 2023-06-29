package com.wangxin.iot.controller.third;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ICardInformationService;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mobile.OneLinkServiceImpl;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.rest.base.CodeMsg;
import com.wangxin.iot.rest.base.Result;
import com.wangxin.iot.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("mobile")
public class MobileController {

    @Autowired
    private CardInformationMapper cardInformationMapper;
    @Autowired
    private ICardInformationService cardInformationService;
    @Autowired
    private IotTemplateFactory iotTemplateFactory;

    /**
     * 修改卡状态
     * @param param
     * @return
     */
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

    /**
     * 查询卡基本信息
     * @param param
     * @return
     */
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

        // 修改iot_card_information表
        CardInformation cardInformation = new CardInformation();
        cardInformation.setMsisdn(msisdn);
        cardInformation.setCardState(status);
        cardInformation.setActivationTime(activationTime);

        UpdateWrapper<CardInformation> wrapper = new UpdateWrapper<>();
        wrapper.eq("iccid", iccid);
        cardInformationMapper.update(cardInformation, wrapper);

        return Result.success(null);
    }
}
