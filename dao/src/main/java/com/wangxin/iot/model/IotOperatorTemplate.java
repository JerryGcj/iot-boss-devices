package com.wangxin.iot.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * @author: yanwin
 * @Date: 2020/2/21
 */
@Data
public class IotOperatorTemplate {
    @TableId(type = IdType.ASSIGN_UUID)
    private String id;
    private String operatorId;
    private String handlerClass;
    private String template;
    private String type;
}
