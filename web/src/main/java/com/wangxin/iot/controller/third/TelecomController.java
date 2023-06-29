package com.wangxin.iot.controller.third;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.ratelimit.anno.RateLimit;
import com.wangxin.iot.rest.base.CodeMsg;
import com.wangxin.iot.rest.base.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("telecom")
public class TelecomController {

    @Autowired
    private ITelecomGatewayService iTelecomGatewayService;

    /**
     * 电信修改卡状态接口
     * @param param
     * @return
     */
    @RequestMapping("modifyTelecomCardStatus")
    public Result modifyTelecomCardStatus(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("电信修改卡状态，{}",param);
        //发生位置是后台接口
        param.put("action","0");
        iTelecomGatewayService.updateCardStatus(param);
        return Result.success(new CodeMsg(0,"修改成功，"+param.get("iccid")));
    }

    /**
     * 电信查询卡主状态（数据更新）
     * @param param
     * @return
     */
    @RateLimit(seconds = 3)
    @RequestMapping("mainStatus")
    public Result mainStatus(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("电信查询卡主状态，{}",param);
        //发生位置是后台接口
        param.put("action","0");
        iTelecomGatewayService.mainStatus(param);
        return Result.success(new CodeMsg(0,"更新成功，"+param.get("iccid")));
    }

    /**
     * 电信查询实名状态
     * @param param
     * @return
     */
    @RequestMapping("realNameStatus")
    public Result realNameStatus(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("电信查询实名状态，{}",param);
        iTelecomGatewayService.realNameStatus(param.get("access_number"));
        return Result.success(new CodeMsg(0,"更新成功，"+param.get("access_number")));
    }

    /**
     * 根据iccid查询接入号
     * @param param
     * @return
     */
    @RequestMapping("acquireAccessNumber")
    public Result acquireAccessNumber(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("电信根据iccid查询接入号，{}",param);
        String accessNumber = iTelecomGatewayService.acquireAccessNumber(param.get("iccid"));
        return Result.success(new CodeMsg(0, accessNumber));
    }

    /**
     * 自主限速接口
     * @param param
     * @return
     */
    @RequestMapping("setSpeedValue")
    public Result setSpeedValue(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("电信自主限速，{}",param);
        //发生位置是后台接口
        param.put("action","0");
        iTelecomGatewayService.setSpeedValue(param);
        return Result.success(new CodeMsg(0,"操作成功，"+param.get("iccid")));
    }

    /**
     * 电信清除实名信息
     * @param param
     * @return
     */
    @RequestMapping("removeRealName")
    public Result removeRealName(@RequestBody Map<String,String> param){
        if(CollectionUtils.isEmpty(param)){
            return Result.fail(CodeMsg.SERVER_ERROR);
        }
        log.info("电信清除实名信息，{}",param);
        boolean flag = iTelecomGatewayService.removeRealName(param.get("access_number"));
        if(flag){
            return Result.success(new CodeMsg(0,"清除成功，"+param.get("access_number")));
        }else{
            return Result.fail(new CodeMsg(500,"清除失败，"+param.get("access_number")));
        }
    }
}
