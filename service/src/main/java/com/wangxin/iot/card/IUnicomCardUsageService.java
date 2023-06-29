package com.wangxin.iot.card;

import com.wangxin.iot.domain.RefCardModel;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
public interface IUnicomCardUsageService {
    /**
     * 更新日用表设备用量
     * @param iccid
     * @param map
     */
    void updateUsage(String iccid, Map map);

    /**
     * 更新日用表设备用量
     * @param map
     */
    void updateUsage(Map map);

    /**
     * 初次保存用量
     * @param iccid
     * @param activeDate
     * @param currentUsage
     */
    void saveUsage(String iccid, LocalDate activeDate,BigDecimal currentUsage);

    /**
     * 将日用表的同步到套餐表
     * @param refCardModel
     */
    void syncRefUsage(RefCardModel refCardModel);
}
