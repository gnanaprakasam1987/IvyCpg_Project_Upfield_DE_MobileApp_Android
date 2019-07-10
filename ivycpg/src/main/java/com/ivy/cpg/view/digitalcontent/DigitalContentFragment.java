package com.ivy.cpg.view.digitalcontent;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.core.view.GravityCompat;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager.widget.ViewPager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.initiative.InitiativeActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.cpg.view.order.scheme.SchemeApply;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DigitalContentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

/**
 * Digital content module fragment
 * This Fragment is used for both seller and retailer wise digital content module.
 */
public class DigitalContentFragment extends IvyBaseFragment implements BrandDialogInterface, FiveLevelFilterCallBack {

    private BusinessModel mBModel;
    DigitalContentHelper mDigitalContentHelper;

    private ArrayList<DigitalContentBO> mDigitalContentList;

    private DrawerLayout mDrawerLayout;
    private TabLayout tabLayout;
    private View view;

    private int mSelectedTab = 0;
    private static final String BRAND = "Brand";
    private boolean isClicked = false;
    public int mScreenWidth = 0, mScreenHeight = 0;
    private int isImg = 1, isAudio = 2, isVideo = 3, isXls = 4, isPDF = 5, isOthers = 6;
    private int mImgCount = 0, mAudioCount = 0, mVideoCount = 0, mXlsCount = 0, mPDFCount = 0, mOthersCount = 0;
    private String calledFrom = "", screenCode = "MENU_STK_ORD";
    private final String MENU_Init = "Initiative";
    private String mScreenTitle = "Digital Content";
    private static final String MENU_DGT_SW = "MENU_DGT_SW";
    private static final String MENU_DGT = "MENU_DGT";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) Objects.requireNonNull(getActivity()).getApplicationContext();
        mBModel.setContext(getActivity());
        mDigitalContentHelper = DigitalContentHelper.getInstance(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_digital_content, container, false);

        DisplayMetrics displaymetrics = new DisplayMetrics();
        Objects.requireNonNull(getActivity()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;
        mScreenHeight = displaymetrics.heightPixels;

        Bundle extras = getArguments();
        if (extras == null) {
            extras = getActivity().getIntent().getExtras();
        }
        if (extras != null) {
            screenCode = extras.getString("ScreenCode");
            calledFrom = extras.getString("FromInit");
            screenCode = screenCode != null ? screenCode : "";
            calledFrom = calledFrom != null ? calledFrom : "Digi";
        }

        FrameLayout drawer = view.findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Set title to action bar
        if (extras != null) {
            mScreenTitle = extras.getString("screentitle");
        }
        //If this screen is called from Menu item or Order flow..
        if (screenCode.equals("MENU_STK_ORD")
                || screenCode.equals("MENU_ORDER") || calledFrom.equals("Digi"))
            mScreenTitle = mBModel.configurationMasterHelper
                    .getHomescreentwomenutitle(MENU_DGT);


        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {

                if (((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar() != null) {
                    setScreenTitle(mScreenTitle);
                    getActivity().supportInvalidateOptionsMenu();
                }
            }

            public void onDrawerOpened(View drawerView) {

                if (((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                    getActivity().supportInvalidateOptionsMenu();
                }
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);

        new LoadAsyncTask(-1).execute();

        LinearLayout footer = view.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);
        Button btnClose = view.findViewById(R.id.btn_close);
        if (mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            btnClose.setVisibility(View.VISIBLE);

            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(Objects.requireNonNull(getActivity()).getApplicationContext(), getActivity(),
                            "", getActivity().getResources().getString(R.string.move_next_activity),
                            false, getActivity().getResources().getString(R.string.ok),
                            getActivity().getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                            Bundle extras = getActivity().getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", true);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
                            getActivity().finish();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    }).show();

                }
            });
        } else {
            btnClose.setVisibility(View.GONE);
        }
        Button btn_next = view.findViewById(R.id.btn_next);
        if (MENU_Init.equals(calledFrom)) {
            btn_next.setVisibility(View.VISIBLE);
            btn_next.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {
                    click(2);
                }
            });
        } else {
            btn_next.setVisibility(View.GONE);
        }
        if (!mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY && !MENU_Init.equals(calledFrom)) {
            footer.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        isClicked = false;

        if (mBModel.labelsMasterHelper
                .applyLabels((Object) "menu_dgt") != null)
            mScreenTitle = mBModel.labelsMasterHelper
                    .applyLabels((Object) "menu_dgt");

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(mScreenTitle);

            //For Other Digital content fragments
            mDigitalContentHelper.mSelectedActivityName = mScreenTitle;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_only_next, menu);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_next).setVisible(false);


        menu.findItem(R.id.menu_fivefilter).setVisible(true);

        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }
        if (calledFrom.equalsIgnoreCase("MENU_DGT_SW") || calledFrom.equalsIgnoreCase("DigiCS")
                || !mDigitalContentHelper.isProductMapped()) {
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
        }

        if (drawerOpen) {
            menu.clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {

                if (screenCode.equals("MENU_DGT_SW")) {
                    Intent intent = new Intent(getActivity(),
                            HomeScreenActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                    getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                } else
                    click(1);
            }
            return true;
        } else if (i == R.id.menu_next) {
            click(2);
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;

        }
        return super.onOptionsItemSelected(item);

    }

    /**
     * Method used to call Five level filter.
     */
    private void FiveFilterFragment() {
        try {

            if (tabLayout != null) {
                mSelectedTab = tabLayout.getSelectedTabPosition();
            }

            mDrawerLayout.openDrawer(GravityCompat.END);

            FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    mBModel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            // set Fragment class Arguments
            FilterFiveFragment<Object> mFragment = new FilterFiveFragment<>();
            mFragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, mFragment, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    /**
     * Click action
     *
     * @param action Action type
     */
    public void click(int action) {
        if (!isClicked) {
            isClicked = true;
            if (action == 1) {

                SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getActivity().getApplicationContext());
                mDigitalContentHelper.setIsDigitalContent();
                mDigitalContentHelper.setDigitalContentInDB(getActivity().getApplicationContext());
                mBModel.getRetailerMasterBO().setIsDigitalContent("Y");

                switch (calledFrom) {
                    case MENU_Init:

                        if (mBModel.configurationMasterHelper.IS_INITIATIVE) {
                            mBModel.outletTimeStampHelper
                                    .updateTimeStampModuleWise(DateTimeUtils
                                            .now(DateTimeUtils.TIME));
                            Intent intent = new Intent(getActivity(),
                                    InitiativeActivity.class);
                            intent.putExtra("ScreenCode", screenCode);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                            getActivity().finish();
                        } else if (mBModel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                            Intent init = new Intent(getActivity(),
                                    OrderDiscount.class);
                            init.putExtra("ScreenCode", screenCode);
                            startActivity(init);
                            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                            getActivity().finish();
                        } else if (schemeHelper.IS_SCHEME_ON
                                && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
                            Intent init = new Intent(getActivity(),
                                    SchemeApply.class);
                            init.putExtra("ScreenCode", screenCode);
                            startActivity(init);
                            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                            getActivity().finish();
                        } else {
                            mBModel.outletTimeStampHelper
                                    .updateTimeStampModuleWise(DateTimeUtils
                                            .now(DateTimeUtils.TIME));
                            Intent intent;
                            if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                                intent = new Intent(getActivity(), CatalogOrder.class);
                            } else {
                                intent = new Intent(getActivity(), StockAndOrder.class);
                            }
                            intent.putExtra("OrderFlag", "Nothing");
                            intent.putExtra("ScreenCode", screenCode);
                            startActivity(intent);
                            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        }

                        break;
                    case "Digi": {
                        mBModel.outletTimeStampHelper
                                .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                        Intent intent = new Intent(getActivity(),
                                HomeScreenTwo.class);
                        startActivity(intent);
                        getActivity().finish();
                        break;
                    }
                    case "DigiCS": {
                        Intent intent = new Intent(getActivity(),
                                HomeScreenActivity.class).putExtra("menuCode", "MENU_COUNTER");
                        startActivity(intent);
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        getActivity().finish();
                        break;
                    }
                    case "FloatDigi":
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        getActivity().finish();
                }
            } else if (action == 2) {
                mDigitalContentHelper.setIsDigitalContent();
                mDigitalContentHelper.setDigitalContentInDB(getContext().getApplicationContext());
                mBModel.getRetailerMasterBO().setIsDigitalContent("Y");
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));
                Intent i = new Intent(getActivity(),
                        OrderSummary.class);
                i.putExtra("ScreenCode", screenCode);
                startActivity(i);
                getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                getActivity().finish();
            }
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int pid) {
        // Close the drawer
        mDrawerLayout.closeDrawers();
        new LoadAsyncTask(pid).execute();


    }

    @Override
    public void updateGeneralText(String mFilterText) {


    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    public HashMap<Integer, Integer> mSelectedIdByLevelId;

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();

        if (mFilteredPid != 0 && !mFilterText.equalsIgnoreCase("")) {
            new LoadAsyncTask(mFilteredPid).execute();
        } else {
            new LoadAsyncTask(-1).execute();
        }


    }

    /**
     * Load images in the view
     */
    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private ProgressDialog progressDialogue;
        private int pid;
        Vector<DigitalContentBO> items;
        int size;
        ArrayList<DigitalContentBO> mImageList;

        private LoadAsyncTask(int pid) {
            super();
            this.pid = pid;
        }


        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                mDigitalContentList = new ArrayList<>();
                mImageList = new ArrayList<>();
                items = mDigitalContentHelper.getDigitalMaster();
                Activity activity = getActivity();
                if (activity != null && isAdded())
                    if (items == null) {
                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
                                mBModel.showAlert(getResources().getString(R.string.no_data_exists),
                                        0);
                            }
                        });
                        return false;
                    }
                size = items.size();


                loadDigitalContentByType(pid);
                /*
                 * loop through the mImageList size
                 * check the loop count is less than mDigitalContentList size
                 * if yes add the mImageList element to the loop count position
                 * increase loop count to 5
                 * if no add the mImageList element at end of mDigitalContentList.
                 */
                int loopCount = 0;
                mImgCount = 0;
                mAudioCount = 0;
                mVideoCount = 0;
                mXlsCount = 0;
                mPDFCount = 0;
                mOthersCount = 0;
                for (int j = 0; j < mImageList.size(); j++) {
                    int mSize = mDigitalContentList.size();
                    if (loopCount < mSize) {
                        if (mImageList.get(j).isLessimagewidth())//compares image width to screen width
                        {
                            mDigitalContentList.add(mImageList.get(j));
                        } else {
                            mDigitalContentList.add(loopCount, mImageList.get(j));
                            loopCount += 5;
                        }
                    } else {
                        mDigitalContentList.add(mImageList.get(j));
                    }
                }
                //for sorting types of files in group
                Collections.sort(mDigitalContentList, DigitalContentBO.imgFileCompartor);
                mDigitalContentHelper.setFilteredDigitalMaster(mDigitalContentList);


                if (mDigitalContentList.size() > 0) {
                    for (int i = 0; i < mDigitalContentList.size(); i++) {
                        if (mDigitalContentList.get(i).getImgFlag() == isImg)
                            mImgCount++;
                        else if (mDigitalContentList.get(i).getImgFlag() == isAudio)
                            mAudioCount++;
                        else if (mDigitalContentList.get(i).getImgFlag() == isVideo)
                            mVideoCount++;
                        else if (mDigitalContentList.get(i).getImgFlag() == isXls)
                            mXlsCount++;
                        else if (mDigitalContentList.get(i).getImgFlag() == isPDF)
                            mPDFCount++;
                        else
                            mOthersCount++;
                    }
                }

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.loading),
                    true, false);
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            progressDialogue.dismiss();

            tabLayout = view.findViewById(R.id.tab_layout);
            if (tabLayout != null) {
                tabLayout.removeAllTabs();
                tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            }

            ViewPager viewPager = view.findViewById(R.id.pager);
            PagerAdapter adapter = new PagerAdapter
                    (getChildFragmentManager());
            if (mImgCount > 0)
                adapter.addFragment(new DigitalContentImagesFragment(), getResources().getString(R.string.tab_text_images) + ":" + mImgCount);
            if (mAudioCount > 0)
                adapter.addFragment(new DigitalContentAudioFragment(), getResources().getString(R.string.tab_text_audio) + ":" + mAudioCount);
            if (mVideoCount > 0)
                adapter.addFragment(new DigitalContentVideoFragment(), getResources().getString(R.string.tab_text_video) + ":" + mVideoCount);
            if (mXlsCount > 0)
                adapter.addFragment(new DigitalContentXlsFragment(), getResources().getString(R.string.tab_text_xls) + ":" + mXlsCount);
            if (mPDFCount > 0)
                adapter.addFragment(new DigitalContentPdfFragment(), getResources().getString(R.string.tab_text_pdf) + ":" + mPDFCount);
            if (mOthersCount > 0)
                adapter.addFragment(new DigitalContentOthersFragment(), getResources().getString(R.string.tab_text_others) + ":" + mOthersCount);

            if (viewPager != null) {
                viewPager.setAdapter(adapter);
                tabLayout.setupWithViewPager(viewPager);
                viewPager.setCurrentItem(mSelectedTab);
                changeTabsFont();
            }


        }

        private void changeTabsFont() {

            ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
            int tabsCount = vg.getChildCount();
            for (int j = 0; j < tabsCount; j++) {
                ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
                int tabChildCount = vgTab.getChildCount();
                for (int i = 0; i < tabChildCount; i++) {
                    View tabViewChild = vgTab.getChildAt(i);
                    if (tabViewChild instanceof TextView) {
                        ((TextView) tabViewChild).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    }
                }
            }
        }

        /**
         * Preparing list based on digital content type
         *
         * @param pid productId
         */
        private void loadDigitalContentByType(int pid) {
            for (int i = 0; i < size; ++i) {
                DigitalContentBO ret = items.elementAt(i);
                if (pid != -1 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !ret.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (ret.getParentHierarchy().contains("/" + pid + "/") || pid == -1) {
                    if ((ret.getFileName().endsWith(".png") || ret.getFileName().endsWith(".jpeg")
                            || ret.getFileName().endsWith(".jpg") || ret.getFileName().endsWith(".JPG")
                            || ret.getFileName().endsWith(".PNG"))) {

                        ret.setTextbgcolor(1073741824); //decimal code of 40% of black
                        ret.setTextcolor(Color.BLACK);

                        ret.setImgFlag(isImg);
                        mImageList.add(ret);

                    } else {
                        if (ret.getFileName().endsWith("xls")
                                || ret.getFileName().endsWith("xlsx")) {
                            ret.setTextbgcolor(1713404746); //decimal code of 40% of green
                            ret.setImgFlag(isXls);
                        } else {
                            ret.setTextbgcolor(1724731969); //decimal code of 40% of red
                            if (ret.getFileName().endsWith("pdf"))
                                ret.setImgFlag(isPDF);
                            else if (ret.getFileName().endsWith(".mp4")
                                    || ret.getFileName().endsWith("3gp"))
                                ret.setImgFlag(isVideo);
                            else if (ret.getFileName().endsWith(".mp3")
                                    || ret.getFileName().endsWith(".wma")
                                    || ret.getFileName().endsWith(".wav")
                                    || ret.getFileName().endsWith(".ogg"))
                                ret.setImgFlag(isAudio);
                            else
                                ret.setImgFlag(isOthers);
                        }
                        ret.setTextcolor(Color.WHITE);

                        mDigitalContentList.add(ret);

                    }
                }
            }
        }
    }


    /**
     * Loading separate fragment for all digital content types
     */
    public class PagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        private PagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }


        @Override
        public int getCount() {
            return mFragmentList.size();
        }

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mDigitalContentHelper.clearInstance();
        unbindDrawables(view.findViewById(R.id.root));
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
    }

}
