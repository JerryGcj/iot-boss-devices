package com.wangxin.iot.task.xxl;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.IIotRefundRecordService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.constants.CardCostActiveEnum;
import com.wangxin.iot.constants.CardStatusEnum;
import com.wangxin.iot.constants.UnicomCardStatusEnum;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.mapper.IotUnicomRefCardCostMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.IotRefundRecord;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.task.UnicomCardCostMonitorTask;
import com.wangxin.iot.unicom.api.IoTGatewayApi;
import com.wangxin.iot.unicom.response.CommonJsonResponse;
import com.wangxin.iot.utils.DateUtils;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.cert.CertificatesManager;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.FileInputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: yanwin
 * @Date: 2021/1/20
 */
@Component
@Slf4j
public class UnicomXxlJob {

    // 商户号
    private static final String mchId = "1613718558";
    // 商户证书序列号
    private static final String mchSerialNo = "5E4197B10177EAB2724AEDA75AFD06A1B3D8032A";
    private static final String apiV3Key = "O8GNjioSp8GiCJ0xRwmCglydaIeIh5K2";
    private CloseableHttpClient httpClient;

    @Autowired
    IotUnicomCardInfoMapper iotUnicomCardInfoMapper;
    @Autowired
    IUnicomGatewayService iUnicomGatewayService;
    @Autowired
    IoTGatewayApi ioTGatewayApi;
    @Autowired
    UnicomCardCostMonitorTask unicomCardCostMonitorTask;
    @Autowired
    IotUnicomRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    IIotRefundRecordService refundRecordService;

    /**
     * 卡是激活的，但没套餐，停机
     * @param params
     * @return
     */
    @XxlJob("autoStop")
    public ReturnT autoStop(String params){
        List<String> toStopIccid = iotRefCardCostMapper.getToStopIccid();
        if(CollectionUtils.isNotEmpty(toStopIccid)){
            toStopIccid.forEach(item->{
                try{
                    XxlJobLogger.log("卡是激活状态但是没套餐的卡："+item);
                    Map businessMap = new HashMap(3);
                    //发生位置是xxl-job定时任务
                    businessMap.put("action","5");
                    businessMap.put("goalState","3");
                    businessMap.put("iccid",item);
                    //操作卡
                    iUnicomGatewayService.updateCardStatus(businessMap);
                    UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("iccid", item);
                    IotUnicomCardInfo cardInformation = new IotUnicomCardInfo();
                    cardInformation.setSimStatus(3);
                    iotUnicomCardInfoMapper.update(cardInformation, updateWrapper);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 卡自动复机
     * @param params
     * @return
     */
    @XxlJob("autoRecovery")
    public ReturnT autoRecovery(String params){
        XxlJobLogger.log("自动复机任务开始-----");
        List<Map<String,String>> unActiveCard = iotRefCardCostMapper.getUnActiveCard(0);
        if(CollectionUtils.isNotEmpty(unActiveCard)){
            for (Map<String,String> item:unActiveCard) {
                XxlJobLogger.log("符合自动复机的卡："+item);
                String iccid = item.get("iccid");
                String cardId = item.get("cardId");
                String refId = item.get("refId");
                String simStatus = String.valueOf(item.get("sim_status"));
                //卡状态不是激活，那么调用接口激活
                if(!UnicomCardStatusEnum.ACTIVE.getCode().equals(simStatus)){
                    log.info("卡号：{}已停机，有待生效的套餐，自动生效",iccid);
                    //将即将生效的套餐改为生效中
                    unicomCardCostMonitorTask.updateStatus(iccid,cardId,refId, CardStatusEnum.ACTIVATED, CardCostActiveEnum.ACTIVED);
                }else{
                    //是否有生效中的套餐，改为已失效
                    Integer activeCount = iotRefCardCostMapper.getInAdvancePackageCount(iccid, CardCostActiveEnum.ACTIVED.getActive(),0);
                    if(activeCount > 0){
                        log.info("卡号：{},状态是{}，有生效中的套餐，设置为到期失效",iccid,simStatus);
                        IotUnicomRefCardCost updateRefCardCost = new IotUnicomRefCardCost();
                        updateRefCardCost.setActive(CardCostActiveEnum.OVERTIME_DISABLED.getActive());
                        updateRefCardCost.setUpdateTime(new Date());
                        QueryWrapper<IotUnicomRefCardCost> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("iccid",iccid);
                        queryWrapper.eq("active", CardCostActiveEnum.ACTIVED.getActive());
                        iotRefCardCostMapper.update(updateRefCardCost,queryWrapper);
                    }
                    log.info("卡:{}未生效的套餐:{}改为已生效",iccid,refId);
                    //把未生效的套餐改为生效中
                    IotUnicomRefCardCost updateRefCardCost = new IotUnicomRefCardCost();
                    updateRefCardCost.setId(refId);
                    updateRefCardCost.setActive(CardCostActiveEnum.ACTIVED.getActive());
                    iotRefCardCostMapper.updateById(updateRefCardCost);
                }

            }
        }else{
            XxlJobLogger.log("没有符合自动复机的卡");
        }
        return ReturnT.SUCCESS;
    }
    /**
     * 我们平台是停机，联通平台是激活的卡，设置成停机状态
     * @param params
     * @return
     */
    @XxlJob("syncCardStatus")
    public ReturnT syncCardStatus(String params){
        if(StringUtils.isEmpty(params)){
            XxlJobLogger.log("参数为空，不执行");
            return ReturnT.SUCCESS;
        }
//        ourStatus=2,upstreamStatus=3
        String[] split = params.split(",");
        String ourStatus = split[0].split("=")[1];
        String upstreamStatus = split[1].split("=")[1];
        List<String> iccids = iotUnicomCardInfoMapper.getIccidByStatus(Integer.valueOf(ourStatus));
        Set<String> set = new HashSet<>();
        while (true){
            List<String> collect = iccids.stream().limit(50).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                try {
                    Map map = new HashMap(1);
                    map.put("iccids",collect);
                    CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                    Map data = commonJsonResponse.getData();
                    if(CollectionUtils.isNotEmpty(data) && data.get("resultCode").equals("0000")){
                        List<HashMap> result  = (List<HashMap>)data.get("terminals");
                        if(CollectionUtils.isNotEmpty(result)){
                            for (HashMap<String, String> maps : result) {
                                String simStatus = maps.get("simStatus");
                                if(simStatus.equals(upstreamStatus)){
                                    set.add(maps.get("iccid"));
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                    return ReturnT.FAIL;
                }
                finally {
                    //避免死循环
                    iccids.removeAll(collect);
                }
            }else{
                break;
            }
        }
        XxlJobLogger.log("prefect ending");
        set.forEach(item->{
            XxlJobLogger.log("状态不一致的item:"+item);
            Map businessMap = new HashMap(3);
            //发生位置是xxl-job定时任务
            businessMap.put("action","4");
            businessMap.put("goalState",upstreamStatus);
            businessMap.put("iccid",item);
            //操作卡
            iUnicomGatewayService.updateCardStatus(businessMap);
        });
        return ReturnT.SUCCESS;
    }

    /**
     * 套餐还有流量但已失效并且卡是停机状态，改成激活
     * @param
     * @return
     */
    @XxlJob("syncCardActive")
    public ReturnT syncCardActive(String params){
        List<IotUnicomRefCardCost> iccids = iotRefCardCostMapper.getIccidByActive();
        if(CollectionUtils.isNotEmpty(iccids)){
            for(IotUnicomRefCardCost refCardCost : iccids){
                QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("iccid", refCardCost.getIccid());
                IotUnicomCardInfo cardInformation = iotUnicomCardInfoMapper.selectOne(queryWrapper);
                if(cardInformation.getSimStatus()==3){
                    XxlJobLogger.log("卡号：{}，套餐还有余量但已失效，并且已停机，准备激活",refCardCost.getIccid());
                    Map businessMap = new HashMap(3);
                    //发生位置是xxl-job定时任务
                    businessMap.put("action","5");
                    businessMap.put("goalState","2");
                    businessMap.put("iccid",refCardCost.getIccid());
                    //操作卡
                    iUnicomGatewayService.updateCardStatus(businessMap);
                    IotUnicomRefCardCost cost = new IotUnicomRefCardCost();
                    cost.setActive(1);
                    cost.setId(refCardCost.getId());
                    iotRefCardCostMapper.updateById(cost);
                }
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 套餐生效中但是卡停机状态，改成激活
     * @param
     * @return
     */
    @XxlJob("syncCardActived")
    public ReturnT syncCardActived(String params){
        List<String> iccids = iotUnicomCardInfoMapper.getIccids();
        if(CollectionUtils.isNotEmpty(iccids)){
            for(String iccid : iccids){
                XxlJobLogger.log("卡号：{}，套餐是生效中，但是已停机，准备激活",iccid);
                Map businessMap = new HashMap(3);
                //发生位置是xxl-job定时任务
                businessMap.put("action","5");
                businessMap.put("goalState","2");
                businessMap.put("iccid",iccid);
                //操作卡
                iUnicomGatewayService.updateCardStatus(businessMap);
                UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                updateWrapper.eq("iccid", iccid);
                IotUnicomCardInfo cardInformation = new IotUnicomCardInfo();
                cardInformation.setSimStatus(2);
                iotUnicomCardInfoMapper.update(cardInformation, updateWrapper);
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 没有生效套餐但是卡激活，改成停机
     * @param
     * @return
     */
    @XxlJob("syncCardStop")
    public ReturnT syncCardStop(String params){
        List<String> iccids = iotUnicomCardInfoMapper.getToStopIccids();
        Date currentDate = DateUtils.getCurrentDate();
        if(CollectionUtils.isNotEmpty(iccids)){
            for(String iccid : iccids){
                QueryWrapper<IotUnicomRefCardCost> iotRefCardCostQueryWrapper = new QueryWrapper<>();
                iotRefCardCostQueryWrapper.eq("iccid", iccid);
                List<IotUnicomRefCardCost> list = iotRefCardCostMapper.selectList(iotRefCardCostQueryWrapper);
                //当前时间有生效的套餐
                List<IotUnicomRefCardCost> enableRef = list.stream().filter(item -> (item.getActive() == 0 || item.getActive() == 1) &&
                        item.getValidStart().compareTo(currentDate) <= 0 &&
                        item.getValidEnd().compareTo(currentDate) >= 0
                ).collect(Collectors.toList());
                if(CollectionUtils.isEmpty(enableRef)){
                    XxlJobLogger.log("卡号：{}，当前没有生效中的套餐，但是卡已激活，准备停机",iccid);
                    Map businessMap = new HashMap(3);
                    //发生位置是xxl-job定时任务
                    businessMap.put("action","5");
                    businessMap.put("goalState","3");
                    businessMap.put("iccid",iccid);
                    //操作卡
                    iUnicomGatewayService.updateCardStatus(businessMap);
                    UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                    updateWrapper.eq("iccid", iccid);
                    IotUnicomCardInfo cardInformation = new IotUnicomCardInfo();
                    cardInformation.setSimStatus(3);
                    iotUnicomCardInfoMapper.update(cardInformation, updateWrapper);
                }
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 定时同步卡信息
     * @return
     */
    @XxlJob("syncCardDetails")
    public ReturnT syncCardDetails(String params){
        QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sim_status", "2").isNull("first_purchase_time");
        List<IotUnicomCardInfo> iotUnicomCardInfos = iotUnicomCardInfoMapper.selectList(queryWrapper);
        if(CollectionUtils.isNotEmpty(iotUnicomCardInfos)){
            List<String> iccids = iotUnicomCardInfos.stream().map(IotUnicomCardInfo::getIccid).collect(Collectors.toList());
            while (true){
                List<String> collect = iccids.stream().limit(100).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(collect)){
                    try {
                        collect.forEach(item -> {
                            QueryWrapper<IotUnicomRefCardCost> queryWrapper1 = new QueryWrapper<>();
                            queryWrapper1.eq("iccid",item).orderByAsc("create_time");
                            List<IotUnicomRefCardCost> list = iotRefCardCostMapper.selectList(queryWrapper1);
                            if(CollectionUtils.isNotEmpty(list)){
                                IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                                iotUnicomCardInfo.setFirstPurchaseTime(list.get(0).getCreateTime());
                                UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("iccid",item);
                                iotUnicomCardInfoMapper.update(iotUnicomCardInfo,updateWrapper);
                            }
                        });
                    /*Map map = new HashMap(1);
                    map.put("iccids",collect);
                    CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                    Map data = commonJsonResponse.getData();
                    if(CollectionUtils.isNotEmpty(data) && data.get("resultCode").equals("0000")){
                        List<HashMap> result  = (List<HashMap>)data.get("terminals");
                        if(CollectionUtils.isNotEmpty(result)){
                            IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                            for (HashMap<String, String> maps : result) {
                                XxlJobLogger.log("卡号：{}，同步卡信息返回，response={}",maps.get("iccid"),maps);
                                iotUnicomCardInfo.setDateActivated(DateUtils.formatFullStringToDate(maps.get("dateActivated")));
                                UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("iccid",maps.get("iccid"));
                                iotUnicomCardInfoMapper.update(iotUnicomCardInfo,updateWrapper);
                            }
                        }
                    }*/
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    finally {
                        //避免死循环
                        iccids.removeAll(collect);
                    }
                }else{
                    break;
                }
            }
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 处理失效时间已超过当前时间但套餐还是生效并且卡是激活的
     * @param params
     * @return
     */
    @XxlJob("syncOverdueActived")
    public ReturnT syncOverdueActived(String params){
        List<IotUnicomRefCardCost> refCardCosts = iotRefCardCostMapper.getOverdueActived();
        if(CollectionUtils.isNotEmpty(refCardCosts)){
            refCardCosts.forEach(item->{
                XxlJobLogger.log("卡号：{}，有早于当前时间还生效的套餐，id={}，",item.getIccid(),item.getId());
                //把当前套餐改失效
                IotUnicomRefCardCost ref = new IotUnicomRefCardCost();
                ref.setActive(2);
                ref.setId(item.getId());
                iotRefCardCostMapper.updateById(ref);
                //看看当前时间有没有生效或待生效的套餐，有就不停机
                List<IotUnicomRefCardCost> refCardCostList = iotRefCardCostMapper.getOverdueActived(item.getIccid());
                if(CollectionUtils.isEmpty(refCardCostList)){
                    Map businessMap = new HashMap(3);
                    //发生位置是xxl-job定时任务
                    businessMap.put("action","5");
                    businessMap.put("goalState","3");
                    businessMap.put("iccid",item.getIccid());
                    //操作卡
                    iUnicomGatewayService.updateCardStatus(businessMap);
                }else{
                    //如果是待生效的套餐，改为生效并激活卡
                    if(refCardCostList.get(0).getActive()==0){
                        IotUnicomRefCardCost ref1 = new IotUnicomRefCardCost();
                        ref1.setActive(1);
                        ref1.setId(refCardCostList.get(0).getId());
                        iotRefCardCostMapper.updateById(ref1);
                        Map businessMap = new HashMap(3);
                        //发生位置是xxl-job定时任务
                        businessMap.put("action","5");
                        businessMap.put("goalState","2");
                        businessMap.put("iccid",item.getIccid());
                        //操作卡
                        iUnicomGatewayService.updateCardStatus(businessMap);
                    }
                }
            });
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 查询卡实名状态
     * @param params
     * @return
     */
    @XxlJob("unicomRealNameStatus")
    public ReturnT realNameStatus(String params){
        XxlJobLogger.log("自动查询卡实名状态任务开始-----");
        /*QueryWrapper<RealNameSystem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operator_type",2).eq("status","0");
        List<RealNameSystem> realNameSystems = realNameSystemMapper.selectList(queryWrapper);
        if(CollectionUtils.isNotEmpty(realNameSystems)){
            realNameSystems.forEach(item -> {
                XxlJobLogger.log("iccid：{}，接入号：{}，即将开始查询实名状态---",item.getIccid(),item.getMsisdn());
                iUnicomGatewayService.realNameStatus(item.getIccid());
            });
        }else{
            XxlJobLogger.log("没有未实名的卡");
        }*/
        List<String> iccids = iotUnicomCardInfoMapper.getIccidRealNameToQuery(Arrays.asList(params.split(",")));
        XxlJobLogger.log("雁飞平台未实名的卡"+iccids);
        if(CollectionUtils.isNotEmpty(iccids)){
            while (true){
                List<String> collect = iccids.stream().limit(50).collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(collect)){
                    try{
                        Map map = new HashMap();
                        map.put("iccids", collect);
                        CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                        if(commonJsonResponse!=null &&commonJsonResponse.isSuccess() && "0000".equals(commonJsonResponse.getStatus())){
                            Map<String, Object> data = commonJsonResponse.getData();
                            if(CollectionUtils.isNotEmpty(data)){
                                List<Map> list = (List<Map>)data.get("terminals");
                                for (Map terminal : list) {
                                    IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                                    JSONObject jsonObject = new JSONObject(terminal);
                                    String realNameStatus = jsonObject.getString("realNameStatus");
                                    String iccid = jsonObject.getString("iccid");
                                    Date dateActivated = jsonObject.getDate("dateActivated");
                                    //实名制通过
                                    if("2".equals(realNameStatus)){
                                        iotUnicomCardInfo.setRealNameStatus(1);
                                    }else{
                                        iotUnicomCardInfo.setRealNameStatus(0);
                                    }
                                    iotUnicomCardInfo.setDateActivated(dateActivated);
                                    UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                                    updateWrapper.eq("iccid", iccid);
                                    if(iotUnicomCardInfo.getDateActivated() != null || iotUnicomCardInfo.getRealNameStatus() != null){
                                        iotUnicomCardInfoMapper.update(iotUnicomCardInfo, updateWrapper);
                                    }
                                }
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                        return ReturnT.FAIL;
                    }finally {
                        //避免死循环
                        iccids.removeAll(collect);
                    }
                }else{
                    break;
                }
            }

        }
        return ReturnT.SUCCESS;
    }

    @XxlJob("transferRequltQuery")
    public ReturnT transferRequltQuery(String params){
        QueryWrapper<IotRefundRecord> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("refund_status",4,5,6).eq("is_new","0");
        List<IotRefundRecord> refundRecords = refundRecordService.list(queryWrapper);
        if(CollectionUtils.isNotEmpty(refundRecords)){
            refundRecords.forEach(item -> {
                IotRefundRecord newRefundRecord = new IotRefundRecord();
                try{
                    String certPath = "/root/ufi-backend/apiclient_key.pem";
                    //String certPath = "/Users/xiaojian/work/apiclient_key.pem";
                    PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(new FileInputStream(certPath));
                    // 向证书管理器增加需要自动更新平台证书的商户信息
                    CertificatesManager.getInstance().putMerchant(mchId, new WechatPay2Credentials(mchId,
                            new PrivateKeySigner(mchSerialNo, merchantPrivateKey)), apiV3Key.getBytes(StandardCharsets.UTF_8));
                    httpClient = WechatPayHttpClientBuilder.create()
                            .withMerchant(mchId, mchSerialNo, merchantPrivateKey)
                            .withValidator(new WechatPay2Validator(CertificatesManager.getInstance().getVerifier(mchId)))
                            .build();
                    URIBuilder uriBuilder = new URIBuilder("https://api.mch.weixin.qq.com/v3/transfer/batches/batch-id/"+item.getRefundId()+"?need_query_detail=true&offset=0&detail_status=ALL");
                    HttpGet httpGet = new HttpGet(uriBuilder.build());
                    httpGet.addHeader("Accept", "application/json");
                    log.info("退款单号：{}，商家转账到零钱查询转账结果请求参数：{}",item.getRefundId(),uriBuilder.build());
                    CloseableHttpResponse response = httpClient.execute(httpGet);
                    String bodyAsString = EntityUtils.toString(response.getEntity());
                    log.info("退款单号：{}，商家转账到零钱查询转账结果请求返回：{}",item.getRefundId(),bodyAsString);
                    JSONObject json = JSONObject.parseObject(bodyAsString);
                    if(bodyAsString.contains("code")||bodyAsString.contains("message")){
                        String code = json.getString("code");
                        String message = json.getString("message");
                        if("NOT_FOUND".equals(code)){
                            newRefundRecord.setRefundStatus(2);
                        }
                        newRefundRecord.setUserReceivedAccount(message);
                    }else{
                        JSONObject jsonObject = json.getJSONObject("transfer_batch");
                        String outBatchNo = jsonObject.getString("out_batch_no");
                        String batchStatus = jsonObject.getString("batch_status");
                        String closeReason = jsonObject.getString("close_reason");
                        String updateTime = jsonObject.getString("update_time");
                        if("WAIT_PAY".equals(batchStatus)){
                            //待付款，商户员工确认付款阶段
                            newRefundRecord.setRefundStatus(6);
                            newRefundRecord.setUserReceivedAccount("待商户员工确认付款");
                        }
                        if("ACCEPTED".equals(batchStatus)){
                            //已受理。批次已受理成功，若发起批量转账的30分钟后，转账批次单仍处于该状态，可能原因是商户账户余额不足等
                            newRefundRecord.setRefundStatus(4);
                            Date date = new Date();
                            int min = DateUtils.minutesBetween(item.getUpdateTime(),date);
                            if(min>30){
                                newRefundRecord.setRefundStatus(5);
                                newRefundRecord.setUserReceivedAccount("转账结果查询异常，请登录商户平台确认");
                            }
                        }
                        if("FINISHED".equals(batchStatus)){
                            //已完成。批次内的所有转账明细单都已处理完成
                            Date time = DateUtils.getDateByRFC3339(updateTime);
                            newRefundRecord.setRefundStatus(1);
                            newRefundRecord.setUserReceivedAccount("退款成功");
                            newRefundRecord.setSuccessTime(time);
                            //后续流程自动化处理
                            if("1".equals(item.getAutomation())){
                                refundRecordService.automation(item);
                            }
                        }
                        if("CLOSED".equals(batchStatus)){
                            //已关闭。可查询具体的批次关闭原因确认
                            newRefundRecord.setRefundStatus(2);
                            newRefundRecord.setUserReceivedAccount(closeReason);
                        }
                    }
                    newRefundRecord.setId(item.getId());
                    refundRecordService.updateById(newRefundRecord);
                }catch (Exception e){
                    e.printStackTrace();
                    newRefundRecord.setRefundStatus(5);
                    newRefundRecord.setUserReceivedAccount("转账结果查询异常，请联系技术");
                    newRefundRecord.setId(item.getId());
                    refundRecordService.updateById(newRefundRecord);
                }
            });
        }
        return ReturnT.SUCCESS;
    }
}
