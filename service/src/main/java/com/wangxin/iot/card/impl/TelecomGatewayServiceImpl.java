package com.wangxin.iot.card.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotTelecomCardInfoService;
import com.wangxin.iot.card.ITelecomCardUsageService;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.card.IotTelecomRefCardCostService;
import com.wangxin.iot.constants.TelecomCardStatusEnum;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.OrderUpstreamMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.OrderUpstream;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.telecom.api.TelecomGatewayApi;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.ParseXMLUtils;
import com.wangxin.iot.utils.StringUtils;
import com.wangxin.iot.utils.wechat.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * @author: yanwin
 * @Date: 2020/11/5
 */
@Service
@Slf4j
public class TelecomGatewayServiceImpl extends ServiceImpl<IotTelecomCardInfoMapper, IotTelecomCardInfo> implements ITelecomGatewayService {

    @Autowired
    TelecomGatewayApi telecomGatewayApi;
    @Autowired
    OrderUpstreamMapper orderUpstreamMapper;
    @Autowired
    ITelecomCardUsageService telecomCardUsageService;
    @Autowired
    IotTelecomCardInfoMapper iotTelecomCardInfoMapper;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    IIotTelecomCardInfoService telecomCardInfoService;

    /**
     * 流程6：在用--->停机
     *
     * 迁移方式：调用停机保号/复机/测试期去激活（disabledNumber）接口，参数 orderTypeId 值为 19。
     *
     *
     *
     * 7）流程7：停机 --->在用
     *
     * 迁移方式：调用停机保号/复机/测试期去激活（disabledNumber）接口，参数 orderTypeId 值为 20。
     *
     *
     * 19 表示停机保号，
     * 20 表示停机保号后复机,
     * 21 表示测试期去激活，
     * 22 表示测试期去激活后回到测试激活
     * @param reqMap
     * @return
     */
    @Override
    public boolean updateCardStatus(Map reqMap) {
        Map<String,String> reqsMap = new HashMap<>();
        reqsMap.put("method", reqMap.get("method").toString());
        reqsMap.put("access_number", reqMap.get("access_number").toString());
        if("disabledNumber".equals(reqMap.get("method").toString())){
            reqsMap.put("acctCd", "");
            reqsMap.put("orderTypeId", reqMap.get("orderTypeId").toString());
        }
        String result = telecomGatewayApi.launchReq(reqsMap);
        if(StringUtils.isNotEmpty(result)){
            try {
                //针对不同请求，处理返回结果
                this.handlerResponse(reqMap,result);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 针对不同请求，处理返回结果
     * @param reqMap
     * @param response
     * @throws Exception
     */
    private void handlerResponse(Map reqMap,String response) throws Exception {
        String method = (String)reqMap.get("method");
        // 活卡激活
        if("requestServActive".equals(method)){
            JSONObject jsonObject = JSONObject.parseObject(response);
            if("0".equals(jsonObject.getString("RESULT"))){
                if("成功".equals(jsonObject.getString("SMSG"))){
                    log.info("电信活卡激活：requestServActive 修改卡状态成功：{}",reqMap.get("access_number"));
                    //接口修改成功，修改数据库
                    QueryWrapper<IotTelecomCardInfo>  queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("access_number",reqMap.get("access_number"));
                    IotTelecomCardInfo iotTelecomCardInfo = new IotTelecomCardInfo();
                    iotTelecomCardInfo.setSimStatus(TelecomCardStatusEnum.ACTIVE.getIntCode());
                    this.iotTelecomCardInfoMapper.update(iotTelecomCardInfo,queryWrapper);
                }
            }else{
                log.info("电信活卡激活：requestServActive " +
                        "修改卡 {} 状态失败：，原因",reqMap.get("access_number"),jsonObject.getString("SMSG"));
                //把错误信息记录下来
                OrderUpstream orderUpstream = new OrderUpstream();
                //电信卡来源
                orderUpstream.setSource(2);
                //目标状态
                orderUpstream.setMirror(reqMap.get("method").toString());
                orderUpstream.setIccid(reqMap.get("access_number").toString());
                orderUpstream.setErrorMsg(jsonObject.getString("SMSG"));
                orderUpstream.setBizOrderNo(jsonObject.getString("GROUP_TRANSACTIONID"));
                //发生位置，如果不写，默认不知道（4）
                int action = reqMap.get("action") == null ? 4 : Integer.valueOf(reqMap.get("action").toString());
                orderUpstream.setAction(action);
                orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
            }
        }else{
            //判断一下返回信息是不是xml
            if(StringUtils.isXmlDocument(response)){
                //停机保号/复机/测试期去激活 操作
                Map<String, String> notifyMap = WXPayUtil.xmlToMap(response);
                String res = notifyMap.get("result");
                if("0".equals(res)){
                    String orderTypeId = reqMap.get("orderTypeId").toString();
                    log.info("电信 停机保号/复机/测试期去激活 ：disabledNumber " +
                            "修改卡状态成功：{},目标状态：{}",reqMap.get("access_number"),orderTypeId);
                    //接口修改成功，修改数据库
                    QueryWrapper<IotTelecomCardInfo>  queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("access_number",reqMap.get("access_number"));

                    IotTelecomCardInfo telecomCardInfo = new IotTelecomCardInfo();
                    //激活
                    if("20".equals(orderTypeId)){
                        telecomCardInfo.setSimStatus(TelecomCardStatusEnum.ACTIVE.getIntCode());
                    }else if("19".equals(orderTypeId)){
                        //停机
                        telecomCardInfo.setSimStatus(TelecomCardStatusEnum.CLEANED.getIntCode());
                    }
                    this.iotTelecomCardInfoMapper.update(telecomCardInfo,queryWrapper);
                }else {
                    log.info("电信 停机保号/复机/测试期去激活 ：disabledNumber" +
                            " 修改卡状态失败：{},目标状态：{}",reqMap.get("access_number"),reqMap.get("orderTypeId").toString());
                    OrderUpstream orderUpstream = new OrderUpstream();
                    //电信卡来源
                    orderUpstream.setSource(2);
                    //目标状态
                    orderUpstream.setMirror(reqMap.get("method")+"_"+reqMap.get("orderTypeId").toString());
                    orderUpstream.setIccid(reqMap.get("access_number").toString());
                    orderUpstream.setErrorMsg(notifyMap.get("resultMsg"));
                    //上游单号
                    orderUpstream.setBizOrderNo(notifyMap.get("GROUP_TRANSACTIONID"));
                    //发生位置，如果不写，默认不知道（4）
                    int action = reqMap.get("action") == null ? 4 : Integer.valueOf(reqMap.get("action").toString());
                    orderUpstream.setAction(action);
                    orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
                }
            }else{
                //不是xml，说明接口返回了错误信息
                if(!response.contains("已存在相关停机记录，无需再次办理停机")&&!response.contains("已存在停机类型为120000的停机记录")){
                    log.info("电信 停机保号/复机/测试期去激活 ：disabledNumber" +
                            " 修改卡状态失败：{},目标状态：{}",reqMap.get("access_number"),reqMap.get("orderTypeId").toString());
                    OrderUpstream orderUpstream = new OrderUpstream();
                    //电信卡来源
                    orderUpstream.setSource(2);
                    //目标状态
                    orderUpstream.setMirror(reqMap.get("method")+"_"+reqMap.get("orderTypeId").toString());
                    orderUpstream.setIccid(reqMap.get("access_number").toString());
                    orderUpstream.setErrorMsg(response);
                    //发生位置，如果不写，默认不知道（4）
                    int action = reqMap.get("action") == null ? 4 : Integer.valueOf(reqMap.get("action").toString());
                    orderUpstream.setAction(action);
                    orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
                }
            }
        }
    }
    @Override
    public String acquireAccessNumber(String iccid) {
        Map reqMap = new HashMap();
        //请求url
        reqMap.put("method","getTelephone");
        reqMap.put("iccid",iccid);
        String accessNumber = "";
        try {
            String response = this.telecomGatewayApi.launchReq(reqMap);
            if(StringUtils.isEmpty(response)){
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(response);
            String result = jsonObject.getString("RESULT");
            if("0".equals(result)){
                accessNumber = jsonObject.getString("SMSG");
                UpdateWrapper<IotTelecomCardInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("iccid",iccid);
                IotTelecomCardInfo iotTelecomCardInfo = new IotTelecomCardInfo();
                iotTelecomCardInfo.setAccessNumber(accessNumber);
                iotTelecomCardInfoMapper.update(iotTelecomCardInfo,updateWrapper);
            }
        }catch (Exception e){
            log.error("根据iccid：{}获取accessNumber发生异常{}",iccid,e);
            e.printStackTrace();
        }
        return accessNumber;
    }

    @Override
    public void syncUsage(RefCardModel refCardModel) {
        Map req = new HashMap();
        req.put("access_number",refCardModel.getAccessNumber());
        req.put("method","queryTrafficOfToday");
        String res = this.telecomGatewayApi.launchReq(req);
        if(StringUtils.isEmpty(res)){
            return;
        }
        String replace = res.replace("<root>", "").replace("</root>", "");
        try {
            Map<String, String> notifyMap = WXPayUtil.xmlToMap(replace);
            String total_bytes_cnt = notifyMap.get("TOTAL_BYTES_CNT");
            String totalUsage = total_bytes_cnt.replace("MB", "");
            Map map = new HashMap();
            map.put("totalUsage",totalUsage);
            map.put("iccid",refCardModel.getIccid());
            map.put("accessNumber",refCardModel.getAccessNumber());
            telecomCardUsageService.updateUsage(map);
        } catch (Exception e) {
            log.error("同步日用表 卡:{}用量出现异常，{}",refCardModel.getAccessNumber(),e);
            e.printStackTrace();
            //出现异常后，ref也不用同步
            return;
        }
        //同步ref
        telecomCardUsageService.syncRefUsage(refCardModel);
    }

    @Override
    public void realNameStatus(String accessNumber) {
        QueryWrapper<RealNameSystem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("msisdn",accessNumber);
        RealNameSystem realNameSystem = realNameSystemMapper.selectOne(queryWrapper);
        if(realNameSystem.getRetryCount().intValue()<4){
            Map req = new HashMap();
            req.put("access_number",accessNumber);
            req.put("method","realNameQueryIot");
            String res = this.telecomGatewayApi.launchReq(req);
            log.info("自动查询卡实名状态任务，接入号：{}，实名状态查询返回：{}",accessNumber,res);
            if(StringUtils.isEmpty(res)){
                return;
            }
            String resultCode = "";
            String prodMainStatusName = "";
            String certNumber = "";
            int simStatus = 0;
            try {
                if(StringUtils.isXmlDocument(res)){
                    List<Map<String, String>> list = ParseXMLUtils.parse(res);
                    for(int i=0;i<list.size();i++){
                        Map<String, String> map = list.get(i);
                        Iterator<String> iters = map.keySet().iterator();
                        while(iters.hasNext()){
                            String key = iters.next();
                            if(key=="resultCode"){
                                resultCode = map.get(key);
                            }else if(key=="prodMainStatusName"){
                                prodMainStatusName = map.get(key);
                            }else if(key=="certNumber"){
                                certNumber = map.get(key);
                            }
                        }
                    }
                    if("0".equals(resultCode)){
                        switch (prodMainStatusName){
                            case "可激活":
                                simStatus=1;
                                break;
                            case "测试激活":
                                simStatus=2;
                                break;
                            case "测试去激活":
                                simStatus=3;
                                break;
                            case "在用":
                                simStatus=4;
                                break;
                            case "停机":
                                simStatus=5;
                                break;
                            case "运营商管理状态":
                                simStatus=6;
                                break;
                        }
                        if(!"运营商管理状态".equals(prodMainStatusName)){
                            certNumber = certNumber.replaceAll("(\\d{6})\\d{8}(\\w{4})","$1****$2");
                            realNameSystemMapper.editStatus(certNumber,accessNumber);
                            //实名后去激活试用套餐
                            telecomCardInfoService.activePackage(accessNumber);
                        }
                        iotTelecomCardInfoMapper.updateStatus(simStatus,accessNumber);
                    }else{
                        //增加重试次数
                        realNameSystemMapper.addCount(accessNumber);
                    }
                }else{
                    //增加重试次数
                    realNameSystemMapper.addCount(accessNumber);
                }
            } catch (Exception e) {
                log.error("自动查询卡实名状态任务，接入号：{}，返回解析异常：{}",accessNumber,e);
                e.printStackTrace();
            }
        }else{
            //多次查询都未实名，将其改为驳回状态
            RealNameSystem realName = new RealNameSystem();
            realName.setStatus("2");
            realName.setId(realNameSystem.getId());
            realNameSystemMapper.updateById(realName);
        }
    }

    @Override
    public String mainStatus(Map reqMap) {
        Map newMap = new HashMap();
        newMap.put("method",reqMap.get("method"));
        newMap.put("access_number",reqMap.get("access_number"));
        String productMainStatusCd = "";
        try {
            String response = this.telecomGatewayApi.launchReq(newMap);
            if(StringUtils.isEmpty(response)){
                return null;
            }
            JSONObject jsonObject = JSONObject.parseObject(response);
            String result = jsonObject.getString("resultCode");
            String resultMsg = jsonObject.getString("resultMsg");
            if("0".equals(result)){
                String servCreateDate = jsonObject.getString("servCreateDate");
                Date activeDate = DateUtils.formatSmallStringToDate(servCreateDate);
                JSONArray jsonArray = jsonObject.getJSONArray("productInfo");
                JSONObject json = jsonArray.getJSONObject(0);
                productMainStatusCd = json.getString("productMainStatusCd");
                IotTelecomCardInfo iotTelecomCardInfo = new IotTelecomCardInfo();
                iotTelecomCardInfo.setSimStatus(Integer.parseInt(productMainStatusCd));
                iotTelecomCardInfo.setDateActivated(activeDate);
                iotTelecomCardInfoMapper.update(iotTelecomCardInfo,new UpdateWrapper<IotTelecomCardInfo>().eq("access_number", reqMap.get("access_number")));
            }else {
                log.info("电信 {}，查询卡主状态失败",reqMap.get("access_number"));
                OrderUpstream orderUpstream = new OrderUpstream();
                //电信卡来源
                orderUpstream.setSource(2);
                //目标状态
                orderUpstream.setMirror(reqMap.get("method").toString());
                orderUpstream.setIccid(reqMap.get("access_number").toString());
                orderUpstream.setErrorMsg(resultMsg);
                //上游单号
                orderUpstream.setBizOrderNo(jsonObject.getString("GROUP_TRANSACTIONID"));
                //发生位置，如果不写，默认不知道（4）
                int action = reqMap.get("action") == null ? 4 : Integer.valueOf(reqMap.get("action").toString());
                orderUpstream.setAction(action);
                orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
            }
        }catch (Exception e){
            log.error("根据access_number：{}查询卡主状态发生异常{}",reqMap.get("access_number"),e);
            e.printStackTrace();
        }
        return productMainStatusCd;
    }

    @Override
    public void handlerCallback13( Map<String, String> notifyMap) {
        String acceptMsg = notifyMap.get("ACCEPTMSG");
        String accessType = notifyMap.get("ACCEPTTYPE");
        String accessNumber = notifyMap.get("ACCNBR");
        try {
            //先查询是否有这张卡
            QueryWrapper<IotTelecomCardInfo> cardWrapper = new QueryWrapper<>();
            cardWrapper.eq("access_number", accessNumber);
            IotTelecomCardInfo card = iotTelecomCardInfoMapper.selectOne(cardWrapper);
            if(null!=card){
                //推送类型是13并且msgs是添加了单独断网。
                if("13".equals(accessType) && acceptMsg.contains("添加单独断网")){
                    Map<String,String> reqMap = new HashMap<>();
                    reqMap.put("method","singleCutNet");
                    reqMap.put("access_number",accessNumber);
                    reqMap.put("action","DEL");
                    this.telecomGatewayApi.launchReq(reqMap);
                }
                //推送类型是7 活卡激活
                if("7".equals(accessType) && acceptMsg.contains("活卡")){
                    //查询是否有支付成功的订单
                    QueryWrapper<Order> queryWrapper = new QueryWrapper<>();
                    queryWrapper.eq("access_number", accessNumber).eq("pay_state", 4).eq("operator_type", 3);
                    List<Order> lists = orderMapper.selectList(queryWrapper);
                    if(CollectionUtils.isEmpty(lists)){
                        log.info("接入号：{}，接收到活卡激活的推送，未查询到支付成功的订单，准备停机", accessNumber);
                        //没有支付成功的订单，去停机
                        Map<String,String> reqMap = new HashMap<>();
                        reqMap.put("method","disabledNumber");
                        reqMap.put("access_number",accessNumber);
                        reqMap.put("acctCd","");
                        reqMap.put("orderTypeId","19");
                        this.telecomGatewayApi.launchReq(reqMap);
                    }else{
                        //将数据库状态改为激活
                        IotTelecomCardInfo newCardInfo = new IotTelecomCardInfo();
                        newCardInfo.setSimStatus(4);
                        newCardInfo.setId(card.getId());
                        iotTelecomCardInfoMapper.updateById(newCardInfo);
                    }
                }
            }

        } catch (Exception e) {
            log.error("卡：{}处理回调异常：{}",accessNumber,e);
            e.printStackTrace();
        }
    }

    @Override
    public void setSpeedValue(Map reqMap) {
        String ids = reqMap.get("ids").toString();
        String speedValue = reqMap.get("speedValue").toString();
        QueryWrapper<IotTelecomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id", ids);
        List<IotTelecomCardInfo> cardList = iotTelecomCardInfoMapper.selectList(queryWrapper);
        String action = "ADD";
        if("31".equals(speedValue)){
            action = "DEL";
            speedValue = "10";
        }
        for(IotTelecomCardInfo cardInfo : cardList){
            Map<String,String> reqsMap = new HashMap<>(4);
            reqsMap.put("method", "speedLimitAction");
            reqsMap.put("access_number", cardInfo.getAccessNumber());
            reqsMap.put("speedValue", speedValue);
            reqsMap.put("action", action);
            String response = telecomGatewayApi.launchReq(reqsMap);
            if(StringUtils.isNotEmpty(response)){
                try {
                    JSONObject jsonObject = JSONObject.parseObject(response);
                    String result = jsonObject.getString("resultCode");
                    String resultMsg = jsonObject.getString("resultMsg");
                    if("0000".equals(result)){
                        IotTelecomCardInfo iotTelecomCardInfo = new IotTelecomCardInfo();
                        iotTelecomCardInfo.setSpeedValue(speedValue);
                        iotTelecomCardInfo.setId(cardInfo.getId());
                        iotTelecomCardInfoMapper.updateById(iotTelecomCardInfo);
                    }else {
                        log.info("电信 {}，自主限速失败",cardInfo.getAccessNumber());
                        OrderUpstream orderUpstream = new OrderUpstream();
                        //电信卡来源
                        orderUpstream.setSource(2);
                        //目标状态
                        orderUpstream.setMirror("speedLimitAction");
                        orderUpstream.setIccid(cardInfo.getAccessNumber());
                        orderUpstream.setErrorMsg(resultMsg);
                        //上游单号
                        //orderUpstream.setBizOrderNo(jsonObject.getString("GROUP_TRANSACTIONID"));
                        orderUpstream.setAction(0);
                        orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
                    }
                } catch (Exception e) {
                    log.error("卡：{}，处理自主限速返回数据异常：{}",cardInfo.getAccessNumber(),e);
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public boolean removeRealName(String accessNumber) {
        Map req = new HashMap();
        req.put("access_number",accessNumber);
        req.put("method","ordinaryRealNameClear");
        String res = this.telecomGatewayApi.launchReq(req);
        log.info("清除实名信息任务，接入号：{}，返回：{}",accessNumber,res);
        if(StringUtils.isEmpty(res)){
            return false;
        }
        try {
            JSONObject jsonObject = JSONObject.parseObject(res);
            String result = jsonObject.getString("resultCode");
            String resultMsg = jsonObject.getString("resultMsg");
            if("0".equals(result)){
                realNameSystemMapper.delete(new QueryWrapper<RealNameSystem>().eq("msisdn", accessNumber));
                return true;
            }
        } catch (Exception e) {
            log.error("卡：{}，清除实名信息异常：{}",accessNumber,e);
            e.printStackTrace();
        }
        return false;
    }
}
