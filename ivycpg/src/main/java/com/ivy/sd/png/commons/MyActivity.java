package com.ivy.sd.png.commons;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.util.Commons;

import java.util.Locale;

public class MyActivity extends FragmentActivity implements ApplicationConfigs {

	private TextView mScreenTitleTV;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// setContentView(R.layout.screen_title);

		mScreenTitleTV = (TextView) findViewById(R.id.title_tv);

		SharedPreferences sharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(this);

		Configuration config = new Configuration();
		Locale locale = config.locale;
		if (!Locale.getDefault().equals(
				sharedPrefs.getString("languagePref", LANGUAGE))) {
			locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0,2));
			Locale.setDefault(locale);
			config.locale = locale;
			getBaseContext().getResources().updateConfiguration(config,
					getBaseContext().getResources().getDisplayMetrics());
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		Commons.print("IVYBASEACTIVITY,"+ "ONREUSME CALLED");
		SDUtil.useNetworkProvidedValues(this);
	}

	public void setScreenTitle(String title) {
		if (mScreenTitleTV != null)
			if (title != null)
				mScreenTitleTV.setText(title);
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		// super.onBackPressed();
	}
}
