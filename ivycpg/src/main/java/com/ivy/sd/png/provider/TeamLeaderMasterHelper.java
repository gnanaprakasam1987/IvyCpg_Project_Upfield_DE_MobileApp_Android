package com.ivy.sd.png.provider;

/**
 * Created by rajesh.k on 28-04-2016.
 */


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.TaskAssignBO;
import com.ivy.sd.png.bo.TeamLeadBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

public class TeamLeaderMasterHelper {
    private static final String TAG = "Team Leader Helper";
    private BusinessModel bmodel;
    private Context context;

    private static TeamLeaderMasterHelper instance = null;


    private ArrayList<String> mMerchandiserAbsenteesIDList;
    private ArrayList<TaskAssignBO> mAbsenteesList;



    private HashMap<String, ArrayList<TaskAssignBO>> mTaskAbsenteesListBymerchandiserID;
    private HashMap<String, String> mMerchandiserNameByUserID;

    private HashMap<String, UserMasterBO> mMerchandiserBOByUserID = new HashMap<String, UserMasterBO>();






    /*****************************************************/

    private ArrayList<TeamLeadBO> mUserList;

    protected TeamLeaderMasterHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static TeamLeaderMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TeamLeaderMasterHelper(context);

        }
        return instance;
    }

    /**
     * get Merchandiser name from location master
     * depends on locationid get  merchandiser details

     */






    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }
    /**
     * download present merchandiser data
     * if Atd_id=1 in Attendance detales
     */



    public void downloadAbsenteesMerchandiser() {

        mAbsenteesList = new ArrayList<TaskAssignBO>();
        mMerchandiserAbsenteesIDList = new ArrayList<String>();
        mTaskAbsenteesListBymerchandiserID = new HashMap<String, ArrayList<TaskAssignBO>>();
        try {
            TaskAssignBO taskAssignBO;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );

            db.createDataBase();
            db.openDataBase();
            String query = "select userid,retailerid,retailerName from ReallocationPlannedStoreList";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                String merchandiserID = "";
                ArrayList<TaskAssignBO> taskList = new ArrayList<TaskAssignBO>();
                while (c.moveToNext()) {
                    taskAssignBO = new TaskAssignBO();
                    taskAssignBO.setUseid(c.getString(0));
                    taskAssignBO.setRetailerID(c.getInt(1));
                    taskAssignBO.setRetailerName(c.getString(2));

                    //taskAssignBO.setIsDedicated(c.getInt(3));
                    mAbsenteesList.add(taskAssignBO);
                    // mMerchandiserAbsenteesIDList.add(taskAssignBO.getUseid());
                    if (!merchandiserID.equals(taskAssignBO.getUseid())) {
                        if (merchandiserID != "") {
                            mMerchandiserAbsenteesIDList.add(merchandiserID);
                            mTaskAbsenteesListBymerchandiserID.put(
                                    merchandiserID, taskList);
                            taskList = new ArrayList<TaskAssignBO>();
                            taskList.add(taskAssignBO);
                            merchandiserID = taskAssignBO.getUseid();

                        } else {
                            taskList.add(taskAssignBO);
                            merchandiserID = taskAssignBO.getUseid();

                        }
                    } else {
                        taskList.add(taskAssignBO);
                    }

                }
                if (taskList.size() > 0) {
                    mMerchandiserAbsenteesIDList.add(merchandiserID);
                    mTaskAbsenteesListBymerchandiserID.put(merchandiserID,
                            taskList);
                }
            }

            c.close();
            db.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("TL Exception ", e);

        }
    }


    public ArrayList<String> getAbsenteesMerchandiserIDList() {
        if (mMerchandiserAbsenteesIDList != null) {
            return mMerchandiserAbsenteesIDList;

        }
        return new ArrayList<String>();
    }


    public HashMap<String, ArrayList<TaskAssignBO>> getAbsentTaskListByMerchandiserID() {
        return mTaskAbsenteesListBymerchandiserID;
    }

    public ArrayList<TaskAssignBO> getAbsenteesList() {
        if (mAbsenteesList != null) {
            return mAbsenteesList;
        }
        return new ArrayList<TaskAssignBO>();
    }

    public void downloadMerchandiser() {
        UserMasterBO userBo;
        mMerchandiserNameByUserID = new HashMap<String, String>();
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );

            db.createDataBase();
            db.openDataBase();
            String query = "select userid,username from usermaster where isDeviceUser=0";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    userBo = new UserMasterBO();
                    userBo.setUserid(c.getInt(0));
                    userBo.setUserName(c.getString(1));

                    mMerchandiserNameByUserID.put(c.getString(0),
                            c.getString(1));
                    mMerchandiserBOByUserID
                            .put(userBo.getUserid() + "", userBo);
                }

            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("TL Exception ", e);
        }
    }


    public HashMap<String, UserMasterBO> getMerchandiserBOByUserID() {
        return mMerchandiserBOByUserID;
    }

    public HashMap<String, String> getMerchandiserNameByUserID() {
        return mMerchandiserNameByUserID;
    }



    public void deleTLtable() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );

        db.createDataBase();
        db.openDataBase();
        // db.deleteSQL("AttendanceDetail", null, true);

        db.closeDB();

    }
    
    /**--------------------------------------------------*/

    public void downloadUserDetails(){
        mUserList=new ArrayList<>();
        TeamLeadBO teamLeadBO;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb=new StringBuffer();
            sb.append("select username,RLU.userid,status,storecount from ReallocationUserDetails RLU ");
            sb.append(" inner join usermaster um on  RLU.userid=um.userid");
            Cursor c=db.selectSQL(sb.toString());
            if(c.getCount()>0){
                while(c.moveToNext()){
                    teamLeadBO=new TeamLeadBO();
                    teamLeadBO.setUserName(c.getString(0));
                    teamLeadBO.setUserID(c.getString(1));
                    teamLeadBO.setStatus(c.getString(2));
                    teamLeadBO.setRetailerCount(c.getInt(3));
                    mUserList.add(teamLeadBO);

                }
            }
            c.close();
        }catch (Exception e){
            Commons.print(e.getMessage());
        }finally {
            db.closeDB();
        }


    }
    public ArrayList<TeamLeadBO> getUserList(){
        if(mUserList!=null){
            return mUserList;
        }
        return new ArrayList<>();
    }

    public void saveReAllocation(ArrayList<String> presentList,HashMap<String,ArrayList<TaskAssignBO>> assignedTaskListByUserId){
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.createDataBase();
            db.openDataBase();

            db.deleteSQL(DataMembers.tbl_ReallocationHeader,null,true);
            db.deleteSQL(DataMembers.tbl_Reallocationdetail,null,true);

            if(presentList!=null){
                StringBuffer headerBuffer;
                StringBuffer detailBuffer;
                for(String presentid:presentList){
                    ArrayList<TaskAssignBO> assignedTaskList=assignedTaskListByUserId.get(presentid);
                    if(assignedTaskList!=null) {
                        String tid = QT("TL"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + SDUtil.now(SDUtil.DATE_TIME_ID));
                        headerBuffer=new StringBuffer();
                        headerBuffer.append(tid+","+bmodel.userMasterHelper.getUserMasterBO().getUserid()+",");
                        headerBuffer.append(presentid+","+ bmodel.QT(SDUtil.now(SDUtil.DATE)));
                        headerBuffer.append(","+bmodel.QT(Utils.getGMTDateTime("yyyy/MM/dd HH:mm:ss")));

                        db.insertSQL(DataMembers.tbl_ReallocationHeader,DataMembers.tbl_ReallocationHeader_Cols,headerBuffer.toString());
                        for(TaskAssignBO taskAssignBO:assignedTaskList){
                            detailBuffer=new StringBuffer();
                            detailBuffer.append(tid+","+bmodel.QT(taskAssignBO.getRetailerID()+""));
                            detailBuffer.append(","+taskAssignBO.getUseid()+","+presentid);
                            db.insertSQL(DataMembers.tbl_Reallocationdetail,DataMembers.tbl_Reallocationdetail_Cols,detailBuffer.toString());
                        }
                    }



                }
            }






        }catch (Exception e){
            Commons.printException(e);
        }finally {
            db.closeDB();
        }




    }



}

