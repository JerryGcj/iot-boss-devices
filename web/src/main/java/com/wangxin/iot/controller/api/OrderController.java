package com.wangxin.iot.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.constants.PayStatus;
import com.wangxin.iot.constants.ResponseConstants;
import com.wangxin.iot.event.WxPayCallbackEvent;
import com.wangxin.iot.mapper.CustomerSalesDiscountMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.StandardCostMapper;
import com.wangxin.iot.mapper.SysUserMapper;
import com.wangxin.iot.model.*;
import com.wangxin.iot.model.api.order.OrderResponse;
import com.wangxin.iot.model.api.order.Response;
import com.wangxin.iot.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 套餐订购接口
 */
@Controller
@RequestMapping("/api/v1.0")
@Slf4j
public class OrderController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    StandardCostMapper standardCostMapper;
    @Autowired
    private CustomerSalesDiscountMapper customerSalesDiscountMapper;
    @Autowired
    private ApplicationContext applicationContext;

    @ResponseBody
    @RequestMapping(value = "/buy", method={RequestMethod.POST})
    public Response inquire(@RequestBody String request){
        log.info("api套餐订购接口报文{}", request);
        long l1 = System.currentTimeMillis();
        Response result = new Response();
        Map<String, String> map = JsonUtil.parseRequestJson(request);
        if(null==map||map.isEmpty()){
            result.setCode(ResponseConstants.FAIL_CODE1.getCode());
            result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
            log.info("api订购接口返回{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
            return result;
        }
        try{
            //解析请求参数
            String iccid = map.get("iccid");
            String timeStamp = map.get("timeStamp");
            String sign = map.get("sign");
            String traderId = map.get("traderId");
            String productId = map.get("productId");
            String cycle = map.get("cycle");
            String effectiveType = map.get("effectiveType");
            String outTradeNo = map.get("outTradeNo");
            Date now = new Date();
            if(StringUtils.isBlank(cycle)){
                cycle = "1";
                map.remove("cycle");
            }
            if(StringUtils.isBlank(effectiveType)){
                effectiveType = "0";                map.remove("effectiveType");
            }
            //判断用户名是否为空
            if(StringUtils.isBlank(traderId)){
                log.info("api订购接口"+outTradeNo+"返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }

            //判断下家订单号是否符合规格
            if(StringUtils.isBlank(outTradeNo)){
                log.info("api订购接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }else{
                if(outTradeNo.length() > 50){
                    log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE6.getCode(), ResponseConstants.FAIL_CODE6.getMsg());
                    result.setCode(ResponseConstants.FAIL_CODE6.getCode());
                    result.setMsg(ResponseConstants.FAIL_CODE6.getMsg());
                    log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                    return result;
                }
            }
            //判断卡号是否符合规格
            if(StringUtils.isBlank(iccid)){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            String[] iccids = iccid.split(",");
            if(iccids.length>100){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE18.getCode(), ResponseConstants.FAIL_CODE18.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE18.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE18.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            //判断时间戳是否为空 && 判断时间戳是否符合格式
            if(StringUtils.isBlank(timeStamp)){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }else{
                try {
                    long time = Math.abs(now.getTime()/1000 - Long.parseLong(timeStamp));
                    if(time > 300){//5分钟以内
                        result.setCode(ResponseConstants.FAIL_CODE14.getCode());
                        result.setMsg(ResponseConstants.FAIL_CODE14.getMsg());
                        log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE14.getCode(), ResponseConstants.FAIL_CODE14.getMsg());
                        return result;
                    }
                } catch (Exception e) {
                    result.setCode(ResponseConstants.FAIL_CODE15.getCode());
                    result.setMsg(ResponseConstants.FAIL_CODE15.getMsg());
                    log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE15.getCode(), ResponseConstants.FAIL_CODE15.getMsg());
                    return result;
                }
            }
            //判断套餐编码是否符合格式
            if(StringUtils.isBlank(productId)){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE7.getCode(), ResponseConstants.FAIL_CODE7.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE7.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE7.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", traderId);
            SysUser user = sysUserMapper.selectOne(userQueryWrapper);
            if(null==user){
                log.info("api订购接口"+outTradeNo+"返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            if(user.getStatus()==2||user.getDelFlag()==1){
                log.info("api订购接口"+outTradeNo+"返回{}{}", ResponseConstants.FAIL_CODE5.getCode(), ResponseConstants.FAIL_CODE5.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE5.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE5.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            String ip = com.wangxin.iot.utils.StringUtils.getRemoteAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            String ips = user.getIpWhite();
            if(StringUtils.isBlank(ips)||!ips.contains(ip)){
                result.setCode(ResponseConstants.FAIL_CODE9.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE9.getMsg());
                log.info(outTradeNo+" api订购接口返回{}{}", ip, ResponseConstants.FAIL_CODE9.getMsg());
                return result;
            }
            //验证签名是否一致
            map.remove("sign");
            String signStr = HttpClientHelper.getParamsByAscending(map);
            String thisSign = MD5Util.md5Encrypt32Lower(signStr+user.getTheKey());
            if(!thisSign.equals(sign)){
                log.info(outTradeNo+" 签名源串："+signStr+user.getTheKey());
                log.info(outTradeNo+" 签名："+thisSign);
                result.setCode(ResponseConstants.FAIL_CODE3.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE3.getMsg());
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE3.getCode(), ResponseConstants.FAIL_CODE3.getMsg());
                return result;
            }

            //根据用户名和套餐编码查询是否配置了套餐
            QueryWrapper<CustomerSalesDiscount> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("package_id", productId).eq("agent_id", user.getId());
            CustomerSalesDiscount customerSalesDiscount = customerSalesDiscountMapper.selectOne(queryWrapper);
            if(null==customerSalesDiscount){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE7.getCode(), ResponseConstants.FAIL_CODE7.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE7.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE7.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }

            //验证订单号是否重复
            List<Order> orderLists = orderMapper.getByMchOrderId(user.getId(), outTradeNo);
            if(!orderLists.isEmpty()||orderLists.size()>0){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE10.getCode(), ResponseConstants.FAIL_CODE10.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE10.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE10.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            //验证余额
            if(user.getBalance().compareTo(customerSalesDiscount.getSalesPrice().multiply(new BigDecimal(iccids.length)))==-1){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE12.getCode(), ResponseConstants.FAIL_CODE12.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE12.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE12.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            //余额充足，开始扣除
            int a = customerSalesDiscountMapper.updateUserBalance(user.getId(), customerSalesDiscount.getSalesPrice().multiply(new BigDecimal(iccids.length)));
            if(a==0){
                log.info(outTradeNo+" api订购接口返回{}{}", ResponseConstants.FAIL_CODE19.getCode(), ResponseConstants.FAIL_CODE19.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE19.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE19.getMsg());
                log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
                return result;
            }
            //校验通过，开始插入订单
            //生成订单号
            StandardCost byTerminalSale = standardCostMapper.getByTerminalSale(customerSalesDiscount.getPackageId(),traderId);
            String orderNo = DateUtils.getTimetampNo("A");
            int insert = 0;
            for (int i=0;i<iccids.length;i++){
                Order order = new Order();
                order.setContainsFlow(byTerminalSale.getContainsFlow());

                order.setIccid(iccids[i]);
                order.setPackageId(customerSalesDiscount.getPackageId());
                order.setTradingMoney(customerSalesDiscount.getSalesPrice());
                order.setCreateUser(traderId);

                order.setCompanyName(user.getUserCompany());
                order.setCustomerId(user.getId());

                order.setPackageName(customerSalesDiscount.getPackageName());
                order.setBuyNumber(Integer.parseInt(cycle));
                order.setOrderId(orderNo);
                order.setPaymentChannel("2");
                order.setMchOrderId(outTradeNo);
                order.setEffectType(effectiveType);
                order.setCreateTime(new Date());
                order.setOrderState(2);
                order.setPayState(PayStatus.paySuccess.getCode());

                try{
                    insert = orderMapper.insert(order);
                }catch (Exception e){
                    insert = 0;
                    log.error("订单："+outTradeNo+"中的卡："+iccids[i]+"-->订购套餐："+customerSalesDiscount.getPackageName()+"插入异常：", e);
                }
                if(insert==1){
                    applicationContext.publishEvent(new WxPayCallbackEvent(order));
                }
            }
            OrderResponse orderResponse = new OrderResponse();
            orderResponse.setBizOrderNo(orderNo);
            orderResponse.setOutTradeNo(outTradeNo);
            result.setCode(ResponseConstants.SUCCESS_CODE.getCode());
            result.setMsg(ResponseConstants.SUCCESS_CODE.getMsg());
            result.setData(orderResponse);
            log.info(outTradeNo+" api订购接口返回{}", result);
            log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
            return result;
        }catch (Exception e){
            log.error("api订购接口异常：", e);
            log.info("api订购接口返回{}{}", ResponseConstants.FAIL_CODE20.getCode(), ResponseConstants.FAIL_CODE20.getMsg());
            result.setCode(ResponseConstants.FAIL_CODE20.getCode());
            result.setMsg(ResponseConstants.FAIL_CODE20.getMsg());
            log.info("--------耗时ms:"+(System.currentTimeMillis() - l1));
            return result;
        }

    }

}
