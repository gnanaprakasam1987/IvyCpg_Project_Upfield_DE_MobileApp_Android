package com.ivy.cpg.view.sync;

import android.app.Activity;
import android.os.Handler;

import com.ivy.cpg.view.callanalysis.CallAnalysisActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.settings.UserSettingsActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.ReAllocationActivity;

public class UploadThread extends Thread {

    private Activity ctx;
    private int uploadType;
    private Handler handler;
    private boolean isFromCallAnalysis;


    public static final int UPLOAD_FILE_IN_AMAZON = 5959;
    public static final int AMAZONIMAGE_UPLOAD = 501;
    public static final int AZURE_IMAGE_UPLOAD = 999;
    public static final int SYNC_UPLOAD = 5;
    public static final int SYNC_SIH_UPLOAD = -30;
    public static final int SYNC_SEQ_NUMBER_UPLOAD = -31;

    public static final int SYNC_STK_APPLY_UPLOAD = -33;
    public static final int SYNC_LYTY_PT_UPLOAD = -40;
    public static final int SYNC_REALLOC_UPLOAD = -36;
    public static final int SYNC_PICK_LIST_UPLOAD = -50;
    public static final int SYNC_TRIP = -80;
    public static final int SYNC_ORDER_DELIVERY_STATUS_UPLOAD = -47;
    public static final int ATTENDANCE_UPLOAD = 101;


    public UploadThread(Activity ctx, int uploadType) {
        this.ctx = ctx;
        this.uploadType = uploadType;
    }

    public UploadThread(Activity ctx, int uploadType, boolean isFromCallAnalysis) {
        this.ctx = ctx;
        this.uploadType = uploadType;
        this.isFromCallAnalysis = isFromCallAnalysis;
    }

    public UploadThread(Activity ctx, int uploadType, Handler handler) {
        this.ctx = ctx;
        this.handler = handler;
        this.uploadType = uploadType;

    }

    public void run() {
        BusinessModel bmodel = (BusinessModel) ctx.getApplicationContext();
        bmodel.setContext(ctx);

        if (uploadType == SYNC_UPLOAD) {

            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);

            Handler handler;

            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {

                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                UploadHelper.UPLOAD_STATUS status = mUploadHelper.uploadTransactionDataByType(handler, UploadThread.SYNC_UPLOAD, ctx.getApplicationContext());

                if (BuildConfig.FLAVOR.equalsIgnoreCase("aws")) {
                    sendMessage(status, handler, false);
                }
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (uploadType == SYNC_REALLOC_UPLOAD) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            ReAllocationActivity frm = (ReAllocationActivity) ctx;

            if (bmodel.isOnline()) {
                UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);
                UploadHelper.UPLOAD_STATUS status = mUploadHelper.uploadTransactionDataByType(frm.getHandler(), UploadThread.SYNC_REALLOC_UPLOAD, ctx.getApplicationContext());
                sendMessage(status, frm.getHandler(), false);
            } else {
                frm.getHandler().sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }
        } else if (uploadType == SYNC_ORDER_DELIVERY_STATUS_UPLOAD || uploadType == SYNC_TRIP ||
                uploadType == SYNC_PICK_LIST_UPLOAD || uploadType == SYNC_LYTY_PT_UPLOAD ||
                uploadType == SYNC_SEQ_NUMBER_UPLOAD || uploadType == SYNC_STK_APPLY_UPLOAD ||
                uploadType == SYNC_SIH_UPLOAD ) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            UploadHelper mUploadHelper = UploadHelper.getInstance(ctx);

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            if (bmodel.isOnline()) {
                UploadHelper.UPLOAD_STATUS status = mUploadHelper.uploadTransactionDataByType(handler, uploadType, ctx.getApplicationContext());
                if (BuildConfig.FLAVOR.equalsIgnoreCase("aws"))
                sendMessage(status, handler, true);
            } else {
                handler.sendEmptyMessage(
                        DataMembers.NOTIFY_CONNECTION_PROBLEM);
            }

        } else if (uploadType == AMAZONIMAGE_UPLOAD) {

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.uploadImageToAmazonCloud(handler);

        } else if (uploadType == AZURE_IMAGE_UPLOAD) {

            Handler handler;
            if (isFromCallAnalysis) {
                CallAnalysisActivity fragment = (CallAnalysisActivity) ctx;
                handler = fragment.getHandler();
            } else {
                HomeScreenActivity fragment = (HomeScreenActivity) ctx;
                handler = fragment.getHandler();
            }

            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            bmodel.uploadImageToAzureCloud(handler);

        } else if (uploadType == UPLOAD_FILE_IN_AMAZON) {
            bmodel = (BusinessModel) ctx.getApplicationContext();
            bmodel.setContext(ctx);
            UserSettingsActivity frm = (UserSettingsActivity) ctx;

            bmodel.uploadFileInAmazon(frm.getHandler());
        }
    }

    private void sendMessage(UploadHelper.UPLOAD_STATUS uploadStatus, Handler handler, boolean continueOnSuccess) {

        if (uploadStatus == UploadHelper.UPLOAD_STATUS.SUCCESS && continueOnSuccess) {

            handler.sendEmptyMessage(
                    DataMembers.NOTIFY_UPLOADED_CONTINUE);
        } else if (uploadStatus == UploadHelper.UPLOAD_STATUS.SUCCESS) {

            handler.sendEmptyMessage(
                    DataMembers.NOTIFY_UPLOADED);
        } else if (uploadStatus == UploadHelper.UPLOAD_STATUS.TOKEN_ERROR) {
            handler.sendEmptyMessage(
                    DataMembers.NOTIFY_TOKENT_AUTHENTICATION_FAIL);
        } else if (uploadStatus == UploadHelper.UPLOAD_STATUS.URL_NOTFOUND) {
            handler.sendEmptyMessage(
                    DataMembers.NOTIFY_URL_NOT_CONFIGURED);
        } else {
            handler.sendEmptyMessage(
                    DataMembers.NOTIFY_UPLOAD_ERROR);
        }
    }
}

