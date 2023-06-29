package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IIotCardWechatRelationService;
import com.wangxin.iot.mapper.WechatRelationMapper;
import com.wangxin.iot.model.IotCardWechatRelation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

/**
 * @Description: iot_card_wechat_relation
 * @Author: jeecg-boot
 * @Date:   2020-04-26
 * @Version: V1.0
 */
@Service
@Slf4j
public class IotCardWechatRelationServiceImpl extends ServiceImpl<WechatRelationMapper, IotCardWechatRelation> implements IIotCardWechatRelationService {

}
