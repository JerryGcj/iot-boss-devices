package com.wangxin.iot.config;


import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * 短信发送MD5工具类
 * @version: V1.0
 * @author 张闻帅
 * @Date: 2018/08/23
 */
public class SmsMd5Util {

    /**
     * 签名字符
     * @param userId 签名的字符串
     * @param ts 密钥
     * @param apiKey 由平台 方分配
     * @param inputCharset 编码格式
     * @return 签名结果
     */
    public static String sign(String userId, String ts,String apiKey, String inputCharset) {
        userId = userId + ts + apiKey;
        return getMD5(userId.toLowerCase());
    }


    public static String getMD5(String str) {
        try {
            // 生成一个MD5加密计算摘要
            MessageDigest md = MessageDigest.getInstance("MD5");
            // 计算md5函数
            md.update(str.getBytes());
            // digest()最后确定返回md5 hash值，返回值为8为字符串。因为md5 hash值是16位的hex值，实际上就是8位的字符
            // BigInteger函数则将8位的字符串转换成16位hex值，用字符串来表示；得到字符串形式的hash值
            String md5=new BigInteger(1, md.digest()).toString(16);
            //BigInteger会把0省略掉，需补全至32位
            return fillMD5(md5);
        } catch (Exception e) {
            throw new RuntimeException("MD5加密错误:"+e.getMessage(),e);
        }
    }

    public static String fillMD5(String md5){
        return md5.length()==32?md5:fillMD5("0"+md5);
    }
}
