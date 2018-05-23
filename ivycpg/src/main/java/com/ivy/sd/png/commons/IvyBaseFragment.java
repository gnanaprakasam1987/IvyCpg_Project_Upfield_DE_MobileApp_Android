package com.ivy.sd.png.commons;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.squareup.leakcanary.RefWatcher;

import java.util.Locale;

public class IvyBaseFragment extends Fragment implements ApplicationConfigs {


    TextView mScreenTitleTV;
    BusinessModel bmodel;
    TextView messagetv;

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(getActivity());


        Configuration config = new Configuration();
        Locale locale = config.locale;
        if (!Locale.getDefault().equals(
                sharedPrefs.getString("languagePref", LANGUAGE))) {
            locale = new Locale(sharedPrefs.getString("languagePref", LANGUAGE).substring(0, 2));
            Locale.setDefault(locale);
            config.locale = locale;
            getActivity().getResources().updateConfiguration(config,
                    getActivity().getResources().getDisplayMetrics());
        }
        super.onConfigurationChanged(newConfig);
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    public void setScreenTitle(String title) {

        mScreenTitleTV = (TextView) getActivity().findViewById(R.id.tv_toolbar_title);
        if (mScreenTitleTV != null) {
            mScreenTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            mScreenTitleTV.setText(title);
        }
    }

    public void setToolbarColor(Bitmap bitmap) {
        // Generate the palette and get the vibrant swatch
        // See the createPaletteSync() and checkVibrantSwatch() methods
        // from the code snippets above
        Palette p = createPaletteSync(bitmap);
        Palette.Swatch vibrantSwatch = checkVibrantSwatch(p);

        // Set the toolbar background and text colors
        Toolbar toolbar = (Toolbar) getActivity().findViewById(R.id.toolbar);
        if (vibrantSwatch != null)
            toolbar.setBackgroundColor(vibrantSwatch.getRgb());
//        toolbar.setTitleTextColor(vibrantSwatch.getTitleTextColor());
    }

    // Generate palette synchronously and return it
    public Palette createPaletteSync(Bitmap bitmap) {
        Palette p = Palette.from(bitmap).generate();
        return p;
    }

    // Return a palette's vibrant swatch after checking that it exists
    private Palette.Swatch checkVibrantSwatch(Palette p) {
        if (p != null) {
            Palette.Swatch vibrant = p.getVibrantSwatch();
            if (vibrant != null) {
                return vibrant;
            }
            // Throw error
        }
        return null;
    }

    public void onBackPressed() {
    }


    public void customProgressDialog(AlertDialog.Builder builder, String message) {

        try {
            View view = View.inflate(getActivity(), R.layout.custom_alert_dialog, null);

            TextView title = (TextView) view.findViewById(R.id.title);
            title.setText(DataMembers.SD);
            messagetv = (TextView) view.findViewById(R.id.text);
            messagetv.setText(message);

            builder.setView(view);
            builder.setCancelable(false);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void updaterProgressMsg(String msg) {
        if (messagetv != null)
            messagetv.setText(msg);
    }

    @Override public void onDestroy() {
        super.onDestroy();
        RefWatcher refWatcher = BusinessModel.getRefWatcher(getActivity());
        refWatcher.watch(this);
    }

}
