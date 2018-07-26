package com.ivy.cpg.view.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;

/**
 * Created by abbas.a on 25/07/18.
 * auto
 */

public class LastSyncTimeHelper {

    private SharedPreferences mLastUploadAndDownloadPref;

    public LastSyncTimeHelper(Context context){
        Context mContext = context;
        mLastUploadAndDownloadPref = mContext.getSharedPreferences("lastUploadAndDownload", Context.MODE_PRIVATE);
    }

    public void updateUploadedTime() {
        try {
            SharedPreferences.Editor edt = mLastUploadAndDownloadPref.edit();
            edt.putString("uploadDate",
                    SDUtil.now(SDUtil.DATE_GLOBAL));
            edt.putString("uploadTime", SDUtil.now(SDUtil.TIME));
            edt.apply();


        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    public void updateDownloadTime() {
        try {
            SharedPreferences.Editor edt = mLastUploadAndDownloadPref.edit();
            edt.putString("downloadDate",
                    SDUtil.now(SDUtil.DATE_GLOBAL));
            edt.putString("downloadTime", SDUtil.now(SDUtil.TIME));
            edt.apply();

        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }



    public String getLastDownloadTime(){
        return mLastUploadAndDownloadPref.getString("downloadTime", "");
    }

    public String getLastUploadTime(){
        return mLastUploadAndDownloadPref.getString("uploadTime", "");
    }

    public String getLastDownloadDate(){
        return mLastUploadAndDownloadPref.getString("downloadDate", "");
    }

    public String getLastUplaodDate(){
        return mLastUploadAndDownloadPref.getString("uploadDate", "");
    }

}
