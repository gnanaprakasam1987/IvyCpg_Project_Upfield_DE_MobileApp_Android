package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TabWidget;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

public class OrderReportFrag extends Fragment {
	private FragmentTabHost tabHost;
	private BusinessModel bmodel;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.activity_order_report,
				container, false);
		// requestWindowFeature(Window.FEATURE_NO_TITLE);
		getActivity().getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		// getActivity().setContentView(R.layout.activity_order_report);

		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());

		// ScreenOrientation.setScreenOrientation((getActivity().getApplicationContext(),
		// bmodel.configurationMasterHelper.IS_DEVICE_ORIENTATION,
		// bmodel.configurationMasterHelper.DEVICE_ORIENTATION);

		/** TabHost will have Tabs */
		tabHost = (FragmentTabHost) rootView.findViewById(android.R.id.tabhost);
		tabHost.setup(getActivity(), getChildFragmentManager(),
				R.id.realtabcontent);

		/** Defining tab builder for Order report tab */
		tabHost.addTab(tabHost.newTabSpec("order").setIndicator("order"),
				OrderReportFragment.class, null);

		/** Defining tab builder for Order report tab */
		tabHost.addTab(tabHost.newTabSpec("P order").setIndicator("P order"),
				PreviousDayOrderReportFragment.class, null);

		/** Orange Theme **/

			TabWidget widget = tabHost.getTabWidget();
			for (int i = 0; i < widget.getChildCount(); i++) {
				View v = widget.getChildAt(i);

				// Look for the title view to ensure this is an indicator and
				// not a
				// divider.
				TextView tv = (TextView) v.findViewById(android.R.id.title);
				if (tv == null) {
					continue;
				}
				v.setBackgroundResource(R.drawable.tab_selection);
			}

		return rootView;
	}

	private View createTab(final String title) {
		final View tab = LayoutInflater.from(getActivity()).inflate(
				R.layout.tab, null);
		((TextView) tab.findViewById(R.id.tab_text)).setText(title);
		return tab;
	}

}
