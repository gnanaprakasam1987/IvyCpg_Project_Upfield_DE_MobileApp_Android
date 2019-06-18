package com.ivy.core.base.view;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.apptutoriallibrary.AppTutorialPlugin;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.ivy.core.base.view.BaseActivity.CAMERA_AND_WRITE_PERMISSION;
import static com.ivy.core.base.view.BaseActivity.LOCATION_PERMISSION;
import static com.ivy.core.base.view.BaseActivity.PHONE_STATE_AND_WRITE_PERMISSON;

public abstract class BaseFragment extends Fragment implements BaseIvyView {


    private Unbinder mUnBinder;
    private Dialog dialog;
    private TextView progressMsgTxt;

    public boolean isPreVisit = false;
    public Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        isPreVisit = ((Activity)context).getIntent().getBooleanExtra("PreVisit",false);

        return inflater.inflate(setContentViewLayout(), container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mUnBinder = ButterKnife.bind(this, view);
        init(view);

        initializeDi();

        getMessageFromAliens();

        setUpViews();
        super.onViewCreated(view, savedInstanceState);

    }

    private BasePresenter mBasePresenter;

    public void setBasePresenter(BasePresenter presenter) {
        mBasePresenter = presenter;
    }

    public abstract void initializeDi();

    protected abstract int setContentViewLayout();

    public abstract void init(View view);

    /**
     * Abstract method which can be used to get the data
     * via intent for other activities
     */
    protected abstract void getMessageFromAliens();

    /**
     * Set up the views.
     */
    protected abstract void setUpViews();


    @Override
    public void showLoading(int strinRes) {
        ((BaseActivity) getActivity()).showLoading(strinRes);
    }

    @Override
    public void showLoading(String message) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showLoading(message);
        else
            showDialog(message);
    }

    @Override
    public void showLoading() {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showLoading();
        else
            showDialog(getString(R.string.loading));

    }

    public void showDialog(String msg) {

        if (dialog == null) {
            dialog = new Dialog(getActivity());
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.setCancelable(false);
            dialog.setContentView(R.layout.custom_alert_dialog);

            TextView title = dialog.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            progressMsgTxt = dialog.findViewById(R.id.text);
        }
        progressMsgTxt.setText(msg);

        dialog.show();

    }

    public void hideLoadingCustom() {
        if (dialog != null)
            dialog.dismiss();
    }

    @Override
    public void hideLoading() {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).hideLoading();
        else
            hideLoadingCustom();
    }

    @Override
    public void hideKeyboard() {
        ((BaseActivity) getActivity()).hideKeyboard();
    }

    @Override
    public void onError(int resId) {
        ((BaseActivity) getActivity()).onError(resId);
    }

    @Override
    public void onError(String message) {
        ((BaseActivity) getActivity()).onError(message);
    }

    @Override
    public void showMessage(int resId) {

        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showMessage(resId);
        } else {
            Toast.makeText(getActivity(), getString(resId), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(String message) {
        if (getActivity() instanceof BaseActivity) {
            ((BaseActivity) getActivity()).showMessage(message);
        } else {
            if (message != null) {
                Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getActivity(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean isNetworkConnected() {
        return ((BaseActivity) getActivity()).isNetworkConnected();
    }


    /**
     * Set the unBinder object from butter knife so that the unbinding can be
     * taken care from the base activity on destroy
     *
     * @param unBinder unBinder of the ButterKnife
     */
    public void setUnBinder(Unbinder unBinder) {
        mUnBinder = unBinder;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mBasePresenter.onDetach();
        try {
            mUnBinder.unbind();
            mUnBinder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void setLayoutDirection(int direction) {

    }

    @Override
    public void handleLayoutDirection(String language) {

    }

    @Override
    public void setBlueTheme() {

    }

    @Override
    public void setRedTheme() {

    }

    @Override
    public void setOrangeTheme() {

    }

    @Override
    public void setGreenTheme() {

    }

    @Override
    public void setPinkTheme() {

    }

    @Override
    public void setNavyBlueTheme() {

    }

    @Override
    public void setFontSize(String fontSize) {

    }

    @Override
    public void showAlert(String title, String msg) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showAlert(title, msg);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).showAlert(title, msg, null);
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showAlert(title, msg, positiveClickListener);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).showAlert(title, msg, positiveClickListener);
    }

    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, boolean isCancelable) {
        ((BaseActivity) getActivity()).showAlert(title, msg, positiveClickListener, isCancelable);
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, CommonDialog.negativeOnClickListener negativeOnClickListener) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).showAlert(title, msg, positiveClickListener, negativeOnClickListener);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).showAlert(title, msg, positiveClickListener, negativeOnClickListener);
    }

    @Override
    public void createNFCManager() {

    }

    @Override
    public void resumeNFCManager() {

    }

    @Override
    public void pauseNFCManager() {

    }

    public void startActivity(Class activity) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).startActivity(activity);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).startActivity(activity);

    }

    public void startActivityAndFinish(Class activity) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).startActivityAndFinish(activity);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).startActivity(activity);

    }

    public void requestLocation(final Activity ctxt) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).requestLocation(ctxt);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).requestLocation(ctxt);
    }

    @Override
    public void setScreenTitle(String title) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).setScreenTitle(title);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).setScreenTitle(title);
    }

    @Override
    public void setUpToolbar(String title) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).setUpToolbar(title);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).setUpToolbar(title);
    }

    public void applyAlertDialogTheme(Context context, AlertDialog.Builder builder) {
        if (getActivity() instanceof BaseActivity)
            ((BaseActivity) getActivity()).applyAlertDialogTheme(context, builder);
        else if (getActivity() instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) getActivity()).applyAlertDialogTheme(context, builder);
    }

    // Todo to be removed
    public boolean checkAndRequestPermissionAtRunTime(int mGroup) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int permissionStatus;
        if (mGroup == PHONE_STATE_AND_WRITE_PERMISSON) {
            permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.READ_PHONE_STATE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }


            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PHONE_STATE_AND_WRITE_PERMISSON);
                return false;
            }
        } else if (mGroup == CAMERA_AND_WRITE_PERMISSION) {
            permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.CAMERA);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), CAMERA_AND_WRITE_PERMISSION);
                return false;
            }
        } else if (mGroup == LOCATION_PERMISSION) {
            permissionStatus = ContextCompat.checkSelfPermission(getContext(),
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                if (mBasePresenter.isLocationConfigurationEnabled()) {
                    listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION);
                }
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), LOCATION_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        AppTutorialPlugin.getInstance().onResume();

    }
}
