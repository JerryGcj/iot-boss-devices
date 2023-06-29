package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.SysUser;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface SysUserMapper extends BaseMapper<SysUser> {
    @MapKey("iccid")
    @Select("select c.iccid,channel_id,the_key from sys_user u inner join iot_card_information c on c.customer_id=u.id and c.iccid=#{iccid}")
    Map<String, Map<String,String>> getUserScreat(String iccid);

    @Select("select id,status,del_flag,ip_white,the_key,balance from sys_user where username=#{username}")
    SysUser queryUser(String username);
}
