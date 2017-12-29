package com.ivy.cpg.view.competitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompanyBO;
import com.ivy.sd.png.bo.CompetitorBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;

public class CompetitorTackingFragment extends IvyBaseFragment {

    private int mSelectedCompany;
    public int competitorid, trackinglistId;
    private View view;
    private BusinessModel bmodel;
    private ListView lvcategorylist;

    private String from = "";
    private String calledBy = "0";
    private TabLayout tabLayout;
    private boolean isFromChild;

    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_competitor, container, false);

        viewInitialise(view);

        final Intent i = getActivity().getIntent();
        from = i.getStringExtra("from");
        calledBy = from != null ? from : "0";
        isFromChild = i.getBooleanExtra("isFromChild", false);

        return view;

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true); // To display menu items in the Action Bar
    }

    /**
     * Initialize the Views
     */
    private void viewInitialise(View view) {

        lvcategorylist = (ListView) view.findViewById(R.id.lvcategorylist);
        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);

        float scale = getContext().getResources().getDisplayMetrics().widthPixels;
        scale = scale / bmodel.competitorTrackingHelper.getCompanyList().size();
//        scale=scale/3;
        TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);

        lvcategorylist.setCacheColorHint(0);

        CompanyBO companyBO;
        for (int i = 0; i < bmodel.competitorTrackingHelper.getCompanyList().size(); i++) {
//        for(int i=0;i<3;i++){
            companyBO = bmodel.competitorTrackingHelper.getCompanyList().get(i);
            TabLayout.Tab tab = tabLayout.newTab();

            TextView txtVw = new TextView(getActivity());
            txtVw.setGravity(Gravity.CENTER);
            txtVw.setWidth((int) scale);
            txtVw.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            txtVw.setTextColor(color);
            txtVw.setMaxLines(1);
            txtVw.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            txtVw.setText(companyBO.getCompetitorName());
            txtVw.setAllCaps(true);

            tab.setTag(companyBO.getCompetitorid());
            tab.setCustomView(txtVw);
            tabLayout.addTab(tab);
        }
        changeTabsFont();
        mSelectedCompany = (int) tabLayout.getTabAt(0).getTag();
        updateList();

        if (bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            LinearLayout footer = (LinearLayout) view.findViewById(R.id.footer);
            footer.setVisibility(View.VISIBLE);

            Button btnClose = (Button) view.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                            "", getActivity().getResources().getString(R.string.move_next_activity),
                            false, getActivity().getResources().getString(R.string.ok),
                            getActivity().getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
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


    @Override
    public void onStart() {
        super.onStart();

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        // Logout the Application If values in objects are cleared
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            setScreenTitle(
                    bmodel.mSelectedActivityName);

        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab != null) {
                    mSelectedCompany = (int) tab.getTag();
                    updateList();
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
    public void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("Competitor Tracking");
        bmodel.competitorTrackingHelper.loadcompetitors();
        updateList();
    }


    /**
     * Called whenever we call invalidateOptionsMenu()
     */


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (calledBy.equals("3")) {
//                startActivity(new Intent(getActivity(), CSHomeScreen.class));
                getActivity().finish();
            } else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Save the values in Aysnc task through Background
     *
     * @author gnanaprakasam.d
     */
    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.competitorTrackingHelper.saveCompetitor();
                bmodel.updateIsVisitedFlag();
                if (!calledBy.equals("3"))
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_COMPETITOR);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.saving),
					true, false);*/
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
            //	progressDialogue.dismiss();
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();

            if (calledBy.equals("3")) {
//                startActivity(new Intent(getActivity(), CSHomeScreen.class));
                getActivity().finish();
            } else {
                startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
        }

    }


    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }


    private void updateList() {
        ArrayList<CompetitorBO> items = new ArrayList<>();

        for (int i = 0; i < bmodel.competitorTrackingHelper
                .getCompetitorMaster().size(); i++) {
            CompetitorBO ret = (CompetitorBO) bmodel.competitorTrackingHelper
                    .getCompetitorMaster().get(i);
            if (ret.getCompanyID() == mSelectedCompany || mSelectedCompany == -1)
                items.add(ret);
        }
        lvcategorylist.setAdapter(new CompetitorAdapter(items));
    }

    private class CompetitorAdapter extends ArrayAdapter<CompetitorBO> {
        private ArrayList<CompetitorBO> items;

        public CompetitorAdapter(ArrayList<CompetitorBO> items) {
            super(getActivity(), R.layout.competitor_home, items);
            this.items = items;
        }

        public CompetitorBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final CompetitorViewHolder holder;

            if (convertView == null) {

                holder = new CompetitorViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity());
                convertView = (View) inflater.inflate(
                        R.layout.competitor_home, null);


                holder.tvCategoryName = (TextView) convertView
                        .findViewById(R.id.activity_name);
                holder.ivIndicator = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_iv);
                holder.llCircle = (LinearLayout) convertView
                        .findViewById(R.id.icon_ll);


                convertView.setTag(holder);

            } else {
                holder = (CompetitorViewHolder) convertView.getTag();
            }

            holder.mCompetitorBO = items.get(position);

            holder.tvCategoryName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.tvCategoryName.setText(holder.mCompetitorBO.getProductname());
            if (holder.mCompetitorBO.isAchieved()) {
                holder.llCircle.setBackgroundResource(R.drawable.activity_icon_bg_completed);
                holder.ivIndicator.setColorFilter(Color.argb(255, 255, 255, 255));
            } else {
                holder.llCircle.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.ivIndicator.setColorFilter(Color.argb(0, 0, 0, 0));
            }

            convertView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(),
                            SubCompetitorTrackingActivity.class);
                    intent.putExtra("screentitle", holder.mCompetitorBO.getProductname());
                    intent.putExtra("companyId", holder.mCompetitorBO.getCompanyID());
                    intent.putExtra("competitorId", holder.mCompetitorBO.getCompetitorpid());
                    intent.putExtra("from", calledBy);
                    startActivity(intent);

                }
            });


            return convertView;
        }
    }

    class CompetitorViewHolder {
        CompetitorBO mCompetitorBO;
        TextView tvCategoryName;
        ImageView ivIndicator;
        LinearLayout llCircle;
    }


}