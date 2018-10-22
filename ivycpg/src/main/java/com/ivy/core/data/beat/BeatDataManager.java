package com.ivy.core.data.beat;

import com.ivy.sd.png.bo.BeatMasterBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface BeatDataManager {

    Observable<ArrayList<BeatMasterBO>> fetchBeats();

    Observable<ArrayList<BeatMasterBO>> fetchBeatsForUser(int userId);

    Observable<ArrayList<BeatMasterBO>> fetchAdhocPlannedBeats();

    Single<String> fetchBeatsId(String beatsName);

    Single<BeatMasterBO> fetchBeatMaster(String beatId);


}
