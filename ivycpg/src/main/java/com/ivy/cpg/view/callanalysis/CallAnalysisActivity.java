
package com.ivy.cpg.view.callanalysis;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.sync.SyncContractor;
import com.ivy.cpg.view.sync.UploadHelper;
import com.ivy.cpg.view.sync.UploadPresenterImpl;
import com.ivy.cpg.view.van.vanunload.VanUnLoadModuleHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SyncRetailerBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SBDHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.SyncRetailerSelectActivity;
import com.ivy.sd.png.view.SyncVisitedRetailer;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.Vector;

public class CallAnalysisActivity extends IvyBaseActivityNoActionBar
        implements View.OnClickListener, SyncContractor.SyncView {

    public static final String MENU_CALL_ANLYS = "MENU_CALL_ANLYS";

    private BusinessModel bmodel;

    private Spinner spinnerNoOrderReason, spinnerNooCollectionReason, spinnerFeedback;
    private Button mNoOrderCameraBTN;
    private EditText edt_noOrderReason;
    private EditText edt_other_remarks;

    private TextView tv_edt_time_taken;
    protected TextView TVMenuName;

    private ArrayAdapter<ReasonMaster> collectionReasonAdapter;
    private ArrayAdapter<ReasonMaster> feedBackReasonAdapter;


    private boolean collectionReasonFlag = false;
    private String mImageName;
    private String mImagePath;
    private boolean isPhotoTaken = false;
    private String mFeedbackReasonId = "";
    private String mFeedBackId = "0";

    //Close Call - CallA38
    protected CardView contentCloseCall;


    protected boolean isCloseCallAsMenu = false;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private ProgressDialog progressDialog;
    private UploadPresenterImpl presenter;
    private SharedPreferences mLastSyncSharedPref;
    private static int REQUEST_CODE_RETAILER_WISE_UPLOAD = 100;

    private boolean isSubmitButtonClicked = false;

    private Vector<ConfigureBO> menuDB = new Vector<>();
    private Button img_pause;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_analysis_new);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        // Handling session out
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getIntent().getStringExtra("screentitle"));
        }

        mLastSyncSharedPref = getSharedPreferences("lastSync", Context.MODE_PRIVATE);

        VanUnLoadModuleHelper mVanUnloadHelper = VanUnLoadModuleHelper.getInstance(this);
        UploadHelper mUploadHelper = UploadHelper.getInstance(this);

        presenter = new UploadPresenterImpl(this, bmodel, this, mUploadHelper, mVanUnloadHelper);


        /* set handler for the Timer class */
        if (bmodel.timer != null) {
            bmodel.timer.setHandler(handler);
        }

        CardView content_card = findViewById(R.id.content_card);
        try {

            RecyclerView recyclerView = findViewById(R.id.callAnalysisListRecycler);
            RecyclerView rvModule = findViewById(R.id.module_recylcer);

            spinnerNoOrderReason = findViewById(R.id.spinnerNoorderreason);
            spinnerNooCollectionReason = findViewById(R.id.spinnerNooCollectionReason);
            spinnerFeedback = findViewById(R.id.spinner_feedback);
            edt_noOrderReason = findViewById(R.id.edtNoorderreason);
            edt_other_remarks = findViewById(R.id.edt_other_remarks);
            mNoOrderCameraBTN = findViewById(R.id.btn_camera);
            contentCloseCall = findViewById(R.id.content_closeCallCard);
            TVMenuName = findViewById(R.id.tvMenuName);
            img_pause = findViewById(R.id.img_pause);

            contentCloseCall.setVisibility(View.GONE);

            if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_CAPTURE_PHOTO) {
                mNoOrderCameraBTN.setVisibility(View.VISIBLE);
                edt_noOrderReason.setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_EDITTEXT) {
                edt_noOrderReason.setVisibility(View.VISIBLE);
                mNoOrderCameraBTN.setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.IS_SHOW_PAUSE_CALL_ANALYSIS)
                img_pause.setVisibility(View.VISIBLE);

            mNoOrderCameraBTN.setOnClickListener(this);

            if (bmodel.configurationMasterHelper.SHOW_GLOBAL_NO_ORDER_REASON && (hasOrderScreenEnabled() && (hasActivityDone() || bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON)
                    && bmodel.getRetailerMasterBO().getIsOrdered().equals("N"))) {
                spinnerNoOrderReason.setVisibility(View.VISIBLE);
                bmodel.reasonHelper.downloadNonProductiveReasonMaster(); // Do not remove this method as this will cause translation error in "Others" string
                spinnerNoOrderReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (parent.getSelectedItem().toString().equalsIgnoreCase(getResources().getString(R.string.other_reason_with_credit))) {
                            edt_other_remarks.setVisibility(View.VISIBLE);
                        } else {
                            hideKeyboard();
                            edt_other_remarks.setText("");
                            edt_other_remarks.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            } else {
                spinnerNoOrderReason.setVisibility(View.GONE);
                if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_EDITTEXT)
                    edt_noOrderReason.setVisibility(View.GONE);
                if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_CAPTURE_PHOTO)
                    mNoOrderCameraBTN.setVisibility(View.GONE);
            }


            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_REASON
                    && hasInvoice() && !bmodel.configurationMasterHelper.IS_COLLECTION_MANDATE) {
                if (hasCollectionMenuActivityDone())
                    spinnerNooCollectionReason.setVisibility(View.GONE);
                else {
                    collectionReasonFlag = true;
                    spinnerNooCollectionReason.setVisibility(View.VISIBLE);
                }
            } else {
                spinnerNooCollectionReason.setVisibility(View.GONE);
            }


            if (bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL) {

                feedBackReasonAdapter = new ArrayAdapter<>(this, R.layout.call_analysis_spinner_layout);
                feedBackReasonAdapter
                        .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
                feedBackReasonAdapter.add(new ReasonMaster("0", getResources().getString(R.string.select_feedback)));
                loadFeedbackReason();
                spinnerFeedback.setAdapter(feedBackReasonAdapter);
                spinnerFeedback.setVisibility(View.VISIBLE);
            } else {
                spinnerFeedback.setVisibility(View.GONE);
            }


            collectionReasonAdapter = new ArrayAdapter<>(this,
                    R.layout.call_analysis_spinner_layout);
            collectionReasonAdapter.add(new ReasonMaster("0", getResources().getString(R.string.select_reason_for_no_collection)));
            loadCollectionReason();
            collectionReasonAdapter
                    .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
            spinnerNooCollectionReason.setAdapter(collectionReasonAdapter);

            ArrayAdapter<ReasonMaster> spinnerAdapter = new ArrayAdapter<>(this,
                    R.layout.call_analysis_spinner_layout);
            spinnerAdapter.add(new ReasonMaster(-1 + "", getResources().getString(R.string.select_reason_for_no_order)));
            if (bmodel.reasonHelper.getNonProductiveReasonMaster() != null) {
                for (ReasonMaster temp : bmodel.reasonHelper
                        .getNonProductiveReasonMaster())
                    spinnerAdapter.add(temp);
            }
            spinnerAdapter
                    .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
            spinnerNoOrderReason.setAdapter(spinnerAdapter);

            if (bmodel.getRetailerMasterBO().getRetailerID() == null) {
                Toast.makeText(
                        this,
                        getResources()
                                .getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            Vector<ConfigureBO> callanalysismenu = bmodel.configurationMasterHelper
                    .downloadCallAnalysisMenu();

            ArrayList<ConfigureBO> configlist = updateCallAnalysisMenu(callanalysismenu);

            for (ConfigureBO configureBO : callanalysismenu) {
                if (configureBO.getConfigCode().equalsIgnoreCase("CallA38")) {
                    contentCloseCall.setVisibility(View.VISIBLE);
                    TVMenuName.setText(StringUtils.isNullOrEmpty(configureBO.getMenuName()) ? "Activity Completion Status" : configureBO.getMenuName());
                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                    linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                    ModuleAdapter moduleAdapter = new ModuleAdapter(getTimeTakenData());
                    rvModule.setLayoutManager(linearLayoutManager);
                    rvModule.setItemAnimator(new DefaultItemAnimator());
                    rvModule.setAdapter(moduleAdapter);
                }
            }


            if (DeviceUtils.isTabletDevice(this)) {
                if (configlist.size() > 0) {

                    GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
                    RecyclerAdapter recyclerAdapter = new RecyclerAdapter(configlist);
                    recyclerView.setLayoutManager(gridLayoutManager);
                    recyclerView.setItemAnimator(new DefaultItemAnimator());

                    recyclerView.setAdapter(recyclerAdapter);
                } else {
                    content_card.setVisibility(View.GONE);
                }

            } else {
                LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
                linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
                RecyclerAdapter recyclerAdapter = new RecyclerAdapter(configlist);
                recyclerView.setLayoutManager(linearLayoutManager);
                recyclerView.setItemAnimator(new DefaultItemAnimator());

                recyclerView.setAdapter(recyclerAdapter);
                if (configlist == null || configlist.size() == 0) {
                    content_card.setVisibility(View.GONE);
                }
            }


            TextView tv_duration = findViewById(R.id.tv_duration);
            tv_duration.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));

            tv_edt_time_taken = findViewById(R.id.edt_time_taken);
            tv_edt_time_taken.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));

            if (bmodel.configurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
                findViewById(R.id.ll_duration).setVisibility(View.GONE);

            Button btn_close = findViewById(R.id.button1);
            btn_close.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));

            TextView tv_sale = findViewById(R.id.sale);
            tv_sale.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));

            View view_sale_header = findViewById(R.id.view_dotted_line);
            view_sale_header.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            //updating FIT score for current retailer
            bmodel.updateCurrentFITscore(bmodel.getRetailerMasterBO());
            // update Total Weight
            bmodel.updateRetailersTotWgt(bmodel.getRetailerMasterBO());

            SBDHelper.getInstance(this).calculateSBDDistribution(getApplicationContext());

            img_pause.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    bmodel.updateIsVisitedFlag("P");

                    // stop timer
                    if (bmodel.timer != null) {
                        bmodel.timer.pauseTimer();
                        bmodel.timer = null;
                    }

                    bmodel.outletTimeStampHelper.updateTimeStamp(DateTimeUtils
                            .now(DateTimeUtils.TIME), mFeedbackReasonId);


                    if (!hasActivityDone() && !bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL && !bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON) {
                        bmodel.outletTimeStampHelper.deleteTimeStampAllModule();
                        bmodel.outletTimeStampHelper.deleteTimeStamp();
                        bmodel.outletTimeStampHelper.deleteTimeStampImages();
                        bmodel.outletTimeStampHelper.deleteImagesFromFolder();
                        bmodel.outletTimeStampHelper.deleteTimeStampRetailerDeviation();

                    } else {
                        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                                .now(DateTimeUtils.TIME));
                        bmodel.saveModuleCompletion("MENU_CALL_ANLYS", true);
                    }
                    resetRemarksBO();
                    if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        resetSellerConfiguration();
                    }

                    bmodel.productHelper.clearProductHelper();
                    finish();
                }
            });
        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        private ArrayList<ConfigureBO> configlist;

        private RecyclerAdapter(ArrayList<ConfigureBO> configlist) {
            this.configlist = configlist;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.call_analysis_list_item, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.Name.setText(configlist.get(position).getMenuName());

            try {
                if (configlist.get(position).getKpiTarget().equals("-1")) {
                    //  holder.ll_seekbar.setVisibility(View.GONE);
                    // holder.tv_progress_text.setVisibility(View.GONE);

                    holder.tv_achieved_value.setText(configlist.get(position).getMenuNumber());
                    //holder.tv_target_value.setVisibility(View.GONE);

                } else {
                    // holder.ll_seekbar.setVisibility(View.VISIBLE);
                    StringBuilder sb = new StringBuilder();
                    // holder.tv_progress_text.setVisibility(View.VISIBLE);
                    // holder.tv_target_value.setVisibility(View.VISIBLE);

                    //holder.seekBar.setEnabled(false);
                    //  holder.seekBar.setProgress((int) SDUtil.convertToDouble(configlist.get(position).getKpiAchieved()));
                    //holder.seekBar.setMax((int) SDUtil.convertToDouble(configlist.get(position).getKpiTarget()));

                    sb.append(bmodel.formatValue
                            (SDUtil.convertToDouble(configlist.get(position)
                                    .getKpiAchieved())));

                    sb.append("/" + bmodel.formatValue(SDUtil.convertToDouble
                            (configlist.get(position).getKpiTarget())));


                    // holder.tv_achieved_value.setText(bmodel.formatValue(SDUtil.convertToDouble(configlist.get(position).getKpiAchieved())));
                    //  holder.tv_target_value.setText("/" + bmodel.formatValue(SDUtil.convertToDouble(configlist.get(position).getKpiTarget())));

                    if ((int) SDUtil.convertToDouble(configlist.get(position).getKpiTarget()) > 0) {
                        int ach = (int) SDUtil.convertToDouble(configlist.get(position).getKpiAchieved());
                        int tgt = (int) SDUtil.convertToDouble(configlist.get(position).getKpiTarget());
                        int percent = (ach * 100) / tgt;
                        if (percent > 100) {
                            percent = 100;
                        }
                        sb.append(" (" + percent + "%" + ")");
                        //holder.tv_progress_text.setText(percent + "% " + getResources().getString(R.string.percent_of_tot_target_achieved));
                    }
                    holder.tv_achieved_value.setText(sb.toString());
                }
            } catch (Exception ex) {
                Commons.printException(ex);
            }


            TypedArray typearr = CallAnalysisActivity.this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                holder.itemView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                holder.itemView.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }
        }

        @Override
        public int getItemCount() {
            return configlist.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView Name;
            // TextView tv_target_value;
            TextView tv_achieved_value;
            // LinearLayout ll_seekbar;
            //  SeekBar seekBar;
            //   TextView tv_progress_text;

            MyViewHolder(View row) {
                super(row);
                Name = row.findViewById(R.id.menunametxt);
                Name.setTypeface(FontUtils.getFontRoboto(CallAnalysisActivity.this, FontUtils.FontType.MEDIUM));
                tv_achieved_value = row.findViewById(R.id.tv_menuvalue_achieved);
                tv_achieved_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            }

        }
    }


    class ModuleAdapter extends RecyclerView.Adapter<ModuleAdapter.MyViewHolder> {

        private ArrayList<ConfigureBO> configlist;

        private ModuleAdapter(ArrayList<ConfigureBO> configlist) {
            this.configlist = configlist;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.module_time_list, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {

            if (configlist.get(position).isDone()) {
                holder.timeSpent.setVisibility(View.VISIBLE);
                holder.ivDone.setImageResource(R.drawable.ic_tick_enable);
                holder.timeSpent.setText(configlist.get(position).getRegex());
            } else {
                holder.timeSpent.setVisibility(View.GONE);
                holder.ivDone.setImageResource(R.drawable.ic_cross_enable);
            }
            holder.menuName.setText(configlist.get(position).getMenuName());

        }

        @Override
        public int getItemCount() {
            return configlist.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView menuName;
            TextView timeSpent;
            ImageView ivDone;

            MyViewHolder(View row) {
                super(row);
                menuName = row.findViewById(R.id.menunametxt);
                timeSpent = row.findViewById(R.id.tv_time_spent);
                ivDone = row.findViewById(R.id.iv_done);
            }

        }
    }

    public ArrayList<ConfigureBO> updateCallAnalysisMenu(
            Vector<ConfigureBO> callanalysismenu) {
        ArrayList<ConfigureBO> config = new ArrayList<>();

        try {
            double day_obj = (bmodel.getRetailerMasterBO().getDaily_target_planned());
            double mtd_obj = (bmodel.getRetailerMasterBO().getMonthly_target());
            double mtd_act = (bmodel.getRetailerMasterBO()
                    .getMonthly_acheived());

            double day_act;
            if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                if (bmodel.getRetailerMasterBO().getIsVansales() == 1)
                    day_act = bmodel.getInvoiceAmount();
                else
                    day_act = bmodel.getOrderValue();

            } else {
                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    day_act = bmodel.getInvoiceAmount();
                } else {
                    day_act = bmodel.getOrderValue();
                }
            }

            double mtd_actul = (mtd_act + day_act);

            double salesPercentValue;
            if (day_obj > 0) {

                salesPercentValue = (day_act / day_obj) * 100;
            } else {
                salesPercentValue = 0;
            }
            if (getResources().getBoolean(R.bool.config_is_achieved_max_100)) {
                if (salesPercentValue > 100)
                    salesPercentValue = 100;
            }

            // parvalue
            float vday_tar = (float) bmodel.getRetailerMasterBO()
                    .getVisit_frequencey();
            float vday_ach = bmodel.getRetailerMasterBO().getVisitDoneCount();
            float vday_ach_today = vday_ach + 1;

            double target_pervisit = 0;
            double planned_acheived;
            double par_value;

            if (vday_tar > 0) {

                target_pervisit = (mtd_obj / vday_tar);
            }
            if (target_pervisit > 0) {
                planned_acheived = (vday_ach_today * target_pervisit);
                par_value = (mtd_act / planned_acheived) * 100;

            } else {

                par_value = 0;
            }

            if (getResources().getBoolean(R.bool.config_is_achieved_max_100)) {
                if (par_value > 100) {
                    par_value = 100;
                }
            }

            float SBDAchievePer;

            if (bmodel.getRetailerMasterBO().getSbdDistributionTarget() > 0) {
                SBDAchievePer = ((float) bmodel.getRetailerMasterBO()
                        .getSbdDistributionAchieve() / (float) bmodel
                        .getRetailerMasterBO().getSbdDistributionTarget()) * 100;
            } else {
                SBDAchievePer = 0;
            }


            int size = callanalysismenu.size();
            ConfigureBO con;

            for (int i = 0; i < size; i++) {
                con = new ConfigureBO();
                if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA10")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA0")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA2")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA3")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA12")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.formatValue(day_act) + "/"
                            + bmodel.formatValue(day_obj) + " , "
                            + bmodel.formatPercent(salesPercentValue) + " %");
                    if (callanalysismenu.get(i).getConfigCode()
                            .equalsIgnoreCase("CallA12"))

                        con.setKpiTarget(day_obj + "");
                    con.setKpiAchieved(day_act + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA1")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber((bmodel.formatValue(par_value) + " %"));


                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA19")) {

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.formatValue(day_act) + "/"
                            + bmodel.formatValue(day_obj) + " , "
                            + bmodel.formatPercent(salesPercentValue) + " %");

                    con.setKpiTarget(day_obj + "");
                    con.setKpiAchieved(day_act + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA5")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.retailerMasterBO
                            .getInitiative_achieved()
                            + "/"
                            + bmodel.retailerMasterBO.getInitiative_target()
                            + " , "
                            + bmodel.formatPercent(SDUtil.convertToFloat(bmodel
                            .getRetailerMasterBO()
                            .getInitiativePercent())) + " %");

                    con.setKpiTarget(bmodel.retailerMasterBO.getInitiative_target() + "");
                    con.setKpiAchieved(bmodel.retailerMasterBO
                            .getInitiative_achieved() + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA6")) {

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.getRetailerMasterBO()
                            .getSBDMerchAchieved()
                            + "/"
                            + bmodel.getRetailerMasterBO().getSBDMerchTarget()
                            + " , "
                            + bmodel.formatPercent(SDUtil.convertToFloat(bmodel
                            .getRetailerMasterBO().getSbdMercPercent()))
                            + " %");

                    con.setKpiTarget(bmodel.getRetailerMasterBO().getSBDMerchTarget() + "");
                    con.setKpiAchieved(bmodel.getRetailerMasterBO()
                            .getSBDMerchAchieved() + "");
                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA7")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());

                    con.setMenuNumber(bmodel.formatValue(bmodel
                            .getCollectionValue()) + "");


                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA13")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA11")) {

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    SBDHelper.getInstance(this).calculateSBDDistribution(getApplicationContext());
                    con.setMenuNumber(bmodel.getRetailerMasterBO()
                            .getSbdDistributionAchieve()
                            + "/"
                            + bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget()
                            + " , "
                            + bmodel.formatPercent(SBDAchievePer) + "%");

                    con.setKpiTarget(bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget() + "");
                    con.setKpiAchieved(bmodel.getRetailerMasterBO()
                            .getSbdDistributionAchieve() + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA14")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA4")) {

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.getRetailerMasterBO()
                            .getSbdDistStock()
                            + "/"
                            + bmodel.configurationMasterHelper
                            .getsbddistpostwihtouthistory()
                            + "/"
                            + bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget()
                            + " , "
                            + bmodel.formatPercent(SBDAchievePer) + "%");


                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA15")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA8")
                        || callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA9")) {

                    double per;
                    if (mtd_obj != 0) {
                        per = (mtd_actul / mtd_obj) * 100;
                    } else {
                        per = 0;
                    }
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.formatValue(mtd_actul) + "/"
                            + bmodel.formatValue(mtd_obj) + " , "
                            + bmodel.formatPercent(per));

                    con.setKpiTarget(mtd_obj + "");
                    con.setKpiAchieved(mtd_actul + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA16")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.getRetailerMasterBO()
                            .getSbdMerchInitAcheived()
                            + "/"
                            + bmodel.getRetailerMasterBO()
                            .getSbdMerchInitTarget()
                            + " , "
                            + bmodel.formatPercent(SDUtil.convertToFloat(bmodel
                            .getRetailerMasterBO()
                            .getSbdMerchInitPrecent())) + " %");

                    con.setKpiTarget(bmodel.getRetailerMasterBO()
                            .getSbdMerchInitTarget() + "");
                    con.setKpiAchieved(bmodel.getRetailerMasterBO()
                            .getSbdMerchInitAcheived() + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equals("CallA17")) {

                    con.setMenuName(callanalysismenu.get(i).getMenuName());

                    int totalLines = bmodel.getTotalLines();

                    con.setMenuNumber(totalLines + "");
                    con.setKpiTarget("-1");
                    con.setKpiAchieved(totalLines + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equals("CallA18")) {
                    double sbdpercent = ((float) bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget()) == 0 ? 0
                            : (bmodel.getPreSbdAchieved() / (float) bmodel
                            .getRetailerMasterBO()
                            .getSbdDistributionTarget()) * 100;
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.getPreSbdAchieved()
                            + "/"
                            + bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget() + " , "
                            + bmodel.formatPercent(sbdpercent) + "%");

                    con.setKpiTarget(bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget() + "");
                    con.setKpiAchieved(bmodel.getPreSbdAchieved() + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equals("CallA20")) {
                    double totalIndicativeOrderValue = bmodel.productHelper
                            .getTotalIndicativeOrderAmount();

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber("("
                            + bmodel.formatValue(totalIndicativeOrderValue)
                            + "/"
                            + bmodel.formatValue((day_act - totalIndicativeOrderValue))
                            + ")/" + bmodel.formatValue(day_obj) + ","
                            + bmodel.formatPercent(salesPercentValue) + " %");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equals("CallA21")) {
                    String totalCaseVolme;
                    if (bmodel.configurationMasterHelper.IS_INVOICE)
                        totalCaseVolme = bmodel.productHelper
                                .getOrderDetailVolume("InvoiceMaster",
                                        "InvoiceDetails", "OD.caseQty",
                                        "OH.InvoiceNo = OD.InvoiceID ");
                    else
                        totalCaseVolme = bmodel.productHelper
                                .getOrderDetailVolume("OrderHeader",
                                        "OrderDetail", "OD.caseQty",
                                        "OH.OrderID = OD.OrderID");

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(totalCaseVolme + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equals("CallA22")) {

                    String totalCaseVolme;
                    if (bmodel.configurationMasterHelper.IS_INVOICE)
                        totalCaseVolme = bmodel.productHelper
                                .getOrderDetailVolume("InvoiceMaster",
                                        "InvoiceDetails", "OD.pcsQty",
                                        "OH.InvoiceNo = OD.InvoiceID ");
                    else
                        totalCaseVolme = bmodel.productHelper
                                .getOrderDetailVolume("OrderHeader",
                                        "OrderDetail", "OD.pieceqty",
                                        "OH.OrderID = OD.OrderID");

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(totalCaseVolme + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equals("CallA23")) {

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.formatValue(day_act) + "");

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA29")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    final float totalWeight = DashBoardHelper.getInstance(this).getTotalWeight(bmodel.getRetailerMasterBO().getRetailerID());
                    con.setMenuNumber(totalWeight + "");
                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA30")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    final double salesReturnValue = SalesReturnHelper.getInstance(this).getSalesRetunTotalValue(getApplicationContext());
                    con.setMenuNumber(bmodel.formatValue(salesReturnValue) + "");

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA31")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    double salesReturnValue = SalesReturnHelper.getInstance(this).getSalesReturnValue(getApplicationContext());
                    //day_act - order Value
                    if (salesReturnValue > day_act)
                        con.setMenuNumber("0");
                    else
                        con.setMenuNumber(bmodel.formatValue(day_act - salesReturnValue));

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA33")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    int totalFocusBrand;
                    int totalFocusBrand1 = 0;
                    int totalFocusBrand2 = 0;
                    int totalFocusBrand3 = 0;
                    int totalFocusBrand4 = 0;
                    for (ProductMasterBO productBO : bmodel.productHelper.getProductMaster()) {
                        if (productBO.getIsFocusBrand() == 1) {
                            totalFocusBrand1 += 1;
                        }
                        if (productBO.getIsFocusBrand2() == 1) {
                            totalFocusBrand2 += 1;
                        }
                        if (productBO.getIsFocusBrand3() == 1) {
                            totalFocusBrand3 += 1;
                        }
                        if (productBO.getIsFocusBrand4() == 1) {
                            totalFocusBrand4 += 1;
                        }
                    }
                    totalFocusBrand = totalFocusBrand1 + totalFocusBrand2 + totalFocusBrand3 + totalFocusBrand4;

                    con.setMenuNumber(bmodel.getTotalFocusBrandLines() + "/" + totalFocusBrand);

                    con.setKpiTarget(totalFocusBrand + "");
                    con.setKpiAchieved(bmodel.getTotalFocusBrandLines() + "");

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA34")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    int totalMSP = 0;
                    for (ProductMasterBO productBO : bmodel.productHelper.getProductMaster()) {
                        if (productBO.getIsMustSell() == 1) {
                            totalMSP++;
                        }
                    }
                    con.setMenuNumber(bmodel.getTotalMSLLines() + "/" + totalMSP);

                    con.setKpiTarget(totalMSP + "");
                    con.setKpiAchieved(bmodel.getTotalMSLLines() + "");

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA35")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());

                    bmodel.productHelper.getDistributionLevels();
                    con.setMenuNumber(bmodel.productHelper.achLevelID + "/" + bmodel.productHelper.totLevelID);

                    con.setKpiTarget(bmodel.productHelper.totLevelID + "");
                    con.setKpiAchieved(bmodel.productHelper.achLevelID + "");
                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA36")) {
                    //FIT score
                    config.addAll(bmodel.getFITscore());

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA37")) {
                    //Group wise FIT score
                    config.addAll(bmodel.getGroupWiseFITScore());

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA38")) {
                    isCloseCallAsMenu = true;
                } else if (callanalysismenu.get(i).getConfigCode().equals("CallA39")) {
                    String totalVolume;
                    if (bmodel.configurationMasterHelper.IS_INVOICE)
                        totalVolume = bmodel.productHelper
                                .getOrderDetailVolume("InvoiceMaster",
                                        "InvoiceDetails", "OD.Qty",
                                        "OH.InvoiceNo = OD.InvoiceID ");
                    else
                        totalVolume = bmodel.productHelper
                                .getOrderDetailVolume("OrderHeader",
                                        "OrderDetail", "OD.Qty",
                                        "OH.OrderID = OD.OrderID");

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(totalVolume + "");

                } else if (callanalysismenu.get(i).getConfigCode().equals("CallA40")) {

                    String salesReturnVolume;

                    salesReturnVolume = bmodel.productHelper
                            .getOrderDetailVolume("SalesReturnHeader",
                                    "SalesReturnDetails", "OD.totalQty",
                                    "OH.uid = OD.uid");

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(salesReturnVolume + "");
                }

                if (!callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA36")
                        && !callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA37")
                        && !callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA38")) {

                    config.add(con);
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return config;

    }

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

    public void onClose(View v) {
        hideKeyboard();
        try {

            if (bmodel.configurationMasterHelper.IS_COLLECTION_MANDATE
                    && bmodel.retailerMasterBO.getRpTypeCode().equalsIgnoreCase("CASH")
                    && bmodel.hasPendingInvoice(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), bmodel.getRetailerMasterBO().getRetailerID())) {

                Toast.makeText(this, getResources().getString(R.string.collection_mandatory), Toast.LENGTH_SHORT).show();
                return;
            }

            if (bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL && !hasActivityDone()) {
                ReasonMaster reasonMaster = (ReasonMaster) spinnerFeedback.getSelectedItem();
                mFeedbackReasonId = reasonMaster.getReasonDesc();
                mFeedBackId = reasonMaster.getReasonID();
            }

            // No use so commented by Abbas.
            //getMessage();

            ReasonMaster reason = (ReasonMaster) spinnerNoOrderReason
                    .getSelectedItem();
            String mSelectedReasonId = reason.getReasonID();
            if (bmodel.configurationMasterHelper.SHOW_GLOBAL_NO_ORDER_REASON && (hasOrderScreenEnabled() && (hasActivityDone() || bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON)
                    && bmodel.getRetailerMasterBO().getIsOrdered().equals("N"))) {
                if (reason.getReasonID().equals("-1")) {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.select_no_order_reason),
                            Toast.LENGTH_LONG).show();
                } else if (mSelectedReasonId.equals("0") && edt_other_remarks.getText().toString().equals("")) {
                    Toast.makeText(this, getResources().getString(R.string.enter_remarks), Toast.LENGTH_LONG).show();
                } else if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_CAPTURE_PHOTO && !isPhotoTaken) {
                    Toast.makeText(this, getResources().getString(R.string.photo_mandatory), Toast.LENGTH_SHORT).show();
                } else {
                    bmodel.outletTimeStampHelper.deleteTimeStampModuleWise("MENU_STK_ORD");
                    showCollectionReasonOrDialog();
                }
            } else if (!hasActivityDone() && bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL) {
                showFeedbackReasonOrDialog();
            } else {
                showCollectionReasonOrDialog();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }

    private void showCollectionReasonOrDialog() {
        if (collectionReasonFlag) {
            ReasonMaster collectioReasonBO = (ReasonMaster) spinnerNooCollectionReason
                    .getSelectedItem();
            if (collectioReasonBO.getReasonID().equals("0")) {
                showCollectionReason();
            } else {
                if (!isCloseCallAsMenu)
                    showDialog(0);
                else {
                    closeCallDone();
                }
            }
        } else {
            if (!isCloseCallAsMenu)
                showDialog(0);
            else {
                closeCallDone();
            }
        }
    }

    private void showFeedbackReasonOrDialog() {
        ReasonMaster reasonMasterBO = (ReasonMaster) spinnerFeedback.getSelectedItem();
        if (reasonMasterBO.getReasonID().equals("0")) {
            Toast.makeText(CallAnalysisActivity.this, getResources().getString(R.string.select_feedback_reason), Toast.LENGTH_SHORT).show();
        } else {
            showCollectionReasonOrDialog();
        }
    }


    private String getMessage() {
        StringBuilder sb = new StringBuilder();
        boolean isStoreCheckMenu = false;
        boolean isStockOrder = false;

        menuDB = bmodel.configurationMasterHelper.getActivityMenu();


        for (ConfigureBO config : menuDB) {
            if (config.getConfigCode().equals(ConfigurationMasterHelper.MENU_STORECHECK))
                isStoreCheckMenu = true;
            if (config.getHasLink() == 1 && !config.isDone()
                    && !config.getConfigCode().equals(MENU_CALL_ANLYS)) {
                sb.append(config.getMenuName()).append(" ").append(getResources().getString(R.string.is_not_done)).append("\n");
            }

            if (config.getHasLink() == 1 && !config.isDone()
                    && config.getConfigCode().equals("MENU_STK_ORD")) {
                isStockOrder = true;
            }

        }


        if (isStoreCheckMenu) {
            Vector<ConfigureBO> mInStoreMenu = bmodel.configurationMasterHelper
                    .getStoreCheckMenu();
            for (ConfigureBO config : mInStoreMenu) {
                if (config.getHasLink() == 1 && !config.isDone()
                        && !config.getConfigCode().equals("MENU_CLOSE")) {

                    sb.append(config.getMenuName()).append(" ").append(getResources().getString(R.string.is_not_done)).append("\n");
                }

            }
        }

        // Order taken but focus pack not ordered
        if (bmodel.configurationMasterHelper.IS_FOCUS_PACK_NOT_DONE && !isStockOrder) {
            bmodel.getOrderedFocusBrandList();
            if (bmodel.getTotalFocusBrandLines() < bmodel.getTotalFocusBrands()) {
                StringBuilder msg = new StringBuilder();
                for (String focusBrand : bmodel.getTotalFocusBrandList()) {
                    if (!bmodel.getOrderedFocusBrands().contains(focusBrand))
                        msg.append(focusBrand).append(", ");
                }

                sb.append(getResources().getString(R.string.order_not_placed_focus_pack)).append(" ").append(msg.toString().trim().substring(0, msg.toString().trim().length() - 1)).append(". \n");
            }
        }

        return sb.toString();

    }

    /**
     * Check whether any activity is done on this call or not.
     *
     * @return boolean
     */
    private boolean hasActivityDone() {
        try {
            if (isClosingStockDone()) {
                return true;
            } else {
                menuDB = bmodel.configurationMasterHelper.getActivityMenu();

                for (ConfigureBO config : menuDB) {
                    if (!config.getConfigCode().equals(MENU_CALL_ANLYS)
                            && !config.getConfigCode().equals(StandardListMasterConstants.MENU_COLLECTION_VIEW)
                            && !config.getConfigCode().equals(StandardListMasterConstants.MENU_REV)) {
                        if (config.getHasLink() == 1 && config.isDone()) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }

    public boolean isClosingStockDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(CallAnalysisActivity.this, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select stockid from "
                    + DataMembers.tbl_closingstockheader + " where retailerid="
                    + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID())
                    + " AND DistributorID=" + bmodel.getRetailerMasterBO().getDistributorId());
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    /**
     * Method to check order screen enable or not
     *
     * @return boolean
     */
    private boolean hasOrderScreenEnabled() {
        menuDB = bmodel.configurationMasterHelper.getActivityMenu();
        for (ConfigureBO configureBO : menuDB) {
            if ((configureBO.getConfigCode().equals("MENU_ORDER") ||
                    configureBO.getConfigCode().equals("MENU_STK_ORD") ||
                    configureBO.getConfigCode().equals("MENU_CATALOG_ORDER") && configureBO.getHasLink() == 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasCollectionMenuActivityDone() {

        try {
            menuDB = bmodel.configurationMasterHelper.getActivityMenu();

            for (ConfigureBO config : menuDB) {
                if (config.getHasLink() == 1
                        && config.isDone()
                        && config.getConfigCode().equals(
                        StandardListMasterConstants.MENU_COLLECTION)) {
                    return true;
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }


    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(CallAnalysisActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_u_want_close_call))
                        .setMessage(getMessage())
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        isSubmitButtonClicked = false;

                                        closeCallDone();

                                    }

                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                if (bmodel.configurationMasterHelper.IS_SYNC_FROM_CALL_ANALYSIS) {
                    builder.setNeutralButton(getResources().getString(R.string.submit), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {

                            showDialog(2);


                        }
                    });
                }

                bmodel.applyAlertDialogTheme(builder);
                break;

            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CallAnalysisActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.please_finish_mandatory_modules))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        resetRemarksBO();
                                        bmodel.outletTimeStampHelper
                                                .updateTimeStampModuleWise(DateTimeUtils
                                                        .now(DateTimeUtils.TIME));
                                       /* BusinessModel.loadActivity(
                                                CallAnalysisActivity.this,
                                                DataMembers.actHomeScreenTwo);*/
                                        Intent myIntent = new Intent(CallAnalysisActivity.this, HomeScreenTwo.class);
                                        startActivityForResult(myIntent, 0);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;
            case 2:
                AlertDialog.Builder builder_sync = new AlertDialog.Builder(CallAnalysisActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.are_you_sure_you_want_to_submit))
                        .setMessage(getResources().getString(R.string.submitted_orders_not_allowed_to_edit))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        isSubmitButtonClicked = true;
                                        closeCallDone();
//                                        presenter.isFromCallAnalysis = true;
//                                        presenter.validateAndUpload();
                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder_sync);
                break;

        }
        return null;

    }

    private void closeCallDone() {


        if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
            bmodel.configurationMasterHelper.IS_SIH_VALIDATION = true;
        }

        if (collectionReasonFlag) {
            ReasonMaster collectioReasonBO = (ReasonMaster) spinnerNooCollectionReason
                    .getSelectedItem();
            if (!collectioReasonBO.getReasonID()
                    .equals("0")) {
                doCallAnalysisCloseAction(
                        collectioReasonBO
                                .getReasonID(),
                        StandardListMasterConstants.COLLECTION_REASON_TYPE);
            } else {
                showCollectionReason();

            }
        } else {
            doCallAnalysisCloseAction("0", "");
        }
    }

    private void showCollectionReason() {
        Toast.makeText(CallAnalysisActivity.this, getResources().getString(R.string.select_collection_reason), Toast.LENGTH_SHORT).show();
    }

    private void showNoOrderReason() {
        Toast.makeText(CallAnalysisActivity.this, getResources().getString(R.string.select_noorder_reason), Toast.LENGTH_SHORT).show();
    }

    private void doCallAnalysisCloseAction(String collectionReasonID,
                                           String collectionReasonType) {
        if (collectionReasonType
                .equals(StandardListMasterConstants.COLLECTION_REASON_TYPE)
                && collectionReasonID.equals("0")) {
            showCollectionReason();
            return;
        }

        ReasonMaster temp = (ReasonMaster) spinnerNoOrderReason
                .getSelectedItem();
        if (!temp.getReasonID().equals("-1")) {

            // Consider it as a non productive
            NonproductivereasonBO nonproductive = new NonproductivereasonBO();
            nonproductive.setReasonid(temp.getReasonID());
            nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO()
                    .getDownloadDate());
            nonproductive.setReasontype("NP");
            nonproductive.setCollectionReasonID(collectionReasonID);
            nonproductive.setCollectionReasonType(collectionReasonType);
            nonproductive.setImagePath(mImagePath);
            nonproductive.setImageName(mImageName);
            bmodel.saveNonproductivereason(nonproductive, edt_other_remarks.getText().toString());
            bmodel.updateIsVisitedFlag("Y");
            // Alert the user
            Toast.makeText(CallAnalysisActivity.this,
                    getResources().getString(R.string.reason_saved),
                    Toast.LENGTH_SHORT).show();
        } else if (!collectionReasonID.equals("0")) {
            NonproductivereasonBO nonproductive = new NonproductivereasonBO();
            nonproductive.setReasonid("0");
            nonproductive.setDate(bmodel.userMasterHelper.getUserMasterBO()
                    .getDownloadDate());
            nonproductive.setReasontype("");
            nonproductive.setCollectionReasonID(collectionReasonID);
            nonproductive.setCollectionReasonType(collectionReasonType);
            bmodel.saveNonproductivereason(nonproductive, "");
            bmodel.updateIsVisitedFlag("Y");
            // Alert the user
            Toast.makeText(CallAnalysisActivity.this,
                    getResources().getString(R.string.reason_saved),
                    Toast.LENGTH_SHORT).show();
        } else if (!mFeedBackId.equals("0") || hasActivityDone()) {
            bmodel.updateIsVisitedFlag("Y");
        }else if(bmodel.configurationMasterHelper.IS_SHOW_PAUSE_CALL_ANALYSIS){
            bmodel.updateIsVisitedFlag("N");
        }


        // Rollback the review plan if review
        // done not order or stock
        if (bmodel.getRetailerMasterBO().getIsReviewPlan().equals("Y")
                && bmodel.getRetailerMasterBO().getIsOrderMerch().equals("N")) {
            bmodel.setIsReviewPlan("N");
            bmodel.getRetailerMasterBO().setIsReviewPlan("N");
        }
        // Roll Back collection View done if not not order or stock not done
        if (bmodel.getRetailerMasterBO().getIsCollectionView().equals("Y")
                && bmodel.getRetailerMasterBO().getIsOrderMerch().equals("N")) {
            bmodel.getRetailerMasterBO().setIsCollectionView("N");
        }


        // stop timer
        if (bmodel.timer != null) {
            bmodel.timer.stopTimer();
            bmodel.timer = null;
        }

        if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_EDITTEXT) {
            if (edt_noOrderReason.getText().length() > 0) {
                bmodel.outletTimeStampHelper.updateTimeStamp(DateTimeUtils
                        .now(DateTimeUtils.TIME), edt_noOrderReason.getText().toString());
            } else {
                showNoOrderReason();
            }
        } else {
            bmodel.outletTimeStampHelper.updateTimeStamp(DateTimeUtils
                    .now(DateTimeUtils.TIME), mFeedbackReasonId);
        }

        if (!hasActivityDone() && !bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL && !bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON) {
            bmodel.outletTimeStampHelper.deleteTimeStampAllModule();
            bmodel.outletTimeStampHelper.deleteTimeStamp();
            bmodel.outletTimeStampHelper.deleteTimeStampImages();
            bmodel.outletTimeStampHelper.deleteImagesFromFolder();
            bmodel.outletTimeStampHelper.deleteTimeStampRetailerDeviation();

        } else {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            bmodel.saveModuleCompletion("MENU_CALL_ANLYS", true);
        }
        resetRemarksBO();
        if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
            resetSellerConfiguration();
        }

        bmodel.productHelper.clearProductHelper();

        if (isSubmitButtonClicked) {
            presenter.isFromCallAnalysis = true;
            presenter.validateAndUpload(false);
        } else {
//            BusinessModel.loadActivity(CallAnalysisActivity.this,
//                    DataMembers.actPlanning);
            finish();
        }

    }

    private void resetSellerConfiguration() {
        bmodel.configurationMasterHelper.IS_SIH_VALIDATION = bmodel.configurationMasterHelper.IS_SIH_VALIDATION_MASTER;
        bmodel.configurationMasterHelper.IS_STOCK_IN_HAND = bmodel.configurationMasterHelper.IS_STOCK_IN_HAND_MASTER;
        bmodel.configurationMasterHelper.IS_WSIH = bmodel.configurationMasterHelper.IS_WSIH_MASTER;
        SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_ON = SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_ON_MASTER;
        SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_SHOW_SCREEN = SchemeDetailsMasterHelper.getInstance(this).IS_SCHEME_SHOW_SCREEN_MASTER;
        bmodel.configurationMasterHelper.SHOW_TAX = bmodel.configurationMasterHelper.SHOW_TAX_MASTER;
        bmodel.configurationMasterHelper.IS_INVOICE = bmodel.configurationMasterHelper.IS_INVOICE_MASTER;
    }

    private void resetRemarksBO() {
        bmodel.setOrderHeaderNote("");
        bmodel.setRField1("");
        bmodel.setRField2("");
        bmodel.setSaleReturnNote("");
        bmodel.setSaleReturnRfValue("");
        bmodel.setStockCheckRemark("");
        bmodel.setAssetRemark("");
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view view to unbind
     */
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

    public Handler getHandler() {
        return handler;
    }


    public void loadCollectionReason() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(StandardListMasterConstants.COLLECTION_REASON_TYPE));

            if (c != null) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    collectionReasonAdapter.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void loadFeedbackReason() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster(StandardListMasterConstants.FEEDBACK_TYPE));
            if (c != null) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    feedBackReasonAdapter.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean hasInvoice() {
        try {
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select InvoiceNo from "
                    + DataMembers.tbl_InvoiceMaster + " where Retailerid='"
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + "' and upload = 'N'");
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    return true;
                }

            }
            db.closeDB();
        } catch (Exception e) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            resetRemarksBO();
           /* BusinessModel.loadActivity(CallAnalysisActivity.this,
                    DataMembers.actHomeScreenTwo);*/
            Intent myIntent = new Intent(this, HomeScreenTwo.class);
            startActivityForResult(myIntent, 0);
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        if (vw == mNoOrderCameraBTN) {
            ReasonMaster reason = (ReasonMaster) spinnerNoOrderReason
                    .getSelectedItem();
            if (reason.getReasonID().equals("-1")) {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.select_no_order_reason),
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (FileUtils.isExternalStorageAvailable(10)) {

                String mModuleName = "MENU_CALL_ANLYS";
                mImageName = "NP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "_" + bmodel.retailerMasterBO.getRetailerID()
                        + "_" + mModuleName
                        + "_" + Commons.now(Commons.DATE_TIME) + "_img.jpg";

                mImagePath = "NonProductive/"
                        + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                        .replace("/", "") + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mImageName;

                String fnameStarts = "NP_" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "_" + bmodel.retailerMasterBO.getRetailerID()
                        + "_" + mModuleName
                        + "_" + Commons.now(Commons.DATE);

                boolean nFilesThere = bmodel
                        .checkForNFilesInFolder(
                                FileUtils.photoFolderPath, 1,
                                fnameStarts);
                if (nFilesThere) {
                    showFileDeleteAlert(fnameStarts);
                } else {
                    Intent intent = new Intent(this,
                            CameraActivity.class);
                    intent.putExtra(CameraActivity.QUALITY, 40);
                    String path = FileUtils.photoFolderPath + "/"
                            + mImageName;
                    intent.putExtra(CameraActivity.PATH, path);
                    startActivityForResult(intent,
                            bmodel.CAMERA_REQUEST_CODE);
                }

            }

        }
    }

    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(CallAnalysisActivity.this);
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + " " + 1 + " "
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        isPhotoTaken = false;
                        Intent intent = new Intent(CallAnalysisActivity.this,
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = FileUtils.photoFolderPath + "/" + mImageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == bmodel.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(bmodel.mSelectedActivityName
                        + "Camers Activity : Sucessfully Captured.");
                isPhotoTaken = true;
                bmodel.reasonHelper.saveImage(mImageName, mImagePath);

            }
        } else if (requestCode == REQUEST_CODE_RETAILER_WISE_UPLOAD) {
            if (resultCode == Activity.RESULT_OK) {
                presenter.prepareSelectedRetailerIds();
                if (presenter.getVisitedRetailerId() != null
                        && presenter.getVisitedRetailerId().toString().length() > 0) {
                    presenter.upload();
                } else {
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_unsubmitted_orders), 0);
                }

            }
        } else {
            Commons.print(bmodel.mSelectedActivityName
                    + "Camers Activity : Canceled");
            isPhotoTaken = false;
        }
    }

    private void hideKeyboard() {
        try {
            View view = this.getCurrentFocus();
            if (view != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                if (CallAnalysisActivity.this.isDestroyed()) { // or call isFinishing() if min sdk version < 17
                    return false;
                }
            } else if (CallAnalysisActivity.this.isFinishing()) { // or call isFinishing() if min sdk version < 17
                return false;
            }

            switch (msg.what) {

                case DataMembers.NOTIFY_UPDATE:
                    builder = new AlertDialog.Builder(CallAnalysisActivity.this);
                    setMessageInProgressDialog(builder, msg.obj.toString());
                    alertDialog.show();
                    return true;

                case DataMembers.NOTIFY_NOT_USEREXIST:
                    dismissProgressDialog();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.password_does_not_match), 0);
                    return true;

                case DataMembers.NOTIFY_NO_INTERNET:
                    dismissProgressDialog();
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
                    return true;

                case DataMembers.NOTIFY_CONNECTION_PROBLEM:
                    dismissProgressDialog();
                    bmodel.showAlert(
                            getResources()
                                    .getString(R.string.no_network_connection), 0);
                    return true;

                case DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL:
                    dismissProgressDialog();
                    bmodel.showAlert(getResources().getString(R.string.sessionout_loginagain), 0);
                    return true;


                case DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC:
                    // obj will contain a string representing the error message
                    dismissCurrentProgressDialog();
                    if (msg.obj instanceof String) {
                        String errorMessage = (String) msg.obj;
                        dismissCurrentProgressDialog();
                        displayMessage(errorMessage);
                    }
                    CallAnalysisActivity.this.finish();
                    return true;


                case DataMembers.NOTIFY_UPLOADED_CONTINUE:
                    dismissProgressDialog();
                    presenter.upload();
                    return true;

                case DataMembers.NOTIFY_UPLOADED:
                    if (!bmodel.configurationMasterHelper.IS_SYNC_WITH_IMAGES
                            && (presenter.getImageFilesCount() > 0 || presenter.getTextFilesCount() > 0)) {

                        builder = new AlertDialog.Builder(CallAnalysisActivity.this);
                        setMessageInProgressDialog(builder, getResources().getString(
                                R.string.image_uploading));
                        alertDialog.show();
                        presenter.uploadImages();


                    } else {
                        dismissProgressDialog();
                        updateLastSync();

                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.successfully_uploaded),
                                6004);
                    }
                    return true;
                case DataMembers.NOTIFY_UPLOAD_ERROR:
                    dismissProgressDialog();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.upload_failed_please_try_again), 0);
                    return true;
                case DataMembers.NOTIFY_UPLOADED_IMAGE:
                    if (bmodel.configurationMasterHelper.SHOW_SYNC_RETAILER_SELECT)
                        presenter.loadRetailerSelectionScreen();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.images_sucessfully_uploaded), 0);
                    return true;
                case DataMembers.NOTIFY_UPLOAD_ERROR_IMAGE:
                    dismissProgressDialog();
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.images_upload_failed_please_try_again),
                            0);
                    return true;

                case DataMembers.NOTIFY_WEB_UPLOAD_ERROR:

                    dismissProgressDialog();
                    bmodel.showAlert((String) msg.obj, 0);
                    return true;
                case DataMembers.NOTIFY_WEB_UPLOAD_SUCCESS:

                    bmodel.photocount = 0;
                    dismissProgressDialog();

                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.successfully_uploaded),
                            6004);

                    return true;
                case DataMembers.NOTIFY_CALL_ANALYSIS_TIMER:
                    if (tv_edt_time_taken != null && msg.obj != null)
                        tv_edt_time_taken.setText(msg.obj.toString());
                    return true;

                default:
                    return false;
            }
        }
    });


    private void setMessageInProgressDialog(AlertDialog.Builder builder, String message) {
        LayoutInflater inflater = (LayoutInflater) CallAnalysisActivity.this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.custom_alert_dialog,
                (ViewGroup) CallAnalysisActivity.this.findViewById(R.id.layout_root));
        TextView messagetv = layout.findViewById(R.id.text);
        messagetv.setText(message);
        builder.setView(layout);
        builder.setCancelable(false);
    }


    /**
     * If there is a progress dialog, dismiss it and set progressDialog to null.
     */
    public void dismissCurrentProgressDialog() {
        if (progressDialog != null) {
            progressDialog.hide();
            progressDialog.dismiss();
            progressDialog = null;
        }
    }

    /**
     * Displays a message to the user, in the form of a Toast.
     *
     * @param message Message to be displayed.
     */
    public void displayMessage(String message) {
        if (message != null) {
            Toast.makeText(CallAnalysisActivity.this, message, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLastSync() {
        SharedPreferences.Editor edt = mLastSyncSharedPref.edit();
        edt.putString("date", DateTimeUtils.convertFromServerDateToRequestedFormat(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat));
        edt.putString("time", DateTimeUtils.now(DateTimeUtils.TIME));
        edt.apply();
    }

    //
    @Override
    public void showAttendanceNotCompletedToast() {
        bmodel.showAlert(
                getResources()
                        .getString(R.string.attendance_activity_not_completed), 0);
    }

    @Override
    public void showNoInternetToast() {
        bmodel.showAlert(
                getResources()
                        .getString(R.string.no_network_connection), 0);
    }

    @Override
    public void showOrderExistWithoutInvoice() {
        bmodel.showAlert(
                getResources().getString(
                        R.string.order_exist_without_invoice),
                0);
    }

    @Override
    public void showNoDataExist() {
        Toast.makeText(this,
                getResources().getString(R.string.no_data_exists),
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showProgressLoading() {

        dismissProgressDialog();
        builder = new AlertDialog.Builder(this);
        customProgressDialog(builder, getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void showProgressUploading() {
        dismissProgressDialog();
        builder = new AlertDialog.Builder(this);
        customProgressDialog(builder, getResources().getString(R.string.uploading_data));
        alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void cancelProgress() {
        dismissProgressDialog();
    }

    @Override
    public void showRetailerSelectionScreen(List<SyncRetailerBO> isVisitedRetailerList) {
        Intent intent = new Intent(this, SyncRetailerSelectActivity.class);
        SyncVisitedRetailer catObj = new SyncVisitedRetailer(isVisitedRetailerList);
        Bundle bun = new Bundle();
        bun.putParcelable("list", catObj);
        intent.putExtras(bun);
        startActivityForResult(intent, REQUEST_CODE_RETAILER_WISE_UPLOAD);
    }

    @Override
    public void showAlertImageUploadRecommended() {
        showAlertOkCancel(
                getResources()
                        .getString(
                                R.string.image_upload_recommended),
                3);
    }

    @Override
    public void showAlertNoUnSubmittedOrder() {
        bmodel.showAlert(
                getResources().getString(
                        R.string.no_unsubmitted_orders),
                0);
    }

    public void showAlertOkCancel(String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(
                this);
        builder.setCancelable(false);
        builder.setMessage(msg);
        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (idd == 0) {
                            presenter.validateAndUpload(false);
                        } else if (idd == 3) {
                            // isClicked = false;
                            // withPhotosCheckBox.setChecked(true);
                            presenter.updateIsWithImageStatus(true);
                            presenter.upload();
                        }
                    }

                });
        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // isClicked = false;
                        if (idd == 3) {
                            presenter.upload();
                        }
                    }
                });


        bmodel.applyAlertDialogTheme(builder);
    }

    private void dismissProgressDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private ArrayList<ConfigureBO> getTimeTakenData() {
        ArrayList<ConfigureBO> moduleList = new ArrayList<>();
        boolean isStoreCheckMenu = false;
        SimpleDateFormat format = new SimpleDateFormat("yyyy/mm/dd HH:mm:ss");
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String language = sharedPrefs.getString("languagePref", ApplicationConfigs.LANGUAGE);
        try {
            DBUtil db = new DBUtil(CallAnalysisActivity.this, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("select OD.TimeIn,OD.TimeOut,ifnull(hm.MName,'') from "
                    + DataMembers.tbl_outlet_time_stamp_detail
                    + " OD Left join HhtMenuMaster hm on hm.HHTCode = OD.ModuleCode and lang="
                    + StringUtils.getStringQueryParam(language)
                    + " where retailerid=" + StringUtils.getStringQueryParam(bmodel.getRetailerMasterBO().getRetailerID())
                    + " AND ModuleCode != 'MENU_CALL_ANLYS'");

            if (c != null) {
                while (c.moveToNext()) {
                    ConfigureBO configureBO = new ConfigureBO();
                    Date date1 = format.parse(c.getString(0));
                    Date date2 = format.parse(c.getString(1));
                    long difference = date2.getTime() - date1.getTime();

                    configureBO.setMenuName(c.getString(2));
                    configureBO.setDone(true);
                    configureBO.setRegex(String.format("%02d:%02d:%02d",
                            difference / (60 * 60 * 1000) % 24,
                            difference / (60 * 1000) % 60,
                            difference / 1000 % 60));

                    moduleList.add(configureBO);
                }
            }

            Objects.requireNonNull(c).close();
            db.closeDB();

            Map<String, ConfigureBO> myMap = new HashMap<>();
            for (ConfigureBO collectionBO : moduleList) {
                String menuName = collectionBO.getMenuName();
                if (myMap.containsKey(menuName)) {
                    String time1=collectionBO.getRegex();
                    String time2=myMap.get(menuName).getRegex();
                    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");
                    timeFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date1 = timeFormat.parse(time1);
                    Date date2 = timeFormat.parse(time2);
                    long sum = date1.getTime() + date2.getTime();
                    String date3 = timeFormat.format(new Date(sum));
                    myMap.get(menuName).setRegex(date3);
                } else
                    myMap.put(menuName, collectionBO);
            }
            moduleList.clear();

            for (String productMenuCode : myMap.keySet()) {
                moduleList.add(myMap.get(productMenuCode));
            }

            for (ConfigureBO config : bmodel.configurationMasterHelper.getActivityMenu()) {
                if (config.getConfigCode().equals(ConfigurationMasterHelper.MENU_STORECHECK))
                    isStoreCheckMenu = true;
                if (config.getHasLink() == 1 && !config.isDone()
                        && !config.getConfigCode().equals(MENU_CALL_ANLYS)) {
                    if(!myMap.keySet().contains(config.getMenuName())){
                        moduleList.add(config);
                    }
                }

            }


            if (isStoreCheckMenu) {
                Vector<ConfigureBO> mInStoreMenu = bmodel.configurationMasterHelper
                        .getStoreCheckMenu();
                for (ConfigureBO config : mInStoreMenu) {
                    if (config.getHasLink() == 1 && !config.isDone()
                            && !config.getConfigCode().equals("MENU_CLOSE"))
                        moduleList.add(config);


                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return moduleList;
    }

}
