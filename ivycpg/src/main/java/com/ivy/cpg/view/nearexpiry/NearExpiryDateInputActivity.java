package com.ivy.cpg.view.nearexpiry;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.net.ParseException;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;

public class NearExpiryDateInputActivity extends IvyBaseActivityNoActionBar implements
        View.OnClickListener{

    private BusinessModel mBModel;
    private EditText QUANTITY;

    private Button date1;
    private EditText CA1;
    private EditText PC1;
    private EditText OU1;
    private EditText BACTHNO1;
    private Button date2;
    private EditText CA2;
    private EditText PC2;
    private EditText OU2;
    private EditText BACTHNO2;
    private Button date3;
    private EditText CA3;
    private EditText PC3;
    private EditText OU3;
    private EditText BACTHNO3;
    private Button date4;
    private EditText CA4;
    private EditText PC4;
    private EditText OU4;
    private EditText BACTHNO4;
    private Button date5;
    private EditText CA5;
    private EditText PC5;
    private EditText OU5;
    private EditText BACTHNO5;
    private Button date6;
    private EditText CA6;
    private EditText PC6;
    private EditText OU6;
    private EditText BACTHNO6;
    private String append = "";
    private ProductMasterBO mSKUBO;

    private int mYear;
    private int mMonth;
    private int mDay;
    private String date = "";
    private String datec1;
    private String datec2;
    private String datec3;
    private String datec4;
    private String datec5;
    private String datec6;
    private String mOrderTitle = " ";

    NearExpiryTrackingHelper mNearExpiryHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_nearexpiry);

        mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);

        mBModel = (BusinessModel) getApplicationContext();
        Button btn_ok = findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(this);
        ListView list = findViewById(R.id.list);
        ExpiryAdapter adapter = new ExpiryAdapter();
        list.setAdapter(adapter);

        Bundle mArgs = getIntent().getExtras();
        final String pid = mArgs!=null?mArgs.getString("PID"):"";

        Toolbar toolbar = findViewById(R.id.toolbar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        Vector<ProductMasterBO> items = mBModel.productHelper.getProductMaster();

        for (int i = 0; i < items.size(); ++i) {
            ProductMasterBO sku = items.elementAt(i);
            if (sku.getProductID().equals(pid)) {
                mSKUBO = sku;
                break;
            }
        }

        if (toolbar != null ) {

            setSupportActionBar(toolbar);

            if (getSupportActionBar() != null) {

                getSupportActionBar().setDisplayShowTitleEnabled(false);

                if ((getIntent().getBooleanExtra("PreVisit",false)))
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                else
                    getSupportActionBar().setDisplayHomeAsUpEnabled(false);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            if (mSKUBO != null && mSKUBO.getProductName() != null)
                setScreenTitle(mSKUBO.getProductName());
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s)) {
            s = s + append;
            QUANTITY.setText(s);
        } else
            QUANTITY.setText(append);

    }

    @SuppressLint("ResourceType")
    public void numberPressed(View vw) {

        if (QUANTITY == null) {
            Toast.makeText(NearExpiryDateInputActivity.this,
                    getResources().getString(R.string.please_select_item),
                    Toast.LENGTH_SHORT).show();
        } else {
            int id = vw.getId();
            if (id == R.id.calcone) {
                append = "1";
                eff();
            } else if (id == R.id.calctwo) {
                append = "2";
                eff();
            } else if (id == R.id.calcthree) {
                append = "3";
                eff();
            } else if (id == R.id.calcfour) {
                append = "4";
                eff();
            } else if (id == R.id.calcfive) {
                append = "5";
                eff();
            } else if (id == R.id.calcsix) {
                append = "6";
                eff();
            } else if (id == R.id.calcseven) {
                append = "7";
                eff();
            } else if (id == R.id.calceight) {
                append = "8";
                eff();
            } else if (id == R.id.calcnine) {
                append = "9";
                eff();
            } else if (id == R.id.calczero) {
                append = "0";
                eff();
            } else if (id == R.id.calcdel) {
                if (QUANTITY.getId() == 1) {
                    String s = QUANTITY.getText().toString();
                    if (!s.isEmpty()) {
                        s = s.substring(0, s.length() - 1);

                        if (s.length() == 0) {
                            s = "0";
                        }
                    }
                    QUANTITY.setText(s);

                } else {

                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    String strQty = Integer.toString(s);
                    QUANTITY.setText(strQty);
                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (!s.contains("."))
                    QUANTITY.append(".");
            }
        }
    }

    @Override
    public void onClick(View v) {
        if (R.id.btn_ok == v.getId()) {
            boolean isflag = true;
            for (NearExpiryDateBO nearExpiryDateBO : mSKUBO.getLocations().get(mNearExpiryHelper.mSelectedLocationIndex)
                    .getNearexpiryDate()) {
                if (!nearExpiryDateBO.getNearexpPC().equals("0")
                        || !nearExpiryDateBO.getNearexpCA().equals("0")
                        || !nearExpiryDateBO.getNearexpOU().equals("0")) {

                    if (nearExpiryDateBO.getDate().equals("")) {
                        Toast.makeText(NearExpiryDateInputActivity.this,
                                getResources().getString(R.string.select_date),
                                Toast.LENGTH_SHORT).show();
                        isflag = false;
                        break;

                    }
                } else {
                    if (!nearExpiryDateBO.getDate().equals(""))
                        nearExpiryDateBO.setDate("");
                }
            }
            if (isflag) {
                setResult(1);
                finish();
            }
        }
    }

    private static String getMaxdays() {
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
        Calendar c = Calendar.getInstance();

        for (int i = 1; i <= 5; i++) {
            c.add(Calendar.MONTH, 1);

        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int maxDay;

        // Getting Maximum day for Given Month
        maxDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);

        c.set(year, month, maxDay);

        return formatter.format(c.getTime());
    }

    private boolean checkToDate(String date, String selectedDay) {

        Calendar today = Calendar.getInstance();
        Calendar selectday = Calendar.getInstance();
        Calendar maxday = Calendar.getInstance();

        Calendar day1 = Calendar.getInstance();
        Calendar day2 = Calendar.getInstance();
        Calendar day3 = Calendar.getInstance();
        Calendar day4 = Calendar.getInstance();
        Calendar day5 = Calendar.getInstance();
        Calendar day6 = Calendar.getInstance();

        today.add(Calendar.DAY_OF_MONTH, 0);

        String maxDayString = getMaxdays();

        Date todayDate = today.getTime();
        Date maxDate = null;
        Date selectDate = null;
        Date selectDate1 = null;
        Date selectDate2 = null;
        Date selectDate3 = null;
        Date selectDate4 = null;
        Date selectDate5 = null;
        Date selectDate6 = null;

        SimpleDateFormat formatter;

        try {
            formatter = new SimpleDateFormat("MM/dd/yyyy", Locale.getDefault());
            selectDate = formatter.parse(selectedDay);

            if (!datec1.equals(""))
                selectDate1 = formatter.parse(datec1);
            if (!datec2.equals(""))
                selectDate2 = formatter.parse(datec2);
            if (!datec3.equals(""))
                selectDate3 = formatter.parse(datec3);
            if (!datec4.equals(""))
                selectDate4 = formatter.parse(datec4);
            if (!datec5.equals(""))
                selectDate5 = formatter.parse(datec5);
            if (!datec6.equals(""))
                selectDate6 = formatter.parse(datec6);

            String tday = formatter.format(todayDate);
            todayDate = formatter.parse(tday);
            maxDate = formatter.parse(maxDayString);

        } catch (java.text.ParseException | ParseException e) {
            Commons.printException("" + e);
        }
        selectday.setTime(selectDate);
        today.setTime(todayDate);
        maxday.setTime(maxDate);

        if (selectDate1 != null)
            day1.setTime(selectDate1);
        if (selectDate2 != null)
            day2.setTime(selectDate2);
        if (selectDate3 != null)
            day3.setTime(selectDate3);
        if (selectDate4 != null)
            day4.setTime(selectDate4);
        if (selectDate5 != null)
            day5.setTime(selectDate5);
        if (selectDate6 != null)
            day6.setTime(selectDate6);

        int m1 = -1;
        int m2 = -1;
        int m3 = -1;
        int m4 = -1;
        int m5 = -1;
        int m6 = -1;

        long t = today.getTimeInMillis();
        long m = maxday.getTimeInMillis();
        long s = selectday.getTimeInMillis();
        int sMonth = selectDate.getMonth();

        if (selectDate1 != null)
            m1 = selectDate1.getMonth();

        if (selectDate2 != null)
            m2 = selectDate2.getMonth();

        if (selectDate3 != null)
            m3 = selectDate3.getMonth();

        if (selectDate4 != null)
            m4 = selectDate4.getMonth();

        if (selectDate5 != null)
            m5 = selectDate5.getMonth();

        if (selectDate6 != null)
            m6 = selectDate6.getMonth();

        if (t <= s && s <= m) {

            if ("date1".equals(date) && (m2 != sMonth) && (m3 != sMonth)
                    && (m4 != sMonth) && (m5 != sMonth)
                    && (m6 != sMonth)) {
                return true;
            }

            if ("date2".equals(date) && (m1 != sMonth) && (m3 != sMonth)
                    && (m4 != sMonth) && (m5 != sMonth)
                    && (m6 != sMonth)) {
                return true;
            }
            if ("date3".equals(date) && (m1 != sMonth) && (m2 != sMonth)
                    && (m4 != sMonth) && (m5 != sMonth)
                    && (m6 != sMonth)) {
                return true;
            }
            if ("date4".equals(date) && (m1 != sMonth) && (m2 != sMonth)
                    && (m3 != sMonth) && (m5 != sMonth)
                    && (m6 != sMonth)) {
                return true;
            }
            if ("date5".equals(date) && (m1 != sMonth) && (m2 != sMonth)
                    && (m3 != sMonth) && (m3 != sMonth)
                    && (m6 != sMonth)) {
                return true;
            }
            if ("date6".equals(date) && (m1 != sMonth) && (m2 != sMonth)
                    && (m3 != sMonth) && (m4 != sMonth)
                    && (m5 != sMonth)) {
                return true;
            }
        }
        return false;
    }

    private class ExpiryAdapter extends ArrayAdapter {

        public ExpiryAdapter() {
            super(NearExpiryDateInputActivity.this, R.layout.dialog_enter_expiry);
        }

        public int getCount() {
            return 1;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = LayoutInflater.from(getBaseContext());
                row = inflater.inflate(R.layout.dialog_enter_expiry, parent, false);
                date1 = row.findViewById(R.id.datePicker1);

                CA1 = row.findViewById(R.id.ca1);
                PC1 = row.findViewById(R.id.pc1);
                OU1 = row.findViewById(R.id.ou1);
                BACTHNO1 = row.findViewById(R.id.batchno1);

                TextView tv_piece = row.findViewById(R.id.tv_piece);
                TextView tv_case = row.findViewById(R.id.tv_case);
                TextView tv_outer = row.findViewById(R.id.tv_outer);

                date1.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(0).getDate());

                datec1 = mNearExpiryHelper
                        .changeMonthNameToNommddyyyy(mSKUBO
                                .getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .getNearexpiryDate().get(0).getDate());

                CA1.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(0).getNearexpCA());
                PC1.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(0).getNearexpPC());
                OU1.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(0).getNearexpOU());
                BACTHNO1.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(0).getBatchNo());

                date2 = row.findViewById(R.id.datePicker2);
                CA2 = row.findViewById(R.id.ca2);
                PC2 = row.findViewById(R.id.pc2);
                OU2 = row.findViewById(R.id.ou2);
                BACTHNO2 = row.findViewById(R.id.batchno2);

                TextView tv_piece2 = row.findViewById(R.id.tv_piece2);
                TextView tv_case2 = row.findViewById(R.id.tv_case2);
                TextView tv_outer2 = row.findViewById(R.id.tv_outer2);

                date2.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(1).getDate());
                datec2 = mNearExpiryHelper
                        .changeMonthNameToNommddyyyy(mSKUBO
                                .getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .getNearexpiryDate().get(1).getDate());

                CA2.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(1).getNearexpCA());
                PC2.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(1).getNearexpPC());
                OU2.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(1).getNearexpOU());
                BACTHNO2.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(1).getBatchNo());

                date3 = row.findViewById(R.id.datePicker3);
                CA3 = row.findViewById(R.id.ca3);
                PC3 = row.findViewById(R.id.pc3);
                OU3 = row.findViewById(R.id.ou3);
                BACTHNO3 = row.findViewById(R.id.batchno3);

                TextView tv_piece3 = row.findViewById(R.id.tv_piece3);
                TextView tv_case3 = row.findViewById(R.id.tv_case3);
                TextView tv_outer3 = row.findViewById(R.id.tv_outer3);

                date3.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(2).getDate());
                datec3 = mNearExpiryHelper
                        .changeMonthNameToNommddyyyy(mSKUBO
                                .getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .getNearexpiryDate().get(2).getDate());

                CA3.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(2).getNearexpCA());
                PC3.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(2).getNearexpPC());
                OU3.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(2).getNearexpOU());
                BACTHNO3.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(2).getBatchNo());

                date4 = row.findViewById(R.id.datePicker4);
                CA4 = row.findViewById(R.id.ca4);
                PC4 = row.findViewById(R.id.pc4);
                OU4 = row.findViewById(R.id.ou4);
                BACTHNO4 = row.findViewById(R.id.batchno4);

                TextView tv_piece4 = row.findViewById(R.id.tv_piece4);
                TextView tv_case4 = row.findViewById(R.id.tv_case4);
                TextView tv_outer4 = row.findViewById(R.id.tv_outer4);

                date4.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(3).getDate());

                datec4 = mNearExpiryHelper
                        .changeMonthNameToNommddyyyy(mSKUBO
                                .getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .getNearexpiryDate().get(3).getDate());
                CA4.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(3).getNearexpCA());
                PC4.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(3).getNearexpPC());
                OU4.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(3).getNearexpOU());
                BACTHNO4.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(3).getBatchNo());

                date5 = row.findViewById(R.id.datePicker5);
                CA5 = row.findViewById(R.id.ca5);
                PC5 = row.findViewById(R.id.pc5);
                OU5 = row.findViewById(R.id.ou5);
                BACTHNO5 = row.findViewById(R.id.batchno5);

                TextView tv_piece5 = row.findViewById(R.id.tv_piece5);
                TextView tv_case5 = row.findViewById(R.id.tv_case5);
                TextView tv_outer5 = row.findViewById(R.id.tv_outer5);

                date5.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(4).getDate());
                datec5 = mNearExpiryHelper
                        .changeMonthNameToNommddyyyy(mSKUBO
                                .getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .getNearexpiryDate().get(4).getDate());
                CA5.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(4).getNearexpCA());
                PC5.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(4).getNearexpPC());
                OU5.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(4).getNearexpOU());
                BACTHNO5.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(4).getBatchNo());

                date6 = row.findViewById(R.id.datePicker6);
                CA6 = row.findViewById(R.id.ca6);
                PC6 = row.findViewById(R.id.pc6);
                OU6 = row.findViewById(R.id.ou6);
                BACTHNO6 = row.findViewById(R.id.batchno6);

                TextView tv_piece6 = row.findViewById(R.id.tv_piece6);
                TextView tv_case6 = row.findViewById(R.id.tv_case6);
                TextView tv_outer6 = row.findViewById(R.id.tv_outer6);

                date6.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(5).getDate());
                datec6 = mNearExpiryHelper
                        .changeMonthNameToNommddyyyy(mSKUBO
                                .getLocations()
                                .get(mNearExpiryHelper.mSelectedLocationIndex)
                                .getNearexpiryDate().get(5).getDate());
                CA6.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(5).getNearexpCA());
                PC6.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(5).getNearexpPC());
                OU6.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(5).getNearexpOU());
                BACTHNO6.setText(mSKUBO.getLocations()
                        .get(mNearExpiryHelper.mSelectedLocationIndex)
                        .getNearexpiryDate().get(5).getBatchNo());

                if (!mBModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    row.findViewById(R.id.ll_piece).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_piece2).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_piece3).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_piece4).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_piece5).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_piece6).setVisibility(View.GONE);
                } else {
                    if (mBModel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.tv_piece).getTag()) != null) {
                        mOrderTitle = mBModel.labelsMasterHelper.applyLabels(row
                                .findViewById(R.id.tv_piece).getTag());
                        String title = mBModel.labelsMasterHelper.applyLabels(row
                                .findViewById(R.id.tv_piece).getTag());
                        tv_piece.setText(title);
                        tv_piece2.setText(title);
                        tv_piece3.setText(title);
                        tv_piece4.setText(title);
                        tv_piece5.setText(title);
                        tv_piece6.setText(title);
                    } else
                        mOrderTitle = "PC";
                }

                if (mSKUBO.getPcUomid() == 0 || !mSKUBO.isPieceMapped()) {
                    PC1.setEnabled(false);
                    PC2.setEnabled(false);
                    PC3.setEnabled(false);
                    PC4.setEnabled(false);
                    PC5.setEnabled(false);
                    PC6.setEnabled(false);
                } else {
                    PC1.setEnabled(true);
                    PC2.setEnabled(true);
                    PC3.setEnabled(true);
                    PC4.setEnabled(true);
                    PC5.setEnabled(true);
                    PC6.setEnabled(true);
                }


                if (!mBModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    row.findViewById(R.id.ll_outer).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_outer2).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_outer3).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_outer4).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_outer5).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_outer6).setVisibility(View.GONE);
                } else {
                    if (!" ".equals(mOrderTitle)) {
                        if (mBModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_outer).getTag()) != null) {
                            mOrderTitle = mOrderTitle + "," + mBModel.labelsMasterHelper.applyLabels(row
                                    .findViewById(R.id.tv_outer).getTag());
                            String title = mBModel.labelsMasterHelper.applyLabels(row
                                    .findViewById(R.id.tv_outer).getTag());
                            tv_outer.setText(title);
                            tv_outer2.setText(title);
                            tv_outer3.setText(title);
                            tv_outer4.setText(title);
                            tv_outer5.setText(title);
                            tv_outer6.setText(title);
                        } else
                            mOrderTitle = mOrderTitle + "," + "OU";
                    } else
                        mOrderTitle = "OU";
                }

                if (mSKUBO.getOuUomid() == 0 || !mSKUBO.isOuterMapped()) {
                    OU1.setEnabled(false);
                    OU2.setEnabled(false);
                    OU3.setEnabled(false);
                    OU4.setEnabled(false);
                    OU5.setEnabled(false);
                    OU6.setEnabled(false);
                } else {
                    OU1.setEnabled(true);
                    OU2.setEnabled(true);
                    OU3.setEnabled(true);
                    OU4.setEnabled(true);
                    OU5.setEnabled(true);
                    OU6.setEnabled(true);
                }


                if (!mBModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    row.findViewById(R.id.ll_case).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_case2).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_case3).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_case4).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_case5).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_case6).setVisibility(View.GONE);

                } else {
                    if (!" ".equals(mOrderTitle)) {
                        if (mBModel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.tv_case).getTag()) != null) {
                            mOrderTitle = mOrderTitle + "," + mBModel.labelsMasterHelper.applyLabels(row
                                    .findViewById(R.id.tv_case).getTag());
                            String title = mBModel.labelsMasterHelper.applyLabels(row
                                    .findViewById(R.id.tv_case).getTag());
                            tv_case.setText(title);
                            tv_case2.setText(title);
                            tv_case3.setText(title);
                            tv_case4.setText(title);
                            tv_case5.setText(title);
                            tv_case6.setText(title);
                        } else
                            mOrderTitle = mOrderTitle + "," + "CA";
                    } else
                        mOrderTitle = "CA";
                }

                if (mSKUBO.getCaseUomId() == 0 || !mSKUBO.isCaseMapped()) {
                    CA1.setEnabled(false);
                    CA2.setEnabled(false);
                    CA3.setEnabled(false);
                    CA4.setEnabled(false);
                    CA5.setEnabled(false);
                    CA6.setEnabled(false);
                } else {
                    CA1.setEnabled(true);
                    CA2.setEnabled(true);
                    CA3.setEnabled(true);
                    CA4.setEnabled(true);
                    CA5.setEnabled(true);
                    CA6.setEnabled(true);
                }
                if (!mNearExpiryHelper.SHOW_BATCH_NO) {
                    row.findViewById(R.id.ll_batchno1).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_batchno2).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_batchno3).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_batchno4).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_batchno5).setVisibility(View.GONE);
                    row.findViewById(R.id.ll_batchno6).setVisibility(View.GONE);
                }

                if (mBModel.configurationMasterHelper.IS_TEAMLEAD && mBModel.configurationMasterHelper.IS_AUDIT_USER) {
                    PC1.setEnabled(false);
                    PC2.setEnabled(false);
                    PC3.setEnabled(false);
                    PC4.setEnabled(false);
                    PC5.setEnabled(false);
                    PC6.setEnabled(false);

                    OU1.setEnabled(false);
                    OU2.setEnabled(false);
                    OU3.setEnabled(false);
                    OU4.setEnabled(false);
                    OU5.setEnabled(false);
                    OU6.setEnabled(false);

                    CA1.setEnabled(false);
                    CA2.setEnabled(false);
                    CA3.setEnabled(false);
                    CA4.setEnabled(false);
                    CA5.setEnabled(false);
                    CA6.setEnabled(false);

                    date1.setEnabled(false);
                    date2.setEnabled(false);
                    date3.setEnabled(false);
                    date4.setEnabled(false);
                    date5.setEnabled(false);
                    date6.setEnabled(false);

                }


                date1.setOnClickListener(new View.OnClickListener() {
                    @SuppressLint("NewApi")
                    @Override
                    public void onClick(View v) {

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                                Locale.ENGLISH);

                        String boDate;
                        if (date1.getText().toString().equals(""))
                            boDate = df.format(c.getTime());
                        else
                            boDate = date1.getText().toString();

                        String formatDate = mNearExpiryHelper
                                .changeMonthNameToNoyyyymmdd(boDate);

                        int day = SDUtil.convertToInt(formatDate.substring(8, 10));
                        int month = SDUtil.convertToInt(formatDate.substring(5, 7));
                        int year = SDUtil.convertToInt(formatDate.substring(0, 4));

                        mDay = day;
                        mMonth = month - 1;
                        mYear = year;

                        DatePickerDialog dpd = new DatePickerDialog(NearExpiryDateInputActivity.this, R.style.DatePickerDialogStyle,
                                new DatePickerDialog.OnDateSetListener() {

                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {

                                        String datef = mNearExpiryHelper
                                                .dateformat(year, monthOfYear,
                                                        dayOfMonth);
                                        datec1 = mNearExpiryHelper
                                                .changeDate(datef);

                                        if (checkToDate("date1", datec1)) {
                                            date1.setText(mNearExpiryHelper
                                                    .changeMonthNoToName(datef));
                                            date = date1.getText().toString();

                                            mSKUBO.getLocations()
                                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                                    .getNearexpiryDate().get(0)
                                                    .setDate(date);
                                        } else {
                                            Toast.makeText(
                                                    NearExpiryDateInputActivity.this,
                                                    getResources().getString(
                                                            R.string.invaliddate),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }, mYear, mMonth, mDay);
                        dpd.show();

                    }
                });

                CA1.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = CA1;
                        int inType = CA1.getInputType(); // backup the
                        CA1.setInputType(InputType.TYPE_NULL); // disable
                        CA1.onTouchEvent(event); // call native
                        CA1.setInputType(inType); // restore input
                        CA1.requestFocus();
                        if (CA1.getText().length() > 0)
                            CA1.setSelection(CA1.getText().length());
                        return true;
                    }
                });

                CA1.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                CA1.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(0)
                                    .setNearexpCA(s.toString());

                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                PC1.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = PC1;
                        int inType = PC1.getInputType(); // backup
                        // the
                        PC1.setInputType(InputType.TYPE_NULL); // disable
                        PC1.onTouchEvent(event); // call native
                        PC1.setInputType(inType); // restore input
                        PC1.selectAll();
                        PC1.requestFocus();
                        if (PC1.getText().length() > 0)
                            PC1.setSelection(PC1.getText().length());
                        return true;
                    }
                });

                PC1.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                PC1.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(0)
                                    .setNearexpPC(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                OU1.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = OU1;
                        int inType = OU1.getInputType(); // backup
                        // the
                        OU1.setInputType(InputType.TYPE_NULL); // disable
                        OU1.onTouchEvent(event); // call native
                        OU1.setInputType(inType); // restore input
                        OU1.requestFocus();
                        if (OU1.getText().length() > 0)
                            OU1.setSelection(OU1.getText().length());
                        return true;
                    }
                });

                OU1.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                OU1.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(0)
                                    .setNearexpOU(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                BACTHNO1.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                BACTHNO1.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(0)
                                    .setBatchNo(s.toString());

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                date2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                                Locale.ENGLISH);
                        String boDate;
                        if (date2.getText().toString().equals(""))
                            boDate = df.format(c.getTime());
                        else
                            boDate = date2.getText().toString();


                        String formatDate = mNearExpiryHelper
                                .changeMonthNameToNoyyyymmdd(boDate);

                        int day = SDUtil.convertToInt(formatDate.substring(8, 10));
                        int month = SDUtil.convertToInt(formatDate.substring(5, 7));
                        int year = SDUtil.convertToInt(formatDate.substring(0, 4));

                        mDay = day;
                        mMonth = month - 1;
                        mYear = year;
                        DatePickerDialog dpd = new DatePickerDialog(NearExpiryDateInputActivity.this, R.style.DatePickerDialogStyle,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {

                                        String datef = mNearExpiryHelper
                                                .dateformat(year, monthOfYear,
                                                        dayOfMonth);

                                        datec2 = mNearExpiryHelper
                                                .changeDate(datef);

                                        if (checkToDate("date2", datec2)) {
                                            date2.setText(mNearExpiryHelper
                                                    .changeMonthNoToName(datef));
                                            date = date2.getText().toString();
                                            mSKUBO.getLocations()
                                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                                    .getNearexpiryDate().get(1)
                                                    .setDate(date);
                                        } else {
                                            Toast.makeText(
                                                    NearExpiryDateInputActivity.this,
                                                    getResources().getString(
                                                            R.string.invaliddate),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }, mYear, mMonth, mDay);
                        dpd.show();

                    }
                });

                CA2.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = CA2;
                        int inType = CA2.getInputType(); // backup
                        // the
                        CA2.setInputType(InputType.TYPE_NULL); // disable
                        CA2.onTouchEvent(event); // call native
                        CA2.setInputType(inType); // restore input
                        CA2.requestFocus();
                        if (CA2.getText().length() > 0)
                            CA2.setSelection(CA2.getText().length());
                        return true;
                    }
                });

                CA2.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                CA2.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(1)
                                    .setNearexpCA(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                PC2.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = PC2;
                        int inType = PC2.getInputType(); // backup
                        // the
                        PC2.setInputType(InputType.TYPE_NULL); // disable
                        PC2.onTouchEvent(event); // call native
                        PC2.setInputType(inType); // restore input
                        PC2.requestFocus();
                        if (PC2.getText().length() > 0)
                            PC2.setSelection(PC2.getText().length());
                        return true;
                    }
                });

                PC2.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                PC2.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(1)
                                    .setNearexpPC(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                OU2.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = OU2;
                        int inType = OU2.getInputType(); // backup
                        // the
                        OU2.setInputType(InputType.TYPE_NULL); // disable
                        OU2.onTouchEvent(event); // call native
                        OU2.setInputType(inType); // restore input
                        OU2.requestFocus();
                        if (OU2.getText().length() > 0)
                            OU2.setSelection(OU2.getText().length());
                        return true;
                    }
                });

                OU2.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                OU2.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(1)
                                    .setNearexpOU(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                BACTHNO2.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                BACTHNO2.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(1)
                                    .setBatchNo(s.toString());

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                date3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                                Locale.ENGLISH);
                        String boDate;
                        if (date3.getText().toString().equals(""))
                            boDate = df.format(c.getTime());
                        else
                            boDate = date3.getText().toString();

                        String formatDate = mNearExpiryHelper
                                .changeMonthNameToNoyyyymmdd(boDate);

                        int day = SDUtil.convertToInt(formatDate.substring(8, 10));
                        int month = SDUtil.convertToInt(formatDate.substring(5, 7));
                        int year = SDUtil.convertToInt(formatDate.substring(0, 4));

                        mDay = day;
                        mMonth = month - 1;
                        mYear = year;

                        DatePickerDialog dpd = new DatePickerDialog(NearExpiryDateInputActivity.this, R.style.DatePickerDialogStyle,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {

                                        String datef = mNearExpiryHelper
                                                .dateformat(year, monthOfYear,
                                                        dayOfMonth);

                                        datec3 = mNearExpiryHelper
                                                .changeDate(datef);

                                        if (checkToDate("date3", datec3)) {
                                            date3.setText(mNearExpiryHelper
                                                    .changeMonthNoToName(datef));
                                            date = date3.getText().toString();
                                            mSKUBO.getLocations()
                                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                                    .getNearexpiryDate().get(2)
                                                    .setDate(date);
                                        } else {
                                            Toast.makeText(
                                                    NearExpiryDateInputActivity.this,
                                                    getResources().getString(
                                                            R.string.invaliddate),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }, mYear, mMonth, mDay);
                        dpd.show();

                    }
                });

                CA3.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = CA3;
                        int inType = CA3.getInputType(); // backup
                        // the
                        CA3.setInputType(InputType.TYPE_NULL); // disable
                        CA3.onTouchEvent(event); // call native
                        CA3.setInputType(inType); // restore input
                        CA3.requestFocus();
                        if (CA3.getText().length() > 0)
                            CA3.setSelection(CA3.getText().length());
                        return true;
                    }
                });

                CA3.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                CA3.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(2)
                                    .setNearexpCA(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                PC3.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = PC3;
                        int inType = PC3.getInputType(); // backup
                        // the
                        PC3.setInputType(InputType.TYPE_NULL); // disable
                        PC3.onTouchEvent(event); // call native
                        PC3.setInputType(inType); // restore input
                        PC3.requestFocus();
                        if (PC3.getText().length() > 0)
                            PC3.setSelection(PC3.getText().length());
                        return true;
                    }
                });

                PC3.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                PC3.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(2)
                                    .setNearexpPC(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                OU3.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = OU3;
                        int inType = OU3.getInputType(); // backup
                        // the
                        OU3.setInputType(InputType.TYPE_NULL); // disable
                        OU3.onTouchEvent(event); // call native
                        OU3.setInputType(inType); // restore input
                        OU3.requestFocus();
                        if (OU3.getText().length() > 0)
                            OU3.setSelection(OU3.getText().length());
                        return true;
                    }
                });

                OU3.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                OU3.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(2)
                                    .setNearexpOU(s.toString());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                BACTHNO3.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                BACTHNO3.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(2)
                                    .setBatchNo(s.toString());

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                date4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                                Locale.ENGLISH);
                        String boDate;
                        if (date4.getText().toString().equals(""))
                            boDate = df.format(c.getTime());
                        else
                            boDate = date4.getText().toString();
                        String formatDate = mNearExpiryHelper
                                .changeMonthNameToNoyyyymmdd(boDate);

                        int day = SDUtil.convertToInt(formatDate.substring(8, 10));
                        int month = SDUtil.convertToInt(formatDate.substring(5, 7));
                        int year = SDUtil.convertToInt(formatDate.substring(0, 4));

                        mDay = day;
                        mMonth = month - 1;
                        mYear = year;

                        DatePickerDialog dpd = new DatePickerDialog(NearExpiryDateInputActivity.this, R.style.DatePickerDialogStyle,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        String datef = mNearExpiryHelper
                                                .dateformat(year, monthOfYear,
                                                        dayOfMonth);

                                        datec4 = mNearExpiryHelper
                                                .changeDate(datef);

                                        if (checkToDate("date4", datec4)) {
                                            date4.setText(mNearExpiryHelper
                                                    .changeMonthNoToName(datef));
                                            date = date4.getText().toString();
                                            mSKUBO.getLocations()
                                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                                    .getNearexpiryDate().get(3)
                                                    .setDate(date);
                                        } else {
                                            Toast.makeText(
                                                    NearExpiryDateInputActivity.this,
                                                    getResources().getString(
                                                            R.string.invaliddate),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }, mYear, mMonth, mDay);
                        dpd.show();

                    }
                });

                CA4.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = CA4;
                        int inType = CA4.getInputType(); // backup
                        // the
                        CA4.setInputType(InputType.TYPE_NULL); // disable
                        CA4.onTouchEvent(event); // call native
                        CA4.setInputType(inType); // restore input
                        CA4.requestFocus();
                        if (CA4.getText().length() > 0)
                            CA4.setSelection(CA4.getText().length());
                        return true;
                    }
                });

                CA4.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                CA4.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(3)
                                    .setNearexpCA(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                PC4.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = PC4;
                        int inType = PC4.getInputType(); // backup
                        // the
                        PC4.setInputType(InputType.TYPE_NULL); // disable
                        PC4.onTouchEvent(event); // call native
                        PC4.setInputType(inType); // restore input
                        PC4.requestFocus();
                        if (PC4.getText().length() > 0)
                            PC4.setSelection(PC4.getText().length());
                        return true;
                    }
                });

                PC4.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                PC4.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(3)
                                    .setNearexpPC(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                OU4.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = OU4;
                        int inType = OU4.getInputType(); // backup
                        // the
                        OU4.setInputType(InputType.TYPE_NULL); // disable
                        OU4.onTouchEvent(event); // call native
                        OU4.setInputType(inType); // restore input
                        OU4.requestFocus();
                        if (OU4.getText().length() > 0)
                            OU4.setSelection(OU4.getText().length());
                        return true;
                    }
                });

                OU4.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                OU4.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(3)
                                    .setNearexpOU(s.toString());

                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                BACTHNO4.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                BACTHNO4.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(3)
                                    .setBatchNo(s.toString());

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                date5.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                                Locale.ENGLISH);
                        String boDate;
                        if (date5.getText().toString().equals(""))
                            boDate = df.format(c.getTime());
                        else
                            boDate = date5.getText().toString();

                        String formatDate = mNearExpiryHelper
                                .changeMonthNameToNoyyyymmdd(boDate);

                        int day = SDUtil.convertToInt(formatDate.substring(8, 10));
                        int month = SDUtil.convertToInt(formatDate.substring(5, 7));
                        int year = SDUtil.convertToInt(formatDate.substring(0, 4));

                        mDay = day;
                        mMonth = month - 1;
                        mYear = year;

                        DatePickerDialog dpd = new DatePickerDialog(NearExpiryDateInputActivity.this, R.style.DatePickerDialogStyle,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        String datef = mNearExpiryHelper
                                                .dateformat(year, monthOfYear,
                                                        dayOfMonth);

                                        datec5 = mNearExpiryHelper
                                                .changeDate(datef);

                                        if (checkToDate("date5", datec5)) {

                                            date5.setText(mNearExpiryHelper
                                                    .changeMonthNoToName(datef));

                                            date = date5.getText().toString();
                                            mSKUBO.getLocations()
                                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                                    .getNearexpiryDate().get(4)
                                                    .setDate(date);
                                        } else {
                                            Toast.makeText(
                                                    NearExpiryDateInputActivity.this,
                                                    getResources().getString(
                                                            R.string.invaliddate),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }, mYear, mMonth, mDay);
                        dpd.show();

                    }
                });

                CA5.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = CA5;
                        int inType = CA5.getInputType(); // backup
                        // the
                        CA5.setInputType(InputType.TYPE_NULL); // disable
                        CA5.onTouchEvent(event); // call native
                        CA5.setInputType(inType); // restore input
                        CA5.requestFocus();
                        if (CA5.getText().length() > 0)
                            CA5.setSelection(CA5.getText().length());
                        return true;
                    }
                });

                CA5.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                CA5.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(4)
                                    .setNearexpCA(s.toString());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                PC5.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = PC5;
                        int inType = PC5.getInputType(); // backup
                        // the
                        PC5.setInputType(InputType.TYPE_NULL); // disable
                        PC5.onTouchEvent(event); // call native
                        PC5.setInputType(inType); // restore input
                        PC5.requestFocus();
                        if (PC5.getText().length() > 0)
                            PC5.setSelection(PC5.getText().length());
                        return true;
                    }
                });

                PC5.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                PC5.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(4)
                                    .setNearexpPC(s.toString());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                OU5.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = OU5;
                        int inType = OU5.getInputType(); // backup
                        // the
                        OU5.setInputType(InputType.TYPE_NULL); // disable
                        OU5.onTouchEvent(event); // call native
                        OU5.setInputType(inType); // restore input
                        OU5.requestFocus();
                        if (OU5.getText().length() > 0)
                            OU5.setSelection(OU5.getText().length());
                        return true;
                    }
                });
                OU5.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                OU5.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(4)
                                    .setNearexpOU(s.toString());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                BACTHNO5.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                BACTHNO5.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(4)
                                    .setBatchNo(s.toString());

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

                date6.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Calendar c = Calendar.getInstance();
                        SimpleDateFormat df = new SimpleDateFormat("dd/MMM/yyyy",
                                Locale.ENGLISH);
                        String boDate;
                        if (date6.getText().toString().equals(""))
                            boDate = df.format(c.getTime());
                        else
                            boDate = date6.getText().toString();

                        String formatDate = mNearExpiryHelper
                                .changeMonthNameToNoyyyymmdd(boDate);

                        int day = SDUtil.convertToInt(formatDate.substring(8, 10));
                        int month = SDUtil.convertToInt(formatDate.substring(5, 7));
                        int year = SDUtil.convertToInt(formatDate.substring(0, 4));

                        mDay = day;
                        mMonth = month - 1;
                        mYear = year;

                        DatePickerDialog dpd = new DatePickerDialog(NearExpiryDateInputActivity.this, R.style.DatePickerDialogStyle,
                                new DatePickerDialog.OnDateSetListener() {

                                    @Override
                                    public void onDateSet(DatePicker view, int year,
                                                          int monthOfYear, int dayOfMonth) {
                                        String datef = mNearExpiryHelper
                                                .dateformat(year, monthOfYear,
                                                        dayOfMonth);

                                        datec6 = mNearExpiryHelper
                                                .changeDate(datef);

                                        if (checkToDate("date6", datec6)) {
                                            date6.setText(mNearExpiryHelper
                                                    .changeMonthNoToName(datef));
                                            date = date6.getText().toString();
                                            mSKUBO.getLocations()
                                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                                    .getNearexpiryDate().get(5)
                                                    .setDate(date);
                                        } else {
                                            Toast.makeText(
                                                    NearExpiryDateInputActivity.this,
                                                    getResources().getString(
                                                            R.string.invaliddate),
                                                    Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                }, mYear, mMonth, mDay);

                        dpd.show();

                    }
                });

                CA6.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = CA6;
                        int inType = CA6.getInputType(); // backup
                        // the
                        CA6.setInputType(InputType.TYPE_NULL); // disable
                        CA6.onTouchEvent(event); // call native
                        CA6.setInputType(inType); // restore input
                        CA6.requestFocus();
                        if (CA6.getText().length() > 0)
                            CA6.setSelection(CA6.getText().length());
                        return true;
                    }
                });

                CA6.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                CA6.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(5)
                                    .setNearexpCA(s.toString());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                PC6.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = PC6;
                        int inType = PC6.getInputType(); // backup
                        // the
                        PC6.setInputType(InputType.TYPE_NULL); // disable
                        PC6.onTouchEvent(event); // call native
                        PC6.setInputType(inType); // restore input
                        PC6.requestFocus();
                        if (PC6.getText().length() > 0)
                            PC6.setSelection(PC6.getText().length());
                        return true;
                    }
                });

                PC6.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {
                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                PC6.setSelection(s.toString().length());

                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(5)
                                    .setNearexpPC(s.toString());
                        }
                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });

                OU6.setOnTouchListener(new View.OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = OU6;
                        int inType = OU6.getInputType(); // backup
                        // the
                        OU6.setInputType(InputType.TYPE_NULL); // disable
                        OU6.onTouchEvent(event); // call native
                        OU6.setInputType(inType); // restore input
                        OU6.requestFocus();
                        if (OU6.getText().length() > 0)
                            OU6.setSelection(OU6.getText().length());
                        return true;
                    }
                });

                OU6.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before,
                                              int count) {

                        if (!"0".equals(s)) {
                            if (s.toString().length() > 0)
                                OU6.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(5)
                                    .setNearexpOU(s.toString());
                        }

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count,
                                                  int after) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                    }
                });
                BACTHNO6.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        if (!"".equals(s)) {
                            if (s.toString().length() > 0)
                                BACTHNO6.setSelection(s.toString().length());
                            mSKUBO.getLocations()
                                    .get(mNearExpiryHelper.mSelectedLocationIndex)
                                    .getNearexpiryDate().get(5)
                                    .setBatchNo(s.toString());

                        }
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });

            }
            return row;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
