package com.ivy.ui.mvp.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.Html;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.mvp.MVPNotification;
import com.ivy.cpg.view.mvp.MvpBO;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.view.OnSingleClickListener;
import com.ivy.ui.mvp.MVPConstants;
import com.ivy.ui.mvp.MVPContractor;
import com.ivy.ui.mvp.adapter.MVPDashboardAdapter;
import com.ivy.ui.mvp.adapter.MVPRankingListAdapter;
import com.ivy.ui.mvp.di.DaggerMVPComponent;
import com.ivy.ui.mvp.di.MVPModule;
import com.ivy.ui.mvp.model.MVPKPIGroupBO;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnLongClick;

public class MVPFragmentNew extends BaseFragment implements MVPContractor.MVPView {

    @BindView(R.id.txtSeller)
    TextView txtSeller;

    @BindView(R.id.txtSellerRankSelf)
    TextView txtSellerRankSelf;

    @BindView(R.id.txtSellerRankTeam)
    TextView txtSellerRankTeam;

    @BindView(R.id.txtSellerRankEnterprise)
    TextView txtSellerRankEnterprise;

    @BindView(R.id.btnAchievement)
    RadioButton btnAchievement;

    @BindView(R.id.btnRanksandPoints)
    RadioButton btnRanksandPoints;

    @BindView(R.id.dashboardLv)
    RecyclerView rView;

    @BindView(R.id.filter)
    ImageView spnFilter;

    @BindView(R.id.kpiTitle)
    TextView kpiTitle;

    @BindView(R.id.kpiActual)
    TextView kpiActual;

    @BindView(R.id.kpiTarget)
    TextView kpiTarget;

    @BindView(R.id.kpiBalance)
    TextView kpiBalance;

    @BindView(R.id.pieChart)
    PieChart pieChart;

    @BindView(R.id.headerTitle)
    TextView headerTitle;

    @BindView(R.id.im_user)
    ImageView circularImageView;

    Spinner spinner;
    int selectedKPIID = -1;

    private String screenTitle;
    private String menuCode;

    @Inject
    MVPContractor.MVPPresenter<MVPContractor.MVPView> mvpPresenter;

    ArrayList<String> groupName = new ArrayList<>();
    private MVPRankingListAdapter mvpRankingListAdapter;

    String selectedGroup;
    boolean isAchievementTab = true;

    ArrayList<DashBoardBO> dashboardListData = new ArrayList<>();
    private MVPDashboardAdapter dashboardListAdapter;
    DividerItemDecoration dividerItemDecoration;

    String imageFileName;
    private static final int CAMERA_REQUEST_CODE = 1;

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_mvp_new;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        mvpPresenter.fetchSellerInfo();
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null) {
            Bundle bundle = getArguments();
            menuCode = bundle.getString("menuCode");
            screenTitle = bundle.getString("screentitle");
        }
    }

    @Override
    public void init(View view) {
        dashboardListData = new ArrayList<>();
    }

    @Override
    protected void setUpViews() {
        setUpActionBar();
        setProfileImage();
        dividerItemDecoration = new DividerItemDecoration(Objects.requireNonNull(getActivity()), DividerItemDecoration.VERTICAL);
        rView.setHasFixedSize(true);
        rView.setNestedScrollingEnabled(false);
        rView.setLayoutManager(new LinearLayoutManager(getActivity()));
    }

    @OnLongClick(R.id.im_user)
    public boolean setImage(View view) {
        showAlert("", context.getResources().getString(R.string.capture_picture_request),
                () -> {
                    captureUserProfilePicture();
                }, () -> {

                });
        return false;
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
    }

    private void setUpActionBar() {
        getActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActionBar().setElevation(0);
        }

        if (screenTitle != null)
            setScreenTitle(screenTitle);

        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void initializeDi() {
        DaggerMVPComponent.builder().ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .mVPModule(new MVPModule(this))
                .build()
                .inject(this);
        setBasePresenter((BasePresenter) mvpPresenter);
    }

    @Override
    public void populateHierarchy(ArrayList<String> mvpGroupList) {
        groupName.clear();
        groupName.addAll(mvpGroupList);
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void setSellerRanking(String groupName) {
        ArrayList<MvpBO> list;
        boolean chkGroupneeded = false;

        txtSellerRankSelf.setVisibility(View.GONE);
        txtSellerRankTeam.setVisibility(View.GONE);
        txtSellerRankEnterprise.setVisibility(View.GONE);

        selectedGroup = groupName;
        txtSeller.setText(mvpPresenter.getUserInfo().getUserName());
        if (selectedKPIID == -1) {
            list = mvpPresenter.getMvpUserList();
        } else {
            list = mvpPresenter.getMvpKPIList();
            chkGroupneeded = true;
        }

        for (MvpBO mvp : list) {
            if (mvp.getUserID() == mvpPresenter.getUserInfo().getUserid() && (!chkGroupneeded || mvp.getKpiId().equals(String.valueOf(selectedKPIID)))) {
                String rankTxt = "", text = "";
                switch (mvp.getGroupName().toLowerCase()) {
                    case MVPConstants
                            .KEYWORD_SELF:
                        rankTxt = getResources().getString(R.string.self_rank);
                        text = "<font color=#000000>" + mvp.getRank() + "</font> <font color=#ffffff>&nbsp;&nbsp;&nbsp;"
                                + rankTxt + "</font>";
                        txtSellerRankSelf.setText(Html.fromHtml(text));
                        txtSellerRankSelf.setVisibility(View.VISIBLE);
                        break;
                    case MVPConstants
                            .KEYWORD_TEAM:
                        rankTxt = getResources().getString(R.string.team_rank);
                        text = "<font color=#000000>&nbsp;" + mvp.getRank() + "</font> <font color=#ffffff>&nbsp;&nbsp;&nbsp;&nbsp;"
                                + rankTxt + "</font>";
                        txtSellerRankTeam.setText(Html.fromHtml(text));
                        txtSellerRankTeam.setVisibility(View.VISIBLE);
                        break;
                    case MVPConstants
                            .KEYWORD_ENTERPRISE:
                        rankTxt = getResources().getString(R.string.enterprise_rank);
                        text = "<font color=#000000>" + mvp.getRank() + "</font> <font color=#ffffff>&nbsp;&nbsp;&nbsp;"
                                + rankTxt + "</font>";
                        txtSellerRankEnterprise.setText(Html.fromHtml(text));
                        txtSellerRankEnterprise.setVisibility(View.VISIBLE);
                        break;
                    default:
                        break;
                }
            }
        }

    }

    @Override
    public void populateRankingList() {
        ArrayList<MvpBO> list;
        boolean chkGroupneeded = false;
        spnFilter.setEnabled(true);
        headerTitle.setText(getResources().getString(R.string.reportsandranks));
        ArrayList<MvpBO> mvpFilteredList = new ArrayList<>();
        if (selectedKPIID == -1) {
            list = mvpPresenter.getMvpUserList();
        } else {
            list = mvpPresenter.getMvpKPIList();
            chkGroupneeded = true;
        }
        for (MvpBO mvp : list) {
            if (mvp.getUserID() != mvpPresenter.getUserInfo().getUserid() && (mvp.getGroupName() != null && mvp.getGroupName().equalsIgnoreCase(selectedGroup))
                    && (!chkGroupneeded || mvp.getKpiId().equals(String.valueOf(selectedKPIID)))) {
                mvpFilteredList.add(mvp);
            }
        }
        rView.addItemDecoration(dividerItemDecoration);
        mvpRankingListAdapter = new MVPRankingListAdapter(getActivity(), mvpFilteredList, mvpPresenter.getUserInfo().getUserid() + "");
        rView.setAdapter(mvpRankingListAdapter);
    }

    @Override
    public void populateKPIFilter() {
//
//        if(mvpPresenter.getMvpKPIGroupList() != null && mvpPresenter.getMvpKPIGroupList().size() > 0 && selectedKPIID == -1){
//            selectedKPIID = mvpPresenter.getMvpKPIGroupList().get(0).getMvpKPIID();
//            mvpPresenter.fetchSellerDashboardDetails();
//        }
        spnFilter.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                PopupMenu popup = new PopupMenu(getActivity(), v);
                for (int i = 0; i < mvpPresenter.getMvpKPIGroupList().size(); i++) {
                    MVPKPIGroupBO groupBo = mvpPresenter.getMvpKPIGroupList().get(i);
                    popup.getMenu().add(0, groupBo.getMvpKPIID(), i, groupBo.getMvpKpiName());
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        kpiTitle.setText(item.getTitle().toString());
                        selectedKPIID = item.getItemId();
                        setSellerRanking(selectedGroup);
                        if (!isAchievementTab)
                            populateRankingList();
                        else
                            populateAchievementList();
                        mvpPresenter.fetchSellerDashboardDetails();
                        return false;
                    }
                });
                popup.show();
            }
        });
    }

    @Override
    public void populateAchievementList() {
        headerTitle.setText(getResources().getString(R.string.monthlyachievement));
        spnFilter.setEnabled(false);
        mvpPresenter.fetchSellerDashboardForUserAndInterval(String.valueOf(mvpPresenter.getUserInfo().getUserid()), MVPConstants.INTERVAL);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_mvp, menu);
        try {
            MenuItem menuItem = menu.findItem(R.id.spinnerGroup);
            spinner = (Spinner) menuItem.getActionView();
            ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(),
                    R.layout.spinner_item, groupName);
            adapter.setDropDownViewResource(R.layout.spinner_item);
            spinner.setAdapter(adapter);
            spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    setSellerRanking(groupName.get(position));
                    if (!isAchievementTab)
                        populateRankingList();
                    else
                        populateAchievementList();
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_notification) {
            Intent intent = new Intent(getActivity(), MVPNotification.class);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.in_from_bottom, R.anim.stay);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @OnClick({R.id.btnAchievement, R.id.btnRanksandPoints})
    public void onRadioButtonClicked(RadioButton radioButton) {
        boolean checked = radioButton.isChecked();
        switch (radioButton.getId()) {
            case R.id.btnAchievement:
                if (checked) {
                    btnAchievement.setTextColor(getResources().getColor(android.R.color.white));
                    btnRanksandPoints.setTextColor(getResources().getColor(R.color.black_bg1));
                    isAchievementTab = true;
                    populateAchievementList();
                }
                break;
            case R.id.btnRanksandPoints:
                if (checked) {
                    btnRanksandPoints.setTextColor(getResources().getColor(android.R.color.white));
                    btnAchievement.setTextColor(getResources().getColor(R.color.black_bg1));
                    isAchievementTab = false;
                    populateRankingList();
                }
                break;
        }
    }

    ArrayList<DashBoardBO> dashBoardList = new ArrayList<>();

    @Override
    public void setSellerKPIDetails(ArrayList<DashBoardBO> dashBoardList) {
        this.dashBoardList = dashBoardList;
        for (DashBoardBO dashboard : dashBoardList) {
            if (dashboard.getKpiTypeLovID() == selectedKPIID) {
                kpiTitle.setText(dashboard.getText());
                kpiActual.setText(getResources().getString(R.string.actual) + " : " + SDUtil.getWholeNumber(dashboard.getKpiAcheived()) + "");
                kpiTarget.setText(getResources().getString(R.string.target) + " : " + SDUtil.getWholeNumber(dashboard.getKpiTarget()) + "");
                double balance = SDUtil.convertToInt(dashboard.getKpiTarget()) - SDUtil.convertToInt(dashboard.getKpiAcheived());
                kpiBalance.setText(getResources().getString(R.string.balance) + " : " + (balance > 0 ? SDUtil.getWholeNumber(balance + "") : "0"));
                buildPieChart(dashboard);
                break;
            }
        }
    }

    private void buildPieChart(DashBoardBO dashboard) {
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(0, 0, 0, 0);
        pieChart.setDragDecelerationFrictionCoef(0.95f);
        pieChart.setDrawHoleEnabled(true);
        pieChart.setTransparentCircleColor(Color.TRANSPARENT);
        pieChart.setTransparentCircleAlpha(110);
        pieChart.setDrawCenterText(false);

        // enable rotation of the chart by touch
        pieChart.setRotationEnabled(false);
        pieChart.setHighlightPerTapEnabled(true);
        pieChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);


        Legend l = pieChart.getLegend();
        l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        l.setOrientation(Legend.LegendOrientation.VERTICAL);
        l.setDrawInside(false);
        l.setEnabled(false);

        setOffset(pieChart);
        pieChart.setHoleColor(Color.TRANSPARENT);
        pieChart.setHoleRadius(50f);
        pieChart.setTransparentCircleRadius(28f);
        pieChart.setMaxAngle(360f); // HALF CHART
        pieChart.setRotationAngle(180f);
        // entry label styling
        pieChart.setEntryLabelColor(Color.TRANSPARENT);
        pieChart.setEntryLabelTextSize(0f);

        ArrayList<PieEntry> entries = new ArrayList<>();

        double balanceValue = SDUtil.convertToInt(dashboard.getKpiTarget()) - SDUtil.convertToInt(dashboard.getKpiAcheived());
        entries.add(new PieEntry(SDUtil.convertToFloat(dashboard.getKpiAcheived())));
        entries.add(new PieEntry(balanceValue >= 0 ? SDUtil.convertToFloat(balanceValue + "") : 0));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setSliceSpace(0f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        colors.add(ContextCompat.getColor(context, R.color.colorPrimary));
        colors.add(ContextCompat.getColor(context, R.color.Orange));

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(0f);
        pieChart.setData(data);
    }

    private void setOffset(PieChart mChart) {
        Display display = ((Activity) context).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;
        int offset = (int) (height * 0.20); /* percent to move */
        ConstraintLayout.LayoutParams rlParams =
                (ConstraintLayout.LayoutParams) mChart.getLayoutParams();
        rlParams.setMargins(0, 10, 0, -offset);
        mChart.setLayoutParams(rlParams);
    }

    @Override
    public void setDashboardListAdapter(List<DashBoardBO> dashBoardBOS) {
        dashboardListData.clear();
        dashboardListData.addAll(dashBoardBOS);
        rView.removeItemDecoration(dividerItemDecoration);
        dashboardListAdapter = new MVPDashboardAdapter(Objects.requireNonNull(getActivity()), dashboardListData, mvpPresenter.getLabelsMap());
        rView.setAdapter(dashboardListAdapter);
    }

    @Override
    public void setProfileImage() {
        if (StringUtils.isNullOrEmpty(mvpPresenter.getUserInfo().getImagePath())) {
            String[] imgPaths = mvpPresenter.getUserInfo().getImagePath().split("/");
            String path = imgPaths[imgPaths.length - 1];
            File imgFile = new File(getActivity().getExternalFilesDir(
                    Environment.DIRECTORY_DOWNLOADS)
                    + "/"
                    + mvpPresenter.getUserInfo().getUserid()
                    + DataMembers.DIGITAL_CONTENT
                    + "/"
                    + DataMembers.USER + "/"
                    + path);
            if (imgFile.exists()) {
                try {
                    Glide.with(getActivity())
                            .load(imgFile)
                            .centerCrop()
                            .placeholder(R.drawable.face)
                            .error(R.drawable.face)
                            .into(circularImageView);

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            } else {
                circularImageView
                        .setImageResource(R.drawable.face);
            }
        }
    }

    private void captureUserProfilePicture() {
        if (FileUtils.isExternalStorageAvailable(10)) {
            imageFileName = "USER_" + mvpPresenter.getUserInfo().getUserid() + "_"
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME) + "_img.jpg";
            try {
                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(CameraActivity.QUALITY, 40);
                intent.putExtra(CameraActivity.PATH, FileUtils.photoFolderPath + "/" + imageFileName);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            showMessage(R.string.external_storage_not_avail);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case CAMERA_REQUEST_CODE:
                if (resultCode == 1) {
                    Uri uri = FileUtils.getUriFromFile(getActivity(), FileUtils.photoFolderPath + "/" + imageFileName);
                    mvpPresenter.getUserInfo().setImagePath(imageFileName);
                    mvpPresenter.updateUserProfile(mvpPresenter.getUserInfo());
                    circularImageView.invalidate();
                    circularImageView.setImageURI(uri);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void showUserImageAlert() {
        showMessage("Image Saved Successfully");
    }
}
