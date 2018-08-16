package com.ivy.cpg.fcmnotification;

import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationJobService  extends JobService {

    private static final String TAG = "MyJobService";

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Performing long running task in scheduled job");




        return false;
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
