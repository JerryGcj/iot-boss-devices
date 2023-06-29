package com.wangxin.iot.model.jasper.callback;

import lombok.Data;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * 联通Jasper推送请求参数，过去24小时的数据用量
 *
 * @author wx
 * @date 2020/1/3
 */
@Data
@XmlRootElement(name = "Past24HDataUsage")
public class Past24HDataUsage implements Serializable {
    /**
     * ICCID
     */
    private String iccid;

    /**
     * 数据用量
     */
    private Long dataUsage;
}
