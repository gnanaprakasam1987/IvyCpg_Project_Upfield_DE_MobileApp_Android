package com.ivy.ui.profile.edit;



import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.ui.profile.create.model.ContractStatus;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public interface IProfileEditContract {

    interface ProfileEditView extends BaseIvyView {

        void createImageView();

        void createImageView(String path);

        void createImageView(int userId, String path);

        void createEditTextView(int mNumber, String configCode, String menuName,
                                String values, boolean IS_UPPERCASE_LETTER,
                                int mandatory, int MAX_CREDIT_DAYS);

        void createCheckBoxView(String isSEZzone, int mandatory, String menuName);

        void createSpinnerView(int number, String menuName, String menuCode, int id);

        void createSpinnerView(int mNumber, String MName, String menuCode, int id, int locid);

        void createLatlongTextView(int mNumber, String MName, String textvalue);

        void showSuccessfullyProfileUpdatedAlert();

        void navigateToProfileScreen();

        void profileEditShowMessage(int resouceId, String msg);

        void imageViewOnClick(int userId, String path, boolean hasProfileImagePath);

        void takePhoto(String imageFileName, boolean isForLatLong);

        void setlatlongtextview(String lat, String longitude);

        void updateRetailerFlexValues(ArrayList<RetailerFlexBO> retailerFlexBOArrayList, String menuCode, String MName);

        void isLatLongCameravailable(boolean b);

        void getNearbyRetailerIds(Vector<RetailerMasterBO> retailerMasterBOVector);

        void createNearByRetailerView(int mNumber, String MName, boolean isEditMode);

        void getNearbyRetailersEditRequest(Vector<RetailerMasterBO> mSelectedIds);

        void retailersButtonOnClick(Vector<RetailerMasterBO> retailersList, int VALUE_NEARBY_RETAILER_MAX);

        void createPriorityProductView(ArrayList<StandardListBO> mPriorityProductList, String selectedProductID,
                                       int mNumber, String MName, String textvalue, String productID);

        void createAttributeView(int flag);

        void createDrugLicenseExpDate(String mName, int mNumber, String data);

        void createFoodLicenceExpDate(String mName, int mNumber, String data);

        String getChennalSelectedItem();

        void setChennalFocus();

        String getSubChennalSelectedItem();

        int getSubChennalSelectedItemId();

        void setSubChennalFocus();

        String getDynamicEditTextValues(int mNumber);

        void setDynamicEditTextFocus(int mNumber);

        HashMap<Integer, NewOutletAttributeBO> getSelectedAttribList();

        int subChannelGetSelectedItem();

        Vector<RetailerMasterBO> getSelectedIds();

        ChannelBO getChennalSelectedItemBO();

        int getContractSpinnerSelectedItemListId();

        int getLocation1SelectedItemLocId();

        int getLocation2SelectedItemLocId();

        int getLocation3SelectedItemLocId();

        RetailerFlexBO getRField4SpinnerSelectedItem();

        RetailerFlexBO getRField5SpinnerSelectedItem();

        RetailerFlexBO getRField6SpinnerSelectedItem();

        RetailerFlexBO getRField7SpinnerSelectedItem();

        boolean getSEZcheckBoxCheckedValues();

        String getFoodLicenceExpDateValue();

        String getDrugLicenceExpDateValue();

        ArrayList<StandardListBO> getSelectedPriorityProductList();

        void showAlert();

        void addLengthFilter(String regex);

        void checkRegex(String regex);

    }

    @PerActivity
    interface ProfileEditPresenter<V extends ProfileEditView> extends BaseIvyPresenter<V> {

        void getProfileEditDataFromLocalDataBase();

        void getImageLongClickListener(boolean isForLatLong);

        void getImageOnClickListener();

        void getLatLongCameraBtnClickListene(boolean isForLatLong);

        void getCameraReqestCode();

        Vector<SubchannelBO> getSubChannelMaster();

        int getSubchannelid();

        String getPreviousProfileChangesList(String configCode);

        ArrayList<ContractStatus> getContractStatusList(String listName);

        ArrayList<LocationBO> getLocationMasterList1(String locationName);

        ArrayList<LocationBO> getLocationMasterList2(String locationName);

        ArrayList<LocationBO> getLocationMasterList3(String locationName);

        String[] getParentLevelName(int locid, boolean b);

        String[] getParentLevelName(boolean b);

        void downloadRetailerFlexValues(String type, String menuCode, String MName);

        boolean IS_BAIDU_MAP();

        Vector<RetailerMasterBO> getNearByRetailers();

        void setNearByRetailers(Vector<RetailerMasterBO> nearByRetailers);

        void getNearbyRetailerIds();

        void getNearbyRetailersEditRequest();

        void getLinkRetailerListByDistributorId();

        //AttributeView
        void checkIsCommonAttributeView();

        HashMap<Integer, ArrayList<Integer>> getAttributeListByLocationId();

        int getLevel(int attrId);

        ArrayList<NewOutletAttributeBO> getAttributeMapList(String attribName);

        ArrayList<NewOutletAttributeBO> getAttributeList();

        ArrayList<NewOutletAttributeBO> getAttributeListChild();

        ArrayList<NewOutletAttributeBO> getAttributeParentList();

        ArrayList<Integer> getCommonAttributeList();

        ArrayList<Integer> getChannelAttributeList();

        void saveUpdatedProfileEdit();

        void verifyOTP(String mType, String mValue);

        Vector<ChannelBO> getChannelMaster();

        void validateOTP(String type, String value);

        void updateProfile();

        void updateLatLong(String lat, String longitude);

        boolean checkRegex(int menuNumber, String typedText);

    }
}
