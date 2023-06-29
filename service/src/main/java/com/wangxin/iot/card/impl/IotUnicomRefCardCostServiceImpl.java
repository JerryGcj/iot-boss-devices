package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.ICustomerSalesDiscountService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.card.IUnicomShareProfitsService;
import com.wangxin.iot.card.IotUnicomRefCardCostService;
import com.wangxin.iot.constants.OrderStatus;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.mapper.*;
import com.wangxin.iot.model.IotCardOrder;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.StandardCost;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 */
@Service
@Slf4j
public class IotUnicomRefCardCostServiceImpl extends ServiceImpl<IotUnicomRefCardCostMapper, IotUnicomRefCardCost> implements IotUnicomRefCardCostService {
    @Autowired
    DiscountPackageMapper discountPackageMapper;
    @Autowired
    StandardCostMapper standardCostMapper;
    @Autowired
    IUnicomShareProfitsService shareProfitsService;
    @Autowired
    ICustomerSalesDiscountService customerSalesDiscountService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    IotCardOrderMapper cardOrderMapper;
    @Autowired
    IotUnicomCardInfoMapper unicomCardInfoMapper;
    @Autowired
    IUnicomGatewayService iIoTGatewayApiService;

    @Override
    public void saveRefWithOrder(Order order) {

        StandardCost standardCost = standardCostMapper.selectById(order.getPackageId());
        String basicPackage = standardCost.getBasicPackage();
        Date currentDate = DateUtils.getCurrentDate();
        IotUnicomRefCardCost iotRefCardCost = new IotUnicomRefCardCost();
        iotRefCardCost.setUsaged(BigDecimal.ZERO);
        iotRefCardCost.setInitUsaged(BigDecimal.ZERO);
        iotRefCardCost.setCostId(standardCost.getId());
        iotRefCardCost.setIccid(order.getIccid());
        iotRefCardCost.setCostType(Integer.parseInt(standardCost.getBasicPackage()));
        iotRefCardCost.setOriginUse(standardCost.getContainsFlow());
        iotRefCardCost.setCostName(standardCost.getPackageName());
        iotRefCardCost.setFreeType(standardCost.getFreeType());
        iotRefCardCost.setCreateTime(currentDate);
        //基础套餐类型
        String inclusionType = standardCost.getInclusionType();
        String now = DateUtils.formatDateToString(currentDate,DateUtils.YYYY_MM_DD_HH_MM_SS);
        QueryWrapper<IotUnicomRefCardCost> iotRefCardCostQueryWrapper = new QueryWrapper<>();
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
        List<IotUnicomRefCardCost> list = this.list(iotRefCardCostQueryWrapper);


        //当前时间有失效的套餐
        List<IotUnicomRefCardCost> disRef = list.stream().filter(item -> (item.getActive() == 2 || item.getActive() == 3) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());

        //当前时间有生效的套餐
        List<IotUnicomRefCardCost> enableRef = list.stream().filter(item -> (item.getActive() == 0 || item.getActive() == 1) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());
        //从来没订过， //订购的是加油包  //当前时间内，既没有失效的套餐，也没有生效的套餐
        if(CollectionUtils.isEmpty(list) ||
            "1".equals(basicPackage)||
            (CollectionUtils.isEmpty(disRef) && CollectionUtils.isEmpty(enableRef))){
            iotRefCardCost.setActive(1);
            iotRefCardCost.setValidStart(currentDate);
            log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置生效时间：{}",order.getIccid(),currentDate);
            //加油包
            if(basicPackage.equals("1")){
                //加油包的结束日期是基础包的结束日期
                Map map = this.baseMapper.getByIccid(order.getIccid(), "0", currentDate);
                iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(map.get("valid_end").toString()));
                iotRefCardCost.setParentId(map.get("id").toString());
            }else{
                Date endDate = null;
                //按照自然月计费
                if("0".equals(inclusionType)){
                    endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(now));
                    iotRefCardCost.setValidEnd(endDate);
                }else if("1".equals(inclusionType)){
                    //按使用时间计费
                    endDate = DateUtils.addDays2CurrentDate(standardCost.getPeriodOfValidity());
                    iotRefCardCost.setValidEnd(endDate);
                }else if("2".equals(inclusionType)){
                    //按联通计费
                    //当前时间是否小于27号，是的话将失效时间设为本月26，否则为下月26
                    int day = LocalDate.now().getDayOfMonth();
                    endDate = DateUtils.endOf26(day);
                    iotRefCardCost.setValidEnd(endDate);
                }
                log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置失效时间：{}",order.getIccid(),endDate);
            }
        }else{
            //当前有失效套餐，没有生效套餐
            if(CollectionUtils.isNotEmpty(disRef) && CollectionUtils.isEmpty(enableRef)){
                //找到介于当前时间内的失效套餐
                List<IotUnicomRefCardCost> sortDisRef = disRef.stream().sorted(Comparator.comparing(IotUnicomRefCardCost::getCreateTime).reversed()).collect(Collectors.toList());
                //说明当前时间存在基础套餐，流量用超或者失效了
                if(CollectionUtils.isNotEmpty(sortDisRef)){
                    Date start = sortDisRef.get(0).getValidEnd();
                    iotRefCardCost.setActive(0);
                    //如果订购时间正好距离下一份套餐包的开始时间不到一个小时，那么设置为生效状态。
                    //自动停复机定时任务，提前一小时将套餐设置为停机状态。
                    if(DateUtils.addHour2Date(currentDate,1).compareTo(start)>0){
                        iotRefCardCost.setActive(1);
                    }
                    iotRefCardCost.setValidStart(start);
                    //按照自然月计费
                    if("0".equals(inclusionType)){
                        //看看当前套餐的失效时间是否是当月的最后一天
                        if(DateUtils.ifMonthEnd(start)){
                            Date start1 = DateUtils.getFirstDateOfMonth(DateUtils.addMonths2Date(start,1));
                            String nextStart1 = DateUtils.formatDateToString(start1,DateUtils.YYYY_MM_DD_HH_MM_SS);
                            iotRefCardCost.setValidStart(start1);
                            iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1)));
                            log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",order.getIccid(),start1,DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1)));
                        }else{
                            String nextStart = DateUtils.formatDateToString(start,DateUtils.YYYY_MM_DD_HH_MM_SS);
                            log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",order.getIccid(),start,DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart)));
                            iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart)));
                        }
                    }else if("1".equals(inclusionType)){
                        //按使用时间计费
                        log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",order.getIccid(),start,DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                        iotRefCardCost.setValidEnd(DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                    }else if("2".equals(inclusionType)){
                        //按联通计费
                        //当前时间是否小于27号，是的话将失效时间设为本月26，否则为下月26
                        int day = LocalDate.now().getDayOfMonth();
                        log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",order.getIccid(),start,DateUtils.endOf26(day));
                        iotRefCardCost.setValidEnd(DateUtils.endOf26(day));
                    }
                }
            }else{
                //下一份套餐的开始时间计算(按照生效时间倒叙排序)
                List<IotUnicomRefCardCost> sortRef = list.stream().filter(item->item.getActive() == 0 || item.getActive() == 1)
                        .sorted(Comparator.comparing(IotUnicomRefCardCost::getValidEnd).reversed())
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(sortRef)){
                    IotUnicomRefCardCost iotRefCardCost1 = sortRef.get(0);
                    Date start = iotRefCardCost1.getValidEnd();
                    String nextStart = DateUtils.formatDateToString(start,DateUtils.YYYY_MM_DD_HH_MM_SS);
                    //开始时间
                    iotRefCardCost.setValidStart(start);
                    //未生效
                    iotRefCardCost.setActive(0);
                    log.info("卡号：{}，当前有生效套餐，设置新生效时间：{}",order.getIccid(),start);
                    Date endDate = null;
                    //按照自然月计费
                    if("0".equals(inclusionType)){
                        //看看当前套餐的失效时间是否是当月的最后一天
                        if(DateUtils.ifMonthEnd(start)){
                            Date start1 = DateUtils.getFirstDateOfMonth(DateUtils.addMonths2Date(start,1));
                            String nextStart1 = DateUtils.formatDateToString(start1,DateUtils.YYYY_MM_DD_HH_MM_SS);
                            iotRefCardCost.setValidStart(start1);
                            endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1));
                        }else{
                            endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart));
                        }
                        iotRefCardCost.setValidEnd(endDate);
                    }else if("1".equals(inclusionType)){
                        //按使用时间计费
                        endDate = DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity());
                        iotRefCardCost.setValidEnd(endDate);
                    }else if("2".equals(inclusionType)){
                        //按联通计费
                        //当前时间是否小于27号，是的话将失效时间设为本月26，否则为下月26
                        int day = LocalDate.now().getDayOfMonth();
                        endDate = DateUtils.endOf26(day);
                        iotRefCardCost.setValidEnd(endDate);
                    }
                    log.info("卡号：{}，当前有生效套餐，设置新失效时间：{}",order.getIccid(),endDate);
                }
            }
        }
        //插入套餐关系表
        int insert = this.baseMapper.insert(iotRefCardCost);
        if(insert == 1){
            //修改订单状态为成功
            orderMapper.updateOrderStatus(order.getId(), OrderStatus.doSuccess.getCode(),order.getOrderState());
        }else{
            orderMapper.updateOrderStatus(order.getId(), OrderStatus.doFail.getCode(),order.getOrderState());
        }
    }

    @Override
    public void saveRefWithCardOrder(IotCardOrder cardOrder) {
        StandardCost standardCost = standardCostMapper.selectById(cardOrder.getPackageId());
        String basicPackage = standardCost.getBasicPackage();
        Date currentDate = DateUtils.getCurrentDate();
        String now = DateUtils.formatDateToString(currentDate,DateUtils.YYYY_MM_DD_HH_MM_SS);
        IotUnicomRefCardCost iotRefCardCost = new IotUnicomRefCardCost();
        iotRefCardCost.setUsaged(BigDecimal.ZERO);
        iotRefCardCost.setInitUsaged(BigDecimal.ZERO);
        iotRefCardCost.setOrderId(cardOrder.getId());
        iotRefCardCost.setCostId(standardCost.getId());
        iotRefCardCost.setIccid(cardOrder.getIccid());
        iotRefCardCost.setCostType(Integer.parseInt(standardCost.getBasicPackage()));
        iotRefCardCost.setOriginUse(standardCost.getContainsFlow());
        iotRefCardCost.setCostName(standardCost.getPackageName());
        iotRefCardCost.setFreeType(standardCost.getFreeType());
        iotRefCardCost.setCreateTime(currentDate);
        //基础套餐类型
        String inclusionType = standardCost.getInclusionType();

        QueryWrapper<IotUnicomRefCardCost> iotRefCardCostQueryWrapper = new QueryWrapper<>();
        //没订过套餐，基础包到期失效的，再次订购，直接生效
        iotRefCardCostQueryWrapper.eq("iccid", cardOrder.getIccid());
        //基础包
        if(basicPackage.equals("0")){
            iotRefCardCostQueryWrapper.eq("cost_type","0");
        }else if(basicPackage.equals("1")){
            //加油包
            iotRefCardCostQueryWrapper.eq("cost_type","1");
        }
        //当前卡 所有的套餐
        List<IotUnicomRefCardCost> list = this.list(iotRefCardCostQueryWrapper);


        //当前时间有失效的套餐
        List<IotUnicomRefCardCost> disRef = list.stream().filter(item -> (item.getActive() == 2 || item.getActive() == 3) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());

        //当前时间有生效的套餐
        List<IotUnicomRefCardCost> enableRef = list.stream().filter(item -> (item.getActive() == 0 || item.getActive() == 1) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());
        //从来没订过， //订购的是加油包  //当前时间内，既没有失效的套餐，也没有生效的套餐
        if(CollectionUtils.isEmpty(list) ||
                "1".equals(basicPackage)||
                (CollectionUtils.isEmpty(disRef) && CollectionUtils.isEmpty(enableRef))){
            iotRefCardCost.setActive(1);
            iotRefCardCost.setValidStart(currentDate);
            log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置生效时间：{}",cardOrder.getIccid(),currentDate);
            //加油包
            if(basicPackage.equals("1")){
                //加油包的结束日期是基础包的结束日期
                Map map = this.baseMapper.getByIccid(cardOrder.getIccid(), "0", currentDate);
                iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(map.get("valid_end").toString()));
                iotRefCardCost.setParentId(map.get("id").toString());
            }else{
                Date endDate = null;
                //按照自然月计费
                if("0".equals(inclusionType)){
                    endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(now));
                    iotRefCardCost.setValidEnd(endDate);
                }else if("1".equals(inclusionType)){
                    //按使用时间计费
                    endDate = DateUtils.addDays2CurrentDate(standardCost.getPeriodOfValidity());
                    iotRefCardCost.setValidEnd(endDate);
                }else if("2".equals(inclusionType)){
                    //按联通计费
                    //当前时间是否小于27号，是的话将失效时间设为本月26，否则为下月26
                    int day = LocalDate.now().getDayOfMonth();
                    endDate = DateUtils.endOf26(day);
                    iotRefCardCost.setValidEnd(endDate);
                }
                log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置失效时间：{}",cardOrder.getIccid(),endDate);
            }
        }else{
            //当前有失效套餐，没有生效套餐
            if(CollectionUtils.isNotEmpty(disRef) && CollectionUtils.isEmpty(enableRef)){
                //找到介于当前时间内的失效套餐
                List<IotUnicomRefCardCost> sortDisRef = disRef.stream().sorted(Comparator.comparing(IotUnicomRefCardCost::getCreateTime).reversed()).collect(Collectors.toList());
                //说明当前时间存在基础套餐，流量用超或者失效了
                if(CollectionUtils.isNotEmpty(sortDisRef)){
                    Date start = sortDisRef.get(0).getValidEnd();
                    iotRefCardCost.setActive(0);
                    //如果订购时间正好距离下一份套餐包的开始时间不到一个小时，那么设置为生效状态。
                    //自动停复机定时任务，提前一小时将套餐设置为停机状态。
                    if(DateUtils.addHour2Date(currentDate,1).compareTo(start)>0){
                        iotRefCardCost.setActive(1);
                    }
                    iotRefCardCost.setValidStart(start);
                    //按照自然月计费
                    if("0".equals(inclusionType)){
                        //看看当前套餐的失效时间是否是当月的最后一天
                        if(DateUtils.ifMonthEnd(start)){
                            Date start1 = DateUtils.getFirstDateOfMonth(DateUtils.addMonths2Date(start,1));
                            String nextStart1 = DateUtils.formatDateToString(start1,DateUtils.YYYY_MM_DD_HH_MM_SS);
                            iotRefCardCost.setValidStart(start1);
                            iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1)));
                            log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",cardOrder.getIccid(),start1,DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1)));
                        }else{
                            String nextStart = DateUtils.formatDateToString(start,DateUtils.YYYY_MM_DD_HH_MM_SS);
                            log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",cardOrder.getIccid(),start,DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart)));
                            iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart)));
                        }
                    }else if("1".equals(inclusionType)){
                        //按使用时间计费
                        log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",cardOrder.getIccid(),start,DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                        iotRefCardCost.setValidEnd(DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                    }else if("2".equals(inclusionType)){
                        //按联通计费
                        //当前时间是否小于27号，是的话将失效时间设为本月26，否则为下月26
                        int day = LocalDate.now().getDayOfMonth();
                        log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",cardOrder.getIccid(),start,DateUtils.endOf26(day));
                        iotRefCardCost.setValidEnd(DateUtils.endOf26(day));
                    }
                }
            }else{
                //下一份套餐的开始时间计算(按照生效时间倒叙排序)
                List<IotUnicomRefCardCost> sortRef = list.stream().filter(item->item.getActive() == 0 || item.getActive() == 1)
                        .sorted(Comparator.comparing(IotUnicomRefCardCost::getValidEnd).reversed())
                        .collect(Collectors.toList());
                if(CollectionUtils.isNotEmpty(sortRef)){
                    IotUnicomRefCardCost iotRefCardCost1 = sortRef.get(0);
                    Date start = iotRefCardCost1.getValidEnd();
                    String nextStart = DateUtils.formatDateToString(start,DateUtils.YYYY_MM_DD_HH_MM_SS);
                    //开始时间
                    iotRefCardCost.setValidStart(start);
                    //未生效
                    iotRefCardCost.setActive(0);
                    log.info("卡号：{}，当前有生效套餐，设置新生效时间：{}",cardOrder.getIccid(),start);
                    Date endDate = null;
                    //按照自然月计费
                    if("0".equals(inclusionType)){
                        //看看当前套餐的失效时间是否是当月的最后一天
                        if(DateUtils.ifMonthEnd(start)){
                            Date start1 = DateUtils.getFirstDateOfMonth(DateUtils.addMonths2Date(start,1));
                            String nextStart1 = DateUtils.formatDateToString(start1,DateUtils.YYYY_MM_DD_HH_MM_SS);
                            iotRefCardCost.setValidStart(start1);
                            endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1));
                        }else{
                            endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart));
                        }
                        iotRefCardCost.setValidEnd(endDate);
                    }else if("1".equals(inclusionType)){
                        //按使用时间计费
                        endDate = DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity());
                        iotRefCardCost.setValidEnd(endDate);
                    }else if("2".equals(inclusionType)){
                        //按联通计费
                        //当前时间是否小于27号，是的话将失效时间设为本月26，否则为下月26
                        int day = LocalDate.now().getDayOfMonth();
                        endDate = DateUtils.endOf26(day);
                        iotRefCardCost.setValidEnd(endDate);
                    }
                    log.info("卡号：{}，当前有生效套餐，设置新失效时间：{}",cardOrder.getIccid(),endDate);
                }
            }
        }
        //插入套餐关系表
        int insert = this.baseMapper.insert(iotRefCardCost);
        if(insert == 1){
            //修改订单状态为成功
            cardOrderMapper.updateOrderStatus(cardOrder.getId(), String.valueOf(OrderStatus.doSuccess.getCode()),cardOrder.getOrderState());
        }else{
            cardOrderMapper.updateOrderStatus(cardOrder.getId(),String.valueOf(OrderStatus.doFail.getCode()),cardOrder.getOrderState());
        }
    }

    @Override
    public void activePackage(String iccid) {
        QueryWrapper<IotUnicomRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("iccid",iccid).eq("active",0);
        IotUnicomRefCardCost unicomRefCardCost = this.getOne(queryWrapper);
        if(null!=unicomRefCardCost){
            StandardCost standardCost = standardCostMapper.selectById(unicomRefCardCost.getCostId());
            IotUnicomRefCardCost newRefCardCost = new IotUnicomRefCardCost();
            newRefCardCost.setId(unicomRefCardCost.getId());
            newRefCardCost.setActive(1);
            newRefCardCost.setValidStart(new Date());
            newRefCardCost.setValidEnd(DateUtils.addDays2Date(new Date(),standardCost.getPeriodOfValidity()));
            this.updateById(newRefCardCost);
            //看一下卡是什么状态，不是激活的话去激活
            QueryWrapper<IotUnicomCardInfo> unicomQueryWrapper = new QueryWrapper<>();
            unicomQueryWrapper.eq("iccid",iccid);
            IotUnicomCardInfo unicomCardInfo = unicomCardInfoMapper.selectOne(unicomQueryWrapper);
            if(unicomCardInfo.getSimStatus()!=2){
                //去激活卡
                Map paramMap = new HashMap();
                //发生位置是monitor定时任务
                paramMap.put("action","1");
                paramMap.put("iccid", iccid);
                paramMap.put("goalState","2");
                iIoTGatewayApiService.updateCardStatus(paramMap);
            }
        }
    }

}
