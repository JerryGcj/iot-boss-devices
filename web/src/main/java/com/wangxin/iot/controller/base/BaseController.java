package com.wangxin.iot.controller.base;

import com.wangxin.iot.constants.ErrorConstants;
import com.wangxin.iot.rest.exception.ApiException;
import com.wangxin.iot.rest.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;

/**
 * @Description :  Controller 抽象功能
 * @author: 张闻帅
 * @version: V1.0
 * @Date: 2019/08/10
 */
@Configuration
@Slf4j
public class BaseController {
	/**
	 * ApiException 异常捕获
	 * @param api   内部自定义异常
	 * @return
	 */
	@ExceptionHandler({ ApiException.class})
	public ErrorResponse handlerException(ApiException api, HttpServletResponse response) {

		log.error("[retCode:"+api.getRetCode()+", Retessage: "+ api.getRetMessage()+"]");

		ErrorResponse errorResponse = new ErrorResponse(api);
		return errorResponse;
	}

	/**
	 * 所有异常捕获
	 * @param e 非自定义异常
	 * @return
	 */
	@ExceptionHandler(Exception.class)
	public ErrorResponse handlerException(Exception e, HttpServletResponse response) {

		log.error("Error Message: ", e);

		ErrorResponse errorResponse = new ErrorResponse(ErrorConstants.SYS_ERR);
		return errorResponse;
	}
}
