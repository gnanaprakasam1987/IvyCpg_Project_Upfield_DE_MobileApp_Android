package com.ivy.sd.png.provider;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.Map;
import java.util.Vector;

/**
 * Created by ramkumard on 6/2/19.
 * To download data's required to do store related activities.
 */
@SuppressLint("StaticFieldLeak")
public class DownloadProductsAndPrice extends AsyncTask<Integer, Integer, Boolean> {
    private AlertDialog alertDialog;
    private BusinessModel bmodel;
    private Context mContext;
    private String photoPath = "", fnameStarts = "", mVisitMode = "", mNFCReasonId = "";
    private boolean isProfile,isPreVisit;
    private static final String MENU_STK_ORD = "MENU_STK_ORD";

    public DownloadProductsAndPrice(Context context, String photoPath, String fnameStarts, String visitMode,
                                    String nfcReasonId, boolean isFromProfile) {
        this.mContext = context;
        this.photoPath = photoPath;
        this.fnameStarts = fnameStarts;
        this.mVisitMode = visitMode;
        this.mNFCReasonId = nfcReasonId;
        this.isProfile = isFromProfile;
        bmodel = (BusinessModel) mContext.getApplicationContext();
    }

    public void setIsPrevisit(boolean isPreVisit){
        this.isPreVisit = isPreVisit;
    }

    @Override
    protected Boolean doInBackground(Integer... params) {
        try {
            if (!isCancelled()) {

                bmodel.getRetailerWiseSellerType();
                if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && isProfile) {
                    bmodel.configurationMasterHelper.updateConfigurationSelectedSellerType(bmodel.getAppDataProvider().getRetailMaster().getIsVansales() != 1);
                }

                GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_STK_ORD);
                if (genericObjectPair != null) {
                    bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                    bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                }
                bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_STK_ORD));
                bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(
                        bmodel.productHelper.getFilterProductLevels(), true));

                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                    //to reload product filter if diffrent retailer selected
                    bmodel.productHelper.setmLoadedGlobalProductId(0);
                }

                bmodel.configurationMasterHelper
                        .loadOrderAndStockConfiguration(bmodel.retailerMasterBO
                                .getSubchannelid());

                if (bmodel.productHelper.isSBDFilterAvaiable())
                    SBDHelper.getInstance(mContext).loadSBDFocusData(mContext);

                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                    bmodel.batchAllocationHelper.downloadBatchDetails(bmodel
                            .getAppDataProvider().getRetailMaster().getGroupId());
                    bmodel.batchAllocationHelper.downloadProductBatchCount();
                }

                bmodel.productHelper.downloadBomMaster();

                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN
                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                    bmodel.productHelper.downlaodReturnableProducts(MENU_STK_ORD);
                    if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                        bmodel.productHelper.downloadTypeProducts();
                        bmodel.productHelper.downloadGenericProductID();
                    }
                }


                if (!bmodel.configurationMasterHelper.SHEME_NOT_APPLY_DEVIATEDSTORE
                        || (bmodel.getAppDataProvider().getRetailMaster().getIsDeviated() != null && !"Y".equals(bmodel.getAppDataProvider().getRetailMaster().getIsDeviated()))) {

                    SchemeDetailsMasterHelper.getInstance(mContext).initializeScheme(mContext,
                            bmodel.getAppDataProvider().getUser().getUserid(), bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION);

                }

                if (bmodel.configurationMasterHelper.SHOW_DISCOUNT) {
                    bmodel.productHelper.downloadProductDiscountDetails();
                    bmodel.productHelper.downloadDiscountIdListByTypeId();
                }

                if (bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS) {
                    bmodel.productHelper.downloadDocketPricing();
                }

                //Getting Attributes mapped for the retailer
                bmodel.getAttributeHierarchyForRetailer();

                bmodel.reasonHelper.downloadReasons();

            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return Boolean.TRUE;
    }

    protected void onPreExecute() {
        if (!isCancelled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            ((IvyBaseActivityNoActionBar)mContext).customProgressDialog(builder, mContext.getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }
    }

    protected void onProgressUpdate(Integer... progress) {
    }

    protected void onPostExecute(Boolean result) {
        if (!isCancelled()) {
            if (isPreVisit)
                startPreVisit();
            else if (isProfile)
                saveTimeStamp();
            if (alertDialog != null && alertDialog.isShowing())
                alertDialog.dismiss();

        }
    }

    private void saveTimeStamp(){
        float distance = 0.0f;
        try {
            // to get last user visited retailer sequence and location to calculate distance..
            bmodel.outletTimeStampHelper.getlastRetailerDatas();
            distance = calculateDistanceBetweenRetailers();
        } catch (Exception e) {
            Commons.printException(e);
        }

        String date = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
        String time = DateTimeUtils.now(DateTimeUtils.TIME);

        String dateTime = date + " " + time;
        if (bmodel.configurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            dateTime = IvyConstants.DEFAULT_TIME_CONSTANT;
        bmodel.outletTimeStampHelper.setTimeIn(dateTime);
        bmodel.outletTimeStampHelper.setUid(StringUtils.QT("OTS" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID)));


        boolean outletTimeStampSaved = bmodel.outletTimeStampHelper.saveTimeStamp(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), time
                , distance, photoPath, fnameStarts, mVisitMode, mNFCReasonId);

        //set selected retailer location and its used on retailer modules
        bmodel.mSelectedRetailerLatitude = LocationUtil.latitude;
        bmodel.mSelectedRetailerLongitude = LocationUtil.longitude;

        if (bmodel.configurationMasterHelper.SHOW_LOCATION_PASSWORD_DIALOG && (bmodel.configurationMasterHelper.ret_skip_otp_flag == 1
                || bmodel.configurationMasterHelper.ret_skip_otp_flag == 2))
            bmodel.outletTimeStampHelper.updateRetailerDeviationTimeStamp();

        if (outletTimeStampSaved) {
            Intent i = new Intent(mContext, HomeScreenTwo.class);
            i.putExtra("isLocDialog", true);
            i.putExtra("isMandatoryDialog", true);
            i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            mContext.startActivity(i);
            ((ProfileActivity)mContext).finish();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.not_able_to_register_visit), Toast.LENGTH_LONG).show();
        }
    }

    private void startPreVisit(){
        Intent i = new Intent(mContext, HomeScreenTwo.class);
        i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        i.putExtra("PreVisit",true);
        mContext.startActivity(i);
    }

    private float calculateDistanceBetweenRetailers() {
        float notValidLocation = -1;

        if (bmodel.outletTimeStampHelper.getLastRetailerLattitude() != 0 && bmodel.outletTimeStampHelper.getLastRetailerLongitude() != 0 && LocationUtil.latitude != 0 && LocationUtil.longitude != 0) {
            return LocationUtil.calculateDistance(bmodel.outletTimeStampHelper.getLastRetailerLattitude(), bmodel.outletTimeStampHelper.getLastRetailerLongitude());
        }
        return notValidLocation;

    }
}
