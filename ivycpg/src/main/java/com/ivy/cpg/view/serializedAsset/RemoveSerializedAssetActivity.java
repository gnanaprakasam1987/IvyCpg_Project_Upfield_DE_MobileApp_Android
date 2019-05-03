package com.ivy.cpg.view.serializedAsset;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;

public class RemoveSerializedAssetActivity extends IvyBaseActivityNoActionBar {

    private ArrayList<SerializedAssetBO> mList;
    private BusinessModel bModel;
    private String mModuleName = "";
    private ListView mListView;
    protected Button btnDelete;
    protected ArrayList<ReasonMaster> mAssetReasonList;
    protected ArrayAdapter<ReasonMaster> mAssetReasonSpinAdapter;
    SerializedAssetHelper assetTrackingHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.remove_asset_dailog);
        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);
        assetTrackingHelper = SerializedAssetHelper.getInstance(this);


        mListView = findViewById(R.id.lv_assetlist);
        mListView.setCacheColorHint(0);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        btnDelete = findViewById(R.id.btn_delete);
        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (isAssetSelectedToDelete()) {
                    if (isAssetSelectedWithReason()) {
                        mDialog();
                    } else {
                        Toast.makeText(RemoveSerializedAssetActivity.this, getString(R.string.select_reason), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RemoveSerializedAssetActivity.this, getResources().getString(R.string.nothing_selected_to_remove), Toast.LENGTH_LONG).show();
                }
            }
        });

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.removeasset));
        }

        if (getIntent().getExtras() != null) {
            mModuleName = getIntent().getStringExtra("module");
        }
        updateList();
    }

    /**
     * To check is asset selected
     *
     * @return is Selected
     */
    private boolean isAssetSelectedToDelete() {

        for (SerializedAssetBO bo : mList) {
            if (bo.isSelectedToRemove()) {
                return true;
            }
        }
        return false;
    }

    /**
     * To check is asset selected with reason
     *
     * @return is Selected
     */
    private boolean isAssetSelectedWithReason() {


        for (SerializedAssetBO bo : mList) {
            if (bo.isSelectedToRemove() && bo.getReason1ID().equalsIgnoreCase("0"))
                return false;

        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }


    /**
     * Update list and load reasons
     */
    private void updateList() {

        Vector<SerializedAssetBO> items;
        assetTrackingHelper.loadRemovableAssets(getApplicationContext(), mModuleName);

        items = assetTrackingHelper.getRemovableAssets();
        if (items == null) {
            return;
        }
        int siz = items.size();
        mList = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            SerializedAssetBO ret = items.elementAt(i);
            if (bModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !ret.getParentHierarchy().contains("/" + bModel.productHelper.getmSelectedGlobalProductId() + "/"))
                continue;
            mList.add(ret);
        }

        mAssetReasonList = new ArrayList<>();
        bModel.reasonHelper.loadAssetReasonsBasedOnType("ASSET_REMOVE");
        mAssetReasonList.add(new ReasonMaster("0", "--Select Reason--"));
        mAssetReasonList.addAll(bModel.reasonHelper.getAssetReasonsBasedOnType());
        mAssetReasonSpinAdapter = new ArrayAdapter<>(RemoveSerializedAssetActivity.this,
                R.layout.spinner_bluetext_layout, mAssetReasonList);
        mAssetReasonSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        MyAdapter mSchedule = new MyAdapter(mList);
        mListView.setAdapter(mSchedule);

    }

    private class MyAdapter extends ArrayAdapter<SerializedAssetBO> {
        private final ArrayList<SerializedAssetBO> items;

        public MyAdapter(ArrayList<SerializedAssetBO> items) {
            super(RemoveSerializedAssetActivity.this, R.layout.row_asset_dailog, items);
            this.items = items;
        }

        public SerializedAssetBO getItem(int position) {
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
            SerializedAssetBO product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_asset_dailog, parent, false);
                holder = new ViewHolder();
                holder.tvAssetName = row
                        .findViewById(R.id.tv_lt_assetname);
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
                    R.string.serial_no);

            if (bModel.labelsMasterHelper
                    .applyLabels((Object) "asset_serialno") != null)
                mSno = bModel.labelsMasterHelper
                        .applyLabels((Object) "asset_serialno");

            mSno = mSno +  " : " + holder.productObj.getSNO();

            String strLabel;
            if (assetTrackingHelper.SHOW_ASSET_VENDOR) {
                strLabel = getResources().getString(R.string.vendor);
                if (bModel.labelsMasterHelper
                        .applyLabels((Object) "asset_vendor") != null)
                    strLabel = bModel.labelsMasterHelper
                            .applyLabels((Object) "asset_vendor");
                mSno = mSno + "   " + strLabel + " : " + holder.productObj.getVendorName();
            }
            if (assetTrackingHelper.SHOW_ASSET_MODEL) {
                strLabel = getResources().getString(R.string.model);
                if (bModel.labelsMasterHelper
                        .applyLabels((Object) "asset_model") != null)
                    strLabel = bModel.labelsMasterHelper
                            .applyLabels((Object) "asset_model");
                mSno = mSno + "   " + strLabel + " : " + holder.productObj.getModelName();
            }
            if (assetTrackingHelper.SHOW_ASSET_TYPE) {
                strLabel = getResources().getString(R.string.type);
                if (bModel.labelsMasterHelper
                        .applyLabels((Object) "asset_type") != null)
                    strLabel = bModel.labelsMasterHelper
                            .applyLabels((Object) "asset_type");
                mSno = mSno + "   " + strLabel + " : " + holder.productObj.getAssetType();
            }
            if (assetTrackingHelper.SHOW_ASSET_CAPACITY) {
                strLabel = getResources().getString(R.string.capacity);
                if (bModel.labelsMasterHelper
                        .applyLabels((Object) "asset_capacity") != null)
                    strLabel = bModel.labelsMasterHelper
                            .applyLabels((Object) "asset_capacity");
                mSno = mSno + "   " + strLabel + " : " + holder.productObj.getCapacity();
            }
            holder.tvSNO.setText(mSno);

            TypedArray mTypedArray = RemoveSerializedAssetActivity.this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
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

        SerializedAssetBO productObj;
        TextView tvAssetName;
        TextView tvSNO;
        TextView tvInstall;
        CheckBox chkRemove;
        Spinner SPRemove;
        int ref;

    }

    /**
     * Showing alert dialog
     */
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

        bModel.applyAlertDialogTheme(alertDialogBuilder);
    }

    /**
     * Removing asset
     */
    private void removeAsset() {

        String mReasonID;
        String mSBDId;
        String mPOSMIdDialog;
        String mSNODialog;
        try {
            Iterator itr = mList.iterator();
            while (itr.hasNext()) {
                SerializedAssetBO assetTrackingBO = (SerializedAssetBO) itr.next();
                if (assetTrackingBO.isSelectedToRemove()) {

                    if ("N".equals(assetTrackingBO.getFlag())) {
                        mPOSMIdDialog = assetTrackingBO.getPOSM();
                        mSNODialog = assetTrackingBO.getSNO();
                        mSBDId = assetTrackingBO.getSBDId();
                        if (!assetTrackingBO.getReason1ID().equalsIgnoreCase("0")) {
                            mReasonID = assetTrackingBO.getReason1ID();
                            assetTrackingHelper
                                    .deleteAsset(getApplicationContext(), mPOSMIdDialog,
                                            mSNODialog, mSBDId, mReasonID, mModuleName,assetTrackingBO.getNFCTagId(),assetTrackingBO.getReferenceId());

                            itr.remove();
                        }

                    } else {
                        assetTrackingHelper
                                .deleteAssetTransaction(getApplicationContext(), assetTrackingBO.getSNO());
                        itr.remove();
                    }
                    bModel.saveModuleCompletion(HomeScreenTwo.MENU_SERIALIZED_ASSET, true);
                }
            }

            MyAdapter mSchedule = new MyAdapter(mList);
            mListView.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
