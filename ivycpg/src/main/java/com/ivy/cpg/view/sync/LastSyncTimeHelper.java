package com.ivy.cpg.view.sync;

import android.content.Context;
import android.content.SharedPreferences;

import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

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
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            edt.putString("uploadTime", DateTimeUtils.now(DateTimeUtils.TIME));
            edt.apply();


        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    public void updateDownloadTime() {
        try {
            SharedPreferences.Editor edt = mLastUploadAndDownloadPref.edit();
            edt.putString("downloadDate",
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
            edt.putString("downloadTime", DateTimeUtils.now(DateTimeUtils.TIME));
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
