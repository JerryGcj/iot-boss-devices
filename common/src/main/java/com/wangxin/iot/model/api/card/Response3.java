package com.wangxin.iot.model.api.card;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class Response3 {

    private String iccid;

    private String msisdn;

    private String imsi;

    private String imei;

    private BigDecimal flowTotal;

    private BigDecimal flowUsed;

    private BigDecimal flowLeft;

    private String activationTime;

    private String overTime;

    private int status;

    private int onLineStatus;

    private int onOffStatus;

    //private List<PackageListResponse> packageList;

    @Override
    public String toString() {
        return "Response3 [iccid=" + iccid + ", msisdn=" + msisdn + ", imsi= "+imsi+", imei= "+imei+", flowTotal=" + flowTotal + ", flowUsed=" + flowUsed + ", flowLeft=" + flowLeft + ", " +
                "activationTime= "+activationTime+", overTime=" + overTime + ", status=" + status + ", onLineStatus=" + onLineStatus + ", onOffStatus=" + onOffStatus + "]";
    }
}
