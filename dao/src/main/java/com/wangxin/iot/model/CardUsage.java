package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/2/20
 */
@Data
@TableName("iot_card_usage")
public class CardUsage {
    /**UUID*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String iccid;
    private LocalDate date;
    private BigDecimal cardUsage;
    private Date createDate;
}
