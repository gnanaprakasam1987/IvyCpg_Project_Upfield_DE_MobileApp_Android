package com.ivy.cpg.view.dashboard;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.survey.QuestionBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.FitScoreBO;
import com.ivy.sd.png.bo.HHTModuleBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

import me.relex.circleindicator.CircleIndicator;


public class FitScoreDashboardFragment extends IvyBaseFragment implements BrandDialogInterface {
    static BusinessModel bmodel;
    FragmentManager fm;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private TextView category_count, no_data;
    private RecyclerView category_recycler, questions_dashboard_recycler;
    private CardView content_card;

    ArrayList<FitScoreBO> fitScoreList = new ArrayList<>();
    int count = 0;
    private View view;
    ViewPager vpPager;
    CollapsingToolbarLayout collapsing;
    CircleIndicator indicator;
    MyPagerAdapter adapterViewPager;
    private ArrayList<Fragment> fragmentList = new ArrayList<>();
    private int selectItem = 0;

    TextView total_weightage_bottom, total_score_bottom;
    String screenTitle = "", title = "", menuCode = "";
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_fitscore_dashboard, container, false);


        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        if (getActivity().getIntent().getExtras() != null) {
            screenTitle = getActivity().getIntent().getExtras().getString("screentitle");
            menuCode = getActivity().getIntent().getExtras().getString("menuCode");
        }
        setHasOptionsMenu(true);
        if (!bmodel.configurationMasterHelper.IS_FITSCORE_NEEDED) {
            Toast.makeText(getActivity(), "Configuration is not Enabled", Toast.LENGTH_LONG).show();
        }
        init();
        return view;
    }

    protected void init() {
        setLocalization();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        content_card = (CardView) view.findViewById(R.id.content_card);

        vpPager = (ViewPager) view.findViewById(R.id.viewpager);
        collapsing = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing);
        indicator = (CircleIndicator) view.findViewById(R.id.indicator);

        category_recycler = (RecyclerView) view.findViewById(R.id.category_recycler);
        questions_dashboard_recycler = (RecyclerView) view.findViewById(R.id.questions_dashboard_recycler);
        category_count = (TextView) view.findViewById(R.id.category_count);

        no_data = (TextView) view.findViewById(R.id.no_data);
        no_data.setVisibility(View.GONE);

        total_weightage_bottom = (TextView) view.findViewById(R.id.total_weightage_bottom);
        total_score_bottom = (TextView) view.findViewById(R.id.total_score_bottom);

        LinearLayoutManager categoryManager = new LinearLayoutManager(getActivity().getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        category_recycler.setLayoutManager(categoryManager);
        LinearLayoutManager questionManager = new LinearLayoutManager(getActivity().getApplicationContext());
        questions_dashboard_recycler.setLayoutManager(questionManager);

        bmodel.fitscoreHelper.getModules();
        Vector<String> categoryNames = new Vector<>();
        for (HHTModuleBO hhtModule : bmodel.fitscoreHelper.getHhtModuleList()) {
            switch (hhtModule.getModule()) {
                case DataMembers.FIT_STOCK:
                    categoryNames.add(DataMembers.MODULE_STOCK);
                    break;
                case DataMembers.FIT_PRICE:
                    categoryNames.add(DataMembers.MODULE_PRICE);
                    break;
                case DataMembers.FIT_ASSET:
                    categoryNames.add(DataMembers.MODULE_ASSET);
                    break;
                case DataMembers.FIT_POSM:
                    categoryNames.add(DataMembers.MODULE_POSM);
                    break;
                case DataMembers.FIT_PROMO:
                    categoryNames.add(DataMembers.MODULE_PROMO);
                    break;
            }
        }

        mSelectedFilterMap.put("Category", "-1");
        content_card.setVisibility(View.GONE);
        category_recycler.setAdapter(new CategoryBrandRecycler(categoryNames));
        mSelectedFilterMap.put("Brand", "0");
        category_count.setText(categoryNames.size() + "");
    }

    @Override
    public void onStart() {
        super.onStart();
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(screenTitle);
        }

        checkandaddScreens(DataMembers.FIT_STOCK);
        adapterViewPager = new MyPagerAdapter(getChildFragmentManager(), fragmentList);
        new setAdapterTask().execute();
        loadSKUandScore(DataMembers.MODULE_STOCK);
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName, List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        //mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {

    }

    static class ViewHolder {
        TextView sno, pName, target, acheived, weightage, score;
        Button btnPhoto;
        QuestionBO dashboardDataObj;
        int mActivityMappingID = 0, mPosition = 0;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 3) {
                // load_PieSemiCircleChart_Data();
            }

        }
    };

    private void backClick() {
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                .now(SDUtil.TIME));
        bmodel.saveModuleCompletion(menuCode);
        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);//HomeScreenTwo.class);
        startActivity(intent);
        getActivity().finish();
    }

    //    /**
//     * By Default this screen should load in Landscape mode, Use BaseActivity means screen taking more to load,
//     * so the same code in BaseActivity and remove the BaseActivity Implementation
//     */
    private void setLocalization() {
        getActivity().getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());

        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", ApplicationConfigs.LANGUAGE))) {
            locale = new Locale(sharedPrefs.getString("languagePref", ApplicationConfigs.LANGUAGE));
            Locale.setDefault(locale);
            config.locale = locale;
            getActivity().getBaseContext().getResources().updateConfiguration(config,
                    getActivity().getBaseContext().getResources().getDisplayMetrics());
        }
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    class CategoryBrandRecycler extends RecyclerView.Adapter<CategoryBrandRecycler.ViewHolder> {

        private Vector<String> items;

        public CategoryBrandRecycler(Vector<String> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fitscore_modules_recycler_item, parent, false);
            return new ViewHolder(v);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.categoryObj = items.get(position);
            holder.category_name.setText(holder.categoryObj);
            holder.category_name.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

            holder.category_name.setTextColor(Color.WHITE);
            holder.category_name.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    selectItem = 1;
                    loadSKUandScore(((TextView) v).getText().toString());
                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();//bmodel.mPerfectSurveyHelper.getCategoryQuestionSummary().size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            String categoryObj;
            TextView category_name;

            public ViewHolder(View v) {
                super(v);
                category_name = (TextView) v.findViewById(R.id.category_name);

            }
        }
    }

    private void loadSKUandScore(String s) {
        fitScoreList.clear();
        if (s.equalsIgnoreCase(DataMembers.MODULE_STOCK)) {
            bmodel.fitscoreHelper.getFitScoreforStockandPriceCheck(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_STOCK);
            fitScoreList = bmodel.fitscoreHelper.getFitScoreList();
            if (fitScoreList != null && fitScoreList.size() > 0) {
                questions_dashboard_recycler.setAdapter(new QuestionsRecycler1(fitScoreList, s));
                no_data.setVisibility(View.GONE);
            } else {
                no_data.setVisibility(View.VISIBLE);
            }
            title = DataMembers.MODULE_STOCK;
            checkandaddScreens(DataMembers.FIT_STOCK);
        } else if (s.equalsIgnoreCase(DataMembers.MODULE_PRICE)) {
            bmodel.fitscoreHelper.getFitScoreforStockandPriceCheck(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_PRICE);
            fitScoreList = bmodel.fitscoreHelper.getFitScoreList();
            if (fitScoreList != null && fitScoreList.size() > 0) {
                questions_dashboard_recycler.setAdapter(new QuestionsRecycler1(fitScoreList, s));
                no_data.setVisibility(View.GONE);
            } else {
                no_data.setVisibility(View.VISIBLE);
            }
            title = DataMembers.MODULE_PRICE;
            checkandaddScreens(DataMembers.FIT_PRICE);
        } else if (s.equalsIgnoreCase(DataMembers.MODULE_ASSET)) {
            bmodel.fitscoreHelper.getFitScoreforAssetandPOSM(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_ASSET);
            fitScoreList = bmodel.fitscoreHelper.getFitScoreList();
            if (fitScoreList != null && fitScoreList.size() > 0) {
                questions_dashboard_recycler.setAdapter(new QuestionsRecycler1(fitScoreList, s));
                no_data.setVisibility(View.GONE);
            } else {
                no_data.setVisibility(View.VISIBLE);
            }
            title = DataMembers.MODULE_ASSET;
            checkandaddScreens(DataMembers.FIT_ASSET);
        } else if (s.equalsIgnoreCase(DataMembers.MODULE_POSM)) {
            bmodel.fitscoreHelper.getFitScoreforAssetandPOSM(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_POSM);
            fitScoreList = bmodel.fitscoreHelper.getFitScoreList();
            if (fitScoreList != null && fitScoreList.size() > 0) {
                questions_dashboard_recycler.setAdapter(new QuestionsRecycler1(fitScoreList, s));
                no_data.setVisibility(View.GONE);
            } else {
                no_data.setVisibility(View.VISIBLE);
            }
            title = DataMembers.MODULE_POSM;
            checkandaddScreens(DataMembers.FIT_POSM);
        } else if (s.equalsIgnoreCase(DataMembers.MODULE_PROMO)) {
            bmodel.fitscoreHelper.getFitScoreforPromo(bmodel.getRetailerMasterBO().getRetailerID(), DataMembers.FIT_PROMO);
            fitScoreList = bmodel.fitscoreHelper.getFitScoreList();
            if (fitScoreList != null && fitScoreList.size() > 0) {
                questions_dashboard_recycler.setAdapter(new QuestionsRecycler1(fitScoreList, s));
                no_data.setVisibility(View.GONE);
            } else {
                no_data.setVisibility(View.VISIBLE);
            }
            title = DataMembers.MODULE_PROMO;
            checkandaddScreens(DataMembers.FIT_PROMO);
        }
        double total = 0, weightage = 0;
        for (FitScoreBO fitScore : fitScoreList) {
            if (fitScore.getScore() != null) {
                total = total + SDUtil.convertToDouble(fitScore.getScore());
                weightage = SDUtil.convertToDouble(fitScore.getWeightage());
            }
        }

        total_weightage_bottom.setText(SDUtil.roundIt((total / 100) * weightage, 2) + "");
        total_score_bottom.setText("");
        adapterViewPager = new MyPagerAdapter(getChildFragmentManager(), fragmentList);
        vpPager.setAdapter(adapterViewPager);
        indicator.setViewPager(vpPager);
        vpPager.setCurrentItem(selectItem);
    }

    class QuestionsRecycler1 extends RecyclerView.Adapter<QuestionsRecycler1.ViewHolder> {

        private ArrayList<FitScoreBO> skuList;
        String menu;

        public QuestionsRecycler1(ArrayList<FitScoreBO> skuList, String menu) {
            this.skuList = skuList;
            this.menu = menu;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_fitscore_dashboard_sf, parent, false);
            return new ViewHolder(v);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.fitScoreBO = skuList.get(position);
            holder.txtHeader.setText(skuList.get(position).getHeader());
            holder.txtTarget.setText(skuList.get(position).getTarget());
            holder.txtAchieved.setText(skuList.get(position).getAchieved());
            holder.txtWeightage.setText(skuList.get(position).getWeightage());
            if (skuList.get(position).getScore() != null) {
                holder.txtScore.setText(SDUtil.roundIt(SDUtil.convertToDouble(skuList.get(position).getScore()), 2));
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return skuList.size();//bmodel.mPerfectSurveyHelper.getCategoryQuestionSummary().size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtHeader, txtTarget, txtAchieved, txtWeightage, txtScore;
            FitScoreBO fitScoreBO;

            public ViewHolder(View v) {
                super(v);
                txtHeader = (TextView) v.findViewById(R.id.txtHeader);
                txtTarget = (TextView) v.findViewById(R.id.txtTarget);
                txtAchieved = (TextView) v.findViewById(R.id.txtAchieved);
                txtWeightage = (TextView) v.findViewById(R.id.txtWeightage);
                txtScore = (TextView) v.findViewById(R.id.txtScore);
            }
        }
    }

    public class MyPagerAdapter extends FragmentStatePagerAdapter {
        private ArrayList<Fragment> fragmentList = new ArrayList<>();

        public MyPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragmentList) {
            super(fragmentManager);
            this.fragmentList = fragmentList;
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return this.fragmentList.size();
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return this.fragmentList.get(position);
        }

    }

    private void checkandaddScreens(String Module) {
        //vpPager.removeAllViews();
        fragmentList = new ArrayList<>();
        fragmentList.add(new FitScoreChartFragment().newInstance(bmodel.getRetailerMasterBO().getRetailerID(), "ALL", "ALL"));
        fragmentList.add(new FitScoreChartFragment().newInstance(bmodel.getRetailerMasterBO().getRetailerID(), Module, title));
    }

    private class setAdapterTask extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void... params) {
            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            vpPager.setAdapter(adapterViewPager);
            indicator.setViewPager(vpPager);
            vpPager.setCurrentItem(selectItem);
        }
    }
}

