package com.wangxin.iot.card.impl;

import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.doudian.open.api.order_batchDecrypt.OrderBatchDecryptResponse;
import com.doudian.open.api.order_batchDecrypt.data.DecryptInfosItem;
import com.doudian.open.api.order_batchDecrypt.data.OrderBatchDecryptData;
import com.doudian.open.api.order_batchDecrypt.param.CipherInfosItem;
import com.doudian.open.api.order_orderDetail.OrderOrderDetailResponse;
import com.doudian.open.api.order_orderDetail.data.OrderOrderDetailData;
import com.doudian.open.api.order_searchList.OrderSearchListResponse;
import com.doudian.open.api.order_searchList.data.OrderSearchListData;
import com.doudian.open.api.order_searchList.data.ShopOrderListItem;
import com.wangxin.iot.card.IDouyinGatewayApiService;
import com.wangxin.iot.douyin.IElectronChannelDouyinOrderService;
import com.wangxin.iot.douyin.entity.ElectronChannelDouyinOrder;
import com.wangxin.iot.douyin.gateway.DouyiGateWayApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author anan
 * @date 2022/12/6 09:22
 */
@Slf4j
@Service
public class DouyinGatewayApiServiceImpl implements IDouyinGatewayApiService {
    @Autowired
    DouyiGateWayApi douyiGateWayApi;
    @Autowired
    IElectronChannelDouyinOrderService electronChannelDouyinOrderService;
    @Autowired
    Executor executor;
    @Override
    public void pullOrder(Long page,String shopId,Long startTime,Long endTime) {

        OrderSearchListResponse response = douyiGateWayApi.pullOrder(Long.valueOf(shopId), page, 100L, startTime,endTime);
        if(response.isSuccess() && "10000".equals(response.getCode())){
            //处理订单。
            OrderSearchListData data = response.getData();
            List<ShopOrderListItem> shopOrderList = data.getShopOrderList();
            this.electronChannelDouyinOrderService.handlerPullOrder(shopOrderList);
            if(data.getSize() == 0 || data.getTotal()<100L){
                return;
            }
            this.pullOrder(++page,shopId,startTime,endTime);
        }
    }
    @Override
    public void pullOneOrder(String shopId, String orderId) {
        OrderOrderDetailResponse orderOrderDetailResponse = this.douyiGateWayApi.orderDetailResponse(Long.valueOf(shopId), orderId);
        String code = orderOrderDetailResponse.getCode();
        if(orderOrderDetailResponse.isSuccess()&&"10000".equals(code)){
            OrderOrderDetailData data = orderOrderDetailResponse.getData();
            this.electronChannelDouyinOrderService.handlerPullOneOrder(data.getShopOrderDetail());
        }
    }

    @Override
    public void decrypt(String shopId,List<ElectronChannelDouyinOrder> toDecryptLists) {
        if(CollectionUtils.isNotEmpty(toDecryptLists)){
            int size = toDecryptLists.size();
             //解密接口每次只能处理50条，每条订单有4列数据需要解密，故取 12 ，50/4 = 12
            if(size > 12){
                //循环次数
                int count = size%12==0 ? (size/12) : (size/12+1);
                for (int i = 0; i < count; i++) {
                    List<ElectronChannelDouyinOrder> collect = toDecryptLists.stream().limit(12).collect(Collectors.toList());
                    try {
                        this.handlerMax12Record(shopId,collect);
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        //避免死循环
                        toDecryptLists.removeAll(collect);
                    }
                }
            }else{
               this.handlerMax12Record(shopId,toDecryptLists);
            }

        }
    }

    private void handlerMax12Record(String shopId, List<ElectronChannelDouyinOrder> toDecryptLists){
        List<CipherInfosItem> cipherInfosItems = this.transformList(toDecryptLists);
        OrderBatchDecryptResponse orderBatchDecryptResponse = this.douyiGateWayApi.batchDecrypt(Long.valueOf(shopId), cipherInfosItems);
            OrderBatchDecryptData data = orderBatchDecryptResponse.getData();
            if(data != null){
                List<DecryptInfosItem> decryptInfos = data.getDecryptInfos();
                if(CollectionUtils.isNotEmpty(decryptInfos)){
                    Map<String, List<DecryptInfosItem>> groupList = decryptInfos.stream().collect(Collectors.groupingBy(DecryptInfosItem::getAuthId));
                    groupList.forEach((k,v)->{
                       try{
                           Stream<ElectronChannelDouyinOrder> electronChannelDouyinOrderStream = toDecryptLists.stream().filter(douyinOrders -> douyinOrders.getOrderId().equals(k));
                           electronChannelDouyinOrderStream.findFirst().ifPresent(queryOrder->{
                               ElectronChannelDouyinOrder updateOrder = new ElectronChannelDouyinOrder();
                               updateOrder.setId(queryOrder.getId());
                               //将数组中的item的错误码收集一下
                               Set<Long> errorNos = v.stream().map(item -> item.getErrNo()).collect(Collectors.toSet());
                               //批量解密每一单有4个字段待解密，必须都是解密的，才进行解密
                               long count = v.stream().filter(decryptInfosItem -> decryptInfosItem.getErrNo().intValue() == 0).count();
                               //如果有一个包含了 （300008：请停止非必要的解密，12小时内自动恢复 ） 就不处理该条记录
                               if(!errorNos.contains(300008L) && count==4){
                                   for (DecryptInfosItem order:v) {
                                       if(order.getCipherText().equals(queryOrder.getCusIdno())){
                                           updateOrder.setCusIdno(order.getDecryptText());
                                       }
                                       if(order.getCipherText().equals(queryOrder.getCusName())){
                                           updateOrder.setCusName(order.getDecryptText());
                                       }
                                       if(order.getCipherText().equals(queryOrder.getCusPhone())){
                                           updateOrder.setCusPhone(order.getDecryptText());
                                       }
                                       if(order.getCipherText().equals(queryOrder.getDetailAddr())){
                                           updateOrder.setDetailAddr(order.getDecryptText());
                                       }
                                   }
                                   //已脱敏
                                   updateOrder.setSensitiveIs(1);
                                   this.electronChannelDouyinOrderService.updateById(updateOrder);
                               }
                           });
                       }catch (Exception e){
                           e.printStackTrace();
                       }
                    });
                }
            }
    }
    private List<CipherInfosItem> transformList(List<ElectronChannelDouyinOrder> toDecryptLists){
        List<CipherInfosItem> result = new ArrayList<>();
        toDecryptLists.forEach(item->{
            CipherInfosItem cusIdNo = new CipherInfosItem();
            cusIdNo.setAuthId(item.getOrderId());
            cusIdNo.setCipherText(item.getCusIdno());
            result.add(cusIdNo);
            CipherInfosItem cusName = new CipherInfosItem();
            cusName.setAuthId(item.getOrderId());
            cusName.setCipherText(item.getCusName());
            result.add(cusName);
            CipherInfosItem detailAdd = new CipherInfosItem();
            detailAdd.setAuthId(item.getOrderId());
            detailAdd.setCipherText(item.getDetailAddr());
            result.add(detailAdd);
            CipherInfosItem phone = new CipherInfosItem();
            phone.setAuthId(item.getOrderId());
            phone.setCipherText(item.getCusPhone());
            result.add(phone);
        });
        return result;
    }
}
