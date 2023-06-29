/*
 * Copyright (c) 2017. Cardinal Operations and/or its affiliates. All rights reserved.
 * CARDINAL OPERATIONS PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wangxin.iot.rest.response;

import com.wangxin.iot.constants.SuccessConstants;
import com.wangxin.iot.rest.base.BaseResponse;

/**
 * 数据对象返回实体
 * @author Mark (majianyou@wxdata.cn)
 * @date 2018/08/13
 */
public class DataResponse<T> extends BaseResponse {
    private T data;

    public DataResponse(){super();}

    public DataResponse(SuccessConstants constants) {
        super(constants);
    }

    public DataResponse(T data) {
        super(SuccessConstants.SUCCESS);
        this.data = data;
    }

    public DataResponse(T data, SuccessConstants constants) {
        super(constants);
        this.data = data;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
