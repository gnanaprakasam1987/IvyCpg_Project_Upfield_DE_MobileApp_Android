package com.ivy.cpg.view.supervisor.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.amazonaws.com.google.gson.Gson;


/**
 * All Network Level utilities are defined here
 */

public class NetworkUtils {

    private NetworkUtils() {
        // This utility class is not publicly instantiable
    }

    /**
     * @param context Application Context
     * @return true if Connected, false if not connected
     */

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }


    public static String convertToSting(Object object) {
        return new Gson().toJson(object);
    }

    public static Object convertToObject(String jsonString, Object object) {
        return new Gson().fromJson(jsonString, object.getClass());
    }

}
