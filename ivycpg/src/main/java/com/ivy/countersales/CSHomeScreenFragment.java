package com.ivy.countersales;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.cpg.view.asset.PosmTrackingActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.planogram.CounterPlanogramActivity;
import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivyretail.views.CompetitorTrackingActivity;

import java.io.File;
import java.io.FilenameFilter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by mayuri.v on 6/16/2017.
 */
public class CSHomeScreenFragment extends IvyBaseFragment implements AppBarLayout.OnOffsetChangedListener {
    private static final String MENU_STOCK_CS = "MENU_STOCK_CS";
    private static final String MENU_CUSTOMER_CS = "MENU_CUST_VISIT_CS";
    public static final String MENU_COUNTER_PLANOGRAM = "MENU_COUNTER_PLANOGRAM";
    private static final String MENU_PLANOGRAM_CS = "MENU_PLANOGRAM_CS";
    public static final String MENU_DGT_CS = "MENU_DGT_CS";
    private static final String MENU_COMPETITOR_CS = "MENU_COMPETITOR_CS";
    private static final String MENU_CLOSE_CS = "MENU_CLOSE_CS";
    private static final String MENU_CS_STOCK_APPLY = "MENU_STOCK_APPLY_CS";
    public static final String MENU_POSM_CS = "MENU_POSM_CS";

    private BusinessModel bmodel;
    private ListView activityList;
    private String title;
    private Vector<ConfigureBO> menuDB = new Vector<ConfigureBO>();
    private static final HashMap<String, Integer> menuIcons = new HashMap<String, Integer>();
    private TypedArray typearr;
    private boolean isClick = false;
    private boolean isCreated;
    private IconicAdapter mSchedule;

    private int scrollRange = -1;
    CollapsingToolbarLayout collapsingToolbar;
    boolean isVisible = false;
    TextView cName, retailerNameTxt, mActivityDoneCount, mActivityTotalCount;
    String counterName = "";
    String retName = "";
    private Vector<ConfigureBO> mTempMenuList = new Vector<>();

    private AlertDialog alertDialog;
    private SurveyHelperNew surveyHelperNew;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        View view = inflater.inflate(R.layout.activity_cshomescreen_two_fragment, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        surveyHelperNew = SurveyHelperNew.getInstance(getActivity());

        activityList = (ListView) view.findViewById(R.id.listView1);
        cName = (TextView) view.findViewById(R.id.counterName);
        retailerNameTxt = (TextView) view.findViewById(R.id.retailer_name);
        retailerNameTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        cName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        activityList.setCacheColorHint(0);
        activityList.setDivider(null);

        ((TextView) view.findViewById(R.id.label_activity_count)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        mActivityDoneCount = (TextView) view.findViewById(R.id.activity_done_count);
        mActivityTotalCount = (TextView) view.findViewById(R.id.activity_total_count);

        mActivityDoneCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mActivityTotalCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        counterName = bmodel.userMasterHelper.getUserMasterBO().getCounterName();
        if (counterName != null && !counterName.isEmpty()) {
            cName.setText(counterName);
            cName.setVisibility(View.VISIBLE);
        } else {
            cName.setVisibility(View.GONE);
        }
        retName = bmodel.retailerMasterBO.getRetailerName();
        if (retName != null && !retName.isEmpty()) {
            retailerNameTxt.setText(retName);
            retailerNameTxt.setVisibility(View.VISIBLE);
        } else {
            retailerNameTxt.setVisibility(View.GONE);
        }
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        setScreenTitle("");

        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        try {
            LinearLayout bg = (LinearLayout) view.findViewById(R.id.root);
            File f = new File(
                    getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("bg_menu");
                    }
                });
                for (File temp : files) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(temp
                            .getAbsolutePath());
                    Drawable bgrImage = new BitmapDrawable(this.getResources(), bitmapImage);
                    int sdk = Build.VERSION.SDK_INT;
                    if (sdk < Build.VERSION_CODES.JELLY_BEAN) {
                        bg.setBackgroundDrawable(bgrImage);
                    } else {
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {//check to change
                        bg.setBackground(bgrImage);
//                        }
                    }
                    break;
                }

            }
        } catch (Exception e) {
            Commons.print("" + e);
        }
        try {
            ImageView cs_homescr_img = (ImageView) view.findViewById(R.id.cs_homescr_img);
            File f = new File(
                    getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("client_banner");
                    }
                });
                if (files != null && files.length > 0) {
                    for (File temp : files) {
                        Bitmap bitmapImage = BitmapFactory.decodeFile(temp
                                .getAbsolutePath());
                        //setToolbarColor(bitmapImage);
                        cs_homescr_img.setImageBitmap(bitmapImage);
                        break;
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        menuIcons.put(MENU_STOCK_CS, R.drawable.activity_icon_stock_check);//from res
        menuIcons.put(MENU_CUSTOMER_CS, R.drawable.icon_stock);
        menuIcons.put(MENU_COUNTER_PLANOGRAM, R.drawable.icon_stock);
        menuIcons.put(MENU_PLANOGRAM_CS, R.drawable.icon_order);//home scr two
        menuIcons.put(MENU_DGT_CS, R.drawable.ic_vector_gallery);//home scr frag
        menuIcons.put(MENU_COMPETITOR_CS, R.drawable.icon_competitor);//home scr two
        menuIcons.put(MENU_CS_STOCK_APPLY, R.drawable.icon_order);//load mgt frag
        menuIcons.put(MENU_POSM_CS, R.drawable.icon_survey);//home scr two
        menuIcons.put(StandardListMasterConstants.MENU_CS_RPT,//Counter Sales Report (for Loreal project)
                R.drawable.icon_reports);

        // Load the HHTTable
        menuDB = bmodel.configurationMasterHelper
                .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_COUNTER);
        mTempMenuList = new Vector<>(menuDB);
        // menuDB.add(new ConfigureBO(MENU_CLOSE_CS, "Close", "", 150, 1, 1));
        for (ConfigureBO menu : mTempMenuList) {
            if (menu.getConfigCode().equalsIgnoreCase(MENU_CLOSE_CS)) {
                mTempMenuList.remove(menu);
            }
        }
        try {
            int length = bmodel.retailerMasterBO.getRetailerName().indexOf("/");
            if (length == -1)
                length = bmodel.retailerMasterBO.getRetailerName().length();
            title = bmodel.retailerMasterBO.getRetailerName().substring(0,
                    length);
        } catch (Exception e) {
            // TODO: handle exception
        }

        collapsingToolbar =
                (CollapsingToolbarLayout) view.findViewById(R.id.collapse_toolbar);
        collapsingToolbar.setTitleEnabled(false);
        AppBarLayout MyAppbar = (AppBarLayout) view.findViewById(R.id.MyAppbar);

        MyAppbar.addOnOffsetChangedListener(this);

        updateMenuVisitStatus();
        return view;

    }

    @Override
    public void onStart() {
        super.onStart();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isClick = false;
        isCreated = false;

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        mActivityDoneCount.setText(new DecimalFormat("0").format(getMenuVisitCount(mTempMenuList)));
        mActivityTotalCount.setText(String.valueOf("/" + mTempMenuList.size()));

        mSchedule = new IconicAdapter(menuDB);
        activityList.setAdapter(mSchedule);
    }

    private int getMenuVisitCount(Vector<ConfigureBO> list) {
        int mMneuDoneCount = 0;
        for (ConfigureBO menu : list) {
            if (menu.isDone())
                mMneuDoneCount++;
        }
        return mMneuDoneCount;
    }

    private void updateMenuVisitStatus() {
        bmodel.isModuleDone();
        int size = menuDB.size();
        for (int i = 0; i < size; i++) {
            if (menuDB.get(i).getConfigCode().equals(MENU_STOCK_CS)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.mCounterSalesHelper.isClosingStockDone())
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_CUSTOMER_CS)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.mCounterSalesHelper.isCustomerVisitkDone())
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_COUNTER_PLANOGRAM)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.mCounterSalesHelper.isCounterPlanogramDone())
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_PLANOGRAM_CS)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.mCounterSalesHelper.isPlanogramDone())
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_DGT_CS)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if ("Y".equals
                            (bmodel.getRetailerMasterBO().getIsDigitalContent()))
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_COMPETITOR_CS)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.mCounterSalesHelper.isCompetitorTrackingDone())
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_POSM_CS)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            } else if (menuDB.get(i).getConfigCode().equals(MENU_CS_STOCK_APPLY)) {
                if (menuDB.get(i).getHasLink() == 1) {
                    if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode()))
                        menuDB.get(i).setDone(true);
                } else {
                    if (getPreviousMenuBO(menuDB.get(i)).isDone())
                        menuDB.get(i).setDone(true);
                }
            }
        }
    }

    private ConfigureBO getPreviousMenuBO(ConfigureBO config) {

        try {
            for (int i = 0; i < menuDB.size(); i++) {
                if (menuDB.get(i).getConfigCode()
                        .equals(config.getConfigCode())) {
                    if (!menuDB.get(i - 1).getConfigCode().equals(MENU_CLOSE_CS)) {
                        return menuDB.get(i - 1);
                    }
                }
            }
        } catch (Exception e) {
            return menuDB.get(0);
        }
        return null;
    }


    private boolean isPreviousDone(ConfigureBO config) {

        try {
            for (int i = 0; i < menuDB.size(); i++) {
                if (menuDB.get(i).getConfigCode()
                        .equals(config.getConfigCode())) {
                    Commons.print("prev" + menuDB.get(i).getConfigCode() + "i="
                            + i);
                    for (int j = 0; j < i; j++) {
                        if (menuDB.get(j).getMandatory() == 0
                                && !menuDB.get(j).isDone()
                                && menuDB.get(j).getHasLink() == 1) {

                            return false;
                        }

                    }
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }

    private void loadCustomerVisitScreen() {

        bmodel.mCounterSalesHelper.getConcernRaised();
        Intent i = new Intent(getActivity(), CustomerVisitActivity.class);
        startActivity(i);
//        getActivity().finish();
    }


    private void showDrafts() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        ArrayList<String> lst = new ArrayList<>();

        adapter.add("New Customer");
        for (CounterSaleBO bo : bmodel.mCounterSalesHelper.getLstDraft()) {
            adapter.add(bo.getCustomerName() + " - " + bo.getContactNumber());
        }
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(adapter, 0, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int pos) {
                bmodel.mCounterSalesHelper.setUid(bmodel.userMasterHelper.getUserMasterBO().getUserid() + "" + bmodel.retailerMasterBO.getRetailerID() + "" + SDUtil.now(SDUtil.DATE_TIME_ID));
                if (pos != 0) {
                    bmodel.setCounterSaleBO(bmodel.mCounterSalesHelper.getLstDraft().get(pos - 1));
                    // load answers for selected draft
                    surveyHelperNew.loadCSSurveyAnswers(0, bmodel.getCounterSaleBO().getLastUid());
                } else {
                    bmodel.setCounterSaleBO(null);
                }

                loadCustomerVisitScreen();
                dialogInterface.dismiss();
            }
        });


        bmodel.applyAlertDialogTheme(builder);
    }


    private void gotoNextActivity(ConfigureBO menu, int hasLink) {


        if (menu.getConfigCode().equals(MENU_STOCK_CS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {

                bmodel.mSelectedActivityName = menu.getMenuName();

                new PrepareDataForStockCheck().execute();

            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_CUSTOMER_CS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    || menu.getModule_Order() == 1) {
                bmodel.mCounterSalesHelper.downloadDrafts();

                // for cs survey
                surveyHelperNew.downloadModuleId("STANDARD");
                surveyHelperNew.downloadQuestionDetails("MENU_SURVEY_CS");

                // Load Data for Special Filter
                bmodel.configurationMasterHelper.downloadFilterList();

                if (bmodel.mCounterSalesHelper.getLstDraft().size() > 0) {
                    isCreated = false;
                    showDrafts();
                } else {
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    bmodel.setCounterSaleBO(null);
                    bmodel.mCounterSalesHelper.setUid(bmodel.userMasterHelper.getUserMasterBO().getUserid() + "" + bmodel.retailerMasterBO.getRetailerID() + "" + SDUtil.now(SDUtil.DATE_TIME_ID));
                    loadCustomerVisitScreen();
                }
            }
        } else if (menu.getConfigCode().equalsIgnoreCase(MENU_COUNTER_PLANOGRAM) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                try {
                    PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
                    mPlanoGramHelper.mSelectedActivityName = menu.getMenuName();
                    mPlanoGramHelper.loadConfigurations();
                    int counterId = bmodel.getCounterId();
                    mPlanoGramHelper.downloadCounterPlanoGram(counterId);
                    mPlanoGramHelper.loadPlanoGramInEditMode(counterId);

                    if (mPlanoGramHelper.getCsPlanogramMaster() != null && mPlanoGramHelper.getCsPlanogramMaster().size() > 0) {
                        Intent in = new Intent(getActivity(),
                                CounterPlanogramActivity.class);
                        in.putExtra("from", "3");
                        in.putExtra("counterId", counterId);
                        startActivity(in);
//                        getActivity().finish();
                    } else {
                        dataNotMapped();
                        isCreated = false;
                        return;
                    }
                } catch (Exception e) {
                    Commons.print("" + e);
                    isCreated = false;
                    Toast.makeText(getActivity(),
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } else if (menu.getConfigCode().equals(MENU_POSM_CS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(getActivity());

                assetTrackingHelper.loadDataForAssetPOSM(MENU_POSM_CS);

                bmodel.mSelectedActivityName = menu.getMenuName();

                bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                        SDUtil.now(SDUtil.DATE_GLOBAL),
                        SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                Intent i = new Intent(getActivity(),
                        PosmTrackingActivity.class);
                i.putExtra("CurrentActivityCode", menu.getConfigCode());
                i.putExtra("screentitle", menu.getMenuName());
                startActivity(i);
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_PLANOGRAM_CS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                PlanoGramHelper mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
                mPlanoGramHelper.loadConfigurations();
                mPlanoGramHelper.mSelectedActivityName = menu.getMenuName();
                bmodel.productHelper.downloadProductFilter(MENU_PLANOGRAM_CS);
                mPlanoGramHelper.downloadLevels(MENU_PLANOGRAM_CS,
                        bmodel.retailerMasterBO.getRetailerID());
                mPlanoGramHelper.downloadMaster(MENU_PLANOGRAM_CS);
                mPlanoGramHelper
                        .loadPlanoGramInEditMode(bmodel.retailerMasterBO
                                .getRetailerID());
                if (mPlanoGramHelper.getPlanogramMaster() != null && mPlanoGramHelper.getPlanogramMaster().size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    int counterId = bmodel.getCounterId();
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    Intent in = new Intent(getActivity(),
                            PlanoGramActivity.class);
                    in.putExtra("from", "3");
                    in.putExtra("counterId", counterId);
                    startActivity(in);
//                    getActivity().finish();
                } else {
                    dataNotMapped();
                    isCreated = false;
                }
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_DGT_CS) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(getActivity());

                mDigitalContentHelper.downloadDigitalContent("COUNTER");
                if (mDigitalContentHelper.getDigitalMaster() != null
                        && mDigitalContentHelper.getDigitalMaster()
                        .size() > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());
                    int counterId = bmodel.getCounterId();
                    Intent i = new Intent(getActivity(),
                            DigitalContentActivity.class);
                    i.putExtra("FromInit", "DigiCS");
                    i.putExtra("counterId", counterId);
                    i.putExtra("screentitle", menu.getMenuName());
                    startActivity(i);
//                    getActivity().finish();
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.data_not_mapped),
                            0);
                    isCreated = false;
                }
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_COMPETITOR_CS)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP
                    ) {
                bmodel.competitorTrackingHelper.downloadCompanyMaster(MENU_COMPETITOR_CS);
                bmodel.competitorTrackingHelper.downloadTrackingList();
                bmodel.competitorTrackingHelper
                        .downloadCompetitors(MENU_COMPETITOR_CS);
                bmodel.competitorTrackingHelper.loadcompetitors();
                int companySize = bmodel.competitorTrackingHelper
                        .getCompanyList().size();
                if (companySize > 0) {
                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME),
                            MENU_COMPETITOR_CS);
                    int counterId = bmodel.getCounterId();
                    Intent intent = new Intent(getActivity(),
                            CompetitorTrackingActivity.class);
                    intent.putExtra("from", "3");
                    intent.putExtra("counterId", counterId);
                    startActivity(intent);
//                    getActivity().finish();
                } else {
                    dataNotMapped();
                    isCreated = false;
                }
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } else if (menu.getConfigCode().equals(MENU_CLOSE_CS)
                && hasLink == 1) {
            bmodel.mCounterSalesHelper.downloadDrafts();
            if (bmodel.mCounterSalesHelper.getLstDraft().size() > 0) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle(R.string.do_u_want_discard_draft);
                builder.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        bmodel.mCounterSalesHelper.discardDraft();
                        bmodel.setCounterId(0);
                        Intent intent = new Intent(getActivity(),
                                HomeScreenActivity.class);
                        intent.putExtra("menuCode", "MENU_COUNTER");
                        intent.putExtra("title", "");
                        startActivity(intent);
//                        getActivity().finish();
                        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                    }
                });
                builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        isCreated = false;
                    }
                });
                builder.show();
            } else {
                bmodel.setCounterId(0);
                Intent i = new Intent(getActivity(),
                        HomeScreenActivity.class);
                i.putExtra("menuCode", "MENU_COUNTER");
                i.putExtra("title", "");
                startActivity(i);
//                getActivity().finish();
                getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }

        } else if (menu.getConfigCode().equals(MENU_CS_STOCK_APPLY)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                bmodel.mSelectedActivityName = menu.getMenuName();
                new PrepareDataForStockApply().execute();
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        }

   /*     else if (menu.getConfigCode().equals(StandardListMasterConstants.MENU_CS_RPT)
                && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {
                bmodel.mCounterSalesHelper.loadVisitedCustomer();
                if (bmodel.mCounterSalesHelper.getCSCustomerVisitedUID().size() >= 1) {
                    Intent intent = new Intent(getActivity(), CsReportActivity.class);
                    bmodel.mSelectedActivityName=menu.getMenuName();
                    startActivity(intent);
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
                }

            } else {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }
        } */
        else {

            isCreated = false;
        }
    }

    class PrepareDataForStockApply extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_STOCK_APPLY_CS");
                bmodel.productHelper
                        .downloadProductsWithFiveLevelFilter("MENU_STOCK_APPLY_CS");
            } else {
                bmodel.productHelper.downloadProductFilter("MENU_STOCK_APPLY_CS");
                bmodel.productHelper.downloadProducts("MENU_STOCK_APPLY_CS");
            }

            bmodel.CS_StockApplyHelper.loadStockDetails();
            bmodel.CS_StockApplyHelper.loadStockType();


            bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    SDUtil.now(SDUtil.TIME), "MENU_STOCK_APPLY_CS");


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            alertDialog.dismiss();


            Intent intent = new Intent(getActivity(),
                    CS_StockApply.class);
            startActivity(intent);
            //finish();

        }
    }

    public void dataNotMapped() {
        Toast.makeText(getActivity(),
                getResources().getString(R.string.data_not_mapped),
                Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        if (scrollRange == -1) {
            scrollRange = appBarLayout.getTotalScrollRange();
        }
        if (scrollRange + verticalOffset == 0) {
            if (retName != null && !retName.isEmpty()) {
                setScreenTitle(retName);
            } else {
                setScreenTitle("");
            }
            isVisible = true;
        } else if (isVisible) {
            setScreenTitle("");
            isVisible = false;
        }
    }


    class IconicAdapter extends ArrayAdapter<ConfigureBO> {

        Vector<ConfigureBO> items;

        private IconicAdapter(Vector<ConfigureBO> menuDB) {
            super(getActivity(), R.layout.homescreentwo_listitem, menuDB);
            this.items = menuDB;
        }

        public ConfigureBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            ConfigureBO configTemp = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.homescreentwo_listitem,
                        parent, false);
                holder = new ViewHolder();
                holder.ioncLty = (LinearLayout) convertView.findViewById(R.id.icon_ll);
                holder.iconIV = (ImageView) convertView
                        .findViewById(R.id.list_item_icon_iv);
               /* holder.numIcon = (TextView) convertView
                        .findViewById(R.id.menu_number_tv);*/
                holder.activityname = (TextView) convertView
                        .findViewById(R.id.activityName);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.activityname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.position = position;
            holder.config = configTemp;
            //holder.numIcon.setText(""+configTemp.getMenuNumber());
            holder.activityname.setText("" + configTemp.getMenuName());
            holder.menuCode = configTemp.getConfigCode();
            holder.hasLink = configTemp.getHasLink();

            if (configTemp.getHasLink() == 0) {
                holder.activityname
                        .setBackgroundResource(R.drawable.list_menu_right_bg_normal_gray);
                holder.activityname.setTextColor(getResources().getColor(
                        android.R.color.black));

            } else {
                /*holder.activityname
                        .setBackgroundColor(typearr.getColor(R.styleable.MyTextView_primarycolor, 0));*/
                /*holder.activityname.setTextColor(getResources().getColor(
                        android.R.color.white));*/
            }

            Integer i = menuIcons.get(configTemp.getConfigCode());
            if (i != null)
                holder.iconIV.setImageResource(i);
            else
                holder.iconIV.setImageResource(menuIcons.get(MENU_STOCK_CS));


            if (holder.config.isDone()) {
                holder.ioncLty.setBackgroundResource(R.drawable.activity_icon_bg_completed);
                holder.iconIV.setColorFilter(Color.argb(255, 255, 255, 255));

            } else {
                holder.ioncLty.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.iconIV.setColorFilter(Color.argb(0, 0, 0, 0));
            }

            convertView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (!isCreated) {
                        isCreated = true;
                        gotoNextActivity(holder.config, holder.hasLink);
                    }
                }
            });


            return convertView;
        }

        class ViewHolder {
            ConfigureBO config;
            String menuCode;
            int position;
            LinearLayout ioncLty;
            ImageView iconIV;
            //TextView numIcon;
            TextView activityname;
            int hasLink;
        }

    }

    class PrepareDataForStockCheck extends AsyncTask<String, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(String... params) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels(MENU_STOCK_CS);
                bmodel.productHelper
                        .downloadProductsWithFiveLevelFilter(MENU_STOCK_CS);
            } else {
                bmodel.productHelper.downloadProductFilter(MENU_STOCK_CS);
                bmodel.productHelper.downloadProducts(MENU_STOCK_CS);
            }


            /** Download location to load in the filter. **/
            bmodel.CS_StockApplyHelper.loadStockType();

            bmodel.productHelper.downloadTaggedProducts(MENU_STOCK_CS);
            bmodel.CS_StockApplyHelper.updateCSSihForStockCheck();

            bmodel.reasonHelper.downloadCSStockReasons();
            bmodel.mCounterSalesHelper.updateStockReasons();


            // Load Data for Special Filter
            bmodel.configurationMasterHelper.downloadFilterList();


            bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                    SDUtil.now(SDUtil.DATE_GLOBAL),
                    SDUtil.now(SDUtil.TIME), MENU_STOCK_CS);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            alertDialog.dismiss();


            Intent intent = new Intent(getActivity(),
                    CS_StockCheckFragmentActivity.class);
            startActivity(intent);

        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                return true;
        }
        return super.onOptionsItemSelected(item);

    }

}
