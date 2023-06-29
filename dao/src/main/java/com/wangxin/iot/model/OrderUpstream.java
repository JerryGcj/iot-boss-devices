package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@TableName("iot_order_upstream")
@Data
public class OrderUpstream implements Serializable {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String iccid;
//    private String accessNumber;
    private String outTradeNo;
    private String bizOrderNo;
    private String errorMsg;
    private Integer status;
    private Integer retryCount;
    private Integer action;
    private String mirror;
    private Integer source;
    private Date createTime;
    private Date updateTime;

}