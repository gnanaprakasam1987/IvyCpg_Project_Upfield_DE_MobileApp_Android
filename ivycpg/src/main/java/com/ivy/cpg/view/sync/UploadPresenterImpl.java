package com.ivy.cpg.view.sync;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ivy.cpg.view.attendance.AttendanceHelper;
import com.ivy.cpg.view.delivery.invoice.DeliveryManagementHelper;
import com.ivy.cpg.view.van.vanunload.VanUnLoadModuleHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SyncRetailerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar on 19/3/18.
 * Upload Presenter Implementation
 */

public class UploadPresenterImpl implements SyncContractor.SyncPresenter {
    private BusinessModel mBModel;
    private UploadHelper mUploadHelper;
    private VanUnLoadModuleHelper mVanUnloadHelper;
    private Context mContext;
    private SyncContractor.SyncView view;

    private List<SyncRetailerBO> isVisitedRetailerList;
    private boolean isDayClosed;
    private boolean isWithImage;
    public boolean isFromCallAnalysis;


    public UploadPresenterImpl(Context mContext, BusinessModel mBModel, SyncContractor.SyncView view
            , UploadHelper mUploadHelper, VanUnLoadModuleHelper mVanUnloadHelper) {
        this.mBModel = mBModel;
        this.mUploadHelper = mUploadHelper;
        this.mVanUnloadHelper = mVanUnloadHelper;
        this.mContext = mContext;
        this.view = view;

    }

    @Override
    public boolean isValidUser(String userName, String password) {

        return mBModel.synchronizationHelper.validateUser(userName, password);
    }

    @Override
    public void loadRetailerSelectionScreen() {

    }

    @Override
    public void updateDayCloseStatus(boolean isDayClosed) {
        this.isDayClosed = isDayClosed;
    }

    @Override
    public void updateIsWithImageStatus(boolean isWithImage) {
        this.isWithImage = isWithImage;
    }

    @Override
    public void validateAndUpload(boolean isDayCloseChecked) {

        // CALL05 is set to true and RFild is 1 (kellog's Specific validation)
        if (mBModel.configurationMasterHelper.IS_COLLECTION_MANDATE) {

            StringBuilder retailerIds = new StringBuilder();
            for (RetailerMasterBO retailerMasterBO : mBModel.getRetailerMaster()) {
                if (retailerMasterBO.getRpTypeCode().equalsIgnoreCase("CASH")) {
                    if (retailerIds.length() > 0)
                        retailerIds.append(",");

                    retailerIds.append(retailerMasterBO.getRetailerID());
                }
            }
            if (retailerIds.length() > 0 && mBModel.hasPendingInvoice(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), retailerIds.toString())) {

                Toast.makeText(mContext, mContext.getResources().getString(R.string.collection_mandatory), Toast.LENGTH_SHORT).show();
                return;

            }
        }

        // No internet connection
        if (!mBModel.isOnline()) {
            view.showNoInternetToast();
            return;
        }

        // Order exist without converting to Invoice.
        if (mBModel.configurationMasterHelper.IS_INVOICE
                && mBModel.isOrderExistToCreateInvoiceAll()) {
            view.showOrderExistWithoutInvoice();
            return;
        }


        if (mUploadHelper.checkDataForSync() || mUploadHelper.checkSIHTable()
                || mUploadHelper.checkStockTable()) {

            if (isDayCloseChecked)
                doDayCloseUpdates();

            if (mBModel.configurationMasterHelper.SHOW_SYNC_RETAILER_SELECT) {
                new LoadRetailerIsVisited().execute();
            } else {
                int dbImageCount = mBModel.synchronizationHelper.countImageFiles();
                if (mBModel.configurationMasterHelper.photocount >= 10 && (((double) dbImageCount / mBModel.configurationMasterHelper.photocount) * 100) >= mBModel.configurationMasterHelper.photopercent) {
                    view.showAlertImageUploadRecommended();

                } else {

                    upload();
                }
            }

        } else if ((isWithImage || !mBModel.configurationMasterHelper.IS_SYNC_WITH_IMAGES) && mBModel.synchronizationHelper.countImageFiles() > 0) {
            if (isDayCloseChecked)
                doDayCloseUpdates();
            // If user selected with images or if user section disabled
            // And image count is > 0 then
            uploadImages();
        } else {
            if (isDayCloseChecked) { // To give space for Image upload we are moving this part here...
                doDayCloseUpdates();
                upload();
            } else
                view.showAlertNoUnSubmittedOrder();
        }
    }

    private void doDayCloseUpdates() {
        mBModel.synchronizationHelper.closeDay(1);
        DeliveryManagementHelper deliveryManagementHelper = DeliveryManagementHelper.getInstance(mContext);
        if (deliveryManagementHelper.isDeliveryModuleAvailable()) {
            deliveryManagementHelper.updateNotDeliveryDetails();
        }

        EmptyReconciliationHelper.getInstance(mContext).updateTable();
        if (mBModel.configurationMasterHelper.CALCULATE_UNLOAD) {
            mVanUnloadHelper.vanUnloadAutomatically(mContext.getApplicationContext());
            mVanUnloadHelper.vanUnloadNonSalableAutomatically(mContext.getApplicationContext());
            mVanUnloadHelper.vanUnloadFreeSiHAutomatically(mContext.getApplicationContext());
        }
    }


    class LoadRetailerIsVisited extends AsyncTask<Integer, Integer, Boolean> {


        protected void onPreExecute() {
            view.showProgressLoading();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                isVisitedRetailerList = mBModel.synchronizationHelper.getRetailerIsVisited();
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            view.cancelProgress();
            if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                    && !isDayClosed) {
                view.showRetailerSelectionScreen(isVisitedRetailerList);

            } else {
                upload();
            }

        }

    }

    @Override
    public void upload() {

        if (AttendanceHelper.getInstance(mContext).hasInOutAttendanceEnabled(mContext.getApplicationContext()))
            AttendanceHelper.getInstance(mContext).updateAttendaceDetailInTime(mContext.getApplicationContext());

        view.showProgressUploading();

        if (mUploadHelper.checkSIHTable())
            new MyThread((Activity) mContext, DataMembers.SYNCSIHUPLOAD, isFromCallAnalysis).start();
        else if (mUploadHelper.checkStockTable())
            new MyThread((Activity) mContext, DataMembers.SYNCSTKAPPLYUPLOAD, isFromCallAnalysis).start();
        else if (mBModel.synchronizationHelper.checkLoyaltyPoints())
            new MyThread((Activity) mContext, DataMembers.SYNCLYTYPTUPLOAD, isFromCallAnalysis).start();
        else if (mBModel.synchronizationHelper.checkPickListData())
            new MyThread((Activity) mContext, DataMembers.SYNCPICKLISTUPLOAD, isFromCallAnalysis).start();
        else if (isVisitedRetailerList != null && isVisitedRetailerList.size() > 0
                && !isDayClosed) {
            new MyThread((Activity) mContext, DataMembers.SYNCUPLOADRETAILERWISE, isFromCallAnalysis).start();
        } else if (mBModel.synchronizationHelper.checkOrderDeliveryStatusTable()) {
            new MyThread((Activity) mContext, DataMembers.SYNC_ORDER_DELIVERY_STATUS_UPLOAD, isFromCallAnalysis).start();
        }else if (mBModel.synchronizationHelper.checkTripData())
            new MyThread((Activity) mContext, DataMembers.SYNC_TRIP, isFromCallAnalysis).start();
        else if (mUploadHelper.checkDataForSync()) {
            new MyThread((Activity) mContext, DataMembers.SYNCUPLOAD, isFromCallAnalysis).start();
        } else {
            view.showNoDataExist();
        }
    }

    @Override
    public void uploadImages() {

        if (AttendanceHelper.getInstance(mContext).hasInOutAttendanceEnabled(mContext.getApplicationContext()))
            AttendanceHelper.getInstance(mContext).updateAttendaceDetailInTime(mContext.getApplicationContext());

        view.showProgressUploading();

        if (mBModel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE) {
            new MyThread((Activity) mContext,DataMembers.AZURE_IMAGE_UPLOAD).start();
        }else if (mBModel.configurationMasterHelper.IS_S3_CLOUD_STORAGE) {
            new MyThread((Activity) mContext,
                    DataMembers.AMAZONIMAGE_UPLOAD, isFromCallAnalysis).start();
        }
    }


    @Override
    public boolean isDayClosed() {
        return mBModel.synchronizationHelper.isDayClosed();
    }

    @Override
    public Vector<NonproductivereasonBO> getMissedCallRetailers() {
        return mBModel.getMissedCallRetailers();
    }

    @Override
    public boolean isOdameterON() {
        return mBModel.configurationMasterHelper
                .isOdaMeterOn();
    }

    @Override
    public int getImageFilesCount() {
        return mBModel.synchronizationHelper
                .countImageFiles();
    }

    @Override
    public int getTextFilesCount() {
        return mBModel.synchronizationHelper.countTextFiles();
    }


    @Override
    public StringBuilder getVisitedRetailerId() {
        return mUploadHelper.getVisitedRetailerIds();
    }

    public void setIsVisitedRetailerList(List<SyncRetailerBO> isVisitedRetailerList){
        this.isVisitedRetailerList = isVisitedRetailerList;
    }

    @Override
    public void prepareSelectedRetailerIds() {
        mUploadHelper.setVisitedRetailerIds(new StringBuilder());
        for (SyncRetailerBO sbo : isVisitedRetailerList) {
            //if (sbo.isChecked()) {
            mUploadHelper.getVisitedRetailerIds().append(
                    mBModel.QT(sbo.getRetailerId()));
            mUploadHelper.getVisitedRetailerIds().append(",");
            //}
        }
        if (mUploadHelper.getVisitedRetailerIds() != null && mUploadHelper.getVisitedRetailerIds().toString().length() > 0) {
            mUploadHelper.getVisitedRetailerIds().delete(mUploadHelper.getVisitedRetailerIds().length() - 1, mUploadHelper.getVisitedRetailerIds().length());
        }
    }

    @Override
    public boolean checkDataForSync() {
        return mUploadHelper.checkDataForSync();
    }
}
