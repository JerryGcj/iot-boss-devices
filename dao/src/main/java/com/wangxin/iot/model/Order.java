package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: 订单列表
 * @Author: jeecg-boot
 * @Date:   2020-02-21
 * @Version: V1.0
 */
@Data
@TableName("iot_order")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class Order {
    
	/**UUID*/
	@TableId(type = IdType.ASSIGN_UUID)
	private String id;
	/**订单号*/
	private String orderId;
	/**支付订单号*/
	private String payOrderId;
	private String iccid;
	/**
	 * 电信的接入卡号
	 */
	private String accessNumber;
	/**第三方支付单号*/
	private String upstreamPaymentOrderNumber;
	/**商户订单号*/
	private String mchOrderId;
	private String appId;
	/**微信商户号*/
	private String mchId;
	/**客户手机号*/
	private String mobile;
	/**客户id*/
	private String customerId;
	private String openId;
	/**合伙人id*/
	private String partnerId;
	/**公司名称*/
	private String companyName;
	/**套餐id*/
	private String packageId;
	/**套餐名称*/
	private String packageName;
	/**交易金额（元）*/
	private java.math.BigDecimal tradingMoney;
	private BigDecimal containsFlow;
	/**购买数量*/
	private Integer buyNumber;
	/**状态0:未支付  1：支付退出  2：支付异常 3：支付失败 4：支付成功 5：退款失败  6：退款成功  7：未实名*/
	private Integer payState;
	/**订单状态（1:创建 2:处理中 3:成功 4:失败 5:未确认 6:下月待续费 7:已提交 8:待回调）*/
	private Integer orderState;
	/**支付渠道*/
	private String paymentChannel;
	/**生效方式 0:立即生效 1:下月生效*/
	private String effectType;
	/**备注*/
	private String note;
	private Integer operatorType;
	private String refundRecvAccout;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**创建者*/
	private String createUser;
	/**创建IP*/
	private String createIp;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updateDate;
	/**更新者*/
	private String updateUser;
	/**更新IP*/
	private String updateIp;

	private Date refundTime;
}
