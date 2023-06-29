package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: stop_iccid
 * @Author: jeecg-boot
 * @Date:   2022-09-02
 * @Version: V1.0
 */
@Data
@TableName("stop_iccid")
public class StopIccid implements Serializable {
    private static final long serialVersionUID = 1L;

	/**id*/
	@TableId(type = IdType.ASSIGN_UUID)
    private String id;
	/**iccid*/
    private String iccid;
	/**mobile*/
    private String mobile;
	/**userId*/
    private String userId;
    private String operatorType;
	/**status*/
    private String status;
	/**createTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
	/**createBy*/
    private String createBy;
	/**updateTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
	/**updateBy*/
    private String updateBy;
}
