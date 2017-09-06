package com.ivy.lib;

import android.util.Log;

public class Logs {
	
	private static StackTraceElement[] stackTraceElements;
	
	public static void debug(String tag, String msg) {
		//stackTraceElements = Thread.currentThread().getStackTrace();

		/*int si = stackTraceElements.length;
		
		for (int i = 0; i < si; i++) {
			Log.d(tag, stackTraceElements[i].getMethodName());	
		}
		*/
		
		//Log.d(tag, "Method : " + stackTraceElements[3].getMethodName() + " :: " + msg);
		 Log.d(tag, msg);
	}
	
	public static void exception(String tag, String msg) {
		Log.e(tag, msg);
	}
	
	public static void query(String tag, String msg) {
		Log.i(tag, msg);
	}
}
