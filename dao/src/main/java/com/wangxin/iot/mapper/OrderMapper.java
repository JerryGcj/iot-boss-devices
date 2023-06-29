package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * @Description: 订单列表
 * @Author: jeecg-boot
 * @Date:   2020-02-21
 * @Version: V1.0
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {
    /**
     * 根据卡号，更新卡状态
     * @param orderId
     * @param orderState
     * @return
     */
    @Update("update iot_order set order_state=#{orderState},update_date=now() where id=#{orderId} and order_state=#{oldOrderState}")
    int updateOrderStatus(@Param("orderId")String orderId,@Param("orderState")Integer orderState,@Param("oldOrderState")Integer oldOrderState);


    @Select("select id,order_id,iccid,mch_order_id,iccid,customer_id,package_id,package_name,buy_number,pay_state,order_state,create_time from iot_order " +
            "where customer_id=#{customerId} and order_id =#{orderId}")
    List<Order> getByOrderId(@Param("customerId") String customerId, @Param("orderId") String orderId);

    @Select("select id,order_id,iccid,mch_order_id,iccid,customer_id,package_id,package_name,buy_number,pay_state,order_state,create_time from iot_order " +
            "where customer_id=#{customerId} and mch_order_id =#{mchOrderId}")
    List<Order> getByMchOrderId(@Param("customerId") String customerId, @Param("mchOrderId") String mchOrderId);

}
