package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.PhotoCaptureProductBO;
import com.ivy.sd.png.bo.PhotoTypeMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.PhotoCaptureHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * The Class PhotoCaptureActivity is used to take photo , according to the
 * configuration mapped in the server Two filter in this screen.
 * <p/>
 * Filter 1 based on PhotoTypeMaster(for example OnSelf or OnSelf
 * <p/>
 * Filter 2 based on ConfigActivityTable (filter is based on ProductContent
 * level in the Table)
 */
public class PhotoCaptureActivity extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private String _imagename = "", _imagePath = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private Spinner spnPhotoType;
    private BusinessModel bmodel;
    private String retailerID;
    private int mTypeID, mProductID, mFilterProductID;
    private boolean isClicked = true;
    private static PhotoCaptureProductBO phcapture = new PhotoCaptureProductBO();
    private ArrayList<PhotoCaptureProductBO> phcaptureList = new ArrayList<PhotoCaptureProductBO>();

    private static Button btnFromDate, btnToDate, btn = null;
    private static String outPutDateFormat;
    private ImageView imgViewImage;
    private Spinner productSelectionSpinner;
    private ArrayAdapter<PhotoCaptureProductBO> productSelectionAdapter;
    private boolean isPLtype = false;
    // if pl type selected
    private EditText etSkuName, etABV, etLotCode, etSeqNo, etFeedback;
    private Toolbar toolbar;
    private ArrayAdapter<PhotoTypeMasterBO> photoTypeAdapter;

    private boolean isFromSurvey;
    private ArrayAdapter<StandardListBO> locationAdapter;
    private static int selecteditem = 0;
    private String locationid = "0";
    private ImageView capture_img, retake_img, dummy_capture_img;

    private PhotoCaptureHelper photoCaptureHelper;
    private boolean isFromChild;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_capture);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        photoCaptureHelper = PhotoCaptureHelper.getInstance(getApplicationContext());

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        retailerID = bmodel.getRetailerMasterBO().getRetailerID();

        capture_img = (ImageView) findViewById(R.id.capture_img);
        retake_img = (ImageView) findViewById(R.id.retake_img);
        dummy_capture_img = (ImageView) findViewById(R.id.dummy_capture_img);

        capture_img.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePic();
            }
        });

        Button save_btn = (Button) findViewById(R.id.save_btn);
        save_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addToGallery();
            }
        });

        if (getIntent().getExtras() != null)
            isFromSurvey = getIntent().getExtras().getBoolean("fromSurvey");

        bmodel.productHelper.downloadInStoreLocations();
        photoCaptureHelper.downloadPhotoCaptureProducts();
        photoCaptureHelper.downloadPhotoTypeMaster();
        photoCaptureHelper.loadPhotoCaptureDetailsInEditMode(bmodel.getRetailerMasterBO().getRetailerID());


        spnPhotoType = (Spinner) findViewById(R.id.phototype);

        btnFromDate = (Button) findViewById(R.id.btn_fromdate);
        btnFromDate.setOnClickListener(this);
        btnToDate = (Button) findViewById(R.id.btn_todate);
        btnToDate.setOnClickListener(this);
        imgViewImage = (ImageView) findViewById(R.id.img_show_image);
        productSelectionSpinner = (Spinner) findViewById(R.id.spin_parentlevel);

        etSkuName = (EditText) findViewById(R.id.etSkuName);
        etABV = (EditText) findViewById(R.id.etABV);
        etLotCode = (EditText) findViewById(R.id.etLotCode);
        etSeqNo = (EditText) findViewById(R.id.etSeqNum);
        etFeedback = (EditText) findViewById(R.id.etFeedback);

        productSelectionAdapter = new ArrayAdapter<PhotoCaptureProductBO>(this,
                R.layout.spinner_bluetext_layout);
        productSelectionAdapter.add(new PhotoCaptureProductBO(0, getResources().getString(R.string.select_prod)));
        if (photoCaptureHelper.getPhotoCaptureProductList() != null &&
                photoCaptureHelper.getPhotoCaptureProductList().size() != 0) {
            for (PhotoCaptureProductBO bo : photoCaptureHelper.getPhotoCaptureProductList()) {
                productSelectionAdapter.add(bo);
            }
        }
        productSelectionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        productSelectionSpinner.setAdapter(productSelectionAdapter);


        if (bmodel.configurationMasterHelper.SHOW_DATE_BTN) {
            ((LinearLayout) findViewById(R.id.ll_fromdate))
                    .setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.ll_todate))
                    .setVisibility(View.VISIBLE);
        } else {
            ((LinearLayout) findViewById(R.id.ll_fromdate))
                    .setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.ll_todate))
                    .setVisibility(View.GONE);
        }

        productSelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PhotoCaptureProductBO photoCaptureProductBO = (PhotoCaptureProductBO) parent.getSelectedItem();
                if (photoCaptureProductBO != null) {
                    mFilterProductID = photoCaptureProductBO.getProductID();
                    spnPhotoType.setAdapter(photoTypeAdapter);
                } else {
                    mFilterProductID = 0;
                    spnPhotoType.setAdapter(new ArrayAdapter<PhotoTypeMasterBO>(PhotoCaptureActivity.this, android.R.layout.simple_spinner_item, new PhotoTypeMasterBO[]{new PhotoTypeMasterBO(0, "--Select PhotoType--")}));

                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        // Set title to tool bar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolBarTitle;
        if (toolbar != null)
            setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        save_btn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        isFromChild = getIntent().getBooleanExtra("isFromChild", false);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String str = extras.getString("screen_title", "");
            if (str != null && !str.isEmpty()) {
                toolBarTitle.setText(str);
            } else {
                toolBarTitle.setText(bmodel.labelsMasterHelper
                        .applyLabels((Object) "menu_photo"));
            }
        } else {
            toolBarTitle.setText(bmodel.labelsMasterHelper
                    .applyLabels((Object) "menu_photo"));
        }


        getSupportActionBar().setIcon(R.drawable.icon_photo);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the appLogo icon from action bar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        //getSupportActionBar().setTitle(bmodel.mSelectedActivityName);
        getSupportActionBar().setIcon(null);


        outPutDateFormat = bmodel.configurationMasterHelper.outDateFormat;

        locationAdapter = new ArrayAdapter<StandardListBO>(this,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            locationAdapter.add(temp);


        if (photoCaptureHelper.getPhotoTypeMaster().size() > 0) {
            photoTypeAdapter = new ArrayAdapter<PhotoTypeMasterBO>(
                    this, R.layout.spinner_bluetext_layout,
                    photoCaptureHelper.getPhotoTypeMaster());
            photoTypeAdapter
                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

            spnPhotoType
                    .setOnItemSelectedListener(new OnItemSelectedListener() {

                        @Override
                        public void onItemSelected(AdapterView<?> parent,
                                                   View view, int pos, long id) {
                            PhotoTypeMasterBO temp = (PhotoTypeMasterBO) parent
                                    .getSelectedItem();
                            mTypeID = temp.getPhotoTypeId();
                            phcaptureList = temp.getPhotoCaptureProductList();
//                            productSelectionSpinner.setAdapter(productSelectionAdapter);
                            if (temp.getPhotoTypeCode().equals("PT"))
                                isPLtype = true;
                            else
                                isPLtype = false;
                            onLoadModule();
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> arg0) {
                        }
                    });
        }

        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            StandardListBO selectedId = locationAdapter
                    .getItem(bmodel.productHelper.getmSelectedGLobalLocationIndex());
            selecteditem = bmodel.productHelper.getmSelectedGLobalLocationIndex();
            ClearAll();
            locationid = selectedId.getListID();
            // locationName = " -" + selectedId.getListName();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (phcapture != null) {
            if (phcapture.getInStoreLocations() != null) {
                if (etFeedback.getText().toString() != null &&
                        etFeedback.getText().toString().length() > 0) {
                    phcapture.getInStoreLocations().get(selecteditem).setFeedback(etFeedback.getText().toString());
                } else {
                    phcapture.getInStoreLocations().get(selecteditem).setFeedback("");
                }

                if (isPLtype) {
                    if (etSkuName.getText().toString() != null &&
                            etSkuName.getText().toString().length() > 0) {
                        phcapture.getInStoreLocations().get(selecteditem).setSkuname(etSkuName.getText().toString());
                    }
                    if (etABV.getText().toString() != null &&
                            etABV.getText().toString().length() > 0) {
                        phcapture.getInStoreLocations().get(selecteditem).setAbv(etABV.getText().toString());
                    }
                    if (etLotCode.getText().toString() != null &&
                            etLotCode.getText().toString().length() > 0) {
                        phcapture.getInStoreLocations().get(selecteditem).setLotcode(etLotCode.getText().toString());
                    }
                    if (etSeqNo.getText().toString() != null &&
                            etSeqNo.getText().toString().length() > 0) {
                        phcapture.getInStoreLocations().get(selecteditem).setSeqno(etSeqNo.getText().toString());
                    }
                }
            }
        }
    }


    private void updateProductList() {
        // imgViewImage.setImageResource(0);
        btnFromDate.setText(phcapture.getInStoreLocations().get(selecteditem).getFromDate());
        btnToDate.setText(phcapture.getInStoreLocations().get(selecteditem).getToDate());
        if (phcapture.getInStoreLocations().get(selecteditem).getFeedback() != null &&
                phcapture.getInStoreLocations().get(selecteditem).getFeedback().length() > 0)
            etFeedback.setText(phcapture.getInStoreLocations().get(selecteditem).getFeedback() + "");
        else
            etFeedback.setText("");

        if (isPLtype) {
            ((CardView) findViewById(R.id.card_view1))
                    .setVisibility(View.VISIBLE);
            ((LinearLayout) findViewById(R.id.ll_pl))
                    .setVisibility(View.VISIBLE);
            etSkuName.setText(phcapture.getInStoreLocations().get(selecteditem).getSkuname() + "");
            etABV.setText(phcapture.getInStoreLocations().get(selecteditem).getAbv() + "");
            etLotCode.setText(phcapture.getInStoreLocations().get(selecteditem).getLotcode() + "");
            etSeqNo.setText(phcapture.getInStoreLocations().get(selecteditem).getSeqno() + "");
        } else {
            ((CardView) findViewById(R.id.card_view1))
                    .setVisibility(View.GONE);
            ((LinearLayout) findViewById(R.id.ll_pl))
                    .setVisibility(View.GONE);
            etSkuName.setText("");
            etABV.setText("");
            etLotCode.setText("");
            etSeqNo.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isClicked = true;

        onLoadModule();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {

            if (resultCode == 1) {
                getPhotoBo(_imagename, _imagePath);
                Commons.print("IMAGE NAME:" + _imagename);
                imgViewImage.setImageResource(0);

                capture_img.setImageResource(0);
                //capture_img.setVisibility(View.INVISIBLE);
                capture_img.setImageResource(android.R.color.transparent);
                retake_img.setVisibility(View.GONE);
                dummy_capture_img.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                setImagefromCamera(mProductID, mTypeID);
                bmodel.photocount++;
            } else {
                getPhotoBo("", "");
                Commons.print("IMAGE NAME:" + ",Camers Activity : Canceled");
                imgViewImage.setImageResource(0);

                capture_img.setImageResource(0);
                //capture_img.setVisibility(View.INVISIBLE);
                capture_img.setImageResource(android.R.color.transparent);
                retake_img.setVisibility(View.GONE);
                dummy_capture_img.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                setImagefromCamera(mProductID, mTypeID);
            }
        }
        isClicked = true;
    }

    private void setImagefromCamera(int productID, int photoTypeID) {
        Commons.print("selected " + selecteditem);

        for (PhotoTypeMasterBO temp : photoCaptureHelper.getPhotoTypeMaster()) {

            if (temp.getPhotoTypeId() == photoTypeID) {
                ArrayList<PhotoCaptureProductBO> tem1 = temp.getPhotoCaptureProductList();
                for (PhotoCaptureProductBO t : tem1) {
                    if (t.getInStoreLocations().get(selecteditem).getProductID() == productID) {
                        if (t.getInStoreLocations().get(selecteditem).getImageName() != null
                                && !t.getInStoreLocations().get(selecteditem).getImageName().equals("")) {
                            String path = HomeScreenFragment.folder.getPath() + "/"
                                    + t.getInStoreLocations().get(selecteditem).getImageName();
                            if (bmodel.planogramMasterHelper.isImagePresent(path)) {
                                Uri uri = bmodel.planogramMasterHelper
                                        .getUriFromFile(path);
                                imgViewImage.setImageURI(uri);

                                //BitmapFactory.decodeFile(uri, bmOptions);
                                setPic(path);
                                //capture_img.setImageURI(uri);

                                uri = null;
                                break;
                            }
                        } else {
                            imgViewImage
                                    .setImageResource(R.drawable.no_image_available);


//                            capture_img.setVisibility(View.INVISIBLE);
                            capture_img.setImageResource(android.R.color.transparent);
                            retake_img.setVisibility(View.GONE);
                            dummy_capture_img.setVisibility(View.VISIBLE);
                            toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                        }
                    } else if (productID == 0) {
                        imgViewImage
                                .setImageResource(R.drawable.no_image_available);


//                        capture_img.setVisibility(View.INVISIBLE);
                        capture_img.setImageResource(android.R.color.transparent);
                        retake_img.setVisibility(View.GONE);
                        dummy_capture_img.setVisibility(View.VISIBLE);
                        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                    } else {
                        imgViewImage
                                .setImageResource(R.drawable.no_image_available);


//                        capture_img.setVisibility(View.INVISIBLE);
                        capture_img.setImageResource(android.R.color.transparent);
                        retake_img.setVisibility(View.GONE);
                        dummy_capture_img.setVisibility(View.VISIBLE);
                        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                    }
                }
                break;
            } else {
                imgViewImage
                        .setImageResource(R.drawable.no_image_available);


//                capture_img.setVisibility(View.INVISIBLE);
                capture_img.setImageResource(android.R.color.transparent);
                retake_img.setVisibility(View.GONE);
                dummy_capture_img.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_capture_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        try {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            else {
                if (bmodel.productHelper.getInStoreLocation().size() < 2)
                    menu.findItem(R.id.menu_location_filter).setVisible(false);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
            return true;
        } else if (i == R.id.menu_capture) {
            capturePic();
        } else if (i == R.id.menu_gallery) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            Intent mIntent = new Intent(PhotoCaptureActivity.this,
                    Gallery.class);
            mIntent.putExtra("from", "photo_cap");
            startActivity(mIntent);
            // finish();
            return true;
        } else if (i == R.id.menu_save) {
            addToGallery();
        } else if (i == R.id.menu_location_filter) {
            showLocationFilterAlert();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backButtonClick() {
        try {
            mDialog();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void mDialog() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setIcon(null)
                .setCancelable(false)
                .setTitle(
                        getResources().getString(
                                R.string.photo_capture_not_saved_go_back))
                .setPositiveButton(getResources().getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                                bmodel.outletTimeStampHelper
                                        .updateTimeStampModuleWise(SDUtil
                                                .now(SDUtil.TIME));
                                bmodel.outletTimeStampHelper
                                        .updateTimeStampModuleWise(SDUtil
                                                .now(SDUtil.TIME));
                                if (isFromSurvey == true) {

                                    finish();
                                } else {

                                    Intent mIntent = new Intent(
                                            PhotoCaptureActivity.this,
                                            HomeScreenTwo.class);
                                    if (isFromChild)
                                        mIntent.putExtra("isStoreMenu", true);
                                    startActivity(mIntent);
                                    finish();
                                }
                                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                            }
                        })
                .setNegativeButton(getResources().getString(R.string.cancel),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        bmodel.applyAlertDialogTheme(alertDialogBuilder);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_fromdate) {
            btn = btnFromDate;
            btn.setTag("datePicker1");
            DialogFragment newFragment = new DatePickerFragment();
            newFragment.show(getSupportFragmentManager(), "datePicker1");

        } else if (i == R.id.btn_todate) {
            btn = btnToDate;
            btn.setTag("datePicker2");
            DialogFragment newFragment1 = new DatePickerFragment();
            newFragment1.show(getSupportFragmentManager(), "datePicker2");

        } else {
        }

    }

    @SuppressLint("ValidFragment")
    public static class DatePickerFragment extends DialogFragment implements
            DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar selectedDate = new GregorianCalendar(year, month, day);

            if (btn.getTag().equals("datePicker1")) {
                if (phcapture != null && phcapture.getInStoreLocations() != null) {
                    if (selectedDate.after(Calendar.getInstance())) {
                        Toast.makeText(getActivity(),
                                R.string.future_date_not_allowed,
                                Toast.LENGTH_SHORT).show();
                        phcapture.getInStoreLocations().get(selecteditem)
                                .setFromDate(DateUtil.convertDateObjectToRequestedFormat(
                                        Calendar.getInstance().getTime(),
                                        outPutDateFormat));
                        btn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                                .getInstance().getTime(), outPutDateFormat));
                    } else {
                        if (phcapture != null && phcapture.getInStoreLocations() != null) {
                            phcapture.getInStoreLocations().get(selecteditem).setFromDate(DateUtil.convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(), outPutDateFormat));
                            btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(), outPutDateFormat));
                        }
                    }
                }
            } else if (this.getTag().equals("datePicker2")) {
                if (phcapture != null && phcapture.getInStoreLocations() != null) {
                    if (phcapture.getInStoreLocations().get(selecteditem).getFromDate() != null
                            && phcapture.getInStoreLocations().get(selecteditem).getFromDate().length() > 0) {
                        Date dateMfg = DateUtil.convertStringToDateObject(
                                phcapture.getInStoreLocations().get(selecteditem).getFromDate(), outPutDateFormat);
                        if (dateMfg != null && selectedDate.getTime() != null
                                && dateMfg.after(selectedDate.getTime())) {
                            Toast.makeText(getActivity(), R.string.competitor_date,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            if (phcapture != null && phcapture.getInStoreLocations() != null) {
                                phcapture.getInStoreLocations().get(selecteditem).setToDate(DateUtil.convertDateObjectToRequestedFormat(
                                        selectedDate.getTime(), outPutDateFormat));
                                btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                        selectedDate.getTime(), outPutDateFormat));
                            }
                        }
                    }
                } else {
                    if (phcapture != null && phcapture.getInStoreLocations() != null) {
                        phcapture.getInStoreLocations().get(selecteditem).setToDate(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                }
            }
        }
    }

    private void showFileDeleteAlert(final String photoTypeID,
                                     final String pid, final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                PhotoCaptureActivity.this);
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(PhotoCaptureActivity.this,
                                CameraActivity.class);
                        String _path = HomeScreenFragment.folder.getPath() + "/"
                                + _imagename;
                        intent.putExtra("quality", 40);
                        intent.putExtra("path", _path);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);

                        return;
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isClicked = true;
                        return;
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }// end of showChangeA

    /**
     * Save the values in Aysnc task through Background
     *
     * @author gnanaprakasam.d
     */
    class SavePhotoDetails extends AsyncTask<String, Integer, Boolean> {
        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                photoCaptureHelper.savePhotocaptureDetails(retailerID);

                bmodel.updateIsVisitedFlag();
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_PHOTO);
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(PhotoCaptureActivity.this,
                    DataMembers.SD, getResources().getString(R.string.saving),
					true, false);*/
            builder = new AlertDialog.Builder(PhotoCaptureActivity.this);

            bmodel.customProgressDialog(alertDialog, builder, PhotoCaptureActivity.this, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground

            alertDialog.dismiss();
            //	progressDialogue.dismiss();
            if (result == Boolean.TRUE) {


                new CommonDialog(getApplicationContext(), PhotoCaptureActivity.this,
                        "", getResources().getString(R.string.saved_successfully),
                        false, getResources().getString(R.string.ok),
                        null, new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        if (BusinessModel.isPhotoCaptureFromHomeScreen) {
                            Intent intent = new Intent(PhotoCaptureActivity.this,
                                    HomeScreenTwo.class);

                            Bundle extras = getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
                            finish();
                        } else if (isFromSurvey) {
                            finish();
                        }
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();


            }
        }

    }


    public void onLoadModule() {
        try {
            for (PhotoCaptureProductBO sku : phcaptureList) {

                if (sku.getInStoreLocations().get(selecteditem).getProductID() == mFilterProductID) {
                    phcapture = sku;
                    mProductID = phcapture.getInStoreLocations().get(selecteditem).getProductID();
                    //tvProduct.setText(phcapture.getInStoreLocations().get(selecteditem).getProductName() + "");
                    updateProductList();
                    setImagefromCamera(mProductID, mTypeID);
                } else if (mFilterProductID == 0) {
                    phcapture = new PhotoCaptureProductBO();
                    phcapture.setInStoreLocations(bmodel.productHelper.cloneLocationList(bmodel.productHelper.locations));
                    phcapture.getInStoreLocations().get(selecteditem).setFromDate("");
                    phcapture.getInStoreLocations().get(selecteditem).setToDate("");
                    mProductID = 0;
                    //  setImagefromCamera(mProductID, mTypeID);
                    imgViewImage
                            .setImageResource(R.drawable.no_image_available);

                    capture_img.setImageResource(android.R.color.transparent);
                    //capture_img.setVisibility(View.INVISIBLE);
                    retake_img.setVisibility(View.GONE);
                    dummy_capture_img.setVisibility(View.VISIBLE);
                    toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /*
       * Show Location wise Filter
       */
    private void showLocationFilterAlert() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setSingleChoiceItems(locationAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        StandardListBO selectedId = locationAdapter
                                .getItem(item);
                        selecteditem = item;
                        ClearAll();
                        locationid = selectedId.getListID();
                        dialog.dismiss();

                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    public void ClearAll() {
        _imagename = "";
        _imagePath = "";
        imgViewImage.setImageResource(R.drawable.no_image_available);


        capture_img.setImageResource(android.R.color.transparent);
        //capture_img.setVisibility(View.INVISIBLE);
        retake_img.setVisibility(View.GONE);
        dummy_capture_img.setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        photoTypeAdapter.notifyDataSetChanged();
        productSelectionAdapter.notifyDataSetChanged();
        productSelectionSpinner.setAdapter(productSelectionAdapter);
        spnPhotoType.setAdapter(photoTypeAdapter);

    }

    private void getPhotoBo(String _imagename, String _imagePath) {
        ArrayList<PhotoTypeMasterBO> list = photoCaptureHelper.getPhotoTypeMaster();
        ArrayList<PhotoCaptureProductBO> lst;

        int zize = list.size();
        for (int i = 0; i < zize; i++) {
            if (list.get(i).getPhotoTypeId() == mTypeID) {
                lst = list.get(i).getPhotoCaptureProductList();
                for (int j = 0; j < lst.size(); j++)
                    if (lst.get(j).getInStoreLocations().get(selecteditem).getProductID() == mFilterProductID) {
                        lst.get(j).getInStoreLocations().get(selecteditem).setImageName(_imagename);
                        lst.get(j).getInStoreLocations().get(selecteditem).setImagepath(_imagePath);
                        Commons.print("location id" + bmodel.productHelper.getInStoreLocation().get(selecteditem).getListID() + " image" + _imagename + " listid" + locationid);
                        break;
                    }

            }
        }

    }


    //mayuri--capturepic--4/3/2017
    public void capturePic() {
        if (bmodel.isExternalStorageAvailable()) {
            if (mProductID != 0) {
                if (mTypeID != 0) {

                    if (isClicked) {
                        isClicked = false;

                        _imagename = retailerID + "_" + mTypeID + "_"
                                + mProductID + "_" + locationid + "_" + SDUtil.now(SDUtil.DATE_GLOBAL_PLAIN)
                                + ".jpg";

                        _imagePath = bmodel.userMasterHelper.getUserMasterBO
                                ().getDistributorid()
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO
                                ().getDownloadDate()
                                .replace("/", "") + "/" + _imagename;

                        String fnameStarts = retailerID + "_" + mTypeID
                                + "_" + mProductID + "_" + locationid + "_"
                                + Commons.now(Commons.DATE);

                        boolean nfiles_there = bmodel
                                .checkForNFilesInFolder(
                                        HomeScreenFragment.folder.getPath(), 1,
                                        fnameStarts);

                        if (nfiles_there) {
                            showFileDeleteAlert(mTypeID + "", mProductID
                                    + "", fnameStarts);

                        } else {
                            try {
                                Thread.sleep(10);
                                Intent intent = new Intent(
                                        PhotoCaptureActivity.this,
                                        CameraActivity.class);
                                String _path = HomeScreenFragment.folder.getPath()
                                        + "/" + _imagename;
                                //  intent.putExtra("quality", 40);
                                intent.putExtra("path", _path);
                                startActivityForResult(intent,
                                        CAMERA_REQUEST_CODE);
                            } catch (Exception e) {
                                Commons.printException(e);
                            }
                        }

                    }

                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.please_select_atleast_one_type),
                            Toast.LENGTH_SHORT).show();
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_select_atleast_one_category),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.sdcard_is_not_ready_to_capture_img),
                    Toast.LENGTH_SHORT).show();
        }
        // return true;
    }

    private void setPic(String path) {
        // Get the dimensions of the View
        int targetW = capture_img.getWidth();
        int targetH = capture_img.getHeight();


        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        //BitmapFactory.decode
        BitmapFactory.decodeFile(path, bmOptions);

        //BitmapFactory.decodeByteArray(imgarr,null,null,bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        capture_img.setImageBitmap(bitmap);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.photocapture_toolbar_bg));

        retake_img.setVisibility(View.VISIBLE);
        dummy_capture_img.setVisibility(View.GONE);
    }

    public void addToGallery() {
        String mSkuName = "", mABV = "", mLotCode = "", mSeqNo = "", mFeedback = "";
        if (photoCaptureHelper.hasPhotoTaken(mFilterProductID, mTypeID)) {
            mFeedback = etFeedback.getText().toString();
            if (mFeedback.length() > 0)
                phcapture.getInStoreLocations().get(selecteditem).setFeedback(mFeedback);
            if (isPLtype) {
                mSkuName = etSkuName.getText().toString();
                mABV = etABV.getText().toString();
                mLotCode = etLotCode.getText().toString();
                mSeqNo = etSeqNo.getText().toString();
                if (mSkuName.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter Product Name", Toast.LENGTH_SHORT).show();
                    etSkuName.requestFocus();
                } else if (mABV.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter ABU Value", Toast.LENGTH_SHORT).show();
                    etABV.requestFocus();
                } else if (mLotCode.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter Lot Number", Toast.LENGTH_SHORT).show();
                    etLotCode.requestFocus();
                } else if (mSeqNo.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter Sequence Number", Toast.LENGTH_SHORT).show();
                    etSeqNo.requestFocus();
                } else {
                    phcapture.getInStoreLocations().get(selecteditem).setSkuname(mSkuName);
                    phcapture.getInStoreLocations().get(selecteditem).setAbv(mABV);
                    phcapture.getInStoreLocations().get(selecteditem).setLotcode(mLotCode);
                    phcapture.getInStoreLocations().get(selecteditem).setSeqno(mSeqNo);

                    new SavePhotoDetails().execute();
                }

            } else
                new SavePhotoDetails().execute();
        } else {
            bmodel = (BusinessModel) getApplicationContext();
            bmodel.setContext(this);
            if (!photoCaptureHelper.hasPhotoTaken(mFilterProductID, mTypeID) &&
                    productSelectionSpinner.getSelectedItem().toString().equalsIgnoreCase(getResources().getString(R.string.select_prod))
                    && spnPhotoType.getSelectedItem().toString().equalsIgnoreCase("--Select PhotoType--")
                    && etFeedback.length() == 0) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);
            } else {
                bmodel.showAlert(
                        getResources().getString(R.string.take_photos_to_save), 0);
            }
        }
        //return true;
    }
}
