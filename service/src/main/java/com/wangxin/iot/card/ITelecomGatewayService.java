package com.wangxin.iot.card;

import com.wangxin.iot.domain.RefCardModel;

import java.util.Map;

/**
 * @author: yanwin
 * @Date: 2020/11/5
 */
public interface ITelecomGatewayService {

    /**
     * 调用电信接口，修改卡状态,
     1: requestServActive:
        * 调用活卡激活接口（requestServActive）接口。迁移至”在用”状态后正式进入商用期，开始计费。
     2: disabledNumber:
         * 19表示停机保号，
         * 20表示停机保号后复机,
         * 21表示测试期去激活，
         * 22表示测试期去激活后回到测试激活
     *
     */
    boolean updateCardStatus(Map reqMap);

    /**
     *  根据iccid，获取对应的accessNumber
     * @param iccid
     */
    String acquireAccessNumber(String iccid);

    /**
     * 同步卡用量
     * @param refCardModels
     */
    void syncUsage(RefCardModel refCardModels);

    /**
     * 查询卡实名状态
     * @param accessNumber
     */
    void realNameStatus(String accessNumber);

    /**
     * 卡主状态查询
     * @param reqMap
     */
    String mainStatus(Map reqMap);

    /**
     * 推送类型是13并且msg是添加了单独断网。
     * @param notifyMap
     */
    void handlerCallback13(Map<String, String> notifyMap);

    /**
     * 自主限速
     * @param reqMap
     */
    void setSpeedValue(Map reqMap);

    /**
     * 清除实名信息
     * @param accessNumber
     */
    boolean removeRealName(String accessNumber);
}
