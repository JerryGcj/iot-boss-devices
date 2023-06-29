package com.wangxin.iot.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.constants.ResponseConstants;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.SysUserMapper;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotRefCardCost;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.SysUser;
import com.wangxin.iot.model.api.card.CardResponse;
import com.wangxin.iot.model.api.card.Response3;
import com.wangxin.iot.model.api.orderList.OrderListResponse;
import com.wangxin.iot.model.api.orderList.Response1;
import com.wangxin.iot.model.api.orderList.SonOrderResponse;
import com.wangxin.iot.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 查询卡信息接口
 */
@Controller
@RequestMapping("/api/v1.0/query")
@Slf4j
public class QueryCardController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private IotRefCardCostMapper refCardCostMapper;
    @Autowired
    private CardInformationMapper cardInformationMapper;

    @RequestMapping(value = "/cardInfo")
    @ResponseBody
    public CardResponse query(@RequestBody String request){
        log.info("卡信息查询接口请求报文{}", request);
        CardResponse result = new CardResponse();
        try{
            Map<String, String> map = JsonUtil.parseRequestJson(request);
            if(null==map||map.isEmpty()){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("卡信息查询接口返回{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }
            String traderId = map.get("traderId");
            String iccid = map.get("iccid");
            String timeStamp = map.get("timeStamp");
            String sign = map.get("sign");
            Date now = new Date();
            //判断用户名是否为空
            if(StringUtils.isBlank(traderId)){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(Frequently.isLimit(traderId)){
                System.out.println("订单查询频繁");
                result.setCode(ResponseConstants.FAIL_CODE16.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE16.getMsg());
                return result;
            }
            //判断卡号是否符合规格
            if(StringUtils.isBlank(iccid)){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }
            //判断时间戳是否为空 && 判断时间戳是否符合格式
            if(StringUtils.isBlank(timeStamp)){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }else{
                try {
                    long time = Math.abs(now.getTime()/1000 - Long.parseLong(timeStamp));
                    if(time > 300){//5分钟以内
                        result.setCode(ResponseConstants.FAIL_CODE14.getCode());
                        result.setMsg(ResponseConstants.FAIL_CODE14.getMsg());
                        log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE14.getCode(), ResponseConstants.FAIL_CODE14.getMsg());
                        return result;
                    }
                } catch (Exception e) {
                    result.setCode(ResponseConstants.FAIL_CODE15.getCode());
                    result.setMsg(ResponseConstants.FAIL_CODE15.getMsg());
                    log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE15.getCode(), ResponseConstants.FAIL_CODE15.getMsg());
                    return result;
                }
            }
            //判断签名是否为空
            if(StringUtils.isBlank(sign)){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }
            QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", traderId);
            SysUser user = sysUserMapper.selectOne(userQueryWrapper);
            if(null==user){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(user.getStatus()==2||user.getDelFlag()==1){
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE5.getCode(), ResponseConstants.FAIL_CODE5.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE5.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE5.getMsg());
                return result;
            }
            //验证签名是否一致
            map.remove("sign");
            String signStr = HttpClientHelper.getParamsByAscending(map);
            String thisSign = MD5Util.md5Encrypt32Lower(signStr+user.getTheKey());
            if(!thisSign.equals(sign)){
                log.info("卡信息查询接口签名源串："+signStr+user.getTheKey());
                log.info("卡信息查询接口签名："+thisSign);
                result.setCode(ResponseConstants.FAIL_CODE3.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE3.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE3.getCode(), ResponseConstants.FAIL_CODE3.getMsg());
                return result;
            }
            //校验IP
            String ip = com.wangxin.iot.utils.StringUtils.getRemoteAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            String ips = user.getIpWhite();
            if(StringUtils.isBlank(ips)||!ips.contains(ip)){
                result.setCode(ResponseConstants.FAIL_CODE9.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE9.getMsg());
                log.info("卡信息查询接口返回{}{}", ip, ResponseConstants.FAIL_CODE9.getMsg());
                return result;
            }
            //参数校验通过，去数据库查询订单
            QueryWrapper<CardInformation> cardInfoQueryWrapper = new QueryWrapper<>();
            cardInfoQueryWrapper.eq("iccid", iccid).eq("customer_id", user.getId());
            CardInformation cardInformation = cardInformationMapper.selectOne(cardInfoQueryWrapper);
            if(null==cardInformation){
                result.setCode(ResponseConstants.FAIL_CODE2.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE2.getMsg());
                log.info("卡信息查询接口返回{}{}", ResponseConstants.FAIL_CODE2.getCode(), ResponseConstants.FAIL_CODE2.getMsg());
                return result;
            }
            QueryWrapper<IotRefCardCost> refCardCostQueryWrapper = new QueryWrapper<>();
            refCardCostQueryWrapper.eq("card_iccid", iccid).eq("active", 1);
            IotRefCardCost refCardCost = refCardCostMapper.selectOne(refCardCostQueryWrapper);
            Response3 resp = new Response3();
            if(null==refCardCost){
                resp.setFlowTotal(new BigDecimal("0"));
                resp.setFlowUsed(new BigDecimal("0"));
                resp.setFlowLeft(new BigDecimal("0"));
            }else{
                BigDecimal flowTotal = refCardCost.getOriginUse();
                BigDecimal flowUsed = refCardCost.getUsaged().multiply(cardInformation.getCustomPackageUse()).setScale(3, BigDecimal.ROUND_HALF_UP);
                BigDecimal flowLeft = flowTotal.subtract(flowUsed).setScale(3, BigDecimal.ROUND_HALF_UP);
                resp.setFlowTotal(flowTotal);
                resp.setFlowUsed(flowUsed);
                resp.setFlowLeft(flowLeft);
            }
            Date activationTime = cardInformation.getActivationTime();
            Date overTime = cardInformation.getLoseEfficacyTime();
            int deviceOnOffState = 1;
            String msisdn = "";
            String imsi = "";
            String imei = "";
            if(null==activationTime){
                resp.setActivationTime("");
            }else{
                resp.setActivationTime(DateUtils.formatDateToFullString(cardInformation.getActivationTime()));
            }
            if(null==overTime){
                resp.setOverTime("");
            }else{
                resp.setOverTime(DateUtils.formatDateToFullString(cardInformation.getLoseEfficacyTime()));
            }
            if(StringUtils.isNotBlank(cardInformation.getDeviceOnOffState())){
                deviceOnOffState = Integer.parseInt(cardInformation.getDeviceOnOffState());
            }
            if(StringUtils.isNotBlank(cardInformation.getMsisdn())){
                msisdn = cardInformation.getMsisdn();
            }
            if(StringUtils.isNotBlank(cardInformation.getImsi())){
                imsi = cardInformation.getImsi();
            }
            if(StringUtils.isNotBlank(cardInformation.getImei())){
                imei = cardInformation.getImei();
            }
            resp.setIccid(iccid);
            resp.setMsisdn(msisdn);
            resp.setImsi(imsi);
            resp.setImei(imei);
            resp.setStatus(Integer.parseInt(cardInformation.getCardState()));
            resp.setOnLineStatus(1);
            resp.setOnOffStatus(deviceOnOffState);
            result.setCode(ResponseConstants.SUCCESS_CODE.getCode());
            result.setMsg(ResponseConstants.SUCCESS_CODE.getMsg());
            result.setData(resp);
            log.info("卡信息查询接口返回{}", result);
            return result;
        }catch (Exception e){
            result.setCode(ResponseConstants.FAIL_CODE20.getCode());
            result.setMsg(ResponseConstants.FAIL_CODE20.getMsg());
            log.error("卡信息查询接口异常{}", e);
            return result;
        }
    }
}
