package com.ivy.cpg.view.van.manualvanload;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import static com.ivy.cpg.view.van.manualvanload.ManualVanLoadActivity.VAN_RETURN_PRODUCT_RESULT_CODE;

/**
 * Created by Hanifa on 21/8/18.
 */
class LoadReturnProductDialogAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
    private Context mContext;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;


    public LoadReturnProductDialogAsyncTask(Context context) {
        this.mContext = context;
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
            BusinessModel bModel = (BusinessModel) mContext.getApplicationContext();
            ManualVanLoadHelper.getInstance(mContext.getApplicationContext()).setReturnQty();
            bModel.productHelper.calculateOrderReturnValue();
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
            Intent intent = new Intent(mContext,VanLoadReturnProductActivity.class);
            ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(mContext, R.anim.zoom_enter, R.anim.hold);
            ActivityCompat.startActivityForResult((Activity) mContext, intent, VAN_RETURN_PRODUCT_RESULT_CODE, opts.toBundle());

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }
}
