package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

public class About extends AppCompatActivity {

    private BusinessModel bmodel;
    Toolbar toolbar;
    TextView tv_copyrightWaring, groupTitle12;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        try {
            int theme = bmodel.configurationMasterHelper.getMVPTheme();
            super.setTheme(theme);
            String font = bmodel.configurationMasterHelper.getFontSize();
            setFontStyle(font);
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        setContentView(R.layout.activity_about);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle(getResources().getString(R.string.about_ivy));

        TextView version = (TextView) findViewById(R.id.appVersionTV);
        version.setText(getResources().getString(R.string.version) + bmodel.getApplicationVersionName());

        String supportNo = bmodel.getSupportNo();
        TextView support = (TextView) findViewById(R.id.customerSupport);
        if (supportNo.length() > 0)
            support.setText(supportNo);
        else
            support.setVisibility(View.GONE);


        tv_copyrightWaring = (TextView) findViewById(R.id.copyrightWaringTV);
        groupTitle12 = (TextView) findViewById(R.id.GroupTitle);

        tv_copyrightWaring.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        version.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        groupTitle12.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
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
}
