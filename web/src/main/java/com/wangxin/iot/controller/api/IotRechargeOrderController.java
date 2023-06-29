package com.wangxin.iot.controller.api;

import com.wangxin.iot.card.IIotRechargeOrderService;
import com.wangxin.iot.rest.base.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * 用户充值controller
 */
@Controller
@RequestMapping("/mobileApi/rechargeOrder")
@Slf4j
public class IotRechargeOrderController {

    @Autowired
    private ApplicationContext applicationContext;
    @Autowired
    private IIotRechargeOrderService iIotRechargeOrderService;
    @ResponseBody
    @RequestMapping(value = "/recharge", method={RequestMethod.POST})
    public Result<?> inquire(@RequestBody String request){
       return Result.success(null);
    }

}
