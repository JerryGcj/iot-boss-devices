package com.wangxin.iot.model.wechat;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/4/13
 */
@Data
public class OrderCostModel {
    @NotNull
    private String iccid;
    //下单类型
    private String type;
    private Integer operatorType;
    @NotNull
    private String packageId;
    private String accessNumber;
    private String packageName;
    private BigDecimal salesPrice;
    private String appId;
    private String mchId;
    private String openId;
    private String mobile;
    private Integer costType;
    private String createUser;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    private BigDecimal originUse;
    private BigDecimal usaged;
    private Integer active;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validStart;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    private Date validEnd;
}
