package com.wangxin.iot.constants;

/**
 * 卡资费套餐的状态
 *
 * @author wx
 * @date 2020/2/27
 */
public enum CardCostActiveEnum {
    /**
     * 未生效
     */
    INACTIVE(0),

    /**
     * 生效中
     */
    ACTIVED(1),

    /**
     * 到期失效
     */
    OVERTIME_DISABLED(2),
    /**
     * 用量超了，失效
     */
    OVER_USAGE_DISABLED(3);
    /**
     * 有效状态
     */
    private Integer active;

    CardCostActiveEnum(Integer active) {
        this.active = active;
    }

    public Integer getActive() {
        return active;
    }
}
