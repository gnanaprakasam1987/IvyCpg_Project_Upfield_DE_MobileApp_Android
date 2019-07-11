package com.ivy.cpg.view.sync;

import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.SyncRetailerBO;

import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar on 16/3/18.
 */

public interface SyncContractor {

    interface SyncPresenter {

        boolean isValidUser(String userName, String password);

        boolean isDayClosed();

        Vector<NonproductivereasonBO> getMissedCallRetailers();

        boolean isOdameterON();

        int getImageFilesCount();

        int getTextFilesCount();

        StringBuilder getVisitedRetailerId();

        void loadRetailerSelectionScreen();

        void updateDayCloseStatus(boolean isDayClosed);

        void updateIsWithImageStatus(boolean isWithImage);

        void validateAndUpload(boolean isDayCloseChecked);

        void upload();

        void uploadImages();

        void prepareSelectedRetailerIds();

        boolean checkDataForSync();

    }

    interface SyncView {
        void showAttendanceNotCompletedToast();

        void showNoInternetToast();

        void showOrderExistWithoutInvoice();

        void showNoDataExist();

        void showAlertNoUnSubmittedOrder();


        void showProgressLoading();

        void showProgressUploading();

        void cancelProgress();

        void showRetailerSelectionScreen(List<SyncRetailerBO> isVisitedRetailerList);

        void showAlertImageUploadRecommended();
    }
}
