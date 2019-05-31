package com.ivy.lib;

import android.content.Context;
import android.database.DatabaseUtils;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.widget.ImageView;

import com.bumptech.glide.request.target.BitmapImageViewTarget;

import java.io.File;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

public class Utils {

	private static final String TAG = "Utils";

	/**
	 * MM/dd/yyyy
	 * 
	 * @return
	 */
	public static String getDate() {
		return now("MM/dd/yyyy");
	}

	public static String getDate(String format) {
		return now(format);
	}
	
	
	/**
	 * 
	 * @return GMT time
	 * yyyy-MM-dd HH:mm:ss
	 */
	public static String getGMTDateTime(String format){
		SimpleDateFormat sdf= new SimpleDateFormat(format,Locale.ENGLISH);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        String s=sdf.format(new Date());
        return s;
	}

	/**
	 * Format HH:mm:ss
	 * 
	 * @return
	 */
	public static String getTime() {
		return now("HH:mm:ss");
	}

	public static String getTimeForPicker() {
		return now("hh:mm a");
	}

	public static String getTimeForID() {
		return now("HHmmss");
	}

	/**
	 * Format MM/dd/yyyy HH:mm:ss
	 * 
	 * @return
	 */
	public static String getDateTime() {
		return now("MM/dd/yyyy HH:mm:ss");
	}

	public static String getDateTime(String format) {
		return now(format);
	}

	/**
	 * Format : MMddyyyy
	 * 
	 * @return
	 */
	public static String getDateForID() {
		return now("yyyyMMdd");
	}

	/**
	 * 
	 * @param month
	 * @param day
	 * @param year
	 * @return
	 */
	public static String getDateForID(int month, int day, int year) {
		StringBuffer sb = new StringBuffer();
		sb.append(month).append(day).append(year);
		return sb.toString();
	}

	/**
	 * Format : yyDHHmmss
	 * 
	 * @return
	 */
	public static String getDateTimeForID() {
		return now("yyDHHmmss");
	}
	
	/**
	 * Format : yyDHHmmssSSS
	 * 
	 * @return
	 */
	public static String getDateTimeForIDWithMilliSec() {
		return now("yyDHHmmssSSS");
	}

	/**
	 * 
	 * @param date
	 * @return Jan 1, 1970
	 */
	public static String formatDateAsHumanReadable(String date) {
		// Logs.debug("Utils", date);

		if (null != date)
			if (!date.equals("")) {
				try {
					String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat("yyyy-MM-dd");
					Date dt = cf.parse(dat[0]);
					SimpleDateFormat sf = new SimpleDateFormat("MMM dd, yyyy");
					return sf.format(dt);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		// return DateFormat.getDateInstance().format(new Date(date));

		return "";
	}

	/**
	 * @param date
	 * @param fromFormat
	 * @param toFormat
	 * @return
	 */
	public static String formatDateAsUserRequired(String date,
			String fromFormat, String toFormat) {

		if (null != date)
			if (!date.equals("")) {
				try {
					// String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat(fromFormat);
					// Date dt = cf.parse(dat[0]);
					Date dt = cf.parse(date);
					SimpleDateFormat sf = new SimpleDateFormat(toFormat);
					return sf.format(dt);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}

		return "";

	}

	public static String formatDateAsHumanReadable(String date, String format) {

		if (null != date)
			if (!date.equals("")) {
				try {
					String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat(format);
					Date dt = cf.parse(dat[0]);
					SimpleDateFormat sf = new SimpleDateFormat("MMM dd, yyyy");
					return sf.format(dt);
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		// return DateFormat.getDateInstance().format(new Date(date));

		return "";
	}

	public static int getDayOfWeek() {
		/*
		 * Calendar cal = Calendar.getInstance(); //MonthDisplayHelper helper =
		 * new MonthDisplayHelper(cal.get(Calendar.YEAR),
		 * cal.get(Calendar.MONTH)); return cal.get(Calendar.DAY_OF_WEEK);
		 */
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
	}

	public static int getLastDayOfMonth() {
		return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
	}

	public static int getDayOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
	}

	public static int getWeekOfMonth() {
		return Calendar.getInstance().get(Calendar.DAY_OF_WEEK_IN_MONTH);
	}

	/*
	public static int getBusinessWeekOfMonth() {
		Calendar c = Calendar.getInstance();
		MonthDisplayHelper helper = new MonthDisplayHelper(
				c.get(Calendar.YEAR), c.get(Calendar.MONTH));
		int firstDayOfMonth = helper.getFirstDayOfMonth();
		int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

		Logs.exception(TAG, "first day of month : " + firstDayOfMonth);
		Logs.exception(TAG, "day of week : " + dayOfWeek);
		
		if (dayOfWeek < firstDayOfMonth)
		{
			Logs.exception(TAG, "Calendar.WEEK_OF_MONTh-1="+(c.get(Calendar.WEEK_OF_MONTH)-1));
			
			return c.get(Calendar.WEEK_OF_MONTH) - 1;
		}

		Logs.exception(TAG, "Calendar.WEEK_OF_MONTh="+c.get(Calendar.WEEK_OF_MONTH));
		
		return c.get(Calendar.WEEK_OF_MONTH);
	}*/
	

	 public static int getBusinessWeekOfMonth() {
			Calendar c = Calendar.getInstance();
			
			return getBusinessWeekOfMonth(c);
			
			/*
			MonthDisplayHelper helper = new MonthDisplayHelper(
					c.get(Calendar.YEAR), c.get(Calendar.MONTH));
			int firstDayOfMonth = helper.getFirstDayOfMonth();
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

			Log.d(TAG, "first day of month : " + firstDayOfMonth);
			
			Log.d(TAG, "day of week : " + dayOfWeek);
			
			if (dayOfWeek < firstDayOfMonth)
			{
				Log.d(TAG, "c.get(Calendar.WEEK_OF_MONTH) = "+c.get(Calendar.WEEK_OF_MONTH)+"Calendar.WEEK_OF_MONTh-1="+(c.get(Calendar.WEEK_OF_MONTH)-1));
				Log.d(TAG, "dayOfWeek < firstDayOfMonth is true");
				
				int bussiness_week= c.get(Calendar.WEEK_OF_MONTH) - 1;
				Log.d(TAG, "bussiness_week="+bussiness_week);
				
				return bussiness_week;
			}
			else
			{
				Log.d(TAG, "dayOfWeek < firstDayOfMonth is false");
			}

			Log.d(TAG, "Calendar.WEEK_OF_MONTh="+c.get(Calendar.WEEK_OF_MONTH));
			
			int bussiness_week= c.get(Calendar.WEEK_OF_MONTH);
			Log.d(TAG, "bussiness_week="+bussiness_week);
			return bussiness_week;*/
		}

	    
	    public static int getBusinessWeekOfMonth(Calendar c) 
	    {
			int date=c.get(Calendar.DATE);
			
			if(date<=7)		
				return 1;
			
			int quotient=date/7;
			int remainder=date%7;
			int week=quotient;
			if(remainder==0)
			{
				
			}
			else
			{
				week=week+1;
			}
			
			return week;
			/*
			MonthDisplayHelper helper = new MonthDisplayHelper(
					c.get(Calendar.YEAR), c.get(Calendar.MONTH));
			int firstDayOfMonth = helper.getFirstDayOfMonth();
			int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

			Log.d(TAG, "first day of month : " + firstDayOfMonth);
			
			Log.d(TAG, "day of week : " + dayOfWeek);
			
			if (dayOfWeek < firstDayOfMonth)
			{
				Log.d(TAG, "c.get(Calendar.WEEK_OF_MONTH) = "+c.get(Calendar.WEEK_OF_MONTH)+"Calendar.WEEK_OF_MONTh-1="+(c.get(Calendar.WEEK_OF_MONTH)-1));
				Log.d(TAG, "dayOfWeek < firstDayOfMonth is true");
				
				int bussiness_week= c.get(Calendar.WEEK_OF_MONTH) - 1;
				Log.d(TAG, "bussiness_week="+bussiness_week);
				
				return bussiness_week;
			}
			else
			{
				Log.d(TAG, "dayOfWeek < firstDayOfMonth is false");
			}

			Log.d(TAG, "Calendar.WEEK_OF_MONTh="+c.get(Calendar.WEEK_OF_MONTH));
			
			int bussiness_week= c.get(Calendar.WEEK_OF_MONTH);
			Log.d(TAG, "bussiness_week="+bussiness_week);
			return bussiness_week;*/
		}

	    
	public static int getMonth() {
		return Calendar.getInstance().get(Calendar.MONTH) + 1;
	}

	/**
	 * 
	 * @param month
	 * @param day
	 * @param year
	 * @return
	 */
	public static String convertToDate(int month, int day, int year) {
		StringBuilder sb = new StringBuilder();
		if (month < 10)
			sb.append("0").append(month);
		else
			sb.append(month);
		sb.append("/");

		if (day < 10)
			sb.append("0").append(day);
		else
			sb.append(day);

		sb.append("/");

		sb.append(year);
		return sb.toString();
	}

	public static String now(String format) {
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(format,Locale.ENGLISH);
		return sdf.format(cal.getTime());
	}

	public static String parseString(Double value) {
		return formatAsTwoDecimal(value);
	}

	public static String formatAsTwoDecimal(String val) {
		if (val != null) {
			if (val.equals(null) || val.equals(""))
				return "0.00";

			if (val.indexOf(".") == -1)
				return val.concat(".00");

			if ((val.substring(val.indexOf(".") + 1).length()) == 1)
				return val.concat("0");
		} else {
			return "0.00";
		}
		return formatAsTwoDecimal(Double.parseDouble(val));
	}

	public static String formatAsTwoDecimal(Double value) {
		if (value == null)
			return "0.00";
		if (value == 0)
			return "0.00";

		DecimalFormat decimalFormat = new DecimalFormat("#.##");
		String val = decimalFormat.format(value);

		if (val.indexOf(".") == -1)
			return val.concat(".00");

		if ((val.substring(val.indexOf(".") + 1).length()) == 1)
			return val.concat("0");

		return val;
	}

	public static String formatAsIndianRupee(int value) {
		StringBuilder sb = new StringBuilder();

		return "";
	}

	public static String formatAsIndianRupee(double value) {

		return "";
	}

	public static String QT(String data) {
		if (data != null)
			return "'" + data + "'";
		return "''";
	}

	public static String trimRight(String data, String remove) {
		if (data == null)
			return data;
		if (data.equals(""))
			return data;

		if (remove == null)
			return data;
		if (remove.equals(""))
			return data;

		int dataLen = data.length();
		int removeLen = remove.length();

		int lastIndexOfRemove = data.lastIndexOf(remove);

		if (lastIndexOfRemove != -1) {
			if (dataLen == (lastIndexOfRemove + removeLen))
				return data.substring(0, dataLen - removeLen);
		}
		return data;
	}

	public static String trimLeft(String data, String remove) {
		if (data == null)
			return data;
		if (data.equals(""))
			return data;

		if (remove == null)
			return data;
		if (remove.equals(""))
			return data;

		if (data.indexOf(remove) == 0)
			return data.substring(remove.length(), data.length());

		return data;
	}

	public static String convertToString(List list, String separater) {
		if (list != null) {
			int size = list.size();
			if (size > 0) {
				StringBuffer sb = new StringBuffer();
				for (int i = 0; i < size; i++) {
					sb.append(list.get(i)).append(separater);
				}

				return trimRight(sb.toString(), separater);
			}
		}
		return null;
	}

	public static String convertToString(String[] stringArray, String separater) {
		return convertToString(stringArray, separater, false);
	}

	public static String convertToString(String[] stringArray,
			String separater, boolean sqlEscape) {
		if (stringArray == null || separater == null)
			return null;

		int size = stringArray.length;
		StringBuilder sb = new StringBuilder();

		for (int i = 0; i < size; i++) {

			if (sqlEscape)
				sb.append(DatabaseUtils.sqlEscapeString(stringArray[i]));
			else
				sb.append(stringArray[i]);

			if (i != size - 1)
				sb.append(separater);
		}

		return sb.toString();
	}

	/**
	 * an int < 0 if second Date is less than the first Date, 0 if they are
	 * equal, and an int > 0 if this Date is greater.
	 * 
	 * @param firstDate
	 * @param secondDate
	 * @return
	 */
	public static int compareDate(String firstDate, String secondDate) {
		int result = 0;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			result = sf.parse(firstDate).compareTo(sf.parse(secondDate));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;

	}

	public static boolean isNextDate(String date) {
		boolean result = false;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date newDate = calendar.getTime();

		try {
			int compare = sf.parse(sf.format(newDate))
					.compareTo(sf.parse(date));
			if (compare == 0) {
				result = true;
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}

		Logs.debug(TAG, "Next Date : " + date);

		return result;
	}

	public static Calendar getNextDate(String date) {
		boolean result = false;
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DAY_OF_YEAR, 1);
		Date newDate = calendar.getTime();

		Logs.debug(TAG, "Next Date : " + date);

		return calendar;
	}

	public static int getDaysDifference(String fromDate, String toDate,
			String format) {

		if (fromDate != null && toDate != null && format != null) {
			Calendar today = Calendar.getInstance();

			SimpleDateFormat formatter = new SimpleDateFormat(format);

			Date fromDat = null;
			Date toDat = null;

			try {
				fromDat = formatter.parse(fromDate);
				toDat = formatter.parse(toDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}

			if (fromDat != null && toDat != null) {

				Calendar from = Calendar.getInstance();
				from.setTime(fromDat);

				Calendar to = Calendar.getInstance();
				to.setTime(toDat);

				long diff = to.getTimeInMillis() - from.getTimeInMillis();

				long days = diff / (24 * 60 * 60 * 1000);

				return (int) days;

			}
		}

		return 0;
	}

	public static boolean setScreenTimeOut(Context context, int milliseconds) {
		return android.provider.Settings.System.putInt(
				context.getContentResolver(),
				android.provider.Settings.System.SCREEN_OFF_TIMEOUT,
				milliseconds);
	}

	public static double round(double unrounded, int precision) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, BigDecimal.ROUND_HALF_EVEN);

		return rounded.doubleValue();
	}
	
	

	public static double round(double unrounded, int precision, int roundingMode) {
		BigDecimal bd = new BigDecimal(unrounded);
		BigDecimal rounded = bd.setScale(precision, roundingMode);

		return rounded.doubleValue();
	}
	public static String getSdcardPath(Context context) {

		String sSDpath = null;
		String[] storageDirectories;
		String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
		try {

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				List<String> results = new ArrayList<String>();
				File[] externalDirs = context.getExternalFilesDirs(null);
				for (File file : externalDirs) {
					String path = file.getPath().split("/Android")[0];
					if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Environment.isExternalStorageRemovable(file))
							|| rawSecondaryStoragesStr != null && rawSecondaryStoragesStr.contains(path)) {
						results.add(path);
					}
				}
				storageDirectories = results.toArray(new String[0]);
			} else {
				final Set<String> rv = new HashSet<>();

				if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
					final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
					Collections.addAll(rv, rawSecondaryStorages);
				}
				storageDirectories = rv.toArray(new String[rv.size()]);
			}
			if (storageDirectories.length > 0)
				sSDpath = storageDirectories[0];
		}catch (Exception e){
			e.printStackTrace();
			sSDpath="";
		}
		return sSDpath;
	}

	/**
	 * This method used re
	 * @param context
	 * @param imageView
	 * @param radius
	 * @return
	 */
	public static BitmapImageViewTarget getRoundedImageTarget(@NonNull final Context context, @NonNull final AppCompatImageView imageView,
															  final float radius) {
		return new BitmapImageViewTarget(imageView) {
			@Override
			protected void setResource(final Bitmap resource) {
				RoundedBitmapDrawable circularBitmapDrawable =
						RoundedBitmapDrawableFactory.create(context.getResources(), resource);
				circularBitmapDrawable.setCornerRadius(radius);
				imageView.setImageDrawable(circularBitmapDrawable);
			}
		};
	}

}
