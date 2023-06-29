package com.wangxin.iot.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.domain.CardInformationModel;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.domain.RefCardMonitorModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Description: iot_telecom_card_info
 * @Author: jeecg-boot
 * @Date:   2020-07-16
 * @Version: V1.0
 */
@Mapper
public interface IotTelecomCardInfoMapper extends BaseMapper<IotTelecomCardInfo> {
    /**
     * 根据接入号，查询通道
     * @param accessNumber
     * @return
     */
    @Select("select operator_id from iot_telecom_card_info where access_number=#{accessNumber}")
    String getOperatorIdByAccessNumber(String accessNumber);

    /**
     * 根据Iccid，查运营商
     * @param iccid
     * @return
     */
    @Select("select operator_id from iot_telecom_card_info where iccid=#{iccid}")
    String getOperatorIdByIccid(String iccid);

    List<IotTelecomCardInfo> getTelecomCardByIccid(@Param("iccid") String iccid,@Param("active")String active);
    /**
     * 查询激活或者停用状态的卡
     * @return
     */
    @Select("select id,iccid,sim_status,custom_package_use from iot_telecom_card_info where  sim_status in(4,5) ")
    List<CardInformationModel> getByStatus();
    /**
     * 查询激活的卡，并且数据用量在两小时之内没有更新过的卡
     * @param status
     * @param hour
     * @return
     */
    @Select("SELECT iccid  FROM iot_telecom_card_info WHERE sim_status = #{status} AND DATE_ADD( data_usage_change_time,INTERVAL #{hour} HOUR ) <= NOW()")
    List<String> getIccidsByActive(@Param("status") String status, @Param("hour") Integer hour);

    /**
     * 同步卡用量,联合卡表了
     * @param status
     * @param active
     * @param hour
     * @return
     */
    List<RefCardModel> getRefModel(@Param("status") Integer status, @Param("active") Integer active, @Param("hour") Integer hour);

    /**
     * 只查套餐表
     * @param hour
     * @return
     */
    List<RefCardModel> getSyncRefModel(@Param("hour") Integer hour);

    /**
     * 要查询的卡号
     * @param hour
     * @return
     */
    List<String> getSyncIccids(@Param("hour") Integer hour);

    /**
     * 定时监控要停机的卡套餐，只查时间快到期的和用量比较多的卡
     * @param multiple
     * @param days
     * @return
     */
    List<RefCardMonitorModel> monitorCard(@Param("multiple") BigDecimal multiple, @Param("days") Integer days, @Param("simStatus") Integer simStatus);

    @Update("update iot_telecom_card_info set sim_status=#{simStatus} where access_number=#{accessNumber}")
    void updateStatus(@Param("simStatus") Integer simStatus,@Param("accessNumber") String accessNumber);

    @Select("SELECT access_number FROM iot_telecom_card_info WHERE sim_status = #{status}")
    List<String> getIccidByStatus(Integer status);
}
