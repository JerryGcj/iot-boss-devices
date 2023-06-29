package com.wangxin.iot.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.wangxin.iot.model.AgentReduceRecord;
import com.wangxin.iot.model.Order;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.beans.BeanUtils;

import java.math.BigDecimal;
import java.util.Date;

@Mapper
public interface AgentReduceRecordMapper extends BaseMapper<AgentReduceRecord> {

    default void saveRecord(Order order, BigDecimal money){
        AgentReduceRecord agentReduceRecord = new AgentReduceRecord();
        BeanUtils.copyProperties(order,agentReduceRecord);
        agentReduceRecord.setId(null);
        agentReduceRecord.setCreateTime(new Date());
        agentReduceRecord.setMoney(money);
        agentReduceRecord.setAgentName(order.getCompanyName());
        agentReduceRecord.setOperatorType(2);
        agentReduceRecord.setAgentId(order.getCustomerId());
        //区分哪个运营商
        agentReduceRecord.setOperatorType(1);
        this.insert(agentReduceRecord);
    }
}