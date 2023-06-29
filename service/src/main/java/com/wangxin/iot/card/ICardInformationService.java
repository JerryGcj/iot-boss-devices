package com.wangxin.iot.card;

import com.wangxin.iot.config.TemplateConfig;

import java.util.List;

/**
 * @author: yanwin
 * @Date: 2020/2/27
 * 物联网卡核心service
 */
public interface ICardInformationService {
    void syncCardUsaged(List<String> iccid, TemplateConfig templateConfig);

    int updateCardStatus(String status, String iccid);
}
