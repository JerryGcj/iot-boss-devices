package com.wangxin.iot.other;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.constants.RedisKeyConstants;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.SysUserMapper;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.third.hu.JhApiConfig;
import com.wangxin.iot.utils.redis.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/3/13
 */
@Component
public class IotTemplateFactory {
    @Autowired
    private RedisUtil redisUtil;
    @Autowired
    CardInformationMapper cardInformationMapper;
    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    SysUserMapper sysUserMapper;
    /**
     * 根据卡号获取对应的模板
     * @param iccid
     * @return
     */
    public IotOperatorTemplate getOperatorTemplate(String iccid){
        IotOperatorTemplate iotOperatorTemplate = null;
        //redis缓存中有没有卡号和模板对应关系
        Object hget = redisUtil.hget(RedisKeyConstants.ICCID_TEMPLATE.getMessage(), iccid);
        if(hget != null){
            iotOperatorTemplate = JSON.parseObject(hget.toString(), IotOperatorTemplate.class);
        }
        //缓存中没有，则从数据库查
        if(iotOperatorTemplate == null){
            String operationId = cardInformationMapper.getOperationIdByIccid(iccid);
            //运营商对应模板
            iotOperatorTemplate = CacheComponent.getInstance().getTemplateByOperation(operationId);
           //卡号对应的上游通道的密钥信息
            if("jhThirdServiceImpl".equals(iotOperatorTemplate.getHandlerClass())){
                Map<String, Map<String, String>> userScreat = sysUserMapper.getUserScreat(iccid);
            if(!CollectionUtils.isEmpty(userScreat)){
                Map<String, String> stringStringMap = userScreat.get(iccid);
                String channelId = stringStringMap.get("channel_id");
                String key = stringStringMap.get("the_key");
                JhApiConfig jhApiConfig = JSON.parseObject(iotOperatorTemplate.getTemplate(), JhApiConfig.class);
                jhApiConfig.setTraderId(channelId);
                jhApiConfig.setAppSecret(key);
                iotOperatorTemplate.setTemplate(JSON.toJSONString(jhApiConfig));
            }
            }
            redisUtil.hset(RedisKeyConstants.ICCID_TEMPLATE.getMessage(), iccid,JSON.toJSONString(iotOperatorTemplate));
        }
        return iotOperatorTemplate;
    }

    /**
     * 根据卡号，获取对应的通道服务
     * @param iccid
     * @return
     */
    public ThirdService getExecutorThridService(String iccid){
        IotOperatorTemplate operatorTemplate = this.getOperatorTemplate(iccid);
        return  (ThirdService)applicationContext.getBean(operatorTemplate.getHandlerClass());
    }

    /**
     * 根据模板，获取对应的通道服务
     * @param operatorTemplate
     * @return
     */
    public ThirdService getExecutorThridService(IotOperatorTemplate operatorTemplate){
        return  (ThirdService)applicationContext.getBean(operatorTemplate.getHandlerClass());
    }
}
