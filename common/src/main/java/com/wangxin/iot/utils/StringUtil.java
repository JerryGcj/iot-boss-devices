package com.wangxin.iot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.compile;

public class StringUtil {
    private static final String TOKEN = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    public static final String SPLIT = "-";
    private static final String NUMBER_TOKEN = "1234567890";
    // {}
    private static Pattern pyRegex = compile("\\{[0-9]*\\}");
    /** 邮箱正则*/
    private static Pattern emailRegex = compile("^([a-z0-9A-Z]+[-|_|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$");
    /** 电话正则*/
    private static Pattern telephoneRegex = compile("^(((13[0-9])|(15([0-9]))|(17([0-9]))|(18[0-9]))\\d{8})|(0\\d{2}-\\d{8})|(0\\d{3}-\\d{7})$");
    /** 小时分钟正则*/
    private static Pattern hoursMinRegex = compile("^(([0-1][0-9])|(2[0-3])):[0-5][0-9]");

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static final String loginNameKey = "loginName";
    public static final String loginCompanyDescKey = "loginCompanyDesc";

    public static String getNumberToken(int size) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            sb.append(NUMBER_TOKEN.charAt(random.nextInt(NUMBER_TOKEN.length())));
        }
        return sb.toString();
    }


    public static String getToken(int size) {
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            sb.append(TOKEN.charAt(random.nextInt(TOKEN.length())));
        }
        return sb.toString();
    }

    public static String getRandomString(int length) { //length表示生成字符串的长度
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static String getRandomNumber(int length) { //length表示生成字符串的长度
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    public static boolean isEmpty(String account) {
        return account == null || "".equals(account.trim());
    }

    public static boolean isNotEmpty(String userName) {
        return !isEmpty(userName);
    }

    /**
     * 将时间转换格式为："YYYY-MM-DD HH:mm:ss"的字符串
     * @return
     */
    public static String format(Date date){
        synchronized (DATE_FORMAT){
            String dateStr = DATE_FORMAT.format(date);
            return dateStr;
        }
    }

    /**
     * 将时间转换格式为："YYYY-MM-DD HH:mm:ss"的字符串
     * @return
     */
    public static Date formatStringToDate(String date){
        try{
            synchronized (DATE_FORMAT){
                Date dateStr = DATE_FORMAT.parse(date);
                return dateStr;
            }
        }catch (ParseException e){
           e.printStackTrace();
        }
        return null;
    }
    /**
     * 获取20位UUID
     * @return
     */
    public static String getUUID(){
        UUID id=UUID.randomUUID();
        String[] idd=id.toString().split("-");
        return idd[0]+idd[1]+idd[2]+idd[3];
    }

    /**
     * 验证邮箱
     * @param email 邮箱
     * @return
     */
    public static boolean checkEmail(String email){
        boolean flag = false;
        try{
            Matcher matcher = emailRegex.matcher(email);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

    /**
     * 验证手机号码
     * @param telephone 手机号
     * @return
     */
    public static boolean checkTelephoner(String telephone){
        boolean flag = false;
        try{
            Matcher matcher = telephoneRegex.matcher(telephone);
            flag = matcher.matches();
        }catch(Exception e){
            flag = false;
        }
        return flag;
    }

    public static boolean checkHoursMin(String hoursMin){
        if(hoursMin == null){
            return false;
        }
        Matcher matcher = hoursMinRegex.matcher(hoursMin);
        return matcher.matches();
    }

    /**
     * 利用正则表达式判断字符串是否是数字
     * @param str
     * @return
     */
    public static boolean isNumeric(String str){
        Pattern pattern = compile("[0-9]*");
        Matcher isNum = pattern.matcher(str);
        if( !isNum.matches() ){
            return false;
        }
        return true;
    }






    public static String toUtf8String(String s) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c >= 0 && c <= 255) {
                sb.append(c);
            } else {
                byte[] b;
                try {
                    b = Character.toString(c).getBytes("utf-8");
                } catch (Exception ex) {
                    System.out.println(ex);
                    b = new byte[0];
                }
                for (int j = 0; j < b.length; j++) {
                    int k = b[j];
                    if (k < 0) {
                        k += 256;
                    }
                    sb.append("%" + Integer.toHexString(k).toUpperCase());
                }
            }
        }
        return sb.toString();
    }

    public static String getExtension(String fileName){
        int lastDelIndex = fileName.lastIndexOf('.');
        if(lastDelIndex != fileName.length() - 1){
            return fileName.substring(lastDelIndex + 1);
        }
        return null;
    }

    /**
     * 获取带有 "." 的文件后缀
     * @param fileName
     * @return
     */
    public static String getExtensionPreDel(String fileName){
        int lastDelIndex = fileName.lastIndexOf('.');
        if(lastDelIndex != fileName.length() - 1){
            return fileName.substring(lastDelIndex);
        }
        return null;
    }

    /**
     * 占位符替换
     * @param source    源字符串
     * @param targetArr 目标字符数组
     * @return  占位符呗替换后的字符串
     */
    public static String formatRepBraces(String source, String[] targetArr){
        if(source == null || targetArr == null){
            throw new NullPointerException("str or strArr can not be null");
        }
        int len = targetArr.length;
        Matcher matcher = pyRegex.matcher(source);
        int index = 0;
        while (matcher.find()){
            if(index >= len){
                break;
            }
            if(targetArr[index] != null){
                source = matcher.replaceFirst(targetArr[index]);
            }
            index++;
            matcher = pyRegex.matcher(source);
        }
        return source;
    }
}
