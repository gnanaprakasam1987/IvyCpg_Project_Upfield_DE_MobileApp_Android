package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.NearByRetailerDialog;

import java.util.Vector;

public class ProfileEditActivity extends IvyBaseActivityNoActionBar implements NearByRetailerDialog.NearByRetailerInterface {
    private BusinessModel bmodel;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);


        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            setScreenTitle(getResources().getString(R.string.profile_edit_screen__title));
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }




    @Override
    public void onBackPressed() {

    }

    @Override
    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        ProfileEditFragment mProfileEditFragment;
        if (getSupportFragmentManager().findFragmentById(R.id.activity_profile_edit) instanceof ProfileEditFragment) {
            mProfileEditFragment = (ProfileEditFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.activity_profile_edit);
            mProfileEditFragment.updateNearByRetailer(list);
        }
    }


    public void updateCancel() {
        setResult(2);
        finish();
    }
}
