package com.ivy.sd.png.view;

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
import com.ivy.sd.png.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Created by rajkumar.s on 3/28/2017.
 */

public class AddAssetDialogFragment extends DialogFragment implements View.OnClickListener, TextView.OnEditorActionListener {

    Button btnAdd;
    BusinessModel bmodel;
    private static final String SELECT = "-Select-";
    private Spinner masset;
    private Spinner mbrand;
    private EditText mSNO;
    private static Button addinstalldate;
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

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        View view = inflater.inflate(R.layout.add_asset_dailog, container);
        Context context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();

        masset = (Spinner) view.findViewById(R.id.spinner_asset);
        mbrand = (Spinner) view.findViewById(R.id.spinner_brand);

        addinstalldate = (Button) view.findViewById(R.id.date_button);
        mSNO = (EditText) view.findViewById(R.id.etxt_sno);
        // mSNO.setInputType(InputType.TYPE_CLASS_TEXT);
        //   mSNO.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        // mSNO.setKeyListener(DigitsKeyListener.getInstance(false,true));
//        mSNO.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View view, MotionEvent motionEvent) {
//                edittext = mSNO;
//                edittext.setTag(mSNO);
//                int inType = mSNO.getInputType();
//                mSNO.setInputType(InputType.TYPE_NULL);
//                mSNO.onTouchEvent(motionEvent);
//                mSNO.setInputType(inType);
//                mSNO.selectAll();
//                mSNO.requestFocus();
//                return true;
//            }
//        });
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnCancel = (Button) view.findViewById(R.id.btn_cancel);
        btnCancel.setOnClickListener(this);
        btnSave.setOnClickListener(this);

        loadeddata();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        //
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getDialog().getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    private void loadeddata() {

        bmodel.assetTrackingHelper.downloadAssetsPosm("MENU_ASSET");

        Vector vposm = bmodel.assetTrackingHelper.getAssetPosmNames();

        int siz = vposm.size();

        ArrayAdapter<CharSequence> mAssetSpinAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        mAssetSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        mAssetSpinAdapter.add(SELECT);
        Commons.print("mposmsiz==" + vposm.size());

        for (int k = 0; k < siz; ++k) {
            mAssetSpinAdapter.add(vposm.elementAt(k).toString());

        }

        Commons.print("mAssetSpinAdapter" + mAssetSpinAdapter + ","
                + masset);
        masset.setAdapter(mAssetSpinAdapter);
        masset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int arg2, long arg3) {

                Commons.print("posmid="
                        + bmodel.assetTrackingHelper.getAssetPosmIds(masset
                        .getSelectedItem().toString()));
                bmodel.assetTrackingHelper
                        .downloadAssetBrand(bmodel.assetTrackingHelper
                                .getAssetPosmIds(masset.getSelectedItem()
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


        addinstalldate.setText(todayDate);


        addinstalldate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Button b = (Button) v;
                if (b == addinstalldate) {

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
                                    addinstalldate.setText(DateUtil
                                            .convertDateObjectToRequestedFormat(
                                                    selectedDate.getTime(),
                                                    ConfigurationMasterHelper.outDateFormat));
                                    Calendar currentcal = Calendar
                                            .getInstance();
                                    if (selectedDate.after(currentcal)) {
                                        Toast.makeText(
                                                getActivity(),
                                                R.string.future_date_not_allowed,
                                                Toast.LENGTH_SHORT).show();
                                        addinstalldate.setText(DateUtil
                                                .convertDateObjectToRequestedFormat(
                                                        currentcal.getTime(),
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

    // check from here
    private void loadBrandData() {
        ArrayAdapter<CharSequence> massetbrandsadapter = new ArrayAdapter<>(
                getActivity(), R.layout.spinner_bluetext_layout);
        massetbrandsadapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        Vector vbrand = bmodel.assetTrackingHelper.getAssetBrandNames();
        if (vbrand == null || vbrand.size() < 1) {
            mbrand.setAdapter(null);
            massetbrandsadapter.add(SELECT);
            mbrand.setAdapter(massetbrandsadapter);
            return;
        }
        int vbrandsiz = vbrand.size();
        if (vbrandsiz == 0)
            return;

        massetbrandsadapter.add(SELECT);

        for (int i = 0; i < vbrandsiz; ++i) {

            massetbrandsadapter.add(vbrand.elementAt(i).toString());

        }
        mbrand.setAdapter(massetbrandsadapter);
    }

    private void setAddAssetDetails() {

        assetBo.setPOSM(bmodel.assetTrackingHelper.getAssetPosmIds(masset
                .getSelectedItem().toString()));

        if (!mbrand.getSelectedItem().toString()
                .equals(SELECT))
            assetBo.setBrand(bmodel.assetTrackingHelper.getAssetBrandIds(mbrand
                    .getSelectedItem().toString()));
        else
            assetBo.setBrand("0");

        assetBo.setNewInstallDate(addinstalldate.getText().toString());

        assetBo.setSNO(mSNO.getText().toString());

        bmodel.assetTrackingHelper.setAssetTrackingBO(assetBo);

    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_save) {

            try {
                if (!masset.getSelectedItem().toString()
                        .equals(SELECT)

                        && !mSNO.getText().toString().equals("")) {
                    if (!bmodel.assetTrackingHelper
                            .isExistingRetailerSno(mSNO.getText()
                                    .toString())) {
                        setAddAssetDetails();
                        bmodel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                        bmodel.assetTrackingHelper
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

    public void numberPressed(View vw) {
        if (edittext == null) {
            bmodel.showAlert(
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
