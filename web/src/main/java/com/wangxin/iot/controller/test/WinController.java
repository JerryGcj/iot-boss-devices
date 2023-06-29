package com.wangxin.iot.controller.test;

import com.wangxin.iot.mapper.IotRefCardCostMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Description :  测试
 * @author: Mark (majianyou@wxdata.cn)
 * @version:
 * @Date: 2018/08/13
 */

@Slf4j
@RestController
@RequestMapping("demo")
public class WinController {

	private static final ThreadLocal<Integer> THREAD_LOCAL = ThreadLocal.withInitial(()-> null);
	Lock lock = new ReentrantLock();
	int i = 0;


	@Autowired
	private ApplicationContext applicationContext;

	@Autowired
	private IotRefCardCostMapper iotRefCardCostMapper;

	@RequestMapping(value = "threadLocal/{userId}", method = RequestMethod.GET)
	public Integer threadLocal(@PathVariable Integer userId){
		System.out.println(Thread.currentThread().getName()+"\t"+"before:"+THREAD_LOCAL.get());
		THREAD_LOCAL.set(userId);
		System.out.println(Thread.currentThread().getName()+"\t"+"end:"+THREAD_LOCAL.get());
		return userId;
	}

	@RequestMapping(value = "sync", method = RequestMethod.GET)
	public Integer sync(){
//		applicationContext.getBeansOfType(BeanPostProcessors.class);
		return i++;
	}
	@RequestMapping(value = "date", method = RequestMethod.GET)
	public String[] date(){
		return applicationContext.getBeanDefinitionNames();
	}
	@RequestMapping(value = "date2", method = RequestMethod.GET)
	public Date date2(){
		return new Date();
	}
}
