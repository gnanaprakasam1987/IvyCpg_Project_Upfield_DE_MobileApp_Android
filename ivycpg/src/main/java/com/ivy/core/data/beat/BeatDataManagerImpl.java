package com.ivy.core.data.beat;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;

public class BeatDataManagerImpl implements BeatDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public BeatDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = dbUtil;
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
    public Observable<ArrayList<BeatMasterBO>> fetchBeats() {
        return Observable.fromCallable(new Callable<ArrayList<BeatMasterBO>>() {
            @Override
            public ArrayList<BeatMasterBO> call() {

                ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();

                try {
                    initDb();
                    BeatMasterBO beat;
                    String beatCols = "BeatID,BeatDescription,today,UserId,BeatCode";

                    Cursor c = mDbUtil.selectSQL("SELECT distinct " + beatCols
                            + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " +
                            "ifnull((SELECT UserId FROM" + DataMembers.tbl_beatMaster +
                            " WHERE UserId=" + appDataProvider.getUser().getUserid() + "),0) Order by BeatDescription asc");
                    if (c != null) {
                        while (c.moveToNext()) {
                            beat = new BeatMasterBO();
                            beat.setBeatId(c.getInt(0));
                            beat.setBeatDescription(c.getString(1));
                            beat.setToday(c.getInt(2));
                            beat.setUserId(c.getInt(3));
                            beat.setBeatCode(c.getString(4));
                            beatMasterBOS.add(beat);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();

                return beatMasterBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<BeatMasterBO>> fetchBeatsForUser(final int userId) {
        return Observable.fromCallable(new Callable<ArrayList<BeatMasterBO>>() {
            @Override
            public ArrayList<BeatMasterBO> call() {

                ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();

                try {
                    initDb();
                    BeatMasterBO beat;
                    Cursor c = mDbUtil.selectSQL("SELECT distinct " + DataMembers.tbl_beatMaster_cols
                            + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " + userId);
                    if (c != null) {
                        while (c.moveToNext()) {
                            beat = new BeatMasterBO();
                            beat.setBeatId(c.getInt(0));
                            beat.setBeatDescription(c.getString(1));
                            beat.setToday(c.getInt(2));
                            beat.setUserId(c.getInt(3));
                            beat.setBeatCode(c.getString(4));
                            beatMasterBOS.add(beat);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();

                return beatMasterBOS;
            }
        });
    }

    @Override
    public Observable<ArrayList<BeatMasterBO>> fetchAdhocPlannedBeats() {
        return Observable.fromCallable(new Callable<ArrayList<BeatMasterBO>>() {
            @Override
            public ArrayList<BeatMasterBO> call() {

                ArrayList<BeatMasterBO> beatMasterBOS = new ArrayList<>();

                try {
                    initDb();
                    BeatMasterBO beat;
                    Cursor c = mDbUtil.selectSQL("select distinct RBM.BeatID, BM.BeatDescription,BM.today from RetailerBeatMapping RBM " +
                            "inner join BeatMaster BM on BM.beatID = RBM.BeatID");
                    if (c != null) {
                        while (c.moveToNext()) {
                            beat = new BeatMasterBO();
                            beat.setBeatId(c.getInt(0));
                            beat.setBeatDescription(c.getString(1));
                            beat.setToday(c.getInt(2));
                            beat.setUserId(c.getInt(3));
                            beat.setBeatCode(c.getString(4));
                            beatMasterBOS.add(beat);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();

                return beatMasterBOS;
            }
        });
    }

    @Override
    public Single<String> fetchBeatsId(final String beatsName) {

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String beatId ="";
                try {
                    initDb();

                    String beatCols = "BeatID";

                    Cursor c = mDbUtil.selectSQL("SELECT distinct " + beatCols
                            + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " +
                            "ifnull((SELECT UserId FROM" + DataMembers.tbl_beatMaster +
                            " WHERE UserId=" + appDataProvider.getUser().getUserid() + "),0) AND BeatDescription = " + beatsName);
                    if (c != null) {
                        while (c.moveToNext()) {

                            beatId = String.valueOf(c.getInt(0));


                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();

                return beatId;

            }
        });

    }

    @Override
    public Single<BeatMasterBO> fetchBeatMaster(final String beatId) {
        return Single.fromCallable(new Callable<BeatMasterBO>() {
            @Override
            public BeatMasterBO call() throws Exception {
                BeatMasterBO beat = null;
                try {
                    initDb();

                    String beatCols = "BeatID,BeatDescription,today,UserId,BeatCode";

                    Cursor c = mDbUtil.selectSQL("SELECT distinct " + beatCols
                            + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " +
                            "ifnull((SELECT UserId FROM" + DataMembers.tbl_beatMaster +
                            " WHERE UserId=" + appDataProvider.getUser().getUserid() + "),0) AND BeatID = "+beatId);
                    if (c != null) {
                        while (c.moveToNext()) {
                            beat = new BeatMasterBO();
                            beat.setBeatId(c.getInt(0));
                            beat.setBeatDescription(c.getString(1));
                            beat.setToday(c.getInt(2));
                            beat.setUserId(c.getInt(3));
                            beat.setBeatCode(c.getString(4));
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                shutDownDb();

                return beat;

            }
        });
    }
}
