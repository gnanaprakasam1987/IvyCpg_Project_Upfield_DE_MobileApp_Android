package com.ivy.cpg.view.asset;


import android.content.res.TypedArray;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
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

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.asset.assetservicedi.AssetServiceModule;
import com.ivy.cpg.view.asset.assetservicedi.DaggerAssetServiceComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;

import java.util.ArrayList;
import java.util.Vector;


import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class AssetServiceActivity extends BaseActivity implements AssetServiceContract.AssetServiceView {

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

    @Inject
    BaseIvyPresenter<BaseIvyView> viewAssetServicePresenter;


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
                if (isAssetSelectedToService()) {
                    if (isAssetSelectedWithReason()) {
                        showConfirmDialog();
                    } else {
                        Toast.makeText(AssetServiceActivity.this, getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(AssetServiceActivity.this, getString(R.string.select_asset_service), Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    @Override
    public void initializeDi() {

        DaggerAssetServiceComponent.builder()
                .assetServiceModule(new AssetServiceModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) viewAssetServicePresenter);


    }

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
            if (bModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !ret.getParentHierarchy().contains("/" + bModel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            mList.add(ret);
        }

        mAssetReasonList = new ArrayList<>();
        bModel.reasonHelper.loadAssetReasonsBasedOnType("ASSET_SER_REQ");
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
        showAlert("", "Do you want to Save asset service?", new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                saveAssetService();
            }
        }, true);
    }

    private void saveAssetService() {
        String mReasonID, assetId, serialNo;

        boolean isAdded = false;

        ArrayList<AssetTrackingBO> lstTemp = new ArrayList<>();
        lstTemp.addAll(mList);
        assetTrackingHelper.deleteServiceTable(getApplicationContext());
        for (int i = 0; i < lstTemp.size(); i++) {
            if (lstTemp.get(i).isSelectedToRemove()) {
                assetId = lstTemp.get(i).getPOSM();
                serialNo = lstTemp.get(i).getSNO();

                if (!lstTemp.get(i).getReason1ID().equalsIgnoreCase("0")) {
                    isAdded = true;
                    mReasonID = lstTemp.get(i).getReason1ID();
                    assetTrackingHelper
                            .saveAssetServiceDetails(getApplicationContext(), assetId, serialNo, mReasonID, mModuleName);
                }
                //bModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
            }
        }
        if (isAdded)
            Toast.makeText(this, "Saved SuccessFully", Toast.LENGTH_SHORT).show();
        finish();
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
                        .inflate(R.layout.item_asset_service, parent, false);
                holder = new ViewHolder();
                holder.tvAssetName = row.findViewById(R.id.tv_lt_assetname);
                holder.tvSNO = row.findViewById(R.id.tv_lt_sno);
                holder.chkRemove = row.findViewById(R.id.chk);
                holder.SPRemove = row.findViewById(R.id.sp_remove_reason);
                holder.SPRemove.setEnabled(false);
                holder.chkRemove.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton compoundButton, boolean isSelected) {
                        if (isSelected && !holder.chkRemove.isEnabled()) {
                            holder.productObj.setSelectedToRemove(true);
                            holder.SPRemove.setEnabled(false);
                        } else if (isSelected) {
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

            for (int i = 0; i < mAssetReasonSpinAdapter.getCount(); i++) {
                if (mAssetReasonSpinAdapter.getItem(i).getReasonID().equalsIgnoreCase(product.getReason1ID())) {
                    holder.SPRemove.setSelection(i);
                }
            }


            holder.chkRemove.setEnabled(!holder.productObj.isSelectedReason());
            holder.chkRemove.setChecked(holder.productObj.isSelectedReason());


            holder.tvAssetName.setText(holder.productObj.getPOSMName());


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

        CheckBox chkRemove;
        Spinner SPRemove;
        int ref;

    }


    private boolean isAssetSelectedWithReason() {

        for (AssetTrackingBO bo : mList) {
            if (!bo.getReason1ID().equalsIgnoreCase("0") && !bo.isSelectedReason()) {
                return true;
            }
        }
        return false;
    }

    private boolean isAssetSelectedToService() {

        for (AssetTrackingBO bo : mList) {
            if (bo.isSelectedToRemove() && !bo.isSelectedReason()) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mUnBinder.unbind();
    }
}
