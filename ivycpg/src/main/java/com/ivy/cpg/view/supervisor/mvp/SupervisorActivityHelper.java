package com.ivy.cpg.view.supervisor.mvp;


import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.ivy.cpg.view.supervisor.SupervisorModuleConstants;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.LinkedHashMap;

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

    public void loginToFirebase() {

        if (FirebaseAuth.getInstance().getCurrentUser() == null || FirebaseDatabase.getInstance() == null) {
            String email = SupervisorModuleConstants.FIREBASE_EMAIL;
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

    public void downloadOutletListAws(Context context) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String queryStr = "select retailerId, latitude,longitude,retailerName,address,username from SupRetailerMaster SRM " +
                    "inner join usermaster um on um.userid = SRM.userId";

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

}
