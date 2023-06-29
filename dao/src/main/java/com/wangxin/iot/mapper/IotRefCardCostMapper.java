package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.model.IotRefCardCost;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/2/20
 */
@Mapper
public interface IotRefCardCostMapper extends BaseMapper<IotRefCardCost> {

    @Update("update iot_ref_card_cost set usaged = #{usaged},update_time= now() where active= #{active} and card_iccid=#{iccid} and cost_type=0")
    Integer updateUsage(@Param("usaged")BigDecimal usaged,@Param("active") String active,@Param("iccid") String iccid);

    @Update("update iot_ref_card_cost set active = active- 1 where  card_iccid=#{iccid}")
    Integer updateActive(@Param("iccid") String iccid);

    @Select("select count(1) from real_name_system where iccid=#{iccid} and status=#{status} ORDER BY create_time desc limit 1")
    Integer getRealNameInfo(@Param("iccid") String iccid, @Param("status") String status);

    @Select("select id,card_iccid,cost_type,valid_start,valid_end from iot_ref_card_cost where active=#{active}")
    List<RefCardModel> getNeedSync(String active);

    @Select("select id,card_iccid,cost_type,valid_start,valid_end from iot_ref_card_cost" +
            " where card_iccid=#{model.cardIccid} and cost_type=0 and #{model.validStart} between valid_start and valid_end ")
    RefCardModel getBasicRefByOil(@Param("model")RefCardModel refCardModel);

    @Select("select id,valid_end from iot_ref_card_cost where card_iccid=#{iccid} and cost_type=#{type} and #{currentDate} between valid_start and valid_end order by create_time desc limit 1" )
    RefCardModel getByIccid(@Param("iccid")String iccid, @Param("type")String type, @Param("currentDate")Date currentDate);

    @Select("select * from iot_ref_card_cost where parent_id=#{parentId}")
    List<IotRefCardCost> getOilByParentId(String parentId);

    @Select("select id,valid_start from iot_ref_card_cost where card_iccid=#{iccid} and cost_type=0 and active=#{active} and valid_start <= #{endTime}")
    Map getNextPackageIn24Hour(@Param("iccid") String iccid, @Param("active") Integer active, @Param("endTime") Date endTime);

    /**
     * 获取提前生效的套餐条数
     * @param iccid
     * @param active
     * @return
     */
    @Select("select count(1) from iot_ref_card_cost where  card_iccid=#{iccid} and active=#{active} and cost_type=#{costType}")
    Integer getInAdvancePackageCount(@Param("iccid") String iccid, @Param("active") Integer active,@Param("costType") Integer costType);

    /**
     * 查询未生效的卡套餐
     * @param active
     * @return
     */
    @Select("SELECT a.card_iccid,b.card_state,b.id as cardId,a.id as refId" +
            " FROM `iot_ref_card_cost` a " +
            "INNER JOIN iot_card_information b ON a.card_iccid = b.iccid AND a.active = #{active} AND DATE_ADD(now(),INTERVAL 1 HOUR) > valid_start")
    List<Map<String,String>> getUnActiveCard(@Param("active") Integer active);

}
