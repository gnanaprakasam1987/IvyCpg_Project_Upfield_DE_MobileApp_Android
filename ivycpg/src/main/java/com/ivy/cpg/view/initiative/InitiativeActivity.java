package com.ivy.cpg.view.initiative;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.scheme.SchemeApply;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.utils.FontUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.Vector;

public class InitiativeActivity extends IvyBaseActivityNoActionBar implements
        OnClickListener {

    private LinearLayout main;
    private BusinessModel bmodel;
    private Vector<InitiativeHeaderBO> initiativeHeaderBOVector;
    private Button initiativeButton[];
    private boolean isClicked=false;
    private String screenCode = "MENU_STK_ORD";
    private final int INITIATIVE_RESULT_CODE = 117;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.initiative);

        main = findViewById(R.id.initiativeLayout);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
            }
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setIcon(null);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // Set title to toolbar
        setScreenTitle(getResources().getString(R.string.initiative));

        Button btn_next = findViewById(R.id.btn_next);
        if (bmodel.getAppDataProvider().getUser().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        btn_next.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                    if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                        Intent i = new Intent(InitiativeActivity.this,
                                DigitalContentActivity.class);
                        i.putExtra("ScreenCode", screenCode);
                        i.putExtra("FromInit", "Initiative");
                        startActivity(i);
                    } else {
                        Intent i = new Intent(InitiativeActivity.this,
                                OrderSummary.class);
                        i.putExtra("ScreenCode", screenCode);
                        startActivity(i);
                    }
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();

            }
        });

        // Download the list of Initiatives.
        initiativeHeaderBOVector = bmodel.initiativeHelper
                .getInitiativeHeaderBOVector();

        if (initiativeHeaderBOVector.size() == 0) {
            (findViewById(R.id.initiativeLayout_lay)).setVisibility(View.GONE);
            Toast.makeText(this,
                    getResources().getString(R.string.no_initiative_available),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isClicked = false;

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.getAppDataProvider().getUser().getUserid() == 0) {
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
                .getAppDataProvider().getRetailMaster().getRetailerID(), false, false, bmodel
                .getAppDataProvider().getRetailMaster().getSubchannelid());

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
            initiativeButton[j] = view.findViewById(R.id.text);
            ImageView i = view.findViewById(R.id.icon);
            initiativeButton[j].setText(sbd.getDescription());
            initiativeButton[j].setTag(sbd);
            initiativeButton[j].setOnClickListener(this);
            initiativeButton[j].setTypeface(FontUtils.getFontRoboto(InitiativeActivity.this, FontUtils.FontType.MEDIUM));
            if (sbd.isDone() && sbd.isDistributed()) {
                i.setBackground(getResources().getDrawable(
                        R.drawable.ok_tick));
                initiativeButton[j].setText(sbd.getDescription());
                initiativeButton[j].setTextColor(Color.BLACK);
            } else if (sbd.isDistributed() && !sbd.isDone()) {
                i.setBackground(getResources().getDrawable(
                        R.drawable.icon_alert));
                initiativeButton[j].setText(sbd.getDescription());
                initiativeButton[j].setTextColor(Color.BLACK);
            } else {
                i.setBackground(getResources().getDrawable(
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
                if (resultCode == RESULT_OK) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            case INITIATIVE_RESULT_CODE :
                overridePendingTransition(0, R.anim.zoom_exit);
                break;
        }
    }

    @Override
    public void onClick(View v) {
        if (!isClicked) {
            isClicked = true;
            Button b = (Button) v;

            InitiativeHeaderBO init = (InitiativeHeaderBO) b.getTag();

            Intent intent = new Intent(InitiativeActivity.this,InitiativeEditActivity.class);
            Bundle bundle = new Bundle();
            bundle.putParcelable("INITIATIVE_BO",init);

            intent.putExtras(bundle);

            ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
            ActivityCompat.startActivityForResult(this, intent, INITIATIVE_RESULT_CODE, opts.toBundle());

        }
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
                Commons.printException(e);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                Intent init = new Intent(InitiativeActivity.this,
                        OrderDiscount.class);
                init.putExtra("ScreenCode", screenCode);
                startActivity(init);
            } else if (schemeHelper.IS_SCHEME_ON
                    && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
                Intent intent = new Intent(InitiativeActivity.this,
                        SchemeApply.class);
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
