package com.ivy.sd.png.view.reports;

import android.database.Cursor;
import android.database.SQLException;
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

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Collections;
import java.util.Vector;

public class VolumeReportFragment extends Fragment {
	private BusinessModel bmodel;
	private View view;
	private ListView listview;
	private Vector<RetailerMasterBO> volumereport;
	private TextView ap3mtotal, vtargettotal, vacturaltotal;
	private double ap3mtot, vtgttot, vacttot;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_volume_report, container,
				false);
		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}
		ap3mtotal = (TextView) view.findViewById(R.id.ap3mtotal);
		vtargettotal = (TextView) view.findViewById(R.id.vtargettotal);
		vacturaltotal = (TextView) view.findViewById(R.id.vactualtotal);
		listview = (ListView) view.findViewById(R.id.list);
		listview.setCacheColorHint(0);
		volumereport = downloadVolumeReport();
		Collections.sort(volumereport,
				RetailerMasterBO.WalkingSequenceComparator);
		// Load listview.
		MyAdapter mSchedule = new MyAdapter(volumereport);
		listview.setAdapter(mSchedule);
		ap3mtot = 0;
		vtgttot = 0;
		vacttot = 0;
		for (int i = 0; i < volumereport.size(); i++) {
			ap3mtot = ap3mtot + volumereport.get(i).getAp3m();
			vtgttot = (vtgttot + volumereport.get(i).getDaily_target_planned());
			vacttot = vacttot + volumereport.get(i).getVisit_Actual();

		}
		ap3mtotal.setText(bmodel.formatValue(ap3mtot) + "");
		vtargettotal.setText(bmodel.formatValue(vtgttot) + "");
		vacturaltotal.setText(bmodel.formatValue(vacttot) + "");
		return view;
	}

	private Vector<RetailerMasterBO> downloadVolumeReport() {
		int index = 1;
		Vector<RetailerMasterBO> retailer = new Vector<RetailerMasterBO>();
		try {
			RetailerMasterBO retailerbo, ret;
			ret = new RetailerMasterBO();
			DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			Cursor c = db
					.selectSQL("select RM.RetailerID,RetailerName,isDeviated,daily_target_planned, " +
							"when RC.weekno not null then RC.WalkingSeq when RC.Date= "+bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))+" then RC.WalkingSeq else 0 end as seq "+

							"from RetailerMaster RM"
							+ " inner join Retailermasterinfo RMI on RM.retailerid = RMI.retailerid"
							+ " left join RetailerClientMappingMaster RC on RC.rid=RM.RetailerID"
							+ " where RMI.isToday=1 or isDeviated='Y' order by seq");
			if (c != null) {
				while (c.moveToNext()) {
					retailerbo = new RetailerMasterBO();
					retailerbo.setRetailerID(c.getString(0));
					retailerbo.setRetailerName(c.getString(1));
					retailerbo.setIsDeviated(c.getString(2));
					if (!c.getString(2).equalsIgnoreCase("Y")) {
						retailerbo.setWalkingSequence(index);
						index++;
					} else
						retailerbo.setWalkingSequence(1000000);
					retailerbo.setDaily_target_planned(c.getDouble(3));
					retailer.add(retailerbo);

				}
				c.close();
			}
			if (bmodel.configurationMasterHelper.IS_INVOICE) {
				c = db.selectSQL("select Retailerid, sum(invNetamount) from InvoiceMaster group by retailerid");
			} else {
				c = db.selectSQL("select RetailerID, sum(OrderValue) from OrderHeader group by retailerid");
			}
			if (c != null) {
				while (c.moveToNext()) {
					for (int i = 0; i < retailer.size(); i++) {
						ret = retailer.get(i);
						if (ret.getRetailerID().equals(c.getString(0))) {
							ret.setVisit_Actual(c.getDouble(1));
						}
					}
				}
				c.close();
			}
			c = db.selectSQL("select retailerId,base_value from "
					+ DataMembers.tbl_DTPMaster);

			if (c != null) {
				while (c.moveToNext()) {
					for (int i = 0; i < retailer.size(); i++) {
						ret = retailer.get(i);
						if (ret.getRetailerID().equals(c.getString(0))) {
							ret.setAp3m(c.getDouble(1));
						}
					}
				}

			}
			c.close();
			db.closeDB();
		} catch (SQLException e) {
			Commons.printException(e);
		}
		return retailer;

	}

	class MyAdapter extends ArrayAdapter<RetailerMasterBO> {
		Vector<RetailerMasterBO> items;

		private MyAdapter(Vector<RetailerMasterBO> items) {
			super(getActivity(), R.layout.row_volume_report, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			RetailerMasterBO volumereport = (RetailerMasterBO) items
					.get(position);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.row_volume_report, parent,
						false);
				holder = new ViewHolder();
				holder.visitno = (TextView) row.findViewById(R.id.visitno);

				holder.storename = (TextView) row.findViewById(R.id.storename);
				holder.AP3M = (TextView) row.findViewById(R.id.av3m);
				holder.vtarget = (TextView) row.findViewById(R.id.vtarget);
				holder.vactual = (TextView) row.findViewById(R.id.vactual);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			if (volumereport.getIsDeviated().equalsIgnoreCase("Y"))
				holder.visitno.setText("0");
			else
				holder.visitno.setText(volumereport.getWalkingSequence()
						+ "".trim());
			holder.storename.setText(volumereport.getRetailerName().trim());
			holder.vtarget.setText(bmodel.formatValue(volumereport
					.getDaily_target_planned()) + "".trim());
			holder.vactual.setText(bmodel.formatValue(volumereport
					.getVisit_Actual()) + "".trim());
			holder.AP3M.setText(bmodel.formatValue(volumereport.getAp3m())
					+ "".trim());
			return (row);
		}
	}

	class ViewHolder {
		TextView visitno, storename, AP3M, vtarget, vactual;
	}
}
