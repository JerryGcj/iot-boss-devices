package com.wangxin.iot.model.api.product;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class Response2 {

    /**
     * 套餐id
     */
    private String packageId;
    /**
     * 套餐名称
     */
    private String packageName;
    /**
     * 运营商
     */
    private String operatorType;
    /**
     * 售价
     */
    private BigDecimal price;

    @Override
    public String toString() {
        return "Response2 [packageId=" + packageId + ", packageName=" + packageName + ", operatorType= "+operatorType+", price=" + price + "]";
    }
}
