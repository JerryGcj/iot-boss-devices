package com.wangxin.iot.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.constants.ResponseConstants;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.SysUserMapper;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.model.SysUser;
import com.wangxin.iot.model.api.orderList.OrderListResponse;
import com.wangxin.iot.model.api.orderList.Response1;
import com.wangxin.iot.model.api.orderList.SonOrderResponse;
import com.wangxin.iot.utils.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 查询订单接口
 */
@Controller
@RequestMapping("/api/v1.0/query")
@Slf4j
public class QueryOrderController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private OrderMapper orderMapper;

    @RequestMapping(value = "/order")
    @ResponseBody
    public OrderListResponse query(@RequestBody String request){
        log.info("订单查询接口请求报文{}", request);
        OrderListResponse result = new OrderListResponse();
        List<SonOrderResponse> res = new ArrayList<>();
        try{
            Map<String, String> map = JsonUtil.parseRequestJson(request);
            if(null==map||map.isEmpty()){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("订单查询接口返回{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }
            String traderId = map.get("traderId");
            String bizOrderNo = map.get("bizOrderNo");
            String outTradeNo = map.get("outTradeNo");
            String timeStamp = map.get("timeStamp");
            String sign = map.get("sign");
            Date now = new Date();
            //判断用户名是否为空
            if(StringUtils.isBlank(traderId)){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }

            if(Frequently.isLimit(traderId)){
                System.out.println("订单查询频繁");
                result.setCode(ResponseConstants.FAIL_CODE16.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE16.getMsg());
                return result;
            }
            //判断订单号是否符合规格
            if(StringUtils.isBlank(bizOrderNo)&&StringUtils.isBlank(outTradeNo)){
                result.setCode(ResponseConstants.FAIL_CODE8.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE8.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE8.getCode(), ResponseConstants.FAIL_CODE8.getMsg());
                return result;
            }
            //判断时间戳是否为空 && 判断时间戳是否符合格式
            if(StringUtils.isBlank(timeStamp)){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }else{
                try {
                    long time = Math.abs(now.getTime()/1000 - Long.parseLong(timeStamp));
                    if(time > 300){//5分钟以内
                        result.setCode(ResponseConstants.FAIL_CODE14.getCode());
                        result.setMsg(ResponseConstants.FAIL_CODE14.getMsg());
                        log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE14.getCode(), ResponseConstants.FAIL_CODE14.getMsg());
                        return result;
                    }
                } catch (Exception e) {
                    result.setCode(ResponseConstants.FAIL_CODE15.getCode());
                    result.setMsg(ResponseConstants.FAIL_CODE15.getMsg());
                    log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE15.getCode(), ResponseConstants.FAIL_CODE15.getMsg());
                    return result;
                }
            }
            //判断签名是否为空
            if(StringUtils.isBlank(sign)){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }

            QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", traderId);
            SysUser user = sysUserMapper.selectOne(userQueryWrapper);
            if(null==user){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(user.getStatus()==2||user.getDelFlag()==1){
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE5.getCode(), ResponseConstants.FAIL_CODE5.getMsg());
                result.setCode(ResponseConstants.FAIL_CODE5.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE5.getMsg());
                return result;
            }
            //验证签名是否一致
            if(StringUtils.isBlank(bizOrderNo)){
                map.remove("bizOrderNo");
            }
            if(StringUtils.isBlank(outTradeNo)){
                map.remove("outTradeNo");
            }
            map.remove("sign");
            String signStr = HttpClientHelper.getParamsByAscending(map);
            String thisSign = MD5Util.md5Encrypt32Lower(signStr+user.getTheKey());
            if(!thisSign.equals(sign)){
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询签名源串："+signStr+user.getTheKey());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询签名："+thisSign);
                result.setCode(ResponseConstants.FAIL_CODE3.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE3.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE3.getCode(), ResponseConstants.FAIL_CODE3.getMsg());
                return result;
            }
            //校验IP
            String ip = com.wangxin.iot.utils.StringUtils.getRemoteAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            String ips = user.getIpWhite();
            if(StringUtils.isBlank(ips)||!ips.contains(ip)){
                result.setCode(ResponseConstants.FAIL_CODE9.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE9.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ip, ResponseConstants.FAIL_CODE9.getMsg());
                return result;
            }
            //参数校验通过，去数据库查询订单
            List<Order> lists = null;
            try{
                if(StringUtils.isNotBlank(outTradeNo)){
                    lists = orderMapper.getByMchOrderId(user.getId(), outTradeNo);
                }else{
                    lists = orderMapper.getByOrderId(user.getId(), bizOrderNo);
                }
            }catch (Exception e){
                lists = null;
                log.error("从数据库查询订单详情异常：", e);
            }
            if(lists.isEmpty()||lists.size()==0){
                result.setCode(ResponseConstants.FAIL_CODE8.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE8.getMsg());
                log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}{}", ResponseConstants.FAIL_CODE8.getCode(), ResponseConstants.FAIL_CODE8.getMsg());
                return result;
            }

            for(Order order : lists){
                SonOrderResponse response = new SonOrderResponse();
                response.setSubBizOrderNo(order.getOrderId());
                response.setIccid(order.getIccid());
                response.setStatus(order.getOrderState());
                res.add(response);
            }
            Response1 resp = new Response1();
            resp.setGmtCreate(DateUtils.formatDateToFullString(lists.get(0).getCreateTime()));
            resp.setBizOrderNo(lists.get(0).getOrderId());
            resp.setOutTradeNo(lists.get(0).getMchOrderId());
            resp.setSubBizOrderResponseList(res);
            result.setCode(ResponseConstants.SUCCESS_CODE.getCode());
            result.setMsg(ResponseConstants.SUCCESS_CODE.getMsg());
            result.setData(resp);
            log.info(bizOrderNo+"/"+outTradeNo+" 订单查询接口返回{}", result);
            return result;
        }catch (Exception e){
            result.setCode(ResponseConstants.FAIL_CODE16.getCode());
            result.setMsg(ResponseConstants.FAIL_CODE16.getMsg());
            log.error("订单查询接口异常{}", e);
            return result;
        }
    }
}
