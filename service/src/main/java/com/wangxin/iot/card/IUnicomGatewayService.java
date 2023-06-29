package com.wangxin.iot.card;

import com.wangxin.iot.domain.RefCardModel;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author: yanwin
 * @Date: 2020/7/20
 */
public interface IUnicomGatewayService {
    /**
     * 调用联通接口，修改卡状态
     * @param reqMap
     */
    boolean updateCardStatus(Map reqMap);
    /**
     * 同步卡用量
     * @param refCardModel
     * @param currentDate
     */
    @Deprecated
    void syncUsaged(RefCardModel refCardModel, Date currentDate);


    void syncUsage(List<RefCardModel> refCardModels);

    /**
     * 较正卡流量
     * @param iccids
     */
    void regulateFlow(Set<String> iccids);

    /**
     * 调用联通接口，修改通信计划
     * @param reqMap
     */
    boolean updateCommunicationPlan(Map reqMap);

    /**
     * 查询卡实名状态
     * @param iccid
     */
    void realNameStatus(String iccid);
}
