package com.ivy.sd.png.view;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ExpensesBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.reports.DynamicReportFragment;

import java.util.ArrayList;
import java.util.Calendar;

public class ExpenseActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private TextView tvcamera, tvImgCount;
    private EditText et_exp_date, et_amount;
    private Spinner sp_expenses;
    private TextView tvDone, tvClear;
    private String Tid = "";
    private boolean isHeaderExists;
    private Context mContext;
    TabLayout tabLayout;
    private DynamicReportFragment dynamicReportFragment;

/*    // for displaying month name in header
    private String[] monthName = {"January", "February", "March", "April", "May", "June", "July",
            "August", "September", "October", "November", "December"};*/

    private int exp_type = 0;
    private static final String TAG = "ExpenseActivity";
    private String imageFileName = "", amountValue, dateValue, photoNamePath;
    private ArrayList<String> imagesList;

    // Disable Motorola ET1 Scanner Plugin
    final String ACTION_SCANNERINPUTPLUGIN = "com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
    final String EXTRA_PARAMETER = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
    final String DISABLE_PLUGIN = "DISABLE_PLUGIN";
    private static final int CAMERA_REQUEST_CODE = 1;

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        mContext = ExpenseActivity.this;
        initializeItem();
    }

    private void initializeItem() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            // Set title to actionbar
            getSupportActionBar().setTitle(getResources().getString(R.string.expense_title));
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        et_exp_date = (EditText) findViewById(R.id.et_exp_date);
        et_amount = (EditText) findViewById(R.id.et_amount);
        sp_expenses = (Spinner) findViewById(R.id.sp_expenses);
        tvcamera = (TextView) findViewById(R.id.tv_camera);
        tvImgCount = (TextView) findViewById(R.id.tv_img_count);
        tvDone = (TextView) findViewById(R.id.tv_done);
        tvClear = (TextView) findViewById(R.id.tv_clear);

        imagesList = new ArrayList<String>();
        photoNamePath = HomeScreenFragment.photoPath + "/";
        Commons.print("Photo Path, " + "" + photoNamePath);

        bmodel.expenseSheetHelper.loadExpenseData();
        loadExpenses();


        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_current_month)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_mtd)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_p3m)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_content, new DailyExpenseFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                if (tab.getPosition() == 0) {
                    transaction.replace(R.id.fragment_content, new DailyExpenseFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else if (tab.getPosition() == 1) {
                    transaction.replace(R.id.fragment_content, new CurrentMonthExpenseFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    transaction.replace(R.id.fragment_content, new PastMonthExpenseFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        et_exp_date.setText(SDUtil.now(SDUtil.DATE_GLOBAL));
        et_exp_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showCalendar();
            }
        });

        checkHeader();

        sp_expenses.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                SpinnerBO reString = (SpinnerBO) adapterView.getSelectedItem();
                exp_type = reString.getId();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        tvcamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exp_type == 0)
                    Toast.makeText(mContext, getString(R.string.alert_exp_type), Toast.LENGTH_SHORT).show();
                else if (et_amount.getText().toString().length() == 0)
                    Toast.makeText(mContext, getString(R.string.alert_amount), Toast.LENGTH_SHORT).show();
                else
                    takePhoto();
            }
        });

        tvClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sp_expenses.setSelection(0);
                et_amount.setText("");
                imagesList.clear();
                tvImgCount.setText("");

            }
        });


        tvDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (exp_type == 0)
                    Toast.makeText(mContext, getString(R.string.alert_exp_type), Toast.LENGTH_SHORT).show();
                else if (et_amount.getText().toString().length() == 0)
                    Toast.makeText(mContext, getString(R.string.alert_amount), Toast.LENGTH_SHORT).show();
                else {
                    amountValue = et_amount.getText().toString();
                    dateValue = et_exp_date.getText().toString();
                    new SaveAsyncTask().execute();
                }
            }
        });


    }

    private void showCalendar() {
        final Calendar c1 = Calendar.getInstance();
        int mToYear = c1.get(Calendar.YEAR);
        int mToMonth = c1.get(Calendar.MONTH);
        int mToDay = c1.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog dpd1 = new DatePickerDialog(mContext, R.style.DatePickerDialogStyle,
                new DatePickerDialog.OnDateSetListener() {

                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        et_exp_date
                                .setText(year
                                        + "/"
                                        + ((monthOfYear + 1) < 10 ? "0"
                                        + (monthOfYear + 1)
                                        : (monthOfYear + 1))
                                        + "/"
                                        + ((dayOfMonth) < 10 ? "0"
                                        + (dayOfMonth)
                                        : (dayOfMonth)));

                        checkHeader();

                        imagesList.clear();
                        sp_expenses.setSelection(0);
                        et_amount.setText("");
                        tvImgCount.setText("");


                    }
                }, mToYear, mToMonth, mToDay);

        dpd1.getDatePicker().setCalendarViewShown(false);

        dpd1.getDatePicker().setMinDate(getMonthFirstDateMills());
        dpd1.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        dpd1.show();
    }

    private long getMonthFirstDateMills() {

        Calendar mCalendarTo = Calendar.getInstance();
        mCalendarTo.set(Calendar.DAY_OF_MONTH, 1);

        mCalendarTo.set(Calendar.HOUR_OF_DAY, mCalendarTo.getMinimum(Calendar.HOUR_OF_DAY));
        mCalendarTo.set(Calendar.MINUTE, mCalendarTo.getMinimum(Calendar.MINUTE));
        mCalendarTo.set(Calendar.SECOND, mCalendarTo.getMinimum(Calendar.SECOND));
        mCalendarTo.set(Calendar.MILLISECOND, mCalendarTo.getMinimum(Calendar.MILLISECOND));

        return mCalendarTo.getTimeInMillis();
    }

    private void loadExpenses() {
        ArrayAdapter<SpinnerBO> spinnerAdapter = new ArrayAdapter<SpinnerBO>(mContext,
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAdapter.add(new SpinnerBO(0, "---Select---"));

        for (SpinnerBO temp : bmodel.expenseSheetHelper.getExpnenses()) {
            spinnerAdapter.add(temp);
        }

        sp_expenses.setAdapter(spinnerAdapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_history).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
        return true;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_leave_approval, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                startActivity(new Intent(mContext, HomeScreenActivity.class));
                finish();
                break;
        }
        return true;
    }


   /* public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        public PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    tab1 = new DailyExpenseFragment();
                    return tab1;
                case 1:
                    CurrentMonthExpenseFragment tab2 = new CurrentMonthExpenseFragment();
                    return tab2;
                case 2:
                    DynamicReportFragment tab3 = new DynamicReportFragment();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }*/

    private void takePhoto() {
        if (bmodel.isExternalStorageAvailable()) {

            imageFileName = "EXP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";

            String path = photoNamePath + "/" + imageFileName;
            try {
                Intent i = new Intent();
                i.setAction(ACTION_SCANNERINPUTPLUGIN);
                i.putExtra(EXTRA_PARAMETER, DISABLE_PLUGIN);
                mContext.sendBroadcast(i);

//                Thread.sleep(100);

                Intent intent = new Intent(mContext, CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), path);
//                intent.putExtra(
//                        getResources().getString(R.string.is_save_required), false);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);

            } catch (Exception e) {
                Commons.print("error opening camera");
                Commons.printException(e);
                // TODO: handle exception
            }
        } else {
            Toast.makeText(
                    mContext,
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                Commons.print(TAG + ",Camers Activity : Sucessfully Captured.");

                //For adding server ref path to image name
                String path = "Expense/"
                        + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                        .replace("/", "") + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/";
                imagesList.add(path + imageFileName);
                tvImgCount.setText("" + imagesList.size());

            } else {
                Commons.print(TAG + ",Camers Activity : Canceled");
            }
        }
    }

    private void checkHeader() {
        Tid = bmodel.expenseSheetHelper.checkExpenseHeader(et_exp_date.getText().toString());
        if (Tid.length() == 0)
            isHeaderExists = false;
        else
            isHeaderExists = true;
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if (isHeaderExists) {
                    double totalAmount = 0;
                    totalAmount = bmodel.expenseSheetHelper.getExpenseTotal(Tid, dateValue) + Double.parseDouble(amountValue);
                    bmodel.expenseSheetHelper.updateHeaderInsert(Tid, totalAmount, amountValue, exp_type, imagesList, exp_type + "" + SDUtil
                            .now(SDUtil.DATE_TIME_ID));
                } else {
                    ExpensesBO expensesBO = new ExpensesBO();
                    expensesBO.setTid(bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                            .now(SDUtil.DATE_TIME_ID));
                    expensesBO.setTypeId(exp_type);
                    expensesBO.setRefId(exp_type + "" + SDUtil
                            .now(SDUtil.DATE_TIME_ID));
                    expensesBO.setAmount(amountValue);
                    expensesBO.setImageList(imagesList);
                    bmodel.expenseSheetHelper.saveAllData(expensesBO, dateValue);
                }

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(mContext,
                    DataMembers.SD, getResources().getString(R.string.saving),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();

            Toast.makeText(mContext,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            imagesList.clear();
            sp_expenses.setSelection(0);
            et_amount.setText("");
            tvImgCount.setText("");

            if (tabLayout.getSelectedTabPosition() == 0) {
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.replace(R.id.fragment_content, new DailyExpenseFragment());
                transaction.addToBackStack(null);
                transaction.commit();
            }


        }

    }
}

