package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.card.ICustomerSalesDiscountService;
import com.wangxin.iot.mapper.AgentReduceRecordMapper;
import com.wangxin.iot.mapper.CustomerSalesDiscountMapper;
import com.wangxin.iot.model.CustomerSalesDiscount;
import com.wangxin.iot.model.Order;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description: 客户销售折扣管理
 * @Author: jeecg-boot
 * @Date:   2020-01-19
 * @Version: V1.0
 */
@Service
@Slf4j
public class CustomerSalesDiscountServiceImpl implements ICustomerSalesDiscountService {
    @Autowired
    CustomerSalesDiscountMapper customerSalesDiscountMapper;
    @Autowired
    AgentReduceRecordMapper agentReduceRecordMapper;

    @Override
    public Map<String, String> getUserScreat(String userId) {
        Map<String, Map<String, String>> userScreat = customerSalesDiscountMapper.getUserScreat(userId);
        return userScreat.get(userId);
    }

    @Override
    @Async
    public void updateUserBalance(Order order) {
        //(目前只扣 一级代理商 太仓优漫通讯贸易有限公司,   刘辉, 的钱)
        try {
            String union = this.getTopUserName(order.getCreateUser());
            String[] split = union.split(";");
            String topUsername = split[0];
            String topUserId = split[1];
            BigDecimal balance = new BigDecimal(split[2]);
            log.info("顶级代理是{}，余额是：{}，当前代理：{}",topUsername,balance,order.getCreateUser());
            if("tcymtx".equals(topUsername) || "liuhui".equals(topUsername)){
                CustomerSalesDiscount customerSalesDiscount = customerSalesDiscountMapper.selectOne(new QueryWrapper<CustomerSalesDiscount>().eq("package_id", order.getPackageId()).eq("agent_id", topUserId));
                //得到这个卡一级代理配置的成本价
                BigDecimal salesPrice = customerSalesDiscount.getSalesPrice();
                if(salesPrice.compareTo(balance)>0){
                    log.info("代理商：{} 余额不足，余额{}",topUsername,balance);
                }
                int i = customerSalesDiscountMapper.updateUserBalance(topUserId, salesPrice);
                if(i != 1){
                    log.info("代理商：{} 减余额失败：余额{}",topUsername,balance);
                }else{
                    log.info("代理商：{} 减余额成功：余额{}",topUsername,balance.subtract(salesPrice));

                }
                //扣减金额记录流水表
                agentReduceRecordMapper.saveRecord(order,salesPrice);
            }
        }catch (Exception e){
            e.printStackTrace();
            log.error("扣代理商钱失败，订单：{}",order);
        }
    }

    public String getTopUserName(String username){
        Map<String, Map<String, Object>> topUsername = customerSalesDiscountMapper.getTopUsername(username);
        return this.getParentName(username,topUsername);
    }

    /**
     * 获取除admin外最顶级的父username
     * @param username
     * @param relation
     * @return
     */
    public String getParentName(String username,Map<String, Map<String, Object>> relation){
        Map<String, Object> stringStringMap = relation.get(username);
        String createBy = stringStringMap.get("create_by").toString();
        if("admin".equals(createBy)){
            return username+";"+stringStringMap.get("id")+";"+stringStringMap.get("balance").toString();
        }
        return this.getParentName(createBy,relation);

    }
}
