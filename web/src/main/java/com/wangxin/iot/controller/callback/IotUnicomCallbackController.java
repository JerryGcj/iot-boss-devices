package com.wangxin.iot.controller.callback;

import com.alibaba.fastjson.JSONObject;
import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.delayed.entity.IccidDelayed;
import com.wangxin.iot.delayed.thread.TakeIccidHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * 联通推送Controller
 *
 * @author wx
 * @date 2020/1/2
 */
@Slf4j
@Controller
@RequestMapping("/api/v1/unicom/callback")
public class IotUnicomCallbackController {
    @Autowired
    TakeIccidHandler takeIccidHandler;
    /**
     * IMEI变更推送
     * @param
     */
    @ResponseBody
    @RequestMapping(value = "/simImeiChange", method = RequestMethod.POST)
    public ResponseEntity simImeiChange(@RequestBody String request) {
        log.info("接收到联通IMEI变更推送报文：{}", request);
        try {
            if(StringUtils.isNotBlank(request)){
                JSONObject jsonObject = JSONObject.parseObject(request);
                JSONObject json = jsonObject.getJSONObject("data");
                String iccid = json.getString("iccid");
                IccidDelayed iccidDelayed = new IccidDelayed(iccid, 30L, TimeUnit.SECONDS);
                TakeIccidHandler.blockingQueue.put(iccidDelayed);
            }
        } catch (Exception e) {
            log.error("处理IMEI变更回调异常:{}",e);
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }


}
