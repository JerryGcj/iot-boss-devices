package com.wangxin.iot.utils;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.UnsupportedEncodingException;
/**
 * 短信发送MD5工具类
 * @version: V1.0
 * @author 张闻帅
 * @Date: 2018/08/23
 */
public class SmsMd5Util {

    /**
     * 签名字符
     * @param text 签名的字符串
     * @param key 密钥
     * @param inputCharset 编码格式
     * @return 签名结果
     */
    public static String sign(String text, String key, String inputCharset) {
        text = text + key;
        return DigestUtils.md5Hex(getContentBytes(text, inputCharset));
    }

    /**
     * @param content
     * @param charset
     * @throws UnsupportedEncodingException
     */
    private static byte[] getContentBytes(String content, String charset) {
        if (charset == null || "".equals(charset)) {
            return content.getBytes();
        }
        try {
            return content.getBytes(charset);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("MD5签名过程中出现错??指定的编码集不对,您目前指定的编码集是:" + charset);
        }
    }
}
