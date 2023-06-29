package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.model.StopIccid;

/**
 * @Description: stop_iccid
 * @Author: jeecg-boot
 * @Date:   2022-09-02
 * @Version: V1.0
 */
public interface IStopIccidService extends IService<StopIccid> {

    void saveStopIciid(String iccid, String customerId, String cardCostStatus);
}
