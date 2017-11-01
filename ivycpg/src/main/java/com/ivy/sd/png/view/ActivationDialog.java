package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ActivationBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.List;

public class ActivationDialog extends Dialog implements OnClickListener {
	private BusinessModel bmodel;
	private Button add, close;
	private OnDismissListener addBatch;
	private Activity activity;
	private ListView listView;
	private ActivationListViewAdapter adapter;

	public ActivationDialog(Context context) {
		super(context);
	}

	public ActivationDialog(Activity activity, OnDismissListener addBatch) {
		super(activity);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		this.activity = activity;
		this.addBatch = addBatch;
		setContentView(R.layout.dialog_activation);
		setCancelable(true);
		bmodel = (BusinessModel) activity.getApplicationContext();
		TextView title = (TextView) findViewById(R.id.title);
		title.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));
		add = (Button) findViewById(R.id.add);
		add.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
		add.setOnClickListener(this);
		close = (Button) findViewById(R.id.close);
		close.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
		close.setOnClickListener(this);
		listView = (ListView) findViewById(R.id.lvwplist);
		adapter = new ActivationListViewAdapter(
				bmodel.activationHelper.getAppUrls());
		listView.setAdapter(adapter);
	}

	@Override
	public void onClick(View v) {

		int id = v.getId();
		if (id == R.id.add) {
			if (isAtleastOneRadioSelected()) {
				addBatch.onDismiss(ActivationDialog.this);
			} else
				Toast.makeText(activity, R.string.please_select_item,
						Toast.LENGTH_SHORT).show();
		} else if (id == R.id.close) {
			ActivationDialog.this.dismiss();
		}
	}

	ActivationBO activationObj;

	private class ActivationListViewAdapter extends ArrayAdapter<ActivationBO> {

		private List<ActivationBO> items;

		public ActivationListViewAdapter(List<ActivationBO> items) {
			super(activity, R.layout.row_activation_dialog, items);
			this.items = items;
		}

		public ActivationBO getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return items.size();
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			final ViewHolder holder;
			activationObj = (ActivationBO) items.get(position);

			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = activity.getLayoutInflater();
				row = inflater.inflate(R.layout.row_activation_dialog, parent,
						false);
				holder = new ViewHolder();
				holder.environmentRadioBtn = (RadioButton) row
						.findViewById(R.id.environmentRadioBtn);
				holder.environmentRadioBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
				holder.environmentRadioBtn
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								for (ActivationBO bo : items)
									bo.setChecked(false);
								if (((RadioButton) v).isChecked())
									holder.activationBO.setChecked(true);
								else
									holder.activationBO.setChecked(false);
								adapter.notifyDataSetChanged();
							}
						});

				row.setTag(holder);

			} else {
				holder = (ViewHolder) row.getTag();
			}
			holder.activationBO = activationObj;
			holder.environmentRadioBtn.setText(""+holder.activationBO.getEnviroinment());

			holder.environmentRadioBtn.setChecked(holder.activationBO
					.isChecked());

			return (row);
		}

	}

	protected boolean isAtleastOneRadioSelected() {
		for (ActivationBO bo : bmodel.activationHelper.getAppUrls()) {
			if (bo.isChecked() == true) {
				SharedPreferences.Editor editor = PreferenceManager
						.getDefaultSharedPreferences(activity).edit();
				editor.putString("appUrlNew",bo.getUrl());
				editor.putString("application", bo.getEnviroinment());
				editor.commit();
				return true;
			}
		}
		return false;
	}

	class ViewHolder {
		ActivationBO activationBO;
		RadioButton environmentRadioBtn;
	}

}
