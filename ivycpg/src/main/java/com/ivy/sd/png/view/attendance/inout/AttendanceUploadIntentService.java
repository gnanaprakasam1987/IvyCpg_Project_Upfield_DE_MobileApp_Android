package com.ivy.sd.png.view.attendance.inout;


import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ivy.sd.png.model.BusinessModel;

public class AttendanceUploadIntentService extends IntentService {

    public AttendanceUploadIntentService(){
        super(AttendanceUploadIntentService.class.getSimpleName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        String attendance = "";
        if(intent!=null && intent.getExtras()!=null)
            attendance = intent.getStringExtra("Attendance");

        BusinessModel businessModel = (BusinessModel)getApplicationContext();
        businessModel.updateAttendanceTime(getApplicationContext(),attendance);
    }
}
