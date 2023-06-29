package com.wangxin.iot.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.constants.ResponseConstants;
import com.wangxin.iot.mapper.CustomerSalesDiscountMapper;
import com.wangxin.iot.mapper.SysUserMapper;
import com.wangxin.iot.model.CustomerSalesDiscount;
import com.wangxin.iot.model.SysUser;
import com.wangxin.iot.model.api.product.ProductResponse;
import com.wangxin.iot.model.api.product.Response2;
import com.wangxin.iot.utils.Frequently;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * 查询可充值套餐接口
 */
@Controller
@RequestMapping("/api/v1.0/query")
@Slf4j
public class QueryProductController {

    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private CustomerSalesDiscountMapper customerSalesDiscountMapper;

    @RequestMapping(value = "/product")
    @ResponseBody
    public ProductResponse query(HttpServletRequest request){
        log.info("可充值套餐查询接口请求报文{}", request.getParameter("traderId"));
        ProductResponse result = new ProductResponse();
        List<Response2> res = new ArrayList<>();
        try{
            if(null==request){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("可充值套餐查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }
            String traderId = request.getParameter("traderId");
            if(StringUtils.isBlank(traderId)){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info("可充值套餐查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(Frequently.isLimit(traderId)){
                System.out.println("可充值套餐查询频繁");
                result.setCode(ResponseConstants.FAIL_CODE16.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE16.getMsg());
                return result;
            }
            QueryWrapper<SysUser> userQueryWrapper = new QueryWrapper<>();
            userQueryWrapper.eq("username", traderId);
            SysUser user = sysUserMapper.selectOne(userQueryWrapper);
            if(null==user){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info(traderId+" 可充值套餐查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(2==user.getStatus()||user.getDelFlag()==1){
                result.setCode(ResponseConstants.FAIL_CODE5.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE5.getMsg());
                log.info(traderId+" 可充值套餐查询接口返回{}{}", ResponseConstants.FAIL_CODE5.getCode(), ResponseConstants.FAIL_CODE5.getMsg());
                return result;
            }
            String ip = com.wangxin.iot.utils.StringUtils.getRemoteAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            String ips = user.getIpWhite();
            if(StringUtils.isBlank(ips)||!ips.contains(ip)){
                result.setCode(ResponseConstants.FAIL_CODE9.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE9.getMsg());
                log.info(traderId+" 可充值套餐查询接口返回{}{}", ip, ResponseConstants.FAIL_CODE9.getMsg());
                return result;
            }
            //根据用户名查询可充值套餐
            QueryWrapper<CustomerSalesDiscount> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("agent_id", user.getId());
            List<CustomerSalesDiscount> lists = customerSalesDiscountMapper.selectList(queryWrapper);
            if(lists.isEmpty()||lists.size()==0){
                result.setCode(ResponseConstants.FAIL_CODE17.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE17.getMsg());
                log.info(traderId+" 可充值套餐查询接口返回{}{}", ResponseConstants.FAIL_CODE17.getCode(), ResponseConstants.FAIL_CODE17.getMsg());
                return result;
            }
            String oreratorName = "";
            for(CustomerSalesDiscount t : lists){
                Response2 response = new Response2();
                response.setPackageId(t.getPackageId());
                response.setPackageName(t.getPackageName());
                switch (t.getOperatorType()){
                    case "1":
                        oreratorName = "移动";
                        break;
                    case "2":
                        oreratorName = "联通";
                        break;
                    case "3":
                        oreratorName = "电信";
                        break;
                    case "4":
                        oreratorName = "第三方";
                        break;
                }
                response.setOperatorType(oreratorName);
                response.setPrice(t.getSalesPrice());
                res.add(response);
            }
            result.setCode(ResponseConstants.SUCCESS_CODE.getCode());
            result.setMsg(ResponseConstants.SUCCESS_CODE.getMsg());
            result.setData(res);
            log.info(traderId+" 可充值套餐查询接口返回{}", result);
            return result;
        }catch (Exception e){
            result.setCode(ResponseConstants.FAIL_CODE16.getCode());
            result.setMsg(ResponseConstants.FAIL_CODE16.getMsg());
            log.error("可充值套餐查询接口异常{}", e);
            return result;
        }
    }
}
