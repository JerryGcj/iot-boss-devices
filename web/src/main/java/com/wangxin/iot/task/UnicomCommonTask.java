package com.wangxin.iot.task;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.IUnicomCardUsageService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.mapper.IotUnicomRefCardCostMapper;
import com.wangxin.iot.mapper.OrderUpstreamMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.OrderUpstream;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.unicom.api.IoTGatewayApi;
import com.wangxin.iot.unicom.response.CommonJsonResponse;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 联通卡的常规监控
 * @author  by 18765 on 2020/1/8 10:11
 */
@Component
@Slf4j
public class UnicomCommonTask {

    @Autowired
    IotUnicomRefCardCostMapper iotUnicomRefCardCostMapper;
    @Autowired
    IUnicomGatewayService unicomGatewayService;

    @Autowired
    IUnicomCardUsageService cardUsageService;
    @Autowired
    IotUnicomCardInfoMapper iotUnionCardInfoMapper;
    @Autowired
    OrderUpstreamMapper orderUpstreamMapper;
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    IoTGatewayApi ioTGatewayApi;
    /**
     * 10分钟执行一次，查询联通实名制未通过的卡
     */
    //@Scheduled(initialDelay = 1000*60*60,fixedDelay = 1000*60*10)
    public void realNameQuery(){
        List<String> iccids = realNameSystemMapper.iccids();
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
                                String realNameStatus = maps.get("realNameStatus");
                                //已实名认证
                                if("2".equals(realNameStatus)){
                                    RealNameSystem realNameSystem = new RealNameSystem();
                                    realNameSystem.setStatus("1");
                                    realNameSystemMapper.update(realNameSystem,new UpdateWrapper<RealNameSystem>().eq("iccid",maps.get("iccid")));
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    //避免死循环
                    iccids.removeAll(collect);
                }
            }else{
                break;
            }
        }

    }
    /**
     *同步卡用量，同步完成后，将总用量同步到ref中
     */
    @Scheduled(initialDelay = 1000*60*15,fixedDelay = 1000*60*60)
    public void syncUsage(){
        //2小时之内，没有接收过推送的卡
        List<RefCardModel> refModel = iotUnionCardInfoMapper.getSyncRefModel( 2);
        Map<String, List<RefCardModel>> collect = refModel.stream().collect(Collectors.groupingBy(RefCardModel::getOperatorId));
        collect.forEach((k,v)->{
            unicomGatewayService.syncUsage(v);
        });

    }
    /**
     * 10分钟执行一次，调用联通(source=1)失败的重新处理。
     */
    @Scheduled(initialDelay = 1000*60*30,fixedDelay = 1000*60*15)
    public void retry(){
        log.info("查询调用unicom接口失败的卡，重新操作");
        List<OrderUpstream> record = orderUpstreamMapper.getRecord(1,5);
        if(CollectionUtils.isNotEmpty(record)){
           record.forEach(item->{
               //当前已经是这个状态了，没必要改
               if(item.getErrorMsg().contains("1193")){
                    return;
               }
                Map map = new HashMap(3);
                //发生位置是retry定时任务
                map.put("action","3");
                map.put("iccid",item.getIccid());
                map.put("goalState",item.getMirror());
                boolean success = unicomGatewayService.updateCardStatus(map);
                Integer status=0;
               if(success){
                   status = 1;
               }else{
                   //第四次失败后，状态改为失败丢弃
                   if(item.getRetryCount() == 4){
                       status = 2;
                   }
               }
                //调用次数累加
                orderUpstreamMapper.incrRetryCount(item.getId(),status,item.getRetryCount());
            });
        }
    }

    /**
     * 每天上午11点，将前天用量的内存标志位清除。
     */
    @Scheduled(cron = "0 0 11 * * ?")
    public void removeUpdateUsageContainer(){
        log.info("联通蜂窝---昨天用量的内存标志位清除");
        Date yesterday = DateUtils.addDays2Date(DateUtils.getCurrentDate(), -2);
        String stringYesterday = DateUtils.formatDateToString(yesterday);
        Set<Object> objects = redisUtil.sGet(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage());
        if(CollectionUtils.isNotEmpty(objects)){
            objects.forEach(item->{
                if(item.toString().contains(stringYesterday)){
                    redisUtil.setRemove(RedisKeyConstants.UPDATE_USAGE_CONTAINER.getMessage(),item);
                }
            });
        }
    }

}
