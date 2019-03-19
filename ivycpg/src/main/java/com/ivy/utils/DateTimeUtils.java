package com.ivy.utils;

import android.support.annotation.NonNull;

import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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
    public static  String defaultDateFormat = "MM/dd/yyyy";
    private static final String serverDateFormat = "yyyy/MM/dd";
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
     * @deprecated
     * @see {@link #getDateCount(String, String, String)}
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

    //Get First Day of the month
    public static String getFirstDayOfCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH,
                Calendar.getInstance().getActualMinimum(Calendar.DAY_OF_MONTH));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd",
                Locale.ENGLISH);
        return sdf.format(cal.getTime());
    }

    public static int getCurrentMonth() {
        Calendar cal = Calendar.getInstance();
        return cal.get(Calendar.MONTH) + 1;
    }

    /**
     * Add days to the Date provided
     *
     * @param dateInput Given Date
     * @param noofDays   No of Days to be added
     * @return date
     */
    public static Date addDaystoDate(Date dateInput, int noofDays){
        Calendar c = Calendar.getInstance();
        c.setTime(dateInput);
        // manipulate date
        c.add(Calendar.DATE, noofDays);
        // convert calendar to date
        return c.getTime();
    }

    /**
     * getDateCount between fromDate and toDate
     *
     * @param fromDate starting date
     * @param toDate   ending date
     * @param format   which format of date
     * @return dateCount
     */
    public static int getDateCount(String fromDate, String toDate, String format) {
        Date d1, d2;
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat(format, Locale.US);
            d1 = dateFormat.parse(fromDate);
            d2 = dateFormat.parse(toDate);
            return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
        } catch (ParseException e) {
            return -1;
        }

    }

    /**
     * convert date object to user requested format.
     *
     * @param dateInput     date in Object
     * @param outDateFormat expected output date format
     * @return date string in requested format
     */
    public static String convertDateObjectToRequestedFormat(Date dateInput, String outDateFormat) {
        String outDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(outDateFormat,
                    Locale.ENGLISH);
            outDate = sdf.format(dateInput);
        } catch (Exception e) {
            SimpleDateFormat sdf = new SimpleDateFormat(defaultDateFormat,
                    Locale.ENGLISH);
            outDate = sdf.format(dateInput);
            Commons.printException("convertDateObjectToRequestedFormat" + e);
        }
        return outDate;
    }

    /**
     * convert date from server format(yyyy/MM/dd) to user requested format.
     * This will be used to display the date in screens.
     * <p>
     * If input outDateFormat is incorrect, then this methiod will return in MM/dd/yyyy
     *
     * @param dateInput     date in String
     * @param outDateFormat expected output date format
     * @return date String in requested format
     */
    public static String convertFromServerDateToRequestedFormat(String dateInput, String outDateFormat) {
        String outDate;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(serverDateFormat,
                    Locale.ENGLISH);
            if (outDateFormat.equals(serverDateFormat)) {
                return dateInput;
            } else {
                Date date = sdf.parse(dateInput);
                sdf = new SimpleDateFormat(outDateFormat, Locale.ENGLISH);
                outDate = sdf.format(date);
            }
        } catch (Exception e) {
            Commons.printException(e + "");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(ConfigurationMasterHelper.outDateFormat,
                        Locale.ENGLISH);
                Date date = sdf.parse(dateInput);
                sdf = new SimpleDateFormat(outDateFormat, Locale.ENGLISH);
                outDate = sdf.format(date);
                return outDate;
            } catch (Exception e1) {
                Commons.printException("convertFromServerDateToRequestedFormat" + e1);
                return dateInput;
            }
        }
        return outDate;
    }

    /**
     * Convert date from userspecific format to server format (yyyy/MM/dd).
     * While saving the date in DB, this method will be useful.
     *
     * @param dateInput       date in String
     * @param dateInputFormat input date format
     * @return date string in server format
     */
    public static String convertToServerDateFormat(String dateInput,
                                                   String dateInputFormat) {
        String outDate;
        try {

            SimpleDateFormat sdf = new SimpleDateFormat(dateInputFormat,
                    Locale.ENGLISH);
            if (dateInputFormat.equals(serverDateFormat)) {
                return dateInput;
            } else {
                Date date = sdf.parse(dateInput);
                sdf = new SimpleDateFormat(serverDateFormat, Locale.ENGLISH);
                outDate = sdf.format(date);
            }
        } catch (Exception e) {
            Commons.printException(e + "");
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(defaultDateFormat,
                        Locale.ENGLISH);
                Date date = sdf.parse(dateInput);
                sdf = new SimpleDateFormat(serverDateFormat, Locale.ENGLISH);
                outDate = sdf.format(date);
                return outDate;
            } catch (Exception e1) {
                Commons.printException("convertToServerDateFormat" + e1);
                return dateInput;
            }
        }
        return outDate;
    }

    /**
     * convert date (yyyy/MM/dd) from server format to user requested format.
     * This method is use full to apply validations.
     *
     * @param dateInput    date in String
     * @param dateInputFormat input date format
     * @return date object
     */
    public static Date convertStringToDateObject(String dateInput,
                                                 String dateInputFormat) {
        Date outDate;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(dateInputFormat,
                    Locale.ENGLISH);
            outDate = sdf.parse(dateInput);
        } catch (Exception e) {
            Commons.printException("convertStringToDateObject" + e);
            return null;
        }
        return outDate;
    }

    /**
     *
     * @param format input date format
     * @return SimpleDateFormat
     */

    @NonNull
    public static SimpleDateFormat getDateFormat (String format){
        return new SimpleDateFormat(format,Locale.US);
    }

    public static String getTimeZone() {
        try {
            return TimeZone.getDefault().getDisplayName(false, TimeZone.SHORT,
                    Locale.ENGLISH);
        } catch (Exception e) {

        }
        return "UTC";
    }

    public static String convertDateTimeObjectToRequestedFormat(String inputText,String inputDateFormat, String outDateFormat) {
        String outDate="";
        try {
            DateFormat outputFormat = new SimpleDateFormat(outDateFormat, Locale.ENGLISH);
            DateFormat inputFormat = new SimpleDateFormat(inputDateFormat, Locale.ENGLISH);
            Date date = inputFormat.parse(inputText);
            outDate = outputFormat.format(date);
        } catch (Exception e) {

            Commons.printException("convertDateObjectToRequestedFormat" + e);
        }
        return outDate;
    }

}
