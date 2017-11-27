package com.ivy.sd.png.view.reports;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class DashboardReportFragment extends IvyBaseFragment {
	private View view;
	private BusinessModel bmodel;
	private ListView lstView;
	private ArrayList<DashBoardBO> mylist;
	private ViewGroup.LayoutParams layout_qty;

	private LinearLayout layout_kpi,layout_kpi_footer;

	private TextView tvTemp1,tvTemp2,tvTemp3;

	@SuppressLint("InlinedApi")
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_dashboard_report, container, false);

		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}

		layout_qty = new LayoutParams(77, LayoutParams.MATCH_PARENT);

		layout_kpi = (LinearLayout) view
				.findViewById(R.id.layout_kpi);
		layout_kpi_footer = (LinearLayout) view
				.findViewById(R.id.layout_bottom_1);

		lstView = (ListView) view.findViewById(R.id.list);
		lstView.setCacheColorHint(0);



		bmodel.dashBoardHelper.loadSellerDashBoardReport(bmodel.userMasterHelper.getUserMasterBO().getUserid() + "");


		loadTitleView(layout_kpi);


		mylist = new ArrayList<DashBoardBO>();

		for (DashBoardBO temp : bmodel.dashBoardHelper.getMonthList()) {
			mylist.add(temp);
		}


		MyAdapter mSchedule = new MyAdapter(mylist);
		lstView.setAdapter(mSchedule);

		loadBottomViews(layout_kpi_footer);
		return view;

	}

	@Override
	public void onStart() {
		super.onStart();

	}

	private DashBoardBO monthList;

	private class MyAdapter extends ArrayAdapter<DashBoardBO> {
		private ArrayList<DashBoardBO> items;

		public MyAdapter(ArrayList<DashBoardBO> items) {
			super(getActivity(), R.layout.row_dashboard_report, items);
			this.items = items;
		}

		public DashBoardBO getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return items.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder;



			View row = convertView;
			//if (row == null) {
			LayoutInflater inflater = getActivity().getLayoutInflater();
			row = inflater.inflate(R.layout.row_dashboard_report, parent, false);
			holder = new ViewHolder();

			holder.tvMonth = (TextView) row
					.findViewById(R.id.tv_month);
			monthList = (DashBoardBO) items.get(position);






			Set kpiListwithValue = new HashSet();

			for (DashBoardBO kpiResultList : bmodel.dashBoardHelper.getDashBoardReportList()) {
				if (monthList.getMonthID().equals(kpiResultList.getMonthID())){
					kpiListwithValue = kpiResultList.getMonthKpiList().entrySet();
					break;
				}
			}



			for (DashBoardBO mKPI : bmodel.dashBoardHelper.getKpiList()) {

				Iterator mapiterator = kpiListwithValue.iterator();

				while (mapiterator.hasNext()) {
					Map.Entry mapEntry = (Map.Entry) mapiterator.next();
					String keyValue = (String) mapEntry.getKey();
					ArrayList<String> value = (ArrayList<String>) mapEntry.getValue();

					if(keyValue.equals(mKPI.getKpiTypeLovID()+"")){

						LinearLayout KPIlinearlayout = new LinearLayout(getContext());
						KPIlinearlayout.setOrientation(LinearLayout.VERTICAL);
						KPIlinearlayout.setBackgroundColor(getContext().getResources().getColor(R.color.light_grey));

						tvTemp1 = new TextView(getActivity());
						tvTemp1.setLayoutParams(layout_qty);
						tvTemp1.setTextAppearance(getActivity(),
								R.style.TextViewBold);
						tvTemp1.setPadding(0, 0, 0, 0);
						tvTemp1.setBackgroundColor(getResources().getColor(R.color.competitor_brand));
						tvTemp1.setText(SDUtil.format(Double.parseDouble(value.get(0)), 0, 0) + "");
						tvTemp1.setGravity(Gravity.CENTER);


						tvTemp2 = new TextView(getActivity());
						tvTemp2.setLayoutParams(layout_qty);
						tvTemp2.setTextAppearance(getActivity(),
								R.style.TextViewBold);
						tvTemp2.setBackgroundColor(getResources().getColor(R.color.GRAY));
						tvTemp2.setPadding(0, 0, 0, 0);
						tvTemp2.setText(SDUtil.format(Double.parseDouble(value.get(1)), 0, 0) + "");
						tvTemp2.setGravity(Gravity.CENTER);

						tvTemp3 = new TextView(getActivity());
						tvTemp3.setLayoutParams(layout_qty);
						tvTemp3.setTextAppearance(getActivity(),
								R.style.TextViewBold);
						tvTemp3.setPadding(0, 0, 0, 0);
						tvTemp3.setText(SDUtil.format(Double.parseDouble(value.get(2)), 0, 0) + "");
						tvTemp3.setGravity(Gravity.CENTER);

						KPIlinearlayout.addView(tvTemp1);
						KPIlinearlayout.addView(tvTemp2);
						KPIlinearlayout.addView(tvTemp3);

						((LinearLayout) row).addView(KPIlinearlayout);

						break;
					}

				}

			}



			row.setTag(holder);
			//} else {
			holder = (ViewHolder) row.getTag();
			//}

			holder.tvMonth.setText(monthList.getMonthName());




			return row;
		}
	}

	class ViewHolder {
		private TextView tvMonth;

		LinearLayout rowLayout;
	}



	/**
	 * Creates dynamic views for title view.
	 *
	 * @param kpiLayout
	 */
	private void loadTitleView(LinearLayout kpiLayout) {
		try{


			if (bmodel.dashBoardHelper.getKpiList() != null
					&& bmodel.dashBoardHelper.getKpiList().size() > 0) {
				for (int i = 0; i < bmodel.dashBoardHelper.getKpiList().size(); i++) {
					DashBoardBO obj = bmodel.dashBoardHelper.getKpiList().get(i);

					tvTemp1 = new TextView(getActivity());
					tvTemp1.setLayoutParams(layout_qty);
					tvTemp1.setGravity(Gravity.CENTER);
					tvTemp1.setTextAppearance(getActivity(),
							R.style.TextViewListTitle);
					tvTemp1.setBackgroundResource(R.drawable.list_title_bg);
					tvTemp1.setText(obj.getText() + "");

					kpiLayout.addView(tvTemp1);



				}
			}

		} catch (Exception e) {
		}

	}

	private void loadBottomViews(LinearLayout kpiFooterLayout) {
		try{


			if (bmodel.dashBoardHelper.getKpiList() != null
					&& bmodel.dashBoardHelper.getKpiList().size() > 0) {
				for (int i = 0; i < bmodel.dashBoardHelper.getKpiList().size(); i++) {
					DashBoardBO obj = bmodel.dashBoardHelper.getKpiList().get(i);

					LinearLayout KPIFooterlinearlayout = new LinearLayout(getActivity());
					KPIFooterlinearlayout.setOrientation(LinearLayout.VERTICAL);
					KPIFooterlinearlayout.setBackgroundColor(getActivity().getResources().getColor(R.color.light_grey));

					tvTemp1 = new TextView(getActivity());
					tvTemp1.setLayoutParams(layout_qty);
					tvTemp1.setTextAppearance(getActivity(),
							R.style.TextViewBold);
					tvTemp1.setPadding(0, 0, 0, 0);
					tvTemp1.setBackgroundColor(getResources().getColor(R.color.competitor_brand));
					//tvTemp1.setText(obj.getKpiTarget() + "");
					tvTemp1.setText(SDUtil.format(Double.parseDouble(obj.getKpiTarget()), 0, 0) + "");
					tvTemp1.setGravity(Gravity.CENTER);


					tvTemp2 = new TextView(getActivity());
					tvTemp2.setLayoutParams(layout_qty);
					tvTemp2.setTextAppearance(getActivity(),
							R.style.TextViewBold);
					tvTemp2.setBackgroundColor(getResources().getColor(R.color.GRAY));
					tvTemp2.setPadding(0, 0, 0, 0);
					//tvTemp2.setText(obj.getKpiAcheived() + "");
					tvTemp2.setText(SDUtil.format(Double.parseDouble(obj.getKpiAcheived()), 0, 0) + "");
					tvTemp2.setGravity(Gravity.CENTER);

					tvTemp3 = new TextView(getActivity());
					tvTemp3.setLayoutParams(layout_qty);
					tvTemp3.setTextAppearance(getActivity(),
							R.style.TextViewBold);
					tvTemp3.setPadding(0, 0, 0, 0);
					//tvTemp3.setText(SDUtil.roundIt(obj.getCalculatedPercentage(), 2) + "");
					tvTemp3.setText(SDUtil.format(Double.parseDouble(SDUtil.roundIt(obj.getCalculatedPercentage(), 2)), 0, 0) + "");
					tvTemp3.setGravity(Gravity.CENTER);

					KPIFooterlinearlayout.addView(tvTemp1);
					KPIFooterlinearlayout.addView(tvTemp2);
					KPIFooterlinearlayout.addView(tvTemp3);

					kpiFooterLayout.addView(KPIFooterlinearlayout);



				}
			}
		}
		catch (Exception e){}


	}

	private double kk(int id, int initID) {
		double aa = 0;
		return aa;

	}

}
