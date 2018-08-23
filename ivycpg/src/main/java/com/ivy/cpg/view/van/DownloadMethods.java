package com.ivy.cpg.view.van;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

/**
 * Created by Hanifa on 20/8/18.
 */

public class DownloadMethods {

    private Context mContext;
    private DownloadInterface downloadInterface;
    private String menuCode, menuName;

    public DownloadMethods(Context context, DownloadInterface downloadInterface, String menuCode, String menuName) {
        this.mContext = context;
        this.downloadInterface = downloadInterface;
        this.menuCode = menuCode;
        this.menuName = menuName;

        new DownloadLoadAsync().execute();
    }


    private class DownloadLoadAsync extends AsyncTask<Integer, Integer, Boolean> {
        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            downloadInterface.showProgress(builder, mContext.getResources().getString(R.string.loading));
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                downloadInterface.loadMethods(menuCode, menuName);
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {
            // TO DO Auto-generated method stub

        }

        protected void onPostExecute(Boolean result) {
            if (result) {
                downloadInterface.intentCall(menuCode, menuName);
            } else {
                downloadInterface.hideProgress();
            }

        }

    }

}

