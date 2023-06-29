package com.wangxin.iot.domain;

import lombok.Data;

import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/5/26
 */
@Data
public class RefCardModel {
    private String id;
    private String parentId;
    private String cardIccid;
    private String iccid;
    private String accessNumber;
    private String operatorId;
    private Integer costType;
    /**
     * 生效状态：0 未生效，1 生效中 2 已失效
     */
    private Integer active;
    private Date validStart;
    private Date validEnd;
    private Date pushDailyTime;
}
