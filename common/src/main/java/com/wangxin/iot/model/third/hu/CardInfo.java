package com.wangxin.iot.model.third.hu;

import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
@Data
public class CardInfo{
    private int id;
    private String iccid;
    private String msisdn;
    private int status;
    private long activeTime;
    private int onOff;
    private String ip;
    private String data;
    private String totalFlow;
    private long refreshTime;
    private long termEndTime;
}
