package com.wangxin.iot.model.jasper.callback;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 * 联通Jasper推送请求参数，SIM卡状态变更
 *
 * @author wx
 * @date 2020/1/3
 */
@Data
@XmlRootElement(name = "SimStateChange")
public class SimStateChange implements Serializable {
    /**
     * ICCID
     */
    private String iccid;

    /**
     * 变更前状态
     */
    private String previousState;

    /**
     * 变更后状态
     */
    private String currentState;

    /**
     * 变更时间
     */
    private Date dateChanged;
}
