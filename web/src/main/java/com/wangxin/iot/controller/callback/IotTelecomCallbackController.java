package com.wangxin.iot.controller.callback;

import com.wangxin.iot.card.ITelecomGatewayService;
import com.wangxin.iot.utils.wechat.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;

/**
 * 电信推送Controller
 *
 * @author wx
 * @date 2020/1/2
 */
@Slf4j
@Controller
@RequestMapping("/api/v1/telecom/callback")
public class IotTelecomCallbackController {
    @Autowired
    private ITelecomGatewayService telecomGatewayService;
    /**
     * 电信业务报竣推送
     * @param
     */
    @ResponseBody
    @RequestMapping(value = "/stern", method = RequestMethod.POST)
    public ResponseEntity stern(HttpServletRequest request) {
        StringBuffer sb = new StringBuffer() ;
        InputStream is;
        try {
            is = request.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String s = "" ;
            while((s=br.readLine())!=null){
                sb.append(s) ;
            }
            String str =sb.toString();
            log.info("电信业务报竣推送：{}", str);
            Map<String, String> notifyMap = WXPayUtil.xmlToMap(str);
            telecomGatewayService.handlerCallback13(notifyMap);
        } catch (Exception e) {
            log.error("处理电信回调异常:{}",e);
            e.printStackTrace();
        }
        return new ResponseEntity(HttpStatus.OK);
    }


}
