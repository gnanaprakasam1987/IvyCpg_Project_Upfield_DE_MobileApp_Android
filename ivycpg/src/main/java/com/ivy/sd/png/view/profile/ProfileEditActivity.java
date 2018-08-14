package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.sd.png.view.NewoutletContainerFragment;
import com.ivy.ui.profile.edit.view.ProfileEditFragmentNew;

import java.util.Vector;

public class ProfileEditActivity extends IvyBaseActivityNoActionBar
        implements NearByRetailerDialog.NearByRetailerInterface {

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
            Toast.makeText(ProfileEditActivity.this, getResources().getString(R.string.sessionout_loginagain), Toast.LENGTH_SHORT).show();
            finish();
        }
        if (bmodel.configurationMasterHelper.IS_CONTACT_TAB){
            Fragment fragment = new NewoutletContainerFragment();
            Bundle bundle = new Bundle();
            bundle.putBoolean("isEdit",true);
            fragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }else{
            Fragment fragment = new ProfileEditFragmentNew();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(null)
                    .commit();
        }

    }




    @Override
    public void onBackPressed() {}

    @Override
    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        ProfileEditFragmentNew profileEditFragmentNew;
        if (getSupportFragmentManager().findFragmentById(R.id.activity_profile_edit) instanceof ProfileEditFragmentNew) {
            profileEditFragmentNew = (ProfileEditFragmentNew) getSupportFragmentManager().findFragmentById(R.id.activity_profile_edit);
            profileEditFragmentNew.updateNearByRetailer(list);
        }
    }

    public void updateCancel() {
        setResult(2);
        finish();
    }
}
