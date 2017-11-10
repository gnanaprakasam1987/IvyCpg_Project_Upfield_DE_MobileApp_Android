package com.ivy.sd.png.survey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SurveyHelperNew;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.PhotoCaptureActivity;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.SlantView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * IsExclude score will not work/applicable for multi select questions.
 */
public class SurveyActivityNewFragment extends IvyBaseFragment implements TabLayout.OnTabSelectedListener, BrandDialogInterface, OnClickListener {

    private BusinessModel bmodel;

    private int tabPos;
    private int tabCount;
    private String imageName = "", pathSrc = "";
    ;
    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int DRAG_AND_DROP = 2;
    private boolean isClicked = true;
    private QuestionBO surveyPhcapture = new QuestionBO();
    private SurveyBO surveyBO = new SurveyBO();

    private DrawerLayout mDrawerLayout;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();

    private ListView questionsListView;
    private ListAdapter listAdapter;

    private ArrayList<QuestionBO> mQuestions;

    private QuestionBO questBO;

    private AlertDialog alertDialog;
    private ArrayAdapter<UserMasterBO> supervisiorAdapter;
    private int selecteditem = 0;
    private int mSurveyType;
    private String mMenuCode = "";
    private int screenMode = 0;

    // For Computing score

    private TextView surveyScoreTextView, overAllSurveyScoreTextView;
    private LinearLayout surveyScoreLinearLayout, surveyScoreOverAllLinearLayout;

    private String path = "";
    private boolean isSaveClicked;
    private boolean checkClicked;
    private int index;
    private int top;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private Vector<LevelBO> mFinalParentIdList;
    private TypedArray typearr;
    private boolean isViewMode;


    private TabLayout tabLayout;
    private Button saveButton;
    private ImageView imgView;
    private boolean isNext = false;
    private String mFrom = "";

    private AlertDialog objDialog = null;
    private boolean hide_selectuser_icon = false;
    private ArrayList<StandardListBO> childList;
    private ArrayAdapter<String> mChildUserNameAdapter;
    private int mSelectedIdIndex = -1, isFromDragDrop = -1;
    private String childUserName = "";
    private boolean isFromChild;
    protected Boolean isMultiPhotoCaptureEnabled = false;

    @Override
    public void onAttach(Context context) {

        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_new_survey, container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(R.id.drawer_layout);
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        tabLayout = (TabLayout) view.findViewById(R.id.tabs);
        tabLayout.setOnTabSelectedListener(this);

        Bundle extras = getActivity().getIntent().getExtras();
        if (extras != null) {
            isNext = extras.getBoolean("IsMoveNextActivity", false);
            mFrom = extras.getString("from") != null ? extras.getString("from") : "";
        }
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        initializeView(view);


        return view;
    }

    private void initializeView(View view) {
        questionsListView = (ListView) view.findViewById(R.id.lv_qustions);
        questionsListView.setCacheColorHint(0);

        surveyScoreTextView = (TextView) view
                .findViewById(R.id.questionScoreTV);
        surveyScoreTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        surveyScoreTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        overAllSurveyScoreTextView = (TextView) view.findViewById(R.id.surveyScoreTV);
        overAllSurveyScoreTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        overAllSurveyScoreTextView.setTextColor(ContextCompat.getColor(getActivity(), R.color.white));

        surveyScoreLinearLayout = (LinearLayout) view.findViewById(
                R.id.questionScore_ll);
        surveyScoreOverAllLinearLayout = (LinearLayout) view.findViewById(
                R.id.surveyScore_ll);
        saveButton = (Button) view.findViewById(R.id.save);
        saveButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        saveButton.setOnClickListener(this);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                setScreenTitle(bmodel.mSelectedActivityName);
                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                //if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));
                getActivity().supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        Bundle extras = getArguments();
        if (extras == null) {
            extras = getActivity().getIntent().getExtras();
        }

        try {
            if (extras != null) {
                mSurveyType = extras.getInt("SurveyType", 0);
                mMenuCode = extras.getString("menucode", "");
                screenMode = extras.getInt("screenMode", 0);
            } else {
                mSurveyType = getActivity().getIntent().getExtras().getInt("SurveyType", 0);
                mMenuCode = getActivity().getIntent().getExtras().getString("menucode", "");
                screenMode = getActivity().getIntent().getExtras().getInt("screenMode", 0);
            }
        } catch (Exception e) {
            mSurveyType = 0;
            mMenuCode = "MENU_SURVEY";
            screenMode = 0;
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isSaveClicked = false;
    }

    private void showUserDialog() {
        childList = bmodel.mAttendanceHelper.loadChildUserList();
        if (childList != null && childList.size() > 0) {
            if (childList.size() > 1) {
                showDialog();
            } else if (childList.size() == 1) {
                hide_selectuser_icon = true;
                bmodel.setSelectedUserId(childList.get(0).getChildUserId());
                loadListData();
            }
        } else {
            hide_selectuser_icon = true;
            bmodel.setSelectedUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            loadListData();
        }

    }

    private void showDialog() {
        mChildUserNameAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : childList)
            mChildUserNameAdapter.add(temp.getChildUserName());


        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select User");
        builder.setSingleChoiceItems(mChildUserNameAdapter, mSelectedIdIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedIdIndex = item;
                        bmodel.setSelectedUserId(childList.get(item).getChildUserId());
                        childUserName = childList.get(item).getChildUserName();
                        if (mMenuCode.equals("MENU_SURVEY_BA_CS"))
                            setScreenTitle(bmodel.mSelectedActivityName + " (" +
                                    childUserName + ")");
                        hide_selectuser_icon = false;
                        loadListData();
                        dialog.dismiss();
                    }
                });

        objDialog = bmodel.applyAlertDialogTheme(builder);
        objDialog.setCancelable(false);
    }


    @Override
    public void onStart() {

        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.configurationMasterHelper.ENABLE_MULTIPLE_PHOTO)
            isMultiPhotoCaptureEnabled = true;
        //condition to check CNT01
        if (!mMenuCode.equals("MENU_SURVEY_CS") && bmodel.configurationMasterHelper.IS_CNT01) {
            //if CNT01 is enabled
            if (objDialog != null) {
                if (!objDialog.isShowing()) {
//                    showUserDialog();
                }
            } else {
                showUserDialog();
            }
        } else {
            //if CNT01 is disabled
            loadListData();
        }

    }

    private void loadListData() {

        if (isFromDragDrop == -1) {
            if (!mFrom.equalsIgnoreCase("HomeScreenTwo"))
                bmodel.mSurveyHelperNew
                        .loadSurveyAnswers(0);
        }
        //  isFromDragDrop=-1;

        bmodel.mSurveyHelperNew.mSelectedFilter = -1;

        path = "/" + "Survey" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/";

        if (getView() != null)
            if (tabLayout != null) {
                tabLayout.removeAllTabs();
            }

        tabCount = bmodel.mSurveyHelperNew.getSurvey().size();

        float scale = getContext().getResources().getDisplayMetrics().widthPixels;
        scale = scale / tabCount;

        // Add a survey in Tab
        for (int i = 0; i < tabCount; i++) {
            TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);

            tabOne.setGravity(Gravity.CENTER);
            tabOne.setWidth((int) scale);

            tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            tabOne.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tabOne.setText(bmodel.mSurveyHelperNew.getSurvey().get(i)
                    .getSurveyName());
            TabLayout.Tab tab = tabLayout.newTab().setCustomView(tabOne);
            tab.setTag(""
                    + bmodel.mSurveyHelperNew.getSurvey().get(i)
                    .getSurveyID());

            if (tabLayout != null) {

                tabLayout.addTab(tab, false);
            }
            if (i == 0) {
                tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }
        }

        if (tabCount > 0) {
            tabLayout.getTabAt(tabPos).select();
        }

        bmodel.mSurveyHelperNew.highlightQuest = false;
        // configure filter
        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");

        if (mMenuCode.equalsIgnoreCase(SurveyHelperNew.cs_feedback_menucode))
            saveButton.setVisibility(View.GONE);
        if (screenMode == 1)
            isViewMode = true;

        if (mSurveyType == 1) {
            supervisiorAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_singlechoice);
            for (UserMasterBO userBo : bmodel.userMasterHelper
                    .getUserMasterBO().getJoinCallUserList()) {
                if (userBo.getIsJointCall() == 1)
                    supervisiorAdapter.add(userBo);
            }

            for (UserMasterBO user : bmodel.userMasterHelper.getUserMasterBO()
                    .getJoinCallUserList()) {
                if (user.getIsJointCall() == 1) {
                    bmodel.mSurveyHelperNew.mSelectedSuperVisiorID = user
                            .getUserid();
                    break;
                }

            }
        }

        questionsListView.setSelectionFromTop(index, top);
    }

    /**
     * Return true if currently displaying survey has question with weight.
     *
     * @return true or false
     */
    private boolean hasQuestionWithWeightSetInSurvey() {

        int questionsCount;

        if (mQuestions == null || mQuestions.isEmpty())
            return false;

        questionsCount = mQuestions.size();

        for (int i = 0; i < questionsCount; i++) {
            if (mQuestions.get(i).getQuestWeight() > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Return true if any question in the module has weight.
     *
     * @return true or false
     */
    private boolean hasQuestionWithWeightSetInOverAllSurvey() {

        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            /*if (sBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey) {*/

            ArrayList<QuestionBO> questionsList = sBO.getQuestions();
            int questionsCount;

            if (questionsList == null || questionsList.isEmpty())
                return false;

            questionsCount = questionsList.size();


            for (int i = 0; i < questionsCount; i++) {

                if (questionsList.get(i).getQuestWeight() > 0) {
                    return true;
                }
            }

            /*}*/
        }
        return false;
    }

    /**
     * Calculate survey score and update UI
     */
    private void updateSurveyScore() {
        float achievedScoreWithoutBonus = 0;
        float bonusScoreAchieved = 0;
        float targetScore = 0;

        int questionsCount = mQuestions.size();
        for (int i = 0; i < questionsCount; i++) {
            if (!mQuestions.get(i).isExcludeQuestionWeight() && mQuestions.get(i).getIsBonus() != 1) {
                achievedScoreWithoutBonus = achievedScoreWithoutBonus + mQuestions.get(i).getQuestScore();
            }

            if (mQuestions.get(i).getIsBonus() == 1) {
                bonusScoreAchieved = bonusScoreAchieved + mQuestions.get(i).getQuestScore();

            }
            targetScore = targetScore
                    + mQuestions.get(i).getQuestWeight();
        }

        if (bonusScoreAchieved > surveyBO.getMaxBonusScore())
            bonusScoreAchieved = surveyBO.getMaxBonusScore();

        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == mQuestions.get(0).getSurveyid()) {
                sBO.setAchievedScore(achievedScoreWithoutBonus + bonusScoreAchieved);
                sBO.setTargtScore(targetScore);
                sBO.setBonusScoreAchieved(bonusScoreAchieved);
            }

        }

        String overallScore = achievedScoreWithoutBonus + bonusScoreAchieved + "/" + targetScore + "";
        surveyScoreTextView.setText(overallScore);

    }

    /**
     * Calculate overall survey score and update the UI
     */
    private void updateOverAllSurveyScore() {
        float achievedScoreOverAllWithoutBonus = 0;
        float targetScoreOverAll = 0;
        float bonusScoreAchievedOverAll = 0;
        for (SurveyBO surveyBO : bmodel.mSurveyHelperNew.getSurvey()) {
            float bonusScoreAchieved = 0;
            ArrayList<QuestionBO> moduleQuestList = surveyBO.getQuestions();
            int questionsCount = moduleQuestList.size();
            for (int i = 0; i < questionsCount; i++) {
                if (!moduleQuestList.get(i).isExcludeQuestionWeight() && moduleQuestList.get(i).getIsBonus() != 1) {
                    achievedScoreOverAllWithoutBonus = achievedScoreOverAllWithoutBonus
                            + moduleQuestList.get(i).getQuestScore();
                }

                if (moduleQuestList.get(i).getIsBonus() == 1) {
                    bonusScoreAchieved = bonusScoreAchieved + moduleQuestList.get(i).getQuestScore();
                }
                targetScoreOverAll = targetScoreOverAll
                        + moduleQuestList.get(i).getQuestWeight();
            }

            if (bonusScoreAchieved > this.surveyBO.getMaxBonusScore())
                bonusScoreAchieved = this.surveyBO.getMaxBonusScore();

            bonusScoreAchievedOverAll = bonusScoreAchievedOverAll + bonusScoreAchieved;

        }
        String strOverAllScore = achievedScoreOverAllWithoutBonus + bonusScoreAchievedOverAll + "/" + targetScoreOverAll + "";
        overAllSurveyScoreTextView.setText(strOverAllScore);
    }

    /**
     * Load the Questions based on Selected Survey and Filter
     *
     * @param mSelectedSurveyId survey id
     * @param mSelectedBrand    brand id
     */
    private void onLoadQuestion(int mSelectedSurveyId, int mSelectedBrand) {

        ArrayList<QuestionBO> items = new ArrayList<>();

        for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == mSelectedSurveyId)
                items = surBO.getQuestions();

        }


        if (items == null || items.isEmpty())
            return;

        mQuestions = new ArrayList<>();

        for (QuestionBO question : items) {
            if (question.getSurveyid() == mSelectedSurveyId
                    || mSelectedSurveyId == -1) {
                if (question.getBrandID() == mSelectedBrand
                        || mSelectedBrand == -1 && question.getIsSubQuestion() == 0) {
                    mQuestions.add(question);
                }
            }
        }


        listAdapter = new QuestionAdapter();
        questionsListView.setAdapter(listAdapter);
        questionsListView.setRecyclerListener(new AbsListView.RecyclerListener() {
            @Override
            public void onMovedToScrapHeap(View view) {
                if (view.hasFocus()) {
                    view.clearFocus(); //we can put it inside the second if as well, but it makes sense to do it to all scraped views
                    //Optional: also hide keyboard in that case
                    if (view instanceof EditText) {
                        InputMethodManager imm = (InputMethodManager) view.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                }
            }
        });

        /*questionsListView.setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeRight() {
                if ((tabPos - 1) >= 0 && tabLayout != null)
                    tabLayout.getTabAt(tabPos - 1).select();
            }

            public void onSwipeLeft() {
                if ((tabPos + 1) <= tabCount && tabLayout != null)
                    tabLayout.getTabAt(tabPos + 1).select();
            }
        });*/
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {

        tabPos = tab.getPosition();
        if (tabPos != 0) {
            TabLayout.Tab tab1 = tabLayout.getTabAt(0);
            if (tab1 != null) {
                TextView text = (TextView) tab1.getCustomView();
                text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }
        TextView text = (TextView) tab.getCustomView();
        text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        bmodel.mSurveyHelperNew.mSelectedSurvey = Integer.parseInt(tab.getTag()
                .toString());

        for (SurveyBO sBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey) {
                surveyBO = sBO;
            }
        }

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mFinalParentIdList != null) {
            loadQuestionFromFiveLevelFilter(bmodel.mSurveyHelperNew.mSelectedSurvey, mFinalParentIdList);
        } else {
            onLoadQuestion(bmodel.mSurveyHelperNew.mSelectedSurvey,
                    bmodel.mSurveyHelperNew.mSelectedFilter);
        }
        /* Show or hide footer which display survey score and overall score*/
        if (bmodel.configurationMasterHelper.SHOW_TOTAL_SCORE_IN_SURVEY) {

            // Sometime, one survey may have score but other survey may not.
            // So we are checking data before displaying score.
            if (hasQuestionWithWeightSetInSurvey()) {
                surveyScoreLinearLayout.setVisibility(View.VISIBLE);
                updateSurveyScore();
                surveyScoreOverAllLinearLayout.setVisibility(View.VISIBLE);
                updateOverAllSurveyScore();
            } else {
                surveyScoreLinearLayout.setVisibility(View.INVISIBLE);
                if (hasQuestionWithWeightSetInOverAllSurvey()) {
                    surveyScoreOverAllLinearLayout.setVisibility(View.VISIBLE);
                    updateOverAllSurveyScore();
                } else {
                    surveyScoreOverAllLinearLayout.setVisibility(View.INVISIBLE);
                }
            }
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
        TextView text = (TextView) tab.getCustomView();
        text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void onClick(View view) {
        if (view == saveButton) {
            if (!checkClicked) {
                checkClicked = true;
                if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_ALL) {
                    if (bmodel.mSurveyHelperNew.isAllAnswered()) {
                        if (bmodel.mSurveyHelperNew.hasPhotoToSave())
                            new SaveSurveyTask().execute();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsListView.setAdapter(listAdapter);
                            checkClicked = false;
                        }
                    } else {
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.pleaseanswerallthequestions), 0);
                        checkClicked = false;
                    }
                } else if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_MANDATORY) {
                    if (bmodel.mSurveyHelperNew.isMandatoryQuestionAnswered()) {

                        if (bmodel.mSurveyHelperNew.hasPhotoToSave())
                            new SaveSurveyTask().execute();
                        else {

                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsListView.setAdapter(listAdapter);
                            checkClicked = false;
                        }
                    } else {
                        isSaveClicked = true;
                        questionsListView.setAdapter(listAdapter);
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.please_answer_all_mandatory_questions),
                                0);
                        checkClicked = false;
                    }
                } else {


                    if (bmodel.mSurveyHelperNew.hasDataToSave()) {
                        if (bmodel.mSurveyHelperNew.hasPhotoToSave())
                            new SaveSurveyTask().execute();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsListView.setAdapter(listAdapter);
                            checkClicked = false;
                        }
                    } else {
                        bmodel.showAlert(
                                getResources().getString(R.string.no_data_tosave), 0);
                        questionsListView.setAdapter(listAdapter);
                        checkClicked = false;
                    }
                }
            }
        }

    }

    class QuestionAdapter extends BaseAdapter {


        public QuestionAdapter() {
        }

        @Override
        public int getCount() {
            return mQuestions.size();
        }

        @Override
        public Object getItem(int position) {
            return mQuestions.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View view, ViewGroup arg2) {
            final QuestionHolder holder;
            View row = view;
            if (row == null) {

                holder = new QuestionHolder();
                LayoutInflater inflater = getLayoutInflater(null);

                row = inflater.inflate(R.layout.questionandanswerset, arg2, false);
                holder.photoLayout = (LinearLayout) row
                        .findViewById(R.id.ll_photo);

                holder.right_container = (RelativeLayout) row
                        .findViewById(R.id.right_container);

                holder.groupNameLayout = (LinearLayout) row
                        .findViewById(R.id.ll_groupname);

                holder.questionNO = (TextView) row
                        .findViewById(R.id.questionno);
                holder.questionTV = (TextView) row
                        .findViewById(R.id.questionTV);
                holder.questionTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.questionNO.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.groupName = (TextView) row
                        .findViewById(R.id.groupname);
                holder.groupName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

                holder.minPhoto = (TextView) row
                        .findViewById(R.id.minphotoTV);
                holder.minPhoto.setTextColor(ContextCompat.getColor(getActivity(), R.color.dead_store_score));
                holder.imp = (TextView) row.findViewById(R.id.imp);


                holder.QscoreTV = (TextView) row.findViewById(R.id.scoreTV);
                holder.QscoreTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.dead_store_score));
                holder.QscoreTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.answerLayout = (LinearLayout) row
                        .findViewById(R.id.answerLL);
                holder.subQuestLayout = (LinearLayout) row
                        .findViewById(R.id.subqLL);

                holder.camBtn = (ImageView) row.findViewById(R.id.imgBtn);
                holder.tv_counter = (TextView) row.findViewById(R.id.textOne);
                holder.photoBtn = (ImageView) row.findViewById(R.id.photos);
                holder.dragBtn = (ImageView) row.findViewById(R.id.dragDropIcon);
                holder.dragDropLayout = (LinearLayout) row.findViewById(R.id.dragDropLayout);
                holder.camIndicatorLty = (LinearLayout) row.findViewById(R.id.indicator_view);
                holder.minPhoto.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                holder.mandatoryView = (RelativeLayout) row.findViewById(R.id.mandatory_view);
                holder.slantView = (SlantView) row.findViewById(R.id.slant_view_bg);

                holder.questionBO = mQuestions.get(position);


                holder.camBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgView = holder.camBtn;
                        photoFunction(holder.questionBO, 0);
                    }
                });

                holder.photoBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        imgView = holder.photoBtn;
                        photoFunction(holder.questionBO, 1);
                    }
                });

                holder.dragBtn.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
                        surveyHelperNew.setQuestionBODragDrop(holder.questionBO);
                        surveyPhcapture = holder.questionBO;

                        Intent intent = new Intent(getActivity(), DragDropPictureActivity.class);
                        intent.putExtra("BrandId", holder.questionBO.getBrandID());
                        intent.putExtra("QuestiionId", holder.questionBO.getQuestionID());
                        intent.putExtra("QuestionDesc", holder.questionBO.getQuestionDescription());
                        intent.putExtra("SurveyId", bmodel.mSurveyHelperNew.mSelectedSurvey);
                        startActivityForResult(intent, DRAG_AND_DROP);
                    }
                });


                row.setTag(holder);
            } else {
                holder = (QuestionHolder) row.getTag();
            }

            holder.questionBO = mQuestions.get(position);
            if (position % 2 == 0) {
                holder.slantView.setColor(Color.WHITE);
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            } else {
                holder.slantView.setColor(ContextCompat.getColor(getActivity(), R.color.row_alternate_grey));
                row.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.row_alternate_grey));
            }

            if ("".equals(holder.questionBO.getGroupName())) {
                holder.groupNameLayout.setVisibility(View.GONE);
            } else {

                holder.groupNameLayout.setVisibility(View.VISIBLE);
                holder.groupName.setText(holder.questionBO.getGroupName());
            }
            int qNo = position + 1;
            String strQuestionNo = qNo + ". ";
            holder.questionNO.setText(strQuestionNo);
            holder.questionTV.setText(holder.questionBO.getQuestionDescription());

            if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_MANDATORY && isSaveClicked) {
                if (holder.questionBO.getIsMandatory() == 1 && holder.questionBO.isMandatoryQuestNotAnswered()) {
                    holder.mandatoryView.setVisibility(View.VISIBLE);
                } else {
                    holder.mandatoryView.setVisibility(View.GONE);
                }
            } else {
                holder.mandatoryView.setVisibility(View.GONE);
            }


            if (holder.questionBO.getIsBonus() == 1)
                holder.questionTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary));
            else
                holder.questionTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.list_item_values_text_color));
            holder.QscoreTV.setTag(holder.questionBO);

            int strminPhoto = holder.questionBO.getMinPhoto();

            if (strminPhoto == 1)
                holder.minPhoto.setText(strminPhoto + " Photo Required");
            else if (strminPhoto > 1)
                holder.minPhoto.setText(strminPhoto + " Photos Required");
            else
                holder.minPhoto.setText("0 Photo  required");


            if (holder.questionBO.getIsScore() == 1 && bmodel.configurationMasterHelper.SHOW_SCORE_IN_SURVEY) {
                holder.QscoreTV.setVisibility(View.VISIBLE);
                holder.isScoreAvailable = true;
            } else {
                holder.QscoreTV.setVisibility(View.INVISIBLE);
                holder.isScoreAvailable = false;
            }

            if (holder.questionBO.getIsPhotoReq() == 0) {
                holder.isPhotoAvailable = false;
                holder.photoLayout.setVisibility(View.INVISIBLE);
                //Drag and Drop Functionality Disabled
                holder.camIndicatorLty.setVisibility(View.GONE);
                holder.dragBtn.setVisibility(View.GONE);
                holder.dragDropLayout.setVisibility(View.GONE);
            } else {
                holder.isPhotoAvailable = true;
                holder.photoLayout.setVisibility(View.VISIBLE);
                //Drag and Drop Functionality Enabled
                holder.camIndicatorLty.setVisibility(View.VISIBLE);
                holder.dragBtn.setVisibility(View.VISIBLE);
                holder.dragDropLayout.setVisibility(View.VISIBLE);
                if (holder.questionBO.getMinPhoto() == 1 && holder.questionBO.getMaxPhoto() == 1) {
                    holder.camBtn.setVisibility(View.VISIBLE);
                    holder.camIndicatorLty.setVisibility(View.GONE);
                    holder.photoBtn.setVisibility(View.GONE);
                } else if ((holder.questionBO.getMinPhoto() == 2 && holder.questionBO.getMaxPhoto() == 2) ||
                        (holder.questionBO.getMinPhoto() == 4 && holder.questionBO.getMaxPhoto() == 4)) {
                    holder.camBtn.setVisibility(View.VISIBLE);
                    /*holder.camIndicatorLty.setVisibility(View.VISIBLE);
                    holder.photoBtn.setVisibility(View.VISIBLE);*/
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_DRAGDROP_IN_SURVEY) {
                //Drag and Drop Functionality Disabled
                holder.camIndicatorLty.setVisibility(View.GONE);
                holder.dragBtn.setVisibility(View.GONE);
                holder.dragDropLayout.setVisibility(View.GONE);
            }

            if (holder.questionBO.getIsPhotoReq() != 0 && holder.questionBO.getMinPhoto() >= 1) {
                holder.minPhoto.setVisibility(View.VISIBLE);
            } else {
                holder.minPhoto.setVisibility(View.INVISIBLE);
            }


            if (holder.questionBO.getImageNames().size() > 0) {
                holder.tv_counter.setVisibility(View.VISIBLE);

                //---------- remove duplicate image  name  from given list-----------//

                for (int i = 0; i < holder.questionBO.getImageNames().size(); i++) {

                    for (int j = i + 1; j < holder.questionBO.getImageNames().size(); j++) {
                        if (holder.questionBO.getImageNames().get(i).toString().equalsIgnoreCase(holder.questionBO.getImageNames().get(j).toString())) {
                            holder.questionBO.getImageNames().remove(j);
                            j--;
                        }
                    }
                }
                String imageName = holder.questionBO.getImageNames().size() + "";
                holder.tv_counter.setText(imageName);
                holder.camBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
            } else {
                holder.tv_counter.setVisibility(View.GONE);
            }

            if (holder.isPhotoAvailable || holder.isScoreAvailable) {
                holder.right_container.setVisibility(View.VISIBLE);
            } else {
                holder.right_container.setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_MANDATORY
                    && holder.questionBO.getIsMandatory() == 1)
                holder.imp.setVisibility(View.VISIBLE);
            else
                holder.imp.setVisibility(View.GONE);

            switch (holder.questionBO.getQuestionType()) {
                case "OPT":
                    showRadioGroup(holder.answerLayout, holder.questionBO, holder.QscoreTV, holder.subQuestLayout, holder.questionNO.getText().toString());
                    break;
                case "MULTISELECT":
                    showCheckBox(holder.answerLayout, holder.questionBO, holder.QscoreTV, holder.subQuestLayout, holder.questionNO.getText().toString());
                    break;
                case "POLL":
                    showSpinner(holder.answerLayout, holder.questionBO, holder.QscoreTV, holder.subQuestLayout, holder.questionNO.getText().toString());
                    break;
                case "NUM":
                    showEditText(1, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
                    break;
                case "PERC":
                    showEditText(2, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
                    break;
                default:
                    showEditText(0, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
            }

            return row;
        }
    }


    class QuestionHolder {
        QuestionBO questionBO;
        LinearLayout answerLayout;
        LinearLayout groupNameLayout, camIndicatorLty, dragDropLayout;
        LinearLayout subQuestLayout;
        RelativeLayout right_container;
        TextView questionNO;
        TextView questionTV;
        TextView tv_counter;
        TextView imp;
        ImageView camBtn, photoBtn, dragBtn;
        LinearLayout photoLayout;
        TextView groupName;
        TextView minPhoto;
        TextView QscoreTV;
        boolean isScoreAvailable, isPhotoAvailable;
        RelativeLayout mandatoryView;
        SlantView slantView;
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
//                ArrayList<String> mImagePathData=new ArrayList<>();
//                if(surveyPhcapture.getImagePathData()==null)
//                {
//                    mImagePathData.add(pathSrc);
//                    surveyPhcapture.setImagePathData(mImagePathData);
//                }
//                else
//                {
//                    mImagePathData=surveyPhcapture.getImagePathData();
//                    mImagePathData.add(pathSrc);
//                    surveyPhcapture.setImagePathData(mImagePathData);
//                }

                surveyPhcapture.getImageNames().add(path + imageName);
                if (isMultiPhotoCaptureEnabled) {
                    Toast.makeText(getActivity(), "Photo Captured and Saved Successfully", Toast.LENGTH_SHORT).show();
                    isClicked = true;
                    if (surveyPhcapture.getImageNames().size() < surveyPhcapture.getMaxPhoto()) {
                        photoFunction(surveyPhcapture, 0);
                    }
                }
            }
        } else if (requestCode == DRAG_AND_DROP) {
            if (resultCode == Activity.RESULT_OK) {
                ArrayList<String> mSavedData = data.getStringArrayListExtra("savedData");
                if (mSavedData != null && mSavedData.size() != 0) {
                    surveyPhcapture.getImageNames().clear();
                    surveyPhcapture.getImageNames().addAll(mSavedData);
                } else if (mSavedData != null && mSavedData.size() == 0) {
                    surveyPhcapture.getImageNames().clear();
                }
                isFromDragDrop = 1;
            }
//            SurveyHelperNew surveyHelperNew= SurveyHelperNew.getInstance(getActivity());
//            questBO=surveyHelperNew.getQuestionBODragDrop();
        }
        isClicked = true;

    }

    private void showRadioGroup(LinearLayout answerLL,
                                final QuestionBO mCurrentQuestionBO, final TextView qScore, final LinearLayout subQuestionLL, final String qNO) {
        try {
            answerLL.removeAllViews();
            RadioGroup mRadioGroup = new RadioGroup(getActivity());
            mRadioGroup.setOrientation(RadioGroup.VERTICAL);
            mRadioGroup.setPadding(0, 0, 55, 0);

            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params1.bottomMargin = 0;

            final ArrayList<AnswerBO> answers = mCurrentQuestionBO.getAnswersList();
            int answerCount = answers.size();

            boolean checked = false;
            for (int i = 0; i < answerCount; i++) {

                final RadioButton radioButton = new RadioButton(getActivity());
                radioButton.setId(answers.get(i).getAnswerID());
                radioButton.setText(answers.get(i).getAnswer());
                radioButton.setTextColor(ContextCompat.getColor(getActivity(), R.color.FullBlack));
                radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_micro_small));

                if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(
                        answers.get(i).getAnswerID())) {
                    radioButton.setChecked(true);
                    checked = true;
                } else {
                    radioButton.setChecked(false);
                }

                if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(
                        answers.get(i).getAnswerID())) {
                    if (answers.get(i).isExcluded()) {
                        mCurrentQuestionBO.setQuestScore(0);
                        mCurrentQuestionBO.setExcludeQuestionWeight(answers.get(i)
                                .isExcluded());
                    } else {

                        mCurrentQuestionBO.setQuestScore(answers.get(i).getScore());
                        mCurrentQuestionBO.setExcludeQuestionWeight(false);
                    }
                }

                radioButton
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(CompoundButton buttonView,
                                                         boolean isChecked) {
                                View view = getView();
                                if (view != null) {
                                    InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                }
                                if (isChecked) {
                                    subQuestionLL.removeAllViews();
                                    int id = buttonView.getId();

                                    if (!mCurrentQuestionBO.getSelectedAnswerIDs().isEmpty()) {
                                        int tempAnsSize = answers.size();
                                        for (int i = 0; i < tempAnsSize; i++) {
                                            if (answers.get(i).getAnswerID() == mCurrentQuestionBO
                                                    .getSelectedAnswerIDs().get(0)) {
                                                int tempQuestListSize = answers.get(i).getQuestionList().size();
                                                for (int j = 0; j < tempQuestListSize; j++) {
                                                    String s = answers.get(i).getQuestionList().get(j) + "";
                                                    if (!"0".equals(s)) {
                                                        removeQID(answers.get(i).getQuestionList().get(j));
                                                    }

                                                }
                                            }
                                        }
                                    }
                                    mCurrentQuestionBO.getSelectedAnswerIDs()
                                            .clear();
                                    mCurrentQuestionBO.getSelectedAnswer()
                                            .clear();
                                    mCurrentQuestionBO.setSelectedAnswerID(id);
                                    mCurrentQuestionBO.setSelectedAnswer(radioButton.getText().toString());

                                    int tempAnsSize1 = answers.size();
                                    for (int i = 0; i < tempAnsSize1; i++) {
                                        if (answers.get(i).getAnswerID() == mCurrentQuestionBO
                                                .getSelectedAnswerIDs().get(0)) {
                                            int tempQuestListSize2 = answers.get(i).getQuestionList().size();
                                            for (int j = 0; j < tempQuestListSize2; j++) {
                                                String s = answers.get(i).getQuestionList().get(j) + "";
                                                if (!"0".equals(s)) {
                                                    checkQID(answers.get(i).getQuestionList().get(j));
                                                    generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                                                }

                                            }
                                        }
                                    }
                                }
                                if (mCurrentQuestionBO.equals(qScore.getTag())) {
                                    String strScore = mCurrentQuestionBO.getQuestScore() + "/"
                                            + mCurrentQuestionBO.getQuestWeight();
                                    qScore.setText(strScore);

                                    questionsListView.invalidateViews();
                                    updateSurveyScore();
                                    updateOverAllSurveyScore();
                                }
                            }

                        });
                LinearLayout linLayoutRad = new LinearLayout(getActivity());
                linLayoutRad.addView(radioButton, params1);
                mRadioGroup.addView(linLayoutRad);
            }
            if (mCurrentQuestionBO.equals(qScore.getTag())) {
                String strScore = mCurrentQuestionBO.getQuestScore() + "/"
                        + mCurrentQuestionBO.getQuestWeight();
                qScore.setText(strScore);
                questionsListView.invalidateViews();
                updateSurveyScore();
                updateOverAllSurveyScore();
            }

            if (isViewMode)
                mRadioGroup.setEnabled(false);

            answerLL.addView(mRadioGroup);

            if (checked) {
                subQuestionLL.removeAllViews();
                int tempAnsSize3 = answers.size();
                for (int i = 0; i < tempAnsSize3; i++) {
                    if (answers.get(i).getAnswerID() == mCurrentQuestionBO
                            .getSelectedAnswerIDs().get(0)) {
                        int tempQuestListSize3 = answers.get(i).getQuestionList().size();
                        for (int j = 0; j < tempQuestListSize3; j++) {
                            String s = answers.get(i).getQuestionList().get(j) + "";
                            if (!"0".equals(s)) {
                                checkQID(answers.get(i).getQuestionList().get(j));
                                generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (bmodel.configurationMasterHelper.IS_CNT01
                && mMenuCode.equals("MENU_SURVEY_BA_CS")) {
            setScreenTitle(bmodel.mSelectedActivityName + " (" +
                    childUserName + ")");
        } else {
            setScreenTitle(bmodel.mSelectedActivityName);
        }
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        BusinessModel.getInstance().trackScreenView("Survey");

//        questionsListView.setSelectionFromTop(index, top);

    }

    @Override
    public void onPause() {
        super.onPause();
        // save index and top position
        index = questionsListView.getFirstVisiblePosition();
        View v = questionsListView.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
    }

    private void showSpinner(LinearLayout answerLL,
                             final QuestionBO mCurrentQuestionBO, final TextView qScore, final LinearLayout subQuestionLL, final String qNO) {
        answerLL.removeAllViews();
        if (mCurrentQuestionBO.getAnswersList().get(0).getAnswerID() != -1
                && !mCurrentQuestionBO.getAnswersList().get(0).getAnswer()
                .equals(getResources().getString(R.string.plain_select))) {
            AnswerBO selectBo = new AnswerBO();
            selectBo.setAnswerID(-1);
            selectBo.setAnswer(getResources().getString(R.string.plain_select));
            mCurrentQuestionBO.getAnswersList().add(0, selectBo);
        }
        final ArrayList<AnswerBO> answers = mCurrentQuestionBO.getAnswersList();
        final Spinner items = (Spinner) getActivity().getLayoutInflater().inflate(R.layout.cust_spinner, null);
        /*final Spinner items;
        items = new Spinner(getActivity());*/
        ArrayAdapter<String> comboAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        int ansSize = answers.size();
        for (int i = 0; i < ansSize; i++)
            comboAdapter.add(answers.get(i).getAnswer());
        comboAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        items.setAdapter(comboAdapter);
        int itmSize = items.getCount();

        // settiing first value as default answer.
        if (!answers.isEmpty() && mCurrentQuestionBO.getSelectedAnswerIDs().isEmpty()) {
            mCurrentQuestionBO
                    .setSelectedAnswerID(mCurrentQuestionBO
                            .getAnswersList().get(0)
                            .getAnswerID());
            mCurrentQuestionBO.setSelectedAnswer(mCurrentQuestionBO.getAnswersList().get(0).getAnswer());
        }

        for (int i = 0; i < itmSize; i++) {
            if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(
                    answers.get(i).getAnswerID())) {
                items.setSelection(i);
                subQuestionLL.removeAllViews();
                int tempAnsListSize1 = answers.size();
                for (int k = 0; k < tempAnsListSize1; k++) {
                    if (answers.get(k).getAnswerID() == mCurrentQuestionBO
                            .getSelectedAnswerIDs().get(0)) {
                        int tempQuestListSize1 = answers.get(k).getQuestionList().size();
                        for (int j = 0; j < tempQuestListSize1; j++) {
                            String s = answers.get(i).getQuestionList().get(j) + "";
                            if (!"0".equals(s)) {
                                checkQID(answers.get(i).getQuestionList().get(j));
                                generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                            }
                        }
                    }
                }
                if (answers.get(i).isExcluded()) {
                    mCurrentQuestionBO.setQuestScore(0);
                    mCurrentQuestionBO.setExcludeQuestionWeight(answers.get(i)
                            .isExcluded());
                } else {

                    mCurrentQuestionBO.setQuestScore(answers.get(i).getScore());
                    mCurrentQuestionBO.setExcludeQuestionWeight(false);
                }
                break;
            }
        }

//        // settiing first value as default answer.
//        if (!answers.isEmpty() && mCurrentQuestionBO.getSelectedAnswerIDs().isEmpty()) {
//            mCurrentQuestionBO
//                    .setSelectedAnswerID(mCurrentQuestionBO
//                            .getAnswersList().get(0)
//                            .getAnswerID());
//            mCurrentQuestionBO.setSelectedAnswer(mCurrentQuestionBO.getAnswersList().get(0).getAnswer());
//        }

        items.post(new Runnable() {
            public void run() {
                items.setOnTouchListener(new OnTouchListener() {
                    @Override
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        View view1 = getView();
                        if (view1 != null) {
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        return false;
                    }
                });
                items.setOnItemSelectedListener(new OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {

                        mCurrentQuestionBO.getSelectedAnswerIDs()
                                .clear();
                        mCurrentQuestionBO.getSelectedAnswer()
                                .clear();
                        if (position != 0) {
                            mCurrentQuestionBO
                                    .setSelectedAnswerID(mCurrentQuestionBO
                                            .getAnswersList().get(position)
                                            .getAnswerID());
                            mCurrentQuestionBO.setSelectedAnswer(mCurrentQuestionBO.getAnswersList().get(position).getAnswer());

                            int tempAnsListSize1 = answers.size();
                            for (int i = 0; i < tempAnsListSize1; i++) {
                                if (answers.get(i).getAnswerID() == mCurrentQuestionBO
                                        .getSelectedAnswerIDs().get(0)) {
                                    int tempQuestListSize1 = answers.get(i).getQuestionList().size();
                                    mCurrentQuestionBO.setQuestScore(answers.get(i).getScore());
                                    for (int j = 0; j < tempQuestListSize1; j++) {
                                        String s = answers.get(i).getQuestionList().get(j) + "";
                                        if (!"0".equals(s)) {
                                            checkQID(answers.get(i).getQuestionList().get(j));
                                            generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                                        } else subQuestionLL.removeAllViews();
                                    }
                                }
                            }


                            if (mCurrentQuestionBO.equals(qScore.getTag())) {
                                String strScore = mCurrentQuestionBO.getQuestScore() + "/"
                                        + mCurrentQuestionBO.getQuestWeight();
                                qScore.setText(strScore);
                                //questionsListView.invalidateViews();
                                updateSurveyScore();
                                updateOverAllSurveyScore();
                            }
                        } else subQuestionLL.removeAllViews();

                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });

        if (mCurrentQuestionBO.equals(qScore.getTag())) {
            String strScore = mCurrentQuestionBO.getQuestScore() + "/"
                    + mCurrentQuestionBO.getQuestWeight();
            qScore.setText(strScore);
            questionsListView.invalidateViews();
            updateSurveyScore();
            updateOverAllSurveyScore();
        }

        if (isViewMode)
            items.setEnabled(false);

        answerLL.addView(items);

    }

    private void showCheckBox(LinearLayout answerLL,
                              final QuestionBO mCurrentQuestionBO, final TextView qScore, final LinearLayout subQuestionLL, final String qNO) {
        answerLL.removeAllViews();
        answerLL.setPadding(0, 0, 55, 0);
        LinearLayout layoutTemp = new LinearLayout(getActivity());
        LinearLayout.LayoutParams layoutParamsTemp = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        layoutTemp.setOrientation(LinearLayout.VERTICAL);
        layoutTemp.setLayoutParams(layoutParamsTemp);
        final ArrayList<AnswerBO> answers = mCurrentQuestionBO.getAnswersList();
        CheckBox checkBox;
        float score = 0;

        boolean isExclude = false;
        int answerSize = answers.size();
        subQuestionLL.removeAllViews();
        mCurrentQuestionBO.setQuestScore(0);
        for (int i = 0; i < answerSize; i++) {

            checkBox = new CheckBox(getActivity());
            checkBox.setId(answers.get(i).getAnswerID());
            checkBox.setText(answers.get(i).getAnswer());
            checkBox.setTextColor(ContextCompat.getColor(getActivity(), R.color.FullBlack));
            checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_micro_small));

            if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(
                    answers.get(i).getAnswerID())) {
                checkBox.setChecked(true);
                //   subQuestionLL.removeAllViews();
                int tempQuestListSize1 = answers.get(i).getQuestionList().size();
                for (int j = 0; j < tempQuestListSize1; j++) {
                    String s = answers.get(i).getQuestionList().get(j) + "";
                    if (!"0".equals(s)) {
                        checkQID(answers.get(i).getQuestionList().get(j));
                        generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                    }
                }

            } else {

                checkBox.setChecked(false);
            }


            if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(
                    answers.get(i).getAnswerID())) {

                score = score + answers.get(i).getScore();

                if (answers.get(i).isExcluded())
                    isExclude = true;

                mCurrentQuestionBO.setQuestScore((mCurrentQuestionBO.getQuestScore() + answers.get(i).getScore()));
            }

            if (isExclude) {
                score = 0;
                mCurrentQuestionBO.setExcludeQuestionWeight(true);
            } else {
                mCurrentQuestionBO.setExcludeQuestionWeight(false);
            }

            // mCurrentQuestionBO.setQuestScore(answers.get(i).getScore());

            final float finalScore = score;
            checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    Integer obj = buttonView.getId();
                    View view = getView();
                    if (view != null) {
                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                    }
                    if (isChecked) {
                        //  subQuestionLL.removeAllViews();
                        if (!mCurrentQuestionBO.getSelectedAnswerIDs()
                                .contains(obj))
                            mCurrentQuestionBO.setSelectedAnswerID(buttonView
                                    .getId());
                        mCurrentQuestionBO.setSelectedAnswer(buttonView.getText().toString());
                        int tempAnsListSize1 = answers.size();
                        for (int i = 0; i < tempAnsListSize1; i++) {
                            if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(answers.get(i).getAnswerID())) {
                                int tempQuestListSize1 = answers.get(i).getQuestionList().size();
                                for (int j = 0; j < tempQuestListSize1; j++) {
                                    String s = answers.get(i).getQuestionList().get(j) + "";
                                    if (!"0".equals(s)) {
                                        checkQID(answers.get(i).getQuestionList().get(j));
                                        generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                                    }
                                }

                            }
                        }

                    } else {
                        subQuestionLL.removeAllViews();
                        if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(
                                obj)) {
                            mCurrentQuestionBO.getSelectedAnswerIDs().remove(
                                    obj);
                        }

                        for (int i = 0; i < answers.size(); i++) {
                            if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(answers.get(i).getAnswerID())) {
                                int tempQuestListSize1 = answers.get(i).getQuestionList().size();
                                for (int j = 0; j < tempQuestListSize1; j++) {
                                    String s = answers.get(i).getQuestionList().get(j) + "";
                                    if (!"0".equals(s)) {
                                        checkQID(answers.get(i).getQuestionList().get(j));
                                        generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                                    }

                                }

                            }
                        }
                    }
                    if (mCurrentQuestionBO.equals(qScore.getTag())) {
                        String strScore = String.valueOf(finalScore) + "/"
                                + mCurrentQuestionBO.getQuestWeight();
                        qScore.setText(strScore);
                        questionsListView.invalidateViews();
                        updateSurveyScore();
                        updateOverAllSurveyScore();
                    }

                }

            });

            if (mCurrentQuestionBO.equals(qScore.getTag())) {
                String strScore = String.valueOf(finalScore) + "/"
                        + mCurrentQuestionBO.getQuestWeight();
                qScore.setText(strScore);
                questionsListView.invalidateViews();
                updateSurveyScore();
                updateOverAllSurveyScore();
            }


            if (isViewMode)
                checkBox.setEnabled(false);
            layoutTemp.addView(checkBox);
        }
        answerLL.addView(layoutTemp);

    }

    private void removeQID(int qid) {

        ArrayList<QuestionBO> items = new ArrayList<>();

        for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey) {
                items = surBO.getQuestions();
                break;
            }
        }

        for (int k = 0; k < items.size(); k++) {
            if (items.get(k).getQuestionID() == qid) {
                items.remove(k);
            }
        }

    }

    private void checkQID(int qid) {
        boolean notExsist = false;
        ArrayList<QuestionBO> items = new ArrayList<>();
        for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey)
                items = surBO.getQuestions();


        }

        for (int k = 0; k < items.size(); k++) {

            if (items.get(k).getQuestionID() == qid) {
                notExsist = true;
            }
        }

        if (!notExsist) {
            for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
                if (surBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey)
                    surBO.getQuestions().addAll(bmodel.mSurveyHelperNew.addDepQuestionDetailsToSurvey(bmodel.mSurveyHelperNew.mSelectedSurvey, qid));
            }
        }
    }

    private void generateViews(LinearLayout ll, Integer qID, Boolean remove, final String group, int childCount) {

        try {
            ArrayList<QuestionBO> items = new ArrayList<>();

            for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
                if (surBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey)
                    items = surBO.getQuestions();

            }

            questBO = new QuestionBO();
            for (int k = 0; k < items.size(); k++) {

                if (items.get(k).getQuestionID() == qID) {
                    questBO = items.get(k);
                    break;
                }
            }

            if (questBO == null) {
                Commons.print("QBO>>>>IS>>>>>>," + "" + questBO);
                return;
            }


            if (questBO != null) {

                final ViewGroup parent = null;
                LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View view = layoutInflater.inflate(R.layout.subquestionandanswerset, parent, false);
                LinearLayout photoLayout = (LinearLayout) view
                        .findViewById(R.id.ll_photo);
                RelativeLayout right_container = (RelativeLayout) view
                        .findViewById(R.id.right_container);

                boolean isScoreAvailable, isPhotoAvailable;
                TextView questionNO = (TextView) view
                        .findViewById(R.id.questionno);

                TextView questionTV = (TextView) view
                        .findViewById(R.id.questionTV);

                questionTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                questionNO.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                TextView minPhoto = (TextView) view
                        .findViewById(R.id.minphotoTV);

                minPhoto.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                minPhoto.setTextColor(ContextCompat.getColor(getActivity(), R.color.dead_store_score));

                TextView imp = (TextView) view.findViewById(R.id.imp);


                TextView QscoreTV = (TextView) view.findViewById(R.id.scoreTV);
                QscoreTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

                QscoreTV.setTextColor(ContextCompat.getColor(getActivity(), R.color.dead_store_score));

                LinearLayout answerLayout = (LinearLayout) view
                        .findViewById(R.id.answerLL);
                LinearLayout subQuestLayout = (LinearLayout) view
                        .findViewById(R.id.subqLL);

                final ImageView camBtn = (ImageView) view.findViewById(R.id.imgBtn);
                final TextView tv_counter = (TextView) view.findViewById(R.id.textOne);
                final ImageView photoBtn = (ImageView) view.findViewById(R.id.photos);
                tv_counter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                final RelativeLayout mandatoryView = (RelativeLayout) view.findViewById(R.id.sub_mandatory_view);
                final SlantView slantView = (SlantView) view.findViewById(R.id.slant_view_bg);
                tv_counter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                slantView.setColor(Color.WHITE);
                camBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        photoFunction(questBO, 0);
                    }
                });

                photoBtn.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        photoFunction(questBO, 1);
                    }
                });

                int childCnt = childCount + 1;
                String strQNo = group + childCnt + ".";
                questionNO.setText(strQNo);
                questionTV.setText(questBO.getQuestionDescription());

                QscoreTV.setTag(questBO);

                String strMinPhoto = questBO.getMinPhoto() + "";

                if (strMinPhoto != null && strMinPhoto.length() > 0)
                    minPhoto.setText(strMinPhoto + " Photo Required");
                else
                    minPhoto.setText("0 Photo  required");

                if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_MANDATORY && isSaveClicked) {
                    if (questBO.getIsMandatory() == 1 && questBO.isMandatoryQuestNotAnswered()) {
                        mandatoryView.setVisibility(View.VISIBLE);
                    } else {
                        mandatoryView.setVisibility(View.GONE);
                    }
                } else {
                    mandatoryView.setVisibility(View.GONE);
                }

                if (questBO.getIsPhotoReq() != 0 && questBO.getMinPhoto() > 1) {
                    minPhoto.setVisibility(View.VISIBLE);
                } else {
                    minPhoto.setVisibility(View.INVISIBLE);
                }


                if (questBO.getIsScore() == 1 && bmodel.configurationMasterHelper.SHOW_SCORE_IN_SURVEY) {
                    isScoreAvailable = true;
                    QscoreTV.setVisibility(View.VISIBLE);
                } else {
                    isScoreAvailable = false;
                    QscoreTV.setVisibility(View.INVISIBLE);

                }

                if (questBO.getIsPhotoReq() == 0) {
                    isPhotoAvailable = false;
                    photoLayout.setVisibility(View.INVISIBLE);
                } else {
                    isPhotoAvailable = true;
                    photoLayout.setVisibility(View.VISIBLE);
                }

                if (isScoreAvailable || isPhotoAvailable) {
                    right_container.setVisibility(View.VISIBLE);
                } else {
                    right_container.setVisibility(View.GONE);
                }

                if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_MANDATORY
                        && questBO.getIsMandatory() == 1)
                    imp.setVisibility(View.VISIBLE);
                else
                    imp.setVisibility(View.GONE);


                if (questBO.getImageNames().size() == 0)
                    tv_counter.setVisibility(View.GONE);
                else {

                    //---------- remove duplicate image  name  from given list-----------//

                    for (int i = 0; i < questBO.getImageNames().size(); i++) {

                        for (int j = i + 1; j < questBO.getImageNames().size(); j++) {
                            if (questBO.getImageNames().get(i).toString().equalsIgnoreCase(questBO.getImageNames().get(j).toString())) {
                                questBO.getImageNames().remove(j);
                                j--;
                            }
                        }
                    }
                    tv_counter.setVisibility(View.VISIBLE);
                    String imageName = questBO.getImageNames().size() + "";
                    tv_counter.setText(imageName);
                    camBtn.setColorFilter(ContextCompat.getColor(getContext(), R.color.colorPrimary));
                }

                if (remove) {
                    questBO.getSelectedAnswerIDs().clear();
                    questBO.getSelectedAnswer().clear();
                }

                switch (questBO.getQuestionType()) {
                    case "OPT":
                        showRadioGroup(answerLayout, questBO, QscoreTV, subQuestLayout, questionNO.getText().toString());
                        break;
                    case "MULTISELECT":
                        showCheckBox(answerLayout, questBO, QscoreTV, subQuestLayout, questionNO.getText().toString());
                        break;
                    case "POLL":
                        showSpinner(answerLayout, questBO, QscoreTV, subQuestLayout, questionNO.getText().toString());
                        break;
                    case "NUM":
                        showEditText(1, answerLayout, questBO, subQuestLayout);
                        break;
                    case "PERC":
                        showEditText(2, answerLayout, questBO, subQuestLayout);
                        break;
                    default:
                        showEditText(0, answerLayout, questBO, subQuestLayout);
                        break;
                }
                ll.addView(view);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @SuppressLint("StringFormatInvalid")
    private void photoFunction(QuestionBO questBO, int i) {

        if (!questBO.getSelectedAnswer().isEmpty()
                || !questBO.getSelectedAnswerIDs().isEmpty()) {
            if (bmodel.isExternalStorageAvailable()) {
                if (questBO.getQuestionID() != 0) {
                    if (questBO.getIsPhotoReq() == 1 && questBO.getImageNames().size() >= questBO.getMaxPhoto()) {

                        bmodel.showAlert(
                                String.format(
                                        getResources()
                                                .getString(
                                                        R.string.You_have_already_taken_max_images),
                                        questBO.getMaxPhoto()),
                                0);
                    } else {
                        if (isClicked) {
                            isClicked = false;
                            surveyPhcapture = questBO;
                            imageName = "SVY_"
                                    + bmodel.retailerMasterBO
                                    .getRetailerID() + "_"
                                    + questBO.getSurveyid() + "_"
                                    + questBO.getQuestionID() + "_"
                                    + SDUtil.now(SDUtil.DATE_TIME_ID)
                                    + ".jpg";

                            try {
                                if (i == 0) {
                                    questBO.setTempImagePath((questBO.getImage1Path() != null && questBO.getImage1Path().length() > 0 && isFileExist(questBO.getImage1Path())) ? questBO.getImage1Path() : (questBO.getTempImagePath() != null && questBO.getTempImagePath().length() > 0 && isFileExist(questBO.getTempImagePath())) ? questBO.getTempImagePath() : "");
                                } else {
                                    questBO.setTempImagePath((questBO.getImage2Path() != null && questBO.getImage2Path().length() > 0 && isFileExist(questBO.getImage2Path())) ? questBO.getImage2Path() : (questBO.getTempImagePath() != null && questBO.getTempImagePath().length() > 0 && isFileExist(questBO.getTempImagePath())) ? questBO.getTempImagePath() : "");
                                }
                                Thread.sleep(10);
                                Intent intent = new Intent(
                                        getActivity(),
                                        CameraActivity.class);
                                String path = HomeScreenFragment.folder
                                        .getPath() + "/" + imageName;
                                if (i == 0) {
                                    questBO.setImage1Path(path);
                                    questBO.setImage1Captured(true);
                                } else {
                                    questBO.setImage2Path(path);
                                    questBO.setImage2Captured(true);
                                }
                                pathSrc = path;
                                Log.e("TakenPath", path);
                                intent.putExtra("quality", 40);
                                intent.putExtra("path", path);
                                startActivityForResult(intent,
                                        CAMERA_REQUEST_CODE);
                            } catch (Exception e) {
                                Commons.printException("" + e);
                            }

                        }
                    }

                } else {
                    Toast.makeText(
                            getActivity(),
                            getResources()
                                    .getString(
                                            R.string.please_select_atleast_one_sku),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.sdcard_is_not_ready_to_capture_img),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            String qType = questBO.getQuestionType();
            if ("OPT".equals(qType) || "MULTISELECT".equals(qType) || "POLL".equals(qType)) {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.selectoptionforphoto),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.please_answer_all_mandatory_questions),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEditText(int i, LinearLayout answerLL,
                              final QuestionBO mCurrentQuestionBO, final LinearLayout subQLL) {
        answerLL.removeAllViews();
        subQLL.removeAllViews();

        //EditText et = new EditText((new ContextThemeWrapper(getActivity(), R.style.EditTextGreyNew)));
        EditText et = (EditText) getActivity().getLayoutInflater().inflate(R.layout.survey_dit_text, null);

        if (!mCurrentQuestionBO.getSelectedAnswer().isEmpty())
            et.setText(mCurrentQuestionBO.getSelectedAnswer().get(0));
        else
            et.setText("");
        et.setMinLines(1);
        et.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_micro_small));
        et.setMaxLines(1);
        et.setCursorVisible(true);
        et.requestFocus();
        et.requestFocusFromTouch();

        /*Display display = ((WindowManager) getActivity().getSystemService(
                Context.WINDOW_SERVICE)).getDefaultDisplay();
        int width = display.getWidth() - 50;*/

        //LayoutParams lp = new LayoutParams(width, LayoutParams.WRAP_CONTENT);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(lp);
        et.setPadding(15, 7, 7, 7);
        et.setTextColor(Color.BLACK);

        if (i == 1)
            et.setInputType(InputType.TYPE_CLASS_NUMBER);

        if (i == 2) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        }


        et.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

                String s1 = s.toString().replaceAll("'", "''").trim();

                mCurrentQuestionBO.getSelectedAnswerIDs().clear();
                mCurrentQuestionBO.getSelectedAnswer().clear();
                mCurrentQuestionBO.setSelectedAnswer(s1);

                if (!"".equals(s1) && s1.length() > 0) {
                    mCurrentQuestionBO.setSelectedAnswerID(0);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        if (isViewMode)
            et.setEnabled(false);

        answerLL.addView(et);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(
                R.menu.menu_survey, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        boolean navDrawerOpen = false;
        boolean drawerOpen = false;
        if (mDrawerLayout != null)
            drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        if (mSurveyType == 0)
            menu.findItem(R.id.menu_joint_call_survey).setVisible(false);
        if (bmodel.userMasterHelper.getUserMasterBO().getJoinCallUserList() != null
                && bmodel.userMasterHelper.getUserMasterBO()
                .getJoinCallUserList().size() == 0)
            menu.findItem(R.id.menu_joint_call_survey).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY && bmodel.productHelper.isFilterAvaiable(mMenuCode))
            menu.findItem(R.id.menu_fivefilter).setVisible(true);

     /*   else if (bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY) {
            menu.findItem(R.id.menu_product_filter).setVisible(true);
        }*/


        if (bmodel.configurationMasterHelper.SHOW_SMS_IN_SURVEY
                && bmodel.mSelectedActivityConfigCode
                .equalsIgnoreCase(bmodel.configurationMasterHelper.smsmenutype))
            menu.findItem(R.id.menu_msg).setVisible(true);
        else
            menu.findItem(R.id.menu_msg).setVisible(false);
        Commons.print("sms"
                + bmodel.configurationMasterHelper.SHOW_SMS_IN_SURVEY
                + bmodel.mSelectedActivityName);
        if (bmodel.configurationMasterHelper.SHOW_PHOTOCAPTURE_IN_SURVEY
                && bmodel.mSelectedActivityConfigCode
                .equalsIgnoreCase(bmodel.configurationMasterHelper.photocapturemenutype))
            menu.findItem(R.id.menu_photo_capture).setVisible(true);
        else
            menu.findItem(R.id.menu_photo_capture).setVisible(false);

        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);


        if (mMenuCode.equalsIgnoreCase(SurveyHelperNew.cs_feedback_menucode)) {
            menu.findItem(R.id.menu_photo_capture).setVisible(false);
            menu.findItem(R.id.menu_msg).setVisible(false);
            menu.findItem(R.id.menu_joint_call_survey).setVisible(false);
            menu.findItem(R.id.menu_save).setVisible(false);
        }

        if (!mMenuCode.equals("MENU_SURVEY_CS") && bmodel.configurationMasterHelper.IS_CNT01) {
//            menu.findItem(R.id.menu_select).setVisible(true);
            if (hide_selectuser_icon) {
                menu.findItem(R.id.menu_select).setVisible(false);
            } else
                menu.findItem(R.id.menu_select).setVisible(true);
        } else {
            menu.findItem(R.id.menu_select).setVisible(false);
        }

        if (drawerOpen || navDrawerOpen)
            menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            if (mFrom.equalsIgnoreCase("HomeScreenTwo")) {
                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                if (isFromChild)
                    intent.putExtra("isStoreMenu", true);
                startActivity(intent);
            }
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_save) {
            if (!checkClicked) {
                checkClicked = true;
                if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_ALL) {
                    if (bmodel.mSurveyHelperNew.isAllAnswered()) {
                        if (bmodel.mSurveyHelperNew.hasPhotoToSave())
                            new SaveSurveyTask().execute();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsListView.setAdapter(listAdapter);
                            checkClicked = false;
                        }
                    } else {
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.pleaseanswerallthequestions), 0);
                        checkClicked = false;
                    }
                } else if (bmodel.configurationMasterHelper.IS_SURVEY_ANSWER_MANDATORY) {
                    if (bmodel.mSurveyHelperNew.isMandatoryQuestionAnswered()) {

                        if (bmodel.mSurveyHelperNew.hasPhotoToSave())
                            new SaveSurveyTask().execute();
                        else {

                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsListView.setAdapter(listAdapter);
                            checkClicked = false;
                        }
                    } else {
                        isSaveClicked = true;
                        questionsListView.setAdapter(listAdapter);
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.please_answer_all_mandatory_questions),
                                0);
                        checkClicked = false;
                    }
                } else {


                    if (bmodel.mSurveyHelperNew.hasDataToSave()) {
                        if (bmodel.mSurveyHelperNew.hasPhotoToSave())
                            new SaveSurveyTask().execute();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsListView.setAdapter(listAdapter);
                            checkClicked = false;
                        }
                    } else {
                        bmodel.showAlert(
                                getResources().getString(R.string.no_data_tosave), 0);
                        questionsListView.setAdapter(listAdapter);
                        checkClicked = false;
                    }
                }
            }
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_joint_call_survey) {
            showSupervisiorAlert();

            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_msg) {
            bmodel.mSurveyHelperNew.remarkDone = "Y";
            StringBuilder strBuilder = new StringBuilder();
            strBuilder.append("There is a Potential Order for ");
            strBuilder.append(bmodel.retailerMasterBO.getRetailerName().trim());
            strBuilder.append("\n");
            strBuilder.append("Contact Details :");
            strBuilder.append("\n");
            if (bmodel.retailerMasterBO.getContactname() != null) {
                strBuilder.append("");
                strBuilder.append(bmodel.retailerMasterBO.getContactname().trim());
                strBuilder.append(",");
            }
            if (bmodel.retailerMasterBO.getLocName() != null) {
                strBuilder.append("");
                strBuilder.append(bmodel.retailerMasterBO.getLocName().trim());
                strBuilder.append(",");
            }
            if (bmodel.retailerMasterBO.getContactnumber() != null) {
                strBuilder.append("");
                strBuilder.append(bmodel.retailerMasterBO.getContactnumber());
                strBuilder.append(".");
            }
            Intent intentsms = new Intent(Intent.ACTION_VIEW, Uri.parse("sms:" + ""));
            intentsms.putExtra("sms_body", strBuilder.toString());
            startActivity(intentsms);
            return true;
        } else if (i == R.id.menu_photo_capture) {
            Intent intent = new Intent(getActivity(),
                    PhotoCaptureActivity.class);
            intent.putExtra("fromSurvey", true);
            startActivity(intent);
            getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            return true;
        } else if (i == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), mMenuCode);
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), mMenuCode)) {
                        bmodel.saveModuleCompletion(mMenuCode);
                        getActivity().finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", mMenuCode);
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        } else if (i == R.id.menu_select) {
            //select user
            showUserDialog();
            return true;
        }
        return false;
    }




    /*
     * Show Location wise Filter
     */

    private void showSupervisiorAlert() {
        AlertDialog.Builder alertBuilder;

        alertBuilder = new AlertDialog.Builder(getActivity());
        alertBuilder.setTitle(null);
        alertBuilder.setSingleChoiceItems(supervisiorAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        UserMasterBO selectedId = supervisiorAdapter
                                .getItem(item);
                        selecteditem = item;
                        bmodel.mSurveyHelperNew.mSelectedSuperVisiorID = selectedId
                                .getUserid();
                        new LoadSuperVisorSurveyAnswer().execute();

                        dialog.dismiss();
                    }
                });


        bmodel.applyAlertDialogTheme(alertBuilder);
    }


    class LoadSuperVisorSurveyAnswer extends
            AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.mSurveyHelperNew
                        .loadSuperVisorSurveyAnswers(bmodel.mSurveyHelperNew.mSelectedSuperVisiorID);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getActivity().getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            onLoadQuestion(bmodel.mSurveyHelperNew.mSelectedSurvey,
                    bmodel.mSurveyHelperNew.mSelectedFilter);
        }

    }


    private void productFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", "Brand");
            bundle.putString("filterHeader", bmodel.productHelper
                    .getRetailerModuleChildLevelBO().get(0).getProductLevel());
            bundle.putString("isFrom", "Survey");
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getRetailerModuleChildLevelBO());

            if (bmodel.productHelper.getRetailerModuleParentLeveBO() != null
                    && bmodel.productHelper.getRetailerModuleParentLeveBO()
                    .size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getRetailerModuleParentLeveBO().get(0)
                        .getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getRetailerModuleParentLeveBO());
            } else {
                bundle.putBoolean("isFormBrand", false);
            }

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        bmodel.mSurveyHelperNew.mSelectedFilter = id;
        mDrawerLayout.closeDrawers();
        onLoadQuestion(bmodel.mSurveyHelperNew.mSelectedSurvey,
                bmodel.mSurveyHelperNew.mSelectedFilter);

    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    private class SaveSurveyTask extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if ("MENU_NEW_RET".equals(mMenuCode)) {
                    bmodel.mSurveyHelperNew.saveAnswerNewRetailer(mMenuCode, screenMode);

                } else {
                    bmodel.mSurveyHelperNew.deleteUnusedImages();
                    bmodel.mSurveyHelperNew.saveAnswer(mMenuCode);
                }
                bmodel.updateIsVisitedFlag();
                bmodel.saveModuleCompletion(mMenuCode);
                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                Commons.printException("" + e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            bmodel.customProgressDialog(alertDialog, builder, getActivity(), getResources().getString(R.string.savingquestionanswers));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
           /* String temp = SDUtil.now(SDUtil.DATE_TIME_ID);
            bmodel.outletTimeStampHelper.setUid(bmodel.QT("OTS" + temp));*/
            Log.e("Result", String.valueOf(result));
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
            alertDialog.dismiss();
            bmodel.mSurveyHelperNew.remarkDone = "N";
            checkClicked = false;

            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.ok),
                    null, isNext, new CommonDialog.positiveOnClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    questionsListView.invalidateViews();
                }
            }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {
                    Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                    Bundle extras = getActivity().getIntent().getExtras();
                    if (extras != null) {
                        intent.putExtra("IsMoveNextActivity", extras.getBoolean("IsMoveNextActivity", false));
                        intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                    }
                    startActivity(intent);
                    getActivity().finish();
                }
            }).show();

        }

    }


    public class OnSwipeTouchListener implements OnTouchListener {

        private final GestureDetector gestureDetector = new GestureDetector(getActivity(),
                new GestureListener());

        public boolean onTouch(final View view, final MotionEvent motionEvent) {

            return gestureDetector.onTouchEvent(motionEvent);

        }

        private final class GestureListener extends SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;

            @Override
            public boolean onDown(MotionEvent e) {
                return true;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2,
                                   float velocityX, float velocityY) {

                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD
                                && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                onSwipeRight();
                            } else {
                                onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD
                                && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                onSwipeBottom();
                            } else {
                                onSwipeTop();
                            }
                        }
                    }
                } catch (Exception exception) {
                    Commons.printException("" + exception);
                }
                return super.onFling(e1, e2, velocityX, velocityY);
            }
        }

        public void onSwipeRight() {
        }

        public void onSwipeLeft() {
        }

        public void onSwipeTop() {
        }

        public void onSwipeBottom() {
        }
    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updategeneraltext(String filtertext) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadStartVisit() {
        // TODO Auto-generated method stub

    }

   /* @Override
    public void onTabReselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onTabUnselected(Tab arg0, FragmentTransaction arg1) {
        // TODO Auto-generated method stub

    }*/

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        loadQuestionFromFiveLevelFilter(
                bmodel.mSurveyHelperNew.mSelectedSurvey,

                parentidList);

        mDrawerLayout.closeDrawers();

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        loadQuestionFromFiveLevelFilter(
                bmodel.mSurveyHelperNew.mSelectedSurvey,

                parentidList);
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mFinalParentIdList = parentidList;

        mDrawerLayout.closeDrawers();

    }

    private void loadQuestionFromFiveLevelFilter(int surveyId, Vector<LevelBO>

            finalSelectionList) {

        ArrayList<QuestionBO> items = new ArrayList<>();

        for (SurveyBO surBO : bmodel.mSurveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == bmodel.mSurveyHelperNew.mSelectedSurvey)
                items = surBO.getQuestions();

        }

        if (items == null || items.isEmpty())
            return;

        mQuestions = new ArrayList<>();
        for (LevelBO levelBo : finalSelectionList) {

            for (QuestionBO question : items) {
                if (question.getSurveyid() == surveyId || surveyId == -1) {
                    if (question.getBrandID() == levelBo.getProductID()
                            || levelBo.getProductID() == -1 && question.getIsSubQuestion() == 0) {
                        mQuestions.add(question);
                    }
                }
            }
        }
        SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
        surveyHelperNew.setmQuestionData(mQuestions);

        /*questionsListView.setOnTouchListener(new OnSwipeTouchListener() {
            public void onSwipeRight() {
                if ((tabPos - 1) >= 0 && tabLayout != null)
                    tabLayout.getTabAt(tabPos - 1).select();
            }

            public void onSwipeLeft() {
                if ((tabPos + 1) <= tabCount && tabLayout != null)
                    tabLayout.getTabAt(tabPos + 1).select();
            }
        });*/


        listAdapter = new QuestionAdapter();
        questionsListView.setAdapter(listAdapter);


    }

    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "Survey");
            bundle.putBoolean("isAttributeFilter", false);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }

    public class InputFilterMinMax implements InputFilter {
        private final int min;
        private final int max;

        public InputFilterMinMax(int min, int max) {
            this.min = min;
            this.max = max;
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            //noinspection EmptyCatchBlock
            try {
                int input = Integer.parseInt(dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length()));
                if (isInRange(min, max, input))
                    return null;
            } catch (NumberFormatException nfe) {
                Commons.printException("" + nfe);
            }
            return "";
        }

        private boolean isInRange(int a, int b, int c) {
            return b > a ? c >= a && c <= b : c >= b && c <= a;
        }
    }

//    private Bitmap getCircularBitmapFrom(Bitmap source) {
//        if (source == null || source.isRecycled()) {
//            return null;
//        }
//        float radius = source.getWidth() > source.getHeight() ? ((float) source
//                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
//        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
//                source.getHeight(), Bitmap.Config.ARGB_8888);
//
//        Paint paint = new Paint();
//        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
//                Shader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
//                radius, paint);
//
//        return bitmap;
//    }

    private boolean isFileExist(String filePath) {
        try {
            File f = new File(filePath);

            if (f.exists()) {
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}