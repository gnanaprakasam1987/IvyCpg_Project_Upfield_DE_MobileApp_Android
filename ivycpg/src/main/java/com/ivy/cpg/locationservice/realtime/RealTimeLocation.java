package com.ivy.cpg.locationservice.realtime;

import android.content.Context;

import com.ivy.cpg.locationservice.LocationDetailBO;

import java.io.Serializable;

public interface RealTimeLocation extends Serializable {
    void onRealTimeLocationReceived(LocationDetailBO locationDetailBO, Context context);
}
