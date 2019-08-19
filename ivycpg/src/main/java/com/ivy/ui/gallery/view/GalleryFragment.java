package com.ivy.ui.gallery.view;

import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.gallery.GalleryConstant;
import com.ivy.ui.gallery.GalleryContract;
import com.ivy.ui.gallery.GalleryFilterListener;
import com.ivy.ui.gallery.adapter.GalleryViewAdapter;
import com.ivy.ui.gallery.di.DaggerGalleryComponent;
import com.ivy.ui.gallery.di.GalleryModule;
import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class GalleryFragment extends BaseFragment implements GalleryContract.GalleryView, TabLayout.OnTabSelectedListener, GalleryViewAdapter.ItemClickListener, GalleryFilterListener {

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.gallery_recycler_view)
    RecyclerView galleryRecyclerView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @BindView(R.id.right_drawer)
    FrameLayout drawer;

    @BindView(R.id.no_data_tv)
    AppCompatTextView noDataTv;


    private GalleryViewAdapter galleryAdapter;
    private HashMap<String, ArrayList<GalleryBo>> galleryArrayListHashMap = new HashMap<>();
    private ArrayList<String> sectionArrayList = new ArrayList<>();
    private Context context;
    private GridLayoutManager layoutManager;
    private ArrayList<String> selectedFilterList = null;
    private boolean isFilterApplied;

    @Inject
    GalleryContract.GalleryPresenter<GalleryContract.GalleryView> galleryPresenter;

    @Override
    public void initializeDi() {

        DaggerGalleryComponent
                .builder()
                .ivyAppComponent
                        (((BusinessModel) getActivity()
                                .getApplication())
                                .getComponent())
                .galleryModule(new GalleryModule(this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) galleryPresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.gallery_fragment;
    }

    @Override
    public void init(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        setUpToolbar(getString(R.string.gallery));
        setHasOptionsMenu(true);
        setUpTabView();
        setUpRecyclerView();
        setUpDrawerView();
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void setUpTabView() {
        tabLayout.addOnTabSelectedListener(this);
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.current_visit)));
        tabLayout.addTab(tabLayout.newTab().setText(getString(R.string.last_visit)));
    }

    private void setUpRecyclerView() {
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);

        galleryRecyclerView.setHasFixedSize(false);
        galleryRecyclerView.setItemViewCacheSize(200);
        galleryRecyclerView.setDrawingCacheEnabled(true);
        galleryRecyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        if (is7InchTablet)
            layoutManager = new GridLayoutManager(getActivity(), 3);
        else
            layoutManager = new GridLayoutManager(getActivity(), 2);

        galleryRecyclerView.setLayoutManager(layoutManager);
    }

    private void setUpDrawerView() {

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                drawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (drawerLayout != null)
                    setScreenTitle(getString(R.string.gallery));

                getActivity().invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                setScreenTitle(getResources().getString(R.string.filter));

                getActivity().invalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(mDrawerToggle);
    }

    @Override
    public void updateGalleryView(HashMap<String, ArrayList<GalleryBo>> galleryList, ArrayList<String> sectionList) {
        isFilterApplied = false;
        noDataTv.setVisibility(View.GONE);
        galleryRecyclerView.setVisibility(View.VISIBLE);

        galleryArrayListHashMap.clear();
        galleryArrayListHashMap.putAll(galleryList);

        sectionArrayList.clear();
        sectionArrayList.addAll(sectionList);

        galleryAdapter = new GalleryViewAdapter(context, galleryArrayListHashMap, sectionArrayList, this, this);
        galleryAdapter.setLayoutManager(layoutManager);
        galleryRecyclerView.setAdapter(galleryAdapter);
        getActivity().invalidateOptionsMenu();

    }

    @Override
    public void updateFilteredData(HashMap<String, ArrayList<GalleryBo>> galleryList, ArrayList<String> filteredSectionList) {

        galleryArrayListHashMap.clear();
        galleryArrayListHashMap.putAll(galleryList);

        galleryAdapter = new GalleryViewAdapter(context, galleryArrayListHashMap, filteredSectionList, this, this);
        galleryAdapter.setLayoutManager(layoutManager);
        galleryRecyclerView.setAdapter(galleryAdapter);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void showDataNotMappedMsg() {

        galleryArrayListHashMap = new HashMap<>();
        sectionArrayList = new ArrayList<>();
        selectedFilterList = new ArrayList<>();
        getActivity().invalidateOptionsMenu();
        galleryRecyclerView.setVisibility(View.GONE);
        noDataTv.setVisibility(View.VISIBLE);
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        String imgDirectory = ((tab.getPosition() == 0)
                ? FileUtils.photoFolderPath
                : (context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + galleryPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT));

        galleryPresenter.fetchGalleryData(imgDirectory, tab.getPosition() == 1);

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_gallery, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean drawerOpen = false;

        if (drawerLayout != null) {
            drawerOpen = drawerLayout.isDrawerOpen(GravityCompat.END);
        }

        if (isFilterApplied)
            menu.findItem(R.id.menu_gallery_section).getIcon().setColorFilter(ContextCompat.getColor(context, R.color.Orange), PorterDuff.Mode.SRC_ATOP);
        else
            menu.findItem(R.id.menu_gallery_section).getIcon().setColorFilter(ContextCompat.getColor(context, R.color.white), PorterDuff.Mode.SRC_ATOP);

        menu.findItem(R.id.menu_gallery_delete).setVisible(false);
        menu.findItem(R.id.menu_gallery_share).setVisible(false);
        menu.findItem(R.id.menu_gallery_section).setVisible(!galleryArrayListHashMap.isEmpty());

        if (drawerOpen) {
            menu.clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawers();
            else {
                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                startActivity(intent);
                getActivity().finish();
            }
        } else if (item.getItemId() == R.id.menu_gallery_section) {
            showFilter();

        }

        return super.onOptionsItemSelected(item);
    }


    private void showFilter() {
        try {

            drawerLayout.openDrawer(GravityCompat.END);

            FragmentManager fm = getActivity().getSupportFragmentManager();
            GalleryFilterFragment frag = (GalleryFilterFragment) fm
                    .findFragmentByTag(GalleryConstant.FILTER);
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable(GalleryConstant.SECTION_LIST, sectionArrayList);
            bundle.putSerializable(GalleryConstant.FILTERED_LIST, selectedFilterList);
            // set FragmentClass Arguments
            GalleryFilterFragment mFragment = new GalleryFilterFragment();
            mFragment.setArguments(bundle);
            mFragment.setFilterViewListener(this);
            ft.replace(R.id.right_drawer, mFragment, GalleryConstant.FILTER);
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }


    @Override
    public void onItemClicked(ImageView sharedImageView, int sectionPos, int imageViewPos, ArrayList<GalleryBo> galleryBoArrayList) {

        Intent viewIntent = new Intent(context, ImagePagerActivity.class);
        viewIntent.putParcelableArrayListExtra("galleryList", galleryBoArrayList);
        viewIntent.putExtra("curPos", imageViewPos);
        startActivity(viewIntent);
    }

    @Override
    public void apply(ArrayList<String> sectionFilteredList) {

        drawerLayout.closeDrawers();
        selectedFilterList = new ArrayList<>();
        selectedFilterList.addAll(sectionFilteredList);
        galleryPresenter.updateSectionedFilterList(sectionFilteredList);
        isFilterApplied = true;
    }

    @Override
    public void clearAll() {

        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawers();
        selectedFilterList = new ArrayList<>();
        galleryPresenter.updateSectionedFilterList(null);
        isFilterApplied = false;
    }
}
