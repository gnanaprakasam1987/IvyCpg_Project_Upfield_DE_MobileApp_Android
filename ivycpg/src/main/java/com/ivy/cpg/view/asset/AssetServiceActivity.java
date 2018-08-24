package com.ivy.cpg.view.asset;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
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

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.Vector;


import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class AssetServiceActivity extends BaseActivity {

    @BindView(R.id.recycler_asset_service)
    ListView assetServiceList;


    @BindView(R.id.btn_delete)
    Button assetService;

    protected ArrayAdapter<ReasonMaster> mAssetReasonSpinAdapter;

    private AssetTrackingHelper assetTrackingHelper;

    protected ArrayList<ReasonMaster> mAssetReasonList;
    private BusinessModel bModel;


    private ArrayList<AssetTrackingBO> mList;

    private String mModuleName = "";

    private Unbinder mUnBinder;

    @Override
    public int getLayoutId() {
        return R.layout.asset_service_dialog;
    }

    @Override
    protected void initVariables() {
        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);
        assetTrackingHelper = AssetTrackingHelper.getInstance(this);
        mUnBinder = ButterKnife.bind(this);
        updateList();
        assetService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showConfirmDialog();
            }
        });
    }

    @Override
    public void initializeDi() {


    }


    /*@OnClick(R.id.btn_delete)
    public void saveServiceData() {
        showConfirmDialog();
    }*/

    @Override
    protected void getMessageFromAliens() {
        if (getIntent().getExtras() != null) {
            mModuleName = getIntent().getStringExtra("module");
        }
    }

    private void updateList() {
        assetTrackingHelper.getAssetService(getApplicationContext(), mModuleName);
        Vector<AssetTrackingBO> items = assetTrackingHelper.getAssetServiceList();
        if (items == null) {
            return;
        }
        int siz = items.size();
        mList = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {
            AssetTrackingBO ret = items.elementAt(i);
            mList.add(ret);
        }

        mAssetReasonList = new ArrayList<>();
        bModel.reasonHelper.loadAssetReasonsBasedOnType("ASSET_REMOVE");
        mAssetReasonList.add(new ReasonMaster("0", "--Select Reason--"));
        mAssetReasonList.addAll(bModel.reasonHelper.getAssetReasonsBasedOnType());
        mAssetReasonSpinAdapter = new ArrayAdapter<>(AssetServiceActivity.this,
                R.layout.spinner_bluetext_layout, mAssetReasonList);
        mAssetReasonSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        MyAdapter mSchedule = new MyAdapter(mList);
        assetServiceList.setAdapter(mSchedule);
    }

    @Override
    protected void setUpViews() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.asset_service));
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    private void showConfirmDialog() {
        showAlert("", "Do you want to Save  asset service?", new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                saveAssetService();
            }
        });
    }

    private void saveAssetService() {
        String mReasonID, assetId, serialNo;

        ArrayList<AssetTrackingBO> lstTemp = new ArrayList<>();
        lstTemp.addAll(mList);

        for (int i = 0; i < lstTemp.size(); i++) {
            if(lstTemp.get(i).isSelectedToRemove()) {
                assetId = lstTemp.get(i).getPOSM();
                serialNo = lstTemp.get(i).getSNO();

                if (!lstTemp.get(i).getReason1ID().equalsIgnoreCase("0")) {
                    mReasonID = lstTemp.get(i).getReason1ID();
                    assetTrackingHelper
                            .saveAssetServiceDetails(getApplicationContext(), assetId, serialNo, mReasonID, mModuleName);

                    //  mList.remove(i);
                }
                bModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
            }


        }


    }


    private class MyAdapter extends ArrayAdapter<AssetTrackingBO> {
        private final ArrayList<AssetTrackingBO> items;

        public MyAdapter(ArrayList<AssetTrackingBO> items) {
            super(AssetServiceActivity.this, R.layout.row_asset_dailog, items);
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

        @NonNull
        public View getView(final int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;
            AssetTrackingBO product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_asset_dailog, parent, false);
                holder = new ViewHolder();
                holder.tvAssetName = row.findViewById(R.id.tv_lt_assetname);
                holder.tvSNO = row.findViewById(R.id.tv_lt_sno);
                holder.tvInstall = row
                        .findViewById(R.id.tv_lt_install);
                holder.chkRemove = row.findViewById(R.id.chk);
                holder.SPRemove = row.findViewById(R.id.sp_remove_reason);
                holder.SPRemove.setEnabled(false);
                holder.chkRemove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                        if (isSelected) {
                            holder.productObj.setSelectedToRemove(true);
                            holder.SPRemove.setEnabled(true);
                        } else {
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
            holder.tvAssetName.setText(holder.productObj.getPOSMName());

            holder.tvInstall.setText(holder.productObj.getNewInstallDate());

            String mSno = getResources().getString(
                    R.string.serial_no)
                    + ":" + holder.productObj.getSNO();
            holder.tvSNO.setText(mSno);

            TypedArray mTypedArray = AssetServiceActivity.this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = mTypedArray.getColor(R.styleable.MyTextView_accentcolor, 0);
            final int secondary_color = mTypedArray.getColor(R.styleable.MyTextView_textColorPrimary, 0);
            if ("Y".equals(holder.productObj.getFlag())) {
                holder.tvAssetName.setTextColor(color);
            } else {
                holder.tvAssetName.setTextColor(secondary_color);
            }

            return row;
        }
    }

    class ViewHolder {

        AssetTrackingBO productObj;
        TextView tvAssetName;
        TextView tvSNO;
        TextView tvInstall;
        CheckBox chkRemove;
        Spinner SPRemove;
        int ref;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }
}
