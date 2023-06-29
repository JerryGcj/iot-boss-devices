package com.wangxin.iot.task;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.mobile.ThirdService;
import com.wangxin.iot.model.IotOperatorTemplate;
import com.wangxin.iot.model.RealNameSystem;
import com.wangxin.iot.other.IotTemplateFactory;
import com.wangxin.iot.utils.MD5Util;
import com.wangxin.iot.utils.wechat.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Slf4j
public class RealNameStatusTask {

    @Autowired
    private RealNameSystemMapper realNameSystemMapper;
    @Autowired
    private RestTemplate restTemplate;
    @Autowired
    IotTemplateFactory iotTemplateFactory;
    /**
     * 15m 执行一次，查询移动实名制未通过的卡
     */
    //@Scheduled(initialDelay = 1000*60*60,fixedDelay = 1000*60*15)
    public void mobileRealNameQuery(){
        QueryWrapper<RealNameSystem> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operator_type", "1");
        queryWrapper.eq("status", "0");
        queryWrapper.gt("create_time", "2022-10-11 10:06:24");
        List<RealNameSystem> realNameSystems = realNameSystemMapper.selectList(queryWrapper);
        realNameSystems.forEach(item->{
            try {
                IotOperatorTemplate templateByOperation = iotTemplateFactory.getOperatorTemplate(item.getIccid());
                //根据iccid,查找对应的通道
                ThirdService thirdService = iotTemplateFactory.getExecutorThridService(templateByOperation);
                thirdService.realNameStatus(item.getIccid(),item.getId(),templateByOperation);
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

}
