package com.ivy.cpg.locationservice.activitytracking;


import android.app.IntentService;
import android.content.Intent;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.sd.png.util.Commons;

import java.util.List;

import static com.ivy.cpg.locationservice.LocationConstants.IN_VEHICLE;
import static com.ivy.cpg.locationservice.LocationConstants.ON_FOOT;
import static com.ivy.cpg.locationservice.LocationConstants.RUNNING;
import static com.ivy.cpg.locationservice.LocationConstants.STILL;
import static com.ivy.cpg.locationservice.LocationConstants.TILTING;
import static com.ivy.cpg.locationservice.LocationConstants.UNKNOWN;
import static com.ivy.cpg.locationservice.LocationConstants.WALKING;

public class ActivityIntentService extends IntentService {
    protected static final String TAG = "Activity";

    public ActivityIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {

//      Check whether the Intent contains activity recognition data//
        if (ActivityRecognitionResult.hasResult(intent)) {

            // Get the update
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            // Get the most probable activity from the list of activities in the update
            DetectedActivity mostProbableActivity = result.getMostProbableActivity();

            // Get the confidence percentage for the most probable activity
            int confidence = mostProbableActivity.getConfidence();

            // Get the type of activity
            int activityType = mostProbableActivity.getType();

            Commons.print( "ActivityRecognitionResult -- " + result.getProbableActivities());

            if (confidence >= 50) {
                if (activityType == DetectedActivity.ON_FOOT) {
                    DetectedActivity betterActivity = walkingOrRunning(result.getProbableActivities());

                    if (null != betterActivity)
                        activityType = betterActivity.getType();
                }
            }

            broadcastActivity(getNameFromType(activityType));
        }
    }

    private DetectedActivity walkingOrRunning(List<DetectedActivity> probableActivities) {
        DetectedActivity myActivity = null;
        int confidence = 0;
        for (DetectedActivity activity : probableActivities) {
            if (activity.getType() != DetectedActivity.RUNNING && activity.getType() != DetectedActivity.WALKING)
                continue;

            if (activity.getConfidence() > confidence) {
                confidence = activity.getConfidence();
                myActivity = activity;
            }
        }

        return myActivity;
    }

    /**
     * Map detected activity types to strings
     *
     * @param activityType The detected activity type
     * @return A user-readable name for the type
     */
    private String getNameFromType(int activityType) {
        switch (activityType) {
            case DetectedActivity.IN_VEHICLE:
                return IN_VEHICLE;
            case DetectedActivity.ON_BICYCLE:
                return LocationConstants.ON_BICYCLE;
            case DetectedActivity.RUNNING:
                return RUNNING;
            case DetectedActivity.WALKING:
                return WALKING;
            case DetectedActivity.ON_FOOT:
                return ON_FOOT;
            case DetectedActivity.STILL:
                return STILL;
            case DetectedActivity.TILTING:
                return TILTING;
            default:
                return UNKNOWN;
        }
    }
    private void broadcastActivity(String activityTtype) {
        Intent intent = new Intent("com.ivy.BROADCAST_DETECTED_ACTIVITY");
        intent.putExtra("type", activityTtype);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
