package com.ivy.countersales;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.util.Commons;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class CustomerVisitFragment extends IvyBaseFragment implements View.OnClickListener, CShistoryDialog.CShistoryInerface {

    Vector<ConfigureBO> mCustomerVisitModules;
    BusinessModel bmodel;
    Button btnConsulting, btnFreeSample, btnApply, btnSales, btn_feedback;
    ImageView btn_history;
    EditText edt_name, edt_address, edt_contact, edt_freqVisit;
    private AlertDialog alertDialog;
    MyReceiver receiver;
    String refid = "";
    private View view;
    private String uid = "";

    CheckBox chkMale, chkFemale;
    private static final String MENU_CONSULT_CS = "MENU_CONSULT_CS";
    private static final String MENU_SAMPLE_CS = "MENU_SAMPLE_CS";
    private static final String MENU_TEST_CS = "MENU_TEST_CS";
    private static final String MENU_SALE_CS = "MENU_SALE_CS";
    private static final String MENU_SURVEY_CS = "MENU_SURVEY_CS";

    ArrayAdapter<StandardListBO> ageAdapter;
    private RadioGroup ageRadioGroup;

    EditText edt_email;
    ArrayList<StandardListBO> ageGroupList;
    private SurveyHelperNew surveyHelperNew;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        surveyHelperNew = SurveyHelperNew.getInstance(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        bmodel.mSelectedActivityName = "Customer visit";
        setScreenTitle("" + bmodel.mSelectedActivityName);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.customer_visit_fragment, container, false);
        btnConsulting = (Button) view.findViewById(R.id.btn_consulting);
        btnConsulting.setOnClickListener(this);
        btnFreeSample = (Button) view.findViewById(R.id.btn_freeSample);
        btnFreeSample.setOnClickListener(this);
        btnApply = (Button) view.findViewById(R.id.btn_apply);
        btnApply.setOnClickListener(this);
        btnSales = (Button) view.findViewById(R.id.btn_sale);
        btnSales.setOnClickListener(this);
        btn_history = (ImageView) view.findViewById(R.id.btn_history);
        btn_history.setOnClickListener(this);

        btn_feedback = (Button) view.findViewById(R.id.btn_feedback);
        btn_feedback.setOnClickListener(this);
        edt_name = (EditText) view.findViewById(R.id.edt_name);
        edt_address = (EditText) view.findViewById(R.id.edt_addr);
        edt_contact = (EditText) view.findViewById(R.id.edt_contactnum);
        edt_freqVisit = (EditText) view.findViewById(R.id.edt_freq);
        edt_email = (EditText) view.findViewById(R.id.edt_email);

        ageRadioGroup = (RadioGroup) view.findViewById(R.id.radiogroup);

        edt_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edt_address.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edt_contact.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        edt_freqVisit.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        edt_address.setHorizontallyScrolling(false);
        edt_address.setMaxLines(Integer.MAX_VALUE);

        chkMale = (CheckBox) view.findViewById(R.id.chk_male);
        chkFemale = (CheckBox) view.findViewById(R.id.chk_female);
        chkMale.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        chkFemale.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        chkMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chkFemale.setChecked(false);
                chkMale.setChecked(true);
            }
        });
        chkFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                chkFemale.setChecked(true);
                chkMale.setChecked(false);
            }
        });
       /* chkMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {

                if(isChecked){

                }
            }
        });
        chkFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    chkMale.setChecked(false);
                }
            }
        });*/

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Customer Visit Details");
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mCustomerVisitModules = bmodel.mCounterSalesHelper.downloadCustomerVisitModules();
        for (ConfigureBO bo : mCustomerVisitModules) {
            if (bo.getConfigCode().equals(MENU_CONSULT_CS))
                btnConsulting.setVisibility(View.VISIBLE);
            else if (bo.getConfigCode().equals(MENU_SAMPLE_CS))
                btnFreeSample.setVisibility(View.VISIBLE);
            else if (bo.getConfigCode().equals(MENU_TEST_CS))
                btnApply.setVisibility(View.VISIBLE);
            else if (bo.getConfigCode().equals(MENU_SURVEY_CS))
                btn_feedback.setVisibility(View.VISIBLE);
            else if (bo.getConfigCode().equals(MENU_SALE_CS))
                btnSales.setVisibility(View.VISIBLE);

        }



        /* Register reciver to receive downlaod status. */
        IntentFilter filter = new IntentFilter(MyReceiver.PROCESS_RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        receiver = new MyReceiver();
        getActivity().registerReceiver(receiver, filter);

        ((TextView) view.findViewById(R.id.txt_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.txt_addr)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.txt_contactnum)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.txt_freq)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.txt_gender)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.txt_email)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        try {
            ageGroupList = bmodel.reasonHelper.downloadCSAgeGroup();

            for (int j = 0; j < ageGroupList.size(); j++) {
                AppCompatRadioButton radioButton = new AppCompatRadioButton(getActivity());
                radioButton.setId(Integer.parseInt(ageGroupList.get(j).getListID()));
                radioButton.setText(ageGroupList.get(j).getListName());
                ageRadioGroup.addView(radioButton);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        ageRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {

            }
        });

        if (bmodel.getCounterSaleBO() != null) {
            edt_name.setText(bmodel.getCounterSaleBO().getCustomerName());
            edt_address.setText(bmodel.getCounterSaleBO().getAddress());
            edt_contact.setText(bmodel.getCounterSaleBO().getContactNumber());
            edt_freqVisit.setText(bmodel.getCounterSaleBO().getFreqVisit());
            edt_email.setText(bmodel.getCounterSaleBO().getEmail());
            if (bmodel.getCounterSaleBO().getGender() != null)
                if (bmodel.getCounterSaleBO().getGender().equals("M")) {
                    chkFemale.setChecked(false);
                    chkMale.setChecked(true);
                } else {
                    chkMale.setChecked(false);
                    chkFemale.setChecked(true);
                }
            if (bmodel.getCounterSaleBO().getAgeGroup() != null)
                if (!bmodel.getCounterSaleBO().getAgeGroup().equals("0"))
                    ageRadioGroup.check(Integer.parseInt(bmodel.getCounterSaleBO().getAgeGroup()));


            uid = bmodel.getCounterSaleBO().getUid();
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        bmodel.setContext(getActivity());


        // update tick mark based on the data
        if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getmSampleProducts() != null && bmodel.getCounterSaleBO().getmSampleProducts().size() > 0)
            btnFreeSample.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok_tick, 0);
        else
            btnFreeSample.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (bmodel.getCounterSaleBO() != null &&
                ((bmodel.getCounterSaleBO().isDraft() && bmodel.getCounterSaleBO().isSaleDrafted()) ||
                        (bmodel.getCounterSaleBO().getmSalesproduct() != null && bmodel.getCounterSaleBO().getmSalesproduct().size() > 0)))
            btnSales.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok_tick, 0);
        else
            btnSales.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getAttributeId() > 0) {
            btnConsulting.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok_tick, 0);
        } else
            btnConsulting.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

        if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getmTestProducts().size() > 0) {
            btnApply.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok_tick, 0);
        } else
            btnApply.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
        if (surveyHelperNew.hasDataToSave()) {
            btn_feedback.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ok_tick, 0);
        } else
            btn_feedback.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);

    }

    public class MyReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.CSSEARCH";

        @Override
        public void onReceive(Context context, Intent intent) {
            updateReceiver(intent);
        }
    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);
        switch (method) {
            case SynchronizationHelper.VOLLEY_CUSTOMER_SEARCH:
                alertDialog.dismiss();
                if (SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE
                        .equals(errorCode)) {

                    try {

                        HashMap<String, JSONObject> response = bmodel.synchronizationHelper.getmJsonObjectResponseByTableName();
                        for (String key : response.keySet()) {
                            response.get(key).getJSONArray(SynchronizationHelper.JSON_FIELD_KEY).put("upload");
                            for (int j = 0; j < response.get(key).getJSONArray(SynchronizationHelper.JSON_DATA_KEY).length(); j++) {
                                JSONArray value = (JSONArray) response.get(key).getJSONArray(SynchronizationHelper.JSON_DATA_KEY).get(j);
                                value.put("S");
                            }
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(response.get(key), false);
                        }
                    } catch (Exception ex) {

                    }

                    HashMap<String, String> mHeaderLst = bmodel.mCounterSalesHelper.downloadCustomerHeaderInformation("", false);
                    if (mHeaderLst != null && !mHeaderLst.isEmpty()) {
                        mHistoryDialog = new CShistoryDialog(getActivity(), mHeaderLst, bmodel,false);
                        mHistoryDialog.show();
                        mHistoryDialog.setCancelable(false);
                        mHistoryDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                            @Override
                            public void onDismiss(DialogInterface dialogInterface) {
                                if (bmodel.mCounterSalesHelper.getmHeaderLst() != null && !bmodel.mCounterSalesHelper.getmHeaderLst().isEmpty()) {
                                    HashMap<String, String> lstHeader = bmodel.mCounterSalesHelper.getmHeaderLst();
                                    edt_name.setText(lstHeader.get("name"));
                                    edt_address.setText(lstHeader.get("address"));
                                    edt_email.setText(lstHeader.get("email"));
                                    String gender = lstHeader.get("gender");
                                    if (gender.equals("M")) {
                                        chkFemale.setChecked(false);
                                        chkMale.setChecked(true);
                                    } else {
                                        chkMale.setChecked(false);
                                        chkFemale.setChecked(true);
                                    }
                                    for (int j = 0; j < ageGroupList.size(); j++) {
                                        if (ageGroupList.get(j).getListName().equalsIgnoreCase(lstHeader.get("age"))) {
                                            ageRadioGroup.check(Integer.parseInt(ageGroupList.get(j).getListID()));
                                            break;
                                        }
                                    }
                                }

                            }
                        });
                    } else {
                        Toast.makeText(getActivity(), "Data not available", Toast.LENGTH_LONG).show();
                    }

                } else {
                    String errorMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorCode);
                    if (errorMessage != null) {
                        Toast.makeText(getActivity(), errorMessage,
                                Toast.LENGTH_SHORT).show();
                    }
                }
                break;
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_counter_sales, menu);
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        if (bmodel.getCounterSaleBO() != null)
            menu.findItem(R.id.menu_delete).setVisible(bmodel.getCounterSaleBO().isSaleDrafted());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            setValues();
            if (bmodel.getCounterSaleBO() != null) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.doyouwantgoback);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
//                        Intent intent = new Intent(getActivity(),
//                                CSHomeScreen.class);
//                        startActivity(intent);
                        getActivity().finish();
                    }
                });
                builder.setNegativeButton(R.string.cancel, null);
                builder.show();
            } else {
//                Intent i = new Intent(getActivity(),
//                        CSHomeScreen.class);
//                startActivity(i);
                getActivity().finish();
            }

            return true;
        } else if (item.getItemId() == R.id.menu_next) {
            setValues();
            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().isDraft()) {

                new loadSummaryScreen().execute();
            } else {

                if (bmodel.getCounterSaleBO() != null &&
                        ((bmodel.getCounterSaleBO().getmTestProducts().size() > 0)
                                || (!bmodel.getCounterSaleBO().getResolution().equals("") || bmodel.getCounterSaleBO().getAttributeId() != 0 || !bmodel.getCounterSaleBO().getConnsultingFeedback().equals(""))
                                || (bmodel.getCounterSaleBO().getmSampleProducts() != null)
                                || (bmodel.getCounterSaleBO().getmSalesproduct() != null))
                        || (surveyHelperNew.hasDataToSave())) {


                    if (edt_email.getText().toString().length() > 0 && !isValidEmail(edt_email.getText().toString())) {

                        Toast.makeText(getActivity(), getResources().getString(R.string.enter_valid_email_id), Toast.LENGTH_LONG).show();
                        return false;
                    }
                    if (edt_name.getText().toString().equals("")) {
                        Toast.makeText(getActivity(), "Customer name is Mandatory to save", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    Intent i = new Intent(getActivity(),
                            CS_sale_summary.class);
                    i.putExtra("refid", refid);
                    i.putExtra("isFromSale", false);
                    startActivity(i);
                    getActivity().finish();


                } else {
                    Toast.makeText(getActivity(), R.string.no_data_tosave, Toast.LENGTH_LONG).show();
                }

            }

            /*//in case, directly moving to summary without visiting sale screen
            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().isDraft()) {
                if (bmodel.getCounterSaleBO().getmSalesproduct() == null
                        || (bmodel.getCounterSaleBO().getmSalesproduct() != null && bmodel.getCounterSaleBO().getmSalesproduct().size() == 0)) {
                    //only if size=0, because if draft is edited then no need to fetch data from Db
                    bmodel.getCounterSaleBO().setmSalesproduct(bmodel.mCounterSalesHelper.getDraftedSaleProducts(uid));
                }
            }*/


            return true;
        } else if (item.getItemId() == R.id.menu_delete) {

            mDialog();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_consulting) {
            bmodel.mSelectedActivityName = "Consulting";
            Intent in = new Intent(getActivity(), CSConsultingActivity.class);
            startActivity(in);

        } else if (view.getId() == R.id.btn_freeSample) {
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_STK_ORD");
                bmodel.productHelper
                        .downloadProductsWithFiveLevelFilter("MENU_STK_ORD");

            } else {
                bmodel.productHelper.downloadProductFilter("MENU_STK_ORD");
                bmodel.productHelper.downloadProducts("MENU_STK_ORD");
            }

            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().isDraft()) {
                bmodel.getCounterSaleBO().setmSampleProducts(bmodel.mCounterSalesHelper.getDraftedSampleProducts(uid));
            }

            bmodel.mSelectedActivityName = "Free Sample";
            Intent in = new Intent(getActivity(), CSFreeSample.class);
            startActivity(in);

        } else if (view.getId() == R.id.btn_apply) {
            new DownloadTestProducts().execute();
        } else if (view.getId() == R.id.btn_sale) {

            new DownloadProducts().execute();

        } else if (R.id.btn_history == view.getId()) {

            if (!edt_contact.getText().toString().equals("")) {
                //if (bmodel.isOnline())
                edt_name.setText("");
                edt_address.setText("");
                edt_email.setText("");
                ageRadioGroup.clearCheck();
                chkMale.setChecked(false);
                chkFemale.setChecked(true);
                new DownloadCustomerHistory().execute();
               /* else
                    Toast.makeText(
                            getActivity(),
                            getActivity().getResources().getString(
                                    R.string.no_network_connection),
                            Toast.LENGTH_SHORT).show();*/
            } else
                Toast.makeText(getActivity(), "Please enter contact number.", Toast.LENGTH_LONG).show();
        } else if (view.getId() == R.id.btn_feedback) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                bmodel.productHelper
                        .downloadFiveLevelFilterNonProducts(MENU_SURVEY_CS);
            else
                bmodel.productHelper.downloadProductFilter(MENU_SURVEY_CS);

            if (surveyHelperNew.getSurvey() != null
                    && surveyHelperNew.getSurvey().size() > 0) {
                surveyHelperNew.setFromCSsurvey(true);

                bmodel.mSelectedActivityName = "Feedback";
                Intent intent = new Intent(getActivity(),
                        SurveyActivityNew.class);
                intent.putExtra("SurveyType", 0);
                intent.putExtra("menucode", MENU_SURVEY_CS);
                startActivity(intent);

            } else {

                bmodel.showAlert(
                        getResources().getString(R.string.data_not_mapped),
                        0);

            }
        }

    }


    class DownloadTestProducts extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_TEST_CS");
                bmodel.productHelper
                        .downloadProductsWithFiveLevelFilter("MENU_TEST_CS");

            } else {
                bmodel.productHelper.downloadProductFilter("MENU_TEST_CS");
                bmodel.productHelper.downloadProducts("MENU_TEST_CS");
            }

            bmodel.mCounterSalesHelper.updatetestStock();
            bmodel.mSelectedActivityName = "Apply/Test";


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (alertDialog != null)
                alertDialog.dismiss();

            Intent in = new Intent(getActivity(), CSapply.class);
            startActivity(in);

        }
    }


    class DownloadProducts extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_SALE_CS");
                bmodel.productHelper
                        .downloadProductsWithFiveLevelFilter("MENU_SALE_CS");

            } else {
                bmodel.productHelper.downloadProductFilter("MENU_SALE_CS");
                bmodel.productHelper.downloadProducts("MENU_SALE_CS");
            }

            if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {
                bmodel.productHelper.downloadChildSKUs();
            }

            bmodel.mCounterSalesHelper.downloadCSStock();

            bmodel.schemeDetailsMasterHelper.downloadSchemeMethods();

            bmodel.productHelper.updateCounterSalesProductColor();

            //setting header detail in object, because user can able to see summary screen from sale screen..
            setValues();

            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().isDraft()) {
                bmodel.getCounterSaleBO().setmSalesproduct(bmodel.mCounterSalesHelper.getDraftedSaleProducts(uid));
            }

            bmodel.mSelectedActivityName = "Sales";


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            alertDialog.dismiss();
            Intent in = new Intent(getActivity(), CSsale.class);
            in.putExtra("refid", refid);
            startActivity(in);
            getActivity().finish();

        }
    }

    class DownloadCustomerHistory extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            bmodel.synchronizationHelper.downloadCustomerSearch(edt_contact.getText().toString());
        }
    }

    private void setValues() {
        String name = edt_name.getText().toString();
        String addr = edt_address.getText().toString();
        String contact = edt_contact.getText().toString();
        String freqVisit = edt_freqVisit.getText().toString();
        String email = edt_email.getText().toString();
        String ageGroup = ageRadioGroup.getCheckedRadioButtonId() + "";
        String gender = (chkMale.isChecked() ? "M" : "F");

        if (!name.equals("") || !addr.equals("") || !contact.equals("") || !freqVisit.equals("") || !email.equals("")) {
            if (bmodel.getCounterSaleBO() != null) {
                bmodel.getCounterSaleBO().setCustomerName(name);
                bmodel.getCounterSaleBO().setAddress(addr);
                bmodel.getCounterSaleBO().setContactNumber(contact);
                bmodel.getCounterSaleBO().setFreqVisit(freqVisit);
                bmodel.getCounterSaleBO().setEmail(email);
                bmodel.getCounterSaleBO().setAgeGroup(ageGroup);
                bmodel.getCounterSaleBO().setGender(gender);
            } else {
                CounterSaleBO csBo = new CounterSaleBO();
                csBo.setCustomerName(name);
                csBo.setAddress(addr);
                csBo.setContactNumber(contact);
                csBo.setFreqVisit(freqVisit);
                csBo.setEmail(email);
                csBo.setAgeGroup(ageGroup);
                csBo.setGender(gender);

                bmodel.setCounterSaleBO(csBo);
            }
        }
    }


    CShistoryDialog mHistoryDialog;

    @Override
    public void onDismiss(String referenceId) {
        refid = referenceId;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(receiver);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    public boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }

    class loadSummaryScreen extends AsyncTask<String, Void, Void> {
        AlertDialog alertDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_SALE_CS");
                bmodel.productHelper
                        .downloadProductsWithFiveLevelFilter("MENU_SALE_CS");

            } else {
                bmodel.productHelper.downloadProductFilter("MENU_SALE_CS");
                bmodel.productHelper.downloadProducts("MENU_SALE_CS");
            }

            if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {
                bmodel.productHelper.downloadChildSKUs();
            }

            bmodel.mCounterSalesHelper.downloadCSStock();

            bmodel.schemeDetailsMasterHelper.downloadSchemeMethods();

            bmodel.productHelper.updateCounterSalesProductColor();

            //setting header detail in object, because user can able to see summary screen from sale screen..
            setValues();

            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().isDraft()) {
                bmodel.getCounterSaleBO().setmSalesproduct(bmodel.mCounterSalesHelper.getDraftedSaleProducts(uid));
            }

            bmodel.mSelectedActivityName = "Sales";

            bmodel.schemeDetailsMasterHelper.loadOrderedBuyProductsForCounter(uid);
            bmodel.schemeDetailsMasterHelper.loadOrderedFreeProductsForCounter(uid);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (alertDialog != null) {
                alertDialog.dismiss();
            }

            if (bmodel.getCounterSaleBO() != null &&
                    ((bmodel.getCounterSaleBO().getmTestProducts().size() > 0)
                            || (!bmodel.getCounterSaleBO().getResolution().equals("") || bmodel.getCounterSaleBO().getAttributeId() != 0 || !bmodel.getCounterSaleBO().getConnsultingFeedback().equals(""))
                            || (bmodel.getCounterSaleBO().getmSampleProducts() != null)
                            || (bmodel.getCounterSaleBO().getmSalesproduct() != null))
                    || (surveyHelperNew.hasDataToSave())) {


                if (edt_email.getText().toString().length() > 0 && !isValidEmail(edt_email.getText().toString())) {

                    Toast.makeText(getActivity(), getResources().getString(R.string.enter_valid_email_id), Toast.LENGTH_LONG).show();
                    return;
                }
                if (edt_name.getText().toString().equals("")) {
                    Toast.makeText(getActivity(), "Customer name is Mandatory to save", Toast.LENGTH_LONG).show();
                    return;
                }
                Intent i = new Intent(getActivity(),
                        CS_sale_summary.class);
                i.putExtra("refid", refid);
                i.putExtra("isFromSale", false);
                startActivity(i);
                getActivity().finish();


            } else {
                Toast.makeText(getActivity(), R.string.no_data_tosave, Toast.LENGTH_LONG).show();
            }


        }
    }

    public void mDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        alertDialogBuilder
                .setIcon(null)
                .setCancelable(false)
                .setTitle(
                        getResources().getString(
                                R.string.do_you_want_delete_order))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                bmodel.mCounterSalesHelper.deletedSalesDetails(uid);
                                bmodel.getCounterSaleBO().setSaleDrafted(false);
                                bmodel.getCounterSaleBO().setmSalesproduct(null);
                                onResume();
                                Toast.makeText(getActivity(), R.string.order_deleted_sucessfully + uid, Toast.LENGTH_SHORT).show();
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        bmodel.applyAlertDialogTheme(alertDialogBuilder);
    }
}
