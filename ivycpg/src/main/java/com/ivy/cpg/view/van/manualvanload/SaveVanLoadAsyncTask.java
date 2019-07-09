package com.ivy.cpg.view.van.manualvanload;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ModuleTimeStampHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;

/**
 * Created by Hanifa on 21/8/18.
 */
 class SaveVanLoadAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
    private Context mContext;
    private int selectedSubDepotId;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    BusinessModel businessModel;

    public SaveVanLoadAsyncTask(Context context, int selectedSubDepotId) {
        this.mContext = context;
        this.selectedSubDepotId = selectedSubDepotId;
        businessModel=(BusinessModel)context.getApplicationContext();

    }

    protected void onPreExecute() {
        builder = new AlertDialog.Builder(mContext);

        ((IvyBaseActivityNoActionBar) mContext).customProgressDialog(builder, mContext.getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();

    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {
            ManualVanLoadHelper manualVanLoadHelper = ManualVanLoadHelper.getInstance(mContext.getApplicationContext());
            manualVanLoadHelper.saveVanLoad(ProductHelper.getInstance(mContext.getApplicationContext()).getLoadMgmtProducts(), selectedSubDepotId);
            // Clear the Values from the Objects after save in DB
            if (ConfigurationMasterHelper.getInstance(mContext.getApplicationContext()).SHOW_PRODUCTRETURN) {
                manualVanLoadHelper.clearBomReturnProductsTable();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE; // Return your real result here
    }

    protected void onProgressUpdate(Integer... progress) {

    }

    protected void onPostExecute(Boolean result) {
        // result is the value returned from doInBackground
        try {

            alertDialog.dismiss();
            Toast.makeText(mContext,
                    mContext.getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
            businessModel.saveModuleCompletion("MENU_MANUAL_VAN_LOAD", false);
            ModuleTimeStampHelper moduleTimeStampHelper = ModuleTimeStampHelper.getInstance(mContext.getApplicationContext());
            moduleTimeStampHelper.saveModuleTimeStamp("Out");
            moduleTimeStampHelper.setTid("");
            moduleTimeStampHelper.setModuleCode("");
            ((AppCompatActivity) mContext).finish();
            ((AppCompatActivity) mContext).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }
}
