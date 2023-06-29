package com.wangxin.iot.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author: yanwin
 * @Date: 2020/5/26
 */
@Data
public class RefCardMonitorModel {
    private String id;
    private String parentId;
    private String iccid;
    private String accessNumber;
    private Integer simStatus;
    private BigDecimal customPackageUse;
    private BigDecimal originUse;
    private BigDecimal usaged;
    private BigDecimal initUsaged;
    private Date validStart;
    private Date validEnd;
    private Integer active;
    private String freeType;
}
