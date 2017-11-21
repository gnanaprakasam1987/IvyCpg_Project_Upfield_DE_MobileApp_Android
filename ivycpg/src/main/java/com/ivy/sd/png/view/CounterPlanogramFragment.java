package com.ivy.sd.png.view;

import android.app.Activity;
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
import com.ivy.sd.png.bo.CounterPlanogramBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Vector;

public class CounterPlanogramFragment extends IvyBaseFragment implements
        OnClickListener {

    // Disable Motorola ET1 Scanner Plugin
    final String ACTION_SCANNERINPUTPLUGIN = "com.motorolasolutions.emdk.datawedge.api.ACTION_SCANNERINPUTPLUGIN";
    final String EXTRA_PARAMETER = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PARAMETER";
    final String DISABLE_PLUGIN = "DISABLE_PLUGIN";
    //

    private static final String TAG = "CounterPlanogramFrag";
    private static final int CAMERA_REQUEST_CODE = 1;
    // private RadioGroup rdGrp;
    private RadioButton rdYes, rdNo;
    private String imageFileName = "", filterName;
    // private File clientfolder, serverfolder;
    boolean is_photo_there = false, is_supervisor = false;
    ;
    private BusinessModel bmodel;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private Vector<CounterPlanogramBO> vPlanogram = new Vector<CounterPlanogramBO>();
    private ImageView imgFromServer, imgFromCamera, imgFromSuperior, imgAduit;
    private String checked = "N", imgServerPath = "", imgCameraPath = "",
            imgSuperCameraPath = "";
    private int IMAGE_MAX_SIZE = 500;
    private View view;
    private Context ctx;
    private TextView txt_pgtfilterName;
    private ArrayAdapter<CounterPlanogramBO> filterAdapter;
    private int selecteditem;
    private int selectedImageId;
    private String selectedImageName = "";
    private AlertDialog mTLReasonAlert;
    private LinearLayout reason_Layout, aduit_Layout;
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private Spinner adherence_reason;
    private int imageCount = 1;
    private String photoNamePath;
    private AlertDialog alert;
    private String from = "";
    private String calledBy = "0";
    private boolean isDialogPopup;
    private int counterId = -1;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_planogram, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        ctx = getActivity().getApplicationContext();

        Intent intent = getActivity().getIntent();
        Bundle extras = intent.getExtras();
        counterId = extras.getInt("counterId");

        // download data for planogram
        vPlanogram = bmodel.planogramMasterHelper.getCsPlanogramMaster();
        txt_pgtfilterName = (TextView) view.findViewById(R.id.txt_pgfiltername);
        reason_Layout = (LinearLayout) view.findViewById(R.id.reason_layout);
        aduit_Layout = (LinearLayout) view.findViewById(R.id.aduit_layout);
        adherence_reason = (Spinner) view.findViewById(R.id.sp_reason);

        imgAduit = (ImageView) view.findViewById(R.id.btn_audit);
        imgAduit.setScaleType(ScaleType.FIT_XY);

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
        /*
         * if(bmodel.configurationMasterHelper.PLANO_IMG_ADHE_VALIDATE) {
		 * rdYes.setEnabled(false); rdNo.setEnabled(false); } else {
		 * rdYes.setEnabled(true); rdNo.setEnabled(true); }
		 */
        isDialogPopup = false;
        photoNamePath = HomeScreenFragment.photoPath + "/";

        Commons.print("Photo Path, "+ "" + photoNamePath);

        rdYes.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked == true) {
                    rdNo.setChecked(false);
                    bmodel.planogramMasterHelper.setCSImageAdherence(
                            "1" , selectedImageId);
                    reason_Layout.setVisibility(View.GONE);
                }
            }
        });

        rdNo.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                // TODO Auto-generated method stub
                if (isChecked == true) {
                    rdYes.setChecked(false);
                    bmodel.planogramMasterHelper.setCSImageAdherence(
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
                        bmodel.planogramMasterHelper.setCSImageAdherenceReason(
                                reString.getReasonID() , selectedImageId);

                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> arg0) {
                        // TODO Auto-generated method stub

                    }
                });

        // chooseFilterHeading();
        selectedImageId = bmodel.planogramMasterHelper.getCsPlanogramMaster().get(0).getImageId();
        selectedImageName = bmodel.planogramMasterHelper.getCsPlanogramMaster().get(0).getImageName();

        if (savedInstanceState != null) {
            selectedImageId = savedInstanceState.getInt("id");
            selectedImageName = savedInstanceState.getString("name");
            updatebrandtext(selectedImageId , selectedImageName);
        }

        Commons.print("on create view ");

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
        spinnerAdapter = new ArrayAdapter<ReasonMaster>(getActivity(),
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
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
            filterAdapter = new ArrayAdapter<CounterPlanogramBO>(getActivity(),
                    android.R.layout.select_dialog_singlechoice);
            for(CounterPlanogramBO planogramBO : vPlanogram) {
                filterAdapter.add(planogramBO);
            }
            updatebrandtext(selectedImageId , selectedImageName);

        } catch (Exception e){
            Commons.printException(e);
        }

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);

    }

    @Override
    public void onPause() {
        if (mTLReasonAlert != null)
            mTLReasonAlert.dismiss();
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        // / if statement to make sure the alert is displayed only for the first
        // time
        if(bmodel.planogramMasterHelper.getCsPlanogramMaster().size() != 1)
            if (isDialogPopup)
                showImageFilter();


    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("id", selectedImageId);
        outState.putString("name", selectedImageName);

        // Commons.print("onSaveInstanceState " + filterId);
    }

    public void setImagefromCamera(int selectedImageId) { // set Image of
        // the camera
        for (final CounterPlanogramBO planogramBO : vPlanogram) {

            if(selectedImageId == planogramBO.getImageId()) {

                if (!planogramBO.getPlanogramCameraImgName().equals("")) {
                    String path = photoNamePath
                            + planogramBO.getPlanogramCameraImgName();
                    Commons.print("image path " + path);
                    if (bmodel.planogramMasterHelper.isImagePresent(path)) {
                        Uri uri = bmodel.planogramMasterHelper
                                .getUriFromFile(path);
                        imgCameraPath = path;
                        imgFromCamera.setImageURI(uri);

                        enableAdherence();
                        uri = null;
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
    public void setImagefromServer(int selectedImageId) {
        for (final CounterPlanogramBO planogramBO : vPlanogram) {
            if(selectedImageId == planogramBO.getImageId()) {
                if (planogramBO.getImageName() != null) {
                    File imgFile = new File(getActivity().getExternalFilesDir(
                            Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
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
                                + bmodel.userMasterHelper.getUserMasterBO()
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
                Commons.print(TAG+ ",Camers Activity : Sucessfully Captured.");
                searchAndUpdateImage();

            } else {
                Commons.print(TAG+ ",Camers Activity : Canceled");
            }
        }
    }

    /**
     *
     */
    private void takePhoto() {
        if (bmodel.isExternalStorageAvailable()) {


            imageFileName = "CPL_" + bmodel.retailerMasterBO.getRetailerID()
                    + "_" + counterId + "_" + selectedImageId + "_"
                    + Commons.now(Commons.DATE_TIME) + "_img.jpg";


            String path = photoNamePath + imageFileName;
            try {
                Intent i = new Intent();
                i.setAction(ACTION_SCANNERINPUTPLUGIN);
                i.putExtra(EXTRA_PARAMETER, DISABLE_PLUGIN);
                getActivity().sendBroadcast(i);

//                Thread.sleep(100);

                Intent intent = new Intent(getActivity(), CameraActivity.class);
                intent.putExtra(getResources().getString(R.string.quality), 40);
                intent.putExtra(getResources().getString(R.string.path), path);
//                intent.putExtra(
//                        getResources().getString(R.string.is_save_required), false);
                is_photo_there = true;
                startActivityForResult(intent, CAMERA_REQUEST_CODE);

            } catch (Exception e) {
                Commons.print("error opening camera");
                Commons.printException(e);
                // TODO: handle exception
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
	 * Vector<ChildLevelBo> items = bmodel.planogramMasterHelper
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
        bmodel.planogramMasterHelper.setCSImagePath(path , selectedImageId);

        enableAdherence();
        clearImageViews();

        setImagefromCamera(selectedImageId);
    }

    public void updatebrandtext(int selectedImageId , String seletedImageName) {
        try {

			/*
			 * if (bid == -1) { Toast.makeText(getActivity(),
			 * getResources().getString(R.string.data_not_mapped),
			 * Toast.LENGTH_SHORT).show(); } else {
			 */
            txt_pgtfilterName.setText(seletedImageName);
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

            // Clear Image views before setting the images
            // for category

            // locSelectionId =
            // bmodel.planogramMasterHelper.showFirstFilterName(filterId);
            setImagefromServer(selectedImageId);
            setImagefromCamera(selectedImageId);
            // }

        } catch (Exception e) {
            // TODO: handle exception
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
        }else if (i == R.id.menu_photo) {/*
			 * if (bmodel.getNoOfImages()) { Toast.makeText( getActivity(),
			 * getResources() .getString( R.string.
			 * its_highly_recommend_you_to_upload_the_images_before_capturing_new_image
			 * ), Toast.LENGTH_SHORT).show(); } else
			 */
            // if (mSelectedLocationId == locSelectionId) {
            try {
                for (final CounterPlanogramBO planogramBO : vPlanogram) {
                    if (planogramBO.getImageId() == selectedImageId) {
                        if (planogramBO.getImageName() != null) {
                            File imgFile = new File(getActivity()
                                    .getExternalFilesDir(
                                            Environment.DIRECTORY_DOWNLOADS)
                                    + "/"
                                    + bmodel.userMasterHelper.getUserMasterBO()
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
            if (checkforValidation()) {
                new SaveAsyncTask().execute();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.please_filldetails),
                        Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {

        //  private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        // private Boolean status = false;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                if(counterId > 0) {
                    bmodel.planogramMasterHelper.savePhotocapture(counterId);
                    //bmodel.saveModuleCompletion(CSHomeScreen.MENU_COUNTER_PLANOGRAM);
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

            customProgressDialog(builder, getActivity(), getResources().getString(R.string.saving));
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
        imgServerPath = new String();
        imgCameraPath = new String();
        imgSuperCameraPath = new String();
    }

    public void setSuperImageVisible() {
        imgFromSuperior.setVisibility(View.GONE);
        is_supervisor = false;
        for (final CounterPlanogramBO planogramBO : vPlanogram) {
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

    public boolean checkforValidation() {
        for (final CounterPlanogramBO planogramBO : vPlanogram) {
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
            ReasonMaster s = (ReasonMaster) spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
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
        builder.setSingleChoiceItems(filterAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        CounterPlanogramBO selectedId = filterAdapter
                                .getItem(item);
                        selecteditem = item;
                        ClearAll();
                        imgServerPath = new String();
                        imgCameraPath = new String();
                        selectedImageId = selectedId
                                .getImageId();
                        selectedImageName = selectedId.getImageName();
                        txt_pgtfilterName.setText(selectedImageName);
                        setImagefromServer(selectedImageId);
                        setImagefromCamera(selectedImageId);
                        dialog.dismiss();

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }
}
