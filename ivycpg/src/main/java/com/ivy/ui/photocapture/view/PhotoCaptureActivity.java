package com.ivy.ui.photocapture.view;

import android.content.Intent;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureProductBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.Spinadapter;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.di.DaggerPhotoCaptureComponent;
import com.ivy.ui.photocapture.di.PhotoCaptureModule;

import java.util.ArrayList;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class PhotoCaptureActivity extends BaseActivity implements PhotoCaptureContract.PhotoCaptureView {

    @Inject
    PhotoCaptureContract.PhotoCapturePresenter<PhotoCaptureContract.PhotoCaptureView> photoCapturePresenter;

    private boolean isFromMenuClick;

    @BindView(R.id.spin_parentlevel)
    private Spinner parentSpinner;

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

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void setProductListData(ArrayList<PhotoCaptureProductBO> productListData) {

    }

    @OnClick(R.id.capture_img)
    public void onCaptureImageClick() {

    }

    @OnClick(R.id.save_btn)
    public void onSaveClicked() {

    }

    @OnItemSelected(R.id.phototype)
    public void onPhotoTypeSpinnerSelected(Spinner spinner, int position) {
    }


    @OnItemSelected(R.id.spin_parentlevel)
    public void onParentSpinnerSelected(Spinner spinner, int position) {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.photo_capture_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
