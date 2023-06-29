package com.wangxin.iot.controller.api;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.wangxin.iot.constants.ResponseConstants;
import com.wangxin.iot.mapper.SysUserMapper;
import com.wangxin.iot.model.SysUser;
import com.wangxin.iot.model.api.balance.BalanceResponse;
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

/**
 * 查询余额接口
 */
@Controller
@RequestMapping("/api/v1.0/query")
@Slf4j
public class QueryBalanceController {

    @Autowired
    private SysUserMapper sysUserMapper;

    @RequestMapping(value = "/balance")
    @ResponseBody
    public BalanceResponse query(HttpServletRequest request){
        log.info("余额查询接口请求报文{}", request.getParameter("traderId"));
        BalanceResponse result = new BalanceResponse();
        try{
            if(null==request){
                result.setCode(ResponseConstants.FAIL_CODE1.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE1.getMsg());
                log.info("余额查询接口返回{}{}", ResponseConstants.FAIL_CODE1.getCode(), ResponseConstants.FAIL_CODE1.getMsg());
                return result;
            }
            String traderId = request.getParameter("traderId");
            if(StringUtils.isBlank(traderId)){
                result.setCode(ResponseConstants.FAIL_CODE4.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE4.getMsg());
                log.info("余额查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(Frequently.isLimit(traderId)){
                System.out.println("余额查询频繁");
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
                log.info(traderId+" 余额查询接口返回{}{}", ResponseConstants.FAIL_CODE4.getCode(), ResponseConstants.FAIL_CODE4.getMsg());
                return result;
            }
            if(2==user.getStatus()||user.getDelFlag()==1){
                result.setCode(ResponseConstants.FAIL_CODE5.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE5.getMsg());
                log.info(traderId+" 余额查询接口返回{}{}", ResponseConstants.FAIL_CODE5.getCode(), ResponseConstants.FAIL_CODE5.getMsg());
                return result;
            }
            String ip = com.wangxin.iot.utils.StringUtils.getRemoteAddr(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest());
            String ips = user.getIpWhite();
            if(StringUtils.isBlank(ips)||!ips.contains(ip)){
                result.setCode(ResponseConstants.FAIL_CODE9.getCode());
                result.setMsg(ResponseConstants.FAIL_CODE9.getMsg());
                log.info(traderId+" 余额查询接口返回{}{}", ip, ResponseConstants.FAIL_CODE9.getMsg());
                return result;
            }
            result.setCode(ResponseConstants.SUCCESS_CODE.getCode());
            result.setMsg(ResponseConstants.SUCCESS_CODE.getMsg());
            result.setData(user.getBalance());
            log.info(traderId+" 余额查询接口返回{}", result);
            return result;
        }catch (Exception e){
            result.setCode(ResponseConstants.FAIL_CODE16.getCode());
            result.setMsg(ResponseConstants.FAIL_CODE16.getMsg());
            log.error("余额查询接口异常{}", e);
            return result;
        }
    }
}
