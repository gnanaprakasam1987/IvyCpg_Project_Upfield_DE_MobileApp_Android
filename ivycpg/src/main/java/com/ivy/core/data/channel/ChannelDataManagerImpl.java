package com.ivy.core.data.channel;

import android.database.Cursor;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class ChannelDataManagerImpl implements ChannelDataManager {

    private DBUtil mDbUtil;

    private DataManager dataManager;
    private int retChannelId = 0;
    private int retLocationId = 0;

    @Inject
    public ChannelDataManagerImpl(@DataBaseInfo DBUtil dbUtil, DataManager dataManager) {
        this.mDbUtil = dbUtil;
        this.dataManager = dataManager;
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
    public Observable<ArrayList<ChannelBO>> fetchChannels() {

        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {
                int channelId = 0;
                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 2");
                    if (c.getCount() > 0) {
                        if (c.moveToLast()) {
                            channelId = c.getInt(0);
                        }
                    }

                } catch (Exception ignored) {

                }
                return channelId;
            }
        }).flatMapObservable(new Function<Integer, ObservableSource<? extends ArrayList<ChannelBO>>>() {
            @Override
            public ObservableSource<? extends ArrayList<ChannelBO>> apply(final Integer channelLevelId) {
                return Observable.fromCallable(new Callable<ArrayList<ChannelBO>>() {
                    @Override
                    public ArrayList<ChannelBO> call() {
                        ArrayList<ChannelBO> channelMaster = new ArrayList<>();
                        try {
                            Cursor c = mDbUtil
                                    .selectSQL("SELECT chid, chName FROM ChannelHierarchy where levelid=" + channelLevelId);
                            if (c != null) {

                                while (c.moveToNext()) {
                                    ChannelBO temp = new ChannelBO();
                                    temp.setChannelId(c.getInt(0));
                                    temp.setChannelName(c.getString(1));
                                    channelMaster.add(temp);
                                }
                                c.close();
                            }

                        } catch (Exception ignored) {

                        }
                        shutDownDb();

                        return channelMaster;
                    }
                });
            }
        });

    }

    @Override
    public Observable<ArrayList<SubchannelBO>> fetchSubChannels() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {

                int subChannelLevelId = 0;
                try {
                    initDb();


                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 1");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            subChannelLevelId = c.getInt(0);
                        }
                    }
                    c.close();

                } catch (Exception ignored) {

                }
                return subChannelLevelId;
            }
        }).flatMapObservable(new Function<Integer, ObservableSource<? extends ArrayList<SubchannelBO>>>() {
            @Override
            public ObservableSource<? extends ArrayList<SubchannelBO>> apply(final Integer subChannelLevelId) {
                return Observable.fromCallable(new Callable<ArrayList<SubchannelBO>>() {
                    @Override
                    public ArrayList<SubchannelBO> call() {
                        ArrayList<SubchannelBO> channelMaster = new ArrayList<>();
                        try {
                            Cursor c = mDbUtil.selectSQL("SELECT chid, parentid, chName FROM ChannelHierarchy where levelid=" + subChannelLevelId);
                            if (c != null) {

                                while (c.moveToNext()) {
                                    SubchannelBO temp = new SubchannelBO();
                                    temp.setSubchannelid(c.getInt(0));
                                    temp.setChannelid(c.getInt(1));
                                    temp.setSubChannelname(c.getString(2));
                                    channelMaster.add(temp);
                                }
                                c.close();
                            }

                        } catch (Exception ignored) {

                        }
                        shutDownDb();

                        return channelMaster;
                    }
                });
            }
        });
    }

    @Override
    public Single<String> fetchChannelName(final String channelId) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {

                int channelId = 0;
                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 2");
                    if (c.getCount() > 0) {
                        if (c.moveToLast()) {
                            channelId = c.getInt(0);
                        }
                    }
                    c.close();
                } catch (Exception ignored) {

                }
                return channelId;
            }
        }).flatMap(new Function<Integer, SingleSource<? extends String>>() {
            @Override
            public SingleSource<? extends String> apply(final Integer channelLevelId) {
                return Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() {
                        String channelName = "";
                        try {
                            Cursor c = mDbUtil.selectSQL("SELECT chName FROM ChannelHierarchy where levelid=" + channelLevelId + " and ChId = " + channelId);
                            if (c != null) {

                                while (c.moveToNext()) {
                                    channelName = c.getString(0);

                                }
                                c.close();
                            }

                        } catch (Exception ignored) {

                        }
                        shutDownDb();
                        return channelName;
                    }
                });
            }
        });
    }

    @Override
    public Single<String> fetchSubChannelName(final String subChannelId) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {

                int subChannelLevelId = 0;
                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 1");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            subChannelLevelId = c.getInt(0);
                        }
                    }
                    c.close();
                } catch (Exception ignored) {
                }
                return subChannelLevelId;
            }
        }).flatMap(new Function<Integer, SingleSource<? extends String>>() {
            @Override
            public SingleSource<? extends String> apply(final Integer subChannelLevelId) {
                return Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() {
                        String channelName = "";
                        try {
                            Cursor c = mDbUtil.selectSQL("SELECT chid, parentid, chName FROM ChannelHierarchy where levelid=" + subChannelLevelId + " and ChId = " + subChannelId);
                            if (c != null) {

                                while (c.moveToNext()) {
                                    channelName = c.getString(0);

                                }
                                c.close();
                            }

                        } catch (Exception ignored) {

                        }
                        shutDownDb();
                        return channelName;
                    }
                });
            }
        });
    }

    @Override
    public Single<String> getChannelHierarchyForDiscount(final int channelId) {
        return null;
    }

    @Override
    public Single<String> getChannelHierarchy() {

        if (dataManager.getRetailMaster() != null)
            retChannelId = dataManager.getRetailMaster().getChannelID();
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {
                int mChildLevel = 0;
                int mContentLevel = 0;
                try {
                    initDb();

                    Cursor c = mDbUtil.selectSQL("select min(Sequence) as childlevel," +
                            "(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + retChannelId + ") " +
                            "as contentlevel  from ChannelLevel");
                    if (c != null) {
                        while (c.moveToNext()) {
                            mChildLevel = c.getInt(0);
                            mContentLevel = c.getInt(1);
                        }
                        c.close();
                    }
                } catch (Exception ignored) {

                }
                return mContentLevel - mChildLevel + 1;
            }
        }).flatMap(new Function<Integer, SingleSource<? extends String>>() {
            @Override
            public SingleSource<? extends String> apply(final Integer loopEnd) throws Exception {

                return Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() {
                        StringBuilder sql;
                        StringBuilder sql1 = new StringBuilder();
                        String str = "";

                        for (int i = 2; i <= loopEnd; i++) {
                            sql1.append(" LM").append(i).append(".ChId");
                            if (i != loopEnd)
                                sql1.append(",");
                        }
                        sql = new StringBuilder("select " + sql1 + "  from ChannelHierarchy LM1");
                        for (int i = 2; i <= loopEnd; i++)
                            sql.append(" INNER JOIN ChannelHierarchy LM").append(i).append(" ON LM").append(i - 1).append(".ParentId = LM").append(i).append(".ChId");
                        sql.append(" where LM1.ChId=").append(retChannelId);

                        try {
                            Cursor c = mDbUtil.selectSQL(sql.toString());

                            while (c.moveToNext()) {
                                for (int i = 0; i < c.getColumnCount(); i++) {
                                    str = str + c.getString(i);
                                    if (c.getColumnCount() > 1 && i != c.getColumnCount())
                                        str = str + ",";
                                }
                                if (str.endsWith(","))
                                    str = str.substring(0, str.length() - 1);
                            }
                            c.close();

                        } catch (Exception ignored) {

                        }
                        shutDownDb();
                        return (str.isEmpty() ? "0" : str);
                    }
                });


            }
        });
    }

    @Override
    public Single<String> getLocationHierarchy() {
        if (dataManager.getRetailMaster() != null)
            retLocationId = dataManager.getRetailMaster().getLocationId();
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int mChildLevel = 0;
                int mContentLevel = 0;
                try {
                    initDb();

                    String sb = "select min(Sequence) as childlevel,(select Sequence from LocationLevel l1 " +
                            "inner join locationmaster lm on l1.id=LM.loclevelid where lm.locid=" +
                            retLocationId +
                            ") as contentlevel  from LocationLevel";

                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c != null) {
                        while (c.moveToNext()) {
                            mChildLevel = c.getInt(0);
                            mContentLevel = c.getInt(1);
                        }
                        c.close();
                    }
                } catch (Exception ignored) {

                }
                return mContentLevel - mChildLevel + 1;
            }
        }).flatMap(new Function<Integer, SingleSource<? extends String>>() {
            @Override
            public SingleSource<? extends String> apply(final Integer loopEnd) throws Exception {
                return Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() {

                        String sql, sql1 = "", str = retLocationId + ",";
                        try {
                            for (int i = 2; i <= loopEnd; i++) {
                                sql1 = sql1 + " LM" + i + ".Locid";
                                if (i != loopEnd)
                                    sql1 = sql1 + ",";
                            }
                            sql = "select " + sql1 + "  from LocationMaster LM1";
                            for (int i = 2; i <= loopEnd; i++)
                                sql = sql + " INNER JOIN LocationMaster LM" + i + " ON LM" + (i - 1)
                                        + ".LocParentId = LM" + i + ".LocId";
                            sql = sql + " where LM1.LocId=" + retLocationId;
                            Cursor c = mDbUtil.selectSQL(sql);
                            if (c != null) {
                                while (c.moveToNext()) {
                                    for (int i = 0; i < c.getColumnCount(); i++) {
                                        str = str + c.getString(i);
                                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                                            str = str + ",";
                                    }
                                }
                            }

                            assert c != null;
                            c.close();

                        } catch (Exception ignored) {

                        }

                        if (str.endsWith(","))
                            str = str.substring(0, str.length() - 1);

                        shutDownDb();
                        return str;
                    }
                });
            }
        });

    }

    @Override
    public Single<String> fetchChannelIds() {


        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                String sql;
                String sql1 = "";
                String str = "";
                int channelid = 0;

                try {
                    initDb();

                    if (dataManager.getRetailMaster() != null)
                        channelid = dataManager.getRetailMaster().getSubchannelid();

                    int mChildLevel = 0;
                    int mContentLevel = 0;
                    Cursor c = mDbUtil.selectSQL("select min(Sequence) as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelid + ") as contentlevel  from ChannelLevel");
                    if (c != null) {
                        while (c.moveToNext()) {
                            mChildLevel = c.getInt(0);
                            mContentLevel = c.getInt(1);
                        }
                        c.close();
                    }

                    int loopEnd = mContentLevel - mChildLevel + 1;

                    for (int i = 2; i <= loopEnd; i++) {
                        sql1 = sql1 + " LM" + i + ".ChId";
                        if (i != loopEnd)
                            sql1 = sql1 + ",";
                    }
                    sql = "select LM1.ChId," + sql1 + "  from ChannelHierarchy LM1";
                    for (int i = 2; i <= loopEnd; i++)
                        sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                                + ".ParentId = LM" + i + ".ChId";
                    sql = sql + " where LM1.ChId=" + channelid;
                    c = mDbUtil.selectSQL(sql);
                    if (c != null) {
                        while (c.moveToNext()) {
                            for (int i = 0; i < c.getColumnCount(); i++) {
                                str = str + c.getString(i);
                                if (c.getColumnCount() > 1 && i != c.getColumnCount())
                                    str = str + ",";
                            }
                            if (str.endsWith(","))
                                str = str.substring(0, str.length() - 1);
                        }
                        c.close();
                    }

                } catch (Exception ignored) {

                }

                return str;
            }
        });


    }

    /**
     * To check whether the retailer mapped in account group for survey
     *
     * @return - account Group ID
     */
    @Override
    public Single<String> getAccountGroupIds() {

        return Single.fromCallable(new Callable<String>() {
            @Override
            public String call() throws Exception {
                try {
                    initDb();
                    StringBuilder accountGroupId = new StringBuilder();
                    String retailerId = "0";

                    if (dataManager.getRetailMaster() != null)
                        retailerId = dataManager.getRetailMaster().getRetailerID();

                    Cursor c = mDbUtil.selectSQL("select groupid from AccountGroupDetail where retailerid=" + retailerId);
                    if (c != null) {
                        if (c.moveToNext()) {
                            if (accountGroupId.length() > 0)
                                accountGroupId.append(",");
                            accountGroupId.append(c.getString(0));
                        }
                        c.close();
                    }
                    shutDownDb();
                    return accountGroupId.toString();
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return "";
            }
        });
    }

    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();

    }
}
