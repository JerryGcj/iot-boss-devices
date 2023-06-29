package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.DiscountPackage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 套餐折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-17
 * @Version: V1.0
 */
@Mapper
public interface DiscountPackageMapper extends BaseMapper<DiscountPackage> {
    @Select("select id from iot_discount_package where operator_id=#{operatorId}")
    List<String> getIdByOperatorId(String operatorId);
}
