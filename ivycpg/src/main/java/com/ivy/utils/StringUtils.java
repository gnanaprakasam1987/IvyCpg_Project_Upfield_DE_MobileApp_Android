package com.ivy.utils;

import android.text.TextUtils;
import android.util.Patterns;
import android.webkit.URLUtil;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {


    private StringUtils() {

    }

    public static boolean validRegex(String pattern, String str) {

        if (pattern.equals("")) {
            return true;
        }

        Pattern mPattern = Pattern.compile(pattern);
        Matcher matcher = mPattern.matcher(str);

        // Entered text does not match the pattern
        if (!matcher.matches()) {
            // It does not match partially too
            return matcher.hitEnd();
        }
        return true;
    }


    public static String getStringQueryParam(String data) {
        if (data != null)
            return "'" + data + "'";
        else
            return null;
    }

    public static boolean isNullOrEmpty(String text) {
        return (text == null || text.trim().equals("null") || text.trim()
                .length() <= 0);
    }

    public static boolean isValidEmail(CharSequence target) {
        return !isNullOrEmpty(target.toString()) && Pattern.compile(
                "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                        "\\@" +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                        "(" +
                        "\\." +
                        "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                        ")+"
        ).matcher(target).matches();
    }

    public static boolean isValidURL(CharSequence targetUrl) {
        return !isNullOrEmpty(targetUrl.toString())
                && URLUtil.isValidUrl(targetUrl.toString())
                && Patterns.WEB_URL.matcher(targetUrl).matches();
    }

    public static boolean isValidRegx(CharSequence target, String regx) {

        if (regx.equals("")) {
            return true;
        }
        String value = regx.replaceAll("\\<.*?\\>", "");
        return !TextUtils.isEmpty(target) && Pattern.compile(value).matcher(target).matches();
    }

    /**
     * Removing single quotes from input string
     *
     * @param str - input string
     * @return formatted string
     */
    public static String removeQuotes(String str) {
        return str.replaceAll("'", " ");
    }


}
