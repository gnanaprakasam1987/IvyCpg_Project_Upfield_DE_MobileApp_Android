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
class LoadReturnProductDialogAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
    private Context mContext;
    private ReturnProDialogNumPress returnProDialogNumPress;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;


    public LoadReturnProductDialogAsyncTask(Context context, ReturnProDialogNumPress returnProDialogNumPress) {
        this.mContext = context;
        this.returnProDialogNumPress = returnProDialogNumPress;
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
            VanLoadReturnProductDialog returnProductDialog = new VanLoadReturnProductDialog(
                    mContext, ((ManualVanLoadActivity) mContext));
            returnProductDialog.show();
            returnProductDialog.setCancelable(false);
            returnProDialogNumPress.dialogNumPress(returnProductDialog);
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }
}
