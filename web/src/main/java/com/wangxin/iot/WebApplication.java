package com.wangxin.iot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
/** 当pom文件引入spring-aop时，默认启动aop，此处显示启动aop，方便理解 */
@EnableAspectJAutoProxy(exposeProxy=true)
/** 如果mybatis中service实现类中加入事务注解，需要此处添加该注解 */
@EnableTransactionManagement
@EnableScheduling
public class WebApplication {

	public static void main(String[] args) {

		SpringApplication.run(WebApplication.class, args);
	}
}
