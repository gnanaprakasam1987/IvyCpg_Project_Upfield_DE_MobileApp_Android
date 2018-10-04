package com.ivy.core.data.beat;

import com.ivy.sd.png.bo.BeatMasterBO;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface BeatDataManager {

    Observable<ArrayList<BeatMasterBO>> fetchBeats();

    Observable<ArrayList<BeatMasterBO>> fetchBeatsForUser(int userId);

    Observable<ArrayList<BeatMasterBO>> fetchAdhocPlannedBeats();


}
