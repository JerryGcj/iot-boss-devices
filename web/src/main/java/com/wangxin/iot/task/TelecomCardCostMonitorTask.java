package com.wangxin.iot.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.constants.CardCostActiveEnum;
import com.wangxin.iot.constants.TelecomCardStatusEnum;
import com.wangxin.iot.domain.IotTelecomRefCardCost;
import com.wangxin.iot.domain.RefCardMonitorModel;
import com.wangxin.iot.mapper.IotTelecomCardInfoMapper;
import com.wangxin.iot.mapper.IotTelecomRefCardCostMapper;
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
public class TelecomCardCostMonitorTask {
    @Autowired
    Executor executor;

    @Autowired
    ITelecomGatewayService iTelecomGatewayService;
    @Autowired
    private IotTelecomCardInfoMapper iotTelecomCardInfoMapper;
    @Autowired
    private IotTelecomRefCardCostMapper iotTelecomRefCardCost;

    /**
     * 自动复机
     */
    @Scheduled(initialDelay = 1000*60*60,fixedDelay = 1000*60*60*2)
    public void cardAutoStartMonitor(){
        List<Map<String,String>> unActiveCard = iotTelecomRefCardCost.getUnActiveCard(0);
        if(CollectionUtils.isNotEmpty(unActiveCard)){
            unActiveCard.forEach(item->{
                String iccid = item.get("iccid");
                String accessNumber = item.get("access_number");
                String refId = item.get("refId");
                String simStatus = String.valueOf(item.get("sim_status"));
                //卡状态不是激活，那么调用接口激活
                if(!TelecomCardStatusEnum.ACTIVE.getCode().equals(simStatus)){
                    log.info("卡号：{} 已停机，有待生效的套餐，自动生效",iccid);
                    //将即将生效的套餐改为生效中
                    updateStatus(accessNumber,refId,TelecomCardStatusEnum.ACTIVE.getIntCode(),CardCostActiveEnum.ACTIVED);
                }else{
                    //是否有生效中的套餐，改为已失效
                    if(iotTelecomRefCardCost.getInAdvancePackageCount(iccid, CardCostActiveEnum.ACTIVED.getActive()) > 0){
                        log.info("卡号：{},状态是{}，有生效中的套餐，设置为到期失效",iccid,simStatus);
                        IotTelecomRefCardCost updateRefCardCost = new IotTelecomRefCardCost();
                        updateRefCardCost.setActive(CardCostActiveEnum.OVERTIME_DISABLED.getActive());
                        updateRefCardCost.setUpdateTime(new Date());
                        QueryWrapper<IotTelecomRefCardCost> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("iccid",iccid);
                        queryWrapper.eq("active",CardCostActiveEnum.ACTIVED.getActive());
                        iotTelecomRefCardCost.update(updateRefCardCost,queryWrapper);
                    }
                    log.info("卡:{}未生效的套餐:{}改为已生效",iccid,refId);
                    //把未生效的套餐改为生效中
                    IotTelecomRefCardCost updateRefCardCost = new IotTelecomRefCardCost();
                    updateRefCardCost.setId(refId);
                    updateRefCardCost.setActive(CardCostActiveEnum.ACTIVED.getActive());
                    iotTelecomRefCardCost.updateById(updateRefCardCost);
                }
            });
        }
    }
    /**
     * 程序启动30分钟后，开始监控使用量和有效期的卡,
     * 1小时检测一次
     */
    @Scheduled(initialDelay = 1000*60*30,fixedDelay = 1000*60*60)
    public void cardAutoStopMonitor(){
        log.info("定时监控卡套餐的使用量和有效期...");
        List<RefCardMonitorModel> refCardMonitorModels = iotTelecomCardInfoMapper.monitorCard(new BigDecimal("3"), 2,4);
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
                    List<IotTelecomRefCardCost> collect = iotTelecomRefCardCost.getOilByParentId( item.getId());
                    if(CollectionUtils.isNotEmpty(collect)){
                        for (IotTelecomRefCardCost unicom:collect) {
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
                    boolean flag = usage.compareTo(critical) > 0;
                    if(flag){
                        //加油包的用量= 总用量-原始用量/比例系数
                        oilUsage = usage.subtract(critical);
                        //基础包的用量
                        baseUsage = usage.subtract(oilUsage);
                    }
                    BigDecimal initUsages = item.getInitUsaged() == null ? BigDecimal.ZERO : item.getInitUsaged();
                    //加油包也手动配置了初始用量
                    initUsages = initUsages.add(addOilInit);
                    //计算总用量时候，加油包也算虚量
                    BigDecimal realUsage = baseUsage.multiply(item.getCustomPackageUse()).add(initUsages).add(oilUsage.multiply(item.getCustomPackageUse()));
                    log.info("{} 卡总流量{}，总用量：{}，加油包用量：{}", item.getIccid(), item.getOriginUse().add(addOil), realUsage, oilUsage);
                    //===================================== 用量超过了 =================================
                    if(realUsage.compareTo(item.getOriginUse().add(addOil)) >= 0){
                        Date endTime;
                        //看看当前套餐是否是免费套餐
                        if("1".equals(item.getFreeType())){
                            endTime = DateUtils.addDays2CurrentDate(7);
                        }else{
                            endTime = DateUtils.addHour2Date(now, 12);
                        }
                        Map nextPackageIn24Hour = iotTelecomRefCardCost.getNextPackageIn24Hour(item.getIccid(), CardCostActiveEnum.INACTIVE.getActive(), endTime);
                        if(CollectionUtils.isNotEmpty(nextPackageIn24Hour)){
                            //将当前套餐改为失效
                            updateFailure(oilIds,3,item.getFreeType());
                            //将即将生效的套餐改为生效中
                            updateTaskEffect(nextPackageIn24Hour.get("id").toString(), now);
                        }else{
                            //当前这个套餐是失效的
                            if(item.getActive().intValue() == 2 || item.getActive().intValue() == 3){
                                //有没有提前生效的基础包
                                Integer inAdvancePackageCount = iotTelecomRefCardCost.getInAdvancePackageCount(item.getIccid(), 1);
                                if(inAdvancePackageCount>=1){
                                    return;
                                }
                            }
                            log.info("卡号：{} 用量已超，申请停机，总量：{}，用量：{}，停机",item.getIccid(),item.getOriginUse().add(addOil),usage);
                            // 修改套餐为已失效，卡状态为已停机
                            updateStatus(item.getAccessNumber(), oilIds, TelecomCardStatusEnum.CLEANED.getIntCode(), CardCostActiveEnum.OVER_USAGE_DISABLED);
                        }
                        return;
                    }
                    //================================ 时间到期 =====================================
                    Date compareDate = DateUtils.addHour2Date(validEnd,-1);
                    //时间到期了
                    //当前套餐时间已失效
                    if(now.compareTo(compareDate) >=0 ){
                        //12小时内是否有即将生效的基础包
                        Date endTime = DateUtils.addHour2Date(validEnd, 12);
                        Map nextPackageIn24Hour = iotTelecomRefCardCost.getNextPackageIn24Hour(item.getIccid(), CardCostActiveEnum.INACTIVE.getActive(), endTime);
                        if(CollectionUtils.isNotEmpty(nextPackageIn24Hour)){
                            //将当前套餐改为失效
                            updateFailure(oilIds,2,item.getFreeType());
                            //将即将生效的套餐改为生效中
                            updateTaskEffect(nextPackageIn24Hour.get("id").toString(), (Date) nextPackageIn24Hour.get("valid_start"));
                        }else{
                            //下个周期没有可用套餐，，停机
                            String overDate = DateUtils.formatDateToFullString(item.getValidEnd());
                            log.info("卡号：{} 超出有效期 {}，申请停机",item.getIccid(),overDate);
                            // 修改套餐为已失效，卡状态为已停机
                            updateStatus(item.getAccessNumber(),oilIds, TelecomCardStatusEnum.CLEANED.getIntCode(), CardCostActiveEnum.OVERTIME_DISABLED);
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
     * @param iotRefCardCostId
     * @param cardStatus
     * @param cardCostStatus
     */
    public void updateStatus(String iccid,String iotRefCardCostId, Integer cardStatus, CardCostActiveEnum cardCostStatus) {
        log.info("电信定时监控卡iccid：{}套餐，调用接口修改卡状态为：{}，", iccid,cardStatus);
        Map businessMap = new HashMap();
        //发生位置是monitor定时任务
        businessMap.put("action","1");
        //停机 --->在用 orderTypeId =20
        businessMap.put("method","disabledNumber");
        businessMap.put("acctCd","");
        businessMap.put("access_number",iccid);
        //停机
        if(cardStatus.intValue() == TelecomCardStatusEnum.CLEANED.getIntCode()){
            businessMap.put("orderTypeId","19");
        }else{
            //激活
            businessMap.put("orderTypeId","20");
        }
        iTelecomGatewayService.updateCardStatus(businessMap);
        // 修改套餐状态
        if (iotRefCardCostId != null) {
            String id = iotRefCardCostId;
            if(iotRefCardCostId.endsWith(",")){
                id = iotRefCardCostId.substring(0, iotRefCardCostId.length()-1);
            }
            String[] ids= id.split(",");
            for(int i=0;i<ids.length;i++){
                IotTelecomRefCardCost updateRefCardCost = new IotTelecomRefCardCost();
                updateRefCardCost.setId(ids[i]);
                updateRefCardCost.setActive(cardCostStatus.getActive());
                updateRefCardCost.setValidEnd(new Date());
                iotTelecomRefCardCost.updateById(updateRefCardCost);
            }
        }
    }
    /**
     * 将当前套餐改为失效
     * @param id
     */
    public void updateFailure(String id, int active, String freeType){
        String oilId = id;
        if(id.endsWith(",")){
            oilId = id.substring(0, id.length()-1);
        }
        String[] oilIds = oilId.split(",");
        for(int i=0;i<oilIds.length;i++){
            IotTelecomRefCardCost updateRefCardCost = new IotTelecomRefCardCost();
            updateRefCardCost.setId(oilIds[i]);
            updateRefCardCost.setActive(active);
            if("1".equals(freeType)){
                updateRefCardCost.setValidEnd(new Date());
            }
            iotTelecomRefCardCost.updateById(updateRefCardCost);
        }
    }

    /**
     * 将即将生效的套餐改为生效中
     * @param id
     */
    public void updateTaskEffect(String id, Date date){
        IotTelecomRefCardCost updateRefCardCost = new IotTelecomRefCardCost();
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
        iotTelecomRefCardCost.updateById(updateRefCardCost);
    }


}
