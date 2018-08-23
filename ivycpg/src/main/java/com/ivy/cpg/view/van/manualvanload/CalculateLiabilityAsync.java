package com.ivy.cpg.view.van.manualvanload;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.view.View;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

/**
 * Created by Hanifa on 21/8/18.
 */

public class CalculateLiabilityAsync {
    private Context mContext;

    private int selectedSubDepotId;

    public CalculateLiabilityAsync(Context context, BusinessModel businessModel, int selectedSubDepotId) {
        this.mContext = context;

        this.selectedSubDepotId = selectedSubDepotId;

        new CalculateLiability().execute();
    }


    class CalculateLiability extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;


        protected void onPreExecute() {
            builder = new AlertDialog.Builder(mContext);

            customProgressDialog(builder, mContext.getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
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
                new ProductSummaryDialog(mContext, selectedSubDepotId);
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        }

    }


    private void customProgressDialog(AlertDialog.Builder builder, String message) {

        try {
            View view = View.inflate(mContext, R.layout.custom_alert_dialog, null);

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            TextView messagetv = (TextView) view.findViewById(R.id.text);
            messagetv.setText(message);

            builder.setView(view);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }


}
