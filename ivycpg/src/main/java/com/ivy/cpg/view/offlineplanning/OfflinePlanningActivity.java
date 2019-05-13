package com.ivy.cpg.view.offlineplanning;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipDescription;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.SearchView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.DragEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.GridLayoutAnimationController;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.nonfield.NonFieldBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

/**
 * Created by mansoor.k on 3/14/2018.
 */

public class OfflinePlanningActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private ListView lvRetailer, lvNonField, lvDateWise;
    private TextView tvNonField, tvRetailerCount, tvMonthName, tvDMonthName, tvDayDate, tvBackToMonthView;
    private SearchView mSearchRetailer;
    private InputMethodManager imm;
    private Vector<RetailerMasterBO> retailerList = new Vector<>();
    private Vector<NonFieldBO> nonFieldList = new Vector<>();
    private RetailerAdapter retailerAdapter;
    private NonFieldAdapter nonFieldAdapter;
    private ImageView imBack, imPrev, imNext;
    private List<CalenderBO> mCalenderAllList;
    private static final SimpleDateFormat dateFormatGeneral = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
    private int dayInWeekCount = -1;
    private Calendar currentDate = Calendar.getInstance();
    private ArrayList<String> mCampaginDates;
    private String planFromDate, planToDate;
    private GridView mCalendar;
    private CalendarAdapter madapter;
    private final MyDragEventListener myDragEventListener = new MyDragEventListener();
    private String dayWishSelectedDate = "";
    private ViewFlipper mViewFlipper;
    private OfflinePlanHelper offlinePlanHelper;
    private boolean isMonthViewVisible = true;
    private HashMap<String, ArrayList<OfflineDateWisePlanBO>> mHashMapData = new HashMap<>();
    private String hoverDate = "";
    private DayWishPlanningAdapter dayWishPlanningAdapter;
    LinearLayout layoutCalendar, layoutDayWise;
    private RsdHolder mRsdholder;
    private boolean isRetaieler = false;
    private static String mEntityRetailer = "RETAILER";
    private static String mEntityNFA = "NFA";
    private static String mEntityDistributor = "DIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        setContentView(R.layout.activity_offline_planning);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);


        offlinePlanHelper = OfflinePlanHelper.getInstance(this);


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        setPlanDates();
        initializeViews();
        setValues();
        new LoadCalendar().execute();

    }

    private void initializeViews() {

        lvRetailer = findViewById(R.id.list_rsd);
        lvNonField = findViewById(R.id.list_nfd);
        lvDateWise = findViewById(R.id.list_daywise);

        tvNonField = findViewById(R.id.tvNonField);
        tvRetailerCount = findViewById(R.id.tv_rsd_count);
        mSearchRetailer = findViewById(R.id.searchRetailer);

        imBack = findViewById(R.id.img_back);
        imPrev = findViewById(R.id.img_prev);
        imNext = findViewById(R.id.img_next);

        tvMonthName = findViewById(R.id.tv_month);
        tvDMonthName = findViewById(R.id.tv_month_daywise);
        tvDayDate = findViewById(R.id.txt_dayDate);
        tvBackToMonthView = findViewById(R.id.txt_backToMonthView);

        layoutCalendar = findViewById(R.id.ll_calendar);
        layoutDayWise = findViewById(R.id.ll_daywise);

        mCalendar = findViewById(R.id.grid_calendar);
        final String GRIDLAYOUT_TAG = "GridLayout";
        mCalendar.setTag(GRIDLAYOUT_TAG);
        mCalendar.setOnDragListener(myDragEventListener);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.trans_left_in); // zoom_enter and trans_left_in looks good
        GridLayoutAnimationController controller = new GridLayoutAnimationController(animation, .2f, .2f);
        mCalendar.setLayoutAnimation(controller);

        mViewFlipper = findViewById(R.id.view_flipper);
        mViewFlipper.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.trans_right_in));
        mViewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.trans_left_out));

        tvRetailerCount.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        tvNonField.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        tvMonthName.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        tvDMonthName.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        tvDayDate.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        tvBackToMonthView.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
    }

    private void setValues() {

        if (bmodel.getRetailerMaster().size() > 0) {
            retailerList = bmodel.getRetailerMaster();

            if (bmodel.configurationMasterHelper.IS_LOAD_ONLY_SUBD) {
                retailerList.clear();
                retailerList.addAll(bmodel.getSubDMaster());
            }

            Collections.sort(retailerList, RetailerMasterBO.RetailerNameComparator);
            retailerAdapter = new RetailerAdapter(retailerList);
            lvRetailer.setAdapter(retailerAdapter);
        }

        if (bmodel.configurationMasterHelper.IS_LOAD_NON_FIELD) {
            nonFieldList = offlinePlanHelper.downLoadNonFieldList();
            if (nonFieldList.size() > 0) {
                nonFieldAdapter = new NonFieldAdapter(nonFieldList);
                lvNonField.setAdapter(nonFieldAdapter);
            }

        } else {
            lvNonField.setVisibility(View.GONE);
            tvNonField.setVisibility(View.GONE);
            LinearLayout.LayoutParams params = (LinearLayout.LayoutParams)
                    lvRetailer.getLayoutParams();
            params.weight = 2.0f;
            lvRetailer.setLayoutParams(params);
        }

        imBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mSearchRetailer.isIconified()) {
                    tvRetailerCount.setVisibility(View.VISIBLE);
                    mSearchRetailer.setIconified(true);
                    if (imm != null && imm.isActive())
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                } else {
                    startActivity(new Intent(OfflinePlanningActivity.this, HomeScreenActivity.class));
                    finish();
                }
            }
        });

        tvBackToMonthView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isMonthViewVisible) {
                    mViewFlipper.showPrevious();
                    isMonthViewVisible = true;
                }
            }
        });


        mSearchRetailer.onActionViewExpanded();
        mSearchRetailer.setIconified(true);
        mSearchRetailer.clearFocus();

        /* Code for changing the textColor and hint color for the search view */
        SearchView.SearchAutoComplete searchAutoComplete = mSearchRetailer.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchAutoComplete.setHintTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));
        searchAutoComplete.setTextColor(ResourcesCompat.getColor(getResources(), R.color.white, null));

        ImageView closeButton = mSearchRetailer.findViewById(R.id.search_close_btn);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                retailerAdapter = new RetailerAdapter(retailerList);
                lvRetailer.setAdapter(retailerAdapter);
                mSearchRetailer.setQuery("", false);
                mSearchRetailer.setIconified(true);
                imBack.setVisibility(View.VISIBLE);
                tvRetailerCount.setVisibility(View.VISIBLE);
                if (imm != null && imm.isActive())
                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        });

        mSearchRetailer.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                tvRetailerCount.setVisibility(View.GONE);
            }
        });

        mSearchRetailer.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                if (s.length() > 2) {
                    retailerAdapter.getFilter().filter(s);
                } else if (s.length() == 0) {
                    retailerAdapter = new RetailerAdapter(retailerList);
                    lvRetailer.setAdapter(retailerAdapter);
                    imBack.setVisibility(View.VISIBLE);
                    tvRetailerCount.setVisibility(View.VISIBLE);
                    // TVRetailerCount.setVisibility(View.GONE);
                    //  setRetailerHeader(filteredSubD.size());
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                imBack.setVisibility(View.GONE);
                if (s.length() > 2) {
                    retailerAdapter.getFilter().filter(s);
                } else if (s.length() == 0) {
                    retailerAdapter = new RetailerAdapter(retailerList);
                    lvRetailer.setAdapter(retailerAdapter);
                    imBack.setVisibility(View.VISIBLE);
                    tvRetailerCount.setVisibility(View.VISIBLE);

                }
                return false;
            }
        });

        lvDateWise.setOnDragListener(myDragEventListener);
        layoutDayWise.setVisibility(View.GONE);
        layoutCalendar.setVisibility(View.VISIBLE);

        lvRetailer.setOnDragListener(myDragEventListener);

        lvRetailer.setTag("listRsd");

        lvRetailer.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int position, long arg3) {
                isRetaieler = true;
                startDrag(v, position);
                return true;
            }

        });

        lvNonField.setOnDragListener(myDragEventListener);

        lvNonField.setTag("listRsd");
        lvNonField.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> arg0, View v,
                                           int position, long arg3) {
                isRetaieler = false;
                startDrag(v, position);
                return true;
            }

        });


        imNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, 1);
                Calendar toCalendar = Calendar.getInstance();
                try {
                    Date dateTo = dateFormatGeneral.parse(planToDate);
                    toCalendar.setTime(dateTo);
                    if (currentDate.get(Calendar.MONTH) <= toCalendar.get(Calendar.MONTH)) {
                        if (toCalendar.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR)) {
                            if (toCalendar.get(Calendar.MONTH) - currentDate.get(Calendar.MONTH) <= 0) {
                                updateCalendar();
                            } else {
                                currentDate.add(Calendar.MONTH, -1);
                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.endOfPeriod), Toast.LENGTH_SHORT).show();
                            }
                        } else
                            updateCalendar();
                    } else {
                        currentDate.add(Calendar.MONTH, -1);
                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.endOfPeriod), Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        imPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                Calendar fromCalendar = Calendar.getInstance();
                try {
                    Date dateTo = dateFormatGeneral.parse(planFromDate);
                    fromCalendar.setTime(dateTo);
                    if (currentDate.get(Calendar.MONTH) >= fromCalendar.get(Calendar.MONTH)) {
                        if (fromCalendar.get(Calendar.YEAR) != currentDate.get(Calendar.YEAR)) {
                            if (fromCalendar.get(Calendar.MONTH) - currentDate.get(Calendar.MONTH) >= 0)
                                updateCalendar();
                            else {
                                currentDate.add(Calendar.MONTH, +1);
                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.endOfPeriod), Toast.LENGTH_SHORT).show();
                            }
                        } else
                            updateCalendar();
                    } else {
                        currentDate.add(Calendar.MONTH, +1);
                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.endOfPeriod), Toast.LENGTH_SHORT).show();
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    public class LoadCalendar extends AsyncTask<String, Void, String> {

        private ProgressDialog progressDialogue;

        protected void onPreExecute() {
            progressDialogue = ProgressDialog.show(OfflinePlanningActivity.this,
                    DataMembers.SD, getResources().getString(R.string.loading),
                    true, false);
        }

        @Override
        protected String doInBackground(String... params) {
            try {

                offlinePlanHelper.downloadOfflinePlanList();
                mHashMapData = offlinePlanHelper.getmHashMapData();


            } catch (Exception e) {
                Commons.printException(e);
                return "Error";
            }
            return "";
        }

        @Override
        protected void onPostExecute(String s) {
            progressDialogue.dismiss();
            updateCalendar();

        }

    }

    class RetailerAdapter extends BaseAdapter implements Filterable {
        private Vector<RetailerMasterBO> items;
        private Vector<RetailerMasterBO> mFilteredList;
        private ValueFilter valueFilter;

        RetailerAdapter(Vector<RetailerMasterBO> items) {
            this.items = items;
            this.mFilteredList = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public RetailerMasterBO getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return SDUtil.convertToInt(items.get(i).getRetailerID());
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            final RsdHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(OfflinePlanningActivity.this);
                convertView = inflater.inflate(
                        R.layout.row_offline_list_layout, viewGroup, false);

                holder = new RsdHolder();
                holder.tvName = convertView.findViewById(R.id.tv_rsd);
                holder.tvAddress=convertView.findViewById(R.id.tv_address);
                holder.ivEntityType = convertView.findViewById(R.id.ivEntityType);

                holder.tvName.setTypeface(FontUtils.getFontRoboto(OfflinePlanningActivity.this, FontUtils.FontType.MEDIUM));
                //holder.tvAddress.setTypeface(FontUtils.getFontRoboto(OfflinePlanningActivity.this, FontUtils.FontType.LIGHT));
                holder.ivEntityType.setImageDrawable(getResources().getDrawable(R.drawable.ic_store_ofplan));

                convertView.setTag(holder);
            } else {
                holder = (RsdHolder) convertView.getTag();
            }

            holder.retailerMasterBO = items.get(position);
            holder.tvName.setText(holder.retailerMasterBO.getRetailerName());

            String address=(holder.retailerMasterBO.getAddress1()!=null?(holder.retailerMasterBO.getAddress1()):"");

            holder.tvAddress.setText(address);

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (valueFilter == null) {
                valueFilter = new ValueFilter();
            }
            return valueFilter;
        }


        private class ValueFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                FilterResults results = new FilterResults();
                if (charSequence != null && charSequence.length() > 0) {
                    Vector<RetailerMasterBO> mData = new Vector<>();
                    for (int i = 0; i < mFilteredList.size(); i++) {
                        if (mFilteredList.get(i).getRetailerName().toUpperCase().contains(charSequence.toString().toUpperCase())) {
                            mData.add(mFilteredList.get(i));
                        }
                    }
                    results.count = mData.size();
                    results.values = mData;
                } else {
                    results.count = mFilteredList.size();
                    results.values = mFilteredList;

                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                items = (Vector<RetailerMasterBO>) filterResults.values;
                notifyDataSetChanged();
            }
        }
    }


    class NonFieldAdapter extends ArrayAdapter<NonFieldBO> {
        private Vector<NonFieldBO> items;

        NonFieldAdapter(Vector<NonFieldBO> items) {
            super(OfflinePlanningActivity.this, R.layout.row_offline_list_layout);
            this.items = items;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public NonFieldBO getItem(int i) {
            return items.get(i);
        }

        @Override
        public long getItemId(int i) {
            return items.get(i).getReasonID();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup viewGroup) {
            final RsdHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(OfflinePlanningActivity.this);
                convertView = inflater.inflate(
                        R.layout.row_offline_list_layout, viewGroup, false);

                holder = new RsdHolder();
                holder.tvName = convertView.findViewById(R.id.tv_rsd);
                holder.ivEntityType = convertView.findViewById(R.id.ivEntityType);

                holder.tvName.setTypeface(FontUtils.getFontRoboto(OfflinePlanningActivity.this, FontUtils.FontType.MEDIUM));
                holder.ivEntityType.setImageDrawable(getResources().getDrawable(R.drawable.ic_non_field));
                convertView.setTag(holder);
            } else {
                holder = (RsdHolder) convertView.getTag();
            }

            holder.nonFieldBO = items.get(position);
            holder.tvName.setText(holder.nonFieldBO.getReason());
            return convertView;
        }
    }

    private void updateCalendar() {

        mCalenderAllList = new ArrayList<>();
        String[] calendarDate = getDateRange();
        dayInWeekCount = getWeekDayCount(calendarDate[0]);
        mCalenderAllList = getDaysBetweenDates(calendarDate[0], calendarDate[1]);
        mCampaginDates = getDateInBetween(planFromDate, planToDate);

        setMonthTV();

        Calendar mCal = (Calendar) currentDate.clone();
        int[] mToday = new int[3];
        mToday[0] = mCal.get(Calendar.DAY_OF_MONTH);
        mToday[1] = mCal.get(Calendar.MONTH); // zero based
        mToday[2] = mCal.get(Calendar.YEAR);

        loadCalendar();
    }

    private void setPlanDates() {

        Calendar aCalendar = Calendar.getInstance();
        aCalendar.add(Calendar.MONTH, -1);
        aCalendar.set(Calendar.DATE, 1);
        planFromDate = dateFormatGeneral.format(aCalendar.getTime());
        Commons.print("planFromDate" + planFromDate);

        Calendar zCalendar = Calendar.getInstance();
        zCalendar.add(Calendar.MONTH, 1);
        zCalendar.set(Calendar.DATE, zCalendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        planToDate = dateFormatGeneral.format(zCalendar.getTime());
        Commons.print("planFromDate" + planToDate);

    }

    private String[] getDateRange() {
        Date beginning, end;
        String[] dates = new String[5];

        {
            Calendar calendar = getCalendarForNow();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMinimum(Calendar.DAY_OF_MONTH));
            setTimeToBeginningOfDay(calendar);
            beginning = calendar.getTime();
            dates[0] = dateFormatGeneral.format(beginning);

        }

        {

            Calendar calendar = getCalendarForNow();
            calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
            setTimeToEndOfDay(calendar);
            end = calendar.getTime();
            dates[1] = dateFormatGeneral.format(end);
        }

        return dates;
    }


    private Calendar getCalendarForNow() {
        Calendar calendar = (Calendar) currentDate.clone();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return calendar;
    }

    private void setTimeToBeginningOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private void setTimeToEndOfDay(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }


    private int getWeekDayCount(String date) {
        int count;
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(dateFormatGeneral.parse(date));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        count = c.get(Calendar.DAY_OF_WEEK) - 1;
        if (count == 0) {
            count = 7;
        }
        return count;
    }

    private List<CalenderBO> getDaysBetweenDates(String startDate, String endDate) {
        List<CalenderBO> cal = new ArrayList<>();
        Date startdate = new Date();
        Date enddate;
        enddate = addDateNew(endDate);
        try {
            startdate = dateFormatGeneral.parse(startDate);
        } catch (ParseException e) {
            Commons.printException(e);
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startdate);

        CalenderBO cBO;
        while (calendar.getTime().before(enddate)) {
            cBO = new CalenderBO();
            Date result = calendar.getTime();
            SimpleDateFormat df1 = new SimpleDateFormat("dd", Locale.US);
            int d = SDUtil.convertToInt(df1.format(result));
            SimpleDateFormat outFormat = new SimpleDateFormat("EE", Locale.getDefault());
            String goal = outFormat.format(result);
            calendar.add(Calendar.DATE, 1);
            cBO.setCal_date(dateFormatGeneral.format(result));
            cBO.setDay(goal);
            cBO.setDate(d);
            cal.add(cBO);
        }
        return cal;
    }

    private Date addDateNew(String dateOne) {
        Date date1 = new Date();
        try {
            date1 = dateFormatGeneral.parse(dateOne);
        } catch (ParseException e) {
            Commons.printException(e);
        }
        System.out.println(dateFormatGeneral.format(date1));
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        cal.add(Calendar.DAY_OF_MONTH, 1); // add 28 days
        date1 = cal.getTime();
        System.out.println(dateFormatGeneral.format(date1));
        return date1;
    }

    private ArrayList<String> getDateInBetween(String startDate, String endDate) {

        ArrayList<String> date = new ArrayList<>();
        Calendar fromCal = Calendar.getInstance();
        Calendar toCal = Calendar.getInstance();
        try {
            fromCal.setTime(dateFormatGeneral.parse(startDate));
            toCal.setTime(dateFormatGeneral.parse(endDate));

        } catch (ParseException e) {
            Commons.printException(e);
        }
        while (!fromCal.after(toCal)) {
            date.add(dateFormatGeneral.format(fromCal.getTime()));
            fromCal.add(Calendar.DATE, 1);
        }
        return date;

    }

    private void setMonthTV() {
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM yyyy", Locale.US);
        tvMonthName.setText(sdf.format(currentDate.getTime()));
        tvDMonthName.setText(sdf.format(currentDate.getTime()));
    }

    private void loadCalendar() {
        final DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);

        madapter = new CalendarAdapter(this, metrics) {
            @Override
            protected void onDate(int[] date, int position, View item) {

            }
        };

        mCalendar.setAdapter(madapter);
        mCalendar.startLayoutAnimation();


    }


    public abstract class CalendarAdapter extends BaseAdapter {
        private final Context mContext;
        private final DisplayMetrics mDisplayMetrics;
        private List<String> mItems, mItems1, mItems2;
        private double mDayHeight;

        private CalendarAdapter(Context c,
                                DisplayMetrics metrics) {
            mContext = c;
            mDisplayMetrics = metrics;
            populateMonth();
        }

        /*
         * @param date     - null if day title (0 - dd / 1 - mm / 2 - yy)
         * @param position - position in item list
         * @param item     - view for calendarDate
         */
        protected abstract void onDate(int[] date, int position, View item);

        private void populateMonth() {
            mItems = new ArrayList<>();  // Day  - dd - Calendar Day
            mItems1 = new ArrayList<>(); // Date  - yyyy/MM/dd - Calendar Date
            mItems2 = new ArrayList<>(); // Date - yyyy/MM/dd - Campaign Date
            if (dayInWeekCount > 1) {
                for (int i = 1; i < dayInWeekCount; i++) {
                    mItems.add("No");
                    mItems1.add("No");
                    mItems2.add("No");
                }
            }

            for (int i = 0; i < mCalenderAllList.size(); i++) {
                mItems.add(String.valueOf(mCalenderAllList.get(i).getDate()));
                mItems1.add(String.valueOf(mCalenderAllList.get(i).getCal_date()));
            }

            mItems2.addAll(mCampaginDates);

            double rows = Math.ceil((double) mItems.size() / 7.0);
            int cellCount = (int) rows * 7;
            int loop = mItems.size();
            for (int i = 0; i < (cellCount - loop); i++) {
                mItems.add("No");
                mItems1.add("No");
                mItems2.add("No");
            }

            switch ((int) rows) {
                case 5:
                    containerHeight(5.75);
                    break;
                case 6:
                    containerHeight(7.10);
                    break;
                default:
                    containerHeight(6.0);
                    break;
            }
        }

        private int getBarHeight() {
            switch ((int) mDisplayMetrics.density) {
                case DisplayMetrics.DENSITY_HIGH:
                    return 48;
                case DisplayMetrics.DENSITY_MEDIUM:
                    return 32;
                case DisplayMetrics.DENSITY_LOW:
                    return 24;
                default:
                    return 24;
            }
        }

        private void containerHeight(double ratio) {
            DisplayMetrics dm = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(dm);
            mDayHeight = (mCalendar.getMeasuredHeight() / ratio) + getBarHeight();
        }

        private int[] getDateNew(String position) {
            boolean pass = true;
            SimpleDateFormat df2 = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            SimpleDateFormat df1 = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            Date d1;

            try {
                d1 = df2.parse(position);
                position = df1.format(d1);
            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            String[] strArray = position.split("/");
            int[] date = new int[strArray.length];
            for (int i = 0; i < strArray.length; i++) {
                switch (i) {
                    case 1:
                        int day = SDUtil.convertToInt(strArray[i]);
                        if (day > 0)
                            date[i] = SDUtil.convertToInt(strArray[i]) - 1;
                        else {
                            date[i] = SDUtil.convertToInt(strArray[i]) + 11;
                            pass = false;
                        }
                        break;
                    case 2:
                        if (pass)
                            date[i] = SDUtil.convertToInt(strArray[i]);
                        else {
                            date[i] = SDUtil.convertToInt(strArray[i]) - 1;
                        }
                        break;
                    default:
                        date[i] = SDUtil.convertToInt(strArray[i]);
                        break;
                }

            }

            return date;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final CalenderHolder holder;

            if (convertView == null) {

                holder = new CalenderHolder();
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.list_item_calender, parent, false);

                holder.TVDate = convertView.findViewById(R.id.tv_date);
                holder.TVCount = convertView.findViewById(R.id.tv_count);
                holder.plan_icon = convertView.findViewById(R.id.plan_icon);
                holder.ll_date = convertView.findViewById(R.id.ll_date);
                convertView.setOnDragListener(myDragEventListener);
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (imm != null && imm.isActive())
                            imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

                        if (holder.calBO != null) {
                            if (holder.isValid) {
                                try {
                                    String dateStr = holder.calBO.getCal_date();
                                    dayWishSelectedDate = dateStr;
                                    ArrayList<OfflineDateWisePlanBO> mDayWiseList = new ArrayList<>();
                                    if (mHashMapData.get(dateStr) != null)
                                        mDayWiseList = mHashMapData.get(dateStr);

                                    int size = mDayWiseList.size();

                                    if (size != 0 || dateFormatGeneral.parse(holder.calBO.getCal_date()).after(new Date())) {
                                        SimpleDateFormat format1 = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
                                        Date dt1 = format1.parse(dateStr);
                                        DateFormat format2 = new SimpleDateFormat("EEEE", Locale.US);
                                        String dayOfTheWeek = format2.format(dt1);

                                        tvDayDate.setText(String.format("%s - %s", mItems.get(position), dayOfTheWeek));

                                        mViewFlipper.showNext();

                                        setDateWiseAdapter(mDayWiseList, dateStr);
                                        isMonthViewVisible = false;
                                    } else {
                                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.noPlan), Toast.LENGTH_LONG).show();
                                    }
                                } catch (ParseException e) {
                                    Commons.printException(e);
                                }

                            } else {
                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.unplannedDay), Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                });

                LinearLayout.LayoutParams rel_btn = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) mDayHeight);
                holder.ll_date.setLayoutParams(rel_btn);
                convertView.setTag(holder);
            } else {
                holder = (CalenderHolder) convertView.getTag();
            }

            if (mItems.get(position).equals("No")) {
                holder.TVDate.setText("");
                holder.TVCount.setVisibility(View.INVISIBLE);
                holder.plan_icon.setVisibility(View.INVISIBLE);
                holder.isDataPresent = false;
            } else {
                holder.TVDate.setText(mItems.get(position));
                holder.isDataPresent = true;
                if (mItems2.contains(mItems1.get(position))) {
                    holder.TVDate.setTextColor(getResources().getColor(R.color.FullBlack));
                    holder.isValid = true;

                } else {
                    holder.TVDate.setTextColor(getResources().getColor(R.color.light_gray));
                    holder.isValid = false;
                }
                try {
                    if (dateFormatGeneral.parse(mItems1.get(position)).before(new Date())) {
                        holder.TVDate.setTextColor(getResources().getColor(R.color.FullBlack));
                        holder.TVDate.setTypeface(null, Typeface.NORMAL);
                    }
                } catch (ParseException e) {
                    Commons.printException(e);
                }


                if (mHashMapData != null && mHashMapData.get(mItems1.get(position)) != null && mHashMapData.get(mItems1.get(position)).size() > 0) {
                    holder.TVCount.setText(String.valueOf(mHashMapData.get(mItems1.get(position)).size()));
                    holder.TVCount.setVisibility(View.VISIBLE);
                    holder.plan_icon.setVisibility(View.VISIBLE);
                } else {
                    holder.TVCount.setVisibility(View.INVISIBLE);
                    holder.plan_icon.setVisibility(View.INVISIBLE);
                }
            }


            int[] date = {};
            if (!mItems1.get(position).equals("No")) {
                date = getDateNew(mItems1.get(position));
            }
            if (date != null && date.length > 0) {

                for (int i = 0; i < mCalenderAllList.size(); i++) {
                    if ((mCalenderAllList.get(i).getDate() + "").equals(mItems.get(position)) && (mCalenderAllList.get(i).getCal_date() + "").equals(mItems1.get(position))) {
                        holder.calBO = mCalenderAllList.get(i);
                    }
                }
            }

            if ((hoverDate.equals(mItems1.get(position)))) {
                holder.ll_date.setBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.divider_view_color, null));
            } else {
                holder.ll_date.setBackgroundColor(Color.WHITE);
            }
            return convertView;
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }
    }

    class CalenderHolder {
        CalenderBO calBO;
        TextView TVDate, TVCount;
        RelativeLayout ll_date;
        ImageView plan_icon;
        Boolean isValid = false;
        Boolean isDataPresent = false;
    }

    private void setDateWiseAdapter(ArrayList<OfflineDateWisePlanBO> mDayWiseList, String dateStr) {
        dayWishPlanningAdapter = new DayWishPlanningAdapter(mDayWiseList, dateStr);
        lvDateWise.setAdapter(dayWishPlanningAdapter);
    }

    class MyDragEventListener implements View.OnDragListener {

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int action = event.getAction();

            switch (action) {

                case DragEvent.ACTION_DRAG_STARTED:
                    if (imm != null && imm.isActive())
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_ENTERED:
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_LOCATION:
                    try {
                        if (isMonthViewVisible) {

                            CalenderHolder holder = (CalenderHolder) v.getTag();
                            if (holder != null) {
                                if (holder.isDataPresent) {
                                    hoverDate = holder.calBO.getCal_date();
                                    if (madapter != null) {
                                        madapter.notifyDataSetChanged();
                                    }
                                } else {
                                    hoverDate = "";
                                    madapter.notifyDataSetChanged();
                                }
                            }
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                    v.invalidate();
                    return true;
                case DragEvent.ACTION_DRAG_EXITED:
                    v.invalidate();
                    return true;

                case DragEvent.ACTION_DROP:
                    try {
                        if (mHashMapData == null) {
                            mHashMapData = offlinePlanHelper.getmHashMapData();
                        }
                        if (isMonthViewVisible) {

                            CalenderHolder holder = (CalenderHolder) v.getTag();
                            if (holder != null) {
                                if ((!dateFormatGeneral.parse(holder.calBO.getCal_date()).before(new Date())) &&
                                        (!dateFormatGeneral.parse(holder.calBO.getCal_date()).equals(new Date()))) {

                                    if (holder.isValid) {

                                        OfflineDateWisePlanBO rsd = new OfflineDateWisePlanBO();
                                        if (isRetaieler) {
                                            rsd.setEntityId(SDUtil.convertToInt(mRsdholder.retailerMasterBO.getRetailerID()));
                                            rsd.setName(mRsdholder.retailerMasterBO.getRetailerName());
                                        } else {
                                            rsd.setEntityId(mRsdholder.nonFieldBO.getReasonID());
                                            rsd.setName(mRsdholder.nonFieldBO.getReason());
                                        }

                                        String dateStr = holder.calBO.getCal_date();
                                        ArrayList<OfflineDateWisePlanBO> mData;

                                        if (mHashMapData.get(dateStr) != null) {
                                            mData = mHashMapData.get(dateStr);
                                            if (isRetaieler) {
                                                boolean isAvailable = false;
                                                boolean isRoutePlanned = false;
                                                for (OfflineDateWisePlanBO ofBo : mData) {
                                                    if (ofBo.getEntityType().equals(mEntityRetailer)) {
                                                        if (ofBo.getEntityId() == SDUtil.convertToInt(mRsdholder.retailerMasterBO.getRetailerID())) {
                                                            isAvailable = true;
                                                        }
                                                    }

                                                    if (ofBo.getEntityType().equals(mEntityDistributor)) {
                                                        if (ofBo.getEntityId() == mRsdholder.retailerMasterBO.getSubdId()) {
                                                            isAvailable = true;
                                                        }
                                                    }
                                                    if (ofBo.getEntityType().equals(mEntityNFA)) {
                                                        isRoutePlanned = true;

                                                    }
                                                }

                                                if (bmodel.configurationMasterHelper.IS_PLAN_RETIALER_NON_FIELD) {
                                                    if (!isAvailable) {
                                                        addPlan(dateStr, mRsdholder.retailerMasterBO);
                                                    } else {
                                                        hoverDate = "";
                                                        madapter.notifyDataSetChanged();
                                                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.retailerExists), Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }
                                                } else if (isRoutePlanned) {
                                                    hoverDate = "";
                                                    madapter.notifyDataSetChanged();
                                                    Toast.makeText(OfflinePlanningActivity.this, getString(R.string.notAllowtoPlanOutlet), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                } else {
                                                    if (!isAvailable) {
                                                        addPlan(dateStr, mRsdholder.retailerMasterBO);
                                                    } else {
                                                        hoverDate = "";
                                                        madapter.notifyDataSetChanged();
                                                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.retailerExists), Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }
                                                }
                                            } else {
                                                boolean isAvailable = false;
                                                boolean isRetailerPlanned = false;
                                                for (OfflineDateWisePlanBO ofBo : mData) {
                                                    if (ofBo.getEntityType().equals(mEntityNFA)) {
                                                        if (ofBo.getEntityId() == mRsdholder.nonFieldBO.getReasonID()) {
                                                            isAvailable = true;
                                                        }
                                                    }
                                                    if (ofBo.getEntityType().equals(mEntityRetailer) || ofBo.getEntityType().equals(mEntityDistributor)) {
                                                        isRetailerPlanned = true;
                                                    }

                                                }
                                                if (bmodel.configurationMasterHelper.IS_PLAN_RETIALER_NON_FIELD) {
                                                    if (!isAvailable) {
                                                        addPlan(dateStr, mRsdholder.nonFieldBO);
                                                    } else {
                                                        hoverDate = "";
                                                        madapter.notifyDataSetChanged();
                                                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.nonFieldExists), Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }
                                                } else if (isRetailerPlanned) {
                                                    hoverDate = "";
                                                    madapter.notifyDataSetChanged();
                                                    Toast.makeText(OfflinePlanningActivity.this, getString(R.string.notAllowtoPlannonField), Toast.LENGTH_SHORT).show();
                                                    return false;
                                                } else {
                                                    if (!isAvailable) {
                                                        addPlan(dateStr, mRsdholder.nonFieldBO);
                                                    } else {
                                                        hoverDate = "";
                                                        madapter.notifyDataSetChanged();
                                                        Toast.makeText(OfflinePlanningActivity.this, getString(R.string.nonFieldExists), Toast.LENGTH_SHORT).show();
                                                        return false;
                                                    }
                                                }
                                            }
                                        } else {
                                            if (isRetaieler)
                                                addPlan(dateStr, mRsdholder.retailerMasterBO);
                                            else
                                                addPlan(dateStr, mRsdholder.nonFieldBO);
                                        }

                                        setMonthTV();
                                        if (isRetaieler)
                                            retailerAdapter.notifyDataSetChanged();
                                        else
                                            nonFieldAdapter.notifyDataSetChanged();
                                        madapter.notifyDataSetChanged();

                                        return true;
                                    } else {
                                        return false;
                                    }
                                } else {
                                    hoverDate = "";
                                    madapter.notifyDataSetChanged();
                                    Toast.makeText(getApplicationContext(), getString(R.string.pastAndCurrentDays), Toast.LENGTH_LONG).show();
                                    return false;
                                }
                            }
                        } else {
                            if ((!dateFormatGeneral.parse(dayWishSelectedDate).before(new Date())) &&
                                    (!dateFormatGeneral.parse(dayWishSelectedDate).equals(new Date()))) {
                                OfflineDateWisePlanBO rsd = new OfflineDateWisePlanBO();
                                if (isRetaieler) {
                                    rsd.setEntityId(SDUtil.convertToInt(mRsdholder.retailerMasterBO.getRetailerID()));
                                    rsd.setName(mRsdholder.retailerMasterBO.getRetailerName());
                                } else {
                                    rsd.setEntityId(mRsdholder.nonFieldBO.getReasonID());
                                    rsd.setName(mRsdholder.nonFieldBO.getReason());
                                }
                                String dateStr = dayWishSelectedDate;

                                ArrayList<OfflineDateWisePlanBO> mData = new ArrayList<>();

                                if (mHashMapData.get(dateStr) != null) {
                                    mData = mHashMapData.get(dateStr);
                                    if (isRetaieler) {
                                        boolean isAvailable = false;
                                        boolean isRoutePlanned = false;
                                        for (OfflineDateWisePlanBO ofBo : mData) {
                                            if (ofBo.getEntityType().equals(mEntityRetailer)) {
                                                if (ofBo.getEntityId() == SDUtil.convertToInt(mRsdholder.retailerMasterBO.getRetailerID())) {
                                                    isAvailable = true;
                                                }
                                            }
                                            if (ofBo.getEntityType().equals(mEntityDistributor)) {
                                                if (ofBo.getEntityId() == mRsdholder.retailerMasterBO.getSubdId()) {
                                                    isAvailable = true;
                                                }
                                            }
                                            if (ofBo.getEntityType().equals(mEntityNFA)) {
                                                isRoutePlanned = true;
                                            }
                                        }

                                        if (bmodel.configurationMasterHelper.IS_PLAN_RETIALER_NON_FIELD) {
                                            if (!isAvailable) {
                                                addPlan(dateStr, mRsdholder.retailerMasterBO);
                                            } else {
                                                hoverDate = "";
                                                madapter.notifyDataSetChanged();
                                                setDateWiseAdapter(mData, dateStr);
                                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.retailerExists), Toast.LENGTH_SHORT).show();
                                                return false;
                                            }
                                        } else if (isRoutePlanned) {
                                            hoverDate = "";
                                            madapter.notifyDataSetChanged();
                                            setDateWiseAdapter(mData, dateStr);
                                            Toast.makeText(OfflinePlanningActivity.this, getString(R.string.notAllowtoPlanOutlet), Toast.LENGTH_SHORT).show();
                                            return false;
                                        } else {
                                            if (!isAvailable) {
                                                addPlan(dateStr, mRsdholder.retailerMasterBO);
                                            } else {
                                                hoverDate = "";
                                                madapter.notifyDataSetChanged();
                                                setDateWiseAdapter(mData, dateStr);
                                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.retailerExists), Toast.LENGTH_SHORT).show();
                                                return false;
                                            }
                                        }
                                    } else {
                                        boolean isAvailable = false;
                                        boolean isRetailerPlanned = false;
                                        for (OfflineDateWisePlanBO ofBo : mData) {
                                            if (ofBo.getEntityType().equals(mEntityNFA)) {
                                                if (ofBo.getEntityId() == mRsdholder.nonFieldBO.getReasonID()) {
                                                    isAvailable = true;
                                                }
                                            }
                                            if (ofBo.getEntityType().equals(mEntityRetailer) || ofBo.getEntityType().equals(mEntityDistributor)) {
                                                isRetailerPlanned = true;
                                            }

                                        }
                                        if (bmodel.configurationMasterHelper.IS_PLAN_RETIALER_NON_FIELD) {
                                            if (!isAvailable) {
                                                addPlan(dateStr, mRsdholder.nonFieldBO);
                                            } else {
                                                hoverDate = "";
                                                madapter.notifyDataSetChanged();
                                                setDateWiseAdapter(mData, dateStr);
                                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.nonFieldExists), Toast.LENGTH_SHORT).show();
                                                return false;
                                            }
                                        } else if (isRetailerPlanned) {
                                            hoverDate = "";
                                            madapter.notifyDataSetChanged();
                                            setDateWiseAdapter(mData, dateStr);
                                            Toast.makeText(OfflinePlanningActivity.this, getString(R.string.notAllowtoPlannonField), Toast.LENGTH_SHORT).show();
                                            return false;
                                        } else {
                                            if (!isAvailable) {
                                                addPlan(dateStr, mRsdholder.nonFieldBO);
                                            } else {
                                                hoverDate = "";
                                                madapter.notifyDataSetChanged();
                                                setDateWiseAdapter(mData, dateStr);
                                                Toast.makeText(OfflinePlanningActivity.this, getString(R.string.nonFieldExists), Toast.LENGTH_SHORT).show();
                                                return false;
                                            }
                                        }
                                    }
                                } else {
                                    if (isRetaieler)
                                        addPlan(dateStr, mRsdholder.retailerMasterBO);
                                    else
                                        addPlan(dateStr, mRsdholder.nonFieldBO);
                                }

                                setMonthTV();
                                if (isRetaieler)
                                    retailerAdapter.notifyDataSetChanged();
                                else
                                    nonFieldAdapter.notifyDataSetChanged();
                                madapter.notifyDataSetChanged();
                                setDateWiseAdapter(mHashMapData.get(dateStr), dateStr);
                                return true;
                            } else {
                                Toast.makeText(getApplicationContext(), getString(R.string.pastAndCurrentDays), Toast.LENGTH_LONG).show();
                                return false;
                            }
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                        hoverDate = "";
                        madapter.notifyDataSetChanged();
                        return false;
                    }

                default: // unknown case
                    hoverDate = "";
                    madapter.notifyDataSetChanged();
                    return false;

            }

        }
    }

    private void addPlan(String date, RetailerMasterBO retailerMasterBO) {
        OfflineDateWisePlanBO offlineDateWisePlanBO = new OfflineDateWisePlanBO();

        offlineDateWisePlanBO.setPlanId(0);
        offlineDateWisePlanBO.setDate(date);
        offlineDateWisePlanBO.setDistributorId(retailerMasterBO.getDistributorId());
        offlineDateWisePlanBO.setUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());

        if (retailerMasterBO.getSubdId() == 0) {
            offlineDateWisePlanBO.setEntityType(mEntityRetailer);
            offlineDateWisePlanBO.setEntityId(SDUtil.convertToInt(retailerMasterBO.getRetailerID()));
        } else {
            offlineDateWisePlanBO.setEntityType(mEntityDistributor);
            offlineDateWisePlanBO.setEntityId(retailerMasterBO.getSubdId());
        }
        offlineDateWisePlanBO.setStatus("I");
        offlineDateWisePlanBO.setSequence(0);
        offlineDateWisePlanBO.setName(retailerMasterBO.getRetailerName());
        ArrayList<OfflineDateWisePlanBO> mData = new ArrayList<>();
        if (mHashMapData != null) {
            if (mHashMapData.get(date) != null) {
                mData = mHashMapData.get(date);
                if (!mData.contains(offlineDateWisePlanBO)) {
                    mData.add(offlineDateWisePlanBO);

                } else {
                    mData.remove(offlineDateWisePlanBO);
                    mData.add(offlineDateWisePlanBO);
                }
            } else {
                mData.add(offlineDateWisePlanBO);
            }
        } else {
            mHashMapData = new HashMap<>();
            mData.add(offlineDateWisePlanBO);
        }
        mHashMapData.put(date, mData);
        offlinePlanHelper.setmHashMapData(mHashMapData);
        offlinePlanHelper.savePlan(this, offlineDateWisePlanBO);

    }

    private void addPlan(String date, NonFieldBO nonFieldBO) {
        OfflineDateWisePlanBO offlineDateWisePlanBO = new OfflineDateWisePlanBO();

        offlineDateWisePlanBO.setPlanId(0);
        offlineDateWisePlanBO.setDate(date);
        offlineDateWisePlanBO.setDistributorId(0);
        offlineDateWisePlanBO.setUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
        offlineDateWisePlanBO.setEntityId(nonFieldBO.getReasonID());
        offlineDateWisePlanBO.setEntityType(mEntityNFA);
        offlineDateWisePlanBO.setStatus("I");
        offlineDateWisePlanBO.setSequence(0);
        offlineDateWisePlanBO.setName(nonFieldBO.getReason());
        ArrayList<OfflineDateWisePlanBO> mData = new ArrayList<>();

        if (mHashMapData != null) {
            if (mHashMapData.get(date) != null) {
                mData = mHashMapData.get(date);
                if (!mData.contains(offlineDateWisePlanBO)) {
                    mData.add(offlineDateWisePlanBO);

                } else {
                    mData.remove(offlineDateWisePlanBO);
                    mData.add(offlineDateWisePlanBO);
                }
            } else {
                mData.add(offlineDateWisePlanBO);
            }
        } else {
            mHashMapData = new HashMap<>();
            mData.add(offlineDateWisePlanBO);
        }
        mHashMapData.put(date, mData);
        offlinePlanHelper.setmHashMapData(mHashMapData);
        offlinePlanHelper.savePlan(this, offlineDateWisePlanBO);


    }

    class DayWishPlanningAdapter extends BaseAdapter {

        private final ArrayList<OfflineDateWisePlanBO> mDataList;
        private String selectedDate;

        private DayWishPlanningAdapter(ArrayList<OfflineDateWisePlanBO> mDataList, String selectedDate) {
            this.mDataList = mDataList;
            this.selectedDate = selectedDate;
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDataList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final MyViewHolder holder;
            if (convertView == null) {
                holder = new MyViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getBaseContext());
                convertView = inflater.inflate(R.layout.list_item_daywise_planning, parent, false);

                holder.TVRetailerName = convertView.findViewById(R.id.txt_retailerName);
                holder.IVDelete = convertView.findViewById(R.id.iv_delete);
                holder.IvEntityType = convertView.findViewById(R.id.ivEntityType);


                holder.IVDelete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(OfflinePlanningActivity.this);
                        builder.setMessage(getResources().getString(R.string.do_you_want_delete_plan));
                        builder.setCancelable(false);
                        builder.setPositiveButton(getResources().getString(R.string.ok),
                                new android.content.DialogInterface.OnClickListener() {

                                    public void onClick(DialogInterface dialog, int which) {
                                        ArrayList<OfflineDateWisePlanBO> mData;
                                        mHashMapData = offlinePlanHelper.getmHashMapData();
                                        OfflineDateWisePlanBO offlineDateWisePlanBO = holder.dayWiseRetailer;
                                        offlineDateWisePlanBO.setStatus("D");

                                        if (mHashMapData != null) {
                                            if (mHashMapData.get(selectedDate) != null) {
                                                mData = mHashMapData.get(selectedDate);
                                                if (mData.contains(offlineDateWisePlanBO)) {
                                                    mData.remove(offlineDateWisePlanBO);
                                                    mHashMapData.put(selectedDate, mData);
                                                    offlinePlanHelper.setmHashMapData(mHashMapData);
                                                    offlinePlanHelper.updatePlan(OfflinePlanningActivity.this, offlineDateWisePlanBO);
                                                    //mDataList.remove(position);
                                                    dayWishPlanningAdapter.notifyDataSetChanged();
                                                    madapter.notifyDataSetChanged();
                                                    Toast.makeText(OfflinePlanningActivity.this, getString(R.string.plan_deleted), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        } else {
                                            mHashMapData = new HashMap<>();
                                        }
                                        retailerAdapter.notifyDataSetChanged();
                                    }

                                });
                        builder.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                            }
                        });
                        bmodel.applyAlertDialogTheme(builder);
                    }
                });

                convertView.setOnDragListener(myDragEventListener);
                convertView.setTag(holder);
            } else {
                holder = (MyViewHolder) convertView.getTag();
            }
            holder.dayWiseRetailer = mDataList.get(position);
            holder.TVRetailerName.setText(holder.dayWiseRetailer.getName());

            if (holder.dayWiseRetailer.getEntityType().equalsIgnoreCase(mEntityDistributor) ||
                    holder.dayWiseRetailer.getEntityType().equalsIgnoreCase(mEntityRetailer))
                holder.IvEntityType.setImageDrawable(getResources().getDrawable(R.drawable.ic_store_ofplan));

            else if (holder.dayWiseRetailer.getEntityType().equalsIgnoreCase(mEntityNFA))
                holder.IvEntityType.setImageDrawable(getResources().getDrawable(R.drawable.ic_non_field));

            try {
                if ((dateFormatGeneral.parse(selectedDate).before(new Date())) ||
                        (dateFormatGeneral.parse(selectedDate).equals(new Date()))) {
                    holder.IVDelete.setVisibility(View.GONE);
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }


            return convertView;
        }

    }

    // dayWise List Holder
    class MyViewHolder {
        TextView TVRetailerName;
        ImageView IVDelete, IvEntityType;
        OfflineDateWisePlanBO dayWiseRetailer;
    }

    private void startDrag(View v, int position) {
        ClipData.Item item;

        if (isRetaieler)
            item = new ClipData.Item(retailerList.get(position)
                    .toString());
        else
            item = new ClipData.Item(nonFieldList.get(position)
                    .toString());


        mRsdholder = (RsdHolder) v.getTag();

        String[] clipDescription = {ClipDescription.MIMETYPE_TEXT_PLAIN};
        ClipData dragData;
        if (isRetaieler)
            dragData = new ClipData(mRsdholder.retailerMasterBO.getRetailerName(), clipDescription, item);
        else
            dragData = new ClipData(mRsdholder.nonFieldBO.getReason(), clipDescription, item);

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        View vg = inflater.inflate(R.layout.drag_pjp_rsd_view, null);
        TextView tv = vg.findViewById(R.id.tv_rsd);
        if (isRetaieler)
            tv.setText(mRsdholder.retailerMasterBO.getRetailerName());
        else
            tv.setText(mRsdholder.nonFieldBO.getReason());

        vg.refreshDrawableState();

        View.DragShadowBuilder myShadow = new View.DragShadowBuilder(v);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (isRetaieler)
                v.startDragAndDrop(dragData, // ClipData
                        myShadow, // View.DragShadowBuilder
                        retailerList.get(position).toString(), // Object myLocalState
                        0); // flags
            else
                v.startDragAndDrop(dragData, // ClipData
                        myShadow, // View.DragShadowBuilder
                        nonFieldList.get(position).toString(), // Object myLocalState
                        0); // flags
        } else {
            if (isRetaieler)
                v.startDrag(dragData, // ClipData
                        myShadow, // View.DragShadowBuilder
                        retailerList.get(position).toString(), // Object myLocalState
                        0); // flags
            else
                v.startDrag(dragData, // ClipData
                        myShadow, // View.DragShadowBuilder
                        nonFieldList.get(position).toString(), // Object myLocalState
                        0); // flags
        }

    }

    public class RsdHolder {
        RetailerMasterBO retailerMasterBO;
        NonFieldBO nonFieldBO;
        TextView tvName,tvAddress;
        ImageView ivEntityType;
    }


}
