package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.TerminalSalesDiscount;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description: 终端销售折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Mapper
public interface TerminalSalesDiscountMapper extends BaseMapper<TerminalSalesDiscount> {

}
