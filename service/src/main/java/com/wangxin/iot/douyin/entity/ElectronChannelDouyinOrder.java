package com.wangxin.iot.douyin.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * @Description: electron_channel_douyin_order
 * @Author: jeecg-boot
 * @Date:   2022-12-06
 * @Version: V1.0
 */
@Data
@TableName("electron_channel_douyin_order")
public class ElectronChannelDouyinOrder implements Serializable {
    private static final long serialVersionUID = 1L;


	/**id*/
	@TableId(type = IdType.ID_WORKER_STR)
    private String id;
	/**代理ID*/
	private String orderId;
    private String cusId;
	/**产品通道ID*/
    private String agentId;
	/**客户姓名*/
    private String cusName;
	/**客户手机号*/
    private String cusPhone;
	/**身份证号*/
    private String cusIdno;
	/**省*/
    private String province;
	/**市*/
    private String city;
    private String district;
    private String street;
	/**详细地址*/
    private String detailAddr;
	private Integer transferIs;
	/**支付金额(分)*/
	private Long payAmount;
	private String authorName;
	/**店铺id*/
    private Long shopId;
	/**商户名称*/
    private String shopName;
	/**店铺订单号*/
	/**下单端(0、站外 1、火山 2、抖音 3、头条 4、西瓜 5、微信 6、值点app 7、头条lite 8、懂车帝 9、皮皮虾 11、抖音极速版 12、TikTok 13、musically 14、穿山甲 15、火山极速版 16、服务市场 26、番茄小说 27、UG教育营销电商平台 28、Jumanji 29、电商SDK)*/
    private Long bType;
	/**小程序ID*/
    private String appId;
	/**支付类型 (0、货到付款 1 、微信 2、支付宝 3、小程序 4、银行卡 5、余额 7、无需支付（0元单） 8、DOU分期（信用支付） 9、新卡支付 12、先用后付)*/
    private Integer payType;
	/**订单状态(1:拉取成功，2:解密成功，3:同步成功 )*/
    private Long orderStatus;
	/**抖音下单日期*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date douyinCreateTime;
	/**抖音商品ID*/
    private Long productId;
	/**商品skuId*/
    private Long skuId;
	/**直播主播ID*/
    private Long authorId;
	/**直播主播名称*/
    private String themeType;
	/**直播间ID*/
    private Long roomId;
    private String roomName;
	/**内容id*/
    private String contentId;
	/**视频id*/
    private String videoId;
	/**流量来源id(1:已发货未签收通知，2:已签收未激活通知，3:作废通知)*/
    private String originId;
    private Integer sensitiveIs;
    private String syncCode;
    private String syncExpress;
	/**创建者*/
    private String createBy;
	/**创建时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
	/**更新者*/
    private String updateBy;
	/**更新时间*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
}
