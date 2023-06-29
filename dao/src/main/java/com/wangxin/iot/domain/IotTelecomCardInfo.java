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
 * @Description: iot_telecom_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
@Data
@TableName("iot_telecom_card_info")
public class IotTelecomCardInfo implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
	/**客户id*/
    private String customIccid;
    private String virtualIccid;
	/**
	 * 接如卡号
	 */
	private String accessNumber;
	/**卡号*/
    private String iccid;

	/**运营商账户id*/
    private String operatorAccountId;
	/**本月用量（MB）*/
    private BigDecimal monthToDateUsage;
	/**IMEI*/
    private String imsi;

	/**msisdn*/
    private String msisdn;
	/**实名制状态*/
    private Integer realNameStatus;
	/**imei*/
    private String imei;
	/**
	 * 卡主状态说明。 取值范围：
	 * 可激活：尚未激活，处于未激活或测试期未激活状态
	 * 测试激活：处于测试期已激活状态
	 * 测试去激活：处于测试期去激活状态
	 * 在用：处于正常使用状态
	 * 停机：处于停机保号或去激活停机状态
	 * 运营商管理状态：处于预开通、欠费停机、紧急停机等运营商管理的状态
	 * 拆机：已拆机
	 SIM卡状态:
	 	1：可激活 2：测试激活 3：测试去激活 4：在用 5：停机 6：运营商管理状态 7：拆机

	 */
    private Integer simStatus;
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
	private String speedValue;
	/**
	 * 卡状态变更时间
	 */
	private Date simStatusChangeTime;
	/**
	 * 卡用量变更时间
	 */
    private Date dataUsageChangeTime;
    private Date dateActivated;
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
