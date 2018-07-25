package com.ivy.ui.profile.edit.view;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.bo.RetailerFlexBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.MapDialogue;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.edit.IProfileEditContract;
import com.ivy.ui.profile.edit.di.DaggerProfileEditComponent;
import com.ivy.ui.profile.edit.di.ProfileEditModule;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FontUtils;

import org.jetbrains.annotations.NonNls;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;
import java.util.regex.Pattern;

import javax.inject.Inject;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;

public class ProfileEditFragmentNew extends BaseFragment implements IProfileEditContract.ProfileEditView {

    @BindView(R.id.profile_edit_scrollview)
    ScrollView mScrollView;

    @BindView(R.id.profile_edit_save)
    Button mButtonSave;

    private LinearLayout mRootLinearLayout = null;

    /*ProfileImageView */
    private ImageView mProfileImageView = null;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int LATLONG_CAMERA_REQUEST_CODE = 2;
    private String imageFileName, cameraFilePath = "";

    private TextView textview[] = new TextView[100];
    private AppCompatEditText editText[] = new AppCompatEditText[100];

    private MaterialSpinner channel, subchannel, location1, location2, location3,
            contractSpinner, rField5Spinner, rField6Spinner, rField7Spinner, rField4Spinner;

    private ArrayList<InputFilter> inputFilters = null;

    private LinearLayout.LayoutParams weight1,weight2;
    private LinearLayout.LayoutParams mcommonsparams=null,params5;

    private Vector<ChannelBO> channelMaster = null;
    private ArrayList<NewOutletBO>  mcontractStatusList = null;
    private ArrayList<LocationBO> mLocationMasterList1 = null, mLocationMasterList2 = null, mLocationMasterList3 = null;
    private ArrayAdapter<LocationBO> locationAdapter1 = null, locationAdapter2 = null;

    private int locid = 0,loc2id = 0,subChannelSpinnerCount = 0;
    private String MName;
    private String menuCode;
    private int id;
    static String lat = "", longitude = "";
    private boolean isLatLongCameravailable = false;

    private TextView latlongtextview;
    private ImageView imageView, latlongCameraBtn;

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

        weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.weight = 2;
        weight2.gravity = Gravity.CENTER;


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
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
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
        mScrollView.addView(getmRootLinearLayout());
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
                                   String values,boolean IS_UPPERCASE_LETTER,
                                   int Mandatory,int MAX_CREDIT_DAYS) {
        getmRootLinearLayout().addView(getEditTextView(mNumber, configCode, menuName,
                values,IS_UPPERCASE_LETTER,Mandatory,MAX_CREDIT_DAYS), getCommonsparams());

    }

    @Override
    public void createSpinnerView(Vector<ChannelBO> channelMaster,int mNumber, String MName, String menuCode, int id) {
        this.channelMaster = channelMaster;
        getmRootLinearLayout().addView(getSpinnerView(mNumber,MName,menuCode,id), getCommonsparams());
    }

    @Override
    public void createSpinnerView(int mNumber, String MName, String menuCode, int id) {
        getmRootLinearLayout().addView(getSpinnerView(mNumber,MName,menuCode,id), getCommonsparams());
    }

    @Override
    public void createLatlongTextView(int mNumber, String MName,  String textvalue) {
        getmRootLinearLayout().addView(getLatlongTextView(mNumber,MName,textvalue), getCommonsparams());
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
    public void createTextView() {

    }

    @Override
    public void createCheckBoxView() {

    }

    @Override
    public void createButtonView() {

    }





    @Override
    public void createEditTextWithSpiinerView() {

    }

    @Override
    public void showSuccessfullyProfileUpdatedAlert() {

    }

    @Override
    public void navigateToProfileScreen() {

    }

    @Override
    public void showMessage(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
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

    /*This method is used to create a new LinearLayout with attributes */
    private LinearLayout createLinearLayout(int oriendation, int resourcesId) {
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(oriendation);
        if (resourcesId != 0) linearlayout.setBackgroundColor(resourcesId);
        return linearlayout;
    }

    /*This method is used to create a new LinearLayout with attributes */
    private LinearLayout createLinearLayout(int oriendation, int resourcesId, float weightSum) {
        LinearLayout linearlayout = new LinearLayout(getActivity());
        linearlayout.setOrientation(oriendation);
        linearlayout.setWeightSum(weightSum);
        if (resourcesId != 0) linearlayout.setBackgroundColor(resourcesId);
        return linearlayout;
    }

    /*This method is used to create a new LinearLayout with attributes */
    private LinearLayout createLinearLayout() {
        return new LinearLayout(getActivity());
    }


    private LinearLayout.LayoutParams getCommonsparams(){
        if(mcommonsparams ==null){
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
        editText[mNumber].addTextChangedListener(new TextWatcher() {
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
                    editText[mNumber].setText(s);
                    editText[mNumber].setSelection(editText[mNumber].length());
                }
            }
        });

        //if  Email
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_78) && Mandatory == 1) {
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
            verifyBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getActivity()));
            verifyBtn.setText(getResources().getString(R.string.verify));
            verifyBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_bg1));
            verifyBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //verifyOTP("EMAIL", editText[mNumber].getText().toString());
                }
            });
            emailLayout.addView(verifyBtn, verifyButtonParams);
            linearlayout.addView(emailLayout, weight1);
        } else
            linearlayout.addView(editTextInputLayout, weight1);

        /*ContactNumber,PHNO1,PHNO2,MOBILE,FAX*/
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_30) ||
                comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_79) ||
                comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_86)) {

            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, menuName, values, IS_UPPERCASE_LETTER));

            if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_79) && Mandatory == 1)  /*MOBILE*/ {

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
                verifyBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getActivity()));
                verifyBtn.setText(getResources().getString(R.string.verify));
                verifyBtn.setTextColor(ContextCompat.getColor(getContext(), R.color.black_bg1));
                verifyBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //verifyOTP("MOBILE", editText[mNumber].getText().toString());
                    }
                });

                mobileLayout.addView(verifyBtn, mobileParam1);

                linearlayout.addView(mobileLayout, weight1);
            } else
                linearlayout.addView(editTextInputLayout, weight1);

        }

        //CREDITPERIOD
        if (comparConfigerCode(mConfigCode, ProfileConstant.PROFILE_25)) {
            editTextInputLayout = new TextInputLayout(getActivity());
            editTextInputLayout.addView(getSingleEditTextView(mNumber, mConfigCode, menuName, values, IS_UPPERCASE_LETTER));
            editText[mNumber].addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!qty.equals("")) {
                        if (SDUtil.convertToInt(qty) > MAX_CREDIT_DAYS) {
                            //Delete the last entered number and reset the qty
                            editText[mNumber].setText(qty.length() > 1 ? qty.substring(0, qty.length() - 1) : "0");
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

    /*Get the EditTextView*/
    private AppCompatEditText getSingleEditTextView(int positionNumber, String configCode,
                                                    String menuName, String values, boolean IS_UPPERCASE_LETTER) {

        editText[positionNumber] = new AppCompatEditText(getActivity());
        editText[positionNumber].setTextSize(TypedValue.COMPLEX_UNIT_PX, getActivity().getResources().getDimension(R.dimen.font_small));
        editText[positionNumber].setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
        editText[positionNumber].setTextColor(ContextCompat.getColor(getContext(), R.color.filer_level_text_color));
        editText[positionNumber].setText(values);
        editText[positionNumber].setHint(menuName);

        if (!comparConfigerCode(configCode, ProfileConstant.PROFILE_78)) {//if not Email //cmd for not apply inputfilter value for email id
            getInputFilter(positionNumber);
        }

        if (!IS_UPPERCASE_LETTER)
            editText[positionNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME);
        else
            editText[positionNumber].setInputType(InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        if (comparConfigerCode(configCode, ProfileConstant.PROFILE_30)
                || comparConfigerCode(configCode, ProfileConstant.PROFILE_79)
                || comparConfigerCode(configCode, ProfileConstant.PROFILE_86)) {
            editText[positionNumber].setInputType(InputType.TYPE_CLASS_PHONE);
        }

        if (comparConfigerCode(configCode, ProfileConstant.PROFILE_25)) {
            editText[positionNumber].setInputType(InputType.TYPE_CLASS_NUMBER);
            if (values.equals("0") || values.equals("-1"))
                editText[positionNumber].setText("");
            else
                editText[positionNumber].setText(values);
        }

        return editText[positionNumber];
    }

    @Override
    public void updateRetailerFlexValues(ArrayList<RetailerFlexBO> retailerFlexBOArrayList) {

        if (menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_53)){
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
        }else if(menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_54)){
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
        }else if(menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_55)){
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
        }else if(menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_28)){
            ArrayAdapter<RetailerFlexBO> rField4Adapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            rField4Adapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
            rField4Adapter.add(new RetailerFlexBO("0", getActivity().getResources()
                    .getString(R.string.select_str) + " " + MName));
            int selPos = 0;
            for (int i = 0; i <retailerFlexBOArrayList.size(); i++) {
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
        isLatLongCameravailable=b;
    }


    // * ROFILE09, PROFILE10, PROFILE11, PROFILE12, PROFILE41, PROFILE42*/
    private LinearLayout getSpinnerView(int mNumber, String MName, @NonNls String menuCode, int id) {

        this.menuCode=menuCode;
        this.MName=MName;
        this.id=id;
        mLocationMasterList1=profileEditPresenter.getLocationMasterList1();
        mLocationMasterList2=profileEditPresenter.getLocationMasterList2();
        mLocationMasterList3=profileEditPresenter.getLocationMasterList3();

        LinearLayout layout = createLinearLayout(LinearLayout.HORIZONTAL, getActivity().getResources().getColor(R.color.white_box_start));
        LinearLayout.LayoutParams spinweight = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        spinweight.weight = 1;
        spinweight.gravity = Gravity.CENTER;

        if (menuCode.equals(ProfileConstant.PROFILE_06)) {
            channel = new MaterialSpinner(getActivity());
            channel.setId(mNumber);
            channel.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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

        if (menuCode.equals(ProfileConstant.PROFILE_43)) {
            int selected_pos = 0;
            try {
                contractSpinner = new MaterialSpinner(getActivity());
                contractSpinner.setId(mNumber);
                contractSpinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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

        if (menuCode.equals(ProfileConstant.PROFILE_07)) {
            subchannel = new MaterialSpinner(getActivity());
            subchannel.setId(mNumber);
            subchannel.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
            subchannel.setFloatingLabelText(MName);
            layout.addView(subchannel, spinweight);

        }
        else if (menuCode.equals(ProfileConstant.PROFILE_13)) {
            try {
                location1 = new MaterialSpinner(getActivity());
                location1.setId(mNumber);
                location1.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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
                String[] loc1 = profileEditPresenter.getParentLevelName(locid,false);

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
        }
        else if (menuCode.equals(ProfileConstant.PROFILE_14)) {
            try {
                location2 = new MaterialSpinner(getActivity());
                location2.setId(mNumber);
                location2.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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
                String[] loc2 =profileEditPresenter.getParentLevelName(locid,true);

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
        }
        else if (menuCode.equals(ProfileConstant.PROFILE_15)) {
            try {
                location3 = new MaterialSpinner(getActivity());
                location3.setId(mNumber);
                location3.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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
                String[] loc3 =  profileEditPresenter.getParentLevelName(loc2id,true);
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
        }
        else if (menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_53)) {
            rField5Spinner = new MaterialSpinner(getActivity());
            rField5Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
            rField5Spinner.setId(mNumber);
            rField5Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_5);
            layout.addView(rField5Spinner, spinweight);

        }
        else if (menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_54)) {
            rField6Spinner = new MaterialSpinner(getActivity());
            rField6Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
            rField6Spinner.setId(mNumber);
            rField6Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_6);
            layout.addView(rField6Spinner, spinweight);

        }
        else if (menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_55)) {
            rField7Spinner = new MaterialSpinner(getActivity());
            rField7Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
            rField7Spinner.setId(mNumber);
            rField7Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_7);
            layout.addView(rField7Spinner, spinweight);

        }
        else if (menuCode.equalsIgnoreCase(ProfileConstant.PROFILE_28)) {
            rField4Spinner = new MaterialSpinner(getActivity());
            rField4Spinner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
            rField4Spinner.setId(mNumber);
            rField4Spinner.setFloatingLabelText(MName);
            profileEditPresenter.downloadRetailerFlexValues(ProfileConstant.RFIELD_4);
            layout.addView(rField4Spinner, spinweight);
        }
        return layout;

    }

    /*This method is used to create a new LinearLayout with attributes */
    private TextView getSingleTextView(int positionNumber, String menuName) {
        textview[positionNumber] = new TextView(getActivity());
        textview[positionNumber].setText(menuName);
        textview[positionNumber].setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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
        latlongtextview.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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
            String mPreviousProfileChanges=profileEditPresenter.getPreviousProfileChangesList(ProfileConstant.PROFILE_07);
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
                    if (++subChannelSpinnerCount > 1){
                        // addAttributeView(1);
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


    public Vector<SubchannelBO> getSubChannelMaster( ) {
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
            editText[positionNumber].setFilters(stockArr);
            if (inputFilters.size() == 2)
                editText[positionNumber].setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
        }
    }

}
