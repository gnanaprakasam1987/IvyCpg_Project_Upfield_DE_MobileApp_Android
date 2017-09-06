package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

public class DSRMTDReportFragment extends Fragment {
	private BusinessModel bmodel;
	private View view;
	private ListView lvwplist;
	private Vector<DashBoardBO> dsrmtdlist;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_dsr_mtd_report, container,
				false);
		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}
		lvwplist = (ListView) view.findViewById(R.id.lvwplist);
		lvwplist.setCacheColorHint(0);
		dsrmtdlist = bmodel.dashBoardHelper.downloadDSRMTD();
		// Load listview.
		MyAdapter mSchedule = new MyAdapter(dsrmtdlist);
		lvwplist.setAdapter(mSchedule);
		return view;
	}

	class MyAdapter extends ArrayAdapter<DashBoardBO> {
		Vector<DashBoardBO> items;

		private MyAdapter(Vector<DashBoardBO> items) {
			super(getActivity(), R.layout.row_dsr_mtd_report, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			DashBoardBO dsrmtdreport = (DashBoardBO) items.get(position);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.row_dsr_mtd_report, parent,
						false);
				holder = new ViewHolder();
				holder.text = (TextView) row.findViewById(R.id.text);
				holder.ap3m = (TextView) row.findViewById(R.id.ap3m);
				holder.target = (TextView) row.findViewById(R.id.target);
				holder.ach = (TextView) row.findViewById(R.id.ach);
				holder.index = (TextView) row.findViewById(R.id.index);
				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			holder.text.setText(dsrmtdreport.getText());
			holder.ap3m
					.setText(bmodel.formatValue(dsrmtdreport.getAp3m()) + "");
			holder.target.setText(bmodel.formatValue(dsrmtdreport.getTarget())
					+ "");
			holder.ach.setText(bmodel.formatValue(dsrmtdreport.getAcheived())
					+ "");
			if (dsrmtdreport.getTarget() != 0)
				holder.index.setText(bmodel.formatPercent((dsrmtdreport
						.getAcheived() / dsrmtdreport.getTarget()) * 100) + "");
			else
				holder.index.setText("0");
			return (row);
		}
	}

	class ViewHolder {
		TextView text, ach, target, ap3m, index;

	}
}
