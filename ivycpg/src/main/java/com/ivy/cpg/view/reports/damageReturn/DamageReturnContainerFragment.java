package com.ivy.cpg.view.reports.damageReturn;


import android.content.Context;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;


public class DamageReturnContainerFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    CompositeDisposable compositeDisposable;
    View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
         view = inflater.inflate(R.layout.fragment_newoutlet_container, container, false);
        getContractDate();
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



    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed())
            compositeDisposable.clear();

    }

    private void getContractDate() {

        compositeDisposable = new CompositeDisposable();
        compositeDisposable.add((Disposable) DamageReturenReportHelper.getInstance().downloadPendingDeliveryReport(getActivity())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<PendingDeliveryBO>>() {
                    @Override
                    public void onNext(ArrayList<PendingDeliveryBO> mcontractList) {

                        DamageReturenReportHelper.getInstance().setPandingDeliveryBOS(mcontractList);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_data), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        addFragment();
                    }
                }));
    }

    private void addFragment(){
        ViewPager viewPager = view.findViewById(R.id.pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());

        adapter.addFragment(new DamageReturnPendingFragment(), getResources().getString(R.string.pending));
        adapter.addFragment(new DamageReturnCompletedFragment(), getResources().getString(R.string.complete));

        viewPager.setAdapter(adapter);

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);

    }

}
