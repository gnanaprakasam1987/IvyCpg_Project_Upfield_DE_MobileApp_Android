package com.ivy.cpg.view.asset;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Created by rajkumar.s on 3/28/2017.
 * This dialog is used to add new asset.
 */

public class AddAssetDialogFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    BusinessModel mBModel;

    private static final String SELECT = "-Select-";
    private Spinner mAsset;
    private Spinner mBrand;
    private EditText mSNO;
    private Button btnAddInstallDate;
    EditText edittext;

    private int mYear;
    private int mMonth;
    private int mDay;
    Button btnSave, btnCancel;
    private String append = "";
    private TextView txtSerialNo;
    private String serialNoTag;
    private final AssetTrackingBO assetBo = new AssetTrackingBO();
    AssetTrackingHelper assetTrackingHelper;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog().getWindow() != null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        setCancelable(false);

        View view = inflater.inflate(R.layout.add_asset_dailog, container);

        Context context = getActivity();
        mBModel = (BusinessModel) context.getApplicationContext();
        assetTrackingHelper = AssetTrackingHelper.getInstance(context);

        txtSerialNo = view.findViewById(R.id.label_scan);
        serialNoTag = getResources().getString(R.string.serial_no);
        ((TextView) view.findViewById(R.id.label_scan)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        if (mBModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.label_scan).getTag()) != null) {
            serialNoTag = mBModel.labelsMasterHelper.applyLabels(view.findViewById(R.id.label_scan).getTag());
            txtSerialNo.setText(serialNoTag);
        }

        mAsset = view.findViewById(R.id.spinner_asset);
        mBrand = view.findViewById(R.id.spinner_brand);
        btnAddInstallDate = view.findViewById(R.id.date_button);
        mSNO = view.findViewById(R.id.etxt_sno);
        btnSave = view.findViewById(R.id.btn_save);
        btnCancel = view.findViewById(R.id.btn_cancel);
        ImageView imageView_barcode_scan = view.findViewById(R.id.imageView_barcode_scan);
        imageView_barcode_scan.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        loadData();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        Window window = getDialog().getWindow();
        if (window != null) {
            //lp.copyFrom(window.getAttributes()); cmd for device alignment issue
            window.setAttributes(lp);
        }


    }

    /**
     * Preparing screen
     */
    private void loadData() {

        assetTrackingHelper.downloadAssetsPosm(getContext().getApplicationContext(), "MENU_ASSET");

        Vector mPOSMList = assetTrackingHelper.getAssetPosmNames();

        int siz = mPOSMList.size();

        ArrayAdapter<CharSequence> mAssetSpinAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        mAssetSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        mAssetSpinAdapter.add(SELECT);

        for (int k = 0; k < siz; ++k) {
            mAssetSpinAdapter.add(mPOSMList.elementAt(k).toString());

        }

        Commons.print("mAssetSpinAdapter" + mAssetSpinAdapter + ","
                + mAsset);
        mAsset.setAdapter(mAssetSpinAdapter);
        mAsset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {

                assetTrackingHelper
                        .downloadAssetBrand(getActivity().getApplicationContext(), assetTrackingHelper
                                .getAssetPosmIds(mAsset.getSelectedItem()
                                        .toString()));

                if (position != 0
                        && assetTrackingHelper.getAssetBrandNames().size() > 0) {
                    loadBrandData();
                } else {
                    if (position == 0 || assetTrackingHelper.getAssetBrandNames().size() == 0)
                        ((TextView) getView().findViewById(R.id.brand_spinner_txt)).setVisibility(View.GONE);
                    mBrand.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        String todayDate = DateTimeUtils.convertFromServerDateToRequestedFormat(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);


        btnAddInstallDate.setText(todayDate);
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        btnAddInstallDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Button b = (Button) v;
                if (b == btnAddInstallDate) {

                    // Launch Date Picker Dialog
                    DatePickerDialog dpd = new DatePickerDialog(
                            getActivity(), R.style.DatePickerDialogStyle,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view,
                                                      int year, int monthOfYear,
                                                      int dayOfMonth) {
                                    mYear = year;
                                    mMonth = monthOfYear;
                                    mDay = dayOfMonth;
                                    Calendar selectedDate = new GregorianCalendar(
                                            year, monthOfYear, dayOfMonth);
                                    btnAddInstallDate.setText(DateTimeUtils
                                            .convertDateObjectToRequestedFormat(
                                                    selectedDate.getTime(),
                                                    ConfigurationMasterHelper.outDateFormat));
                                    Calendar mCurrentCalendar = Calendar
                                            .getInstance();
                                    if (selectedDate.after(mCurrentCalendar)) {
                                        Toast.makeText(
                                                getActivity(),
                                                R.string.future_date_not_allowed,
                                                Toast.LENGTH_SHORT).show();
                                        btnAddInstallDate.setText(DateTimeUtils
                                                .convertDateObjectToRequestedFormat(
                                                        mCurrentCalendar.getTime(),
                                                        ConfigurationMasterHelper.outDateFormat));

                                        mYear = mCurrentCalendar.get(Calendar.YEAR);
                                        mMonth = mCurrentCalendar.get(Calendar.MONTH);
                                        mDay = mCurrentCalendar.get(Calendar.DAY_OF_MONTH);

                                    }

                                }
                            }, mYear, mMonth, mDay);
                    dpd.show();
                }

            }
        });

        mSNO.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() > 0)
                    mSNO.setSelection(s.toString().length());
            }
        });
    }

    /**
     * Load brands
     */
    private void loadBrandData() {
        ((TextView) getView().findViewById(R.id.brand_spinner_txt)).setVisibility(View.VISIBLE);
        mBrand.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> mAssetBrandsAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        mAssetBrandsAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        Vector mBrand = assetTrackingHelper.getAssetBrandNames();
        if (mBrand == null || mBrand.size() < 1) {
            this.mBrand.setAdapter(null);
            mAssetBrandsAdapter.add(SELECT);
            this.mBrand.setAdapter(mAssetBrandsAdapter);
            return;
        }
        int mBrandSize = mBrand.size();
        if (mBrandSize == 0)
            return;

        mAssetBrandsAdapter.add(SELECT);

        for (int i = 0; i < mBrandSize; ++i) {

            mAssetBrandsAdapter.add(mBrand.elementAt(i).toString());

        }
        this.mBrand.setAdapter(mAssetBrandsAdapter);
    }

    /**
     * Set values for adding asset
     */
    private void setAddAssetDetails() {

        assetBo.setPOSM(assetTrackingHelper.getAssetPosmIds(mAsset
                .getSelectedItem().toString()));

        if (mBrand.getSelectedItem() != null) {
            if (!mBrand.getSelectedItem().toString()
                    .equals(SELECT))
                assetBo.setBrand(assetTrackingHelper.getAssetBrandIds(mBrand
                        .getSelectedItem().toString()));
            else
                assetBo.setBrand("0");
        } else
            assetBo.setBrand("0");

        assetBo.setNewInstallDate(btnAddInstallDate.getText().toString());

        assetBo.setSNO(mSNO.getText().toString());

        assetTrackingHelper.setAssetTrackingBO(assetBo);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_save) {

            try {
                if (!mAsset.getSelectedItem().toString()
                        .equals(SELECT)
                        && !mBrand.getSelectedItem().toString()
                        .equals(SELECT)) {
                    if(mSNO.getText().toString().trim().equals("")){
                        Toast.makeText(
                                getActivity(),
                                getResources()
                                        .getString(
                                                R.string.enter) + " " + serialNoTag,
                                Toast.LENGTH_SHORT).show();
                    }
                    if (!assetTrackingHelper
                            .getUniqueSerialNo(mSNO.getText()
                                    .toString())) {
                        setAddAssetDetails();
                        //mBModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                        assetTrackingHelper
                                .saveAssetAddAndDeleteDetails(getContext().getApplicationContext(), "MENU_ASSET");
                        Toast.makeText(
                                getActivity(),
                                getResources()
                                        .getString(
                                                R.string.saved_successfully),
                                Toast.LENGTH_SHORT).show();
                        dismiss();


                    } else {
                        Toast.makeText(
                                getActivity(),serialNoTag + " " + getResources().getString(R.string.already_exist),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(
                            getActivity(),
                            getResources().getString(
                                    R.string.no_assets_exists),
                            Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.no_assets_exists),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.imageView_barcode_scan) {
            scanBarCode();
        } else if (view.getId() == R.id.btn_cancel) {
            dismiss();
        }
    }

    private void scanBarCode() {
        {
            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        AddAssetDialogFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                    } else {
                        mSNO.setText(result.getContents());
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    /**
     * Key pad click event
     *
     * @param vw Selected View
     */
    public void numberPressed(View vw) {
        if (edittext == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(edittext.getText()
                        .toString());
                s = s / 10;
                String strQty = s + "";
                edittext.setText(strQty);
            } else {
                if (getView() != null) {
                    Button ed = (Button) getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    /**
     * Set values to the selected view
     */
    private void eff() {
        String s = edittext.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQty = edittext.getText() + append;
            edittext.setText(strQty);
        } else
            edittext.setText(append);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }
}
