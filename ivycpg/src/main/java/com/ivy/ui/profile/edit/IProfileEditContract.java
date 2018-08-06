package com.ivy.ui.profile.edit;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;

import org.jetbrains.annotations.NonNls;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public interface IProfileEditContract {

    interface ProfileEditView extends BaseIvyView{
        void createImageView();

        void createImageView(String path);

        void createImageView(int userId ,String path);

        void createEditTextView(int mNumber,String configCode, String menuName,
                                String values,boolean IS_UPPERCASE_LETTER,
                                int Mandatory,int MAX_CREDIT_DAYS);

        void createCheckBoxView(String isSEZzone,int Mandatory,String menuName);

        void createSpinnerView(Vector<ChannelBO> channelMaster,int mNumber, String MName, String menuCode, int id);

        void createSpinnerView(int mNumber, String MName, String menuCode, int id);

        void createSpinnerView(int mNumber, String MName, String menuCode, int id,int locid );

        void createLatlongTextView( int mNumber, String MName,  String textvalue);

        void showSuccessfullyProfileUpdatedAlert();

        void navigateToProfileScreen();

        void showMessage(String msg);

        void imageViewOnClick(int userId ,String path, boolean hasProfileImagePath);

        void takePhoto(String imageFileName, boolean isForLatLong);

        void setlatlongtextview(String lat,String longitude);

        void addLengthFilter(String regex);  //Regex

        void checkRegex(String regex);

        void updateRetailerFlexValues(ArrayList<RetailerFlexBO> retailerFlexBOArrayList);

        void isLatLongCameravailable(boolean b);

        void getNearbyRetailerIds(Vector<RetailerMasterBO> retailerMasterBOVector);

        void createNearByRetailerView(int mNumber, String MName, boolean isEditMode);

        void getNearbyRetailersEditRequest(Vector<RetailerMasterBO> mSelectedIds);

        void retailersButtonOnClick(Vector<RetailerMasterBO> retailersList,int VALUE_NEARBY_RETAILER_MAX );

        void createPriorityProductView(ArrayList<StandardListBO> mPriorityProductList,String selectedProductID ,
                                       int mNumber, String MName, String textvalue, String productID);

        void createAttributeView(int flag);




    }

    @PerActivity
    interface ProfileEditPresenter<V extends ProfileEditView> extends BaseIvyPresenter<V>{

        void downLoadDataFromDataBase();

        void validateOTP(String type, String value);

        void updateProfile();

        void imageLongClickListener(boolean isForLatLong);

        void latlongCameraBtnClickListene(boolean isForLatLong);

        void imageOnClickListener();

        void isCameraReqestCode();

        Vector<SubchannelBO> getSubChannelMaster();

        int getSubchannelid();

        String getPreviousProfileChangesList(String configCode);

        ArrayList<NewOutletBO> getContractStatusList();

        ArrayList<LocationBO> getLocationMasterList1();

        ArrayList<LocationBO> getLocationMasterList2();

        ArrayList<LocationBO> getLocationMasterList3();

        String[] getParentLevelName(int locid, boolean b);

        String[] getParentLevelName( boolean b);

        void downloadRetailerFlexValues( String type);

        boolean IS_BAIDU_MAP();

        Vector<RetailerMasterBO> getNearByRetailers();

        void setNearByRetailers(Vector<RetailerMasterBO> nearByRetailers);

        void getNearbyRetailerIds();

        void getNearbyRetailersEditRequest();

        void getLinkRetailerListByDistributorId();

        //AttributeView
        void isCommonAttributeView();

        HashMap<Integer, ArrayList<Integer>> getAttributeListByLocationId();


        int getLevel(int attrId);

        ArrayList<NewOutletAttributeBO> getAttributeMapList(String attribName);

        ArrayList<NewOutletAttributeBO> getAttributeList();

        ArrayList<NewOutletAttributeBO> getAttributeListChild();

        ArrayList<NewOutletAttributeBO> getAttributeParentList();

        ArrayList<Integer> getCommonAttributeList();

        ArrayList<Integer> getChannelAttributeList();

    }
}
