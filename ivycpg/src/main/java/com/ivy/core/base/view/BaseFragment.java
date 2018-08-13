package com.ivy.core.base.view;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.DataMembers;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class BaseFragment extends Fragment implements BaseIvyView {


    private Unbinder mUnBinder;
    private Dialog dialog;
    private TextView progressMsgTxt;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(setContentViewLayout(), container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);
        initVariables(view);

        initializeDi();

        getMessageFromAliens();

        setUpViews();

    }

    private BasePresenter mBasePresenter;

    public void setBasePresenter(BasePresenter presenter) {
        mBasePresenter = presenter;
    }

    public abstract void initializeDi();

    protected abstract int setContentViewLayout();

    public abstract void initVariables(View view);

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
        ((BaseActivity) getActivity()).showLoading(message);
    }

    @Override
    public void showLoading() {
        if(getActivity() instanceof BaseActivity)
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
        if(getActivity() instanceof BaseActivity)
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
        ((BaseActivity) getActivity()).showMessage(resId);
    }

    @Override
    public void showMessage(String message) {
        if(getActivity() instanceof BaseActivity){
            ((BaseActivity) getActivity()).showMessage(message);
        }else{
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

    }

    @Override
    public void showAlert(String title, String msg, CommonDialog.PositiveClickListener positiveClickListener) {

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

}
