package com.ivy.cpg.view.van.vanunload;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ModuleTimeStampHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.CommonPrintPreviewActivity;

import java.util.Vector;

public class SaveVanUnloadAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private Context mContext;
    private Vector<LoadManagementBO> mVanUnloadList;
    private BusinessModel businessModel;
    private VanUnLoadModuleHelper mVanUnLoadModuleHelper;


    public SaveVanUnloadAsyncTask(Context context, Vector<LoadManagementBO> vanunloadlist, BusinessModel businessModel) {

        this.mContext = context;
        this.mVanUnloadList = vanunloadlist;
        this.businessModel = businessModel;
        this.mVanUnLoadModuleHelper = VanUnLoadModuleHelper.getInstance(mContext.getApplicationContext());
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        builder = new AlertDialog.Builder(mContext);

        ((IvyBaseActivityNoActionBar) mContext).customProgressDialog(builder, mContext.getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {

            mVanUnLoadModuleHelper.saveVanUnLoad(mVanUnloadList, mContext.getApplicationContext());
            mVanUnLoadModuleHelper.UpdateSIH(mVanUnloadList, mContext.getApplicationContext());

            // Save non salable sih stock to Vanunloaddetails table
            if (businessModel.configurationMasterHelper.SHOW_NON_SALABLE_UNLOAD) {
                mVanUnLoadModuleHelper.saveVanUnloadNonsalable(mVanUnloadList, mContext);
                mVanUnLoadModuleHelper.UpdateNonSalableSIH(mVanUnloadList, mContext);
            }
            // If unloading empty
            mVanUnLoadModuleHelper.updateEmptyReconilationTable(mVanUnloadList, mContext.getApplicationContext());

            if (businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                    || businessModel.configurationMasterHelper.COMMON_PRINT_LOGON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_INTERMEC
                    || businessModel.configurationMasterHelper.COMMON_PRINT_MAESTROS) {
                mVanUnLoadModuleHelper.getVanUnloadDetailsForPrint(mVanUnLoadModuleHelper.getTransactionId(), mContext);
            }

            businessModel.saveModuleCompletion("MENU_VAN_UNLOAD", false);

        } catch (Exception e) {
            Commons.printException("" + e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }


    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
        // result is the value returned from doInBackground
        try {

            alertDialog.dismiss();

            ((IvyBaseActivityNoActionBar) mContext).showMessage(mContext.getString(R.string.saved_successfully));

            ModuleTimeStampHelper moduleTimeStampHelper = ModuleTimeStampHelper.getInstance(mContext.getApplicationContext());
            moduleTimeStampHelper.saveModuleTimeStamp("Out");
            moduleTimeStampHelper.setTid("");
            moduleTimeStampHelper.setModuleCode("");

            if (businessModel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || businessModel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                    || businessModel.configurationMasterHelper.COMMON_PRINT_LOGON
                    || businessModel.configurationMasterHelper.COMMON_PRINT_INTERMEC
                    || businessModel.configurationMasterHelper.COMMON_PRINT_MAESTROS) {
                businessModel.mCommonPrintHelper.xmlRead("van", false, null, null, null, null,mVanUnLoadModuleHelper.getReasonList());

                businessModel.writeToFile(String.valueOf(businessModel.mCommonPrintHelper.getInvoiceData()),
                        StandardListMasterConstants.PRINT_FILE_UNLOAD + mVanUnLoadModuleHelper.getTransactionId(), "/" + DataMembers.PRINT_FILE_PATH, "");

                Intent intent = new Intent(((VanUnloadActivity) mContext),
                        CommonPrintPreviewActivity.class);
                intent.putExtra("isHomeBtnEnable", true);
                intent.putExtra("isFromVanUnload", true);
                mContext.startActivity(intent);
            }

            ((VanUnloadActivity) mContext).finish();
            ((VanUnloadActivity) mContext).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

}
