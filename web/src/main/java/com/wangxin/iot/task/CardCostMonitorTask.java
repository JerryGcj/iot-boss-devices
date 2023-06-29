package com.wangxin.iot.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangxin.iot.card.IStopIccidService;
import com.wangxin.iot.constants.CardCostActiveEnum;
import com.wangxin.iot.mapper.CardInformationMapper;
import com.wangxin.iot.mapper.IotRefCardCostMapper;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.CardInformation;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.IotRefCardCost;
import com.wangxin.iot.model.MobileRefCardMonitorModel;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.utils.DateUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
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
public class CardCostMonitorTask {
    @Autowired
    Executor executor;
    @Autowired
    private IotTemplateFactory iotTemplateFactory;

    @Autowired
    private CardInformationMapper cardInformationMapper;

    @Autowired
    private IotRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    private IStopIccidService stopIccidService;

    @Scheduled(initialDelay = 1000*60*30,fixedDelay = 1000*60*60)
    public void cardAutoStopMonitor(){
        log.info("定时监控卡套餐的使用量和有效期...");
        //此处设置虚量倍数是2，代表虚量50%，会向下包含，
        List<MobileRefCardMonitorModel> refCardMonitorModels = cardInformationMapper.monitorCard(null, 2,3);
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
                    List<IotRefCardCost> collect = iotRefCardCostMapper.getOilByParentId(item.getId());
                    if(CollectionUtils.isNotEmpty(collect)){
                        for (IotRefCardCost unicom:collect) {
                            oilIds += unicom.getId()+",";
                            addOil = addOil.add(unicom.getOriginUse());
                            addOilInit = addOilInit.add(unicom.getInitUsaged());
                        }
                    }
                    //基础包到期时间
                    Date validEnd = item.getValidEnd();
                    // 判断该套餐是否超出有效期，是否超出套餐最大量，如果超出，则修改套餐为已失效，卡状态为已停机
                    BigDecimal usage = item.getUsaged() == null ? BigDecimal.ZERO : item.getUsaged();
                    BigDecimal initUsages = item.getInitUsaged() == null ? BigDecimal.ZERO : item.getInitUsaged();
                    //加油包也手动配置了初始用量
                    initUsages = initUsages.add(addOilInit);
                    //总流量
                    BigDecimal sumOriginFlow = item.getOriginUse().add(addOil);
                    //真实用量加油包的虚量也和基础包保持一致
                    BigDecimal realUsage = usage.multiply(item.getCustomPackageUse()).add(initUsages);
                    log.info("{} 卡总流量{}，初始用量：{}，总用量：{}", item.getIccid(),sumOriginFlow,initUsages, realUsage);
                    //===================================== 用量超过了 =================================
                    if(realUsage.compareTo(sumOriginFlow) >= 0){
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
                            log.info("卡号：{} 用量已超，申请停机，总量：{}，用量：{}，停机",item.getIccid(),sumOriginFlow,realUsage);
                            // 修改套餐为已失效，卡状态为已停机
                            updateStatus(item.getIccid(), item.getCid(), oilIds, item.getCustomerId(), "4", CardCostActiveEnum.OVER_USAGE_DISABLED);
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
                                //下个周期没有可用套餐，停机
                                log.info("卡号：{} 超出有效期 {}，申请停机",item.getIccid(),DateUtils.formatDateToFullString(item.getValidEnd()));
                                // 修改套餐为已失效，卡状态为已停机
                                updateStatus(item.getIccid(), item.getCid(), oilIds, item.getCustomerId(), "4", CardCostActiveEnum.OVERTIME_DISABLED);
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
     * 根据当前生效套餐的到期时间查询3小时内即将要生效的基础包
     * @param iccid
     * @param time
     * @return
     */
    public IotRefCardCost getWillActiveCardCost(String iccid, Date time) {
        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_iccid", iccid);
        queryWrapper.eq("cost_type", 0);
        queryWrapper.eq("active", CardCostActiveEnum.INACTIVE.getActive());
        queryWrapper.le("valid_start", time);
        return iotRefCardCostMapper.selectOne(queryWrapper);
    }

    /**
     * 查询生效中的加油包
     * @param iccid
     * @return
     */
    public List<IotRefCardCost> getActivedOilCardCostList(String iccid) {
        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_iccid", iccid).eq("cost_type", 1).eq("active", CardCostActiveEnum.ACTIVED.getActive()).orderByAsc("valid_start");
        return iotRefCardCostMapper.selectList(queryWrapper);
    }

    /**
     * 根据最早生效中的加油包获取已失效的基础包
     * @param iccid
     * @param startTime
     * @param endTime
     * @return
     */
    public IotRefCardCost getNoActiveCardCost(String iccid, Date startTime, Date endTime) {
        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_iccid", iccid).eq("cost_type", 0)
                .in("active",CardCostActiveEnum.OVERTIME_DISABLED.getActive(),CardCostActiveEnum.OVER_USAGE_DISABLED.getActive())
                .eq("valid_end", endTime).le("valid_start", startTime);
        return iotRefCardCostMapper.selectOne(queryWrapper);
    }

    /**
     * 分页查询所有卡信息列表
     * @param current
     * @return
     */
    public List<CardInformation> getCardInfoList(Integer current) {
        // 分页查询卡信息列表
        IPage<CardInformation> page = new Page<>(current, 100);
        IPage<CardInformation> cardInformationPage = cardInformationMapper.selectPage(page, null);
        return cardInformationPage.getRecords();
    }

    /**
     * 查询状态为生效中的套餐列表
     * @param iccid
     * @return
     */
    public List<IotRefCardCost> getActivedCardCostList(String iccid) {
//        log.info("定时监控卡套餐，查询状态为生效中的套餐");
        // 查询生效中的套餐列表
        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_iccid", iccid);
        queryWrapper.eq("active", CardCostActiveEnum.ACTIVED.getActive());
        return iotRefCardCostMapper.selectList(queryWrapper);
    }

    /**
     * 在基础套餐时间内订购过的加油包的总量
     * @param iccid
     * @return
     */
    public List<IotRefCardCost> getOilCardCostList(String iccid, String costType, Date startTime, Date endTime) {
        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_iccid", iccid).eq("cost_type", costType).ge("valid_start", startTime).le("valid_end", endTime);
        return iotRefCardCostMapper.selectList(queryWrapper);
    }

    /**
     * 查询状态为未生效且在有效期内的套餐列表
     * @param iccid
     * @return
     */
    public List<IotRefCardCost>  getInActiveCardCostList(String iccid) {
//        log.info("定时监控卡套餐，查询状态为未生效且在有效期内的套餐");
        //提前三个小时
        Date now = new Date();
        Date startTime = DateUtils.addHour2Date(now, -6);
        Date endTime = DateUtils.addHour2Date(now, 6);
        // 查询未生效且在有效期内的套餐列表
        QueryWrapper<IotRefCardCost> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("card_iccid", iccid);
        queryWrapper.eq("active", CardCostActiveEnum.INACTIVE.getActive());
        queryWrapper.between("valid_start", startTime, endTime);
        queryWrapper.gt("valid_end", endTime);
        return iotRefCardCostMapper.selectList(queryWrapper);
    }

    /**
     * 修改卡和套餐状态
     * @param iccid
     * @param cardInfoId
     * @param iotRefCardCostId
     * @param cardStatus
     * @param cardCostStatus
     */
    public void updateStatus(String iccid, String cardInfoId, String iotRefCardCostId, String customerId, String cardStatus, CardCostActiveEnum cardCostStatus) {
        log.info("定时监控卡iccid：{}套餐，调用接口修改卡状态为：{}，", iccid,cardStatus);
        IotOperatorTemplate operatorTemplate = iotTemplateFactory.getOperatorTemplate(iccid);
        ThirdService executorThridService = iotTemplateFactory.getExecutorThridService(operatorTemplate);
        Map<String, String> paramMap = new HashMap();
        paramMap.put("iccid", iccid);
        paramMap.put("status",cardStatus);
        paramMap.put("action","1");
        boolean response = executorThridService.modifyCard(paramMap, operatorTemplate);
        if (!response) {
            log.error("调用接口修改卡状态失败，iccid：{}", iccid);
            //修改失败后，继续下面的逻辑
//            return;
        }
        if("4".equals(cardStatus)){
            stopIccidService.saveStopIciid(iccid, customerId, String.valueOf(cardCostStatus.getActive()));
        }
        // 修改卡状态
        CardInformation updateCardInfo = new CardInformation();
        updateCardInfo.setId(cardInfoId);
        updateCardInfo.setCardState(cardStatus);
        cardInformationMapper.updateById(updateCardInfo);
        // 修改套餐状态
        if (iotRefCardCostId != null) {
            String id = iotRefCardCostId;
            if(iotRefCardCostId.endsWith(",")){
                id = iotRefCardCostId.substring(0, iotRefCardCostId.length()-1);
            }
            String[] ids= id.split(",");
            for(int i=0;i<ids.length;i++){
                IotRefCardCost updateRefCardCost = new IotRefCardCost();
                updateRefCardCost.setId(ids[i]);
                updateRefCardCost.setActive(cardCostStatus.getActive());
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
            IotRefCardCost updateRefCardCost = new IotRefCardCost();
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
        IotRefCardCost updateRefCardCost = new IotRefCardCost();
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
