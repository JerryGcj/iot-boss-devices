package com.wangxin.iot.web;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.wangxin.iot.WebApplication;
import com.wangxin.iot.card.IIotCardOrderService;
import com.wangxin.iot.card.IIotUnicomCardInfoService;
import com.wangxin.iot.card.IUnicomGatewayService;
import com.wangxin.iot.domain.IotUnicomCardInfo;
import com.wangxin.iot.domain.IotUnicomRefCardCost;
import com.wangxin.iot.event.WxPayCallbackEvent;
import com.wangxin.iot.mapper.IotUnicomCardInfoMapper;
import com.wangxin.iot.mapper.IotUnicomRefCardCostMapper;
import com.wangxin.iot.mapper.OrderMapper;
import com.wangxin.iot.mapper.RealNameSystemMapper;
import com.wangxin.iot.model.IotCardOrder;
import com.wangxin.iot.model.Order;
import com.wangxin.iot.other.CacheComponent;
import com.wangxin.iot.task.UnicomCardCostMonitorTask;
import com.wangxin.iot.task.UnicomCommonTask;
import com.wangxin.iot.task.xxl.UnicomXxlJob;
import com.wangxin.iot.unicom.api.IoTGatewayApi;
import com.wangxin.iot.unicom.response.CommonJsonResponse;
import com.wangxin.iot.utils.DateUtils;
import com.wangxin.iot.utils.HttpClientHelper;
import com.wangxin.iot.utils.redis.RedisUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by 18765 on 2020/1/2 14:14
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = WebApplication.class)
public class UnicomTest {
    @Autowired
    IoTGatewayApi ioTGatewayApi;
    @Autowired
    RealNameSystemMapper realNameSystemMapper;
    @Autowired
    IUnicomGatewayService iUnicomGatewayService;
    @Autowired
    OrderMapper orderMapper;
    @Autowired
    ApplicationContext applicationContext;
    @Autowired
    UnicomCommonTask unicomCommonTask;
    @Autowired
    UnicomCardCostMonitorTask cardCostMonitorTask;
    @Autowired
    IIotUnicomCardInfoService iotUnicomCardInfoService;
    @Autowired
    private IotUnicomRefCardCostMapper iotRefCardCostMapper;
    @Autowired
    IotUnicomCardInfoMapper iotUnicomCardInfoMapper;
    @Autowired
    IIotCardOrderService cardOrderService;
    @Autowired
    UnicomXxlJob unicomXxlJob;
    @Autowired
    RedisUtil redisUtil;
    @Test
    public void publishEvent(){
        /*Order order = orderMapper.selectById("24afe598bc33ce14575104832802ec25");
        applicationContext.publishEvent(new WxPayCallbackEvent(order));*/
        IotCardOrder cardOrder = cardOrderService.getById("5fdf874ad1f7054fcda57d9c82212e62");
        cardOrderService.unicomOrderPackage(cardOrder);
    }
    @Test
    public void retry(){
        unicomXxlJob.realNameStatus(null);
    }
    @Test
    public void transferRequltQuery(){
        unicomXxlJob.syncCardDetails(null);
    }
    @Test
    public void syncUsage(){
        unicomCommonTask.syncUsage();
    }
    @Test
    public void cardAutoStartMonitor(){
        cardCostMonitorTask.cardAutoStopMonitor();
    }
    @Test
    public void removeUpdateUsageContainer(){
        unicomCommonTask.removeUpdateUsageContainer();
    }
    @Test
    public void updateCardStatus(){
        Map businessMap = new HashMap();
        //发生位置是公众号
        //businessMap.put("action","2");
        //businessMap.put("goalState","2");
        String[] iccids = {"89860622300031916466\n"};
        businessMap.put("iccids",iccids);
        //激活卡
        ioTGatewayApi.wsGetTerminalDetails(businessMap);
    }
    @Test
    public void getCardDetails(){
        Map businessMap = new HashMap();
        String[] iccids = {"89860622310016090005"};
        businessMap.put("iccids",iccids);
        ioTGatewayApi.wsGetTerminalDetails(businessMap);
    }
    @Test
    public void getUsage(){
        Map businessMap = new HashMap();
        businessMap.put("iccid","89860622310016090005");
        ioTGatewayApi.wsGetTerminalUsageDataDetails(businessMap);
    }
    @Test
    public void test(){
        Stream<String> stringStream = Stream.of("" +
                "");
        stringStream.forEach(item->{
            Map<String,Object> unicom = new HashMap<>();
            unicom.put("changeType","3");
            unicom.put("targetValue","3");
            unicom.put("iccid",item);
            try {
                CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsEditTerminal(unicom);
                if (commonJsonResponse != null) {
                    Map<String, Object> data = commonJsonResponse.getData();
                    if (CollectionUtils.isNotEmpty(data)) {
                        String status = data.get("resultCode").toString();
                        //接口返回成功了
                        if ("0000".equals(status)) {
                            System.out.println("修改成功" +item);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        });
    }

    /**
     * 我们平台和上游状态不一致时，把我们平台的停机卡设置成停机在联通平台上
     */
    @Test
    public void syncStatus(){
        QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sim_status", "2");
        List<IotUnicomCardInfo> iotUnicomCardInfos = iotUnicomCardInfoMapper.selectList(queryWrapper);
        List<String> iccids = iotUnicomCardInfos.stream().map(IotUnicomCardInfo::getIccid).collect(Collectors.toList());
        Set<String> set = new HashSet<>();
        while (true){
            List<String> collect = iccids.stream().limit(50).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                try {
                    Map map = new HashMap(1);
                    map.put("iccids",collect);
                    CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                    Map data = commonJsonResponse.getData();
                    if(CollectionUtils.isNotEmpty(data) && data.get("resultCode").equals("0000")){
                        List<HashMap> result  = (List<HashMap>)data.get("terminals");
                        if(CollectionUtils.isNotEmpty(result)){
                            for (HashMap<String, String> maps : result) {
                                String simStatus = maps.get("simStatus");
                                if(simStatus.equals("3")){
                                    set.add(maps.get("iccid"));
                                }
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    //避免死循环
                    iccids.removeAll(collect);
                }
            }else{
                break;
            }
        }
        System.out.println("prefect ending");
        set.forEach(System.out::println);
    }

    @Test
    public void syncCardActive(){
        List<IotUnicomRefCardCost> iccids = iotRefCardCostMapper.getIccidByActive();
        if(CollectionUtils.isNotEmpty(iccids)){
            for(IotUnicomRefCardCost refCardCost : iccids){
                QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
                queryWrapper.eq("iccid", refCardCost.getIccid());
                IotUnicomCardInfo cardInformation = iotUnicomCardInfoMapper.selectOne(queryWrapper);
                if(cardInformation.getSimStatus()==3){
                    System.out.println("卡号："+refCardCost.getIccid()+"，套餐还有余量，但是已停机，准备激活");
                    Map businessMap = new HashMap(3);
                    //发生位置是xxl-job定时任务
                    businessMap.put("action","5");
                    businessMap.put("goalState","2");
                    businessMap.put("iccid",refCardCost.getIccid());
                    //操作卡
                    iUnicomGatewayService.updateCardStatus(businessMap);
                    IotUnicomRefCardCost cost = new IotUnicomRefCardCost();
                    cost.setActive(1);
                    cost.setId(refCardCost.getId());
                    iotRefCardCostMapper.updateById(cost);
                }
            }
        }

    }

    @Test
    public void refund() throws Exception {
        String xml = "<xml><return_code>SUCCESS</return_code><appid><![CDATA[wx0a6b204fca8086c1]]></appid><mch_id><![CDATA[1585124041]]></mch_id><nonce_str><![CDATA[e1b3e1c83eab4e405d3da95bb73a16ac]]></nonce_str><req_info><![CDATA[B0cUEJf4LvsBRV+0SYxP3bq4cHvB6ozdenQkZx1bP6zBF8cjWe4ApgbkLGfZc4b5E9DAKh+Cp1cX3VHM98Njo/YGeMZmqnwO8PtLvQmLE+sU5imsQPo/oVtXfXZdo9Xo+dqAFRtmZ4cJ0PMyXuy88GTHvK2Y+Ky4+rzMRnBFlYbp9N7JwfuArmR7SBQ7NZPtdgW/CAtexstAkeM3k0m6N+SKyBDvXhnNzv9amHyeK1MZiryzInymstWVTLa6I4LJQfHuv7U7SOJogXVo5if/FMvYZ9a58wOFJU81xq63/9AspMabntcHWmFkSl/nw9byF1E2uyvZ3GCL9PhoBD/JhoJVzNF0DXZdeBQA5hAgXsMRfxv0nWlo0Qazk/G0JT3Rodx8E13n+MsGdEpR4j5Zz6ZLNPdCzeGvn9x21j2iyqbrWkQAWAWkRooY6KJdVBAJ4JYiAcaPebEXvatOE8OC9EJEqyUzR3HSyXceAl8yUxdnkOX6BzP6PExsj96gm1QkMpnHPpaexuK5uLwWn9PK28m14jcggCZrOsPYIfiIL+uS7mGp6E/S4jv6R/W9QOUAJsqAxb3AK+fkMGWlDVHPdyey0VjUKpb23AeSgCO6DAlX6AQPH27rnv/j8h0pK0+zOiSaH5nskH9vozSCALFzHIY/vVQzAEepxi2xK56kbMIGDpJwWofdEATUVIXKn08GDqWD861ietcX/QV3GwNmWPaIXWZLqpFH0JX28MtDPWw0FvZHOqY8GjwtV0yUpMRGAQgNUZwfJY9k9H39hr/YMmb3MYD/sAxgszfBhf0f83/5j1Ude95iyMkj5TKDC9ESymPygyygR0O3p/SYbmm5S4JvMaDPTjR6eDfO+QxXDflmEr+D5JJdBlJjQTeRZPzIUl0U4g142IIUyiUC0K/0kfZ/1K2l4ru8fHjAumsZRhEMKQ+JSPCihe5kbt9a+tDQ3UJzbbc7e3Ei/XfjAoqI8qQ3BxSZiVGbu84PJMlmMnbT0jhUwdphEyflAJJY/PqsuipDDSNTjITZQVTW/gpO105gaW1MJSg0b/TX7gaDk77gYiBtwt5KAovFKf/dAk9dh3Hdqg0DFa1RKxsrlCgPvw==]]></req_info></xml>";
        Map<String,String> requestHeaders = new HashMap<String, String>();
        requestHeaders.put("Content-Type", "application/xml");
        String res = HttpClientHelper.post("http://localhost:8005/iot-boss-unicom/refund/notify", xml, requestHeaders, "UTF-8");
        System.out.println(res);

    }

    @Test
    public void syncCardDetails(){
        QueryWrapper<IotUnicomCardInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("sim_status", "2").eq("iccid", "89860620180055940754");
        List<IotUnicomCardInfo> iotUnicomCardInfos = iotUnicomCardInfoMapper.selectList(queryWrapper);
        List<String> iccids = iotUnicomCardInfos.stream().map(IotUnicomCardInfo::getIccid).collect(Collectors.toList());
        Set<String> set = new HashSet<>();
        while (true){
            List<String> collect = iccids.stream().limit(50).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(collect)){
                try {
                    Map map = new HashMap(1);
                    map.put("iccids",collect);
                    CommonJsonResponse commonJsonResponse = ioTGatewayApi.wsGetTerminalDetails(map);
                    Map data = commonJsonResponse.getData();
                    System.out.println(data);
                    if(CollectionUtils.isNotEmpty(data) && data.get("resultCode").equals("0000")){
                        List<HashMap> result  = (List<HashMap>)data.get("terminals");
                        if(CollectionUtils.isNotEmpty(result)){
                            IotUnicomCardInfo iotUnicomCardInfo = new IotUnicomCardInfo();
                            for (HashMap<String, String> maps : result) {
                                iotUnicomCardInfo.setDateActivated(DateUtils.formatFullStringToDate(maps.get("dateActivated")));
                                UpdateWrapper<IotUnicomCardInfo> updateWrapper = new UpdateWrapper<>();
                                updateWrapper.eq("iccid",maps.get("iccid"));
                                iotUnicomCardInfoService.update(iotUnicomCardInfo,updateWrapper);
                            }
                        }
                    }
                }catch (Exception e){
                    e.printStackTrace();
                }
                finally {
                    //避免死循环
                    iccids.removeAll(collect);
                }
            }else{
                break;
            }
        }
        System.out.println("prefect ending");
    }

    @Test
    public void editNetworkAccessConfig(){
        Map map = new HashMap(2);
        map.put("iccid","89860620180055940754");
        map.put("nacId","21001931");
        CommonJsonResponse commonJsonResponse = ioTGatewayApi.editNetworkAccessConfig(map);
        Map data = commonJsonResponse.getData();
        System.out.println(data);
    }

    private String descrypt(String reqInfo,String mchId)
            throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
        byte[] reqInfoB = Base64.decodeBase64(reqInfo);
        String key = DigestUtils.md5Hex(CacheComponent.getInstance().getKeyByMchId(mchId)).toLowerCase();

        if (Security.getProvider("BC") == null){
            Security.addProvider(new BouncyCastleProvider());
        }
        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS7Padding", "BC");
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
        return new String(cipher.doFinal(reqInfoB));
    }

    @Test
    public void regulateFlow(){
        Set<String> iccids = new HashSet<>();
        iccids.add("89860621320020223964");
        iUnicomGatewayService.regulateFlow(iccids);
    }
}
