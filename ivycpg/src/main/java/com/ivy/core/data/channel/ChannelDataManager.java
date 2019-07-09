package com.ivy.core.data.channel;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.SubchannelBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ChannelDataManager extends AppDataManagerContract {

    Observable<ArrayList<ChannelBO>> fetchChannels();

    Observable<ArrayList<SubchannelBO>> fetchSubChannels();

    Single<String> fetchSubChannelName(String subChannelId);

    Single<String> fetchChannelName(String channelId);

    Single<String> getChannelHierarchyForDiscount(int channelId);

    Single<String> getChannelHierarchy();

    Single<String> getLocationHierarchy();

    Single<String> fetchChannelIds();

    Single<String>getAccountGroupIds();
}
