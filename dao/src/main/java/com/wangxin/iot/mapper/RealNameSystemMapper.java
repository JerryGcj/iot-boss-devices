package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.RealNameSystem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RealNameSystemMapper extends BaseMapper<RealNameSystem> {

    @Update("update real_name_system set id_card_number=#{idCardNumber},status='1' where msisdn=#{msisdn}")
    int editStatus(@Param("idCardNumber") String idCardNumber, @Param("msisdn") String msisdn);

    @Update("update real_name_system set status='1' where iccid=#{iccid}")
    int editUnicomStatus(@Param("iccid") String iccid);

    @Update("update real_name_system set retry_count=retry_count+1,update_time=now() where msisdn=#{msisdn}")
    int addCount(@Param("msisdn") String msisdn);

    @Select("select iccid from real_name_system where operator_type=2 and status in ('0','2')")
    List<String> iccids();
}
