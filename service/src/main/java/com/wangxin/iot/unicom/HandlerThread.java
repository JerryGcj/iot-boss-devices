package com.wangxin.iot.unicom;

import com.wangxin.iot.model.Card;
import com.wangxin.iot.utils.ThreadPoolUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by 18765 on 2020/1/6 9:20
 */
@Component
@Slf4j
public class HandlerThread implements InitializingBean {

    public static final  BlockingQueue<Card> CARD_QUEUE = new LinkedBlockingQueue();

    @Autowired
//    IotRefCardCostService iotRefCardCostService;

    @Override
    public void afterPropertiesSet() {
        log.info("开启线程监听soap返回数据的队列");
        ThreadPoolUtils.getInstance().getThreadPoolExecutor().execute(new Custom());
    }
    class Custom implements Runnable{
        @Override
        public void run() {
            while (true){
                Card card = null;
                try {
                    card =  CARD_QUEUE.take();
                    log.info("队列的长度：{}，\n 队列中包含card:{}",CARD_QUEUE.size(),card);
                    //TODO(如果卡状态发生改变，修改数据库)
//                    iotRefCardCostService.updateUsaged(card);
                } catch (Exception e) {
                    log.info("处理error-> card:{}",card);
                    e.printStackTrace();
                }
            }
        }
    }
}
