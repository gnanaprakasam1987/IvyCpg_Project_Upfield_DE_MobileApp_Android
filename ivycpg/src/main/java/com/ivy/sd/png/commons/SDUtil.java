package com.ivy.sd.png.commons;

import android.content.Context;

import com.ivy.lib.Logs;
import com.ivy.lib.base64.Base64;
import com.ivy.sd.png.util.DataMembers;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class SDUtil {

    public static final int TIME = 0;
    public static final int DATE = 1;
    public static int DATE_TIME_ID = 3;
    public static final int DATE_TIME = 2;
    public static final int DATE_GLOBAL = 4;
    public static final int DATE_TIME_ID_MILLIS = 5;
    public static final int DATE_TIME_NEW = 6;
    public static final int GMT_DATE_TIME = 7;
    public static final int DATE_GLOBAL_PLAIN = 8;
    public static final int DATE_GLOBAL_EIPHEN = 9;
    public static final int DATE_DOB_FORMAT_PLAIN = 10;
    public static int CALCULATION_PRECISION_COUNT =3;


    /**
     * Removing single quotes from input string
     *
     * @param str - input string
     * @return formatted string
     */
    public static String removeQuotes(String str) {
        return str.replaceAll("'", " ");
    }

    /**
     * Convert String into Int. handle exception.
     *
     * @param val - string value
     * @return equivalent Int value or 0 if exception
     */
    public static int convertToInt(String val) {
        if (val == null)
            return 0;
        try {
            return Integer.parseInt(val);
        } catch (NumberFormatException exc) {
            Logs.exception("convertToInt", exc.toString());

        }
        return 0;
    }

    /**
     * Convert String into Long. handle exception.
     *
     * @param val - string value
     * @return equivalent Long value or -1 if exception
     */
    public static long convertToLong(String val) {
        if (val == null)
            return 0;
        if (val.equals(""))
            return 0;
        try {
            return Long.parseLong(val);
        } catch (NumberFormatException exc) {
            Logs.exception("convertToLong", exc.toString());
            return -1;
        }
    }

    /**
     * Convert String into Float. handle exception.
     *
     * @param val
     * @return equivalent Float value or 0 if exception
     */
    public static float convertToFloat(String val) {
        if (val == null)
            return 0.0f;
        try {
            return Float.parseFloat(val);
        } catch (NumberFormatException exc) {
            Logs.exception("convertToFloat", exc.toString());
        }
        return 0.0f;
    }

    /**
     * Convert String into Double. handle exception.
     *
     * @param val
     * @return equivalent double value or 0 if exception
     */
    public static double convertToDouble(String val) {
        if (val == null)
            return 0.0;
        try {
            return Double.parseDouble(val);
        } catch (NumberFormatException exc) {
            Logs.exception("convertToDouble", exc.toString());

        }
        return 0.0;
    }

    /**
     * This method will round the float or Decimal value. It wont round off like
     * Math.round instead it will simply truncate the value.
     *
     * @param val
     * @param n   - decimal place length
     * @return
     */
    public static String roundIt(double val, int n) {
        if (val == 0)
            return "0";
        String st, st1;
        int i, len;
        st = Double.toString(val);
        i = st.indexOf(".");
        if (i == -1) // no points
            st = st + ".00";
        else {
            st1 = st.substring(i + 1); // after points
            len = st1.length();
            if (len > n)
                st1 = st1.substring(0, n);
            else if (len < n)
                st1 = st1 + "0";
            st = st.substring(0, i) + "." + st1;
        }
        return st;
    }

    /**
     * Comments added
     * @param value
     * @param numberofDecimals
     * @return
     */
    public static BigDecimal truncateDecimal(double value, int numberofDecimals) {

        if (numberofDecimals == -1)
            return new BigDecimal(value);
        else
            return new BigDecimal(SDUtil.format(value,
                    numberofDecimals,
                    0));
    }

    public static String format(double value, int precisionCount, int separatorCount) {
        return format(value, precisionCount, separatorCount, false);
    }

    public static String format(double value, int precisionCount, int separatorCount, Boolean isDotForGroup) {
        try {
            if (value == 0.0D) {
                return "0";
            } else {
                StringBuilder e = new StringBuilder();
                StringBuilder commaFormat = new StringBuilder("#");
                StringBuilder numberFormat;
                int i;
                if (precisionCount > 0) {
                    numberFormat = new StringBuilder(".");

                    for (i = 0; i < precisionCount; ++i) {
                        numberFormat.append("0");
                    }

                    e.append(numberFormat);
                }

                if (separatorCount > 0) {
                    numberFormat = new StringBuilder(",");

                    for (i = 0; i < separatorCount; ++i) {
                        numberFormat.append("#");
                    }

                    commaFormat.append(numberFormat);
                }

                DecimalFormatSymbols otherSymbols = new DecimalFormatSymbols(Locale.ENGLISH);

                if (isDotForGroup) {
                    otherSymbols.setDecimalSeparator(',');
                    otherSymbols.setGroupingSeparator('.');
                }

                DecimalFormat var9 = new DecimalFormat(commaFormat.toString() + e.toString(), otherSymbols);
                var9.setRoundingMode(RoundingMode.HALF_UP);
                return var9.format(value);
            }
        } catch (Exception var8) {
            return "Err";
        }
    }

    public static double formatAsPerCalculationConfig(double value){
        return convertToDouble(format(value, CALCULATION_PRECISION_COUNT,0));
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
            month = convertToInt(DataMembers.backDate.substring(0, 2));
            day = convertToInt(DataMembers.backDate.substring(3, 5));
            year = convertToInt(DataMembers.backDate.substring(6, 10));
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
        } else if (DATE_GLOBAL_EIPHEN == dateFormat) {
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

    /**
     * This method will automatically enable "Use Network Provided values" in
     * android device. This method need android.permission.WRITE_SECURE_SETTINGS
     * permission in Android Manifest.
     *
     * @param c Context
     */
    public static void useNetworkProvidedValues(Context c) {
        try {
            int i = android.provider.Settings.System.getInt(
                    c.getContentResolver(),
                    android.provider.Settings.System.AUTO_TIME);
            if (i == 0) {
                android.provider.Settings.System.putInt(c.getContentResolver(),
                        android.provider.Settings.System.AUTO_TIME, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * This method will convert in input data into MD5 hash equivalent and apply
     * base64
     *
     * @param data
     * @return data
     */
    public static String convertIntoMD5hashAndBase64(String data) {
        String base64String = "";
        try {
            byte[] bytesOfMessage = data.getBytes("UTF-8");
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] thedigest = md.digest(bytesOfMessage);
            base64String = Base64.encode(thedigest, 0, thedigest.length);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return base64String;
    }

    /* This method will round the float or Decimal value using
     * Math.round */
    public static double mathRoundoff(double value) {
        double roundOff = Math.round(value * 100.0) / 100.0;
        return roundOff;
    }

    /**
     * This method will convert exponential value into String Using
     * BigDecimal or Format
     */
    static DecimalFormat df = new DecimalFormat("###.00"); //'0's restricted to two decimal places for JNJ after discussing with abbas

    public static String getWithoutExponential(Double value) {
        return ((value + "").contains("E")
                ? df.format(new BigDecimal(value)) : (format(value, 2, 0)));
    }

    public static String getWithoutExponential(String value) {

        if (value == null)
            value = "0";
        else if(value.contains("E"))
            value = df.format(new BigDecimal(value));
        else if (value.isEmpty())
            value = "0";

        return value;

    }

}
