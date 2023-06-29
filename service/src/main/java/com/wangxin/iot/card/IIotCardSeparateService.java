package com.wangxin.iot.card;

import com.baomidou.mybatisplus.extension.service.IService;
import com.wangxin.iot.domain.IotCardSeparateEntity;

import java.util.List;
import java.util.Map;

/**
 * Created by 18765 on 2020/1/4 11:22
 */

public interface IIotCardSeparateService extends IService<IotCardSeparateEntity> {
    /**
     * 我们平台正常有套餐，移动停机，复机，并且将异常停机卡记录
     * @param lists
     */
    void syncStopReasonAndRestartCard(List<Map<String, String>> lists);
}
