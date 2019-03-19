package com.ivy.sd.png.view;


import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.cpg.view.retailercontact.ContactCreationFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.profile.edit.view.ProfileEditFragmentNew;

import java.util.ArrayList;
import java.util.List;


public class NewoutletContainerFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
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

    class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager manager) {
            super(manager);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }
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

                ViewPager viewPager = view.findViewById(R.id.pager);

                ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

                if (isFromEditProfileView)
                    adapter.addFragment(new ProfileEditFragmentNew(), getResources().getString(R.string.profile_edit_screen__title));
                else
                    adapter.addFragment(new NewOutletFragment(), getResources().getString(R.string.outlet));

                if (bmodel.configurationMasterHelper.IS_CONTACT_TAB) {
                    adapter.addFragment(ContactCreationFragment.getInstance(isFromEditProfileView), getResources().getString(R.string.contact));
                }

                viewPager.setAdapter(adapter);

                TabLayout tabLayout = view.findViewById(R.id.tab_layout);
                tabLayout.setupWithViewPager(viewPager);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
