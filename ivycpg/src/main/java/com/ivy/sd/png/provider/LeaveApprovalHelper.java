package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LeaveApprovalBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class LeaveApprovalHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static LeaveApprovalHelper instance = null;
    private ArrayList<LeaveApprovalBO> leavePending;
    private ArrayList<LeaveApprovalBO> leaveApproved;
    String mLeaveApproval = "LeaveApprovalDetails";

    public ArrayList<LeaveApprovalBO> getLeavePending() {
        return leavePending;
    }

    public ArrayList<LeaveApprovalBO> getLeaveApproved() {
        return leaveApproved;
    }


    protected LeaveApprovalHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static LeaveApprovalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LeaveApprovalHelper(context);
        }
        return instance;
    }

    public void loadLeaveData() {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String sql;
            Cursor c;


            leavePending = new ArrayList<LeaveApprovalBO>();
            leaveApproved = new ArrayList<LeaveApprovalBO>();

            sql = new String("select LR.userid,LR.refid,LR.fromdate,LR.todate,LR.status,SM.Listname,SM1.Listname,UM.username from Leaverequestdetails LR "
                    + "inner join StandardListMaster SM1 on SM1.Listid= LR.reasonid "
                    + "inner join StandardListMaster SM on SM.ListCode = LR.status "
                    + "inner join UserMaster UM on UM.userid = LR.userid "
                    + "AND LR.fromdate >= " + QT(getTodayDate()));
            c = db.selectSQL(sql);


            sql = null;
            if (c != null) {
                while (c.moveToNext()) {
                    LeaveApprovalBO leveas = new LeaveApprovalBO();
                    leveas.setUserId(c.getInt(0));
                    leveas.setRefId(c.getInt(1));
                    leveas.setFromDate(c.getString(2));
                    leveas.setToDate(c.getString(3));
                    leveas.setStatusCode(c.getString(4));
                    leveas.setStatus(c.getString(5));
                    leveas.setReason(c.getString(6));
                    leveas.setUsername(c.getString(7));
                    leveas.setChanged(false);
                    leveas.setSelected(false);

                    leavePending.add(leveas);


                }
            }

            c.close();
            c = null;

            sql = new String("select LR.userid,LR.refid,LR.fromdate,LR.todate,LR.status,SM.Listname,SM1.Listname,UM.username from Leaverequestdetails LR "
                    + "inner join StandardListMaster SM1 on SM1.Listid= LR.reasonid "
                    + "inner join StandardListMaster SM on SM.ListCode = LR.status "
                    + "inner join UserMaster UM on UM.userid = LR.userid "
                    + "AND LR.fromdate < " + QT(getTodayDate()));
            c = db.selectSQL(sql);


            sql = null;
            if (c != null) {
                while (c.moveToNext()) {
                    LeaveApprovalBO leveas = new LeaveApprovalBO();
                    leveas.setUserId(c.getInt(0));
                    leveas.setRefId(c.getInt(1));
                    leveas.setFromDate(c.getString(2));
                    leveas.setToDate(c.getString(3));
                    leveas.setStatusCode(c.getString(4));
                    leveas.setStatus(c.getString(5));
                    leveas.setReason(c.getString(6));
                    leveas.setUsername(c.getString(7));
                    leveas.setChanged(false);
                    leveas.setSelected(false);

                    leaveApproved.add(leveas);

                }
            }

            c.close();
            c = null;

            //data from transaction table

            sql = new String(
                    "select LA.refid,LA.status,SM.Listname from LeaveApprovalDetails LA " +
                            "inner join StandardListMaster SM on SM.ListCode = LA.status");
            c = db.selectSQL(sql);
            sql = null;
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < leavePending.size(); i++) {
                        if (c.getInt(0) == leavePending.get(i).getRefId()) {
                            leavePending.get(i).setStatusCode(c.getString(1));
                            leavePending.get(i).setStatus(c.getString(2));
                        }

                    }
                }
            }

            c.close();
            c = null;


            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

   /* public boolean hasDataTosave(ArrayList<LeaveApprovalBO> leaveusers) {

        for (LeaveApprovalBO usersBO : leaveusers) {
            if (usersBO.isChanged())
                return true;
        }

        return false;
    } */


    public void saveStatusTransaction(ArrayList<LeaveApprovalBO> leaves) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            String sql;
            Cursor statusCursor;

            String statusColumns = "RefId, Status, ApprovedDate, uid, Upload";

            String values;


            for (LeaveApprovalBO approvalBO : leaves) {
                if (approvalBO.isChanged()) {
                    // delete transaction if exist

                    db.deleteSQL(mLeaveApproval, "RefId="
                            + QT(""+approvalBO.getRefId()), false);
                    // save
                    values = approvalBO.getRefId() + ","
                            + QT(approvalBO.getStatusCode()) + ","
                            + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                            + QT(bmodel.userMasterHelper.getUserMasterBO().getUserid() + SDUtil
                            .now(SDUtil.DATE_TIME_ID))+ ","
                            +QT("N");


                    db.insertSQL(mLeaveApproval, statusColumns, values);
                }
            }


            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            db.closeDB();
        }
    }

    public String QT(String data) {
        return "'" + data + "'";
    }

    private String getTodayDate() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();
        return formatter.format(date.getTime());
    }
}
