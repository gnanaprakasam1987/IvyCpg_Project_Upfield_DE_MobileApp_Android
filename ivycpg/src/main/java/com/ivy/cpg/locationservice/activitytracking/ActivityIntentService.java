package com.ivy.cpg.locationservice.activitytracking;


import android.app.IntentService;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;
import com.ivy.sd.png.util.Commons;

import java.util.List;

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
                return "in_vehicle";
            case DetectedActivity.ON_BICYCLE:
                return "RIDE";
            case DetectedActivity.RUNNING:
                return "RUN";
            case DetectedActivity.WALKING:
                return "walking";
            case DetectedActivity.ON_FOOT:
                return "on_foot";
            case DetectedActivity.STILL:
                return "still";
            case DetectedActivity.TILTING:
                return "tilting";
            default:
                return "unknown";
        }
    }
    private void broadcastActivity(String activityTtype) {
        Intent intent = new Intent("com.ivy.BROADCAST_DETECTED_ACTIVITY");
        intent.putExtra("type", activityTtype);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

}
