package com.wangxin.iot.model;

import lombok.Data;

/**
 * Created by 18765 on 2020/1/3 17:14
 */
@Data
public class CardQueryCron {
    private String id;
    private String cron;
    private String cronDesc;
    private String type;
}
