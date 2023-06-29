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
 * @Description: 终端销售折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Data
@TableName("iot_terminal_sales_discount")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class TerminalSalesDiscount {
    
	/**uuid*/
	@TableId(type = IdType.ASSIGN_UUID)
	private String id;
	/**运营商类型 0:移动 1:联通 3:电信*/
	private String operatorType;
	/**套餐id*/
	private String packageId;
	/**套餐名称*/
	private String packageName;
	private String packageType;
	/**状态 0:上架 1:下架*/
	private String state;
	/**销售价格（元）*/
	private java.math.BigDecimal salesPrice;
	/**创建日期*/
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**创建者*/
	private String createUser;
	/**创建IP*/
	private String createIp;
	/**更新日期*/
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updateDate;
	/**更新者*/
	private String updateUser;
	/**更新IP*/
	private String updateIp;
}
