package com.wangxin.iot.utils;

import org.joda.time.DateTime;
import org.springframework.util.StringUtils;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

/**
 *
 * @author ziwen Date: 13-8-26 Time: 下午3:38
 *
 * 此类包含将String类型日期和Date类型日期相互转化的各种方法。 此类的方法是给业务代码调用使用。
 * 有方法需要输入格式的参数部分，类中都有规定格式常量。
 */
public class DateUtils {

    /**
     * 日期格式
     */
    public static final String YYYY_MM_DD_HH_MM_SS     = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYYMMDD_HH_MM_SS_SLASH     = "yyyy/MM/dd HH:mm:ss";

    public static final String YYYY_MM_DD_HH_MM        = "yyyy-MM-dd HH:mm";

    public static final String YYYY_MM_DD_HH_MM_SS_SSS = "yyyy-MM-dd HH:mm:ss.SSS";

    public static final String YYYY_MM_DD_T_HH_MM_SS   = "yyyy-MM-dd'T'HH:mm:ss";

    public static final String YYYY_MM_DD              = "yyyy-MM-dd";

    public static final String MM_DD                   = "MM-dd";

    public static final String YYYY_MM                 = "yyyy-MM";

    public static final String YYYYMMDD_POINT          = "yyyy.MM.dd";

    public static final String YYYYMMDD                = "yyyyMMdd";

    public static final String YYYYMMDDHHMMSS          = "yyyyMMddHHmmss";

    public static final String YYYYMMDDHH              = "yyyyMMddHH";

    public static final String YYYYMM                  = "yyyyMM";

    public static final String YYYY_M_D                = "yyyy-M-d";

    public static final String HH_MM_SS                = "HH:mm:ss";

    public static final String YEAR                    = "yyyy";

    public static final String MONTH                   = "MM";

    public static final String DAY                     = "dd";

    public static final String HOUR                    = "HH";

    public static final String MINUTE                  = "mm";

    public static final String SECOND                  = "ss";

    public static final String WEEK_EEEE               = "EEEE";

    public static final String TIME_UNIT_HOURLY        = "hourly";

    public static final String TIME_UNIT_DAILY         = "daily";

    public static final String TIME_UNIT_WEEKLY        = "weekly";

    public static final String TIME_UNIT_MONTHLY       = "monthly";
    public static final SimpleDateFormat hhmmsss = new SimpleDateFormat(
            "HH:mm:ss");
    /**
     * 一天的毫秒数
     */
    public static final long   ONEDAY_MILLISECONDS     = 86400000;

    /**
     * 一小时的毫秒数
     */
    public static final long   ONEHOUR_MILLISECONDS    = 3600000;

    public static final String END_DAY                 = "endday";

    public static final String START_DAY               = "startday";

    /**
     * 方法描述:  获得时间戳编号
     * 日    期： 2016-05-04
     * @param noType 编号前缀
     * @return 编号前缀+"yyyyMMddHHmmssSSS"格式的时间戳
     * 返回类型： String
     */
    public static String getTimetampNo(String noType) {
        StringBuffer currBusNoBuffer = new StringBuffer();
        String currNo = formatDateToString(new Date(), "yyyyMMddHHmmssSSS");
        currBusNoBuffer.append(noType).append(currNo).append(genSixRandomNum());
        String str = currBusNoBuffer.toString();
        return str;
    }

    /**
     * 生成六位随机数
     * @return
     */
    public static int genSixRandomNum() {
        return new Random().nextInt(899999)+100000;
    }

    /**
     * localdate转date
     */
    public static Date LocalDateToUdate(LocalDate localDate) {
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDate.atStartOfDay().atZone(zone).toInstant();
        return  Date.from(instant);
    }
    /**
     * @see 将字符串转化为JAVA时间类型。
     *
     * @return Date date。JAVA时间类型。
     * @param String。字符串。
     */
    public static Date formatStringToDate(String dateStr)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        try
        {
            return sdf.parse(dateStr);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Date formatSmallStringToDate(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        try
        {
            return sdf.parse(dateStr);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * @see 将字符串转化为JAVA时间类型(精确到秒)。
     *
     * @return Date date。JAVA时间类型。
     * @param String。字符串。
     */
    public static Date formatFullStringToDate(String dateStr)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try
        {
            return sdf.parse(dateStr);
        }
        catch (Exception e)
        {
            return null;
        }
    }

    public static Date formatStringToDate(String strDate, String strFormat) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        return format.parse(strDate);
    }

    public static Calendar formatStringToCalendar(String strDate, String strFormat) throws ParseException
    {
        Date date = formatStringToDate(strDate, strFormat);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal;
    }

    /**
     * 指定毫秒数表示的日历
     *
     * @param millis
     *            毫秒数
     * @return 指定毫秒数表示的日历
     */
    public static Calendar getCalendar(long millis) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date(millis));
        return cal;
    }

    /**
     * @see 将时间转为字符串。
     *
     * @return String。传入时间的格式化字符串。
     * @param Date
     *            date。需要格式化的时间。
     */
    public static String formatDateToString(Date date)
    {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static String formatDateToFullString(Date date)
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String formatDateToString(Date date, String strFormat)
    {
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        return format.format(date);
    }

    /**
     * 指定日期按指定格式显示
     *
     * @param cal
     *            指定的日期
     * @param pattern
     *            指定的格式
     * @return 指定日期按指定格式显示
     */
    public static String formatDate(Calendar cal, String pattern) {
        return getSDFormat(pattern).format(cal.getTime());
    }

    // 指定模式的时间格式
    private static SimpleDateFormat getSDFormat(String pattern) {
        return new SimpleDateFormat(pattern);
    }
    /**
     * @see 将时间转为字符串。
     *
     * @return String。传入时间的格式化字符串。
     * @param Timestamp
     *            timestamp。需要格式化的时间。
     */
    public static String formatTimestampToString(Timestamp timestamp)
    {
        long milliseconds = timestamp.getTime();
        Date date = new Date(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        return format.format(date);
    }

    public static String formatTimestampToFullString(Timestamp timestamp)
    {
        long milliseconds = timestamp.getTime();
        Date date = new Date(milliseconds);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    public static String formatTimestampToString(Timestamp timestamp, String strFormat)
    {
        long milliseconds = timestamp.getTime();
        Date date = new Date(milliseconds);
        SimpleDateFormat format = new SimpleDateFormat(strFormat);
        return format.format(date);
    }

    /**
     * 将CST的时间字符串转换成需要的日期格式字符串<br>
     *
     * @param cststr
     *            The source to be dealed with. <br>
     *            (exp:Fri Jan 02 00:00:00 CST 2009)
     * @param fmt
     *            The format string
     * @return string or <code>null</code> if the cststr is unpasrseable or is
     *         null return null,else return the string.
     *
     */
    public static String getDateFmtStrFromCST(String cststr, String fmt)
    {
        if ((null == cststr) || (null == fmt))
        {
            return null;
        }
        String str = null;
        SimpleDateFormat sdfy = new SimpleDateFormat(fmt.trim());
        SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss 'CST' yyyy", Locale.US);
        try
        {
            str = sdfy.format(sdf.parse(cststr.trim()));
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
        return str;
    }

    /**
     * 获取格式化当前时间对应的日期和时间
     *
     * @return 例如：{"1989-08-15", "20:18:32"}
     */
    public static String[] getCurrentDateTime()
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String dateTime = sdf.format(new Date());
        String[] result = dateTime.split(" ");
        if (result.length < 2)
        {
            return null;
        }
        else
        {
            return result;
        }
    }

    /**
     * 得到当前系统时间，并转换成字符串 <功能详细描述>
     *
     * @param formatStr
     *            日期格式
     * @return String [返回类型说明]
     * @see [类、类#方法、类#成员]
     */
    public static String getCurrentDateString(String formatStr)
    {
        if (null == formatStr || formatStr.equals(""))
        {
            formatStr = DateUtils.YYYY_MM_DD_HH_MM_SS;
        }
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat format = new SimpleDateFormat(formatStr);
        return format.format(calendar.getTime());
    }

    /**
     * 得到当前系统时间
     *
     * @return Date [返回类型说明]
     */
    public static Date getCurrentDate()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime();
    }

    /**
     * 得到当前系统昨天时间
     *
     * @return Date [返回类型说明]
     */
    public static Date getYesterdayDate()
    {
        Calendar day = Calendar.getInstance();
        day.setTime(new Date());
        day.add(Calendar.DATE, -1);
        return day.getTime();
    }

    /**
     * 得到当前系统时间
     *
     * @return long [返回类型说明]
     * @see [类、类#方法、类#成员]
     */
    public static long getCurrentTimestamp()
    {
        Calendar calendar = Calendar.getInstance();
        return calendar.getTime().getTime();
    }

    /**
     * @see根据传入的两个时间求时间间隔
     * @param d2
     * @return second
     */
    public static int getDayBetween(Date d1, Date d2)
    {
        // return (int)(d1.getTime()-d2.getTime())/(1000*60*60*24);
        Date[] d = new Date[2];
        d[0] = d1;
        d[1] = d2;
        Calendar[] cal = new Calendar[2];
        for (int i = 0; i < cal.length; i++)
        {
            cal[i] = Calendar.getInstance();
            cal[i].setTime(d[i]);
            cal[i].set(Calendar.HOUR_OF_DAY, 0);
            cal[i].set(Calendar.MINUTE, 0);
            cal[i].set(Calendar.SECOND, 0);
        }
        long m = cal[0].getTime().getTime();
        long n = cal[1].getTime().getTime();
        return (int) ((long) (m - n) / 1000);
    }

    /**
     * @see根据传入的两个时间求时间间隔
     * @param d2
     * @return second
     */
    public static int getSecondsBetween(Date d1, Date d2)
    {
        // return (int)(d1.getTime()-d2.getTime())/(1000*60*60*24);
        Date[] d = new Date[2];
        d[0] = d1;
        d[1] = d2;
        Calendar[] cal = new Calendar[2];
        for (int i = 0; i < cal.length; i++)
        {
            cal[i] = Calendar.getInstance();
            cal[i].setTime(d[i]);
        }
        long m = cal[0].getTime().getTime();
        long n = cal[1].getTime().getTime();
        return (int) ((long) (m - n) / (1000));
    }

    /**
     *
     * @see根据传入的两个时间求时间间隔
     * @param d2
     * @return second
     */
    public static int[] getDayMinuteBetween(Date d1, Date d2)
    {
        Date[] d = new Date[2];
        d[0] = d1;
        d[1] = d2;
        Calendar[] cal = new Calendar[2];
        for (int i = 0; i < cal.length; i++)
        {
            cal[i] = Calendar.getInstance();
            cal[i].setTime(d[i]);
        }
        long m = cal[0].getTime().getTime();
        long n = cal[1].getTime().getTime();
        int between[] = new int[4];
        between[0] = (int) ((long) (m - n) / (1000 * 24 * 60 * 60));
        between[1] = (int) ((long) (m - n) % (1000 * 24 * 60 * 60)) / (1000 * 60 * 60);
        between[2] = (int) ((long) (m - n) % (1000 * 60 * 60)) / (1000 * 60);
        between[3] = (int) ((long) (m - n) % (1000 * 60)) / (1000);
        return between;
    }

    /**
     * @see 根据传入的两个时间求时间间隔
     * @param d1,d2
     * @return 返回时间间隔，如*秒钟，*分钟，*小时，*天
     */
    public static String getTimeBetween(Date d1, Date d2)
    {
        Date[] d = new Date[2];
        d[0] = d1;
        d[1] = d2;
        Calendar[] cal = new Calendar[2];
        for (int i = 0; i < cal.length; i++)
        {
            cal[i] = Calendar.getInstance();
            cal[i].setTime(d[i]);
        }
        long m = cal[0].getTime().getTime();
        long n = cal[1].getTime().getTime();
        // 取间隔天数
        int daytime = (int) ((long) (m - n) / (1000 * 60 * 60 * 24));
        if (Math.abs(daytime) > 0)
        {
            return Math.abs(daytime) + "天";
        }
        // 取间隔小时数
        int hourtime = (int) ((long) (m - n) / (1000 * 60 * 60));
        if (Math.abs(hourtime) > 0)
        {
            return Math.abs(hourtime) + "小时";
        }
        // 取间隔分钟数
        int secondtime = (int) ((long) (m - n) / (1000 * 60));
        if (Math.abs(secondtime) > 0)
        {
            return Math.abs(secondtime) + "分钟";
        }
        // 取间隔秒钟数
        int minuteime = (int) ((long) (m - n) / (1000));
        if (Math.abs(minuteime) >= 0)
        {
            return Math.abs(minuteime) + "秒钟";
        }
        return null;
    }

    /**
     * 获取当前时间延后n分钟的格式化时间 yyyyMMddHHmmss
     *
     * @return string
     */
    public static String addMinute2Now(int min, String fmt)
    {
        if (StringUtils.isEmpty(fmt))
        {
            fmt = YYYY_MM_DD_HH_MM_SS;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, min);
        SimpleDateFormat sdf = new SimpleDateFormat(fmt);
        return sdf.format(calendar.getTime());
    }

    /**
     * 时间加法器,在给定时间上累加n小时 由于Date类型的月份用0-11表示1-12月，固2008年12月27日输入为"2008-11-27".
     *
     * @param beginTime
     *            开始时间
     * @param hour
     *            加数(单位:小时) 如果为负数,代表减法.
     * @return 计算后的时间
     */
    public static Date addHour2Date(Date beginTime, int hour)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(beginTime);
        c.add(Calendar.HOUR_OF_DAY, hour);
        return c.getTime();
    }

    /**
     * 在当天日期上加上天数，天数可以为负
     *
     * @param days
     *            要增加的天数
     * @return 添加后的日期
     */
    public static Date addDays2CurrentDate(int days)
    {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(new Date());
        startDT.add(Calendar.DATE, days);
        return startDT.getTime();
    }

    /**
     * 在日期上加上天数，天数可以为负
     *
     * @param date
     *            日期基数
     * @param days
     *            要增加的天数
     * @return 添加后的日期
     */
    public static Date addDays2Date(Date date, int days)
    {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.DATE, days);
        return startDT.getTime();
    }

    /**
     * 在当天日期上加上月份，月份可以为负
     *
     * @param months
     *            要增加的月份
     * @return 添加后的日期
     */
    public static Date addMonths2CurrentDate(int months)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.MONTH, months);
        return c.getTime();
    }

    /**
     * 在日期上加上月份，月份可以为负
     *
     * @param date
     *            日期基数
     * @param months
     *            要增加的月份
     * @return 添加后的日期
     */
    public static Date addMonths2Date(Date date, int months)
    {
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        c.add(Calendar.MONTH, months);
        return c.getTime();
    }

    // 增加或减少年数 当天
    public static Date addYears2Date(Date date, int num)
    {
        Calendar startDT = Calendar.getInstance();
        startDT.setTime(date);
        startDT.add(Calendar.YEAR, num);
        return startDT.getTime();
    }

    /**
     * @see
     * @param num
     * @return Date
     * @throws Exception
     */
    public static Date addYears2CurrentDate(int num)
    {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.YEAR, num);
        return c.getTime();
    }

    // 天数差
    public static int getQuot()
    {
        Calendar cc = Calendar.getInstance();
        cc.setTime(new Date());
        int currmum = cc.get(Calendar.DAY_OF_MONTH); // 当月的第几天
        int maxmum = cc.getActualMaximum(Calendar.DAY_OF_MONTH); // 当月最大天数
        return (maxmum - currmum);
    }

    // 百分比 (辅助算靓号的价格)
    public static float getPercent()
    {
        Calendar cc = Calendar.getInstance();
        cc.setTime(new Date());
        float currmum = cc.get(Calendar.DAY_OF_MONTH);
        float maxmum = cc.getActualMaximum(Calendar.DAY_OF_MONTH);
        System.out.println(currmum / maxmum);
        return currmum / maxmum;
    }

    //
    public static Date getLastDay()
    {
        int quot = getQuot(); // 天数差
        Date lastDate = addDays2CurrentDate(quot); // 最后一天
        return lastDate;
    }

    /*
     * 返回系统当前时间的前几个月的日期
     */
    public static Date getBeforDate()
    {
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DATE);
        if (month > 5)
        {
            String aString = year + "-" + (month - 5) + "-" + day;
            Date date = formatStringToDate(aString);
            return date;
        }
        String bString = (year - 1) + "-" + (month + 12 - 5) + "-" + day;
        Date dates = formatStringToDate(bString);
        return dates;
    }

    /**
     * @see 获得所在月份的第一天
     * @param date
     *            月份所在的时间
     * @return 月份的第一天
     */
    public static Date getFirstDateOfMonth(Date date)
    {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DAY_OF_MONTH, 1);
        now.set(Calendar.HOUR_OF_DAY, 0);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        return now.getTime();
    }

    public static Date getFirstDateOfMonth2(Date date)
    {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.DATE, 0);
        now.set(Calendar.HOUR, 12);
        now.set(Calendar.MINUTE, 0);
        now.set(Calendar.SECOND, 0);
        return now.getTime();
    }

    /**
     * @see
     *
     * @return 月份的第一天
     */
    public static String getFirstDayOfCurrentMonth()
    {
        Calendar localTime = Calendar.getInstance();
        localTime.setTime(new Date());
        int year = localTime.get(Calendar.YEAR);
        int month = localTime.get(Calendar.MONTH);
        localTime.set(year, month, 1, 0, 0, 0);
        return DateUtils.formatDateToString(localTime.getTime(), DateUtils.YYYY_MM_DD_HH_MM_SS);
    }

    /**
     * @see
     * @param
     *            date
     * @return 月份的最后一天
     */
    public static Date getLastDateOfMonth(Date date)
    {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(Calendar.MONTH, now.get(Calendar.MONTH) + 1);
        now.set(Calendar.DATE, 1);
        now.set(Calendar.DATE, now.get(Calendar.DATE) - 1);
        now.set(Calendar.HOUR, 11);
        now.set(Calendar.MINUTE, 59);
        now.set(Calendar.SECOND, 59);
        return now.getTime();
    }

    /**
     * 返回此日期所在月的第一天
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @return 此日期所在月的第一天
     */
    public static String getFirstDateOfMonth(String date, String format, String returnFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            Date d = sdf.parse(date);
            Calendar localTime = Calendar.getInstance();
            localTime.setTime(d);
            int year = localTime.get(Calendar.YEAR);
            int month = localTime.get(Calendar.MONTH);
            localTime.set(year, month, 1);
            return DateUtils.formatDateToString(localTime.getTime(), returnFormat);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回此日期所在月的最后一天
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @return 此日期所在月的最后一天
     */
    public static String getLastDateOfMonth(String date, String format, String returnFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            Date d = sdf.parse(date);
            Calendar localTime = Calendar.getInstance();
            localTime.setTime(d);
            int year = localTime.get(Calendar.YEAR);
            int month = localTime.get(Calendar.MONTH);
            localTime.set(year, month + 1, 0);
            return DateUtils.formatDateToString(localTime.getTime(), returnFormat);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回此日期所在一周的第一天
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @return 此日期所在一周的第一天
     */
    public static String getFirstDateOfWeek(String date, String format, String returnFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            Date d = sdf.parse(date);
            Calendar localTime = Calendar.getInstance();
            localTime.setTime(d);
            int year = localTime.get(Calendar.YEAR);
            int week = localTime.get(Calendar.WEEK_OF_YEAR);
            localTime.setWeekDate(year, week, 1);
            return DateUtils.formatDateToString(localTime.getTime(), returnFormat);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回此日期所在一周的最后一天
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @return 此日期所在一周的最后一天
     */
    public static String getLastDateOfWeek(String date, String format, String returnFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            Date d = sdf.parse(date);
            Calendar localTime = Calendar.getInstance();
            localTime.setTime(d);
            int year = localTime.get(Calendar.YEAR);
            int week = localTime.get(Calendar.WEEK_OF_YEAR);
            localTime.setWeekDate(year, week, 7);
            return DateUtils.formatDateToString(localTime.getTime(), returnFormat);
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回当前日期所在一年前第一个月的第一天
     *
     * @param returnFormat
     *            返回的时间格式
     * @return 时间字符串
     */
    public static Date getFirstDayOfPreYearFirstMonth()
    {
        Date preYearMonthDate = DateUtils.addMonths2CurrentDate(-11);
        Date fistMonthDate = DateUtils.getFirstDateOfMonth(preYearMonthDate);
        return fistMonthDate;
    }

    /**
     * 返回此日期所在一周的时间范围,
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @return 时间范围字符串
     */
    public static String getWeekDurationString(String date, String format, String returnFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            Date d = sdf.parse(date);
            Calendar localTime = Calendar.getInstance();
            localTime.setTime(d);
            int year = localTime.get(Calendar.YEAR);
            int week = localTime.get(Calendar.WEEK_OF_YEAR);
            localTime.setWeekDate(year, week, 1);
            String startWeekDay = DateUtils.formatDateToString(localTime.getTime(), returnFormat);
            localTime.setWeekDate(year, week, 7);
            String endWeekDay = DateUtils.formatDateToString(localTime.getTime(), returnFormat);
            return startWeekDay + " ~ " + endWeekDay;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回此日期所在月的时间范围,
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @return 时间范围字符串
     */
    public static String getMonthDurationString(String date, String format, String returnFormat)
    {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        try
        {
            Date d = sdf.parse(date);
            Calendar localTime = Calendar.getInstance();
            localTime.setTime(d);
            int year = localTime.get(Calendar.YEAR);
            int month = localTime.get(Calendar.MONTH);
            localTime.set(year, month, 1);
            String startWeekDay = DateUtils.formatDateToString(localTime.getTime(), returnFormat);
            localTime.set(year, month + 1, 0);
            String endWeekDay = DateUtils.formatDateToString(localTime.getTime(), returnFormat);
            return startWeekDay + " ~ " + endWeekDay;
        }
        catch (ParseException e)
        {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 返回此日期所在周／月的最后一天
     *
     * @param date
     *            某一天的时间
     * @param format
     *            输入的时间格式
     * @param returnFormat
     *            返回的时间格式
     * @param timeUnit
     *            TIME_UNIT_DAILY, TIME_UNIT_WEEKLY, TIME_UNIT_MONTHLY
     * @return 此日期所在月的最后一天
     */
    public static String getDurationEndDate(String date, String format, String returnFormat, String timeUnit)
    {
        if (timeUnit.equals(DateUtils.TIME_UNIT_DAILY))
        {
            return date;
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_WEEKLY))
        {
            return DateUtils.getLastDateOfWeek(date, format, returnFormat);
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_MONTHLY))
        {
            return DateUtils.getLastDateOfMonth(date, format, returnFormat);
        }
        return date;
    }

    /**
     * 获取从起始到结束时间内的日期字符串倒序集合
     *
     * @param startTimestamp
     *            开始时间戳
     * @param endTimestamp
     *            结束时间戳
     * @return 倒序时间List
     */
    public static List<String> getDateList(long startTimestamp, long endTimestamp)
    {
        return DateUtils.getDateList(startTimestamp, endTimestamp, false);
    }

    /**
     * 获取从起始到结束时间内的日期字符串集合
     *
     * @param startTimestamp
     *            开始时间戳
     * @param endTimestamp
     *            结束时间戳
     * @param isAsc
     *            true: 正序， false: 倒序
     * @return 倒序时间List
     */
    public static List<String> getDateList(long startTimestamp, long endTimestamp, boolean isAsc)
    {
        List<String> resultList = new ArrayList<String>();
        SimpleDateFormat sdf = new SimpleDateFormat(DateUtils.YYYY_MM_DD);
        long interValue = DateUtils.ONEDAY_MILLISECONDS;
        if (isAsc)
        {
            long tempTime = startTimestamp;
            while (tempTime <= endTimestamp)
            {
                String date = sdf.format(tempTime).substring(0, 10);
                resultList.add(date);
                tempTime += interValue;
            }
        }
        else
        {
            long tempTime = endTimestamp;
            while (tempTime >= startTimestamp)
            {
                String date = sdf.format(tempTime).substring(0, 10);
                resultList.add(date);
                tempTime -= interValue;
            }
        }
        return resultList;
    }

    /**
     * 获取从起始到结束时间内的日期字符串集合, 取周／月的第一天
     *
     * @param startTimestamp
     *            开始时间戳
     * @param endTimestamp
     *            结束时间戳
     * @param isAsc
     *            true: 正序， false: 倒序
     * @param timeUnit
     *            TIME_UNIT_DAILY, TIME_UNIT_WEEKLY, TIME_UNIT_MONTHLY
     * @return 倒序时间List
     */
    public static List<String> getDateList(long startTimestamp, long endTimestamp, boolean isAsc, String timeUnit)
    {
        List<String> resultList = DateUtils.getDateList(startTimestamp, endTimestamp, isAsc);
        if (timeUnit.equals(DateUtils.TIME_UNIT_DAILY))
        {
            return resultList;
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_WEEKLY))
        {
            List<String> weekResultList = new ArrayList<String>();
            for (String date : resultList)
            {
                String weekFirstDate = DateUtils.getFirstDateOfWeek(date, DateUtils.YYYY_MM_DD, DateUtils.YYYY_MM_DD);
                if (!weekResultList.contains(weekFirstDate))
                {
                    weekResultList.add(weekFirstDate);
                }
            }
            return weekResultList;
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_MONTHLY))
        {
            List<String> monthResultList = new ArrayList<String>();
            for (String date : resultList)
            {
                String monthFirstDate = DateUtils.getFirstDateOfMonth(date, DateUtils.YYYY_MM_DD, DateUtils.YYYY_MM_DD);
                if (!monthResultList.contains(monthFirstDate))
                {
                    monthResultList.add(monthFirstDate);
                }
            }
            return monthResultList;
        }
        return resultList;
    }

    /**
     * 获取此日期所在周／月的第一天
     *
     * @param dateString
     *            format DateUtils.YYYY_MM_DD
     * @param timeUnit
     * @return
     */
    public static String getFirstDateOfTimeUnit(String dateString, String timeUnit)
    {
        if (timeUnit.equals(DateUtils.TIME_UNIT_WEEKLY))
        {
            return DateUtils.getFirstDateOfWeek(dateString, DateUtils.YYYY_MM_DD, DateUtils.YYYY_MM_DD);
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_MONTHLY))
        {
            return DateUtils.getFirstDateOfMonth(dateString, DateUtils.YYYY_MM_DD, DateUtils.YYYY_MM_DD);
        }
        return dateString;
    }

    /**
     * 获取前一年每月的第一天
     *
     * @return 正序时间List
     */
    public static List<String> getPreYearFirstMonthDateList()
    {
        Date preYearMonthDate = DateUtils.getFirstDayOfPreYearFirstMonth();
        List<String> resultList = DateUtils.getDateList(preYearMonthDate.getTime(),
                DateUtils.getCurrentTimestamp(),
                true,
                DateUtils.TIME_UNIT_MONTHLY);
        return resultList;
    }

    /**
     * 获取从起始到结束时间内的日期字符串集合, 取周／月的最后一天
     *
     * @param startTimestamp
     *            开始时间戳
     * @param endTimestamp
     *            结束时间戳
     * @param isAsc
     *            true: 正序， false: 倒序
     * @param timeUnit
     *            TIME_UNIT_DAILY, TIME_UNIT_WEEKLY, TIME_UNIT_MONTHLY
     * @return 倒序时间List
     */
    public static List<String> getDateListOfDurationEnd(long startTimestamp,
                                                        long endTimestamp,
                                                        boolean isAsc,
                                                        String timeUnit)
    {
        List<String> resultList = DateUtils.getDateList(startTimestamp, endTimestamp, isAsc);
        if (timeUnit.equals(DateUtils.TIME_UNIT_DAILY))
        {
            return resultList;
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_WEEKLY))
        {
            List<String> weekResultList = new ArrayList<String>();
            for (String date : resultList)
            {
                String weekEndDate = DateUtils.getLastDateOfWeek(date, DateUtils.YYYY_MM_DD, DateUtils.YYYY_MM_DD);
                if (!weekResultList.contains(weekEndDate))
                {
                    weekResultList.add(weekEndDate);
                }
            }
            return weekResultList;
        }
        else if (timeUnit.equals(DateUtils.TIME_UNIT_MONTHLY))
        {
            List<String> monthResultList = new ArrayList<String>();
            for (String date : resultList)
            {
                String monthEndDate = DateUtils.getLastDateOfMonth(date, DateUtils.YYYY_MM_DD, DateUtils.YYYY_MM_DD);
                if (!monthResultList.contains(monthEndDate))
                {
                    monthResultList.add(monthEndDate);
                }
            }
            return monthResultList;
        }
        return resultList;
    }



    /**
     * @see 获得本周的最后一天
     * @param date
     * @return
     */
    @SuppressWarnings("static-access")
    public static Date getLastDateOfWeek(Date date)
    {
        Calendar now = Calendar.getInstance();
        now.setTime(date);
        now.set(now.DAY_OF_WEEK, 7);
        return now.getTime();
    }

    // 获得周统计的统计时间
    public static Date getStartDateByWeek(Long year, Long month, Long week) throws Exception
    {
        Date date = formatStringToDate(year + "-" + month, "yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.DATE,
                cal.get(Calendar.DATE) + (7 + (Calendar.MONDAY) - cal.get(Calendar.DAY_OF_WEEK)) % 7
                        + ((Long) (7 * week)).intValue());
        return cal.getTime();
    }

    // 获得月统计的统计时间
    public static Date getStartDateByMonth(Long year, Long month) throws ParseException
    {
        Date date = formatStringToDate(year + "-" + month, "yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        return getFirstDateOfMonth(cal.getTime());
    }

    // 获得季统计的统计时间
    public static Date getStartDateByQuarter(Long year, Long quarter) throws ParseException
    {
        Date date = formatStringToDate(year + "-" + (quarter * 3), "yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        return getFirstDateOfMonth(cal.getTime());
    }

    // 获得半年统计的统计时间
    public static Date getStartDateBySemi(Long year, Long semi) throws ParseException
    {
        Date date = formatStringToDate(year + "-" + (semi * 6), "yyyy-MM");
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        return getFirstDateOfMonth(cal.getTime());
    }

    // 获得年统计的统计时间
    public static Date getStartDateByYear(Long year) throws ParseException
    {
        Date date = formatStringToDate((year + 1) + "-01-01", "yyyy-MM-dd");
        return getFirstDateOfMonth(date);
    }

    /**
     * @see 获得当前月之后某月有多少天
     * @param date
     *            月份所在的时间
     * @return 当前月之后某月多少天
     */
    @SuppressWarnings("deprecation")
    public static int getDayByMonth(Date date, int months)
    {
        int tempMonth = date.getMonth() + 1 + months;
        int years = tempMonth / 12;
        int month = tempMonth % 12;
        Calendar time = Calendar.getInstance();
        time.clear();
        time.set(Calendar.YEAR, date.getYear() + years);
        time.set(Calendar.MONTH, month - 1);// Calendar对象默认一月为0
        int day = time.getActualMaximum(Calendar.DAY_OF_MONTH);// 本月份的天数
        return day;
    }

    /**
     * @see 获得指定日期所在的月之后某月的最后一天
     * @param date
     *            日期
     * @param Months
     *            月数 1为本月
     * @return 获得指定日期所在的月之后某月的最后一天
     */
    @SuppressWarnings("deprecation")
    public static Date getDateByMonth(Date date, int Months)
    {
        int tempMonth = date.getMonth() + 1 + Months;
        int years = tempMonth / 12;
        int month = tempMonth % 12;
        Calendar time = Calendar.getInstance();
        time.clear();
        time.set(Calendar.YEAR, date.getYear() + 1900 + years);
        time.set(Calendar.MONTH, month - 1);// Calendar对象默认一月为0
        time.set(Calendar.DATE, time.get(Calendar.DATE) - 1);
        time.set(Calendar.HOUR, 11);
        time.set(Calendar.MINUTE, 59);
        time.set(Calendar.SECOND, 59);
        return time.getTime();
    }

    /**
     * @see 获得当年的第一天。
     *
     * @return Date数组。第一位是当年的第一天和第二位是当年的最后一天。
     */
    public static Date[] getFirstAndLastDays(String yyyy)
    {
        Date date = DateUtils.formatStringToDate(yyyy + "-01-01");
        String dateStr = formatDateToString(date);
        String year = dateStr.substring(0, 4);
        // 当年第一天的字符串形式。
        String firstDayStr = dateStr.replaceFirst(year + "-\\d{2}-\\d{2}", year + "-01-01");
        // 当年最后一天的字符串形式。
        String lastDayStr = dateStr.replaceFirst(year + "-\\d{2}-\\d{2}", year + "-12-31");
        Date firstDay = formatStringToDate(firstDayStr);
        Date lastDay = formatStringToDate(lastDayStr);
        return new Date[] { firstDay, lastDay };
    }

    public static boolean isLateTime(Date nowTime, String otime)
    {
        SimpleDateFormat sform = new SimpleDateFormat("yyyyMMddHHmmss");
        String snowTime = sform.format(nowTime);
        Long time1 = Long.valueOf(snowTime);
        Long time2 = Long.valueOf(otime);
        if (time1 < time2)
            return true;
        return false;
    }

    /**
     * @see 获得某月的剩余天数
     * @param date
     * @param Months
     * @return
     */
    public static int getLastDayByMonth(Date date, int Months)
    {
        return getSecondsBetween(getDateByMonth(new Date(), Months), date) / 86400;
    }

    // 根据推荐日期获得结算日期
    public static Date getRecommendFootDate(Date date) throws ParseException
    {
        Date dd = DateUtils.addMonths2Date(date, 1);
        Calendar cal = Calendar.getInstance();
        cal.setTime(dd);
        cal.set(Calendar.DATE, 10);
        if (cal.getTime().before(dd))
        {
            cal.set(Calendar.MONTH, cal.get(Calendar.MONTH) + 1);
        }
        return cal.getTime();
    }

    /**
     * @see 获得剩余时间 int[1] 天 int[2] 小时 int[3] 分钟
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    public static int[] getLastTime(Date endDate, Date startDate) throws Exception
    {
        int[] lastTime = new int[3];
        // 获取当天时间相对截止时间的时间 时间为00:00:00
        int dayLong = DateUtils.getSecondsBetween(endDate, startDate);
        // 获取天数
        Double day = (dayLong) / 86400.0;
        int hours = (dayLong) % 86400 / 60 / 60;
        int minute = (dayLong) % 86400 % 3600 / 60;
        String dayStr = day.toString().substring(0, day.toString().indexOf("."));
        lastTime[0] = Integer.valueOf(dayStr);
        lastTime[1] = hours;
        lastTime[2] = minute;
        return lastTime;
    }

    /**
     * @see 获取指定日期month月之后的所在日期
     * @see 如3月5号 1月之后所在日期4月3号
     * @param dt
     *            指定日期
     * @param month
     *            月份数
     * @return
     */
    public static Date getDateByDateAndMonth(Date dt, int month)
    {
        int day = 0;
        if (null == dt)
            return null;
        for (int i = 0; i < month; i++)
        {
            day += DateUtils.getDayByMonth(dt, i);
        }
        return DateUtils.addDays2Date(dt, day - 1);
    }

    /**
     * @see 获取指定日期month月之前所在日期
     * @see 本月：9月28号，前一个月为8月29号
     * @param dt
     *            指定日期
     * @param month
     *            月份数
     * @return
     */
    public static Date getDateBeforeNMonth(Date dt, int month)
    {
        int day = 0;
        if (null == dt)
            return null;
        int size = Math.abs(month);
        for (int i = 0; i < size; i++)
        {
            day -= DateUtils.getDayByMonth(dt, -i);
        }
        return DateUtils.addDays2Date(dt, day);
    }

    /**
     * @param stype
     *            返回值类型 0为多少天，1为多少个月，2为多少年 date1开始日期date2结束日期
     * @return
     */
    public static int compareDate(String date1, String date2, int stype)
    {
        int n = 0;
        // String[] u = {"天","月","年"};
        String formatStyle = "yyyy-MM-dd";
        DateFormat df = new SimpleDateFormat(formatStyle);
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        try
        {
            c1.setTime(df.parse(date1));
            c2.setTime(df.parse(date2));
        }
        catch (Exception e3)
        {
            System.out.println("wrong occured");
        }
        while (!c1.after(c2))
        {
            n++;
            if (stype == 1)
            {
                c1.add(Calendar.MONTH, 1);// 比较月份，月份+1
            }
            else
            {
                c1.add(Calendar.DATE, 1); // 比较天数，日期+1
            }
        }
        n = n - 1;
        if (stype == 2)
        {
            int yushu = (int) n % 365;
            n = yushu == 0 ? (n / 365) : ((n / 365) - 1);
        }
        // System.out.println(date1+" -- "+date2+" 相差多少"+u[stype]+":"+n);
        return n;
    }

    /**
     * 获取日期是星期几<br>
     *
     * @see 想返回数字:1为周一2为周二，去掉数组weekDays,直接返回w
     * @see 想返回汉字周几见下
     * @param dt
     * @return 当前日期是星期几
     */
    public static int getWeekOfDate(Date dt)
    {
        // String[] weekDays = {"星期日", "星期一", "星期二", "星期三", "星期四", "星期五",
        // "星期六"};
        Calendar cal = Calendar.getInstance();
        cal.setTime(dt);
        int w = cal.get(Calendar.DAY_OF_WEEK) - 1;
        if (w < 0 || w == 0)
        {
            w = 7;
        }
        return w;
        // return weekDays[w];
    }

    /**
     * @see两个日期的差距(天数)
     */
    public static long getDistDates(Date startDate, Date endDate)
    {
        long totalDate = 0;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        long timestart = calendar.getTimeInMillis();
        calendar.setTime(endDate);
        long timeend = calendar.getTimeInMillis();
        totalDate = Math.abs((timeend - timestart)) / (1000 * 60 * 60 * 24);
        return totalDate;
    }

    /**
     * @see两个日期的差距(毫秒)
     */
    public static long getDistDatesInMillis(Date startDate, Date endDate)
    {
        long totalDate = 0;
        long timestart = 0;
        long timeend = 0;
        Calendar calendar = Calendar.getInstance();
        if (null != startDate)
        {
            calendar.setTime(startDate);
            timestart = calendar.getTimeInMillis();
        }
        if (null != endDate)
        {
            calendar.setTime(endDate);
            timeend = calendar.getTimeInMillis();
        }
        totalDate = Math.abs((timeend - timestart));
        return totalDate;
    }

    /**
     * @see dateU 往前推X小时X分钟 或者往后推
     * @param dateU
     *            为当前时间
     * @param minTime
     *            为想减去的时间
     * @return
     * @throws Exception
     */
    public static Date getMinDate(Date dateU, String minTime, Long flag) throws Exception
    {
        Date wantDate = null;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm"); // 转换为02:30
        // 2小时30分钟
        wantDate = sdf.parse(minTime);
        String strDate = sdf.format(wantDate);
        int ss = 0;// 转换后的毫秒数
        if (null != strDate)
        {
            int Hour = Integer.parseInt(strDate.split(":")[0].toString());
            int minute = Integer.parseInt(strDate.split(":")[1].toString());
            ss = Hour * 60 * 60 * 1000 + minute * 60 * 1000;
        }
        Long chaSec = 0L;
        if (flag == 1)
        {// 往前推
            chaSec = dateU.getTime() - ss;
        }
        else if (flag == 2)
        {
            chaSec = dateU.getTime() + ss;
        }
        Date d = new Date(chaSec);
        return d;
    }

    public static Date changeSqlDateToUtilDate(java.sql.Date dt)
    {
        Date dtTemp = new Date(dt.getTime());
        return dtTemp;
    }

    public static java.sql.Date changeUtilDateToSqlDate(Date dt)
    {
        java.sql.Date dtTemp = new java.sql.Date(dt.getTime());
        return dtTemp;
    }

    public static java.sql.Date changeStringToSqlDate(String dt)
    {
        Date date = formatStringToDate(dt);
        java.sql.Date dtTemp = new java.sql.Date(date.getTime());
        return dtTemp;
    }

    public static String changeSqlDateToString(java.sql.Date dt, String strFormat)
    {
        Date date = changeSqlDateToUtilDate(dt);
        return DateUtils.formatDateToString(date, strFormat);
    }

    /**
     * @see
     * @param dt
     * @return
     */
    public static Timestamp changeDateToTimestamp(Date dt)
    {
        String str = formatDateToString(dt, "yyyy-MM-dd HH:mm:ss");
        return Timestamp.valueOf(str);
    }

    /**
     * 计算两个日期之间相差的天数
     *
     * @param smdate
     *            较小的时间
     * @param bdate
     *            较大的时间
     * @return 相差天数
     * @throws ParseException
     */
    public static int daysBetween(Date smdate, Date bdate) throws Exception
    {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        smdate = sdf.parse(sdf.format(smdate));
        bdate = sdf.parse(sdf.format(bdate));
        Calendar cal = Calendar.getInstance();
        cal.setTime(smdate);
        long time1 = cal.getTimeInMillis();
        cal.setTime(bdate);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        return Integer.parseInt(String.valueOf(between_days));
    }

    public static long differentDays(Date date1,Date date2)
    {
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
        String payday =  sdf.format(date1);
        String nowday=sdf.format(date2);

        try {
            Date fDate=sdf.parse(payday);
            Date oDate=sdf.parse(nowday);
            long days=(oDate.getTime()-fDate.getTime())/(1000*3600*24);
       /*     Calendar aCalendar = Calendar.getInstance();
            aCalendar.setTime(fDate);
            int day1 = aCalendar.get(Calendar.MONTH);
            aCalendar.setTime(oDate);
            int day2 = aCalendar.get(Calendar.MONTH);
            int days=day2-day1;
            if(days<0){
                days=-days;
            }*/

            return days;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return  0;
    }

    public static String getHhMmSsTime(Date date) {
        return hhmmsss.format(date);
    }

    public  static Date ChangeDate(Date d ,String format){
        SimpleDateFormat sim=new SimpleDateFormat(format);
        String formdate=sim.format(d);
        try {
            Date newd=sim.parse(formdate);
            return newd;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    public  static Date ChangeStringDate(String d ,String format){
        SimpleDateFormat sim=new SimpleDateFormat(format);
        try {
            Date newd=sim.parse(d);
            return newd;
        }catch (ParseException e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取指定日期所在月份结束的时间
     * @return
     */
    public static String getMonthEnd(String specifiedDay) {
        Date data = null;
        try {
            data = new SimpleDateFormat("yyyy-MM").parse(specifiedDay);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(data);

        //设置为当月最后一天
        c.set(Calendar.DAY_OF_MONTH, c.getActualMaximum(Calendar.DAY_OF_MONTH));
        //将小时至23
        c.set(Calendar.HOUR_OF_DAY, 23);
        //将分钟至59
        c.set(Calendar.MINUTE, 59);
        //将秒至59
        c.set(Calendar.SECOND, 59);
        //将毫秒至999
        c.set(Calendar.MILLISECOND, 999);
        // 本月第一天的时间戳转换为字符串
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
        Date date;
        try {
            date = sdf.parse(sdf.format(new Date(new Long(c.getTimeInMillis()))));
            //Date date = sdf.parse(sdf.format(new Long(s)));// 等价于
            return sdf.format(date);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Date endOf26(int day){
        Date end = null;
        try {
            if(day<27){
                String now = formatDateToString(getCurrentDate(),"yyyy-MM-26 23:59:59");
                end = formatFullStringToDate(now);
            }else{
                Date next = addMonths2Date(new Date(),1);
                String nextNow = formatDateToString(next,"yyyy-MM-26 23:59:59");
                end = formatFullStringToDate(nextNow);
            }
            return end;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 判断某个日期是否是当月最后一天
     * @param date
     * @return
     */
    public static boolean ifMonthEnd(Date date) {
        Calendar c=Calendar.getInstance();
        c.setTime(date);
        Calendar c1 = Calendar.getInstance();
        c1.setTime(date);
        c.add(Calendar.DAY_OF_MONTH,1);
        if(c.get(Calendar.MONTH)!=c1.get(Calendar.MONTH)){
            return Boolean.TRUE;
        } else {
            return Boolean.FALSE;
        }
    }

    public static Date getDateByRFC3339(String dateStr){
        DateTime dateTime = new DateTime(dateStr);
        long timeInMillis = dateTime.toCalendar(Locale.getDefault()).getTimeInMillis();
        Date date = new Date(timeInMillis);
        return date;
    }

    public static int minutesBetween(Date date1,Date date2){
        long time = (date2.getTime()-date1.getTime())/1000;
        long between_days=time/60;
        return Integer.parseInt(String.valueOf(between_days));
    }

    public static void main(String[] args) {
        Date currentDate = DateUtils.getCurrentDate();
        String now = DateUtils.formatDateToString(currentDate,"yyyy-MM-26 23:59:59");
        Date ss = DateUtils.formatFullStringToDate("2022-09-30 15:20:23");
        Date sss = DateUtils.addMonths2Date(ss,1);
        String nextStart = DateUtils.formatDateToString(getFirstDateOfMonth(addMonths2Date(currentDate,1)),DateUtils.YYYY_MM_DD_HH_MM_SS);
        LocalDate localDate = LocalDate.now();
        //是否是月初的两天
        int earlyMonth = localDate.getDayOfMonth();
        //System.out.println(endOf26(28));
        System.out.println(getLastDateOfMonth(ss));
        ifMonthEnd(ss);
    }
}

