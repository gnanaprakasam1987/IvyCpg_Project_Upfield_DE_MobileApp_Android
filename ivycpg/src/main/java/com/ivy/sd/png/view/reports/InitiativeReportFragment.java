package com.ivy.sd.png.view.reports;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
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
import com.ivy.sd.png.bo.InitiativeHolder;
import com.ivy.sd.png.bo.InitiativeReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class InitiativeReportFragment extends Fragment {
	private View view;
	private BusinessModel bmodel;
	private ListView lv;

	private LinearLayout lableTopLayout, totalLayout, totalMTDLayout,
			totalDistLayout;

	private ViewGroup.LayoutParams linearlprams1, linearlprams2;
	private int pixels;

	private TextView storeTitle;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_initiative_report, container,
				false);
		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());

		lableTopLayout = (LinearLayout) view.findViewById(R.id.header);
		totalDistLayout = (LinearLayout) view
				.findViewById(R.id.total_dist_layout);
		totalLayout = (LinearLayout) view.findViewById(R.id.total_layout);
		totalMTDLayout = (LinearLayout) view
				.findViewById(R.id.total_mtd_layout);
		storeTitle = (TextView) view.findViewById(R.id.store_title);

		DisplayMetrics metrics = getActivity().getResources()
				.getDisplayMetrics();
		float dp = 2f;
		float fpixels = metrics.density * dp;
		pixels = (int) (fpixels + 0.5f);

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}
		linearlprams2 = new LayoutParams((int) getResources().getDimension(
				R.dimen.product_name_twenty_character),
				LayoutParams.MATCH_PARENT);

		loadTitleView(lableTopLayout);

		lv = (ListView) view.findViewById(R.id.list);
		lv.setCacheColorHint(0);
		try {

			loadTotalView(totalLayout);
			loadMTDTotalView(totalMTDLayout);
			loadTotalDistributionView(totalDistLayout);
			MyAdapter adapter = new MyAdapter(
					bmodel.initiativeHelper.getInitlist());
			lv.setAdapter(adapter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			Commons.printException(e);
		}
		return view;
	}

	/**
	 * Adapter for Initiative hit
	 * 
	 */

	InitiativeReportBO initreport;

	class MyAdapter extends ArrayAdapter<InitiativeReportBO> {
		Vector<InitiativeReportBO> items;
		int size;

		private MyAdapter(Vector<InitiativeReportBO> items) {
			super(getActivity(), R.layout.row_initiative_report, items);
			this.items = items;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;

			initreport = (InitiativeReportBO) items.get(position);
			View row = convertView;

			try {
				if (row == null) {
					LayoutInflater inflater = getActivity().getLayoutInflater();
					row = inflater.inflate(R.layout.row_initiative_report,
							parent, false);
					holder = new ViewHolder();
					holder.visitno = (TextView) row.findViewById(R.id.visitno);

					holder.storename = (TextView) row
							.findViewById(R.id.storename);

					// holder.rowLayout = (LinearLayout) row;
					if (initreport.getInitiativeHit() != null
							&& initreport.getInitiativeHit().length > 0) {

						for (int k = 0; k < initreport.getInitiativeHit().length; k++) {

							TextView initiativeHitTv = new TextView(
									getActivity());
							initiativeHitTv.setLayoutParams(linearlprams2);
							initiativeHitTv.setGravity(Gravity.CENTER_VERTICAL
									| Gravity.RIGHT);
							initiativeHitTv.setTextAppearance(getActivity(),
									R.style.TextViewListTitle);
							initiativeHitTv
									.setHeight((int) getResources()
											.getDimension(
													R.dimen.list_view_height_volume_report));
							initiativeHitTv.setTextSize(R.dimen.font_small);
							initiativeHitTv.setTypeface(null, Typeface.BOLD);
							initiativeHitTv.setPadding(0, 0, pixels, 0);
							initiativeHitTv.setText(bmodel.initiativeHelper
									.getInitativeList().get(k) + "");
							initiativeHitTv.setSingleLine();
							((LinearLayout) row).addView(initiativeHitTv);
						}
					}

					row.setTag(holder);
				} else {
					holder = (ViewHolder) row.getTag();
				}
				holder.initbo = initreport;
				holder.rowLayout = (LinearLayout) row;
				if (initreport.getIsdeviated().equalsIgnoreCase("Y"))
					holder.visitno.setText("0");
				else
					holder.visitno
							.setText(initreport.getWalkingSequence() + "");
				holder.storename.setText(initreport.getRetailername());

				if (holder.initbo.getInitiativeHit() != null
						&& holder.initbo.getInitiativeHit().length > 0) {

					holder.childIndex = 2;
					for (holder.k = 0; holder.k < holder.initbo
							.getInitiativeHit().length; holder.k++) {
						((TextView) (holder.rowLayout)
								.getChildAt(holder.childIndex))
								.setText((holder.initbo.getInitiativeHit()[holder.k])
										+ "");
						holder.childIndex++;
					}
				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				Commons.printException(e);
			}

			return (row);
		}
	}

	/**
	 * View Holder object for Initiative hit
	 * 
	 */
	class ViewHolder {
		TextView visitno, storename, init1, init2, init3;
		InitiativeReportBO initbo;
		TextView initiative[];
		LinearLayout rowLayout;
		int childIndex, k;
	}

	/**
	 * Creates dynamic views for title view.
	 * 
	 * @param lLayout2
	 */
	private void loadTitleView(LinearLayout lLayout2) {

		linearlprams1 = new LayoutParams((int) getResources().getDimension(
				R.dimen.product_name_twenty_character),
				android.view.ViewGroup.LayoutParams.MATCH_PARENT);
		for (int i = 0; i < bmodel.initiativeHelper.getInitativeList().size(); i++) {
			InitiativeHolder obj = bmodel.initiativeHelper.getInitativeList()
					.get(i);
			if (obj.getIsParent() == 1) {
				TextView intitDescprition = new TextView(getActivity());
				intitDescprition.setLayoutParams(linearlprams1);
				intitDescprition.setGravity(Gravity.CENTER);

				intitDescprition.setTextAppearance(getActivity(),
						R.style.TextViewListTitle);
				intitDescprition
						.setBackgroundResource(R.drawable.list_title_bg);
				intitDescprition.setPadding(storeTitle.getPaddingLeft(),
						storeTitle.getPaddingTop(),
						storeTitle.getPaddingRight(),
						storeTitle.getPaddingBottom());
				intitDescprition.setText(obj.getInitiativeDesc() + "");
				lLayout2.addView(intitDescprition);
			}
		}

	}

	/**
	 * Creates dynamic views for total
	 * 
	 * @param totalLayout2
	 */
	private void loadTotalView(LinearLayout totalLayout2) {

		for (int i = 0; i < bmodel.initiativeHelper.getInitativeList().size(); i++) {
			InitiativeHolder obj = bmodel.initiativeHelper.getInitativeList()
					.get(i);

			TextView initTotal = new TextView(getActivity());
			initTotal.setLayoutParams(linearlprams2);
			initTotal.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

			initTotal.setTextSize(R.dimen.font_small);
			initTotal.setTypeface(null, Typeface.BOLD);
			initTotal.setPadding(0, 0, pixels, 0);
			initTotal.setText(bmodel.formatValue(obj.getTotalInitiative()));
			initTotal.setSingleLine();
			totalLayout2.addView(initTotal);
		}
	}

	/**
	 * Creates dynamic views for total MTD
	 * 
	 * @param totalMTDLayout2
	 */
	private void loadMTDTotalView(LinearLayout totalMTDLayout2) {
		for (int i = 0; i < bmodel.initiativeHelper.getInitativeList().size(); i++) {
			InitiativeHolder obj = bmodel.initiativeHelper.getInitativeList()
					.get(i);

			TextView initMTDTotal = new TextView(getActivity());
			initMTDTotal.setLayoutParams(linearlprams2);
			initMTDTotal.setGravity(Gravity.CENTER_VERTICAL | Gravity.RIGHT);

			initMTDTotal.setTextSize(R.dimen.font_small);
			initMTDTotal.setTypeface(null, Typeface.BOLD);
			initMTDTotal.setPadding(0, 0, pixels, 0);
			initMTDTotal.setText(bmodel.formatValue(obj.getTotalMTD()));
			initMTDTotal.setSingleLine();
			totalMTDLayout2.addView(initMTDTotal);

		}
	}

	/**
	 * Creates dynamic views for Distribution total.
	 * 
	 * @param totalDistLayout2
	 */
	private void loadTotalDistributionView(LinearLayout totalDistLayout2) {
		for (int i = 0; i < bmodel.initiativeHelper.getInitativeList().size(); i++) {
			InitiativeHolder obj = bmodel.initiativeHelper.getInitativeList()
					.get(i);

			TextView initDsitributionTotal = new TextView(getActivity());
			initDsitributionTotal.setLayoutParams(linearlprams2);
			initDsitributionTotal.setGravity(Gravity.CENTER_VERTICAL
					| Gravity.RIGHT);


			initDsitributionTotal.setTextSize(R.dimen.font_small);
			initDsitributionTotal.setTypeface(null, Typeface.BOLD);
			initDsitributionTotal.setPadding(0, 0, pixels, 0);
			initDsitributionTotal.setText(obj.getHitCount() + "");
			initDsitributionTotal.setSingleLine();
			totalDistLayout2.addView(initDsitributionTotal);

		}
	}

}
