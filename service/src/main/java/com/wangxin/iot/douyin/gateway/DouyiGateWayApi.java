package com.wangxin.iot.douyin.gateway;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.doudian.open.api.order_batchDecrypt.OrderBatchDecryptRequest;
import com.doudian.open.api.order_batchDecrypt.OrderBatchDecryptResponse;
import com.doudian.open.api.order_batchDecrypt.param.CipherInfosItem;
import com.doudian.open.api.order_batchDecrypt.param.OrderBatchDecryptParam;
import com.doudian.open.api.order_logisticsAdd.OrderLogisticsAddRequest;
import com.doudian.open.api.order_logisticsAdd.OrderLogisticsAddResponse;
import com.doudian.open.api.order_logisticsAdd.param.OrderLogisticsAddParam;
import com.doudian.open.api.order_orderDetail.OrderOrderDetailRequest;
import com.doudian.open.api.order_orderDetail.OrderOrderDetailResponse;
import com.doudian.open.api.order_orderDetail.param.OrderOrderDetailParam;
import com.doudian.open.api.order_review.OrderReviewRequest;
import com.doudian.open.api.order_review.OrderReviewResponse;
import com.doudian.open.api.order_review.param.OrderReviewParam;
import com.doudian.open.api.order_searchList.OrderSearchListRequest;
import com.doudian.open.api.order_searchList.OrderSearchListResponse;
import com.doudian.open.api.order_searchList.param.CombineStatusItem;
import com.doudian.open.api.order_searchList.param.OrderSearchListParam;
import com.doudian.open.core.AccessToken;
import com.doudian.open.core.AccessTokenBuilder;
import com.doudian.open.core.GlobalConfig;
import com.doudian.open.exception.DoudianOpException;
import com.wangxin.iot.douyin.model.DouyinUpdateExpressModel;
import com.wangxin.iot.utils.redis.RedisUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author anan
 * @date 2022/12/5 13:49
 * 拉取订单任务不能超过2小时，否则可能会有token过期风险。
 */
@Component
@Slf4j
public class DouyiGateWayApi implements InitializingBean {
    public static final String TOKEN_PREFIX = "dy_access_token_";
    //token过期时间小于两小时后刷新
    public static final Long EXPIRE_SECOND = 7200L;
    @Value("${douyin.appKey}")
    String appKey;
    @Value("${douyin.appSecret}")
    String appSecret;
    @Value("#{'${douyin.jsthShopId}'.split(',')}")
    List<Long> jsthShopId;
    @Autowired
    RedisUtil redisUtil;
    @Override
    public void afterPropertiesSet() throws Exception {
        //设置appKey和appSecret，全局设置一次
        GlobalConfig.initAppKey(appKey);
        GlobalConfig.initAppSecret(appSecret);
        List<Long> expireShopIds = new ArrayList<>();
        jsthShopId.forEach(item->{
            String realKey = TOKEN_PREFIX + item;
            Long expire = redisUtil.getExpire(realKey);
            if(expire == null || expire < 0 || DouyiGateWayApi.EXPIRE_SECOND>expire){
                expireShopIds.add(item);
            }
        });
        if(CollectionUtils.isNotEmpty(expireShopIds)){
            this.createToken(expireShopIds);
        }

    }
    private void createToken(List<Long> shopIds){
        shopIds.forEach(item->{
            //入参为shopId
            AccessToken accessToken = AccessTokenBuilder.build(item);
            if(accessToken != null){
                String s = JSONObject.toJSONString(accessToken);
                JSONObject jsonObject = JSONObject.parseObject(s);
                Long expireIn = jsonObject.getLong("expireIn");
                redisUtil.set(TOKEN_PREFIX+item,s,expireIn);
                log.info("抖店 shopId:{},获取token成功",item);
            }else{
                log.info("抖店 shopId:{},获取token失败，AccessToken 为null",item);
            }
        });
    }
    /**
     * 获取订单详情单个
     * @param shopId
     * @param orderId
     * @return
     */
    public OrderOrderDetailResponse orderDetailResponse(Long shopId, String orderId){
        AccessToken accessToken = this.getAccessToken(shopId);
        OrderOrderDetailRequest request = new OrderOrderDetailRequest();
        OrderOrderDetailParam param = request.getParam();
        param.setShopOrderId(orderId);
        return request.execute(accessToken);
    }
    /**
     * 获取订单列表
     * @param page
     * @param size
     * @param start
     * @param end
     * @return
     */
    public OrderSearchListResponse pullOrder(Long shopId,Long page,Long size,Long start,Long end){
        AccessToken accessToken = this.getAccessToken(shopId);
        OrderSearchListRequest orderSearchListRequest = new OrderSearchListRequest();
        OrderSearchListParam orderSearchListParam = new OrderSearchListParam();
        List<CombineStatusItem> combineStatusItems = new ArrayList<>();
        CombineStatusItem combineStatusItem = new CombineStatusItem();
        //订单状态，2：待发货，105：已支付
        combineStatusItem.setOrderStatus("105");
        combineStatusItems.add(combineStatusItem);
        orderSearchListParam.setCombineStatus(combineStatusItems);
        orderSearchListParam.setPage(page);
        orderSearchListParam.setSize(size);
        orderSearchListParam.setCreateTimeStart(start);
        orderSearchListParam.setCreateTimeEnd(end);
        orderSearchListRequest.setParam(orderSearchListParam);
        //调用Open Api
        OrderSearchListResponse response = null;
        try {
            response = orderSearchListRequest.execute(accessToken);
        }catch (DoudianOpException e){
            e.printStackTrace();
            //处理异常
        }
        return response;
    }

    /**
     * 批量解密，每次最多可处理50条
     * @param shopId
     * @param items
     * @return
     */
    public  OrderBatchDecryptResponse batchDecrypt(Long shopId,List<CipherInfosItem> items){
        AccessToken accessToken = this.getAccessToken(shopId);
        OrderBatchDecryptRequest orderBatchDecryptRequest = new OrderBatchDecryptRequest();
        OrderBatchDecryptParam orderBatchDecryptParam = new OrderBatchDecryptParam();
        orderBatchDecryptParam.setCipherInfos(items);
        orderBatchDecryptRequest.setParam(orderBatchDecryptParam);
        //调用Open Api
        OrderBatchDecryptResponse response = null;
        try {
            response = orderBatchDecryptRequest.execute(accessToken);
        }catch (DoudianOpException e){
            e.printStackTrace();
            //处理异常
        }
        return response;
    }
    /**
     * 更新发货信息
     * @param shopId
     */
    public OrderLogisticsAddResponse updateExpress(Long shopId, DouyinUpdateExpressModel douyinUpdateExpressModel){
        AccessToken accessToken = this.getAccessToken(shopId);
        OrderLogisticsAddRequest request = new OrderLogisticsAddRequest();
        OrderLogisticsAddParam param = request.getParam();
        param.setOrderId(douyinUpdateExpressModel.getOrderId());
        param.setCompanyCode(douyinUpdateExpressModel.getCompanyCode());
        param.setLogisticsCode(douyinUpdateExpressModel.getLogisticsCode());
        OrderLogisticsAddResponse response = request.execute(accessToken);
        return response;
    }

    /**
     * 获取快递公司列表
     * @param shopId
     * @return
     */
    public OrderLogisticsAddResponse getLogisticsCompanyList(Long shopId){

        AccessToken accessToken = this.getAccessToken(shopId);
        OrderLogisticsAddRequest request = new OrderLogisticsAddRequest();
        OrderLogisticsAddResponse response = request.execute(accessToken);
        return response;
    }

    /**
     * 0 审核通过
     * 200001 下单身份信息180天内在该卡品运营商处重复下单，未通过审核
     * 200002 下单身份信息已在该卡品运营商处办理了5张电话卡，未通过审核
     * 200003 下单身份信息年龄不在16-60岁（部分卡品16-30岁），未通过审核
     * 200004 下单地址为该卡品运营商禁售地区，未通过审核
     * 200005 因其他原因，未能通过运营商审核，具体可联系商家
     * @param shopId
     * @param code
     * @return
     */
    public OrderReviewResponse reviewRequest(Long shopId,String orderId,String code){
        AccessToken accessToken = this.getAccessToken(shopId);
        OrderReviewRequest request = new OrderReviewRequest();
        OrderReviewParam param = request.getParam();
        param.setTaskType(3001L);
        param.setRejectCode(Long.valueOf(code));
        param.setObjectId(orderId);
        OrderReviewResponse response = request.execute(accessToken);
        return response;
    }
    /**
     * 获取token
     * @param shopId
     * @return
     */
    private AccessToken getAccessToken(Long shopId){
        Long realShopId = shopId==null ? this.jsthShopId.get(0) : shopId;
        String realKey = TOKEN_PREFIX + realShopId;
        Long expire = redisUtil.getExpire(realKey);
        if(DouyiGateWayApi.EXPIRE_SECOND > expire){
            this.createToken(Arrays.asList(shopId));
        }
        JSONObject jsonObject = JSONObject.parseObject(redisUtil.get(realKey).toString());
        return AccessToken.wrap(jsonObject.getString("accessToken"), jsonObject.getString("refreshToken"));
    }
}
