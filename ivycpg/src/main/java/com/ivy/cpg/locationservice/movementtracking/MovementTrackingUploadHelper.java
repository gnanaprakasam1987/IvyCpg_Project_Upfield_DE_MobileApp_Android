package com.ivy.cpg.locationservice.movementtracking;


import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.lib.rest.JSONFormatter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ActivationHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

public class MovementTrackingUploadHelper {


    private static MovementTrackingUploadHelper instance = null;

    private MovementTrackingUploadHelper() {
    }

    public static MovementTrackingUploadHelper getInstance() {
        if (instance == null) {
            instance = new MovementTrackingUploadHelper();
        }
        return instance;
    }


}
