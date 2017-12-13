package com.ivy.cpg.view.planogram;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.io.File;
import java.io.FileInputStream;
import java.util.Vector;

public class CounterPlanogramFragment extends IvyBaseFragment implements
        OnClickListener {

    private static final String TAG = "CounterPlanogramFrag";
    private static final int CAMERA_REQUEST_CODE = 1;
    private RadioButton rdYes, rdNo;
    private String imageFileName = "";
    boolean is_photo_there = false, is_supervisor = false;

    private BusinessModel mBModel;
    private Vector<CounterPlanoGramBO> vPlanogram = new Vector<>();
    private ImageView imgFromServer, imgFromCamera, imgFromSuperior;
    private String imgServerPath = "", imgCameraPath = "",
            imgSuperCameraPath = "";
    private View view;
    private TextView text_imageName;
    private ArrayAdapter<CounterPlanoGramBO> filterAdapter;
    private int mSelectedItem;
    private int selectedImageId;
    private String selectedImageName = "";
    private LinearLayout reason_Layout;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private Spinner adherence_reason;
    private String photoNamePath;
    private boolean isDialogPopup;
    private int counterId = -1;
    PlanoGramHelper mPlanoGramHelper;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        mPlanoGramHelper = PlanoGramHelper.getInstance(getActivity());
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_planogram, container, false);

        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            counterId = extras.getInt("counterId");
        }

        // download data for planogram
        vPlanogram = mPlanoGramHelper.getCsPlanogramMaster();
        text_imageName = (TextView) view.findViewById(R.id.txt_pgfiltername);
        reason_Layout = (LinearLayout) view.findViewById(R.id.reason_layout);
        adherence_reason = (Spinner) view.findViewById(R.id.sp_reason);

        ImageView imageView_audit = (ImageView) view.findViewById(R.id.btn_audit);
        imageView_audit.setScaleType(ScaleType.FIT_XY);

        imgFromServer = (ImageView) view
                .findViewById(R.id.planogram_image_view);
        imgFromServer.setScaleType(ScaleType.FIT_XY);
        imgFromServer.setOnClickListener(this);

        imgFromCamera = (ImageView) view.findViewById(R.id.capture_image_view);
        imgFromCamera.setScaleType(ScaleType.FIT_XY);
        imgFromCamera.setOnClickListener(this);

        imgFromSuperior = (ImageView) view
                .findViewById(R.id.supervisor_image_view);
        imgFromSuperior.setScaleType(ScaleType.FIT_XY);
        imgFromSuperior.setOnClickListener(this);

        rdYes = (RadioButton) view.findViewById(R.id.yes);
        rdNo = (RadioButton) view.findViewById(R.id.no);
        rdYes.setEnabled(false);
        rdNo.setEnabled(false);
        isDialogPopup = false;
        photoNamePath = HomeScreenFragment.photoPath + "/";


        rdYes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    rdNo.setChecked(false);
                    mPlanoGramHelper.setCSImageAdherence(
                            "1" , selectedImageId);
                    reason_Layout.setVisibility(View.GONE);
                }
            }
        });

        rdNo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    rdYes.setChecked(false);
                    mPlanoGramHelper.setCSImageAdherence(
                            "0" , selectedImageId);
                    reason_Layout.setVisibility(View.VISIBLE);
                }
            }
        });

        loadReason();
        adherence_reason.setAdapter(spinnerAdapter);
        adherence_reason
                .setOnItemSelectedListener(new OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> arg0, View arg1,
                                               int arg2, long arg3) {
                        ReasonMaster reString = (ReasonMaster) arg0
                                .getSelectedItem();
                        mPlanoGramHelper.setCSImageAdherenceReason(
                                reString.getReasonID() , selectedImageId);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {

                    }
                });

        // chooseFilterHeading();
        selectedImageId = mPlanoGramHelper.getCsPlanogramMaster().get(0).getImageId();
        selectedImageName = mPlanoGramHelper.getCsPlanogramMaster().get(0).getImageName();

        if (savedInstanceState != null) {
            selectedImageId = savedInstanceState.getInt("id");
            selectedImageName = savedInstanceState.getString("name");
            updateImageView(selectedImageId, selectedImageName);
        }


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (ReasonMaster temp : mBModel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("POG")
                    || temp.getReasonCategory().equalsIgnoreCase("NONE")) {
                spinnerAdapter.add(temp);

            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            filterAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.select_dialog_singlechoice);
            for (CounterPlanoGramBO planogramBO : vPlanogram) {
                filterAdapter.add(planogramBO);
            }
            updateImageView(selectedImageId, selectedImageName);

        } catch (Exception e){
            Commons.printException(e);
        }

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(mPlanoGramHelper.mSelectedActivityName);

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // / if statement to make sure the alert is displayed only for the first
        // time
        if (mPlanoGramHelper.getCsPlanogramMaster().size() != 1)
            if (isDialogPopup)
                showImageFilter();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", selectedImageId);
        outState.putString("name", selectedImageName);

    }

    public void setImageFromCamera(int selectedImageId) { // set Image of
        // the camera
        for (final CounterPlanoGramBO planogramBO : vPlanogram) {

            if(selectedImageId == planogramBO.getImageId()) {

                if (!planogramBO.getPlanogramCameraImgName().equals("")) {
                    String path = photoNamePath
                            + planogramBO.getPlanogramCameraImgName();
                    if (mBModel.isImagePresent(path)) {
                        Uri uri = mBModel
                                .getUriFromFile(path);
                        imgCameraPath = path;
                        imgFromCamera.setImageURI(uri);

                        enableAdherence();
                    }
                } else {
                    imgFromCamera
                            .setImageResource(R.drawable.no_image_available);
                }

                if (planogramBO.getAdherence() != null
                        && planogramBO.getAdherence().equals("1")) {
                    rdYes.setChecked(true);
                    planogramBO.setAdherence("1");
                } else if (planogramBO.getAdherence() != null
                        && planogramBO.getAdherence().equals("0")) {
                    rdNo.setChecked(true);
                    planogramBO.setAdherence("0");
                    adherence_reason.setSelection(getStatusIndex(planogramBO
                            .getReasonID()));
                    adherence_reason.setSelected(true);
                }
                break;
            }
        }
    }

    /**
     * Show the image from server which was downloaded according to the product
     * and location wise
     */
    public void setImageFromServer(int selectedImageId) {
        for (final CounterPlanoGramBO planogramBO : vPlanogram) {
            if(selectedImageId == planogramBO.getImageId()) {
                if (planogramBO.getImageName() != null) {
                    File imgFile = new File(getActivity().getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + mBModel.userMasterHelper.getUserMasterBO()
                            .getUserid()
                            + DataMembers.DIGITAL_CONTENT
                            + "/"
                            + DataMembers.PLANOGRAM
                            + "/"
                            + planogramBO.getImageName());
                    Commons.print("img name" + planogramBO.getImageName());
                    if (imgFile.exists()) {
                        try {
                            Commons.print("img path"
                                    + imgFile.getAbsolutePath());
                            Bitmap myBitmap = decodeFile(imgFile);
                            imgServerPath = imgFile.getAbsolutePath();
                            imgFromServer.setImageBitmap(myBitmap);
                            /*
                             * Bitmap myBitmap =
							 * BitmapFactory.decodeFile(imgFile
							 * .getAbsolutePath()); imgServerPath =
							 * imgFile.getAbsolutePath();
							 * imgFromServer.setImageBitmap(myBitmap);
							 */

                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    } else {
                        imgFromServer
                                .setImageResource(R.drawable.no_image_available);
                    }
                }
                if (is_supervisor) {
                    if (planogramBO.getPlanogramSuperCameraPath() != null) {
                        File imgFile = new File(getActivity()
                                .getExternalFilesDir(
                                        Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + mBModel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + planogramBO.getPlanogramSuperCameraPath());
                        if (imgFile.exists()) {
                            Bitmap myBitmap = BitmapFactory.decodeFile(imgFile
                                    .getAbsolutePath());
                            imgSuperCameraPath = imgFile.getAbsolutePath();
                            imgFromSuperior.setImageBitmap(myBitmap);
                        }
                    }
                }

                break;
            }

        }
    }

    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        int IMAGE_MAX_SIZE = 500;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.ceil(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return b;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.planogram_image_view) {
            openImage(imgServerPath);
        } else if (v.getId() == R.id.capture_image_view) {
            openImage(imgCameraPath);
        } else if (v.getId() == R.id.supervisor_image_view) {
            openImage(imgSuperCameraPath);
        }
    }

    /**
     * method calls while after capture the photo and return to this page and
     * save the image
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                searchAndUpdateImage();

            }
        }
    }

    /**
     *
     */
    private void takePhoto() {
        if (mBModel.isExternalStorageAvailable()) {


            imageFileName = "CPL_" + mBModel.retailerMasterBO.getRetailerID()
                    + "_" + counterId + "_" + selectedImageId + "_"
                    + Commons.now(Commons.DATE_TIME) + "_img.jpg";


            String path = photoNamePath + imageFileName;
            try {


                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), path);
                is_photo_there = true;
                startActivityForResult(intent, CAMERA_REQUEST_CODE);

            } catch (Exception e) {
                Commons.print("error opening camera");
                Commons.printException(e);
            }
        } else {
            Toast.makeText(
                    getActivity(),
                    getResources().getString(
                            R.string.unable_to_access_the_sdcard),
                    Toast.LENGTH_SHORT).show();
        }
    }

	/*
     * private void chooseFilterHeading() {
	 * 
	 * Vector<ChildLevelBo> items = mPlanoGramHelper
	 * .getLevelMaster(); if (items == null) { return; }
	 * 
	 * if (filterId != 0) return;
	 * 
	 * int siz = items.size(); for (int i = 0; i < siz; ++i) { ChildLevelBo ret
	 * = (ChildLevelBo) items.elementAt(i); filter_Heading =
	 * ret.getProductLevel(); filterId = ret.getProductId(); break; } }
	 */

    public void searchAndUpdateImage() {
        String path = imageFileName;
        Commons.print(TAG+  ",Img " +  imageFileName);
        mPlanoGramHelper.setCSImagePath(path, selectedImageId);

        enableAdherence();
        clearImageViews();

        setImageFromCamera(selectedImageId);
    }

    public void updateImageView(int selectedImageId, String mSelectedImageName) {
        try {


            text_imageName.setText(mSelectedImageName);
            // Change the Brand button Name
            clearImageViews();
            setSuperImageVisible();
            // Clear the screen contents
            imgFromCamera.setImageResource(R.drawable.no_image_available);
            imgFromServer.setImageResource(R.drawable.no_image_available);
            rdYes.setChecked(false);
            rdNo.setChecked(false);
            reason_Layout.setVisibility(View.GONE);
            adherence_reason.setSelection(0);

            setImageFromServer(selectedImageId);
            setImageFromCamera(selectedImageId);


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // force the garbage collector to run
        System.gc();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_counter_inventory, menu);

    }

    // /**
    // * Called whenever we call invalidateOptionsMenu()
    // */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
//            startActivity(new Intent(getActivity(),
//                    CSHomeScreen.class));
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_next) {
            nextButtonClick();
            return true;
        } else if (i == R.id.menu_photo) {
            try {
                for (final CounterPlanoGramBO planogramBO : vPlanogram) {
                    if (planogramBO.getImageId() == selectedImageId) {
                        if (planogramBO.getImageName() != null) {
                            File imgFile = new File(getActivity()
                                    .getExternalFilesDir(
                                            Environment.DIRECTORY_DOWNLOADS)
                                    + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO()
                                    .getUserid()
                                    + DataMembers.DIGITAL_CONTENT
                                    + "/"
                                    + DataMembers.PLANOGRAM
                                    + "/"
                                    + planogramBO.getImageName());
                            if (imgFile.exists()) {
                                takePhoto();

                            } else {
                                Toast.makeText(getActivity(),
                                        "Server image not available",
                                        Toast.LENGTH_SHORT).show();

                            }
                        }
                    }
                }
            } catch (Exception e) {
                Commons.printException(""+e);
            }

			/*
			 * } else Toast.makeText(getActivity(),
			 * getResources().getString(R.string.data_not_mapped),
			 * Toast.LENGTH_SHORT).show();
			 */

            return true;
        } else if(i == R.id.menu_filter) {
            showImageFilter();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void nextButtonClick() {
        try {
            if (checkForValidation()) {
                new SaveAsyncTask().execute();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.please_filldetails),
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if(counterId > 0) {
                    mPlanoGramHelper.saveCounterPlanoGram(counterId);
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
           /* progressDialogue = ProgressDialog.show(getActivity(),
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
            // progressDialogue.dismiss();
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {


//                startActivity(new Intent(getActivity(),
//                        CSHomeScreen.class));
                getActivity().finish();


            }

        }

    }

    public void ClearAll() {
        imgFromCamera.setImageResource(R.drawable.no_image_available);
        imgFromServer.setImageResource(R.drawable.no_image_available);
        rdYes.setChecked(false);
        rdNo.setChecked(false);
        rdYes.setEnabled(false);
        rdNo.setEnabled(false);
        reason_Layout.setVisibility(View.GONE);
        adherence_reason.setSelection(0);

    }

    public void enableAdherence() {
        rdYes.setEnabled(true);
        rdYes.setChecked(false);
        rdNo.setEnabled(true);
        rdNo.setChecked(false);
    }

    public void clearImageViews() {
        imgFromServer.setImageResource(0);
        imgFromCamera.setImageResource(0);
        imgServerPath = "";
        imgCameraPath = "";
        imgSuperCameraPath = "";
    }

    public void setSuperImageVisible() {
        imgFromSuperior.setVisibility(View.GONE);
        is_supervisor = false;
        for (final CounterPlanoGramBO planogramBO : vPlanogram) {
            if(planogramBO.getImageId() == selectedImageId) {
                if (planogramBO.getPlanogramSuperCameraPath() != null
                        && !planogramBO.getPlanogramSuperCameraPath().equals(
                        "null")
                        && planogramBO.getPlanogramSuperCameraPath().trim()
                        .length() > 0) {
                    is_supervisor = true;
                    imgFromSuperior.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    public boolean checkForValidation() {
        for (final CounterPlanoGramBO planogramBO : vPlanogram) {
            if (planogramBO.getAdherence() != null) {
                return true;
            }
        }
        return false;

    }

    /*
     * Open the Image in Photo Gallery while onClick
     */
    public void openImage(String fileName) {
        if (fileName.trim().length() > 0) {
            try {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse("file://" + fileName),
                        "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unloadimage),
                    Toast.LENGTH_SHORT).show();
        }
    }



    int getStatusIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster ReasonBO = spinnerAdapter.getItem(i);
            if (ReasonBO != null) {
                if (ReasonBO.getReasonID().equals(reasonId))
                    return i;
            }
        }
        return -1;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    private void showImageFilter () {
        AlertDialog.Builder builder;
        isDialogPopup = true;
        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(filterAdapter, mSelectedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        CounterPlanoGramBO selectedId = filterAdapter
                                .getItem(item);
                        mSelectedItem = item;
                        ClearAll();
                        imgServerPath = "";
                        imgCameraPath = "";
                        if (selectedId != null) {
                            selectedImageId = selectedId
                                    .getImageId();
                            selectedImageName = selectedId.getImageName();
                        }
                        text_imageName.setText(selectedImageName);
                        setImageFromServer(selectedImageId);
                        setImageFromCamera(selectedImageId);
                        dialog.dismiss();

                    }
                });

        mBModel.applyAlertDialogTheme(builder);
    }
}
