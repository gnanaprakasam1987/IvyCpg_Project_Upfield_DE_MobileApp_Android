package com.ivy.ui.activation.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.ActivationDialog;
import com.ivy.sd.png.view.ScreenActivationFragment;
import com.ivy.ui.activation.ActivationContract;
import com.ivy.ui.activation.data.ActivationError;
import com.ivy.ui.activation.di.ActivationModule;
import com.ivy.ui.activation.di.DaggerActivationComponent;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DeviceUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ActivationActivity extends BaseActivity implements ActivationContract.ActivationView {

    @Inject
    ActivationContract.ActivationPresenter<ActivationContract.ActivationView> mActivationPresenter;

    @BindView(R.id.activationKey)
    EditText mActivationKeyEdt;

    @BindView(R.id.tv_already_activated)
    TextView mAlreadyActivatedTxt;

    @BindView(R.id.version)
    TextView mVersionNameTxt;

    private ActivationDialog activation;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_screen_activation;
    }

    @Override
    protected void initVariables() {
        setUnBinder(ButterKnife.bind(this));
    }

    @Override
    public void initializeDi() {
        DaggerActivationComponent.builder()
                .activationModule(new ActivationModule())
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);

        mActivationPresenter.onAttach(this);


    }

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

    }

    @Override
    protected void getMessageFromAliens() {

    }


    @SuppressLint("SetTextI18n")
    @Override
    protected void setUpViews() {

        mVersionNameTxt.setText(getString(R.string.version) + AppUtils.getApplicationVersionName(this));
    }

    @Override
    public void showActivationEmptyError() {

    }

    @Override
    public void showInvalidActivationError() {

    }

    @Override
    public void navigateToLoginScreen() {

    }

    @Override
    public void showInvalidUrlError() {

    }

    //private ProgressDialog progressDialogue;
    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;

    @Override
    public void showLoading() {
        super.showLoading();

        builder = new AlertDialog.Builder(this);
        alertDialog = builder.create();
        // alertDialog.show();
    }

    @Override
    public void hideLoading() {
        super.hideLoading();
        if (alertDialog != null)
            alertDialog.dismiss();
    }

    @Override
    public void showActivationError(ActivationError activationError) {
        Toast.makeText(this, "" + activationError.getMessage() + activationError.getStatus(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showAppUrlIsEmptyError() {

    }

    @Override
    public void showJsonExceptionError() {

    }

    @Override
    public void showServerError() {

    }

    @Override
    public void showActivatedSuccessMessage() {

    }

    @Override
    public void showPreviousActivationError() {
        showMessage(R.string.previous_activation_not_done_for_this_device);
    }

    @Override
    public void showActivationDialog() {
        ActivationDialog activation = new ActivationDialog(
                this, addUrl);
        activation.setCancelable(false);
        activation.show();
    }

    @Override
    public void showTryValidKeyError() {

    }

    @Override
    public void doValidationSuccess() {
        mActivationPresenter.doActivation(mActivationKeyEdt.getText().toString(),
                DeviceUtils.getIMEINumber(this),
                AppUtils.getApplicationVersionName(this),
                AppUtils.getApplicationVersionNumber(this));

    }

    @Override
    public void showActivationFailedError() {

    }

    DialogInterface.OnDismissListener addUrl = new DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            activation.dismiss();
            mActivationPresenter.doActionForActivationDismiss();

        }
    };

}
