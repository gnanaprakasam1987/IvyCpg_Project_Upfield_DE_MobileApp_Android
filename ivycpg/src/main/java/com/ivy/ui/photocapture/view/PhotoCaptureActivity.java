package com.ivy.ui.photocapture.view;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.cpg.view.photocapture.PhotoTypeMasterBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.di.DaggerPhotoCaptureComponent;
import com.ivy.ui.photocapture.di.PhotoCaptureModule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

import static com.ivy.core.IvyConstants.DEFAULT_DATE_FORMAT;

public class PhotoCaptureActivity extends BaseActivity implements PhotoCaptureContract.PhotoCaptureView, DataPickerDialogFragment.UpdateDateInterface {


    private static final String TAG_DATE_PICKER_FROM = "date_picker_from";
    private static final String TAG_DATE_PICKER_TO = "date_picker_to";

    @Inject
    PhotoCaptureContract.PhotoCapturePresenter<PhotoCaptureContract.PhotoCaptureView> photoCapturePresenter;

    private boolean isFromMenuClick;

    @BindView(R.id.spin_parentlevel)
    private Spinner productSpinner;

    @BindView(R.id.phototype)
    private Spinner photoTypeSpinner;

    @BindView(R.id.btn_fromdate)
    private Button fromDateBtn;

    @BindView(R.id.btn_todate)
    private Button toDateBtn;

    @BindView(R.id.img_show_image)
    private ImageView showImgView;

    @BindView(R.id.etSkuName)
    private EditText skuNameEditText;

    @BindView(R.id.etABV)
    private EditText abvEditText;

    @BindView(R.id.etLotCode)
    private EditText lotCodeEditText;

    @BindView(R.id.etSeqNum)
    private EditText seqNumberEditText;

    @BindView(R.id.etFeedback)
    private EditText feedbackEditText;

    @BindView(R.id.productDetailsCard)
    private CardView productDetailsCardView;


    private ArrayAdapter<PhotoCaptureProductBO> productSelectionAdapter;

    private ArrayAdapter<PhotoTypeMasterBO> photoTypeAdapter;

    private int mSelectedProductId = 0, mSelectedTypeId = 0, mSelectedLocationId = 0;

    @Override
    public int getLayoutId() {
        return R.layout.activity_capture_photo;
    }

    @Override
    protected void initVariables() {



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
        }

    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));

        checkAndRequestPermissionAtRunTime(CAMERA_AND_WRITE_PERMISSION);

        handleDateButton(photoCapturePresenter.isDateEnabled());

        setProductAdapter();

        setPhotoTypeAdapter();
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

    @OnClick(R.id.capture_img)
    public void onCaptureImageClick() {

    }

    @OnClick(R.id.save_btn)
    public void onSaveClicked() {

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

    @OnItemSelected(R.id.phototype)
    public void onPhotoTypeSpinnerSelected(Spinner spinner, int position) {
        mSelectedTypeId = photoTypeAdapter.getItem(position).getPhotoTypeId();
        if (position != 0) {
            if (photoTypeAdapter.getItem(position).getPhotoTypeCode().equals("PT"))
                productDetailsCardView.setVisibility(View.VISIBLE);

            loadLocalData();
        }

    }

    private void loadLocalData() {

    }


    @OnItemSelected(R.id.spin_parentlevel)
    public void onProductSpinnerSelected(Spinner spinner, int position) {
        photoTypeSpinner.setSelection(0);
        mSelectedProductId = productSelectionAdapter.getItem(position).getProductID();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_capture_menu, menu);
        return super.onCreateOptionsMenu(menu);
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
                Toast.makeText(this,
                        R.string.future_date_not_allowed,
                        Toast.LENGTH_SHORT).show();
            else {
                fromDateBtn.setText(DateUtil.convertDateObjectToRequestedFormat(
                        selectedDate.getTime(), DEFAULT_DATE_FORMAT));
            }
        } else if (tag.equals(TAG_DATE_PICKER_TO)) {

        }
    }
}
