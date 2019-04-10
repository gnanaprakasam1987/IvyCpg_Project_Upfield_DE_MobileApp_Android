package com.ivy.cpg.view.dashboard;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KellogsDashboardFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private FragmentManager fm;

    private TabLayout tabLayout;
    private String mSelectedTab;
    private RecyclerView rvwplist;
    private View view;
    private ScrollView scrollView1;
    private TableLayout table_main;

    MyPagerAdapter adapterViewPager;
    ViewPager vpPager;

    private String CODE_PLATFORM = "PLATFORM";
    private String CODE_PRIORITY = "PRIORITY";

    private DashBoardHelper dashBoardHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        dashBoardHelper = DashBoardHelper.getInstance(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment__klgs_dashboard, container, false);

        viewInitialise(view);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        setUpActionBar();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    mSelectedTab = (String) tab.getTag();
                    new LoadAsyncTask().execute();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }


    private void viewInitialise(View view) {
        vpPager = (ViewPager) view.findViewById(R.id.viewpager);
        scrollView1 = (ScrollView) view.findViewById(R.id.scrollView1);
        table_main = (TableLayout) view.findViewById(R.id.table_main);

        rvwplist = (RecyclerView) view.findViewById(R.id.dashboardRv);
        rvwplist.setHasFixedSize(false);
        rvwplist.setNestedScrollingEnabled(false);
        rvwplist.setLayoutManager(new LinearLayoutManager(getActivity()));


        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        float scale = getContext().getResources().getDisplayMetrics().widthPixels;
        ArrayList<StandardListBO> mTabs = dashBoardHelper.getDashTabs();
        scale = scale / mTabs.size();
        TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);

        StandardListBO standardListBO;
        for (int i = 0; i < mTabs.size(); i++) {
//        for(int i=0;i<3;i++){
            standardListBO = mTabs.get(i);
            TabLayout.Tab tab = tabLayout.newTab();

            TextView txtVw = new TextView(getActivity());
            txtVw.setGravity(Gravity.CENTER);
            txtVw.setWidth((int) scale);
            txtVw.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            txtVw.setTextColor(color);
            txtVw.setMaxLines(1);
            txtVw.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            txtVw.setText(standardListBO.getListName());
            txtVw.setAllCaps(true);

            tab.setTag(standardListBO.getListCode());
            tab.setCustomView(txtVw);
            tabLayout.addTab(tab);
        }
        changeTabsFont();
        mSelectedTab = (String) tabLayout.getTabAt(0).getTag();
        new LoadAsyncTask().execute();
    }

    private void setUpActionBar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        }

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));
            bmodel.saveModuleCompletion("MENU_DASH_KELGS_ACT", true);
            Intent j = new Intent(getActivity(), HomeScreenTwo.class);
            startActivity(j);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {

            if (fm != null)
                fm = null;
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void changeTabsFont() {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }
            }
        }
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
        private final List<SKUWiseTargetBO> items;

        public MyAdapter(List<SKUWiseTargetBO> items) {
            this.items = items;
        }

        @Override
        public MyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_klgsskuwisetgt, parent, false);
            return new MyAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final MyAdapter.ViewHolder holder, final int position) {
            SKUWiseTargetBO product = items.get(position);

            holder.productbo = product;

            //typefaces
            holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.acheived.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.sales.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.targetTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.acheivedTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.salesTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.psname.setText(holder.productbo.getProductName());

            holder.psname.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    dashBoardHelper.mSelectedSkuIndex = position;
                    adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager());
                    vpPager.setAdapter(adapterViewPager);
                }
            });

            holder.target.setText("" + product.getTarget());
            holder.acheived.setText("" + product.getAchieved());
            holder.sales.setText("" + product.getrField() + "%");

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView psname;
            TextView target, targetTitle;
            TextView acheived, acheivedTitle;
            TextView sales, salesTitle;
            SKUWiseTargetBO productbo;
            View rowDotBlue, rowDotGreen, rowDotOrange, verticalSeparatorTarget, verticalSeparatorSales;

            public ViewHolder(View row) {
                super(row);
                psname = (TextView) row
                        .findViewById(R.id.factorName_dashboard_tv);
                target = (TextView) row
                        .findViewById(R.id.target_dashboard_tv);
                acheived = (TextView) row
                        .findViewById(R.id.acheived_dashboard_tv);
                sales = (TextView) row
                        .findViewById(R.id.sales_dashboard_tv);
                targetTitle = (TextView) row
                        .findViewById(R.id.target_title);
                acheivedTitle = (TextView) row
                        .findViewById(R.id.achived_title);
                salesTitle = (TextView) row
                        .findViewById(R.id.sales_title);
                rowDotBlue = (View) row
                        .findViewById(R.id.row_dot_blue);
                rowDotGreen = (View) row
                        .findViewById(R.id.row_dot_green);
                rowDotOrange = (View) row
                        .findViewById(R.id.row_dot_orange);
                verticalSeparatorTarget = (View) row
                        .findViewById(R.id.verticalSeparatorTarget);
                verticalSeparatorSales = (View) row
                        .findViewById(R.id.verticalSeparatorSales);


            }
        }
    }

    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if (mSelectedTab.equals(CODE_PLATFORM))
                    dashBoardHelper.loadPlatformDashboardData();

                else if (mSelectedTab.equals(CODE_PRIORITY)) {
                    dashBoardHelper.loadPriorityDashboardData();
                    initTable();
                } else {
                    dashBoardHelper.loadKlgsDashboardData(mSelectedTab);
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            alertDialog.dismiss();

            if (!mSelectedTab.equals(CODE_PRIORITY)) {
                scrollView1.setVisibility(View.GONE);
                table_main.removeAllViews();
                rvwplist.setVisibility(View.VISIBLE);
                vpPager.setVisibility(View.VISIBLE);
                dashBoardHelper.mSelectedSkuIndex = 0;
                MyAdapter mSchedule = new MyAdapter(dashBoardHelper.getSkuWiseTargetList());
                rvwplist.setAdapter(mSchedule);
                adapterViewPager = new MyPagerAdapter(getActivity().getSupportFragmentManager());
                vpPager.setAdapter(adapterViewPager);
            } else {
                rvwplist.setVisibility(View.GONE);
                vpPager.setVisibility(View.GONE);
                scrollView1.setVisibility(View.VISIBLE);
            }
        }
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private int NUM_ITEMS = 1;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0: // Fragment # 0 - This will show FirstFragment
                    return new KlgsHalfPieChartFragement();
               /* case 1: // Fragment # 0 - This will show FirstFragment different title
                    return new KlgsHalfPieChartFragement();*/
                default:
                    return null;
            }
        }

    }

    public void initTable() {
        TableRow tbRowHeader = new TableRow(getActivity());
        tbRowHeader.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.list_title_bg_color));
        tbRowHeader.setMinimumHeight(getResources().getDimensionPixelSize(R.dimen.list_header_height));
        TableRow.LayoutParams params = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);
        params.setMargins(25, 10, 25, 10);
        TextView tv0 = new TextView(getActivity());
        tv0.setText(CODE_PRIORITY);
        tv0.setTextColor(Color.WHITE);
        tv0.setGravity(Gravity.CENTER);
        tv0.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv0.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_secondary));
        tbRowHeader.addView(tv0, params);
        for (int i = 0; i < dashBoardHelper.productsIds.size(); i++) {
            TextView tv1 = new TextView(getActivity());
            tv1.setId(i + 20);
            tv1.setText(dashBoardHelper.getProductName(dashBoardHelper.productsIds.get(i)));
            tv1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tv1.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_secondary));
            tv1.setTextColor(Color.WHITE);
            tv0.setGravity(Gravity.CENTER);
            tbRowHeader.addView(tv1, params);
        }
        table_main.addView(tbRowHeader);

        Collections.sort(dashBoardHelper.priorityIds, dashBoardHelper.productIdsComparator);

        for (int i = 0; i < dashBoardHelper.priorityIds.size(); i++) {
            TableRow tbRowItems = new TableRow(getActivity());
            TextView t1v = new TextView(getActivity());
            t1v.setText("" + dashBoardHelper.priorityIds.get(i));
            t1v.setTextColor(Color.BLACK);
            t1v.setGravity(Gravity.CENTER);
            t1v.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            t1v.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_primary));
            tbRowItems.addView(t1v, params);
            for (int j = 0; j < dashBoardHelper.productsIds.size(); j++) {
                TextView t2v = new TextView(getActivity());
                t2v.setId(j + 25);
                t2v.setText("" + dashBoardHelper.getProductAch(dashBoardHelper.productsIds.get(j), dashBoardHelper.priorityIds.get(i)));
                t2v.setTextColor(Color.BLACK);
                t2v.setGravity(Gravity.CENTER);
                t2v.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                t2v.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_primary));
                tbRowItems.addView(t2v, params);
            }
            table_main.addView(tbRowItems);
        }

    }

}
