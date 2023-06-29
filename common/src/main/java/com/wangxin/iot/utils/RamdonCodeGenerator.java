package com.wangxin.iot.utils;

import java.util.Random;

/**
 * Xie Chaohong (nowingfly@gmail.com)
 * 2017-06-18
 */
public class RamdonCodeGenerator {
    public static String genCaptcha() {
        Random random = new Random();
        int size = 6;
        String vCode = "";
        char c;
        for (int i = 0; i < size; i++) {
            // 产生一个26以内的随机整数
            int number = random.nextInt(26);
            // if (number % 2 == 0) {
            c = (char) ('0' + (char) ((int) (Math.random() * 10)));
            // 如果生成的是奇数，则随机生成一个字母
            // } else {
            // c = (char) ((char) ((int) (Math.random() * 26)) + 'A');
            // }
            vCode = vCode + c;
        }

        return vCode;
    }
}
