package com.wangxin.iot.web;

import com.wangxin.iot.card.IDouyinGatewayApiService;
import com.wangxin.iot.task.xxl.DouyinOrderJob;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class DouyinTest {
	@Autowired
	IDouyinGatewayApiService douyinGatewayApiService;
	@Autowired
	DouyinOrderJob douyinOrderJob;
	@Test
	public void pullOneOrder() {
		douyinGatewayApiService.pullOneOrder("37636259","6919512215622391519");
	}
	@Test
	public void pullOrder()  {
		douyinOrderJob.pullOrder("37636259");
	}
	@Test
	public void decryptDouyinOrder()  {
		douyinOrderJob.decryptDouyinOrder("37636259");
	}
	@Test
	public void pushCancelMsg()  {
		douyinOrderJob.pushCancelMsg("37636259");
	}
	@Test
	public void pushExpressInfo()  {
		douyinOrderJob.pushExpress("37636259");
	}


}
