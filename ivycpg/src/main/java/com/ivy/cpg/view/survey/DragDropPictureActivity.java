package com.ivy.cpg.view.survey;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

public class DragDropPictureActivity extends IvyBaseActivityNoActionBar implements DragDropListener {

    protected BusinessModel bmodel;
    protected Toolbar toolbar;
    protected String questionDesc;
    protected RecyclerView imageRecyclerView, thumbnailRecyclerView;
    protected TextView TVQuestion;
    protected int brandId, questionId, minPhoto, maxPhoto, initThumbnailSize, initImgSrcSize, selectedSurveyId;
    protected QuestionBO questionBO, surveyPhcapture;
    protected SurveyHelperNew surveyHelperNew;
    protected ArrayList<QuestionBO> mQuestionData = new ArrayList<>();
    protected ArrayList<String> imageSrc = new ArrayList<>();
    protected ArrayList<String> thumbnailSrc = new ArrayList<>();
    private static final int CAMERA_REQUEST_CODE = 1;
    private String imageName = "", path = "", pathSrc = "";
    protected FloatingActionButton fabCam;
    protected ListAdapter topListAdapter, bottomListAdapter;
    //Bottom Layout
    protected LinearLayout layout1, layout2, layout3, layout4;
    protected TextView TVMin, TVMinTitle, TVMaxTitle, TVMax;
    protected Button BTSave;
    protected Boolean isMultiPhotoCaptureEnabled=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setContentView(R.layout.activity_drag_drop_picture);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        questionDesc = getIntent().getStringExtra("QuestionDesc");
        questionId = getIntent().getIntExtra("QuestiionId", -1);
        brandId = getIntent().getIntExtra("BrandId", -1);
        selectedSurveyId = getIntent().getIntExtra("SurveyId", -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }
        setScreenTitle("Survey Gallery");

        path = "/" + "Survey" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/";

        if (surveyHelperNew.ENABLE_MULTIPLE_PHOTO)
            isMultiPhotoCaptureEnabled=true;

        initViews();
        initTopRecyclerView();
        initBottomRecyclerView();
        processView();
        initImgSrcSize = imageSrc.size();
        initThumbnailSize = thumbnailSrc.size();

    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        //processView();
    }

    protected void initViews() {
        TVQuestion = (TextView) findViewById(R.id.txt_question);
        TVMin = (TextView) findViewById(R.id.totalValue);
        TVMax = (TextView) findViewById(R.id.lcp);
        TVMaxTitle = (TextView) findViewById(R.id.totalText);
        TVMinTitle = (TextView) findViewById(R.id.lpc_title);

        layout1 = (LinearLayout) findViewById(R.id.ll_value);
        layout2 = (LinearLayout) findViewById(R.id.ll_lpc);
        layout3 = (LinearLayout) findViewById(R.id.ll_totqty);
        layout4 = (LinearLayout) findViewById(R.id.ll_dist);
        BTSave = (Button) findViewById(R.id.btn_next);
        layout1.setVisibility(View.VISIBLE);
        layout2.setVisibility(View.VISIBLE);
        layout3.setVisibility(View.GONE);
        layout4.setVisibility(View.GONE);
        TVMax.setGravity(Gravity.CENTER);
        TVMin.setGravity(Gravity.CENTER);
        TVMaxTitle.setText(getString(R.string.minPhoto));
        TVMinTitle.setText(getString(R.string.maxPhoto));
        BTSave.setText(getString(R.string.save));
        BTSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        TVQuestion.setText(questionDesc);
        fabCam = (FloatingActionButton) findViewById(R.id.fab_dragdrop_cam);
        fabCam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (maxPhoto > imageSrc.size()) {
                    photoFunction(questionBO, 0);
                } else {
                    bmodel.showAlert(
                            String.format(
                                    getResources()
                                            .getString(
                                                    R.string.You_have_already_taken_max_images),
                                    maxPhoto),
                            0);
                }
            }
        });
        BTSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFunction();
            }
        });
    }

    /*  ProcessView --> Process the Image data and check the questionID, brandId and SurveyID, based on this, store the
     maintain this data in separate arrayList "imageSrc" and rest of the image data are
     maintained in other arrayList "thumbnailSrc"   */
    protected void processView() {
        surveyHelperNew = SurveyHelperNew.getInstance(DragDropPictureActivity.this);
        imageSrc = new ArrayList<>();
        thumbnailSrc = new ArrayList<>();
        ArrayList<QuestionBO> items = new ArrayList<>();
        for (SurveyBO surBO : surveyHelperNew.getSurvey()) {
            items = surBO.getQuestions();
            for (int i = 0; i < items.size(); i++) {
                if (items.get(i).getImageNames() != null) {
                    if (items.get(i).getQuestionID() == questionId && items.get(i).getBrandID() == brandId && items.get(i).getSurveyid() == selectedSurveyId) {
                        for (int j = 0; j < items.get(i).getImageNames().size(); j++) {
                            imageSrc.add(items.get(i).getImageNames().get(j));
                        }
                    } else {
                        for (int j = 0; j < items.get(i).getImageNames().size(); j++) {
                            thumbnailSrc.add(items.get(i).getImageNames().get(j));
                        }
                    }
                }
            }

        }
        // Remove any duplicate entries present in the same arrayList if any
        imageSrc = removeDuplicates(imageSrc);
        thumbnailSrc = removeDuplicates(thumbnailSrc);
        //Images which present in the "imageSrc" should be removed in "thumbnailSrc" in order to avoid the duplication
        if (thumbnailSrc.size() != 0)
            thumbnailSrc.removeAll(imageSrc);
        //Getting the current Object using Getter & Setter
        questionBO = surveyHelperNew.getQuestionBODragDrop();
        minPhoto = questionBO.getMinPhoto();
        maxPhoto = questionBO.getMaxPhoto();
        TVMin.setText(minPhoto + "");
        TVMax.setText(maxPhoto + "");

        /* ListAdapter (Activity, List of Data, Type, Max. photo count)
          TYPE --> Based on this value, the UI will be changed in the Adapter,
          Set the DragListener to the recyclerView to enable the Drag&Drop Functionality. */
        topListAdapter = new ListAdapter(DragDropPictureActivity.this, imageSrc, this, 1, maxPhoto);
        imageRecyclerView.setAdapter(topListAdapter);
        imageRecyclerView.setOnDragListener(topListAdapter.getDragInstance());

        bottomListAdapter = new ListAdapter(DragDropPictureActivity.this, thumbnailSrc, this, 2, maxPhoto);
        thumbnailRecyclerView.setAdapter(bottomListAdapter);
        thumbnailRecyclerView.setOnDragListener(bottomListAdapter.getDragInstance());
    }

    private ArrayList<String> removeDuplicates(ArrayList<String> values) {
        HashSet<String> hashSet = new HashSet<String>();
        hashSet.addAll(values);
        values.clear();
        values.addAll(hashSet);
        return values;
    }

    // Returns the current data count and its PUBLIC because, it is accessed from Adapter as well,
    public int getArrayCount() {
        return imageSrc.size();
    }

    protected void initTopRecyclerView() {
        imageRecyclerView = (RecyclerView) findViewById(R.id.image_recyclerview);
        if (imageRecyclerView != null) {
            imageRecyclerView.setHasFixedSize(true);
        }
        final GridLayoutManager mLayoutManager = new GridLayoutManager(this, 3);
        imageRecyclerView.setLayoutManager(mLayoutManager);
        imageRecyclerView.setItemAnimator(new DefaultItemAnimator());
        // int spacingInPixels = getResources().getDimensionPixelSize(R.dimen.dimen_2dp);
        // imageRecyclerView.addItemDecoration(new GridSpacingItemDecoration(3, spacingInPixels, true, 0));
    }

    protected void initBottomRecyclerView() {
        thumbnailRecyclerView = (RecyclerView) findViewById(R.id.thumnail_recyclerview);
        if (thumbnailRecyclerView != null)
            thumbnailRecyclerView.setHasFixedSize(true);
        // Check the current device is Tablet or Mobile.
        //If tablet, column count is 5 and If Mobile, column count is 3
        if (isTabletDevice(DragDropPictureActivity.this)) {
            thumbnailRecyclerView.setLayoutManager(new GridLayoutManager(this, 5));
        } else {
            thumbnailRecyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        }
        thumbnailRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    @Override
    public void onBackPressed() {

    }

    // Static method to determine the device.
    private static boolean isTabletDevice(Context activityContext) {
        if (activityContext != null) {
            boolean device_large = ((activityContext.getResources()
                    .getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_LARGE || (activityContext
                    .getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) == Configuration.SCREENLAYOUT_SIZE_XLARGE);
            if (device_large) {
                DisplayMetrics metrics = new DisplayMetrics();
                Activity activity = (Activity) activityContext;
                activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);

                if (metrics.densityDpi == DisplayMetrics.DENSITY_DEFAULT
                        || metrics.densityDpi == DisplayMetrics.DENSITY_HIGH
                        || metrics.densityDpi == DisplayMetrics.DENSITY_MEDIUM
                        || metrics.densityDpi == DisplayMetrics.DENSITY_TV
                        || metrics.densityDpi == DisplayMetrics.DENSITY_XHIGH) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try {
            menu.removeItem(R.id.menu_dragdrop_cam);
            menu.removeItem(R.id.menu_dragdrop_save);
        } catch (Exception e) {
            Commons.printException(e);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            // Checking whether any transaction is made, if yes, get a confirmation from the user to close the activity.
            if (initImgSrcSize == imageSrc.size() && initThumbnailSize == thumbnailSrc.size())
                finish();
            else {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(DragDropPictureActivity.this);
                alertDialog.setMessage(getString(R.string.saveTransaction));
                alertDialog.setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        saveFunction();
                    }
                });
                alertDialog.setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alertDialog.setCancelable(false);
                alertDialog.show();
            }
        } else if (id == R.id.menu_dragdrop_cam) {
            if (maxPhoto > imageSrc.size()) {
                photoFunction(questionBO, 0);
            } else {
                bmodel.showAlert(
                        String.format(
                                getResources()
                                        .getString(
                                                R.string.You_have_already_taken_max_images),
                                maxPhoto),
                        0);
            }
        } else if (id == R.id.menu_dragdrop_save) {
            saveFunction();
        }

        return super.onOptionsItemSelected(item);
    }

    // Method to save the transactions
    //Append the path with the imageName to an ArrayList and send it back with the intent RESULT_OK
    private void saveFunction() {
        ArrayList<String> saveData = new ArrayList<>();
        for (int i = 0; i < imageSrc.size(); i++) {
            int index = imageSrc.get(i).indexOf("SVY_");
            String imageName = imageSrc.get(i).substring(index);
            saveData.add(path + imageName);
        }
        Intent intent = new Intent();
        intent.putExtra("savedData", saveData);
        setResult(RESULT_OK, intent);
        finish();
        Toast.makeText(this, getString(R.string.saved_successfully), Toast.LENGTH_SHORT).show();
    }

    //Method to remove the data from the Object.
    public boolean removeFromDataList(String removePath) {
        if (questionBO.getImageNames() != null) {
            if (questionBO.getImageNames().contains(removePath)) {
                questionBO.getImageNames().remove(questionBO.getImageNames().indexOf(removePath));
                return true;
            } else
                return false;
        }
        return false;
    }

    //Clear the present recyclerView and its adapter in order to increase the performance.
    protected void onDestroy() {
        super.onDestroy();
        if (imageRecyclerView != null) {
            imageRecyclerView.setItemAnimator(null);
            imageRecyclerView.setAdapter(null);
            imageRecyclerView = null;
        }
        if (thumbnailRecyclerView != null) {
            thumbnailRecyclerView.setItemAnimator(null);
            thumbnailRecyclerView.setAdapter(null);
            thumbnailRecyclerView = null;
        }
        unbindDrawables(findViewById(R.id.root));
        System.gc();
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

    @Override
    public void setEmptyListTop(boolean visibility) {

    }

    @Override
    public void setEmptyListBottom(boolean visibility) {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_dragdrop, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @SuppressLint("StringFormatInvalid")
    private void photoFunction(QuestionBO questBO, int i) {

        if (!questBO.getSelectedAnswer().isEmpty()
                || !questBO.getSelectedAnswerIDs().isEmpty()) {
            if (bmodel.isExternalStorageAvailable()) {
                if (questBO.getQuestionID() != 0) {
                    surveyPhcapture = questBO;
                    imageName = "SVY_"
                            + bmodel.retailerMasterBO
                            .getRetailerID() + "_"
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
                                DragDropPictureActivity.this,
                                CameraActivity.class);
                        String path = FileUtils.photoFolderPath + "/" + imageName;
                        if (i == 0) {
                            questBO.setImage1Path(path);
                            questBO.setImage1Captured(true);
                        } else {
                            questBO.setImage2Path(path);
                            questBO.setImage2Captured(true);
                        }

                        pathSrc = path;
                        Log.e("TakenPath", path);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                CAMERA_REQUEST_CODE);
                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }

                } else {
                    Toast.makeText(
                            DragDropPictureActivity.this,
                            getResources()
                                    .getString(
                                            R.string.please_select_atleast_one_sku),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(
                        DragDropPictureActivity.this,
                        getResources()
                                .getString(
                                        R.string.sdcard_is_not_ready_to_capture_img),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            String qType = questBO.getQuestionType();
            if ("OPT".equals(qType) || "MULTISELECT".equals(qType) || "POLL".equals(qType)) {
                Toast.makeText(
                        DragDropPictureActivity.this,
                        getResources()
                                .getString(
                                        R.string.selectoptionforphoto),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(
                        DragDropPictureActivity.this,
                        getResources()
                                .getString(
                                        R.string.please_answer_all_mandatory_questions),
                        Toast.LENGTH_SHORT).show();
            }
        }
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
                // surveyPhcapture.getImageNames().add(path + imageName);
                imageSrc.add(path + imageName);
                topListAdapter.notifyDataSetChanged();
                // processView();
                if(isMultiPhotoCaptureEnabled) {
                    Toast.makeText(DragDropPictureActivity.this, getString(R.string.continuousImageCapturedToast), Toast.LENGTH_SHORT).show();
                    //Checking whether the max. required photo is taken, if not, navigate again to to photoFunction()
                    if (imageSrc.size() < maxPhoto) {
                        photoFunction(surveyPhcapture, 0);
                    }
                }
            }
        }

    }

    private boolean isFileExist(String filePath) {
        try {
            File f = new File(filePath);
            return f.exists();
        } catch (Exception e) {
            return false;
        }
    }

    //RecyclerView column spacing
    private class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;
        private int headerNum;

        GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge, int headerNum) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
            this.headerNum = headerNum;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view) - headerNum; // item position

            if (position >= 0) {
                int column = position % spanCount; // item column

                if (includeEdge) {
                    outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                    outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

                    if (position < spanCount) { // top edge
                        outRect.top = spacing;
                    }
                    outRect.bottom = spacing; // item bottom
                } else {
                    outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                    outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                    if (position >= spanCount) {
                        outRect.top = spacing; // item top
                    }
                }
            } else {
                outRect.left = 0;
                outRect.right = 0;
                outRect.top = 0;
                outRect.bottom = 0;
            }
        }
    }
}
