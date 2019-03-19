package com.ivy.cpg.view.nonfield;


import android.content.Context;
import android.database.Cursor;
import android.util.SparseArray;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LeaveRuleBO;
import com.ivy.sd.png.bo.LeaveSpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

/**
 * @author mansoor
 */
public class NonFieldHelper {
    private BusinessModel bmodel;
    private static NonFieldHelper instance = null;
    private ArrayList<NonFieldBO> nonFieldReasonList = new ArrayList<>();
    private SparseArray<NonFieldBO> reasonBOByreasonID = new SparseArray<>();
    private SparseArray<LeaveSpinnerBO> leaveTypeByID = new SparseArray<>();
    private ArrayList<LeaveSpinnerBO> leavesList = new ArrayList<>();
    private String[] weekOffDays = new String[7];
    private ArrayList<NonFieldBO> lstRadioBtn = new ArrayList<>();
    private ArrayList<NonFieldBO> nonFieldList = new ArrayList<>();
    private ArrayList<String> dateList;
    private ArrayList<LeaveRuleBO> leavesBo = new ArrayList<>();

    private NonFieldHelper(Context context) {
        bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static NonFieldHelper getInstance(Context context) {
        if (instance == null) {
            instance = new NonFieldHelper(context);
        }
        return instance;
    }

    /**
     * Down load Non fiels Reasons
     */
    public void downNonFieldReasons(Context context) {
        try {
            reasonBOByreasonID = new SparseArray<>();
            nonFieldReasonList.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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

    public String getReasonBOByReasonID(int n) {
        NonFieldBO reasonBo = reasonBOByreasonID.get(n);
        if (reasonBo != null) {
            return reasonBo.getReason();
        }
        return null;
    }

    public ArrayList<NonFieldBO> getNonFieldReasonList() {
        return nonFieldReasonList;
    }

    public ArrayList<LeaveSpinnerBO> getLeavesTypeList() {
        return leavesList;
    }

    private void setNonFieldReasonList(ArrayList<NonFieldBO> nonFieldReasonList) {
        this.nonFieldReasonList = nonFieldReasonList;
    }


    public String getLeaveTypeByID(int n) {
        LeaveSpinnerBO typeBo = leaveTypeByID.get(n);
        if (typeBo != null) {
            return typeBo.getSpinnerTxt();
        }
        return null;
    }

    private void setLeavesTypeList(ArrayList<LeaveSpinnerBO> leavesList) {
        this.leavesList = leavesList;
    }


    public void downLeaveTypes(Context context) {
        try {
            leaveTypeByID = new SparseArray<>();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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

    /**
     * Download Session Types
     */
    public void dynamicRadioButtton(Context context) {
        try {
            lstRadioBtn.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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

    public ArrayList<NonFieldBO> getRadioButtonNames() {
        return lstRadioBtn;
    }

    public ArrayList<NonFieldBO> getNonFieldList() {
        return nonFieldList;
    }

    public void setNonFieldList(ArrayList<NonFieldBO> nonFieldList) {
        this.nonFieldList = nonFieldList;
    }

    /**
     * Save Non field Work Details
     */
    public void saveNonFieldWorkDetails(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "Tid, DateIn,Atd_ID,ReasonID, FromDate, ToDate, Session, Remarks,Timezone,Status,jointUserId,LeaveType_LovId,TotalDays,TimeSpent,userid";

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + "";

            for (int i = 0; i < nonFieldList.size(); i++) {

                String values = bmodel.QT(tid)
                        + ","
                        + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
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
                        + bmodel.QT(DateTimeUtils.getTimeZone()) + "," + "'R'," + nonFieldList.get(i).getJointUserId() + ","
                        + nonFieldList.get(i).getLeaveLovId() + "," + nonFieldList.get(i).getTotalDays() + "," + bmodel.QT(nonFieldList.get(i).getTimeSpent()) + "," + userid;

                db.insertSQL("AttendanceDetail", columns, values);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("saveNonFieldDetails" + e);
        }

    }


    /**
     * Down load Non Field Details After saving add details
     */
    public void downloadNonFieldDetails(Context context) {
        getMonthList(context);
        try {
            nonFieldList.clear();
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
     * To Get List Of Month Name Between Two Date
     */
    private void getMonthList(Context context) {
        String date1 = getTopDate(context);
        String date2 = getEndDate(context);

        dateList = new ArrayList<>();
        DateFormat formater1 = DateTimeUtils.getDateFormat("yyyy/MM/dd");
        DateFormat formater = DateTimeUtils.getDateFormat("MMM-yyyy");

        Calendar beginCalendar = Calendar.getInstance();
        Calendar finishCalendar = Calendar.getInstance();

        try {
            beginCalendar.setTime(formater1.parse(date1));
            finishCalendar.setTime(formater1.parse(date2));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        while (beginCalendar.before(finishCalendar)) {
            // add one month to date per loop
            String date = formater.format(beginCalendar.getTime()).toUpperCase();
            dateList.add(date);
            beginCalendar.add(Calendar.MONTH, 1);
        }
    }

    /**
     * To get Highest Date from AttendanceDetail
     *
     * @return -string Highest Date
     */
    private String getTopDate(Context context) {
        String date = "";

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
    private String getEndDate(Context context) {
        String date = "";

        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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

    /**
     * To Get Month Name
     *
     * @param date - Date
     * @return Month Name
     */
    private String monthName(String date) {
        String monthName = "";
        for (int i = 0; i < dateList.size(); i++) {
            String name = dateList.get(i);
            DateFormat formater1 = DateTimeUtils.getDateFormat("yyyy/MM/dd");
            DateFormat formater = DateTimeUtils.getDateFormat("MMM-yyyy");
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
     */
    public void deleteNonfield(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME);
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
        }
    }

    public ArrayList<LeaveRuleBO> getLeavesBo() {
        return leavesBo;
    }

    public LeaveRuleBO checkRule(int lovId, String fromDate, Context context) {
        LeaveRuleBO leaveRuleBO = new LeaveRuleBO();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
            SimpleDateFormat sdf = DateTimeUtils.getDateFormat("yyyy/MM/dd");
            Date selected_date = sdf.parse(date);

            SimpleDateFormat simpleDateformat = DateTimeUtils.getDateFormat("E");
            String selected_day = simpleDateformat.format(selected_date);

            if (weekOffDays.length > 0)
                for (String s : weekOffDays)
                    if (s.equalsIgnoreCase(selected_day)) {
                        isWeekOff = true;
                        break;
                    }

        } catch (Exception e) {
            Commons.printException(e);
            isWeekOff = false;
        }
        return isWeekOff;
    }

    public void downWeekOffs(Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
        leavesBo = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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

                    ArrayList<String> dates = new ArrayList<>();
                    DateFormat formatter = DateTimeUtils.getDateFormat("yyyy/MM/dd");

                    if (c.getCount() > 1) {
                        if (c.isFirst()) {
                            startDate = formatter.parse(fromDate);
                            endDate = formatter.parse(leaveRuleBO.getEffectiveTo());
                        }

                        if (c.isLast()) {
                            startDate = formatter.parse(leaveRuleBO.getEffectiveFrom());
                            endDate = formatter.parse(toDate);
                        }
                    } else {
                        startDate = formatter.parse(fromDate);
                        endDate = formatter.parse(toDate);
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
                            if (isHoliday(dates.get(i), context.getApplicationContext()) || isWeekOff(dates.get(i)))
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
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
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
    public void saveLeaveDetails(double totaldays, int lovid, Context context) {
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String columns = "Tid, DateIn,Atd_ID,ReasonID,FromDate,ToDate,Session,Remarks,Timezone,Status,jointUserId,LeaveType_LovId,TotalDays,userid";

            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();

            String tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + "";

            for (int i = 0; i < nonFieldList.size(); i++) {
                String status = "R";
                if (leavesBo.get(i).getAutoApproval().equalsIgnoreCase("y"))
                    status = "S";

                String values = bmodel.QT(tid)
                        + ","
                        + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
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
                        + bmodel.QT(DateTimeUtils.getTimeZone()) + "," + bmodel.QT(status) + "," + nonFieldList.get(i).getJointUserId() + ","
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
        }
    }

    //cmd for to check leave already applied for given date
    public boolean getCheckAlreadyApplied(int atdId, String fromDate, String toDate, int sessionId, Context context) {
        boolean is_applied = false;
        String sesCode = "";
        int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
        try {
            DBUtil db = new DBUtil(context.getApplicationContext(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT Tid from AttendanceDetail " +
                    "where userid=" + userid + " AND (" +
                    bmodel.QT(fromDate) + " BETWEEN FromDate AND ToDate " + " OR " +
                    bmodel.QT(toDate) + " BETWEEN FromDate AND ToDate) " +
                    "AND Status !=" + bmodel.QT("D") + " AND Session=" + sessionId);
            if (c != null)
                if (c.getCount() == 1)
                    sesCode = bmodel.getStandardListCode(sessionId);


            Cursor c1;
            String currentDate = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
            String condition, query;
            switch (sesCode) {
                case "FN":

                    if (currentDate.equals(fromDate) || currentDate.equals(toDate))
                        condition = " AND " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.TIME)) + "<=" + bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF"));
                    else
                        condition = " AND Session=" + sessionId;

                    query = "SELECT Tid,Session from AttendanceDetail " +
                            "where Atd_ID = " + atdId + " AND (" +
                            bmodel.QT(fromDate) + " BETWEEN FromDate AND ToDate " + " OR " +
                            bmodel.QT(toDate) + " BETWEEN FromDate AND ToDate) " +
                            "AND Status !=" + bmodel.QT("D") + condition;

                    c1 = db.selectSQL(query);

                    if (c1 != null) {
                        if (c.getCount() > 0) {
                            is_applied = true;
                        }
                        c.close();
                        c1.close();
                    }
                    break;
                case "AN":

                    if (currentDate.equals(fromDate) || currentDate.equals(toDate))
                        condition = " AND " + bmodel.QT(DateTimeUtils.now(DateTimeUtils.TIME)) + ">" + bmodel.QT(bmodel.getStandardListNameByCode("ATTENDANCE_CUTOFF"));
                    else
                        condition = " AND Session=" + sessionId;

                    query = "SELECT Tid,Session from AttendanceDetail " +
                            "where Atd_ID = " + atdId + " AND (" +
                            bmodel.QT(fromDate) + " BETWEEN FromDate AND ToDate " + " OR " +
                            bmodel.QT(toDate) + " BETWEEN FromDate AND ToDate) " +
                            "AND Status !=" + bmodel.QT("D") + condition;

                    c1 = db.selectSQL(query);

                    if (c1 != null) {
                        if (c.getCount() > 0) {
                            is_applied = true;
                        }
                        c.close();
                        c1.close();
                    }
                    break;
                default:
                    if (c != null) {
                        if (c.getCount() > 0) {
                            is_applied = true;
                        }
                        c.close();
                    }
                    break;
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("applied leaves Exception", e);
            is_applied = false;
        }
        return is_applied;
    }

    public String changemonthName(String date) {
        String monthName = date;

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat month_date = DateTimeUtils.getDateFormat("MMM");
        String month_name = month_date.format(cal.getTime()).toUpperCase();
        if (date.contains(month_name)) {
            monthName = "THIS MONTH";
        }


        cal.add(Calendar.MONTH, -1);
        String previousMonth = DateTimeUtils.getDateFormat("MMM").format(cal.getTime()).toUpperCase();
        if (date.contains(previousMonth)) {
            monthName = "LAST MONTH";
        }

        return monthName;
    }

}
