package com.ivy.sd.png.view;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.profile.IProfileContractor;
import com.ivy.ui.profile.ProfileConstant;
import com.ivy.ui.profile.di.DaggerProfileComponent;
import com.ivy.ui.profile.di.ProfileModule;
import com.ivy.ui.profile.view.ProfileBaseBo;
import com.ivy.ui.profile.view.ProfileStepperAdapter;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;


public class ProfileContainerFragment extends BaseFragment
        implements StepperLayout.StepperListener,IProfileContractor.IProfileView {

    private BusinessModel bmodel;
    private boolean isFromEditProfileView;
    private static final String SCREEN_MODE_INTENT_KEY ="screenMode";
    private static final String RETAILERID_INTENT_KEY ="retailerId";
    Bundle bundle;

    private boolean isProfileView;

    private Context context;
    private String retailerId="",channelName;
    private int channelid ;
    private boolean isShowAttribute;

    private StepperLayout mStepperLayout;

    public static int selectedChannelId;

    @Inject
    IProfileContractor.IProfilePresenter<IProfileContractor.IProfileView> profilePresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(getActivity());

        this.context = context;
    }

    @Override
    public void initializeDi() {
        DaggerProfileComponent.builder()
                .ivyAppComponent(((BusinessModel) context.getApplicationContext()).getComponent())
                .profileModule(new ProfileModule(this))
                .build().inject(this);
        setBasePresenter((BasePresenter) profilePresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.profile_base_fragment;
    }

    @Override
    public void init(View view) {

        selectedChannelId = 0 ;

        initializeItem(view);
    }

    @Override
    protected void getMessageFromAliens() {
        getBundleValues();
    }

    @Override
    protected void setUpViews() {

        mStepperLayout.setAdapter(new ProfileStepperAdapter(
                ((FragmentActivity)context).getSupportFragmentManager(),
                context,retailerId,isShowAttribute,
                bmodel.configurationMasterHelper.IS_CONTACT_TAB,isFromEditProfileView,isProfileView,channelid,channelName));

        mStepperLayout.setListener(this);

    }

    private void initializeItem(View view) {

        if (((AppCompatActivity)context).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setTitle(null);
            Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        }

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mStepperLayout = view.findViewById(R.id.stepperLayout);

        mStepperLayout.setOffscreenPageLimit(3);
    }

    private void getBundleValues() {
        Bundle bundle = getArguments();

        if (bundle == null)
            bundle = ((Activity)context).getIntent().getExtras();

        if (bundle != null) {
            isFromEditProfileView = bundle.getBoolean("isEdit", false);
            if (isFromEditProfileView) {
                setScreenTitle(getResources().getString(R.string.profile_edit_screen__title));
            } else {
                if (bundle.getString("screentitle") == null)
                    setScreenTitle(bmodel.getMenuName("MENU_NEW_RETAILER"));
                else
                    setScreenTitle(bundle.getString("screentitle"));
            }

            if (bmodel.configurationMasterHelper.IS_CONTACT_TAB) {

                int screenMode = ((Activity) context).getIntent().getIntExtra(SCREEN_MODE_INTENT_KEY, 0);
                retailerId = bundle.getString(RETAILERID_INTENT_KEY, "");

                if (screenMode == 1)
                    isProfileView = true;

                bmodel.newOutletHelper.setRetailerContactList(new ArrayList<>());
                if (isFromEditProfileView) {
                    bmodel.newOutletHelper.setRetailerContactList(bmodel.profilehelper.getContactBos(retailerId, isFromEditProfileView));
                }
            }

            channelid  = bundle.getInt("channelid",0);
            if(bundle.containsKey("channelName"))
                channelName = bundle.getString("channelName","");
        }

        int attribAvail = bmodel.profilehelper.isConfigAvail("RETAILER_PROFILE", ProfileConstant.ATTRIBUTE);

        if (attribAvail > 0)
            isShowAttribute = true;
    }

    @Override
    public void onCompleted(View completeButton) {
    }

    @Override
    public void onError(VerificationError verificationError) {
    }

    @Override
    public void onStepSelected(int newStepPosition) {

    }

    @Override
    public void onReturn() {
    }

    @Override
    public void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    private ProfileBaseBo profileFieldsBo = new ProfileBaseBo();

    @Subscribe
    public void onMessageEvent(Object retailerProfileField) {
        ProfileBaseBo profileBaseBo = (ProfileBaseBo)retailerProfileField;

        if (profileBaseBo.getFieldName().equalsIgnoreCase("Profile"))
            this.profileFieldsBo.setProfileFields(profileBaseBo.getProfileFields());
        else if (profileBaseBo.getFieldName().equalsIgnoreCase("Contact"))
            this.profileFieldsBo.setContactList(profileBaseBo.getContactList());
        else if (profileBaseBo.getFieldName().equalsIgnoreCase("Attribute"))
            this.profileFieldsBo.setAttributeList(profileBaseBo.getAttributeList());

        if (profileBaseBo.getStatus().equalsIgnoreCase("Save"))
            profilePresenter.saveProfileData(profileFieldsBo);
    }

    @Override
    public void showSuccessMessage() {
        showMessage("Saved Successfully");
    }

    @Override
    public void showFailureMessage() {
        showMessage("Save Failed");
    }

    @Override
    public void showAlert() {
        showAlertDialog(getResources().getString(R.string.profile_updated_scccess));
    }

    private void showAlertDialog(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);
        builder.setCancelable(false);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                ((Activity) context).finish();
            }
        });
        applyAlertDialogTheme(getActivity(), builder);
    }

}
