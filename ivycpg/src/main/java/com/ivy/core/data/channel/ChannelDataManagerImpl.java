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
            public Integer call() throws Exception {

                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 2");
                    int leveid = 0;
                    if (c.getCount() > 0) {
                        if (c.moveToLast()) {
                            return c.getInt(0);
                        }
                    }

                } catch (Exception ignored) {

                }
                return 0;
            }
        }).flatMapObservable(new Function<Integer, ObservableSource<? extends Vector<ChannelBO>>>() {
            @Override
            public ObservableSource<? extends Vector<ChannelBO>> apply(final Integer channelLevelId) throws Exception {
                return Observable.fromCallable(new Callable<Vector<ChannelBO>>() {
                    @Override
                    public Vector<ChannelBO> call() throws Exception {
                        Vector<ChannelBO> channelMaster = new Vector<ChannelBO>();
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
            public Integer call() throws Exception {

                try {
                    mDbUtil.createDataBase();
                    mDbUtil.openDataBase();

                    Cursor c = mDbUtil.selectSQL("select levelid from channellevel order by Sequence desc limit 1");
                    if (c.getCount() > 0) {
                        while (c.moveToNext()) {
                            return c.getInt(0);
                        }
                    }

                } catch (Exception ignored) {

                }
                return 0;
            }
        }).flatMapObservable(new Function<Integer, ObservableSource<? extends Vector<SubchannelBO>>>() {
            @Override
            public ObservableSource<? extends Vector<SubchannelBO>> apply(final Integer subChannelLevelId) throws Exception {
                return Observable.fromCallable(new Callable<Vector<SubchannelBO>>() {
                    @Override
                    public Vector<SubchannelBO> call() throws Exception {
                        Vector<SubchannelBO> channelMaster = new Vector<SubchannelBO>();
                        try {
                            Cursor c = mDbUtil
                                    .selectSQL("SELECT chid, parentid, chName FROM ChannelHierarchy where levelid=" + subChannelLevelId);
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
    public Single<String> fetchChannelName(String channelId) {
        return null;
    }

    @Override
    public Single<String> fetchSubChannelName(String subChannelId) {
        return null;
    }

    @Override
    public Single<String> getChannelHierarchyForDiscount(int channelId) {
        return null;
    }
}
