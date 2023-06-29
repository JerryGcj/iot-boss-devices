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
 * @Description: 分润明细
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
@Data
@TableName("iot_share_profits")
public class ShareProfits implements Serializable {
    private static final long serialVersionUID = 1L;

	/**UUID*/
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
	/**创建日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**订单号*/
	private String shareOrderNo;
	/**购买数量*/
	private Integer purchaseQuantity;
	/**集成电路卡识别码20位字符*/
    private String iccid;
	private String mobile;
	/**上级代理ID*/
    private String higherAgentId;
	/**上级代理*/
    private String higherAgent;
	/**下级代理ID*/
    private String lowerAgentId;
	/**下级代理*/
    private String lowerAgent;
	/**套餐价格(元)*/
	private BigDecimal packageMoney;
	/**分润金额(元)*/
	private BigDecimal shareMoney;
	/**套餐名称*/
	private String packageName;
	/**状态(0：创建，1：成功，2：失败，3：异常)*/
	private String status;
	private String shareStatus;
	/**套餐ID*/
    private String packageId;
	/**备注*/
    private String remark;
    private String operatorType;
	/**创建者*/
    private String createUser;
	/**更新日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
	/**更新者*/
    private String updateUser;
	private String orderNo;
	private String payOrderNo;
}
