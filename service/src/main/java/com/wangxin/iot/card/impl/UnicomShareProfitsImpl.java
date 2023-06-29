package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IUnicomShareProfitsService;
import com.wangxin.iot.mapper.CustomerSalesDiscountMapper;
import com.wangxin.iot.mapper.ShareProfitsMapper;
import com.wangxin.iot.model.CustomerSalesDiscount;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.ShareProfits;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author: yanwin
 * @Date: 2020/4/27
 */
@Service
@Slf4j
public class UnicomShareProfitsImpl extends ServiceImpl<ShareProfitsMapper, ShareProfits> implements IUnicomShareProfitsService {

    @Autowired
    CustomerSalesDiscountMapper customerSalesDiscountMapper;

    @Override
    @Async
    public void shareProfit(Order order) {
        try {
            //查到所有套餐折扣用户信息
            List<CustomerSalesDiscount> customerSalesDiscounts = customerSalesDiscountMapper.selectList(null);
            //要分润的记录
            List<ShareProfits> doSaveProfits = new ArrayList<>();
            //最底层客户，子节点
            CustomerSalesDiscount leafAgent = customerSalesDiscounts.stream().filter(
                    item -> item.getAgentId().equals(order.getCustomerId()) && item.getPackageId().equals(order.getPackageId())).findAny().get();
            BigDecimal salesPrice = leafAgent.getSalesPrice();
            if(Integer.valueOf(order.getBuyNumber())>1){
                //若一个订单购买了多份
                salesPrice = salesPrice.multiply(new BigDecimal(order.getBuyNumber()));
            }
            //当前节点分润明细
            BigDecimal shareMoney = order.getTradingMoney().subtract(salesPrice);
            doSaveProfits.add(this.assmbleProfit(order,leafAgent,shareMoney,null));
            //递归处理其余节点的分润明细
            this.handlerProfit(order,leafAgent,customerSalesDiscounts,doSaveProfits);
            if(!saveBatch(doSaveProfits)){
                log.info("批量保存分润明细失败：订单如下{}",doSaveProfits);
            }
        }catch (Exception e){
            log.error("代理商分润异常，订单：{}",order);
            e.printStackTrace();
        }

    }


    private void handlerProfit(Order order, CustomerSalesDiscount leafAgent, List<CustomerSalesDiscount> customerSalesDiscounts, List<ShareProfits> doSaveProfits){
        //分润截至到admin
        if("admin".equals(leafAgent.getCreateUser())){
            return;
        }
        //当前代理商的父代理商
        CustomerSalesDiscount parentCustomer = customerSalesDiscounts.stream().filter(
                item -> item.getAgentUsername().equals(leafAgent.getCreateUser())  &&
                        item.getPackageId().equals(order.getPackageId()))
                .findFirst().get();
        //算出的分润明细
        BigDecimal shareMoney = leafAgent.getSalesPrice().subtract(parentCustomer.getSalesPrice());
        if(Integer.valueOf(order.getBuyNumber())>1){
            shareMoney = shareMoney.multiply(new BigDecimal(order.getBuyNumber()));
        }
        ShareProfits shareProfits = this.assmbleProfit(order, leafAgent,shareMoney, parentCustomer);
        //分润单放集合中
        doSaveProfits.add(shareProfits);
        //递归，继续寻找上一级分润明细
        this.handlerProfit(order,parentCustomer,customerSalesDiscounts,doSaveProfits);
    }

    /**
     * 通过订单，找到要分润的数据
     * @param order
     * @param leafAgent
     * @param shareMoney
     * @param parentCustomer
     * @return
     */
    private ShareProfits assmbleProfit(Order order, CustomerSalesDiscount leafAgent, BigDecimal shareMoney, CustomerSalesDiscount parentCustomer){
        ShareProfits shareProfits = new ShareProfits();
        //联通的卡分润情况
        shareProfits.setStatus("0");
        shareProfits.setShareOrderNo(order.getOrderId());
        shareProfits.setOrderNo(order.getOrderId());
        shareProfits.setPayOrderNo(order.getPayOrderId());
        shareProfits.setIccid(order.getIccid());
        shareProfits.setPackageId(order.getPackageId());
        shareProfits.setPackageName(order.getPackageName());
        shareProfits.setPackageMoney(order.getTradingMoney());
        shareProfits.setPurchaseQuantity(order.getBuyNumber());
        //运营商联通类型
        shareProfits.setOperatorType(String.valueOf(order.getOperatorType()));
        shareProfits.setCreateTime(new Date());
        if(parentCustomer == null){
            shareProfits.setLowerAgent("终端用户");
            shareProfits.setLowerAgentId(null);
            shareProfits.setHigherAgent(leafAgent.getAgentName());
            shareProfits.setHigherAgentId(leafAgent.getAgentId());
        }else{
            shareProfits.setLowerAgent(leafAgent.getAgentName());
            shareProfits.setLowerAgentId(leafAgent.getAgentId());
            shareProfits.setHigherAgent(parentCustomer.getAgentName());
            shareProfits.setHigherAgentId(parentCustomer.getAgentId());
        }
        shareProfits.setShareMoney(shareMoney);
        return shareProfits;
    }
}
