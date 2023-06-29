package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.ICustomerSalesDiscountService;
import com.wangxin.iot.card.IIotTelecomCardInfoService;
import com.wangxin.iot.card.IotTelecomRefCardCostService;
import com.wangxin.iot.constants.OrderStatus;
import com.wangxin.iot.domain.IotTelecomCardInfo;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.mapper.DiscountPackageMapper;
import com.wangxin.iot.mapper.IotTelecomRefCardCostMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.StandardCostMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.StandardCost;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author: yanwin
 * @Date: 2020/5/25
 */
@Service
@Slf4j
public class TelecomRefCardCostServiceImpl  extends ServiceImpl<IotTelecomRefCardCostMapper, IotTelecomRefCardCost> implements IotTelecomRefCardCostService {
    @Autowired
    DiscountPackageMapper discountPackageMapper;
    @Autowired
    StandardCostMapper standardCostMapper;
    @Autowired
    ICustomerSalesDiscountService customerSalesDiscountService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    IIotTelecomCardInfoService telecomCardInfoService;

    @Override
    public synchronized void  saveRefWithOrder(Order order){

        StandardCost standardCost = standardCostMapper.selectById(order.getPackageId());
        BigDecimal containsFlow = standardCost.getContainsFlow();
        //BigDecimal number = new BigDecimal(order.getBuyNumber());
        //BigDecimal originUse = containsFlow.divide(number);
        String basicPackage = standardCost.getBasicPackage();
        Date currentDate = DateUtils.getCurrentDate();
        IotTelecomRefCardCost iotRefCardCost = new IotTelecomRefCardCost();
        iotRefCardCost.setUsaged(BigDecimal.ZERO);
        iotRefCardCost.setInitUsaged(BigDecimal.ZERO);
        iotRefCardCost.setOrderId(order.getId());
        iotRefCardCost.setCostId(standardCost.getId());
        iotRefCardCost.setIccid(order.getIccid());
        iotRefCardCost.setCostType(Integer.parseInt(standardCost.getBasicPackage()));
        iotRefCardCost.setOriginUse(standardCost.getContainsFlow());
        iotRefCardCost.setCostName(standardCost.getPackageName());
        iotRefCardCost.setFreeType(standardCost.getFreeType());
        iotRefCardCost.setCreateTime(currentDate);
        iotRefCardCost.setAccessNumber(order.getAccessNumber());
        //基础套餐类型
        String inclusionType = standardCost.getInclusionType();

        QueryWrapper<IotTelecomRefCardCost> iotRefCardCostQueryWrapper = new QueryWrapper<>();
        //没订过套餐，基础包到期失效的，再次订购，直接生效
        iotRefCardCostQueryWrapper.eq("iccid", order.getIccid());
        //基础包
        if(basicPackage.equals("0")){
            iotRefCardCostQueryWrapper.eq("cost_type","0");
        }else if(basicPackage.equals("1")){
            //加油包
            iotRefCardCostQueryWrapper.eq("cost_type","1");
        }
        //当前卡 所有的套餐
        List<IotTelecomRefCardCost> list = this.list(iotRefCardCostQueryWrapper);


        //当前时间有失效的套餐
        List<IotTelecomRefCardCost> disRef = list.stream().filter(item -> (item.getActive() == 2 || item.getActive() == 3) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());

        //当前时间有生效的套餐
        List<IotTelecomRefCardCost> enableRef = list.stream().filter(item -> (item.getActive() == 0 || item.getActive() == 1) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());
        //从来没订过， //订购的是加油包  //当前时间内，既没有失效的套餐，也没有生效的套餐
        if( CollectionUtils.isEmpty(list) ||
                "1".equals(basicPackage)||
                (CollectionUtils.isEmpty(disRef) && CollectionUtils.isEmpty(enableRef))){
            iotRefCardCost.setActive(1);
            iotRefCardCost.setValidStart(currentDate);
            //加油包
            if(basicPackage.equals("1")){
                //加油包的结束日期是基础包的结束日期
                Map map = this.baseMapper.getByIccid(order.getIccid(), "0", currentDate);
                iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(map.get("valid_end").toString()));
                iotRefCardCost.setParentId(map.get("id").toString());
            }else{
                //按照自然月计费
                if("0".equals(inclusionType)){
                    //当前时间+延后几个月
                    iotRefCardCost.setValidEnd(DateUtils.addMonths2CurrentDate(standardCost.getPeriodOfValidity()));
                }else if("1".equals(inclusionType)){
                    log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置失效时间：{}",order.getIccid(),DateUtils.addDays2CurrentDate(standardCost.getPeriodOfValidity()));
                    //按使用时间计费
                    iotRefCardCost.setValidEnd(DateUtils.addDays2CurrentDate(standardCost.getPeriodOfValidity()));
                }
            }
        }else{
            //当前有失效套餐，没有生效套餐
            if(CollectionUtils.isNotEmpty(disRef) && CollectionUtils.isEmpty(enableRef)){
                //找到介于当前时间内的失效套餐
                List<IotTelecomRefCardCost> sortDisRef = disRef.stream().sorted(Comparator.comparing(IotTelecomRefCardCost::getCreateTime).reversed()).collect(Collectors.toList());
                //说明当前时间存在基础套餐，流量用超或者失效了
                if(CollectionUtils.isNotEmpty(sortDisRef)){
                    iotRefCardCost.setActive(0);
                    Date start = sortDisRef.get(0).getValidEnd();
                    log.info("卡号：{}，有失效没有生效的套餐，开始设置生效时间：{}",order.getIccid(),start);
                    //如果订购时间正好距离下一份套餐包的开始时间不到一个小时，那么设置为生效状态。
                    //自动停复机定时任务，提前一小时将套餐设置为停机状态。
                    if(DateUtils.addHour2Date(currentDate,1).compareTo(start)>0){
                        iotRefCardCost.setActive(1);
                    }
                    iotRefCardCost.setValidStart(start);
                    //按照自然月计费
                    if("0".equals(inclusionType)){
                        //当前时间+延后几个月
                        iotRefCardCost.setValidEnd(DateUtils.addMonths2Date(start,standardCost.getPeriodOfValidity()));
                    }else if("1".equals(inclusionType)){
                        log.info("卡号：{}，有失效没有生效的套餐，开始设置失效时间：{}",order.getIccid(),DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                        //按使用时间计费
                        iotRefCardCost.setValidEnd(DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                    }
                }
            }else{
                //下一份套餐的开始时间计算(按照生效时间倒叙排序)
                List<IotTelecomRefCardCost> sortRef = list.stream().filter(item->item.getActive() == 0 || item.getActive() == 1)
                        .sorted(Comparator.comparing(IotTelecomRefCardCost::getValidEnd).reversed())
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(sortRef)){
                    IotTelecomRefCardCost iotRefCardCost1 = sortRef.get(0);
                    Date start = iotRefCardCost1.getValidEnd();
                    log.info("卡号：{}，其它情况配置套餐，开始设置生效时间：{}",order.getIccid(),start);
                    //开始时间
                    iotRefCardCost.setValidStart(start);
                    //未生效
                    iotRefCardCost.setActive(0);
                    //按照自然月计费
                    if("0".equals(inclusionType)){
                        //当前时间+延后几个月
                        iotRefCardCost.setValidEnd(DateUtils.addMonths2Date(start,standardCost.getPeriodOfValidity()));
                    }else if("1".equals(inclusionType)){
                        log.info("卡号：{}，其它情况配置套餐，开始设置失效时间：{}",order.getIccid(),DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                        //按使用时间计费
                        iotRefCardCost.setValidEnd(DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                    }
                }
            }
        }
        //插入套餐关系表
        int insert = this.baseMapper.insert(iotRefCardCost);
        if(insert == 1){
            //修改订单状态为成功
            orderMapper.updateOrderStatus(order.getId(), OrderStatus.doSuccess.getCode(),order.getOrderState());
        }else{
            orderMapper.updateOrderStatus(order.getId(),OrderStatus.doFail.getCode(),order.getOrderState());
        }
    }

    @Override
    public void activePackage(String accessNumber){
        QueryWrapper<IotTelecomRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("access_number",accessNumber).eq("free_type","1").eq("active",0);
        IotTelecomRefCardCost refCardCost = this.getOne(queryWrapper);
        if(null!=refCardCost){
            StandardCost standardCost = standardCostMapper.selectById(refCardCost.getCostId());
            if(null!=standardCost){
                IotTelecomRefCardCost newRefCardCost = new IotTelecomRefCardCost();
                newRefCardCost.setId(refCardCost.getId());
                newRefCardCost.setActive(1);
                newRefCardCost.setValidStart(new Date());
                newRefCardCost.setValidEnd(DateUtils.addDays2Date(new Date(),standardCost.getPeriodOfValidity()));
                this.updateById(newRefCardCost);
                //看看卡是什么状态
                IotTelecomCardInfo telecomCardInfo = telecomCardInfoService.getOne(new QueryWrapper<IotTelecomCardInfo>().eq("access_number",accessNumber));
                if(telecomCardInfo.getSimStatus()==5){
                    //去激活卡

                }
            }
        }
    }
}
