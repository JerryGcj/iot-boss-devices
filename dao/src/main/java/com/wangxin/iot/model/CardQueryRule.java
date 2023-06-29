package com.wangxin.iot.model;

import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/1/14
 */
@Data
public class CardQueryRule {
    private String account;
    private String licenceKey;
    private String iccidBegin;
    private String iccidEnd;
}
