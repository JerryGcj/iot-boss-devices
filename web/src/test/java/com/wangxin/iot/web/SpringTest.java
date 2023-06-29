package com.wangxin.iot.web;

import com.wangxin.iot.douyin.IElectronChannelDouyinOrderService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @author anan
 * @date 2023/3/27 16:27
 */
@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SpringTest {
    @Autowired
    IElectronChannelDouyinOrderService electronChannelDouyinOrderService;
    @Test
    public void test(){
//        electronChannelDouyinOrderService.test1();
    }
}
