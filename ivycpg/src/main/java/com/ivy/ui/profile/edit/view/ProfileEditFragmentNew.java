package com.ivy.ui.profile.edit.view;


import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.view.ContextThemeWrapper;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatEditText;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.location.LocationUtil;
import com.ivy.maplib.BaiduMapDialogue;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletAttributeBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.MapDialogue;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.sd.png.view.RetailerOTPDialog;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.ui.profile.edit.di.DaggerProfileEditComponent;
import com.ivy.ui.profile.edit.di.ProfileEditModule;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;

import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.app.Activity.RESULT_OK;

public class ProfileEditFragmentNew extends BaseFragment implements IProfileEditContract.ProfileEditView ,RetailerOTPDialog.OTPListener {

    @BindView(R.id.profile_edit_scrollview)
    ScrollView mScrollView;

    @BindView(R.id.profile_edit_save)
    Button mButtonSave;

    private LinearLayout mRootLinearLayout = null;
    private LinearLayout.LayoutParams weight1, weight2, weight3;
    private LinearLayout.LayoutParams mcommonsparams = null, params5;
    private LinearLayout.LayoutParams params8;
    private LinearLayout.LayoutParams paramsAttrib, paramsAttribSpinner;
    private LinearLayout.LayoutParams weight0wrap;
    private LinearLayout.LayoutParams weight4;
    private LinearLayout.LayoutParams params6;


    private TextView latlongtextview;
    private TextView nearbyTextView;
    private TextView priorityproducttextview;
    private ImageView latlongCameraBtn;
    private AppCompatCheckBox inSEZcheckBox = null;



    private TextView textview[] = new TextView[100];

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer,AppCompatEditText> editTextHashMap=new HashMap<>();

    private MaterialSpinner channel, subchannel, location1, location2, location3,
            contractSpinner, rField5Spinner, rField6Spinner, rField7Spinner, rField4Spinner;


    private ArrayList<InputFilter> inputFilters = null;
    private Vector<ChannelBO> channelMaster = null;
    private ArrayList<NewOutletBO> mcontractStatusList = null;
    private ArrayList<LocationBO> mLocationMasterList1 = null, mLocationMasterList2 = null, mLocationMasterList3 = null;
    private ArrayAdapter<LocationBO> locationAdapter1 = null, locationAdapter2 = null;
    private Vector<RetailerMasterBO> mSelectedIds = new Vector<>();
    private ArrayList<StandardListBO> mPriorityProductList = null, selectedPriorityProductList = null;
    private HashMap<String, MaterialSpinner> spinnerHashMap = null;
    private HashMap<String, ArrayAdapter<NewOutletAttributeBO>> spinnerAdapterMap = null;
    private ArrayList<NewOutletAttributeBO> attributeHeaderList = null;
    private HashMap<String, ArrayList<ArrayList<NewOutletAttributeBO>>> listHashMap = null;
    private HashMap<String, ArrayList<Integer>> attributeIndexMap = null;
    private HashMap<Integer, NewOutletAttributeBO> selectedAttribList = null;
    private ArrayList<Integer> attributeIndexList = null;

    private int locid = 0;
    private int subChannelSpinnerCount = 0;
    private String MName;
    private String menuCode;
    private int id;
    private String selectedProductID = "";
    static String lat = "", longitude = "";
    private boolean isLatLongCameravailable = false;
    private int spinnerCount = 0;
    private int check = 0;

    /*ProfileImageView */
    private ImageView mProfileImageView = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int LATLONG_CAMERA_REQUEST_CODE = 2;
    private String imageFileName, cameraFilePath = "";

    @SuppressLint("StaticFieldLeak")
    static TextView dlExpDateTextView = null, flExpDateTextView = null;

    @Inject
    IProfileEditContract.ProfileEditPresenter<IProfileEditContract.ProfileEditView> profileEditPresenter;

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_profile_edit;
    }

    @Override
    public void initVariables(View view) {

        params5 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params5.setMargins(0, 0, 20, 0);
        params5.gravity = Gravity.CENTER;

        params8 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params8.gravity = Gravity.CENTER;
        params8.setMargins(0, 0, 0, 0);

        weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.weight = 2;
        weight2.gravity = Gravity.CENTER;

        weight3 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight3.weight = 3;
        weight3.gravity = Gravity.CENTER;

        paramsAttrib = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        paramsAttrib.weight = .7f;
        paramsAttrib.setMargins(0, 0, 10, 0);

        paramsAttribSpinner = new LinearLayout.LayoutParams(0, android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        paramsAttribSpinner.weight = 2.3f;

        weight0wrap= new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight0wrap.setMargins(10, 0, 0, 5);

        weight4 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight4.gravity = Gravity.CENTER;
        weight4.setMargins(30, 0, 0, 0);

        params6 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params6.setMargins(0, 0, 0, 0);
        params6.gravity = Gravity.CENTER;
    }

    @Override
    public void initializeDi() {
        DaggerProfileEditComponent.builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .profileEditModule(new ProfileEditModule(this))
                .build().inject(this);
        setBasePresenter((BasePresenter) profileEditPresenter);
    }

    @Override
    protected void setUpViews() {
        mButtonSave.setTypeface(FontUtils.getFontBalooHai(getContext(), FontUtils.FontType.REGULAR));
        setHasOptionsMenu(true);
        profileEditPresenter.downLoadDataFromDataBase();
        mScrollView.addView(getmRootLinearLayout());
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    public void onDestroy() {
        if (dlExpDateTextView != null)
            dlExpDateTextView = null;
        if (flExpDateTextView != null)
            flExpDateTextView = null;
        super.onDestroy();
    }

    @OnClick(R.id.profile_edit_save)
    public void seveUpdateProfile(){
        profileEditPresenter.saveUpdatedProfileEdit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //bmodel.latlongImageFileName = "";
            getActivity().finish();
        }

        return super.onOptionsItemSelected(item);
    }

    /*Set empty ImageView */
    @Override
    public void createImageView() {
        getmRootLinearLayout().addView(getImageView());
    }

    /*Set Image by URI using path params */
    @Override
    public void createImageView(String path) {
        getmRootLinearLayout().addView(getImageView());
        mScrollView.addView(getmRootLinearLayout());
        String filePath = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + path;
        if (AppUtils.checkImagePresent(filePath)) {
            try {
                Uri uri = AppUtils.getUriFromFile(getActivity(), HomeScreenFragment.photoPath + "/" + path);
                mProfileImageView.invalidate();
                mProfileImageView.setImageURI(uri);
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
    }

    /*Set Image by Bitmap using path and userId */
    @Override
    public void createImageView(int userId, String path) {
        getmRootLinearLayout().addView(getImageView());
        mScrollView.addView(getmRootLinearLayout());
        File imgFile = new File(getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + userId + DataMembers.DIGITAL_CONTENT + "/" + DataMembers.PROFILE + "/" + path);
        if (imgFile.exists()) {
            Bitmap myBitmap = AppUtils.decodeFile(imgFile);
            mProfileImageView.setScaleType(ImageView.ScaleType.FIT_XY);
            mProfileImageView.setAdjustViewBounds(true);
            mProfileImageView.setImageBitmap(myBitmap);
        }
    }

    @Override
    public void imageViewOnClick(int userId, String path, boolean hasProfileImagePath) {
        if (!AppUtils.isEmptyString(cameraFilePath)) {
            if (new File(cameraFilePath).exists())
                openImage(new File(cameraFilePath));
            else
                Toast.makeText(getActivity(), getResources().getString(R.string.unloadimage), Toast.LENGTH_SHORT).show();
        } else {
            File filePath = null;
            if (hasProfileImagePath) {
                filePath = new File(getActivity().getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + path);
            } else {
                filePath = new File(getActivity().getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS)
                        + "/" + userId + DataMembers.DIGITAL_CONTENT + "/" + DataMembers.PROFILE + "/" + path);
            }
            if (filePath.exists())
                openImage(filePath);
            else
                Toast.makeText(getActivity(), getResources().getString(R.string.unloadimage), Toast.LENGTH_SHORT).show();

        }
    }

    @Override
    public void takePhoto(String imageFileName, boolean isForLatLong) {
        this.imageFileName = imageFileName;
        if (AppUtils.isExternalStorageAvailable()) {
            try {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), HomeScreenFragment.photoPath + "/" + ((!isForLatLong) ? imageFileName : AppUtils.latlongImageFileName));
                startActivityForResult(intent, (!isForLatLong) ? CAMERA_REQUEST_CODE : LATLONG_CAMERA_REQUEST_CODE);

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_access_the_sdcard), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @NonNls Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                profileEditPresenter.isCameraReqestCode();
                String path = HomeScreenFragment.photoPath + "/" + imageFileName;
                Uri uri = AppUtils.getUriFromFile(getActivity(), path);
                cameraFilePath = HomeScreenFragment.photoPath + "/" + imageFileName;
                mProfileImageView.setImageDrawable(null);
                mProfileImageView.invalidate();
                mProfileImageView.setImageURI(uri);
                mProfileImageView.refreshDrawableState();
            }
        }
        if (requestCode == LATLONG_CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                //String latlongCameraFilePath = HomeScreenFragment.photoPath + "/" + bmodel.latlongImageFileName;
                Toast.makeText(getActivity(), "Photo Captured Successfully", Toast.LENGTH_LONG).show();
            }
        }
        if (resultCode == RESULT_OK) {
            if (data.hasExtra("lat") && data.hasExtra("isChanged")) {
                lat = data.getExtras().getDouble("lat") + "";
                longitude = data.getExtras().getDouble("lon") + "";
                if (data.getExtras().getBoolean("isChanged")) {
                    latlongtextview.setText(lat + ", " + longitude);
                    if (isLatLongCameravailable) {
                        latlongCameraBtn.setVisibility(View.VISIBLE);
                    }
                }
            }
        }
    }


    @Override
    public void createEditTextView(int mNumber, String configCode, String menuName,
                                   String values, boolean IS_UPPERCASE_LETTER,
                                   int Mandatory, int MAX_CREDIT_DAYS) {
        getmRootLinearLayout().addView(getEditTextView(mNumber, configCode, menuName,
                values, IS_UPPERCASE_LETTER, Mandatory, MAX_CREDIT_DAYS), getCommonsparams());


    }

    @Override
    public void createCheckBoxView(String isSEZzone, int Mandatory, String menuName) {

        getmRootLinearLayout().addView(getCheckBoxView(isSEZzone,Mandatory,menuName),getCommonsparams());
    }

    @Override
    public void createSpinnerView(Vector<ChannelBO> channelMaster, int mNumber, String MName, String menuCode, int id) {
        this.channelMaster = channelMaster;
        getmRootLinearLayout().addView(getSpinnerView(mNumber, MName, menuCode, id), getCommonsparams());
    }


    @Override
    public void createSpinnerView(int mNumber, String MName, String menuCode, int id) {
        getmRootLinearLayout().addView(getSpinnerView(mNumber, MName, menuCode, id), getCommonsparams());
    }

    @Override
    public void createSpinnerView(int mNumber, String MName, String menuCode, int id, int locid) {
        this.locid = locid;
        getmRootLinearLayout().addView(getSpinnerView(mNumber, MName, menuCode, id), getCommonsparams());
    }


    @Override
    public void createLatlongTextView(int mNumber, String MName, String textvalue) {
        getmRootLinearLayout().addView(getLatlongTextView(mNumber, MName, textvalue), getCommonsparams());
    }

    @Override
    public void createNearByRetailerView(int mNumber, String MName, boolean isEditMode) {

        getmRootLinearLayout().addView(getNearByRetailerView(mNumber, MName, isEditMode), getCommonsparams());
    }

    @Override
    public void createPriorityProductView(ArrayList<StandardListBO> mPriorityProductList,
                                          String selectedProductID, int mNumber,
                                          String MName, String textvalue, String productID) {
        this.mPriorityProductList = mPriorityProductList;
        this.selectedProductID = selectedProductID;
        getmRootLinearLayout().addView(getPriorityProductView(mNumber, MName, textvalue, productID), getCommonsparams());
    }

    @Override
    public void createAttributeView(int flag) {
        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        LLParams.setMargins(10, 5, 10, 5);
        getmRootLinearLayout().addView(addAttributeView(flag), LLParams);

    }


    @Override
    public void createDrugLicenseExpDate(String mName,int mNumber,String data) {
        getmRootLinearLayout().addView(getDurgExpDateView(mName,mNumber,data), getCommonsparams());
    }


    @Override
    public void createFoodLicenceExpDate(String mName,int mNumber,String data) {
        getmRootLinearLayout().addView(getFoodExpDateView(mName,mNumber,data), getCommonsparams());
    }


    @Override
    public String getChennalSelectedItem() {
        return channel.getSelectedItem().toString().toLowerCase();
    }

    @Override
    public ArrayList<NewOutletBO> getContractStatusList() {
        return mcontractStatusList;
    }

    @Override
    public int getContractSpinnerSelectedItemListId() {
        return ((NewOutletBO) contractSpinner.getSelectedItem()).getListId();
    }

    @Override
    public int getLocation1SelectedItemLocId() {
        return ((LocationBO) location1.getSelectedItem()).getLocId();
    }



    @Override
    public int getLocation2SelectedItemLocId() {
        return ((LocationBO) location2.getSelectedItem()).getLocId();
    }


    @Override
    public int getLocation3SelectedItemLocId() {
        return ((LocationBO) location3.getSelectedItem()).getLocId();
    }

    @Override
    public RetailerFlexBO getRField4SpinnerSelectedItem() {
        return (RetailerFlexBO) rField4Spinner.getSelectedItem();
    }

    @Override
    public RetailerFlexBO getRField5SpinnerSelectedItem() {
        return (RetailerFlexBO) rField5Spinner.getSelectedItem();
    }

    @Override
    public boolean getSEZcheckBoxCheckedValues() {
        return inSEZcheckBox.isChecked();
    }

    @Override
    public RetailerFlexBO getRField6SpinnerSelectedItem() {
        return (RetailerFlexBO) rField6Spinner.getSelectedItem();
    }

    @Override
    public RetailerFlexBO getRField7SpinnerSelectedItem() {
        return (RetailerFlexBO) rField7Spinner.getSelectedItem();
    }

    @Override
    public String getFoodLicenceExpDateValue() {
        return flExpDateTextView.getText().toString();
    }

    @Override
    public String getDrugLicenceExpDateValue() {
        return dlExpDateTextView.getText().toString();
    }

    @Override
    public void setChennalFocus() {
        channel.requestFocus();
    }

    @Override
    public String getSubChennalSelectedItem() {
        return subchannel.getSelectedItem().toString().toLowerCase();
    }

    @Override
    public int getSubChennalSelectedItemId() {
        return ((SpinnerBO) subchannel.getSelectedItem()).getId();
    }

    @Override
    public void setSubChennalFocus() {
        subchannel.requestFocus();
    }

    @Override
    public ArrayList<StandardListBO> getSelectedPriorityProductList() {
        return selectedPriorityProductList;
    }

    @Override
    public String getDynamicEditTextValues(int mNumber) {
        AppCompatEditText value = editTextHashMap.get(mNumber);
        if (value != null) {
            return value.getText().toString().trim();
        } else {
            if (editTextHashMap.containsKey(mNumber)) {
                // Okay, there's a key but the value is null
            } else {
                // Definitely no such key
            }
            return "";
        }

    }

    @Override
    public void setDynamicEditTextFocus(int mNumber) {
        EditText value = editTextHashMap.get(mNumber);
        if (value != null) {
            value.requestFocus();
        } else {
            // No such key
        }

    }

    @SuppressLint("UseSparseArrays")
    @Override
    public HashMap<Integer, NewOutletAttributeBO> getSelectedAttribList() {
        if(selectedAttribList!=null){
            return  selectedAttribList;
        }
        else selectedAttribList =new HashMap<>();
        return selectedAttribList;
    }

    @Override
    public int subChannelGetSelectedItem() {
        return ((SpinnerBO) subchannel.getSelectedItem()).getId();
    }

    @Override
    public Vector<RetailerMasterBO> getSelectedIds() {
        return mSelectedIds;
    }

    @Override
    public ChannelBO getChennalSelectedItemBO() {
        return   (ChannelBO) channel.getSelectedItem();
    }

    @Override
    public void setlatlongtextview(String lat, String longitude) {
        if (latlongtextview != null)
            latlongtextview.setText(lat + "," + longitude);
    }


    @Override
    public void addLengthFilter(String regex) {
        inputFilters = new ArrayList<>();
        inputFilters.add(AppUtils.getInputFilter(regex));
    }


    @Override
    public void checkRegex(String regex) {
        final String reg;
        try {
            if (regex != null && !regex.isEmpty()) {
                if (regex.contains("<") && regex.contains(">")) {
                    reg = regex.replaceAll("\\<.*?\\>", "");
                } else {
                    reg = regex;
                }
                //data.replaceAll("\\(.*?\\)", "()"); //if you want to keep the brackets
                InputFilter filter = new InputFilter() {
                    public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                        for (int i = start; i < end; i++) {
                            String checkMe = String.valueOf(source.charAt(i));
                            if (!Pattern.compile(reg).matcher(checkMe).matches()) {
                                Log.d("", "invalid");
                                return "";
                            }
                        }
                        return null;
                    }
                };
                inputFilters.add(filter);
            }
        } catch (Exception ex) {
            Commons.printException("regex check", ex);
        }
    }


    @Override
    public void showSuccessfullyProfileUpdatedAlert() {

    }

    @Override
    public void navigateToProfileScreen() {

    }

    @Override
    public void profileEditShowMessage(int resouceId,String msg) {
        Toast.makeText(getActivity(),
                getActivity().getResources().getString(R.string.attribute) + " " + msg, Toast.LENGTH_LONG).show();
    }


    @Override
    public void updateRetailerFlexValues(ArrayList<RetailerFlexBO> retailerFlexBOArrayList) {

        if (menuCode.equalsIgnoreCase(ProfileConstant.RFIELD5)) {
            ArrayAdapter<RetailerFlexBO> rField5Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField5Adapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField5Adapter.add(new RetailerFlexBO("0", getActivity().getResources().getString(R.string.select_str) + " " + MName));
            int selPos = 0;
            for (int i = 0; i < retailerFlexBOArrayList.size(); i++) {
                RetailerFlexBO retBO = retailerFlexBOArrayList.get(i);
                rField5Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }
            rField5Spinner.setAdapter(rField5Adapter);
            rField5Spinner.setSelection(selPos);
            rField5Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }
            });
        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RFIELD6)) {
            ArrayAdapter<RetailerFlexBO> rField6Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField6Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField6Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));

            int selPos = 0;
            for (int i = 0; i < retailerFlexBOArrayList.size(); i++) {
                RetailerFlexBO retBO = retailerFlexBOArrayList.get(i);
                rField6Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }
            rField6Spinner.setAdapter(rField6Adapter);
            rField6Spinner.setSelection(selPos);
            rField6Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {

                }

                public void onNothingSelected(AdapterView<?> arg0) {

                }
            });
        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RFIELD7)) {
            ArrayAdapter<RetailerFlexBO> rField7Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField7Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField7Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));
            int selPos = 0;
            for (int i = 0; i < retailerFlexBOArrayList.size(); i++) {
                RetailerFlexBO retBO = retailerFlexBOArrayList.get(i);
                rField7Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }
            rField7Spinner.setAdapter(rField7Adapter);
            rField7Spinner.setSelection(selPos);
            rField7Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {

                }

                public void onNothingSelected(AdapterView<?> arg0) {

                }

            });
        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RField4)) {
            ArrayAdapter<RetailerFlexBO> rField4Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField4Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField4Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));
            int selPos = 0;
            for (int i = 0; i < retailerFlexBOArrayList.size(); i++) {
                RetailerFlexBO retBO = retailerFlexBOArrayList.get(i);
                rField4Adapter.add(retBO);
                if (id == Integer.valueOf(retBO.getId()))
                    selPos = i + 1;
            }
            rField4Spinner.setAdapter(rField4Adapter);
            rField4Spinner.setSelection(selPos);
            rField4Spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });
        }

    }

    @Override
    public void isLatLongCameravailable(boolean b) {
        isLatLongCameravailable = b;
    }


    @Override
    public void getNearbyRetailerIds(Vector<RetailerMasterBO> retailerMasterBOVector) {
        this.mSelectedIds = retailerMasterBOVector;
    }

    @Override
    public void getNearbyRetailersEditRequest(Vector<RetailerMasterBO> mSelectedIds) {
        this.mSelectedIds.clear();
        this.mSelectedIds = mSelectedIds;
    }

    @Override
    public void retailersButtonOnClick(Vector<RetailerMasterBO> retailersList, int VALUE_NEARBY_RETAILER_MAX) {

        if (retailersList != null && retailersList.size() > 0) {
            NearByRetailerDialog dialog = new NearByRetailerDialog(getActivity(), VALUE_NEARBY_RETAILER_MAX, retailersList, mSelectedIds);
            dialog.show();
            dialog.setCancelable(false);
        }
    }


    private LinearLayout getmRootLinearLayout() {
        if (mRootLinearLayout == null) {
            mRootLinearLayout = new LinearLayout(getActivity());
            mRootLinearLayout.setOrientation(LinearLayout.VERTICAL);
            mRootLinearLayout.setBackgroundDrawable(getActivity().getResources().getDrawable(R.drawable.background_noise));
            return mRootLinearLayout;
        }
        return mRootLinearLayout;
    }


    private View getImageView() {
        View view;
        LayoutInflater inflater = (LayoutInflater) getActivity().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        view = inflater.inflate(R.layout.profile_edit_image_view, null);
        mProfileImageView = (ImageView) view.findViewById(R.id.profile_edit_image);

        mProfileImageView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                //Dont allow if Fun57 is enabled and mandatory,
                //Generally check for location and show toast if no location found.
                profileEditPresenter.imageLongClickListener(false);
                return true;
            }
        });
        mProfileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileEditPresenter.imageOnClickListener();
            }
        });
        return view;
    }


    private LinearLayout createLinearLayout(int oriendation, int resourcesId) {
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(oriendation);
        if (resourcesId != 0) linearlayout.setBackgroundColor(resourcesId);
        return linearlayout;
    }


    private LinearLayout createLinearLayout(int oriendation, int resourcesId, float weightSum) {
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(oriendation);
        linearlayout.setWeightSum(weightSum);
        if (resourcesId != 0) linearlayout.setBackgroundColor(resourcesId);
        return linearlayout;
    }


    private LinearLayout createLinearLayout() {
        return new LinearLayout(getActivity());
    }


    private LinearLayout.LayoutParams getCommonsparams() {
        if (mcommonsparams == null) {
            mcommonsparams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            mcommonsparams.setMargins(10, 15, 10, 0);
            return mcommonsparams;
        }
        return mcommonsparams;
    }


    private LinearLayout getEditTextView(final int mNumber, String mConfigCode, String menuName,
                                         String values, final boolean IS_UPPERCASE_LETTER,
                                         int Mandatory, final int MAX_CREDIT_DAYS) {

        LinearLayout linearlayout = createLinearLayout(LinearLayout.HORIZONTAL,
                getActivity().getResources().getColor(R.color.white_box_start));
        TextInputLayout editTextInputLayout;
        editTextInputLayout = new TextInputLayout(getActivity());
        editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, menuName, values, IS_UPPERCASE_LETTER));

        editTextHashMap.get(mNumber).addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            }

            @Override
            public void afterTextChanged(Editable et) {
                String s = et.toString();
                if (IS_UPPERCASE_LETTER && !s.equals(s.toUpperCase())) {
                    s = s.toUpperCase();
                    editTextHashMap.get(mNumber).setText(s);
                    editTextHashMap.get(mNumber).setSelection(editTextHashMap.get(mNumber).length());
                }
            }
        });

        //if  Email
        if (comparConfigerCode(mConfigCode, ProfileConstant.EMAIL) && Mandatory == 1) {
            LinearLayout emailLayout = createLinearLayout(LinearLayout.HORIZONTAL, 0, 10);
            LinearLayout.LayoutParams emailParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            emailParam.weight = 7;

            LinearLayout.LayoutParams verifyButtonParams = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
            verifyButtonParams.weight = 3;
            verifyButtonParams.setMargins(0, 0, 0, 2);
            verifyButtonParams.gravity = Gravity.BOTTOM;

            emailLayout.addView(editTextInputLayout, emailParam);

            Button verifyBtn = new Button(getActivity());
            verifyBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
            verifyBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR, getActivity()));
            verifyBtn.setText(getResources().getString(R.string.verify));
            verifyBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_bg1));
            verifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    profileEditPresenter.verifyOTP("EMAIL",  editTextHashMap.get(mNumber).getText().toString());
                }
            });
            emailLayout.addView(verifyBtn, verifyButtonParams);
            linearlayout.addView(emailLayout, weight1);
        } else
            linearlayout.addView(editTextInputLayout, weight1);

        /*ContactNumber,PHNO1,PHNO2,MOBILE,FAX*/
        if (comparConfigerCode(mConfigCode, ProfileConstant.CONTACT_NUMBER) ||
                comparConfigerCode(mConfigCode, ProfileConstant.MOBILE) ||
                comparConfigerCode(mConfigCode, ProfileConstant.FAX)) {

            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, menuName, values, IS_UPPERCASE_LETTER));

            if (comparConfigerCode(mConfigCode, ProfileConstant.MOBILE) && Mandatory == 1)  /*MOBILE*/ {

                LinearLayout mobileLayout = createLinearLayout(LinearLayout.HORIZONTAL, 0, 10);
                LinearLayout.LayoutParams mobileParam = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                mobileParam.weight = 7;

                LinearLayout.LayoutParams mobileParam1 = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
                mobileParam1.setMargins(0, 0, 0, 2);
                mobileParam1.weight = 3;
                mobileParam1.gravity = Gravity.BOTTOM;

                mobileLayout.addView(editTextInputLayout, mobileParam);

                Button verifyBtn = new Button(getActivity());
                verifyBtn.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
                verifyBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR, getActivity()));
                verifyBtn.setText(getResources().getString(R.string.verify));
                verifyBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_bg1));
                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        profileEditPresenter.verifyOTP("MOBILE", editTextHashMap.get(mNumber).getText().toString());
                    }
                });

                mobileLayout.addView(verifyBtn, mobileParam1);

                linearlayout.addView(mobileLayout, weight1);
            } else
                linearlayout.addView(editTextInputLayout, weight1);

        }

        //CREDITPERIOD
        if (comparConfigerCode(mConfigCode, ProfileConstant.CREDITPERIOD)) {
            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, menuName, values, IS_UPPERCASE_LETTER));
            editTextHashMap.get(mNumber).addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!qty.equals("")) {
                        if (SDUtil.convertToInt(qty) > MAX_CREDIT_DAYS) {
                            //Delete the last entered number and reset the qty
                            editTextHashMap.get(mNumber).setText(qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0");
                            Toast.makeText(getActivity(), getResources().getString(R.string.max_credit_days_allowed)
                                    + " " + MAX_CREDIT_DAYS, Toast.LENGTH_LONG).show();
                        }
                    }
                }
            });
            linearlayout.addView(editTextInputLayout, weight1);
        }

        return linearlayout;
    }

    private LinearLayout getCheckBoxView(String mSEZzone,int mMandatory,String mMenuName){

        LinearLayout baselayout = new LinearLayout(getActivity());
        baselayout.setOrientation(LinearLayout.VERTICAL);

        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.VERTICAL);

        inSEZcheckBox = new AppCompatCheckBox(getActivity());

        if (mSEZzone.equals("1")) {
            inSEZcheckBox.setChecked(true);
        } else {
            inSEZcheckBox.setChecked(false);
        }
        linearlayout.addView(inSEZcheckBox, weight2);

        LinearLayout textLayout = new LinearLayout(getActivity());
        textLayout.setOrientation(LinearLayout.HORIZONTAL);

        if (mMandatory == 1) {
            LinearLayout.LayoutParams weight0 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            TextView mn_textview = new TextView(getActivity());
            mn_textview.setText("*");
            mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            mn_textview.setTextColor(Color.RED);
            textLayout.addView(mn_textview, weight0);
        }

        TextView days = new TextView(getActivity());
        days.setText(mMenuName);
        days.setTextColor(Color.BLACK);
        days.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        textLayout.addView(days, params8);
        baselayout.addView(textLayout);
        baselayout.addView(linearlayout);

        return baselayout;
    }


    @SuppressLint("RestrictedApi")
    private LinearLayout getFoodExpDateView(String mName,int mNumber,String data){

        LinearLayout secondlayout = new LinearLayout(getActivity());
        LinearLayout firstlayout = new LinearLayout(getActivity());
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout finallayout = new LinearLayout(getActivity());
        finallayout.setOrientation(LinearLayout.HORIZONTAL);
        TextView tv_label = new TextView(getActivity());
        tv_label.setText(mName);
        tv_label.setTextColor(Color.BLACK);
        tv_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        tv_label.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity())) ;
        firstlayout.addView(tv_label, params8);
        flExpDateTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.datePickerButton), null, 0);
        flExpDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        flExpDateTextView.setTextColor(Color.BLACK);
        flExpDateTextView.setGravity(Gravity.CENTER);
        flExpDateTextView.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
        flExpDateTextView.setId(mNumber);
        flExpDateTextView.setTypeface(flExpDateTextView.getTypeface(), Typeface.NORMAL);
        flExpDateTextView.setText(data);
        secondlayout.addView(flExpDateTextView, weight0wrap);
        finallayout.addView(firstlayout, params8);
        finallayout.addView(secondlayout, weight4);
        linearlayout.addView(finallayout, params6);
        flExpDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                 String date = flExpDateTextView.getText().toString();
                if (!date.equalsIgnoreCase("Select Date") && date.contains("/") && date.split("/").length == 3) {
                    year = Integer.valueOf(date.split("/")[0]);
                    month = Integer.valueOf(date.split("/")[1]) - 1;
                    day = Integer.valueOf(date.split("/")[2]);
                }
                DialogFragment newFragment = new DatePickerFragment("FLEXPDATE", year, month, day);
                newFragment.show(getActivity().getSupportFragmentManager(), "flDatePicker");
            }
        });

        return linearlayout;
    }


    @SuppressLint("RestrictedApi")
    private LinearLayout getDurgExpDateView(String mName, int mNumber, String data){
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout firstlayout = new LinearLayout(getActivity());
        LinearLayout secondlayout = new LinearLayout(getActivity());

        LinearLayout finallayout = new LinearLayout(getActivity());
        finallayout.setOrientation(LinearLayout.HORIZONTAL);

        TextView tv_label = new TextView(getActivity());
        tv_label.setText(mName);
        tv_label.setTextColor(Color.BLACK);
        tv_label.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        tv_label.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity()));
        firstlayout.addView(tv_label, params8);
        dlExpDateTextView = new TextView(new ContextThemeWrapper(getActivity(), R.style.datePickerButton), null, 0);
        dlExpDateTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        dlExpDateTextView.setTextColor(Color.BLACK);
        dlExpDateTextView.setGravity(Gravity.CENTER);
        dlExpDateTextView.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
        dlExpDateTextView.setId(mNumber);
        dlExpDateTextView.setTypeface(dlExpDateTextView.getTypeface(), Typeface.NORMAL);

        dlExpDateTextView.setText(data);
        secondlayout.addView(dlExpDateTextView, weight0wrap);
        finallayout.addView(firstlayout, params8);
        finallayout.addView(secondlayout, weight4);
        linearlayout.addView(finallayout, params6);

        dlExpDateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int year = c.get(Calendar.YEAR);
                int month = c.get(Calendar.MONTH);
                int day = c.get(Calendar.DAY_OF_MONTH);
                 String date = dlExpDateTextView.getText().toString();
                if (!date.equalsIgnoreCase("Select Date") && date.contains("/") && date.split("/").length == 3) {
                    year = Integer.valueOf(date.split("/")[0]);
                    month = Integer.valueOf(date.split("/")[1]) - 1;
                    day = Integer.valueOf(date.split("/")[2]);
                }
                 DialogFragment newFragment = new DatePickerFragment("DLEXPDATE", year, month, day);
                newFragment.show(getActivity().getSupportFragmentManager(), "dlDatePicker");
            }
        });
        return linearlayout;
    }


    private AppCompatEditText getSingleEditTextView(int positionNumber, String configCode,
                                                    String menuName, String values, boolean IS_UPPERCASE_LETTER) {
        AppCompatEditText appCompatEditText = new AppCompatEditText(getActivity());
        appCompatEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        appCompatEditText.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        appCompatEditText.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        appCompatEditText.setText(values);
        appCompatEditText.setHint(menuName);

        if (!IS_UPPERCASE_LETTER)
            appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        else
            appCompatEditText.setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        if (comparConfigerCode(configCode, ProfileConstant.CONTACT_NUMBER)
                || comparConfigerCode(configCode, ProfileConstant.MOBILE)
                || comparConfigerCode(configCode, ProfileConstant.FAX)) {
            appCompatEditText.setInputType(InputType.TYPE_CLASS_PHONE);
        }

        if (comparConfigerCode(configCode, ProfileConstant.CREDITPERIOD)) {
            appCompatEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
            if (values.equals("0") || values.equals("-1"))
                appCompatEditText.setText("");
            else
                appCompatEditText.setText(values);
        }
        editTextHashMap.put(positionNumber,appCompatEditText);
        if (!comparConfigerCode(configCode, ProfileConstant.EMAIL)) {//if not Email //cmd for not apply inputfilter value for email id
            getInputFilter(positionNumber);
        }
        return appCompatEditText;
    }


    private LinearLayout getSpinnerView(int mNumber, String MName, @NonNls String menuCode, int id) {

        this.menuCode = menuCode;
        this.MName = MName;
        this.id = id;
        mLocationMasterList1 = profileEditPresenter.getLocationMasterList1();
        mLocationMasterList2 = profileEditPresenter.getLocationMasterList2();
        mLocationMasterList3 = profileEditPresenter.getLocationMasterList3();

        LinearLayout layout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));
        LinearLayout.LayoutParams spinweight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinweight.weight = 1;
        spinweight.gravity = Gravity.CENTER;

        if (menuCode.equals(ProfileConstant.CHANNEL)) {
            channel = new MaterialSpinner(getActivity());
            channel.setId(mNumber);
            channel.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            channel.setFloatingLabelText(MName);
            //channel.setGravity(Gravity.CENTER);

            layout.addView(channel, spinweight);

            ArrayAdapter<ChannelBO> channelAdapter = new ArrayAdapter<ChannelBO>(getActivity(), android.R.layout.simple_spinner_item);
            channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            channelAdapter.add(new ChannelBO(0, getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));
            int position = 0, setPos = 0;
            int channelID = id;

            if (channelMaster != null)
                for (ChannelBO temp : channelMaster) {
                    channelAdapter.add(temp);
                    if (temp.getChannelId() == channelID)
                        setPos = position + 1;
                    position++;
                }
            channel.setAdapter(channelAdapter);
            channel.setSelection(setPos, false);
            channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    ChannelBO tempBo = (ChannelBO) parent.getSelectedItem();
                    if (subchannel != null)
                        loadsubchannel(tempBo.getChannelId());
                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });

        }

        if (menuCode.equals(ProfileConstant.CONTRACT)) {
            int selected_pos = 0;
            try {
                contractSpinner = new MaterialSpinner(getActivity());
                contractSpinner.setId(mNumber);
                contractSpinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                contractSpinner.setFloatingLabelText(MName);
                mcontractStatusList = new ArrayList<>();
                mcontractStatusList.add(0, new NewOutletBO(0, getResources().getString(R.string.select_str) + " " + MName));
                mcontractStatusList.addAll(profileEditPresenter.getContractStatusList());
                ArrayAdapter<NewOutletBO> contractStatusAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, mcontractStatusList);
                contractStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                contractSpinner.setAdapter(contractStatusAdapter);
                contractSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {
                    }

                });

                for (int i = 0; i < mcontractStatusList.size(); i++) {
                    if (id == mcontractStatusList.get(i).getListId())
                        selected_pos = i;
                }
                contractSpinner.setSelection(selected_pos);
                layout.addView(contractSpinner, spinweight);

            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        if (menuCode.equals(ProfileConstant.SUBCHANNEL)) {
            subchannel = new MaterialSpinner(getActivity());
            subchannel.setId(mNumber);
            subchannel.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            subchannel.setFloatingLabelText(MName);
            layout.addView(subchannel, spinweight);

        } else if (menuCode.equals(ProfileConstant.LOCATION01)) {
            try {
                location1 = new MaterialSpinner(getActivity());
                location1.setId(mNumber);
                location1.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                location1.setFloatingLabelText(MName);
                if (mLocationMasterList1 == null) {
                    mLocationMasterList1 = new ArrayList<LocationBO>();
                }
                mLocationMasterList1.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));

                locationAdapter1 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList1);

                String loc1id = "";
                int pos = 0, setPos = 0;
                String[] loc1 = profileEditPresenter.getParentLevelName(locid, false);

                if (loc1 != null) {
                    loc1id = loc1[0];
                }
                for (LocationBO loc : mLocationMasterList1) {
                    if (loc.getLocId() == SDUtil.convertToInt(loc1id)) {
                        setPos = pos;
                    }
                    pos++;
                }
                location1.setAdapter(locationAdapter1);
                location1.setSelection(setPos);
                location1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                layout.addView(location1, spinweight);

            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (menuCode.equals(ProfileConstant.LOCATION02)) {
            try {
                location2 = new MaterialSpinner(getActivity());
                location2.setId(mNumber);
                location2.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                location2.setFloatingLabelText(MName);
                //location2.setGravity(Gravity.CENTER);
                if (mLocationMasterList2 == null) {
                    mLocationMasterList2 = new ArrayList<LocationBO>();
                }
                mLocationMasterList2.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));
                locationAdapter2 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList2);
                String loc2id = "";
                int pos = 0, setPos = 0;
                String[] loc2 = profileEditPresenter.getParentLevelName(locid, true);

                if (loc2 != null) {
                    loc2id = loc2[0];
                }
                for (LocationBO loc : mLocationMasterList2) {
                    if (loc.getLocId() == SDUtil.convertToInt(loc2id)) {
                        setPos = pos;
                    }
                    pos++;
                }
                location2.setAdapter(locationAdapter2);
                location2.setSelection(setPos);
                location2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {
                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        updateLocationAdapter1(tempBo.getLocId());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });
                location2.setPadding(0, 0, 0, 12);
                layout.addView(location2, spinweight);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (menuCode.equals(ProfileConstant.LOCATION)) {
            try {
                location3 = new MaterialSpinner(getActivity());
                location3.setId(mNumber);
                location3.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
                location3.setFloatingLabelText(MName);
                //location3.setGravity(Gravity.CENTER);
                if (mLocationMasterList3 == null) {
                    mLocationMasterList3 = new ArrayList<LocationBO>();
                }
                mLocationMasterList3.add(0, new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str) + " " + MName));

                ArrayAdapter<LocationBO> locationAdapter3 = new ArrayAdapter<LocationBO>(getActivity(),
                        android.R.layout.simple_spinner_item, mLocationMasterList3);
                String locid = "";
                int pos = 0, setPos = 0;
                String[] loc3 = profileEditPresenter.getParentLevelName(true);
                if (loc3 != null) {
                    locid = loc3[0];
                }
                for (LocationBO loc : mLocationMasterList3) {
                    if (loc.getLocId() == SDUtil.convertToInt(locid)) {
                        setPos = pos;
                    }
                    pos++;
                }
                location3.setAdapter(locationAdapter3);
                location3.setSelection(setPos);
                location3.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent, View view,
                                               int pos, long id) {
                        LocationBO tempBo = (LocationBO) parent.getSelectedItem();
                        updateLocationAdapter2(tempBo.getLocId());
                    }

                    public void onNothingSelected(AdapterView<?> arg0) {

                    }

                });

                layout.addView(location3, spinweight);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RFIELD5)) {
            rField5Spinner = new MaterialSpinner(getActivity());
            rField5Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            rField5Spinner.setId(mNumber);
            rField5Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_5);
            layout.addView(rField5Spinner, spinweight);

        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RFIELD6)) {
            rField6Spinner = new MaterialSpinner(getActivity());
            rField6Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            rField6Spinner.setId(mNumber);
            rField6Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_6);
            layout.addView(rField6Spinner, spinweight);

        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RFIELD7)) {
            rField7Spinner = new MaterialSpinner(getActivity());
            rField7Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            rField7Spinner.setId(mNumber);
            rField7Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_7);
            layout.addView(rField7Spinner, spinweight);

        } else if (menuCode.equalsIgnoreCase(ProfileConstant.RField4)) {
            rField4Spinner = new MaterialSpinner(getActivity());
            rField4Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
            rField4Spinner.setId(mNumber);
            rField4Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_4);
            layout.addView(rField4Spinner, spinweight);
        }
        return layout;

    }


    private TextView getSingleTextView(int positionNumber, String menuName) {
        textview[positionNumber] = new TextView(getActivity());
        textview[positionNumber].setText(menuName);
        textview[positionNumber].setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        textview[positionNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        textview[positionNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        return textview[positionNumber];
    }


    private LinearLayout getLatlongTextView(final int mNumber, String MName, final String textvalue) {

        LinearLayout linearlayout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = createLinearLayout(); //TextView Title
        firstlayout.setPadding(0, 0, 0, 0);
        firstlayout.addView(getSingleTextView(mNumber, MName));

        LinearLayout secondlayout = createLinearLayout(LinearLayout.HORIZONTAL, 0, 2f);
        secondlayout.setPadding(0, 0, 0, 0);
        secondlayout.setGravity(Gravity.CENTER_VERTICAL);

        latlongtextview = new TextView(getActivity());
        latlongtextview.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        latlongtextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        latlongtextview.setText(textvalue);
        latlongtextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));//setTextSize(TypedValue.COMPLEX_UNIT_SP, getContext().getResources().getDimension(R.dimen.font_medium));

        latlongCameraBtn = new ImageView(getActivity());
        latlongCameraBtn.setBackgroundResource(R.drawable.ic_photo_camera);
        latlongCameraBtn.setScaleType(ImageView.ScaleType.FIT_XY);
        latlongCameraBtn.setVisibility(View.GONE);
        latlongCameraBtn.setPadding(5, 2, 5, 2);
        secondlayout.addView(latlongtextview, 0, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1.7f));
        secondlayout.addView(latlongCameraBtn, 1, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 0.1f));

        secondlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lat = LocationUtil.latitude + "";
                longitude = LocationUtil.longitude + "";
                onMapViewClicked();
            }
        });

        latlongCameraBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                profileEditPresenter.latlongCameraBtnClickListene(true);
            }
        });

        linearlayout.addView(firstlayout, params5);
        linearlayout.addView(secondlayout, weight2);
        return linearlayout;

    }


    private LinearLayout getNearByRetailerView(int mNumber, String MName, boolean isEditMode) {

        LinearLayout layout = createLinearLayout(LinearLayout.HORIZONTAL,
                getActivity().getResources().getColor(R.color.white_box_start));

        LinearLayout firstlayout = createLinearLayout();
        firstlayout.addView(getSingleTextView(mNumber, MName), weight1);
        textview[mNumber].setTextColor(Color.BLACK);

        LinearLayout secondlayout = createLinearLayout(LinearLayout.HORIZONTAL, 0);

        Button retailerButton = new Button(getActivity());
        retailerButton.setText(R.string.edit);
        retailerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileEditPresenter.getLinkRetailerListByDistributorId();
            }
        });

        nearbyTextView = new TextView(getActivity());
        nearbyTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        nearbyTextView.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        nearbyTextView.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        nearbyTextView.setGravity(Gravity.CENTER);

        ScrollView scrl = new ScrollView(getActivity());
        LinearLayout lyt = createLinearLayout();
        lyt.addView(nearbyTextView, weight1);
        scrl.addView(lyt, weight1);

        secondlayout.addView(scrl, weight1);
        secondlayout.addView(retailerButton, weight3);
        layout.addView(firstlayout, weight2);
        layout.addView(secondlayout, weight1);

        if (!isEditMode) {
            retailerButton.setVisibility(View.GONE);
            profileEditPresenter.getNearbyRetailerIds();
        } else {
            profileEditPresenter.getNearbyRetailersEditRequest();
        }
        // showing nearby retailers
        for (RetailerMasterBO bo : mSelectedIds) {
            nearbyTextView.setText(nearbyTextView.getText() + DataMembers.CR1 + bo.getRetailerName());
        }

        return layout;
    }


    private LinearLayout getPriorityProductView(final int mNumber, final String MName, final String textvalue, final String productID) {

        LinearLayout linearlayout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));
        LinearLayout firstlayout = createLinearLayout();
        firstlayout.setPadding(0, 0, 0, 12);
        firstlayout.addView(getSingleTextView(mNumber, MName)); //TextView
        LinearLayout secondlayout = createLinearLayout(LinearLayout.HORIZONTAL, 0);
        secondlayout.setPadding(0, 0, 0, 12);
        priorityproducttextview = new TextView(getActivity());
        priorityproducttextview.setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        priorityproducttextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity()));
        priorityproducttextview.setText(textvalue);
        secondlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPriorityProductList != null) {
                    FragmentManager fm = getActivity().getSupportFragmentManager();
                    @NonNls CustomFragment dialogFragment = new CustomFragment();
                    @NonNls Bundle bundle = new Bundle();
                    bundle.putString("title", MName);
                    bundle.putString("screentitle", MName);
                    bundle.putInt("hasLink", 0);
                    bundle.putString("productID", productID);
                    dialogFragment.setArguments(bundle);
                    dialogFragment.show(fm, "Sample Fragment");
                    dialogFragment.setCancelable(false);

                } else {
                    //  Toast.makeText(getActivity(), getResources().getString(R.string.priority_products_not_available), Toast.LENGTH_SHORT).show();
                }
            }
        });
        priorityproducttextview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        secondlayout.addView(priorityproducttextview);

        linearlayout.addView(firstlayout, params5);
        linearlayout.addView(secondlayout, weight1);

        return linearlayout;

    }


    //To create layout for Retailer Attribute
    private LinearLayout addAttributeView(int flag) {

        LinearLayout parentLayout = null;
        try {
            //flag==0 - add common atrributes and attributes for current(from DB) channel
            //flag==1 - add new(if user changing the channel, then corresponding attributes loaded) channel attributes
            boolean isCommon = false,
                    isFromChannel = false;

            boolean isNewChannel = false;

            if (flag == 0) isCommon = true;
            else if (flag == 1) isFromChannel = true;

            //Params
            LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            innerParams.setMargins(0, 0, 10, 0);

            LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            titleParams.setMargins(3, 0, 0, 0);

            ArrayList<Integer> mNewAttributeListByLocationID = null;// Newly selected channel's attribute

            if (isCommon) {

                // creating parent layout for attributes
                parentLayout = new LinearLayout(getActivity());
                parentLayout.setTag("attributeLayout");
                parentLayout.setOrientation(LinearLayout.VERTICAL);

                LinearLayout titleLayout = new LinearLayout(getActivity());
                titleLayout.setOrientation(LinearLayout.HORIZONTAL);

                // title
                TextView titleTV = new TextView(getActivity());
                titleTV.setText(getResources().getString(R.string.attribute));
                titleTV.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_medium));
                titleTV.setTextColor(Color.BLACK);
                titleLayout.addView(titleTV, titleParams);

                parentLayout.addView(titleLayout, LLParams);
                profileEditPresenter.isCommonAttributeView();


            } else if (isFromChannel) {
                isNewChannel = true;
                // getting existing attribute layout and clearig childs for loading attributes of current channel
                parentLayout = (LinearLayout) getView().findViewWithTag("attributeLayout");
                if (parentLayout != null) {
                    for (int i = 0; i < parentLayout.getChildCount(); i++) {
                        if (parentLayout.getChildAt(i).getTag() != null &&
                                ((String) parentLayout.getChildAt(i).getTag()).equals("channel"))
                            parentLayout.removeViewAt(i);
                    }
                }
                // getting newly selected channel's attribute
                mNewAttributeListByLocationID = new ArrayList<>();
                if (profileEditPresenter.getAttributeListByLocationId() != null
                        && profileEditPresenter.getAttributeListByLocationId().get(((SpinnerBO) subchannel.getSelectedItem()).getId()) != null)
                    mNewAttributeListByLocationID.addAll(profileEditPresenter.getAttributeListByLocationId().get(((SpinnerBO) subchannel.getSelectedItem()).getId()));

            }

            spinnerHashMap = new HashMap<>();
            spinnerAdapterMap = new HashMap<>();
            if (isFromChannel && isNewChannel) {

                // User selected a sub channel an it is new one.
                for (int i = 0; i < profileEditPresenter.getAttributeParentList().size(); i++) {

                    final NewOutletAttributeBO parentBO;
                    parentBO = profileEditPresenter.getAttributeParentList().get(i);

                    if (mNewAttributeListByLocationID.contains(parentBO.getAttrId())) {

                        LinearLayout layout = new LinearLayout(getActivity());

                        // setting tag as channel, used to remove channel views particularly and update new one if channel changed
                        layout.setTag("channel");

                        layout.setOrientation(LinearLayout.HORIZONTAL);
                        layout.setGravity(Gravity.CENTER_VERTICAL);
                        layout.setWeightSum(3f);
                        layout.setLayoutParams(LLParams);

                        //Attribute label
                        final String attribName = parentBO.getAttrName();
                        TextView mn_textview = new TextView(getActivity());
                        mn_textview.setText(attribName);
                        mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                        mn_textview.setTextColor(Color.BLACK);
                        mn_textview.setLayoutParams(paramsAttrib);
                        layout.addView(mn_textview);

                        final int columnCount = profileEditPresenter.getLevel(parentBO.getAttrId());
                        MaterialSpinner spinner;
                        LinearLayout innerLL = new LinearLayout(getActivity());
                        innerLL.setOrientation(LinearLayout.VERTICAL);
                        innerLL.setLayoutParams(paramsAttribSpinner);
                        LinearLayout innerHL = new LinearLayout(getActivity());
                        innerHL.setWeightSum(2);
                        innerHL.setLayoutParams(LLParams);
                        innerHL.setOrientation(LinearLayout.HORIZONTAL);
                        boolean isAdded = false;
                        //  ArrayList<Integer> indexList = attributeIndexMap.get(attribName);

                        //creating child levels
                        for (int j = 0; j < columnCount; j++) {
                            isAdded = false;
                            final ArrayList<NewOutletAttributeBO> attrbList;
                            final int index = j;
                            //   final int selectedPos = indexList.get(j);
                            spinner = new MaterialSpinner(getActivity());

                            attrbList = new ArrayList<>();
                            attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                                    .getString(R.string.select_str) + " " + getActivity().getResources()
                                    .getString(R.string.attribute)));
                            attrbList.addAll(profileEditPresenter.getAttributeMapList(attribName));

                            final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                    android.R.layout.simple_spinner_item, attrbList);
                            spinner.setAdapter(arrayAdapter);
                            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinner.setLayoutParams(innerParams);
                            innerHL.addView(spinner);
                            spinnerAdapterMap.put(attribName + index, arrayAdapter);
                            //  spinner.setSelection(selectedPos + 1);
                            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                    if (++check > spinnerCount) {
                                        selectedAttribList.remove(parentBO.getAttrId());
                                        selectedAttribList.put(parentBO.getAttrId(), attrbList.get(position));

                                        if (index < columnCount)
                                            loadAttributeSpinner(attribName + parentBO.getAttrId(), attrbList.get(position).getAttrId());
                                    }
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> parent) {
                                }
                            });
                            spinnerHashMap.put(attribName + index, spinner);
                            if ((j + 1) % 2 == 0) {
                                innerLL.addView(innerHL);
                                innerHL = new LinearLayout(getActivity());
                                innerHL.setWeightSum(2);
                                innerHL.setLayoutParams(LLParams);
                                innerHL.setOrientation(LinearLayout.HORIZONTAL);
                                isAdded = true;
                            }
                        }
                        if (!isAdded) {
                            innerLL.addView(innerHL);
                        }

                        layout.addView(innerLL);
                        parentLayout.addView(layout);
                    }
                }
            } else if (isCommon) {
                //Call while creating view first time
                //Call while creating a  view

                if (profileEditPresenter.getAttributeList().size() > 0) {
                    // There is a attribute for current retailer..

                    ArrayList<Integer> mAddedCommonAttributeList = new ArrayList<>();
                    int rowCount = profileEditPresenter.getAttributeList().size();
                    updateRetailerAttribute(profileEditPresenter.getAttributeList());

                     /*Even if there is a record for current retailer.. we should load common attributes in the view,
                     so that user can add new attribute for current retailer*/

                    for (NewOutletAttributeBO newOutletAttributeBO : attributeHeaderList) {
                        if (profileEditPresenter.getCommonAttributeList().contains(newOutletAttributeBO.getAttrId()))
                            mAddedCommonAttributeList.add(newOutletAttributeBO.getAttrId());
                    }
                    prepareCommonAttributeView(profileEditPresenter.getCommonAttributeList(), parentLayout, mAddedCommonAttributeList);
                    //

                    for (int i = 0; i < rowCount; i++) {

                        final NewOutletAttributeBO parentBO;
                        parentBO = attributeHeaderList.get(i);
                        /*Allowing only if parent attribute is available in common list or channel's(from Db) attribute
                        assert mChannelAttributeList != null;*/
                        if ((isCommon && (profileEditPresenter.getCommonAttributeList().contains(parentBO.getAttrId())
                                || (profileEditPresenter.getChannelAttributeList() != null
                                && profileEditPresenter.getChannelAttributeList().contains(parentBO.getAttrId()))))) {

                            @NonNls LinearLayout layout = new LinearLayout(getActivity());
                            // setting tag as channel, used to remove channel views particularly and update new one if channel changed
                            if (profileEditPresenter.getChannelAttributeList() != null
                                    && profileEditPresenter.getChannelAttributeList().contains(parentBO.getAttrId()))
                                layout.setTag("channel");
                            layout.setOrientation(LinearLayout.HORIZONTAL);
                            layout.setGravity(Gravity.CENTER_VERTICAL);
                            layout.setWeightSum(3f);
                            layout.setLayoutParams(LLParams);

                            final String attribName = parentBO.getAttrName();
                            TextView mn_textview = new TextView(getActivity());
                            mn_textview.setText(attribName);
                            mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                            mn_textview.setTextColor(Color.BLACK);
                            mn_textview.setLayoutParams(paramsAttrib);
                            layout.addView(mn_textview);

                            final int columnCount = profileEditPresenter.getLevel(parentBO.getAttrId());

                            MaterialSpinner spinner;

                            LinearLayout innerLL = new LinearLayout(getActivity());
                            innerLL.setOrientation(LinearLayout.VERTICAL);
                            innerLL.setLayoutParams(paramsAttribSpinner);

                            LinearLayout innerHL = new LinearLayout(getActivity());
                            innerHL.setWeightSum(2);
                            innerHL.setLayoutParams(LLParams);
                            innerHL.setOrientation(LinearLayout.HORIZONTAL);

                            boolean isAdded = false;
                            ArrayList<Integer> indexList = attributeIndexMap.get(attribName);
                            //creating child levels
                            for (int j = 0; j < columnCount; j++) {
                                isAdded = false;
                                final ArrayList<NewOutletAttributeBO> attrbList;
                                final int index = j;
                                final int selectedPos = indexList.get(j);
                                spinner = new MaterialSpinner(getActivity());
                                attrbList = new ArrayList<>();
                                attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                                        .getString(R.string.select_str) + " " + getActivity().getResources()
                                        .getString(R.string.attribute)));
                                attrbList.addAll(listHashMap.get(attribName).get(j));
                                final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                                        android.R.layout.simple_spinner_item, attrbList);
                                spinner.setAdapter(arrayAdapter);
                                arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                                spinner.setLayoutParams(innerParams);
                                innerHL.addView(spinner);
                                spinnerAdapterMap.put(attribName + index, arrayAdapter);
                                spinner.setSelection(selectedPos + 1);
                                spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        if (++check > spinnerCount) {
                                            selectedAttribList.remove(parentBO.getAttrId());
                                            selectedAttribList.put(parentBO.getAttrId(), attrbList.get(position));
                                            if (index < columnCount)
                                                loadAttributeSpinner(attribName + parentBO.getAttrId(), attrbList.get(position).getAttrId());
                                        }
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }
                                });
                                spinnerHashMap.put(attribName + index, spinner);
                                if ((j + 1) % 2 == 0) {
                                    innerLL.addView(innerHL);
                                    innerHL = new LinearLayout(getActivity());
                                    innerHL.setWeightSum(2);
                                    innerHL.setLayoutParams(LLParams);
                                    innerHL.setOrientation(LinearLayout.HORIZONTAL);
                                    isAdded = true;
                                }
                            }
                            if (!isAdded) {
                                innerLL.addView(innerHL);
                            }
                            layout.addView(innerLL);
                            parentLayout.addView(layout);
                        }

                    }
                } else {
                    // No attributes for current retailer..
                    // so just adding common attributes(Because user can add attribute for current retailer from edit screen).
                    prepareCommonAttributeView(profileEditPresenter.getCommonAttributeList()
                            , parentLayout, new ArrayList<Integer>());
                }


            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return parentLayout;
    }

    //To load spinner values for Retailer Attribute based on it's previous spinner selection
    private void loadAttributeSpinner(String key, int attribId) {
        MaterialSpinner spinner = spinnerHashMap.get(key);
        ArrayAdapter<NewOutletAttributeBO> adapter = spinnerAdapterMap.get(key);
        if (spinner != null && adapter != null) {
            ArrayList<NewOutletAttributeBO> arrayList = new ArrayList<>();
            arrayList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                    .getString(R.string.select_str) + " " + getActivity().getResources()
                    .getString(R.string.attribute)));
            for (NewOutletAttributeBO attributeBO : profileEditPresenter.getAttributeList()) {
                if (attribId == attributeBO.getParentId()) {
                    arrayList.add(attributeBO);
                }
            }

            adapter.clear();
            adapter.addAll(arrayList);
            spinner.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }


    @SuppressLint("UseSparseArrays")
    private void updateRetailerAttribute(ArrayList<NewOutletAttributeBO> list) {

        attributeHeaderList = new ArrayList<>();
        listHashMap = new HashMap<>();
        attributeIndexMap = new HashMap<>();
        selectedAttribList = new HashMap<>();
        ArrayList<ArrayList<NewOutletAttributeBO>> attributeGroupedList;
        ArrayList<NewOutletAttributeBO> childList = profileEditPresenter.getAttributeListChild();
        ArrayList<NewOutletAttributeBO> parentList = profileEditPresenter.getAttributeParentList();

        int attribID;
        int tempAttribID;
        int parentID = 0;
        int tempParentID = 0;
        String title = "";
        NewOutletAttributeBO tempBO = new NewOutletAttributeBO();
        int tempLevel;
        for (NewOutletAttributeBO attributeBO : list) {
            attributeGroupedList = new ArrayList<>();
            attributeIndexList = new ArrayList<>();
            attribID = attributeBO.getAttrId();
            for (int i = childList.size() - 1; i >= 0; i--) {
                NewOutletAttributeBO attributeBO1 = childList.get(i);
                tempAttribID = attributeBO1.getAttrId();
                if (attribID == tempAttribID) {
                    tempBO = attributeBO1;
                    tempParentID = attributeBO1.getParentId();
                    attributeGroupedList.add(getAttributeGroupedList(tempParentID, childList, tempAttribID));
                    continue;
                }
                if (tempAttribID == tempParentID) {
                    tempParentID = attributeBO1.getParentId();
                    attributeGroupedList.add(getAttributeGroupedList(tempParentID, childList, tempAttribID));
                }
            }

            for (NewOutletAttributeBO attributeBO2 : parentList) {
                parentID = attributeBO2.getAttrId();
                tempLevel = profileEditPresenter.getLevel(attributeBO2.getAttrId());
                if (tempParentID == parentID) {
                    spinnerCount += tempLevel;
                    title = attributeBO2.getAttrName();
                    attributeHeaderList.add(attributeBO2);
                }
            }
            Collections.reverse(attributeGroupedList);
            Collections.reverse(attributeIndexList);
            listHashMap.put(title, attributeGroupedList);
            attributeIndexMap.put(title, attributeIndexList);
            selectedAttribList.put(tempParentID, tempBO);
        }
    }


    private ArrayList<NewOutletAttributeBO> getAttributeGroupedList(int parentID
            , ArrayList<NewOutletAttributeBO> list, int attribID) {
        ArrayList<NewOutletAttributeBO> arrayList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            NewOutletAttributeBO obj = list.get(i);
            if (parentID == obj.getParentId()) {
                arrayList.add(obj);
            }
        }
        for (int i = 0; i < arrayList.size(); i++) {
            NewOutletAttributeBO obj = arrayList.get(i);
            if (attribID == obj.getAttrId())
                attributeIndexList.add(i);
        }
        return arrayList;
    }


    private void prepareCommonAttributeView(ArrayList<Integer> mCommonAttributeList
            , LinearLayout parentLayout, ArrayList<Integer> mAddedCommonAttributeList) {

        LinearLayout.LayoutParams LLParams = new LinearLayout.LayoutParams(android.widget.LinearLayout.LayoutParams.MATCH_PARENT,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT);
        LinearLayout.LayoutParams innerParams = new LinearLayout.LayoutParams(0,
                android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, 1f);

        int rowCount = profileEditPresenter.getAttributeParentList().size();
        selectedAttribList = new HashMap<>();
        for (int i = 0; i < rowCount; i++) {

            final NewOutletAttributeBO parentBO = profileEditPresenter.getAttributeParentList().get(i);
            if (mCommonAttributeList.contains(parentBO.getAttrId())
                    && !mAddedCommonAttributeList.contains(parentBO.getAttrId())) {

                LinearLayout layout = new LinearLayout(getActivity());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                layout.setGravity(Gravity.CENTER_VERTICAL);
                layout.setWeightSum(3f);
                layout.setLayoutParams(LLParams);
                final String attribName = parentBO.getAttrName();
                TextView mn_textview = new TextView(getActivity());
                mn_textview.setText(attribName);
                mn_textview.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                mn_textview.setTextColor(Color.BLACK);
                mn_textview.setLayoutParams(paramsAttrib);
                layout.addView(mn_textview);
                final int columnCount = profileEditPresenter.getLevel(parentBO.getAttrId());
                MaterialSpinner spinner;
                LinearLayout innerLL = new LinearLayout(getActivity());
                innerLL.setOrientation(LinearLayout.VERTICAL);
                innerLL.setLayoutParams(paramsAttribSpinner);
                LinearLayout innerHL = new LinearLayout(getActivity());
                innerHL.setWeightSum(2);
                innerHL.setLayoutParams(LLParams);
                innerHL.setOrientation(LinearLayout.HORIZONTAL);
                boolean isAdded = false;
                // ArrayList<Integer> indexList = attributeIndexMap.get(attribName);
                //creating child levels
                for (int j = 0; j < columnCount; j++) {
                    isAdded = false;
                    final ArrayList<NewOutletAttributeBO> attrbList;
                    final int index = j;
                    //final int selectedPos = indexList.get(j);
                    spinner = new MaterialSpinner(getActivity());
                    attrbList = new ArrayList<>();
                    attrbList.add(0, new NewOutletAttributeBO(-1, getActivity().getResources()
                            .getString(R.string.select_str) + " " + getActivity().getResources()
                            .getString(R.string.attribute)));
                    attrbList.addAll(profileEditPresenter.getAttributeMapList(attribName));
                    final ArrayAdapter<NewOutletAttributeBO> arrayAdapter = new ArrayAdapter<>(getActivity(),
                            android.R.layout.simple_spinner_item, attrbList);
                    spinner.setAdapter(arrayAdapter);
                    arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    spinner.setLayoutParams(innerParams);
                    innerHL.addView(spinner);
                    spinnerAdapterMap.put(attribName + index, arrayAdapter);
                    // spinner.setSelection(selectedPos + 1);
                    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            if (++check > spinnerCount) {
                                selectedAttribList.remove(parentBO.getAttrId());
                                selectedAttribList.put(parentBO.getAttrId(), attrbList.get(position));
                                if (index < columnCount)
                                    loadAttributeSpinner(attribName + parentBO.getAttrId(), attrbList.get(position).getAttrId());
                            }
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {
                        }
                    });
                    spinnerHashMap.put(attribName + index, spinner);
                    if ((j + 1) % 2 == 0) {
                        innerLL.addView(innerHL);
                        innerHL = new LinearLayout(getActivity());
                        innerHL.setWeightSum(2);
                        innerHL.setLayoutParams(LLParams);
                        innerHL.setOrientation(LinearLayout.HORIZONTAL);
                        isAdded = true;
                    }
                }
                if (!isAdded) {
                    innerLL.addView(innerHL);
                }
                layout.addView(innerLL);
                parentLayout.addView(layout);
            }
        }
    }


    @Override
    public void generateOTP() {

    }

    @Override
    public void dismissListener(String type, boolean isVerfied) {

    }


    @SuppressLint("ValidFragment")
    public class CustomFragment extends DialogFragment {

        private TextView mTitleTV;
        private Button mOkBtn, mDismisBtn;
        private ListView mPriorityproductLV;

        private String mTitle = "";
        private String mMenuName = "";
        private int hasLink = 0;
        private int mSelectedpostion = -1;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = getArguments().getString("title");
            mMenuName = getArguments().getString("screentitle");
            hasLink = getArguments().getInt("hasLink");

        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
                savedInstanceState) {
            View rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);

            return rootView;
        }

        @Override
        public void onStart() {
            super.onStart();
            getDialog().setTitle(mTitle);
            mTitleTV = (TextView) getView().findViewById(R.id.title);
            mTitleTV.setVisibility(View.GONE);
            mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
            if (hasLink == 0)
                mOkBtn.setVisibility(View.GONE);

            mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
            mDismisBtn.setVisibility(View.GONE);
            mPriorityproductLV = (ListView) getView().findViewById(R.id.lv_colletion_print);

            if (hasLink == 0) {
                for (int i = 0; i < mPriorityProductList.size(); i++) {
                    if (mPriorityProductList.get(i).getListID().equals(selectedProductID))
                        mSelectedpostion = i;
                }
                ArrayAdapter<StandardListBO> adapter = new ArrayAdapter<StandardListBO>(getActivity(), android.R.layout.simple_list_item_single_choice, mPriorityProductList);
                mPriorityproductLV.setAdapter(adapter);
                mPriorityproductLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
                if (mSelectedpostion != -1)
                    mPriorityproductLV.setItemChecked(mSelectedpostion, true);
                mPriorityproductLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        selectedPriorityProductList = new ArrayList<StandardListBO>();
                        StandardListBO standardListBO = mPriorityProductList.get(position);
                        selectedProductID = standardListBO.getListID();
                        selectedPriorityProductList.add(standardListBO);
                        priorityproducttextview.setText(standardListBO.getListName());
                        getDialog().dismiss();
                    }
                });
            } else if (hasLink == 1) {
                MyAdapter adapter = new MyAdapter();
                mPriorityproductLV.setAdapter(adapter);
            }

            mOkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    updatePriorityProducts();
                    getDialog().dismiss();

                }
            });

        }

        class MyAdapter extends BaseAdapter {

            @Override
            public int getCount() {
                return mPriorityProductList.size();
            }

            @Override
            public Object getItem(int position) {
                return null;
            }

            @Override
            public long getItemId(int position) {
                return 0;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                LayoutInflater inflater = (LayoutInflater) getActivity()
                        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                final ViewHolder holder;
                if (convertView == null) {
                    holder = new ViewHolder();
                    convertView = inflater.inflate(R.layout.list_priotityproduct,
                            null);
                    holder.productNameTV = (TextView) convertView.findViewById(R.id.tv_product_name);
                    holder.productSelectCB = (CheckBox) convertView.findViewById(R.id.cb_productselect);
                    holder.productSelectCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            holder.standardListBO.setChecked(isChecked);
                        }
                    });
                    convertView.setTag(holder);

                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                holder.standardListBO = mPriorityProductList.get(position);
                holder.productNameTV.setText(holder.standardListBO.getListName());
                holder.productSelectCB.setChecked(holder.standardListBO.isChecked());
                return convertView;
            }
        }

        class ViewHolder {
            StandardListBO standardListBO;
            TextView productNameTV;
            CheckBox productSelectCB;

        }
    }


    private void updatePriorityProducts() {
        selectedPriorityProductList = new ArrayList<>();
        selectedPriorityProductList.clear();
        StringBuffer sb = new StringBuffer();

        for (StandardListBO standardListBO : mPriorityProductList) {
            if (standardListBO.isChecked()) {
                selectedPriorityProductList.add(standardListBO);
                if (sb.length() > 0)
                    sb.append(", ");
                sb.append(standardListBO.getListName());
            }
            if (selectedPriorityProductList.size() > 0) {
                priorityproducttextview.setText(sb.toString());

            } else {
                priorityproducttextview.setText("");
            }
        }
    }


    public void onMapViewClicked() {
        @NonNls Intent in;
        int REQUEST_CODE = 100;
        if (profileEditPresenter.IS_BAIDU_MAP())
            in = new Intent(getActivity(), BaiduMapDialogue.class);
        else
            in = new Intent(getActivity(), MapDialogue.class);

        double latdoub = Double.valueOf(lat);
        double longdoub = Double.valueOf(longitude);

        in.putExtra("lat", latdoub);
        in.putExtra("lon", longdoub);
        startActivityForResult(in, REQUEST_CODE);
    }


    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        nearbyTextView.setText("");
        for (RetailerMasterBO bo : list) {
            nearbyTextView.setText(nearbyTextView.getText() + DataMembers.CR1 + bo.getRetailerName());
        }
        mSelectedIds = list;
    }


    private void updateLocationAdapter1(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<>();
        for (LocationBO locationBO : mLocationMasterList1) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }
        locationAdapter1 = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item, locationList);
        if (locationAdapter1.getCount() > 0) {
            if (!(locationAdapter1.getItem(0)).getLocName().toLowerCase().contains("select"))
                locationAdapter1.insert(new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str)), 0);
        } else
            locationAdapter1.insert(new LocationBO(0, getActivity()
                    .getResources().getString(R.string.select_str)), 0);

        location1.setAdapter(locationAdapter1);
    }


    private void updateLocationAdapter2(int parentId) {
        ArrayList<LocationBO> locationList = new ArrayList<LocationBO>();
        for (LocationBO locationBO : mLocationMasterList2) {
            if (parentId == locationBO.getParentId()) {
                locationList.add(locationBO);
            }
        }
        locationAdapter2 = new ArrayAdapter<LocationBO>(getActivity(),
                android.R.layout.simple_spinner_item, locationList);
        if (locationAdapter2 != null && locationAdapter2.getCount() > 0) {
            if (!((LocationBO) locationAdapter2.getItem(0)).getLocName().toLowerCase().contains("select"))
                locationAdapter2.insert(new LocationBO(0, getActivity()
                        .getResources().getString(R.string.select_str)), 0);
        } else
            locationAdapter2.insert(new LocationBO(0, getActivity()
                    .getResources().getString(R.string.select_str)), 0);
        location2.setAdapter(locationAdapter2);

    }


    private void loadsubchannel(int channelid) {
        if (subchannel != null) {
            Vector items = getSubChannelMaster();

            int siz = items.size();
            if (siz == 0) {
                return;
            }
            ArrayAdapter<SpinnerBO> subchannelAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            subchannelAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            subchannelAdapter.add(new SpinnerBO(0, getActivity().getResources()
                    .getString(R.string.select_str)));

            int position = 0, setPos = 0;
            int subChannelID = getSubchannelid();
            String mPreviousProfileChanges = profileEditPresenter.getPreviousProfileChangesList(ProfileConstant.SUBCHANNEL);
            if (!AppUtils.isEmptyString(mPreviousProfileChanges))
                if (!mPreviousProfileChanges.equals(subChannelID + ""))
                    subChannelID = SDUtil.convertToInt(mPreviousProfileChanges);
            for (int i = 0; i < siz; ++i) {
                SubchannelBO ret = (SubchannelBO) items.elementAt(i);
                if (channelid != 0) {
                    if (channelid == ret.getChannelid()) {
                        subchannelAdapter.add(new SpinnerBO(ret.getSubchannelid(),
                                ret.getSubChannelname()));
                        if (ret.getSubchannelid() == subChannelID) {
                            setPos = position + 1;
                        }
                        position++;
                    }
                }
            }
            subchannel.setAdapter(subchannelAdapter);
            subchannel.setSelection(setPos, false);
            subchannel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                    //SpinnerBO tempBo = (SpinnerBO) parent.getSelectedItem();
                    if (++subChannelSpinnerCount > 1) {
                        addAttributeView(1);
                    }

                }

                public void onNothingSelected(AdapterView<?> arg0) {
                }

            });
        }
    }


    public int getSubchannelid() {
        return profileEditPresenter.getSubchannelid();
    }


    public Vector<SubchannelBO> getSubChannelMaster() {
        return profileEditPresenter.getSubChannelMaster();
    }

    /*comparing two values with equalsIgnoreCase*/
    private boolean comparConfigerCode(String configCode, @NonNls String configCodeFromDB) {
        return configCode.equalsIgnoreCase(configCodeFromDB);
    }

    //Open the Image in Photo Gallery while onClick
    private void openImage(File fileName) {
        if (fileName.getAbsolutePath().trim().length() > 0) {
            Uri path;
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", fileName);
                    intent.setDataAndType(path, "image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                } else {
                    path = Uri.fromFile(fileName);
                    intent.setDataAndType(path, "image/*");
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                }
                startActivity(intent);

            } catch (ActivityNotFoundException e) {
                Commons.printException("" + e);
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unloadimage),
                    Toast.LENGTH_SHORT).show();
        }
    }

    //InputFilder
    private void getInputFilter(int positionNumber) {
        if (inputFilters != null && inputFilters.size() > 0) {
            InputFilter[] stockArr = new InputFilter[inputFilters.size()];
            stockArr = inputFilters.toArray(stockArr);
            editTextHashMap.get(positionNumber).setFilters(stockArr);
            if (inputFilters.size() == 2)
            editTextHashMap.get(positionNumber).setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        int year;
        int month;
        int day;
        @NonNls
        String code;

        public DatePickerFragment(String code, int year, int month, int day) {
            this.code = code;
            this.year = year;
            this.month = month;
            this.day = day;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar selectedDate = new GregorianCalendar(year, month, day);
            if (selectedDate.after(Calendar.getInstance())) {
                @NonNls SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                if (code.equalsIgnoreCase("DLEXPDATE"))
                    dlExpDateTextView.setText(sdf.format(selectedDate.getTime()));

                else if (code.equalsIgnoreCase("FLEXPDATE"))
                    flExpDateTextView.setText(sdf.format(selectedDate.getTime()));
                this.year = year;
                this.day = day;
                this.month = month;
            } else {
                Toast.makeText(getActivity(), "Select future date", Toast.LENGTH_SHORT).show();
            }
        }
    }


}
