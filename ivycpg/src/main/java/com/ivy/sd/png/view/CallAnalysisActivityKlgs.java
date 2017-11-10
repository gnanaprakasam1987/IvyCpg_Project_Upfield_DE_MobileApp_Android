
package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;

import java.util.Vector;

public class CallAnalysisActivityKlgs extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private TextView mTime;
    public static final String MENU_CALL_ANLYS = "MENU_CALL_ANALYS_KELGS";
    LinearLayout ll_content;
    private Toolbar toolbar;
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        ll_content = (LinearLayout) findViewById(R.id.ll_content);
        taskDataBO = bmodel.taskHelper.getPendingTaskData();

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
        createView();

        /** set handler for the Timer class */
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

            tv_duration = (TextView) findViewById(R.id.tv_duration);
            tv_duration.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            tv_edt_time_taken = (TextView) findViewById(R.id.edt_time_taken);
            tv_edt_time_taken.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

            btn_close = (Button) findViewById(R.id.button1);
            btn_close.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        } catch (Exception e) {
            Commons.printException(e);

        }
    }

    private void createView() {

        try {
            if (taskDataBO != null) {
                LayoutInflater inflater = LayoutInflater.from(this);

                View cardView = null;


                for (TaskDataBO taskBo : taskDataBO) {
                    cardView = inflater.inflate(R.layout.task_child_view, null);
                    TextView tv_taskDesc = (TextView) cardView.findViewById(R.id.tv_task_desc);
                    tv_taskDesc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    tv_taskDesc.setText(taskBo.getTaskDesc());
                    TextView tv_taskOwner = (TextView) cardView.findViewById(R.id.tv_task_owner);
                    tv_taskOwner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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

    public void onBack(View v) {
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));
        resetRemarksBO();
        BusinessModel.loadActivity(CallAnalysisActivityKlgs.this,
                DataMembers.actHomeScreenTwo);
        finish();
    }

    public void onClose(View v) {

        try {
            getMessage();
            showDialog(0);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
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
                                        doCallAnalysisCloseAction("0", "");

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
                                                .updateTimeStampModuleWise(SDUtil
                                                        .now(SDUtil.TIME));
                                        BusinessModel.loadActivity(
                                                CallAnalysisActivityKlgs.this,
                                                DataMembers.actHomeScreenTwo);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;

        }
        return null;

    }


    private void doCallAnalysisCloseAction(String collectionReasonID,
                                           String collectionReasonType) {
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
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));

        bmodel.outletTimeStampHelper.updateTimeStamp(SDUtil
                .now(SDUtil.TIME), "");
        bmodel.saveModuleCompletion("MENU_CALL_ANALYS_KELGS");
        bmodel.productHelper.clearProductHelper();
        BusinessModel.loadActivity(CallAnalysisActivityKlgs.this,
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


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            resetRemarksBO();
            BusinessModel.loadActivity(CallAnalysisActivityKlgs.this,
                    DataMembers.actHomeScreenTwo);
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return false;
    }


}
