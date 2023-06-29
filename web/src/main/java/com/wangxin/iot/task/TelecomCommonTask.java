package com.wangxin.iot.task;

import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.mapper.OrderUpstreamMapper;
import com.wangxin.iot.model.OrderUpstream;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 电信卡常规监控
 * @date 2021-05-12
 */
@Component
@Slf4j
public class TelecomCommonTask {


    @Autowired
    ITelecomGatewayService telecomGatewayService;
    @Autowired
    IotTelecomCardInfoMapper iotTelecomCardInfoMapper;
    @Autowired
    OrderUpstreamMapper orderUpstreamMapper;
    @Autowired
    Executor executor;
    @Autowired
    RedisUtil redisUtil;

    /**
     *  同步卡用量，同步完成后，将总用量同步到ref中
     */
    @Scheduled(initialDelay = 1000*60*15,fixedDelay = 1000*60*60)
    public void syncUsage(){
        //3小时之内，没有接收过推送的卡

        List<RefCardModel> refModel = iotTelecomCardInfoMapper.getSyncRefModel(2);
        log.info("电信同步卡用量开始，本次待同步数量：{}",refModel.size());
        ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
        if (!CollectionUtils.isEmpty(refModel)){
            refModel.forEach(item-> threadPoolExecutor.execute(() -> {
                telecomGatewayService.syncUsage(item);
                log.info("同步Telecom卡用量：  核心线程数：{},完成任务数：{}缓冲区大小,{}",
                        threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount(),threadPoolExecutor.getThreadPoolExecutor().getQueue().size());
            }));
        }
    }

    /**
     * 40分钟执行一次，调用电信(source=2)失败的重新处理。
     */
    @Scheduled(initialDelay = 1000*60*30,fixedDelay = 1000*60*40)
    public void retry(){
        log.info("查询调用telecom接口失败的卡，重新操作");
        List<OrderUpstream> record = orderUpstreamMapper.getRecord(2,15);
        if(!CollectionUtils.isEmpty(record)){
           record.forEach(item->{
               //当前已经是这个状态了，没必要改
                Map map = new HashMap(3);
                //发生位置是retry定时任务
                map.put("action","3");
                if(item.getMirror().contains("disabledNumber")){
                    map.put("method",item.getMirror().split("_")[0]);
                    map.put("orderTypeId",item.getMirror().split("_")[1]);
                }else{
                    map.put("method",item.getMirror());
                }
                map.put("access_number",item.getIccid());
                boolean success = telecomGatewayService.updateCardStatus(map);
                Integer status=0;
               if(success){
                   status = 1;
               }else{
                   //第四次失败后，状态改为失败丢弃
                   if(item.getRetryCount() == 14){
                       status = 2;
                   }
               }
                //调用次数累加
                orderUpstreamMapper.incrRetryCount(item.getId(),status,item.getRetryCount());
            });
        }
    }

}
