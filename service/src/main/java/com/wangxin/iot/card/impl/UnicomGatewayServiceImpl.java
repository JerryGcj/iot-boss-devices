package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.IUnicomCardUsageService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.card.IotUnicomRefCardCostService;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomCardUsage;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.*;
import com.wangxin.iot.model.OrderUpstream;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.unicom.api.IoTGatewayApi;
import com.wangxin.iot.unicom.response.CommonJsonResponse;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: yanwin
 * @Date: 2020/7/20
 */
@Service
@Slf4j
public class UnicomGatewayServiceImpl implements IUnicomGatewayService {
    /**
     * 一兆
     */
    private static final BigDecimal ONE_M = new BigDecimal(1024);
    @Autowired
    IotUnicomCardUsageMapper iotUnicomCardUsageMapper;
    @Autowired
    IoTGatewayApi ioTGatewayApi;
    @Autowired
    IUnicomCardUsageService unicomCardUsageService;
    @Autowired
    IotUnicomCardInfoMapper iotUnicomCardInfoMapper;
    @Autowired
    OrderUpstreamMapper orderUpstreamMapper;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    IotUnicomRefCardCostService unicomRefCardCostService;

    @Override
    public void regulateFlow(Set<String> iccids) {
        if(CollectionUtils.isNotEmpty(iccids)){
            LocalDate now = LocalDate.now();
            String cycleStartDate = null;
            if(now.getDayOfMonth()>=27){
                cycleStartDate = DateUtils.formatDateToString(DateUtils.addMonths2Date(new Date(),1),"yyyy-MM-dd HH:mm:ss");
            }else{
                cycleStartDate = DateUtils.formatDateToString(new Date(),"yyyy-MM-dd HH:mm:ss");
            }
            Date currentDate = DateUtils.getCurrentDate();
            for (String iccid : iccids) {
                QueryWrapper<IotUnicomRefCardCost> queryWrapper = new QueryWrapper<>();
                //当前时间内的套餐
                queryWrapper.eq("iccid",iccid);
                queryWrapper.le("valid_start",currentDate).ge("valid_end",currentDate);
                List<IotUnicomRefCardCost> iotUnicomRefCardCosts = unicomRefCardCostService.list(queryWrapper);
                if(CollectionUtils.isEmpty(iotUnicomRefCardCosts)){
                    continue;
                }
                //获取在当前时间内的基础包
                Optional<IotUnicomRefCardCost> any = iotUnicomRefCardCosts.stream().filter(item -> item.getCostType() == 0).findAny();
                String finalCycleStartDate = cycleStartDate;
                any.ifPresent(cardIccid->{
                    //封装请求参数
                    HashMap map = new HashMap(2);
                    map.put("iccid",iccid);
                    map.put("cycleStartDate", finalCycleStartDate);
                    //请求结果
                    CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalUsageDataDetails(map);
                    if(commonJsonResponse != null && commonJsonResponse.getData().get("resultCode").equals("0000")){
                        Map<String, Object> result = commonJsonResponse.getData();
                        List<Map> usageDetails = (List<Map>)result.get("usageDetails");
                        //按照日期分组，按照用量求和
                        Map<String, LongSummaryStatistics> collect =
                                usageDetails.parallelStream().collect(
                                        Collectors.groupingBy(item -> item.get("sessionStartTime").toString().substring(0,10),
                                                Collectors.summarizingLong(item -> (long) item.get("dataVolume"))));
                        //更新到日用表的每日用量
                        collect.forEach((k,v)->{
                            BigDecimal usage = new BigDecimal(v.getSum()).divide(ONE_M, 3, BigDecimal.ROUND_HALF_UP);
                            LocalDate localDate = LocalDate.parse(k, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
                            int exists = iotUnicomCardUsageMapper.getCountByIccidAndDate(iccid, k);
                            if(exists > 0){
                                iotUnicomCardUsageMapper.updateUsage(usage,iccid,localDate);
                            }else{
                                IotUnicomCardUsage iotUnicomCardUsage = new IotUnicomCardUsage();
                                iotUnicomCardUsage.setCreateDate(new Date());
                                iotUnicomCardUsage.setIccid(iccid);
                                iotUnicomCardUsage.setCardUsage(usage);
                                iotUnicomCardUsage.setDate(localDate);
                                iotUnicomCardUsageMapper.insert(iotUnicomCardUsage);
                            }
                        });
                        //将同步完日用表的最新流量更新到总的流量上。
                        RefCardModel refCardModel = new RefCardModel();
                        BeanUtils.copyProperties(cardIccid,refCardModel);

                        unicomCardUsageService.syncRefUsage(refCardModel);
                    }
                });
            }
        }
    }

    @Override
    public boolean updateCommunicationPlan(Map param) {
        Map<String,Object> unicom = new HashMap<>();
        unicom.put("nacId",param.get("nacId"));
        unicom.put("iccid",param.get("iccid"));
        try {
            CommonJsonResponse commonJsonResponse = ioTGatewayApi.editNetworkAccessConfig(unicom);
            if(commonJsonResponse != null){
                Map<String, Object> data = commonJsonResponse.getData();
                if(CollectionUtils.isNotEmpty(data)){
                    String status = data.get("resultCode").toString();
                    //接口返回成功了
                    if("0000".equals(status)){
                        return true;
                    }else{
                        //6079代表卡已经是目标状态。
                        if("6079".equals(status)||"E004".equals(status)){
                            return true;
                        }
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("{} 修改卡通信计划异常，参数：{}", param, e);
        }
        return false;
    }

    @Override
    public boolean updateCardStatus(Map param) {
        Map<String,Object> unicom = new HashMap<>();
        unicom.put("changeType","3");
        unicom.put("targetValue",param.get("goalState"));
        unicom.put("iccid",param.get("iccid"));
        try {
            CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsEditTerminal(unicom);
            if(commonJsonResponse != null){
                Map<String, Object> data = commonJsonResponse.getData();
                if(CollectionUtils.isNotEmpty(data)){
                    String status = data.get("resultCode").toString();
                    //接口返回成功了
                    if("0000".equals(status)){
                        //修改数据库状态
                        IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                        iotUnicomCardInfo.setSimStatus(Integer.valueOf(param.get("goalState").toString()));
                        iotUnicomCardInfo.setUpdateTime(new Date());
                        UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                        updateWrapper.eq("iccid",param.get("iccid"));
                        iotUnicomCardInfoMapper.update(iotUnicomCardInfo,updateWrapper);
                        return true;
                    }else{
                        //1193代表卡已经是目标状态。
                        if("1193".equals(status)){
                            //修改数据库状态
                            IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                            iotUnicomCardInfo.setSimStatus(Integer.valueOf(param.get("goalState").toString()));
                            iotUnicomCardInfo.setUpdateTime(new Date());
                            if(2 == Integer.valueOf(param.get("goalState").toString())){
                                iotUnicomCardInfo.setDateActivated(new Date());
                            }
                            UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                            updateWrapper.eq("iccid",param.get("iccid"));
                            iotUnicomCardInfoMapper.update(iotUnicomCardInfo,updateWrapper);
                            return true;
                        }else{
                            if("9017".equals(status)||"1044".equals(status)){
                                return true;
                            }
                            OrderUpstream orderUpstream = new OrderUpstream();
                            //联通卡来源
                            orderUpstream.setSource(1);
                            orderUpstream.setMirror(param.get("goalState").toString());
                            orderUpstream.setIccid(param.get("iccid").toString());
                            orderUpstream.setErrorMsg(data.get("resultCode")+";"+data.get("resultDesc"));
                            //发生位置，如果不写，默认不知道（4）
                            int action = param.get("action") == null ? 4 : Integer.valueOf(param.get("action").toString());
                            orderUpstream.setAction(action);
                            orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
                        }
                    }
                }else{
                    //联通系统出现问题，譬如内部错误
                    String errorMessage = commonJsonResponse.getStatus() + "\t" +commonJsonResponse.getMessage();
                    OrderUpstream orderUpstream = new OrderUpstream();
                    //联通卡来源
                    orderUpstream.setSource(1);
                    orderUpstream.setMirror(param.get("goalState").toString());
                    orderUpstream.setIccid(param.get("iccid").toString());
                    orderUpstream.setErrorMsg(errorMessage);
                    //发生位置，如果不写，默认不知道（4）
                    int action = param.get("action") == null ? 4 : Integer.valueOf(param.get("action").toString());
                    orderUpstream.setAction(action);
                    orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
                    //发送预警短信
                    //String content = "【荀草物联】卡："+param.get("iccid").toString()+"，修改为目标状态：（"+param.get("goalState").toString()+"）时异常，请及时查看。";
                    //SendSmsUtil.sendSms(content);
                }
            }else{
                //联通系统出现问题
                String errorMessage = "调用联通修改卡状态接口异常";
                OrderUpstream orderUpstream = new OrderUpstream();
                //联通卡来源
                orderUpstream.setSource(1);
                orderUpstream.setMirror(param.get("goalState").toString());
                orderUpstream.setIccid(param.get("iccid").toString());
                orderUpstream.setErrorMsg(errorMessage);
                //发生位置，如果不写，默认不知道（4）
                int action = param.get("action") == null ? 4 : Integer.valueOf(param.get("action").toString());
                orderUpstream.setAction(action);
                orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
                //发送预警短信
                //String content = "【荀草物联】卡："+param.get("iccid").toString()+"，修改为目标状态：（"+param.get("goalState").toString()+"）时异常，请及时查看。";
                //SendSmsUtil.sendSms(content);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("参数：{}，修改卡状态异常：",param,e);
            OrderUpstream orderUpstream = new OrderUpstream();
            //联通卡来源
            orderUpstream.setSource(1);
            orderUpstream.setMirror(param.get("goalState").toString());
            orderUpstream.setIccid(param.get("iccid").toString());
            orderUpstream.setErrorMsg(e.toString());
            int action = param.get("action") == null ? 4 : Integer.valueOf(param.get("action").toString());
            orderUpstream.setAction(action);
            orderUpstreamMapper.saveOnelinkUpstream(orderUpstream);
            //发送预警短信
            //String content = "【荀草物联】卡："+param.get("iccid").toString()+"，修改为目标状态：（"+param.get("goalState").toString()+"）时异常，请及时查看。";
            //SendSmsUtil.sendSms(content);
        }
        return false;
    }

    @Override
    public void syncUsage(List<RefCardModel> refCardModel) {
        if(refCardModel.size() >= 50){
            log.info("两个小时内未接收到联通推送的流量，待同步的卡数量：{}",refCardModel.size());
            while (true){
                List<RefCardModel> collect = refCardModel.stream().limit(50).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(collect)){
                   try {
                       this.queryUnicom(collect);
                   }catch (Exception e){
                       e.printStackTrace();
                   }finally {
                    //避免死循环
                    refCardModel.removeAll(collect);
                   }
                }else{
                    break;
                }
            }
        }else{
            this.queryUnicom(refCardModel);
        }
    }
    private void queryUnicom(List<RefCardModel> refCardModel){
        List<String> iccids = refCardModel.stream().map(item -> item.getIccid()).collect(Collectors.toList());
        Map businessMap = new HashMap(1);
        businessMap.put("iccids",iccids);
        CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(businessMap);
        if(commonJsonResponse != null && "0000".equals(commonJsonResponse.getStatus())){
            Map<String, Object> result = commonJsonResponse.getData();
            List<Map> terminals = (List<Map>)result.get("terminals");
            if(CollectionUtils.isNotEmpty(terminals)){
                terminals.forEach(map-> {
                    //更新日用表
                    try {
                        unicomCardUsageService.updateUsage(map);
                    }catch (Exception e){
                        log.error("同步日用表 卡用量出现异常，{}",map);
                        //当某个卡同步usage表出现异常时，那么ref也不用同步了
                        Optional<RefCardModel> refCard = refCardModel.stream().filter(item -> item.getIccid().equals(map.get("iccid").toString())).findAny();
                        refCard.ifPresent(item->refCardModel.remove(item));
                    }
                });
            }
            refCardModel.forEach(item->{
                //同步ref
                unicomCardUsageService.syncRefUsage(item);
            });
        }
    }
    @Deprecated
    @Override
    public void syncUsaged(RefCardModel refCardModel, Date currentDate) {
        Map map = new HashMap(1);
        map.put("iccid",refCardModel.getIccid());
        //请求联通接口
        CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalUsageDataDetails(map);
        if(commonJsonResponse != null){
            Map<String, Object> result = commonJsonResponse.getData();
            List<HashMap> usageDetails = (List<HashMap>)result.get("usageDetails");
            String today = DateUtils.formatDateToString(currentDate,"yyyy-MM-dd");
            String yesterday = DateUtils.formatDateToString(DateUtils.addDays2Date(currentDate,-1),"yyyy-MM-dd");
            if(CollectionUtils.isNotEmpty(usageDetails)){
                //今天的用量
                long todayUsage = usageDetails.stream().filter(item -> item.get("sessionStartTime").toString().substring(0, 10).equals(today)).mapToLong(item -> (Long) item.get("dataVolume")).sum();
                //昨天的用量
                long yesterdayUsage = usageDetails.stream().filter(item -> item.get("sessionStartTime").toString().substring(0, 10).equals(yesterday)).mapToLong(item -> (Long) item.get("dataVolume")).sum();
                Map updateMap = new HashMap(2);
                updateMap.put(LocalDate.parse(today, DateTimeFormatter.ofPattern("yyyy-MM-dd")), new BigDecimal(todayUsage).divide(ONE_M,3,BigDecimal.ROUND_HALF_UP));
                updateMap.put(LocalDate.parse(yesterday, DateTimeFormatter.ofPattern("yyyy-MM-dd")), new BigDecimal(yesterdayUsage).divide(ONE_M,3,BigDecimal.ROUND_HALF_UP));
                //更新用量到usage表
                unicomCardUsageService.updateUsage(refCardModel.getIccid(),updateMap);
                //把usage表中的周期用量，更新到ref中
                unicomCardUsageService.syncRefUsage(refCardModel);
            }
        }
    }

    @Override
    public void realNameStatus(String iccid) {
        QueryWrapper<RealNameSystem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iccid",iccid).orderByDesc("create_time");
        List<RealNameSystem> realNameSystem = realNameSystemMapper.selectList(queryWrapper);
        if(realNameSystem.get(0).getRetryCount().intValue()<4){
            Map map = new HashMap(1);
            map.put("iccids",new String[]{iccid});
            try{
                CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                Map data = commonJsonResponse.getData();
                if(CollectionUtils.isNotEmpty(data) && data.get("resultCode").equals("0000")){
                    List<HashMap> result  = (List<HashMap>)data.get("terminals");
                    if(CollectionUtils.isNotEmpty(result)){
                        for (HashMap<String, String> maps : result) {
                            String simStatus = maps.get("simStatus");
                            String realNameStatus = maps.get("realNameStatus");
                            //已实名认证
                            if("2".equals(realNameStatus)){
                                realNameSystemMapper.editUnicomStatus(iccid);
                                //同步卡状态
                                iotUnicomCardInfoMapper.updateStatus(Integer.parseInt(simStatus),iccid);
                                //去激活套餐
                                unicomRefCardCostService.activePackage(iccid);
                            }else{
                                //增加重试次数
                                realNameSystemMapper.addCount(realNameSystem.get(0).getMsisdn());
                            }
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }
}
