package com.ivy.sd.png.view.reports;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.SKUReportBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Vector;

public class SKUReportFragment extends IvyBaseFragment {
	/** Called when the activity is first created. */
	private ListView lvwplist;
	private Spinner spinnerbrand;
	private BusinessModel bmodel;
	private ArrayList<SKUReportBO> mylist;
	private Vector<SKUReportBO> values = new Vector<SKUReportBO>();
	private View view;
	private Vector<ChildLevelBo> vbeat = new Vector<ChildLevelBo>();
	private SKUReportBO product;

	@Override
	public void onAttach(Activity activity) {

		bmodel = (BusinessModel) getActivity().getApplicationContext();
		bmodel.setContext(getActivity());
		ChildLevelBo childLevelBO = new ChildLevelBo();
		childLevelBO.setProductid(0);
		childLevelBO.setParentid(0);
		childLevelBO.setPlevelName(getResources().getString(R.string.all));

		bmodel.productHelper.getChildLevelBo().add(childLevelBO);

		super.onAttach(activity);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		getActivity().getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

		view = inflater.inflate(R.layout.fragment_sku_report, container, false);

		if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
			Toast.makeText(getActivity(),
					getResources().getString(R.string.sessionout_loginagain),
					Toast.LENGTH_SHORT).show();
			getActivity().finish();
		}

		spinnerbrand = (Spinner) view.findViewById(R.id.brandSpinner);
		lvwplist = (ListView) view.findViewById(R.id.list);
		lvwplist.setCacheColorHint(0);

		return view;
	}

	@Override
	public void onStart() {
		loadData();
		super.onStart();
	}

	private void loadData() {
		try {
			vbeat = bmodel.productHelper.getChildLevelBo();
			if (vbeat == null) {
				Toast.makeText(getActivity(),
						getResources().getString(R.string.nodatadownload),
						Toast.LENGTH_LONG).show();
				return;
			}

			
			values = bmodel.reportHelper
					.downloadSKUReport();
			
			/** sort based in the waking sequence **/
			Collections.sort(vbeat, ChildLevelBo.LoadingOrder);
			ArrayAdapter<ChildLevelBo> childAdapter = new ArrayAdapter<ChildLevelBo>(
					getActivity(), android.R.layout.simple_spinner_item, vbeat);
			childAdapter
					.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
			spinnerbrand.setAdapter(childAdapter);

			spinnerbrand
					.setOnItemSelectedListener(new OnItemSelectedListener() {
						public void onItemSelected(AdapterView<?> parent,
								View view, int position, long id) {
							ChildLevelBo childBo = (ChildLevelBo) parent
									.getSelectedItem();
							
							updateSKUReportGrid(childBo.getProductid());
						}

						public void onNothingSelected(AdapterView<?> parent) {
						}
					});

		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	private void updateSKUReportGrid(int parentID) {

		mylist = new ArrayList<SKUReportBO>();
		if (values == null) {
			bmodel.showAlert(
					getResources().getString(R.string.no_products_exists), 0);
			mylist.clear();
			return;
		}
		int siz = values.size();

		for (int i = 0; i < siz; ++i) {
			SKUReportBO ret = (SKUReportBO) values.elementAt(i);
			
			if(ret.getParentID()==parentID||parentID==0)
				
			mylist.add(ret);
		}

		MyAdapter mSchedule = new MyAdapter(mylist);
		lvwplist.setAdapter(mSchedule);

	}

	class MyAdapter extends ArrayAdapter<Object> {
		private ArrayList<SKUReportBO> items;

		public MyAdapter(ArrayList<SKUReportBO> items) {
			super(getActivity(), R.layout.row_sku_report);
			this.items = items;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return items.size();
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder holder;
			product = (SKUReportBO) items.get(position);
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getActivity().getLayoutInflater();
				row = inflater.inflate(R.layout.row_sku_report, parent, false);
				holder = new ViewHolder();
				holder.psname = (TextView) row.findViewById(R.id.closePRODNAME);
				holder.qty = (TextView) row.findViewById(R.id.closeQTY);
				holder.ouqty = (TextView) row.findViewById(R.id.sku_OU);
				holder.msqqty = (TextView) row.findViewById(R.id.sku_MSQ);

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}

			holder.psname.setText(product.getProdname() + "");
			holder.qty.setText(product.getQty());
			holder.ouqty.setText(product.getOuqty() + "");
			holder.msqqty.setText(product.getMsqqty() + "");

			return (row);
		}
	}

	class ViewHolder {
		String productId, productCode, pname;// product id
		TextView psname, qty, ouqty, msqqty;
		String gty;
		int ref;
	}

	public void onBackPressed() {
		// do something on back.
		return;
	}

}