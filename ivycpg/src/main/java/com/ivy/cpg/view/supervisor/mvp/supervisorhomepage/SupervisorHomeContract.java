package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;

import android.content.Context;

import com.ivy.cpg.view.supervisor.helper.DetailsBo;

public interface SupervisorHomeContract {

    interface SupervisorHomeView{
        void firebaseLoginFailure();
        void updateSellerCount();
        void updateSellerMarkerInfo(DetailsBo detailsBo);
        void updateSellerFirebaseInfo(DetailsBo detailsBo);

    }

    interface SupervisorHomePresenter{
        void loginToFirebase(Context context,SupervisorHomeView supervisorHomeView);
        void getSellerMarkerInfo(DetailsBo detailsBo);
        void getSellerCount(Context context);
        void getSellerUpdatesFirebase(SupervisorHomeView supervisorHomeView);
    }

}
