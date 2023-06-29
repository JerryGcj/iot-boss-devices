package com.wangxin.iot.rest.base;

import com.wangxin.iot.constants.SuccessConstants;
import lombok.Data;

/**
 * @ClassName : BaseResponse
 * @Description : Response信息 父类
 * @author: Mark (majianyou@wxdata.cn)
 * @version:V1.0
 * @Date: 2018-8-13
 */
@Data
public class BaseResponse {

    private Integer retCode;

    private String retMsg;

    private  String retInfo;

    private  int httpCode;

    public BaseResponse(){}
    public BaseResponse(SuccessConstants constants) {
        this.retCode = constants.getRetCode();
        this.retMsg = constants.getRetMsg();
        this.retInfo=constants.getRetInfo();
        this.httpCode=constants.getHttpCode();
    }
}
