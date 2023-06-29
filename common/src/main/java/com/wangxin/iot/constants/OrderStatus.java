package com.wangxin.iot.constants;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author 伟
 * @date 2018/5/21
 * @time 下午1:54
 * @description
 */
public enum OrderStatus {
    create(1,"创建")
    ,doing(2,"处理中")
    ,doSuccess(3,"支付成功")
    ,doFail(4,"支付失败")
    ,unConfirm(5,"未确认");



    private int code;
    private String desc;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    @Override
    public String toString() {
        return "PayStatus{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }

    OrderStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isPayStatus(String strValue, OrderStatus enumValue) {

        return String.valueOf(enumValue.code).equals(strValue);
    }

    public static String getStringCode(OrderStatus payStatus) {
        return String.valueOf(payStatus.code);
    }
    public static OrderStatus getPayStatusByCode(Integer code){
        Optional<OrderStatus> payStatus = Stream.of(OrderStatus.values()).filter(item -> item.getCode() == code).findFirst();
        return payStatus.get();
    }
}
