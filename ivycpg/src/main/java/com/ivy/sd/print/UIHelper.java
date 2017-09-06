/**
 * *******************************************
 * CONFIDENTIAL AND PROPRIETARY
 * <p/>
 * The source code and other information contained herein is the confidential and the exclusive property of
 * ZIH Corp. and is subject to the terms and conditions in your end user license agreement.
 * This source code, and any other information contained herein, shall not be copied, reproduced, published,
 * displayed or distributed, in whole or in part, in any medium, by any means, for any purpose except as
 * expressly permitted under such license agreement.
 * <p/>
 * Copyright ZIH Corp. 2010
 * <p/>
 * ALL RIGHTS RESERVED
 * *********************************************
 */
package com.ivy.sd.print;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;

import com.ivy.sd.png.model.BusinessModel;

public class UIHelper {
    ProgressDialog loadingDialog;
    Activity activity;
    BusinessModel bmodel;

    public UIHelper(Activity activity) {
        this.bmodel = (BusinessModel) activity.getApplicationContext();
        this.activity = activity;
    }

    public void showLoadingDialog(final String message) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    loadingDialog = ProgressDialog.show(activity, "", message, true, false);
                }
            });
        }
    }

    public void updateLoadingDialog(final String message) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    loadingDialog.setMessage(message);
                }
            });
        }
    }

    public boolean isDialogActive() {
        if (loadingDialog != null) {
            return loadingDialog.isShowing();
        } else {
            return false;
        }
    }

    public void dismissLoadingDialog() {
        if (activity != null && loadingDialog != null) {
            loadingDialog.dismiss();
        }
    }

    public void showErrorDialog(String errorMessage) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity).setMessage(errorMessage).setTitle("Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }

    public void showErrorDialogOnGuiThread(final String errorMessage) {
        if (activity != null) {
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    AlertDialog.Builder builder = new AlertDialog.Builder(activity).setMessage(errorMessage).setTitle("Error").setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.dismiss();
                            dismissLoadingDialog();
                        }
                    });
                }
            });
        }
    }

}
