package com.wangxin.iot.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.CustomerSalesDiscount;
import com.wangxin.iot.model.TerminalSalesDiscount;
import org.apache.catalina.User;
import org.apache.ibatis.annotations.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * @Description: 客户销售折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Mapper
public interface CustomerSalesDiscountMapper extends BaseMapper<CustomerSalesDiscount> {

    @Update("update sys_user set balance=balance-#{balance} where id=#{userId}")
    int updateUserBalance(@Param("userId") String userId, @Param("balance")BigDecimal balance);

    @Select("select balance from sys_user where id=#{id}")
    BigDecimal getUserById(String id);

    @Select("select balance,id from sys_user where username=#{username}")
    Map getUserByUsername(String username);

    @Select("select id,username,balance,create_by from sys_user")
    @MapKey("username")
    Map<String, Map<String,Object>> getTopUsername(String username);

    @MapKey("id")
    @Select("select id,channel_id,the_key from sys_user where id=#{id}")
    Map<String, Map<String,String>> getUserScreat(String id);

    @Select("select operator_type,package_id,package_name,sales_price from iot_customer_sales_discount where agent_id=#{agentId}")
    List<CustomerSalesDiscount> queryByUserId(@Param("agentId")String agentId);

    @Select("select operator_type,package_id,package_name,sales_price from iot_customer_sales_discount where agent_id=#{agentId} and package_id=#{packageId}")
    CustomerSalesDiscount queryByIds(@Param("agentId")String agentId, @Param("packageId")String packageId);

}
