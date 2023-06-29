package com.wangxin.iot.model;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * @Description: iot_recharge_order
 * @Author: jeecg-boot
 * @Date:   2021-06-24
 * @Version: V1.0
 */
@Data

public class IotRechargeOrderModel implements Serializable {

    private static final long serialVersionUID = 1L;

	/**充值用户*/
    private String openId;
	/**商品id*/
    private String productId;
	/**充值金额*/
    private BigDecimal money;

}
