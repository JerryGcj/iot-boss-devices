package com.wangxin.iot.rest.base;

/**
 * @author: yanwin
 * @Date: 2020/3/3
 */
/**
 * 只要get不要set,进行更好的封装
 */
public class CodeMsg {

    private int code;
    private String msg;

    //通用的异常
    public static CodeMsg SUCCESS=new CodeMsg(0,"success");

    public static CodeMsg SERVER_ERROR=new CodeMsg(50000,"服务端异常");


    //登陆模块异常....60000
    //商品模块...70000
    //订单...80000


    public CodeMsg fillArgs(Object...args){
        int code=this.code;
        String message=String.format(this.msg,args);
        return new CodeMsg(code,message);
    }

    public CodeMsg(int code, String msg) {
        this.code=code;
        this.msg=msg;
    }


    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    //注意需要重写toString 方法,不然到前端页面是一个对象的地址....
    @Override
    public String toString() {
        return "CodeMsg{" +
                "code=" + code +
                ", msg='" + msg + '\'' +
                '}';
    }

}
