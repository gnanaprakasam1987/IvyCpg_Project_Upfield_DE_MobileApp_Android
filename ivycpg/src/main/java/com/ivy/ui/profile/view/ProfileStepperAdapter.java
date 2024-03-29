package com.ivy.ui.profile.view;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;

import com.ivy.cpg.view.retailercontact.ContactCreationFragment;
import com.ivy.cpg.view.retailercontact.RetailerContactFragment;
import com.ivy.ui.profile.attribute.view.ProfileAttributeFragment;
import com.ivy.ui.profile.create.view.NewOutletFragmentNew;
import com.ivy.ui.profile.edit.view.ProfileEditFragmentNew;
import com.stepstone.stepper.Step;
import com.stepstone.stepper.adapter.AbstractFragmentStepAdapter;
import com.stepstone.stepper.viewmodel.StepViewModel;


public class ProfileStepperAdapter extends AbstractFragmentStepAdapter {

    private int pageCount;
    private String retailerId;
    private boolean isAttribAvail, isContactAvail, isProfileEditView, isProfileViewOnly;

    private int channelId;
    private String channelName;

    public ProfileStepperAdapter(FragmentManager fm, Context context, String retailerId,
                          boolean isAttribAvail, boolean isContactAvail,
                          boolean isProfileEditView, boolean isProfileViewOnly,int channelId,String channelName) {
        super(fm, context);

        if (isAttribAvail && isContactAvail)
            this.pageCount = 3;
        else if (isAttribAvail || isContactAvail)
            this.pageCount = 2;
        else
            this.pageCount = 1;

        this.isAttribAvail = isAttribAvail;
        this.isContactAvail = isContactAvail;
        this.isProfileEditView = isProfileEditView;
        this.isProfileViewOnly = isProfileViewOnly;
        this.retailerId = retailerId;

        this.channelId = channelId;
        this.channelName = channelName;
    }

    @Override
    public Step createStep(int position) {

        switch (position) {
            case 0:
                if (isProfileEditView)
                    return new ProfileEditFragmentNew();
                else {
                    NewOutletFragmentNew newOutletFragmentNew = new NewOutletFragmentNew();
                    Bundle bundleNewoutLet = new Bundle();
                    bundleNewoutLet.putInt("channelid", channelId);
                    bundleNewoutLet.putString("channelName", channelName);
                    newOutletFragmentNew.setArguments(bundleNewoutLet);
                    return newOutletFragmentNew;

                }
            case 1:
                if (isAttribAvail)
                    return new ProfileAttributeFragment();
                else
                    return ContactCreationFragment.getInstance(isProfileEditView);
            case 2:
                if (isProfileViewOnly){
                    Bundle bundle = new Bundle();
                    bundle.putString("RetailerId",retailerId);
                    RetailerContactFragment retailerContactFragment = new RetailerContactFragment();
                    retailerContactFragment.setArguments(bundle);
                    return retailerContactFragment;
                }
                else
                    return ContactCreationFragment.getInstance(isProfileEditView);
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return pageCount;
    }

    @NonNull
    @Override
    public StepViewModel getViewModel(@IntRange(from = 0) int position) {

        StepViewModel.Builder builder = new StepViewModel.Builder(context);
        switch (position) {
            case 0:
                if (pageCount >= 2)
                    addBuilder(builder, "Profile");
                else
                    addBuilderComplete(builder, "Profile");
                break;
            case 1:
                if (pageCount == 3)
                    addBuilder(builder, "Attributes");
                else if (isAttribAvail)
                    addBuilderComplete(builder, "Attributes");
                else if (isContactAvail)
                    addBuilderComplete(builder, "Contact");
                break;
            case 2:
                addBuilderComplete(builder, "Contact");
                break;
            default:
                throw new IllegalArgumentException("Unsupported position: " + position);
        }
        return builder.create();
    }

    private void addBuilderComplete(StepViewModel.Builder builder, String profile) {
        builder
                .setTitle(profile)
                .setBackButtonLabel("Back")
                .setEndButtonLabel("Complete");
    }

    private void addBuilder(StepViewModel.Builder builder, String profile) {
        builder
                .setTitle(profile)
                .setEndButtonLabel("Next")
                .setBackButtonLabel("Back");
    }
}
