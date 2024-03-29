package com.ivy.cpg.view.acknowledgement;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

import static com.ivy.lib.Utils.QT;

/**
 * Created by anandasir.v on 9/7/2017.
 */

public class AcknowledgementHelper {

    private final Context mContext;
    private static AcknowledgementHelper instance = null;
    private ArrayList<JointCallAcknowledgementBO> acknowledgementList;
    private ArrayList<JointCallAcknowledgementCountBO> acknowledgementCountList;

    private AcknowledgementHelper(Context context) {
        this.mContext = context;
        acknowledgementList = new ArrayList<>();
    }

    public static AcknowledgementHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AcknowledgementHelper(context);
        }
        return instance;
    }

    public ArrayList<JointCallAcknowledgementBO> getAcknowledgementList() {
        return acknowledgementList;
    }

    public ArrayList<JointCallAcknowledgementCountBO> getAcknowledgementCountList() {
        return acknowledgementCountList;
    }

    /**
     * Download loadJOinCallAcknowledgement
     */
    public void loadJointCallAcknowledgement(String userID) {
        try {
            acknowledgementList = new ArrayList<>();
            acknowledgementList.clear();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT " + DataMembers.tbl_jointcallacknowledgement_cols + " FROM " +
                            DataMembers.tbl_jointcallacknowledgement + " where Upload ='Y' " +
                            "and AckDate is null or AckDate = '' and UserID = '" + userID + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    JointCallAcknowledgementBO obj = new JointCallAcknowledgementBO();
                    obj.setUserid(c.getString(0));
                    obj.setUsername(c.getString(1));
                    obj.setBeat(c.getString(2));
                    obj.setRetailer(c.getString(3));
                    obj.setDate(c.getString(4));
                    obj.setValue(c.getString(5));
                    obj.setRefid(c.getString(6));
                    obj.setAck_Date(c.getString(7));
                    obj.setUpload(c.getString(8));
                    acknowledgementList.add(obj);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void loadJointCallAcknowledgementCount() {
        try {
            acknowledgementCountList = new ArrayList<>();
            acknowledgementCountList.clear();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select UserID,Username,count(*) from JointCallAcknowledgement where Upload ='Y' and AckDate is null or AckDate = '' group by UserID");
            if (c != null) {
                while (c.moveToNext()) {
                    JointCallAcknowledgementCountBO obj = new JointCallAcknowledgementCountBO();
                    obj.setUserID(c.getString(0));
                    obj.setUserName(c.getString(1));
                    obj.setCount(c.getString(2));
                    acknowledgementCountList.add(obj);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateAcknowledgement(String userID, String refID, String status) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();
            db.updateSQL("Update " + DataMembers.tbl_jointcallacknowledgement + " set upload = " + QT(status) + " , Ack_Date ="
                    + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + " where UserID = " + QT(userID) + " and RefID = " + QT(refID));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }
}
