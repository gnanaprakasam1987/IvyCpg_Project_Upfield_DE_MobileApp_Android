package com.ivy.ui.profile.edit.view;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.ui.profile.view.ProfileBaseFragment;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.util.ArrayList;
import java.util.Vector;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import io.reactivex.Observer;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.observers.DisposableObserver;

public class ProfileEditActivity extends IvyBaseActivityNoActionBar
        implements NearByRetailerDialog.NearByRetailerInterface {

    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
            AppSchedulerProvider appSchedulerProvider = new AppSchedulerProvider();
            ArrayList<RetailerContactBo> retailerContactList=new ArrayList<>();
            bmodel.newOutletHelper.setRetailerContactList(retailerContactList); //Just for clear the old contact list
            new CompositeDisposable().add((Disposable) bmodel.profilehelper.downloadRetailerContact(bmodel.getAppDataProvider().getRetailMaster().getRetailerID(), true)
                    .subscribeOn(appSchedulerProvider.io())
                    .observeOn(appSchedulerProvider.ui())
                    .subscribeWith(arrayListObserver()));
        }else
            openProfileEditScreen();



    }

    private Observer<ArrayList<RetailerContactBo>> arrayListObserver() {
        return new DisposableObserver<ArrayList<RetailerContactBo>>() {
            @Override
            public void onNext(ArrayList<RetailerContactBo> contactList) {
                bmodel.newOutletHelper.setRetailerContactList(contactList);
                openProfileEditScreen();
            }

            @Override
            public void onError(Throwable e) {
                Commons.print(e.getMessage());
            }

            @Override
            public void onComplete() {
            }
        };
    }

    private void openProfileEditScreen(){
        Fragment fragment = new ProfileBaseFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("isEdit",true);
        bundle.putString("retailerId", bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
           finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
