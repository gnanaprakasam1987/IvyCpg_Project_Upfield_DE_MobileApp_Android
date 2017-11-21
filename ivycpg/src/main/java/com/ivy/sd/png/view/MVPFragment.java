package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CollapsingToolbarLayout;
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
import com.ivy.sd.png.bo.MVPBadgeBO;
import com.ivy.sd.png.bo.MvpBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
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

        collapsingToolbar = (CollapsingToolbarLayout) view.findViewById(R.id.collapsing_toolbar);
        peerView = (LinearLayout) view.findViewById(R.id.ll_peer);
        teamBadgeView = (LinearLayout) view.findViewById(R.id.ll_teambadge);
        badgeView = (LinearLayout) view.findViewById(R.id.ll_badge);
        rank = (TextView) view.findViewById(R.id.txtRank);
        rank.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        badgepoints_count = (TextView) view.findViewById(R.id.badgepoints_count);
        badgepoints_count.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
            bmodel.mvpHelper.downloadMVPIdBySuperwisorId();
            List<Integer> mvpIdList = bmodel.mvpHelper.getMvpUserIdList();
            if (mvpIdList != null && !mvpIdList.isEmpty()) {
                new DownloadMVPData().execute(mvpIdList.get(0));
            }
        } else {
            new DownloadMVPData().execute(bmodel.userMasterHelper.getUserMasterBO().getUserid());
        }
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        TextView rankTxtv = (TextView) view.findViewById(R.id.txtRankTitle);
        rankTxtv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView pointTxtv = (TextView) view.findViewById(R.id.txtpointsTitle);
        pointTxtv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView badgesTxtv = (TextView) view.findViewById(R.id.textview411);
        badgesTxtv.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));

        TextView peerTxtv = (TextView) view.findViewById(R.id.peer_textview);
        peerTxtv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        userName = (TextView) view.findViewById(R.id.tv_username);
        userName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        userDesignation = (TextView) view.findViewById(R.id.tv_designation);
        userDesignation.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        return view;
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
        TextView points = (TextView) view.findViewById(R.id.txtPoints);
        points.setTypeface(bmodel.configurationMasterHelper
                .getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

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

                ImageView iv = (ImageView) view.findViewById(R.id.badge);

                if (mvpbadge.getBadgeBitmap() != null) {
                    iv.setImageBitmap(mvpbadge.getBadgeBitmap());
                } else {
                    iv.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_badge));
                }

                if (!"0".equals(mvpbadge.getBadgeCount())) {
                    String strBadgeCount = mvpbadge.getBadgeCount() + "";
                    ((TextView) view.findViewById(R.id.badge_count)).setText(strBadgeCount);
                }

                ((TextView) view.findViewById(R.id.badge_count)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView) view.findViewById(R.id.tv_badge_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                String strBadgename = mvpbadge.getBadgeName() + "";
                ((TextView) view.findViewById(R.id.tv_badge_name)).setText(strBadgename);
                totalBadgeCount = (totalBadgeCount + Integer.parseInt(mvpbadge.getBadgeCount()));
                badgeView.addView(view);
            }
            String strBadgeCount = Integer.toString(totalBadgeCount);
            badgepoints_count.setText(strBadgeCount);
        }
    }

    private Bitmap decodeFile(File f) {
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //The new size we want to scale to
            final int REQUIRED_SIZE = 70;

            //Find the correct scale value. It should be the power of 2.
            int width_tmp = o.outWidth;
            int height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
            Commons.printException(e + "");
        }
        return null;
    }

    private Bitmap getBitMapImage(String name) {
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.MVP + "/" + name);
        return decodeFile(file);
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

                    ((TextView) view.findViewById(R.id.tv_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    ((TextView) view.findViewById(R.id.tv_rank)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    ((TextView) view.findViewById(R.id.tv_score)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

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
            bmodel.mvpHelper.loadMVPData(params[0]);
            bmodel.mvpHelper.downloadBadgeUrlList();
            mvpData = bmodel.mvpHelper.getMvpDataList();
            mvpTeamBadgeData = bmodel.mvpHelper.getMvpBadgeDataList();
            mvpBadgeInfo = bmodel.mvpHelper.getMvpBadgeInfoList();
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
            textView1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            LinearLayout.LayoutParams linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutParams.gravity = Gravity.LEFT | Gravity.CENTER;

            textView1.setLayoutParams(linearLayoutParams);
            if (str_name != null)
                textView1.setText(str_name);
            textView1.setTextColor(Color.parseColor("#000000"));
            textView1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
            textView1.setTypeface(Typeface.DEFAULT_BOLD);
            linearLayout.addView(textView1);

            final TextView textView2 = new TextView(getActivity());
            textView2.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            linearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            linearLayoutParams.gravity = Gravity.LEFT | Gravity.CENTER;

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
