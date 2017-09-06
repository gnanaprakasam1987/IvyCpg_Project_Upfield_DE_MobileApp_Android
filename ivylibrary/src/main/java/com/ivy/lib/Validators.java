package com.ivy.lib;

import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validators {
	private static Pattern pattern;
	private static Matcher matcher;
 
	private static final String EMAIL_PATTERN = 
		"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
		+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	
	public static boolean isEmpty(TextView view) {
		if ((view.getText().toString().trim()).equals(""))
			return true;

		return false;
	}

	public static boolean isEmpty(EditText view) {
		if ((view.getText().toString().trim()).equals(""))
			return true;

		return false;
	}

	public static boolean isEmpty(TextView[] viewArray) {
		for (TextView view : viewArray)
			if (isEmpty(view))
				return true;

		return false;
	}

	public static boolean isEmpty(EditText[] viewArray) {
		for (EditText view : viewArray)
			if (isEmpty(view))
				return true;

		return false;
	}
	
	public static boolean isNotEmpty(TextView view) {
		if (isEmpty(view))
			return false;
		return true;
	}

	public static boolean isNotEmpty(EditText view) {
		if (isEmpty(view))
			return false;
		return true;
	}

	public static boolean isNotEmpty(TextView[] viewArray) {
		for (TextView view : viewArray)
			if (isNotEmpty(view))
				return true;

		return false;
	}

	public static boolean isNotEmpty(EditText[] viewArray) {
		for (EditText view : viewArray)
			if (isNotEmpty(view))
				return true;

		return false;
	}
	
	public static boolean hasValidEmailId(EditText view) {
		if (view == null)
			return false;
		
		if (isEmailId(view.getText().toString()))
			return true;
		
		return false;
	}
	
	public static boolean isEmailId(String emailId) {
		if (emailId == null)
			return false;
		if (emailId.length() < 5)
			return false;
		
		pattern = Pattern.compile(EMAIL_PATTERN);
		
		matcher = pattern.matcher(emailId);
		return matcher.matches();
		
//		if (emailId.matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"))
//			return true;
//		
//		return false;
	}
	
	public static boolean hasValidMobileNumber(EditText view) {
		if (view == null)
			return false;
		
		if (isMobileNumber(view.getText().toString()))
			return true;
		
		return false;
	}
	
	public static boolean isMobileNumber(String mobileNumber) {
		if (mobileNumber == null)
			return false;
		
		if (mobileNumber.length() != 10 )
			return false;
		
		
		if (!mobileNumber.matches("^(9|8|7).*"))
			return false;

		return true;
	}
}
