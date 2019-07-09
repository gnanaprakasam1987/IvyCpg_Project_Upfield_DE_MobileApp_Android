package com.ivy.ui.announcement.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.ui.announcement.model.AnnouncementBo;

import java.util.ArrayList;

import io.reactivex.Observable;

public interface AnnouncementDataManager extends AppDataManagerContract {


    Observable<ArrayList<AnnouncementBo>> fetchAnnouncementData(boolean isFromHomeSrc);
}
