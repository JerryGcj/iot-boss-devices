package com.wangxin.iot.douyin.model;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author anan
 * @date 2023/1/16 10:52
 */
@Data
@AllArgsConstructor
public class DouyinUpdateExpressModel {
    private String orderId;
    private String companyCode;
    private String logisticsCode;
}
