package com.ivy.core.base.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

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

public abstract class BaseBottomSheetDialogFragment extends BottomSheetDialogFragment implements BaseIvyView{
    private Unbinder mUnBinder;
    private Dialog dialog;
    private TextView progressMsgTxt;

    public Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }


    @SuppressLint("RestrictedApi")
    @Override
    public void setupDialog(Dialog dialog, int style) {
        super.setupDialog(dialog, style);

        View view = LayoutInflater.from(getContext()).inflate(setContentViewLayout(), null);
        dialog.setContentView(view);

        mUnBinder = ButterKnife.bind(this, view);initializeDi();

        getMessageFromAliens();

        initVariables(dialog,view);

        setUpViews();

    }

    private BasePresenter mBasePresenter;

    public void setBasePresenter(BasePresenter presenter) {
        mBasePresenter = presenter;
    }

    public abstract void initializeDi();

    protected abstract int setContentViewLayout();

    public abstract void initVariables(Dialog dialog,View view);

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
        ((BaseActivity) context).showLoading(strinRes);
    }

    @Override
    public void showLoading(String message) {
        ((BaseActivity) context).showLoading(message);
    }

    @Override
    public void showLoading() {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).showLoading();
        else
            showDialog(getString(R.string.loading));

    }

    public void showDialog(String msg) {

        if (dialog == null) {
            dialog = new Dialog(context);
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
        if (context instanceof BaseActivity)
            ((BaseActivity) context).hideLoading();
        else
            hideLoadingCustom();
    }

    @Override
    public void hideKeyboard() {
        ((BaseActivity) context).hideKeyboard();
    }

    @Override
    public void onError(int resId) {
        ((BaseActivity) context).onError(resId);
    }

    @Override
    public void onError(String message) {
        ((BaseActivity) context).onError(message);
    }

    @Override
    public void showMessage(int resId) {

        if (context instanceof BaseActivity) {
            ((BaseActivity) context).showMessage(resId);
        } else {
            Toast.makeText(context, getString(resId), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(String message) {
        if (context instanceof BaseActivity) {
            ((BaseActivity) context).showMessage(message);
        } else {
            if (message != null) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(context, getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean isNetworkConnected() {
        return ((BaseActivity) context).isNetworkConnected();
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
        if (context instanceof BaseActivity)
            ((BaseActivity) context).showAlert(title, msg);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).showAlert(title, msg, null);
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener) {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).showAlert(title, msg, positiveClickListener);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).showAlert(title, msg, positiveClickListener);
    }

    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, boolean isCancelable) {
        ((BaseActivity) context).showAlert(title, msg, positiveClickListener, isCancelable);
    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener, CommonDialog.negativeOnClickListener negativeOnClickListener) {
        ((BaseActivity) context).showAlert(title, msg, positiveClickListener, negativeOnClickListener);
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
        if (context instanceof BaseActivity)
            ((BaseActivity) context).startActivity(activity);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).startActivity(activity);

    }

    public void startActivityAndFinish(Class activity) {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).startActivityAndFinish(activity);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).startActivity(activity);

    }

    public void requestLocation(final Activity ctxt) {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).requestLocation(ctxt);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).requestLocation(ctxt);
    }

    @Override
    public void setScreenTitle(String title) {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).setScreenTitle(title);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).setScreenTitle(title);
    }

    @Override
    public void setUpToolbar(String title) {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).setUpToolbar(title);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).setUpToolbar(title);
    }

    public void applyAlertDialogTheme(Context context, AlertDialog.Builder builder) {
        if (context instanceof BaseActivity)
            ((BaseActivity) context).applyAlertDialogTheme(context, builder);
        else if (context instanceof IvyBaseActivityNoActionBar)
            ((IvyBaseActivityNoActionBar) context).applyAlertDialogTheme(context, builder);
    }

    // Todo to be removed
    public boolean checkAndRequestPermissionAtRunTime(int mGroup) {
        List<String> listPermissionsNeeded = new ArrayList<>();
        int permissionStatus;
        if (mGroup == PHONE_STATE_AND_WRITE_PERMISSON) {
            permissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_PHONE_STATE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.READ_PHONE_STATE);
            }

            permissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }


            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), PHONE_STATE_AND_WRITE_PERMISSON);
                return false;
            }
        } else if (mGroup == CAMERA_AND_WRITE_PERMISSION) {
            permissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.CAMERA);
            }

            permissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
            if (permissionStatus != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (!listPermissionsNeeded.isEmpty()) {
                requestPermissionsSafely(listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]), CAMERA_AND_WRITE_PERMISSION);
                return false;
            }
        } else if (mGroup == LOCATION_PERMISSION) {
            permissionStatus = ContextCompat.checkSelfPermission(context,
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
}
