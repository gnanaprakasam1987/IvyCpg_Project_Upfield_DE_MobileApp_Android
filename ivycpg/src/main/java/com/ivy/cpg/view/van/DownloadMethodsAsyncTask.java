package com.ivy.cpg.view.van;

import android.app.AlertDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

/**
 * Created by Hanifa on 20/8/18.
 */

class DownloadMethodsAsyncTask extends AsyncTask<Integer, Integer, Boolean>  {

    private Context mContext;
    private DownloadAsyncTaskInterface downloadAsyncTaskInterface;
    private String menuCode, menuName;

    public DownloadMethodsAsyncTask(Context context, DownloadAsyncTaskInterface downloadAsyncTaskInterface, String menuCode, String menuName) {
        this.mContext = context;
        this.downloadAsyncTaskInterface = downloadAsyncTaskInterface;
        this.menuCode = menuCode;
        this.menuName = menuName;
    }

    protected void onPreExecute() {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        downloadAsyncTaskInterface.showProgress(builder);
    }

    @Override
    protected Boolean doInBackground(Integer... integers) {
        try {
            downloadAsyncTaskInterface.loadMethods(menuCode, menuName);
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
            downloadAsyncTaskInterface.intentCall(menuCode, menuName);
        } else {
            downloadAsyncTaskInterface.hideProgress();
        }
    }
}

