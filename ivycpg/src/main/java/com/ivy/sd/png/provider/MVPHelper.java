package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.MVPBadgeBO;
import com.ivy.sd.png.bo.MvpBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.List;

public class MVPHelper {

    private final Context mContext;
    private final BusinessModel bmodel;
    private static MVPHelper instance = null;
    private List<MvpBO> mvpDataList;
    private List<MvpBO> mvpBadgeDataList;
    private List<MVPBadgeBO> mvpBadgeInfoList;
    private List<Integer> mvpUserIdList;
    private List<MVPBadgeBO>mMVPBadgeUrlList;

    public List<MvpBO> getMvpDataList() {
        return mvpDataList;
    }

    public List<MvpBO> getMvpBadgeDataList() {
        return mvpBadgeDataList;
    }

    public List<MVPBadgeBO> getMvpBadgeInfoList() {
        return mvpBadgeInfoList;
    }

    private MVPHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static MVPHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MVPHelper(context);
        }
        return instance;
    }

    public void downloadMVPIdBySuperwisorId() {
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuffer sb = new StringBuffer();
            sb.append("select M.userid from MVPReportRanking MVP ");
            sb.append("inner join MVPReportRanking MVP1 on mvp1.UserId=mvp.userid ");
            sb.append("inner join MVPReportRanking m on M.userparentposid=mvp1.userposid ");
            sb.append("where mvp.userid =");
            sb.append(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                mvpUserIdList = new ArrayList<>();
                while (c.moveToNext()) {
                    mvpUserIdList.add(c.getInt(0));
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e+"");
        }
    }

    public List<Integer> getMvpUserIdList() {
        if (mvpUserIdList != null) {
            return mvpUserIdList;
        }
        return new ArrayList<>();
    }

    public void loadMVPData(int userId) {
        try {
            int loopEnd = 0;
            int tempParentID = 0;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            mvpDataList = new ArrayList<>();
            String sql = new String(
                    "select rankid,MVP.userid,MVP.username,SL.Listname as entitylevel,userparentid,rank,"
                            + " totalrank,totalScore,UserPosId,UserParentPosId from MVPReportRanking MVP"
                            + " inner join StandardListMaster SL on Sl.listid = MVP.usertypeLOVID");
            Cursor c = db.selectSQL(sql);
            MvpBO mvp;
            if (c != null) {
                while (c.moveToNext()) {
                    mvp = new MvpBO();

                    mvp.setUserID(c.getInt(1));
                    mvp.setUsername(c.getString(2));
                    mvp.setEntitylevel(c.getString(3));
                    mvp.setRank(c.getInt(5));
                    mvp.setTotalRank(c.getInt(6));
                    mvp.setTotalScore(c.getInt(7));
                    mvp.setUserPosID(c.getInt(8));
                    mvp.setParentPosID(c.getInt(9));

                    mvpDataList.add(mvp);
                }
                c.close();
            }

            mvpBadgeInfoList = new ArrayList<>();
            sql = new String("select badgeid,badgename,imageurl from MVPBadgeMaster");

            c = db.selectSQL(sql);
            MVPBadgeBO mvpBadge;

            if (c != null) {
                while (c.moveToNext()) {
                    mvpBadge = new MVPBadgeBO();
                    mvpBadge.setBadgeID(c.getInt(0));
                    mvpBadge.setBadgeName(c.getString(1));
                    mvpBadge.setBadgeURL(c.getString(2));
                    String[] splitImageName = mvpBadge.getBadgeURL().split("/");
                    if (splitImageName.length > 0) {
                        mvpBadge.setImageName(splitImageName[splitImageName.length - 1]);
                    }
                    mvpBadgeInfoList.add(mvpBadge);
                }
                c.close();
            }

            sql = new String("select mvpb.userid,mbm.Badgeid,count(mbm.Badgeid) from MVPReportRanking mvp"
                    + " inner join  MVPReportBadges mvpb on mvp.userid= mvpb.userid"
                    + " inner join MVPBadgeMaster mbm on mbm.BadgeId = mvpb.Badgeid"
                    + "  group by mvpb.userid,mvpb.Badgeid order by mbm.BadgeId");
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (MvpBO mvpbo : mvpDataList) {
                        if (mvpbo.getUserID() == c.getInt(0)) {
                            mvpbo.getBadgeList().put(c.getInt(1), c.getInt(2));
                        }
                    }
                }
                c.close();
            }

            sql = new String(
                    "select max(Mvp1.UserParentPosId) - min(mvp.UserParentPosId) from MVPReportRanking mvp"
                            + " inner join MVPReportRanking mvp1 on mvp1.userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid());
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    loopEnd = c.getInt(0);
                }
                c.close();
            }

            mvpBadgeDataList = new ArrayList<>();

            for (MvpBO mvpbo : mvpDataList) {
                if (mvpbo.getUserID() == userId) {
                    mvpBadgeDataList.add(mvpbo);
                    tempParentID = mvpbo.getParentPosID();
                    break;
                }
            }

            for (int i = 1; i <= loopEnd; i++) {
                for (MvpBO mvpbo : mvpDataList) {
                    if (mvpbo.getUserPosID() == tempParentID) {
                        mvpBadgeDataList.add(mvpbo);
                        tempParentID = mvpbo.getParentPosID();
                    }
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e+"");
        }
    }

    /**
     * Method to use download badge list
     *
     */
    public void downloadBadgeUrlList(){
        DBUtil db;
        try {
         db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            String query="SELECT Message, ImageUrl, TimeStamp FROM Notification";
            Cursor c=db.selectSQL(query);
            if(c.getCount()>0){
                mMVPBadgeUrlList=new ArrayList<>();
                MVPBadgeBO mvpBadgeBO;
                while(c.moveToNext()){
                    mvpBadgeBO=new MVPBadgeBO();
                    mvpBadgeBO.setBadgeName(c.getString(0));
                    mvpBadgeBO.setBadgeURL(DataMembers.S3_ROOT_DIRECTORY+"/"+c.getString(1));
                    mvpBadgeBO.setTimeStamp(c.getString(2));
                    mMVPBadgeUrlList.add(mvpBadgeBO);
                }
            }
            db.closeDB();
            c.close();
        }catch (Exception e){
            Commons.printException(e+"");
        }
    }

    public List<MVPBadgeBO> getMVPBadgeUrlList(){
        if(mMVPBadgeUrlList!=null){
            return mMVPBadgeUrlList;
        }
        return  new ArrayList<>();
    }
}
