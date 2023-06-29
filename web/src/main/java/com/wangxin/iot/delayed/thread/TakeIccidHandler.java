package com.wangxin.iot.delayed.thread;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.card.IotUnicomRefCardCostService;
import com.wangxin.iot.delayed.entity.IccidDelayed;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.DelayQueue;

/**
 * @author anan
 * @date 2022/11/10 16:25
 */
@Component
@Slf4j
public class TakeIccidHandler implements InitializingBean {

    public static final BlockingQueue<IccidDelayed> blockingQueue = new DelayQueue<>();
    @Autowired
    IUnicomGatewayService iIoTGatewayApiService;
    @Autowired
    IotUnicomRefCardCostService iotUnicomRefCardCostService;
    @Override
    public void afterPropertiesSet() throws Exception {
        log.info("守护线程设置成功");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    try {
                        IccidDelayed iccidDelayed = blockingQueue.take();
                        System.out.println(iccidDelayed.getIccid());
                        QueryWrapper<IotUnicomRefCardCost> queryWrapper = new QueryWrapper<>();
                        queryWrapper.eq("iccid",iccidDelayed.getIccid());
                        queryWrapper.eq("active","1");
                        int count = iotUnicomRefCardCostService.count(queryWrapper);
                        if(count>0){
                            log.info("卡:{}有生效套餐 更换了imei，自动激活：{}",iccidDelayed.getIccid());
                            Map paramMap = new HashMap(3);
                            //发生位置是imei变更回调
                            paramMap.put("action","5");
                            paramMap.put("iccid", iccidDelayed.getIccid());
                            paramMap.put("goalState","2");
                            iIoTGatewayApiService.updateCardStatus(paramMap);
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
        thread.start();
    }
}
