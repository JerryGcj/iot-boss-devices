package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.domain.CardInformationModel;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.MobileRefCardMonitorModel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper
public interface CardInformationMapper extends BaseMapper<CardInformation> {
    @Select("select iccid from iot_card_information where  card_state <>#{status}")
    List<String> getIccidsExcludeState(@Param("status") Integer status);

    @Select("SELECT iccid FROM `iot_card_information` where operator_id='a6072615ea9740698fb65cd3675cc384' and msisdn is null limit 100")
    List<String> getIccid();
    /**
     * 查询正常或者停机的卡
     * @return
     */
    @Select("select id,iccid,card_state,custom_package_use,customer_id from iot_card_information where card_state in(3,4) ")
    List<CardInformationModel> getByStatus();
    /**
     * 根据开始结束卡号，查卡号
     * @param start
     * @param end
     * @return
     */
    @Select("select iccid from iot_card_information where iccid between #{start} and #{end}")
    List<String> getIccidByRegion(@Param("start")String start,@Param("end")String end);


    @Select("select DISTINCT a.iccid  from iot_card_information a inner join iot_ref_card_cost b on a.iccid=b.card_iccid where b.active=#{active}")
    List<String> getIccidsByActiveCost(String active);

    @Select("select DISTINCT a.msisdn,a.iccid  from iot_card_information a inner join iot_ref_card_cost b on a.iccid=b.card_iccid where b.active=#{active}")
    List<Map<String,String>> getMsisdnIccidByActiveCost(String active);
    /**
     * 根据运营商，查询对应的物联网卡。
     * @param operationId
     * @return
     */
    @Select("select iccid from iot_card_information where operator_id = #{operationId} and card_state =#{status}")
    List<String> getIccidByOperationId(@Param("operationId")String operationId,@Param("status") String status);

    @Select("select operator_id from iot_card_information where iccid = #{iccid}")
    String getOperationIdByIccid(@Param("iccid")String iccid);

    @Select("select is_real_name_authentication from iot_card_information where iccid = #{iccid}")
    String getRealNameByIccid(@Param("iccid")String iccid);

    List<CardInformation>  getAll();

    /**
     * 查询卡状态正常并且激活时间为空的卡
     * @param
     * @return
     */
    @Select("select iccid from iot_card_information where card_state = '3' and activation_time is null")
    List<String> getIccidsByActiveDate();

    /**
     * 监控将要符合停机条件的卡
     * @param multiple
     * @param days
     * @param simStatus
     * @return
     */
    List<MobileRefCardMonitorModel> monitorCard(@Param("multiple") BigDecimal multiple, @Param("days")Integer days, @Param("simStatus")Integer simStatus);

    /**
     * 根据订单id去获取要充值套餐的自定义倍数
     * @param id
     * @return
     */
    @Select("select IFNULL(t.custom_package_use,c.custom_package_use) as custom_package_use FROM iot_card_information c INNER JOIN iot_order o on c.iccid=o.iccid left join " +
            "iot_terminal_sales_discount t on o.terminal_id=t.id where o.id=#{id}")
    BigDecimal packageUse(@Param("id")String id);
}
