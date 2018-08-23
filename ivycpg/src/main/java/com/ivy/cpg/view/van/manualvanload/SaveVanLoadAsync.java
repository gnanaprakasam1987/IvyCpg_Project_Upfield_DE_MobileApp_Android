package com.ivy.cpg.view.van.manualvanload;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ModuleTimeStampHelper;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

/**
 * Created by Hanifa on 21/8/18.
 */

public class SaveVanLoadAsync {
    private Context mContext;
    private int selectedSubDepotId;


    public SaveVanLoadAsync(Context context, int selectedSubDepotId) {
        this.mContext = context;
        this.selectedSubDepotId = selectedSubDepotId;

        new SaveVanLoad().execute();
    }

    class SaveVanLoad extends AsyncTask<Integer, Integer, Boolean> {

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
