package com.ivy.sd.png.view.merch;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

public class MerchandisingActivity extends IvyBaseActivityNoActionBar {

    Button btnSBDMerchandising, btnInitiativeMerchandising;
    private BusinessModel bmodel;
    public SBDMerchandisingFragment mSOS;
    InitiativeMerchandisingFragment merchPricing;

    private Toolbar toolbar;

    protected void onCreate(Bundle arg0) {
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);

        super.onCreate(arg0);
        setContentView(R.layout.activity_merchandising);
        Commons.print("title" + getIntent().getStringExtra("screentitle"));
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        //Toolbar setting
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setTitle(
                    getIntent().getStringExtra("screentitle"));
            getSupportActionBar().setIcon(R.drawable.icon_sbd);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.sbdmerchandising).getTag()) != null)
                ((TextView) findViewById(R.id.sbdmerchandising))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.sbdmerchandising)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        btnSBDMerchandising = (Button) findViewById(R.id.sbdmerchandising);
        btnInitiativeMerchandising = (Button) findViewById(R.id.initiativemerchandising);

        btnSBDMerchandising.setSelected(true);
        btnSBDMerchandising.setFocusable(true);
        btnSBDMerchandising.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSOS = new SBDMerchandisingFragment();
                //   mSOS.setFragmentListener(this);

                btnSBDMerchandising.setSelected(true);
                btnSBDMerchandising.setFocusable(true);
                btnInitiativeMerchandising.setSelected(false);
                btnInitiativeMerchandising.setFocusable(false);

                FragmentTransaction mtransaction = getSupportFragmentManager()
                        .beginTransaction();
                mtransaction.replace(R.id.fragment_container, mSOS);
                mtransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                mtransaction.addToBackStack(null);
                mtransaction.commit();
            }
        });
        btnInitiativeMerchandising.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btnInitiativeMerchandising.setSelected(true);
                btnInitiativeMerchandising.setFocusable(true);
                btnSBDMerchandising.setSelected(false);
                btnSBDMerchandising.setFocusable(false);

                if (bmodel.configurationMasterHelper.SHOW_INITIATIVE_MERCHANDISING) {
                    merchPricing = new InitiativeMerchandisingFragment();
                    //     merchPricing.setInitiativeListener(this);
                    FragmentTransaction mtransaction = getSupportFragmentManager()
                            .beginTransaction();
                    mtransaction.replace(R.id.fragment_container, merchPricing);
                    mtransaction
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                    mtransaction.addToBackStack(null);
                    mtransaction.commit();
                }

            }
        });
        mSOS = new SBDMerchandisingFragment();

        if (!bmodel.configurationMasterHelper.SHOW_INITIATIVE_MERCHANDISING) {
            btnInitiativeMerchandising.setVisibility(View.GONE);
        }
        FragmentTransaction mtransaction = getSupportFragmentManager()
                .beginTransaction();
        mtransaction.replace(R.id.fragment_container, mSOS);
        mtransaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        mtransaction.addToBackStack(null);
        mtransaction.commit();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_merchandising, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            ShowDialog(0);
        } else if (i1 == R.id.menu_next) {
            Fragment f = getSupportFragmentManager().findFragmentById(R.id.fragment_container);
            if (f instanceof SBDMerchandisingFragment) {
                ((SBDMerchandisingFragment) f).onsave();
            } else if (f instanceof InitiativeMerchandisingFragment) {
                ((InitiativeMerchandisingFragment) f).onSave();
            }
        }
        return false;
    }

    private void ShowDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(MerchandisingActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(R.string.doyouwantgoback))
                        .setPositiveButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                                        finish();

                                        BusinessModel.loadActivity(MerchandisingActivity.this,
                                                DataMembers.actHomeScreenTwo);
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

                                    }
                                })
                        .setNegativeButton(getResources().getString(R.string.no),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

									/* User clicked Cancel so do some stuff */
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
        }


    }
}
