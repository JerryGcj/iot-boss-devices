/*
 * Copyright (c) 2017. Cardinal Operations and/or its affiliates. All rights reserved.
 * CARDINAL OPERATIONS PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package com.wangxin.iot.rest.base;

import com.wangxin.iot.constants.ErrorConstants;

/**
 * @ClassName : BaseResult
 * @Description : 中间数据结果封装类
 * @author: Mark (majianyou@wxdata.cn)
 * @version: V1.0
 * @Date: 2018-8-13
 */
public class BaseResult<T> {

    protected T data;

    protected ErrorConstants error;

    public BaseResult() {
    }

    public BaseResult(T data, ErrorConstants error) {
        this.data = data;
        this.error = error;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public ErrorConstants getError() {
        return error;
    }

    public void setError(ErrorConstants error) {
        this.error = error;
    }

    public boolean isSuccess() {
        return this.error == null;
    }

    public boolean isFailed(){
        return !isSuccess();
    }
}
