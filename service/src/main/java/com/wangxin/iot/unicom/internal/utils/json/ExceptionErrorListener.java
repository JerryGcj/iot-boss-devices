package com.wangxin.iot.unicom.internal.utils.json;


public class ExceptionErrorListener extends BufferErrorListener {

    public void error(String type, int col) {
        super.error(type, col);
        throw new IllegalArgumentException(buffer.toString());
    }
}
