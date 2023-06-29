package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.ICardUsageService;
import com.wangxin.iot.card.ICustomerSalesDiscountService;
import com.wangxin.iot.card.IotRefCardCostService;
import com.wangxin.iot.constants.OrderStatus;
import com.wangxin.iot.domain.RefCardModel;
import com.wangxin.iot.mapper.*;
import com.wangxin.iot.model.Card;
import com.wangxin.iot.model.IotRefCardCost;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.StandardCost;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 */
@Slf4j
@Service
public class IotRefCardCostServiceImpl extends ServiceImpl<IotRefCardCostMapper, IotRefCardCost> implements IotRefCardCostService {
    @Autowired
    DiscountPackageMapper discountPackageMapper;
    @Autowired
    StandardCostMapper standardCostMapper;

    @Autowired
    ICustomerSalesDiscountService customerSalesDiscountService;
    @Autowired
    ICardUsageService cardUsageService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    CardInformationMapper cardInformationMapper;

    @Override
    public  void saveRefWithOrder(Order order) {
        StandardCost standardCost = standardCostMapper.selectById(order.getPackageId());
        BigDecimal packageUse = cardInformationMapper.packageUse(order.getId());
        String basicPackage = standardCost.getBasicPackage();
        Date currentDate = DateUtils.getCurrentDate();
        String now = DateUtils.formatDateToString(currentDate,DateUtils.YYYY_MM_DD_HH_MM_SS);
        IotRefCardCost iotRefCardCost = new IotRefCardCost();
        iotRefCardCost.setUsaged(BigDecimal.ZERO);
        iotRefCardCost.setCostId(standardCost.getId());
        iotRefCardCost.setCardIccid(order.getIccid());
        iotRefCardCost.setCostType(standardCost.getBasicPackage());
        iotRefCardCost.setOriginUse(standardCost.getContainsFlow());
        iotRefCardCost.setCostName(standardCost.getPackageName());
        iotRefCardCost.setCustomPackageUse(packageUse);
        iotRefCardCost.setOrderId(order.getId());
        iotRefCardCost.setFreeType(standardCost.getFreeType());
        iotRefCardCost.setCreateTime(currentDate);
        //基础套餐类型
        String inclusionType = standardCost.getInclusionType();

        QueryWrapper<IotRefCardCost> iotRefCardCostQueryWrapper = new QueryWrapper<>();
        //没订过套餐，基础包到期失效的，再次订购，直接生效
        iotRefCardCostQueryWrapper.eq("card_iccid", order.getIccid());
        //基础包
        if(basicPackage.equals("0")){
            iotRefCardCostQueryWrapper.eq("cost_type","0");
        }else if(basicPackage.equals("1")){
            //加油包
            iotRefCardCostQueryWrapper.eq("cost_type","1");
        }
        //当前卡 所有的套餐
        List<IotRefCardCost> list = this.list(iotRefCardCostQueryWrapper);

        //当前时间有失效的套餐
        List<IotRefCardCost> disRef = list.stream().filter(item -> (item.getActive() == 2 || item.getActive() == 3) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());

        //当前时间有生效或未生效的套餐
        List<IotRefCardCost> enableRef = list.stream().filter(item -> (item.getActive() == 0 || item.getActive() == 1) &&
                item.getValidStart().compareTo(currentDate) <= 0 &&
                item.getValidEnd().compareTo(currentDate) >= 0
        ).collect(Collectors.toList());

        //从来没订过， //订购的是加油包  //当前时间内，既没有失效的套餐，也没有生效的套餐
        if( CollectionUtils.isEmpty(list) ||
            "1".equals(basicPackage)||
            (CollectionUtils.isEmpty(disRef) && CollectionUtils.isEmpty(enableRef))){
            iotRefCardCost.setActive(1);
            iotRefCardCost.setValidStart(currentDate);
            log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置生效时间：{}",order.getIccid(),currentDate);
            //加油包
            if(basicPackage.equals("1")){
                RefCardModel refCardModel = this.baseMapper.getByIccid(order.getIccid(),"0",currentDate);
                //加油包的结束日期是基础包的结束日期
                iotRefCardCost.setValidEnd(refCardModel.getValidEnd());
                iotRefCardCost.setParentId(refCardModel.getId());
            }else{
                Date endDate = null;
                //按照自然月计费
                if("0".equals(inclusionType)){
                    endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(now));
                    iotRefCardCost.setValidEnd(endDate);
                }else if("1".equals(inclusionType)){
                    endDate = DateUtils.addDays2CurrentDate(standardCost.getPeriodOfValidity());
                    //按使用时间计费
                    iotRefCardCost.setValidEnd(endDate);
                }
                log.info("卡号：{}，从来没订过 或 订购的是加油包 或 当前时间内，既没有失效的套餐，也没有生效的套餐，开始设置失效时间：{}",order.getIccid(), endDate);
            }
        }else{
            //一定有生效或者失效的包
            //把所有的基础包按照失效时间倒序排列
            List<IotRefCardCost> sortDisRef = list.stream().sorted(Comparator.comparing(IotRefCardCost::getValidEnd).reversed()).collect(Collectors.toList());
            //说明当前时间存在基础套餐，流量用超或者失效了
            if(CollectionUtils.isNotEmpty(sortDisRef)){
                iotRefCardCost.setActive(0);
                Date start = sortDisRef.get(0).getValidEnd();
                iotRefCardCost.setValidStart(start);
                log.info("卡号：{}，有生效或者失效的包，开始设置生效时间：{}",order.getIccid(),start);
                Date endDate = null;
                //按照自然月计费
                if("0".equals(inclusionType)){
                    //看看当前套餐的失效时间是否是当月的最后一天
                    if(DateUtils.ifMonthEnd(start)){
                        Date start1 = DateUtils.getFirstDateOfMonth(DateUtils.addMonths2Date(start,1));
                        String nextStart1 = DateUtils.formatDateToString(start1,DateUtils.YYYY_MM_DD_HH_MM_SS);
                        endDate = DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart1));
                        iotRefCardCost.setValidStart(start1);
                        iotRefCardCost.setValidEnd(endDate);
                        log.info("卡号：{}，有生效或者失效的包，设置生效时间：{}，开始设置失效时间：{}",order.getIccid(),start1, endDate);
                    }else{
                        String nextStart = DateUtils.formatDateToString(start,DateUtils.YYYY_MM_DD_HH_MM_SS);
                        log.info("卡号：{}，当前有失效套餐，没有生效套餐，设置生效时间：{}，设置失效时间：{}",order.getIccid(),start,DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart)));
                        iotRefCardCost.setValidEnd(DateUtils.formatFullStringToDate(DateUtils.getMonthEnd(nextStart)));
                    }
                }else if("1".equals(inclusionType)){
                    log.info("卡号：{}，有生效或者失效的包，开始设置失效时间：{}",order.getIccid(), DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                    //按使用时间计费
                    iotRefCardCost.setValidEnd(DateUtils.addDays2Date(start,standardCost.getPeriodOfValidity()));
                }
            }
        }
        //插入套餐关系表
        int insert = this.baseMapper.insert(iotRefCardCost);
        log.info("卡号：{}，添加套餐完成，返回行数：{}",order.getIccid(),insert);
        if(insert == 1){
            //修改订单状态为成功
            orderMapper.updateOrderStatus(order.getId(), OrderStatus.doSuccess.getCode(),order.getOrderState());
        }else{
            orderMapper.updateOrderStatus(order.getId(), OrderStatus.doFail.getCode(),order.getOrderState());
        }
    }

    @Override
    public void saveWithOrder(Order order) {
        StandardCost standardCost;
        if(order.getPaymentChannel().equals("3")){
            standardCost = standardCostMapper.selectById(order.getPackageId());
        }else{
            standardCost = standardCostMapper.selectById(order.getPackageId());
        }
        QueryWrapper<IotRefCardCost> iotRefCardCostQueryWrapper = new QueryWrapper<>();
        iotRefCardCostQueryWrapper.eq("card_iccid", order.getIccid()).eq("active","1").eq("cost_id",standardCost.getId());
        IotRefCardCost cardCost = this.getOne(iotRefCardCostQueryWrapper);
        //当前套餐没有生效的套餐
        if(cardCost == null){
            //日用表记录
            try{
                cardUsageService.saveUsage(order.getIccid(), LocalDate.now(),BigDecimal.ZERO);
            }catch (Exception e){
                e.printStackTrace();
            }
            IotRefCardCost iotRefCardCost = new IotRefCardCost();
            iotRefCardCost.setUsaged(BigDecimal.ZERO);
            iotRefCardCost.setActive(1);
            iotRefCardCost.setCostId(standardCost.getId());
            iotRefCardCost.setCardIccid(order.getIccid());
            iotRefCardCost.setCostType(standardCost.getBasicPackage());
            iotRefCardCost.setOriginUse(standardCost.getContainsFlow());
            iotRefCardCost.setCostName(standardCost.getPackageName());
            Date currentDate = DateUtils.getCurrentDate();
            String inclusionType = standardCost.getInclusionType();
            //如果是接口订购
            if("3".equals(order.getPaymentChannel())){
                //设置一个初始值
                BigDecimal usage = StringUtil.isEmpty(order.getNote()) ? BigDecimal.ZERO:new BigDecimal(order.getNote());
                iotRefCardCost.setInitUsaged(usage);
                iotRefCardCost.setValidStart(order.getCreateTime());
                iotRefCardCost.setValidEnd(order.getUpdateDate());
            }else{
                iotRefCardCost.setValidStart(currentDate);
                //按照自然月计费
                if("0".equals(inclusionType)){
                    //当前时间+延后几个月
                    iotRefCardCost.setValidEnd(DateUtils.addMonths2CurrentDate(standardCost.getPeriodOfValidity()));
                }else if("1".equals(inclusionType)){
                    //按使用时间计费
                    iotRefCardCost.setValidEnd(DateUtils.addDays2CurrentDate(standardCost.getPeriodOfValidity()));
                }
            }
            iotRefCardCost.setCreateTime(currentDate);
            this.baseMapper.insert(iotRefCardCost);
        }else{
            //当前套餐有正在生效的，则延续一个月。
            //未生效
            cardCost.setActive(0);
            cardCost.setId(null);
            //接口订购的套餐,这种情况代表客户在后台手动提交了多个套餐
            if("3".equals(order.getPaymentChannel())){
                //设置一个初始值
                BigDecimal usage = StringUtil.isEmpty(order.getNote()) ? BigDecimal.ZERO:new BigDecimal(order.getNote());
                cardCost.setInitUsaged(usage);
                //已用清零
                cardCost.setUsaged(BigDecimal.ZERO);
                cardCost.setValidStart(order.getCreateTime());
                cardCost.setValidEnd(order.getUpdateDate());
                cardCost.setCreateTime(new Date());
                this.baseMapper.insert(cardCost);
            }else{
                cardCost.setValidStart(cardCost.getValidEnd());
                String inclusionType = standardCost.getInclusionType();
                //按照自然月计费
                if("0".equals(inclusionType)){
                    //当前时间+延后几个月
                    cardCost.setValidEnd(DateUtils.addMonths2Date(cardCost.getValidEnd(),standardCost.getPeriodOfValidity()));
                }else if("1".equals(inclusionType)){
                    //按使用时间计费
                    cardCost.setValidEnd(DateUtils.addDays2Date(cardCost.getValidEnd(),standardCost.getPeriodOfValidity()));
                }
                this.baseMapper.insert(cardCost);
            }
        }
    }

    @Override
    public boolean updateUsaged(Card card) {
        if(card == null){
            return false;
        }
        return baseMapper.updateUsage(card.getData(),"1",card.getIccid()) ==1?true:false;
    }
}
