package com.wangxin.iot.card;

import com.wangxin.iot.domain.RefCardModel;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
public interface ICardUsageService {
    void saveUsage(String iccid, LocalDate activeDate, BigDecimal currentUsage);

    void updateUsage(String iccid, LocalDate localDate, BigDecimal usage);

    BigDecimal getPeriodUsage(RefCardModel refCardModel);

    void syncRefUsage(RefCardModel refCardModel);
}
