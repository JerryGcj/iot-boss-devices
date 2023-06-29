package com.wangxin.iot.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by savior on 16-6-10.
 */
public class TimeUtil {

    public final static DateTimeFormatter DATE_TIME_WITH_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static DateTimeFormatter DATE_WITH_DASH = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static DateTimeFormatter DATE_WITH_VIRGULE = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static DateTimeFormatter DATE_TIME_WITH_NONE = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    public static LocalDateTime parseLocalDateTime(String time) {
        try {
            return LocalDateTime.parse(time, DATE_TIME_WITH_DASH);
        } catch (DateTimeParseException ex) {
            return LocalDateTime.parse(time);
        }
    }

    public static String formatDefult(LocalDateTime dateTime){
        if(dateTime == null){
            return "";
        }
        return dateTime.format(DATE_TIME_WITH_DASH);
    }

    public static String format(LocalDateTime dateTime, DateTimeFormatter formatter){
        if(dateTime == null){
            return "";
        }
        return dateTime.format(formatter);
    }

    public static Long betweenDays(LocalDate begin, LocalDate end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(begin, end);

    }

    public static Long betweenDays(LocalDateTime begin, LocalDateTime end) {
        return betweenDays(begin.toLocalDate(), end.toLocalDate());
    }

    public static Long betweenSeconds(LocalDateTime begin, LocalDateTime end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return 0L;
        }
        return Duration.between(begin, end).getSeconds();
    }

    public static Long betweenMillis(LocalDateTime begin, LocalDateTime end) {
        if (begin == null || end == null || begin.isAfter(end)) {
            return 0L;
        }
        return Duration.between(begin, end).toMillis();
    }

    public static LocalDate parseLocalDate(String dateStr) {
        LocalDate result = null;
        try {
            result = LocalDate.parse(dateStr, DATE_TIME_WITH_DASH);
        } catch (DateTimeParseException ex) {}
        if (result != null) {
            return result;
        }

        try {
            result = LocalDate.parse(dateStr, DATE_WITH_VIRGULE);
        } catch (DateTimeParseException ex) {}
        if (result == null) {
            return result;
        }

        return LocalDate.parse(dateStr);
    }

    public static List<LocalDate> generateLocalDateSeq(LocalDate beginDate, LocalDate endDate) {
        if (beginDate == null || endDate == null || !beginDate.isBefore(endDate)) {
            return Collections.EMPTY_LIST;
        }
        List<LocalDate> l = new ArrayList<>();
        for (LocalDate i = beginDate; i.isBefore(endDate); i = i.plusDays(1)) {
            l.add(i);
        }
        return l;
    }

    public static LocalDateTime ofLocalDateTime(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return LocalDateTime.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH), calendar.get(Calendar.HOUR_OF_DAY),
                        calendar.get(Calendar.MINUTE), calendar.get(Calendar.SECOND),
                        calendar.get(Calendar.MILLISECOND) * 1000);
    }

    public static LocalDate ofLocalDate(long milliseconds) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(milliseconds);
        return LocalDate.of(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1,
                        calendar.get(Calendar.DAY_OF_MONTH));
    }

    public static long toMilliseconds(final LocalDate localDate) {
        if (localDate == null)
            return 0;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, localDate.getYear());
        calendar.set(Calendar.MONTH, localDate.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, localDate.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    public static long toMilliseconds(final LocalDateTime localDateTime) {
        if (localDateTime == null)
            return 0;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, localDateTime.getYear());
        calendar.set(Calendar.MONTH, localDateTime.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, localDateTime.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, localDateTime.getHour());
        calendar.set(Calendar.MINUTE, localDateTime.getMinute());
        calendar.set(Calendar.SECOND, localDateTime.getSecond());
        calendar.set(Calendar.MILLISECOND, localDateTime.getNano() / 1000);
        return calendar.getTimeInMillis();
    }

    public static Date ofDate(final LocalDateTime localDateTime) {
        if (localDateTime == null)
            return null;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, localDateTime.getYear());
        calendar.set(Calendar.MONTH, localDateTime.getMonthValue() - 1);
        calendar.set(Calendar.DAY_OF_MONTH, localDateTime.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, localDateTime.getHour());
        calendar.set(Calendar.MINUTE, localDateTime.getMinute());
        calendar.set(Calendar.SECOND, localDateTime.getSecond());
        calendar.set(Calendar.MILLISECOND, localDateTime.getNano() / 1000);
        return calendar.getTime();
    }

    public static LocalDateTime uDateToLocalDateTime(Date date) {
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        return LocalDateTime.ofInstant(instant, zone);
    }

    public static LocalDate uDateToLocalDate(Date date) {
        return uDateToLocalDateTime(date).toLocalDate();
    }

    public static LocalTime uDateToLocalTime(Date date) {
        return uDateToLocalDateTime(date).toLocalTime();
    }

    public static Date localDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return Date.from(instant);
    }
    public static String dateToString(Date d) {
        if (d == null) return "";
        return new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(d);
    }

    public static String secondsToString(Integer workTime) {
        String hour = String.format("%02d", workTime / 3600);
        String minutes = String.format("%02d", (workTime % 3600) / 60);
        return hour + ":" + minutes + "小时";
    }

    /**
     * 当前时间 的 上周 去掉时分秒
     * @auto zhaolei
     * create time on 2017年10月31日 17:24:35
     * @return
     */
    public static Date upWeekRidHMS(){
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.WEEK_OF_YEAR, -1);
        Date date = null;
        try {
            date = format.parse(format.format(calendar.getTime()));
        }catch (ParseException e){
            return date;
        }finally {
            return date;
        }

    }

    public static void addDay(Date target, int amount){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(target);
        calendar.add(Calendar.DAY_OF_YEAR, amount);
        target.setTime(calendar.getTimeInMillis());
    }

    /**
     * 将格式为："YYYY-MM-DD HH:mm:ss"的字符串转换为时间
     * @param dateStr   "YYYY-MM-DD HH:mm:ss"的字符串
     * @see Date
     * @return  Date
     */
    public static Date parse(String dateStr){
        try {
            Date date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 将小时转换成分钟数
     * @param hoursMin  小时分钟
     * @return  -1：转换错误
     */
    public static int toInt(String hoursMin){
        String[] strs = hoursMin.split(":");
        int mins = -1;
        try{
            mins = Integer.valueOf(strs[0]) * 60 + Integer.valueOf(strs[1]);
        }catch (Exception e){
            mins = -1;
        }
        return mins;
    }
}
