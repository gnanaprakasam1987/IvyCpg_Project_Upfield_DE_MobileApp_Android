package com.ivy.countersales;

import android.app.Dialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.profile.HorizontalDividerItemDecoration;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

/**
 * Created by rajkumar.s on 29-03-2016.
 */
public class CShistoryDialog extends Dialog implements View.OnClickListener {
    ScrollView mscrollview;
    HashMap<String, JSONObject> lstHistory;
    Context context;
    BusinessModel bmodel;
    static String mSaleDetail = "CS_CustomerVisited_SaleDetails";
    static String mSampleDetail = "CS_CustomerVisited_SampleDetails";
    static String mConcernDetail = "CS_CustomerVisited_ConcernDetails";
    static String mTrialDetail = "CS_CustomerVisited_TrailDetails";
    static String mSurveyDetail = "CS_CustomerVisited_SurveyDetails";
    static String mCustomerVisited = "CS_CustomerVisited";
    static String uomcode_case = "CASE";
    static String uomcode_piece = "PIECE";
    static String uomcode_outer = "OUTER";
    Toolbar toolbar;
    public GridLayoutManager gridlaymanager;
    Button btnClose;
    private TabLayout tabLayout;
    private String mSelectedTab;

    HashMap<String, String> mLstHeader;
    ArrayList<HashMap<String, String>> lstTestData;
    private boolean isFromReport = false;

    public CShistoryDialog(final Context ctx, HashMap<String, String> lstHeader, BusinessModel businessModel,boolean isFrmReport) {
        super(ctx);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.dialog_customer_visit_history);

        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle(R.string.cust_history);
        toolbar.setTitleTextColor(Color.WHITE);

        btnClose = (Button) findViewById(R.id.btn_close);
        btnClose.setOnClickListener(this);
        tabLayout = (TabLayout) findViewById(R.id.tab_layout);

        // lstHistory = lstItems;
        mLstHeader = lstHeader;

        isFromReport = isFrmReport;

        context = ctx;
        bmodel = businessModel;


        prepareTabs();
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    mSelectedTab = (String) tab.getTag();
                    if ((context.getResources().getString(R.string.common)).equals(mSelectedTab))
                        prepareCommonView();
                    else
                        prepareViewForHistory(mSelectedTab);
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

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_close) {
            bmodel.mCounterSalesHelper.setmHeaderLst(mLstHeader);
            bmodel.mCounterSalesHelper.deleteSearchRecords();
            dismiss();
        }

    }

    private void prepareTabs() {

        if (mLstHeader != null) {
            lstTestData = bmodel.mCounterSalesHelper.downloadCustomerTestInformation(mLstHeader.get("uid"));
        }

        float scale = context.getResources().getDisplayMetrics().widthPixels;
        if (bmodel.mCounterSalesHelper.getmNumberOfTabs() == 5)
            scale = scale / (bmodel.mCounterSalesHelper.getmNumberOfTabs() - 2);
        else if (bmodel.mCounterSalesHelper.getmNumberOfTabs() == 4)
            scale = scale / (bmodel.mCounterSalesHelper.getmNumberOfTabs() - 1);
        else
            scale = scale / bmodel.mCounterSalesHelper.getmNumberOfTabs();

        TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);
        boolean isAdded = false;

// this method for to set tab order sequence


        if (mLstHeader != null) {

            TabLayout.Tab tab = tabLayout.newTab();
            TextView txtVw = new TextView(context);
            txtVw.setGravity(Gravity.CENTER);
            txtVw.setWidth((int) scale);
            txtVw.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            txtVw.setTextColor(color);
            txtVw.setMaxLines(1);
            txtVw.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_small));
            txtVw.setAllCaps(true);

            txtVw.setText(R.string.cust_visit_title);
            tab.setTag(mCustomerVisited);
            tab.setCustomView(txtVw);
            tabLayout.addTab(tab);

        }

        if (mLstHeader != null && lstTestData != null && lstTestData.size() > 0) {

            TabLayout.Tab tab = tabLayout.newTab();
            TextView txtVw = new TextView(context);
            txtVw.setGravity(Gravity.CENTER);
            txtVw.setWidth((int) scale);
            txtVw.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            txtVw.setTextColor(color);
            txtVw.setMaxLines(1);
            txtVw.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_small));
            txtVw.setAllCaps(true);

            txtVw.setText(context.getResources().getString(R.string.apply_or_test_feedback));
            tab.setTag(mTrialDetail);
            tab.setCustomView(txtVw);
            tabLayout.addTab(tab);

        }

       /* if (lstHistory.get(mSurveyDetail) != null) {
            TabLayout.Tab tab = tabLayout.newTab();
            TextView txtVw = new TextView(context);
            txtVw.setGravity(Gravity.CENTER);
            txtVw.setWidth((int) scale);
            txtVw.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            txtVw.setTextColor(color);
            txtVw.setMaxLines(1);
            txtVw.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_small));
            txtVw.setAllCaps(true);

            txtVw.setText(R.string.cust_survey_title);
            tab.setTag(mSurveyDetail);
            tab.setCustomView(txtVw);
            tabLayout.addTab(tab);
        }
*/
        changeTabsFont();
        mSelectedTab = (String) tabLayout.getTabAt(0).getTag();
        if ((context.getResources().getString(R.string.common)).equals(mSelectedTab))
            prepareCommonView();
        else
            prepareViewForHistory(mSelectedTab);
    }

    private void prepareCommonView() {
      /*  mscrollview = (ScrollView) findViewById(R.id.scrollview);
        mscrollview.removeAllViews();
        LinearLayout.LayoutParams weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.gravity = Gravity.CENTER;

        LinearLayout totalView = new LinearLayout(context);
        totalView.setOrientation(LinearLayout.VERTICAL);

        try {
            for (String table : lstHistory.keySet()) {
                TextView title = new TextView(context);
                title.setPadding(0, 10, 0, 0);
                title.setPaintFlags(title.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
                title.setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getResources().getDimension(R.dimen.font_large));

                JSONArray jsonArray = lstHistory.get(table).getJSONArray(bmodel.synchronizationHelper.JSON_FIELD_KEY);

                LinearLayout headerView = new LinearLayout(context);
                headerView.setOrientation(LinearLayout.HORIZONTAL);
                if (table.equals(mSampleDetail)) {

                    LinearLayout rowView = new LinearLayout(context);


                    JSONArray values = lstHistory.get(table).getJSONArray(bmodel.synchronizationHelper.JSON_DATA_KEY);

                    if (values != null && values.length() > 0) {
                        title.setText(R.string.cust_sample_title);
                        totalView.addView(title, weight1);

                        View rowHeader = getLayoutInflater().inflate(R.layout.cs_history_product_header,
                                null, false);
                        rowHeader.findViewById(R.id.priceTitle).setVisibility(View.GONE);
                        rowHeader.findViewById(R.id.valueTitle).setVisibility(View.GONE);
                        totalView.addView(rowHeader);

                        rowView = (LinearLayout) rowHeader.findViewById(R.id.ll_content);
                    }
                    for (int j = 0; j < values.length(); j++) {
                        JSONArray recordList = (JSONArray) values.get(j);

                        View rowDetail = getLayoutInflater().inflate(R.layout.cs_history_product_detail_view,
                                null, false);

                        TextView pname = (TextView) rowDetail.findViewById(R.id.productname);
                        TextView pcs_qty = (TextView) rowDetail.findViewById(R.id.pcs_qty);
                        TextView case_qty = (TextView) rowDetail.findViewById(R.id.case_qty);
                        TextView outer_qty = (TextView) rowDetail.findViewById(R.id.outer_qty);
                        rowDetail.findViewById(R.id.txt_price).setVisibility(View.GONE);
                        rowDetail.findViewById(R.id.txt_value).setVisibility(View.GONE);

                        pname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                        pcs_qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        case_qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        outer_qty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                        pname.setText(recordList.getString(0) + "");
                        if (recordList.getString(1).equals(uomcode_case)) {
                            case_qty.setText(recordList.getString(2));
                        } else if (recordList.getString(1).equals(uomcode_piece)) {
                            pcs_qty.setText(recordList.getString(2));
                        } else if (recordList.getString(1).equals(uomcode_outer)) {
                            outer_qty.setText(recordList.getString(2));
                        }

                        rowView.addView(rowDetail);

                    }
                } else if (table.equals(mConcernDetail) || table.equals(mTrialDetail)) {
                    ArrayList<NewOutletBO> list = new ArrayList<>();
                    RecyclerView recyclerView = new RecyclerView(context);

                    JSONArray values = lstHistory.get(table).getJSONArray(bmodel.synchronizationHelper.JSON_DATA_KEY);
                    if (values != null && values.length() > 0) {
                        if (table.equals(mConcernDetail))
                            title.setText(R.string.cust_concern_title);
                        else if (table.equals(mTrialDetail))
                            title.setText(R.string.cust_trial_title);

                        totalView.addView(title, weight1);

                        list = new ArrayList<>();

                        View rowHeader = getLayoutInflater().inflate(R.layout.cs_history_visit_details,
                                null, false);
                        recyclerView = (RecyclerView) rowHeader.findViewById(R.id.recyclerview);
                        recyclerView.setNestedScrollingEnabled(false);
                        recyclerView.setHasFixedSize(true);
                        totalView.addView(rowHeader);

                        int sizeLarge = SCREENLAYOUT_SIZE_LARGE; // For 7" tablet
                        boolean is7InchTablet = context.getResources().getConfiguration()
                                .isLayoutSizeAtLeast(sizeLarge);
                        if (is7InchTablet) {
                            gridlaymanager = new GridLayoutManager(context, 3);
                        } else {
                            gridlaymanager = new GridLayoutManager(context, 2);
                        }

                        recyclerView.setLayoutManager(gridlaymanager);
                        recyclerView.addItemDecoration(new HorizontalDividerItemDecoration.Builder(context).size(1).color(Color.parseColor("#EEEEEE")).margin(22, 22).build());
                    }
                    for (int j = 0; j < values.length(); j++) {
                        JSONArray recordList = (JSONArray) values.get(j);
                        for (int k = 0; k < recordList.length(); k++) {
                            LinearLayout dataView = new LinearLayout(context);
                            dataView.setOrientation(LinearLayout.HORIZONTAL);

                            NewOutletBO bo = new NewOutletBO();
                            bo.setmName(jsonArray.getString(k));
                            bo.setValueText(recordList.getString(k));
                            list.add(bo);

                        }
                    }

                    RecyclerViewAdapter profileSchedule = new RecyclerViewAdapter(context.getApplicationContext(), list);
                    recyclerView.setAdapter(profileSchedule);

                }
            }
            mscrollview.addView(totalView);
        } catch (Exception ex) {
            Commons.print("" + ex);
        }*/
    }

    private void prepareViewForHistory(String table) {
        mscrollview = (ScrollView) findViewById(R.id.scrollview);
        mscrollview.removeAllViews();

        LinearLayout.LayoutParams weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.gravity = Gravity.CENTER;


        LinearLayout totalView = new LinearLayout(context);
        totalView.setOrientation(LinearLayout.VERTICAL);

        try {

            if (mLstHeader != null && table.equals(mCustomerVisited)) {


                View rowDetail = getLayoutInflater().inflate(R.layout.cs_history_visit_details,
                        null, false);

                rowDetail.findViewById(R.id.view_dotted_line).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                TextView tv_name_header = (TextView) rowDetail.findViewById(R.id.tv_name_header);
                TextView tv_address_header = (TextView) rowDetail.findViewById(R.id.tv_address_header);
                TextView tv_contact_header = (TextView) rowDetail.findViewById(R.id.tv_contact_header);
                TextView tv_ageGroup_header = (TextView) rowDetail.findViewById(R.id.tv_age_header);
                TextView tv_gender_header = (TextView) rowDetail.findViewById(R.id.tv_gender_header);
                TextView tv_email_header = (TextView) rowDetail.findViewById(R.id.tv_email_header);

                tv_name_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                tv_address_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                tv_contact_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                tv_ageGroup_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                tv_gender_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                tv_email_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                TextView tv_name = (TextView) rowDetail.findViewById(R.id.tv_name);
                TextView tv_address = (TextView) rowDetail.findViewById(R.id.tv_address);
                TextView tv_contact = (TextView) rowDetail.findViewById(R.id.tv_contact);
                TextView tv_ageGroup = (TextView) rowDetail.findViewById(R.id.tv_ageGroup);
                TextView tv_gender = (TextView) rowDetail.findViewById(R.id.tv_gender);
                TextView tv_email = (TextView) rowDetail.findViewById(R.id.tv_email);

                tv_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tv_address.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tv_contact.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tv_ageGroup.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tv_gender.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                tv_email.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                tv_name.setText(mLstHeader.get("name"));
                tv_address.setText(mLstHeader.get("address"));
                tv_contact.setText(mLstHeader.get("contactno"));
                tv_ageGroup.setText(mLstHeader.get("age"));
                tv_gender.setText(mLstHeader.get("gender"));
                tv_email.setText(mLstHeader.get("email"));

                totalView.addView(rowDetail);

                //sale


                ArrayList<HashMap<String, String>> mSalesList
                        = bmodel.mCounterSalesHelper.downloadCustomerSalesInformation(mLstHeader.get("uid"),isFromReport);
                if (mSalesList != null) {
                    String lastRetailer = "", lastCounter = "";
                    View rowSale = null;
                    LinearLayout ll_prod_list = null;
                    for (HashMap<String, String> data : mSalesList) {
                        if (!data.get("retailername").equals(lastRetailer) || !data.get("countername").equals(lastCounter)) {
                            rowSale = getLayoutInflater().inflate(R.layout.cs_history_sale,
                                    null, false);

                            TextView tv_date_header = (TextView) rowSale.findViewById(R.id.tv_date_header);
                            TextView tv_outlet_header = (TextView) rowSale.findViewById(R.id.tv_outlet_header);
                            TextView tv_counter_header = (TextView) rowSale.findViewById(R.id.tv_counter_header);
                            TextView tv_location_header = (TextView) rowSale.findViewById(R.id.tv_location_header);
                            tv_date_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                            tv_outlet_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                            tv_counter_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                            tv_location_header.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                            TextView tv_date = (TextView) rowSale.findViewById(R.id.tv_date);
                            TextView tv_retailer = (TextView) rowSale.findViewById(R.id.tv_retailer);
                            TextView tv_counter = (TextView) rowSale.findViewById(R.id.tv_counter);
                            TextView tv_location = (TextView) rowSale.findViewById(R.id.tv_location);
                            ll_prod_list = (LinearLayout) rowSale.findViewById(R.id.ll_prod_list);

                            tv_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                            tv_retailer.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                            tv_counter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                            tv_location.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                            tv_date.setText(data.get("date"));
                            tv_retailer.setText(data.get("retailername"));
                            tv_counter.setText(data.get("countername"));
                            tv_location.setText(data.get("location"));

                            rowSale.findViewById(R.id.view_dotted_line).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                        }

                        View rowSaleDetail = getLayoutInflater().inflate(R.layout.cs_history_sale_detail,
                                null, false);
                        TextView tv_pname = (TextView) rowSaleDetail.findViewById(R.id.tv_pname);
                        TextView tv_piece = (TextView) rowSaleDetail.findViewById(R.id.tv_piece);
                        TextView tv_value = (TextView) rowSaleDetail.findViewById(R.id.tv_value);

                        tv_pname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        tv_piece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                        tv_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                        tv_pname.setText(data.get("pname"));
                        tv_piece.setText(data.get("qty"));
                        tv_value.setText(data.get("value"));


                        ll_prod_list.addView(rowSaleDetail);

                        lastRetailer = data.get("retailername");
                        lastCounter = data.get("countername");

                    }

                    totalView.addView(rowSale);
                }


            } else if (lstTestData != null && table.equals(mTrialDetail)) {

                for (HashMap<String, String> data : lstTestData) {

                    View rowtest = getLayoutInflater().inflate(R.layout.cs_history_test,
                            null, false);
                    TextView tv_pname = (TextView) rowtest.findViewById(R.id.tv_pname);
                    TextView tv_timespent = (TextView) rowtest.findViewById(R.id.tv_timespent);
                    TextView tv_result = (TextView) rowtest.findViewById(R.id.tv_result);
                    TextView tv_feedback = (TextView) rowtest.findViewById(R.id.tv_feedback);

                    tv_pname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    tv_timespent.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    tv_result.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                    tv_feedback.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                    tv_pname.setText(data.get("productName"));
                    tv_timespent.setText(data.get("timetaken"));
                    tv_result.setText(data.get("result"));
                    tv_feedback.setText(data.get("feedback"));


                    totalView.addView(rowtest);

                }

            }
            /* else if (table.equals(mSurveyDetail)) {

                JSONArray values = lstHistory.get(table).getJSONArray(bmodel.synchronizationHelper.JSON_DATA_KEY);
                ArrayList<String> lstSurveyTypes = new ArrayList<>();
                for (int j = 0; j < values.length(); j++) {
                    JSONArray recordList = (JSONArray) values.get(j);
                    for (int k = 0; k < recordList.length(); k++) {
                        if (!lstSurveyTypes.contains(recordList.getString(0))) {
                            lstSurveyTypes.add(recordList.getString(0));
                        }

                    }
                }
                for (int i = 0; i < lstSurveyTypes.size(); i++) {
                    TextView tv_surveyType = new TextView(context);
                    tv_surveyType.setText(lstSurveyTypes.get(i) + "");
                    tv_surveyType.setPadding(12, 0, 0, 0);
                    totalView.addView(tv_surveyType, weight1);
                    for (int j = 0; j < values.length(); j++) {
                        JSONArray recordList = (JSONArray) values.get(j);
                        if (recordList.getString(0).equals(lstSurveyTypes.get(i))) {

                            LinearLayout dataView = new LinearLayout(context);
                            dataView.setOrientation(LinearLayout.HORIZONTAL);
                            //   for (int k = 0; k < recordList.length(); k++) {
                            TextView tv_title = new TextView(context);
                            tv_title.setText(recordList.getString(1) + "");
                            tv_title.setPadding(12, 0, 0, 0);
                            tv_title.setWidth(100);
                            tv_title.setTypeface(null, Typeface.BOLD);
                            tv_title.setWidth(120);

                            TextView tv_value = new TextView(context);
                            tv_value.setText(recordList.getString(2) + "");
                            tv_value.setPadding(12, 0, 0, 0);
                            //tv_value.setWidth(100);


                            //}
                            dataView.addView(tv_title);
                            dataView.addView(tv_value, weight1);
                            totalView.addView(dataView, weight1);
                        }
                    }

                }

            }
*/
            mscrollview.addView(totalView);
        } catch (Exception ex) {
            Commons.print("" + ex);
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
                    ((TextView) tabViewChild).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                }
            }
        }
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<CShistoryDialog.RecyclerViewAdapter.ViewHolder> {

        private ArrayList<NewOutletBO> items;
        private Context mContext;

        public RecyclerViewAdapter(Context mContext, ArrayList<NewOutletBO> items) {
            this.mContext = mContext;
            this.items = items;
        }

        @Override
        public CShistoryDialog.RecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cs_history_visit_details_row_item, parent, false);
            return new CShistoryDialog.RecyclerViewAdapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(CShistoryDialog.RecyclerViewAdapter.ViewHolder holder, int position) {
            final NewOutletBO projectObj = items.get(position);
            holder.menuText.setText(projectObj.getmName());
            holder.valueText.setText(projectObj.getValueText());
            holder.itemView.setTag(projectObj);
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
            private TextView menuText, valueText;

            public ViewHolder(View itemView) {
                super(itemView);
                menuText = (TextView) itemView.findViewById(R.id.menu_name);
                valueText = (TextView) itemView.findViewById(R.id.value_name);
                menuText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                valueText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            }
        }
    }

    interface CShistoryInerface {
        void onDismiss(String referenceId);
    }
}
