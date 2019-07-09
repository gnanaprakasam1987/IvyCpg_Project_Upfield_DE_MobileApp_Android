package com.ivy.cpg.view.settings;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.cpg.view.login.LoginHelper;
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
            if (bmodel.configurationMasterHelper.MVPTheme == 0) {
                super.setTheme(bmodel.configurationMasterHelper.getMVPTheme());
            } else {
                super.setTheme(bmodel.configurationMasterHelper.MVPTheme);
            }

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

        String supportNo = LoginHelper.getInstance(this).getSupportNo(getApplicationContext());
        TextView support = (TextView) findViewById(R.id.customerSupport);
        if (supportNo.length() > 0)
            support.setText(supportNo);
        else
            support.setVisibility(View.GONE);


        tv_copyrightWaring = (TextView) findViewById(R.id.copyrightWaringTV);
        groupTitle12 = (TextView) findViewById(R.id.GroupTitle);

        tv_copyrightWaring.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        version.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        groupTitle12.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

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

}
