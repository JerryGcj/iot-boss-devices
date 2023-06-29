package com.wangxin.iot.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/2/20
 */
@Data
@TableName("iot_telecom_card_usage")
public class IotTelecomCardUsage {
    /**UUID*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String iccid;
    private String accessNumber;
    private LocalDate date;
    private BigDecimal cardUsage;
    private Date createDate;
}
