package com.wangxin.iot.card;
import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.model.ShareProfits;


/**
 * @Description: 分润明细
 * @Author: jeecg-boot
 * @Date:   2020-02-12
 * @Version: V1.0
 */
public interface IIotShareProfitsService extends IService<ShareProfits> {

    void recycleCommission(IotTelecomRefCardCost refCardCost);
}
