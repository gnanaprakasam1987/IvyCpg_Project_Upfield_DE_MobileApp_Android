package com.ivy.ui.announcement.data;

import android.database.Cursor;

import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.announcement.model.AnnouncementBo;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Single;
import io.reactivex.functions.Function;
import io.reactivex.functions.Function3;

public class AnnouncementDataManagerImpl implements AnnouncementDataManager {
    private DBUtil mDbUtil;
    private DataManager dataManager;
    private ChannelDataManager channelDataManager;
    private String channelId = "";
    private String locationId = "";
    private String accountGrpId = "";


    @Inject
    AnnouncementDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, DataManager dataManager, @ChannelInfo ChannelDataManager channelDataManager) {
        this.mDbUtil = mDbUtil;
        this.dataManager = dataManager;
        this.channelDataManager = channelDataManager;

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
    public Observable<ArrayList<AnnouncementBo>> fetchAnnouncementData(boolean isFromHomeSrc) {
        return Single.zip(channelDataManager.getChannelHierarchy(),
                channelDataManager.getLocationHierarchy(),
                channelDataManager.getAccountGroupIds(),

                new Function3<String, String, String, Object>() {
                    @Override
                    public Object apply(String channelIds, String locationIds, String accountGroupIds) throws Exception {
                        channelId = channelIds + "," + (dataManager.getRetailMaster() != null ? String.valueOf(dataManager.getRetailMaster().getSubchannelid()) : "0");
                        locationId = locationIds;
                        accountGrpId = accountGroupIds;
                        return true;
                    }
                }).flatMapObservable(new Function<Object, ObservableSource<? extends ArrayList<AnnouncementBo>>>() {
            @Override
            public ObservableSource<? extends ArrayList<AnnouncementBo>> apply(Object o) throws Exception {
                return Observable.fromCallable(new Callable<ArrayList<AnnouncementBo>>() {
                    @Override
                    public ArrayList<AnnouncementBo> call() throws Exception {
                        try {
                            initDb();
                            String query;
                            Cursor c;
                            AnnouncementBo announcementBo;
                            ArrayList<AnnouncementBo> announcementBoArrayList = new ArrayList<>();

                            if (isFromHomeSrc) {
                                query = "Select ANT.Description,ANT.Type,ANT.Date from Announcement ANT " +
                                        "inner join AnnouncementCriteriaMapping ACM on ANT.ID=ACM.ID where ACM.CriteriaType='SELLER'";
                                c = mDbUtil.selectSQL(query);

                                if (c != null) {
                                    while (c.moveToNext()) {
                                        announcementBo = new AnnouncementBo();
                                        announcementBo.setDescription(c.getString(0));
                                        announcementBo.setType(c.getString(1));
                                        announcementBo.setDate(c.getString(2));
                                        announcementBoArrayList.add(announcementBo);
                                    }
                                    c.close();
                                }
                            } else {
                                query = "Select DISTINCT ANT.Description,ANT.Type,ANT.Date from Announcement ANT " +
                                        " inner join AnnouncementCriteriaMapping ACM on ANT.ID=ACM.ID" +
                                        " Where (ACM.CriteriaId in(" + channelId + ") AND (ACM.CriteriaType='CHANNEL' OR ACM.CriteriaType='SUBCHANNEL'))" +
                                        " OR (ACM.CriteriaId in(" + locationId + ") AND ACM.CriteriaType='LOCATION')" +
                                        " OR (ACM.CriteriaId in(" + accountGrpId + ") AND ACM.CriteriaType='ACCOUNT_GROUP')" +
                                        " OR (ACM.CriteriaId in(" + dataManager.getRetailMaster().getRetailerID() + ") AND ACM.CriteriaType='RETAILER')";

                                c = mDbUtil.selectSQL(query);

                                if (c != null) {
                                    while (c.moveToNext()) {
                                        announcementBo = new AnnouncementBo();
                                        announcementBo.setDescription(c.getString(0));
                                        announcementBo.setType(c.getString(1));
                                        announcementBo.setDate(c.getString(2));
                                        announcementBoArrayList.add(announcementBo);
                                    }
                                    c.close();
                                }
                            }
                            shutDownDb();
                            return announcementBoArrayList;

                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                        return new ArrayList<>();
                    }
                });
            }
        });
    }

    @Override
    public void tearDown() {
        if (mDbUtil != null)
            mDbUtil.closeDB();
    }
}
