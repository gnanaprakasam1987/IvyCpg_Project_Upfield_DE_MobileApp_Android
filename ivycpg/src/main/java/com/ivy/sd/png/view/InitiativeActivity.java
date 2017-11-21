package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InitiativeHeaderBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.Vector;

public class InitiativeActivity extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private LinearLayout main;
    private BusinessModel bmodel;
    private Vector<InitiativeHeaderBO> initiativeHeaderBOVector;
    private Button initiativeButton[];
    private boolean isClicked=false;
    private String screenCode = "MENU_STK_ORD";
    private Toolbar toolbar;
    private Button btn_next;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.initiative);

        main = (LinearLayout) findViewById(R.id.initiativeLayout);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
            }
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        // Set title to toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setScreenTitle(getResources().getString(R.string.initiative));
        getSupportActionBar().setIcon(null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        btn_next = (Button) findViewById(R.id.btn_next);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        btn_next.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                    if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                        Intent i = new Intent(InitiativeActivity.this,
                                DigitalContentDisplay.class);
                        i.putExtra("ScreenCode", screenCode);
                        i.putExtra("FromInit", "Initiative");
                        startActivity(i);
//                    finish();
                    } else {
                        Intent i = new Intent(InitiativeActivity.this,
                                OrderSummary.class);
                        i.putExtra("ScreenCode", screenCode);
                        startActivity(i);
//                    finish();
                    }
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();

            }
        });

        // Download the list of Initiatives.
        initiativeHeaderBOVector = bmodel.initiativeHelper
                .getInitiativeHeaderBOVector();

        if (initiativeHeaderBOVector.size() == 0) {
            ((ScrollView) findViewById(R.id.initiativeLayout_lay)).setVisibility(View.GONE);
            Toast.makeText(this,
                    getResources().getString(R.string.no_initiative_available),
                    Toast.LENGTH_SHORT).show();
            return;
        }

    }

    @Override
    protected void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        isClicked = false;

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        // Clear all the view
        main.removeAllViews();

        // Load the Initiative details and check weather its covered or not.
        // set isUpdateAchievement to false to stop updation on each and every
        // time
        bmodel.initiativeHelper.loadInitiativeStatus(false, bmodel
                .getRetailerMasterBO().getRetailerID(), false, false, bmodel
                .getRetailerMasterBO().getSubchannelid());

        initiativeButton = new Button[initiativeHeaderBOVector.size()];

        if (initiativeHeaderBOVector.size() > 0)
            constructScreen();
    }

    private void constructScreen() {

        int siz = initiativeHeaderBOVector.size();

        for (int j = 0; j < siz; j++) {
            InitiativeHeaderBO sbd = initiativeHeaderBOVector.get(j);

            LayoutInflater inflater = getLayoutInflater();
            View view = inflater
                    .inflate(R.layout.row_initiative_hit_miss, null);
            initiativeButton[j] = (Button) view.findViewById(R.id.text);
            ImageView i = (ImageView) view.findViewById(R.id.icon);
            initiativeButton[j].setText(sbd.getDescription());
            initiativeButton[j].setTag(sbd);
            initiativeButton[j].setOnClickListener(this);
            initiativeButton[j].setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (sbd.isDone() && sbd.isDistributed()) {
                i.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.ok_tick));
                initiativeButton[j].setText(sbd.getDescription());
                initiativeButton[j].setTextColor(Color.BLACK);
            } else if (sbd.isDistributed() && !sbd.isDone()) {
                i.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.icon_alert));
                initiativeButton[j].setText(sbd.getDescription());
                initiativeButton[j].setTextColor(Color.BLACK);
            } else {
                i.setBackgroundDrawable(getResources().getDrawable(
                        R.drawable.not_cross));
                initiativeButton[j].setText(sbd.getDescription());
                initiativeButton[j].setTextColor(Color.RED);
            }
            view.setPadding(2, 4, 2, 4);
            TextView tv = new TextView(this);
            tv.setText(" ");
            main.addView(tv);
            main.addView(view);
        }

    }


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_CANCELED) {
                } else if (resultCode == RESULT_OK) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
        }
    }

    private InitiativeDialog initDialog;

    @Override
    public void onClick(View v) {
        if (!isClicked) {
            isClicked = true;
            Button b = (Button) v;
            InitiativeHeaderBO init = (InitiativeHeaderBO) b.getTag();
            initDialog = new InitiativeDialog(this, init, this);
            initDialog.show();
            initDialog.setCancelable(false);
        }
    }

    public void numberPressed(View vw) {
        initDialog.numberPressed(vw);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try {
                ((ViewGroup) view).removeAllViews();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {// Intent returnIntent = new Intent();
            // setResult(RESULT_CANCELED, returnIntent);
            // finish();
            if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                Intent init = new Intent(InitiativeActivity.this,
                        OrderDiscount.class);
                init.putExtra("ScreenCode", screenCode);
                startActivity(init);
//                finish();
            } else if (bmodel.configurationMasterHelper.IS_SCHEME_ON
                    && bmodel.configurationMasterHelper.IS_SCHEME_SHOW_SCREEN) {
                Intent intent = new Intent(InitiativeActivity.this,
                        SchemeApply.class);
                intent.putExtra("ScreenCode", screenCode);
                startActivity(intent);
            } else if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                Intent intent = new Intent(InitiativeActivity.this,
                        CrownReturnActivity.class);
                intent.putExtra("OrderFlag", "Nothing");
                intent.putExtra("ScreenCode", screenCode);
                startActivity(intent);
            } else if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                Intent intent = new Intent(InitiativeActivity.this,
                        BatchAllocation.class);
                intent.putExtra("OrderFlag", "Nothing");
                intent.putExtra("ScreenCode", screenCode);
                startActivity(intent);
            } else {
                Intent intent;
                if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                    intent = new Intent(InitiativeActivity.this, CatalogOrder.class);
                } else {
                    intent = new Intent(InitiativeActivity.this, StockAndOrder.class);
                }
                intent.putExtra("OrderFlag", "Nothing");
                intent.putExtra("ScreenCode", screenCode);
                startActivity(intent);
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        }
        return false;
    }
}
