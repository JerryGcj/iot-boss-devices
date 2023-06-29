package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.IotCardOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * @Description: iot_card_order
 * @Author: jeecg-boot
 * @Date:   2021-09-29
 * @Version: V1.0
 */
@Mapper
public interface IotCardOrderMapper extends BaseMapper<IotCardOrder> {

    @Update("update iot_card_order set order_state=#{orderState},update_time=now() where id=#{orderId} and order_state=#{oldOrderState}")
    int updateOrderStatus(@Param("orderId")String orderId, @Param("orderState")String orderState, @Param("oldOrderState")String oldOrderState);
}
