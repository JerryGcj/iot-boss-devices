package com.wangxin.iot.task.xxl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.card.IDouyinGatewayApiService;
import com.wangxin.iot.douyin.IElectronChannelDouyinOrderService;
import com.wangxin.iot.douyin.entity.ElectronChannelDouyinOrder;
import com.wangxin.iot.douyin.entity.ElectronChannelOrderRegular;
import com.wangxin.iot.utils.DateUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.annotation.XxlJob;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author anan
 * @date 2023/2/10 13:50
 */
@Component
@Slf4j
public class DouyinOrderJob {
    @Autowired
    IDouyinGatewayApiService douyinGatewayApiService;
    @Autowired
    IElectronChannelDouyinOrderService electronChannelDouyinOrderService;

    /**
     * 拉取订单
     * @param
     * @return
     */
    @XxlJob("pullOrder")
    public ReturnT pullOrder(String params){
        XxlJobLogger.log("抖店shopId拉取订单开始：{}",params);
        List<String> strings = Arrays.asList(params.trim().split(","));
        strings.forEach(item->{
            try {
                //当前时间减20分钟
                Long startTime = System.currentTimeMillis()/1000-1200;
                //当前时间减10分钟
                Long endTime = System.currentTimeMillis()/1000-600;
                douyinGatewayApiService.pullOrder(0L, item, startTime,endTime);
            }catch (Exception e){
                log.error("抖店shopId查询异常：{},{}",item,e.getMessage());
                e.printStackTrace();
            }
        });
        log.info("执行结束");
        return ReturnT.SUCCESS;
    }

    /**
     * 订单解密
     * @param
     * @return
     */
    @XxlJob("decryptDouyinOrder")
    public ReturnT decryptDouyinOrder(String params){
        QueryWrapper<ElectronChannelDouyinOrder> douyinOrderQueryWrapper = new QueryWrapper<>();
        //加密的
        douyinOrderQueryWrapper.eq("sensitive_is", 0);
        List<String> strings = Arrays.asList(params.trim().split(","));
        douyinOrderQueryWrapper.in("shop_id", strings);
        //4代表退款了，解密接口不能用了
        douyinOrderQueryWrapper.ne("order_status", "4");
        douyinOrderQueryWrapper.ge("create_time", DateUtils.addDays2Date(new Date(), -1));
        List<ElectronChannelDouyinOrder> list = electronChannelDouyinOrderService.list(douyinOrderQueryWrapper);
        XxlJobLogger.log("抖店shopId订单解密开始：{},",list.size());
        log.info("抖店shopId订单解密开始：{},",list.size());
        if(CollectionUtils.isNotEmpty(list)){
            strings.forEach(item->{
                try {
                    douyinGatewayApiService.decrypt(item, list);
                }catch (Exception e){
                    log.error("抖店shopId：{} 解密接口查询异常",item);
                    e.printStackTrace();
                }
            });
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 将作废原因推送到抖店
     * @param
     * @return
     */
    @XxlJob("pushCancelMsg")
    public ReturnT pushCancelMsg(String params){
        List<ElectronChannelOrderRegular> cancelMsgs = electronChannelDouyinOrderService.getCancelMsg(Arrays.asList(params.split(",")));
        if(CollectionUtils.isNotEmpty(cancelMsgs)){
            Map<String, List<ElectronChannelOrderRegular>> collect = cancelMsgs.stream().collect(Collectors.groupingBy(ElectronChannelOrderRegular::getOurId));
            collect.forEach((k,v)->{
                try {
                    XxlJobLogger.log("抖店推送作废开始：{},",cancelMsgs.size());
                    log.info("抖店推送作废开始：{},",cancelMsgs.size());
                    electronChannelDouyinOrderService.pushCancelMsg(Long.valueOf(k),v);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
        return ReturnT.SUCCESS;
    }

    /**
     * 将发货的订单信息同步到抖店
     * @param
     * @return
     */
    @XxlJob("pushExpress")
    public ReturnT pushExpress(String params){
        List<ElectronChannelOrderRegular> expressInfos = electronChannelDouyinOrderService.getExpressInfo(Arrays.asList(params.split(",")));
        if(CollectionUtils.isNotEmpty(expressInfos)){
            Map<String, List<ElectronChannelOrderRegular>> collect = expressInfos.stream().collect(Collectors.groupingBy(ElectronChannelOrderRegular::getOurId));
            collect.forEach((k,v)->{
                try {
                    XxlJobLogger.log("将发货的订单信息同步到抖店：{},",expressInfos.size());
                    log.info("将发货的订单信息同步到抖店：{},",expressInfos.size());
                    electronChannelDouyinOrderService.pushExpressInfo(Long.valueOf(k),v);
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }
        return ReturnT.SUCCESS;
    }

}
