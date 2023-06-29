package com.wangxin.iot.task;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.constants.CardCostActiveEnum;
import com.wangxin.iot.constants.CardStatusEnum;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.domain.RefCardMonitorModel;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.mapper.IotUnicomRefCardCostMapper;
import com.wangxin.iot.model.IotSysConfig;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;

/**
 * 定时监控卡资费套餐的使用量和有效期
 *
 * @author wx
 * @date 2020/2/26
 */
@Slf4j
@Component
public class UnicomCardCostMonitorTask {
    @Autowired
    IUnicomGatewayService iIoTGatewayApiService;
    @Autowired
    Executor executor;
    @Autowired
    private IotUnicomCardInfoMapper iotUnicomCardInfoMapper;
    @Autowired
    private IotUnicomRefCardCostMapper iotRefCardCostMapper;

    /**
     * 程序启动30分钟后，开始监控使用量和有效期的卡,
     * 1小时检测一次
     */
    @Scheduled(initialDelay = 1000*60*30,fixedDelay = 1000*60*60)
    public void cardAutoStopMonitor(){
        log.info("定时监控卡套餐的使用量和有效期...");
//        IotSysConfig config = CacheComponent.getInstance().getConfigByKey("virtual_per_unicom");
//        BigDecimal perUnicom = new BigDecimal(config.getSysValue());
        List<RefCardMonitorModel> refCardMonitorModels = iotUnicomCardInfoMapper.monitorCard(new BigDecimal("2"), 2,2);
        if(CollectionUtils.isNotEmpty(refCardMonitorModels)){
            ThreadPoolTaskExecutor threadPoolExecutor = (ThreadPoolTaskExecutor)executor;
            refCardMonitorModels.forEach(item->
                threadPoolExecutor.execute(()->{
                    Date now = DateUtils.getCurrentDate();
                    //加油包的总量
                    BigDecimal addOil = BigDecimal.ZERO;
                    //每个加油包的虚量
                    BigDecimal addOilInit = BigDecimal.ZERO;
                    String oilIds = item.getId()+",";
                    //当前基础包对应所有的加油包
                    List<IotUnicomRefCardCost> collect = iotRefCardCostMapper.getOilByParentId(item.getId());
                    if(CollectionUtils.isNotEmpty(collect)){
                        for (IotUnicomRefCardCost unicom:collect) {
                            oilIds += unicom.getId()+",";
                            addOil = addOil.add(unicom.getOriginUse());
                            addOilInit = addOilInit.add(unicom.getInitUsaged());
                        }
                    }
                    //基础包到期时间
                    Date validEnd = item.getValidEnd();
                    // 判断该套餐是否超出有效期，是否超出套餐最大量，如果超出，则修改套餐为已失效，卡状态为已停机
                    BigDecimal usage = item.getUsaged() == null ? BigDecimal.ZERO : item.getUsaged();
                    BigDecimal baseUsage = usage;
                    BigDecimal oilUsage = BigDecimal.ZERO;
                    //基础包最大虚量
                    BigDecimal critical = item.getOriginUse().divide(item.getCustomPackageUse(), 3, RoundingMode.HALF_UP);
                    log.info("卡号：{} 原始用量：{}，最大虚量：{}",item.getIccid(), usage, critical);
                    boolean flag = usage.compareTo(critical) > 0;
                    if(flag){
                        //加油包的用量= 总用量-原始用量/比例系数
                        oilUsage = usage.subtract(critical);
                        log.info("卡号：{} 加油包用量：{}",item.getIccid(), oilUsage);
                        //基础包的用量
                        baseUsage = usage.subtract(oilUsage);
                        log.info("卡号：{} 基础包用量：{}",item.getIccid(), baseUsage);
                    }
                    BigDecimal initUsages = item.getInitUsaged() == null ? BigDecimal.ZERO : item.getInitUsaged();
                    //加油包也手动配置了初始用量
                    initUsages = initUsages.add(addOilInit);
                    log.info("卡号：{} 初始用量：{}",item.getIccid(), initUsages);
                    //计算总用量时候，加油包也算虚量
                    BigDecimal realUsage = baseUsage.multiply(item.getCustomPackageUse()).add(initUsages).add(oilUsage.multiply(item.getCustomPackageUse()));
                    log.info("{} 卡总流量{}，总用量：{}，加油包用量：{}", item.getIccid(), item.getOriginUse().add(addOil), realUsage, oilUsage);
                    //===================================== 用量超过了 =================================
                    if(realUsage.compareTo(item.getOriginUse().add(addOil)) >= 0){
                        Date endTime = DateUtils.addHour2Date(now, 24);
                        Map nextPackageIn24Hour = iotRefCardCostMapper.getNextPackageIn24Hour(item.getIccid(), CardCostActiveEnum.INACTIVE.getActive(), endTime);
                        if(CollectionUtils.isNotEmpty(nextPackageIn24Hour)){
                            log.info("卡号：{} 用量超了，存在即将生效的套餐，id为 {}，不停机",item.getIccid(), nextPackageIn24Hour.get("id").toString());
                            //将当前套餐改为失效
                            updateFailure(oilIds,3);
                            //将即将生效的套餐改为生效中
                            updateTaskEffect(nextPackageIn24Hour.get("id").toString(), (Date) nextPackageIn24Hour.get("valid_start"));
                        }else{
                            //当前这个套餐是失效的
                            if(item.getActive().intValue() == 2 || item.getActive().intValue() == 3){
                                //有没有提前生效的基础包
                                Integer inAdvancePackageCount = iotRefCardCostMapper.getInAdvancePackageCount(item.getIccid(), 1,0);
                                if(inAdvancePackageCount>=1){
                                    log.info("卡号：{} 当前套餐已失效，存在提前生效的基础包，因此不停机",item.getIccid());
                                    return;
                                }
                            }
                            log.info("卡号：{} 用量已超，申请停机，总量：{}，用量：{}，停机",item.getIccid(),item.getOriginUse().add(addOil),usage);
                            // 修改套餐为已失效，卡状态为已停机
                            updateStatus(item.getIccid(), item.getId(), oilIds, CardStatusEnum.DEACTIVATED, CardCostActiveEnum.OVER_USAGE_DISABLED);
                        }
                    }else{
                        //================================ 时间到期 =====================================
                        Date compareDate = DateUtils.addHour2Date(validEnd,-1);
                        //时间到期了
                        //当前套餐时间已失效
                        if(now.compareTo(compareDate) >=0 ){
                            //12小时内是否有即将生效的基础包
                            Date endTime = DateUtils.addHour2Date(validEnd, 24);
                            Map nextPackageIn24Hour = iotRefCardCostMapper.getNextPackageIn24Hour(item.getIccid(), CardCostActiveEnum.INACTIVE.getActive(), endTime);
                            if(CollectionUtils.isNotEmpty(nextPackageIn24Hour)){
                                log.info("卡号：{} 当前套餐到期，存在即将生效的套餐，id为 {}，不停机",item.getIccid(), nextPackageIn24Hour.get("id").toString());
                                //将当前套餐改为失效
                                updateFailure(oilIds,2);
                                //将即将生效的套餐改为生效中
                                updateTaskEffect(nextPackageIn24Hour.get("id").toString(), (Date) nextPackageIn24Hour.get("valid_start"));
                            }else{
                                //当前这个套餐是失效的
                                if(item.getActive().intValue() == 2 || item.getActive().intValue() == 3){
                                    //有没有提前生效的基础包
                                    Integer inAdvancePackageCount = iotRefCardCostMapper.getInAdvancePackageCount(item.getIccid(), 1,0);
                                    if(inAdvancePackageCount>=1){
                                        log.info("卡号：{} 当前套餐已到期，存在提前生效的基础包，因此不停机",item.getIccid());
                                        return;
                                    }
                                }
                                //下个周期没有可用套餐，，停机
                                String overDate = DateUtils.formatDateToFullString(item.getValidEnd());
                                log.info("卡号：{} 超出有效期 {}，申请停机",item.getIccid(),overDate);
                                // 修改套餐为已失效，卡状态为已停机
                                updateStatus(item.getIccid(), item.getId(), oilIds, CardStatusEnum.DEACTIVATED, CardCostActiveEnum.OVERTIME_DISABLED);
                            }
                        }
                    }
                    log.info("监控卡套餐的使用量和有效期：  核心线程数：{}，完成任务数：{}，缓冲区大小：{}",
                            threadPoolExecutor.getCorePoolSize(),threadPoolExecutor.getThreadPoolExecutor().getCompletedTaskCount(),
                            threadPoolExecutor.getThreadPoolExecutor().getQueue().size());
                })
            );
        }
    }
    /**
     * 修改卡和套餐状态
     * @param iccid
     * @param cardInfoId
     * @param iotRefCardCostId
     * @param cardStatus
     * @param cardCostStatus
     */
    public void updateStatus(String iccid, String cardInfoId, String iotRefCardCostId, CardStatusEnum cardStatus, CardCostActiveEnum cardCostStatus) {
        log.info("定时监控卡iccid：{}套餐，调用接口修改卡状态为：{}，", iccid,cardStatus.getCode());
        Map paramMap = new HashMap();
        //发生位置是monitor定时任务
        paramMap.put("action","1");
        paramMap.put("iccid", iccid);
        paramMap.put("goalState",cardStatus.getCode());
        iIoTGatewayApiService.updateCardStatus(paramMap);
        // 修改套餐状态
        if (iotRefCardCostId != null) {
            String id = iotRefCardCostId;
            if(iotRefCardCostId.endsWith(",")){
                id = iotRefCardCostId.substring(0, iotRefCardCostId.length()-1);
            }
            String[] ids= id.split(",");
            for(int i=0;i<ids.length;i++){
                IotUnicomRefCardCost updateRefCardCost = new IotUnicomRefCardCost();
                updateRefCardCost.setId(ids[i]);
                updateRefCardCost.setActive(cardCostStatus.getActive());
                log.info("定时监控卡iccid：{},修改套餐：{},目标状态{}",iccid,updateRefCardCost,cardCostStatus.getActive());
                iotRefCardCostMapper.updateById(updateRefCardCost);
            }

        }

    }



    /**
     * 将当前套餐改为失效
     * @param id
     */
    public void updateFailure(String id, int active){
        String oilId = id;
        if(id.endsWith(",")){
            oilId = id.substring(0, id.length()-1);
        }
        String[] oilIds = oilId.split(",");
        for(int i=0;i<oilIds.length;i++){
            IotUnicomRefCardCost updateRefCardCost = new IotUnicomRefCardCost();
            updateRefCardCost.setId(oilIds[i]);
            updateRefCardCost.setActive(active);
            iotRefCardCostMapper.updateById(updateRefCardCost);
        }
    }

    /**
     * 将即将生效的套餐改为生效中
     * @param id
     */
    public void updateTaskEffect(String id, Date date){
        IotUnicomRefCardCost updateRefCardCost = new IotUnicomRefCardCost();
        updateRefCardCost.setId(id);
        updateRefCardCost.setActive(CardCostActiveEnum.ACTIVED.getActive());
        if(null!=date){
            try{
                Date time = DateUtils.addDays2Date(date,1);
                String s = DateUtils.formatDateToString(time, DateUtils.YYYY_MM_DD);
                Date start = DateUtils.formatStringToDate(s+" 00:00:00", DateUtils.YYYY_MM_DD_HH_MM_SS);
                updateRefCardCost.setValidStart(start);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        iotRefCardCostMapper.updateById(updateRefCardCost);
    }


}
