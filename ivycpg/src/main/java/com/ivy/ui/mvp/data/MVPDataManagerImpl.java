package com.ivy.ui.mvp.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.mvp.MvpBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

import javax.inject.Inject;

import io.reactivex.Observable;

public class MVPDataManagerImpl implements MVPDataManager {

    private DBUtil mDbUtil;
    private AppDataProvider appDataProvider;

    @Inject
    MVPDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = mDbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }

    @Override
    public Observable<ArrayList<MvpBO>> fetchSellerInfo() {
        return Observable.fromCallable(() -> {
            ArrayList<MvpBO> mvpDataList = new ArrayList<>();
            try {
                initDb();

                String sql = "select distinct rankid,MVP.userid,MVP.username,SL.Listname as entitylevel,userparentid,rank, " +
                        "totalrank,totalScore,UserPosId,UserParentPosId,GroupName, MVPI.ProfileImageURL " +
                        "from MVPReportRanking MVP inner join StandardListMaster SL on Sl.listid = MVP.usertypeLOVID " +
                        "left join MVPUserImageMaster MVPI on MVPI.UserID = MVP.UserID";
                Cursor c = mDbUtil.selectSQL(sql);
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
                        mvp.setGroupName(c.getString(10));
                        mvp.setImageName(c.getString(11));
                        mvpDataList.add(mvp);
                    }
                    c.close();
                }
                shutDownDb();
                return mvpDataList;
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return new ArrayList<MvpBO>();
        });
    }

    @Override
    public Observable<ArrayList<MvpBO>> fetchMvpKpiAchievements() {
        return Observable.fromCallable(() -> {
            ArrayList<MvpBO> mvpKPIList = new ArrayList<>();
            try {
                initDb();

                String sql = "select totalscore,totalrank,mbm.imageurl,(select listname from standardlistmaster where listid=mvp.kpitypeid) " +
                        "as KpiName,GroupName,MVP.UserID,UserName,rank,KpiTypeID, MVPI.ProfileImageURL from MVPReportKPIRanking mvp " +
                        "inner join mvpbadgemaster mbm on mvp.BadgeId = mbm.BadgeId  " +
                        "left join MVPUserImageMaster MVPI on MVPI.UserID = MVP.UserID";
                Cursor c = mDbUtil.selectSQL(sql);
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
                        mvp.setGroupName(c.getString(4));
                        mvp.setUserID(c.getInt(5));
                        mvp.setUsername(c.getString(6));
                        mvp.setRank(c.getInt(7));
                        mvp.setKpiId(c.getString(8));
                        mvp.setImageName(c.getString(9));
                        mvpKPIList.add(mvp);
                    }
                    c.close();
                }
                shutDownDb();
                return mvpKPIList;
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return new ArrayList<MvpBO>();
        });
    }
}
