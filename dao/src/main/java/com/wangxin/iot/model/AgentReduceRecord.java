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
@TableName("iot_agent_reduce_record")
@EqualsAndHashCode(callSuper = false)
public class AgentReduceRecord {
    /**主键UUID*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String orderId;
    private String agentName;
    private String agentId;
    private String packageId;
    private String packageName;
    private BigDecimal money;
    private int operatorType;
    private Date  createTime;
}
