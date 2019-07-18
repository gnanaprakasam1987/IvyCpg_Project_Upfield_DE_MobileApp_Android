package com.ivy.cpg.view.expense;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;


public class ExpenseFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private TextView tvImgCount;
    private EditText et_exp_date, et_amount;
    private Spinner sp_expenses;
    private String Tid = "";
    private boolean isHeaderExists;
    private Context mContext;
    private TabLayout tabLayout;

    private int exp_type = 0;
    private static final String TAG = "ExpenseFragment";
    private String imageFileName = "", amountValue, dateValue, photoNamePath;
    private ArrayList<String> imagesList;


    private static final int CAMERA_REQUEST_CODE = 1;
    private ExpenseSheetHelper expenseSheetHelper;
    private AppSchedulerProvider appSchedulerProvider;
    private ProgressDialog progressDialogue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_expense, container, false);
        mContext = getActivity();

        initializeItem(view);

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        expenseSheetHelper = ExpenseSheetHelper.getInstance(getActivity());
    }

    private void initializeItem(View view) {


        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = getActivity().getIntent().getExtras();

        if (bundle != null) {
            setScreenTitle(bundle.getString("screentitle"));
        }

        et_exp_date = view.findViewById(R.id.et_exp_date);
        et_amount = view.findViewById(R.id.et_amount);
        sp_expenses = view.findViewById(R.id.sp_expenses);

        TextView tvcamera = view.findViewById(R.id.tv_camera);

        tvImgCount = view.findViewById(R.id.tv_img_count);
        TextView tvDone = view.findViewById(R.id.tv_done);
        TextView tvClear = view.findViewById(R.id.tv_clear);

        imagesList = new ArrayList<>();
        photoNamePath = FileUtils.photoFolderPath + "/";
        Commons.print("Photo Path, " + "" + photoNamePath);

        appSchedulerProvider = new AppSchedulerProvider();

        expenseSheetHelper.loadExpenseData();
        loadExpenses();


        tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_current_month)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_mtd)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_p3m)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_exp_type, new DailyExpenseFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                        .beginTransaction();
                if (tab.getPosition() == 0) {
                    transaction.replace(R.id.fragment_exp_type, new DailyExpenseFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else if (tab.getPosition() == 1) {
                    transaction.replace(R.id.fragment_exp_type, new CurrentMonthExpenseFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    transaction.replace(R.id.fragment_exp_type, new PastMonthExpenseFragment());
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

        et_exp_date.setText(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
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
                    progressDialogue = ProgressDialog.show(mContext,
                            DataMembers.SD, getResources().getString(R.string.saving),
                            true, false);
                    amountValue = et_amount.getText().toString();
                    dateValue = et_exp_date.getText().toString();
                    if (isHeaderExists) {
                        new CompositeDisposable().add(expenseSheetHelper.updateHeaderInsert(Tid, dateValue, amountValue, exp_type, imagesList, exp_type + "" + DateTimeUtils
                                .now(DateTimeUtils.DATE_TIME_ID))
                                .subscribeOn(appSchedulerProvider.io())
                                .observeOn(appSchedulerProvider.ui())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) {
                                        updateUiAfterSave();
                                    }
                                }));
                    } else {
                        ExpensesBO expensesBO = new ExpensesBO();
                        expensesBO.setTid(bmodel.userMasterHelper.getUserMasterBO().getUserid() + DateTimeUtils
                                .now(DateTimeUtils.DATE_TIME_ID));
                        expensesBO.setTypeId(exp_type);
                        expensesBO.setRefId(exp_type + "" + DateTimeUtils
                                .now(DateTimeUtils.DATE_TIME_ID));
                        expensesBO.setAmount(amountValue);
                        expensesBO.setImageList(imagesList);
                        new CompositeDisposable().add(expenseSheetHelper.saveAllData(expensesBO, dateValue)
                                .subscribeOn(appSchedulerProvider.io())
                                .observeOn(appSchedulerProvider.ui())
                                .subscribe(new Consumer<Boolean>() {
                                    @Override
                                    public void accept(Boolean aBoolean) {
                                        updateUiAfterSave();
                                    }
                                }));
                    }
                }
            }
        });


    }

    private void updateUiAfterSave() {
        progressDialogue.dismiss();

        Toast.makeText(mContext,
                getResources().getString(R.string.saved_successfully),
                Toast.LENGTH_SHORT).show();
        imagesList.clear();
        sp_expenses.setSelection(0);
        et_amount.setText("");
        tvImgCount.setText("");

        if (tabLayout.getSelectedTabPosition() == 0) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager()
                    .beginTransaction();
            transaction.replace(R.id.fragment_exp_type, new DailyExpenseFragment());
            transaction.addToBackStack(null);
            transaction.commit();
        }
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

        dpd1.getDatePicker().setMinDate(getFirstDateMills());
        dpd1.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());

        dpd1.show();
    }

    private long getFirstDateMills() {

        Calendar mCalendarTo = GregorianCalendar.getInstance();
        mCalendarTo.add(Calendar.DAY_OF_YEAR, -bmodel.configurationMasterHelper.expenseDays);

        mCalendarTo.set(Calendar.HOUR_OF_DAY, mCalendarTo.getMinimum(Calendar.HOUR_OF_DAY));
        mCalendarTo.set(Calendar.MINUTE, mCalendarTo.getMinimum(Calendar.MINUTE));
        mCalendarTo.set(Calendar.SECOND, mCalendarTo.getMinimum(Calendar.SECOND));
        mCalendarTo.set(Calendar.MILLISECOND, mCalendarTo.getMinimum(Calendar.MILLISECOND));
        return mCalendarTo.getTimeInMillis();
    }

    private void loadExpenses() {
        ArrayAdapter<SpinnerBO> spinnerAdapter = new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerAdapter.add(new SpinnerBO(0, "---Select---"));

        for (SpinnerBO temp : expenseSheetHelper.getExpnenses()) {
            spinnerAdapter.add(temp);
        }

        sp_expenses.setAdapter(spinnerAdapter);
    }


    private void takePhoto() {
        if (bmodel.isExternalStorageAvailable()) {

            imageFileName = "EXP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";

            String path = photoNamePath + "/" + imageFileName;
            try {
                Intent intent = new Intent(mContext, CameraActivity.class);
                intent.putExtra(CameraActivity.QUALITY, 40);
                intent.putExtra(CameraActivity.PATH, path);
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
        Tid = expenseSheetHelper.checkExpenseHeader(et_exp_date.getText().toString());
        isHeaderExists = Tid.length() != 0;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        expenseSheetHelper.clearInstance();
    }
}
