package com.ivy.sd.png.commons;

import android.content.Context;

import com.ivy.lib.Logs;
import com.ivy.lib.base64.Base64;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class SDUtil {

    public static int CALCULATION_PRECISION_COUNT =3;


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

        if (StringUtils.isNullOrEmpty(value))
            value = "0";
        else if(value.contains("E"))
            value = df.format(new BigDecimal(value));

        return value;

    }

    public static String getWholeNumber(String data) {
        String wholeNumber;
        try {
            wholeNumber = data.substring(0, data.lastIndexOf("."));

        } catch (Exception e) {
            Commons.printException("" + e);
            return data;
        }
        return wholeNumber;
    }


    public static boolean isValidDecimal(String value, int wholeValueCount,
                                  int decimalValueCount) {
        String strPattern = "(^([0-9]{0," + wholeValueCount
                + "})?)(\\.[0-9]{0," + decimalValueCount + "})?$";
        return value.matches(strPattern);
    }
}
