package com.wangxin.iot.douyin.entity;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: electron_channel_order_regular
 * @Author: jeecg-boot
 * @Date:   2021-09-29
 * @Version: V1.0
 */
@Data
@TableName("electron_channel_order_regular")
public class ElectronChannelOrderRegular implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    private String id;

	private String orderNum;

	private String outTradeNo;
	/**创建者*/
	private String createBy;

	@TableField(exist = false)
	private String userName;

	/**客户id*/
    private String cusId;
	/**客户姓名*/
    private String cusName;
	/**身份证号*/
	private String cusIdno;

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
	/**
	 * 代理商通道id
    private String agentId;
	/**提交电信平台id*/
    private String ourId;
	private String channelName;

	private String orderSource;

	/**订单状态*/
    private String orderStatus;
	/**快递单号*/
    private String expressNo;

    private String expressCompany;
	/**设备号*/
	//@Excel(name = "设备号", width = 15)
    private String iccid;
	/**接入号*/
    private String accessNumber;
	/**入网时间*/
	//@Excel(name = "入网时间", width = 15)
    private String networkAccessTime;
    private String cancelMsg;
	/**订单来源（1：无语音；2：有语音）*/
	//@Excel(name = "订单来源（1：无语音；2：有语音）", width = 15)
	private Integer fromType;

	private Integer smsStatus;
	/**
	 * 广电省编码
	 */
	private String provinceCode;
	/**
	 * 广电市编码
	 */
	private String cityCode;
	private String canSubmit;
	private String expressState;
	private String errorMsg;
	private String logisticsDetails;
	@TableField(exist = false)
	private JSONArray details;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date commitTime;
	/**
	 * 激活时间
	 */
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date activationDate;

	/**更新者*/
	//@Excel(name = "更新者", width = 15)
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMr.outTradeNo'T+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

	@TableField(exist = false)
	private String agentSimpleName;

	@TableField(exist = false)
	private String orderIds;


}
