package com.wangxin.iot.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: iot_unicom_flow_pool
 * @Author: jeecg-boot
 * @Date:   2021-04-07
 * @Version: V1.0
 */
@Data
@TableName("iot_unicom_flow_pool")
public class IotUnicomFlowPool implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.AUTO)
    private Integer id;
	/**流量池总量(GB)*/
    private BigDecimal total;
	/**流量池用量(GB)*/
    private BigDecimal usaged;
	/**月份*/
    private String statisticsMonth;
	/**createTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date createTime;
	/**updateTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date updateTime;
}
