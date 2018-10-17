package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.os.Parcelable;

import com.ivy.cpg.locationservice.LocationDetailBO;

import java.io.Serializable;

public interface RealTimeLocation extends Serializable,Parcelable {
    void onRealTimeLocationReceived(LocationDetailBO locationDetailBO, Context context);

    void updateAttendanceIn(Context context, String pathNode);

    void updateAttendanceOut(Context context, String pathNode);

    void validateLoginAndUpdate(final Context context, final String pathNode, final LocationDetailBO locationDetailBO, final String from);
}
