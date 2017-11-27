
package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SalesReturnHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.ScreenOrientation;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.ArrayList;
import java.util.Vector;

public class CallAnalysisActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private BusinessModel bmodel;
    private ImageView mGoldStoreIV;
    private Spinner spinnerNoOrderReason, spinnerNooCollectionReason, spinnerFeedback;
    private TextView mTime;
    private ArrayAdapter<ReasonMaster> spinnerAdapter, collectionReasonAdapter, feedBackReasonAdapter;
    public static final String MENU_CALL_ANLYS = "MENU_CALL_ANLYS";
    private ListView listview;
    private ArrayList<ConfigureBO> visitConfig;
    private Vector<ConfigureBO> callanalysismenu = new Vector<ConfigureBO>();
    private int inittarget;
    private Button mNoOrderCameraBTN;
    private int salestarget;
    private double day_act = 0;
    private double day_obj;
    private int disttgt;
    private int merchtgt;
    private boolean collectionReasonFlag = false;
    private String mImageName, mImagePath, mSelectedReasonId;
    private String mModuleName = "MENU_CALL_ANLYS";
    boolean isPhotoTaken = false;
    private String mFeedbackReasonId = "";
    private String mFeedBackId = "";

    private Toolbar toolbar;
    TextView tv_store_status, tv_duration, tv_edt_time_taken, tv_sale;
    EditText edt_noOrderReason;
    EditText edt_other_remarks;
    Button btn_close;
    private RelativeLayout rl_store_status;
    private CardView content_card;

    View view_sale_header;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_analysis_new);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        /** Handling session out */
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

        mTime = (TextView) findViewById(R.id.edt_time_taken);

        /** set handler for the Timer class */
        if (bmodel.timer != null) {
            bmodel.timer.setHandler(handler);
        }
        content_card = (CardView) findViewById(R.id.content_card);
        try {

            listview = (ListView) findViewById(R.id.callAnalysisList);
            listview.setOnTouchListener(new OnTouchListener() {
                // Setting on Touch Listener for handling the touch inside
                // ScrollView
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    // Disallow the touch request for parent scroll on touch of
                    // child view
                    v.getParent().requestDisallowInterceptTouchEvent(true);
                    return false;
                }
            });

            mGoldStoreIV = (ImageView) findViewById(R.id.goldstore_iv);
            rl_store_status = (RelativeLayout) findViewById(R.id.rl_store_status);
            spinnerNoOrderReason = (Spinner) findViewById(R.id.spinnerNoorderreason);
            spinnerNooCollectionReason = (Spinner) findViewById(R.id.spinnerNooCollectionReason);
            spinnerFeedback = (Spinner) findViewById(R.id.spinner_feedback);
            edt_noOrderReason = (EditText) findViewById(R.id.edtNoorderreason);
            edt_other_remarks = (EditText) findViewById(R.id.edt_other_remarks);
            mNoOrderCameraBTN = (Button) findViewById(R.id.btn_camera);

            if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_CAPTURE_PHOTO) {
                mNoOrderCameraBTN.setVisibility(View.VISIBLE);
                edt_noOrderReason.setVisibility(View.GONE);
            }
            if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_EDITTEXT) {
                edt_noOrderReason.setVisibility(View.VISIBLE);
                mNoOrderCameraBTN.setVisibility(View.GONE);
            }

            mNoOrderCameraBTN.setOnClickListener(this);


            String[] dateTime = bmodel.outletTimeStampHelper.getTimeIn().split(" ");

            if ((hasOrderScreenEnabled() && (hasActivityDone() || bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON)
                    && bmodel.getRetailerMasterBO().getIsOrdered().equals("N"))) {
                spinnerNoOrderReason.setVisibility(View.VISIBLE);
                spinnerNoOrderReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                        if (parent.getSelectedItem().toString().equals("Others")) {
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
            }


            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_REASON
                    && hasInvoice()) {
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

                feedBackReasonAdapter = new ArrayAdapter<ReasonMaster>(this, R.layout.call_analysis_spinner_layout);
                feedBackReasonAdapter
                        .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
                feedBackReasonAdapter.add(new ReasonMaster("0", getResources().getString(R.string.select_feedback)));
                loadFeedbackReason();
                spinnerFeedback.setAdapter(feedBackReasonAdapter);
                spinnerFeedback.setVisibility(View.VISIBLE);
            } else {
                spinnerFeedback.setVisibility(View.GONE);
            }


            collectionReasonAdapter = new ArrayAdapter<ReasonMaster>(this,
                    R.layout.call_analysis_spinner_layout);
            collectionReasonAdapter.add(new ReasonMaster("0", getResources().getString(R.string.select_reason_for_no_collection)));
            loadCollectionReason();
            collectionReasonAdapter
                    .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
            spinnerNooCollectionReason.setAdapter(collectionReasonAdapter);

            spinnerAdapter = new ArrayAdapter<ReasonMaster>(this,
                    R.layout.call_analysis_spinner_layout);
            spinnerAdapter.add(new ReasonMaster(-1 + "", getResources().getString(R.string.select_reason_for_no_order)));
            for (ReasonMaster temp : bmodel.reasonHelper
                    .getNonProductiveReasonMaster())
                spinnerAdapter.add(temp);
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

            callanalysismenu = bmodel.configurationMasterHelper
                    .downloadCallAnalysisMenu();

            ArrayList<ConfigureBO> configlist = updateCallAnalysisMenu(callanalysismenu);


            if (ScreenOrientation.isTabletDevice(this)) {
                if (configlist.size() > 0) {
                    ArrayList<ConfigureBO> lstLeft = new ArrayList<>();
                    ArrayList<ConfigureBO> lstRight = new ArrayList<>();

                    for (int i = 0; i < configlist.size(); i++) {

                        if (i % 2 == 0) {
                            lstLeft.add(configlist.get(i));
                        } else {
                            lstRight.add(configlist.get(i));
                        }
                    }
                    ListArrayAdapter adapter = new ListArrayAdapter(this,
                            R.layout.call_analysis_list_item, lstLeft, lstRight);
                    listview.setAdapter(adapter);
                } else {
                    content_card.setVisibility(View.GONE);
                }

            } else {
                ListArrayAdapter adapter = new ListArrayAdapter(this,
                        R.layout.call_analysis_list_item, configlist, null);
                listview.setAdapter(adapter);
                if (configlist == null || configlist.size() == 0) {
                    content_card.setVisibility(View.GONE);
                }
            }


            updateGoldFlag();

            tv_store_status = (TextView) findViewById(R.id.tv_store_status);
            tv_store_status.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            tv_duration = (TextView) findViewById(R.id.tv_duration);
            tv_duration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            tv_edt_time_taken = (TextView) findViewById(R.id.edt_time_taken);
            tv_edt_time_taken.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

            btn_close = (Button) findViewById(R.id.button1);
            btn_close.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

            tv_sale = (TextView) findViewById(R.id.sale);
            tv_sale.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

            view_sale_header = (View) findViewById(R.id.view_dotted_line);
            view_sale_header.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

            //updating FIT score for current retailer
            bmodel.updateCurrentFITscore(bmodel.getRetailerMasterBO());
        } catch (Exception e) {
            Commons.printException(e);

        }
    }


    private void updateGoldFlag() {

        boolean sbdFalg = true, sbdMerch = true, initFalg = true, valueFlag = true;
        float sbdDistTarget = (float) bmodel.getRetailerMasterBO()
                .getSbdDistributionTarget() * (float) disttgt / 100;
        float sbdMerchTarget = (float) bmodel.getRetailerMasterBO()
                .getSBDMerchTarget() * (float) merchtgt / 100;

        float initiativetarget = (float) bmodel.getRetailerMasterBO()
                .getInitiative_target() * (float) inittarget / 100;

        float Salestarget = (float) day_obj * (float) salestarget / 100;
        if (disttgt > 0) {
            if (sbdDistTarget <= bmodel.retailerMasterBO
                    .getSbdDistributionAchieve()
                    && bmodel.retailerMasterBO.getSbdDistributionTarget() != 0) {
                sbdFalg = true;
            } else
                sbdFalg = false;
        }
        if (merchtgt > 0) {
            if (sbdMerchTarget <= bmodel.retailerMasterBO.getSBDMerchAchieved()
                    && bmodel.retailerMasterBO.getSBDMerchTarget() != 0) {
                sbdMerch = true;
            } else {
                sbdMerch = false;
            }
        }
        if (inittarget > 0) {
            if (initiativetarget <= bmodel.retailerMasterBO
                    .getInitiative_achieved()
                    && bmodel.retailerMasterBO.getInitiative_target() != 0) {
                initFalg = true;
            } else {
                initFalg = false;
            }
        }
        if (salestarget > 0) {
            if (Salestarget <= day_act && day_obj != 0) {
                valueFlag = true;
            } else {
                valueFlag = false;
            }
        }
        Commons.print("dist" + sbdFalg + " merch" + sbdMerchTarget + " init"
                + initFalg + " sales" + valueFlag);
        if (disttgt == 0 && merchtgt == 0 && inittarget == 0 && salestarget == 0) {
            rl_store_status.setVisibility(View.GONE);
        } else {
            if (sbdFalg && sbdMerch && initFalg && valueFlag) {
                mGoldStoreIV.setImageResource(R.drawable.icon_star_gold);
                updateGoldenStore(1);
            } else {
                mGoldStoreIV.setImageResource(R.drawable.icon_star);
                updateGoldenStore(0);
            }
            rl_store_status.setVisibility(View.VISIBLE);
        }

    }

    public class ListArrayAdapter extends ArrayAdapter {
        private Context context;
        private int layoutResourceId;
        ArrayList<ConfigureBO> configlist;
        ArrayList<ConfigureBO> configlist_second;

        public ListArrayAdapter(CallAnalysisActivity applicationContext,
                                int simpleExpandableListItem2,
                                ArrayList<ConfigureBO> configlist_first, ArrayList<ConfigureBO> configlist_sec) {
            super(applicationContext, simpleExpandableListItem2, configlist_first);
            context = applicationContext;
            layoutResourceId = simpleExpandableListItem2;
            configlist = configlist_first;
            configlist_second = configlist_sec;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            View row = convertView;
            OperationHolder holder = null;

            if (convertView == null) {

                LayoutInflater inflater = ((Activity) context)
                        .getLayoutInflater();
                row = inflater.inflate(layoutResourceId, parent, false);


                holder = new OperationHolder(row);
                row.setTag(holder);
            } else
                holder = (OperationHolder) row.getTag();


            holder.Name.setText(configlist.get(position).getMenuName());

            try {
                if (configlist.get(position).getKpiTarget().equals("-1")) {
                    holder.ll_seekbar.setVisibility(View.GONE);
                    holder.tv_progress_text.setVisibility(View.GONE);

                    holder.tv_achieved_value.setText(configlist.get(position).getMenuNumber());
                    holder.tv_target_value.setVisibility(View.GONE);

                } else {
                    holder.ll_seekbar.setVisibility(View.VISIBLE);
                    holder.tv_progress_text.setVisibility(View.VISIBLE);
                    holder.tv_target_value.setVisibility(View.VISIBLE);

                    holder.seekBar.setEnabled(false);
                    holder.seekBar.setProgress((int) Double.parseDouble(configlist.get(position).getKpiAchieved()));
                    holder.seekBar.setMax((int) Double.parseDouble(configlist.get(position).getKpiTarget()));


                    holder.tv_achieved_value.setText(bmodel.formatValue(Double.parseDouble(configlist.get(position).getKpiAchieved())));
                    holder.tv_target_value.setText("/" + bmodel.formatValue(Double.parseDouble(configlist.get(position).getKpiTarget())));

                    if ((int) Double.parseDouble(configlist.get(position).getKpiTarget()) > 0) {
                        int ach = (int) Double.parseDouble(configlist.get(position).getKpiAchieved());
                        int tgt = (int) Double.parseDouble(configlist.get(position).getKpiTarget());
                        int percent = (ach * 100) / tgt;
                        if (percent > 100) {
                            percent = 100;
                        }
                        holder.tv_progress_text.setText(percent + "% " + getResources().getString(R.string.percent_of_tot_target_achieved));
                    } /*else if ((int) Double.parseDouble(configlist.get(position).getKpiTarget()) <= 0 && (int) Double.parseDouble(configlist.get(position).getKpiAchieved()) > 0) {
                        holder.tv_progress_text.setText("100" + getResources().getString(R.string.percent_of_tot_target_achieved));
                    }*/
                }
            } catch (Exception ex) {
                Commons.printException(ex);
            }

            if (row.findViewById(R.id.ll_second_layout) != null && position < configlist_second.size()) {// Tab view

                row.findViewById(R.id.ll_second_layout).setVisibility(View.VISIBLE);
                holder.view_separator.setVisibility(View.VISIBLE);

                holder.Name_right.setText(configlist_second.get(position).getMenuName());


                try {
                    if (configlist_second.get(position).getKpiTarget().equals("-1")) {
                        holder.ll_seekbar_right.setVisibility(View.GONE);
                        holder.tv_progress_text_right.setVisibility(View.GONE);

                        holder.tv_achieved_value_right.setText(configlist_second.get(position).getMenuNumber());
                        holder.tv_target_value_right.setVisibility(View.GONE);

                    } else {
                        holder.ll_seekbar_right.setVisibility(View.VISIBLE);
                        holder.tv_progress_text_right.setVisibility(View.VISIBLE);
                        holder.tv_target_value_right.setVisibility(View.VISIBLE);

                        holder.seekBar_right.setProgress((int) Double.parseDouble(configlist_second.get(position).getKpiAchieved()));
                        holder.seekBar_right.setMax((int) Double.parseDouble(configlist_second.get(position).getKpiTarget()));
                        holder.seekBar_right.setEnabled(false);

                        holder.tv_achieved_value_right.setText(bmodel.formatValue(Double.parseDouble(configlist_second.get(position).getKpiAchieved())));
                        holder.tv_target_value_right.setText("/" + bmodel.formatValue(Double.parseDouble(configlist_second.get(position).getKpiTarget())));

                        if ((int) Double.parseDouble(configlist_second.get(position).getKpiTarget()) > 0) {
                            int ach = (int) Double.parseDouble(configlist_second.get(position).getKpiAchieved());
                            int tgt = (int) Double.parseDouble(configlist_second.get(position).getKpiTarget());
                            int percent = (ach * 100) / tgt;
                            if (percent > 100) {
                                percent = 100;
                            }
                            holder.tv_progress_text_right.setText(percent + "% " + getResources().getString(R.string.percent_of_tot_target_achieved));
                        } else if ((int) Double.parseDouble(configlist_second.get(position).getKpiTarget()) <= 0 && (int) Double.parseDouble(configlist_second.get(position).getKpiAchieved()) > 0) {
                            holder.tv_progress_text_right.setText("100% " + getResources().getString(R.string.percent_of_tot_target_achieved));
                        }
                    }
                } catch (Exception ex) {
                    Commons.printException(ex);
                }

            }

            TypedArray typearr = CallAnalysisActivity.this.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }


            return row;

        }

        public class OperationHolder {

            TextView Name;
            TextView tv_target_value;
            TextView tv_achieved_value;
            LinearLayout ll_seekbar;
            SeekBar seekBar;
            TextView tv_progress_text;

            TextView Name_right;
            TextView tv_target_value_right;
            TextView tv_achieved_value_right;
            LinearLayout ll_seekbar_right;
            SeekBar seekBar_right;
            TextView tv_progress_text_right;

            View view_separator;

            OperationHolder(View row) {

                Name = (TextView) row.findViewById(R.id.menunametxt);
                Name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tv_achieved_value = (TextView) row.findViewById(R.id.tv_menuvalue_achieved);
                tv_achieved_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                tv_target_value = (TextView) row.findViewById(R.id.tv_menuvalue_target);
                tv_target_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ll_seekbar = (LinearLayout) row.findViewById(R.id.ll_seekbar);
                seekBar = (SeekBar) row.findViewById(R.id.seek);
                tv_progress_text = (TextView) row.findViewById(R.id.tv_progress_text);
                tv_progress_text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                //
                if (row.findViewById(R.id.ll_second_layout) != null) {// Tab view
                    Name_right = (TextView) row.findViewById(R.id.menunametxt_two);
                    Name_right.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    tv_achieved_value_right = (TextView) row.findViewById(R.id.tv_menuvalue_achieved_two);
                    tv_achieved_value_right.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    tv_target_value_right = (TextView) row.findViewById(R.id.tv_menuvalue_target_two);
                    tv_target_value_right.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    ll_seekbar_right = (LinearLayout) row.findViewById(R.id.ll_seekbar_two);
                    seekBar_right = (SeekBar) row.findViewById(R.id.seek_two);
                    tv_progress_text_right = (TextView) row.findViewById(R.id.tv_progress_text_two);
                    tv_progress_text_right.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                    view_separator = (View) row.findViewById(R.id.view_separator);
                }

            }
        }
    }

    public ArrayList<ConfigureBO> updateCallAnalysisMenu(
            Vector<ConfigureBO> callanalysismenu) {
        ArrayList<ConfigureBO> config = new ArrayList<ConfigureBO>();

        try {
            day_obj = (bmodel.getRetailerMasterBO().getDaily_target_planned());
            double mtd_obj = (bmodel.getRetailerMasterBO().getMonthly_target());
            double mtd_act = (bmodel.getRetailerMasterBO()
                    .getMonthly_acheived());

            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
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
            double planned_acheived = 0;
            double par_value = 0;

            if (vday_tar > 0) {

                target_pervisit = (mtd_obj / vday_tar);
            } else {

                par_value = 0;
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

            float SBDAchievePer = ((float) bmodel.getRetailerMasterBO()
                    .getSbdDistributionAchieve() / (float) bmodel
                    .getRetailerMasterBO().getSbdDistributionTarget()) * 100;

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
                        salestarget = callanalysismenu.get(i).getMandatory();

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
                    inittarget = callanalysismenu.get(i).getMandatory();

                    con.setKpiTarget(bmodel.retailerMasterBO.getInitiative_target() + "");
                    con.setKpiAchieved(bmodel.retailerMasterBO
                            .getInitiative_achieved() + "");

                } else if (callanalysismenu.get(i).getConfigCode()
                        .equalsIgnoreCase("CallA6")) {
                    float percent = ((bmodel.getRetailerMasterBO()
                            .getSBDMerchAchieved() == 0) ? 0
                            : ((bmodel.getRetailerMasterBO()
                            .getSBDMerchTarget() == 0) ? 0
                            : (((float) bmodel.getRetailerMasterBO()
                            .getSBDMerchAchieved() / bmodel
                            .getRetailerMasterBO()
                            .getSBDMerchTarget()) * 100)));

                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    con.setMenuNumber(bmodel.getRetailerMasterBO()
                            .getSBDMerchAchieved()
                            + "/"
                            + bmodel.getRetailerMasterBO().getSBDMerchTarget()
                            + " , "
                            + bmodel.formatPercent(SDUtil.convertToFloat(bmodel
                            .getRetailerMasterBO().getSbdMercPercent()))
                            + " %");
                    merchtgt = callanalysismenu.get(i).getMandatory();

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
                    con.setMenuNumber(bmodel.getRetailerMasterBO()
                            .getSbdDistributionAchieve()
                            + "/"
                            + bmodel.getRetailerMasterBO()
                            .getSbdDistributionTarget()
                            + " , "
                            + bmodel.formatPercent(SBDAchievePer) + "%");
                    disttgt = callanalysismenu.get(i).getMandatory();

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
                    disttgt = callanalysismenu.get(i).getMandatory();


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
                    int totalvalue = bmodel.getTotalLinesTarget();

                    int totalLines = bmodel.getTotalLines();
                    if (totalvalue > 0) {

                        double percentage = Utils
                                .round(((double) totalLines / (double) totalvalue) * 100,
                                        2);
                        if (percentage > 100)
                            percentage = 100;

                        con.setMenuNumber(totalLines + "/" + totalvalue + " , "
                                + percentage + "%");
                    } else {
                        con.setMenuNumber(totalLines + "/0 , 0%");
                    }

                    con.setKpiTarget(totalvalue + "");
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
                    String totalCaseVolme = "";
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

                    String totalCaseVolme = "";
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
                    final float totalWeight = bmodel.productHelper.getTotalWeight(bmodel.getRetailerMasterBO().getRetailerID());
                    con.setMenuNumber(totalWeight + "");
                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA30")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    final double salesReturnValue = SalesReturnHelper.getInstance(this).getSalesRetunTotalValue();
                    con.setMenuNumber(bmodel.formatValue(salesReturnValue) + "");

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA31")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    final double salesReturnValue = SalesReturnHelper.getInstance(this).getSalesReturnValue();

                    con.setMenuNumber(bmodel.formatValue(SalesReturnHelper.getInstance(this).getOrderValue() - salesReturnValue));

                } else if (callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA33")) {
                    con.setMenuName(callanalysismenu.get(i).getMenuName());
                    int totalFocusBrand = 0;
                    int totalFocusBrand1 = 0;
                    int totalFocusBrand2 = 0;
                    int totalFocusBrand3 = 0;
                    int totalFocusBrand4 = 0;
                    for (ProductMasterBO productBO : bmodel.productHelper.getProductMaster()) {
                        if (productBO.getIsFocusBrand() == 1) {
                            totalFocusBrand1 = 1;
                        }
                        if (productBO.getIsFocusBrand2() == 1) {
                            totalFocusBrand2 = 1;
                        }
                        if (productBO.getIsFocusBrand3() == 1) {
                            totalFocusBrand3 = 1;
                        }
                        if (productBO.getIsFocusBrand4() == 1) {
                            totalFocusBrand4 = 1;
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

                }

                if (!callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA36")
                        && !callanalysismenu.get(i).getConfigCode().equalsIgnoreCase("CallA37")) {

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

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void onBack(View v) {
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));
        resetRemarksBO();
        BusinessModel.loadActivity(CallAnalysisActivity.this,
                DataMembers.actHomeScreenTwo);
        finish();
    }

    public void onClose(View v) {
        hideKeyboard();
        try {
            if (bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL && !hasActivityDone()) {
                ReasonMaster reasonMaster = (ReasonMaster) spinnerFeedback.getSelectedItem();
                mFeedbackReasonId = reasonMaster.getReasonDesc();
                mFeedBackId = reasonMaster.getReasonID();

            }

            getMessage();
            ReasonMaster reason = (ReasonMaster) spinnerNoOrderReason
                    .getSelectedItem();
            mSelectedReasonId = reason.getReasonID();
            if ((hasOrderScreenEnabled() && (hasActivityDone() || bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON)
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
                showDialog(0);
            }
        } else
            showDialog(0);
    }

    private void showFeedbackReasonOrDialog() {
        ReasonMaster reasonMasterBO = (ReasonMaster) spinnerFeedback.getSelectedItem();
        if (reasonMasterBO.getReasonID().equals("0")) {
            Toast.makeText(CallAnalysisActivity.this, getResources().getString(R.string.select_feedback_reason), Toast.LENGTH_SHORT).show();
        } else {
            showCollectionReasonOrDialog();
        }
    }

    private Vector<ConfigureBO> menuDB = new Vector<ConfigureBO>();
    private Vector<ConfigureBO> mInStoreMenu = new Vector<>();

    private String getMessage() {
        StringBuilder sb = new StringBuilder();
        boolean isStoreCheckMenu = false;

        menuDB = bmodel.configurationMasterHelper.getActivityMenu();


        for (ConfigureBO config : menuDB) {
            if (config.getConfigCode().equals(ConfigurationMasterHelper.MENU_STORECHECK))
                isStoreCheckMenu = true;
            if (config.getHasLink() == 1 && !config.isDone()
                    && !config.getConfigCode().equals(MENU_CALL_ANLYS)) {

                sb.append(config.getMenuName() + " "
                        + getResources().getString(R.string.is_not_done) + "\n");
            }

        }

        if (isStoreCheckMenu) {
            mInStoreMenu = bmodel.configurationMasterHelper
                    .getStoreCheckMenu();
            for (ConfigureBO config : mInStoreMenu) {
                if (config.getHasLink() == 1 && !config.isDone()
                        && !config.getConfigCode().equals("MENU_CLOSE")) {

                    sb.append(config.getMenuName() + " "
                            + getResources().getString(R.string.is_not_done) + "\n");
                }

            }
        }

        return sb.toString();

    }

    /**
     * Check whether any activity is done on this call or not.
     *
     * @return
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
            DBUtil db = new DBUtil(CallAnalysisActivity.this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select stockid from "
                    + DataMembers.tbl_closingstockheader + " where retailerid="
                    + bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
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
     * @return
     */
    private boolean hasOrderScreenEnabled() {
        menuDB = bmodel.configurationMasterHelper.getActivityMenu();
        for (ConfigureBO configureBO : menuDB) {
            if ((configureBO.getConfigCode().equals("MENU_ORDER") ||
                    configureBO.getConfigCode().equals("MENU_STK_ORD") && configureBO.getHasLink() == 1)) {
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

    private boolean isMandatory() {
        menuDB = bmodel.configurationMasterHelper.getActivityMenu();

        for (ConfigureBO config : menuDB) {
            if (config.getMandatory() == 1 && !config.isDone())
                return true;
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

                                        if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
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

                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
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
                                                .updateTimeStampModuleWise(SDUtil
                                                        .now(SDUtil.TIME));
                                        BusinessModel.loadActivity(
                                                CallAnalysisActivity.this,
                                                DataMembers.actHomeScreenTwo);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;

        }
        return null;

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
            bmodel.updateIsVisitedFlag();
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
            bmodel.updateIsVisitedFlag();
            // Alert the user
            Toast.makeText(CallAnalysisActivity.this,
                    getResources().getString(R.string.reason_saved),
                    Toast.LENGTH_SHORT).show();
        } else if (!mFeedBackId.equals("0")) {
            bmodel.updateIsVisitedFlag();
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

        if (!hasActivityDone() && !bmodel.configurationMasterHelper.SHOW_FEEDBACK_IN_CLOSE_CALL && !bmodel.configurationMasterHelper.SHOW_NO_ORDER_REASON) {
            bmodel.outletTimeStampHelper.deleteTimeStampAllModule();
            bmodel.outletTimeStampHelper.deleteTimeStamp();
            bmodel.outletTimeStampHelper.deleteTimeStampImages();
        }
        resetRemarksBO();
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));

        if (bmodel.configurationMasterHelper.SHOW_NO_ORDER_EDITTEXT) {
            if (edt_noOrderReason.getText().length() > 0) {
                bmodel.outletTimeStampHelper.updateTimeStamp(SDUtil
                        .now(SDUtil.TIME), edt_noOrderReason.getText().toString());
            } else {
                showNoOrderReason();
            }
        } else {
            bmodel.outletTimeStampHelper.updateTimeStamp(SDUtil
                    .now(SDUtil.TIME), mFeedbackReasonId);
        }
        bmodel.saveModuleCompletion("MENU_CALL_ANLYS");
        bmodel.productHelper.clearProductHelper();
        BusinessModel.loadActivity(CallAnalysisActivity.this,
                DataMembers.actPlanning);
        finish();

    }

    private void resetRemarksBO() {
        bmodel.setOrderHeaderNote("");
        bmodel.setRField1("");
        bmodel.setRField2("");
        bmodel.setSaleReturnNote("");
        bmodel.setStockCheckRemark("");
        bmodel.setAssetRemark("");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
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

    private Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            bmodel = (BusinessModel) getApplicationContext();
            mTime.setText(msg.obj + "");
        }
    };


    /**
     * Update Isgoldstore in RetaierMaster db and Retailer bo
     */
    private void updateGoldenStore(int flag) {
        try {
            DBUtil db = new DBUtil(CallAnalysisActivity.this,
                    DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.executeQ("update RetailerMaster set IsGoldStore=" + flag
                    + "  where RetailerID="
                    + bmodel.getRetailerMasterBO().getRetailerID());
            db.closeDB();
            bmodel.getRetailerMasterBO().setIsGoldStore(flag);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void loadCollectionReason() {
        try {
            ReasonMaster reason;
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            DBUtil db = new DBUtil(this, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            resetRemarksBO();
            BusinessModel.loadActivity(CallAnalysisActivity.this,
                    DataMembers.actHomeScreenTwo);
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
            if (reason.getReasonID().equals("0")) {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.select_no_order_reason),
                        Toast.LENGTH_LONG).show();
                return;
            }
            if (bmodel.isExternalStorageAvailable()) {

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
                                HomeScreenFragment.photoPath, 1,
                                fnameStarts);
                if (nFilesThere) {
                    showFileDeleteAlert(fnameStarts);
                } else {
                    Intent intent = new Intent(this,
                            CameraActivity.class);
                    intent.putExtra("quality", 40);
                    String path = HomeScreenFragment.photoPath + "/"
                            + mImageName;
                    intent.putExtra("path", path);
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

                        bmodel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        isPhotoTaken = false;
                        Intent intent = new Intent(CallAnalysisActivity.this,
                                CameraActivity.class);
                        intent.putExtra("quality", 40);
                        String path = HomeScreenFragment.photoPath + "/" + mImageName;
                        intent.putExtra("path", path);
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
        } else {
            Commons.print(bmodel.mSelectedActivityName
                    + "Camers Activity : Canceled");
            isPhotoTaken = false;
        }
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}
