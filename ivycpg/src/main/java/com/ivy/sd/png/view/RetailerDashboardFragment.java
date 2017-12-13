package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.ConsoleMessage;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DashBoardBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.webviewchart.WebAppInterface;

public class RetailerDashboardFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private int selectedDashItem = 0;
    private int lastPosition = -1;
    private StackedColumnPercentageChartFragment stackPerChartFrag;
    private SemiCirclePieChartFragment semiCircChartFrag;
    private Handler sHandler;
    private AppSectionsPagerAdapter mAppSectionsPagerAdapter;
    private ViewPager mViewPager;
    private ActionBar actionBar;
    private ListView dashBoardList;
    private FragmentManager fm;
    private int pagePos = 0;
    private Button prevButton;
    private Button nextButton;

    private AlertDialog alertDialog;
    private TextView incentivetv;
    private Boolean showinitiavite = true;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_retailer_dashboard, container, false);

        mAppSectionsPagerAdapter = new AppSectionsPagerAdapter(
                getActivity().getSupportFragmentManager());
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
            setUpActionBar();
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        incentivetv = (TextView) view.findViewById(R.id.incentivetv);

        if (!bmodel.configurationMasterHelper.SHOW_INDEX_DASH) {
            view.findViewById(R.id.indextv).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.indextv).getTag()) != null)
                    ((TextView) view.findViewById(R.id.indextv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.indextv)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.factortv).getTag()) != null)
                ((TextView) view.findViewById(R.id.factortv))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.factortv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }

        if (!bmodel.configurationMasterHelper.SHOW_TARGET_DASH) {
            view.findViewById(R.id.targettv).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.targettv).getTag()) != null)
                    ((TextView) view.findViewById(R.id.targettv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.targettv)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_ACHIEVED_DASH) {
            view.findViewById(R.id.achievedtv).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.achievedtv).getTag()) != null)
                    ((TextView) view.findViewById(R.id.achievedtv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.achievedtv)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_BALANCE_DASH) {
            view.findViewById(R.id.balancetv).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.balancetv).getTag()) != null)
                    ((TextView) view.findViewById(R.id.balancetv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.balancetv)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
            view.findViewById(R.id.incentivetv).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.incentivetv).getTag()) != null)
                    ((TextView) view.findViewById(R.id.incentivetv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.incentivetv)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_SCORE_DASH) {
            view.findViewById(R.id.scoretv).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.scoretv).getTag()) != null)
                    ((TextView) view.findViewById(R.id.scoretv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.scoretv)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
        prevButton = (Button) view.findViewById(R.id.prev);
        nextButton = (Button) view.findViewById(R.id.next);

        if (pagePos == 0) {
            prevButton.setVisibility(View.GONE);
            nextButton.setVisibility(View.VISIBLE);
        } else {
            prevButton.setVisibility(View.VISIBLE);
            nextButton.setVisibility(View.GONE);
        }

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(pagePos - 1);
                pagePos = mViewPager.getCurrentItem();
                if (pagePos == 0) {
                    prevButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewPager.setCurrentItem(pagePos + 1);
                pagePos = mViewPager.getCurrentItem();

                if (pagePos == 0) {
                    prevButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                }
            }
        });

        dashBoardList = (ListView) view.findViewById(R.id.dashboardlv);
        dashBoardList.setCacheColorHint(0);
        int beatPosition = 0;
        gridListDataLoad(beatPosition);
        DashBoardListViewAdapter dashBoardListViewAdapter = new DashBoardListViewAdapter(getActivity());
        dashBoardList.setAdapter(dashBoardListViewAdapter);
        if (!bmodel.configurationMasterHelper.SHOW_CHART_DASH) {
            view.findViewById(R.id.chartLayout).setVisibility(View.GONE);
        } else {
            sHandler = new Handler() {

                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    try {
                        if (msg.what == 1) {
                            try {
                                semiCircChartFrag.loadSemiCirclePieChart();
                            } catch (Exception e) {
                                Commons.printException(e + "");
                            }
                        } else if (msg.what == 2) {
                            try {
                                stackPerChartFrag
                                        .loadColumnstackedPercentChart();
                            } catch (Exception e) {
                                Commons.printException(e + "");
                            }
                        } else if (msg.what == 3) {
                            cancelProgDialog();
                        }
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
            };
        }
    }

    private void setUpActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (bmodel.getMenuName("MENU_DASH").endsWith(""))
            bmodel.configurationMasterHelper.downloadMainMenu();
        if (getActivity().getIntent().getExtras().getString("screentitle") == null)
            actionBar.setTitle(bmodel.getMenuName("MENU_DASH"));
        else
            actionBar.setTitle(getActivity().getIntent().getExtras().getString("screentitle"));
        actionBar.setIcon(R.drawable.icon_visit);

        //if (!BusinessModel.dashHomeStatic)
            actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowHomeEnabled(true);
        mViewPager = (ViewPager) view.findViewById(R.id.pager);
        mViewPager.setAdapter(mAppSectionsPagerAdapter);
        mViewPager.addOnPageChangeListener(mPageChangeListener);
    }

    private final ViewPager.SimpleOnPageChangeListener mPageChangeListener = new ViewPager.SimpleOnPageChangeListener() {

        @Override
        public void onPageSelected(final int position) {
            AlertDialog.Builder builder;
            final int beatPosition = 0;
            if (!semiCircChartFrag.urlLoad) {
                semiCircChartFrag.urlLoad = true;
                if (alertDialog != null) {
                    builder = new AlertDialog.Builder(getActivity());

                    customProgressDialog(builder, getResources().getString(R.string.loading_data));
                    alertDialog = builder.create();
                    alertDialog.show();
                }
                lastPosition = beatPosition;
                selectedDashItem = 0;
                pagePos = mViewPager.getCurrentItem();
                if (pagePos == 0) {
                    prevButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                }
            } else if (position == 1) {
                pagePos = mViewPager.getCurrentItem();
                if (alertDialog != null) {
                    builder = new AlertDialog.Builder(getActivity());

                    customProgressDialog(builder, getResources().getString(R.string.loading_data));
                    alertDialog = builder.create();
                    alertDialog.show();
                    lastPosition = beatPosition;
                    try {
                        stackPerChartFrag.loadColumnstackedPercentChart();
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (pagePos == 0) {
                    prevButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                }
            } else if (position == 0) {
                if (lastPosition != beatPosition) {
                    lastPosition = beatPosition;
                    selectedDashItem = 0;
                    try {
                        semiCircChartFrag.loadSemiCirclePieChart();
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                pagePos = mViewPager.getCurrentItem();
                if (pagePos == 0) {
                    prevButton.setVisibility(View.GONE);
                    nextButton.setVisibility(View.VISIBLE);
                } else {
                    prevButton.setVisibility(View.VISIBLE);
                    nextButton.setVisibility(View.GONE);
                }
            }
        }
    };

    public class DashBoardListViewAdapter extends ArrayAdapter {

        public DashBoardListViewAdapter(Context context) {
            super(context, R.layout.dashboard_row_layout);
        }

        @Override
        public int getCount() {
            return bmodel.dashBoardHelper.getDashListViewList().size();
        }

        @Override
        public Object getItem(int position) {
            return bmodel.dashBoardHelper.getDashListViewList().get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            final DashBoardBO dashboardData = bmodel.dashBoardHelper
                    .getDashListViewList().get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.retailer_dashboard_row_layout, parent,
                        false);
                holder = new ViewHolder();
                holder.factorName = (TextView) row
                        .findViewById(R.id.factorName_dashboard_tv);
                holder.target = (TextView) row
                        .findViewById(R.id.target_dashboard_tv);
                holder.acheived = (TextView) row
                        .findViewById(R.id.acheived_dashboard_tv);
                holder.balance = (TextView) row
                        .findViewById(R.id.balance_dashboard_tv);
                holder.index = (TextView) row
                        .findViewById(R.id.index_dashboard_tv);
                holder.score = (TextView) row
                        .findViewById(R.id.score_dashboard_tv);
                holder.incentive = (TextView) row
                        .findViewById(R.id.initiative_dashboard_tv);

                if (!bmodel.configurationMasterHelper.SHOW_INDEX_DASH) {
                    holder.index.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_TARGET_DASH) {
                    holder.target.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_BALANCE_DASH) {
                    holder.balance.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_ACHIEVED_DASH) {
                    holder.acheived.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                    holder.incentive.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_SCORE_DASH) {
                    holder.score.setVisibility(View.GONE);
                }

                holder.factorName.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            bmodel.dashBoardHelper.findMinMaxProductLevelRetailerKPI(holder.dashboardDataObj.getKpiID(), holder.dashboardDataObj.getKpiTypeLovID());

                            if (bmodel.dashBoardHelper.getRetailerKpiSku().size() > 0) {
                                Intent i = new Intent(getActivity(),
                                        RetailerKPISKUActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                                i.putExtra("screentitle",
                                        bmodel.getMenuName("MENU_SKUWISESTGT"));
                                i.putExtra("screentitlebk",
                                        actionBar.getTitle());
                                i.putExtra("from", "4");
                                i.putExtra("pid",
                                        holder.dashboardDataObj.getPId());
                                i.putExtra("isFromDash", true);
                                startActivity(i);
                                getActivity().finish();
                            } else {
                                bmodel.showAlert(
                                        getResources().getString(R.string.no_products_exists), 0);
                            }
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    }
                });
                row.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        try {
                            if (bmodel.configurationMasterHelper.SHOW_CHART_DASH) {
                                if (mViewPager.getCurrentItem() == 0) {
                                    selectedDashItem = bmodel.dashBoardHelper
                                            .getDashListViewList().indexOf(
                                                    holder.dashboardDataObj);
                                    semiCircChartFrag.loadSemiCirclePieChart();
                                }
                            }
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                    }
                });
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            if (!showinitiavite
                    || !bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH)
                holder.incentive.setVisibility(View.GONE);
            else {
                holder.incentive.setVisibility(View.VISIBLE);
            }

            holder.dashboardDataObj = dashboardData;
            holder.factorName.setText(dashboardData.getText());

            if (dashboardData.getSubDataCount() > 0) {
                holder.factorName.setClickable(true);
                SpannableString str = new SpannableString(holder.factorName
                        .getText().toString());
                str.setSpan(new UnderlineSpan(), 0, str.length(),
                        Spanned.SPAN_PARAGRAPH);
                str.setSpan(new ForegroundColorSpan(Color.BLUE), 0,
                        str.length(), 0);
                holder.factorName.setText(str);
            } else {
                holder.factorName.setClickable(false);
            }

            if (dashboardData.getFlex1() == 1) {
                holder.acheived.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiAcheived()));
                holder.target.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiTarget()));
                holder.balance.setText(bmodel.dashBoardHelper.getWhole(bmodel.formatValue(SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived()))));
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                holder.index.setText(strCalcPercentage);
                holder.incentive.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiIncentive()));
                holder.score.setText(bmodel.dashBoardHelper.getWhole(dashboardData.getKpiScore()));
            } else {
                String strKpiAchieved = bmodel.formatValue(Double.parseDouble(dashboardData.getKpiAcheived())) + "";
                holder.acheived.setText(strKpiAchieved);
                String strKpiTarget = bmodel.formatValue(Double.parseDouble(dashboardData.getKpiTarget())) + "";
                holder.target.setText(strKpiTarget);
                holder.balance.setText(bmodel.formatValue((SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived()))));
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                holder.index.setText(strCalcPercentage);
                String strKpiIncentive = dashboardData.getKpiIncentive() + "";
                holder.incentive.setText(strKpiIncentive);
                String strKpiScore = dashboardData.getKpiScore() + "";
                holder.score.setText(strKpiScore);
            }
            return row;
        }
    }

    static class ViewHolder {
        TextView factorName;
        TextView target;
        TextView acheived;
        TextView index;
        TextView incentive;
        TextView score;
        TextView balance;
        DashBoardBO dashboardDataObj;
    }

    public class AppSectionsPagerAdapter extends FragmentPagerAdapter {

        public AppSectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            switch (i) {
                case 0:
                    if (semiCircChartFrag == null) {
                        semiCircChartFrag = new SemiCirclePieChartFragment();
                    }
                    return semiCircChartFrag;
                case 1:
                    if (stackPerChartFrag == null) {
                        stackPerChartFrag = new StackedColumnPercentageChartFragment();
                    }
                    return stackPerChartFrag;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Stacked Chart";
                case 1:
                    return "SemiPie Chart";
                default:
                    return null;
            }
        }
    }

    @SuppressLint("ValidFragment")
    public class SemiCirclePieChartFragment extends Fragment {

        WebView semiCircleWv;
        WebSettings webSettings1;
        boolean urlLoad = false;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.semi_circle_pie_chart_layout, container, false);
            try {
                semiCircleWv = (WebView) rootView
                        .findViewById(R.id.semiCircleWv);

                semiCircleWv.setHorizontalScrollBarEnabled(false);
                semiCircleWv.setVerticalScrollBarEnabled(false);
                semiCircleWv.setWebChromeClient(new WebChromeClient() {

                    public boolean onConsoleMessage(ConsoleMessage cm) {
                        Commons.print(cm.message() + " -- From line "
                                + cm.lineNumber() + " of " + cm.sourceId());
                        return true;
                    }
                });
                semiCircleWv.addJavascriptInterface(new WebAppInterface(
                        getActivity(), sHandler), "Android");
                webSettings1 = semiCircleWv.getSettings();
                webSettings1.setAllowFileAccess(false);
                webSettings1.setJavaScriptEnabled(true);
                semiCircleWv.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        return event.getAction() == MotionEvent.ACTION_MOVE;
                    }
                });
                loadLazyUrl();
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return rootView;
        }

        private void loadSemiCirclePieChart() {
            try {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        semiCircleWv.post(new Runnable() {
                            @Override
                            public void run() {
                                if (bmodel.dashBoardHelper
                                        .getDashListViewList() == null
                                        || bmodel.dashBoardHelper
                                        .getDashListViewList().size() == 0) {
                                    cancelProgDialog();
                                    Toast.makeText(getActivity(),
                                            "No chart data to Show",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    semiCircleWv.loadUrl("javascript:semiCircleData("
                                            + bmodel.dashBoardHelper
                                            .getsemiCircleChartData(selectedDashItem)
                                            + ")");
                                }
                            }
                        });
                    }
                }).start();
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        public void loadLazyUrl() {
            semiCircleWv
                    .loadUrl("file:///android_asset/chart/pie-semi-circle/index.htm");
        }

    }

    private void cancelProgDialog() {
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @SuppressLint("ValidFragment")
    public class StackedColumnPercentageChartFragment extends Fragment {

        WebView columnstackedPercWv;
        WebSettings webSettings2;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(
                    R.layout.stacked_column_percentage_chart_layout, container,
                    false);
            try {
                columnstackedPercWv = (WebView) rootView
                        .findViewById(R.id.columnstackedPercWv);
                columnstackedPercWv.setHorizontalScrollBarEnabled(false);
                columnstackedPercWv.setVerticalScrollBarEnabled(false);
                columnstackedPercWv.setWebChromeClient(new WebChromeClient() {

                    public boolean onConsoleMessage(ConsoleMessage cm) {
                        Commons.print(cm.message() + " -- From line "
                                + cm.lineNumber() + " of " + cm.sourceId());
                        return true;
                    }
                });
                columnstackedPercWv.addJavascriptInterface(new WebAppInterface(
                        getActivity(), sHandler), "Android");
                webSettings2 = columnstackedPercWv.getSettings();
                webSettings2.setAllowFileAccess(false);
                webSettings2.setJavaScriptEnabled(true);

                columnstackedPercWv
                        .setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                return event.getAction() == MotionEvent.ACTION_MOVE;
                            }
                        });
                columnstackedPercWv
                        .loadUrl("file:///android_asset/chart/column-stacked-percent/index.htm");
            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return rootView;
        }

        private void loadColumnstackedPercentChart() {
            try {
                new Thread(new Runnable() {

                    @Override
                    public void run() {

                        columnstackedPercWv.post(new Runnable() {
                            @Override
                            public void run() {

                                if (bmodel.dashBoardHelper
                                        .getDashListViewList() == null
                                        || bmodel.dashBoardHelper
                                        .getDashListViewList().size() == 0) {
                                    cancelProgDialog();
                                    Toast.makeText(getActivity(),
                                            "No chart data to Show",
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    columnstackedPercWv.loadUrl("javascript:stackedColumnPerctChartData("
                                            + bmodel.dashBoardHelper
                                            .getColumnstackedPercentData()
                                            + ")");
                                }
                            }
                        });
                    }
                }).start();
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_target_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
        /*if (BusinessModel.dashHomeStatic)
            menu.findItem(R.id.menu_next).setVisible(true);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_next) {
            Intent intent = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(intent);
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            if (stackPerChartFrag != null) {
                stackPerChartFrag.columnstackedPercWv = null;
                stackPerChartFrag.webSettings2 = null;
                stackPerChartFrag = null;
            }
            if (semiCircChartFrag != null) {
                semiCircChartFrag.semiCircleWv = null;
                semiCircChartFrag.webSettings1 = null;
                semiCircChartFrag = null;
            }
            if (sHandler != null)
                sHandler = null;
            if (alertDialog != null)
                alertDialog = null;
            if (mAppSectionsPagerAdapter != null)
                mAppSectionsPagerAdapter = null;
            if (mViewPager != null)
                mViewPager = null;
            if (actionBar != null)
                actionBar = null;
            if (dashBoardList != null)
                dashBoardList = null;
            if (fm != null)
                fm = null;
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void gridListDataLoad(int position) {
        if (position == 0) {
            bmodel.dashBoardHelper.getGridData(0);
            if (bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                incentivetv.setVisibility(View.VISIBLE);
                showinitiavite = true;
            }
        } else {
            bmodel.dashBoardHelper.getGridData(((String) bmodel.dashBoardHelper
                    .getBeatList().get(position)));
            incentivetv.setVisibility(View.GONE);
            showinitiavite = false;
        }
    }
}
