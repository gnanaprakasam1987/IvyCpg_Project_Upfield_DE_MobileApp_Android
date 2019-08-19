package com.ivy.ui.gallery.view;

import android.os.Bundle;
import android.os.PersistableBundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentManager;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Inject;

public class GalleryActivity extends BaseActivity {
    public static int currentPosition;
    private static final String KEY_CURRENT_POSITION = "com.ivy.ui.gallery.view.key.currentPosition";


    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_gallery;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {
        DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) viewBasePresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(getString(R.string.gallery));
        }

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager
                .beginTransaction()
                .add(R.id.fragment_container, new GalleryFragment(), GalleryFragment.class.getSimpleName())
                .commit();

    }

    @Override
    public void onBackPressed() {
        getSupportFragmentManager().popBackStack();
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            super.onBackPressed();
        }
    }
}
