package com.wangxin.iot.douyin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: electron_channel_douyin_user_relation
 * @Author: jeecg-boot
 * @Date:   2022-12-14
 * @Version: V1.0
 */
@Data
@TableName("electron_channel_douyin_user_relation")
public class ElectronChannelDouyinUserRelation implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    private String id;
	/**达人昵称*/
    private String dyName;
	/**达人ID*/
    private String dyId;
	/**用户表ID*/
    private String ourId;
	/**createTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date createTime;
	/**createBy*/
    private String createBy;
	/**updateTime*/

	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
    @DateTimeFormat(pattern="yyyy-MM-dd")
    private Date updateTime;

	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd")
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String updateBy;
}
