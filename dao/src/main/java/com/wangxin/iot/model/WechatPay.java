package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/5/11
 */
@Data
@TableName("iot_wechat_pay")
public class WechatPay {
    private String mchName;
   private String mchId;
   private String mchKey;
}
