package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.wangxin.iot.card.IStopIccidService;
import com.wangxin.iot.mapper.IotCardWechatRelationMapper;
import com.wangxin.iot.mapper.StopIccidMapper;
import com.wangxin.iot.model.IotCardWechatRelation;
import com.wangxin.iot.model.StopIccid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

/**
 * @Description: stop_iccid
 * @Author: jeecg-boot
 * @Date:   2022-09-02
 * @Version: V1.0
 */
@Service
public class StopIccidServiceImpl extends ServiceImpl<StopIccidMapper, StopIccid> implements IStopIccidService {

    @Autowired
    private IotCardWechatRelationMapper cardWechatRelationMapper;

    @Async
    @Override
    public void saveStopIciid(String iccid, String customerId, String cardCostStatus) {
        if("a7ee924cca08294bc6058f5fa1395bd8".equals(customerId)||"57d234910092bc54f427b5d6af8bc216".equals(customerId)){
            QueryWrapper<IotCardWechatRelation> queryWrapper1 = new QueryWrapper<>();
            queryWrapper1.eq("iccid",iccid).orderByDesc("create_time");
            List<IotCardWechatRelation> list = cardWechatRelationMapper.selectList(queryWrapper1);
            if(CollectionUtils.isNotEmpty(list)){
                StopIccid stopIccid = new StopIccid();
                stopIccid.setIccid(iccid);
                stopIccid.setMobile(list.get(0).getMobile());
                stopIccid.setOperatorType("1");
                stopIccid.setUserId(customerId);
                stopIccid.setStatus(cardCostStatus);
                stopIccid.setCreateTime(new Date());
                this.save(stopIccid);
            }
        }
    }
}
