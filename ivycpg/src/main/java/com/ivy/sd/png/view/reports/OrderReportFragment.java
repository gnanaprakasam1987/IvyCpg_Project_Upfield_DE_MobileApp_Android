package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReportonorderbookingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.JExcelHelper;
import com.ivy.sd.png.provider.SalesReturnHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class OrderReportFragment extends Fragment implements OnClickListener,
		OnItemClickListener {

	private TextView totalOrderValue, averageLines, mlpc, mavg_pre_post,
			totalLines, tv_lbl_total_lines,totalvaluetitle,lab_dist_pre_post;
	private ListView lvwplist;
	private Button xlsExport;
	private BusinessModel bmodel;
	private ArrayList<ReportonorderbookingBO> mylist;
	private View view;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


		view = inflater.inflate(R.layout.fragment_order_report, container, false);
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
		tv_lbl_total_lines = (TextView) view.findViewById(R.id.lbl_total_lines);

		xlsExport = (Button) view.findViewById(R.id.btn_export);
		if(bmodel.configurationMasterHelper.IS_EXPORT_ORDER_REPORT){
			xlsExport.setVisibility(View.VISIBLE);
		}

		if(bmodel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL){
			xlsExport.setText(getResources().getString(R.string.export_and_email));
		}
		else if(bmodel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_SHARE){
			xlsExport.setText(getResources().getString(R.string.export_and_share));
		}
		else{
			xlsExport.setText(getResources().getString(R.string.export));
		}
		lvwplist = (ListView) view.findViewById(R.id.lvwplist);
		lvwplist.setCacheColorHint(0);
		xlsExport.setOnClickListener(this);

		lvwplist.setOnItemClickListener(this);
		totalvaluetitle=(TextView) view.findViewById(R.id.totalvaluetitle);
		lab_dist_pre_post=(TextView) view.findViewById(R.id.lab_dist_pre_post);
        mlpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
		totalvaluetitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
		lab_dist_pre_post.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
		mylist = bmodel.reportHelper.downloadOrderreport();
		updateOrderGrid();
		int avglinesorderbooking = bmodel.reportHelper
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
		if (bmodel.configurationMasterHelper.SHOW_TOTAL_LINES) {
			if (bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {
				int totalQty = 0;
				for (ReportonorderbookingBO bo : mylist)
					totalQty = totalQty + bmodel.reportHelper.getTotalQtyfororder(bo.getorderID());
				totalLines.setText(totalQty + "");
				tv_lbl_total_lines.setText(getResources().getString(R.string.tot_qty));
			} else {
				totalLines.setText(avglinesorderbooking + "");
				tv_lbl_total_lines.setText(getResources().getString(R.string.tot_line));
			}

		}
		//xlsExport.setVisibility(View.GONE);

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
			view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.VISIBLE);
			view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.VISIBLE);
			view.findViewById(R.id.dist).setVisibility(View.VISIBLE);
            ((TextView) view.findViewById(R.id.outna)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));



		}
		if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
			view.findViewById(R.id.weighttitle).setVisibility(View.GONE);
		else {
			try {
				if (bmodel.labelsMasterHelper.applyLabels(getActivity().findViewById(
						R.id.weighttitle).getTag()) != null)
					((TextView) getActivity().findViewById(R.id.weighttitle))
							.setText(bmodel.labelsMasterHelper
									.applyLabels(getActivity().findViewById(R.id.weighttitle)
											.getTag()));
				((TextView) view.findViewById(R.id.weighttitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
			} catch (Exception e) {
				Commons.printException(e);
			}
		}
		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.weighttitle).getTag()) != null)
				((TextView) view.findViewById(R.id.weighttitle))
						.setText(bmodel.labelsMasterHelper.applyLabels(view
								.findViewById(R.id.weighttitle).getTag()));
		} catch (Exception e) {
			Commons.printException(e);
		}


		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.outna).getTag()) != null)
				((TextView) view.findViewById(R.id.outna))
						.setText(bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.outna).getTag()));
            ((TextView) view.findViewById(R.id.outna)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        } catch (Exception e) {
			Commons.printException(e);
		}
		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.lpc).getTag()) != null)
				((TextView) view.findViewById(R.id.lpc))
						.setText(bmodel.labelsMasterHelper.applyLabels(view
								.findViewById(R.id.lpc).getTag()));
			((TextView) view.findViewById(R.id.lpc)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

		} catch (Exception e) {
			Commons.printException(e);
		}
		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.outid).getTag()) != null)
				((TextView) view.findViewById(R.id.outid))
						.setText(bmodel.labelsMasterHelper
								.applyLabels(view.findViewById(
										R.id.outid)
										.getTag()));
            ((TextView) view.findViewById(R.id.outid)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        } catch (Exception e) {
			Commons.printException(e);
		}

		try {
			if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
					R.id.lab_total_value).getTag()) != null)
				((TextView) view.findViewById(R.id.lab_total_value))
						.setText(bmodel.labelsMasterHelper
								.applyLabels(view.findViewById(
										R.id.lab_total_value)
										.getTag()));
		} catch (Exception e) {
			Commons.printException(e);
		}

		if (!bmodel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
			totalOrderValue.setVisibility(View.GONE);
			totalvaluetitle.setVisibility(View.GONE);
		}

		return view;

	}


	public void onClick(View comp) {
		Button vw = (Button) comp;
		if (vw == xlsExport) {
			new XlsExport().execute();
		}

	}

	private void updateOrderGrid() {
		double totalvalue = 0;
		int pre = 0, post = 0;

		// Show alert if error loading data.
		if (mylist == null) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.unable_to_load_data),
					Toast.LENGTH_SHORT).show();
			xlsExport.setVisibility(View.GONE);
			return;
		}
		// Show alert if no order exist.
		if (mylist.size() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.no_orders_available),
					Toast.LENGTH_SHORT).show();
			xlsExport.setVisibility(View.GONE);
			return;
		}

		// Calculate the total order value.
		for (ReportonorderbookingBO ret : mylist) {
			totalvalue = totalvalue + SDUtil.convertToDouble(SDUtil.format(ret.getordertot(),
					bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
					0, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
		}

		if (bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
			// Calculate the total order value.
			for (ReportonorderbookingBO ret : mylist) {
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
		if (!bmodel.configurationMasterHelper.SHOW_NETAMOUNT_IN_REPORT)
			totalOrderValue.setText(SDUtil.format(totalvalue,
					bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
					bmodel.configurationMasterHelper.VALUE_COMMA_COUNT, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));
		else
			totalOrderValue.setText(SDUtil.format(getTotValues() - SalesReturnHelper.getInstance(getActivity()).getTotalSalesReturnValue(),
					bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT,
					bmodel.configurationMasterHelper.VALUE_COMMA_COUNT, bmodel.configurationMasterHelper.IS_DOT_FOR_GROUP));

		// Load listview.
		MyAdapter mSchedule = new MyAdapter(mylist);
		lvwplist.setAdapter(mSchedule);

	}

	class MyAdapter extends ArrayAdapter<ReportonorderbookingBO> {
		ArrayList<ReportonorderbookingBO> items;

		private MyAdapter(ArrayList<ReportonorderbookingBO> items) {
			super(getActivity(), R.layout.row_order_report, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			ReportonorderbookingBO orderreport = (ReportonorderbookingBO) items
					.get(position);
			View row = convertView;

			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater
						.inflate(R.layout.row_order_report, parent, false);
				holder = new ViewHolder();
				holder.tvwrname = (TextView) row.findViewById(R.id.PRDNAME);
                holder.ordertxt = (TextView) row.findViewById(R.id.ordertxt);

				holder.tvFocusBrandCount = (TextView) row.findViewById(R.id.focus_brand_count);
				holder.tvMustSellCount = (TextView) row.findViewById(R.id.mustsell_count);

				holder.tvwvalue = (TextView) row.findViewById(R.id.PRDMRP);
				holder.tvwlpc = (TextView) row.findViewById(R.id.PRDRP);
				holder.tvwDist = (TextView) row.findViewById(R.id.dist_txt);
				holder.tvOrderNo = (TextView) row.findViewById(R.id.orderno);
				holder.tvWeight = (TextView) row.findViewById(R.id.tv_weight);
				holder.weighttitle=(TextView) row.findViewById(R.id.weighttitle);
				holder.tv_seller_type = (TextView) row.findViewById(R.id.tv_seller_type);
				holder.tvlpc=(TextView) row.findViewById(R.id.lpc);
				holder.tvoutid=(TextView) row.findViewById(R.id.outid);
				holder.focusbrandlabel=(TextView) row.findViewById(R.id.focusbrand_label);
				holder.mustselllabel=(TextView) row.findViewById(R.id.mustsell_label);
				holder.focus_brand_count1=(TextView) row.findViewById(R.id.focus_brand_count1);
				holder.mustsellcount=(TextView) row.findViewById(R.id.mustsellcount);
				((View) row.findViewById(R.id.invoiceview_doted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

				if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
					holder.tvWeight.setVisibility(View.VISIBLE);
				holder.weighttitle.setVisibility(View.VISIBLE);
				if (!bmodel.configurationMasterHelper.IS_FOCUSBRAND_COUNT_IN_REPORT)
					holder.tvFocusBrandCount.setVisibility(View.GONE);
				//holder.focus_brand_count1.setVisibility(View.GONE);
				if (!bmodel.configurationMasterHelper.IS_MUSTSELL_COUNT_IN_REPORT)
					holder.tvMustSellCount.setVisibility(View.GONE);
			//	holder.mustsellcount.setVisibility(View.GONE);


				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			if (!bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
				holder.tvwDist.setVisibility(View.VISIBLE);

			}

			holder.tvwrname.setText(orderreport.getretailerName());
            holder.tvwrname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
			holder.tvwvalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
            holder.ordertxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tvOrderNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
			holder.tvFocusBrandCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
			holder.tvMustSellCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
			holder.tvFocusBrandCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
			holder.tvlpc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
			holder.weighttitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
			holder.tvoutid.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
			holder.focusbrandlabel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
			holder.mustselllabel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


			try {
				if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(R.id.mustsell_count).getTag()) != null)
				{
					holder.tvMustSellCount.setText(bmodel.labelsMasterHelper.applyLabels(row
							.findViewById(R.id.mustsell_count).getTag()) + " : " + orderreport.getmMustSellCount());
				} else {
					holder.tvMustSellCount.setText(getResources().getString(R.string.must_sell) + " : " + orderreport.getmMustSellCount());
					holder.mustsellcount.setText(" " + " " + orderreport.getmMustSellCount());

				}
				if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
						R.id.focus_brand_count).getTag()) != null) {
					holder.tvFocusBrandCount.setText(bmodel.labelsMasterHelper.applyLabels(row
							.findViewById(R.id.focus_brand_count).getTag()) + " : " + orderreport.getmFocusBrandCount());
				} else {
					holder.tvFocusBrandCount.setText(getResources().getString(R.string.focus_brand) + " : " + orderreport.getmFocusBrandCount());
					holder.focus_brand_count1.setText(" " + " " + orderreport.getmFocusBrandCount());

				}

				if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
					holder.tvwvalue.setVisibility(View.GONE);
				}
			} catch (Exception e) {
				Commons.printException(e);
			}

		//	holder.focus_brand_count1.setText(orderreport.getmFocusBrandCount());
		//	holder.mustsellcount.setText(orderreport.getmMustSellCount());
			holder.tvwvalue.setText(bmodel.formatValue((orderreport
					.getordertot())) + "");
			holder.tvwlpc.setText(orderreport.getlpc());
			holder.tvwDist.setText(orderreport.getDist());
			holder.tvOrderNo.setText(orderreport.getorderID());
			holder.tvWeight.setText(orderreport.getWeight() + "");


			if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
				if (orderreport.getIsVanSeller() == 1)
					holder.tv_seller_type.setText("V");
				else
					holder.tv_seller_type.setText("P");
			} else {
				holder.tv_seller_type.setVisibility(View.INVISIBLE);
			}

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

		/*	TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
			if (position % 2 == 0) {
				row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
			} else {
				row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
			}*/
			return (row);
		}
	}

	class ViewHolder {
		String ref;// product id
		TextView tvwrname,ordertxt;
		TextView tvwvol, tvwvalue, tvwlpc, tvwDist, tvWeight,tvlpc,tvoutid,focus_brand_count1,mustsellcount;
		TextView tvOrderNo, tvFocusBrandCount, tvMustSellCount, tv_seller_type,weighttitle,focusbrandlabel,mustselllabel;

	}

	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		// TODO Auto-generated method stub
		try {
			ReportonorderbookingBO ret = (ReportonorderbookingBO) mylist
					.get(arg2);
			Intent orderreportdetail = new Intent();
			orderreportdetail.putExtra("OBJ",
					ret);
			orderreportdetail.putExtra("isFromOrder", true);
			orderreportdetail.putExtra("TotalValue", ret.getordertot());
			orderreportdetail.putExtra("TotalLines", ret.getlpc());
			orderreportdetail.setClass(getActivity(), Orderreportdetail.class);
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

		private AlertDialog.Builder builder;
		private AlertDialog alertDialog;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			builder = new AlertDialog.Builder(getActivity());

			bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.exporting_orders));
			alertDialog = builder.create();
			alertDialog.show();
		}

		@Override
		protected Boolean doInBackground(Void... params) {
			try {

				ArrayList<String> columnNames = new ArrayList<String>();
				columnNames.add("Distributor");
				columnNames.add("UserCode");
				columnNames.add("UserName");
				columnNames.add("RetailerCode");
				columnNames.add("RetailerName");
				columnNames.add("OrderNo");
				columnNames.add("OrderDate");
				columnNames.add("SKUCode");
				columnNames.add("SKUDescription");
				columnNames.add("OrderQty(Piece)");
				columnNames.add("OrderQty(Case)");
				columnNames.add("OrderQty(Outer)");
				columnNames.add("DeliveryDate");

				bmodel.reportHelper
						.downloadOrderReportToExport();
				HashMap<String,ArrayList<ArrayList<String>>> mOrderDetailsByDistributorName = bmodel.reportHelper
						.getmOrderDetailsByDistributorName();


				for(String distributorName:mOrderDetailsByDistributorName.keySet()){

					ArrayList<JExcelHelper.ExcelBO> mExcelBOList = new ArrayList<>();
					JExcelHelper.ExcelBO excel = bmodel.mJExcelHelper.new ExcelBO();
					excel.setSheetName(distributorName);
					excel.setColumnNames(columnNames);
					excel.setColumnValues(mOrderDetailsByDistributorName.get(distributorName));
					mExcelBOList.add(excel);
					bmodel.mJExcelHelper.createExcel("OrderReport_"+distributorName+".xls",mExcelBOList);
				}

				if(bmodel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL)
				bmodel.reportHelper.downloadOrderEmailAccountCredentials();


			} catch (Exception e) {
				Commons.printException(e);
				return Boolean.FALSE;
			}
			return Boolean.TRUE;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);


			alertDialog.dismiss();
			if (result) {
				Toast.makeText(getActivity(), getResources().getString(R.string.successfully_exported),
						Toast.LENGTH_SHORT).show();

				try {

					if(bmodel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_SHARE) {
						Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);

						ArrayList<Uri> uriList=new ArrayList<>();
						for(String distributorName:bmodel.reportHelper
								.getmOrderDetailsByDistributorName().keySet()){
							File newFile = new File(getActivity().getExternalFilesDir(null) + "", "OrderReport_"+distributorName+".xls");
							uriList.add(Uri.fromFile(newFile));
						}

						sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);

						sharingIntent.setType("application/excel");
						sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
						sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
						//sharingIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{""});

						startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_order_report_using)));
					}
					else if(bmodel.configurationMasterHelper.IS_ORDER_REPORT_EXPORT_AND_EMAIL){

						if(bmodel.reportHelper.getUserName()!=null&&!bmodel.reportHelper.getUserName().equals("")
								&&bmodel.reportHelper.getUserPassword()!=null&&!bmodel.reportHelper.getUserPassword().equals(""))
							  new SendMail(getActivity()
									  ,"Order Report","PFA").execute();
						else
							Toast.makeText(getActivity(),getResources().getString(R.string.invalid_credentials_mail_not_sent), Toast.LENGTH_LONG).show();


					}
				}
				catch (Exception ex){
					Commons.printException(ex);
				}
			}
			else
				Toast.makeText(getActivity(), getResources().getString(R.string.export_failed),
						Toast.LENGTH_SHORT).show();



		}

	}

	public class SendMail extends AsyncTask<Void, Void, Boolean> {

		Session session;

		Context mContext;
		private String subject;
		private String body;


		ProgressDialog progressDialog;

		public SendMail(Context ctx,  String subject, String message) {
			this.mContext = ctx;

			this.subject = subject;
			this.body = message;


		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();

			progressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.sending_email), getResources().getString(R.string.please_wait_some_time), false);
		}

		@Override
		protected Boolean doInBackground(Void... voids) {

			Properties props = System.getProperties();// new Properties();

			//Configuring properties for gmail
			props.put("mail.smtp.host", "smtp.gmail.com");
			props.put("mail.smtp.socketFactory.port", "587");
			props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
			props.put("mail.smtp.auth", "true");
			props.put("mail.smtp.port", "587");
			props.put("mail.smtp.starttls.enable", "true");
			//  props.put("mail.debug",true);

			//Creating a new session
			session = Session.getDefaultInstance(props,
					new javax.mail.Authenticator() {
						//Authenticating the password
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(bmodel.reportHelper.getUserName(), bmodel.reportHelper.getUserPassword());
						}
					});

			try {

				// sendind distributor wise..
				for(String distributorName:bmodel.reportHelper
						.getmOrderDetailsByDistributorName().keySet()) {

					//not allowed if email not available
					if (bmodel.reportHelper.getmEmailIdByDistributorName().get(distributorName) != null) {

						Message message = new MimeMessage(session);
						message.setFrom(new InternetAddress(bmodel.reportHelper.getUserName()));
						message.setRecipient(Message.RecipientType.TO, new InternetAddress(bmodel.reportHelper.getmEmailIdByDistributorName().get(distributorName)));
						message.setSubject(subject);
						message.setText(body);
						//  mm.setContent(message,"text/html; charset=utf-8");

						BodyPart bodyPart = new MimeBodyPart();
						bodyPart.setText(body);//Content(message,"text/html");

						//Attachment
						DataSource source = new FileDataSource(getActivity().getExternalFilesDir(null) + "/"+ "OrderReport_"+distributorName+".xls");
						bodyPart.setDataHandler(new DataHandler(source));
						bodyPart.setFileName("OrderReport_"+distributorName+".xls");

						MimeMultipart multiPart = new MimeMultipart();
						multiPart.addBodyPart(bodyPart);
						message.setContent(multiPart);

						Thread.currentThread().setContextClassLoader(getActivity().getClassLoader());

						MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
						mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
						mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
						mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
						mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
						mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

						//sending mail
						Transport.send(message);
					}
				}
			} catch (Exception ex) {
				Commons.printException(ex);
				return false;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean isSent) {
			super.onPostExecute(isSent);

			progressDialog.dismiss();

			if(isSent){
				Toast.makeText(getActivity(), getResources().getString(R.string.email_sent),
						Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(getActivity(), getResources().getString(R.string.error_in_sending_email),
						Toast.LENGTH_SHORT).show();
			}
		}
	}


	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case 0:
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
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

	private double getTotValues() {
		try {
			DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			Cursor c = db.selectSQL("select sum(ordervalue)from "
					+ DataMembers.tbl_orderHeader + " where  upload='N'");
			if (c != null) {
				if (c.moveToNext()) {
					double i = c.getDouble(0);
					c.close();
					db.closeDB();
					return i;
				}
			}
			c.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}

		return 0;
	}
}
