package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * <p>
 * 用户表
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysUser implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;

    /**
     * 登录账号
     */
    private String username;


    private Integer status;

    private Integer delFlag;
    /**
     * 创建人id
     */
    private String createById;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    private String userCompany;

    /**
     * 用户类型 0:内部用户 1:代理商 2：合伙人 3:企业用户
     */
    private String userType;

    private String ipWhite;

    private String theKey;
    /**
     * 余额
     */
    private BigDecimal balance;
}
