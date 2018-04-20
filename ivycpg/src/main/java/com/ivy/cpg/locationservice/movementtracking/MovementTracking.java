package com.ivy.cpg.locationservice.movementtracking;

import android.content.Context;
import android.location.Location;

import java.io.Serializable;

public interface MovementTracking extends Serializable{
    void uploadLocationDetails(Context context, Location location);

}
