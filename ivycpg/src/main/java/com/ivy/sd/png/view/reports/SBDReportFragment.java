package com.ivy.sd.png.view.reports;

import android.database.Cursor;
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
import com.ivy.sd.png.util.DataMembers;

import java.util.Collections;
import java.util.Vector;

public class SBDReportFragment extends Fragment {
	private BusinessModel bmodel;
	private View view;
	private ListView listview;
	private Vector<RetailerMasterBO> sbdreport;
	private TextView targetgptotal, actualgptotal, merchtotal, gstotal;
	private int mtargetgptotal = 0, mactualgptotal = 0, mmerchtotal = 0,
			mgstotal = 0;
	private TextView mtdtargetgptotal, mtdactualgptotal, mtdmerchtotal,
			mtdgstotal;
	private int mmtdtargetgptotal, mmtdactualgpttotal, mmtdmerchtotal,
			mmtdgstotal;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_sbd_report, container, false);
		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());
		mtargetgptotal = 0;
		mmtdtargetgptotal = 0;
		mmtdactualgpttotal = 0;
		mmtdmerchtotal = 0;
		mmtdgstotal = 0;
		mactualgptotal = 0;
		mmerchtotal = 0;
		mgstotal = 0;
		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}
		targetgptotal = (TextView) view.findViewById(R.id.targetgptotal);
		actualgptotal = (TextView) view.findViewById(R.id.actualgptotal);
		merchtotal = (TextView) view.findViewById(R.id.merchtotal);
		gstotal = (TextView) view.findViewById(R.id.gstotal);
		mtdtargetgptotal = (TextView) view.findViewById(R.id.mtdtargetgp);
		mtdactualgptotal = (TextView) view.findViewById(R.id.mtdactualgp);
		mtdmerchtotal = (TextView) view.findViewById(R.id.mtdmerchgp);
		mtdgstotal = (TextView) view.findViewById(R.id.mtdgstotal);
		listview = (ListView) view.findViewById(R.id.list);
		listview.setCacheColorHint(0);
		
		sbdreport = downloadSBDReport();
		Collections.sort(sbdreport, RetailerMasterBO.WalkingSequenceComparator);
		// Load listview.
		MyAdapter mSchedule = new MyAdapter(sbdreport);
		listview.setAdapter(mSchedule);
		for (int i = 0; i < sbdreport.size(); i++) {
			mtargetgptotal = mtargetgptotal
					+ sbdreport.get(i).getSbdDistributionTarget();
			mactualgptotal = mactualgptotal
					+ sbdreport.get(i).getSbdDistributionAchieve();
			mmerchtotal = mmerchtotal + sbdreport.get(i).getSBDMerchAchieved();
			mgstotal = mgstotal + sbdreport.get(i).getIsGoldStore();
		}
		targetgptotal.setText(mtargetgptotal + "");
		actualgptotal.setText(mactualgptotal + "");
		merchtotal.setText(mmerchtotal + "");
		gstotal.setText(mgstotal + "");

		mtdtargetgptotal.setText(mtargetgptotal + mmtdtargetgptotal + "");
		mtdactualgptotal.setText((mactualgptotal + mmtdactualgpttotal) + "");
		mtdmerchtotal.setText((mmerchtotal + mmtdmerchtotal) + "");

		mtdgstotal.setText((mgstotal + mmtdgstotal) + "");
		return view;
	}

	private Vector<RetailerMasterBO> downloadSBDReport() {
		int index = 1;
		Vector<RetailerMasterBO> retailer = new Vector<RetailerMasterBO>();
		RetailerMasterBO retailerbo, ret;
		ret = new RetailerMasterBO();
		DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
				DataMembers.DB_PATH);
		db.openDataBase();
		int sbdDistAchieved = 0;
//		Cursor c2 = db.selectSQL("SELECT COUNT(productId) FROM SbdDistributionMaster INNER JOIN SbdDistributionAchievedMaster ON gname = GrpName INNER JOIN RetailerMaster ON rid = RetailerID");
//		if (c2 != null) {
//			while (c2.moveToNext()) {
//				sbdDistAchieved = c2.getInt(0);
//			}
//			c2.close();
//		}
		Cursor c = db// distinct GrpName changed by lakshmanan as distinct productId
				.selectSQL("select A.RetailerID,RetailerName,isDeviated,(select count(distinct GrpName)"
						+" from SbdDistributionMaster where channelid = A.ChannelId) "
						+ "as sbdtgt,sbd_dist_achieve,RPS_Merch_Achieved,IsGoldStore,isToday,(select count (sbdid) " +
						" from SbdMerchandisingMaster where ChannelId = A.ChannelId and TypeListId=(select ListId from StandardListMaster where ListCode='MERCH')) as rpstgt, " +
						 "case when RC.weekno not null  then RC.WalkingSeq else 0 end as sequence, case when RC.Date = "+bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))+" then RC.WalkingSeq else 0 end as seq "+
						" from RetailerMaster A"
						+ " inner join retailermasterinfo RMI on A.retailerid = RMI.retailerid "
						+ " left join RetailerClientMappingMaster RC on RC.rid=A.RetailerID"
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
				retailerbo.setSbd_dist_target(c.getInt(3));
				retailerbo.setSbd_dist_achieve(c.getInt(4));

				retailerbo.setIsGoldStore(c.getInt(6));
				retailerbo.setIsToday(c.getInt(7));
				if (c.getInt(5) !=0 && (c.getInt(5) == c.getInt(8)))
					retailerbo.setSBDMerchAchieved(1);
				else
					retailerbo.setSBDMerchAchieved(0);
				retailer.add(retailerbo);

			}
		}
		
		Cursor c1 = db
				.selectSQL("select  tgt,ach,code,type from dashboardmaster where type='MONTH' and (code='SBD_MTD' or code='DSR_GOLDSTORE' or code='DSR_MERCH' or code='DSR_GOLDPOINTS')");
		if (c1 != null) {
			while (c1.moveToNext()) {
				if (c1.getString(2).equalsIgnoreCase("SBD_MTD")) {
					mmtdtargetgptotal = c1.getInt(0);
					mmtdactualgpttotal = c1.getInt(1);
				} if (c1.getString(2).equalsIgnoreCase("DSR_GOLDPOINTS")) {
					
				} else if (c1.getString(2).equalsIgnoreCase("DSR_GOLDSTORE")) {
					mmtdgstotal = c1.getInt(1);
				} else
					mmtdmerchtotal = c1.getInt(1);

			}
		}
		c.close();
		c1.close();
		db.closeDB();
		return retailer;
	}

	class MyAdapter extends ArrayAdapter<RetailerMasterBO> {
		Vector<RetailerMasterBO> items;

		private MyAdapter(Vector<RetailerMasterBO> items) {
			super(getActivity(), R.layout.row_sbd_report, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			RetailerMasterBO sbdreport = (RetailerMasterBO) items.get(position);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.row_sbd_report, parent, false);
				holder = new ViewHolder();
				holder.visitno = (TextView) row.findViewById(R.id.visitno);

				holder.storename = (TextView) row.findViewById(R.id.storename);
				holder.target_gp = (TextView) row.findViewById(R.id.target_gp);
				holder.actual_gp = (TextView) row.findViewById(R.id.actual_gp);
				holder.merch = (TextView) row.findViewById(R.id.merch);
				holder.gs = (TextView) row.findViewById(R.id.gs);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			if (sbdreport.getIsDeviated().equalsIgnoreCase("Y"))
				holder.visitno.setText("0");
			else
				holder.visitno.setText(sbdreport.getWalkingSequence()
						+ "".trim());
			holder.storename.setText(sbdreport.getRetailerName().trim());
			holder.target_gp.setText(sbdreport.getSbdDistributionTarget()
					+ "".trim());
			holder.actual_gp.setText(sbdreport.getSbdDistributionAchieve()
					+ "".trim());
			holder.merch.setText(sbdreport.getSBDMerchAchieved() + "".trim());
			holder.gs.setText(sbdreport.getIsGoldStore() + "".trim());
			return (row);
		}
	}
	
	

	class ViewHolder {
		TextView visitno, storename, target_gp, actual_gp, merch, gs;

	}
}
