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
@TableName("iot_unicom_card_usage")
public class IotUnicomCardUsage {
    /**UUID*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String iccid;
    private LocalDate date;
    private BigDecimal cardUsage;
    private Date createDate;
}
