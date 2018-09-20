package com.ivy.cpg.view.reports.damageReturn;

import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.ivy.cpg.view.order.scheme.SchemeDetailsFragment;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.ProductDetailsFragment;
import com.ivy.sd.png.view.profile.ProfileEditActivity;
import com.ivy.sd.png.view.profile.RetailerContactBo;
import com.ivy.ui.profile.edit.view.ProfileEditFragmentNew;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by nagaganesh.n on 4/27/2017.
 */

public class DamageDetailsActivity extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    String invoiceNo;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            setScreenTitle(getResources().getString(R.string.details));
        }
        if (getIntent().getExtras() != null) {
            invoiceNo = getIntent().getExtras().getString("InvoiceNo");
            status = getIntent().getExtras().getString("status");
        }
        Bundle bndl = new Bundle();
        bndl.putString("invoiceNo", invoiceNo);
        bndl.putString("status", status);
        Fragment fragment = new DamageReturnDetail();
        fragment.setArguments(bndl);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateCancel() {
        setResult(2);
        finish();
    }
    @Override
    public void onBackPressed() {}


}
