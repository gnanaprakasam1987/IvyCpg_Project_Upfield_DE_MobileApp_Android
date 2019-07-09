package com.ivy.ui.announcement;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.ui.announcement.model.AnnouncementBo;

import java.util.ArrayList;
import java.util.HashMap;

public interface AnnouncementContract extends BaseIvyView {

    interface AnnouncementView extends BaseIvyView {

        void onDataNotMappedMsg();

        void updateLabelNames(HashMap<String, String> labelMap);

        void updateListData(ArrayList<AnnouncementBo> announcementBoArrayList);
    }

    @PerActivity
    interface AnnouncementPresenter<V extends AnnouncementView> extends BaseIvyPresenter<V> {

        void fetchData(boolean isFromHomeSrc);
    }
}
