package com.ivy.core.data.channel;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.SubchannelBO;

import java.util.Vector;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class ChannelDataManagerImpl implements ChannelDataManager {

    private DBUtil mDbUtil;

    private AppDataProvider appDataProvider;

    @Inject
    public ChannelDataManagerImpl(@DataBaseInfo DBUtil dbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = dbUtil;
        this.appDataProvider = appDataProvider;
        mDbUtil.createDataBase();
    }

    @Override
    public Observable<Vector<ChannelBO>> fetchChannels() {

        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {
                int channelId = 0;
                try {
                    mDbUtil.openDataBase();

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
        }).flatMapObservable(new Function<Integer, ObservableSource<? extends Vector<ChannelBO>>>() {
            @Override
            public ObservableSource<? extends Vector<ChannelBO>> apply(final Integer channelLevelId) {
                return Observable.fromCallable(new Callable<Vector<ChannelBO>>() {
                    @Override
                    public Vector<ChannelBO> call() {
                        Vector<ChannelBO> channelMaster = new Vector<>();
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

                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }

                        return channelMaster;
                    }
                });
            }
        });

    }

    @Override
    public Observable<Vector<SubchannelBO>> fetchSubChannels() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {

                int subChannelLevelId = 0;
                try {

                    mDbUtil.openDataBase();

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
        }).flatMapObservable(new Function<Integer, ObservableSource<? extends Vector<SubchannelBO>>>() {
            @Override
            public ObservableSource<? extends Vector<SubchannelBO>> apply(final Integer subChannelLevelId) {
                return Observable.fromCallable(new Callable<Vector<SubchannelBO>>() {
                    @Override
                    public Vector<SubchannelBO> call() {
                        Vector<SubchannelBO> channelMaster = new Vector<>();
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

                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }

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

                    mDbUtil.openDataBase();
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

                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }
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

                    mDbUtil.openDataBase();
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

                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }
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
    public Single<String> getChannelHierarchy(final int channelId) {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {
                int mChildLevel = 0;
                int mContentLevel = 0;
                try {
                    mDbUtil.openDataBase();

                    Cursor c = mDbUtil.selectSQL("select min(Sequence) as childlevel," +
                            "(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelId + ") " +
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
                        sql.append(" where LM1.ChId=").append(channelId);

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

                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }

                        return str;
                    }
                });


            }
        });
    }

    @Override
    public Single<String> getLocationHierarchy() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                int mChildLevel = 0;
                int mContentLevel = 0;
                try {
                    mDbUtil.openDataBase();

                    String sb = "select min(Sequence) as childlevel,(select Sequence from LocationLevel l1 " +
                            "inner join locationmaster lm on l1.id=LM.loclevelid where lm.locid=" +
                            appDataProvider.getRetailMaster().getLocationId() +
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

                        String sql, sql1 = "", str = appDataProvider.getRetailMaster().getLocationId() + ",";
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
                            sql = sql + " where LM1.LocId=" + appDataProvider.getRetailMaster().getLocationId();
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

                        } finally {
                            if (mDbUtil != null)
                                mDbUtil.closeDB();
                        }

                        if (str.endsWith(","))
                            str = str.substring(0, str.length() - 1);

                        return str;
                    }
                });
            }
        });
    }


}
