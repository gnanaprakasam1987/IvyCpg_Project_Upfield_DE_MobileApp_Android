
package com.ivy.cpg.view.callanalysis;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.task.TaskHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.Vector;

public class CallAnalysisActivityKlgs extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private TextView mTime;
    public static final String MENU_CALL_ANLYS = "MENU_CALL_ANALYS_KELGS";
    LinearLayout ll_content;
    TextView tv_duration, tv_edt_time_taken;
    private Vector<TaskDataBO> taskDataBO;
    Button btn_close;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_call_analysis_klgs);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        Toolbar toolbar = findViewById(R.id.toolbar);
        ll_content = findViewById(R.id.ll_content);
        taskDataBO = TaskHelper.getInstance(this).getPendingTaskData();

        /* Handling session out */
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

        mTime = findViewById(R.id.edt_time_taken);
        createView();

        /* set handler for the Timer class */
        if (bmodel.timer != null) {
            bmodel.timer.setHandler(handler);
        }

        try {


            if (bmodel.getRetailerMasterBO().getRetailerID() == null) {
                Toast.makeText(
                        this,
                        getResources()
                                .getString(R.string.sessionout_loginagain),
                        Toast.LENGTH_SHORT).show();
                finish();
            }

            tv_duration = findViewById(R.id.tv_duration);
            tv_duration.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));

            tv_edt_time_taken = findViewById(R.id.edt_time_taken);
            tv_edt_time_taken.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.THIN));

            btn_close = findViewById(R.id.button1);
            btn_close.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));

        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    private void createView() {

        try {
            if (taskDataBO != null) {
                LayoutInflater inflater = LayoutInflater.from(this);

                View cardView;


                for (TaskDataBO taskBo : taskDataBO) {
                    cardView = inflater.inflate(R.layout.task_child_view, null);
                    TextView tv_taskDesc = cardView.findViewById(R.id.tv_task_desc);
                    tv_taskDesc.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                    tv_taskDesc.setText(taskBo.getTaskDesc());
                    TextView tv_taskOwner = cardView.findViewById(R.id.tv_task_owner);
                    tv_taskOwner.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
                    tv_taskOwner.setText(taskBo.getTaskOwner());
                    ll_content.addView(cardView);
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

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

    public void onClose(View v) {

        try {
            getMessage();
            showDialog(0);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }
    }


    private Vector<ConfigureBO> menuDB = new Vector<>();

    private String getMessage() {
        Vector<ConfigureBO> mInStoreMenu;
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
     * @return boolean
     */
    private boolean hasActivityDone() {
        try {
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
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }


    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(CallAnalysisActivityKlgs.this)
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
                                        doCallAnalysisCloseAction();

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
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CallAnalysisActivityKlgs.this)
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
                                     /*   BusinessModel.loadActivity(
                                                CallAnalysisActivityKlgs.this,
                                                DataMembers.actHomeScreenTwo);*/

                                        Intent myIntent = new Intent(CallAnalysisActivityKlgs.this, HomeScreenTwo.class);
                                        startActivityForResult(myIntent, 0);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;

        }
        return null;

    }


    private void doCallAnalysisCloseAction() {
        // stop timer
        if (bmodel.timer != null) {
            bmodel.timer.stopTimer();
            bmodel.timer = null;
        }

        if (!hasActivityDone()) {
            bmodel.outletTimeStampHelper.deleteTimeStampAllModule();
            bmodel.outletTimeStampHelper.deleteTimeStamp();
            bmodel.outletTimeStampHelper.deleteTimeStampImages();
        }
        resetRemarksBO();
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                .now(DateTimeUtils.TIME));

        bmodel.outletTimeStampHelper.updateTimeStamp(DateTimeUtils
                .now(DateTimeUtils.TIME), "");
        bmodel.saveModuleCompletion("MENU_CALL_ANALYS_KELGS", true);
        bmodel.productHelper.clearProductHelper();
//        BusinessModel.loadActivity(CallAnalysisActivityKlgs.this,
//                DataMembers.actPlanning);
        finish();

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
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view unbind view
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

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            bmodel = (BusinessModel) getApplicationContext();
            mTime.setText(msg.obj.toString());
            return true;
        }
    });


    /**
     * Update Isgoldstore in RetaierMaster db and Retailer bo
     */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            resetRemarksBO();
           /* BusinessModel.loadActivity(CallAnalysisActivityKlgs.this,
                    DataMembers.actHomeScreenTwo);*/

            Intent myIntent = new Intent(CallAnalysisActivityKlgs.this, HomeScreenTwo.class);
            startActivityForResult(myIntent, 0);
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return false;
    }


}
