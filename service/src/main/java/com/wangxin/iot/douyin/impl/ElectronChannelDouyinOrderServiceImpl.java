package com.wangxin.iot.douyin.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.doudian.open.api.order_logisticsAdd.OrderLogisticsAddResponse;
import com.doudian.open.api.order_orderDetail.data.ShopOrderDetail;
import com.doudian.open.api.order_review.OrderReviewResponse;
import com.doudian.open.api.order_searchList.data.PostAddr;
import com.doudian.open.api.order_searchList.data.ShopOrderListItem;
import com.doudian.open.api.order_searchList.data.SkuOrderListItem;
import com.doudian.open.api.order_searchList.data.UserIdInfo;
import com.wangxin.iot.douyin.IElectronChannelDouyinOrderService;
import com.wangxin.iot.douyin.IElectronChannelDouyinUserRelationService;
import com.wangxin.iot.douyin.IElectronChannelOrderRegularService;
import com.wangxin.iot.douyin.entity.ElectronChannelDouyinOrder;
import com.wangxin.iot.douyin.entity.ElectronChannelDouyinUserRelation;
import com.wangxin.iot.douyin.entity.ElectronChannelOrderRegular;
import com.wangxin.iot.douyin.gateway.DouyiGateWayApi;
import com.wangxin.iot.douyin.mapper.ElectronChannelDouyinOrderMapper;
import com.wangxin.iot.douyin.model.DouyinUpdateExpressModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Description: electron_channel_douyin_order
 * @Author: jeecg-boot
 * @Date:   2022-12-06
 * @Version: V1.0
 */
@Service
@Slf4j
public class ElectronChannelDouyinOrderServiceImpl extends ServiceImpl<ElectronChannelDouyinOrderMapper, ElectronChannelDouyinOrder> implements IElectronChannelDouyinOrderService {
    @Autowired
    IElectronChannelOrderRegularService electronChannelOrderRegularService;
    @Autowired
    IElectronChannelDouyinUserRelationService douyinUserRelationService;
    @Autowired
    DouyiGateWayApi douyiGateWayApi;


    @Override
    public List<ElectronChannelOrderRegular> getExpressInfo(List<String> shopIds) {
        return this.baseMapper.getExpressInfo(shopIds);
    }

    @Override
    public List<ElectronChannelOrderRegular> getCancelMsg(List<String> shopIds) {
        return this.baseMapper.getCancelMsg(shopIds);
    }

    @Override
    public void transferRegularOrder(List<ElectronChannelDouyinOrder> orders) {
        List<String> sensitiveKey = new ArrayList<>(Arrays.asList("菜鸟驿站","快递代收点","盟","宾馆","酒店","茶吧","网吧","游戏厅","台球厅","美容院"));
        if(CollectionUtils.isNotEmpty(orders)){
            List<ElectronChannelOrderRegular> toSave = orders.stream().map(item -> {
                ElectronChannelOrderRegular regular = new ElectronChannelOrderRegular();
                BeanUtils.copyProperties(item, regular);
                if (StringUtils.isBlank(item.getDetailAddr())) {
                    sensitiveKey.forEach(sensitive -> {
                        item.setDetailAddr(item.getDetailAddr().replace(sensitive, ""));
                    });
                    regular.setDetailAddr(item.getDetailAddr());
                }
                if(!item.getDetailAddr().contains(item.getStreet())){
                    regular.setDetailAddr(item.getStreet() + item.getDetailAddr());
                }
                regular.setOrderStatus("1");
                regular.setOutTradeNo(item.getOrderId());
                regular.setChannelName("dy");
                //通过转化来的订单
                regular.setOrderSource("3");
                regular.setCreateTime(new Date());
                return regular;
            }).collect(Collectors.toList());
            electronChannelOrderRegularService.saveBatch(toSave);
            //同步完成后，将集合改为已同步，下次不在处理
            List<ElectronChannelDouyinOrder> collect = orders.stream().map(item -> {
                ElectronChannelDouyinOrder douyinOrder = new ElectronChannelDouyinOrder();
                //已经同步了
                douyinOrder.setTransferIs(1);
                douyinOrder.setId(item.getId());
                return douyinOrder;
            }).collect(Collectors.toList());
            this.updateBatchById(collect);

        }

    }
    @Override
    public void handlerPullOneOrder(ShopOrderDetail item) {
        try {
            List<ElectronChannelDouyinUserRelation> list = douyinUserRelationService.list(null);
            Map<String,String> relation = new HashMap<>();
            list.forEach(rea->{
                relation.put(rea.getDyId(), rea.getOurId());
            });
            ElectronChannelDouyinOrder electronChannelDouyinOrder = new ElectronChannelDouyinOrder();
            electronChannelDouyinOrder.setAppId(String.valueOf(item.getAppId()));
            List<com.doudian.open.api.order_orderDetail.data.SkuOrderListItem> skuOrderList = item.getSkuOrderList();
            if (CollectionUtils.isNotEmpty(skuOrderList)) {
                //商品信息
                com.doudian.open.api.order_orderDetail.data.SkuOrderListItem skuOrderListItem = item.getSkuOrderList().get(0);
                //当维护了商家编码的时候，才自动拉取
                if(org.apache.commons.lang3.StringUtils.isNotEmpty(skuOrderListItem.getCode())){
                    electronChannelDouyinOrder.setAgentId(skuOrderListItem.getCode());
                    electronChannelDouyinOrder.setProductId(skuOrderListItem.getProductId());
                    electronChannelDouyinOrder.setSkuId(skuOrderListItem.getSkuId());
                    electronChannelDouyinOrder.setAuthorId(skuOrderListItem.getAuthorId());
                    electronChannelDouyinOrder.setAuthorName(skuOrderListItem.getAuthorName());
                    electronChannelDouyinOrder.setThemeType(skuOrderListItem.getThemeType());
                    electronChannelDouyinOrder.setRoomId(skuOrderListItem.getRoomId());
                    electronChannelDouyinOrder.setRoomName(skuOrderListItem.getRoomIdStr());
                    electronChannelDouyinOrder.setContentId(skuOrderListItem.getContentId());
                    electronChannelDouyinOrder.setVideoId(skuOrderListItem.getVideoId());
                    electronChannelDouyinOrder.setOriginId(skuOrderListItem.getOriginId());
                    //最外层
                    electronChannelDouyinOrder.setShopId(item.getShopId());
                    electronChannelDouyinOrder.setShopName(item.getShopName());
                    electronChannelDouyinOrder.setPayAmount(item.getPayAmount());
                    electronChannelDouyinOrder.setDouyinCreateTime(new Date(item.getCreateTime()*1000));
                    electronChannelDouyinOrder.setOrderId(item.getOrderId());
                    electronChannelDouyinOrder.setOrderStatus(item.getOrderStatus());
                    electronChannelDouyinOrder.setBType(item.getBType());
                    //收货人发货信息
                    com.doudian.open.api.order_orderDetail.data.PostAddr postAddr = item.getPostAddr();
                    if(postAddr != null){
                        if(postAddr.getProvince() != null){
                            electronChannelDouyinOrder.setProvince(postAddr.getProvince().getName());
                        }
                        if(postAddr.getCity() != null){
                            electronChannelDouyinOrder.setCity(postAddr.getCity().getName());
                        }
                        if(postAddr.getTown() != null){
                            electronChannelDouyinOrder.setDistrict(postAddr.getTown().getName());
                        }
                        if(postAddr.getStreet() != null){
                            electronChannelDouyinOrder.setStreet(postAddr.getStreet().getName());
                        }
                        electronChannelDouyinOrder.setDetailAddr(postAddr.getEncryptDetail());
                    }
                    com.doudian.open.api.order_orderDetail.data.UserIdInfo userIdInfo = item.getUserIdInfo();
                    if(userIdInfo != null){
                        electronChannelDouyinOrder.setCusIdno(userIdInfo.getEncryptIdCardNo());
                        electronChannelDouyinOrder.setCusName(userIdInfo.getEncryptIdCardName());
                        electronChannelDouyinOrder.setCusPhone(item.getEncryptPostTel());
                        electronChannelDouyinOrder.setSensitiveIs(0);
                    }
                    String cusId = relation.get(String.valueOf(skuOrderListItem.getAuthorId()));
                    //都找不到，默认给抖音这个账户
                    if(com.wangxin.iot.utils.StringUtils.isEmpty(cusId)){
                        cusId = "0a68296fb49ca9876dc96a423c7d702a";
                    }
                    //转换一下cusId
                    electronChannelDouyinOrder.setCusId(cusId);
                    //未转换
                    electronChannelDouyinOrder.setTransferIs(0);
                    boolean save = this.save(electronChannelDouyinOrder);
                    if(save){
                        log.info("抖音拉单成功，id:{},orderId:{}",electronChannelDouyinOrder.getId(),electronChannelDouyinOrder.getOrderId());
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @Override
    public void handlerPullOrder(List<ShopOrderListItem> shopOrderList) {
        if(CollectionUtils.isNotEmpty(shopOrderList)){
            //将我们平台和抖音平台的关系缓存起来
            List<ElectronChannelDouyinUserRelation> list = douyinUserRelationService.list(null);
            Map<String,String> relation = new HashMap<>();
            list.forEach(item->{
                relation.put(item.getDyId(), item.getOurId());
            });
           c:for (ShopOrderListItem item : shopOrderList) {
                try {
                    ElectronChannelDouyinOrder electronChannelDouyinOrder = new ElectronChannelDouyinOrder();
                    electronChannelDouyinOrder.setAppId(String.valueOf(item.getAppId()));
                    List<SkuOrderListItem> skuOrderList = item.getSkuOrderList();
                    if (CollectionUtils.isNotEmpty(skuOrderList)) {
                        //商品信息
                        SkuOrderListItem skuOrderListItem = item.getSkuOrderList().get(0);
                        //当维护了商家编码的时候，才自动拉取
                        if(StringUtils.isNotBlank(skuOrderListItem.getCode())){
                            electronChannelDouyinOrder.setAgentId(skuOrderListItem.getCode());
                            electronChannelDouyinOrder.setProductId(skuOrderListItem.getProductId());
                            electronChannelDouyinOrder.setSkuId(skuOrderListItem.getSkuId());
                            electronChannelDouyinOrder.setAuthorId(skuOrderListItem.getAuthorId());
                            electronChannelDouyinOrder.setAuthorName(skuOrderListItem.getAuthorName());
                            electronChannelDouyinOrder.setThemeType(skuOrderListItem.getThemeType());
                            electronChannelDouyinOrder.setRoomId(skuOrderListItem.getRoomId());
                            electronChannelDouyinOrder.setRoomName(skuOrderListItem.getRoomIdStr());
                            electronChannelDouyinOrder.setContentId(skuOrderListItem.getContentId());
                            electronChannelDouyinOrder.setVideoId(skuOrderListItem.getVideoId());
                            electronChannelDouyinOrder.setOriginId(skuOrderListItem.getOriginId());
                            //最外层
                            electronChannelDouyinOrder.setShopId(item.getShopId());
                            electronChannelDouyinOrder.setShopName(item.getShopName());
                            electronChannelDouyinOrder.setPayAmount(item.getPayAmount());
                            electronChannelDouyinOrder.setDouyinCreateTime(new Date(item.getCreateTime()*1000));
                            electronChannelDouyinOrder.setOrderId(item.getOrderId());
                            electronChannelDouyinOrder.setOrderStatus(item.getOrderStatus());
                            electronChannelDouyinOrder.setBType(item.getBType());
                            electronChannelDouyinOrder.setCreateTime(new Date());
                            //收货人发货信息
                            PostAddr postAddr = item.getPostAddr();
                            if(postAddr != null){
                                if(postAddr.getProvince() != null){
                                    electronChannelDouyinOrder.setProvince(postAddr.getProvince().getName());
                                }
                                if(postAddr.getCity() != null){
                                    electronChannelDouyinOrder.setCity(postAddr.getCity().getName());
                                }
                                if(postAddr.getTown() != null){
                                    electronChannelDouyinOrder.setDistrict(postAddr.getTown().getName());
                                }
                                if(postAddr.getStreet() != null){
                                    electronChannelDouyinOrder.setStreet(postAddr.getStreet().getName());
                                }
                                electronChannelDouyinOrder.setDetailAddr(postAddr.getEncryptDetail());
                            }
                            UserIdInfo userIdInfo = item.getUserIdInfo();
                            if(userIdInfo != null){
                                electronChannelDouyinOrder.setCusIdno(userIdInfo.getEncryptIdCardNo());
                                electronChannelDouyinOrder.setCusName(userIdInfo.getEncryptIdCardName());
                                electronChannelDouyinOrder.setCusPhone(item.getEncryptPostTel());
                                electronChannelDouyinOrder.setSensitiveIs(0);
                            }
                            String cusId = relation.get(String.valueOf(skuOrderListItem.getAuthorId()));
                            //都找不到，默认给抖音这个账户
                            if(StringUtils.isBlank(cusId)){
                                cusId = "0a68296fb49ca9876dc96a423c7d702a";
                            }
                            //转换一下cusId
                            electronChannelDouyinOrder.setCusId(cusId);
                            //未转换
                            electronChannelDouyinOrder.setTransferIs(0);
                            boolean save = this.save(electronChannelDouyinOrder);
                            if(save){
                                log.info("抖音拉单成功，id:{},orderId:{}",electronChannelDouyinOrder.getId(),electronChannelDouyinOrder.getOrderId());
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    continue c;
                }
            }
        }
    }

    @Override
    public void pushCancelMsg(Long shopId,List<ElectronChannelOrderRegular> lists) {
        if(CollectionUtils.isEmpty(lists)){
            return;
        }
        //默认审核通过
        List<ElectronChannelDouyinOrder> updateList = new ArrayList<>();
        for (ElectronChannelOrderRegular regular:lists) {
            String code = "0";
            try{
               //订单作废的
                if("5".equals(regular.getOrderStatus()) || "3".equals(regular.getOrderStatus()) ){
                    String cancelMsg = regular.getCancelMsg();
                    if(cancelMsg.contains("超过数量") || cancelMsg.contains("一证五号") || cancelMsg.contains("一证五户")){
                        code = "200002";
                    }
                    else if(cancelMsg.contains("年龄")){
                        code = "200003";
                    }
                    else if(cancelMsg.contains("高危")){
                        code = "200004";
                    }else{
                        //未知原因
                        code = "200005";
                    }
                }
                OrderReviewResponse orderReviewResponse = this.douyiGateWayApi.reviewRequest(shopId, regular.getOutTradeNo(), code);
                //代表处理成功了，更新为对应code，下次不在处理
                if("10000".equals(orderReviewResponse.getCode())){
                    ElectronChannelDouyinOrder douyinOrder = new ElectronChannelDouyinOrder();
                    douyinOrder.setSyncCode(code);
                    douyinOrder.setId(regular.getId());
                    updateList.add(douyinOrder);
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
    }

    @Override
    public void pushExpressInfo(Long shopId, List<ElectronChannelOrderRegular> lists) {
        if(CollectionUtils.isEmpty(lists)){
            return;
        }
        List<ElectronChannelDouyinOrder> updateList = new ArrayList<>();
        for (ElectronChannelOrderRegular regular:lists) {
            try{
                if(StringUtils.isNotBlank(regular.getExpressCompany())){
                    String express = null;
                    String expressCompany = regular.getExpressCompany();
                    if(expressCompany.contains("京东")){
                        express = "jd";
                    }
                    //号卡之家直营的参数快递信息返回的12代表顺丰
                    if(expressCompany.contains("顺丰") || expressCompany.contains("12") ){
                        express = "shunfeng";
                    }
                    if(expressCompany.contains("EMS")){
                        express = "ems";
                    }
                    if(StringUtils.isNotBlank(express)){
                        DouyinUpdateExpressModel item = new DouyinUpdateExpressModel(regular.getOutTradeNo(), express, regular.getExpressNo());
                        OrderLogisticsAddResponse orderLogisticsAddResponse = this.douyiGateWayApi.updateExpress(shopId, item);
                        ElectronChannelDouyinOrder douyinOrder = new ElectronChannelDouyinOrder();
                        douyinOrder.setId(regular.getId());
                        //代表处理成功了，更新为对应code，下次不在处理
                        if("10000".equals(orderLogisticsAddResponse.getCode())){
                            douyinOrder.setSyncExpress(express);
                            updateList.add(douyinOrder);
                        }else if("50002".equals(orderLogisticsAddResponse.getCode())){
                            douyinOrder.setSyncExpress("2");
                            updateList.add(douyinOrder);
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        if(CollectionUtils.isNotEmpty(updateList)){
            this.updateBatchById(updateList);
        }
    }
}
