package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @Description: iot_card_wechat_relation
 * @Author: jeecg-boot
 * @Date:   2020-04-26
 * @Version: V1.0
 */
@Data
@TableName("iot_card_wechat_relation")
public class IotCardWechatRelation implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_UUID)
	private String id;
	/**卡号*/
	private String iccid;
	/**openid*/
	private String openId;
	/**昵称*/
	private String petName;
	private String mobile;
	private String operatorType;
	private java.lang.String virtualIccid;
	private java.lang.String accessNumber;
	/**用户类型(0：终端用户)*/
	private String userType;
	/**状态(0：正常)*/
	private String status;
	/**企业名称*/
	private String userComapny;
	/**
	 * 账户余额
	 */
	private BigDecimal account;
	private Boolean refundSwitch;
	private String delFlag;
	/**创建人*/
	private String createUser;
	/**创建时间*/
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
}
