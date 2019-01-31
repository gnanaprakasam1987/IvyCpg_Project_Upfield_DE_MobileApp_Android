package com.ivy.cpg.view.van.manualvanload;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * Created by Hanifa on 21/8/18.
 */
class CalculateLiabilityAsyncTask extends AsyncTask<Integer, Integer, Boolean> {

    private Context mContext;
    private int selectedSubDepotId;
    private AlertDialog alertDialog;

    public CalculateLiabilityAsyncTask(Context context, int selectedSubDepotId) {
        this.mContext = context;
        this.selectedSubDepotId = selectedSubDepotId;
    }

    protected void onPreExecute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);

        ((IvyBaseActivityNoActionBar) mContext).customProgressDialog(builder, mContext.getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {
            BusinessModel bModel = (BusinessModel) mContext.getApplicationContext();
            ManualVanLoadHelper.getInstance(mContext.getApplicationContext()).setReturnQty();
            bModel.productHelper.calculateOrderReturnValue();
            if (bModel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                bModel.productHelper.setGroupWiseReturnQty();
        } catch (Exception e) {
            Commons.printException("" + e);
            return Boolean.FALSE;
        }
        return Boolean.TRUE; // Return your real result here
    }


    protected void onPostExecute(Boolean result) {
        // result is the value returned from doInBackground
        try {

            alertDialog.dismiss();
            new ProductSummaryDialog(mContext, selectedSubDepotId).show();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }
}
