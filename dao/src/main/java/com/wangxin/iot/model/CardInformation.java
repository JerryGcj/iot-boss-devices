package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 物联网卡信息表
 * @Author: jeecg-boot
 * @Date:   2020-01-20
 * @Version: V1.0
 */
@Data
@TableName("iot_card_information")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class CardInformation {
    
	/**UUID*/
	@TableId(type = IdType.UUID)
	private String id;
	/**集成电路卡识别码20位字符*/
	private String iccid;
	private String virtualIccid;
	/**物联卡号码最长13位数字*/
	private String msisdn;
	/**国际移动用户识别码不超过15位*/
	private String imsi;
	/**国际移动设备识别码*/
	private String imei;
	/**设备开关机状态   0:关机 1：开机*/
	private String deviceOnOffState;
	/**卡状态 0:待激活 1:沉默期 2:测试期 3:正常 4:停机 5:已销号 6:其他*/
	private String cardState;
	/**是否自动停复机    0:开启 1:关闭*/
	private String automaticStopResetMachine;
	/**是否开启卡同步    0:开启 1:关闭*/
	private String cardSynchronization;
	/**流量用量(MB)*/
	private java.math.BigDecimal trafficUse;
	/**套餐id  */
	private String packageId;
	/**套餐显示名称*/
	private String packageName;
	/**入网时间*/
//	@Excel(name = "入网时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date theNetTime;
	/**激活时间*/
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date activationTime;
	/**强制生效时间*/
//	@Excel(name = "强制生效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date mandatoryEffectiveDate;
	/**同步时间*/
//	@Excel(name = "同步时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date synchronizationTime;
	/**失效时间*/
//	@Excel(name = "失效时间", width = 20, format = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date loseEfficacyTime;
	/**运营商类型 0:移动 1:联通 3:电信*/
//	@Excel(name = "运营商类型 0:移动 1:联通 3:电信", width = 15)
	private String operatorType;
	/**导入批次*/
	private String importBatch;
	private String UpstreamFlowPoolNumber;
	private String IsRealNameAuthentication;
	private String AgentStopMachine;
	/**分配批次*/
	private String distributionBatch;
	/**自定义套餐使用量*/
	private java.math.BigDecimal customPackageUse;
	/**运营商id */
	private String operatorId;
	/**运营商显示名称 */
	private String operatorName;
	/**是否是流量卡 0:是 1:不是 */
	private String thirtyDayCard;
	/**分配状态 0:已分配 1:未分配*/
	private String allocationState;
	/**分配用户id*/
	private String customerId;
	/**分配企业名称*/
	private String customerName;
	/**备注 */
	private String note;
	/**创建日期*/
	private Date createDate;
	/**创建者*/
	private String createUser;
	/**创建IP*/
	private String createIp;
	/**更新日期*/
	private Date updateDate;
	/**更新者*/
	private String updateUser;
	/**更新IP*/
	private String updateIp;

	private java.math.BigDecimal trafficTotal;

	/**本月流量余量(MB) */
	private java.math.BigDecimal trafficRemainingMonth;

	/**已超套餐流量(MB) */
	private java.math.BigDecimal trafficTotalMore;

	private String userName;

	private Integer isNew;
}
