package com.ivy.cpg.view.mvp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MVPFragment extends IvyBaseFragment {
    private CollapsingToolbarLayout collapsingToolbar;
    private BusinessModel bmodel;
    private List<MvpBO> mvpData = new ArrayList<>();
    private List<MvpBO> mvpTeamBadgeData = new ArrayList<>();
    private List<MVPBadgeBO> mvpBadgeInfo = new ArrayList<>();
    private String str_name;
    private String str_designation;
    private TextView rank;
    private TextView badgepoints_count;
    private int parentID;
    private LinearLayout peerView;
    private LinearLayout teamBadgeView;
    private LinearLayout badgeView;
    private View view;
    private TextView userName, userDesignation;
    private MVPHelper mvpHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mvpHelper = MVPHelper.getInstance(getActivity());

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_mvp, container, false);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        collapsingToolbar = view.findViewById(R.id.collapsing_toolbar);
        peerView = view.findViewById(R.id.ll_peer);
        teamBadgeView = view.findViewById(R.id.ll_teambadge);
        badgeView = view.findViewById(R.id.ll_badge);
        rank = view.findViewById(R.id.txtRank);
        rank.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.THIN));
        badgepoints_count = view.findViewById(R.id.badgepoints_count);
        badgepoints_count.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
            mvpHelper.downloadMVPIdBySuperwisorId();
            List<Integer> mvpIdList = mvpHelper.getMvpUserIdList();
            if (mvpIdList != null && !mvpIdList.isEmpty()) {
                new DownloadMVPData().execute(mvpIdList.get(0));
            }
        } else {
            new DownloadMVPData().execute(bmodel.userMasterHelper.getUserMasterBO().getUserid());
        }
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        TextView rankTxtv = view.findViewById(R.id.txtRankTitle);
        rankTxtv.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        TextView pointTxtv = view.findViewById(R.id.txtpointsTitle);
        pointTxtv.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        TextView badgesTxtv = view.findViewById(R.id.textview411);
        badgesTxtv.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));

        TextView peerTxtv = view.findViewById(R.id.peer_textview);
        peerTxtv.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

        userName = view.findViewById(R.id.tv_username);
        userName.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        userDesignation = view.findViewById(R.id.tv_designation);
        userDesignation.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setCurrentTabFragment(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        setCurrentTabFragment(0);

        return view;
    }

    private void setCurrentTabFragment(int tabPosition)
    {
        switch (tabPosition)
        {
            case 0 :
                replaceFragment(new MVPKPIFragment());
                break;
            case 1 :
                replaceFragment(new MVPToppersFragment());
                break;
        }
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.mvp_container, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_mv, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            Intent i = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (id == R.id.menu_notification) {
            Intent intent = new Intent(getActivity(), MVPNotification.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.stay);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getBadgeViews(int userid) {
        TextView points = view.findViewById(R.id.txtPoints);
        points.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.THIN));

        for (MvpBO mvp : mvpData) {
            if (mvp.getUserID() == userid) {
                str_name = mvp.getUsername();
                String strRank = mvp.getRank() + "";
                rank.setText(strRank);
                String strTotScore = mvp.getTotalScore() + "";
                points.setText(strTotScore);
                parentID = mvp.getParentPosID();

                str_designation = mvp.getEntitylevel();

                for (Integer key : mvp.getBadgeList().keySet()) {
                    for (MVPBadgeBO mvpBadgeBO : mvpBadgeInfo) {
                        if (key == mvpBadgeBO.getBadgeID() && mvp.getBadgeList().get(key) != null) {
                            mvpBadgeBO.setBadgeCount(mvp.getBadgeList().get(key) + "");
                        }
                    }
                }
                break;
            }
        }

        int totalBadgeCount = 0;
        if (mvpBadgeInfo != null) {
            for (MVPBadgeBO mvpbadge : mvpBadgeInfo) {
                Bitmap bitmap = getBitMapImage(mvpbadge.getImageName());
                mvpbadge.setBadgeBitmap(bitmap);

                LayoutInflater inflater = getActivity().getLayoutInflater();
                final ViewGroup nullParent = null;
                View view = inflater.inflate(
                        R.layout.mvpbadges_list, nullParent);

                ImageView iv = view.findViewById(R.id.badge);

                if (mvpbadge.getBadgeBitmap() != null) {
                    iv.setImageBitmap(mvpbadge.getBadgeBitmap());
                } else {
                    iv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_badge));
                }

                if (!"0".equals(mvpbadge.getBadgeCount())) {
                    String strBadgeCount = mvpbadge.getBadgeCount() + "";
                    ((TextView) view.findViewById(R.id.badge_count)).setText(strBadgeCount);
                }

                ((TextView) view.findViewById(R.id.badge_count)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tv_badge_name)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

                String strBadgename = mvpbadge.getBadgeName() + "";
                ((TextView) view.findViewById(R.id.tv_badge_name)).setText(strBadgename);
                totalBadgeCount = (totalBadgeCount + SDUtil.convertToInt(mvpbadge.getBadgeCount()));
                badgeView.addView(view);
            }
            String strBadgeCount = Integer.toString(totalBadgeCount);
            badgepoints_count.setText(strBadgeCount);
        }
    }

    private Bitmap getBitMapImage(String name) {
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.MVP + "/" + name);
        return FileUtils.decodeFile(file);
    }

    private void getTeamViews() {
        int i = 1;
        if (mvpTeamBadgeData != null) {
            for (MvpBO mvp : mvpTeamBadgeData) {

                LayoutInflater inflater = getActivity().getLayoutInflater();
                final ViewGroup nullParent = null;
                View view = inflater.inflate(
                        R.layout.row_mvp_badge_card, nullParent);

                String strEntityLevel = mvp.getEntitylevel() + "";
                ((TextView) view.findViewById(R.id.desc)).setText(strEntityLevel);
                String strRanks = mvp.getRank() + "/" + mvp.getTotalRank();
                ((TextView) view.findViewById(R.id.rank)).setText(strRanks);

                String strRankCount = mvp.getRank() + "";
                if (i == 1) {
                    view.findViewById(R.id.btn).setBackgroundResource(R.drawable.self);
                    ((TextView) view.findViewById(R.id.rank_count)).setText(strRankCount);
                } else if (i == 2) {
                    view.findViewById(R.id.btn).setBackgroundResource(R.drawable.team);
                    ((TextView) view.findViewById(R.id.rank_count)).setText(strRankCount);
                } else {
                    view.findViewById(R.id.btn).setBackgroundResource(R.drawable.ent);
                    ((TextView) view.findViewById(R.id.rank_count)).setText(strRankCount);
                }

                teamBadgeView.addView(view);
                i++;
            }
        }
    }

    private void getPeerViews(int parentID, int userID) {
        if (mvpData != null) {
            for (MvpBO mvp : mvpData) {
                if (mvp.getUserID() != userID && mvp.getParentPosID() == parentID) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    final ViewGroup nullParent = null;
                    View view = inflater.inflate(
                            R.layout.row_mvp_peer_card, nullParent);

                    ((TextView) view.findViewById(R.id.tv_name)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                    ((TextView) view.findViewById(R.id.tv_rank)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                    ((TextView) view.findViewById(R.id.tv_score)).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

                    String strUsername = mvp.getUsername() + "";
                    ((TextView) view.findViewById(R.id.tv_name)).setText(strUsername);
                    String strRank = mvp.getRank() + "";
                    ((TextView) view.findViewById(R.id.tv_rank)).setText(strRank);
                    String strTotScore = mvp.getTotalScore() + "";
                    ((TextView) view.findViewById(R.id.tv_score)).setText(strTotScore);
                    view.setTag(mvp);

                    if (!bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                        view.setEnabled(false);
                    }

                    view.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            MvpBO selectedMVPBO = (MvpBO) v.getTag();
                            if (selectedMVPBO != null) {
                                new DownloadMVPData().execute(selectedMVPBO.getUserID());
                            }
                        }
                    });
                    peerView.addView(view);
                }
            }
        }
    }

    class DownloadMVPData extends AsyncTask<Integer, Void, Integer> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
            super.onPreExecute();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            mvpHelper.loadMVPData(params[0]);
            mvpHelper.downloadBadgeUrlList();
            mvpData = mvpHelper.getMvpDataList();
            mvpTeamBadgeData = mvpHelper.getMvpBadgeDataList();
            mvpBadgeInfo = mvpHelper.getMvpBadgeInfoList();
            return params[0];
        }

        @Override
        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            alertDialog.dismiss();
            badgeView.removeAllViews();
            teamBadgeView.removeAllViews();
            peerView.removeAllViews();
            if (!bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                getBadgeViews(result);
            }
            getTeamViews();
            getPeerViews(parentID, result);

            collapsingToolbar.setTitle(" ");

            final FrameLayout frameLayout = new FrameLayout(getActivity());
            FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.MATCH_PARENT);
            frameLayout.setLayoutParams(frameLayoutParams);

            // Create new LinearLayout
            final LinearLayout linearLayout = new LinearLayout(getActivity());
            frameLayoutParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, 75);
            frameLayoutParams.gravity = Gravity.BOTTOM;
            frameLayoutParams.setMargins(20, 0, 0, 0);
            linearLayout.setLayoutParams(frameLayoutParams);
            linearLayout.setOrientation(LinearLayout.VERTICAL);

            // Add textviews
            final TextView textView1 = new TextView(getActivity());
            textView1.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutParams.gravity = Gravity.START | Gravity.CENTER;

            textView1.setLayoutParams(linearLayoutParams);
            if (str_name != null)
                textView1.setText(str_name);
            textView1.setTextColor(Color.parseColor("#000000"));
            textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView1.setTypeface(Typeface.DEFAULT_BOLD);
            linearLayout.addView(textView1);

            final TextView textView2 = new TextView(getActivity());
            textView2.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutParams.gravity = Gravity.START | Gravity.CENTER;

            textView2.setLayoutParams(linearLayoutParams);
            if (str_designation != null)
                textView2.setText(str_designation);
            textView2.setTextColor(Color.parseColor("#000000"));
            textView2.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
            linearLayout.addView(textView2);
            frameLayout.addView(linearLayout);
            collapsingToolbar.addView(frameLayout);

            userName.setText("" + str_name);
            userDesignation.setText("" + str_designation);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.main_content));
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
}
