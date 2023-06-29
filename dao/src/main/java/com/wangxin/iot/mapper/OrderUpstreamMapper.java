package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.OrderUpstream;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @Description: 订单列表
 * @Author: jeecg-boot
 * @Date:   2020-02-21
 * @Version: V1.0
 */
@Mapper
public interface OrderUpstreamMapper extends BaseMapper<OrderUpstream> {

    default void saveUpstram(Order order){
        OrderUpstream orderUpstream = new OrderUpstream();
        orderUpstream.setIccid(order.getIccid());
        orderUpstream.setOutTradeNo(order.getOrderId());
        orderUpstream.setStatus(order.getPayState());
        orderUpstream.setErrorMsg(order.getNote());
        orderUpstream.setCreateTime(new Date());
        this.insert(orderUpstream);
    }
    default void saveOnelinkUpstream(OrderUpstream orderUpstream){
        orderUpstream.setRetryCount(1);
        orderUpstream.setCreateTime(new Date());
        this.insert(orderUpstream);
    }

    /**
     * 获取昨天和今天，并且重试次数小于5的
     * @param count
     * @return
     */
    @Select("select id,iccid,retry_count,mirror,status,error_msg from iot_order_upstream where  (status <> 1 or status is null) and source=#{source} and  retry_count<#{count} and create_time > date_sub(DATE_FORMAT(now(),'%Y-%c-%d'),INTERVAL 1 day)")
    List<OrderUpstream> getRecord(@Param("source") Integer source,@Param("count")Integer count);

    @Update("update iot_order_upstream set retry_count = retry_count+1,update_time=now(),status=#{status} where id=#{id} and retry_count = #{oldCount}")
    int incrRetryCount(@Param("id") String id,@Param("status") Integer status,@Param("oldCount") Integer oldCount);
}
