package com.wangxin.iot.card;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotUnicomCardInfo;

import java.util.Map;

/**
 * @Description: iot_union_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
public interface IIotUnicomCardInfoService extends IService<IotUnicomCardInfo> {
    /**
     * 卡状态变更
     * @param simStateChange
     */
    void simStatusChange(Map simStateChange);

    /**
     * 卡24小时用量变更
     * @param ctdUsage
     */
    void dataUsage24Change(Map ctdUsage);

    /**
     * 卡周期内用量变化
     * @param ctdUsage
     */
    void dataUsageCycle(Map ctdUsage);

    /**
     * 卡实名状态
     * @param realNameStatus
     */
    void realNameStatus(Map realNameStatus);

    /**
     * 流量池用量
     * @param flowPool
     */
    void flowPoolUsage(Map flowPool);
}
