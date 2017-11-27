package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ImageView;
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
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static com.ivy.sd.png.view.Gallery.isPhotoDelete;

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
        OnClickListener, DataPickerDialogFragment.UpdateDateInterface {

    private String mImageName = "", mImagePath = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private String mRetailerId;
    private int mTypeID, mProductID, mFilterProductID;
    private boolean isClicked = true;
    private static String outPutDateFormat;
    private boolean isPLType = false;
    private boolean isFromSurvey;
    private static int mSelectedItem = 0;
    private String mLocationId = "0";
    private boolean isFromChild;
    private boolean isFromMenuClick = false;
    private static final String TAG_DATE_PICKER_FROM = "date_picker_from";
    private static final String TAG_DATE_PICKER_TO = "date_picker_to";

    private Button button_fromDate, button_toDate;
    private Button btn = null;
    private Spinner spinner_photoType;
    private ImageView imgViewImage;
    private Spinner productSelectionSpinner;
    private EditText editText_skuName, editText_ABV, editText_LotCode, editText_SeqNo, editText_Feedback;
    private Toolbar toolbar;
    private ImageView imageView_capture, imageView_reTake, imageView_dummyCapture;
    Button save_btn;

    private BusinessModel mBModel;
    private static PhotoCaptureProductBO mPhotoCaptureBO = new PhotoCaptureProductBO();
    private ArrayList<PhotoCaptureProductBO> mPhotoCaptureList = new ArrayList<>();
    private ArrayAdapter<PhotoCaptureProductBO> productSelectionAdapter;
    private ArrayAdapter<PhotoTypeMasterBO> photoTypeAdapter;
    private ArrayAdapter<StandardListBO> locationAdapter;
    private ArrayList<String> totalImgList = new ArrayList<>();
    PhotoCaptureHelper mPhotoCaptureHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_capture);

        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        mPhotoCaptureHelper = PhotoCaptureHelper.getInstance(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        mRetailerId = mBModel.getRetailerMasterBO().getRetailerID();

        imageView_capture = (ImageView) findViewById(R.id.capture_img);
        imageView_reTake = (ImageView) findViewById(R.id.retake_img);
        imageView_dummyCapture = (ImageView) findViewById(R.id.dummy_capture_img);

        imageView_capture.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                capturePic();
            }
        });
        save_btn = (Button) findViewById(R.id.save_btn);
        if (isMaxPhotos())
            save_btn.setVisibility(View.GONE);
        else
            save_btn.setVisibility(View.VISIBLE);

        save_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                addToGallery();
            }
        });

        if (getIntent().getExtras() != null) {
            isFromSurvey = getIntent().getExtras().getBoolean("fromSurvey");

            isFromMenuClick = getIntent().getExtras().getBoolean("isFromMenuClick", false);
        }

        if (isFromMenuClick) {
            mBModel.productHelper.getLocations();
            mBModel.productHelper.downloadInStoreLocations();
            mPhotoCaptureHelper.downloadPhotoCaptureProducts();
            mPhotoCaptureHelper.downloadPhotoTypeMaster();
            mPhotoCaptureHelper.loadPhotoCaptureDetailsInEditMode(mBModel.getRetailerMasterBO().getRetailerID());
        }


        spinner_photoType = (Spinner) findViewById(R.id.phototype);

        button_fromDate = (Button) findViewById(R.id.btn_fromdate);
        button_fromDate.setOnClickListener(this);
        button_toDate = (Button) findViewById(R.id.btn_todate);
        button_toDate.setOnClickListener(this);
        imgViewImage = (ImageView) findViewById(R.id.img_show_image);
        productSelectionSpinner = (Spinner) findViewById(R.id.spin_parentlevel);

        editText_skuName = (EditText) findViewById(R.id.etSkuName);
        editText_ABV = (EditText) findViewById(R.id.etABV);
        editText_LotCode = (EditText) findViewById(R.id.etLotCode);
        editText_SeqNo = (EditText) findViewById(R.id.etSeqNum);
        editText_Feedback = (EditText) findViewById(R.id.etFeedback);

        productSelectionAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        productSelectionAdapter.add(new PhotoCaptureProductBO(0, getResources().getString(R.string.select_prod)));
        if (mPhotoCaptureHelper.getPhotoCaptureProductList() != null &&
                mPhotoCaptureHelper.getPhotoCaptureProductList().size() != 0) {
            for (PhotoCaptureProductBO bo : mPhotoCaptureHelper.getPhotoCaptureProductList()) {
                productSelectionAdapter.add(bo);
            }
        }
        productSelectionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        productSelectionSpinner.setAdapter(productSelectionAdapter);


        if (mBModel.configurationMasterHelper.SHOW_DATE_BTN) {
            (findViewById(R.id.ll_fromdate))
                    .setVisibility(View.VISIBLE);
            (findViewById(R.id.ll_todate))
                    .setVisibility(View.VISIBLE);
        } else {
            (findViewById(R.id.ll_fromdate))
                    .setVisibility(View.GONE);
            (findViewById(R.id.ll_todate))
                    .setVisibility(View.GONE);
        }

        productSelectionSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                PhotoCaptureProductBO photoCaptureProductBO = (PhotoCaptureProductBO) parent.getSelectedItem();
                if (photoCaptureProductBO != null) {
                    mFilterProductID = photoCaptureProductBO.getProductID();
                    spinner_photoType.setAdapter(photoTypeAdapter);
                } else {
                    mFilterProductID = 0;
                    spinner_photoType.setAdapter(new ArrayAdapter<>(PhotoCaptureActivity.this, android.R.layout.simple_spinner_item, new PhotoTypeMasterBO[]{new PhotoTypeMasterBO(0, "--Select PhotoType--")}));

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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
        }

        toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        toolBarTitle.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        save_btn.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        isFromChild = getIntent().getBooleanExtra("isFromChild", false);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String str = extras.getString("screen_title", "");
            if (str != null && !str.isEmpty()) {
                toolBarTitle.setText(str);
            } else {
                toolBarTitle.setText(mBModel.labelsMasterHelper
                        .applyLabels((Object) "menu_photo"));
            }
        } else {
            toolBarTitle.setText(mBModel.labelsMasterHelper
                    .applyLabels((Object) "menu_photo"));
        }


        getSupportActionBar().setIcon(R.drawable.icon_photo);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the appLogo icon from action bar

        getSupportActionBar().setIcon(null);


        outPutDateFormat = ConfigurationMasterHelper.outDateFormat;

        locationAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : mBModel.productHelper.getInStoreLocation())
            locationAdapter.add(temp);

        if (mPhotoCaptureHelper.getPhotoTypeMaster() != null)
            if (mPhotoCaptureHelper.getPhotoTypeMaster().size() > 0) {
                photoTypeAdapter = new ArrayAdapter<>(
                        this, R.layout.spinner_bluetext_layout,
                        mPhotoCaptureHelper.getPhotoTypeMaster());
                photoTypeAdapter
                        .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

                spinner_photoType
                        .setOnItemSelectedListener(new OnItemSelectedListener() {

                            @Override
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int pos, long id) {
                                PhotoTypeMasterBO temp = (PhotoTypeMasterBO) parent
                                        .getSelectedItem();
                                mTypeID = temp.getPhotoTypeId();
                                mPhotoCaptureList = temp.getPhotoCaptureProductList();
                                isPLType = temp.getPhotoTypeCode().equals("PT");
                                onLoadModule();
                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> arg0) {
                            }
                        });
            }

        if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            StandardListBO selectedId = locationAdapter
                    .getItem(mBModel.productHelper.getmSelectedGLobalLocationIndex());
            mSelectedItem = mBModel.productHelper.getmSelectedGLobalLocationIndex();
            ClearAll();
            if (selectedId != null)
                mLocationId = selectedId.getListID();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mPhotoCaptureBO != null) {
            if (mPhotoCaptureBO.getInStoreLocations() != null) {
                if (editText_Feedback.getText().toString().length() > 0) {
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setFeedback(editText_Feedback.getText().toString());
                } else {
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setFeedback("");
                }

                if (isPLType) {
                    if (editText_skuName.getText().toString().length() > 0) {
                        mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setSkuname(editText_skuName.getText().toString());
                    }
                    if (editText_ABV.getText().toString().length() > 0) {
                        mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setAbv(editText_ABV.getText().toString());
                    }
                    if (editText_LotCode.getText().toString().length() > 0) {
                        mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setLotcode(editText_LotCode.getText().toString());
                    }
                    if (editText_SeqNo.getText().toString().length() > 0) {
                        mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setSeqno(editText_SeqNo.getText().toString());
                    }
                }
            }
        }
    }


    private void updateProductList() {
        // imgViewImage.setImageResource(0);
        button_fromDate.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFromDate());
        button_toDate.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getToDate());
        if (mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFeedback() != null &&
                mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFeedback().length() > 0)
            editText_Feedback.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFeedback());
        else
            editText_Feedback.setText("");

        if (isPLType) {
            (findViewById(R.id.card_view1))
                    .setVisibility(View.VISIBLE);
            (findViewById(R.id.ll_pl))
                    .setVisibility(View.VISIBLE);
            editText_skuName.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getSkuname());
            editText_ABV.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getAbv());
            editText_LotCode.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getLotcode());
            editText_SeqNo.setText(mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getSeqno());
        } else {
            (findViewById(R.id.card_view1))
                    .setVisibility(View.GONE);
            (findViewById(R.id.ll_pl))
                    .setVisibility(View.GONE);
            editText_skuName.setText("");
            editText_ABV.setText("");
            editText_LotCode.setText("");
            editText_SeqNo.setText("");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        isClicked = true;
        if (isPhotoDelete)
            save_btn.setVisibility(View.VISIBLE);
        onLoadModule();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {

            if (resultCode == 1) {
                getPhotoBo(mImageName, mImagePath);
                totalImgList.add(mImageName);
                Commons.print("IMAGE NAME:" + mImageName);
                imgViewImage.setImageResource(0);

                imageView_capture.setImageResource(0);
                //imageView_capture.setVisibility(View.INVISIBLE);
                imageView_capture.setImageResource(android.R.color.transparent);
                imageView_reTake.setVisibility(View.GONE);
                imageView_dummyCapture.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                setImageFromCamera(mProductID, mTypeID);
                mBModel.photocount++;
            } else {
                getPhotoBo("", "");
                Commons.print("IMAGE NAME:" + ",Camera Activity : Canceled");
                imgViewImage.setImageResource(0);

                imageView_capture.setImageResource(0);
                imageView_capture.setImageResource(android.R.color.transparent);
                imageView_reTake.setVisibility(View.GONE);
                imageView_dummyCapture.setVisibility(View.VISIBLE);
                toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                setImageFromCamera(mProductID, mTypeID);
            }
        }
        isClicked = true;
    }

    private void setImageFromCamera(int productID, int photoTypeID) {

        for (PhotoTypeMasterBO temp : mPhotoCaptureHelper.getPhotoTypeMaster()) {

            if (temp.getPhotoTypeId() == photoTypeID) {
                ArrayList<PhotoCaptureProductBO> tem1 = temp.getPhotoCaptureProductList();
                for (PhotoCaptureProductBO t : tem1) {
                    if (t.getInStoreLocations().get(mSelectedItem).getProductID() == productID) {
                        if (t.getInStoreLocations().get(mSelectedItem).getImageName() != null
                                && !t.getInStoreLocations().get(mSelectedItem).getImageName().equals("")) {
                            String path = HomeScreenFragment.folder.getPath() + "/"
                                    + t.getInStoreLocations().get(mSelectedItem).getImageName();
                            if (mBModel.planogramMasterHelper.isImagePresent(path)) {
                                Uri uri = mBModel.planogramMasterHelper
                                        .getUriFromFile(path);
                                imgViewImage.setImageURI(uri);

                                setPic(path);

                                break;
                            }
                        } else {
                            imgViewImage
                                    .setImageResource(R.drawable.no_image_available);


                            imageView_capture.setImageResource(android.R.color.transparent);
                            imageView_reTake.setVisibility(View.GONE);
                            imageView_dummyCapture.setVisibility(View.VISIBLE);
                            toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                        }
                    } else if (productID == 0) {
                        imgViewImage
                                .setImageResource(R.drawable.no_image_available);


                        imageView_capture.setImageResource(android.R.color.transparent);
                        imageView_reTake.setVisibility(View.GONE);
                        imageView_dummyCapture.setVisibility(View.VISIBLE);
                        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                    } else {
                        imgViewImage
                                .setImageResource(R.drawable.no_image_available);


//                        imageView_capture.setVisibility(View.INVISIBLE);
                        imageView_capture.setImageResource(android.R.color.transparent);
                        imageView_reTake.setVisibility(View.GONE);
                        imageView_dummyCapture.setVisibility(View.VISIBLE);
                        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

                    }
                }
                break;
            } else {
                imgViewImage
                        .setImageResource(R.drawable.no_image_available);


//                imageView_capture.setVisibility(View.INVISIBLE);
                imageView_capture.setImageResource(android.R.color.transparent);
                imageView_reTake.setVisibility(View.GONE);
                imageView_dummyCapture.setVisibility(View.VISIBLE);
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
     * @param view Root view
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
            if (mBModel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            else {
                if (mBModel.productHelper.getInStoreLocation().size() < 2)
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
            mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            Intent mIntent = new Intent(PhotoCaptureActivity.this,
                    Gallery.class);
            mIntent.putExtra("from", "photo_cap");
            startActivity(mIntent);
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
                                mBModel.outletTimeStampHelper
                                        .updateTimeStampModuleWise(SDUtil
                                                .now(SDUtil.TIME));
                                mBModel.outletTimeStampHelper
                                        .updateTimeStampModuleWise(SDUtil
                                                .now(SDUtil.TIME));

                                if (totalImgList != null)
                                    deleteUnsavedImageFromFolder();
                                if (isFromSurvey) {

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
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        mBModel.applyAlertDialogTheme(alertDialogBuilder);
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_fromdate) {
            btn = button_fromDate;
            btn.setTag(TAG_DATE_PICKER_FROM);
            DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
            newFragment.show(getSupportFragmentManager(), TAG_DATE_PICKER_FROM);

        } else if (i == R.id.btn_todate) {
            btn = button_toDate;
            btn.setTag(TAG_DATE_PICKER_TO);
            DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
            newFragment.show(getSupportFragmentManager(), TAG_DATE_PICKER_TO);

        }

    }


    @Override
    public void updateDate(Date date, String tag) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Calendar selectedDate = new GregorianCalendar(year, month, day);

        if (tag.equals(TAG_DATE_PICKER_FROM)) {
            if (mPhotoCaptureBO != null && mPhotoCaptureBO.getInStoreLocations() != null) {
                if (selectedDate.after(Calendar.getInstance())) {
                    Toast.makeText(this,
                            R.string.future_date_not_allowed,
                            Toast.LENGTH_SHORT).show();
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem)
                            .setFromDate(DateUtil.convertDateObjectToRequestedFormat(
                                    Calendar.getInstance().getTime(),
                                    outPutDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(Calendar
                            .getInstance().getTime(), outPutDateFormat));
                } else {
                    if (mPhotoCaptureBO != null && mPhotoCaptureBO.getInStoreLocations() != null) {
                        mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setFromDate(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                        btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                selectedDate.getTime(), outPutDateFormat));
                    }
                }
            }
        } else if (tag.equals(TAG_DATE_PICKER_TO)) {
            if (mPhotoCaptureBO != null && mPhotoCaptureBO.getInStoreLocations() != null) {
                if (mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFromDate() != null
                        && mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFromDate().length() > 0) {
                    Date dateMfg = DateUtil.convertStringToDateObject(
                            mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getFromDate(), outPutDateFormat);
                    if (dateMfg != null && selectedDate.getTime() != null
                            && dateMfg.after(selectedDate.getTime())) {
                        Toast.makeText(this, R.string.competitor_date,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        if (mPhotoCaptureBO != null && mPhotoCaptureBO.getInStoreLocations() != null) {
                            mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setToDate(DateUtil.convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(), outPutDateFormat));
                            btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(), outPutDateFormat));
                        }
                    }
                }
            } else {
                if (mPhotoCaptureBO != null && mPhotoCaptureBO.getInStoreLocations() != null) {
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setToDate(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                    btn.setText(DateUtil.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), outPutDateFormat));
                }
            }
        }
    }

    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                PhotoCaptureActivity.this);
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        mBModel.deleteFiles(HomeScreenFragment.photoPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(PhotoCaptureActivity.this,
                                CameraActivity.class);
                        String _path = HomeScreenFragment.folder.getPath() + "/"
                                + mImageName;
                        intent.putExtra("quality", 40);
                        intent.putExtra("path", _path);
                        startActivityForResult(intent, CAMERA_REQUEST_CODE);

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        isClicked = true;
                    }
                });

        builder.setCancelable(false);
        mBModel.applyAlertDialogTheme(builder);
    }// end of showChangeA

    /**
     * Save the values in Async task through Background
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
                mPhotoCaptureHelper.savePhotoCaptureDetails(mRetailerId);

                mBModel.updateIsVisitedFlag();
                mBModel.saveModuleCompletion(HomeScreenTwo.MENU_PHOTO);
                mBModel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }

        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(PhotoCaptureActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            if (result == Boolean.TRUE) {


                new CommonDialog(getApplicationContext(), PhotoCaptureActivity.this,
                        "", getResources().getString(R.string.saved_successfully),
                        false, getResources().getString(R.string.ok),
                        null, new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        if (isFromSurvey) {
                            finish();
                        } else {
                            Intent intent = new Intent(PhotoCaptureActivity.this,
                                    HomeScreenTwo.class);

                            Bundle extras = getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", mBModel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
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
            for (PhotoCaptureProductBO sku : mPhotoCaptureList) {

                if (sku.getInStoreLocations().get(mSelectedItem).getProductID() == mFilterProductID) {
                    mPhotoCaptureBO = sku;
                    mProductID = mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).getProductID();
                    updateProductList();
                    setImageFromCamera(mProductID, mTypeID);
                } else if (mFilterProductID == 0) {
                    mPhotoCaptureBO = new PhotoCaptureProductBO();
                    mPhotoCaptureBO.setInStoreLocations(ProductHelper.cloneLocationList(mBModel.productHelper.locations));
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setFromDate("");
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setToDate("");
                    mProductID = 0;
                    imgViewImage
                            .setImageResource(R.drawable.no_image_available);

                    imageView_capture.setImageResource(android.R.color.transparent);
                    imageView_reTake.setVisibility(View.GONE);
                    imageView_dummyCapture.setVisibility(View.VISIBLE);
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
        builder.setSingleChoiceItems(locationAdapter, mSelectedItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        StandardListBO selectedId = locationAdapter
                                .getItem(item);
                        mSelectedItem = item;
                        ClearAll();
                        if (selectedId != null) {
                            mLocationId = selectedId.getListID();
                        }
                        dialog.dismiss();

                    }
                });

        mBModel.applyAlertDialogTheme(builder);
    }

    public void ClearAll() {
        mImageName = "";
        mImagePath = "";
        imgViewImage.setImageResource(R.drawable.no_image_available);


        imageView_capture.setImageResource(android.R.color.transparent);
        imageView_reTake.setVisibility(View.GONE);
        imageView_dummyCapture.setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

        photoTypeAdapter.notifyDataSetChanged();
        productSelectionAdapter.notifyDataSetChanged();
        productSelectionSpinner.setAdapter(productSelectionAdapter);
        spinner_photoType.setAdapter(photoTypeAdapter);

    }

    private void getPhotoBo(String mImageName, String _imagePath) {
        ArrayList<PhotoTypeMasterBO> list = mPhotoCaptureHelper.getPhotoTypeMaster();
        ArrayList<PhotoCaptureProductBO> lst;

        int mSize = list.size();
        for (int i = 0; i < mSize; i++) {
            if (list.get(i).getPhotoTypeId() == mTypeID) {
                lst = list.get(i).getPhotoCaptureProductList();
                for (int j = 0; j < lst.size(); j++)
                    if (lst.get(j).getInStoreLocations().get(mSelectedItem).getProductID() == mFilterProductID) {
                        lst.get(j).getInStoreLocations().get(mSelectedItem).setImageName(mImageName);
                        lst.get(j).getInStoreLocations().get(mSelectedItem).setImagepath(_imagePath);
                        Commons.print("location id" + mBModel.productHelper.getInStoreLocation().get(mSelectedItem).getListID() + " image" + mImageName + " listid" + mLocationId);
                        break;
                    }

            }
        }

    }


    /**
     * Capture Image
     */
    public void capturePic() {
        if (mBModel.isExternalStorageAvailable()) {
            if (mProductID != 0) {
                if (mTypeID != 0) {

                    if (isClicked) {
                        isClicked = false;

                        mImageName = mRetailerId + "_" + mTypeID + "_"
                                + mProductID + "_" + mLocationId + "_" + SDUtil.now(SDUtil.DATE_GLOBAL_PLAIN)
                                + ".jpg";


                        if (mBModel.configurationMasterHelper.IS_PHOTO_CAPTURE_IMG_PATH_CHANGE) {


                            mImagePath = "PhotoCapture/"
                                    + mBModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                                    .replace("/", "") + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mImageName;
                        } else {

                            mImagePath = mBModel.userMasterHelper.getUserMasterBO
                                    ().getDistributorid()
                                    + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                                    + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO
                                    ().getDownloadDate()
                                    .replace("/", "") + "/" + mImageName;
                        }

                        String mFirstNameStarts = mRetailerId + "_" + mTypeID
                                + "_" + mProductID + "_" + mLocationId + "_"
                                + Commons.now(Commons.DATE);

                        boolean mIsFileAvailable = mBModel
                                .checkForNFilesInFolder(
                                        HomeScreenFragment.folder.getPath(), 1,
                                        mFirstNameStarts);

                        if (mIsFileAvailable) {
                            showFileDeleteAlert(mFirstNameStarts);

                        } else {
                            try {
                                Thread.sleep(10);
                                Intent intent = new Intent(
                                        PhotoCaptureActivity.this,
                                        CameraActivity.class);
                                String _path = HomeScreenFragment.folder.getPath()
                                        + "/" + mImageName;
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
                                R.string.select_prod),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(
                    this,
                    getResources().getString(
                            R.string.sdcard_is_not_ready_to_capture_img),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void setPic(String path) {
        // Get the dimensions of the View
        int targetW = imageView_capture.getWidth();
        int targetH = imageView_capture.getHeight();


        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        //BitmapFactory.decode
        BitmapFactory.decodeFile(path, bmOptions);

        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(path, bmOptions);
        imageView_capture.setImageBitmap(bitmap);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.photocapture_toolbar_bg));

        imageView_reTake.setVisibility(View.VISIBLE);
        imageView_dummyCapture.setVisibility(View.GONE);
    }

    public void addToGallery() {
        String mSkuName, mABV, mLotCode, mSeqNo, mFeedback;
        if (mPhotoCaptureHelper.hasPhotoTaken(mFilterProductID, mTypeID)) {
            mFeedback = editText_Feedback.getText().toString();
            if (mFeedback.length() > 0)
                mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setFeedback(mFeedback);
            if (isPLType) {
                mSkuName = editText_skuName.getText().toString();
                mABV = editText_ABV.getText().toString();
                mLotCode = editText_LotCode.getText().toString();
                mSeqNo = editText_SeqNo.getText().toString();
                if (mSkuName.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter Product Name", Toast.LENGTH_SHORT).show();
                    editText_skuName.requestFocus();
                } else if (mABV.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter ABU Value", Toast.LENGTH_SHORT).show();
                    editText_ABV.requestFocus();
                } else if (mLotCode.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter Lot Number", Toast.LENGTH_SHORT).show();
                    editText_LotCode.requestFocus();
                } else if (mSeqNo.length() == 0) {
                    Toast.makeText(PhotoCaptureActivity.this, "Enter Sequence Number", Toast.LENGTH_SHORT).show();
                    editText_SeqNo.requestFocus();
                } else {
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setSkuname(mSkuName);
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setAbv(mABV);
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setLotcode(mLotCode);
                    mPhotoCaptureBO.getInStoreLocations().get(mSelectedItem).setSeqno(mSeqNo);

                    new SavePhotoDetails().execute();
                }

            } else
                new SavePhotoDetails().execute();
        } else {
            mBModel = (BusinessModel) getApplicationContext();
            mBModel.setContext(this);
            if (!mPhotoCaptureHelper.hasPhotoTaken(mFilterProductID, mTypeID) &&
                    productSelectionSpinner.getSelectedItem().toString().equalsIgnoreCase(getResources().getString(R.string.select_prod))
                    && spinner_photoType.getSelectedItem().toString().equalsIgnoreCase("--Select PhotoType--")
                    && editText_Feedback.length() == 0) {
                mBModel.showAlert(
                        getResources().getString(R.string.no_data_tosave), 0);
            } else {
                mBModel.showAlert(
                        getResources().getString(R.string.take_photos_to_save), 0);
            }
        }
    }

    private boolean isMaxPhotos() {
        int dbImageCount = mBModel.synchronizationHelper
                .countImageFiles();
        if (dbImageCount >= mBModel.configurationMasterHelper.photocount) {
            isPhotoDelete = false;
            return true;
        } else {
            return false;
        }
    }

    private void deleteUnsavedImageFromFolder() {
        for (String imgList : totalImgList) {
            mBModel.deleteFiles(HomeScreenFragment.photoPath,
                    imgList);
        }
    }
}
