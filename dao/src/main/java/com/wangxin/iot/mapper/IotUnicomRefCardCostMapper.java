package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.domain.RefCardModel;
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
public interface IotUnicomRefCardCostMapper extends BaseMapper<IotUnicomRefCardCost> {
    /**
     * 查询未生效的卡套餐
     * @param active
     * @return
     */
    @Select("SELECT a.iccid,b.sim_status,b.id as cardId,a.id as refId" +
            " FROM `iot_unicom_ref_card_cost` a " +
            "INNER JOIN iot_unicom_card_info b ON a.iccid = b.iccid AND a.active = #{active} and (a.refund_status not in('0','1') or a.refund_status is null or a.refund_status='') AND DATE_ADD(now(),INTERVAL 1 HOUR) > valid_start")
    List<Map<String,String>> getUnActiveCard(@Param("active") Integer active);
    /**
     * 获取提前生效的套餐条数
     * @param iccid
     * @param active
     * @return
     */
    @Select("select count(1) from iot_unicom_ref_card_cost where  iccid=#{iccid} and active=#{active} and cost_type=#{costType}")
    Integer getInAdvancePackageCount(@Param("iccid") String iccid, @Param("active") Integer active,@Param("costType") Integer costType);

    @Select("select id,valid_start from iot_unicom_ref_card_cost where iccid=#{iccid} and cost_type=0 and active=#{active} and valid_start <= #{endTime}")
    Map getNextPackageIn24Hour(@Param("iccid") String iccid, @Param("active") Integer active, @Param("endTime") Date endTime);
    /**
     * 获取需要同步的套餐
     * @param active
     * @return
     */
    @Select("select id,parent_id,iccid,cost_type,valid_start,valid_end from iot_unicom_ref_card_cost where active=#{active}")
    List<RefCardModel> getNeedSync(String active);

    @Select("select * from iot_unicom_ref_card_cost where parent_id=#{parentId}")
    List<IotUnicomRefCardCost> getOilByParentId(String parentId);


    @Update("update iot_unicom_ref_card_cost set usaged = #{usaged},update_time= now() where active= #{active} and iccid=#{iccid} and cost_type=0")
    Integer updateUsage(@Param("usaged") BigDecimal usaged, @Param("active") String active, @Param("iccid") String iccid);



    @Select("select id,iccid,cost_type,valid_start,valid_end from iot_unicom_ref_card_cost" +
            " where iccid=#{iccid} and cost_type=0  and active=1 and now() between valid_start and valid_end ")
    RefCardModel getBasicRef(@Param("iccid") String  iccid);

    @Select("select id,iccid,cost_type,valid_start,valid_end from iot_unicom_ref_card_cost where id=#{model.parentId}")
    RefCardModel getBasicRefByOilWithParentId(@Param("model") RefCardModel refCardModel);

    @Select("select id,valid_end from iot_unicom_ref_card_cost where iccid=#{iccid} and cost_type=#{type} and #{currentDate} between valid_start and valid_end order by create_time desc limit 1" )
    Map getByIccid(@Param("iccid") String iccid, @Param("type") String type, @Param("currentDate") Date currentDate);

    @Select("select id,iccid from iot_unicom_ref_card_cost where NOW() BETWEEN valid_start and valid_end and cost_type=0 and (active=2 or active=3) and usaged*1.66<100000 and TIMESTAMPDIFF(HOUR,NOW(),valid_end)>24")
    List<IotUnicomRefCardCost> getIccidByActive();

    @Select("select id,iccid,cost_type from iot_unicom_ref_card_cost where active=1 and valid_end<NOW()")
    List<IotUnicomRefCardCost> getOverdueActived();

    @Select("select id,iccid,cost_type,active from iot_unicom_ref_card_cost where iccid=#{iccid} and active in (0,1) and now() between valid_start and valid_end")
    List<IotUnicomRefCardCost> getOverdueActived(@Param("iccid") String  iccid);

    /**
     * 没有生效中的套餐，依然还在激活状态
     * @return
     */
    @Select("select res.iccid from (SELECT a.iccid,b.iccid as biccid,b.cs FROM `iot_unicom_card_info` a left join (select iccid,count(1) as cs from iot_unicom_ref_card_cost where active=1 GROUP BY iccid) b on a.iccid=b.iccid WHERE a.sim_status =2 ) res where res.cs is null")
    List<String> getToStopIccid();
}
