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
 * @Description: iot_card_order
 * @Author: jeecg-boot
 * @Date:   2021-09-29
 * @Version: V1.0
 */
@Data
@TableName("iot_card_order")
public class IotCardOrder implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
	/**客户id*/
    private String cusId;
	/**客户姓名*/
    private String cusName;
	/**客户手机号*/
    private String cusPhone;
	/**省*/
    private String province;
	/**市*/
    private String city;
	/**区*/
    private String district;
	/**详细地址*/
    private String detailAddr;
	/**提交电信平台id*/
    private String userId;
	private String userCompany;
	private String payState;
	/**订单状态*/
    private String orderState;
	/**快递单号*/
    private String expressNo;
	/**设备号*/
    private String iccid;
	/**接入号*/
    private String accessNumber;
	/**入网时间*/
    private String expressCompany;
	private String packageId;
	private String packageName;
	private Integer buyNumber;
	private Integer fromType;
	private String orderType;
	/**微信公众号openid*/
	private String openid;
	private String mchId;
	/**微信支付交易流水号*/
	private String wxTransactionId;
	/**订单支付金额*/
	private BigDecimal orderPayPrice;
	private String remark;
	/**订单支付时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date orderPayTime;
	/**创建者*/
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
	/**更新者*/
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
