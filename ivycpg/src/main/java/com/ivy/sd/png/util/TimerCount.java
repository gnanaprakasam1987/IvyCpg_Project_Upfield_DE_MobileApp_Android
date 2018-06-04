package com.ivy.sd.png.util;

import android.os.Handler;
import android.os.Message;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This class is used to get the time taken by the user. <br>
 * Time Count Starts when creating <b>instance for this class.</b> <br>
 * To get the time, should register the handler by using the method
 * <B>setHandler(Handler handler) </b> <br>
 * To stop the thread call <b>stopTimer()</b> method
 * 
 */
public class TimerCount {

	Timer timer;
	Date mStartTime;
	Date mCurrentTime;
	Date mTimeTaken;
	Handler mHandler;

	Calendar cal;
	SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

	public TimerCount() {
		cal = Calendar.getInstance();
		mStartTime = cal.getTime();
		timer = new Timer();
		timer.scheduleAtFixedRate(new RemindTask(), 1000, 1000);
	}

	class RemindTask extends TimerTask {
		public void run() {
			cal = Calendar.getInstance();
			mCurrentTime = cal.getTime();
			long diff = mCurrentTime.getTime() - mStartTime.getTime();

			double diffInHours = diff / ((double) 1000 * 60 * 60);
			int hours = (int) diffInHours;
			int minutes = (int) ((diffInHours - (int) diffInHours) * 60);
			int seconds = ((int) (diff / 1000));
			int secondsInMinute = seconds % 60;

			

			String time = appendZero(hours) + ":"+appendZero(minutes)+":"+appendZero(secondsInMinute);
			updatehandler(time);
		}
	}

	String appendZero(int time) {
		String tme;
		if (time < 10) {
			tme = "0" + time;
		} else {
			tme = time + "";
		}
		return tme;
	}

	void updatehandler(String time) {
		if (mHandler != null) {
			Message msg = new Message();
			msg.obj = time;
			msg.what = DataMembers.NOTIFY_CALL_ANALYSIS_TIMER;
			mHandler.sendMessage(msg);
		}
	}

	public void stopTimer() {
		timer.cancel(); // Terminate the timer thread
	}

	public void setHandler(Handler handler) {
		mHandler = handler;

	}

}
