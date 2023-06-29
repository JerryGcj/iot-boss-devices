package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.model.StandardCost;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 标准资费列表
 * @Author: jeecg-boot
 * @Date:   2020-01-16
 * @Version: V1.0
 */
@Mapper
public interface StandardCostMapper extends BaseMapper<StandardCost> {

    /**
     * 查询标准套餐信息
     * @param packageId
     * @param createUser
     * @return
     */
    @Select("SELECT s.* FROM `iot_standard_cost` s inner join iot_discount_package d on s.id = d.package_id inner join iot_terminal_sales_discount t on d.id = t.package_id and t.package_id= #{packageId} and t.create_user=#{createUser} and t.state=0")
    StandardCost getByTerminalSale(@Param("packageId") String packageId, @Param("createUser") String createUser);

    /**
     * 查询在当前时间内配置了每日限额的免费套餐的卡
     * @return
     */
    @Select("SELECT\n" +
            "\t* \n" +
            "FROM\n" +
            "\t`iot_telecom_ref_card_cost` \n" +
            "WHERE\n" +
            "\tcost_id IN ( SELECT id FROM iot_standard_cost WHERE free_type = 0 AND daily_limit >0 ) AND NOW() BETWEEN valid_start AND valid_end" +
            "\t")
    List<IotTelecomRefCardCost> getDailyLimit();

    /**
     * 配置了每日限制流量的套餐
     * @return
     */
    @Select("select id, daily_limit from iot_standard_cost WHERE free_type = 0 AND daily_limit >0 ")
    List<StandardCost> getDailyLimitCost();
}
