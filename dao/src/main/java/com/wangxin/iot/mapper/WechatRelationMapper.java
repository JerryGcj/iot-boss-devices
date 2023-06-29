package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.IotCardWechatRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface WechatRelationMapper extends BaseMapper<IotCardWechatRelation> {

    @Update("update iot_card_wechat_relation set mobile=#{mobile} where iccid=#{iccid}")
    int updateMobile(@Param("mobile")String mobile, @Param("iccid")String iccid);
}