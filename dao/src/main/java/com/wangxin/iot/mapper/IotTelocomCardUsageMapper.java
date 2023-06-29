package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.domain.IotTelecomCardUsage;
import com.wangxin.iot.domain.RefCardModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Mapper
public interface IotTelocomCardUsageMapper extends BaseMapper<IotTelecomCardUsage> {
    /**
     * 更新某一天的用量
     * @param usage
     * @param iccid
     * @param date
     * @return
     */
    @Update("update iot_telecom_card_usage set card_usage=#{usage} where iccid=#{iccid} and date =#{date}")
    int updateUsage(@Param("usage") BigDecimal usage, @Param("iccid") String iccid, @Param("date") LocalDate date);

    @Select("SELECT sum(card_usage) FROM `iot_telecom_card_usage` WHERE iccid=#{model.iccid} and date BETWEEN DATE_FORMAT(#{model.validStart},'%Y-%m-%d') and DATE_FORMAT(#{model.validEnd},'%Y-%m-%d')")
    BigDecimal getPeriodUsage(@Param("model") RefCardModel refCardModel);

    @Select("SELECT sum( card_usage ) FROM `iot_telecom_card_usage` WHERE  iccid = #{iccid} and DATE_FORMAT(date,'%Y-%m') = DATE_FORMAT(#{localDate},'%Y-%m') ")
    BigDecimal getCurrentMonthUsage(@Param("iccid") String iccid, @Param("localDate") LocalDate localDate);

    /**
     * 联通平台获取某个周期内的流量用量
     * @param iccid
     * @param periodStart
     * @param periodEnd
     * @return
     */
    @Select("SELECT sum( card_usage ) FROM `iot_telecom_card_usage` WHERE  iccid = #{iccid} and date between #{periodStart} and #{periodEnd} ")
    BigDecimal getCurrentPeriodUsage(@Param("iccid") String iccid, @Param("periodStart") LocalDate periodStart, @Param("periodEnd") LocalDate periodEnd);


    @Select("select count(1) from iot_telecom_card_usage where iccid=#{iccid} and date=#{date}")
    int getCountByIccidAndDate(@Param("iccid") String iccid, @Param("date") String date);

}
