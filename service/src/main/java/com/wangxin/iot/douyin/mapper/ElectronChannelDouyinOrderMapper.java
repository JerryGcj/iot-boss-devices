package com.wangxin.iot.douyin.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.douyin.entity.ElectronChannelDouyinOrder;
import com.wangxin.iot.douyin.entity.ElectronChannelOrderRegular;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Description: electron_channel_douyin_order
 * @Author: jeecg-boot
 * @Date:   2022-12-06
 * @Version: V1.0
 */
@Mapper
public interface ElectronChannelDouyinOrderMapper extends BaseMapper<ElectronChannelDouyinOrder> {


    /**
     * 获取作废待推送的订单
     * @return
     */
    List<ElectronChannelOrderRegular> getCancelMsg(List<String> shopIds);

    /**
     * 获取已发货未签收的订单
     * @return
     */
    List<ElectronChannelOrderRegular> getExpressInfo(List<String> shopIds);
}
