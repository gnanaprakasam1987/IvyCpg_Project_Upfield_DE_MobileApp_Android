package com.ivy.cpg.view.attendance;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.nonfield.NonFieldTwoBo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Vector;

public class AttendanceHelper {

    private BusinessModel bmodel;
    private static AttendanceHelper instance = null;
    private Vector<AttendanceBO> reasonList = new Vector<>();

    private ArrayList<NonFieldTwoBo> nonFieldTwoBoList = new ArrayList<>();

    public Vector<AttendanceBO> getReasonList() {
        return reasonList;
    }


    private void setReasonList(Vector<AttendanceBO> reasonList) {
        this.reasonList = reasonList;
    }

    private AttendanceHelper(Context context) {
        bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static AttendanceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new AttendanceHelper(context);
        }
        return instance;
    }

    /*
     * DownLoad the Reason for Attendance
     */
    public void downloadAttendanceReasons(Context context) {
        reasonList.clear();
        try {
            AttendanceBO reason;
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName,isRequired,ParentId from StandardListMaster where ListType='ATTENDANCE_TYPE'");
            if (c.getCount() > 0) {
                setReasonList(new Vector<AttendanceBO>());
                AttendanceBO reasonBO;
                reasonBO = new AttendanceBO();
                reasonBO.setAtd_Lid(0);
                reasonBO.setAtd_LCode("NONE");
                reasonBO.setAtd_LName(context.getString(R.string.none));
                reasonBO.setAtd_isRequired(0);
                reasonBO.setAtd_PLId(0);
                getReasonList().add(reasonBO);
                while (c.moveToNext()) {
                    reason = new AttendanceBO();
                    reason.setAtd_Lid(c.getInt(0));
                    reason.setAtd_LCode(c.getString(1));
                    reason.setAtd_LName(c.getString(2));
                    reason.setAtd_isRequired(c.getInt(3));
                    reason.setAtd_PLId(c.getInt(4));
                    getReasonList().add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean checkLeaveAttendance(Context context) {
        try {
            String sessionType = "";
            String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);

            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.openDataBase();

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            if (bmodel.configurationMasterHelper.IS_IN_OUT_MANDATE) {
                Cursor c = db
                        .selectSQL("SELECT * FROM AttendanceTimeDetails where userid = " + userid + " AND date = " + bmodel.QT(currentDate) +
                                " AND upload = 'N' or upload ='Y'");
                if (c.getCount() == 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            } else {
                Cursor c1 = db.selectSQL("select Session from AttendanceDetail AD inner join StandardListMaster SLM on " +
                        "SLM.ListId=AD.Session where " +
                        bmodel.QT(currentDate) + " BETWEEN AD.FromDate AND AD.ToDate AND AD.userid=" + userid);
                if (c1 != null) {
                    if (c1.getCount() == 1)
                        while (c1.moveToNext()) {
                            sessionType = bmodel.getStandardListCode(c1.getInt(0));

                        }
                    c1.close();
                }

                String condition = "";
                String query;

                if (sessionType.equals("FN")) {
                    condition = " AND " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.TIME)) + "<=" +
                            bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF"));
                } else if (sessionType.equals("AN")) {
                    condition = " AND " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.TIME)) + ">" +
                            bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF"));
                }
                query = "SELECT * FROM StandardListMaster where Listcode = 'LEAVE'  and listid in (select Atd_ID from AttendanceDetail where " +
                        bmodel.QT(currentDate) + " BETWEEN FromDate AND ToDate AND Status = 'S' AND userid=" + userid + ")" + condition;


                Cursor c = db.selectSQL(query);

                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;

    }

    public boolean loadAttendanceMaster(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT * from AttendanceDetail where DateIn ="
                            + bmodel.QT((DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))));
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("loadAttendanceMaster" + e);
        }
        return false;
    }

    /**
     * Save Attendance Details
     */
    public void saveAttendanceDetails(String date, int atdID, int reasonid,
                                      String fromDate, String toDate,
                                      String atdCode, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + "";

            String columns = "Tid,DateIn,Atd_ID,ReasonID,Timezone,FromDate,ToDate,TimeIn,timespent,status,userid";
            String values = bmodel.QT(tid) + "," + bmodel.QT(date) + ","
                    + atdID + ", " + reasonid + ","
                    + bmodel.QT(DateTimeUtils.getTimeZone()) + ","
                    + bmodel.QT(fromDate) + "," + bmodel.QT(toDate) + ","
                    + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_TIME)) + ",'0:0'," + (atdCode != null && atdCode.equals("LEAVE") ? "'S'" : "'R'" + "," + bmodel.userMasterHelper.getUserMasterBO().getUserid());

            db.insertSQL("AttendanceDetail", columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("saveAttendanceDetails" + e);
        }
    }


    public String getReasonName(String id, Context context) {
        try {

            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListName FROM StandardListMaster"
                    + " WHERE ListId = " + id);
            if (c != null) {
                if (c.moveToNext()) {
                    return c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return "";
    }

    public boolean hasInOutAttendanceEnabled(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT * from HhtMenuMaster where MenuType ="
                            + bmodel.QT("HOME_MENU") + " AND HHTCode = " + bmodel.QT("MENU_IN_OUT") + " AND flag=1");
            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException("deleteNonField exception", e);
            return false;
        }

        return false;
    }

    public void downloadNonFieldTwoDetails(Context context) {
        getNonFieldTwoBoList().clear();

        int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT uid , date , intime , outtime , ifnull(remarks,'') , rowid,reasonid from AttendanceTimeDetails where date ="
                            + bmodel.QT((DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))) + " or outtime IS NULL and userid=" + userid);
            if (c != null) {
                while (c.moveToNext()) {
                    NonFieldTwoBo nonFieldTwoBo = new NonFieldTwoBo();
                    nonFieldTwoBo.setId(c.getString(0));
                    nonFieldTwoBo.setFromDate(c.getString(1));
                    nonFieldTwoBo.setInTime(c.getString(2));
                    nonFieldTwoBo.setOutTime(c.getString(3));
                    nonFieldTwoBo.setRemarks(c.getString(4));
                    nonFieldTwoBo.setRowid(c.getInt(5));

                    if ((nonFieldTwoBo.getInTime() != null && !nonFieldTwoBo.getInTime().trim().equalsIgnoreCase(""))
                            && (nonFieldTwoBo.getOutTime() != null && !nonFieldTwoBo.getOutTime().trim().equalsIgnoreCase(""))) {
                        nonFieldTwoBo.setStatus(bmodel.getResources().getString(R.string.in_complete));
                    } else {
                        nonFieldTwoBo.setStatus(bmodel.getResources().getString(R.string.in_partial));
                    }
                    nonFieldTwoBo.setReason(c.getString(6));
                    getNonFieldTwoBoList().add(nonFieldTwoBo);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException("downloadNonFieldTwoDetails" + e);
        }
    }

    public void saveNonFieldWorkTwoDetail(NonFieldTwoBo nonFieldTwoBo, Context context) {

        if (nonFieldTwoBo != null && nonFieldTwoBo.getId() != null) {
            try {
                DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
                db.createDataBase();
                db.openDataBase();

                int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

                String inTime = nonFieldTwoBo.getInTime() != null ? nonFieldTwoBo.getInTime() : " ";

                String columns = "uid,date,intime,reasonid,userid,latitude,longitude,counterid,Remarks,upload";
                String value = StringUtils.getStringQueryParam(nonFieldTwoBo.getId()) + ","
                        + StringUtils.getStringQueryParam(nonFieldTwoBo.getFromDate()) + ","
                        + StringUtils.getStringQueryParam(inTime) + ","
                        + nonFieldTwoBo.getReason() + "," + userid + ","
                        + StringUtils.getStringQueryParam(LocationUtil.latitude + "") + "," + StringUtils.getStringQueryParam(LocationUtil.longitude + "") + ","
                        + bmodel.getCounterId() + "," + StringUtils.getStringQueryParam(nonFieldTwoBo.getRemarks()) + "," + StringUtils.getStringQueryParam("N");

                db.insertSQL("AttendanceTimeDetails", columns, value);

                db.closeDB();
            } catch (Exception e) {

                Commons.printException("loadAttendanceMaster" + e);
            }
        }


        Commons.print("Attendance Helper," + "Save : " + nonFieldTwoBo);
    }

    public void updateNonFieldWorkTwoDetail(NonFieldTwoBo nonFieldTwoBo, Context context) {
        Commons.print("Attendance Helper," + "Update  : " + nonFieldTwoBo);

        if (nonFieldTwoBo != null && nonFieldTwoBo.getRowid() > 0) {
            try {
                DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
                db.createDataBase();
                db.openDataBase();

                String updateSql = "update AttendanceTimeDetails " +
                        "SET intime = " + bmodel.QT(nonFieldTwoBo.getInTime()) +
                        " , outtime = " + bmodel.QT(nonFieldTwoBo.getOutTime()) +
                        ", upload ='N'" +
                        " WHERE rowid = " + nonFieldTwoBo.getRowid();

                db.updateSQL(updateSql);

                db.closeDB();
            } catch (Exception e) {

                Commons.printException("loadAttendanceMaster" + e);
            }
        }

    }

    public boolean previousInOutTimeCompleted() {
        boolean status = false;

        if (nonFieldTwoBoList.size() == 0)
            return true;

        for (NonFieldTwoBo nonFieldTwoBo : nonFieldTwoBoList) {
            status = (nonFieldTwoBo.getInTime() != null && !nonFieldTwoBo.getInTime().trim().equalsIgnoreCase(""))
                    && (nonFieldTwoBo.getOutTime() != null && !nonFieldTwoBo.getOutTime().trim().equalsIgnoreCase(""));
        }


        return status;
    }

    public ArrayList<NonFieldTwoBo> getNonFieldTwoBoList() {
        return nonFieldTwoBoList;
    }

    public void updateAttendaceDetailInTime(Context context) {
        String uid = "";
        for (int i = 0; i < getNonFieldTwoBoList().size(); i++)
            if (getNonFieldTwoBoList().get(i).getInTime().equals(""))
                uid = getNonFieldTwoBoList().get(i).getId();


        DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();

        String updateSql = "update AttendanceTimeDetails " +
                "SET intime= " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.TIME)) +
                " WHERE uid='" + uid + "'";

        db.updateSQL(updateSql);

        db.closeDB();
    }

    public ArrayList<StandardListBO> loadChildUserList(Context context) {
        ArrayList<StandardListBO> childUserBOs = new ArrayList<>();
        StandardListBO childUserBO;
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT userid, username FROM UserMaster WHERE  isDeviceUser=0 and Relationship = 'CHILD'");

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    childUserBO = new StandardListBO();
                    childUserBO.setChildUserId(c.getInt(0));
                    childUserBO.setChildUserName(c.getString(1));
                    childUserBOs.add(childUserBO);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Error in getting child user list", e);
        }
        return childUserBOs;
    }


    /**
     * This Method checks the given Id is Working status
     *
     * @param id      StandardListMaster ListId
     * @param context Context
     * @return returns boolean
     */
    public boolean isWorkingStatus(int id, Context context) {

        DBUtil db;
        boolean isIdWorking = false;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("select Listid from StandardListMaster where ListCode='WORKING' and ListId = '" + id + "'");
            if (c != null && c.getCount() > 0) {
                c.close();
                isIdWorking = true;
            }
            db.close();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return isIdWorking;
    }

    public boolean isSellerWorking(Context context) {
        DBUtil db;
        boolean check = true;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            int counts = 0;

            Cursor c = db
                    .selectSQL("SELECT HHTCode ,(select COUNT(upload) from AttendanceTimeDetails atd " +
                            "inner join StandardListMaster sm on sm.ListId = atd.reasonid where atd.outtime IS NULL and sm.ListCode='WORKING')" +
                            " as count FROM HhtMenuMaster where HHTCode='MENU_IN_OUT' and Flag=1 and hasLink=1");
            if (c != null) {
                if (c.moveToFirst())
                    counts = c.getInt(1);

                c.close();
            }
            db.close();

            if (counts > 0) {
                check = false;
            } else {
                check = true;
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return check;
    }


}
