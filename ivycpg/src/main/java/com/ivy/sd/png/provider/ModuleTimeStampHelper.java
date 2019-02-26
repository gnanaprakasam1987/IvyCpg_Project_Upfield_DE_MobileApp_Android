package com.ivy.sd.png.provider;

import android.content.Context;

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

/**
 * Created by mayuri.v on 9/4/2017.
 */
public class ModuleTimeStampHelper {
    private final Context context;
    private final BusinessModel bmodel;
    private static ModuleTimeStampHelper instance = null;
    private String tid = "";
    private String moduleCode = "";

    private ModuleTimeStampHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static ModuleTimeStampHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ModuleTimeStampHelper(context);
        }
        return instance;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void saveModuleTimeStamp(String inOrout) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "Tid,DistributorId,UserId,Date,ModuleCode,Activity,Latitude,Longitude,GpsAccuracy,upload";
            String values = bmodel.QT(getTid()) + "," + bmodel.userMasterHelper.getUserMasterBO().getBranchId() + "," +
                    bmodel.userMasterHelper.getUserMasterBO().getUserid() + "," + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME)) + "," +
                    bmodel.QT(getModuleCode()) + "," + bmodel.QT(inOrout) + "," + bmodel.QT(LocationUtil.latitude + "") + "," +
                    bmodel.QT(LocationUtil.longitude + "") + "," + bmodel.QT(LocationUtil.accuracy + "") + ",'N'";
            db.insertSQL(DataMembers.tbl_ModuleActivityDetails, columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
