package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;

import android.content.Context;

import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;

public interface SupervisorHomeContract {

    interface SupervisorHomeView{
        void firebaseLoginFailure();
        void updateSellerCount();
        void updateSellerMarkerInfo(SupervisorModelBo supervisorModelBo);
        void updateSellerFirebaseInfo(SupervisorModelBo supervisorModelBo);

    }

    interface SupervisorHomePresenter{
        void loginToFirebase(Context context,SupervisorHomeView supervisorHomeView);
        void getSellerMarkerInfo(SupervisorModelBo supervisorModelBo);
        void getSellerCount(Context context);
        void getSellerUpdatesFirebase(SupervisorHomeView supervisorHomeView);
    }

}
