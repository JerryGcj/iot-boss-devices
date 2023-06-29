package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.model.StandardCost;

import java.util.List;

/**
 * @Description: 标准资费列表
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
public interface IStandardCostService extends IService<StandardCost> {
    /**
     * 查询在当前时间内配置了每日限额的免费套餐的卡
     * @return
     */
    List<IotTelecomRefCardCost> getDailyLimit();

    List<StandardCost> getDailyLimitCost();
}
