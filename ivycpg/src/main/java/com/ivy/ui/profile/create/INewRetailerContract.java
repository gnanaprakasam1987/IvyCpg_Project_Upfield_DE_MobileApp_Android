package com.ivy.ui.profile.create;

import android.widget.ArrayAdapter;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.primarysale.bo.DistributorMasterBO;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.profile.create.model.ContactTitle;
import com.ivy.ui.profile.create.model.ContractStatus;
import com.ivy.ui.profile.create.model.PaymentType;

import java.util.ArrayList;
import java.util.Vector;

public interface INewRetailerContract {

    interface INewRetailerView extends BaseIvyView {

        int getScreenMode();

        int getChannelId();

        String getChannelName();

        String getRetailerId();

        void hideFooterView();

        ArrayList<String> getImageNameList();

        ArrayList<Integer> getImageIdList();

        void setImageNameList(ArrayList<String> imageNameList);

        void setImageIdList(ArrayList<Integer> imageIdList);


        /*  Create Fields  Contract */

        void addLengthFilter(String regex);

        void addRegexFilter(String regex);

        void createNewRetailerDetailsField(int menuNumber, String menuName, boolean mandatory,
                                           boolean isUppercaseLetter, String mConfigCode);

        void createNewRetailerContactPersonOne(int menuNumber, String menuName, boolean mandatory,
                                               boolean isUppercaseLetter, String mConfigCode, boolean isContactTitle);

        void createNewRetailerContactPersonTwo(int mNumber, String mName, boolean mandatory, boolean isUppercaseLetter, String configCode, boolean isContactTitle);


        void createNewRetailerContactType(int menuNumber, String menuName, boolean mandatory,
                                          boolean isUppercaseLetter, String mConfigCode);

        void createNewRetailerContactEmail(int menuNumber, String menuName, boolean mandatory,
                                           boolean isUppercaseLetter, String mConfigCode);

        void createNewRetailerCreditPeriod(int menuNumber, String menuName, boolean mandatory,
                                           boolean isUppercaseLetter, String mConfigCode);

        void createChannelSpinner(boolean mandatory, int menuNumber, String menuName);

        void createSubChannelSpinner(boolean mandatory, int menuNumber, String menuName);

        void createContactSpinner(boolean mandatory, int menuNumber, String menuName, String configCode);

        void createRouteSpinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createLocationSpinner(boolean mandatory, int mNumber, String mName, String configCode, boolean isLocation1);

        void createLocation1Spinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createLocation2Spinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createPaymentType(boolean mandatory, int mNumber, String mName, String configCode);

        void createDistributor(boolean mandatory, int mNumber, String mName, String configCode);

        void createTaxTypeSpinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createClassTypeSpinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createUserSpinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createRField5Spinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createRField6Spinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createRField7Spinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createRField4Spinner(boolean mandatory, int mNumber, String mName, String configCode);

        void createDaysAndWeeks(boolean mandatory);

        void createTinNum(int mNumber, String mName, boolean mandatory, boolean isUppercaseLetter, String configCode);

        void createRFieldEditText(int mNumber, String mName, boolean mandatory, boolean isUppercaseLetter, String configCode);

        void createPinCode(int mNumber, String mName, boolean mandatory, boolean isUppercaseLetter, String configCode);

        void createGstNo(int mNumber, String mName, boolean mandatory, boolean isUppercaseLetter, String configCode);

        void createLatLongTextView(String mName, int mNumber);

        void createTinExpDataTextView(int mNumber, String mName, boolean mandatory);

        void createDrugLicenseExpDataTextView(int mNumber, String mName, boolean mandatory);

        void createFoodLicenceExpDataTextView(int mNumber, String mName, boolean mandatory);

        void createNearByRetailerView(String MName, boolean mandatory);

        void createPriorityProductView(String mName, boolean mandatory, int hasLink);

        void createSezCheckBox(String menuName, boolean mandatory);

        void createRField3(int mNumber, String mName, boolean mandatory, boolean isUppercaseLetter, String configCode);

        /*  Create Fields  Contract End */


        String getContactPersonTitle(boolean option);

        ArrayList<LocationBO> getLocationAdapter();

        ArrayList<LocationBO> getLocationAdapter1();

        ArrayList<LocationBO> getLocationAdapter2();

        ArrayList<BeatMasterBO> getRouteAdapter();


        void updateContactPersonSelectedTitle(int menuNumber, int position, String value, String spinnerKey);

        void updateChannelSelectedItem(ArrayList<SubchannelBO> subChannelMaster, NewOutletBO outlet, int menuNumber);

        void updateRouteSpinnerData(ArrayList<BeatMasterBO> arrayList);

        void showNearByRetailersDialog(ArrayList<RetailerMasterBO> mNearbyRetailerList, int valueNearbyRetailerMax);

        void updatePriorityProductAutoCompleteTextView(String values, boolean enable);

        boolean isWeekChecked();

        boolean isDayChecked();

        int getSpinnerSelectedItemPosition(String key);

        ArrayList<DistributorMasterBO> getDistributorTypeMasterList();

        void showAlertMessage();

        void showSuccessMessage();

        void showDistributorChangedDialog();

        void doFinishActivity();

        void showAlertDialog(int title);

        void callSurveyActivity();

        void MenuCaptureAlert();

        void updateLocationDataBasedOnPinCode(int menuNumber, String locationName);

        /* Get Field Values */

        String getDynamicEditTextValues(int mNumber);

        String getDynamicTextViewValues(int mNumber);

        String getSelectedPriorityProducts();

        String getSelectedNearByRetailers();

        String getContactPersonTitleOption(NewRetailerConstant.ContactTitleOption contactTitleOption);

        RetailerFlexBO getRField5Spinner();

        RetailerFlexBO getRField6Spinner();

        RetailerFlexBO getRField7Spinner();

        RetailerFlexBO getRField4Spinner();

        StandardListBO getTaxTypeSpinner();

        StandardListBO getClassTypeSpinner();

        UserMasterBO getUserSpinner();

        BeatMasterBO getRouteSpinner();

        ChannelBO getChannelSpinnerSelectedItem();

        ContractStatus getContractSpinnerSelectedItem();

        SpinnerBO getSubChannelSpinnerSelectedItem();

        LocationBO getSelectedLocation(String locationConfigName);

        PaymentType getPaymentType();

        String getSelectedDays();

        String getSelectedWeeks();

        String getBeatName();

        boolean isSEZCheckBoxChecked();

        DistributorMasterBO getDistributorSpinnerSelectedItem();

        String getSelectedLatLong();

        /* Get Field Values End*/

        /* Show Error Contracts */

        void showMandatoryErrorMessage(int position,String menu);

        void showPriorityProductsMandatoryMessage(String menuName);

        void showNearByRetailersMandatory(String menuName);

        void setDynamicEditTextFocus(int mNumber);

        void showNoChannelsError();

        void showNoSubChannelsError();

        void showNoLocationsError();

        void showNoBeatsError();

        void showContactMandatoryErrorMessage();

        void showNoUsersError();

        void showPaymentTypeListEmptyError();

        void showDistributorTypeMasterEmptyError();

        void showTaxListEmptyError();

        void showPriorityProductsEmptyError();

        void showClassTypeEmptyError();

        void showEmptyContactStatusError();

        void setRequestFocusWithErrorMessage(String key, String errorMessage);

        void setSpinnerPosition(String configName, int position);

        /* Show Error Contracts End**/

        void onSurveyDeleteSuccess();

        void showServerErrorMessage(String errorCode);

        void showSessionExpiredMessage();

        void showRetailerDownloadFailedMessage();

        void showNoNetworkMessage();

        void navigateToOpportunityProductsScreen();

        void navigateToNewOutletOrderScreen();

        void showLengthMisMatchError(int position, String menuName, int minLength);

        void showInvalidError(int position, String menuName);

        void showInvalidDateError(int position, String menuName);

        void showSelectPlanError(int position);
    }

    interface INewRetailerPresenter<V extends INewRetailerView> extends BaseIvyPresenter<V> {

        void init();

        void getSavedOutletData();

        String getUid();

        Vector<NewOutletBO> getImageTypeList();

        ArrayList<ContactTitle> getContactTitleList();

        ArrayList<ContractStatus> getContractStatusList();

        void loadInitialData();

        String getOutletData(String menuCode);

        void getSelectedContactTitle(int menuNumber, String spinnerKey);

        int getMaxCreditDays();

        ArrayList<ChannelBO> getChannelList();

        void getChannelSelectedItem(int menuNumber);

        ArrayList<SpinnerBO> getSubChannelsForAChannel(int channelIde);

        int getSubChannel();

        void setContractStatusLovId(int id);

        int getSpinnerSelectedItem(String configCode);

        void getSelectedLocation2Position();

        void getSelectedLocation1Position();

        void getSelectedLocationPosition();

        ArrayList<BeatMasterBO> getCurrentUserRoutes();

        ArrayList<LocationBO> getLocationList();

        ArrayList<LocationBO> getLocation1List();

        ArrayList<LocationBO> getLocation2List();

        ArrayList<PaymentType> getRetailerPaymentTypeList();

        ArrayList<DistributorMasterBO> getDistributorTypeMasterList();

        void getRetailerRoutes(String dId);

        ArrayList<RetailerMasterBO> getRetailerMaster();

        ArrayList<BeatMasterBO> getBeatMaster();

        ArrayList<StandardListBO> getTaxTypeList();

        ArrayList<StandardListBO> getClassTypeList();

        ArrayList<UserMasterBO> getUserList();

        ArrayList<RetailerFlexBO> getRField4List();

        ArrayList<RetailerFlexBO> getRField5List();

        ArrayList<RetailerFlexBO> getRField6List();

        ArrayList<RetailerFlexBO> getRField7List();

        UserMasterBO getUserMasterBO();

        NewOutletBO getOutlet();

        String getCurrentRetailerId();

        boolean isBaiduMap();

        double getLatitude();

        void updateLatitude(double latitude);

        double getLongitude();

        void updateLongitude(double longitude);

        void getLinkRetailerList(int distId);

        ArrayList<String> getSelectedRetailers();

        ArrayList<RetailerMasterBO> getNearbyRetailerList();

        ArrayList<String> getDownloadNearByRetailers();

        void downloadPriorityProducts();

        ArrayList<StandardListBO> getPriorityProductMasterList();

        void saveNewRetailer();

        boolean isValidRetailer();

        void downloadRetailerMaster();

        void clearOrdersAndSaveOutlet();

        ConfigurationMasterHelper getConfigurationMasterHelper();

        void getHomeButtonClick();

        void deleteNewRetailerSurvey();

        void onSurveyMenuClick();

        void getMenuCaptureOptionClick();

        void onOrderMenuClick();

        void onOpportunityProductsMenuClicked();

        void loadLocationDataBasedOnPinCode(String pinCode);

        String getPrefLanguage();

        void setOrderedProductList(ArrayList<ProductMasterBO> productList);

        void setOrderHeader(OrderHeader orderHeader);

        void setOpportunityProductList(ArrayList<ProductMasterBO> productList);

    }
}
