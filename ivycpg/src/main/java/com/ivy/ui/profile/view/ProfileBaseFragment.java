package com.ivy.ui.profile.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.profile.ProfileConstant;
import com.stepstone.stepper.StepperLayout;
import com.stepstone.stepper.VerificationError;

import java.util.ArrayList;
import java.util.Objects;

public class ProfileBaseFragment extends IvyBaseFragment implements StepperLayout.StepperListener {

    private BusinessModel bmodel;
    private boolean isFromEditProfileView;
    private static final String SCREEN_MODE_INTENT_KEY ="screenMode";
    private static final String RETAILERID_INTENT_KEY ="retailerId";
    private boolean isProfileView;

    private Context context;
    private String retailerId="";
    private boolean isShowAttribute;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(getActivity());

        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_base_fragment, container, false);

        getBundleValues();

        initializeItem(view);

        return view;
    }

    private void initializeItem(View view) {

        if (((AppCompatActivity)context).getSupportActionBar() != null) {
            Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setTitle(null);
            Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
            Objects.requireNonNull(((AppCompatActivity) context).getSupportActionBar()).setDisplayShowHomeEnabled(true);
        }

        StepperLayout mStepperLayout;

        mStepperLayout = view.findViewById(R.id.stepperLayout);

        mStepperLayout.setOffscreenPageLimit(3);

        mStepperLayout.setAdapter(new ProfileStepperAdapter(
                ((FragmentActivity)context).getSupportFragmentManager(),
                context,retailerId,isShowAttribute,
                bmodel.configurationMasterHelper.IS_CONTACT_TAB,isFromEditProfileView,isProfileView));

        mStepperLayout.setListener(this);
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
}
