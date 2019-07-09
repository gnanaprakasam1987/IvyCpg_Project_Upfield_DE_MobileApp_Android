package com.ivy.ui.activation.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.di.ActivationModule;
import com.ivy.ui.activation.di.DaggerActivationComponent;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DeviceUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.annotations.Nullable;

public class ActivationActivity extends BaseActivity implements ActivationContract.ActivationView {

    @Inject
    ActivationContract.ActivationPresenter<ActivationContract.ActivationView> mActivationPresenter;

    @BindView(R.id.activationKey)
    EditText mActivationKeyEdt;

    @BindView(R.id.tv_already_activated)
    TextView mAlreadyActivatedTxt;

    @BindView(R.id.version)
    TextView mVersionNameTxt;

    @BindView(R.id.toolbar)
    Toolbar mToolBar;

    private ActivationDialog activation;

    @Override
    public int getLayoutId() {
        return R.layout.activity_activation;
    }

    @Override
    protected void initVariables() {
        setUnBinder(ButterKnife.bind(this));
    }

    @Override
    public void initializeDi() {
        DaggerActivationComponent.builder()
                .activationModule(new ActivationModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) mActivationPresenter);

    }


    @Nullable
    @OnClick(R.id.activate)
    void onActivateClick() {
        if (isNetworkConnected())
            if (hasPermission(Manifest.permission.READ_PHONE_STATE))
                if (!DeviceUtils.getIMEINumber(this).matches("[0]+"))
                    mActivationPresenter.validateActivationKey(mActivationKeyEdt.getText().toString());
                else
                    showMessage(R.string.telephony_not_avail);
            else
                showMessage(getString(R.string.permission_enable_msg) + " " + getString(R.string.permission_phone));
        else
            showMessage(getResources().getString(R.string.no_network_connection));
    }

    @OnClick(R.id.tv_already_activated)
    void onAlreadyActivatedClick() {
        if (isNetworkConnected())
            if (hasPermission(Manifest.permission.READ_PHONE_STATE))
                if (!DeviceUtils.getIMEINumber(this).matches("[0]+"))
                    mActivationPresenter.triggerIMEIActivation(DeviceUtils.getIMEINumber(this), AppUtils.getApplicationVersionName(this), AppUtils.getApplicationVersionNumber(this));
                else
                    showMessage(R.string.telephony_not_avail);
            else
                showMessage(getString(R.string.permission_enable_msg) + " " + getString(R.string.permission_phone));
        else
            showMessage(getResources().getString(R.string.no_network_connection));
    }

    @Override
    protected void getMessageFromAliens() {
    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void setUpViews() {

        setSupportActionBar(mToolBar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mVersionNameTxt.setText(getString(R.string.version) + AppUtils.getApplicationVersionName(this));
    }

    @Override
    public void showActivationEmptyError() {
        showAlert("", getResources().getString(R.string.enter_activation_id));
    }

    @Override
    public void showInvalidActivationError() {
        showAlert("", getResources().getString(R.string.activation_key_should_be_sixteen_character));
    }

    @Override
    public void showAppUrlIsEmptyError() {
        showMessage(R.string.app_url_is_empty);
    }

    @Override
    public void showServerError() {
        showMessage(getResources().getString(R.string.contact_system_admin));
    }

    @Override
    public void showPreviousActivationError() {
        showMessage(getResources().getString(R.string.previous_activation_not_done_for_this_device));
    }

    @Override
    public void showTryValidKeyError() {
        showAlert("", getResources().getString(R.string.invalid_key_try_with_valid_key));
    }

    @Override
    public void showActivationFailedError() {
        showAlert("", getResources().getString(R.string.activation_failed));
    }

    @Override
    public void showConfigureUrlMessage() {
        showMessage(getResources().getString(R.string.please_check_app_url_configured));
    }

    @Override
    public void showContactAdminMessage() {
        showMessage(getResources().getString(R.string.valid_key_oops_contact_device_admin));
    }

    @Override
    public void showActivationError(String activationError) {
        showMessage(activationError);
    }


    @Override
    public void showSuccessfullyActivatedAlert() {
        showAlert("", getString(R.string.successfully_activated), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                navigateToLoginScreen();
            }
        });
    }


    @Override
    public void showActivationDialog() {
        activation = new ActivationDialog(
                this, activationDialogDismissListener, mActivationPresenter.getAppUrls());
        activation.setCancelable(false);
        activation.show();
    }

    @Override
    public void doValidationSuccess() {
        mActivationPresenter.doActivation(mActivationKeyEdt.getText().toString(),
                DeviceUtils.getIMEINumber(this),
                AppUtils.getApplicationVersionName(this),
                AppUtils.getApplicationVersionNumber(this));

    }

    @Override
    public void navigateToLoginScreen() {
        startActivity(new Intent(ActivationActivity.this, LoginScreen.class));
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return super.onOptionsItemSelected(item);
    }

    DialogInterface.OnDismissListener activationDialogDismissListener = new DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            if (activation != null)
                activation.dismiss();
            mActivationPresenter.doActionForActivationDismiss();

        }
    };

}
