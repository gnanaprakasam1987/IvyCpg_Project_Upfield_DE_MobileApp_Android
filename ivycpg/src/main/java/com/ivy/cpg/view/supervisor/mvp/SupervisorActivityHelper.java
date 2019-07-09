package com.ivy.cpg.view.supervisor.mvp;


import android.content.Context;
import android.database.Cursor;
import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.util.LinkedHashMap;

import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FB_APPLICATION_ID;
import static com.ivy.cpg.view.supervisor.SupervisorModuleConstants.FIREBASE_EMAIL;

public class SupervisorActivityHelper {

    private static SupervisorActivityHelper instance = null;
    private LinkedHashMap<Integer,RetailerBo> retailerMasterHashmap =  new LinkedHashMap<>();

    private SupervisorActivityHelper() {
    }

    public static SupervisorActivityHelper getInstance() {
        if (instance == null) {
            instance = new SupervisorActivityHelper();
        }
        return instance;
    }

    public void loginToFirebase(Context context) {

        String appId = AppUtils.getSharedPreferences(context).getString(FB_APPLICATION_ID, "");
        if (appId.equals(""))
            return;

        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            String email = AppUtils.getSharedPreferences(context).getString(FIREBASE_EMAIL,"");
            String password = SupervisorModuleConstants.FIREBASE_PASSWORD;
            // Authenticate with Firebase and subscribe to updates

            if(email.trim().length() > 0 && password.trim().length() > 0) {
                FirebaseAuth.getInstance().signInWithEmailAndPassword(
                        email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Commons.print("firebase auth success");
                        } else {
                            Commons.print("firebase auth failed");
                        }
                    }
                });
            }
        }
    }

    public void downloadOutletListAws(Context context,String date) {

        retailerMasterHashmap.clear();

        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select retailerId, latitude,longitude,retailerName,address,username,channelid,imgpath from SupRetailerMaster SRM " +
                    "inner join usermaster um on um.userid = SRM.userId where date = '"+date+"'";

            Cursor c = db.selectSQL(queryStr);
            if (c != null) {
                while (c.moveToNext()) {
                    RetailerBo retailerBo = new RetailerBo();
                    retailerBo.setRetailerId(c.getInt(0));
                    retailerBo.setMasterLatitude(c.getDouble(1));
                    retailerBo.setMasterLongitude(c.getDouble(2));
                    retailerBo.setRetailerName(c.getString(3));
                    retailerBo.setAddress(c.getString(4));
                    retailerBo.setUserName(c.getString(5));
                    retailerBo.setChannelId(c.getInt(6));
                    retailerBo.setImgPath(c.getString(7));

                    retailerMasterHashmap.put(retailerBo.getRetailerId(),retailerBo);
                }
                c.close();
            }

            setRetailerMasterHashmap(retailerMasterHashmap);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null)
                db.closeDB();
        }
    }

    public LinkedHashMap<Integer, RetailerBo> getRetailerMasterHashmap() {
        return retailerMasterHashmap;
    }

    public void setRetailerMasterHashmap(LinkedHashMap<Integer, RetailerBo> retailerMasterHashmap) {
        this.retailerMasterHashmap = retailerMasterHashmap;
    }

    public RetailerBo getRetailerById(int retailerId){

        RetailerBo retailerBo = new RetailerBo();

        if(retailerMasterHashmap.get(retailerId) != null)
            retailerBo = retailerMasterHashmap.get(retailerId);

        return retailerBo;

    }

    public String retailerNameById(int retailerId){

        if(retailerMasterHashmap.get(retailerId) != null)
            return retailerMasterHashmap.get(retailerId).getRetailerName();

        return "";
    }


    public LatLng retailerLatLngByRId(int retailerId){
        LatLng latLng = new LatLng(0,0);

        if(retailerMasterHashmap.get(retailerId) != null){
            latLng = new LatLng(retailerMasterHashmap.get(retailerId).getMasterLatitude(),retailerMasterHashmap.get(retailerId).getMasterLongitude());
        }

        return latLng;
    }

    public boolean isChatConfigAvail(Context context){
        boolean isChatConfigAvail =false;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor moduleCursor = db.selectSQL("select hhtcode from HhtModuleMaster where hhtCode = 'CHAT02'");
            if (moduleCursor != null && moduleCursor.getCount() > 0) {
                isChatConfigAvail = true;
                moduleCursor.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return isChatConfigAvail;

    }

    public String getDownloadUrl(Context context,String masterName){
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        String downloadurl = "";
        try {
            db.openDataBase();
            db.createDataBase();
            String sb = "select url from urldownloadmaster where " +
                    "mastername='"+masterName+"' and typecode='SYNMAS'";

            Cursor c = db.selectSQL(sb);
            if (c != null) {
                if (c.getCount() > 0 && c.moveToNext()) {
                    downloadurl = c.getString(0);
                }
            }
        }catch (Exception e){
            Commons.printException(e);
        }

        return downloadurl;
    }

}
