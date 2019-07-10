package com.ivy.ui.photocapture.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.di.DaggerPhotoCaptureComponent;
import com.ivy.ui.photocapture.di.PhotoCaptureModule;
import com.ivy.utils.ClickGuard;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.ivy.core.IvyConstants.DEFAULT_DATE_FORMAT;

public class PhotoCaptureActivity extends BaseActivity implements PhotoCaptureContract.PhotoCaptureView, DataPickerDialogFragment.UpdateDateInterface {


    private static final String TAG_DATE_PICKER_FROM = "date_picker_from";
    private static final String TAG_DATE_PICKER_TO = "date_picker_to";

    private static final int CAMERA_REQUEST_CODE = 1;

    private static final int GALLERY_REQUEST_CODE = 2;

    private static String folderPath;

    @Inject
    PhotoCaptureContract.PhotoCapturePresenter<PhotoCaptureContract.PhotoCaptureView> photoCapturePresenter;

    private boolean isFromMenuClick;

    private boolean isFromSurvey;

    @BindView(R.id.spin_parentlevel)
    Spinner productSpinner;

    @BindView(R.id.phototype)
    Spinner photoTypeSpinner;

    @BindView(R.id.btn_fromdate)
    Button fromDateBtn;

    @BindView(R.id.btn_todate)
    Button toDateBtn;

    @BindView(R.id.etSkuName)
    EditText skuNameEditText;

    @BindView(R.id.etABV)
    EditText abvEditText;

    @BindView(R.id.etLotCode)
    EditText lotCodeEditText;

    @BindView(R.id.etSeqNum)
    EditText seqNumberEditText;

    @BindView(R.id.etFeedback)
    EditText feedbackEditText;

    @BindView(R.id.productDetailsCard)
    CardView productDetailsCardView;

    @BindView(R.id.img_show_image)
    ImageView imgViewImage;

    @BindView(R.id.capture_img)
    ImageView imageView_capture;

    @BindView(R.id.retake_img)
    ImageView imageView_reTake;

    @BindView(R.id.dummy_capture_img)
    ImageView imageView_dummyCapture;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.tv_toolbar_title)
    TextView toolBarTitleTxt;

    @BindView(R.id.save_btn)
    Button saveBtn;

    private ArrayAdapter<PhotoCaptureProductBO> productSelectionAdapter;

    private ArrayAdapter<PhotoTypeMasterBO> photoTypeAdapter;

    private ArrayAdapter<PhotoCaptureLocationBO> locationAdapter;

    private int mSelectedProductId = 0, mSelectedTypeId = 0, mSelectedLocationId = 0, selectedType, selectedProduct, selectedLocation;

    private boolean isFromChild, isPLType, isPhotoDeleted;

    private String title, imageName;

    private ArrayList<String> totalImgList = new ArrayList<>();

    private String selectedProductName, selectedTypeName, selectedLocationName;

    @Override
    public int getLayoutId() {
        return R.layout.activity_capture_photo;
    }

    @Override
    protected void initVariables() {

        folderPath = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                + DataMembers.photoFolderName;
    }

    @Override
    public void initializeDi() {
        DaggerPhotoCaptureComponent.builder()
                .photoCaptureModule(new PhotoCaptureModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) photoCapturePresenter);

    }

    @Override
    protected void getMessageFromAliens() {
        if (getIntent().getExtras() != null) {
            isFromMenuClick = getIntent().getExtras().getBoolean("isFromMenuClick", false);
            isFromChild = getIntent().getBooleanExtra("isFromChild", false);
            title = getIntent().getExtras().getString("screen_title", "");
            isFromSurvey = getIntent().getExtras().getBoolean("fromSurvey");
        }

    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));

        checkAndRequestPermissionAtRunTime(CAMERA_AND_WRITE_PERMISSION);

        handleDateButton(photoCapturePresenter.isDateEnabled());

        ClickGuard.guard(saveBtn,imageView_capture,fromDateBtn,toDateBtn);

        setUpToolBar();

        setProductAdapter();

        setPhotoTypeAdapter();

        handleSaveButton();

    }

    private void handleSaveButton() {
        if (photoCapturePresenter.isMaxPhotoLimitReached())
            saveBtn.setVisibility(View.GONE);
        else
            saveBtn.setVisibility(View.VISIBLE);

        saveBtn.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));

    }

    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        toolBarTitleTxt.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the appLogo icon from action bar

        getSupportActionBar().setIcon(null);

        getSupportActionBar().setTitle(null);

        photoCapturePresenter.getTitleLabel();
        if(!StringUtils.isNullOrEmpty(title))
            toolBarTitleTxt.setText(title);
        else
            photoCapturePresenter.getTitleLabel();

    }

    @Override
    public void setToolBarTitle(String title) {
        toolBarTitleTxt.setText(title);
    }

    private void setPhotoTypeAdapter() {
        photoTypeAdapter = new ArrayAdapter<PhotoTypeMasterBO>(
                this, R.layout.spinner_bluetext_layout);
        photoTypeAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        photoTypeSpinner.setAdapter(photoTypeAdapter);
    }

    private void setProductAdapter() {
        productSelectionAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);

        productSelectionAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        productSpinner.setAdapter(productSelectionAdapter);
    }

    private void setLocationAdapter() {
        locationAdapter = new ArrayAdapter<PhotoCaptureLocationBO>(this, android.R.layout.select_dialog_singlechoice){
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                // Replace text with my own
                view.setText(getItem(position).getLocationName());
                return view;
            }
        };
    }

    private void handleDateButton(boolean isEnabled) {
        if (isEnabled) {
            fromDateBtn.setVisibility(View.VISIBLE);
            toDateBtn.setVisibility(View.VISIBLE);
        } else {
            fromDateBtn.setVisibility(View.GONE);
            toDateBtn.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {

            if (resultCode == 1) {

                totalImgList.add(imageName);

                saveDataToLocal();

                handleNoImage();

                setImageToView(imageName);
            }
        } else if (requestCode == GALLERY_REQUEST_CODE) {
            if (data != null && data.getExtras() != null && data.getExtras().containsKey("edited_data") && photoCapturePresenter.getEditedPhotoListData().size() != ((HashMap<String, PhotoCaptureLocationBO>) data.getExtras().getSerializable("edited_data")).size()) {
                isPhotoDeleted = true;
                photoCapturePresenter.setEditedPhotosListData((HashMap<String, PhotoCaptureLocationBO>) data.getExtras().getSerializable("edited_data"));
                handleSaveButton();
            }


        }
    }

    private void saveDataToLocal() {
        if (!isPLType)
            photoCapturePresenter.updateLocalData(mSelectedProductId, mSelectedTypeId, mSelectedLocationId, imageName, feedbackEditText.getText().toString(), selectedProductName, selectedTypeName, selectedLocationName);
        else
            photoCapturePresenter.updateLocalData(mSelectedProductId, mSelectedTypeId, mSelectedLocationId, imageName
                    , feedbackEditText.getText().toString(), skuNameEditText.getText().toString(), abvEditText.getText().toString(),
                    lotCodeEditText.getText().toString(), seqNumberEditText.getText().toString(), selectedProductName, selectedTypeName, selectedLocationName);
    }

    @Override
    public void setProductListData(ArrayList<PhotoCaptureProductBO> productListData) {
        productSelectionAdapter.clear();
        productSelectionAdapter.add(new PhotoCaptureProductBO(0, getResources().getString(R.string.select_prod)));
        if (productListData.size() != 0) {
            for (PhotoCaptureProductBO bo : productListData) {
                productSelectionAdapter.add(bo);
            }
        }
        productSelectionAdapter.notifyDataSetChanged();
    }

    @Override
    public void setPhotoTypeData(ArrayList<PhotoTypeMasterBO> photoTypeData) {
        photoTypeAdapter.clear();
        photoTypeAdapter.add(new PhotoTypeMasterBO(0, getResources().getString(R.string.select_photo_type)));
        if (photoTypeData.size() != 0) {
            for (PhotoTypeMasterBO bo : photoTypeData) {
                photoTypeAdapter.add(bo);
            }
        }
        photoTypeAdapter.notifyDataSetChanged();
    }

    @Override
    public void setLocationData(ArrayList<PhotoCaptureLocationBO> locationBOS) {

        if (photoCapturePresenter.isGlobalLocation()) {
            mSelectedLocationId = locationBOS.get(photoCapturePresenter.getGlobalLocationIndex()).getLocationId();
            selectedLocationName = locationBOS.get(photoCapturePresenter.getGlobalLocationIndex()).getLocationName();
        } else if (!locationBOS.isEmpty()) {
            if (locationBOS.size() >= 2) {
                setLocationAdapter();
                locationAdapter.clear();
                for (PhotoCaptureLocationBO bo : locationBOS) {
                    locationAdapter.add(bo);
                }
            }
            selectedLocation=0;
            mSelectedLocationId = locationBOS.get(0).getLocationId();
            selectedLocationName = locationBOS.get(0).getLocationName();
        }

    }

    @Override
    public String getFromDate() {

        return getResources().getString(R.string.fromdate).equalsIgnoreCase(fromDateBtn.getText().toString())  ? "" : fromDateBtn.getText().toString();
    }

    @Override
    public String getToDate() {

        return  getResources().getString(R.string.todate).equalsIgnoreCase(toDateBtn.getText().toString()) ? "" : toDateBtn.getText().toString();
    }

    @Override
    public void setSpinnerDefaults() {
        productSpinner.setSelection(0);
        isPLType = false;
    }

    @Override
    public void showUpdatedDialog() {

        showAlert("", getResources().getString(R.string.saved_successfully), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if (isFromSurvey) {
                    finish();
                } else {
                    Intent intent = new Intent(PhotoCaptureActivity.this,
                            HomeScreenTwo.class);

                    Bundle extras = getIntent().getExtras();
                    if (extras != null) {
                        intent.putExtra("IsMoveNextActivity", photoCapturePresenter.shouldNavigateToNextActivity());
                        intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                    }

                    startActivity(intent);
                    finish();
                }

            }
        });

    }

    @OnClick(R.id.capture_img)
    public void onCaptureImageClick() {
        if (!FileUtils.isExternalStorageAvailable(10))
            showMessage(R.string.please_select_atleast_one_type);
        else if (mSelectedProductId == 0)
            showMessage(R.string.select_prod);
        else if (mSelectedTypeId == 0)
            showMessage(R.string.please_select_atleast_one_type);
        else
            preparePhotoCapture();

    }

    private void preparePhotoCapture() {

        imageName = photoCapturePresenter.getRetailerId() + "_" + mSelectedTypeId + "_"
                + mSelectedProductId + "_" + mSelectedLocationId + "_" + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL_PLAIN)
                + ".jpg";


        String mFirstNameStarts = photoCapturePresenter.getRetailerId() + "_" + mSelectedTypeId + "_"
                + mSelectedProductId + "_" + mSelectedLocationId + "_"
                + Commons.now(Commons.DATE);

        boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(folderPath, 1, mFirstNameStarts);

        if (mIsFileAvailable)
            showFileDeleteAlert(mFirstNameStarts);
        else
            navigateToCameraActivity();


    }

    private void navigateToCameraActivity() {
        Intent intent = new Intent(
                PhotoCaptureActivity.this,
                CameraActivity.class);
        String _path = FileUtils.photoFolderPath + "/" + imageName;
        //  intent.putExtra("quality", 40);
        intent.putExtra("path", _path);
        startActivityForResult(intent,
                CAMERA_REQUEST_CODE);
    }

    @OnClick(R.id.save_btn)
    public void onSaveClicked() {
        if (totalImgList.size() == 0 && !isPhotoDeleted) {
            showAlert("", getString(R.string.take_photos_to_save));
        } else {
            if (isPLType && totalImgList.get(totalImgList.size() - 1).contains(mSelectedTypeId + "_" + mSelectedProductId) && validatePLType())
                return;

            if (mSelectedProductId != 0 && mSelectedTypeId != 0)
                saveDataToLocal();
            photoCapturePresenter.onSaveButtonClick();
        }

    }

    @OnClick(R.id.btn_fromdate)
    void onFromDateClicked() {
        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
        newFragment.show(getSupportFragmentManager(), TAG_DATE_PICKER_FROM);
    }


    @OnClick(R.id.btn_todate)
    void onToDateClicked() {
        DataPickerDialogFragment newFragment = new DataPickerDialogFragment();
        newFragment.show(getSupportFragmentManager(), TAG_DATE_PICKER_TO);
    }


    private int tempProdPosition;

    @OnItemSelected(R.id.spin_parentlevel)
    public void onProductSpinnerSelected(Spinner spinner, int position) {

        if (totalImgList.size() > 0 && totalImgList.get(totalImgList.size() - 1).contains(photoCapturePresenter.getRetailerId() + "_" + mSelectedTypeId + "_" + mSelectedProductId)) {
            tempProdPosition = selectedProduct;
        }

        isPLType = false;
        selectedProduct = position;
        selectedProductName = productSelectionAdapter.getItem(position).getProductName();
        photoTypeSpinner.setSelection(0);
        mSelectedProductId = productSelectionAdapter.getItem(position).getProductID();

        clearViews();


    }


    @OnItemSelected(R.id.phototype)
    public void onPhotoTypeSpinnerSelected(Spinner spinner, int position) {

        if (totalImgList.size() > 0 && totalImgList.get(totalImgList.size() - 1).contains(photoCapturePresenter.getRetailerId() + "_" + mSelectedTypeId + "_" + mSelectedProductId)) {
            if (isPLType) {
                if (validatePLType()) return;
            } else
                photoCapturePresenter.updateLocalData(mSelectedProductId, mSelectedTypeId, mSelectedLocationId, imageName, feedbackEditText.getText().toString(), selectedProductName, selectedTypeName, selectedLocationName);


        }

        tempProdPosition = selectedProduct;

        selectedTypeName = photoTypeAdapter.getItem(position).getPhotoTypeDesc();
        mSelectedTypeId = photoTypeAdapter.getItem(position).getPhotoTypeId();
        selectedType = position;
        if (position != 0) {
            if (photoTypeAdapter.getItem(position).getPhotoTypeCode().equals("PT")) {
                isPLType = true;
                productDetailsCardView.setVisibility(View.VISIBLE);
            } else {
                isPLType = false;
                productDetailsCardView.setVisibility(View.GONE);
            }

            loadLocalData();
        } else {
            isPLType = false;
            productDetailsCardView.setVisibility(View.GONE);
        }

    }

    private boolean validatePLType() {
        if (skuNameEditText.getText().length() == 0) {
            showMessage("Enter Product Name");
            photoTypeSpinner.setSelection(selectedType);
            productSpinner.setSelection(tempProdPosition);
            return true;
        } else if (abvEditText.getText().length() == 0) {
            showMessage("Enter ABU Value");
            photoTypeSpinner.setSelection(selectedType);
            productSpinner.setSelection(tempProdPosition);
            return true;
        } else if (lotCodeEditText.getText().length() == 0) {
            showMessage("Enter Lot Number");
            photoTypeSpinner.setSelection(selectedType);
            productSpinner.setSelection(tempProdPosition);
            return true;
        } else if (seqNumberEditText.getText().length() == 0) {
            showMessage("Enter Sequence Number");
            photoTypeSpinner.setSelection(selectedType);
            productSpinner.setSelection(tempProdPosition);
            return true;
        }
        photoCapturePresenter.updateLocalData(mSelectedProductId, mSelectedTypeId, mSelectedLocationId, imageName
                , feedbackEditText.getText().toString(), skuNameEditText.getText().toString(), abvEditText.getText().toString(),
                lotCodeEditText.getText().toString(), seqNumberEditText.getText().toString(), selectedProductName, selectedTypeName, selectedLocationName);
        return false;
    }

    private void loadLocalData() {
        if (mSelectedProductId != 0 && mSelectedTypeId != 0) {
            String key = mSelectedProductId + "_" + mSelectedTypeId + "_" + mSelectedLocationId;
            if (photoCapturePresenter.getEditedPhotoListData().containsKey(key)) {
                feedbackEditText.setText(photoCapturePresenter.getEditedPhotoListData().get(key).getFeedback());
                feedbackEditText.setSelection(feedbackEditText.getText().length());
                if (isPLType) {
                    skuNameEditText.setText(photoCapturePresenter.getEditedPhotoListData().get(key).getSKUName());
                    seqNumberEditText.setText(photoCapturePresenter.getEditedPhotoListData().get(key).getSequenceNO());
                    abvEditText.setText(photoCapturePresenter.getEditedPhotoListData().get(key).getAbv());
                    lotCodeEditText.setText(photoCapturePresenter.getEditedPhotoListData().get(key).getLotCode());
                }
                if (StringUtils.isNullOrEmpty(photoCapturePresenter.getEditedPhotoListData().get(key).getImageName())) {
                    handleNoImage();
                } else {
                    setImageToView(photoCapturePresenter.getEditedPhotoListData().get(key).getImageName());
                }
            } else {
                clearViews();

            }
        }
    }

    private void clearViews() {
        handleNoImage();
        feedbackEditText.setText("");
        skuNameEditText.setText("");
        abvEditText.setText("");
        lotCodeEditText.setText("");
        seqNumberEditText.setText("");

    }

    private void setImageToView(String imageName) {
        String path = folderPath + "/" + imageName;

        if (FileUtils.isFileExisting(path)) {
            Uri uri = FileUtils
                    .getUriFromFile(getApplicationContext(), path);
            imgViewImage.setImageURI(uri);

            setImage(path);

        }
    }

    private void handleNoImage() {
        imgViewImage
                .setImageResource(R.drawable.no_image_available);
        imageView_capture.setImageResource(android.R.color.transparent);
        imageView_reTake.setVisibility(View.GONE);
        imageView_dummyCapture.setVisibility(View.VISIBLE);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));

    }

    /**
     * Location filter
     */
    private void showLocationAlertDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setSingleChoiceItems(locationAdapter, selectedLocation,
                onLocationDialogClickListener);

        applyAlertDialogTheme(this, builder);
    }

    private DialogInterface.OnClickListener onLocationDialogClickListener = new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int item) {
            PhotoCaptureLocationBO selectedId = locationAdapter
                    .getItem(item);
            resetData();
            selectedLocation =item;
            if (selectedId != null) {
                mSelectedLocationId = selectedId.getLocationId();
                selectedLocationName = selectedId.getLocationName();
            }
            dialog.dismiss();

        }
    };

    private void resetData() {
        productSpinner.setSelection(0);
        photoTypeSpinner.setSelection(0);
        handleNoImage();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_capture_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        try {
            if (photoCapturePresenter.isGlobalLocation() || photoCapturePresenter.getLocationBOS().size() < 2)
                menu.findItem(R.id.menu_location_filter).setVisible(false);
            else {
                menu.findItem(R.id.menu_location_filter).setVisible(true);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == R.id.menu_location_filter) {
            showLocationAlertDialog();
            return true;
        } else if (i == R.id.menu_capture) {
            preparePhotoCapture();
        } else if (i == R.id.menu_save) {
            onSaveClicked();
        } else if (i == android.R.id.home) {
            backButtonAlertDialog();
            return true;
        } else if (i == R.id.menu_gallery) {
            photoCapturePresenter.updateModuleTime();
            Intent mIntent = new Intent(PhotoCaptureActivity.this,
                    PhotoGalleryActivity.class);
            mIntent.putExtra("isFromPhotoCapture", true);
            mIntent.putExtra("data", photoCapturePresenter.getEditedPhotoListData());
            startActivityForResult(mIntent, GALLERY_REQUEST_CODE);
        }

        return super.onOptionsItemSelected(item);
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
            if (selectedDate.after(Calendar.getInstance()))
                showMessage(R.string.future_date_not_allowed);
            else {
                fromDateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), DEFAULT_DATE_FORMAT));
            }
        } else if (tag.equals(TAG_DATE_PICKER_TO)) {
            if (fromDateBtn.getText().equals(getString(R.string.fromdate))) {
                Toast.makeText(this, R.string.competitor_date,
                        Toast.LENGTH_SHORT).show();
            } else {
                Date dateMfg = DateTimeUtils.convertStringToDateObject(
                        fromDateBtn.getText().toString(), DEFAULT_DATE_FORMAT);

                assert dateMfg != null;
                if (dateMfg.after(selectedDate.getTime())) {
                    showMessage(R.string.competitor_date);
                } else {
                    toDateBtn.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                            selectedDate.getTime(), DEFAULT_DATE_FORMAT));
                }

            }

        }
    }

    /**
     * Alert dialog for deleting image
     *
     * @param imageNameStarts
     */
    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                PhotoCaptureActivity.this);
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        FileUtils.deleteFiles(folderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        navigateToCameraActivity();

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        applyAlertDialogTheme(this, builder);
    }


    /**
     * Set captured image in view
     *
     * @param path
     */
    private void setImage(String path) {
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
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.light_gray));

        imageView_reTake.setVisibility(View.VISIBLE);
        imageView_dummyCapture.setVisibility(View.GONE);
    }


    /**
     * Alert dialog while moving back
     */
    private void backButtonAlertDialog() {

        showAlert("", getString(R.string.photo_capture_not_saved_go_back), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                photoCapturePresenter.updateModuleTime();

                if (totalImgList.size() > 0) {
                    for (String image : totalImgList)
                        FileUtils.deleteFiles(folderPath, image);
                }

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
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
    }

}
