package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.os.Parcelable;

import com.ivy.ivyretail.service.LocationDetailBO;

import java.io.Serializable;

public interface RealTimeLocation extends Serializable {
    void onRealTimeLocationReceived(LocationDetailBO locationDetailBO, Context context);
}
