package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.Card;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface CardMapper extends BaseMapper<Card> {
    /**
     * 查询向上游查询的卡号
     * @return
     */
    @Select("select iccid from card where status = #{status} and NOW() <  ADDDATE(data_update_time,interval #{minute} MINUTE)")
    List<String> getNeedSyncCard(@Param("status") Integer status, @Param("minute") Integer minute);


    @Select("select iccid from card limit #{begin},#{end}")
    List<String> temp(@Param("begin") Integer begin, @Param("end") Integer end);
}