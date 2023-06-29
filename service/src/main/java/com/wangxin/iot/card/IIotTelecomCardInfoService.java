package com.wangxin.iot.card;


import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import java.util.Map;

/**
 * @Description: iot_union_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
public interface IIotTelecomCardInfoService extends IService<IotTelecomCardInfo> {
    /**
     * 卡状态变更
     * @param simStateChange
     */
    void simStatusChange(Map simStateChange);

    void activePackage(String accessNumber);
}
