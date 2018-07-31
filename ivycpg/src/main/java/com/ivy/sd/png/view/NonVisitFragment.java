package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.DateWisePlanBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.profile.CommonReasonDialog;
import com.ivy.sd.png.view.profile.ProfileActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

public class NonVisitFragment extends Fragment implements BrandDialogInterface,
        SearchView.OnQueryTextListener,FiveLevelFilterCallBack {

    private final String MENU_PLANNING = "Day Planning";
    private final String MENU_VISIT = "Trade Coverage";
    private final String MENU_PLANNING_SUB = "Day Planning Sub";
    public boolean profileclick;
    private AbsListView listView;
    private BusinessModel bmodel;
    private RetailerMasterBO retailerObj;
    private ArrayList<RetailerMasterBO> retailer;
    private Spinner spinnerbrand;
    private String calledBy;
    private TypedArray typearr;
    private Spinner spn_mWeek;
    private DisplayMetrics displaymetrics;
    private ArrayList<String> mWeekList;
    private HashMap<String, String> mWeekMap = new HashMap<>();
    private String mSelectedDay;
    private ArrayList<String> mDateList;
    private Spinner spn_mDate, spn_mBeat;
    private View mview;
    private boolean isReasonDialogClicked;
    private RadioGroup mWeekRG;
    private String mSelectedWeek = "All";
    private LinearLayout filterLayout;
    ArrayAdapter<BeatMasterBO> beatAdapter;
    IconicAdapter mSchedule;

    private FloatingActionButton fab;
    ArrayList<String> selectedPosition = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        mview = inflater.inflate(R.layout.nonvisit, container, false);
        setHasOptionsMenu(true);
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        spinnerbrand = mview.findViewById(R.id.brandSpinner);
        filterLayout = mview.findViewById(R.id.filter);
        spn_mWeek = mview.findViewById(R.id.spn_week);
        spn_mDate = mview.findViewById(R.id.spin_date);
        spn_mBeat = mview.findViewById(R.id.spin_beat);
        mWeekRG = mview.findViewById(R.id.week_radiogroup);
        fab = mview.findViewById(R.id.fab);

        displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {

                ArrayList<RetailerMasterBO> retailerMasterBOS = new ArrayList<>();

                for (String retId : selectedPosition) {
                    for (RetailerMasterBO retailerMasterBO : retailer) {
                        if (retailerMasterBO.getRetailerID().equals(retId)) {
                            retailerMasterBOS.add(retailerMasterBO);
                            break;
                        }
                    }
                }

                bmodel.mRetailerHelper.deviateRetailerList = retailerMasterBOS;

                CommonReasonDialog comReasonDialog = new CommonReasonDialog(getActivity(), "deviate");
                comReasonDialog.setNonvisitListener(new CommonReasonDialog.AddNonVisitListener() {
                    @Override
                    public void addReatailerReason() {
                        showAlert(getResources().getString(
                                R.string.saved_successfully));
                    }
                });
                comReasonDialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = comReasonDialog.getWindow();
                lp.copyFrom(window != null ? window.getAttributes() : null);
                lp.width = displaymetrics.widthPixels - 100;
                lp.height = (int) (displaymetrics.heightPixels / 2.5);//WindowManager.LayoutParams.WRAP_CONTENT;
                if (window != null) {
                    window.setAttributes(lp);
                }
            }
        });

        mWeekRG.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                RadioButton radioButton = mWeekRG.findViewById(checkedId);
                mSelectedWeek = radioButton.getText().toString();
                loadData(null);

            }
        });

        int count = mWeekRG.getChildCount();
        for (int i = 0; i < count; i++) {
            View view = mWeekRG.getChildAt(i);
            if (view instanceof RadioButton) {
                RadioButton radioButton = (RadioButton) view;
                radioButton.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }
        }

        TextView tvWeekSelection = mview.findViewById(R.id.week_title);
        tvWeekSelection.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        TextView tvDaySelection = mview.findViewById(R.id.day_title);
        tvDaySelection.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        TextView tvBeatSelection = mview.findViewById(R.id.beat_title);
        tvBeatSelection.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        LinearLayout dateLL = mview
                .findViewById(R.id.ll_datefilter);
        LinearLayout weekLL = mview
                .findViewById(R.id.ll_weekfilter);
        LinearLayout beatLL = mview
                .findViewById(R.id.ll_beatfilter);
        if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {
            mDateList = bmodel.mRetailerHelper.getMaxDaysInRouteSelection();
            if (mDateList == null) {
                mDateList = new ArrayList<>();
            }
            (mview.findViewById(R.id.relativeLayout)).
                    setVisibility(View.GONE);
            filterLayout.setVisibility(View.VISIBLE);
            mDateList.add(0, getResources().getString(R.string.all));
            weekLL.setVisibility(View.GONE);
            beatLL.setVisibility(View.GONE);
        } else {
            dateLL.setVisibility(View.GONE);
        }
        if (bmodel.configurationMasterHelper.SHOW_BEAT_ROUTE) {
            mDateList = bmodel.mRetailerHelper.getMaxDaysInRouteSelection();
            if (mDateList == null) {
                mDateList = new ArrayList<>();
            }

            beatAdapter = new ArrayAdapter<>(getActivity(), R.layout.deviate_retailer_spinner_layout);
            beatAdapter.setDropDownViewResource(R.layout.deviate_retailer_spinner_list);
            beatAdapter.add(new BeatMasterBO(0, getResources().getString(
                    R.string.all), 0));
            for (BeatMasterBO temp : bmodel.beatMasterHealper.getBeatMaster()) {
                beatAdapter.add(temp);
            }

            spn_mBeat.setAdapter(beatAdapter);

            (mview.findViewById(R.id.relativeLayout)).
                    setVisibility(View.GONE);
            filterLayout.setVisibility(View.VISIBLE);
            mDateList.add(0, getResources().getString(R.string.all));
            weekLL.setVisibility(View.GONE);
            dateLL.setVisibility(View.GONE);
        } else {
            beatLL.setVisibility(View.GONE);
        }

        listView = mview.findViewById(R.id.nonvisitlistView1);
        listView.setCacheColorHint(0);


        // Add week in list
        mWeekList = new ArrayList<>();
        mWeekList.add(getResources().getString(R.string.all));
        mWeekList.add(getResources().getString(R.string.week1));
        mWeekList.add(getResources().getString(R.string.week2));
        mWeekList.add(getResources().getString(R.string.week3));
        mWeekList.add(getResources().getString(R.string.week4));
        mSelectedWeek=getResources().getString(R.string.all);

        // for mapping with db
        mWeekMap.put(getResources().getString(R.string.all), getResources()
                .getString(R.string.all));
        mWeekMap.put(getResources().getString(R.string.week1), "wk1");
        mWeekMap.put(getResources().getString(R.string.week2), "wk2");
        mWeekMap.put(getResources().getString(R.string.week3), "wk3");
        mWeekMap.put(getResources().getString(R.string.week4), "wk4");
        mWeekMap.put(getResources().getString(R.string.week5), "wk5");
        try {
            if (!bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {
                if (bmodel.labelsMasterHelper.applyLabels(mview.findViewById(
                        R.id.daytv).getTag()) != null)
                    ((TextView) mview.findViewById(R.id.daytv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(mview.findViewById(R.id.daytv)
                                            .getTag()));
                if (bmodel.labelsMasterHelper.applyLabels(mview.findViewById(
                        R.id.weektv).getTag()) != null)
                    ((TextView) mview.findViewById(R.id.weektv))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(mview.findViewById(R.id.weektv)
                                            .getTag()));
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }


        return mview;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        //calledBy = getActivity().getIntent().getStringExtra("From");
        calledBy=getArguments().getString("From");
        if (calledBy == null)
            calledBy = MENU_VISIT;
    }


    public void loadData(String filter) {
        try {
            int start, end;
            String weekno;
            Commons.print("Non Visit Fragment ," + "On Load Method called ");
            retailer = new ArrayList<>();
            int siz = bmodel.getRetailerMaster().size();
            if (bmodel.configurationMasterHelper.SHOW_WEEK_ROUTE) {
                for (int i = 0; i < siz; i++) {
                    if (mSelectedWeek
                            .equals(getResources().getString(R.string.all))) {
                        Commons.print("filter"
                                + bmodel.getRetailerMaster().get(i).getWeekNo()
                                + " "
                                + mSelectedDay
                                .substring(0, 3));
                        if (!mSelectedDay.equalsIgnoreCase("all")) {
                            if (bmodel
                                    .getRetailerMaster()
                                    .get(i)
                                    .getWeekNo()
                                    .contains(
                                            mSelectedDay
                                                    .substring(0, 2).toUpperCase()))
                                if (filter != null) {
                                    if (bmodel.getRetailerMaster().get(i)
                                            .getRetailerName().toLowerCase()
                                            .contains(filter.toLowerCase()) ||
                                            ((bmodel.getRetailerMaster().get(i)
                                                    .getRetailerCode() != null) &&
                                                    bmodel.getRetailerMaster().get(i)
                                                            .getRetailerCode().toLowerCase()
                                                            .contains(filter.toLowerCase()))) {
                                        retailer.add(bmodel.getRetailerMaster().get(i));
                                    }
                                } else {
                                    retailer.add(bmodel.getRetailerMaster().get(i));
                                }
                        } else {
                            if (filter != null) {
                                if (bmodel.getRetailerMaster().get(i)
                                        .getRetailerName().toLowerCase()
                                        .contains(filter.toLowerCase()) || ((bmodel.getRetailerMaster().get(i)
                                        .getRetailerCode() != null) &&
                                        bmodel.getRetailerMaster().get(i)
                                                .getRetailerCode().toLowerCase()
                                                .contains(filter.toLowerCase()))) {
                                    retailer.add(bmodel.getRetailerMaster().get(i));
                                }
                            } else {
                                retailer.add(bmodel.getRetailerMaster().get(i));
                            }
                        }
                    } else {
                        weekno = bmodel.getRetailerMaster().get(i).getWeekNo();
                        if (!mSelectedDay.equalsIgnoreCase("all")) {
                            if (bmodel
                                    .getRetailerMaster()
                                    .get(i)
                                    .getWeekNo()
                                    .contains(
                                            mSelectedDay
                                                    .substring(0, 2).toUpperCase())) {
                                start = weekno.indexOf(mSelectedDay.substring(0, 2).toUpperCase());
                                end = weekno.indexOf(";", start);
                                if (weekno.substring(start, end).contains(
                                        mWeekMap.get(mSelectedWeek))) {
                                    if (filter != null) {
                                        if (bmodel.getRetailerMaster().get(i)
                                                .getRetailerName().toLowerCase()
                                                .contains(filter.toLowerCase()) ||
                                                ((bmodel.getRetailerMaster().get(i)
                                                        .getRetailerCode() != null) &&
                                                        bmodel.getRetailerMaster().get(i)
                                                                .getRetailerCode().toLowerCase()
                                                                .contains(filter.toLowerCase()))) {
                                            retailer.add(bmodel.getRetailerMaster()
                                                    .get(i));
                                        }
                                    } else {
                                        retailer.add(bmodel.getRetailerMaster().get(i));
                                    }
                                }
                            }
                        } else {
                            if (weekno.contains(
                                    mWeekMap.get(mSelectedWeek))) {
                                if (filter != null) {
                                    if (bmodel.getRetailerMaster().get(i)
                                            .getRetailerName().toLowerCase()
                                            .contains(filter.toLowerCase()) ||
                                            ((bmodel.getRetailerMaster().get(i)
                                                    .getRetailerCode() != null) &&
                                                    bmodel.getRetailerMaster().get(i)
                                                            .getRetailerCode().toLowerCase()
                                                            .contains(filter.toLowerCase()))) {
                                        retailer.add(bmodel.getRetailerMaster()
                                                .get(i));
                                    }
                                } else {
                                    retailer.add(bmodel.getRetailerMaster().get(i));
                                }
                            }
                        }
                    }
                }
            } else if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {
                String date = spn_mDate.getSelectedItem().toString();
                loadDataByusingDateFilter(date, filter);
            } else if (bmodel.configurationMasterHelper.SHOW_BEAT_ROUTE) {
                BeatMasterBO beatBo = (BeatMasterBO) spn_mBeat.getSelectedItem();
                loadDataByBeatFilter(beatBo, filter);
            }


            Collections.sort(retailer, RetailerMasterBO.RetailerNameComparator);
            mSchedule = new IconicAdapter(retailer);
            ((PlanningActivity) getActivity()).updateRetailerCount(mSchedule.getCount(), 2);
            if (listView != null) {
                listView.setAdapter(mSchedule);
                setHasOptionsMenu(true);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private void loadDataByBeatFilter(BeatMasterBO beatBo, String searchStr) {
        retailer = new ArrayList<>();
        if (!(getResources()
                .getString(R.string.all)).equals(beatBo.getBeatDescription())) {
            for (RetailerMasterBO retailerMasterBO : bmodel.getRetailerMaster()) {
                if (retailerMasterBO.getBeatID() == beatBo.getBeatId()) {
                    if (searchStr != null) {
                        if (retailerMasterBO
                                .getRetailerName().toLowerCase()
                                .contains(searchStr.toLowerCase()) ||
                                ((retailerMasterBO.getRetailerCode() != null) &&
                                        retailerMasterBO.getRetailerCode().toLowerCase().contains(searchStr.toLowerCase()))) {
                            retailer.add(retailerMasterBO);
                        }
                    } else {
                        retailer.add(retailerMasterBO);
                    }
                }
            }
        } else {
            for (RetailerMasterBO retailerMasterBO : bmodel.getRetailerMaster()) {
                if (retailerMasterBO.getBeatID() != 0
                        || retailerMasterBO.getBeatID() == 0) {
                    if (searchStr != null) {
                        if (retailerMasterBO
                                .getRetailerName().toLowerCase()
                                .contains(searchStr.toLowerCase()) ||
                                ((retailerMasterBO.getRetailerCode() != null)
                                        && retailerMasterBO.getRetailerCode().toLowerCase().contains(searchStr.toLowerCase()))) {
                            retailer.add(retailerMasterBO);
                        }
                    } else {
                        retailer.add(retailerMasterBO);
                    }
                }

            }
        }

        mSchedule = new IconicAdapter(retailer);
        if (listView != null) {
            listView.setAdapter(mSchedule);
            setHasOptionsMenu(true);
        }
    }


    private void loadDataByusingDateFilter(String date, String searchStr) {
        retailer = new ArrayList<>();
        if (!(getResources().getString(R.string.all).equals(date))) {
            for (RetailerMasterBO retailerMasterBO : bmodel.getRetailerMaster()) {
                if (retailerMasterBO != null) {
                    HashSet<DateWisePlanBO> plannedDateList = retailerMasterBO.getPlannedDates();

                    if (plannedDateList != null) {
                        for (DateWisePlanBO dateWisePlanBO : plannedDateList) {
                            int result = SDUtil.compareDate(date,
                                    dateWisePlanBO.getDate(), "yyyy/MM/dd");
                            if (result == 0) {
                                if (searchStr != null) {
                                    if (retailerMasterBO
                                            .getRetailerName().toLowerCase()
                                            .contains(searchStr.toLowerCase()) ||
                                            ((retailerMasterBO.getRetailerCode() != null)
                                                    && retailerMasterBO.getRetailerCode().toLowerCase().contains(searchStr.toLowerCase()))) {
                                        retailer.add(retailerMasterBO);
                                    }
                                } else {
                                    retailer.add(retailerMasterBO);
                                }
                            }
                        }
                    }
                }
            }
        } else {
            for (RetailerMasterBO retailerMasterBO : bmodel.getRetailerMaster()) {
                if (searchStr != null) {
                    if (retailerMasterBO
                            .getRetailerName().toLowerCase()
                            .contains(searchStr.toLowerCase()) ||
                            ((retailerMasterBO.getRetailerCode() != null)
                                    && retailerMasterBO.getRetailerCode().toLowerCase().contains(searchStr.toLowerCase()))) {
                        retailer.add(retailerMasterBO);
                    }
                } else {
                    retailer.add(retailerMasterBO);
                }
            }
        }

        mSchedule = new IconicAdapter(retailer);
        if (listView != null) {
            listView.setAdapter(mSchedule);
            setHasOptionsMenu(true);
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onResume() {
        super.onResume();
        profileclick = false;
        isReasonDialogClicked = false;
        Commons.print("Non Visit Fragment ," + "On Resume Method Called ");

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {
            updateDateFilter();
        } else if (bmodel.configurationMasterHelper.SHOW_WEEK_ROUTE) {
            updateWeekFilter();
        } else if (bmodel.configurationMasterHelper.SHOW_BEAT_ROUTE) {
            updateBeatFilter();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getView() != null) {
            spinnerbrand = getView().findViewById(R.id.brandSpinner);
            spn_mWeek = getView().findViewById(R.id.spn_week);
        }

        listView = (ListView) getView().findViewById(R.id.nonvisitlistView1);
        listView.setCacheColorHint(0);

        Commons.print("NOn Visit Fragment ," + "On Start method called ");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_search, menu);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                loadData(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                //loadData(query);
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search).setVisible(true);

        if (bmodel.configurationMasterHelper.SHOW_DEVIATION) {
            menu.findItem(R.id.menu_deviate_retailers).setVisible(true);
        } else {
            menu.findItem(R.id.menu_deviate_retailers).setVisible(false);
        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            getActivity().finish();
            return true;
        } else if (item.getItemId() == R.id.menu_deviate_retailers) {
            doDeviations();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private SearchView.OnQueryTextListener searchQueryListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String query) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            return false;
        }
    };

    private void doDeviations() {
        boolean deviationNotAllowedforNewRetailer = false;
        boolean retailerAlreadyPlannedtoday = false;
        boolean noDeviationreasonFound = false;
        boolean deviationNotAllowed = false;

        for (RetailerMasterBO rm : retailer) {
            deviationNotAllowedforNewRetailer = false;
            retailerAlreadyPlannedtoday = false;
            if ((bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION && calledBy
                    .equals(MENU_PLANNING))
                    || (bmodel.configurationMasterHelper.IS_VISITSCREEN_DEV_ALLOW && bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION)) {

                if (bmodel.reasonHelper.getDeviatedReturnMaster().size() != 0) {

                    if (("Y").equals(rm.getIsNew())) {
                        deviationNotAllowedforNewRetailer = true;
                    } else if (bmodel.isAlreadyExistInToday(rm.getRetailerID())) {
                        retailerAlreadyPlannedtoday = true;
                    }
                } else {
                    noDeviationreasonFound = true;
                    break;
                }
            } else {
                deviationNotAllowed = true;
                break;
            }
        }

        if (deviationNotAllowedforNewRetailer) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getResources().getString(
                            R.string.deviation_not_allowed_for_new_retailer),
                    Toast.LENGTH_SHORT).show();
        } else if (retailerAlreadyPlannedtoday) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getResources().getString(
                            R.string.retailer_is_already_planned_for_today),
                    Toast.LENGTH_SHORT).show();
        } else if (noDeviationreasonFound) {
            Toast.makeText(
                    getActivity().getApplicationContext(),
                    getResources().getString(
                            R.string.no_deviate_reason_found_plz_redownload),
                    Toast.LENGTH_SHORT).show();
        } else if (deviationNotAllowed) {
            Toast.makeText(getActivity().getApplicationContext(),
                    getResources().getString(R.string.Deviation_not_allowed),
                    Toast.LENGTH_SHORT).show();
        } else {
            showAlert(
                    getResources()
                            .getString(
                                    R.string.enter_deviate_reason_to_plan_this_retailer_for_today),
                    1);
        }
    }


    private void updateWeekFilter() {
        spn_mDate.setVisibility(View.GONE);
        bmodel.downloadWeekDay();
        ArrayList<StandardListBO> weekDayArray = new ArrayList<>();
        for (int i = 0; i < bmodel.getWeekDay().size(); i++) {
            weekDayArray.add(bmodel.getWeekDay().get(i));
        }
        ArrayAdapter<StandardListBO> weekDayAdapter = new ArrayAdapter<>(
                getActivity(), R.layout.deviate_retailer_spinner_layout,
                weekDayArray);
        weekDayAdapter
                .setDropDownViewResource(R.layout.deviate_retailer_spinner_list);
        spinnerbrand.setAdapter(weekDayAdapter);
        spinnerbrand.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                StandardListBO sdbo = (StandardListBO) parent
                        .getItemAtPosition(position);
                mSelectedDay = spinnerbrand.getSelectedItem().toString();
                loadData(null);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        ArrayAdapter<String> weekAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item, mWeekList);
        weekAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spn_mWeek.setAdapter(weekAdapter);


        spn_mWeek.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                Commons.print("week");

                loadData(null);
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


    }

    private void updateDateFilter() {
        spn_mWeek.setVisibility(View.GONE);
        spinnerbrand.setVisibility(View.GONE);
        if (mDateList != null) {
            if (mDateList.size() > 0) {
                ArrayAdapter<String> dateAdapter = new ArrayAdapter<>(
                        getActivity(), R.layout.deviate_retailer_spinner_layout,
                        mDateList);
                dateAdapter
                        .setDropDownViewResource(R.layout.deviate_retailer_spinner_list);
                spn_mDate.setAdapter(dateAdapter);

                spn_mDate
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> arg0,
                                                       View arg1, int arg2, long arg3) {

                                loadData(null);
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                                // TO DO

                            }
                        });

            }
        }

    }

    private void updateBeatFilter() {
        spn_mBeat.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                loadData(null);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    public void showAlert(String msg, int id) {
        final int idd = id;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getActivity().getResources().getString(
                R.string.no_visit_Planning));
        builder.setMessage(msg);
        // Set an EditText view to get user input
        final Spinner input = new Spinner(getActivity());

        ArrayAdapter<ReasonMaster> spinnerAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item,
                bmodel.reasonHelper.getDeviatedReturnMaster());

        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        input.setAdapter(spinnerAdapter);

        builder.setView(input);

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        isReasonDialogClicked = false;
                        if (idd == 0) {

                            ReasonMaster r = (ReasonMaster) input
                                    .getSelectedItem();
                            Commons.print(r.getReasonDesc() + "DESC");
                            if (!r.getReasonDesc().equalsIgnoreCase(
                                    getResources().getString(
                                            R.string.select_reason))) {


                                bmodel.reasonHelper.setDeviate(
                                        retailerObj.getRetailerID(), r,
                                        retailerObj.getBeatID(), "");


                                retailer = new ArrayList<>();
                                int siz = bmodel.getRetailerMaster().size();

                                for (int i = 0; i < siz; i++) {
                                    retailer.add(bmodel.getRetailerMaster()
                                            .get(i));
                                }

                                mSchedule = new IconicAdapter(
                                        retailer);
                                listView.setAdapter(mSchedule);
                            }
                        } else if (idd == 1) {
                            ReasonMaster r = (ReasonMaster) input
                                    .getSelectedItem();
                            Commons.print(r.getReasonDesc() + "DESC");
                            if (!r.getReasonDesc().equalsIgnoreCase(
                                    getResources().getString(
                                            R.string.select_reason))) {

                                for (RetailerMasterBO tempBo : retailer) {
                                    bmodel.reasonHelper.setDeviate(
                                            tempBo.getRetailerID(), r,
                                            tempBo.getBeatID(), "");
                                }

                                retailer = new ArrayList<>();
                                int siz = bmodel.getRetailerMaster().size();

                                for (int i = 0; i < siz; i++) {
                                    retailer.add(bmodel.getRetailerMaster()
                                            .get(i));
                                }

                                mSchedule = new IconicAdapter(retailer);
                                listView.setAdapter(mSchedule);
                            }
                        }

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        isReasonDialogClicked = false;

                    }
                });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                isReasonDialogClicked = false;
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Commons.print("Non Visit Fragment ," + " On Destroy method called ");
        if (mview != null) {
            unbindDrawables(mview.findViewById(R.id.root));
        }
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view -view
     */
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

    @Override
    public void updateBrandText(String mFilterText, int id) {
        // TO DO Auto-generated method stub

    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // TO DO Auto-generated method stub

    }

    @Override
    public void updateCancel() {
        profileclick = false;
        getActivity().finish();
    }

    @Override
    public void loadStartVisit() {
        // TO DO Auto-generated method stub

    }

    @Override
    public boolean onQueryTextChange(String s) {
        loadData(s);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (s.isEmpty()) {
            loadData(null);
        }
        return false;
    }


    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        // TO DO Auto-generated method stub
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == 2)
                updateCancel();
        }
    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(msg);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //  updateCancel();
                if (calledBy.equalsIgnoreCase(MENU_VISIT)) {
                    Intent i = new Intent(getActivity(), HomeScreenActivity.class);
                    i.putExtra("menuCode", "MENU_VISIT");
                    startActivity(i);
                    getActivity().finish();
                } else if (calledBy.equalsIgnoreCase(MENU_PLANNING)) {
                    Intent i = new Intent(getActivity(), PlanningVisitActivity.class);
                    i.putExtra("isPlanning",true);
                    startActivity(i);
                    getActivity().finish();
                }else if (calledBy.equalsIgnoreCase(MENU_PLANNING_SUB)) {
                    Intent i = new Intent(getActivity(), PlanningVisitActivity.class);
                    i.putExtra("isPlanningSub",true);
                    startActivity(i);
                    getActivity().finish();
                }

//                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }

    class IconicAdapter extends ArrayAdapter {
        ArrayList items;

        private IconicAdapter(ArrayList items) {
            super(getActivity(), R.layout.non_visit_list_item, items);
            this.items = items;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            retailerObj = (RetailerMasterBO) items.get(position);


            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.nonvisit_list_child,
                        parent, false);
                holder = new ViewHolder();

                holder.card_view = convertView.findViewById(R.id.card_view);
                holder.llFirst = convertView.findViewById(R.id.ll_first);
                holder.outletIV = convertView
                        .findViewById(R.id.outlet_iv);
                holder.icon = convertView.findViewById(R.id.outlet);
                holder.outletname = convertView
                        .findViewById(R.id.outletName_tv);
                holder.outletname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.outletLocation = convertView
                        .findViewById(R.id.outlet_location_tv);
                holder.outletLocation.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.info = convertView
                        .findViewById(R.id.info_iv);
                holder.visitFrequency = convertView
                        .findViewById(R.id.visit_frequency);
                holder.outletAddress = convertView.findViewById(R.id.outlet_address_tv);
                holder.outletAddress.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                if (!bmodel.configurationMasterHelper.HAS_PROFILE_BUTTON_IN_RETAILER_LIST)
                    holder.info.setVisibility(View.GONE);


                holder.info.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        retailerObj = retailer.get(holder.ref);
                        bmodel.setRetailerMasterBO(retailerObj);

                        Commons.print("menu visit," +
                                "startvisit=true, non visit faragment");

                        if (!profileclick) {
                            bmodel.newOutletHelper.downloadLinkRetailer();
                            profileclick = true;

                            Intent i = new Intent(getActivity(), ProfileActivity.class);
                            i.putExtra("visit", true);
                            startActivityForResult(i, 1);
                        }


                    }
                });

                convertView.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                        if (selectedPosition.size() == 0) {
                            if ((bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION && calledBy
                                    .equals(MENU_PLANNING)
                                    || (bmodel.configurationMasterHelper.IS_VISITSCREEN_DEV_ALLOW && bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION))) {

                                retailerObj = (RetailerMasterBO) items
                                        .get(holder.ref);
                                bmodel.setRetailerMasterBO(retailerObj);

                                if (bmodel.reasonHelper.getDeviatedReturnMaster()
                                        .size() != 0) {

                                    if (("Y").equals(bmodel.getRetailerMasterBO().getIsNew())) {

                                        Toast t = Toast
                                                .makeText(
                                                        getActivity()
                                                                .getApplicationContext(),
                                                        getResources()
                                                                .getString(
                                                                        R.string.deviation_not_allowed_for_new_retailer),
                                                        Toast.LENGTH_SHORT);
                                        t.show();

                                    } else if (!bmodel
                                            .isAlreadyExistInToday(retailerObj
                                                    .getRetailerID())) {
                                        if ((!isReasonDialogClicked)) {
                                            isReasonDialogClicked = true;
                                            Commons.print("-"
                                                    + bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION);
                                            Commons.print("-" + calledBy);
                                            bmodel.newOutletHelper.downloadLinkRetailer();
                                            Intent i = new Intent(getActivity(), ProfileActivity.class);
                                            i.putExtra("From", calledBy);
                                            if(calledBy.equalsIgnoreCase(MENU_PLANNING))
                                                i.putExtra("isPlanning",true);
                                            else if(calledBy.equalsIgnoreCase(MENU_PLANNING_SUB))
                                                i.putExtra("isPlanningSub",true);
                                            i.putExtra("non_visit", true);
                                            startActivityForResult(i, 1);
                                        }
                                    } else {
                                        Toast.makeText(
                                                getActivity()
                                                        .getApplicationContext(),
                                                getResources()
                                                        .getString(
                                                                R.string.retailer_is_already_planned_for_today),
                                                Toast.LENGTH_SHORT).show();
                                    }
                                } else {

                                    Toast t = Toast
                                            .makeText(
                                                    getActivity()
                                                            .getApplicationContext(),
                                                    getResources()
                                                            .getString(
                                                                    R.string.no_deviate_reason_found_plz_redownload),
                                                    Toast.LENGTH_SHORT);
                                    t.show();
                                }
                            } else {
                                Toast t = Toast.makeText(getActivity()
                                                .getApplicationContext(), getResources()
                                                .getString(R.string.Deviation_not_allowed),
                                        Toast.LENGTH_SHORT);
                                t.show();
                            }

                        }
                    }


                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            if (selectedPosition.contains(retailerObj.getRetailerID())) {
                holder.llFirst.setBackgroundColor(getResources().getColor(R.color.colorPrimaryAlpha));
            } else {
                holder.llFirst.setBackgroundColor(getResources().getColor(android.R.color.white));
            }

            convertView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    if ((bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION && calledBy
                            .equals(MENU_PLANNING)
                            || (bmodel.configurationMasterHelper.IS_VISITSCREEN_DEV_ALLOW && bmodel.configurationMasterHelper.IS_RETAILER_DEVIATION))) {
                        if (bmodel.reasonHelper.getDeviatedReturnMaster()
                                .size() != 0) {

                            retailerObj = (RetailerMasterBO) items
                                    .get(holder.ref);

                            if (("Y").equals(bmodel.getRetailerMasterBO().getIsNew())) {

                                Toast.makeText(
                                        getActivity()
                                                .getApplicationContext(),
                                        getResources()
                                                .getString(
                                                        R.string.deviation_not_allowed_for_new_retailer),
                                        Toast.LENGTH_SHORT).show();

                            } else if (bmodel
                                    .isAlreadyExistInToday(retailerObj
                                            .getRetailerID())) {
                                Toast.makeText(
                                        getActivity()
                                                .getApplicationContext(),
                                        getResources()
                                                .getString(
                                                        R.string.retailer_is_already_planned_for_today),
                                        Toast.LENGTH_SHORT).show();


                            } else {

                                if (selectedPosition.contains(retailerObj.getRetailerID())) {
                                    selectedPosition.remove(retailerObj.getRetailerID());
                                } else {
                                    selectedPosition.add(retailerObj.getRetailerID());
                                }

                                if (selectedPosition.size() > 0) {
                                    fab.setVisibility(View.VISIBLE);
                                } else {
                                    fab.setVisibility(View.GONE);
                                }

                                notifyDataSetChanged();
                            }
                        } else {
                            Toast.makeText(
                                    getActivity()
                                            .getApplicationContext(),
                                    getResources()
                                            .getString(
                                                    R.string.no_deviate_reason_found_plz_redownload),
                                    Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast t = Toast.makeText(getActivity()
                                        .getApplicationContext(), getResources()
                                        .getString(R.string.Deviation_not_allowed),
                                Toast.LENGTH_SHORT);
                        t.show();
                    }
                    return true;
                }
            });

            holder.retailerId = retailerObj.getRetailerID();
            holder.outletname.setText(retailerObj.getRetailerName());

            if (bmodel.configurationMasterHelper.SHOW_RETAILER_LOCATION)//to show retailer location
                holder.outletLocation.setText(retailerObj.getRField4());
            else
                holder.outletLocation.setVisibility(View.INVISIBLE);

            holder.outletAddress.setText(retailerObj.getAddress1());
            holder.ref = position;

            holder.visitFrequency.setText(((bmodel.configurationMasterHelper.SHOW_RETAILER_FREQUENCY) ? " - F - " + ((retailerObj.getVisit_frequencey() > 0) ? retailerObj.getVisit_frequencey() : "0") : ""));

            if (("Y").equals(retailerObj.getIsNew())) {
                holder.outletIV
                        .setImageResource(R.drawable.icon_outlet_all_new);
            } else {
                holder.outletIV.setImageResource(R.drawable.icon_outlet_all);
            }

            if (("Y").equals(retailerObj.getIsDeadStore())) {
                holder.icon.setImageResource(R.drawable.icon_outlet_dead);
            } else {
                holder.icon.setImageResource(android.R.color.transparent);
            }

            return convertView;
        }

        class ViewHolder {
            ImageView info;
            ImageView outletIV;
            ImageView icon;
            TextView outletname;
            TextView outletLocation;
            TextView outletAddress;
            String retailerId;
            int ref;
            LinearLayout llFirst;
            CardView card_view;
            TextView visitFrequency;
        }

    }
}
