package com.ivy.cpg.view.van.vanunload;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.provider.ModuleTimeStampHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class SaveVanUnloadAsyncTask extends AsyncTask<Integer, Integer, Boolean> {
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    private Context mContext;
    private Vector<LoadManagementBO> mVanUnloadList;


    public SaveVanUnloadAsyncTask(Context context, Vector<LoadManagementBO> vanunloadlist) {

        this.mContext = context;
        this.mVanUnloadList = vanunloadlist;
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
            VanUnLoadModuleHelper mVanUnLoadModuleHelper = VanUnLoadModuleHelper.getInstance(mContext.getApplicationContext());
            mVanUnLoadModuleHelper.saveVanUnLoad(mVanUnloadList, mContext.getApplicationContext());
            mVanUnLoadModuleHelper.UpdateSIH(mVanUnloadList, mContext.getApplicationContext());
            // If unloading empty
            mVanUnLoadModuleHelper.updateEmptyReconilationTable(mVanUnloadList, mContext.getApplicationContext());

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

            ((VanUnloadActivity) mContext).finish();
            ((VanUnloadActivity) mContext).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

}
