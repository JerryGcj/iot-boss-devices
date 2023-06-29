package com.wangxin.iot.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class Card implements Serializable {

    private String iccid;
    private String msisdn;
    private String imsi;
    private String imei;
    private Integer status;
    private Date activeTime;
    private Integer onOff;
    private BigDecimal data;
    private Integer sms;
    private Integer voice;
    private Date dataUpdateTime;
    private Date statusUpdateTime;
    private String operatorCustom;
    private String ctdSessionCount;
    private String accountId;
    private String custom10;
    private String globalSimType;
    private String customer;
    private String ratePlan;
    private String dateAdded;
    private String dateModified;
    private String dateShipped;
    private Date createTime;
    private Date updateTime;

}