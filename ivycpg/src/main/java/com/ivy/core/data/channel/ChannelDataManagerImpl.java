package com.ivy.core.data.channel;

import android.database.Cursor;

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

    @Inject
    public ChannelDataManagerImpl(@DataBaseInfo DBUtil dbUtil) {
        this.mDbUtil = dbUtil;
    }

    @Override
    public Observable<Vector<ChannelBO>> fetchChannels() {

        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() {
                int channelId=0;
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 2");
                    if (c.getCount() > 0) {
                        if (c.moveToLast()) {
                            channelId= c.getInt(0);
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
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 1");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            subChannelLevelId= c.getInt(0);
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

                int channelId=0;
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 2");
                    if (c.getCount() > 0) {
                        if (c.moveToLast()) {
                            channelId= c.getInt(0);
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
                        String channelName="";
                        try {
                            Cursor c = mDbUtil.selectSQL("SELECT chName FROM ChannelHierarchy where levelid=" + channelLevelId +" and ChId = "+ channelId);
                            if (c != null) {

                                while (c.moveToNext()) {
                                    channelName=c.getString(0);

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

                int subChannelLevelId=0;
                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();
                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 1");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            subChannelLevelId= c.getInt(0);
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
                        String channelName="";
                        try {
                            Cursor c = mDbUtil.selectSQL("SELECT chid, parentid, chName FROM ChannelHierarchy where levelid=" + subChannelLevelId +" and ChId = "+ subChannelId);
                            if (c != null) {

                                while (c.moveToNext()) {
                                    channelName=c.getString(0);

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
    public Single<String> getChannelHierarchyForDiscount(int channelId) {
        return null;
    }
}
