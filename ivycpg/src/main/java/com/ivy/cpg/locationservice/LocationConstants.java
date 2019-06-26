package com.ivy.cpg.locationservice;


public class LocationConstants {
    public static final int STATUS_SUCCESS = 0;  // Service started Successfully
    public static final int STATUS_LOCATION_PERMISSION = 1; // Location Permission is not enabled
    public static final int STATUS_GPS = 2; // GPS Not enabled
    public static final int STATUS_LOCATION_ACCURACY = 3; // Location Accuracy level is low
    public static final int STATUS_MOCK_LOCATION = 4; // Mock Location is enabled
    public static final int STATUS_SERVICE_ERROR = 5; // Problem in starting Service
    public static final int STATUS_TIME_MISMATCH = 6; // Start time is greater than end
    public static final int STATUS_ALARM_TIME = 7; //Alarm time shoukd not be zero


    public static final int REALTIME_NOTIFICATION_ID = 1112; //Real Time Notification id
    public static final int MOCK_NOTIFICATION_ID = 1113; // Mock Location notification id
    public static final int GPS_NOTIFICATION_ID = 1111; // GPS Notification Id

    public static final long LOCATION_INTERVAL = 60*1000;
    public static final long LOCATION_MAX_WAIT_TIME = 60*60*1000;
    public static final int LOCATION_DISPLACEMENT = 5;



    public static final String IN_VEHICLE = "IN VEHICLE";
    public static final String ON_BICYCLE = "ON BICYCLE";
    public static final String RUNNING = "RUNNING";
    public static final String WALKING = "WALKING";
    public static final String ON_FOOT = "ON FOOT";
    public static final String STILL = "STILL";
    public static final String TILTING = "TILTING";
    public static final String UNKNOWN = "UNKNOWN";

}
