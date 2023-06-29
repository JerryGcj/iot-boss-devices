package com.wangxin.iot.config.event;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.IIotUnicomCardInfoService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.unicom.api.IoTGatewayApi;
import com.wangxin.iot.unicom.response.CommonJsonResponse;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author: yanwin
 * @Date: 2020/5/25
 * @Desc: 刷新卡详情
 */
@Slf4j
@Component
public class NeedSyncDetailsReceiver {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    IoTGatewayApi ioTGatewayApi;
    @Autowired
    IIotUnicomCardInfoService iotUnicomCardInfoService;

    public void receiveMessage(String flag){
       if("1".equals(flag)){
           log.info("联通卡接收到数据更新请求");
           while (true){
               List list = redisUtil.setBatchPop(RedisKeyConstants.NEED_SYNC_CARD.getMessage(), 50);
               if (CollectionUtils.isNotEmpty(list)){
                   Map map = new HashMap(1);
                   map.put("iccids",list);
                   try {
                       CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                       Map data = commonJsonResponse.getData();
                       if(CollectionUtils.isNotEmpty(data) && data.get("resultCode").equals("0000")){
                           List<HashMap> result  = (List<HashMap>)data.get("terminals");
                           if(CollectionUtils.isNotEmpty(result)){
                               IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                               for (HashMap<String, String> maps : result) {
                                   //md,这个字段有问题，先不搞了
                                   //iotUnicomCardInfo.setRatePlan(maps.get("ratePlan"));
                                   iotUnicomCardInfo.setCustomer(maps.get("Customer"));
                                   iotUnicomCardInfo.setDeviceId(maps.get("deviceId"));
                                   iotUnicomCardInfo.setOverageLimitOverride(maps.get("overageLimitOverride"));
                                   iotUnicomCardInfo.setAccountId(maps.get("accountId"));
                                   iotUnicomCardInfo.setOperatorAccountId(maps.get("operatorAccountId"));
                                   iotUnicomCardInfo.setMonthToDateUsage(new BigDecimal(maps.get("monthToDateUsage")));
                                   iotUnicomCardInfo.setOverageLimitReached(new BigDecimal(maps.get("overageLimitReached")));
                                   iotUnicomCardInfo.setMsisdn(maps.get("msisdn"));
                                   if("2".equals(maps.get("realNameStatus"))){
                                       iotUnicomCardInfo.setRealNameStatus(1);
                                   }else{
                                       iotUnicomCardInfo.setRealNameStatus(0);
                                   }
                                   iotUnicomCardInfo.setImsi(maps.get("imsi"));
                                   iotUnicomCardInfo.setImei(maps.get("imei"));
                                   iotUnicomCardInfo.setSimStatus(Integer.valueOf(maps.get("simStatus")));
                                   iotUnicomCardInfo.setDateActivated(DateUtils.formatFullStringToDate(maps.get("dateActivated")));
                                   UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                                   updateWrapper.eq("iccid",maps.get("iccid"));
                                   iotUnicomCardInfoService.update(iotUnicomCardInfo,updateWrapper);
                               }
                           }
                       }
                   }catch (Exception e){
                       e.printStackTrace();
                       log.error("联通数据更新接口报错，本次卡数量：{}",list.size());
                       //这里有可能请求异常，在重新放回去，5分钟后在请求
                       redisUtil.sBatchSet(RedisKeyConstants.NEED_SYNC_CARD.getMessage(),list);
                       try {
                           TimeUnit.SECONDS.sleep(5*60);
                       } catch (InterruptedException e1) {
                           e1.printStackTrace();
                       }
                   }
               }else{
                    break;
               }
           }
       }
    }
}
