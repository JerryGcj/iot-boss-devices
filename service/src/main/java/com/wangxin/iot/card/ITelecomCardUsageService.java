package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotTelecomCardUsage;
import com.wangxin.iot.domain.RefCardModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
public interface ITelecomCardUsageService extends IService<IotTelecomCardUsage> {
    /**
     * 更新日用表设备用量
     * @param map
     */
    void updateUsage(Map<String,String> map);

    /**
     * 将日用表的同步到套餐表
     * @param refCardModel
     */
    void syncRefUsage(RefCardModel refCardModel);
}
