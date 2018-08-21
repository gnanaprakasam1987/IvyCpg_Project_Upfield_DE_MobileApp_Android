package com.ivy.cpg.view.leaveapproval;

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
import java.util.concurrent.Callable;

import io.reactivex.Single;

public class LeaveApprovalHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static LeaveApprovalHelper instance = null;
    private ArrayList<LeaveApprovalBO> leavePending;
    private ArrayList<LeaveApprovalBO> leaveApproved;
    String mLeaveApproval = "LeaveApprovalDetails";

    public ArrayList<LeaveApprovalBO> getLeavePending() {
        if(leavePending==null)
            return new ArrayList<>();
        return leavePending;
    }

    public ArrayList<LeaveApprovalBO> getLeaveApproved() {
        if(leaveApproved==null)
            return new ArrayList<>();
        return leaveApproved;
    }


    private LeaveApprovalHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static LeaveApprovalHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LeaveApprovalHelper(context);
        }
        return instance;
    }
    public void clearInstance() {
        instance = null;
    }

    public Single<Boolean> updateLeaves(){
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    saveStatusTransaction(leavePending);

                    DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                            DataMembers.DB_PATH);
                    db.createDataBase();
                    db.openDataBase();
                    String sql;
                    Cursor c;


                    leavePending = new ArrayList<>();
                    leaveApproved = new ArrayList<>();

                    sql = new String("select distinct LR.userid,LR.refid,LR.fromdate,LR.todate,LR.status,SM.Listname,SM1.Listname,UM.username from Leaverequestdetails LR "
                            + "inner join StandardListMaster SM1 on SM1.Listid= LR.reasonid "
                            + "inner join StandardListMaster SM on SM.ListCode = LR.status "
                            + "inner join UserMaster UM on UM.userid = LR.userid "
                            + "AND LR.fromdate >= " + QT(getTodayDate()));
                    c = db.selectSQL(sql);


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

                    sql = new String("select distinct LR.userid,LR.refid,LR.fromdate,LR.todate,LR.status,SM.Listname,SM1.Listname,UM.username from Leaverequestdetails LR "
                            + "inner join StandardListMaster SM1 on SM1.Listid= LR.reasonid "
                            + "inner join StandardListMaster SM on SM.ListCode = LR.status "
                            + "inner join UserMaster UM on UM.userid = LR.userid "
                            + "AND LR.fromdate < " + QT(getTodayDate()));
                    c = db.selectSQL(sql);


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

                    //data from transaction table

                    sql = new String(
                            "select LA.refid,LA.status,SM.Listname from LeaveApprovalDetails LA " +
                                    "inner join StandardListMaster SM on SM.ListCode = LA.status");
                    c = db.selectSQL(sql);
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
                    db.closeDB();

                } catch (Exception e) {
                    Commons.printException(e);
                }

                return Boolean.TRUE;
            }
        });
    }

    public void saveStatusTransaction(ArrayList<LeaveApprovalBO> leaves) {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();


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
