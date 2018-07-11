package com.ivy.ui.photocapture.view;

import android.content.Intent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.photocapture.PhotoCaptureContract;
import com.ivy.ui.photocapture.di.DaggerPhotoCaptureComponent;
import com.ivy.ui.photocapture.di.PhotoCaptureModule;

import javax.inject.Inject;

import butterknife.ButterKnife;

public class PhotoCaptureActivity extends BaseActivity implements PhotoCaptureContract.PhotoCaptureView {

    @Inject
    PhotoCaptureContract.PhotoCapturePresenter<PhotoCaptureContract.PhotoCaptureView> photoCapturePresenter;


    private boolean isFromMenuClick;

    @Override
    public int getLayoutId() {
        return R.layout.activity_photo_capture;
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
}
