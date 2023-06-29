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
 * @Description: 套餐折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-17
 * @Version: V1.0
 */
@Data
@TableName("iot_discount_package")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class DiscountPackage {
    
	/**uuid*/
	@TableId(type = IdType.ASSIGN_UUID)
	private String id;
	/**运营商类型 0:移动 1:联通 3:电信*/
	private String operatorType;
	/**运营商id*/
	private String operatorId;
	/**运营商名称*/
	private String operatorName;
	/**套餐id*/
	private String packageId;
	/**套餐名称*/
	private String packageName;
	private String packageType;
	/**状态 0:上架 1:下架*/
	private String state;
	/**成本（元）*/
	private java.math.BigDecimal cost;
	/**上游套餐编码*/
	private String upstreamPackageCode;
	/**备注*/
	private String note;
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


	/**包体类型 0:按自然月计费 1:按使用时间计费*/
	private String inclusionType;
}
