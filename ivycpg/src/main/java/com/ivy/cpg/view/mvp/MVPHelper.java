package com.ivy.cpg.view.mvp;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
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
    private List<MVPBadgeBO> mMVPBadgeUrlList;
    private List<MvpBO> mvpKPIList;
    private List<MVPToppersBO> mvpToppersList;

    List<MvpBO> getMvpDataList() {
        return mvpDataList;
    }

    List<MvpBO> getMvpBadgeDataList() {
        return mvpBadgeDataList;
    }

    List<MVPBadgeBO> getMvpBadgeInfoList() {
        return mvpBadgeInfoList;
    }

    List<MvpBO> getMvpKPIList() { return mvpKPIList; }

    public List<MVPToppersBO> getMvpToppersList() {
        return mvpToppersList;
    }

    public void setMvpToppersList(List<MVPToppersBO> mvpToppersList) {
        this.mvpToppersList = mvpToppersList;
    }

    private MVPHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static MVPHelper getInstance(Context context) {
        if (instance == null) {
            instance = new MVPHelper(context);
        }
        return instance;
    }

    void downloadMVPIdBySuperwisorId() {
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String sb = "select M.userid from MVPReportRanking MVP " +
                    "inner join MVPReportRanking MVP1 on mvp1.UserId=mvp.userid " +
                    "inner join MVPReportRanking m on M.userparentposid=mvp1.userposid " +
                    "where mvp.userid =" +
                    bmodel.userMasterHelper.getUserMasterBO().getUserid();
            Cursor c = db.selectSQL(sb);
            if (c.getCount() > 0) {
                mvpUserIdList = new ArrayList<>();
                while (c.moveToNext()) {
                    mvpUserIdList.add(c.getInt(0));
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    List<Integer> getMvpUserIdList() {
        if (mvpUserIdList != null) {
            return mvpUserIdList;
        }
        return new ArrayList<>();
    }

    void loadMVPData(int userId) {
        try {
            int loopEnd = 0;
            int tempParentID = 0;

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            mvpDataList = new ArrayList<>();
            String sql = "select rankid,MVP.userid,MVP.username,SL.Listname as entitylevel,userparentid,rank,"
                    + " totalrank,totalScore,UserPosId,UserParentPosId from MVPReportRanking MVP"
                    + " inner join StandardListMaster SL on Sl.listid = MVP.usertypeLOVID";
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
            sql = "select badgeid,badgename,imageurl from MVPBadgeMaster";

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

            sql = "select mvpb.userid,mbm.Badgeid,count(mbm.Badgeid) from MVPReportRanking mvp"
                    + " inner join  MVPReportBadges mvpb on mvp.userid= mvpb.userid"
                    + " inner join MVPBadgeMaster mbm on mbm.BadgeId = mvpb.Badgeid"
                    + "  group by mvpb.userid,mvpb.Badgeid order by mbm.BadgeId";
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

            sql = "select max(Mvp1.UserParentPosId) - min(mvp.UserParentPosId) from MVPReportRanking mvp"
                    + " inner join MVPReportRanking mvp1 on mvp1.userid=" + bmodel.userMasterHelper.getUserMasterBO().getUserid();

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
            Commons.printException(e + "");
        }
    }

    /**
     * Method to use download badge list
     */
    void downloadBadgeUrlList() {
        DBUtil db;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();

            String query = "SELECT Message, ImageUrl, TimeStamp FROM Notification";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                mMVPBadgeUrlList = new ArrayList<>();
                MVPBadgeBO mvpBadgeBO;
                while (c.moveToNext()) {
                    mvpBadgeBO = new MVPBadgeBO();
                    mvpBadgeBO.setBadgeName(c.getString(0));
                    mvpBadgeBO.setBadgeURL(DataMembers.S3_ROOT_DIRECTORY + "/" + c.getString(1));
                    mvpBadgeBO.setTimeStamp(c.getString(2));
                    mMVPBadgeUrlList.add(mvpBadgeBO);
                }
            }
            db.closeDB();
            c.close();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    List<MVPBadgeBO> getMVPBadgeUrlList() {
        if (mMVPBadgeUrlList != null) {
            return mMVPBadgeUrlList;
        }
        return new ArrayList<>();
    }

    /**
     * To load KPI level achievement data
     * @param userId - login user
     */
    public void loadMVPKPIData(int userId) {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            mvpKPIList = new ArrayList<>();
            String sql = "select totalscore,totalrank,mbm.imageurl,(select listname from standardlistmaster where listid=mvp.kpitypeid) as KpiName" +
                    " from MVPReportKPIRanking mvp inner join mvpbadgemaster mbm on mvp.BadgeId = mbm.BadgeId where userid=" + userId;
            Cursor c = db.selectSQL(sql);
            MvpBO mvp;
            if (c != null) {
                while (c.moveToNext()) {
                    mvp = new MvpBO();

                    mvp.setTotalScore(c.getInt(0));
                    mvp.setTotalRank(c.getInt(1));
                    String[] splitPath = c.getString(2).split("/");
                    String imagename = splitPath[splitPath.length - 1];
                    mvp.setBatchURL(imagename);
                    mvp.setKpiName(c.getString(3));

                    mvpKPIList.add(mvp);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * To load Toppers Data in MVP Screen
     */
    public void loadMVPToppersData() {
        try {

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();

            mvpToppersList = new ArrayList<>();
            String sql = "SELECT A.Name, ifnull(A.Score,0) as Score,B.ImageURL,A.Rank,ifnull(A.DistributorName,'') as Distributor," +
                    "ifnull(A.LocationName,'') as Location from MVPToppers A left join MVPBadgeMaster B on A.BadgeID = B.BadgeID order by A.Rank";
            Cursor c = db.selectSQL(sql);
            MVPToppersBO mvp;
            if (c != null) {
                while (c.moveToNext()) {
                    mvp = new MVPToppersBO();

                    mvp.setName(c.getString(0));
                    mvp.setScore(c.getInt(1));

                    String imageUrl= c.getString(2);
                    int index = imageUrl.lastIndexOf('/');
                    String imageName = "";
                    if (index >= 0) {
                        imageName = imageUrl.substring(index + 1);
                    }
                    mvp.setBadge(imageName);
                    mvp.setRank(c.getInt(3));
                    mvp.setDistributorname(c.getString(4));
                    mvp.setLocationname(c.getString(5));

                    mvpToppersList.add(mvp);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setMvpToppersList(mvpToppersList);
    }
}

