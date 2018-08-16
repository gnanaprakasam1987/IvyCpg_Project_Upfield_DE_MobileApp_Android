package com.ivy.sd.png.view;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.edit.view.ProfileEditFragmentNew;


public class NewoutletContainerFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    TabLayout tabLayout;
    Bundle bundle;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_newoutlet_container, container, false);

        initializeItem(view);

        return view;
    }

    boolean isFromEditProfileView;
    private void initializeItem(View view) {

        try {
            if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
                ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            }
            bundle = getArguments();
            if (bundle == null)
                bundle = getActivity().getIntent().getExtras();

            tabLayout = view.findViewById(R.id.tab_layout);
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();

            if (bundle != null) {

                isFromEditProfileView = bundle.getBoolean("isEdit", false);

                if(isFromEditProfileView){
                    setScreenTitle(getResources().getString(R.string.profile_edit_screen__title));
                }else{
                    if (bundle.getString("screentitle") == null)
                        setScreenTitle(bmodel.getMenuName("MENU_NEW_RETAILER"));
                    else
                        setScreenTitle(bundle.getString("screentitle"));
                }

                if (isFromEditProfileView) {
                    tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.profile_edit_screen__title)));
                    transaction.replace(R.id.fragment_container, new ProfileEditFragmentNew());
                } else {
                    tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.outlet)));

                    transaction.replace(R.id.fragment_container, new NewOutletFragment());
                }

                tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.contact)));

                transaction.addToBackStack(null);
                transaction.commit();
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
                tabLayout.setTabMode(TabLayout.MODE_FIXED);

                tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                        if (tab.getPosition() == 0) {
                            if(isFromEditProfileView){
                                transaction.replace(R.id.fragment_container, new ProfileEditFragmentNew());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }else {
                                transaction.replace(R.id.fragment_container, new NewOutletFragment());
                                transaction.addToBackStack(null);
                                transaction.commit();
                            }
                        } else if (tab.getPosition() == 1) {
                            Fragment fragment = new ContactCreationFragment();
                            Bundle bundle = new Bundle();
                            bundle.putBoolean("isEdit",isFromEditProfileView);
                            fragment.setArguments(bundle);
                            transaction.replace(R.id.fragment_container, fragment);
                            transaction.addToBackStack(null);
                            transaction.commit();
                        }
                    }

                    @Override
                    public void onTabUnselected(TabLayout.Tab tab) {}

                    @Override
                    public void onTabReselected(TabLayout.Tab tab) {}
                });

            }

        } catch (Exception e) {
            setScreenTitle(getResources().getString(R.string.new_retailer));
            Commons.printException(e);
        }

    }

}
