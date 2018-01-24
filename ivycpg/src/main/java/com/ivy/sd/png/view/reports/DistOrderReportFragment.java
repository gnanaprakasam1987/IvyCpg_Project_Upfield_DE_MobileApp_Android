package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.reports.OrderReportBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class DistOrderReportFragment extends IvyBaseFragment implements OnClickListener,
		OnItemClickListener {

	private TextView totalOrderValue, averageLines, mlpc, mavg_pre_post,
			totalLines;
	private ListView lvwplist;
	private BusinessModel bmodel;
	private ArrayList<OrderReportBO> mylist;
	private View view;
	private OrderReportBO mSelectedReportBO;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_order_report, container,
				false);
		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}

		totalOrderValue = (TextView) view.findViewById(R.id.txttotal);
		averageLines = (TextView) view.findViewById(R.id.txtavglines);
		mavg_pre_post = (TextView) view.findViewById(R.id.txt_dist_pre_post);
		mlpc = (TextView) view.findViewById(R.id.lpc);
		totalLines = (TextView) view.findViewById(R.id.txttotallines);
		lvwplist = (ListView) view.findViewById(R.id.list);
		lvwplist.setCacheColorHint(0);
		lvwplist.setOnItemClickListener(this);

		mylist = bmodel.reportHelper.downloadDistributorOrderReport();
		updateOrderGrid();
		double avglinesorderbooking = bmodel.reportHelper
				.getavglinesfororderbooking("OrderHeader");
		if (bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {

			double totoutlets = bmodel.reportHelper
					.getorderbookingCount("OrderHeader");
			double result = avglinesorderbooking / totoutlets;
			String resultS = result + "";
			if (resultS.equals(getResources().getString(R.string.nan))) {
				averageLines.setText("" + 0);
			} else {
				averageLines.setText("" + SDUtil.roundIt(result, 2));
			}

		}
		if (bmodel.configurationMasterHelper.SHOW_TOTAL_LINES)
			totalLines.setText(avglinesorderbooking + "");

		if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
			view.findViewById(R.id.lbl_avg_lines).setVisibility(View.GONE);
			averageLines.setVisibility(View.GONE);
			// mlpc.setVisibility(View.GONE);
		}
		if (!bmodel.configurationMasterHelper.SHOW_TOTAL_LINES) {
			totalLines.setVisibility(View.GONE);
			view.findViewById(R.id.lbl_total_lines).setVisibility(View.GONE);

		}
		if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
			view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
			view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
			view.findViewById(R.id.dist).setVisibility(View.GONE);

		}

		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.outna).getTag()) != null)
				((TextView) view.findViewById(R.id.outna))
						.setText(bmodel.labelsMasterHelper.applyLabels(view
								.findViewById(R.id.outna).getTag()));
		} catch (Exception e) {
			Commons.printException(e);
		}
		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.lpc).getTag()) != null)
				((TextView) view.findViewById(R.id.lpc))
						.setText(bmodel.labelsMasterHelper.applyLabels(view
								.findViewById(R.id.lpc).getTag()));
		} catch (Exception e) {
			Commons.printException(e);
		}

		return view;

	}

	public void onClick(View comp) {

	}

	private void updateOrderGrid() {
		double totalvalue = 0;
		int pre = 0, post = 0;

		// Show alert if error loading data.
		if (mylist == null) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.unable_to_load_data),
					Toast.LENGTH_SHORT).show();
			return;
		}
		// Show alert if no order exist.
		if (mylist.size() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.no_orders_available),
					Toast.LENGTH_SHORT).show();
			return;
		}

		// Calculate the total order value.
		for (OrderReportBO ret : mylist) {
			totalvalue = totalvalue + ret.getOrderTotal();
		}

		if (bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
			// Calculate the total order value.
			for (OrderReportBO ret : mylist) {
				try {
					String str[] = ret.getDist().split("/");
					pre = pre + Integer.parseInt(str[0]);
					post = post + Integer.parseInt(str[1]);
				} catch (Exception e) {
					// TODO: handle exception
					Commons.printException(e);
				}

			}
			float preavg = 0, postavg = 0;
			if (mylist.size() > 0) {
				if (pre > 0) {
					preavg = (float) pre / (float) mylist.size();
				}
				if (post > 0) {
					postavg = (float) post / (float) mylist.size();
				}

				mavg_pre_post.setText(SDUtil.format(preavg, 1, 0) + "/"
						+ SDUtil.format(postavg, 1, 0));

			} else {
				mavg_pre_post.setText("0/0");
			}

		}
		// Format and set on the lable
		totalOrderValue.setText("" + bmodel.formatValue(totalvalue));

		// Load listview.
		MyAdapter mSchedule = new MyAdapter(mylist);
		lvwplist.setAdapter(mSchedule);

	}

	class MyAdapter extends ArrayAdapter<OrderReportBO> {
		ArrayList<OrderReportBO> items;

		private MyAdapter(ArrayList<OrderReportBO> items) {
			super(getActivity(), R.layout.row_order_report, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			OrderReportBO orderreport = (OrderReportBO) items
					.get(position);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater
						.inflate(R.layout.row_order_report, parent, false);
				holder = new ViewHolder();
				holder.tvwrname = (TextView) row.findViewById(R.id.PRDNAME);

				holder.tvwvalue = (TextView) row.findViewById(R.id.PRDMRP);
				holder.tvwlpc = (TextView) row.findViewById(R.id.PRDRP);
				holder.tvwDist = (TextView) row.findViewById(R.id.dist_txt);
				holder.tvOrderNo = (TextView) row.findViewById(R.id.orderno);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			// if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
			// holder.tvwlpc.setVisibility(View.GONE);
			// }
			if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
				holder.tvwDist.setVisibility(View.GONE);

			}

			holder.tvwrname.setText(orderreport.getDistributorName());
			holder.tvwvalue.setText(bmodel.formatValue((orderreport
					.getOrderTotal())) + "");
			holder.tvwlpc.setText(orderreport.getLPC());
			holder.tvwDist.setText(orderreport.getDist());
			holder.tvOrderNo.setText(orderreport.getOrderID());
			;

			if (orderreport.getUpload().equalsIgnoreCase("Y")) {
				holder.tvwrname.setTextColor(getResources().getColor(
						R.color.GREEN));
				holder.tvwvalue.setTextColor(getResources().getColor(
						R.color.GREEN));
				holder.tvwlpc.setTextColor(getResources().getColor(
						R.color.GREEN));
				holder.tvwDist.setTextColor(getResources().getColor(
						R.color.GREEN));
				holder.tvOrderNo.setTextColor(getResources().getColor(
						R.color.GREEN));

			} else {

				row.setBackgroundDrawable(getResources().getDrawable(
						R.drawable.list_selector));
			}
			return (row);
		}
	}

	class ViewHolder {
		String ref;// product id
		TextView tvwrname;
		TextView tvwvol, tvwvalue, tvwlpc, tvwDist;
		TextView tvOrderNo;

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		try {

			OrderReportBO ret = (OrderReportBO) mylist
					.get(arg2);
			bmodel.reportHelper.updateDistributor(ret.getDistributorId()+"");
			bmodel.productHelper
					.downloadDistributorProductsWithFiveLevelFilter("MENU_PS_STKORD");

			Intent orderreportdetail = new Intent();
			orderreportdetail.putExtra("OBJ",
					ret);
			orderreportdetail.putExtra("isFromOrder", true);
			orderreportdetail.putExtra("TotalValue", ret.getOrderTotal());
			orderreportdetail.putExtra("TotalLines", ret.getLPC());
			orderreportdetail.setClass(getActivity(), DistOrderreportdetail.class);
			startActivityForResult(orderreportdetail, 0);

			/*
			 * FragmentTransaction ft=getFragmentManager().beginTransaction();
			 * ft.replace(R.id.realtabcontent, new
			 * OrderReportDetailFragment(),"orderdetail");
			 * ft.addToBackStack(null); ft.commit();
			 */

		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
		}
	}

	public void onBackPressed() {
		// do something on back.
		return;
	}

	class XlsExport extends AsyncTask<Void, Void, Boolean> {

	//	private ProgressDialog progressDialogue;
	private AlertDialog.Builder builder;
		private AlertDialog alertDialog;
		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub
			super.onPreExecute();
			/*progressDialogue = ProgressDialog.show(getActivity(),
					DataMembers.SD, "Exporting orders...", true, false);*/
			builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, "Exporting orders...");
            alertDialog = builder.create();
            alertDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			// TODO Auto-generated method stub
			try {

			} catch (Exception e) {
				Commons.printException(e);
			}
			return Boolean.TRUE; // Return your real result here
		}

		@Override
		protected void onPostExecute(Boolean result) {
			// TODO Auto-generated method stub
			super.onPostExecute(result);

			alertDialog.dismiss();
		//	progressDialogue.dismiss();
			if (result)
				Toast.makeText(getActivity(), "Sucessfully Exported.",
						Toast.LENGTH_SHORT).show();
			else
				Toast.makeText(getActivity(), "Export Failed.",
						Toast.LENGTH_SHORT).show();
		}

	}

	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case 0:
			AlertDialog.Builder builder= new AlertDialog.Builder(getActivity())
					.setIcon(null)
					.setCancelable(false)
					.setTitle("Do you want to delete Invoice")
					.setPositiveButton(getResources().getString(R.string.ok),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int whichButton) {

								}
							})
					.setNegativeButton(
							getResources().getString(R.string.cancel),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
													int whichButton) {
								}
							});
			bmodel.applyAlertDialogTheme(builder);
			break;

		}
		return null;
	}

}
