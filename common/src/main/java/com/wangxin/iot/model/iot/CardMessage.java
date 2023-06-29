package com.wangxin.iot.model.iot;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by 18765 on 2020/1/8 14:31
 * @author 18765
 */
@Data
public class CardMessage implements Serializable {
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
    private String global_sim_type;
    private String customer;
    private String ratePlan;
    private Date dateAdded;
    private Date dateModified;
    private Date dateShipped;
    private Date createTime;
    private Date updateTime;
}
