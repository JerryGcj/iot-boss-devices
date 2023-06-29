package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: iot_sys_config
 * @Author: jeecg-boot
 * @Date:   2020-02-17
 * @Version: V1.0
 */
@Data
@TableName("iot_sys_config")
public class IotSysConfig implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**id*/
	@TableId(type = IdType.AUTO)
    private Integer id;
	/**key*/
    private String sysKey;
	/**value*/
    private String sysValue;
	private boolean open;
	/**desc*/
    private String sysDesc;
	private boolean editable;
	private String isSwitch;
	/**创建日期*/
	private Date createTime;
	/**创建者*/
	private String createUser;
	/**创建IP*/
	private String createIp;
	/**更新日期*/
	private Date updateTime;
	/**更新者*/
	private String updateUser;
	/**更新IP*/
	private String updateIp;
}
