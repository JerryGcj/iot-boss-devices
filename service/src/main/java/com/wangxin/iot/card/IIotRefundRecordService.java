package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.model.IotRefundRecord;
import com.wangxin.iot.rest.base.Result;

/**
 * @Description: iot_refund_record
 * @Author: jeecg-boot
 * @Date:   2021-07-13
 * @Version: V1.0
 */
public interface IIotRefundRecordService extends IService<IotRefundRecord> {

    public void automation(IotRefundRecord iotRefundRecord);
}
