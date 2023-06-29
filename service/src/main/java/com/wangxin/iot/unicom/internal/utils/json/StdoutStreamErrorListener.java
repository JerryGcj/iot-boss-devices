package com.wangxin.iot.unicom.internal.utils.json;


public class StdoutStreamErrorListener extends BufferErrorListener {

    public void end() {
        System.out.print(buffer.toString());
    }
}
