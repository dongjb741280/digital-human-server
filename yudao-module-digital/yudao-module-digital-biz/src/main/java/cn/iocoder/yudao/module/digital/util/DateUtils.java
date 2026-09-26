package cn.iocoder.yudao.module.digital.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.i18n.LocaleContextHolder;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.util.*;

/**
 * @program: fjsms
 * @Date: 2019/1/14 15:54
 * @Author: wangjj6
 * @Description:
 */
@Slf4j
public class DateUtils {
    public final static SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    public final static SimpleDateFormat ym = new SimpleDateFormat("yyyyMM");
    private final static SimpleDateFormat sdfymdhm = new SimpleDateFormat("yyyyMMddHHmmss");
    public final static SimpleDateFormat sdfymdhms = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private final static SimpleDateFormat sdfymd = new SimpleDateFormat("yyyy-MM-dd");
    private final static SimpleDateFormat sdfymdhhmmss = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
    public static final String YYYYMMDDHHMMSS = "yyyyMMddHHmmss";
    public static final String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    public final static String FORMAT_YYYYMM_LINE = "yyyy-MM";
    public final static String FORMAT_YYMMDD = "yyyy-MM-dd";
    public final static String FORMAT_YYYYMMDD24HHMMSS = "yyyy-MM-dd HH:mm:ss";
    public final static String FORMAT_NOLINE_YYMMDD = "yyyyMMdd";
    public final static String FORMAT_NOLINE_YYYYMMDD24HHMMSS = "yyyyMMddHHmmss";
    public final static String FORMAT_NOLINE_YYYYMMDD24HHMMSSSSS = "yyyyMMddHHmmssSSS";
    public final static String FORMAT_24HHMMSS = "HH:mm:ss";
    public final static String FORMAT_YYMM = "yyMM";
    public final static String FORMAT_YYYYMM = "yyyyMM";
    public final static String FOREVER_DATE_LINE = "2099-12-31 23:59:59";
    public final static String FORMAT_YYYY = "yyyy";
    public final static String FORMAT_MM = "MM";
    public final static String FORMAT_DD = "dd";
    private static DateFormat DATEFORMAT_yyyyMMddHHmm = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    public static String FORMAT_HOUR_DATE_TIME = "1";
    public static final String TIME_PATTERN = "HH:mm:ss";
    public static final String DATE_TIME_MS_PATTERN = "yyyy-MM-dd HH:mm:ss.S";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_YYYYMMDD_PATTERN = "yyyyMMdd";
    public static final String DATE_YYYYMM_PATTERN = "yyyyMM";
    public static final String DATE_YYYY_MM_DD_PATTERN = "yyyy-MM-dd";
    public static final String TIME_HHMM_PATTERN = "HH:mm";
    public static final String TIME_HHMM_PATTERN2 = "HHmm";
    public static final String DATE_TIME_NO_HORI_PATTERN = "yyyyMMdd HH:mm:ss";
    public static final String DATE_TIME_NO_SPACE_PATTERN = "yyyyMMddHHmmss";
    public static final String DATE_TIME_PLAYBILL_PATTERN = "yyyyMMdd HH:mm";
    public static final String DATE_TIME_INDEX_PLAYBILL_PATTERN = "yyyy-MM-dd HH:mm";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_ENGLISH_FORMAT = "EEE MMM dd HH:mm:ss zzz yyyy";
    private static final String BASIC_DATE = "2000-01-01 00:00:00";
    public static final String DATE_YYYYMMDDHH = "yyyyMMddHH";




    /**
     * @return
     * @Title: getCurrentDay
     * @Description: TODO 获取当前时间(20161109000000)
     */
    public static String getCurrentDay() {
        return sdfymdhm.format(new Date());
    }

    /**
     * @return
     * @Title: getCurrentDay
     * @Description: TODO 获取当前时间(20161109)
     */
    public static String getCurrentToDay() {
        return sdf.format(new Date());
    }


    public static String stringTime(Date date, String d) {

        return null;
    }

    //yyyyMMdd
    public static String getDay() {
        return sdfymdhm.format(new Date());
    }

    /**
     * 将日期转换为字符串
     *
     * @param date   DATE日期
     * @param format 转换格式
     * @return 字符串日期
     */
    public static String formatDate(Date date, String format) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(format);
        return simpleDateFormat.format(date);
    }

    /**
     * @return
     * @Title: fTime2
     * @Description: TODO 获取time这个日期以前dayAgo天的日期
     */
    public static String fTime(String time, int dayAgo) {
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (dayAgo > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, -dayAgo);//前15天数据
            date = calendar.getTime();
            calendar.setTime(date);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String mon = "";
        String d = "";
        if (month < 10) {
            mon = "0" + month;
        } else {
            mon = month + "";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = "" + day;
        }
        String ret = year + "" + mon + "" + d;
        return ret;
    }

    /**
     * @return
     * @Title: fTime2
     * @Description: TODO 获取time这个日期以后dayAfter天的日期
     */
    public static String fTime2(String time, int dayAfter) {
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (dayAfter > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, +dayAfter);//后15天数据
            date = calendar.getTime();
            calendar.setTime(date);
        }
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        String mon = "";
        String d = "";
        if (month < 10) {
            mon = "0" + month;
        } else {
            mon = month + "";
        }
        if (day < 10) {
            d = "0" + day;
        } else {
            d = "" + day;
        }
        String ret = year + "" + mon + "" + d;
        return ret;
    }

    /**
     * @return
     * @Title: getYesterdayTime
     * @Description: TODO 获取昨天的日期
     */
    public static String getYesterdayTime() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);//前1天
        Date date = calendar.getTime();
        String time = sdfymdhm.format(date);
        return time;
    }

    /**
     * @return
     * @Title: getSunday
     * @Description: TODO 获取最近一个星期天
     */
    public static String getSunday() {
        SimpleDateFormat f = new SimpleDateFormat("yyyyMMdd");
        Calendar c = Calendar.getInstance();
        c.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        return f.format(c.getTime());
    }

    /**
     * @return
     * @Title: getMonthFirstDay
     * @Description: TODO 获取本月第一天
     */
    public static String getCurrentMonthFirstDay() {
        Calendar cal_1 = Calendar.getInstance();//获取当前日期
        cal_1.add(Calendar.MONTH, 0);
        cal_1.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        String firstDay = sdf.format(cal_1.getTime());
        return firstDay;
    }

    /**
     * @return
     * @Title: getMonthFirstDay
     * @Description: TODO 获取上月第一天
     */
    public static String getPreviousMonthFirstDay() {
        //获取当前月第一天：
        Calendar c = Calendar.getInstance();
        c.add(Calendar.MONTH, -1);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        String first = sdf.format(c.getTime());
        return first;
    }

    /**
     * @return
     * @Title: getMonthFirstDay
     * @Description: TODO 获取上月最后一天
     */
    public static String getPreviousMonthLastDay() {
        //获取当前月最后一天
        Calendar ca = Calendar.getInstance();
        ca.set(Calendar.DAY_OF_MONTH, 0);//
        String lastDay = sdf.format(ca.getTime());
        return lastDay;
    }

    /**
     * @return
     * @Title: getCurrentMonthLastDay
     * @Description: TODO 获取指定时间最后一天
     */
    public static String getCurrentMonthLastDay(String time) {
        Date date = null;
        try {
            date = sdf.parse(time);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //获取当前月最后一天
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.DAY_OF_MONTH,
                ca.getActualMaximum(Calendar.DAY_OF_MONTH)); //
        String lastDay = sdf.format(ca.getTime());
        return lastDay;
    }

    /***
     *
     * @Title: getCurrentWeekDay
     * @Description: TODO 获取本周周一
     */
    public static String getCurrentMonday() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);//将每周第一天设为星期一，默认是星期天
        cal.add(Calendar.DATE, 0);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        String monday = sdf.format(cal.getTime());
        return monday;
    }

    /***
     *
     * @Title: getPreviousSunday
     * @Description: TODO 获取上周周日
     */
    public static String getPreviousSunday() {
        Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.MONDAY);//将每周第一天设为星期一，默认是星期天
        cal.add(Calendar.DATE, -1 * 7);
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
        String sunday = sdf.format(cal.getTime());
        return sunday;
    }

    /**
     * @param str
     * @return
     * @Title: getMiniSencond
     * @Description: TODO 将日期转换为毫秒数
     */
    public static String getMiniSencond(String str) {
        long millionSeconds = 0;
        try {
            millionSeconds = sdfymdhm.parse(str).getTime();//毫秒
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return millionSeconds + "";
    }

    /**
     * @param str
     * @return
     * @Title: getDateSencond
     * @Description: TODO 将日期转换为毫秒数
     */
    public static long getDateSencond(String str) {
        long millionSeconds = 0;
        try {
            millionSeconds = sdfymdhms.parse(str).getTime();//毫秒
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return millionSeconds;
    }

    /**
     * 计算日期相差天数
     *
     * @param str1
     * @param str2
     * @return
     */
    public static int getDistanceOfTwoDate(String str1, String str2) {
        int result = 0;
        try {
            Date date1 = sdf.parse(str1);
            Date date2 = sdf.parse(str2);
            Calendar aCalendar = Calendar.getInstance();
            aCalendar.setTime(date1);
            int day1 = aCalendar.get(Calendar.DAY_OF_YEAR);
            aCalendar.setTime(date2);
            int day2 = aCalendar.get(Calendar.DAY_OF_YEAR);
            result = day1 - day2;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * @param msecond
     * @return
     * @Title: long2Date
     * @Description: TODO long 转日期(年-月-日 时-分-秒)
     */
    public static String longToDate(Long msecond) {
        Date date = new Date(msecond);
        return sdfymdhms.format(date);
    }

    public static String dateFormat(String datetemp, String dateformat, String resultformat) {
        String dateresult = "";
        try {
            System.out.println("-------------datetemp-------------" + datetemp);
            if (datetemp != null && !"".equals(datetemp)) {
                if (datetemp.contains("-") || datetemp.contains("/")) {

                    if (datetemp.contains(":")) {
                        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
                        Date dtemp = sdf.parse(datetemp); //转时间
                        SimpleDateFormat sdf2 = new SimpleDateFormat(resultformat);
                        dateresult = sdf2.format(dtemp); //转字符
                    } else {
                        String dtemp2 = datetemp + " 00:00:00";
                        SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
                        Date dtemp = sdf.parse(dtemp2); //转时间
                        SimpleDateFormat sdf2 = new SimpleDateFormat(resultformat);
                        dateresult = sdf2.format(dtemp); //转字符
                    }
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat(dateformat);
                    Date dtemp = sdf.parse(datetemp);
                    SimpleDateFormat sdf2 = new SimpleDateFormat(resultformat);
                    dateresult = sdf2.format(dtemp);
                }
            } else {
                dateresult = datetemp;
            }
            System.out.println("-------------dateresult-------------" + dateresult);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dateresult;
    }

    //传入指定日期和增加的天数
    public static Date dateAddUtils(Date d, int change) {
        try {
            Calendar c = Calendar.getInstance();
            c.setTime(d);   //设置当前日期
            c.add(Calendar.DATE, change); //日期加1天
            d = c.getTime();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return d;
    }

    public static String getDateToString(Date date, String str) {
        switch (str) {
            case "yyyyMMdd":
                return sdf.format(date);
            case "yyyyMMddHHmmss":
                return sdfymdhm.format(date);
            case "yyyy-MM-dd HH:mm:ss":
                return sdfymdhms.format(date);
            case "yyyy-MM-dd":
                return sdfymd.format(date);
            case "yyyy/MM/dd hh:mm:ss":
                return sdfymdhhmmss.format(date);
            default:
                break;
        }
        return sdfymdhms.format(date);
    }

    /*
     * @Description TODO 计算季度
     * @param null 1
     * @return :
     * @author : zhaowang
     * @date : 2020-03-13 00:11
     */
    public static Map<String, String> calcQuarter(String dateStr) {
        Map<String, String> map = new HashMap<String, String>();
        String beginDate = null,//季度第一天
                endDate = null,//季度最后一天
                quarter = null;//第几季度

        Date date = null;
        try {
            date = sdfymdhms.parse(dateStr);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar now = Calendar.getInstance();
        now.setTime(date);

        int minDay = now.getActualMinimum(Calendar.DAY_OF_MONTH);
        int maxDay = now.getActualMaximum(Calendar.DAY_OF_MONTH);

        System.out.println("------------------------季----------------");
        System.out.println("-----2(28) 5(31) 7(31) 8(31) 11(30) 特殊处理这几个月---");

        System.out.println(sdfymdhms.format(now.getTime()));
        int month = now.get(Calendar.MONTH) + 1;
        int day = now.get(Calendar.DATE);
        if (month == 1 || month == 2 || month == 3) {// 01-01 ~ 03-31
            if (month == 2 && day == 28) {
                maxDay = maxDay + 3;
            } else {
                maxDay = maxDay + 2;
            }
            now.set(now.get(Calendar.YEAR), 0, minDay, 00, 00, 00);
            System.out.println("一季度的第一天：" + sdfymdhms.format(now.getTime()));
            beginDate = sdfymdhms.format(now.getTime());
            now.set(now.get(Calendar.YEAR), 2, maxDay, 23, 59, 59);
            System.out.println("一季度的最后一天：" + sdfymdhms.format(now.getTime()));
            endDate = sdfymdhms.format(now.getTime());

            quarter = "一季度";
        } else if (month == 4 || month == 5 || month == 6) {// 04-01 ~ 06-30
            if (month == 5)
                maxDay = maxDay - 1;
            now.set(now.get(Calendar.YEAR), 3, minDay, 00, 00, 00);
            System.out.println("二季度的第一天：" + sdfymdhms.format(now.getTime()));
            beginDate = sdfymdhms.format(now.getTime());
            now.set(now.get(Calendar.YEAR), 5, maxDay, 23, 59, 59);
            System.out.println("二季度的最后一天：" + sdfymdhms.format(now.getTime()));
            endDate = sdfymdhms.format(now.getTime());

            quarter = "二季度";
        } else if (month == 7 || month == 8 || month == 9) {// 07-01 ~ 09-30
            if (month == 7 || month == 8)
                maxDay = maxDay - 1;
            now.set(now.get(Calendar.YEAR), 6, minDay, 00, 00, 00);
            System.out.println("三季度的第一天：" + sdfymdhms.format(now.getTime()));
            beginDate = sdfymdhms.format(now.getTime());
            now.set(now.get(Calendar.YEAR), 8, maxDay, 23, 59, 59);
            System.out.println("三季度的最后一天：" + sdfymdhms.format(now.getTime()));
            endDate = sdfymdhms.format(now.getTime());

            quarter = "三季度";
        } else if (month == 10 || month == 11 || month == 12) {// 10-01 ~ 12-31
            if (month == 11)
                maxDay = maxDay + 1;
            now.set(now.get(Calendar.YEAR), 9, minDay, 00, 00, 00);
            System.out.println("四季度的第一天：" + sdfymdhms.format(now.getTime()));
            beginDate = sdfymdhms.format(now.getTime());
            now.set(now.get(Calendar.YEAR), 11, maxDay, 23, 59, 59);
            System.out.println("四季度的最后一天：" + sdfymdhms.format(now.getTime()));
            endDate = sdfymdhms.format(now.getTime());

            quarter = "四季度";
        }

        map.put("quarter", quarter);
        map.put("beginDate", beginDate);
        map.put("endDate", endDate);
        return map;
    }

    /**
     * 计算月
     *
     * @param dateStr yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Map<String, String> calcMonth(String dateStr) {
        Map<String, String> map = new HashMap<String, String>();
        String beginDate = null,//月第一天
                endDate = null;//月最后一天
        Date date = null;
        dateStr = dateStr + "-01 00:00:00";
        try {
            date = sdfymdhms.parse(dateStr);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        Calendar c = Calendar.getInstance();
        c.setTime(date);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

        c.add(Calendar.MONTH, 0);
        c.set(Calendar.DAY_OF_MONTH, 1);//设置为1号,当前日期既为本月第一天
        String first = format.format(c.getTime());
        first = first + " 00:00:00";
        System.out.println("===============本月first day:" + first);

        //获取当前月最后一天
        Calendar ca = Calendar.getInstance();
        ca.setTime(date);
        ca.set(Calendar.DAY_OF_MONTH, ca.getActualMaximum(Calendar.DAY_OF_MONTH));
        String last = format.format(ca.getTime());
        last = last + " 23:59:59";
        System.out.println("===============本月last day:" + last);

        map.put("beginDate", first);
        map.put("endDate", last);
        return map;
    }

    /**
     * parse date time yyyyMMdd HH:MM:SS
     *
     * @param txt
     * @return
     */
    public static Date parseYMDHMS(String txt) {
        DateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        if (txt != null && txt.length() > 0) {
            try {
                date = dateTimeFormat.parse(txt);
            } catch (ParseException e) {

            }
        }
        return date;
    }

    /**
     * parse yyyy-MM-dd HH:mm
     *
     * @param txt
     * @return
     */
    public static Date parseYMDHm(String txt) {
        Date date = new Date();
        if (txt != null && txt.length() > 0) {
            try {
                date = DATEFORMAT_yyyyMMddHHmm.parse(txt);
            } catch (ParseException e) {

            }
        }
        return date;
    }

    /**
     * parse date to yyyyMMdd
     *
     * @param txt
     * @return
     */
    public static Date parse_yyyyMMdd(String txt) {
        DateFormat dateFormat_ymd = new SimpleDateFormat("yyyyMMdd");
        Date date = new Date();
        if (txt != null && txt.length() > 0) {
            try {
                date = dateFormat_ymd.parse(txt);
            } catch (ParseException e) {

            }
        }
        return date;
    }

    public static Date parse(String txt) {
        DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        Date date = new Date();
        if (txt != null && txt.length() > 0) {
            try {
                date = dateFormat.parse(txt);
            } catch (ParseException e) {

            }
        }
        return date;
    }

    /**
     * parse date
     *
     * @param txt
     * @return
     */
    public static Date parseYMD(String txt) {
        DateFormat dateFormat2 = new SimpleDateFormat(FORMAT_YYMMDD);
        Date date = new Date();
        if (txt != null && txt.length() > 0) {
            try {
                date = dateFormat2.parse(txt);
            } catch (ParseException e) {

            }
        }
        return date;
    }

    /**
     * 将日期按"yyyy-MM-dd HH:mm:ss"格式输出<br>
     * 如果日期的时间部分全为0，则不显示
     */
    public static String fullTime(Date date) {
        if (date == null) {
            return "";
        }
        String format = DATETIME_FORMAT;
        String s = "";
        SimpleDateFormat formator = new SimpleDateFormat(format);
        try {
            s = formator.format(date);
        } catch (Exception ex) {
            s = "";
        }
        if (s != null && s.length() > 11) {
            String sTime = s.substring(11);
            if (sTime.equals("00:00:00")) {
                return s.substring(0, 10);
            }
        }
        return s;
    }

    /**
     * 月份按"yyyy-MM"格式输出<br>
     * currentMonth: 202009
     */
    public static String getLastMonth(String currentMonth) throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMM");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(sdf.parse(currentMonth));
        //取得上一个月时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        String lastMonth = sdf.format(calendar.getTime());
        return lastMonth;
    }

    public static Date getFormatDate(String dateString, String format) throws ParseException {

        if (StringUtils.isBlank(dateString)) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.parse(dateString);
    }

    public static String getFormatDate(Date date, String format) {

        if (null == date) {
            return null;
        }
        SimpleDateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date);
    }

    public static Date addDate(Date date, long day) {
        long time = date.getTime(); // 得到指定日期的毫秒数
        day = day * 24 * 60 * 60 * 1000; // 要加上的天数转换成毫秒数
        time += day; // 相加得到新的毫秒数
        return new Date(time); // 将毫秒数转换成日期
    }

    public static boolean isEffectiveDate(Date nowTime, Date startTime, Date endTime) {
        if (nowTime.getTime() == startTime.getTime()
                || nowTime.getTime() == endTime.getTime()) {
            return true;
        }

        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);

        Calendar begin = Calendar.getInstance();
        begin.setTime(startTime);

        Calendar end = Calendar.getInstance();
        end.setTime(endTime);

        if (date.after(begin) && date.before(end)) {
            return true;
        } else {
            return false;
        }
    }

    public static int differentDaysByMillisecond(Date date1, Date date2) {
        int days = (int) ((date2.getTime() - date1.getTime()) / (1000 * 3600 * 24));
        return days;
    }


    /**
     * java 获取 获取某年某月 所有日期（yyyy-mm-dd格式字符串）
     *
     * @param date（yyyy-mm）
     * @return
     */
    public static List<String> getMonthFullDay(String date) {
        List<String> fullDayList = new ArrayList<>(32);
        try {
            Date dateYYYYMM = new SimpleDateFormat("yyyy-MM").parse(date);
            SimpleDateFormat dateFormatYYYYMMDD = new SimpleDateFormat("yyyy-MM-dd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(dateYYYYMM);
            // 当月1号
            cal.set(Calendar.DAY_OF_MONTH, 1);
            //当前时间
            Calendar now = Calendar.getInstance();
            SimpleDateFormat dateFormatYYYYMM = new SimpleDateFormat("yyyy-MM");
            String month = dateFormatYYYYMM.format(new Date());
            int count = 0;
            if (month.equals(date)) {
                count = now.get(now.DAY_OF_MONTH);
            } else {
                count = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
            }
            for (int j = 1; j <= count; j++) {
                fullDayList.add(dateFormatYYYYMMDD.format(cal.getTime()));
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return fullDayList;
    }

    /**
     * java 获取 获取某年月（yyyy-mm格式字符串）
     *
     * @param date（yyyy）
     * @return
     */
    public static List<String> getMonthFullYear(String date) {
        List<String> fullDayList = new ArrayList<>(32);
        try {
            Calendar cal = Calendar.getInstance();
            int nowYear = cal.get(Calendar.YEAR);
            int paraYear = Integer.valueOf(date);
            int maxMonth = 12;
            if (nowYear == paraYear) {
                maxMonth = cal.get(Calendar.MONTH) + 1;
            }
            for (int j = 1; j <= maxMonth; j++) {
                String month = "";
                if (j < 10) {
                    month = "0" + j;
                } else {
                    month = j + "";
                }
                fullDayList.add(date + "-" + month);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return fullDayList;
    }

    /**
     * 获取两个日期之间的所有月(字符串格式, 按月计算)
     *
     * @param minDate
     * @param maxDate
     * @return
     */
    public static List<String> getMonthBetween(String minDate, String maxDate) {
        List<String> result = new ArrayList<String>();
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");//格式化为年月

            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

            max.setTime(sdf.parse(maxDate));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.MONTH, 1);
            }
        } catch (Exception e) {
            e.getMessage();
        }
        return result;
    }

    /**
     * @param startTime  其实日期
     * @param endTime    终止日期
     * @param formatType 日期格式
     * @return
     */
    public static List<String> getDays(String startTime, String endTime, String formatType) {

        // 返回的日期集合
        List<String> days = new ArrayList<String>();

        DateFormat dateFormat = new SimpleDateFormat(formatType);
        try {
            Date start = dateFormat.parse(startTime);
            Date end = dateFormat.parse(endTime);

            Calendar tempStart = Calendar.getInstance();
            tempStart.setTime(start);

            Calendar tempEnd = Calendar.getInstance();
            tempEnd.setTime(end);
            tempEnd.add(Calendar.DATE, +1);// 日期加1(包含结束)
            while (tempStart.before(tempEnd)) {
                days.add(dateFormat.format(tempStart.getTime()));
                tempStart.add(Calendar.DAY_OF_YEAR, 1);
            }

        } catch (ParseException e) {
            e.printStackTrace();
        }

        return days;
    }

    public static Date getFormatDatetime(String dateString) {
        try {
            return sdfymdhms.parse(dateString);
        } catch (ParseException e) {
            return null;
        }
    }

    /**
     * 获取当前时间的上一月
     * currentMonth: 202009
     */
    public static String getLastMonth() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YYYYMM);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //取得上一个月时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        String lastMonth = sdf.format(calendar.getTime());
        return lastMonth;
    }

    public static String getCurrentMonth() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YYYYMM);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //取得上一个月时间
        //calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        String lastMonth = sdf.format(calendar.getTime());
        return lastMonth;
    }

    public static String getLastQuarter() throws Exception {
        SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_YYYYMM);
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        //取得上一个月时间
        calendar.set(Calendar.MONTH, calendar.get(Calendar.MONTH) - 1);
        String lastMonth = sdf.format(calendar.getTime());
        return lastMonth;
    }

    /**
     * @return
     * @Title: getYesterdayTime
     * @Description: TODO 获取昨天的日期
     */
    public static String getYesterday(String format) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, -1);//前1天
        Date date = calendar.getTime();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        String time = sdf.format(date);
        return time;
    }

    public static boolean current_week() {
        long startTime = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        calendar.setFirstDayOfWeek(Calendar.MONDAY);//设置星期一为一周开始的第一天
        calendar.setMinimalDaysInFirstWeek(4);//可以不用设置
        int weekYear = calendar.get(Calendar.YEAR);//获得当前的年
        calendar.setTimeInMillis(startTime);//时间戳
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);//获得当前日期属于今年的第几周
        System.out.println("第几年：" + weekYear);
        System.out.println("第几周：" + weekOfYear);
        calendar.setWeekDate(weekYear, weekOfYear, 2);//获得指定年的第几周的开始日期
        long starttime = calendar.getTime().getTime();//创建日期的时间该周的第一天，
        calendar.setWeekDate(weekYear, weekOfYear, 1);//获得指定年的第几周的结束日期
        long endtime = calendar.getTime().getTime();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String dateStart = simpleDateFormat.format(starttime);//将时间戳格式化为指定格式
        String dateEnd = simpleDateFormat.format(endtime);
        System.out.println(dateStart);
        System.out.println(dateEnd);
        return false;
    }

    public static String getToday() {
        Calendar calendar = Calendar.getInstance();
        Date date = calendar.getTime();
        String time = sdf.format(date);
        return time;
    }

    public static void main(String[] args) throws Exception {
        /*String ym = DateUtils.ym.format(new Date());
        System.out.println(getLastMonth());
        System.out.println(getCurrentToDay());
        System.out.println(getLastQuarter());
        System.err.println(current_week());*/
        //计算时间范围

        String currentMonth = getCurrentMonth();
        System.err.println(currentMonth);

        //日
        System.err.println(DateUtils.getToday());

        //周
        long startTime = new Date().getTime();
        Calendar calendar = Calendar.getInstance();
        System.out.println("当前日===="+calendar.get(Calendar.DATE));
        //设置星期一为一周开始的第一天
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        //可以不用设置
        calendar.setMinimalDaysInFirstWeek(4);
        int weekYear = calendar.get(Calendar.YEAR);//获得当前的年
        calendar.setTimeInMillis(startTime);//时间戳
        int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
        System.err.println(weekYear + "-" + weekOfYear);
        //月
        System.err.println(DateUtils.getCurrentMonth());
        //获得当前的月
        int month = calendar.get(Calendar.MONTH);
        //季度账期
        int quarter = month % 3 == 0 ? month / 3 : month / 3 + 1;
        System.err.println(weekYear + "-Q" + quarter + "th");
        //获得当前的年
        //获得当前的月
        //季度账期
        int halfYear = month % 6 == 0 ? month / 6 : month / 6 + 1;
        System.err.println(weekYear + "-" + quarter);
        //获得当前的年
        int year = calendar.get(Calendar.YEAR);
        System.err.println(weekYear + "");
        boolean flag = false;
        String pretreatmentDate = "14:33,15:00";
        try {
            if(DateUtils.isInDate(new Date(),DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYMMDD)+" "+pretreatmentDate.split("\\,")[0],DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYMMDD)+" "+pretreatmentDate.split("\\,")[1])){
                flag = true;
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        System.out.println("====="+flag);
        int a=3;
        System.out.println("============="+addDay(-a,DATE_YYYYMMDDHH) );


        //System.out.println(DateUtils.formatDate(RandomDateUtil.randomDate(DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYMMDD)+" "+pretreatmentDate.split("\\,")[0],DateUtils.formatDate(new Date(), DateUtils.FORMAT_YYMMDD)+" "+pretreatmentDate.split("\\,")[1]),DateUtils.DATE_FORMAT));

    }

    /**
     * 获取日期
     * @return
     */
    public static String getDateStr(Date date,String FORMAT_HOUR_DATE_TIME) {
        Calendar calendar=Calendar.getInstance();
        calendar.set(getNowYear(), getNowMonth()-1, 1);
        return getUserDate("yyyyMMddhhmmss");
    }

    /**
     * 获取今年是哪一年
     * @return
     */
    public static Integer getNowYear(){
        Date date = new Date();
        GregorianCalendar gc=(GregorianCalendar)Calendar.getInstance();
        gc.setTime(date);
        return Integer.valueOf(gc.get(1));
    }


    /**
     * 获取本月是哪一月
     * @return
     */
    public static int getNowMonth() {
        Date date = new Date();
        GregorianCalendar gc=(GregorianCalendar)Calendar.getInstance();
        gc.setTime(date);
        return gc.get(2) + 1;
    }

    /**
     * 根据用户传入的时间表示格式，返回当前时间的格式 如果是yyyyMMdd，注意字母y不能大写
     * @param sformat
     * @return
     */
    public static String getUserDate(String sformat) {
        Date currentTime = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat(sformat);
        String dateString = formatter.format(currentTime);
        return dateString;
    }

    /**
     * 将时间格式字符串转换为时间 yyyy-MM-dd HH:mm:ss
     *
     * @param strDate
     * @return
     */
    public static Date parseDate(String strDate) {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date strtodate = formatter.parse(strDate, pos);
        return strtodate;
    }
    /**
     * 2个日期相差天，月份，年份
     * @param data1 yyyy-MM-dd HH:mm:ss
     * @param data2 yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static Period dateDiffer(String data1, String data2 ){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        ParsePosition pos = new ParsePosition(0);
        Date date1 = formatter.parse(data1, pos);
        ParsePosition pos2 = new ParsePosition(0);
        Date date2 = formatter.parse(data2, pos2);
        //将Date类型转化为LocalDate
        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = date1.toInstant();
        LocalDate localDate1 = instant.atZone(zoneId).toLocalDate();
        Instant instant2 = date2.toInstant();
        LocalDate localDate2 = instant2.atZone(zoneId).toLocalDate();
        Period period = Period.between(localDate1, localDate2);
        return period;
    }

    /**
     * @author lvxin
     * @params
     * @Description: 获取当前年第一天
     * @date 2022/9/29 18:50
     */
    /**
     * 获取当年的第一天
     */
    public static String getCurrentFirstOfYear(){
        Calendar currCal=Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return formatDate(getFirstOfYear(currentYear),FORMAT_YYMMDD);
    }

    /**
     * 获取当年的最后一天
     */
    public static String getCurrentLastOfYear(){
        Calendar currCal=Calendar.getInstance();
        int currentYear = currCal.get(Calendar.YEAR);
        return formatDate(getLastOfYear(currentYear),FORMAT_YYMMDD);
    }

    /**
     * 获取某年第一天日期
     * @param year 年份
     * @return Date
     */
    public static Date getFirstOfYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        return calendar.getTime();
    }



    /**
     * 获取某年最后一天日期
     * @param year 年份
     * @return Date
     */
    public static Date getLastOfYear(int year){
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.YEAR, year);
        calendar.roll(Calendar.DAY_OF_YEAR, -1);
        return calendar.getTime();
    }
    public static Date convertStringToDate(String aMask, String strDate) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat(aMask);
        if (log.isDebugEnabled()) {
            log.debug("converting '" + strDate + "' to date with mask '" + aMask + "'");
        }

        try {
            Date date = df.parse(strDate);
            return date;
        } catch (ParseException var5) {
            throw new ParseException(var5.getMessage(), var5.getErrorOffset());
        }
    }

    public static Date convertStringToDate(String strDate) {
        Date aDate = null;

        try {
            if (log.isDebugEnabled()) {
                log.debug("converting date with pattern: " + getDatePattern());
            }

            aDate = convertStringToDate(getDatePattern(), strDate);
        } catch (ParseException var3) {
            log.error("Could not convert '" + strDate + "' to a date, throwing exception");
            var3.printStackTrace();
        }

        return aDate;
    }
    public static String getDatePattern() {
        Locale locale = LocaleContextHolder.getLocale();

        String defaultDatePattern;
        try {
            defaultDatePattern = ResourceBundle.getBundle("ApplicationResources", locale).getString("date.format");
        } catch (MissingResourceException var3) {
            defaultDatePattern = "yyyy-MM-dd";
        }

        return defaultDatePattern;
    }

    /**
     * 判断当前时间是否在时间区内
     * @param nowTime
     * @param amBeginTime
     * @param amEndTime
     * @return
     */
    public static boolean timeCalendar(Date nowTime, Date amBeginTime, Date amEndTime) {
        //设置当前时间
        Calendar date = Calendar.getInstance();
        date.setTime(nowTime);
        //设置开始时间
        Calendar amBegin = Calendar.getInstance();
        amBegin.setTime(amBeginTime);//上午开始时间
        Calendar pmEnd = Calendar.getInstance();
        pmEnd.setTime(amEndTime);//下午结束时间
        //处于开始时间之后，和结束时间之前的判断
        if ((date.after(amBegin) && date.before(pmEnd))) {
            return true;
        } else {
            return false;
        }
    }

    public static String getSysdate(String format) {
        if (format == null || "".equals(format)) {
            format = "yyyy-MM-dd HH:mm:ss";
        }

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }

    /**
     * 判断当前时间是否大于本月11号
     * @param time 目标日期，20230211
     * @return
     */
    public static boolean dateCompare(String time) throws Exception{
        // getCurrentMonth()+"11"
        try {
            Date currentDay = sdf.parse(sdf.format(new Date()));
            Date compareDay = sdf.parse(time);
            //判断当前时间大于21分钟
            if(currentDay.after(compareDay) || currentDay.equals(compareDay)){
                //System.out.println("大于21min---------------");
                return true;
            }
            //System.out.println("小于21min----------------");
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @return
     * @Title: getCurtDay
     * @Description: TODO 获取当前时间(20161109)
     */
    public static String getCurtDay() {
        return sdfymd.format(new Date());
    }

    public static boolean isInDate(Date date,String startDateFormat,String endDateFormat) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date start = sdf.parse(startDateFormat);
        Date end = sdf.parse(endDateFormat);
        if (start.after(end) || start.equals(end)) {
            /**
             * 直接用时间判断
             */
            if (date.getTime() >= start.getTime() || date.getTime() <= end.getTime()) {
                return true;
            }
        } else {
            if (date.getTime() >= start.getTime() && date.getTime() <= end.getTime()) {
                return true;
            }
        }
        return false;
    }
    /**
     * Get datetime string with format {@code yyyyMMddHHmmssSSS}
     *
     * @return date time string
     */
    public static String getTimestamp() {
        DateFormat dfmt = new SimpleDateFormat("yyyyMMddHHmmssSSS");
        return dfmt.format(new Date());
    }
    //日期按小时加减
    public static String addHour(int i, String format){
        Calendar c = Calendar.getInstance();
       // c.setTime(date);
        c.add(Calendar.HOUR_OF_DAY, i);
        Date newDate = c.getTime();
        DateFormat dfmt = new SimpleDateFormat(format);
        return dfmt.format(newDate);
    }

    public static String addDay(int i, String format){
        Calendar c = Calendar.getInstance();
        // c.setTime(date);
        c.add(Calendar.DATE, i);
        Date newDate = c.getTime();
        DateFormat dfmt = new SimpleDateFormat(format);
        return dfmt.format(newDate);
    }
}
