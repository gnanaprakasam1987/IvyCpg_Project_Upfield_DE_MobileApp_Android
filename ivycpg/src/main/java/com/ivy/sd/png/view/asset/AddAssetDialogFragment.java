package com.ivy.sd.png.view.asset;

import android.app.DatePickerDialog;
import android.content.Context;
import android.os.Bundle;
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
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Created by rajkumar.s on 3/28/2017.
 *
 */

public class AddAssetDialogFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    BusinessModel mBModel;
    private static final String SELECT = "-Select-";
    private Spinner mAsset;
    private Spinner mBrand;
    private EditText mSNO;
    private static Button btnAddInstallDate;
    private int mYear;
    private int mMonth;
    private int mDay;
    private final AssetTrackingBO assetBo = new AssetTrackingBO();
    Button btnSave, btnCancel;
    private String append = "";
    EditText edittext;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if(getDialog().getWindow()!=null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }

        setCancelable(false);

        View view = inflater.inflate(R.layout.add_asset_dailog, container);

        Context context = getActivity();
        mBModel = (BusinessModel) context.getApplicationContext();

        mAsset = (Spinner) view.findViewById(R.id.spinner_asset);
        mBrand = (Spinner) view.findViewById(R.id.spinner_brand);
        btnAddInstallDate = (Button) view.findViewById(R.id.date_button);
        mSNO = (EditText) view.findViewById(R.id.etxt_sno);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
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
        if(window!=null) {
            lp.copyFrom(window.getAttributes());
            window.setAttributes(lp);
        }


    }

    /**
     * Preparing screen
     */
    private void loadData() {

        mBModel.assetTrackingHelper.downloadAssetsPosm("MENU_ASSET");

        Vector mPOSMList = mBModel.assetTrackingHelper.getAssetPosmNames();

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
                                       int arg2, long arg3) {

                mBModel.assetTrackingHelper
                        .downloadAssetBrand(mBModel.assetTrackingHelper
                                .getAssetPosmIds(mAsset.getSelectedItem()
                                        .toString()));

                loadBrandData();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        String todayDate = DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);


        btnAddInstallDate.setText(todayDate);


        btnAddInstallDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Button b = (Button) v;
                if (b == btnAddInstallDate) {

                    final Calendar c = Calendar.getInstance();
                    mYear = c.get(Calendar.YEAR);
                    mMonth = c.get(Calendar.MONTH);
                    mDay = c.get(Calendar.DAY_OF_MONTH);

                    // Launch Date Picker Dialog
                    DatePickerDialog dpd = new DatePickerDialog(
                            getActivity(),
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view,
                                                      int year, int monthOfYear,
                                                      int dayOfMonth) {
                                    Calendar selectedDate = new GregorianCalendar(
                                            year, monthOfYear, dayOfMonth);
                                    btnAddInstallDate.setText(DateUtil
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
                                        btnAddInstallDate.setText(DateUtil
                                                .convertDateObjectToRequestedFormat(
                                                        mCurrentCalendar.getTime(),
                                                        ConfigurationMasterHelper.outDateFormat));

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
            }
        });
    }

    /**
     * Load brands
     */
    private void loadBrandData() {
        ArrayAdapter<CharSequence> mAssetBrandsAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        mAssetBrandsAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        Vector mBrand = mBModel.assetTrackingHelper.getAssetBrandNames();
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

        assetBo.setPOSM(mBModel.assetTrackingHelper.getAssetPosmIds(mAsset
                .getSelectedItem().toString()));

        if (!mBrand.getSelectedItem().toString()
                .equals(SELECT))
            assetBo.setBrand(mBModel.assetTrackingHelper.getAssetBrandIds(mBrand
                    .getSelectedItem().toString()));
        else
            assetBo.setBrand("0");

        assetBo.setNewInstallDate(btnAddInstallDate.getText().toString());

        assetBo.setSNO(mSNO.getText().toString());

        mBModel.assetTrackingHelper.setAssetTrackingBO(assetBo);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_save) {

            try {
                if (!mAsset.getSelectedItem().toString()
                        .equals(SELECT)

                        && !mSNO.getText().toString().equals("")) {
                    if (!mBModel.assetTrackingHelper
                            .isExistingRetailerSno(mSNO.getText()
                                    .toString())) {
                        setAddAssetDetails();
                        mBModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                        mBModel.assetTrackingHelper
                                .saveAssetAddAndDeleteDetails("MENU_ASSET");
                        Toast.makeText(
                                getActivity(),
                                getResources()
                                        .getString(
                                                R.string.saved_successfully),
                                Toast.LENGTH_SHORT).show();
                        dismiss();


                    } else {
                        Toast.makeText(
                                getActivity(),
                                getResources()
                                        .getString(
                                                R.string.serial_number_already_exists),
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
        } else if (view.getId() == R.id.btn_cancel) {
            dismiss();
        }
    }

    /**
     * Key pad click event
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
