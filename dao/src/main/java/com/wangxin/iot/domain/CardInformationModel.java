package com.wangxin.iot.domain;

import lombok.Data;

import java.math.BigDecimal;

/**
 * @author: yanwin
 * @Date: 2020/6/11
 */
@Data
public class CardInformationModel {
    private String id;
    private String iccid;
    private String cardState;
    private BigDecimal customPackageUse;
}
