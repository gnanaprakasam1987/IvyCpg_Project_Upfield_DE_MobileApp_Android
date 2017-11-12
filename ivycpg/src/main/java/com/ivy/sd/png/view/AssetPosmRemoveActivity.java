package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AssetTrackingBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.Vector;

public class AssetPosmRemoveActivity extends IvyBaseActivityNoActionBar {

	private ArrayList<AssetTrackingBO> mylist;
	private BusinessModel bmodel;
	private String mModuleName="";
	private ListView lvwplist;
	private Toolbar toolbar;
	private String mposmiddialog;
	private String msnodialog;
	private String msbdid;
	private String mbrandid,mReasonID;
	protected Button btnDelete;
	protected ArrayList<ReasonMaster> mAssetReasonList;
	protected ArrayAdapter<ReasonMaster> mAssetReasonSpinAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.remove_asset_dailog);
		bmodel = (BusinessModel) getApplicationContext();
		bmodel.setContext(this);


		lvwplist = (ListView) findViewById(R.id.lv_assetlist);
		lvwplist.setCacheColorHint(0);

		toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		btnDelete=(Button) findViewById(R.id.btn_delete);
		btnDelete.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if(isAssetSelectedToDelete()){
					if(isAssetSelectedWithReason()) {
						mDialog();
					}
					else
					{
						Toast.makeText(AssetPosmRemoveActivity.this, getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
					}
				}
				else{
					Toast.makeText(AssetPosmRemoveActivity.this,getResources().getString(R.string.nothing_selected_to_remove),Toast.LENGTH_LONG).show();
				}
			}
		});

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setDisplayShowTitleEnabled(false);
		setScreenTitle(getResources().getString(R.string.removeasset));

		if(getIntent().getExtras()!=null){
			mModuleName=getIntent().getStringExtra("module");
		}
		updatelist();
	}

    private boolean isAssetSelectedToDelete(){

		for(AssetTrackingBO bo:mylist){
			if(bo.isSelectedToRemove()){
				return true;
			}
		}
		return false;
	}
	private boolean isAssetSelectedWithReason(){

		for(AssetTrackingBO bo:mylist){
			if(!bo.getReason1ID().equalsIgnoreCase("0")){
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			 finish();
			return true;
		}

		return super.onOptionsItemSelected(item);

	}


	private void updatelist() {

		Vector<AssetTrackingBO> items;
		bmodel.assetTrackingHelper.lodAddRemoveAssets(mModuleName);

		items = bmodel.assetTrackingHelper.getAddRemoveAssets();
		if (items == null) {
			return;
		}
		int siz = items.size();
		mylist = new ArrayList<>();
		for (int i = 0; i < siz; ++i) {
			AssetTrackingBO ret = items.elementAt(i);
			mylist.add(ret);
		}

//		ReasonMaster reason1 = new ReasonMaster();
//		reason1.setReasonID(Integer.toString(0));
//		reason1.setReasonDesc("Select Reason");
	//	mAssetReasonList.add(0, reason1);
		mAssetReasonList=new ArrayList<>();
		bmodel.reasonHelper.loadAssetReasonsBasedOnType("ASSET_REMOVE");
		mAssetReasonList.add(new ReasonMaster("0","--Select Reason--"));
		mAssetReasonList.addAll(bmodel.reasonHelper.getAssetReasonsBasedOnType());
		mAssetReasonSpinAdapter = new ArrayAdapter<>(AssetPosmRemoveActivity.this,
				R.layout.spinner_bluetext_layout, mAssetReasonList);
		mAssetReasonSpinAdapter
				.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
//		ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, reasonList);
//		dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		MyAdapter mSchedule = new MyAdapter(mylist);
		lvwplist.setAdapter(mSchedule);

	}

	private class MyAdapter extends ArrayAdapter<AssetTrackingBO> {
		private final ArrayList<AssetTrackingBO> items;

		public MyAdapter(ArrayList<AssetTrackingBO> items) {
			super(AssetPosmRemoveActivity.this, R.layout.row_asset_dailog, items);
			this.items = items;
		}

		public AssetTrackingBO getItem(int position) {
			return items.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public int getCount() {
			return items.size();
		}

		public View getView(final int position, View convertView, ViewGroup parent) {

			final ViewHolder holder;
			AssetTrackingBO product = items.get(position);
			View row = convertView;
			if (row == null) {
				LayoutInflater inflater = getLayoutInflater();
				row = inflater
						.inflate(R.layout.row_asset_dailog, parent, false);
				holder = new ViewHolder();
				holder.tvassetname = (TextView) row
						.findViewById(R.id.tv_lt_assetname);
				holder.tvsno = (TextView) row.findViewById(R.id.tv_lt_sno);
				holder.tvinstall = (TextView) row
						.findViewById(R.id.tv_lt_install);
				holder.chkRemove=(CheckBox) row.findViewById(R.id.chk);
				holder.SPRemove=(Spinner)row.findViewById(R.id.sp_remove_reason);
				holder.SPRemove.setEnabled(false);
				holder.chkRemove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
					@Override
					public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
						if(isSelected){
							holder.productObj.setSelectedToRemove(true);
							holder.SPRemove.setEnabled(true);
						}
						else{
							holder.productObj.setSelectedToRemove(false);
							holder.SPRemove.setEnabled(false);
						}

					}
				});

				holder.SPRemove.setAdapter(mAssetReasonSpinAdapter);
				holder.SPRemove.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
					@Override
					public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
						ReasonMaster reasonBO = (ReasonMaster) holder.SPRemove
								.getSelectedItem();

						holder.productObj.setReason1ID(reasonBO
								.getReasonID());
						holder.productObj.setReasonDesc(reasonBO
								.getReasonDesc());
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {

					}
				});

				row.setTag(holder);
			} else {
				holder = (ViewHolder) row.getTag();
			}
			holder.productObj = product;
			holder.ref = position;
			holder.tvassetname.setText(holder.productObj.getPOSMName());

			holder.tvinstall.setText(holder.productObj.getNewInstallDate());

			String mSno = getResources().getString(
					R.string.serial_no)
					+ ":" + holder.productObj.getSNO();
			holder.tvsno.setText(mSno);

			TypedArray typearr = AssetPosmRemoveActivity.this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
			final int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
			final int secondary_color = typearr.getColor(R.styleable.MyTextView_textColorPrimary, 0);
			if ("Y".equals(holder.productObj.getFlag())) {
				holder.tvassetname.setTextColor(color);
			} else {
				holder.tvassetname.setTextColor(secondary_color);
			}

			return row;
		}
	}

	class ViewHolder {

		AssetTrackingBO productObj;
		TextView tvassetname;
		TextView tvsno;
		TextView tvinstall;
		CheckBox chkRemove;
		Spinner SPRemove;
		int ref;

	}

	private void mDialog() {

		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				this);


		alertDialogBuilder
				.setTitle("Do you want to remove asset?")
				.setCancelable(false)
				.setPositiveButton(R.string.yes,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								removeAsset();

							}
						})
				.setNegativeButton(R.string.no,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {

								dialog.cancel();
							}
						});

		bmodel.applyAlertDialogTheme(alertDialogBuilder);
	}

	private void removeAsset(){

		ArrayList<AssetTrackingBO> lstTemp=new ArrayList<>();
		lstTemp.addAll(mylist);

		for(int i=0;i<lstTemp.size();i++) {
			if(lstTemp.get(i).isSelectedToRemove()) {

				if ("N".equals(lstTemp.get(i).getFlag())) {
					mposmiddialog = lstTemp.get(i).getPOSM();
					msnodialog = lstTemp.get(i).getSNO();
					msbdid = lstTemp.get(i).getSBDId();
					mbrandid = lstTemp.get(i).getBrand();
					if(!lstTemp.get(i).getReason1ID().equalsIgnoreCase("0")) {
						mReasonID = lstTemp.get(i).getReason1ID();
						bmodel.assetTrackingHelper
								.saveAddAndDeleteDetails(mposmiddialog,
										msnodialog, msbdid, mbrandid, mReasonID, mModuleName);

						mylist.remove(i);
					}


				} else {
					bmodel.assetTrackingHelper
							.deletePosmDetails(lstTemp.get(i)
									.getSNO());
					mylist.remove(i);
				}
				bmodel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
			}
		}

		MyAdapter mSchedule = new MyAdapter(mylist);
		lvwplist.setAdapter(mSchedule);
	}
}
