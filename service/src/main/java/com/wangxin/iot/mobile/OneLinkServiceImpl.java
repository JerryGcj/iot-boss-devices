package com.wangxin.iot.mobile;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.wangxin.iot.card.ICardInformationService;
import com.wangxin.iot.card.IOrderPackageService;
import com.wangxin.iot.constants.CardStatusEnum;
import com.wangxin.iot.constants.RealNameStatus;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.OrderUpstreamMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.*;
import com.wangxin.iot.model.third.hu.OneLinkApiConfig;
import com.wangxin.iot.rest.base.CodeMsg;
import com.wangxin.iot.rest.base.Result;
import com.wangxin.iot.utils.OneLinkUtils;
import com.wangxin.iot.utils.StringUtil;
import com.wangxin.iot.utils.StringUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author: yanwin
 * @Date: 2020/3/16
 */
@Service
@Slf4j
public class OneLinkServiceImpl extends AbstractThirdService{
    /**
     * token过期时间，设置55分钟
     */
    private long tokenOverTime = 55*60*1;
    private static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ICardInformationService cardInformationService;
    @Autowired
    OrderUpstreamMapper orderUpstreamMapper;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    private CardInformationMapper cardInformationMapper;
    @Autowired
    IOrderPackageService orderPackageService;
    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 多个运营商公用一个通道，token不同，
     * 根据appid从redis取，如果没有，则请求上游并缓存到redis
     * 采用了hash结构
     * @param templateByType
     * @return
     */
    @Override
    public String getToken(IotOperatorTemplate templateByType){
        OneLinkApiConfig oneLinkApiConfig = JSON.parseObject(templateByType.getTemplate(),OneLinkApiConfig.class);
        String appid = oneLinkApiConfig.getAppId();
        String token = (String) redisUtil.hget(RedisKeyConstants.ONE_LINK_TOKEN.getMessage()+"_"+appid, appid);
        if(StringUtil.isNotEmpty(token)){
            return token;
        }
        //调用上游接口，获取token
        Map<String,String> reqMap = new LinkedHashMap<>();
        reqMap.put("url",oneLinkApiConfig.getTokenUrl());
        reqMap.put("version",oneLinkApiConfig.getVersion());
        reqMap.put("apiName","/ec/get/token");
        reqMap.put("appid",appid);
        reqMap.put("password",oneLinkApiConfig.getPassword());
        reqMap.put("transid",OneLinkUtils.getTransid(appid));
        //获取token不刷新token
        reqMap.put("refresh","0");
        String url = OneLinkUtils.buildUrl(reqMap);
        Map call = this.restTemplate.getForObject(url, Map.class);
        if("0".equals(call.get("status"))){
            List result = (List)call.get("result");
            Map<String,String> tokens = (Map)result.get(0);
            token = tokens.get("token");
            long ttl = Long.valueOf(String.valueOf(tokens.get("ttl")));
            log.info("oneLink获取token成功，token:{},ttl:{}",token,ttl);
            redisUtil.hset(RedisKeyConstants.ONE_LINK_TOKEN.getMessage()+"_"+appid,appid,token,ttl);
            return token;
        }
        return "";
    }
    @Override
    public Result placeOrderCost(Map<String, String> reqParam, IotOperatorTemplate iotOperatorTemplate) {
        //库存转激活
        boolean flag = this.modifyCard(reqParam, iotOperatorTemplate);
        if(flag){
            //修改db中的卡状态激活
            cardInformationService.updateCardStatus("3",reqParam.get("iccid"));
            log.info("激活卡{}成功，修改db卡状态为已激活",reqParam.get("iccid"));
            return Result.success(null);
        }else{
            log.info("激活卡{}失败，目标状态：{}",reqParam.get("iccid"),reqParam.get("status"));
        }
        return Result.fail(new CodeMsg(1,"订购失败"));
    }

    @Override
    public Map sendReq(String iccid, IotOperatorTemplate iotOperatorTemplate) {
        Map reqMap = new HashMap();
        reqMap.put("iccid",iccid);
        reqMap.put("apiName","/ec/query/sim-basic-info");
        Map call = this.call(reqMap,iotOperatorTemplate);
        log.info(call.toString());
        String status = call.get("status").toString();
        //token过期
        if("12021".equals(status)){
            refreshToken(iotOperatorTemplate);
        }
        Map<String, Object> resultMap = new HashMap<>();
        if (!"0".equals(status)) {
            resultMap.put("error", status);
            resultMap.put("message", call.get("message").toString());
            return resultMap;
        }
        resultMap.put("error", "");
        List result = (List) call.get("result");
        Map<String, String> map2 = (Map) result.get(0);
        String msisdn = map2.get("msisdn");
        String activeDate = map2.get("activeDate");
        resultMap.put("msisdn", msisdn);
        if (StringUtil.isNotEmpty(activeDate)) {
            try {
                resultMap.put("activationTime", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(activeDate));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

//        resultMap.put("data", getUsaged(iccid, iotOperatorTemplate).toString());
        resultMap.put("status",this.getCardStatus(iccid,iotOperatorTemplate));
        return resultMap;
    }

    /**
     * operType:  0:申请停机(已激活转已停机),1:申请复机(已停机转已激活),2:库存转已激活,3:可测试转库存,4:可测试转待激活,5:可测试转已激活,6:待激活转已激活
     * @param reqParam
     * @param iotOperatorTemplate
     * @return
     */
    @Override
    public boolean modifyCard(Map<String, String> reqParam, IotOperatorTemplate iotOperatorTemplate) {
        String status = reqParam.get("status");
        Map<String, String> param = new HashMap<>();
        String operType;
        switch (status) {
            case "3":
                operType = "1";
                break;
            case "4":
                operType = "0";
                break;
            case "6":
                operType = "6";
                break;
            case "5":
                operType = "5";
                break;
            case "test->repo":
                operType = "3";
                break;
            default:
                operType = "2";
        }
        param.put("iccid",reqParam.get("iccid"));
        param.put("apiName","/ec/change/sim-status");
        param.put("operType", operType);
        Map call = this.call(param,iotOperatorTemplate);
        if(CollectionUtils.isEmpty(call)){
            return false;
        }
        //token过期
        if("12021".equals(call.get("status"))){
            refreshToken(iotOperatorTemplate);
        }
        if("0".equals(call.get("status"))){
            return true;
        } else{
            //将错误日志记录下来
            OrderUpstream orderUpstream = new OrderUpstream();
            orderUpstream.setSource(0);
            String mirror = status+";"+operType;
            orderUpstream.setMirror(mirror);
            orderUpstream.setAction(Integer.valueOf(reqParam.get("action")));
            orderUpstream.setIccid(reqParam.get("iccid"));
            //初始化
            orderUpstream.setStatus(3);
            orderUpstream.setErrorMsg(call.get("status")+"\t"+call.get("message").toString());
            if(StringUtils.isEmpty(reqParam.get("id"))){
                orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
            }else{
                //定时任务再次调用失败，则更新错误信息
                orderUpstream.setId(reqParam.get("id"));
                orderUpstream.setUpdateTime(new Date());
                orderUpstreamMapper.updateById(orderUpstream);
            }
        }
        return false;
    }

    @Override
    public Result realNameStatus(String iccid, String id, IotOperatorTemplate iotOperatorTemplate) {
        try{
            Map map = new HashMap();
            map.put("apiName","/ec/query/sim-real-name-status");
            map.put("iccid",iccid);
            Map call = this.call(map, iotOperatorTemplate);
            //token过期
            if("12021".equals(call.get("status"))){
                refreshToken(iotOperatorTemplate);
            }
            if("11011".equals(call.get("status"))){
                return Result.fail(new CodeMsg(2,"移动平台无此卡，请确认！"));
            }
            List result = (List) call.get("result");
            Map map2 = (Map) result.get(0);
            String realNameStatus = map2.get("realNameStatus").toString();
            //已实名
            if("1".equals(realNameStatus)){
                /*RealNameSystem realNameSystem = new RealNameSystem();
                realNameSystem.setStatus("1");
                realNameSystemMapper.update(realNameSystem,new UpdateWrapper<RealNameSystem>().eq("iccid",iccid));*/
                //查询处理中的订单
                QueryWrapper<Order> orderQueryWrapper = new QueryWrapper<>();
                orderQueryWrapper.eq("iccid",iccid).eq("pay_state", 4).eq("order_state", 2).eq("operator_type", 1);
                List<Order> orderList = orderMapper.selectList(orderQueryWrapper);
                if(!CollectionUtils.isEmpty(orderList)){
                    orderList.forEach(item -> {
                        String str = (String) redisUtil.hget(RedisKeyConstants.UN_REAL_NAME_ORDER.getMessage(), iccid+"_"+item.getId());
                        if(StringUtils.isNotBlank(str)){
                            redisUtil.hdel(RedisKeyConstants.UN_REAL_NAME_ORDER.getMessage(), iccid+"_"+item.getId());
                            orderPackageService.wechatOrderPackage(item);
                        }else{
                            log.info("根据卡号：{}获取到的实名认证信息为空",iccid+"_"+item.getId());
                        }
                    });
                }
                //查询卡信息
                QueryWrapper<CardInformation> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("iccid", iccid);
                CardInformation card = cardInformationMapper.selectOne(queryWrapper);
                if(null!=card){
                    String cardState = card.getCardState();
                    if("1".equals(cardState)){
                        log.info("iccid:{} 实名制通过，卡是沉默期，设置成激活",iccid);
                        //将卡状态改为已激活
                        CardInformation cardInformation = new CardInformation();
                        cardInformation.setCardState("3");
                        cardInformation.setIsRealNameAuthentication(realNameStatus);
                        cardInformation.setActivationTime(new Date());
                        cardInformationMapper.update(cardInformation, new UpdateWrapper<CardInformation>().eq("iccid",iccid));
                    }else{
                        log.info("iccid:{} 实名制通过，卡是{}状态，不处理",iccid,cardState);
                        cardInformationMapper.update(new CardInformation().setIsRealNameAuthentication(realNameStatus), new UpdateWrapper<CardInformation>().eq("iccid",iccid));
                    }
                }
                return Result.success(RealNameStatus.SUCC.getDesc());
            }
        }catch (Exception e){
            log.error(iccid+" 调用移动接口查询实名状态异常：", e);
            return Result.fail(new CodeMsg(1,"查询异常！"));
        }

        return Result.fail(new CodeMsg(1,"卡未实名！"));
    }

    public BigDecimal getUsaged(String iccid,IotOperatorTemplate iotOperatorTemplate){
        Map map = new HashMap();
        map.put("iccid",iccid);
        //这些接口暂不使用
        //group-data-usage
//        map.put("apiName","/ec/query/sim-data-usage-daily/batch");
//        map.put("apiName","/ec/query/sim-data-margin");
        map.put("apiName","/ec/query/sim-data-usage");
        Map call = this.call(map, iotOperatorTemplate);
        if(CollectionUtils.isEmpty(call)){
            return null;
        }
        String status = String.valueOf(call.get("status"));
        //token过期
        if("12021".equals(status)){
            refreshToken(iotOperatorTemplate);
        }

        if("0".equals(status)){
            List result = (List) call.get("result");
            Map map2 = (Map) result.get(0);
            return new BigDecimal(map2.get("dataAmount").toString()).divide(new BigDecimal("1024")).setScale(3,BigDecimal.ROUND_HALF_UP);
        }
        return null;
    }

    /**
     * 获取卡的修改历史
     * @param iccid
     * @param iotOperatorTemplate
     * @return
     */
    public Map getChangeHistory(String iccid,IotOperatorTemplate iotOperatorTemplate){
        Map map = new HashMap();
        map.put("iccid",iccid);
        map.put("apiName","/ec/query/sim-change-history");
        Map call = this.call(map, iotOperatorTemplate);
        System.out.println("call"+call);
        return call;
    }

    /**
     * 获取流量池的用量
     * @param groupId
     * @param iotOperatorTemplate
     * @return
     */
    public Map getFlowPoolUsage(String groupId,IotOperatorTemplate iotOperatorTemplate){
        Map map = new HashMap();
        map.put("groupId",groupId);
        map.put("apiName","/ec/query/group-data-margin");
        Map call = this.call(map, iotOperatorTemplate);
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return call;
    }

    /**
     * 风险防控
     * @param iotOperatorTemplate
     * @return
     */
    public Map riskSceneCardList(IotOperatorTemplate iotOperatorTemplate,String msisdn){
        Map map = new HashMap();
        map.put("apiName","/ec/query/sim-region-limit-area");
        map.put("msisdn",msisdn);
        Map call = this.call(map, iotOperatorTemplate);
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return call;
    }
    public String getCardStatus(String iccid,IotOperatorTemplate iotOperatorTemplate){
        String cardStatus = null;
        Map map = new HashMap();
        map.put("iccid",iccid);
        map.put("apiName","/ec/query/sim-status");

        Map call = this.call(map, iotOperatorTemplate);

        if(!CollectionUtils.isEmpty(call) && "0".equals(call.get("status"))){
            List  result = (List)call.get("result");
            Map finalResult = (Map)result.get(0);
            cardStatus = CardStatusEnum.getOwnCodeWithOnelink(Integer.valueOf(finalResult.get("cardStatus").toString()));
        }
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return cardStatus;
    }

    /**
     * 根据msisdn,查询卡状态
     * @param msisdn
     * @param iotOperatorTemplate
     * @return
     */
    public Map getCardStatusByMsisdn(String msisdn,IotOperatorTemplate iotOperatorTemplate){
        Map map = new HashMap();
        map.put("msisdn",msisdn);
        map.put("apiName","/ec/query/sim-status");
        Map call = this.call(map, iotOperatorTemplate);
        if(!CollectionUtils.isEmpty(call) && "0".equals(call.get("status"))){
            List  result = (List)call.get("result");
            return (Map)result.get(0);
        }
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return null;
    }
    /**
     * 操作类型：
     * 1 可测试>库存；
     * 2可测试>待激活；
     * 3可测试>已激活；
     * 4库存>待激活
     * 5库存>已激活
     * 6 待激活>库存
     * 7待激活>已激活
     * 8 待激活>已停机（暂不支持）
     * 9已激活>已停机
     * 10已停机>待激活（暂不支持）
     * 11 已停机>已激活
     * @param msisdns
     */
    @Deprecated
    public void batchModifyCard(List<String> msisdns,IotOperatorTemplate iotOperatorTemplate){
        if(CollectionUtils.isEmpty(msisdns)){
            return;
        }
        String join = String.join("_", msisdns);
        Map map = new HashMap();
        map.put("msisdns",join);
        map.put("apiName","/ec/change/sim-status/batch");
        map.put("operType","9");
        map.put("reason","01");
        this.call(map,iotOperatorTemplate);
    }

    public String realNameReg(String iccid,IotOperatorTemplate iotOperatorTemplate){
        String url = null;
        Map map = new HashMap();
        map.put("iccid",iccid);
        map.put("apiName","/ec/secure/sim-real-name-reg");

        Map call = this.call(map, iotOperatorTemplate);

        if(!CollectionUtils.isEmpty(call) && "0".equals(call.get("status"))){
            List  result = (List)call.get("result");
            Map finalResult = (Map)result.get(0);
            url = finalResult.get("url").toString();
        }
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return url;
    }

    /**
     * 查询是否卡机卡分离
     * @param msisdn
     * @param iotOperatorTemplate
     * @return
     */
    public Map getCardBindStatusByMsisdn(String msisdn,IotOperatorTemplate iotOperatorTemplate){
        Map map = new HashMap(3);
        map.put("msisdn",msisdn);
        map.put("testType","0");
        map.put("apiName","/ec/query/card-bind-status");
        Map call = this.call(map, iotOperatorTemplate);
        if(!CollectionUtils.isEmpty(call) && "0".equals(call.get("status"))){
            List  result = (List)call.get("result");
            return (Map)result.get(0);
        }
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return null;
    }
    /**
     * 查询是否卡机卡分离
     * @param iccid
     * @param iotOperatorTemplate
     * @return
     */
    public Map getSimStopReason(String iccid,IotOperatorTemplate iotOperatorTemplate){
        Map map = new HashMap(3);
        map.put("iccid",iccid);
        map.put("testType","0");
        map.put("apiName","/ec/query/sim-stop-reason");
        Map call = this.call(map, iotOperatorTemplate);
        if(!CollectionUtils.isEmpty(call) && "0".equals(call.get("status"))){
            List  result = (List)call.get("result");
            return (Map)result.get(0);
        }
        //token过期
        if("12021".equals(call.get("status"))){
            this.getToken(iotOperatorTemplate);
        }
        return null;
    }
    public void refreshToken(IotOperatorTemplate iotOperatorTemplate){
        OneLinkApiConfig oneLinkApiConfig = JSON.parseObject(iotOperatorTemplate.getTemplate(),OneLinkApiConfig.class);
        String appId = oneLinkApiConfig.getAppId();
        redisUtil.hdel(RedisKeyConstants.ONE_LINK_TOKEN.getMessage()+"_"+appId, appId);
    }
}
