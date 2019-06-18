package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivy.core.IvyConstants;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.LabelsMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by rajkumar.s on 11/21/2017.
 */

public class SerializedAssetAdapter extends BaseAdapter {

    private ArrayList<SerializedAssetBO> items = null;
    private Context mContext;
    private Fragment mFragment;
    private BusinessModel mBModel;
    SerializedAssetHelper assetTrackingHelper;
    SerializedAssetPresenterImpl mAssetPresenter;
    Button dateBtn;
    private static final String TAG_DATE_PICKER_INSTALLED = "date_picker_installed";
    private static final String TAG_DATE_PICKER_SERVICED = "date_picker_serviced";
    String outPutDateFormat;
    private final String moduleName = "AT_";
    String photoPath;
    String imageName;
    private static final int CAMERA_REQUEST_CODE = 1;
    ArrayAdapter<ReasonMaster> mAssetReasonSpinAdapter;
    ArrayList<ReasonMaster> mAssetReasonList;
    ArrayList<ReasonMaster> mAssetConditionList;
    ArrayAdapter<ReasonMaster> mAssetConditionAdapter;


    public SerializedAssetAdapter(Context context, BusinessModel mBModel, SerializedAssetPresenterImpl mAssetPresenter, Fragment mFragment
            , ArrayList<SerializedAssetBO> items) {
        super();
        this.mContext = context;
        this.mBModel = mBModel;
        this.items = items;
        assetTrackingHelper = SerializedAssetHelper.getInstance(context);
        this.outPutDateFormat = mBModel.configurationMasterHelper.outDateFormat;
        this.mAssetPresenter = mAssetPresenter;
        this.mFragment = mFragment;

        ReasonMaster reason1 = new ReasonMaster();
        reason1.setReasonID(Integer.toString(0));
        reason1.setReasonDesc(context.getString(R.string.plain_select));
        mAssetReasonList = mAssetPresenter.getAssetReasonList();

        if (mAssetReasonList.size() > 0) {
            if (!mAssetReasonList.get(0).getReasonID().equals("0"))
                mAssetReasonList.add(0, reason1);
        }

        mAssetReasonSpinAdapter = new ArrayAdapter<>(mContext,
                R.layout.spinner_bluetext_layout, mAssetReasonList);
        mAssetReasonSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        String select_condition = mContext.getString(R.string.plain_select);
        try {
            if (LabelsMasterHelper.getInstance(context).applyLabels("select_condition") != null)
                select_condition = LabelsMasterHelper.getInstance(context).applyLabels("select_condition");
        } catch (Exception e) {
            Commons.printException(e);
        }
        ReasonMaster reason3 = new ReasonMaster();
        reason3.setConditionID(Integer.toString(0));
        reason3.setReasonDesc(select_condition);
        mAssetConditionList = assetTrackingHelper.getAssetConditionList();

        if (mAssetConditionList.size() > 0) {
            if (!mAssetConditionList.get(0).getConditionID().equals("0"))
                mAssetConditionList.add(0, reason3);
        }

        mAssetConditionAdapter = new ArrayAdapter<>(mContext,
                R.layout.spinner_bluetext_layout, mAssetConditionList);
        mAssetConditionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int arg0) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        View row = convertView;
        if (row == null) {

            holder = new ViewHolder();

            LayoutInflater inflater = LayoutInflater.from(mContext);
            row = inflater.inflate(
                    R.layout.row_serialized_asset, parent, false);
            row.setTag(holder);

            holder.assetDetailLL = row
                    .findViewById(R.id.asset_detail);
            holder.audit = row
                    .findViewById(R.id.btn_audit);
            holder.assetNameTV = row
                    .findViewById(R.id.tv_asset_name);
            //holder.assetNameTV.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.reason1Spin = row
                    .findViewById(R.id.spin_reason1);

            holder.mConditionSpin = row
                    .findViewById(R.id.spin_condition);

            holder.mInstallDate = row
                    .findViewById(R.id.Btn_install_Date);
            holder.llInstallDate = row
                    .findViewById(R.id.ll_install_date);
            //holder.mInstallDate.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.mServiceDate = row
                    .findViewById(R.id.Btn_service_Date);
            holder.ll_service_date = row
                    .findViewById(R.id.ll_service_date);
            //holder.mServiceDate.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.photoBTN = row
                    .findViewById(R.id.btn_photo);
            holder.availQtyRB = row
                    .findViewById(R.id.radio_avail_qty);
            holder.availQtyLL = row
                    .findViewById(R.id.ll_avail_qty);
            holder.serialNoTV = row
                    .findViewById(R.id.tv_serialNo);
            //holder.serialNoTV.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.execQtyLL = row.findViewById(R.id.ll_exec_qty);
            holder.execQtyRB = row.findViewById(R.id.radio_exec_qty);

            holder.reason1Spin.setAdapter(mAssetReasonSpinAdapter);
            holder.mConditionSpin.setAdapter(mAssetConditionAdapter);


            if (assetTrackingHelper.SHOW_ASSET_EXECUTED)
                holder.execQtyLL.setVisibility(View.VISIBLE);


            holder.assetDetailLL.setOnClickListener(v ->
                    mContext.startActivity(new Intent(mContext, SerializedAssetDetailActivity.class)
                            .putExtra("detailBo", holder.assetBO)));

            holder.audit.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if (holder.assetBO.getAudit() == IvyConstants.AUDIT_DEFAULT) {

                        holder.assetBO.setAudit(IvyConstants.AUDIT_OK);
                        holder.audit
                                .setImageResource(R.drawable.ic_audit_yes);

                    } else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_OK) {

                        holder.assetBO.setAudit(IvyConstants.AUDIT_NOT_OK);
                        holder.audit
                                .setImageResource(R.drawable.ic_audit_no);

                    } else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_NOT_OK) {

                        holder.assetBO.setAudit(IvyConstants.AUDIT_DEFAULT);
                        holder.audit
                                .setImageResource(R.drawable.ic_audit_none);
                    }

                }
            });


            holder.reason1Spin
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0,
                                                   View arg1, int arg2, long arg3) {
                            ReasonMaster reasonBO = (ReasonMaster) holder.reason1Spin
                                    .getSelectedItem();

                            holder.assetBO.setReason1ID(reasonBO
                                    .getReasonID());
                            holder.assetBO.setReasonDesc(reasonBO
                                    .getReasonDesc());

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });
            holder.mConditionSpin
                    .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> arg0,
                                                   View arg1, int arg2, long arg3) {
                            ReasonMaster reasonBO = (ReasonMaster) holder.mConditionSpin
                                    .getSelectedItem();

                            holder.assetBO.setConditionID(reasonBO
                                    .getConditionID());
                            holder.assetBO.setReasonDesc(reasonBO
                                    .getReasonDesc());

                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {

                        }
                    });

            holder.mInstallDate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dateBtn = holder.mInstallDate;
                    dateBtn.setTag(holder.assetBO);
                    DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("MODULE", "ASSET");
                    args.putString("selectedDate", holder.assetBO.getInstallDate());
                    args.putString("selectedDateFormat", outPutDateFormat);
                    newFragment.setArguments(args);
                    newFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), TAG_DATE_PICKER_INSTALLED);
                }
            });
            holder.mServiceDate.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dateBtn = holder.mServiceDate;
                    dateBtn.setTag(holder.assetBO);
                    DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
                    Bundle args = new Bundle();
                    args.putString("MODULE", "ASSET");
                    args.putString("selectedDate", holder.assetBO.getServiceDate());
                    args.putString("selectedDateFormat", outPutDateFormat);
                    newFragment.setArguments(args);
                    newFragment.show(((AppCompatActivity) mContext).getSupportFragmentManager(), TAG_DATE_PICKER_SERVICED);
                }
            });

            holder.photoBTN.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (mBModel.synchronizationHelper
                            .isExternalStorageAvailable()) {

                        photoPath = mContext.getExternalFilesDir(
                                Environment.DIRECTORY_PICTURES)
                                + "/" + DataMembers.photoFolderName + "/";

                        imageName = moduleName
                                + mBModel.getRetailerMasterBO()
                                .getRetailerID() + "_"
                                + holder.assetBO.getAssetID() + "_"
                                + holder.assetBO.getSerialNo() + "_"
                                + Commons.now(Commons.DATE_TIME)
                                + "_img.jpg";

                        String fileNameStarts = moduleName
                                + mBModel.getRetailerMasterBO()
                                .getRetailerID() + "_" + holder.assetBO.getAssetID() + "_"
                                + holder.assetBO.getSerialNo() + "_"
                                + Commons.now(Commons.DATE);


                        mAssetPresenter.mSelectedAssetID = holder.assetBO
                                .getAssetID();
                        mAssetPresenter.mSelectedImageName = imageName;
                        mAssetPresenter.mSelectedSerial = holder.assetBO.getSerialNo();

                        boolean nFilesThere = mBModel.checkForNFilesInFolder(photoPath, 1,
                                fileNameStarts);
                        if (nFilesThere) {
                            showFileDeleteAlertWithImage(holder.assetBO.getAssetID()
                                    + "", fileNameStarts, holder.assetBO.getImgName());

                        } else {
                            Intent intent = new Intent(mContext,
                                    CameraActivity.class);
                            intent.putExtra(CameraActivity.QUALITY, 40);
                            String path = photoPath + "/" + imageName;
                            intent.putExtra(CameraActivity.PATH, path);
                            mFragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
                            holder.photoBTN.requestFocus();
                        }

                    } else {
                        Toast.makeText(mContext, mContext.getResources().getString(R.string.external_storage_not_available)
                                , Toast.LENGTH_SHORT)
                                .show();

                    }

                }
            });
            holder.availQtyRB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (holder.assetBO.getAvailQty() == 0) {

                        holder.availQtyRB.setChecked(true);
                        holder.assetBO.setAvailQty(1);
                        holder.reason1Spin.setEnabled(false);

                        if ((holder.assetBO.getImgName() != null)
                                && (!holder.assetBO.getImgName().isEmpty())
                        ) {
                            holder.photoBTN.setEnabled(true);
                            setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);
                        } else {
                            holder.photoBTN.setEnabled(true);
                            holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
                        }

                        holder.reason1Spin.setSelection(0);
                        holder.mConditionSpin.setEnabled(true);
                        holder.mConditionSpin.setSelection(0);
                        holder.mInstallDate.setEnabled(holder.assetBO.getmLastInstallDate().isEmpty());
                        holder.mServiceDate.setEnabled(true);

                        if (!holder.assetBO.getmLastInstallDate().isEmpty())
                            holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.assetBO.getInstallDate(), outPutDateFormat));
                        else {
                            holder.assetBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.mInstallDate.setText(holder.assetBO.getInstallDate());
                        }
                        if (!holder.assetBO.getServiceDate().isEmpty())
                            holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.assetBO.getServiceDate(), outPutDateFormat));
                        else {
                            holder.assetBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.mServiceDate.setText(holder.assetBO.getServiceDate());
                        }

                    } else {
                        holder.availQtyRB.setChecked(false);
                        holder.assetBO.setAvailQty(0);
                        holder.reason1Spin.setEnabled(true);
                        holder.photoBTN.setEnabled(false);
                        holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
                        holder.mConditionSpin.setEnabled(false);
                        holder.mConditionSpin.setSelection(0);
                        holder.mInstallDate.setEnabled(false);
                        holder.mServiceDate.setEnabled(false);

                        if (!holder.assetBO.getmLastInstallDate().isEmpty())
                            holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.assetBO.getInstallDate(), outPutDateFormat));
                        else {
                            holder.assetBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.mInstallDate.setText(holder.assetBO.getInstallDate());
                        }
                        if (!holder.assetBO.getServiceDate().isEmpty())
                            holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.assetBO.getServiceDate(), outPutDateFormat));
                        else {
                            holder.assetBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
                            holder.mServiceDate.setText(holder.assetBO.getServiceDate());
                        }

                    }

                }
            });

            holder.execQtyRB.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if (holder.assetBO.getExecutorQty() == 0) {
                        holder.execQtyRB.setChecked(true);
                        holder.assetBO.setExecutorQty(1);
                    } else {
                        holder.execQtyRB.setChecked(false);
                        holder.assetBO.setExecutorQty(0);
                    }
                }
            });

            if (mBModel.configurationMasterHelper.isAuditEnabled()) {
                holder.audit.setVisibility(View.VISIBLE);
            }

        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.assetBO = items.get(position);

        if (holder.assetBO.getAudit() == IvyConstants.AUDIT_DEFAULT)
            holder.audit.setImageResource(R.drawable.ic_audit_none);
        else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_OK)
            holder.audit.setImageResource(R.drawable.ic_audit_yes);
        else if (holder.assetBO.getAudit() == IvyConstants.AUDIT_NOT_OK)
            holder.audit.setImageResource(R.drawable.ic_audit_no);

        holder.assetNameTV.setText(holder.assetBO.getAssetName());
        holder.reason1Spin.setSelection(mAssetPresenter
                .getItemIndex(holder.assetBO.getReason1ID(),
                        mAssetPresenter.getAssetReasonList(), true));

        String serialNo = mContext.getResources().getString(R.string.serial_no);
        if (mBModel.labelsMasterHelper.applyLabels(row.findViewById(
                R.id.tv_serialNo).getTag()) != null)
            serialNo = mBModel.labelsMasterHelper
                    .applyLabels(row.findViewById(
                            R.id.tv_serialNo).getTag());

        serialNo = serialNo + ": " + holder.assetBO.getSerialNo();

        String strLabel;
        if (assetTrackingHelper.SHOW_ASSET_VENDOR) {
            strLabel = mContext.getResources().getString(R.string.vendor);
            if (mBModel.labelsMasterHelper
                    .applyLabels((Object) "asset_vendor") != null)
                strLabel = mBModel.labelsMasterHelper
                        .applyLabels((Object) "asset_vendor");
            serialNo = serialNo + "   " + strLabel + " : " + holder.assetBO.getVendorName();
        }
        if (assetTrackingHelper.SHOW_ASSET_MODEL) {
            strLabel = mContext.getResources().getString(R.string.model);
            if (mBModel.labelsMasterHelper
                    .applyLabels((Object) "asset_model") != null)
                strLabel = mBModel.labelsMasterHelper
                        .applyLabels((Object) "asset_model");
            serialNo = serialNo + "   " + strLabel + " : " + holder.assetBO.getModelName();
        }
        if (assetTrackingHelper.SHOW_ASSET_TYPE) {
            strLabel = mContext.getResources().getString(R.string.type);
            if (mBModel.labelsMasterHelper
                    .applyLabels((Object) "asset_type") != null)
                strLabel = mBModel.labelsMasterHelper
                        .applyLabels((Object) "asset_type");
            serialNo = serialNo + "   " + strLabel + " : " + holder.assetBO.getAssetType();
        }
        if (assetTrackingHelper.SHOW_ASSET_CAPACITY) {
            strLabel = mContext.getResources().getString(R.string.capacity);
            if (mBModel.labelsMasterHelper
                    .applyLabels((Object) "asset_capacity") != null)
                strLabel = mBModel.labelsMasterHelper
                        .applyLabels((Object) "asset_capacity");
            serialNo = serialNo + "   " + strLabel + " : " + holder.assetBO.getCapacity();
        }

        holder.serialNoTV.setText(serialNo);

        holder.mInstallDate
                .setText((holder.assetBO.getInstallDate() == null) ? DateTimeUtils
                        .convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat) :DateTimeUtils
                        .convertFromServerDateToRequestedFormat(holder.assetBO.getInstallDate(), outPutDateFormat));
        holder.mServiceDate
                .setText((holder.assetBO.getServiceDate() == null) ? DateTimeUtils
                        .convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat) :DateTimeUtils
                        .convertFromServerDateToRequestedFormat(holder.assetBO.getServiceDate(), outPutDateFormat));

        if (holder.assetBO.getAvailQty() > 0) {

            holder.reason1Spin.setEnabled(false);
            if ((holder.assetBO.getImgName() != null)
                    && (!holder.assetBO.getImgName().isEmpty())) {
                holder.photoBTN.setEnabled(true);
                setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);
            } else {
                holder.photoBTN.setEnabled(true);
                holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_photo_camera_blue_24dp, null));
            }

            holder.reason1Spin.setSelection(0);
            holder.mConditionSpin.setEnabled(true);
            holder.mConditionSpin.setSelection(mAssetPresenter
                    .getItemIndex(holder.assetBO.getConditionID(),
                            mAssetPresenter.getAssetConditionList(), false));

            holder.mInstallDate.setEnabled(holder.assetBO.getmLastInstallDate().isEmpty());
            holder.mServiceDate.setEnabled(true);

        } else {

            holder.reason1Spin.setEnabled(true);
            holder.photoBTN.setEnabled(false);
            holder.photoBTN.setImageDrawable(ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.ic_photo_camera_grey_24dp, null));
            holder.mConditionSpin.setEnabled(false);
            holder.mConditionSpin.setSelection(0);
            holder.mInstallDate.setEnabled(false);
            holder.mServiceDate.setEnabled(false);
            holder.assetBO.setImageName("");
            holder.assetBO.setImgName("");
           /* holder.assetBO.setInstallDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));
            holder.assetBO.setServiceDate(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));*/

            if (!holder.assetBO.getmLastInstallDate().isEmpty())
                holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.assetBO.getInstallDate(), outPutDateFormat));
            else
                holder.mInstallDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));

            if (!holder.assetBO.getServiceDate().isEmpty())
                holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(holder.assetBO.getServiceDate(), outPutDateFormat));
            else
                holder.mServiceDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), outPutDateFormat));

        }


        if (!assetTrackingHelper.SHOW_ASSET_REASON) {
            row.findViewById(R.id.reason_ll).setVisibility(View.GONE);
            //holder.reason1Spin.setVisibility(View.GONE);
        }
        if (!assetTrackingHelper.SHOW_ASSET_CONDITION) {
            row.findViewById(R.id.condition_ll).setVisibility(View.GONE);
            //holder.mConditionSpin.setVisibility(View.GONE);
        }
        if (!assetTrackingHelper.SHOW_ASSET_INSTALL_DATE) {
            holder.mInstallDate.setVisibility(View.GONE);
            holder.llInstallDate.setVisibility(View.GONE);
        }
        if (!assetTrackingHelper.SHOW_ASSET_SERVICE_DATE) {
            holder.mServiceDate.setVisibility(View.GONE);
            holder.ll_service_date.setVisibility(View.GONE);
        }

        if (!assetTrackingHelper.SHOW_ASSET_PHOTO) {
            holder.photoBTN.setVisibility(View.GONE);
        }


        if (!assetTrackingHelper.SHOW_ASSET_EXECUTED) {
            holder.execQtyLL.setVisibility(View.GONE);
        }

        if ((holder.assetBO.getImgName() != null)
                && (!holder.assetBO.getImgName().isEmpty())
        ) {
            setPictureToImageView(holder.assetBO.getImgName(), holder.photoBTN);

        } else {
            if (!holder.photoBTN.isEnabled())
                holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_photo_camera_grey_24dp));
            else
                holder.photoBTN.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_photo_camera_blue_24dp));
        }


        if (holder.assetBO.getExecutorQty() == 1) {
            holder.execQtyRB.setChecked(true);
        } else {
            holder.execQtyRB.setChecked(false);
        }


        if (assetTrackingHelper.ASSET_RESTRICT_MANUAL_AVAILABILITY_CHECK) {
            holder.availQtyRB.setEnabled(false);

            if (assetTrackingHelper.SHOW_ASSET_BARCODE
                    && (holder.assetBO.getScanComplete() == 1)) {
                holder.availQtyRB.setChecked(true);
            } else if (holder.assetBO.getAvailQty() == 1) {
                holder.availQtyRB.setChecked(true);
            } else {
                holder.availQtyRB.setChecked(false);
            }

            if ((assetTrackingHelper.SHOW_ASSET_BARCODE &&
                    (holder.assetBO.getSerialNo() == null || holder.assetBO.getSerialNo().trim().isEmpty())) ||
                    (holder.assetBO.getNFCTagId() == null || holder.assetBO.getNFCTagId().trim().isEmpty())) {
                holder.availQtyRB.setEnabled(true);
            }


        } else {
            holder.availQtyRB.setEnabled(true);

            if (holder.assetBO.getAvailQty() == 1) {
                holder.availQtyRB.setChecked(true);
            } else {
                holder.availQtyRB.setChecked(false);
            }
        }


        return row;
    }

    class ViewHolder {
        SerializedAssetBO assetBO;
        TextView assetNameTV;
        TextView serialNoTV;
        Spinner reason1Spin;
        Spinner mConditionSpin;
        ImageView photoBTN;
        Button mInstallDate;
        LinearLayout llInstallDate;
        Button mServiceDate;
        LinearLayout ll_service_date;
        CheckBox availQtyRB;
        LinearLayout availQtyLL;
        ImageButton audit;
        CheckBox execQtyRB;
        LinearLayout execQtyLL;
        LinearLayout assetDetailLL;
    }

    private void setPictureToImageView(String imageName, ImageView imageView) {

        Glide.with(mContext).load(
                mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                        + "/" + DataMembers.photoFolderName + "/" + imageName)
                .centerCrop()
                .placeholder(R.drawable.ic_photo_camera_blue_24dp)
                .error(R.drawable.no_image_available)
                .override(35, 20)
                .transform(mBModel.circleTransform)
                .into(imageView);
    }

    /**
     * Method to check already image captured or not if Already captured, it
     * will show Alert Dialog In Alert Dialog, if click yes,remove image in
     * sdcard and retake photo. If click No, Alert Dialog dismiss
     *
     * @param mAssetId        AssetId
     * @param imageNameStarts imageName
     * @param imageSrc        imagePath
     */
    private void showFileDeleteAlertWithImage(final String mAssetId,
                                              final String imageNameStarts,
                                              final String imageSrc) {

        final CommonDialog commonDialog = new CommonDialog(mContext.getApplicationContext(),
                mContext,
                "",
                mContext.getResources().getString(R.string.word_already) + " " + 1 + " " + mContext.getResources().getString(R.string.word_photocaptured_delete_retake),
                true,
                mContext.getResources().getString(R.string.yes),
                mContext.getResources().getString(R.string.no),
                false,
                mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageSrc, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                        mAssetPresenter.removeExistingImage(mAssetId, imageNameStarts, photoPath);

                        Intent intent = new Intent(mContext,
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = photoPath + "/" + imageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        mFragment.startActivityForResult(intent, CAMERA_REQUEST_CODE);
                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }


    public void updateDate(Date date, String tag) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar selectedDate = new GregorianCalendar(year, month, day);
        SerializedAssetBO bo = (SerializedAssetBO) dateBtn.getTag();

        if (TAG_DATE_PICKER_INSTALLED.equals(tag)) {

            if (selectedDate.after(Calendar.getInstance())) {
                Toast.makeText(mContext,
                        R.string.future_date_not_allowed,
                        Toast.LENGTH_SHORT).show();
                bo.setInstallDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        Calendar.getInstance().getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                        .getInstance().getTime(), outPutDateFormat));
            } else {

                bo.setInstallDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }

        } else if (TAG_DATE_PICKER_SERVICED.equals(tag)) {

            if (bo.getInstallDate() != null
                    && bo.getInstallDate().length() > 0) {
                Date mInstallDate = DateTimeUtils.convertStringToDateObject(
                        bo.getInstallDate(), outPutDateFormat);
                if (mInstallDate != null && selectedDate.getTime() != null
                        && mInstallDate.after(selectedDate.getTime())) {
                    Toast.makeText(mContext,
                            R.string.servicedate_set_after_installdate,
                            Toast.LENGTH_SHORT).show();
                } else if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(mContext,
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    bo.setServiceDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                            Calendar.getInstance().getTime(), outPutDateFormat));
                    dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    bo.setServiceDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            } else {

                bo.setServiceDate(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
                dateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), outPutDateFormat));
            }
        }

    }
}
