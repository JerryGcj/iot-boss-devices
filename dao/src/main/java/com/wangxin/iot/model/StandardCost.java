package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

/**
 * @Description: 标准资费列表
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Data
@TableName("iot_standard_cost")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class StandardCost {
	/**uuid*/
	@TableId(type = IdType.UUID)
	private java.lang.String id;
	/**套餐名称*/
	private java.lang.String packageName;
	/**运营商类型 0:移动 1:联通 3:电信*/
	private java.lang.String operatorType;
	/**套餐类型 0:流量 1:语音 2:短信 3:混合*/
	private java.lang.String packageType;
	/**包含流量*/
	private java.math.BigDecimal containsFlow;
	/**包含语音(分钟)*/
	private java.math.BigDecimal containsVoice;
	/**包含短信(条)*/
	private java.math.BigDecimal containsSms;
	/**包体类型 0:按自然月计费 1:按使用时间计费*/
	private java.lang.String inclusionType;
	/**有效期*/
	private Integer periodOfValidity;
	/**是否是基础套餐 0:是 1:否*/
	private java.lang.String basicPackage;
	/**标准资费（元）*/
	private java.math.BigDecimal standardRates;
	private java.math.BigDecimal dailyLimit;
	/**状态 0:上架 1:下架*/
	private java.lang.String statue;
	/**生效类型 0:立即生效 1：次月生效 2:到期生效*/
	private java.lang.String effectType;
	private java.lang.String freeType;
	/**创建日期*/
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date createTime;
	/**创建者*/
	private java.lang.String createUser;
	/**创建IP*/
	private java.lang.String createIp;
	/**更新日期*/
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private java.util.Date updateDate;
	/**更新者*/
	private java.lang.String updateUser;
	/**更新IP*/
	private java.lang.String updateIp;
}
