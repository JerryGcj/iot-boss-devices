package com.wangxin.iot.constants;

import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author 伟
 * @date 2018/5/21
 * @time 下午1:54
 * @description
 */
public enum PayStatus {
     unpay(0,"未支付")
    ,payExit(1,"支付退出")
    ,payException(2,"支付异常")
    ,payFail(3,"支付失败")
    ,paySuccess(4,"支付成功")

    ,refundReady(5,"退款失败")
    ,refunding(6,"退款成功")
    ,unReal(7,"未实名");


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

    PayStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isPayStatus(String strValue, PayStatus enumValue) {

        return String.valueOf(enumValue.code).equals(strValue);
    }

    public static String getStringCode(PayStatus payStatus) {
        return String.valueOf(payStatus.code);
    }
    public static PayStatus getPayStatusByCode(Integer code){
        Optional<PayStatus> payStatus = Stream.of(PayStatus.values()).filter(item -> item.getCode() == code).findFirst();
        return payStatus.get();
    }
}
