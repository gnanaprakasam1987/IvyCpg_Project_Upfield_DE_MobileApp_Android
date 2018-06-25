package com.ivy.core.base.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.AppUtils;
import com.ivy.utils.NetworkUtils;

import java.util.Locale;

import butterknife.Unbinder;

public abstract class BaseActivity extends AppCompatActivity implements BaseIvyView {


    private ConfigurationMasterHelper configurationMasterHelper;

    private Unbinder mUnBinder;

    private NFCManager nfcManager;

    /**
     * Always set you layout reference using this method
     *
     * @return
     */
    @LayoutRes
    public abstract int getLayoutId();


    /**
     * Initialize your view ids's or variables if needed. (Ex: ButterKnife)
     */
    protected abstract void initVariables();


    public abstract void initializeDi();

    private BasePresenter mBasePresenter;

    public void setBasePresenter(BasePresenter presenter) {
        mBasePresenter = presenter;
        if (mBasePresenter != null) {
            mBasePresenter.getAppTheme();
            mBasePresenter.getAppFontSize();
        }

    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        this.setContentView(this.getLayoutId());

        initializeDi();

        getMessageFromAliens();

        setUpViews();

        initVariables();

    }

    private void setUpDefaults() {

        AppUtils.useNetworkProvidedValues(this);

        configurationMasterHelper = ConfigurationMasterHelper.getInstance(this);

        if (configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER) {
            nfcManager = new NFCManager(this);
            nfcManager.onActivityCreate();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER && nfcManager != null) {
            nfcManager.onActivityResume();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER && nfcManager != null) {
            nfcManager.onActivityPause();
        }
    }


    @TargetApi(Build.VERSION_CODES.M)
    public void requestPermissionsSafely(String[] permissions, int requestCode) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, requestCode);
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    public boolean hasPermission(String permission) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void showLoading() {

    }

    @Override
    public void hideLoading() {

    }

    @Override
    public void onError(int resId) {
        onError(getString(resId));
    }


    @Override
    public void handleLayoutDirection(String language) {
        /*Local Configuration Change Language and layout direction */
        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(language)) {
            locale = new Locale(language.substring(0, 2));
            Locale.setDefault(locale);
            config.locale = locale;
            getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());

            /*Below code used to change the layout direction */
            if (locale.getLanguage().equalsIgnoreCase("ar"))
                setLayoutDirection(View.LAYOUT_DIRECTION_RTL);
            else
                setLayoutDirection(View.LAYOUT_DIRECTION_LTR);

        }
    }

    /**
     * Changes layout direction
     *
     * @param direction 0 or 1
     *                  View.LAYOUT_DIRECTION_RTL 1
     *                  View.LAYOUT_DIRECTION_LTR 0
     */
    @Override
    public void setLayoutDirection(int direction) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            this.getWindow().getDecorView().setLayoutDirection(direction);
        }
    }

    @Override
    public void onError(String message) {
        if (message != null) {
            showSnackBar(message);
        } else {
            showSnackBar(getString(R.string.error));
        }
    }

    private void showSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content),
                message, Snackbar.LENGTH_SHORT);
        View sbView = snackbar.getView();
        TextView textView = (TextView) sbView
                .findViewById(android.support.design.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.show();
    }

    @Override
    public void showMessage(String message) {
        if (message != null) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void showMessage(int resId) {
        showMessage(getString(resId));
    }

    /**
     * To validate if network is connected
     *
     * @return true if Connected. False if not connected
     */

    @Override
    public boolean isNetworkConnected() {
        return NetworkUtils.isNetworkConnected(this);
    }

    @Override
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
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
    protected void onDestroy() {

        if (mUnBinder != null) {
            mUnBinder.unbind();
        }

        if (mBasePresenter != null)
            mBasePresenter.onDetach();

        super.onDestroy();
    }


    /**
     * Abstract method which can be used to get the data
     * via intent for other activities
     */
    protected abstract void getMessageFromAliens();

    /**
     * Set up the views.
     */
    protected abstract void setUpViews();


    public void addFragment(int containerViewId, Fragment fragment, boolean addStack,
                            boolean isReplace, int animationType) {

        if (fragment == null) {
            return;
        }


        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        String tag = fragment.getClass().toString();


        if (isReplace) {
            for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
                fragmentManager.popBackStack();
            }
            fragmentManager.executePendingTransactions();
        }


        if (fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            if (!isReplace) {
                Fragment currentFragment;
                fragmentTransaction.add(containerViewId, fragment, tag);
                if ((currentFragment = getSupportFragmentManager().findFragmentById(containerViewId)) != null) {
                    fragmentTransaction.hide(currentFragment);
                }
            } else {
                fragmentTransaction.replace(containerViewId, fragment, tag);
            }
        }

        if (addStack) {
            fragmentTransaction.addToBackStack(tag);
        } else {
            fragmentManager.popBackStack(tag,
                    FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }
        fragmentTransaction.commit();
        fragmentManager.executePendingTransactions();
    }


    public void setOrientation(boolean isLandscape) {
        if (isLandscape) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }


    public void setFontStyle(String font) {
        if (font.equalsIgnoreCase("Small")) {
            getTheme().applyStyle(R.style.FontStyle_Small, true);
        } else if (font.equalsIgnoreCase("Medium")) {
            getTheme().applyStyle(R.style.FontStyle_Medium, true);
        } else if (font.equalsIgnoreCase("Large")) {
            getTheme().applyStyle(R.style.FontStyle_Large, true);
        } else {
            getTheme().applyStyle(R.style.FontStyle_Small, true);
        }
    }


    public void setScreenTitle(String title) {
        this.screenTitle = title;
        TextView mScreenTitleTV = findViewById(R.id.tv_toolbar_title);
        // mScreenTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mScreenTitleTV.setText(title);


    }


    private String screenTitle;

    public String getScreenTitle() {
        return screenTitle;
    }

    @Override
    public void setBlueTheme() {
        setTheme(R.style.MVPTheme_Blue);
    }

    @Override
    public void setPinkTheme() {
        setTheme(R.style.MVPTheme_Pink);
    }

    @Override
    public void setGreenTheme() {
        setTheme(R.style.MVPTheme_Green);
    }

    @Override
    public void setNavyBlueTheme() {
        setTheme(R.style.MVPTheme_NBlue);
    }

    @Override
    public void setOrangeTheme() {
        setTheme(R.style.MVPTheme_Orange);
    }

    @Override
    public void setRedTheme() {
        setTheme(R.style.MVPTheme_Red);
    }

    @Override
    public void setFontSize(String fontSize) {
        setFontStyle(fontSize);
    }
}


