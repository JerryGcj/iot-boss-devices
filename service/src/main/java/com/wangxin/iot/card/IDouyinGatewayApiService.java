package com.wangxin.iot.card;

import com.wangxin.iot.douyin.entity.ElectronChannelDouyinOrder;

import java.util.List;

/**
 * @author anan
 * @date 2022/12/6 09:21
 */
public interface IDouyinGatewayApiService {
    void pullOneOrder(String shopId,String orderId);
    /**
     * 拉取抖店订单
     * @param page
     * @param shopId
     * @param startTime
     */
    void pullOrder(Long page,String shopId,Long startTime,Long endTime);
    /**
     * 拉取的订单解密
     */
    void decrypt(String shopId,List<ElectronChannelDouyinOrder> toDecryptLists);

}
