package com.wangxin.iot.config.event;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * @author: yanwin
 * @Date: 2021/5/12
 * @Desc: 同步电信卡的access_number
 */
@Slf4j
@Component
public class TelecomSyncDetailsReceiver {
    @Autowired
    RedisUtil redisUtil;
    @Autowired
    ITelecomGatewayService telecomGatewayService;

    public void receiveMessage(String flag){
       if("1".equals(flag)){
           Set<Object> toTos = redisUtil.sGet(RedisKeyConstants.TELECOM_EQUIPMENT_ACCESSNUMBER.getMessage());
           log.info("电信导入卡完毕，共：{}个，开始同步accessNumber",toTos.size());
           if(CollectionUtils.isNotEmpty(toTos)){
               for (Object toTo : toTos) {
                   String iccid = toTo.toString();
                   telecomGatewayService.acquireAccessNumber(iccid);
               }
           }
           //同步完accessNumber,删除对应的key
           redisUtil.del(RedisKeyConstants.TELECOM_EQUIPMENT_ACCESSNUMBER.getMessage());
       }
    }
}
