package com.wangxin.iot.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/2/20
 */
@Data
@TableName("iot_telecom_ref_card_cost")
public class IotTelecomRefCardCost {
    /**UUID*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String parentId;
    private String iccid;
    private String accessNumber;
    private String costName;
    private Integer costType;
    private String costId;
    private String orderId;
    private String freeType;
    private BigDecimal originUse;
    private BigDecimal usaged;
    //初始用量
    private BigDecimal initUsaged;
    /**
     * 生效状态：0 未生效，1 生效中 2 已失效
     */
    private Integer active;
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date validStart;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date validEnd;

    private Date pushDailyTime;

    /**创建日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**创建者*/
    private String createUser;
    /**更新日期*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**更新者*/
    private String updateUser;
}
