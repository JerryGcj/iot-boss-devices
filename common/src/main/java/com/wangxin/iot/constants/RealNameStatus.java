package com.wangxin.iot.constants;

import java.util.Optional;
import java.util.stream.Stream;


public enum RealNameStatus {
    SUCC("0","登记成功"),

    SUCCESS("00000","登记成功"),

    ERROR("40130","ICCID和物联卡号关系错误"),

    OVERRUN("40131","该卡实名登记请求次数已超过系统每日最大限制（10次）"),

    REGISTEREDOTHER("40132","该卡已被其他人实名登记"),

    REGISTERED("40133","该卡已被你认证，无需重复认证"),

    INCONFORMITY("40134","认证对比不一致"),

    FORNATERROR("40135","手机号格式错误"),

    PHONEMEDIANERROR("40136","手机号码位数错误"),

    IDMEDIANERROR("40137","身份证号位数错误"),

    AUTHENTICATING("40138","该卡正在认证，请稍候再试"),

    INFORMATIONERROR("40139","实名登记用户信息变更失败"),

    UNQUALIFIED("40199","身份证图像不符合系统要求，请重新提交认证材料"),

    DOWNTIME("40187","该卡处于管理停机状态，无法自助办理业务，详询客户经理"),

    SYSTEMERROR("49999","实名登记系统异常，请稍后再试"),

    NOQUERYINFO("40050","未查询到物联卡信息，请稍后再试"),

    NETWORKERROR("49998","下级网元异常，请稍后再试");


    private String code;
    private String desc;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
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
        return "RealNameStatus{" +
                "code=" + code +
                ", desc='" + desc + '\'' +
                '}';
    }

    RealNameStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static boolean isPayStatus(String strValue, RealNameStatus enumValue) {

        return String.valueOf(enumValue.code).equals(strValue);
    }

    public static String getStringCode(RealNameStatus payStatus) {
        return String.valueOf(payStatus.code);
    }
    public static RealNameStatus getPayStatusByCode(String code){
        Optional<RealNameStatus> realNameStatus = Stream.of(RealNameStatus.values()).filter(item -> item.getCode() == code).findFirst();
        return realNameStatus.get();
    }
}
