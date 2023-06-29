package com.wangxin.iot.other;

import com.wangxin.iot.mapper.IotOperatorTemplateMapper;
import com.wangxin.iot.mapper.IotSysConfigMapper;
import com.wangxin.iot.mapper.WechatPayMapper;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.IotSysConfig;
import com.wangxin.iot.model.WechatPay;
import com.wangxin.iot.utils.ApplicationContextUtil;
import java.util.List;
import java.util.Set;

/**
 * @author: yanwin
 * @Date: 2020/3/2
 */
public class CacheComponent {
    private List<WechatPay> wechatPays;
    private List<IotSysConfig> sysConfigs;
    /**
     * 所有运营商的模板
     */
    private List<IotOperatorTemplate> iotOperatorTemplates;
    /**
     * 购买平台订购过套餐的卡号
     */
    public Set<String> iccids;

    /**
     * 根据key,获取全局配置
     * @param key
     * @return
     */
    public IotSysConfig getConfigByKey(String key){
        return sysConfigs.stream().filter(item -> item.getSysKey().equals(key)).findAny().get();
    }
    /**
     * 根据商户号，获取对应key
     * @param mchId
     * @return
     */
    public String getKeyByMchId(String mchId){
        WechatPay wechatPay = wechatPays.stream().filter(item -> item.getMchId().equals(mchId)).findAny().get();
        if(wechatPay != null){
            return wechatPay.getMchKey();
        }
        return null;
    }
    /**
     * 根据运营商id，获取模板
     * @param operationId
     * @return
     */
    public IotOperatorTemplate getTemplateByOperation(String operationId){
        return iotOperatorTemplates.stream().filter(item->item.getOperatorId().equals(operationId)).findFirst().get();
    }
    public IotOperatorTemplate getTemplateByType(String type){
        return iotOperatorTemplates.stream().filter(item->item.getType().equals(type)).findFirst().get();
    }
    private CacheComponent(){
        IotOperatorTemplateMapper iotOperatorTemplateMapper
                = (IotOperatorTemplateMapper)ApplicationContextUtil.applicationContext.getBean("iotOperatorTemplateMapper");
        iotOperatorTemplates = iotOperatorTemplateMapper.selectList(null);
        WechatPayMapper wechatPayMapper = (WechatPayMapper)ApplicationContextUtil.applicationContext.getBean("wechatPayMapper");
        wechatPays = wechatPayMapper.selectList(null);
        IotSysConfigMapper iotSysConfigMapper = (IotSysConfigMapper)ApplicationContextUtil.applicationContext.getBean("iotSysConfigMapper");
        sysConfigs = iotSysConfigMapper.selectList(null);
    }
    private static class CacheHolder{
        private static CacheComponent cacheComponent =  new CacheComponent();
    }
    public static CacheComponent getInstance(){
        return CacheHolder.cacheComponent;
    }
}
