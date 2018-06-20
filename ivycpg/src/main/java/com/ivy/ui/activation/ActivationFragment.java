package com.ivy.ui.activation;


import android.Manifest;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.ActivationDialog;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.FontUtils;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;
import me.relex.circleindicator.CircleIndicator;


public class ActivationFragment extends BaseFragment {


    @BindView(R.id.version)
    TextView version;

    @BindView(R.id.activate)
    ImageButton activate;

    @BindView(R.id.activationKey)
    EditText activationKey;

    @BindView(R.id.pager)
    ViewPager viewPager;

    @BindView(R.id.indicator)
    CircleIndicator circleIndicator;

    @BindView(R.id.tv_already_activated)
    TextView alReadyActivated;

    @Inject
    public SharedPreferences appPreferences;

    @Inject
    public BusinessModel businessModel;

    private ActivationDialog activationDialog;

    private String appUrl;

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_screen_activation;
    }

    @Override
    public void initVariables(View view) {

    }

    private void setUpViewPager() {
        ViewPagerAdapter adapterViewPager = new ViewPagerAdapter(getActivity().getSupportFragmentManager());
        viewPager.setAdapter(adapterViewPager);
        circleIndicator.setViewPager(viewPager);
    }

    private void setUpTypeFace() {
        version.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, getActivity().getApplicationContext()));
    }

    @OnClick(R.id.tv_already_activated)
    public void doActivate() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            String imEi = DeviceUtils.getIMEINumber(getActivity().getApplicationContext());
            if (!imEi.matches("[0]+")) {
                // new ScreenActivationFragment.DoActivation().execute(5);
            } else {
                Toast.makeText(getActivity(), getResources().getString(R.string.telephony_not_avail), Toast.LENGTH_LONG).show();
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.permission_enable_msg) +
                    " " + getResources().getString(R.string.permission_phone), Toast.LENGTH_LONG).show();
        }
    }


    @OnClick(R.id.activate)
    public void activate() {

        String imEi = DeviceUtils.getIMEINumber(getActivity().getApplicationContext());
        if (!imEi.matches("[0]+")) {
            // activateOnClick();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.telephony_not_avail), Toast.LENGTH_LONG).show();
        }
    }

}
