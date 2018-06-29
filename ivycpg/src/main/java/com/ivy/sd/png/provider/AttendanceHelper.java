package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttendanceBO;
import com.ivy.sd.png.bo.LeaveRuleBO;
import com.ivy.sd.png.bo.LeaveSpinnerBO;
import com.ivy.sd.png.bo.NonFieldBO;
import com.ivy.sd.png.bo.NonFieldTwoBo;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Vector;

public class AttendanceHelper {

    private BusinessModel bmodel;
    private static AttendanceHelper instance = null;
    private Vector<AttendanceBO> reasonList = new Vector<AttendanceBO>();
    private ArrayList<NonFieldBO> nonFieldReasonList = new ArrayList<NonFieldBO>();
    private ArrayList<NonFieldBO> nonFieldList = new ArrayList<NonFieldBO>();
    private ArrayList<NonFieldTwoBo> nonFieldTwoBoList = new ArrayList<NonFieldTwoBo>();
    private ArrayList<NonFieldBO> lstRadioBtn = new ArrayList<NonFieldBO>();
    private ArrayList<LeaveSpinnerBO> leavesList = new ArrayList<LeaveSpinnerBO>();
    private HashMap<Integer, NonFieldBO> reasonBOByreasonID = new HashMap<Integer, NonFieldBO>();
    private HashMap<Integer, LeaveSpinnerBO> leaveTypeByID = new HashMap<Integer, LeaveSpinnerBO>();
    private ArrayList<LeaveRuleBO> leavesBo = new ArrayList<LeaveRuleBO>();
    ArrayList<String> dateList;

    private String[] weekOffDays = new String[7];

    public Vector<AttendanceBO> getReasonList() {
        return reasonList;
    }

    public ArrayList<LeaveSpinnerBO> getLeavesTypeList() {
        return leavesList;
    }

    public ArrayList<LeaveRuleBO> getLeavesBo() {
        return leavesBo;
    }

    public void setLeavesTypeList(ArrayList<LeaveSpinnerBO> leavesList) {
        this.leavesList = leavesList;
    }

    public void setReasonList(Vector<AttendanceBO> reasonList) {
        this.reasonList = reasonList;
    }

    protected AttendanceHelper(Context context) {
        bmodel = (BusinessModel) context;
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
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
            StringBuilder query = new StringBuilder();
            String currentDate = SDUtil.now(SDUtil.DATE_GLOBAL);
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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


                query.append("SELECT * FROM StandardListMaster where Listcode = 'LEAVE'  and listid in (select Atd_ID from AttendanceDetail where " +
                        bmodel.QT(currentDate) + " BETWEEN FromDate AND ToDate AND Status = 'S' AND userid=" + userid + ")");

                if (sessionType.equals("FN")) {
                    query.append(" AND " + bmodel.QT(SDUtil.now(SDUtil.TIME)) + "<=" + bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF")));
                } else if (sessionType.equals("AN")) {
                    query.append(" AND " + bmodel.QT(SDUtil.now(SDUtil.TIME)) + ">" + bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF")));
                }
                Cursor c = db.selectSQL(query.toString());

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
        DBUtil db = null;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT * from AttendanceDetail where DateIn ="
                            + bmodel.QT((SDUtil.now(SDUtil.DATE_GLOBAL))));
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
    public boolean saveAttendanceDetails(String date, int atdID, int reasonid,
                                         String fromDate, String toDate,
                                         String atdCode, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID) + "";

            String columns = "Tid,DateIn,Atd_ID,ReasonID,Timezone,FromDate,ToDate,TimeIn,timespent,status,userid";
            String values = bmodel.QT(tid) + "," + bmodel.QT(date) + ","
                    + atdID + ", " + reasonid + ","
                    + bmodel.QT(bmodel.getTimeZone()) + ","
                    + bmodel.QT(fromDate) + "," + bmodel.QT(toDate) + ","
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_TIME)) + ",'0:0'," + (atdCode != null && atdCode.equals("LEAVE") ? "'S'" : "'R'" + "," + bmodel.userMasterHelper.getUserMasterBO().getUserid());

            db.insertSQL("AttendanceDetail", columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("saveAttendanceDetails" + e);
            return false;
        }

        return true;
    }

    /**
     * Down load Non fiels Reasons
     */
    public void downNonFieldReasons(Context context) {
        try {
            reasonBOByreasonID = new HashMap<Integer, NonFieldBO>();
            nonFieldReasonList.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListId,ListCode,ListName,isRequired,ParentId from StandardListMaster where ListType='ATTENDANCE_TYPE'");
            setNonFieldReasonList(new ArrayList<NonFieldBO>());
            if (c != null) {
                NonFieldBO reasonBO;
                reasonBO = new NonFieldBO();
                reasonBO.setReasonID(0);
                reasonBO.setCode("NONE");
                reasonBO.setReason(context.getString(R.string.none));
                reasonBO.setIsRequired(0);
                reasonBO.setpLevelId(0);
                reasonBOByreasonID.put(reasonBO.getReasonID(), reasonBO);
                getNonFieldReasonList().add(reasonBO);
                while (c.moveToNext()) {
                    reasonBO = new NonFieldBO();
                    reasonBO.setReasonID(c.getInt(0));
                    reasonBO.setCode(c.getString(1));
                    reasonBO.setReason(c.getString(2));
                    reasonBO.setIsRequired(c.getInt(3));
                    reasonBO.setpLevelId(c.getInt(4));
                    reasonBOByreasonID.put(reasonBO.getReasonID(), reasonBO);
                    getNonFieldReasonList().add(reasonBO);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Non Field Work Loading Exception", e);
        }

    }


    public void downNonFieldTwoReasons(Context context) {
        try {
            reasonBOByreasonID = new HashMap<Integer, NonFieldBO>();
            nonFieldReasonList.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL(bmodel.reasonHelper.getReasonFromStdListMaster("ATR"));

            setNonFieldReasonList(new ArrayList<NonFieldBO>());
            if (c != null) {
                NonFieldBO reasonBO;
                reasonBO = new NonFieldBO();
                reasonBO.setReasonID(0);
                reasonBO.setReason(context.getString(R.string.none));
                //have to check below 3 values
                reasonBO.setCode("NONE");
                reasonBO.setIsRequired(0);
                reasonBO.setpLevelId(0);

                reasonBOByreasonID.put(reasonBO.getReasonID(), reasonBO);
                getNonFieldReasonList().add(reasonBO);
                while (c.moveToNext()) {
                    reasonBO = new NonFieldBO();
                    reasonBO.setReasonID(c.getInt(0));
                    reasonBO.setReason(c.getString(1));
                    //have to check below 3 values
                    reasonBO.setCode("NONE");
                    reasonBO.setIsRequired(0);
                    reasonBO.setpLevelId(0);

                    reasonBOByreasonID.put(reasonBO.getReasonID(), reasonBO);
                    getNonFieldReasonList().add(reasonBO);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Non Field Work Loading Exception", e);
        }

    }

    public String getReasonName(String id, Context context) {
        try {

            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListName FROM StandardListMaster"
                    + " WHERE ListId = " + id);
            if (c != null) {
                while (c.moveToNext()) {
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

    /**
     * Download Session Types
     */
    public void dynamicRadioButtton(Context context) {
        try {
            lstRadioBtn.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListName,ListCode FROM StandardListMaster WHERE ListType='NONFIELD_SESSION_TYPE'");
            if (c != null) {
                NonFieldBO sessionBO;
                while (c.moveToNext()) {
                    sessionBO = new NonFieldBO();
                    sessionBO.setSessionID(c.getInt(0));
                    sessionBO.setSession(c.getString(1));
                    sessionBO.setSessionCode(c.getString(2));
                    getRadioButtonNames().add(sessionBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("Non Field Work Loading Exception", e);
        }

    }

    public String getReasonBOByReasonID(int n) {
        NonFieldBO reasonBo = reasonBOByreasonID.get(n);
        if (reasonBo != null) {
            return reasonBo.getReason();
        }
        return null;
    }

    public String getLeaveTypeByID(int n) {
        LeaveSpinnerBO typeBo = leaveTypeByID.get(n);
        if (typeBo != null) {
            return typeBo.getSpinnerTxt();
        }
        return null;
    }

    public String getSessionBOBySessionID(int n) {
        for (int i = 0; i < lstRadioBtn.size(); i++) {
            if (lstRadioBtn.get(i).getsessionID() == n) {
                return lstRadioBtn.get(i).getSession();
            }
        }
        return null;
    }

    public double getSessionLeaveById(int id) {
        for (int i = 0; i < lstRadioBtn.size(); i++) {
            if (lstRadioBtn.get(i).getsessionID() == id) {
                if (lstRadioBtn.get(i).getSessionCode().equalsIgnoreCase("AN") || lstRadioBtn.get(i).getSessionCode().equalsIgnoreCase("FN"))
                    return 0.5;
                else
                    return 1;
            }
        }
        return 1;
    }

    public int getSessionID(String s) {
        for (int i = 0; i < lstRadioBtn.size(); i++) {
            if (lstRadioBtn.get(i).getSession().equals(s)) {
                return lstRadioBtn.get(i).getsessionID();
            }
        }
        return 0;
    }

    /**
     * Save Non field Work Details
     */
    public boolean saveNonFieldWorkDetails(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String columns = "Tid, DateIn,Atd_ID,ReasonID, FromDate, ToDate, Session, Remarks,Timezone,Status,jointUserId,LeaveType_LovId,TotalDays,TimeSpent,userid";

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID) + "";

            for (int i = 0; i < nonFieldList.size(); i++) {

                String values = bmodel.QT(tid)
                        + ","
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + ","
                        + nonFieldList.get(i).getReasonID()
                        + ","
                        + nonFieldList.get(i).getSubReasonId()
                        + ","
                        + bmodel.QT(nonFieldList.get(i).getFrmDate())
                        + ","
                        + bmodel.QT(nonFieldList.get(i).getToDate())
                        + "," + nonFieldList.get(i).getsessionID() + ","
                        + bmodel.QT(nonFieldList.get(i).getDescription()) + ","
                        + bmodel.QT(bmodel.getTimeZone()) + "," + "'R'," + nonFieldList.get(i).getJointUserId() + ","
                        + nonFieldList.get(i).getLeaveLovId() + "," + nonFieldList.get(i).getTotalDays() + "," + bmodel.QT(nonFieldList.get(i).getTimeSpent()) + "," + userid;

                db.insertSQL("AttendanceDetail", columns, values);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("saveNonFieldDetails" + e);
            return false;
        }

        return true;
    }

    /**
     * Down load Non Field Details After saving add details
     */
    public void downloadNonFieldDetails(Context context) {
        getMonthList(context);
        try {
            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            nonFieldList.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT distinct AD.FromDate,AD.ToDate,AD.Session,AD.Remarks,AD.Atd_ID,AD.Tid,Status,AD.LeaveType_LovId," +
                            "AD.TimeSpent,AD.Upload,UM.username FROM AttendanceDetail AD left join UserMaster UM on UM.userID = AD.UserId order by FromDate desc");
            if (c != null) {
                // setReason(new ArrayList<NonFieldMaster>());
                NonFieldBO reasonBO;
                while (c.moveToNext()) {
                    reasonBO = new NonFieldBO();
                    reasonBO.setFrmDate(c.getString(0) + "");
                    reasonBO.setToDate(c.getString(1) + "");
                    reasonBO.setSessionID(c.getInt(2));
                    reasonBO.setDescription(c.getString(3) + "");
                    reasonBO.setReasonID(c.getInt(4));
                    reasonBO.setTid(c.getString(5));
                    reasonBO.setStatus(c.getString(6));
                    reasonBO.setLeaveLovId(c.getInt(7));
                    reasonBO.setTimeSpent(c.getString(8));
                    reasonBO.setUpload(c.getString(9));
                    reasonBO.setUserName(c.getString(10));
                    String mName = monthName(reasonBO.getFrmDate());
                    reasonBO.setMonthName(mName);
                    dateList.remove(mName);
                    getNonFieldList().add(reasonBO);
                }
                c.close();
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("Non Field Work Loading Exception", e);
        }

    }

    /**
     * Check any Checkbox is selected to delete the item
     *
     * @return bool
     */
    public boolean hasDelete() {
        try {
            for (NonFieldBO nonFeildBo : getNonFieldList()) {
                if (nonFeildBo.isDeleteRequest())
                    return true;
            }
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
        return false;
    }

    /**
     * Delate the selected row in AttendanceDetails
     *
     * @return bool
     */
    public boolean deleteNonfield(Context context) {
        DBUtil db = null;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            for (NonFieldBO nonFeildBo : getNonFieldList()) {
                if (nonFeildBo.isDeleteRequest()) {
                    db.deleteSQL("AttendanceDetail",
                            "Tid=" + bmodel.QT(nonFeildBo.getTid()), false);
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("deleteNonField exception", e);
            return false;
        }

        return true;
    }


    public boolean checkMenuAttendCS(Context context) {

        DBUtil db = null;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT * from HhtMenuMaster where MenuType ="
                            + bmodel.QT("HOME_MENU") + " AND HHTCode = " + bmodel.QT("MENU_PRESENCE"));
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

    public boolean checkMenuInOut(Context context) {
        DBUtil db = null;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
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

        DBUtil db = null;
        try {
            db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT uid , date , intime , outtime , remarks , rowid,reasonid from AttendanceTimeDetails where date ="
                            + bmodel.QT((SDUtil.now(SDUtil.DATE_GLOBAL))) + "and userid=" + userid);
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

		/*NonFieldTwoBo nonFieldTwoBo = new NonFieldTwoBo();
        nonFieldTwoBo.setId(1);
		nonFieldTwoBo.setFromDate("03/18/2016");
		nonFieldTwoBo.setOutTime("11:33:34");
		nonFieldTwoBo.setInTime("");
		nonFieldTwoBo.setReasonId(7793);

		NonFieldTwoBo nonFieldTwoBo1 = new NonFieldTwoBo();
		nonFieldTwoBo1.setId(2);
		nonFieldTwoBo1.setFromDate("03/18/2016");
		nonFieldTwoBo1.setOutTime("12:33:45");
		nonFieldTwoBo1.setInTime("");
		nonFieldTwoBo1.setReasonId(7793);

		getNonFieldTwoBoList().add(nonFieldTwoBo);
		getNonFieldTwoBoList().add(nonFieldTwoBo1);*/


    }

    public void addNonFieldTwoWorkDetails(NonFieldTwoBo nonFieldTwoBo) {
        getNonFieldTwoBoList().add(nonFieldTwoBo);
    }

    public void saveNonFieldWorkTwoDetail(NonFieldTwoBo nonFieldTwoBo, Context context) {

        if (nonFieldTwoBo != null && nonFieldTwoBo.getId() != null) {
            DBUtil db = null;
            try {
                db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();

                int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

                String inTime = nonFieldTwoBo.getInTime() != null ? nonFieldTwoBo.getInTime() : " ";

                String columns = "uid,date,intime,outtime,reasonid,userid,latitude,longitude,counterid,upload";
                String value = bmodel.QT(nonFieldTwoBo.getId()) + ","
                        + bmodel.QT(nonFieldTwoBo.getFromDate()) + ","
                        + bmodel.QT(inTime) + ","
                        + nonFieldTwoBo.getReason() + "," + userid + ","
                        + bmodel.QT(LocationUtil.latitude + "") + "," + bmodel.QT(LocationUtil.longitude + "") + "," + bmodel.getCounterId() + "," + bmodel.QT("N");

                db.insertSQL("AttendanceTimeDetails", columns, value);

                db.closeDB();
            } catch (Exception e) {

                Commons.printException("loadAttendanceMaster" + e);
            }
        }


        Commons.print("Attendance Helper," + "Save : " + nonFieldTwoBo);
    }

    public void updateNonFieldWorkTwoDetail(NonFieldTwoBo nonFieldTwoBo, Context context) {
        //getNonFieldTwoBoList().add(nonFieldTwoBo);
        Commons.print("Attendance Helper," + "Update  : " + nonFieldTwoBo);

        if (nonFieldTwoBo != null && nonFieldTwoBo.getRowid() > 0) {
            DBUtil db = null;
            try {
                db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
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
            if ((nonFieldTwoBo.getInTime() != null && !nonFieldTwoBo.getInTime().trim().equalsIgnoreCase(""))
                    && (nonFieldTwoBo.getOutTime() != null && !nonFieldTwoBo.getOutTime().trim().equalsIgnoreCase(""))) {
                status = true;
            } else {
                return false;
            }
        }


        return status;
    }

    public ArrayList<NonFieldBO> getRadioButtonNames() {
        return lstRadioBtn;
    }

    public void setRadioButtonNames(ArrayList<NonFieldBO> nonfield) {
        this.lstRadioBtn = nonfield;
    }

    public ArrayList<NonFieldBO> getNonFieldReasonList() {
        return nonFieldReasonList;
    }

    public void setNonFieldReasonList(ArrayList<NonFieldBO> nonFieldReasonList) {
        this.nonFieldReasonList = nonFieldReasonList;
    }

    public ArrayList<NonFieldBO> getNonFieldList() {
        return nonFieldList;
    }

    public void setNonFieldList(ArrayList<NonFieldBO> nonFieldList) {
        this.nonFieldList = nonFieldList;
    }

    public ArrayList<NonFieldTwoBo> getNonFieldTwoBoList() {
        return nonFieldTwoBoList;
    }

    public void setNonFieldTwoBoList(ArrayList<NonFieldTwoBo> nonFieldTwoBoList) {
        this.nonFieldTwoBoList = nonFieldTwoBoList;
    }


    public void updateAttendaceDetailInTime(Context context) {
        DBUtil db = null;
        String uid = "";
        for (int i = 0; i < getNonFieldTwoBoList().size(); i++)
            if (getNonFieldTwoBoList().get(i).getInTime().equals(""))
                uid = getNonFieldTwoBoList().get(i).getId();


        db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String updateSql = "update AttendanceTimeDetails " +
                "SET intime= " + bmodel.QT(SDUtil.now(SDUtil.TIME)) +
                " WHERE uid='" + uid + "'";

        db.updateSQL(updateSql);

        db.closeDB();
    }

    public void downLeaveTypes(Context context) {
        try {
            leaveTypeByID = new HashMap<Integer, LeaveSpinnerBO>();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListId,ListName,Flex1 from StandardListMaster where ListType='LEAVE_TYPE'");

            ArrayList<LeaveSpinnerBO> leavesType = new ArrayList<>();
            if (c != null) {
                LeaveSpinnerBO leaveSpinnerBO;
                leaveSpinnerBO = new LeaveSpinnerBO();
                leaveSpinnerBO.setId(0);
                leaveSpinnerBO.setSpinnerTxt(context.getResources().getString(R.string.select));
                leaveSpinnerBO.setFlex("0");
                leaveTypeByID.put(leaveSpinnerBO.getId(), leaveSpinnerBO);
                leavesType.add(leaveSpinnerBO);
                while (c.moveToNext()) {
                    leaveSpinnerBO = new LeaveSpinnerBO();
                    leaveSpinnerBO.setId(c.getInt(0));
                    leaveSpinnerBO.setSpinnerTxt(c.getString(1));
                    leaveSpinnerBO.setFlex(c.getString(2));
                    leaveTypeByID.put(leaveSpinnerBO.getId(), leaveSpinnerBO);
                    leavesType.add(leaveSpinnerBO);
                }
                c.close();
                setLeavesTypeList(leavesType);
            }
            c = null;
            c = db.selectSQL("SELECT WeekOff from AppVariables");
            if (c != null) {
                while (c.moveToNext()) {
                    weekOffDays = c.getString(0).split(",");
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Leaves Type Loading Exception", e);
        }

    }

    public LeaveRuleBO checkRule(int lovId, String fromDate, Context context) {
        LeaveRuleBO leaveRuleBO = new LeaveRuleBO();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT EffectiveTo,NoticeDays from LeaveRule " +
                    "where LeaveType_LovId = " + lovId + " AND " +
                    "EffectiveFrom <= " + bmodel.QT(fromDate) + " AND " +
                    "EffectiveTo >= " + bmodel.QT(fromDate));


            if (c != null) {
                while (c.moveToNext()) {
                    leaveRuleBO = new LeaveRuleBO();
                    leaveRuleBO.setEffectiveTo(c.getString(0));
                    leaveRuleBO.setNoticeDays(c.getInt(1));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("checkRule Exception", e);
        }
        return leaveRuleBO;
    }

    public boolean isHoliday(String date, Context context) {
        boolean isHoliday = false;
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT Description from HolidayMaster " +
                    "where Date  = " + bmodel.QT(date));

            if (c != null) {
                if (c.getCount() > 0) {
                    isHoliday = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("isHoliday Exception", e);
            isHoliday = false;
        }
        return isHoliday;
    }

    public boolean isWeekOff(String date) {
        boolean isWeekOff = false;


        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
            Date selected_date = sdf.parse(date);

            SimpleDateFormat simpleDateformat = new SimpleDateFormat("E");
            String selected_day = simpleDateformat.format(selected_date);

            if (weekOffDays.length > 0)
                for (int i = 0; i < weekOffDays.length; i++) {
                    if (weekOffDays[i].equalsIgnoreCase(selected_day)) {
                        isWeekOff = true;
                        break;
                    }
                }
        } catch (Exception e) {
            Commons.printException(e);
            isWeekOff = false;
        }
        return isWeekOff;
    }

    public void downWeekOffs(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT WeekOff from AppVariables");
            if (c != null) {
                while (c.moveToNext()) {
                    weekOffDays = c.getString(0).split(",");
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Leaves Type Loading Exception", e);
        }

    }

    public void computeLeaves(int lovId, String fromDate, String toDate, int flag, int session, Context context) {
        leavesBo = new ArrayList<LeaveRuleBO>();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT LeaveType_LovId,FrequencyType,EffectiveFrom,EffectiveTo,AllowedDays,NoticeDays,IsAutoApproval from LeaveRule " +
                    "where LeaveType_LovId = " + lovId + " AND " +
                    "((EffectiveFrom BETWEEN " + bmodel.QT(fromDate) + " AND " + bmodel.QT(toDate) + ") OR " +
                    "(EffectiveTo  BETWEEN " + bmodel.QT(fromDate) + " AND " + bmodel.QT(toDate) + ") OR " +
                    " (" + bmodel.QT(fromDate) + " BETWEEN EffectiveFrom  AND EffectiveTo) OR " +
                    " (" + bmodel.QT(toDate) + " BETWEEN EffectiveFrom  AND EffectiveTo))");


            if (c != null) {
                while (c.moveToNext()) {
                    Date startDate = new Date(), endDate = new Date();
                    LeaveRuleBO leaveRuleBO = new LeaveRuleBO();
                    leaveRuleBO.setLeaveTypeLovId(c.getInt(0));
                    leaveRuleBO.setFrequencytype(c.getString(1));
                    leaveRuleBO.setEffectiveFrom(c.getString(2));
                    leaveRuleBO.setEffectiveTo(c.getString(3));
                    leaveRuleBO.setAllowedDays(c.getInt(4));
                    leaveRuleBO.setNoticeDays(c.getInt(5));
                    leaveRuleBO.setAutoApproval(c.getString(6));

                    ArrayList<String> dates = new ArrayList<String>();
                    DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");

                    if (c.getCount() > 1) {
                        if (c.isFirst()) {
                            startDate = (Date) formatter.parse(fromDate);
                            endDate = (Date) formatter.parse(leaveRuleBO.getEffectiveTo());
                        }

                        if (c.isLast()) {
                            startDate = (Date) formatter.parse(leaveRuleBO.getEffectiveFrom());
                            endDate = (Date) formatter.parse(toDate);
                        }
                    } else {
                        startDate = (Date) formatter.parse(fromDate);
                        endDate = (Date) formatter.parse(toDate);
                    }
                    long interval = 24 * 1000 * 60 * 60; // 1 hour in millis
                    long endTime = endDate.getTime(); // create your endtime here, possibly using Calendar or Date
                    long curTime = startDate.getTime();
                    while (curTime <= endTime) {
                        dates.add(formatter.format(new Date(curTime)));
                        curTime += interval;
                    }
                    if (flag == 1) {
                        for (int i = dates.size() - 1; i >= 0; i--) {
                            if (isHoliday(dates.get(i).toString(), context.getApplicationContext()) || isWeekOff(dates.get(i).toString()))
                                dates.remove(i);
                        }

                        double appliedleaves = getAlreadyAppliedLeaves(leaveRuleBO, context);
                        Commons.print("appliedleaves," + "" + appliedleaves);
                        double total_leaves = appliedleaves + dates.size();
                        Commons.print("total_leaves," + "" + total_leaves);
                        if (total_leaves <= leaveRuleBO.getAllowedDays()) {
                            leaveRuleBO.setAppliedDays("" + dates.size());
                            leaveRuleBO.setAvailable(true);
                        } else {
                            leaveRuleBO.setAppliedDays("0");
                            leaveRuleBO.setAvailable(false);
                        }
                    } else {
                        double appliedleaves = getAlreadyAppliedLeaves(leaveRuleBO, context);
                        double session_leave = getSessionLeaveById(session);
                        Commons.print("appliedleaves," + "" + appliedleaves);
                        double total_leaves = appliedleaves + session_leave;
                        Commons.print("total_leaves," + "" + total_leaves);
                        if (total_leaves <= leaveRuleBO.getAllowedDays()) {
                            leaveRuleBO.setAppliedDays("" + session_leave);
                            leaveRuleBO.setAvailable(true);
                        } else {
                            leaveRuleBO.setAppliedDays("0");
                            leaveRuleBO.setAvailable(false);
                        }

                    }
                    leavesBo.add(leaveRuleBO);
                }

                Commons.print("leavesBo," + "" + leavesBo.size());
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("computeLeaves Exception", e);
        }
    }

    private float getAlreadyAppliedLeaves(LeaveRuleBO ruleBO, Context context) {
        float leaves = 0;

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT AD.Tid, LD.ApplyDays from AttendanceDetail AD " +
                    "inner join LeaveDetail LD on LD.Tid  = AD.Tid " +
                    "where AD.LeaveType_LovId = " + ruleBO.getLeaveTypeLovId() + " AND " +
                    "AD.FromDate >= " + bmodel.QT(ruleBO.getEffectiveFrom()) + " AND " +
                    "AD.ToDate <= " + bmodel.QT(ruleBO.getEffectiveTo()) + " AND " +
                    "AD.Status !=" + bmodel.QT("D"));

            if (c != null) {
                while (c.moveToNext()) {
                    leaves += c.getFloat(1);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("applied leaves Exception", e);
            leaves = 0;
        }
        return leaves;
    }

    /**
     * Save Leave Details
     */
    public boolean saveLeaveDetails(double totaldays, int lovid, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String columns = "Tid, DateIn,Atd_ID,ReasonID,FromDate,ToDate,Session,Remarks,Timezone,Status,jointUserId,LeaveType_LovId,TotalDays,userid";

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID) + "";

            for (int i = 0; i < nonFieldList.size(); i++) {
                String status = "R";
                if (leavesBo.get(i).getAutoApproval().equalsIgnoreCase("y"))
                    status = "S";

                String values = bmodel.QT(tid)
                        + ","
                        + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + ","
                        + nonFieldList.get(i).getReasonID()
                        + ","
                        + nonFieldList.get(i).getSubReasonId()
                        + ","
                        + bmodel.QT(nonFieldList.get(i).getFrmDate())
                        + ","
                        + bmodel.QT(nonFieldList.get(i).getToDate())
                        + "," + nonFieldList.get(i).getsessionID() + ","
                        + bmodel.QT(nonFieldList.get(i).getDescription()) + ","
                        + bmodel.QT(bmodel.getTimeZone()) + "," + bmodel.QT(status) + "," + nonFieldList.get(i).getJointUserId() + ","
                        + lovid + "," + totaldays + "," + userid;


                String split_columns = "Tid,StartDate,EndDate,FrequencyType,ApplyDays";

                for (LeaveRuleBO mLeaves : leavesBo) {
                    String split_values = bmodel.QT(tid)
                            + ","
                            + bmodel.QT(mLeaves.getEffectiveFrom())
                            + ","
                            + bmodel.QT(mLeaves.getEffectiveTo())
                            + ","
                            + bmodel.QT(mLeaves.getFrequencytype())
                            + ","
                            + bmodel.QT(mLeaves.getAppliedDays());
                    db.insertSQL("LeaveDetail", split_columns, split_values);
                }


                db.insertSQL("AttendanceDetail", columns, values);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("saveNonFieldDetails" + e);
            return false;
        }

        return true;
    }

    //cmd for to check leave already applied for given date
    public boolean getCheckAlreadyApplied(int atdId, String fromDate, String toDate, int sessionId, Context context) {
        boolean is_applied = false;
        String sesCode = "";
        int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT Tid from AttendanceDetail " +
                    "where userid=" + userid + " AND (" +
                    bmodel.QT(fromDate) + " BETWEEN FromDate AND ToDate " + " OR " +
                    bmodel.QT(toDate) + " BETWEEN FromDate AND ToDate) " +
                    "AND Status !=" + bmodel.QT("D") + " AND Session=" + sessionId);
            if (c != null)
                if (c.getCount() == 1)
                    sesCode = bmodel.getStandardListCode(sessionId);


            Cursor c1 = null;
            StringBuilder sb = new StringBuilder();
            String currentDate = SDUtil.now(SDUtil.DATE_GLOBAL);
            if (sesCode.equals("FN")) {

                sb.append("SELECT Tid,Session from AttendanceDetail " +
                        "where Atd_ID = " + atdId + " AND (" +
                        bmodel.QT(fromDate) + " BETWEEN FromDate AND ToDate " + " OR " +
                        bmodel.QT(toDate) + " BETWEEN FromDate AND ToDate) " +
                        "AND Status !=" + bmodel.QT("D"));

                if (currentDate.equals(fromDate) || currentDate.equals(toDate))
                    sb.append(" AND " + bmodel.QT(SDUtil.now(SDUtil.TIME)) + "<=" + bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF")));
                else
                    sb.append(" AND Session=" + sessionId);

                c1 = db.selectSQL(sb.toString());

                if (c1 != null) {
                    if (c.getCount() > 0) {
                        is_applied = true;
                    }
                    c.close();
                    c1.close();
                }
            } else if (sesCode.equals("AN")) {

                sb.append("SELECT Tid,Session from AttendanceDetail " +
                        "where Atd_ID = " + atdId + " AND (" +
                        bmodel.QT(fromDate) + " BETWEEN FromDate AND ToDate " + " OR " +
                        bmodel.QT(toDate) + " BETWEEN FromDate AND ToDate) " +
                        "AND Status !=" + bmodel.QT("D"));

                if (currentDate.equals(fromDate) || currentDate.equals(toDate))
                    sb.append(" AND " + bmodel.QT(SDUtil.now(SDUtil.TIME)) + ">" + bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF")));
                else
                    sb.append(" AND Session=" + sessionId);

                c1 = db.selectSQL(sb.toString());

                if (c1 != null) {
                    if (c.getCount() > 0) {
                        is_applied = true;
                    }
                    c.close();
                    c1.close();
                }
            } else {
                if (c != null) {
                    if (c.getCount() > 0) {
                        is_applied = true;
                    }
                    c.close();
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("applied leaves Exception", e);
            is_applied = false;
        }
        return is_applied;
    }


    /**
     * To Get Month Name
     *
     * @param date - Date
     * @return Month Name
     */
    String monthName(String date) {
        String monthName = "";
        for (int i = 0; i < dateList.size(); i++) {
            String name = dateList.get(i);
            DateFormat formater1 = new SimpleDateFormat("yyyy/MM/dd");
            DateFormat formater = new SimpleDateFormat("MMM-yyyy");
            try {
                String date1 = formater.format(formater1.parse(date));
                if (date1.equalsIgnoreCase(name)) {
                    return name;
                }
            } catch (ParseException e) {
                e.printStackTrace();
                return monthName;
            }

        }
        return monthName;
    }

    /**
     * To Get List Of Month Name Between Two Date
     *
     * @return - Manoht Name List
     */
    ArrayList<String> getMonthList(Context context) {
        String date1 = getTopDate(context);
        String date2 = getEndDate(context);

        dateList = new ArrayList<>();
        DateFormat formater1 = new SimpleDateFormat("yyyy/MM/dd");
        DateFormat formater = new SimpleDateFormat("MMM-yyyy");

        Calendar beginCalendar = Calendar.getInstance();
        Calendar finishCalendar = Calendar.getInstance();

        try {
            beginCalendar.setTime(formater1.parse(date1));
            finishCalendar.setTime(formater1.parse(date2));
//            date2 = formater.format(formater1.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        while (beginCalendar.before(finishCalendar)) {
            // add one month to date per loop
            String date = formater.format(beginCalendar.getTime()).toUpperCase();
            dateList.add(date);
            beginCalendar.add(Calendar.MONTH, 1);
        }
//        dateList.add(date2);
        return dateList;
    }

    /**
     * To get Highest Date from AttendanceDetail
     *
     * @return -string Highest Date
     */
    String getTopDate(Context context) {
        String date = "";

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT Fromdate FROM AttendanceDetail ORDER BY Fromdate  asc LIMIT 1");


            if (c != null) {
                while (c.moveToNext()) {
                    date = (c.getString(0));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("checkRule Exception", e);
        }


        return date;
    }

    /**
     * To get Lowest Date from AttendanceDetail
     *
     * @return -string Lowest Date
     */
    String getEndDate(Context context) {
        String date = "";

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT Fromdate FROM AttendanceDetail ORDER BY Fromdate  desc LIMIT 1");


            if (c != null) {
                while (c.moveToNext()) {
                    date = (c.getString(0));
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("checkRule Exception", e);
        }


        return date;
    }

    public String changemonthName(String date) {
        String monthName = date;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = new SimpleDateFormat("MMM");
        String month_name = month_date.format(cal.getTime()).toUpperCase();
        if (date.contains(month_name)) {
            monthName = "THIS MONTH";
        }


        cal.add(Calendar.MONTH, -1);
        String previousMonth = new SimpleDateFormat("MMM").format(cal.getTime()).toUpperCase();
        if (date.contains(previousMonth)) {
            monthName = "LAST MONTH";
        }


        return monthName;
    }

    public ArrayList<StandardListBO> loadChildUserList(Context context) {
        ArrayList<StandardListBO> childUserBOs = new ArrayList<>();
        StandardListBO childUserBO;
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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


}
