package com.ivy.cpg.view.tradeCoverage;

import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Filter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.jointcall.JoinCallActivity;
import com.ivy.cpg.view.order.tax.TaxGstHelper;
import com.ivy.cpg.view.order.tax.TaxHelper;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.subd.SubDSelectionDialog;
import com.ivy.cpg.view.tradeCoverage.deviation.PlanningActivity;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.VisitConfiguration;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.CustomFragment;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class VisitFragment extends IvyBaseFragment implements BrandDialogInterface, FiveLevelFilterCallBack, SearchView.OnQueryTextListener, SubDSelectionDialog.SubIdSelectionListner, CustomFragment.RetailerSelectionListener {

    private static final String CODE_PRODUCTIVE = "Filt_01";
    private static final String CODE_NON_PRODUCTIVE = "Filt_02";
    private static final String CODE_VISITED = "Filt_03";
    private static final String CODE_QDVP3 = "Filt_04";
    private static final String CODE_INDICATIVE = "Filt_05";
    private static final String CODE_GOLDEN_STORE = "FIlt_06";
    private static final String CODE_DEAD_STORE = "Filt_07";
    private static final String CODE_HANGING = "Filt_08";

    private static final int PROFILE_REQUEST_CODE = 101;

    private static final String MENU_PLANNING = "Day Planning";
    private static final String MENU_PLANNING_SUB = "Day Planning Sub";
    private static final String MENU_VISIT = "Trade Coverage";
    private static final String MENU_STK_ORD = "MENU_STK_ORD";
    private static final String RETAILER_FILTER_MENU_TYPE = "MENU_VISIT";

    //image icon constants
    private static final String ICON_COOLER = "COOLER";
    private static final String ICON_LOYALITY = "LOYALITY";
    private static final String ICON_CROWN = "CROWN";
    private static final String ICON_DEAD = "DEAD";
    private static final String ICON_ALIVE = "ALIVE";
    private static final String ICON_SKULL = "SKULL";

    private boolean profileClick;
    private BusinessModel bmodel;
    private boolean isClicked;
    private boolean startVisit = false;
    private String calledBy;
    private RecyclerView rvView;
    private ArrayList<RetailerMasterBO> retailer = new ArrayList<>();
    private ArrayList<RetailerMasterBO> startVistitRetailers = new ArrayList<>();
    private Map<String, String> mRetailerProp;
    private Map<String, String> mRetTgtAchv;
    private boolean hasOrderScreen;
    private String mSelecteRetailerType = "ALL";
    RetailerSelectionAdapter retailerSelectionAdapter;
    private RetailerSelectionAdapter.ViewHolder mSelectedRetailer;
    private AutoCompleteTextView mBrandAutoCompleteTV;
    private MapViewListener mapViewListener;
    private boolean isFromPlanning = false;
    private boolean isFromPlanningSub = false;

    private ArrayList<StandardListBO> mRetailerSelectionList;

    private TextView tv_storeVisit;
    TextView tv_target1, tv_target;

    private int mSelectedPostion = -1;
    private StandardListBO mSelectedMenuBO;

    SubDSelectionDialog subDSelectionDialog;
    private int mSelectedSubId = -1;
    private static Context context;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.planning_tab_new, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setElevation(0);
        }

        setScreenTitle(bmodel.configurationMasterHelper.getTradecoveragetitle());
        DashBoardHelper.getInstance(getActivity()).loadProductiveCallsConfig();

        if (bmodel.beatMasterHealper.getBeatMaster() == null || bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        BeatMasterBO b = getTodayBeat();
        if (b != null) {
            bmodel.beatMasterHealper.setTodayBeatMasterBO(b);
        } else {
            BeatMasterBO tempBeat = new BeatMasterBO();
            tempBeat.setBeatId(0);
            if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                tempBeat.setBeatDescription(getResources().getString(
                        R.string.all));
            } else {
                tempBeat.setBeatDescription("No Plan");
            }
            tempBeat.setToday(0);
            bmodel.beatMasterHealper.setTodayBeatMasterBO(tempBeat);

        }


        if (bmodel.configurationMasterHelper.SHOW_RETAILER_SELECTION_FILTER) {
            bmodel.mRetailerHelper.downloadRetailerFilterSelection(RETAILER_FILTER_MENU_TYPE);
            mRetailerSelectionList = bmodel.mRetailerHelper.getRetailerSelectionFilter();
            StandardListBO standardListBO = new StandardListBO();
            standardListBO.setListCode("ALL");
            standardListBO.setListName("All");
            mRetailerSelectionList.add(0, standardListBO);
        }

        mRetailerProp = new HashMap<>();
        mRetTgtAchv = new HashMap<>();


        LinearLayout switchBtnLty = view.findViewById(R.id.ll_view);

        rvView = view.findViewById(R.id.rvList);
        rvView.setLayoutManager(new LinearLayoutManager(getActivity()));

        //update IsOrderWithoutInvoice flag only if seller is van seller or seller dialog is enabled.
        if (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED || bmodel.configurationMasterHelper.IS_INVOICE)
            bmodel.updateIsOrderWithoutInvoice();

        hasOrderScreen = hasOrderScreenEnabled();

        FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), PlanningActivity.class);
                if (isFromPlanning)
                    intent.putExtra("From", MENU_PLANNING);
                else if (isFromPlanningSub)
                    intent.putExtra("From", MENU_PLANNING_SUB);
                startActivityForResult(intent, PROFILE_REQUEST_CODE);
            }
        });

        ImageView mapImageView = view.findViewById(R.id.map_viewchange);
        ImageView crossLine = view.findViewById(R.id.cross_line);
        CardView cardView = view.findViewById(R.id.card_view);
        CardView cardView1 = view.findViewById(R.id.card_view1);
        tv_storeVisit = view.findViewById(R.id.tv_store_visit);
        tv_storeVisit.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        crossLine.setRotation(-5);

        if (getArguments() != null) {
            isFromPlanning = getArguments().getBoolean("isPlanning", false);
            isFromPlanningSub = getArguments().getBoolean("isPlanningSub", false);
        }



        if (isFromPlanning || !bmodel.configurationMasterHelper.IS_MAP)
            switchBtnLty.setVisibility(View.GONE);
        else
            switchBtnLty.setVisibility(View.VISIBLE);

        mapImageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mapViewListener.switchMapView();
            }
        });

        if (bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION)
            fab.setVisibility(View.GONE);
        else if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES
                || bmodel.configurationMasterHelper.IS_NEARBY
                || bmodel.configurationMasterHelper.SHOW_MISSED_RETAILER
                || bmodel.configurationMasterHelper.IS_ADHOC) {
            fab.setVisibility(View.VISIBLE);
        } else {
            fab.setVisibility(View.GONE);
        }




        /* Show/Hide the "all route filter" **/
        if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
            cardView1.setVisibility(View.GONE);
            cardView.setVisibility(View.VISIBLE);
        } else {
            cardView.setVisibility(View.GONE);
            cardView1.setVisibility(View.VISIBLE);

            Spinner daySpinner = view.findViewById(R.id.routeSpinner);

            class BeatAdapter extends ArrayAdapter<BeatMasterBO> {

                Context context;
                int resource, textViewResourceId;
                List<BeatMasterBO> items, tempItems, suggestions;

                private BeatAdapter(Context context, int resource, int textViewResourceId, List<BeatMasterBO> items) {
                    super(context, resource, textViewResourceId, items);
                    this.context = context;
                    this.resource = resource;
                    this.textViewResourceId = textViewResourceId;
                    this.items = items;
                    tempItems = new ArrayList<>(items); // this makes the difference.
                    suggestions = new ArrayList<>();
                }

                @NonNull
                @Override
                public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = convertView;
                    if (convertView == null) {
                        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        view = inflater != null ? inflater.inflate(R.layout.row_dropdown, parent, false) : null;
                    }
                    BeatMasterBO beatMasterBO = items.get(position);
                    if (beatMasterBO != null) {
                        TextView lblName = (TextView) (view != null ? view.findViewById(R.id.lbl_name) : null);
                        if (lblName != null)
                            lblName.setText(beatMasterBO.getBeatDescription());
                    }
                    return view;
                }

                @NonNull
                @Override
                public Filter getFilter() {
                    return nameFilter;
                }

                /**
                 * Custom Filter implementation for custom suggestions we provide.
                 */
                final Filter nameFilter = new Filter() {
                    @Override
                    public CharSequence convertResultToString(Object resultValue) {
                        return resultValue.toString();
                    }

                    @Override
                    protected FilterResults performFiltering(CharSequence constraint) {
                        if (constraint != null) {
                            suggestions.clear();
                            for (BeatMasterBO bmBO : tempItems) {
                                if (constraint.toString().equalsIgnoreCase("all")) constraint = "";
                                if (bmBO.toString().toLowerCase().contains(constraint.toString().toLowerCase())) {
                                    suggestions.add(bmBO);
                                }
                            }
                            FilterResults filterResults = new FilterResults();
                            filterResults.values = suggestions;
                            filterResults.count = suggestions.size();
                            return filterResults;
                        } else {
                            return new FilterResults();
                        }
                    }

                    @Override
                    protected void publishResults(CharSequence constraint, FilterResults results) {

                        if (constraint != null) {
                            List<BeatMasterBO> filterList = (ArrayList<BeatMasterBO>) results.values;
                            if (results.count > 0) {
                                clear();
                                for (BeatMasterBO flList : filterList) {
                                    add(flList);
                                    notifyDataSetChanged();
                                }
                            }
                        } else {
                            clear();
                            for (BeatMasterBO flList : tempItems) {
                                add(flList);
                                notifyDataSetChanged();
                            }

                        }


                    }
                };
            }

            ArrayList<BeatMasterBO> beatBOArray = new ArrayList<>();
            beatBOArray.add(new BeatMasterBO(0, getResources().getString(
                    R.string.all), 0));

            if (bmodel.configurationMasterHelper.IS_BEAT_WISE_RETAILER_DOWNLOAD && bmodel.configurationMasterHelper.IS_ADHOC) {
                ArrayList<BeatMasterBO> adhocBeatList = bmodel.beatMasterHealper.downloadBeatsAdhocPlanned();
                beatBOArray.addAll(adhocBeatList);
            } else {
                beatBOArray.addAll(bmodel.beatMasterHealper.getBeatMaster());
            }
            ArrayAdapter<BeatMasterBO> brandAdapter = new BeatAdapter(
                    getActivity(), R.layout.row_dropdown, R.id.lbl_name,
                    beatBOArray);
            brandAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_item);
            daySpinner.setAdapter(brandAdapter);

            mBrandAutoCompleteTV = view.findViewById(R.id.autoCompleteTextView1);
            mBrandAutoCompleteTV.setAdapter(brandAdapter);
            mBrandAutoCompleteTV.setThreshold(1);
            mBrandAutoCompleteTV.setSelection(0);


            bmodel.daySpinnerPositon = 0;
            BeatMasterBO beatmasterbo = brandAdapter.getItem(0);
            bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);


            loadData(beatmasterbo != null ? beatmasterbo.getBeatId() : 0, null);
            mBrandAutoCompleteTV.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    mBrandAutoCompleteTV.showDropDown();
                    return false;
                }
            });
            mBrandAutoCompleteTV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    bmodel.daySpinnerPositon = position;
                    BeatMasterBO beatmasterbo = (BeatMasterBO) parent
                            .getItemAtPosition(position);
                    bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
                    loadData(beatmasterbo.getBeatId(), null);
                }
            });
            if (brandAdapter.getCount() > 0) {
                daySpinner.setSelection(bmodel.daySpinnerPositon);
            }

            daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int position, long id) {
                    bmodel.daySpinnerPositon = position;
                    BeatMasterBO beatmasterbo = (BeatMasterBO) parent
                            .getItemAtPosition(position);
                    bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
                    loadData(beatmasterbo.getBeatId(), null);
                }

                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        }
        /* End of show all routes **/

        if (getArguments() != null)
            calledBy = getArguments().getString("From");

        if (calledBy == null)
            calledBy = MENU_VISIT;

        updateRetailerAttributes();
        updateRetailerProperty();

        bmodel.mRetailerHelper.IsRetailerGivenNoVisitReason();

        TextView tvStoreLbl = view.findViewById(R.id.tv_label);
        tvStoreLbl.setTypeface(bmodel.configurationMasterHelper
                .getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        TextView lbl_BeatLoc = view.findViewById(R.id.label_BeatLoc);
        lbl_BeatLoc.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView lbl_StoreToVisit = view.findViewById(R.id.label_StoreToVisit);
        lbl_StoreToVisit.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView lbl_TodayTgt = view.findViewById(R.id.label_TodayTgt);
        lbl_TodayTgt.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.label_TodayTgt).getTag()) != null)
                ((TextView) view.findViewById(R.id.label_TodayTgt))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.label_TodayTgt)
                                        .getTag()));

        } catch (Exception e) {
            Commons.printException(e);
            if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT)
                lbl_TodayTgt.setText(getResources().getString(R.string.store_visited));
            if (bmodel.configurationMasterHelper.SHOW_TOTAL_ACHIEVED_VOLUME)
                lbl_TodayTgt.setText(getString(R.string.total_vol));
            if (bmodel.configurationMasterHelper.SHOW_TOTAL_ACHIEVED_VOLUME_WGT)
                lbl_TodayTgt.setText(getString(R.string.total_weight));
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(tvStoreLbl.getTag()) != null)
                tvStoreLbl.setText(bmodel.labelsMasterHelper
                        .applyLabels(tvStoreLbl.getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        TextView spinnerLabel = view.findViewById(R.id.spinnerLabel);
        spinnerLabel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView tv_areaLoc = view.findViewById(R.id.daytv);
        tv_areaLoc.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        tv_areaLoc.setText(bmodel.getDay(bmodel.userMasterHelper
                .getUserMasterBO().getDownloadDate()));

        ImageView img_beatloc = view.findViewById(R.id.img_beatloc);
        ArrayList<String> weekdays = new ArrayList<>();
        weekdays.add("Sunday");
        weekdays.add("Monday");
        weekdays.add("Tuesday");
        weekdays.add("Wednesday");
        weekdays.add("Thursday");
        weekdays.add("Friday");
        weekdays.add("Saturday");

        if (weekdays.contains(tv_areaLoc.getText().toString())) {
            lbl_BeatLoc.setText(getResources().getString(R.string.day_plan));
            img_beatloc.setImageResource(R.drawable.ic_calendar_visit);
        } else {
            lbl_BeatLoc.setText(getResources().getString(R.string.beat_loc));
            img_beatloc.setImageResource(R.drawable.arealocation);
        }

        tv_target = view.findViewById(R.id.tv_tgt);
        tv_target.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.label_BeatLoc).getTag()) != null)
                ((TextView) view.findViewById(R.id.label_BeatLoc))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.label_BeatLoc)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
            if (weekdays.contains(tv_areaLoc.getText().toString())) {
                lbl_BeatLoc.setText(getResources().getString(R.string.day_plan));
                img_beatloc.setImageResource(R.drawable.ic_calendar_visit);
            } else {
                lbl_BeatLoc.setText(getResources().getString(R.string.beat_loc));
                img_beatloc.setImageResource(R.drawable.arealocation);
            }
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.label_StoreToVisit).getTag()) != null)
                ((TextView) view.findViewById(R.id.label_StoreToVisit))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.label_StoreToVisit)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);

        }

        tv_target1 = view.findViewById(R.id.tv_tgt1);
        tv_target1.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        TextView lbl_TodayTgt1 = view.findViewById(R.id.label_TodayTgt1);
        lbl_TodayTgt1.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        if (bmodel.configurationMasterHelper.IS_GST || bmodel.configurationMasterHelper.IS_GST_HSN)
            bmodel.productHelper.taxHelper = TaxGstHelper.getInstance(getActivity());
        else
            bmodel.productHelper.taxHelper = TaxHelper.getInstance(getActivity());

        if (bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            if (bmodel.mSelectedSubId != -1) {
                mSelectedSubId = bmodel.mSelectedSubId;
            } else {
                if (bmodel.getSubDMaster().size() == 1) {
                    bmodel.mSelectedSubId = bmodel.getSubDMaster().get(0).getSubdId();
                    mSelectedSubId = bmodel.mSelectedSubId;
                } else if (bmodel.getSubDMaster().size() > 1) {
                    if (subDSelectionDialog == null) {
                        subDSelectionDialog = new SubDSelectionDialog();
                        Bundle args = new Bundle();
                        args.putInt("subDId", mSelectedSubId);
                        subDSelectionDialog.setArguments(args);
                        subDSelectionDialog.setSubIdSelectionInterface(this);
                        subDSelectionDialog.show(getActivity().getSupportFragmentManager(), "SubDSelectionDialog");
                    }
                }
            }
        }
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_search, menu);
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getActivity().getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                displayTodayRoute(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.search)
                .setVisible(true);
        menu.findItem(R.id.menu_deviate_retailers).setVisible(false);

        menu.findItem(R.id.menu_selection_filter)
                .setVisible(bmodel.configurationMasterHelper.SHOW_RETAILER_SELECTION_FILTER);

        menu.findItem(R.id.menu_subd_selection)
                .setVisible(bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION && bmodel.getSubDMaster().size() > 1);

        if (calledBy.equals(MENU_VISIT)
                && bmodel.configurationMasterHelper.SHOW_JOINT_CALL) {
            menu.findItem(R.id.menu_joincall).setVisible(true);

        }

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i1 == R.id.menu_joincall) {
            Intent planningIntent = new Intent(getActivity(),
                    JoinCallActivity.class);
            planningIntent.putExtra("From", "JOINT_CALL");

            startActivity(planningIntent);
            return true;
        } else if (i1 == R.id.menu_selection_filter) {
            CustomFragment dialogFragment = new CustomFragment();
            dialogFragment.setCallback(this);
            Bundle bundle = new Bundle();
            bundle.putString("title", "Retailer SelectionType");
            bundle.putSerializable("mylist", mRetailerSelectionList);
            dialogFragment.setArguments(bundle);
            dialogFragment.show(getChildFragmentManager(), "Sample Fragment");
        } else if (i1 == R.id.menu_subd_selection) {
            if (subDSelectionDialog == null) {
                subDSelectionDialog = new SubDSelectionDialog();
                Bundle args = new Bundle();
                args.putInt("subDId", mSelectedSubId);
                subDSelectionDialog.setArguments(args);
                subDSelectionDialog.setSubIdSelectionInterface(this);
                subDSelectionDialog.show(getActivity().getSupportFragmentManager(), "SubDSelectionDialog");
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        profileClick = false;
        isClicked = false;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        try {
            retailer = new ArrayList<>();

            if (!bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                displayTodayRoute(null);
            } else {

                if (mBrandAutoCompleteTV.getAdapter().getCount() > 0) {

                    bmodel.beatMasterHealper.setTodayBeatMasterBO(((BeatMasterBO) mBrandAutoCompleteTV.getAdapter().getItem(bmodel.daySpinnerPositon)));
                    mBrandAutoCompleteTV.setText(((BeatMasterBO) mBrandAutoCompleteTV.getAdapter().getItem(bmodel.daySpinnerPositon)).getBeatDescription());
                    mBrandAutoCompleteTV.dismissDropDown();
                    loadData(((BeatMasterBO) mBrandAutoCompleteTV.getAdapter().getItem(bmodel.daySpinnerPositon)).getBeatId(), null);
                    //
                } else
                    loadData(0, null);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }


        if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT)
            tv_target.setText(String.valueOf(getStoreVisited()));

            //cpg137 - Tid18
        else if (bmodel.configurationMasterHelper.SHOW_TOTAL_ACHIEVED_VOLUME_WGT)
            tv_target.setText(String.valueOf(getTotalWeight()));

            //cpg132-task13
        else if (bmodel.configurationMasterHelper.SHOW_TOTAL_ACHIEVED_VOLUME)
            tv_target.setText(String.valueOf(getTotalVolume()));
        else
            tv_target.setText(getTotalVisitActual());


        if (bmodel.configurationMasterHelper.SHOW_STORE_VISITED_COUNT)
            tv_target1.setText(String.valueOf(getStoreVisited()));
        else
            tv_target1.setText(getTotalVisitActual());

        if (retailerSelectionAdapter != null)
            retailerSelectionAdapter.notifyDataSetChanged();

    }


    private void displayTodayRoute(String filter) {

        int siz = bmodel.getRetailerMaster().size();
        retailer = new ArrayList<>();
        startVistitRetailers = new ArrayList<>();
        ArrayList<RetailerMasterBO> retailerWIthSequence = new ArrayList<>();
        ArrayList<RetailerMasterBO> retailerWithoutSequence = new ArrayList<>();


        if (!bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            /* Add today's retailers. **/
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsToday() == 1) {
                    if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                        if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;
                        } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                            continue;
                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                        if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                            continue;

                        } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;

                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                        continue;
                    }
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase()) ||
                                (bmodel.getRetailerMaster().get(i)
                                        .getRetailerCode().toLowerCase())
                                        .contains(filter.toLowerCase())) {

                            if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                                retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                            } else {
                                retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                            }

                        }
                    } else {
                        if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                            retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                        } else {
                            retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                        }
                    }
                }
            }

            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            retailer.addAll(retailerWIthSequence);
            retailer.addAll(retailerWithoutSequence);
            startVistitRetailers.addAll(retailerWIthSequence);

            /* Add today'sdeviated retailers. **/
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getIsDeviated() != null && "Y".equals(bmodel.getRetailerMaster().get(i).getIsDeviated())) {
                    if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                        if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;
                        } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                            continue;
                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                        if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                            continue;

                        } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                                && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                            continue;

                        }

                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                        continue;
                    } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                        continue;
                    } else if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES && ("Y").equals(bmodel.getRetailerMaster().get(i).getIsNew())) {
                        continue;
                    }
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase()) ||
                                (bmodel.getRetailerMaster().get(i)
                                        .getRetailerCode().toLowerCase())
                                        .contains(filter.toLowerCase())) {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }
                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getIsDeviated() != null && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsDeviated())))
                                && bmodel.getRetailerMaster().get(i).getIsToday() == 0) {
                            if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                    .toLowerCase()).contains(filter.toLowerCase()) ||
                                    (bmodel.getRetailerMaster().get(i)
                                            .getRetailerCode() != null && (bmodel.getRetailerMaster().get(i)
                                            .getRetailerCode().toLowerCase())
                                            .contains(filter.toLowerCase()))) {
                                retailer.add(bmodel.getRetailerMaster().get(i));
                            }
                        }

                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }
                }
            }

        } else {
            for (int i = 0; i < siz; i++) {
                if (bmodel.getRetailerMaster().get(i).getDistributorId() == mSelectedSubId &&
                        bmodel.getRetailerMaster().get(i).getSubdId() == 0) {

                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase()) ||
                                (bmodel.getRetailerMaster().get(i)
                                        .getRetailerCode().toLowerCase())
                                        .contains(filter.toLowerCase())) {

                            if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                                retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                            } else {
                                retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                            }

                        }
                    } else {
                        if (bmodel.getRetailerMaster().get(i).getWalkingSequence() != 0) {
                            retailerWIthSequence.add(bmodel.getRetailerMaster().get(i));
                        } else {
                            retailerWithoutSequence.add(bmodel.getRetailerMaster().get(i));
                        }
                    }
                }
            }

            Collections.sort(retailerWIthSequence, RetailerMasterBO.WalkingSequenceComparator);
            Collections.sort(retailerWithoutSequence, RetailerMasterBO.RetailerNameComparator);
            retailer.addAll(retailerWIthSequence);
            retailer.addAll(retailerWithoutSequence);
            startVistitRetailers.addAll(retailerWIthSequence);
        }

        if (!hasOrderScreen)
            setRetailerDoneforNoOrderMenu(retailer);
        retailerSelectionAdapter = new RetailerSelectionAdapter(
                retailer);
        retailerSelectionAdapter.notifyDataSetChanged();

        String strCount = retailerSelectionAdapter.getItemCount() + "";
        tv_storeVisit.setText(strCount);
        rvView.setAdapter(retailerSelectionAdapter);
        setHasOptionsMenu(true);

    }


    private void loadData(int beatId, String filter) {

        retailer = new ArrayList<>();
        int siz = bmodel.getRetailerMaster().size();
        if (!bmodel.configurationMasterHelper.SUBD_RETAILER_SELECTION) {
            for (int i = 0; i < siz; i++) {

                if (mSelecteRetailerType.equalsIgnoreCase(CODE_DEAD_STORE) && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeadStore())) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_GOLDEN_STORE) && bmodel.getRetailerMaster().get(i).getIsGoldStore() != 1) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_HANGING) && !bmodel.getRetailerMaster().get(i).isHangingOrder()) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_INDICATIVE) && bmodel.getRetailerMaster().get(i).getIndicateFlag() != 1) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_NON_PRODUCTIVE)) {
                    if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && ("Y").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                        continue;
                    } else if (!bmodel.configurationMasterHelper.IS_INVOICE && ("Y").equals(bmodel.getRetailerMaster().get(i).isOrdered())) {
                        continue;
                    }

                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_PRODUCTIVE)) {
                    if (!("Y".equals(bmodel.getRetailerMaster().get(i).isOrdered()))) {

                        continue;

                    } else if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && ("N").equals(bmodel.getRetailerMaster().get(i).isInvoiceDone())) {
                        continue;

                    }

                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_QDVP3) && !("1".equals(bmodel.getRetailerMaster().get(i).getRField4()))) {
                    continue;
                } else if (mSelecteRetailerType.equalsIgnoreCase(CODE_VISITED) && !("Y".equals(bmodel.getRetailerMaster().get(i).getIsVisited()))) {
                    continue;
                } else if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES && ("Y").equals(bmodel.getRetailerMaster().get(i).getIsNew())) {
                    continue;
                }

                if ((bmodel.getRetailerMaster().get(i).getBeatID() == beatId || beatId == 0)
                        && (bmodel.getRetailerMaster().get(i).getIsDeviated() != null
                        && ("N").equals(bmodel.getRetailerMaster().get(i).getIsDeviated()))) {

                    if (filter != null) {
                        if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                .toLowerCase()).contains(filter.toLowerCase())) {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }
                    } else {
                        retailer.add(bmodel.getRetailerMaster().get(i));
                    }

                }

            }
        } else {
            for (int i = 0; i < siz; i++) {

                if (bmodel.getRetailerMaster().get(i).getDistributorId() == mSelectedSubId &&
                        bmodel.getRetailerMaster().get(i).getSubdId() == 0) {

                    if ((bmodel.getRetailerMaster().get(i).getBeatID() == beatId || beatId == 0)) {

                        if (filter != null) {
                            if ((bmodel.getRetailerMaster().get(i).getRetailerName()
                                    .toLowerCase()).contains(filter.toLowerCase())) {
                                retailer.add(bmodel.getRetailerMaster().get(i));
                            }
                        } else {
                            retailer.add(bmodel.getRetailerMaster().get(i));
                        }

                    }
                }
            }
        }
        if (!hasOrderScreen)
            setRetailerDoneforNoOrderMenu(retailer);
        retailerSelectionAdapter = new RetailerSelectionAdapter(
                new ArrayList<>(retailer));
        String strCount = "" + retailerSelectionAdapter.getItemCount();
        tv_storeVisit.setText(strCount);
        rvView.setAdapter(retailerSelectionAdapter);
    }

    private void loadFilteredData(String filter) {

        if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
            loadData(0, filter);
        } else {
            displayTodayRoute(filter);
        }

    }

    public boolean isClicked() {
        return isClicked;
    }

    public void setClicked(boolean isClicked) {
        this.isClicked = isClicked;
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {

    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {
        retailerSelectionAdapter.notifyDataSetChanged();
        profileClick = false;

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void updateRetailerAttributes() {
        List<VisitConfiguration> visitConfig;
        if (calledBy.equals(MENU_PLANNING)) {
            visitConfig = bmodel.mRetailerHelper.getVisitPlanning();
        } else {
            visitConfig = bmodel.mRetailerHelper.getVisitCoverage();
        }

        if (visitConfig != null) {
            for (VisitConfiguration configObj : visitConfig)
                mRetTgtAchv.put(configObj.getCode(), configObj.getDesc());
        }
    }

    private void updateRetailerProperty() {

        mRetailerProp = new HashMap<>();
        for (ConfigureBO configureBO : bmodel.configurationMasterHelper
                .getRetailerPropertyList()) {
            mRetailerProp.put(configureBO.getConfigCode(), configureBO.getRField());
        }
    }

    private boolean hasOrderScreenEnabled() {
        List<ConfigureBO> menuDB = bmodel.configurationMasterHelper
                .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
        for (ConfigureBO configureBO : menuDB) {
            if ((("MENU_ORDER").equals(configureBO.getConfigCode()) ||
                    (MENU_STK_ORD).equals(configureBO.getConfigCode()) && configureBO.getHasLink() == 1)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSurveyDone(String menucode, String rid) {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select uid from "
                    + DataMembers.tbl_AnswerHeader + " where retailerid="
                    + bmodel.QT(rid)
                    + " and menucode=" + bmodel.QT(menucode));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    private void setRetailerDoneforNoOrderMenu(ArrayList<RetailerMasterBO> retailer) {
        List<TempBO> outletDetails = null;
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT DISTINCT ModuleCode, RetailerID FROM OutletTimeStampDetail INNER JOIN HhtMenuMaster ON (HHTCode = ModuleCode  AND MenuType = 'ACT_MENU' AND FLAG =1 AND hasLink = 1) WHERE (ModuleCode <>'MENU_CLOSE_CALL') ORDER BY RetailerID");
            if (c != null) {
                outletDetails = new ArrayList<>();
                while (c.moveToNext()) {
                    TempBO bo = new TempBO();
                    bo.setModuleCode(c.getString(0));
                    bo.setRetailerId(c.getString(1));
                    outletDetails.add(bo);
                }
                c.close();
            }
            db.closeDB();
            for (RetailerMasterBO ret : retailer) {
                if (!ret.isSurveyDone())
                    if (outletDetails != null && !outletDetails.isEmpty()) {
                        for (TempBO tBo : outletDetails) {
                            if (tBo.getRetailerId().equals(ret.getRetailerID()))
                                if (isSurveyDone(tBo.getModuleCode(), ret.getRetailerID())) {
                                    ret.setIsSurveyDone(true);
                                    break;
                                }
                        }
                    }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PROFILE_REQUEST_CODE && resultCode == 2) {
            updateCancel();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }


    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        // To Do updateFromFiveLevelFilter

    }

    @Override
    public void updateRetailerSelectionType(String type) {
        mSelecteRetailerType = type;
        loadFilteredData(null);
    }

    @Override
    public void onSubIdSelected(int subID) {
        mSelectedSubId = subID;
        bmodel.mSelectedSubId = subID;
        if (bmodel.configurationMasterHelper.SHOW_ALL_ROUTES) {
            loadData(0, null);
        } else {
            displayTodayRoute(null);
        }
        subDSelectionDialog = null;
    }


    private class TempBO {
        private String moduleCode;
        private String retailerId;

        public String getModuleCode() {
            return moduleCode;
        }

        public void setModuleCode(String moduleCode) {
            this.moduleCode = moduleCode;
        }

        public String getRetailerId() {
            return retailerId;
        }

        public void setRetailerId(String retailerId) {
            this.retailerId = retailerId;
        }
    }

    /**
     * Get today beat object by searching the beatmaster vector.
     *
     * @return -Today beat
     */
    private BeatMasterBO getTodayBeat() {

        if (bmodel.beatMasterHealper.getBeatMaster() != null) {
            for (BeatMasterBO beatMasterBO : bmodel.beatMasterHealper.getBeatMaster()) {
                if (beatMasterBO.getToday() == 1)
                    return beatMasterBO;
            }
        }

        return null;
    }

    private String getTotalVisitActual() {
        String totalActual = "";
        double value = 0.0;

        for (RetailerMasterBO retObj : bmodel.getRetailerMaster()) {
            value += retObj.getVisit_Actual();
        }
        totalActual = bmodel.formatValue(value);

        return totalActual;
    }

    private int getStoreVisited() {
        int count = 0;
        try {

            for (RetailerMasterBO retObj : bmodel.getRetailerMaster()) {
                if (retObj.getIsVisited() != null || retObj.getIsDeviated() != null)
                    if (retObj.getIsVisited().equalsIgnoreCase("Y")
                            && (retObj.getIsToday() == 1 || retObj.getIsDeviated().equalsIgnoreCase("Y"))) {
                        count++;
                    }

            }
        } catch (Exception e) {

        }
        return count;
    }

    private String getTotalVolume() {
        tv_target.setTextSize(14);
        DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME);
        db.openDataBase();
        int pcQty = 0;
        int caseQty = 0;
        int outQty = 0;
        try {

            Cursor c = db.selectSQL("select sum (pieceqty) from OrderDetail");
            if (c != null) {
                if (c.moveToNext()) {
                    pcQty = c.getInt(0);

                }
                c.close();
            }


            Cursor c1 = db.selectSQL("select sum (caseQty) from OrderDetail");

            if (c1 != null) {
                if (c1.moveToNext()) {
                    caseQty = c1.getInt(0);
                }
                c1.close();
            }


            Cursor c2 = db.selectSQL("select sum (outerQty) from OrderDetail");
            if (c2 != null) {
                if (c2.moveToNext()) {
                    outQty = c2.getInt(0);
                }
                c2.close();
            }


        } catch (Exception e) {
            Commons.printException("" + e);
        }
        db.closeDB();


        try {

            StringBuilder sb = new StringBuilder();
            String op = getString(R.string.item_piece);
            String oc = getString(R.string.item_case);
            String ou = getString(R.string.item_outer);

            if (bmodel.labelsMasterHelper
                    .applyLabels("item_piece") != null)
                op = bmodel.labelsMasterHelper
                        .applyLabels("item_piece");
            if (bmodel.labelsMasterHelper
                    .applyLabels("item_case") != null)
                oc = bmodel.labelsMasterHelper
                        .applyLabels("item_case");

            if (bmodel.labelsMasterHelper
                    .applyLabels("item_outer") != null)
                ou = bmodel.labelsMasterHelper
                        .applyLabels("item_outer");


            if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {

                sb.append(op + " " + pcQty);
            }

            if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {

                if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    sb.append(" : " + oc + " " + (caseQty));
                else
                    sb.append(caseQty + " " + oc + " ");
            }
            if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS || bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    sb.append(" : " + ou + " " + outQty);
                else
                    sb.append(ou + " " + outQty);
            }

            return sb.toString();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    private String getTotalWeight() {
        double weight = 0;
        try {

            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("select pieceqty,caseQty,outerQty,uomcount,dOuomQty,weight from OrderDetail");
            if (c != null) {
                while (c.moveToNext()) {
                    int qty = c.getInt(0) +
                            (c.getInt(1) * c.getInt(3) +
                                    (c.getInt(2) * c.getInt(4)));
                    weight = weight + (qty * c.getDouble(5));
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

        return bmodel.formatValue(weight);
    }

    public interface MapViewListener {
        void switchMapView();
    }


    public void setMapViewListener(MapViewListener listener) {
        this.mapViewListener = listener;
    }

    @Override
    public boolean onQueryTextChange(String s) {
        displayTodayRoute(s);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        if (s.isEmpty()) {
            displayTodayRoute(null);
        }
        return false;
    }


    class ValidateRetailerVisit extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;
        String Url;
        RetailerSelectionAdapter.ViewHolder holder;
        private ProgressDialog progressDialogue;

        ValidateRetailerVisit(RetailerSelectionAdapter.ViewHolder holder, String Url) {
            this.Url = Url;
            this.holder = holder;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.validating_retailer_visit),
                    true, false);
            jsonObject = bmodel.synchronizationHelper.getCommonJsonObject();
        }

        @Override
        protected String doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken(false);
            String response = bmodel.synchronizationHelper.sendPostMethod(Url, jsonObject);
            String errorCode = "E01";
            try {
                JSONObject jsonObject = new JSONObject(response);
                Iterator itr = jsonObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        errorCode = jsonObject.getString(key);
                        if (errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                            int validateStatus = jsonObject.getInt("Response");
                            SharedPreferences.Editor editor = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity())
                                    .edit();
                            editor.putInt("trade_coverage_validation",
                                    validateStatus);
                            editor.commit();

                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print(jsonExpection.getMessage());
            }
            return errorCode;
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            progressDialogue.dismiss();
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                if (errorCode
                        .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    //proceed to retailer Selection
                    SharedPreferences sharedPrefs = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    int validate = sharedPrefs.getInt("trade_coverage_validation", 0);
                    if (validate == 1) {
                        mSelectedRetailer = holder;

                        bmodel.setRetailerMasterBO(holder.retailerObjectHolder);
                        bmodel.setVisitretailerMaster(startVistitRetailers);
                        startVisit = calledBy.equals(MENU_PLANNING);

                        if (!profileClick) {
                            profileClick = true;
                            if (bmodel.configurationMasterHelper.isRetailerBOMEnabled && SDUtil.convertToInt(bmodel.getRetailerMasterBO().getCredit_invoice_count()) <= 0) {
                                bmodel.mRetailerHelper.downloadRetailerWiseDeadPdts(SDUtil.convertToInt(holder.retailerObjectHolder.getRetailerID()));
                            }
                            Intent i = new Intent(getActivity(), ProfileActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            if (isFromPlanning) {
                                i.putExtra("From", MENU_PLANNING);
                                i.putExtra("isPlanning", true);
                            } else if (isFromPlanningSub) {
                                i.putExtra("From", MENU_PLANNING_SUB);
                                i.putExtra("isPlanningSub", true);
                            } else {
                                i.putExtra("From", MENU_VISIT);
                                i.putExtra("visit", startVisit);
                                i.putExtra("locvisit", true);
                            }

                            startActivity(i);
                        }
                    } else {
                        bmodel.showAlert(getResources().getString(R.string.validation_msg), 0);
                    }
                } else {
                    String errorMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorCode);
                    if (errorMessage != null) {
                        bmodel.showAlert(errorMessage, 0);
                    }
                }
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private class RetailerSelectionAdapter extends RecyclerView.Adapter<RetailerSelectionAdapter.ViewHolder> {
        private final ArrayList<RetailerMasterBO> items;
        boolean isFirstDone = false;
        boolean isSecondDone = false;

        private RetailerSelectionAdapter(ArrayList<RetailerMasterBO> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view;
            if (bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER)
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.visit_list_rex, parent, false);
            else
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.visit_list_child_item, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.retailerObjectHolder = items.get(position);

            TypedArray typearr = Objects.requireNonNull(getActivity()).getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);

            if (!calledBy.equals(MENU_PLANNING)) {
                // bmodel.loadProductiveCallsConfig();
                if (("Y").equals(holder.retailerObjectHolder.isOrdered()) && (!bmodel.PRD_FOR_SKT)) {   // If ProductiveStockCheck is OFF
                    if (bmodel.configurationMasterHelper.IS_INVOICE && !bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && ("N").equals(holder.retailerObjectHolder.isInvoiceDone())) {
                        holder.line_order_without_invoice
                                .setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Orange));
                    } else {
                        holder.line_order_without_invoice
                                .setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
                    }
                } else if (bmodel.PRD_FOR_SKT && holder.retailerObjectHolder.isProductive().equalsIgnoreCase("Y")) { // If ProductiveStockCheck is ON and then check for Productive is done or not. This value is updated while saving the stockcheck
                    holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
                } else if (!hasOrderScreen && "Y".equals(holder.retailerObjectHolder.getIsVisited())) {
                    holder.line_order_without_invoice
                            .setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
                } else if (("Y").equals(holder.retailerObjectHolder.getIsVisited()) || holder.retailerObjectHolder.isHasNoVisitReason()) {
                    holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Orange));
                } else if (("Y").equals(holder.retailerObjectHolder.getIsDeadStore())) {
                    holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.dark_red));
                } else {
                    holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_gray));
                }

                if (("Y").equals(holder.retailerObjectHolder.getIsDeadStore())) {
                    if (!bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER) {
                        holder.tv_labelTgt1.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_text));
                        holder.tv_labelTgt2.setTextColor(ContextCompat.getColor(getActivity(), R.color.gray_text));
                    }
                } else {
                    if (!bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER) {
                        holder.tv_labelTgt1.setTextColor(color);
                        holder.tv_labelTgt2.setTextColor(color);
                    }
                }


            } else {
                if (holder.retailerObjectHolder.getLastVisitStatus() != null) {
                    switch (holder.retailerObjectHolder.getLastVisitStatus()) {
                        case "P":
                            holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
                            break;
                        case "N":
                            holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Orange));
                            break;
                        default:
                            holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_gray));
                            break;
                    }
                } else {
                    holder.line_order_without_invoice.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.light_gray));
                }

                if (!bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER) {
                    holder.tv_labelTgt1.setTextColor(color);
                    holder.tv_labelTgt2.setTextColor(color);
                }
            }

            String tvText = holder.retailerObjectHolder.getRetailerName();
            holder.outletNameTextView.setText(tvText);

            if (bmodel.configurationMasterHelper.IS_SHOW_RETAILER_LAST_VISIT)
                holder.tv_lastVisit.setText(getLastVisitData(holder.retailerObjectHolder));
            else
                holder.tv_lastVisit.setVisibility(View.GONE);


            if (!bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER) {
                isFirstDone = false;
                isSecondDone = false;

                if (bmodel.configurationMasterHelper.SHOW_RFIELD4)
                    holder.outletLocationTextView.setText(holder.retailerObjectHolder.getRField4());
                else
                    holder.outletLocationTextView.setVisibility(View.GONE);

                if (mRetTgtAchv.containsKey("VST01")) {
                    String desc = mRetTgtAchv.get("VST01");
                    holder.tv_labelTgt1.setText(desc);
                    String strActual = "/" + bmodel.formatValue(holder.retailerObjectHolder.getDaily_target());
                    holder.tv_achvTgt1.setText(bmodel.formatValue(holder.retailerObjectHolder.getVisit_Actual()));
                    holder.tv_actualTgt1.setText(strActual);
                    isFirstDone = true;
                }

                if (mRetTgtAchv.containsKey("VST02")) {
                    String desc = mRetTgtAchv.get("VST02");
                    holder.tv_labelTgt1.setText(desc);
                    String strAchv = bmodel.formatValue(holder.retailerObjectHolder.getVisit_Actual());
                    String strActual = "/" + bmodel.formatValue(holder.retailerObjectHolder.getDaily_target_planned());
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(strAchv);
                        holder.tv_actualTgt1.setText(strActual);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(strAchv);
                        holder.tv_actualTgt2.setText(strActual);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (mRetTgtAchv.containsKey("VST08")) {
                    String desc = mRetTgtAchv.get("VST08");
                    String strAchv = holder.retailerObjectHolder.getMslAch();
                    String strActual = "/" + holder.retailerObjectHolder.getMslTaget();
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(strAchv);
                        holder.tv_actualTgt1.setText(strActual);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(strAchv);
                        holder.tv_actualTgt2.setText(strActual);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (mRetTgtAchv.containsKey("VST09")) {
                    String strActual = "/" + bmodel.formatValue(holder.retailerObjectHolder.getMonthly_target());
                    String strAchv = bmodel.formatValue(holder.retailerObjectHolder.getMonthly_acheived());
                    String desc = mRetTgtAchv.get("VST09");
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(strAchv);
                        holder.tv_actualTgt1.setText(strActual);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(strAchv);
                        holder.tv_actualTgt2.setText(strActual);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (mRetTgtAchv.containsKey("VST15")) {
                    String desc = mRetTgtAchv.get("VST15");
                    String strActual = "/" + holder.retailerObjectHolder.getPlannedVisitCount();
                    int totAchv = holder.retailerObjectHolder.getVisitDoneCount() + bmodel.getTodaysVisitCount(holder.retailerObjectHolder);
                    String strAchv = "" + totAchv;
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(strAchv);
                        holder.tv_actualTgt1.setText(strActual);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(strAchv);
                        holder.tv_actualTgt2.setText(strActual);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (mRetTgtAchv.containsKey("VST11")) {
                    String desc = mRetTgtAchv.get("VST11");
                    int valueQDVP = holder.retailerObjectHolder.getSurveyHistoryScore();
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(String.valueOf(valueQDVP));
                        holder.tv_actualTgt1.setVisibility(View.GONE);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(String.valueOf(valueQDVP));
                        holder.tv_actualTgt2.setVisibility(View.GONE);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (mRetTgtAchv.containsKey("VST17")) {
                    String desc = mRetTgtAchv.get("VST17");
                    String value = holder.retailerObjectHolder.getSalesValue();
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(value);
                        holder.tv_actualTgt1.setVisibility(View.GONE);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(value);
                        holder.tv_actualTgt2.setVisibility(View.GONE);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }
                if (mRetTgtAchv.containsKey("VST18")) {
                    String desc = mRetTgtAchv.get("VST18");
                    String value = holder.retailerObjectHolder.getRField5();
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(value);
                        holder.tv_actualTgt1.setVisibility(View.GONE);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(value);
                        holder.tv_actualTgt2.setVisibility(View.GONE);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }
                if (mRetTgtAchv.containsKey("VST19")) {
                    String desc = mRetTgtAchv.get("VST19");
                    String value = holder.retailerObjectHolder.getCurrentFitScore() + "";
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(Utils.formatAsTwoDecimal(value));
                        holder.tv_actualTgt1.setVisibility(View.GONE);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(Utils.formatAsTwoDecimal(value));
                        holder.tv_actualTgt2.setVisibility(View.GONE);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (mRetTgtAchv.containsKey("VST20")) {
                    BeatMasterBO beatBo = bmodel.beatMasterHealper.getBeatMasterBOByID(holder.retailerObjectHolder.getBeatID());
                    String desc = mRetTgtAchv.get("VST20");
                    String value = beatBo.getBeatDescription();
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(value);
                        holder.tv_actualTgt1.setVisibility(View.GONE);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(value);
                        holder.tv_actualTgt2.setVisibility(View.GONE);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                //total retialers ordered weight
                if (mRetTgtAchv.containsKey("VST22")) {
                    String desc = mRetTgtAchv.get("VST22");
                    if (!isFirstDone) {
                        holder.tv_achvTgt1.setText(bmodel.formatValue(holder.retailerObjectHolder.getmOrderedTotWgt()));
                        holder.tv_actualTgt1.setVisibility(View.GONE);
                        holder.tv_labelTgt1.setText(desc);
                        isFirstDone = true;
                    } else if (!isSecondDone) {
                        holder.tv_achvTgt2.setText(bmodel.formatValue(holder.retailerObjectHolder.getmOrderedTotWgt()));
                        holder.tv_actualTgt2.setVisibility(View.GONE);
                        holder.tv_labelTgt2.setText(desc);
                        isSecondDone = true;
                    }
                }

                if (!isFirstDone) {
                    holder.ll_scoreParent.setVisibility(View.GONE);
                } else if (!isSecondDone) {
                    holder.ll_score2.setVisibility(View.GONE);
                    holder.imgLine2.setVisibility(View.GONE);
                }

            }
            else {
                String address=(holder.retailerObjectHolder.getAddress1()!=null?(holder.retailerObjectHolder.getAddress1()):"")
                        +(holder.retailerObjectHolder.getAddress2()!=null&&!holder.retailerObjectHolder.getAddress2().equals("")?(","+holder.retailerObjectHolder.getAddress2()):"")
                        + (holder.retailerObjectHolder.getAddress3()!=null&&!holder.retailerObjectHolder.getAddress3().equals("")?(","+holder.retailerObjectHolder.getAddress3()):"");

                holder.outletAddress.setText(address);
            }




            if (holder.retailerObjectHolder.getIsDeviated() != null
                    && ("Y").equalsIgnoreCase((holder.retailerObjectHolder.getIsDeviated())))
                holder.ll_iv_deviate.setVisibility(View.VISIBLE);
            else
                holder.ll_iv_deviate.setVisibility(View.GONE);


            if (!bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER) {

                if (mRetailerProp.get("RTPRTY01") != null) {
                    if (("Y").equals(holder.retailerObjectHolder.getIsDeadStore())) {
                        holder.ll_iv_gold_dead.setVisibility(View.VISIBLE);
                        holder.imgGoldDeadStore.setImageResource(R.drawable.ic_dashboard_dead_store);
                    }
                    if (mRetailerProp.get("RTPRTY01").length() > 0 && mRetailerProp.get("RTPRTY01").split("/").length == 2) {
                        holder.ll_iv_gold_dead.setVisibility(View.VISIBLE);
                        holder.imgGoldDeadStore.setImageResource(getMappedDrawableId(mRetailerProp.get("RTPRTY01")));
                        holder.imgGoldDeadStore.setColorFilter(Color.parseColor(getMappedColorCode(mRetailerProp.get("RTPRTY01"),
                                ("Y").equals(holder.retailerObjectHolder.getIsDeadStore()))));
                    }
                } else if (mRetailerProp.get("RTPRTY02") != null && holder.retailerObjectHolder.getIsGoldStore() == 1) {
                    holder.ll_iv_gold_dead.setVisibility(View.VISIBLE);
                    holder.imgGoldDeadStore.setImageResource(R.drawable.ic_action_star_select);
                } else if (mRetailerProp.get("RTPRTY02") != null
                        && ("Y").equals(holder.retailerObjectHolder.getIsVisited())
                        && holder.retailerObjectHolder.getSbdPercent() > ConfigurationMasterHelper.SBD_TARGET_PERCENTAGE) {
                    holder.ll_iv_gold_dead.setVisibility(View.VISIBLE);
                    holder.imgGoldDeadStore.setImageResource(R.drawable.ic_action_star_select);
                } else if (mRetailerProp.get("RTPRTY05") != null && !("0").equals(holder.retailerObjectHolder.getRField4())) {
                    holder.ll_iv_gold_dead.setVisibility(View.VISIBLE);
                    holder.imgGoldDeadStore.setImageResource(R.drawable.ic_action_star_select);
                    if (holder.retailerObjectHolder.getRField4() != null) {
                        try {
                            if (bmodel.mRetailerHelper.getColorCode(holder.retailerObjectHolder.getRField4()).length() > 0) {
                                holder.imgGoldDeadStore.setColorFilter(Color.parseColor(bmodel.mRetailerHelper.getColorCode(holder.retailerObjectHolder.getRField4())), PorterDuff.Mode.SRC_ATOP);
                            }
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                } else {
                    holder.ll_iv_gold_dead.setVisibility(View.GONE);
                }


                if (mRetailerProp.get("RTPRTY03") != null
                        && bmodel.configurationMasterHelper.IS_INVOICE
                        && holder.retailerObjectHolder.isHangingOrder()) {
                    holder.ll_iv_invoice.setVisibility(View.VISIBLE);
                    holder.imgInvoice.setImageResource(R.drawable.ic_dashboard_invoice);
                } else {
                    holder.ll_iv_invoice.setVisibility(View.GONE);
                }

                if (mRetailerProp.get("RTPRTY04") != null && holder.retailerObjectHolder.getIndicateFlag() == 1) {
                    holder.ll_iv_indicative.setVisibility(View.VISIBLE);
                    holder.imgIndicative.setImageResource(R.drawable.ic_dashboard_indicative);
                } else {
                    holder.ll_iv_indicative.setVisibility(View.GONE);
                }

                if (mRetailerProp.get("RTPRTY06") != null) {
                    try {
                        if (holder.retailerObjectHolder.getRField5() != null) {
                            holder.ll_iv_outlet_color.setVisibility(View.VISIBLE);

                            switch (mRetailerProp.get("RTPRTY06")) {
                                case "1":
                                    holder.iv_outlet_color.setImageResource(R.drawable.badge_circle);
                                    holder.iv_outlet_color.setColorFilter(Color.parseColor(holder.retailerObjectHolder.getRField5()));
                                    break;
                                case "2":
                                    holder.iv_outlet_color.setImageResource(R.drawable.ic_thumbs_down);
                                    if (!("1").equals(holder.retailerObjectHolder.getRField5()))
                                        holder.iv_outlet_color.setVisibility(View.GONE);
                                    break;
                                default:
                                    // for default star icon color applying
                                    holder.iv_outlet_color.setColorFilter(Color.parseColor(holder.retailerObjectHolder.getRField5()));
                                    break;
                            }
                        } else {
                            holder.ll_iv_outlet_color.setVisibility(View.GONE);
                        }
                    } catch (Exception ex) {
                        holder.ll_iv_outlet_color.setVisibility(View.GONE);
                    }
                } else
                    holder.ll_iv_outlet_color.setVisibility(View.GONE);


                if (mRetailerProp.get("RTPRTY07") != null) {
                    if (SDUtil.convertToInt(holder.retailerObjectHolder.getCredit_invoice_count()) > 0) {
                        holder.iv_dead_gold_store.setImageResource(R.drawable.ic_dashboard_indicative);
                        holder.ll_iv_dead_gold_store.setVisibility(View.VISIBLE);
                    } else if (holder.retailerObjectHolder.isBomAchieved()) {
                        holder.iv_dead_gold_store.setImageResource(R.drawable.ic_dashboard_indicative);
                        holder.ll_iv_dead_gold_store.setVisibility(View.VISIBLE);
                    } else {
                        holder.ll_iv_dead_gold_store.setVisibility(View.GONE);
                    }
                } else {
                    holder.ll_iv_dead_gold_store.setVisibility(View.GONE);
                }

                if (mRetailerProp.get("RTPRTY08") != null) {
                    if (("1").equals(holder.retailerObjectHolder.getRField4())) {
                        holder.ll_iv_asset_mapped.setVisibility(View.VISIBLE);
                        holder.iv_asset_mapped.setImageResource(R.drawable.ic_action_star_select);
                    }
                    if (mRetailerProp.get("RTPRTY08").length() > 0 && mRetailerProp.get("RTPRTY08").split("/").length == 2) {
                        holder.ll_iv_asset_mapped.setVisibility(View.VISIBLE);
                        holder.iv_asset_mapped.setImageResource(getMappedDrawableId(mRetailerProp.get("RTPRTY08")));
                        holder.iv_asset_mapped.setColorFilter(Color.parseColor(getMappedColorCode(mRetailerProp.get("RTPRTY08"),
                                ("1").equals(holder.retailerObjectHolder.getRField4()))));
                    }
                    if (holder.retailerObjectHolder.getRField4() == null)
                        holder.ll_iv_asset_mapped.setVisibility(View.GONE);
                } else {
                    holder.ll_iv_asset_mapped.setVisibility(View.GONE);
                }

                if (mRetailerProp.get("RTPRTY09") != null) {
                    if (("1").equals(holder.retailerObjectHolder.getRField8())) {
                        holder.ll_iv_cooler.setVisibility(View.VISIBLE);
                        holder.iv_cooler.setImageResource(R.drawable.ic_freeze);
                    }
                    if (mRetailerProp.get("RTPRTY09").length() > 0) {
                        if (mRetailerProp.get("RTPRTY09").split("/").length == 2) {
                            holder.ll_iv_cooler.setVisibility(View.VISIBLE);
                            holder.iv_cooler.setImageResource(getMappedDrawableId(mRetailerProp.get("RTPRTY09")));
                            holder.iv_cooler.setColorFilter(Color.parseColor(getMappedColorCode(mRetailerProp.get("RTPRTY09"),
                                    ("1").equals(holder.retailerObjectHolder.getRField8()))));
                        } else if (mRetailerProp.get("RTPRTY09").equalsIgnoreCase("Task")) {
                            holder.ll_iv_cooler.setVisibility(View.GONE);
                            holder.tvTaskCount.setVisibility(View.VISIBLE);
                            holder.tvTaskCount.setText(getResources().getString(R.string.task) + ":" + holder.retailerObjectHolder.getRField8());
                        }
                    }
                    if (holder.retailerObjectHolder.getRField8() == null)
                        holder.ll_iv_cooler.setVisibility(View.GONE);
                } else {
                    holder.ll_iv_cooler.setVisibility(View.GONE);
                }

                if (mRetailerProp.get("RTPRTY10") != null) {
                    if (("1").equals(holder.retailerObjectHolder.getRField9())) {
                        holder.ll_iv_loyality.setVisibility(View.VISIBLE);
                        holder.iv_loyalty.setImageResource(R.drawable.ic_loyalty);
                    }
                    if (mRetailerProp.get("RTPRTY10").length() > 0 && mRetailerProp.get("RTPRTY10").split("/").length == 2) {
                        holder.ll_iv_loyality.setVisibility(View.VISIBLE);
                        holder.iv_loyalty.setImageResource(getMappedDrawableId(mRetailerProp.get("RTPRTY10")));
                        holder.iv_loyalty.setColorFilter(Color.parseColor(getMappedColorCode(mRetailerProp.get("RTPRTY10"),
                                ("1").equals(holder.retailerObjectHolder.getRField9()))));
                    }
                    if (holder.retailerObjectHolder.getRField9() == null)
                        holder.ll_iv_loyality.setVisibility(View.GONE);

                } else {
                    holder.ll_iv_loyality.setVisibility(View.GONE);
                }

            }


            holder.cardView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isVisitPaused(holder.retailerObjectHolder)) {
                        if (bmodel.configurationMasterHelper.VALIDATE_TRADE_COVERAGE) {
                            SharedPreferences sharedPrefs = PreferenceManager
                                    .getDefaultSharedPreferences(getActivity());
                            int validate = sharedPrefs.getInt("trade_coverage_validation", 0);
                            if (validate == 1) {

                                mSelectedRetailer = holder;

                                bmodel.setRetailerMasterBO(holder.retailerObjectHolder);
                                bmodel.setVisitretailerMaster(startVistitRetailers);
                                startVisit = calledBy.equals(MENU_PLANNING);

                                if (!profileClick && !holder.retailerObjectHolder.getIsNew().equals("Y")) {
                                    profileClick = true;
                                    if (bmodel.configurationMasterHelper.isRetailerBOMEnabled && SDUtil.convertToInt(bmodel.getRetailerMasterBO().getCredit_invoice_count()) <= 0) {
                                        bmodel.mRetailerHelper.downloadRetailerWiseDeadPdts(SDUtil.convertToInt(holder.retailerObjectHolder.getRetailerID()));
                                    }
                                    Intent i = new Intent(getActivity(), ProfileActivity.class);
                                    i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    if (isFromPlanning) {
                                        i.putExtra("From", MENU_PLANNING);
                                        i.putExtra("isPlanning", true);
                                    } else if (isFromPlanningSub) {
                                        i.putExtra("From", MENU_PLANNING_SUB);
                                        i.putExtra("isPlanningSub", true);
                                    } else {
                                        i.putExtra("From", MENU_VISIT);
                                        i.putExtra("visit", startVisit);
                                        i.putExtra("locvisit", true);
                                    }

                                    startActivity(i);

                                }
                            } else {
                                String Url = bmodel.mRetailerHelper.getValidateUrl();
                                if (bmodel.isOnline()) {
                                    if (Url.length() > 0)
                                        new ValidateRetailerVisit(holder, Url).execute();
                                    else
                                        Toast.makeText(getActivity(), R.string.url_not_mapped, Toast.LENGTH_LONG).show();
                                } else
                                    Toast.makeText(getActivity(), R.string.please_connect_to_internet, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            mSelectedRetailer = holder;

                            bmodel.setRetailerMasterBO(holder.retailerObjectHolder);
                            bmodel.setVisitretailerMaster(startVistitRetailers);
                            startVisit = calledBy.equals(MENU_PLANNING);

                            if (!profileClick && !holder.retailerObjectHolder.getIsNew().equals("Y")) {
                                profileClick = true;
                                if (bmodel.configurationMasterHelper.isRetailerBOMEnabled && SDUtil.convertToInt(bmodel.getRetailerMasterBO().getCredit_invoice_count()) <= 0) {
                                    bmodel.mRetailerHelper.downloadRetailerWiseDeadPdts(SDUtil.convertToInt(holder.retailerObjectHolder.getRetailerID()));
                                }
                                Intent i = new Intent(getActivity(), ProfileActivity.class);
                                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                if (isFromPlanning) {
                                    i.putExtra("From", MENU_PLANNING);
                                    i.putExtra("isPlanning", true);
                                } else if (isFromPlanningSub) {
                                    i.putExtra("From", MENU_PLANNING_SUB);
                                    i.putExtra("isPlanningSub", true);
                                } else {
                                    i.putExtra("From", MENU_VISIT);
                                    i.putExtra("visit", startVisit);
                                    i.putExtra("locvisit", true);
                                }

                                startActivity(i);
                            }
                        }
                    } else {
                        Toast.makeText(getActivity(), R.string.visit_paused_msg, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private RetailerMasterBO retailerObjectHolder;

            private TextView outletNameTextView;
            private TextView outletLocationTextView;
            private TextView outletAddress;
            private ImageView imgGoldDeadStore;
            private ImageView imgInvoice;
            private ImageView imgIndicative;
            private ImageView iv_dead_gold_store;
            private ImageView iv_asset_mapped;
            private ImageView iv_cooler;
            private ImageView iv_loyalty;

            private TextView tv_labelTgt1;
            private TextView tv_actualTgt1;
            private TextView tv_achvTgt1;

            private TextView tv_labelTgt2;
            private TextView tv_actualTgt2;
            private TextView tv_achvTgt2;
            private TextView tvTaskCount;
            private TextView tv_lastVisit;

            private ImageView line_order_without_invoice;
            LinearLayout ll_score1;
            LinearLayout ll_score2;
            LinearLayout ll_scoreParent;
            private ImageView imgLine2;
            private CardView cardView;
            private ImageView iv_outlet_color;

            private LinearLayout ll_iv_outlet_color, ll_iv_gold_dead, ll_iv_invoice, ll_iv_indicative,
                    ll_iv_dead_gold_store, ll_iv_asset_mapped, ll_iv_deviate, ll_iv_cooler, ll_iv_loyality;

            public ViewHolder(View itemView) {
                super(itemView);
                cardView = itemView
                        .findViewById(R.id.card_view);

                line_order_without_invoice = itemView
                        .findViewById(R.id.line_order_without_invoice);

                outletNameTextView = itemView
                        .findViewById(R.id.outletName_tv);
                outletLocationTextView = itemView
                        .findViewById(R.id.outletLocation_tv);
                tv_lastVisit = itemView
                        .findViewById(R.id.tv_lastvisit);
                ll_iv_deviate = itemView.findViewById(R.id.ll_iv_deviate);


                if (!bmodel.configurationMasterHelper.IS_SIMPLE_RETIALER) {
                    imgGoldDeadStore = itemView
                            .findViewById(R.id.iv_gold_dead);

                    outletLocationTextView = itemView
                            .findViewById(R.id.outletLocation_tv);
                    outletLocationTextView.setTypeface(bmodel.configurationMasterHelper
                            .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                    imgInvoice = itemView
                            .findViewById(R.id.iv_invoice);

                    imgIndicative = itemView
                            .findViewById(R.id.iv_indicative);

                    iv_dead_gold_store = itemView
                            .findViewById(R.id.iv_dead_gold_store);

                    iv_asset_mapped = itemView
                            .findViewById(R.id.iv_asset_mapped);

                    iv_cooler = itemView
                            .findViewById(R.id.iv_cooler);

                    iv_loyalty = itemView
                            .findViewById(R.id.iv_loyality);

                    tv_labelTgt1 = itemView
                            .findViewById(R.id.labelTgt1);
                    tv_actualTgt1 = itemView
                            .findViewById(R.id.tv_actualTgt1);
                    tv_achvTgt1 = itemView
                            .findViewById(R.id.tv_achvTgt1);

                    tv_labelTgt2 = itemView
                            .findViewById(R.id.labelTgt2);
                    tv_actualTgt2 = itemView
                            .findViewById(R.id.tv_actualTgt2);
                    tv_achvTgt2 = itemView
                            .findViewById(R.id.tv_achvTgt2);
                    tvTaskCount = itemView
                            .findViewById(R.id.tv_task_count);

                    ll_score1 = itemView.findViewById(R.id.ll_score1);
                    ll_score2 = itemView.findViewById(R.id.ll_score2);
                    ll_scoreParent = itemView.findViewById(R.id.ll_scoreParent);
                    imgLine2 = itemView.findViewById(R.id.img_line2);


                    iv_outlet_color = itemView.findViewById(R.id.iv_outlet_color);

                    ll_iv_outlet_color = itemView.findViewById(R.id.ll_iv_outlet_color);
                    ll_iv_gold_dead = itemView.findViewById(R.id.ll_iv_gold_dead);
                    ll_iv_invoice = itemView.findViewById(R.id.ll_iv_invoice);
                    ll_iv_indicative = itemView.findViewById(R.id.ll_iv_indicative);
                    ll_iv_dead_gold_store = itemView.findViewById(R.id.ll_iv_dead_gold_store);
                    ll_iv_asset_mapped = itemView.findViewById(R.id.ll_iv_asset_mapped);
                    ll_iv_cooler = itemView.findViewById(R.id.ll_iv_cooler);
                    ll_iv_loyality = itemView.findViewById(R.id.ll_iv_loyality);
                }
                else {

                    outletAddress = itemView.findViewById(R.id.outletAddress);

                }
            }

        }
    }

    private String getMappedColorCode(String Rfield, boolean isPostive) {
        String colorCode = "#000000";
        String parts[] = Rfield.split("/");
        if (parts.length == 2) {
            try {
                if (parts[1] != null && parts[1].length() > 0) {
                    String colors[] = parts[1].split("~");
                    if (isPostive)
                        colorCode = colors[0];
                    else
                        colorCode = colors[1];
                }

            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        Commons.print("color" + colorCode);
        return colorCode;
    }

    private int getMappedDrawableId(String Rfield) {
        int resid = R.drawable.ic_action_star_select;
        String parts[] = Rfield.split("/");
        String iconName = "";
        if (parts.length == 2) {
            try {
                if (parts[0] != null && parts[0].length() > 0) {
                    iconName = parts[0];
                    switch (iconName) {
                        case ICON_COOLER:
                            resid = R.drawable.ic_freeze;
                            break;
                        case ICON_LOYALITY:
                            resid = R.drawable.ic_loyalty;
                            break;
                        case ICON_CROWN:
                            resid = R.drawable.ic_crown;
                            break;
                        case ICON_DEAD:
                            resid = R.drawable.ic_dead;
                            break;
                        case ICON_SKULL:
                            resid = R.drawable.ic_dashboard_dead_store;
                            break;
                        case ICON_ALIVE:
                            resid = R.drawable.ic_alive;
                            break;
                        default:
                            resid = R.drawable.ic_action_star_select;
                            break;
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        return resid;
    }

    private String getLastVisitData(RetailerMasterBO retailerMasterBO) {
        String strLastVisit = retailerMasterBO.getLastVisitDate() == null ? "" : DateTimeUtils.convertFromServerDateToRequestedFormat(retailerMasterBO.getLastVisitDate(), ConfigurationMasterHelper.outDateFormat);

        if (bmodel.configurationMasterHelper.IS_SHOW_RETAILER_LAST_VISITEDBY) {
            if (StringUtils.isEmptyString(strLastVisit))
                strLastVisit = retailerMasterBO.getLastVisitedBy() == null ? "" : "By : " + retailerMasterBO.getLastVisitedBy();
            else
                strLastVisit = strLastVisit + " | " + (retailerMasterBO.getLastVisitedBy() == null ? "" : "By : " + retailerMasterBO.getLastVisitedBy());
        }

        if (StringUtils.isEmptyString(strLastVisit))
            return "";
        else
            return getResources().getString(R.string.last_vist) + " " + strLastVisit;
    }


    private boolean isVisitPaused(RetailerMasterBO retailerMasterBO) {
        return bmodel.getAppDataProvider().getPausedRetailer() != null
                && !retailerMasterBO.getRetailerID().equals(bmodel.getAppDataProvider().getPausedRetailer().getRetailerID());
    }

}