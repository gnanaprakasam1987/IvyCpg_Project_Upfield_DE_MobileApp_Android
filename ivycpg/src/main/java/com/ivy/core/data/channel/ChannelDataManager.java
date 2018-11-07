package com.ivy.core.data.channel;

import android.content.Context;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.SubchannelBO;

import java.util.Vector;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface ChannelDataManager extends AppDataManagerContract{

    Observable<Vector<ChannelBO>> fetchChannels();

    Observable<Vector<SubchannelBO>> fetchSubChannels();

    Single<String> fetchSubChannelName(String subChannelId);

    Single<String> fetchChannelName(String channelId);

    Single<String> getChannelHierarchyForDiscount(int channelId);

    Single<String> getChannelHierarchy(int channelId);

    Single<String> getLocationHierarchy();

    Single<String> fetchChannelIds();
}
