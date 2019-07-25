package com.ivy.cpg.view.survey;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.ivy.cpg.view.attendance.AttendanceHelper;
import com.ivy.cpg.view.homescreen.HomeScreenFragment;
import com.ivy.lib.pdf.PDFGenerator;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.SlantView;
import com.ivy.ui.photocapture.view.PhotoCaptureActivity;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * IsExclude score will not work/applicable for multi select questions.
 */
public class SurveyActivityNewFragment extends IvyBaseFragment implements TabLayout.OnTabSelectedListener, BrandDialogInterface, OnClickListener, FiveLevelFilterCallBack {
    private BusinessModel bmodel;
    private int tabPos;
    private int tabCount;
    private String imageName = "";

    private static final int CAMERA_REQUEST_CODE = 1;
    private static final int DRAG_AND_DROP = 2;
    private boolean isClicked = true;
    private QuestionBO surveyPhcapture = new QuestionBO();
    private SurveyBO surveyBO = new SurveyBO();
    private DrawerLayout mDrawerLayout;
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private RecyclerView questionsRv;
    private QuestionAdapter rvAdapter;
    private ArrayList<QuestionBO> mQuestions;
    private QuestionBO questBO;
    private AlertDialog alertDialog;
    private ArrayAdapter<UserMasterBO> supervisiorAdapter;
    private int selecteditem = 0;
    private int mSurveyType;
    private String mMenuCode = "";
    private int screenMode = 0;
    private String editRetailerId;
    // For Computing score
    private TextView surveyScoreTextView, overAllSurveyScoreTextView;
    private LinearLayout surveyScoreLinearLayout, surveyScoreOverAllLinearLayout;
    private String path = "";
    private boolean isSaveClicked;
    private boolean checkClicked;
    private int index;
    private int top;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private int mFilteredProductId = -1;
    private boolean isViewMode;
    private TabLayout tabLayout;
    private Button saveButton;
    private String mFrom = "";
    private ArrayList<StandardListBO> childList;
    private int mSelectedIdIndex = -1, isFromDragDrop = -1;
    private String childUserName = "";
    private boolean isFromChild;
    protected Boolean isMultiPhotoCaptureEnabled = false;
    private SurveyHelperNew surveyHelperNew;
    private LinearLayoutManager linearLayoutManager;

    private Context context;
    private boolean isPreVisit = false;
    private EditText mBarcodeEditText;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(((Activity) context));
        surveyHelperNew = SurveyHelperNew.getInstance(context);

        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_new_survey, container, false);
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(this);
        Bundle extras = getArguments();
        if (extras == null)
            extras = ((Activity) context).getIntent().getExtras();
        if (extras != null) {
            isFromChild = extras.getBoolean("isFromChild", false);
            mFrom = extras.getString("from") != null ? extras.getString("from") : "";

            isPreVisit = extras.getBoolean("PreVisit", false);
        }

        initializeView(view);
        return view;
    }

    private void initializeView(View view) {
        questionsRv = (RecyclerView) view.findViewById(R.id.lv_qustions);
        questionsRv.setHasFixedSize(false);
        questionsRv.setNestedScrollingEnabled(false);
        linearLayoutManager = new LinearLayoutManager(context);
        questionsRv.setLayoutManager(linearLayoutManager);
        surveyScoreTextView = (TextView) view
                .findViewById(R.id.questionScoreTV);
        surveyScoreTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        surveyScoreTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
        overAllSurveyScoreTextView = (TextView) view.findViewById(R.id.surveyScoreTV);
        overAllSurveyScoreTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        overAllSurveyScoreTextView.setTextColor(ContextCompat.getColor(context, R.color.white));
        surveyScoreLinearLayout = (LinearLayout) view.findViewById(
                R.id.questionScore_ll);
        surveyScoreOverAllLinearLayout = (LinearLayout) view.findViewById(
                R.id.surveyScore_ll);
        saveButton = (Button) view.findViewById(R.id.save);
        saveButton.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        saveButton.setOnClickListener(this);
        if (((AppCompatActivity) context).getSupportActionBar() != null) {
            ((AppCompatActivity) context).getSupportActionBar().setDisplayShowHomeEnabled(true);
            ((AppCompatActivity) context).getSupportActionBar().setDisplayShowTitleEnabled(false);
            ((AppCompatActivity) context).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(((Activity) context), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                setScreenTitle(bmodel.mSelectedActivityName);
                ((FragmentActivity) context).supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                setScreenTitle(getResources().getString(R.string.filter));
                ((FragmentActivity) context).supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        Bundle extras = getArguments();
        if (extras == null) {
            extras = ((Activity) context).getIntent().getExtras();
        }
        try {
            if (extras != null) {
                mSurveyType = extras.getInt("SurveyType", 0);
                mMenuCode = extras.getString("menuCode", "");
                screenMode = extras.getInt("screenMode", 0);
                editRetailerId = extras.getString("editRetailerId", "");
            } else {
                mSurveyType = ((Activity) context).getIntent().getExtras().getInt("SurveyType", 0);
                mMenuCode = ((Activity) context).getIntent().getExtras().getString("menuCode", "");
                screenMode = ((Activity) context).getIntent().getExtras().getInt("screenMode", 0);
                editRetailerId = ((Activity) context).getIntent().getExtras().getString("editRetailerId", "");
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
        childList = AttendanceHelper.getInstance(context).loadChildUserList(context.getApplicationContext());
        if (childList != null && childList.size() > 0) {
            if (childList.size() > 1) {
                showDialog();
            } else if (childList.size() == 1) {
                bmodel.setSelectedUserId(childList.get(0).getChildUserId());
                loadListData();
            }
        } else {
            bmodel.setSelectedUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            loadListData();
        }
    }

    private void showDialog() {
        ArrayAdapter<String> mChildUserNameAdapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_singlechoice);
        for (StandardListBO temp : childList)
            mChildUserNameAdapter.add(temp.getChildUserName());
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
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
                        loadListData();
                        dialog.dismiss();
                    }
                });
        AlertDialog objDialog = bmodel.applyAlertDialogTheme(builder);
        objDialog.setCancelable(false);
    }

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(((Activity) context));
        if (surveyHelperNew.ENABLE_MULTIPLE_PHOTO)
            isMultiPhotoCaptureEnabled = true;
        loadListData();

    }

    private void loadListData() {

        if (surveyHelperNew.getSurvey() == null) {
            return;
        }

        if (isFromDragDrop == -1) {
            if (!mFrom.equalsIgnoreCase("HomeScreenTwo"))
                surveyHelperNew
                        .loadSurveyAnswers(0);
        }
        //  isFromDragDrop=-1;
        surveyHelperNew.mSelectedFilter = -1;
        path = "/" + "Survey" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/";
        if (getView() != null)
            if (tabLayout != null) {
                tabLayout.removeAllTabs();
            }
        tabCount = surveyHelperNew.getSurvey().size();
        float scale = getContext().getResources().getDisplayMetrics().widthPixels;
        scale = scale / tabCount;
        // Add a survey in Tab
        for (int i = 0; i < tabCount; i++) {
            TextView tabOne = (TextView) LayoutInflater.from(context).inflate(R.layout.custom_tab, null);
            tabOne.setGravity(Gravity.CENTER);
            tabOne.setWidth((int) scale);
            tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tabOne.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tabOne.setText(surveyHelperNew.getSurvey().get(i)
                    .getSurveyName());
            TabLayout.Tab tab = tabLayout.newTab().setCustomView(tabOne);
            tab.setTag(""
                    + surveyHelperNew.getSurvey().get(i)
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
        surveyHelperNew.highlightQuest = false;
        // configure filter
        mSelectedFilterMap.put("Category", "All");
        mSelectedFilterMap.put("Brand", "All");
        if (mMenuCode.equalsIgnoreCase(SurveyHelperNew.cs_feedback_menucode))
            saveButton.setVisibility(View.GONE);
        if (screenMode == 1)
            isViewMode = true;
        if (mSurveyType == 1) {
            supervisiorAdapter = new ArrayAdapter<>(context,
                    android.R.layout.select_dialog_singlechoice);
            for (UserMasterBO userBo : bmodel.userMasterHelper
                    .getUserMasterBO().getJoinCallUserList()) {
                if (userBo.getIsJointCall() == 1)
                    supervisiorAdapter.add(userBo);
            }
            for (UserMasterBO user : bmodel.userMasterHelper.getUserMasterBO()
                    .getJoinCallUserList()) {
                if (user.getIsJointCall() == 1) {
                    surveyHelperNew.mSelectedSuperVisiorID = user
                            .getUserid();
                    break;
                }
            }
        }

        linearLayoutManager.scrollToPositionWithOffset(index - 1, top);
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
        for (SurveyBO sBO : surveyHelperNew.getSurvey()) {
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
        for (SurveyBO sBO : surveyHelperNew.getSurvey()) {
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
        for (SurveyBO surveyBO : surveyHelperNew.getSurvey()) {
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
        for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
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


        rvAdapter = new QuestionAdapter();
        questionsRv.setAdapter(rvAdapter);

        questionsRv.setRecyclerListener(new RecyclerView.RecyclerListener() {
            @Override
            public void onViewRecycled(RecyclerView.ViewHolder holder) {
                if (holder.itemView.hasFocus()) {
                    holder.itemView.clearFocus(); //we can put it inside the second if as well, but it makes sense to do it to all scraped views
                    //Optional: also hide keyboard in that case
                    if (holder.itemView instanceof EditText) {
                        InputMethodManager imm = (InputMethodManager) holder.itemView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(holder.itemView.getWindowToken(), 0);
                    }
                }
            }
        });
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        tabPos = tab.getPosition();

        surveyHelperNew.mSelectedSurvey = SDUtil.convertToInt(tab.getTag()
                .toString());
        for (SurveyBO sBO : surveyHelperNew.getSurvey()) {
            if (sBO.getSurveyID() == surveyHelperNew.mSelectedSurvey) {
                surveyBO = sBO;
            }
        }

        loadQuestionFromFiveLevelFilter(surveyHelperNew.mSelectedSurvey, mFilteredProductId);

        /* Show or hide footer which display survey score and overall score*/
        if (surveyHelperNew.SHOW_TOTAL_SCORE_IN_SURVEY) {
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
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
    }

    @Override
    public void onClick(View view) {
        if (view == saveButton) {
            if (!checkClicked) {
                checkClicked = true;
                if (surveyHelperNew.hasDataToSave()) {

                    if (surveyBO.isMandatory() && !surveyHelperNew.isAllAnswered()) {
                        if (surveyHelperNew.getInvalidEmails().length() > 0) {
                            bmodel.showAlert("Kindly provide valid mail id for \n" + surveyHelperNew.getInvalidEmails(), 0);
                        } else if (surveyHelperNew.getNotInRange().length() > 0) {
                            bmodel.showAlert("Given value is not in range for \n" + surveyHelperNew.getNotInRange(), 0);
                        } else {
                            bmodel.showAlert(
                                    getResources().getString(
                                            R.string.pleaseanswerallthequestions), 0);
                        }
                        checkClicked = false;
                    } else if (surveyHelperNew.IS_SURVEY_ANSWER_ALL) {

                        if (surveyHelperNew.isAllAnswered()) {

                            if (surveyHelperNew.hasPhotoToSave())
                                captureSignature();
                            else {
                                bmodel.showAlert(
                                        getResources().getString(R.string.take_photos_to_save), 0);
                                questionsRv.setAdapter(rvAdapter);
                                checkClicked = false;
                            }
                        } else {

                            if (surveyHelperNew.getInvalidEmails().length() > 0) {
                                bmodel.showAlert("Kindly provide valid mail id for \n" + surveyHelperNew.getInvalidEmails(), 0);
                            } else if (surveyHelperNew.getNotInRange().length() > 0) {
                                bmodel.showAlert("Given value is not in range for \n" + surveyHelperNew.getNotInRange(), 0);
                            } else {
                                bmodel.showAlert(
                                        getResources().getString(
                                                R.string.pleaseanswerallthequestions), 0);
                            }
                            checkClicked = false;
                        }
                    } else if (surveyHelperNew.IS_SURVEY_ANSWER_MANDATORY) {

                        if (surveyHelperNew.isMandatoryQuestionAnswered()) {
                            if (surveyHelperNew.hasPhotoToSave())
                                captureSignature();
                            else {
                                bmodel.showAlert(
                                        getResources().getString(R.string.take_photos_to_save), 0);
                                questionsRv.setAdapter(rvAdapter);
                                checkClicked = false;
                            }
                        } else {
                            isSaveClicked = true;
                            questionsRv.setAdapter(rvAdapter);
                            if (surveyHelperNew.getInvalidEmails().length() > 0) {
                                bmodel.showAlert("Kindly provide valid mail id for \n" + surveyHelperNew.getInvalidEmails(), 0);
                            } else if (surveyHelperNew.getNotInRange().length() > 0) {
                                bmodel.showAlert("Given value is not in range for \n" + surveyHelperNew.getNotInRange(), 0);
                            } else {
                                bmodel.showAlert(
                                        getResources()
                                                .getString(
                                                        R.string.please_answer_all_mandatory_questions),
                                        0);
                            }
                            checkClicked = false;
                        }

                    } else {
                        if (surveyHelperNew.isAnsweredTypeEmail()) {

                            if (surveyHelperNew.hasPhotoToSave())
                                captureSignature();
                            else {
                                bmodel.showAlert(
                                        getResources().getString(R.string.take_photos_to_save), 0);
                                questionsRv.setAdapter(rvAdapter);
                                checkClicked = false;
                            }

                        } else {
                            checkClicked = false;
                            bmodel.showAlert("Kindly provide valid mail id for \n" + surveyHelperNew.getInvalidEmails(), 0);
                        }
                    }
                } else {
                    if (surveyHelperNew.getInvalidEmails().length() > 0) {
                        bmodel.showAlert("Kindly provide valid mail id for \n" + surveyHelperNew.getInvalidEmails(), 0);
                    } else if (surveyHelperNew.getNotInRange().length() > 0) {
                        bmodel.showAlert("Given value is not in range for \n" + surveyHelperNew.getNotInRange(), 0);
                    } else {
                        bmodel.showAlert(
                                getResources().getString(R.string.no_data_tosave), 0);
                    }
                    questionsRv.setAdapter(rvAdapter);
                    checkClicked = false;
                }
            }
        }
    }

    public class QuestionAdapter extends RecyclerView.Adapter<QuestionAdapter.ViewHolder> {
        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.questionandanswerset, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.questionBO = mQuestions.get(position);
            holder.camBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoFunction(holder.questionBO, 0);
                }
            });
            holder.photoBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    photoFunction(holder.questionBO, 1);
                }
            });
            holder.dragBtn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(context);
                    surveyHelperNew.setQuestionBODragDrop(holder.questionBO);
                    surveyPhcapture = holder.questionBO;
                    Intent intent = new Intent(context, DragDropPictureActivity.class);
                    intent.putExtra("BrandId", holder.questionBO.getBrandID());
                    intent.putExtra("QuestiionId", holder.questionBO.getQuestionID());
                    intent.putExtra("QuestionDesc", holder.questionBO.getQuestionDescription());
                    intent.putExtra("SurveyId", surveyHelperNew.mSelectedSurvey);
                    startActivityForResult(intent, DRAG_AND_DROP);
                }
            });
            if (position % 2 == 0) {
                holder.slantView.setColor(Color.WHITE);
                holder.rowLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            } else {
                holder.slantView.setColor(ContextCompat.getColor(context, R.color.divider_view_color));
                holder.rowLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.divider_view_color));
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
            holder.questionBO.setQuestionNo(strQuestionNo);
            holder.questionTV.setText(holder.questionBO.getQuestionDescription());
            if (surveyHelperNew.IS_SURVEY_ANSWER_MANDATORY && isSaveClicked) {
                if (holder.questionBO.getIsMandatory() == 1 && holder.questionBO.isMandatoryQuestNotAnswered()) {
                    holder.mandatoryView.setVisibility(View.VISIBLE);
                } else {
                    holder.mandatoryView.setVisibility(View.GONE);
                }
            } else {
                holder.mandatoryView.setVisibility(View.GONE);
            }
            if (holder.questionBO.getIsBonus() == 1)
                holder.questionTV.setTextColor(ContextCompat.getColor(context, R.color.colorPrimary));
            else
                holder.questionTV.setTextColor(ContextCompat.getColor(context, R.color.list_item_values_text_color));
            holder.QscoreTV.setTag(holder.questionBO);
            int strminPhoto = holder.questionBO.getMinPhoto();
            if (strminPhoto == 1)
                holder.minPhoto.setText(strminPhoto + " Photo Required");
            else if (strminPhoto > 1)
                holder.minPhoto.setText(strminPhoto + " Photos Required");
            else
                holder.minPhoto.setText("0 Photo  required");
            if (holder.questionBO.getIsScore() == 1 && surveyHelperNew.SHOW_SCORE_IN_SURVEY) {
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
                }
            }
            if (!surveyHelperNew.SHOW_DRAGDROP_IN_SURVEY) {
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
            if (surveyHelperNew.IS_SURVEY_ANSWER_MANDATORY
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
                case "EMAIL":
                    showEditText(3, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
                    break;
                case "DATE":
                    showEditText(4, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
                    break;
                case "PH_NO":
                    showEditText(1, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
                    break;
                case "DECIMAL":
                    showEditText(5, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
                    break;
                case "BARCODE":
                    showBarcodeEditText(holder.answerLayout, holder.questionBO, holder.subQuestLayout, false);
                    break;
                case "BARCODE_EDIT":
                    showBarcodeEditText(holder.answerLayout, holder.questionBO, holder.subQuestLayout, true);
                    break;
                default:
                    showEditText(0, holder.answerLayout, holder.questionBO, holder.subQuestLayout);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mQuestions.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            QuestionBO questionBO;
            LinearLayout answerLayout;
            LinearLayout rowLayout;
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

            public ViewHolder(View row) {
                super(row);
                photoLayout = (LinearLayout) row
                        .findViewById(R.id.ll_photo);
                right_container = (RelativeLayout) row
                        .findViewById(R.id.right_container);
                groupNameLayout = (LinearLayout) row
                        .findViewById(R.id.ll_groupname);
                questionNO = (TextView) row
                        .findViewById(R.id.questionno);
                questionTV = (TextView) row
                        .findViewById(R.id.questionTV);
                questionTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                questionNO.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                groupName = (TextView) row
                        .findViewById(R.id.groupname);
                groupName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                minPhoto = (TextView) row
                        .findViewById(R.id.minphotoTV);
                minPhoto.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
                imp = (TextView) row.findViewById(R.id.imp);
                QscoreTV = (TextView) row.findViewById(R.id.scoreTV);
                QscoreTV.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
                QscoreTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                rowLayout = (LinearLayout) row
                        .findViewById(R.id.rowLayout);
                answerLayout = (LinearLayout) row
                        .findViewById(R.id.answerLL);
                subQuestLayout = (LinearLayout) row
                        .findViewById(R.id.subqLL);
                camBtn = (ImageView) row.findViewById(R.id.imgBtn);
                tv_counter = (TextView) row.findViewById(R.id.textOne);
                photoBtn = (ImageView) row.findViewById(R.id.photos);
                dragBtn = (ImageView) row.findViewById(R.id.dragDropIcon);
                dragDropLayout = (LinearLayout) row.findViewById(R.id.dragDropLayout);
                camIndicatorLty = (LinearLayout) row.findViewById(R.id.indicator_view);
                minPhoto.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                mandatoryView = (RelativeLayout) row.findViewById(R.id.mandatory_view);
                slantView = (SlantView) row.findViewById(R.id.slant_view_bg);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                surveyPhcapture.getImageNames().add(path + imageName);
                if (isMultiPhotoCaptureEnabled) {
                    Toast.makeText(context, "Photo Captured and Saved Successfully", Toast.LENGTH_SHORT).show();
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
        } else if (requestCode == IntentIntegrator.REQUEST_CODE) {
            try {
                IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                    } else if (mBarcodeEditText != null) {
                        mBarcodeEditText.setText(result.getContents());
                    }
                }
            } catch (Exception ex) {
                Commons.printException(ex);
            }
        }
        isClicked = true;
    }

    private void showRadioGroup(LinearLayout answerLL,
                                final QuestionBO mCurrentQuestionBO, final TextView qScore, final LinearLayout subQuestionLL, final String qNO) {
        try {
            answerLL.removeAllViews();
            RadioGroup mRadioGroup = new RadioGroup(context);
            mRadioGroup.setOrientation(RadioGroup.VERTICAL);
            mRadioGroup.setPadding(0, 0, 55, 0);
            LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            params1.bottomMargin = 0;
            final ArrayList<AnswerBO> answers = mCurrentQuestionBO.getAnswersList();
            int answerCount = answers.size();
            boolean checked = false;
            for (int i = 0; i < answerCount; i++) {
                if (answers.get(i).getAnswer() != null) {
                    final RadioButton radioButton = new RadioButton(context);
                    radioButton.setId(answers.get(i).getAnswerID());
                    radioButton.setText(answers.get(i).getAnswer());
                    radioButton.setTextColor(ContextCompat.getColor(context, R.color.FullBlack));
                    radioButton.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_primary));
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
                                        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                                        String strScore = String.valueOf(((mCurrentQuestionBO.getMaxScore() > 0 && mCurrentQuestionBO.getQuestScore() > mCurrentQuestionBO.getMaxScore())
                                                ? mCurrentQuestionBO.getMaxScore() : mCurrentQuestionBO.getQuestScore())) + "/"
                                                + mCurrentQuestionBO.getQuestWeight();
                                        qScore.setText(strScore);
                                        questionsRv.invalidate();
                                        updateSurveyScore();
                                        updateOverAllSurveyScore();
                                    }
                                    rvAdapter.notifyDataSetChanged();
                                }
                            });
                    LinearLayout linLayoutRad = new LinearLayout(context);
                    linLayoutRad.addView(radioButton, params1);
                    mRadioGroup.addView(linLayoutRad);
                }
            }
            if (mCurrentQuestionBO.equals(qScore.getTag())) {
                String strScore = String.valueOf(((mCurrentQuestionBO.getMaxScore() > 0 && mCurrentQuestionBO.getQuestScore() > mCurrentQuestionBO.getMaxScore())
                        ? mCurrentQuestionBO.getMaxScore() : mCurrentQuestionBO.getQuestScore())) + "/"
                        + mCurrentQuestionBO.getQuestWeight();
                qScore.setText(strScore);
                questionsRv.invalidate();
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

        setScreenTitle(bmodel.mSelectedActivityName);

        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(((Activity) context));
    }

    @Override
    public void onPause() {
        super.onPause();
        // save index and top position
        index = linearLayoutManager.findFirstVisibleItemPosition();
        View v = questionsRv.getChildAt(0);
        top = (v == null) ? 0 : v.getTop();
    }

    private void showSpinner(LinearLayout answerLL,
                             final QuestionBO mCurrentQuestionBO, final TextView qScore, final LinearLayout subQuestionLL, final String qNO) {
        answerLL.removeAllViews();
        if (mCurrentQuestionBO.getAnswersList().get(0).getAnswerID() != -1
                && mCurrentQuestionBO.getAnswersList().get(0).getAnswer() != null
                && !mCurrentQuestionBO.getAnswersList().get(0).getAnswer()
                .equals(getResources().getString(R.string.plain_select))) {
            AnswerBO selectBo = new AnswerBO();
            selectBo.setAnswerID(-1);
            selectBo.setAnswer(getResources().getString(R.string.plain_select));
            mCurrentQuestionBO.getAnswersList().add(0, selectBo);
        }
        final ArrayList<AnswerBO> answers = mCurrentQuestionBO.getAnswersList();
        final Spinner items = (Spinner) ((Activity) context).getLayoutInflater().inflate(R.layout.cust_spinner, null);

        ArrayAdapter<String> comboAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_bluetext_layout);
        int ansSize = answers.size();
        for (int i = 0; i < ansSize; i++) {
            if (answers.get(i).getAnswer() != null)
                comboAdapter.add(answers.get(i).getAnswer());
        }
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
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
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
                                String strScore = String.valueOf(((mCurrentQuestionBO.getMaxScore() > 0 && mCurrentQuestionBO.getQuestScore() > mCurrentQuestionBO.getMaxScore())
                                        ? mCurrentQuestionBO.getMaxScore() : mCurrentQuestionBO.getQuestScore())) + "/"
                                        + mCurrentQuestionBO.getQuestWeight();
                                qScore.setText(strScore);
                                //questionsListView.invalidateViews();
                                updateSurveyScore();
                                updateOverAllSurveyScore();
                            }
                        } else subQuestionLL.removeAllViews();

                        rvAdapter.notifyDataSetChanged();
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
            }
        });
        if (mCurrentQuestionBO.equals(qScore.getTag())) {
            String strScore = String.valueOf(((mCurrentQuestionBO.getMaxScore() > 0 && mCurrentQuestionBO.getQuestScore() > mCurrentQuestionBO.getMaxScore())
                    ? mCurrentQuestionBO.getMaxScore() : mCurrentQuestionBO.getQuestScore())) + "/"
                    + mCurrentQuestionBO.getQuestWeight();
            qScore.setText(strScore);
            questionsRv.invalidate();
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
        LinearLayout layoutTemp = new LinearLayout(context);
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
            if (answers.get(i).getAnswer() != null) {
                checkBox = new CheckBox(context);
                checkBox.setId(answers.get(i).getAnswerID());
                checkBox.setText(answers.get(i).getAnswer());
                checkBox.setTextColor(ContextCompat.getColor(context, R.color.FullBlack));
                checkBox.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_primary));
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
                            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                        }
                        if (isChecked) {
                            //  subQuestionLL.removeAllViews();
                            if (!mCurrentQuestionBO.getSelectedAnswerIDs()
                                    .contains(obj)) {
                                mCurrentQuestionBO.setSelectedAnswerID(buttonView
                                        .getId());
                                mCurrentQuestionBO.setSelectedAnswer(buttonView.getText().toString());
                            }
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

                            for (int i = 0; i < answers.size(); i++) {
                                int ansid = mCurrentQuestionBO.getSelectedAnswerIDs().indexOf(buttonView.getId());
                                if (mCurrentQuestionBO.getSelectedAnswerIDs().get(ansid).equals(answers.get(i).getAnswerID()) &&
                                        mCurrentQuestionBO.getSelectedAnswerIDs().get(ansid).equals(buttonView.getId())) {
                                    int tempQuestListSize1 = answers.get(i).getQuestionList().size();
                                    for (int j = 0; j < tempQuestListSize1; j++) {
                                        String s = answers.get(i).getQuestionList().get(j) + "";
                                        if (!"0".equals(s)) {
                                            checkQID(answers.get(i).getQuestionList().get(j));
                                            generateViews(subQuestionLL, answers.get(i).getQuestionList().get(j), false, qNO, j);
                                        }
                                    }
                                    break;
                                }
                            }

                            if (mCurrentQuestionBO.getSelectedAnswerIDs().contains(obj)) {
                                mCurrentQuestionBO.getSelectedAnswerIDs().remove(obj);
                                mCurrentQuestionBO.getSelectedAnswer().remove(buttonView.getText().toString());
                            }
                        }
                        if (mCurrentQuestionBO.equals(qScore.getTag())) {
                            String strScore = String.valueOf(((mCurrentQuestionBO.getMaxScore() > 0 && finalScore > mCurrentQuestionBO.getMaxScore())
                                    ? mCurrentQuestionBO.getMaxScore() : finalScore)) + "/"
                                    + mCurrentQuestionBO.getQuestWeight();
                            qScore.setText(strScore);
                            questionsRv.invalidate();
                            updateSurveyScore();
                            updateOverAllSurveyScore();
                        }

                        rvAdapter.notifyDataSetChanged();
                    }
                });
                if (mCurrentQuestionBO.equals(qScore.getTag())) {
                    String strScore = String.valueOf(((mCurrentQuestionBO.getMaxScore() > 0 && finalScore > mCurrentQuestionBO.getMaxScore())
                            ? mCurrentQuestionBO.getMaxScore() : finalScore)) + "/"
                            + mCurrentQuestionBO.getQuestWeight();
                    qScore.setText(strScore);
                    questionsRv.invalidate();
                    updateSurveyScore();
                    updateOverAllSurveyScore();
                }
                if (isViewMode)
                    checkBox.setEnabled(false);
                layoutTemp.addView(checkBox);
            }
        }
        answerLL.addView(layoutTemp);
    }

    private void removeQID(int qid) {
        ArrayList<QuestionBO> items = new ArrayList<>();
        for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == surveyHelperNew.mSelectedSurvey) {
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
        for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == surveyHelperNew.mSelectedSurvey)
                items = surBO.getQuestions();
        }
        for (int k = 0; k < items.size(); k++) {
            if (items.get(k).getQuestionID() == qid) {
                notExsist = true;
            }
        }
        if (!notExsist) {
            for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
                if (surBO.getSurveyID() == surveyHelperNew.mSelectedSurvey)
                    surBO.getQuestions().addAll(surveyHelperNew.addDepQuestionDetailsToSurvey(surveyHelperNew.mSelectedSurvey, qid));
            }
        }
    }

    private void generateViews(LinearLayout ll, Integer qID, Boolean remove, final String group, int childCount) {
        try {
            ArrayList<QuestionBO> items = new ArrayList<>();
            for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
                if (surBO.getSurveyID() == surveyHelperNew.mSelectedSurvey)
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
                LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
                minPhoto.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
                TextView imp = (TextView) view.findViewById(R.id.imp);
                TextView QscoreTV = (TextView) view.findViewById(R.id.scoreTV);
                QscoreTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
                QscoreTV.setTextColor(ContextCompat.getColor(context, R.color.gray_text));
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
                if (surveyHelperNew.IS_SURVEY_ANSWER_MANDATORY && isSaveClicked) {
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
                if (questBO.getIsScore() == 1 && surveyHelperNew.SHOW_SCORE_IN_SURVEY) {
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
                if (surveyHelperNew.IS_SURVEY_ANSWER_MANDATORY
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
                    case "EMAIL":
                        showEditText(3, answerLayout, questBO, subQuestLayout);
                        break;
                    case "DATE":
                        showEditText(4, answerLayout, questBO, subQuestLayout);
                        break;
                    case "PH_NO":
                        showEditText(1, answerLayout, questBO, subQuestLayout);
                        break;
                    case "DECIMAL":
                        showEditText(5, answerLayout, questBO, subQuestLayout);
                        break;
                    case "BARCODE":
                        showBarcodeEditText(answerLayout, questBO, subQuestLayout, false);
                        break;
                    case "BARCODE_EDIT":
                        showBarcodeEditText(answerLayout, questBO, subQuestLayout, true);
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
        if (!(questBO.getSelectedAnswer().isEmpty() && !questBO.getSelectedAnswer().contains(getResources().
                getString(R.string.plain_select)))
                || (!questBO.getSelectedAnswerIDs().isEmpty() && !questBO.getSelectedAnswerIDs().contains(-1))) {
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
                                    + (bmodel.retailerMasterBO.getRetailerID() == null
                                    ? 0 : bmodel.retailerMasterBO.getRetailerID()) + "_"
                                    + questBO.getSurveyid() + "_"
                                    + questBO.getQuestionID() + "_"
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID)
                                    + ".jpg";
                            try {
                                if (i == 0) {
                                    questBO.setTempImagePath((questBO.getImage1Path() != null && questBO.getImage1Path().length() > 0 && isFileExist(questBO.getImage1Path())) ? questBO.getImage1Path() : (questBO.getTempImagePath() != null && questBO.getTempImagePath().length() > 0 && isFileExist(questBO.getTempImagePath())) ? questBO.getTempImagePath() : "");
                                } else {
                                    questBO.setTempImagePath((questBO.getImage2Path() != null && questBO.getImage2Path().length() > 0 && isFileExist(questBO.getImage2Path())) ? questBO.getImage2Path() : (questBO.getTempImagePath() != null && questBO.getTempImagePath().length() > 0 && isFileExist(questBO.getTempImagePath())) ? questBO.getTempImagePath() : "");
                                }
                                Thread.sleep(10);
                                Intent intent = new Intent(
                                        context,
                                        CameraActivity.class);
                                String path = FileUtils.photoFolderPath + "/" + imageName;
                                if (i == 0) {
                                    questBO.setImage1Path(path);
                                    questBO.setImage1Captured(true);
                                } else {
                                    questBO.setImage2Path(path);
                                    questBO.setImage2Captured(true);
                                }
                                Log.e("TakenPath", path);
                                intent.putExtra(CameraActivity.QUALITY, 40);
                                intent.putExtra(CameraActivity.PATH, path);
                                startActivityForResult(intent,
                                        CAMERA_REQUEST_CODE);
                            } catch (Exception e) {
                                Commons.printException("" + e);
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                            context,
                            getResources()
                                    .getString(
                                            R.string.please_select_atleast_one_sku),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(
                        context,
                        getResources()
                                .getString(
                                        R.string.sdcard_is_not_ready_to_capture_img),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            String qType = questBO.getQuestionType();
            if ("OPT".equals(qType) || "MULTISELECT".equals(qType) || "POLL".equals(qType)) {
                Toast.makeText(
                        context,
                        getResources()
                                .getString(
                                        R.string.selectoptionforphoto),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                        context,
                        getResources()
                                .getString(
                                        R.string.answer_to_take_photo),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showEditText(int i, LinearLayout answerLL,
                              final QuestionBO mCurrentQuestionBO, final LinearLayout subQLL) {
        answerLL.removeAllViews();
        subQLL.removeAllViews();
        final EditText et = (EditText) ((Activity) context).getLayoutInflater().inflate(R.layout.survey_dit_text, null);
        if (!mCurrentQuestionBO.getSelectedAnswer().isEmpty())
            et.setText(mCurrentQuestionBO.getSelectedAnswer().get(0));
        else
            et.setText("");
        et.setMinLines(1);
        et.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_primary));
        et.setMaxLines(1);
        et.setCursorVisible(true);
        LayoutParams lp = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        et.setLayoutParams(lp);
        et.setPadding(15, 7, 7, 7);
        et.setTextColor(Color.BLACK);
        if (i == 0) {
            et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
        }
        if (i == 1) {
            setFocusChangeListener(et, mCurrentQuestionBO.getMinValue(), mCurrentQuestionBO.getMaxValue());//For Min/Max value validation
            if (mCurrentQuestionBO.getPrecision() == 0) {
                et.setInputType(InputType.TYPE_CLASS_NUMBER);
            } else {
                et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            }
            int digitsBeforeZero = 8;
            if (mCurrentQuestionBO.getToValue() != null) {
                digitsBeforeZero = mCurrentQuestionBO.getToValue().length();
            }
            if (mCurrentQuestionBO.getFromValue() != null && !mCurrentQuestionBO.getFromValue().equals("")
                    && mCurrentQuestionBO.getToValue() != null && !mCurrentQuestionBO.getToValue().equals("")) {
                if (!et.getText().toString().isEmpty()) {
                    if (!isInRange(SDUtil.convertToFloat(mCurrentQuestionBO.getFromValue()), SDUtil.convertToFloat(mCurrentQuestionBO.getToValue()),
                            SDUtil.convertToFloat(et.getText().toString()))) {
                        et.setTextColor(Color.RED);
                    } else {
                        et.setTextColor(Color.BLACK);
                    }
                }
                et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!et.getText().toString().isEmpty()) {
                            if (!isInRange(SDUtil.convertToFloat(mCurrentQuestionBO.getFromValue()), SDUtil.convertToFloat(mCurrentQuestionBO.getToValue()),
                                    SDUtil.convertToFloat(et.getText().toString()))) {
                                et.setTextColor(Color.RED);
                            } else {
                                et.setTextColor(Color.BLACK);
                            }
                        }
                    }
                });

                InputFilter inputFilter[] = new InputFilter[1];

                if (mCurrentQuestionBO.getPrecision() > 0) {
                    inputFilter[0] = new DecimalDigitsInputFilter(digitsBeforeZero, mCurrentQuestionBO.getPrecision(),
                            0, 0, 0, "", "");
                } else {
                    inputFilter[0] = new InputFilter.LengthFilter(mCurrentQuestionBO.getToValue().length());
                }
                et.setFilters(inputFilter);
            } else if (mCurrentQuestionBO.getPrecision() > 0) {
                et.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(digitsBeforeZero, mCurrentQuestionBO.getPrecision(),
                        0, 0, 0, "", "")});
            }

        }

        if (i == 2) {
            et.setInputType(InputType.TYPE_CLASS_NUMBER);
            et.setFilters(new InputFilter[]{new InputFilterMinMax(0, 100)});
        }
        if (i == 3) {
            if (!isValidEmail(et.getText().toString())) {
                et.setTextColor(Color.RED);
            } else {
                et.setTextColor(Color.BLACK);
            }
            et.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
            et.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (!et.getText().toString().equals("")) {
                        if (!isValidEmail(et.getText().toString())) {
                            et.setTextColor(Color.RED);
                            //Toast.makeText(getContext(),"Kindly provide valid email for "+mCurrentQuestionBO.getQuestionDescription(),Toast.LENGTH_LONG).show();
                        } else {
                            et.setTextColor(Color.BLACK);
                        }
                    }


                }
            });
            InputFilter inputFilter = new InputFilter() {
                @Override
                public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                    for (int i = start; i < end; i++) {
                        String checkMe = String.valueOf(source.charAt(i));

                        if (!Pattern.compile("[a-zA-Z0-9.@]").matcher(checkMe).matches()) {
                            Log.d("", "invalid");
                            return "";
                        }
                    }
                    return null;
                }
            };
            et.setFilters(new InputFilter[]{inputFilter});
        }
        if (i == 4) {
            et.setFocusable(false);
            et.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_calendar_qn, 0, 0, 0);
            et.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    final Calendar calendar = Calendar.getInstance();
                    int yy = calendar.get(Calendar.YEAR);
                    int mm = calendar.get(Calendar.MONTH);
                    int dd = calendar.get(Calendar.DAY_OF_MONTH);
                    DatePickerDialog datePicker = new DatePickerDialog(context, R.style.DatePickerDialogStyle, new DatePickerDialog.OnDateSetListener() {
                        @Override
                        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                                    dayOfMonth);
                            et.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(),
                                    ConfigurationMasterHelper.outDateFormat));
                        }
                    }, yy, mm, dd);
                    DatePicker datePicker1 = datePicker.getDatePicker();
                    Calendar c = Calendar.getInstance();
                    if (mCurrentQuestionBO.getFromValue() != null && !mCurrentQuestionBO.getFromValue().equals("")) {
                        try {
                            String[] splitDates = mCurrentQuestionBO.getFromValue().split("/");
                            c.set(SDUtil.convertToInt(splitDates[0]), SDUtil.convertToInt(splitDates[1]) - 1, SDUtil.convertToInt(splitDates[2]));
                            //Date date = dateFormat.parse(mCurrentQuestionBO.getFromValue());
                            datePicker1.setMinDate(c.getTimeInMillis());
                            Calendar c1 = Calendar.getInstance();
                            splitDates = mCurrentQuestionBO.getToValue().split("/");
                            c1.set(Calendar.HOUR_OF_DAY, 23);
                            c1.set(Calendar.MINUTE, 59);
                            c1.set(Calendar.SECOND, 59);
                            c1.set(SDUtil.convertToInt(splitDates[0]), SDUtil.convertToInt(splitDates[1]) - 1, SDUtil.convertToInt(splitDates[2]));
                            //date = dateFormat.parse(mCurrentQuestionBO.getToValue());
                            datePicker1.setMaxDate(c1.getTimeInMillis());
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }

                    datePicker.show();
                }
            });
        }
        if (i == 5) {
            setFocusChangeListener(et, mCurrentQuestionBO.getMinValue(), mCurrentQuestionBO.getMaxValue());//For Min/Max value validation
            et.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            et.setFilters(new InputFilter[]{new DecimalDigitsInputFilter()});
        }
        et.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String s1 = s.toString().replaceAll("'", "''").trim();
                mCurrentQuestionBO.getSelectedAnswerIDs().clear();
                mCurrentQuestionBO.getSelectedAnswer().clear();

                if (!"".equals(s1) && s1.length() > 0) {
                    mCurrentQuestionBO.setSelectedAnswer(s1);
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


        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });
        answerLL.addView(et);
    }

    private void showBarcodeEditText(LinearLayout answerLL,
                                     final QuestionBO mCurrentQuestionBO, final LinearLayout subQLL, boolean isEditable) {

        answerLL.removeAllViews();
        subQLL.removeAllViews();
        RelativeLayout rootView = (RelativeLayout) ((Activity) context).getLayoutInflater().inflate(R.layout.survey_barcode_edittext, null);
        EditText et = rootView.findViewById(R.id.et_sur_barcode);
        ImageView img_barcode = rootView.findViewById(R.id.imageView_barcode_scan);

        if (!mCurrentQuestionBO.getSelectedAnswer().isEmpty())
            et.setText(mCurrentQuestionBO.getSelectedAnswer().get(0));
        else
            et.setText("");

        et.setMinLines(1);
        et.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.text_size_primary));
        et.setMaxLines(1);

        et.setPadding(15, 7, 7, 7);
        et.setTextColor(Color.BLACK);

        et.setFilters(new InputFilter[]{new InputFilter.LengthFilter(255)});
        et.setFocusable(isEditable);
        et.setClickable(true);

        et.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                String s1 = s.toString().replaceAll("'", "''").trim();
                mCurrentQuestionBO.getSelectedAnswerIDs().clear();
                mCurrentQuestionBO.getSelectedAnswer().clear();

                if (!"".equals(s1) && s1.length() > 0) {
                    mCurrentQuestionBO.setSelectedAnswer(s1);
                    mCurrentQuestionBO.setSelectedAnswerID(0);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            public void afterTextChanged(Editable s) {
            }
        });

        et.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    InputMethodManager imm = (InputMethodManager) textView.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(textView.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });

        img_barcode.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mBarcodeEditText = et;
                scanBarCode();
            }
        });

        answerLL.addView(rootView);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        ((Activity) context).getMenuInflater().inflate(
                R.menu.menu_survey, menu);
    }

    private boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
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
        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        if (bmodel.configurationMasterHelper.SHOW_PRODUCT_FILTER_IN_SURVEY && bmodel.productHelper.isFilterAvaiable(mMenuCode))
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        if (surveyHelperNew.SHOW_SMS_IN_SURVEY
                && bmodel.mSelectedActivityConfigCode
                .equalsIgnoreCase(surveyHelperNew.smsmenutype))
            menu.findItem(R.id.menu_msg).setVisible(true);
        else
            menu.findItem(R.id.menu_msg).setVisible(false);
        Commons.print("sms"
                + surveyHelperNew.SHOW_SMS_IN_SURVEY
                + bmodel.mSelectedActivityName);
        if (surveyHelperNew.SHOW_PHOTOCAPTURE_IN_SURVEY
                && bmodel.mSelectedActivityConfigCode
                .equalsIgnoreCase(surveyHelperNew.photocapturemenutype))
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

        menu.findItem(R.id.menu_select).setVisible(false);

        if (mMenuCode.equalsIgnoreCase("MENU_SURVEY_SW")
                || mMenuCode.equalsIgnoreCase("MENU_SURVEY01_SW")
                || mMenuCode.equalsIgnoreCase("MENU_SURVEY_BA_CS")
                || mMenuCode.equalsIgnoreCase("MENU_NEW_RET")) {
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_reason).setVisible(false);
        }

        if (drawerOpen || navDrawerOpen)
            menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButonClick();
            return true;
        } else if (i == R.id.menu_save) {
            if (!checkClicked) {
                checkClicked = true;
                if (surveyHelperNew.IS_SURVEY_ANSWER_ALL) {
                    if (surveyHelperNew.isAllAnswered()) {
                        if (surveyHelperNew.hasPhotoToSave())
                            captureSignature();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsRv.setAdapter(rvAdapter);
                            checkClicked = false;
                        }
                    } else {
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.pleaseanswerallthequestions), 0);
                        checkClicked = false;
                    }
                } else if (surveyHelperNew.IS_SURVEY_ANSWER_MANDATORY) {
                    if (surveyHelperNew.isMandatoryQuestionAnswered()) {
                        if (surveyHelperNew.hasPhotoToSave())
                            captureSignature();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsRv.setAdapter(rvAdapter);
                            checkClicked = false;
                        }
                    } else {
                        isSaveClicked = true;
                        questionsRv.setAdapter(rvAdapter);
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.please_answer_all_mandatory_questions),
                                0);
                        checkClicked = false;
                    }
                } else {
                    if (surveyHelperNew.hasDataToSave()) {
                        if (surveyHelperNew.hasPhotoToSave())
                            captureSignature();
                        else {
                            bmodel.showAlert(
                                    getResources().getString(R.string.take_photos_to_save), 0);
                            questionsRv.setAdapter(rvAdapter);
                            checkClicked = false;
                        }
                    } else {
                        bmodel.showAlert(
                                getResources().getString(R.string.no_data_tosave), 0);
                        questionsRv.setAdapter(rvAdapter);
                        checkClicked = false;
                    }
                }
            }
            return true;
        } else if (i == R.id.menu_joint_call_survey) {
            showSupervisiorAlert();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_msg) {
            surveyHelperNew.remarkDone = "Y";
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
            Intent intent = new Intent(context,
                    PhotoCaptureActivity.class);
            intent.putExtra("fromSurvey", true);
            startActivity(intent);
            ((Activity) context).overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            return true;
        } else if (i == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), mMenuCode);
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), mMenuCode)) {

                        if (!isPreVisit)
                            bmodel.saveModuleCompletion(mMenuCode, true);

                        if (!mMenuCode.equalsIgnoreCase("MENU_SURVEY_SW")
                                && !mMenuCode.equalsIgnoreCase("MENU_SURVEY01_SW")
                                && !mMenuCode.equalsIgnoreCase("MENU_SURVEY_BA_CS")) {

                            Intent intent = new Intent(context, HomeScreenTwo.class);

                            if (isPreVisit)
                                intent.putExtra("PreVisit", true);

                            startActivity(intent);
                        }
                        ((Activity) context).finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", mMenuCode);
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(((FragmentActivity) context).getSupportFragmentManager(), "ReasonDialogFragment");
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
        alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setTitle(null);
        alertBuilder.setSingleChoiceItems(supervisiorAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        UserMasterBO selectedId = supervisiorAdapter
                                .getItem(item);
                        selecteditem = item;
                        surveyHelperNew.mSelectedSuperVisiorID = selectedId
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
                surveyHelperNew
                        .loadSuperVisorSurveyAnswers(surveyHelperNew.mSelectedSuperVisiorID);
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(context);
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            onLoadQuestion(surveyHelperNew.mSelectedSurvey,
                    surveyHelperNew.mSelectedFilter);
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        surveyHelperNew.mSelectedFilter = id;
        mDrawerLayout.closeDrawers();
        onLoadQuestion(surveyHelperNew.mSelectedSurvey,
                surveyHelperNew.mSelectedFilter);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        loadQuestionFromFiveLevelFilter(
                surveyHelperNew.mSelectedSurvey,
                mProductId);
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mFilteredProductId = mProductId;
        mDrawerLayout.closeDrawers();
    }

    private class SaveSurveyTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if ("MENU_NEW_RET".equals(mMenuCode)) {
                    surveyHelperNew.saveAnswerNewRetailer(mMenuCode, screenMode, editRetailerId);
                } else {
                    surveyHelperNew.deleteUnusedImages();
                    surveyHelperNew.saveAnswer(mMenuCode);
                }
                bmodel.saveModuleCompletion(mMenuCode, true);
                return Boolean.TRUE;
            } catch (Exception e) {
                e.printStackTrace();
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            customProgressDialog(builder, getResources().getString(R.string.savingquestionanswers));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {

            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
            alertDialog.dismiss();
            surveyHelperNew.remarkDone = "N";
            checkClicked = false;
            String negBtn = null;
            if (bmodel.configurationMasterHelper.IS_SURVEY_PDF_SHARE)
                negBtn = getResources().getString(R.string.share);
            new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                    "", getResources().getString(R.string.saved_successfully),
                    false, getActivity().getResources().getString(R.string.ok),
                    negBtn, new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    questionsRv.invalidate();
                    Bundle extras = ((Activity) context).getIntent().getExtras();
                    //Enabled global survey will re-direct to next screen or else screen remains same
                    if (bmodel.configurationMasterHelper.IS_SURVEY_GLOBAL_SAVE || tabCount == 1) {
                        if (extras != null && "HomeScreenTwo".equals(mFrom)) {
                            Intent intent = new Intent(context, HomeScreenTwo.class);
                            intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));

                            if (isPreVisit)
                                intent.putExtra("PreVisit", true);

                            startActivity(intent);
                            ((Activity) context).finish();
                        } else if ("HomeScreen".equals(mFrom)) {
                            HomeScreenFragment currentFragment = (HomeScreenFragment) ((FragmentActivity) (context)).getSupportFragmentManager().findFragmentById(R.id.homescreen_fragment);
                            if (currentFragment != null) {
                                currentFragment.detach(mMenuCode);
                            }
                        }

                    }
                }
            }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {
                    textToPdf();
                }
            }).show();
        }
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // TODO Auto-generated method stub
    }

    private void loadQuestionFromFiveLevelFilter(int surveyId, int filteredProductId) {
        ArrayList<QuestionBO> items = new ArrayList<>();
        for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
            if (surBO.getSurveyID() == surveyHelperNew.mSelectedSurvey)
                items = surBO.getQuestions();
        }
        if (items == null || items.isEmpty())
            return;
        mQuestions = new ArrayList<>();
        for (QuestionBO question : items) {
            if (question.getSurveyid() == surveyId || surveyId == -1) {
                if ((question.getParentHierarchy() != null && question.getParentHierarchy().contains("/" + filteredProductId + "/"))
                        || filteredProductId == -1 && question.getIsSubQuestion() == 0) {
                    mQuestions.add(question);
                }
            }
        }
        SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(context);
        surveyHelperNew.setmQuestionData(mQuestions);
        rvAdapter = new QuestionAdapter();
        questionsRv.setAdapter(rvAdapter);
    }

    private void FiveFilterFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);
            FragmentManager fm = ((FragmentActivity) context)
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
                int input = SDUtil.convertToInt(dest.subSequence(0, dstart).toString() + source + dest.subSequence(dend, dest.length()));
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

    private String checkRange(String input, float minValue, float maxValue) {

        float inputValue = SDUtil.convertToFloat(input);
        if (inputValue <= minValue || inputValue >= maxValue) {
            return "";
        }

        return null;
    }

    public class DecimalDigitsInputFilter implements InputFilter {

        private final Pattern mPattern;
        private final float min;
        private final float max;
        private final int rangeCheckStartAt;
        private final String c;
        private final String endChar;


        public DecimalDigitsInputFilter(int digitsBeforeZero, int digitsAfterZero, float min, float max, int rangeCheckStartAt, String c, String end) {
            mPattern = Pattern.compile(String.format("[0-9]{0,%d}(\\.[0-9]{0,%d})?", digitsBeforeZero, digitsAfterZero));
            this.min = min;
            this.max = max;
            this.rangeCheckStartAt = rangeCheckStartAt;
            this.c = c;
            this.endChar = end;
        }

        private DecimalDigitsInputFilter() {
            mPattern = Pattern.compile(String.format("[0-9]+(\\.[0-9]{0,%d})?", bmodel.configurationMasterHelper.VALUE_PRECISION_COUNT));
            this.min = 0;
            this.max = 0;
            this.rangeCheckStartAt = 0;
            this.c = "";
            this.endChar = "";
        }

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            Matcher matcher = mPattern.matcher(createResultString(source, start, end, dest, dstart, dend));

            if (!matcher.matches()) {
                return "";
            }
            return null;
        }


        private String createResultString(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
            String sourceString = source.toString();
            String destString = dest.toString();
            return destString.substring(0, dstart) + sourceString.substring(start, end) + destString.substring(dend);
        }
    }

    private boolean isInRange(float a, float b, float c) {
        return b > a ? c >= a && c <= b : c >= b && c <= a;

    }

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

    private void onBackButonClick() {

        if (surveyHelperNew.hasDataToSave()) {
            showAlert();
        } else {
            if (mFrom.equalsIgnoreCase("HomeScreenTwo")) {
                Intent intent = new Intent(context, HomeScreenTwo.class);
                if (isFromChild)
                    intent.putExtra("isStoreMenu", true);

                if (isPreVisit)
                    intent.putExtra("PreVisit", true);

                startActivity(intent);
            }
            ((Activity) context).finish();
            ((Activity) context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void showAlert() {
        CommonDialog dialog = new CommonDialog(context, "",
                getResources().getString(R.string.doyouwantgoback), getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if (mFrom.equalsIgnoreCase("HomeScreenTwo")) {
                    Intent intent = new Intent(context, HomeScreenTwo.class);
                    if (isFromChild)
                        intent.putExtra("isStoreMenu", true);

                    if (isPreVisit)
                        intent.putExtra("PreVisit", true);

                    startActivity(intent);
                }
                ((Activity) context).finish();
                ((Activity) context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        }, getResources().getString(R.string.cancel), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

    /**
     * To capture signature for the survey.
     */
    private void captureSignature() {
        SurveyBO surveyBO = hasSignature();
        if (surveyBO != null) {
            String imagename = "SUR_SGN_" + bmodel.getAppDataProvider().getUser().getUserid() + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + ".jpg";
            String serverPath = "Survey/"
                    + bmodel.getAppDataProvider().getUser().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.getAppDataProvider().getUser().getUserid() + "/" + imagename;
            surveyHelperNew.setSignaturePath(serverPath);
            CaptureSignatureDialog signatureDialog = new CaptureSignatureDialog(getActivity(), getResources().getString(R.string.signature_label),
                    getResources().getString(R.string.please_sign_below), getResources().getString(R.string.save), new CaptureSignatureDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick(boolean isSignCaptured) {
                    if (isSignCaptured)
                        new SaveSurveyTask().execute();
                    else
                        Toast.makeText(getActivity(), getResources().getString(R.string.sign_mandatory), Toast.LENGTH_SHORT).show();
                }
            },
                    getResources().getString(R.string.cancel), new CaptureSignatureDialog.NegativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {
                    checkClicked = false;
                }
            }, imagename, surveyBO.getSignaturePath(), FileUtils.photoFolderPath + "/");
            signatureDialog.show();
        } else {
            new SaveSurveyTask().execute();
        }
    }

    /**
     * Whether Signature required for the survey or not
     *
     * @return true or false
     */
    private SurveyBO hasSignature() {
        for (SurveyBO sBO : surveyHelperNew.getSurvey()) {
            if (sBO.isSignatureRequired())
                return sBO;
        }
        return null;
    }

    /**
     * After the survey gets saved, it will get shared through email as PDF.
     */
    private void textToPdf() {
        try {
            if (FileUtils.createFilePathAndFolder(getActivity())) {
                String pdfName = "/Survey_" + surveyBO.getSurveyID() + "_" + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_PLAIN) + ".pdf";
                boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(FileUtils.photoFolderPath, 1, pdfName);

                if (mIsFileAvailable)
                    FileUtils.deleteFiles(FileUtils.photoFolderPath, pdfName);

                PDFGenerator pdfGenerator = new PDFGenerator(FileUtils.photoFolderPath, pdfName, FileUtils.photoFolderPath, bmodel.getAppDataProvider().getUser().getDistributorName(),
                        bmodel.getAppDataProvider().getUser().getDistributorAddress1(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), bmodel.getAppDataProvider().getUser().getUserName(),
                        bmodel.getAppDataProvider().getRetailMaster().getRetailerName());


                writeSurvey(pdfGenerator);
                pdfGenerator.createPdf();
                AppUtils.sendEmail(getActivity(), FileUtils.photoFolderPath + pdfName, "", new String[]{}, "Send email...");
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * To generate survey content for PDF creation.
     *
     * @param pdfGenerator - IVY Lib class used to generate PDF.
     */
    private void writeSurvey(PDFGenerator pdfGenerator) {
        int qNo;
        for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
            Phrase heading = pdfGenerator.addPhrase(surBO.getSurveyName(), PDFGenerator.FONT_BOLD);
            Paragraph headerPara = pdfGenerator.addParagraph(PDFGenerator.ALIGNMENT_CENTER);
            headerPara.add(heading);
            headerPara.add(PDFGenerator.NEW_LINE);
            pdfGenerator.getPdfWordCell().addElement(headerPara);
            qNo = 1;
            for (QuestionBO question : surBO.getQuestions()) {
                String contentStr = qNo + "." + question.getQuestionDescription();
                Phrase contentPhrase = pdfGenerator.addPhrase(contentStr, PDFGenerator.FONT_NORMAL);
                Paragraph contentPara = pdfGenerator.addParagraph(PDFGenerator.ALIGNMENT_JUSTIFIED);
                contentPara.add(PDFGenerator.NEW_LINE);
                contentPara.add(contentPhrase);
                pdfGenerator.getPdfWordCell().addElement(contentPara);

                int answerSize = question.getSelectedAnswerIDs().size();
                for (int j = 0; j < answerSize; j++) {
                    String answerStr = PDFGenerator.addSpace(String.valueOf(qNo).length()) + question
                            .getSelectedAnswer().get(j);
                    Phrase answerPhrase = pdfGenerator.addPhrase(answerStr, PDFGenerator.FONT_BOLD_SMALL);
                    Paragraph answerPara = pdfGenerator.addParagraph(PDFGenerator.ALIGNMENT_JUSTIFIED);
                    answerPara.add(answerPhrase);
                    answerPara.add(PDFGenerator.NEW_LINE);
                    pdfGenerator.getPdfWordCell().addElement(answerPara);
                    if (question.getImageNames() != null && !question.getImageNames().isEmpty()) {
                        pdfGenerator.setImageList(new ArrayList<>());
                        for (String imagePath : question.getImageNames()) {
                            String[] splitPath = imagePath.split("/");
                            String imagename = splitPath[splitPath.length - 1];
                            pdfGenerator.getImageList().add(imagename);
                        }
                        pdfGenerator.addImagesToPdf();
                    }
                }
                qNo++;
            }
        }
        if (!StringUtils.isNullOrEmpty(surveyBO.getSignaturePath())) {
            String[] splitPath = surveyBO.getSignaturePath().split("/");
            String imagename = splitPath[splitPath.length - 1];
            pdfGenerator.addSignature(FileUtils.photoFolderPath + "/" + imagename, PDFGenerator.ALIGNMENT_CENTER, 25);
        }

    }

    private void scanBarCode() {
        {
            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        SurveyActivityNewFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
        }

    }

    private void setFocusChangeListener(EditText editText, int min, int max) {
        editText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    String val = ((EditText) v).getText().toString();
                    if (!TextUtils.isEmpty(val)) {
                        if (Integer.valueOf(val) < min || Integer.valueOf(val) > max) {
                            ((EditText) v).setError(String.format(getResources().getString(R.string.input_char_validation_msg), min, max));
                        }

                    }
                }
            }
        });
    }

}
