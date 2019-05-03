package com.ivy.cpg.view.homescreen.deviceStatus;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.BatteryManager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class DeviceStatusActivity extends IvyBaseActivityNoActionBar {

	private ArrayList<RowItem> status;
	private String currentStatus = "";

	private NetworkInfo mWifi;
	private boolean enabled;
	private int signalstrength;
	private Intent callGPSSettingIntent;
	private SharedPreferences mLastSyncSharedPref;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_device_status);

		BusinessModel bmodel = (BusinessModel) getApplicationContext();
		bmodel.setContext(this);

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(this,
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			finish();
		}
		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		// Set title to toolbar
		getSupportActionBar().setTitle(
				getResources().getString(R.string.device_status));

		getSupportActionBar().setIcon(
				R.drawable.ic_action_ic_action_collections_view_as_grid1);
		// Used to on / off the back arrow icon
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		// Used to remove the app logo actionbar icon and set title as home
		// (title support click)
		getSupportActionBar().setDisplayShowHomeEnabled(true);
		// Used to hide the app logo icon from actionbar
		// getSupportActionBar().setDisplayUseLogoEnabled(false);
		mLastSyncSharedPref=getSharedPreferences("lastSync",MODE_PRIVATE);
		IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = getApplicationContext().registerReceiver(null,
				ifilter);

		int status1 = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
		currentStatus = String.valueOf(status1);

		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		enabled = service.isProviderEnabled(LocationManager.GPS_PROVIDER);

		status = updateListItem();

		// Check if enabled and if not send user to the GPS settings

		ListView lstvw = findViewById(R.id.devicestatusList);
		ListArrayAdapter adapter = new ListArrayAdapter(DeviceStatusActivity.this,
				R.layout.device_status_list_item, status);
		lstvw.setAdapter(adapter);
		lstvw.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View arg1, int pos,
									long arg3) {

				RowItem row = (RowItem) adapter.getItemAtPosition(pos);
				if (row.getTitle().equalsIgnoreCase(
						getResources().getString(R.string.gps))
						&& row.getDesc().equalsIgnoreCase(
						getResources().getString(R.string.off))) {
					callGPSSettingIntent = new Intent(
							android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
					startActivity(callGPSSettingIntent);
				} else if (row.getTitle().equalsIgnoreCase(
						getResources().getString(R.string.wifi))
						&& !mWifi.isConnected()) {
					startActivity(new Intent(
							android.provider.Settings.ACTION_WIFI_SETTINGS));

				}
			}

		});


	}

	class ListArrayAdapter extends ArrayAdapter<Object> {

		private Context context;
		private int ResourceId;
		private ArrayList<RowItem> status;

		public ListArrayAdapter(DeviceStatusActivity deviceStatusActivity,
				int deviceStatusListItem, ArrayList status) {
			super(deviceStatusActivity, deviceStatusListItem, status);
			context = deviceStatusActivity;
			ResourceId = deviceStatusListItem;
			this.status = status;
		}

		public int getCount() {
			return status.size();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			RowItem item = status.get(position);
			View row = convertView;
			ViewHolder holder;
			if (convertView == null) {

				LayoutInflater inflater = ((Activity) context)
						.getLayoutInflater();
				row = inflater.inflate(ResourceId, parent, false);

				holder = new ViewHolder();
				holder.desc = row.findViewById(R.id.desc);
				holder.title = row.findViewById(R.id.titletxt);
				holder.img = row.findViewById(R.id.icon);
				row.setTag(holder);
			} else
				holder = (ViewHolder) row.getTag();

			if (status.get(position).getTitle()
					.equalsIgnoreCase(getResources().getString(R.string.wifi)) && status
							.get(position)
							.getDesc()
							.equalsIgnoreCase(
									getResources().getString(R.string.no))
					|| status
							.get(position)
							.getTitle()
							.equalsIgnoreCase(
									getResources().getString(R.string.gps))
					&& status
							.get(position)
							.getDesc()
							.equalsIgnoreCase(
									getResources().getString(R.string.no))
					|| status
							.get(position)
							.getTitle()
							.equalsIgnoreCase(
									getResources().getString(
											R.string.network_status))
					&& status
							.get(position)
							.getDesc()
							.equalsIgnoreCase(
									getResources().getString(
											R.string.not_available))) {
				holder.desc.setText(status.get(position).getDesc());
				holder.desc.setTextColor(Color.RED);
			} else if (status.get(position).getTitle()
					.equalsIgnoreCase(getResources().getString(R.string.wifi))
					&& status
							.get(position)
							.getDesc()
							.equalsIgnoreCase(
									getResources().getString(R.string.yes))
					|| status
							.get(position)
							.getTitle()
							.equalsIgnoreCase(
									getResources().getString(R.string.gps))
					&& status
							.get(position)
							.getDesc()
							.equalsIgnoreCase(
									getResources().getString(R.string.yes))
					|| status
							.get(position)
							.getTitle()
							.equalsIgnoreCase(
									getResources().getString(
											R.string.network_status))
					&& status
							.get(position)
							.getDesc()
							.equalsIgnoreCase(
									getResources()
											.getString(R.string.available))) {
				holder.desc.setText(status.get(position).getDesc());
				holder.desc.setTextColor(Color.GREEN);

			} else {
				holder.desc.setText(status.get(position).getDesc());
				holder.desc.setTextColor(Color.BLACK);
			}
			holder.title.setText(status.get(position).getTitle());

			holder.img.setImageResource(status.get(position).getImageId());

			return row;

		}

		public class ViewHolder {
			TextView title, desc;
			ImageView img;

		}
	}

	RowItem rowitem;

	public ArrayList<RowItem> updateListItem() {
		status = new ArrayList();
		// if (bmodel.configurationMasterHelper.SHOW_DS_CURRENT_DATE)
		status.add(new RowItem(R.drawable.ic_time, getResources().getString(
				R.string.current_date), DateTimeUtils.convertFromServerDateToRequestedFormat(
				DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
				ConfigurationMasterHelper.outDateFormat)
				+ " "
				+ DateTimeUtils.now(DateTimeUtils.TIME)));
		// if (bmodel.configurationMasterHelper.SHOW_DS_BATTERY_STATUS)
		status.add(new RowItem(R.drawable.ic_battery, getResources().getString(
				R.string.battery_status), currentStatus + "%"));

		ConnectivityManager connManager = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
		mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		// if (bmodel.configurationMasterHelper.SHOW_DS_WIFI)
		if (mWifi.isConnected())
			status.add(new RowItem(R.drawable.ic_wifi, getResources()
					.getString(R.string.wifi), getResources().getString(
					R.string.on)));
		else
			status.add(new RowItem(R.drawable.ic_wifi, getResources()
					.getString(R.string.wifi), getResources().getString(
					R.string.off)));

		// if (bmodel.configurationMasterHelper.SHOW_DS_GPS)
		if (enabled)
			status.add(new RowItem(R.drawable.ic_gps, getResources().getString(
					R.string.gps), getResources().getString(R.string.on)));
		else
			status.add(new RowItem(R.drawable.ic_gps, getResources().getString(
					R.string.gps), getResources().getString(R.string.off)));

		// if (bmodel.configurationMasterHelper.SHOW_DS_NETWORK)
		if (isOnline())
			status.add(new RowItem(R.drawable.ic_network, getResources()
					.getString(R.string.network_status), getResources()
					.getString(R.string.available)));
		else
			status.add(new RowItem(R.drawable.ic_network, getResources()
					.getString(R.string.network_status), getResources()
					.getString(R.string.not_available)));
		// if (bmodel.configurationMasterHelper.SHOW_DS_SYNC_DATE)
		status.add(new RowItem(R.drawable.ic_sync, getResources().getString(
				R.string.last_sync), mLastSyncSharedPref.getString("date","")+" "+mLastSyncSharedPref.getString("time","")));

		TelephonyManager SignalManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

		PhoneStateListener signalListener = new PhoneStateListener() {
			public void onSignalStrengthChanged(int asu) {
				Commons.print("signal strength,"+ String.valueOf(asu));
				signalstrength = asu;
			}
		};
		SignalManager.listen(signalListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
		String signalstatus;
		if (signalstrength >= 0 && signalstrength < 15)
			signalstatus = getResources().getString(R.string.low);
		else if (signalstrength > 15)
			signalstatus = getResources().getString(R.string.high);
		else
			signalstatus = getResources().getString(R.string.medium);
		// if (bmodel.configurationMasterHelper.SHOW_DS_SIGNAL)
		status.add(new RowItem(R.drawable.ic_signal_strength, getResources()
				.getString(R.string.signal_strength), signalstatus));
		return status;

	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public class RowItem {
		private int imageId;
		private String title;
		private String desc;

		public RowItem(int imageId, String title, String desc) {
			this.imageId = imageId;
			this.title = title;
			this.desc = desc;
		}

		public RowItem() {
			// TODO Auto-generated constructor stub
		}

		public int getImageId() {
			return imageId;
		}

		public void setImageId(int imageId) {
			this.imageId = imageId;
		}

		public String getDesc() {
			return desc;
		}

		public void setDesc(String desc) {
			this.desc = desc;
		}

		public String getTitle() {
			return title;
		}

		public void setTitle(String title) {
			this.title = title;
		}

		@Override
		public String toString() {
			return title + "\n" + desc;
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			finish();
		}
		return super.onOptionsItemSelected(item);
	}

}
