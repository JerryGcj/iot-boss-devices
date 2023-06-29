package com.wangxin.iot.other;

import com.alibaba.fastjson.JSON;
import com.wangxin.iot.config.TelecomGatewayApiConfig;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.utils.StringUtils;
import com.wangxin.iot.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author: yanwin
 * @Date: 2020/3/13
 */
@Component
public class TelecomChannelFactory {

    @Autowired
    IotTelecomCardInfoMapper telecomCardInfoMapper;
    @Autowired
    RedisUtil redisUtil;
    /**
     * 根据接入号获取对应的模板
     * @param accessNumber
     * @return
     */
    public TelecomGatewayApiConfig getOperatorTemplate(String accessNumber,String iccid){
        String operatorId = null;
        if(StringUtils.isNotEmpty(accessNumber)){
            operatorId = telecomCardInfoMapper.getOperatorIdByAccessNumber(accessNumber);
        }else{
            operatorId = telecomCardInfoMapper.getOperatorIdByIccid(iccid);
        }
        IotOperatorTemplate template = CacheComponent.getInstance().getTemplateByOperation(operatorId);
        return JSON.parseObject(template.getTemplate(), TelecomGatewayApiConfig.class);
    }


}
