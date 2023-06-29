package com.wangxin.iot.domain;

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
 * @Description: 物联网卡信息表
 * @Author: jeecg-boot
 * @Date:   2020-01-20
 * @Version: V1.0
 */
@Data
@TableName("iot_card_separate")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class IotCardSeparateEntity {

	/**
	 * UUID
	 */
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**
	 * 集成电路卡识别码20位字符
	 */
	private String iccid;

	/**
	 * 物联卡号码最长13位数字
	 */
	private String msisdn;
	/**
	 * 国际移动用户识别码不超过15位
	 */
	private String stopReason;

    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**updateDate*/
    @JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date updateTime;

}