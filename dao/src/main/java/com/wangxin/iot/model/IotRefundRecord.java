package com.wangxin.iot.model;

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
 * @Description: iot_refund_record
 * @Author: jeecg-boot
 * @Date:   2021-07-13
 * @Version: V1.0
 */
@Data
@TableName("iot_refund_record")
public class IotRefundRecord implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    private String id;
	/**openId*/
    private String openId;
	private String iccid;
	private String mobile;
	private String operatorType;
	private String orderId;
	private String refId;
	private String refundId;
	private String packageName;
	private BigDecimal actualRefundMoney;
	/**退款状态（0,审核中，1：成功，2：失败）*/
    private Integer refundStatus;
    private BigDecimal refundMoney;
	private String userCompany;
	/**备注*/
    private String refundMsg;
	private String userId;
	private String channel;
	private String userReceivedAccount;

	private String isNew;
	private String automation;
	/**createTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
	private Date buyTime;
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date successTime;
	private Date updateTime;
}
