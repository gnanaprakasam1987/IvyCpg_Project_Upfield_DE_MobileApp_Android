package com.ivy.utils;

import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.DataMembers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {

    public static final int DATE_TIME = 2;
    public static final int DATE_GLOBAL = 4;
    public static final int DATE_TIME_ID_MILLIS = 5;
    public static final int DATE_TIME_NEW = 6;
    public static final int GMT_DATE_TIME = 7;
    public static final int DATE_GLOBAL_PLAIN = 8;
    public static final int DATE_GLOBAL_HYPHEN = 9;
    public static final int DATE_DOB_FORMAT_PLAIN = 10;
    public static final int TIME = 0;
    public static final int DATE = 1;
    public static int DATE_TIME_ID = 3;

    private DateTimeUtils() {

    }


    /**
     * Return current Day. For Eg : Monday
     *
     * @return
     */
    public static String today() {
        Calendar cal = Calendar.getInstance();
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "Monday";
        }

    }

    /**
     * Return date as String in a given Format.
     *
     * @param dateFormat
     * @return
     */
    public static String now(int dateFormat) {
        Calendar cal = Calendar.getInstance();
        //DataMembers.backDate="2017/16/05";
        //DataMembers.backDate="05/16/2017";
        if (!DataMembers.backDate.isEmpty()) {
            int year, day, month;
            month = SDUtil.convertToInt(DataMembers.backDate.substring(0, 2));
            day = SDUtil.convertToInt(DataMembers.backDate.substring(3, 5));
            year = SDUtil.convertToInt(DataMembers.backDate.substring(6, 10));
            cal.set(year, month - 1, day);
        }

        if (TIME == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE_TIME == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy HH:mm:ss", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE_TIME_NEW == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE_GLOBAL == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE_TIME_ID_MILLIS == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmssSSS", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (GMT_DATE_TIME == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.ENGLISH);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            return sdf.format(cal.getTime());
        } else if (DATE_GLOBAL_PLAIN == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE_GLOBAL_HYPHEN == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        } else if (DATE_DOB_FORMAT_PLAIN == dateFormat) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyy", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        }else {
            SimpleDateFormat sdf = new SimpleDateFormat("MMddyyyyHHmmss", Locale.ENGLISH);
            return sdf.format(cal.getTime());
        }

    }

    /**
     * an int < 0 if second Date is greater than the first Date, 0 if they are
     * equal, and an int > 0 if this Date is greater.
     *
     * @param firstDate
     * @param secondDate
     * @return
     */
    public static int compareDate(String firstDate, String secondDate,
                                  String format) {
        int result = 0;
        SimpleDateFormat sf = new SimpleDateFormat(format);
        try {
            if (firstDate != null && secondDate != null)
                result = sf.parse(firstDate).compareTo(sf.parse(secondDate));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;

    }
}
