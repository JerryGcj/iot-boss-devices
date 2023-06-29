package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: 实名查看
 * @Author: jeecg-boot
 * @Date:   2020-02-10
 * @Version: V1.0
 */
@Data
@TableName("real_name_system")
public class RealNameSystem implements Serializable {
    private static final long serialVersionUID = 1L;

    /**uuid*/
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    /**集成电路卡识别码20位字符*/
    private String iccid;
    /**
     * 虚拟卡号
     */
    private String virtualIccid;
    /**物联卡号码最长13位数字*/
    private String msisdn;
    /**身份证号码*/
    private String idCardNumber;
    /**真实姓名*/
    private String name;
    /**手机号*/
    private String mobile;
    /**商户ID*/
    private String userId;
    /**商户名*/
    private String userCompany;
    private Integer operatorType;
    /**请求流水号*/
    private String serialNumber;
    /**状态（0：待审核，1：成功，2：失败）*/
    private String status;
    /**备注*/
    private String remark;
    /**身份证正面图片地址*/
    private String idFront;
    private String frontPicId;
    /**身份证反面图片地址*/
    private String idBack;
    private String backPicId;
    /**手持身份证图片地址*/
    private String idHandheld;
    private String handPicId;

    private String errorMsg;
    /**
     * 唇语信息
     */
    private String lipsInfo;
    /**
     * oss视频地址
     */
    private String idVideo;
    /**
     * 联通返回的地址
     */
    private String unicomVideoId;

    private Integer retryCount;
    /**创建日期*/
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    /**创建者*/
    private String createUser;
    /**创建IP*/
    private String createIp;
    /**更新日期*/
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    /**更新者*/
    private String updateUser;
    private String updateIp;
}
