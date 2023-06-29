package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IStandardCostService;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.mapper.StandardCostMapper;
import com.wangxin.iot.model.StandardCost;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Description: 标准资费列表
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Service
public class StandardCostServiceImpl extends ServiceImpl<StandardCostMapper, StandardCost> implements IStandardCostService {
    @Override
    public List<StandardCost> getDailyLimitCost() {
        return this.baseMapper.getDailyLimitCost();
    }

    @Override
    public List<IotTelecomRefCardCost> getDailyLimit() {
        return this.baseMapper.getDailyLimit();
    }
}
