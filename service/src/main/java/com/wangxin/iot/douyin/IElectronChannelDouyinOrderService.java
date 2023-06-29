package com.wangxin.iot.douyin;

import com.baomidou.mybatisplus.extension.service.IService;
import com.doudian.open.api.order_orderDetail.data.ShopOrderDetail;
import com.doudian.open.api.order_searchList.data.ShopOrderListItem;
import com.wangxin.iot.douyin.entity.ElectronChannelDouyinOrder;
import com.wangxin.iot.douyin.entity.ElectronChannelOrderRegular;

import java.util.List;
import java.util.Map;

/**
 * @Description: electron_channel_douyin_order
 * @Author: jeecg-boot
 * @Date:   2022-12-06
 * @Version: V1.0
 */
public interface IElectronChannelDouyinOrderService extends IService<ElectronChannelDouyinOrder> {
    void pushExpressInfo(Long shopId,List<ElectronChannelOrderRegular> lists);
    /**
     * 作废原因同步到抖店
     * @param shopId
     * @param lists
     */
    void pushCancelMsg(Long shopId,List<ElectronChannelOrderRegular> lists);
    /**
     * 处理拉取下来的订单保存
     * @param shopOrderList
     */
    void handlerPullOrder(List<ShopOrderListItem> shopOrderList);
    void handlerPullOneOrder(ShopOrderDetail shopOrderDetail);

    /**
     * 将解密完的订单转化到regular表
     * @param orders
     */
    void transferRegularOrder(List<ElectronChannelDouyinOrder> orders);

    /**
     * 获取作废待推送的订单
     * @return
     */
    List<ElectronChannelOrderRegular> getCancelMsg(List<String> shopIds);

    /**
     * 获取到已发货未签收的订单，更新发货信息
     * @return
     */
    List<ElectronChannelOrderRegular> getExpressInfo(List<String> shopIds);

}
