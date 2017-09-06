package com.ivy.sd.png.util;

import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateUtil {

    private DateUtil(){
        //to avoid compile time error when object with no parameter/s created
    }

    private static final String defaultDateFormat = "MM/dd/yyyy";
    private static final String serverDateFormat = "yyyy/MM/dd";


    /**
     * convert date object to user requested format.
     *
     * @param dateInput     date in Object
     * @param outDateFormat expected output date format
     * @return date string in requested format
     */
    public static String convertDateObjectToRequestedFormat(Date dateInput,
                                                            String outDateFormat) {
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
    public static String convertFromServerDateToRequestedFormat(String dateInput,
                                                                String outDateFormat) {
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
     * Add days to the Date provided
     *
     * @param dateInput Given Date
     * @param noofDays   No of Days to be added
     * @return date
     */
    public static Date addDaystoDate(Date dateInput,int noofDays){
        Calendar c = Calendar.getInstance();
        c.setTime(dateInput);
        // manipulate date
        c.add(Calendar.DATE, noofDays); //same with c.add(Calendar.DAY_OF_MONTH, 1);
        // convert calendar to date
        return c.getTime();
    }
}
