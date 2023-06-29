package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/4/24
 */
@Data
@TableName("iot_number_temp")
@EqualsAndHashCode(callSuper = false)
public class NumberTemp {
    /**主键UUID*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String iccid;
    private int processed;
    private Date packageExpire;
    private int delFlag;
}
