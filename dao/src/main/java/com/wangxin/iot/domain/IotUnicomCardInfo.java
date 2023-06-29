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
 * @Description: iot_union_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
@Data
@TableName("iot_unicom_card_info")
public class IotUnicomCardInfo implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String ratePlan;
	/**客户id*/
    private String customer;
	/**设备id*/
    private String deviceId;
	/**卡号*/
    private String iccid;
	/**用量超额重置*/
    private String overageLimitOverride;
	/**账户id*/
    private String accountId;
	/**运营商账户id*/
    private String operatorAccountId;
	/**本月用量（MB）*/
    private BigDecimal monthToDateUsage;
	/**IMEI*/
    private String imsi;
	/**已达到用量限额*/
    private BigDecimal overageLimitReached;
	/**msisdn*/
    private String msisdn;
	/**实名制状态*/
    private Integer realNameStatus;
	/**imei*/
    private String imei;
	/**
	 * SIM卡状态:
    "0": 可测试,
   "1": 可激活,
    "2": 已激活,
    "3": 已停用,
    "4": 已失效,
    "5"": 已清除,
    "6": 已更换,
    "7": 库存,
    "8": 开始*/
    private java.lang.Integer simStatus;
	/**激活时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
	/**分配用户id*/
    private String userId;
	/**分配用户登陆名*/
    private String userName;
	private String userCompany;
	/**运营商id*/
    private String operatorId;
	/**运营商name*/
    private String operatorName;
	/**运营商类型*/
    private Integer operatorType;
	/**客户自定义使用倍数*/
    private BigDecimal customPackageUse;
	/**导入批次*/
    private String importBatch;
	/**分配批次*/
    private String distributionBatch;
	/**
	 * 卡状态变更时间
	 */
	private Date simStatusChangeTime;
	/**
	 * 卡用量变更时间
	 */
    private Date dataUsageChangeTime;
    private Date dateActivated;
	private Date firstPurchaseTime;
	/**创建人*/
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date createTime;
	/**修改人*/
    private String updateBy;
	/**修改时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date updateTime;
	/**0表示未删除,1表示删除*/
    private String delFlag;
}
