package com.ivy.cpg.view.stockcheck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class AvailabiltyCheckActivity extends IvyBaseActivityNoActionBar {
    BusinessModel bmodel;
    private Toolbar toolbar;
    private ProductMasterBO mProductMasterBO;
    private String screenTitle, Pid;
    private int mSelectedLocationIndex;
    private View view_dotted_line;
    private RadioButton rbYesPrice, rbNoPrice;
    private InputMethodManager inputManager;
    private EditText mSelectedET;
    private EditText etShelfPiece, etShelfCase, etShelfOuter;
    private EditText etPricePiece, etPriceCase, etPriceOuter;
    private EditText etMrpPricePiece, etMrpPriceCase, etMrpPriceOuter;
    private EditText etExpPiece, etExpCase, etExpOuter;
    private AppCompatCheckBox chkStockListed;
    private AppCompatCheckBox chkStkDistributed;
    private EditText facingQty;
    private Spinner mReason;
    // Adapter used for Load Reason
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private Button btnSave;
    private PriceTrackingHelper priceTrackingHelper;
    private AppCompatCheckBox chkAvailability;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_availabilty_check);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }


        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        priceTrackingHelper = PriceTrackingHelper.getInstance(this);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        screenTitle = getIntent().getStringExtra("screenTitle");
        Pid = getIntent().getStringExtra("pid");
        mSelectedLocationIndex = getIntent().getIntExtra("selectedLocationIndex", 0);

        initializeViews();
        process();

        setNumberPadlistener();

    }

    @Override
    protected void onStart() {
        super.onStart();
        inputManager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }

    private void initializeViews() {
        view_dotted_line = (View) findViewById(R.id.view_dotted_line);
        view_dotted_line.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mSelectedET = null;

        chkAvailability = (AppCompatCheckBox) findViewById(R.id.chk_availability);

        rbYesPrice = (RadioButton) findViewById(R.id.priceYes);
        rbNoPrice = (RadioButton) findViewById(R.id.priceno);

        chkStockListed = (AppCompatCheckBox) findViewById(R.id.is_listed);
        chkStkDistributed = (AppCompatCheckBox) findViewById(R.id.is_distributed);
        facingQty = (EditText) findViewById(R.id.et_faceqty_csValue);
        mReason = (Spinner) findViewById(R.id.mreason);

        etShelfPiece = (EditText) findViewById(R.id.et_avail_pcValue);
        etShelfCase = (EditText) findViewById(R.id.et_avail_csValue);
        etShelfOuter = (EditText) findViewById(R.id.et_avail_ouValue);

        etPricePiece = (EditText) findViewById(R.id.et_price_pcValue);
        etPriceCase = (EditText) findViewById(R.id.et_price_csValue);
        etPriceOuter = (EditText) findViewById(R.id.et_price_ouValue);

        etMrpPricePiece = (EditText) findViewById(R.id.et_priceMrp_pcValue);
        etMrpPriceCase = (EditText) findViewById(R.id.et_priceMrp_csValue);
        etMrpPriceOuter = (EditText) findViewById(R.id.et_priceMrp_ouValue);


        etExpPiece = (EditText) findViewById(R.id.et_exp_pcValue);
        etExpCase = (EditText) findViewById(R.id.et_exp_csValue);
        etExpOuter = (EditText) findViewById(R.id.et_exp_ouValue);

        btnSave = (Button) findViewById(R.id.btn_save);

        rbYesPrice.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        rbNoPrice.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        etShelfPiece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etShelfCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etShelfOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etPricePiece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etPriceCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etPriceOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etMrpPricePiece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etMrpPriceCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etMrpPriceOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etExpPiece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etExpCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        etExpOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.MEDIUM));

        ((TextView) findViewById(R.id.tvTitleAvaialabilty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvTitleCurrentStock)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_avail_pctitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_avail_cstitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_avail_outitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tvTitlePriceCheck)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_price_pcTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_price_csTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_price_ouTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_priceMrp_pcTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_priceMrp_csTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_priceMrp_ouTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_change_price)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tvTitleExpiryCheck)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_exp_pcTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_exp_csTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_exp_ouTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_is_distributed)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_is_listed)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_faceqty_cstitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_avg_qty_label)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_avg_rfield1_label)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_avg_rfield2_label)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_avg_rfield3_label)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.tv_priceMrp_pcTitle).getTag()) != null)
                ((TextView) findViewById(R.id.tv_priceMrp_pcTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.tv_priceMrp_pcTitle).getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.tv_priceMrp_csTitle).getTag()) != null)
                ((TextView) findViewById(R.id.tv_priceMrp_csTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.tv_priceMrp_csTitle).getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.tv_priceMrp_ouTitle).getTag()) != null)
                ((TextView) findViewById(R.id.tv_priceMrp_ouTitle))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.tv_priceMrp_ouTitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }


        loadReason(); // Initialize Adapter and Load Reason

    }

    @SuppressLint("RestrictedApi")
    private void process() {
        if (screenTitle != null)
            setScreenTitle(screenTitle);
        if (Pid != null) {
            if (!bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                mProductMasterBO = bmodel.productHelper.getTaggedProductBOById(Pid);
            } else {
                mProductMasterBO = bmodel.productHelper.getProductMasterBOById(Pid);
            }
        }

        if (mProductMasterBO != null) {

            //is Distributed
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_DD)
                ((LinearLayout) findViewById(R.id.ll_is_distributed)).setVisibility(View.GONE);

            //isListed
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_LD)
                ((LinearLayout) findViewById(R.id.ll_is_listed)).setVisibility(View.GONE);

            //face Qty
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_FC)
                ((LinearLayout) findViewById(R.id.ll_face_qty)).setVisibility(View.GONE);

            //reason spinner
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_RSN)
                ((LinearLayout) findViewById(R.id.ll_reason)).setVisibility(View.GONE);

            //shelf
            if (!bmodel.configurationMasterHelper.SHOW_COMB_STOCK_CB)
                (findViewById(R.id.ll_availability)).setVisibility(View.GONE);
            if (!bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SC)
                ((LinearLayout) findViewById(R.id.ll_avail_case)).setVisibility(View.GONE);
            else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_avail_cstitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_avail_cstitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_avail_cstitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SP)
                ((LinearLayout) findViewById(R.id.ll_avail_piece)).setVisibility(View.GONE);
            else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_avail_pctitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_avail_pctitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_avail_pctitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SHELF_OUTER)
                ((LinearLayout) findViewById(R.id.ll_avail_outer)).setVisibility(View.GONE);
            else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_avail_outitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_avail_outitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_avail_outitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_AVGDAYS) {
                ((LinearLayout) findViewById(R.id.ll_avgdays)).setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SC &&
                        !bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SP &&
                        !bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SHELF_OUTER) {
                    ((TextView) findViewById(R.id.tvTitleAvaialabilty)).setVisibility(View.GONE);
                    findViewById(R.id.ll_availability).setVisibility(View.GONE);
                }


            } else {
                ((TextView) findViewById(R.id.tv_avg_qty_value)).setText(mProductMasterBO.getQty_klgs() + "");
                ((TextView) findViewById(R.id.tv_avg_rfield1_value)).setText(mProductMasterBO.getRfield1_klgs() + "");
                ((TextView) findViewById(R.id.tv_avg_rfield2_value)).setText(mProductMasterBO.getRfield2_klgs() + "");
                ((TextView) findViewById(R.id.tv_avg_rfield3_value)).setText(mProductMasterBO.getCalc_klgs() + "");
            }
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_DD && !bmodel.configurationMasterHelper.SHOW_STOCK_LD
                    && !bmodel.configurationMasterHelper.SHOW_COMB_STOCK_CB)
                ((RelativeLayout) findViewById(R.id.availability_lty)).setVisibility(View.GONE);


            //price
            if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_CS)
                    ((LinearLayout) findViewById(R.id.ll_price_case)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.tv_price_csTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.tv_price_csTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.tv_price_csTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_OU)
                    ((LinearLayout) findViewById(R.id.ll_price_outer)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.tv_price_ouTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.tv_price_ouTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.tv_price_ouTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_PCS)
                    ((LinearLayout) findViewById(R.id.ll_price_piece)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.tv_price_pcTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.tv_price_pcTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.tv_price_pcTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_MRP_CS)
                    ((LinearLayout) findViewById(R.id.ll_priceMrp_case)).setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_MRP_OU)
                    ((LinearLayout) findViewById(R.id.ll_priceMrp_outer)).setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_MRP_PCS)
                    ((LinearLayout) findViewById(R.id.ll_priceMrp_piece)).setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_CS &&
                        !bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_OU &&
                        !bmodel.configurationMasterHelper.SHOW_STOCK_PRICECHECK_PCS)
                    ((LinearLayout) findViewById(R.id.ll_priceRadio_grpLty)).setVisibility(View.GONE);

            } else {
                ((LinearLayout) findViewById(R.id.ll_price_parentLty)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_price_case)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_price_outer)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_price_piece)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_priceMrp_case)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_priceMrp_outer)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_priceMrp_piece)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_priceRadio_grpLty)).setVisibility(View.GONE);
            }
            if (!priceTrackingHelper.SHOW_PRICE_CHANGED) {
                ((LinearLayout) findViewById(R.id.ll_priceRadio_grpLty)).setVisibility(View.GONE);
            } else {
                ((LinearLayout) findViewById(R.id.ll_priceRadio_grpLty)).setVisibility(View.VISIBLE);
            }

            //Expiry
            if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK) {
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_NEAREXPIRY_CS)
                    ((LinearLayout) findViewById(R.id.ll_exp_outer)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.tv_exp_csTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.tv_exp_csTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.tv_exp_csTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_NEAREXPIRY_OU)
                    ((LinearLayout) findViewById(R.id.ll_exp_case)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.tv_exp_ouTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.tv_exp_ouTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.tv_exp_ouTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_STOCK_NEAREXPIRY_PCS)
                    ((LinearLayout) findViewById(R.id.ll_exp_piece)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.tv_exp_pcTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.tv_exp_pcTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.tv_exp_pcTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

            } else {
                ((LinearLayout) findViewById(R.id.ll_expriyCheck_parentLty)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_exp_outer)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_exp_case)).setVisibility(View.GONE);
                ((LinearLayout) findViewById(R.id.ll_exp_piece)).setVisibility(View.GONE);
            }


            chkStockListed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mProductMasterBO.setIsListed(1);
                    } else if (!isChecked) {
                        mProductMasterBO.setIsListed(0);
                    }

                }
            });

            chkStkDistributed.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mProductMasterBO.setIsDistributed(1);
                    } else if (!isChecked) {
                        mProductMasterBO.setIsDistributed(0);
                    }
                }
            });

            if (mProductMasterBO.getIsListed() == 1)
                chkStockListed.setChecked(true);
            else
                chkStockListed.setChecked(false);

            if (mProductMasterBO.getIsDistributed() == 1)
                chkStkDistributed.setChecked(true);
            else
                chkStkDistributed.setChecked(false);


            facingQty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!qty.equals("")) {
                        int wcqty = SDUtil
                                .convertToInt(facingQty
                                        .getText().toString());

                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .setFacingQty(wcqty);
                    } else {
                        facingQty.setText("0");
                    }

                }
            });

            facingQty.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    mSelectedET = facingQty;
                    mSelectedET.setTag(mProductMasterBO);
                    int inType = facingQty
                            .getInputType();
                    facingQty
                            .setInputType(InputType.TYPE_NULL);
                    facingQty.onTouchEvent(event);
                    facingQty.setInputType(inType);
                    facingQty.selectAll();
                    facingQty.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            facingQty
                                    .getWindowToken(), 0);

                    return true;
                }
            });


            etShelfPiece
                    .addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (!qty.equals("")) {
                                int sp_qty = SDUtil
                                        .convertToInt(etShelfPiece
                                                .getText().toString());

                                mProductMasterBO.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setShelfPiece(sp_qty);


                                if (sp_qty > 0
                                        || SDUtil.convertToInt(etShelfCase.getText().toString()) > 0
                                        || SDUtil.convertToInt(etShelfOuter.getText().toString()) > 0) {
                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setAvailability(1);
                                    updateCheckBoxStatus();
                                } else if (sp_qty == 0) {
                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setAvailability(0);
                                    updateCheckBoxStatus();
                                }

                            } else {
                                mProductMasterBO.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setShelfPiece(-1);
                                if (qty.length() == 0
                                        && etShelfCase.getText().toString().length() == 0
                                        && etShelfOuter.getText().toString().length() == 0) {

                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setAvailability(-1);
                                    updateCheckBoxStatus();
                                }

                            }

                            if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                int totValue = getProductTotalValue(mProductMasterBO);
                                if (totValue > 0) {
                                    mReason.setEnabled(false);
                                    mReason.setSelected(false);
                                    mReason.setSelection(0);
                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setReasonId(0);
                                } else {
                                    mReason.setEnabled(true);
                                    mReason.setSelected(true);
                                    mReason.setSelection(getReasonIndex(mProductMasterBO
                                            .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
                                }
                            }

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s,
                                                  int start, int before, int count) {
                        }
                    });

            etShelfPiece
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etShelfPiece;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etShelfPiece
                                    .getInputType();
                            etShelfPiece
                                    .setInputType(InputType.TYPE_NULL);
                            etShelfPiece.onTouchEvent(event);
                            etShelfPiece.setInputType(inType);
                            etShelfPiece.selectAll();
                            etShelfPiece.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etShelfPiece
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });

            etShelfCase
                    .addTextChangedListener(new TextWatcher() {

                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (!qty.equals("")) {
                                int scqty = SDUtil
                                        .convertToInt(etShelfCase
                                                .getText().toString());

                                mProductMasterBO.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setShelfCase(scqty);

                                if (scqty > 0
                                        || SDUtil.convertToInt(etShelfPiece.getText().toString()) > 0
                                        || SDUtil.convertToInt(etShelfOuter.getText().toString()) > 0) {
                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setAvailability(1);
                                    updateCheckBoxStatus();
                                } else if (scqty == 0) {
                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setAvailability(0);
                                    updateCheckBoxStatus();
                                }

                            } else {
                                mProductMasterBO.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setShelfCase(-1);
                                if (qty.length() == 0
                                        && etShelfPiece.getText().toString().length() == 0
                                        && etShelfOuter.getText().toString().length() == 0) {


                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setAvailability(-1);
                                    updateCheckBoxStatus();
                                }
                            }

                            if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                int totValue = getProductTotalValue(mProductMasterBO);
                                if (totValue > 0) {
                                    mReason.setEnabled(false);
                                    mReason.setSelected(false);
                                    mReason.setSelection(0);
                                    mProductMasterBO.getLocations()
                                            .get(mSelectedLocationIndex).setReasonId(0);
                                } else {
                                    mReason.setEnabled(true);
                                    mReason.setSelected(true);
                                    mReason.setSelection(getReasonIndex(mProductMasterBO
                                            .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
                                }
                            }
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {
                        }

                        @Override
                        public void onTextChanged(CharSequence s,
                                                  int start, int before, int count) {
                        }
                    });
            etShelfCase
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etShelfCase;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etShelfCase
                                    .getInputType();
                            etShelfCase
                                    .setInputType(InputType.TYPE_NULL);
                            etShelfCase.onTouchEvent(event);
                            etShelfCase.setInputType(inType);
                            etShelfCase.selectAll();
                            etShelfCase.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etShelfCase
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });
            etShelfOuter.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!qty.equals("")) {
                        int shelfoqty = SDUtil
                                .convertToInt(etShelfOuter
                                        .getText().toString());
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfOuter(shelfoqty);



                        if (shelfoqty > 0
                                || SDUtil.convertToInt(etShelfCase.getText().toString()) > 0
                                || SDUtil.convertToInt(etShelfPiece.getText().toString()) > 0) {
                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);
                            updateCheckBoxStatus();
                        } else if (shelfoqty == 0) {
                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);
                            updateCheckBoxStatus();
                        }

                    } else {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfOuter(-1);
                        if (qty.length() == 0
                                && etShelfPiece.getText().toString().length() == 0
                                && etShelfCase.getText().toString().length() == 0) {

                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);
                            updateCheckBoxStatus();
                        }
                    }

                    if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                        int totValue = getProductTotalValue(mProductMasterBO);
                        if (totValue > 0) {
                            mReason.setEnabled(false);
                            mReason.setSelected(false);
                            mReason.setSelection(0);
                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setReasonId(0);
                        } else {
                            mReason.setEnabled(true);
                            mReason.setSelected(true);
                            mReason.setSelection(getReasonIndex(mProductMasterBO
                                    .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
                        }
                    }

                }
            });

            etShelfOuter.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    mSelectedET = etShelfOuter;
                    mSelectedET.setTag(mProductMasterBO);
                    int inType = etShelfOuter.getInputType();
                    etShelfOuter.setInputType(InputType.TYPE_NULL);
                    etShelfOuter.onTouchEvent(event);
                    etShelfOuter.setInputType(inType);
                    etShelfOuter.selectAll();
                    etShelfOuter.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            etShelfOuter.getWindowToken(), 0);
                    return true;
                }
            });

            etPricePiece.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        int sp_qty = SDUtil
                                .convertToInt(etPricePiece
                                        .getText().toString());
                        mProductMasterBO.setPrice_pc(qty);
                        if (!rbYesPrice.isChecked()
                                && sp_qty > 0)
                            rbYesPrice.setChecked(true);

                        else if (sp_qty <= 0)
                            rbNoPrice.setChecked(true);
                    }
                }
            });

            etPricePiece
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etPricePiece;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etPricePiece
                                    .getInputType();
                            etPricePiece
                                    .setInputType(InputType.TYPE_NULL);
                            etPricePiece.onTouchEvent(event);
                            etPricePiece.setInputType(inType);
                            etPricePiece.selectAll();
                            etPricePiece.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etPricePiece
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });

            etPriceCase.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.setPrice_ca(qty);
                    }

                }
            });

            etPriceCase
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etPriceCase;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etPriceCase
                                    .getInputType();
                            etPriceCase
                                    .setInputType(InputType.TYPE_NULL);
                            etPriceCase.onTouchEvent(event);
                            etPriceCase.setInputType(inType);
                            etPriceCase.selectAll();
                            etPriceCase.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etPriceCase
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });

            etPriceOuter.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.setPrice_oo(qty);
                    }

                }
            });

            etPriceOuter
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etPriceOuter;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etPriceOuter
                                    .getInputType();
                            etPriceOuter
                                    .setInputType(InputType.TYPE_NULL);
                            etPriceOuter.onTouchEvent(event);
                            etPriceOuter.setInputType(inType);
                            etPriceOuter.selectAll();
                            etPriceOuter.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etPriceOuter
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });


            /**  MRP price Check**/


            etMrpPricePiece.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        int sp_qty = SDUtil
                                .convertToInt(etMrpPricePiece
                                        .getText().toString());
                        mProductMasterBO.setMrp_pc(qty);
                    }
                }
            });

            etMrpPricePiece
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etMrpPricePiece;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etMrpPricePiece
                                    .getInputType();
                            etMrpPricePiece
                                    .setInputType(InputType.TYPE_NULL);
                            etMrpPricePiece.onTouchEvent(event);
                            etMrpPricePiece.setInputType(inType);
                            etMrpPricePiece.selectAll();
                            etMrpPricePiece.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etMrpPricePiece
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });

            etMrpPriceCase.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.setMrp_ca(qty);
                    }

                }
            });

            etMrpPriceCase
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etMrpPriceCase;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etMrpPriceCase
                                    .getInputType();
                            etMrpPriceCase
                                    .setInputType(InputType.TYPE_NULL);
                            etMrpPriceCase.onTouchEvent(event);
                            etMrpPriceCase.setInputType(inType);
                            etMrpPriceCase.selectAll();
                            etMrpPriceCase.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etMrpPriceCase
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });

            etMrpPriceOuter.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.setMrp_ou(qty);
                    }

                }
            });

            etMrpPriceOuter
                    .setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            mSelectedET = etMrpPriceOuter;
                            mSelectedET.setTag(mProductMasterBO);
                            int inType = etMrpPriceOuter
                                    .getInputType();
                            etMrpPriceOuter
                                    .setInputType(InputType.TYPE_NULL);
                            etMrpPriceOuter.onTouchEvent(event);
                            etMrpPriceOuter.setInputType(inType);
                            etMrpPriceOuter.selectAll();
                            etMrpPriceOuter.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    etMrpPriceOuter
                                            .getWindowToken(), 0);
                            return true;
                        }
                    });


            /** MRP Price Check **/

            etExpPiece.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .getNearexpiryDate().get(0)
                                .setNearexpPC(qty);
                    } else {
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .getNearexpiryDate().get(0)
                                .setNearexpPC(qty);
                        etExpPiece.setText(qty);
                    }


                }
            });
            etExpCase.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .getNearexpiryDate().get(0)
                                .setNearexpCA(qty);
                    } else {
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .getNearexpiryDate().get(0)
                                .setNearexpCA(qty);
                        etExpCase.setText(qty);
                    }


                }
            });
            etExpOuter.addTextChangedListener(new TextWatcher() {

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }

                @Override
                public void beforeTextChanged(CharSequence s,
                                              int start, int count, int after) {

                }

                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (!"".equals(qty)) {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .getNearexpiryDate().get(0)
                                .setNearexpOU(qty);
                    } else {
                        qty = qty.length() > 1 ? qty.substring(0,
                                qty.length() - 1) : "0";
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex)
                                .getNearexpiryDate().get(0)
                                .setNearexpOU(qty);
                        etExpOuter.setText(qty);
                    }


                }
            });

            etExpPiece.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    mSelectedET = etExpPiece;
                    mSelectedET.setTag(mProductMasterBO);
                    int inType = etExpPiece.getInputType();
                    etExpPiece.setInputType(InputType.TYPE_NULL);
                    etExpPiece.onTouchEvent(event);
                    etExpPiece.setInputType(inType);
                    etExpPiece.selectAll();
                    etExpPiece.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            etExpPiece.getWindowToken(), 0);
                    return true;
                }
            });

            etExpCase.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    mSelectedET = etExpCase;
                    mSelectedET.setTag(mProductMasterBO);
                    int inType = etExpCase.getInputType();
                    etExpCase.setInputType(InputType.TYPE_NULL);
                    etExpCase.onTouchEvent(event);
                    etExpCase.setInputType(inType);
                    etExpCase.selectAll();
                    etExpCase.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            etExpCase.getWindowToken(), 0);
                    return true;
                }
            });
            etExpOuter.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View arg0, MotionEvent event) {
                    mSelectedET = etExpOuter;
                    mSelectedET.setTag(mProductMasterBO);
                    int inType = etExpOuter.getInputType();
                    etExpOuter.setInputType(InputType.TYPE_NULL);
                    etExpOuter.onTouchEvent(event);
                    etExpOuter.setInputType(inType);
                    etExpOuter.selectAll();
                    etExpOuter.requestFocus();
                    inputManager.hideSoftInputFromWindow(
                            etExpOuter.getWindowToken(), 0);
                    return true;
                }
            });

            //Initial load
            updateCheckBoxStatus();

            //        chkStkDistributed.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.Yellow)));

            chkAvailability.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex).getAvailability() == -1) {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex).setAvailability(1);
                        updateCheckBoxStatus();

                        if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                            mReason.setEnabled(false);
                            mReason.setSelected(false);
                            mReason.setSelection(0);
                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setReasonId(0);
                        }

                        if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SP)
                            etShelfPiece.setText("1");
                        else if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SC)
                            etShelfPiece.setText("1");
                        else if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SHELF_OUTER)
                            etShelfPiece.setText("1");

                    } else if (mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex).getAvailability() == 1) {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex).setAvailability(0);
                        updateCheckBoxStatus();

                        if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                            mReason.setEnabled(true);
                            mReason.setSelected(true);
                            mReason.setSelection(0);
                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setReasonId(0);
                        }


                        if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SP)
                            etShelfPiece.setText("0");
                        else if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SC)
                            etShelfPiece.setText("0");
                        else if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SHELF_OUTER)
                            etShelfPiece.setText("0");

                    } else if (mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex).getAvailability() == 0) {
                        mProductMasterBO.getLocations()
                                .get(mSelectedLocationIndex).setAvailability(-1);
                        updateCheckBoxStatus();

                        if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                            mReason.setEnabled(false);
                            mReason.setSelected(false);
                            mReason.setSelection(0);
                            mProductMasterBO.getLocations()
                                    .get(mSelectedLocationIndex).setReasonId(0);
                        }

                        if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SP)
                            etShelfPiece.setText("");
                        else if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SC)
                            etShelfPiece.setText("");
                        else if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SHELF_OUTER)
                            etShelfPiece.setText("");


                    }

                }
            });


            mReason.setAdapter(spinnerAdapter);

            mReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    ReasonMaster reString = (ReasonMaster) mReason
                            .getSelectedItem();

                    mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex)
                            .setReasonId(SDUtil.convertToInt(reString.getReasonID()));


                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            rbYesPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        rbNoPrice.setChecked(false);
                        rbYesPrice.setButtonDrawable(R.drawable.ic_tick_enable);
                        rbYesPrice.setTextColor(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.plano_yes_green));
                        rbNoPrice.setTextColor(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.plano_yes_grey));
                        rbNoPrice.setButtonDrawable(R.drawable.ic_cross_disable);
                        mProductMasterBO.setPriceChanged(1);
                        if (mProductMasterBO
                                .getPrice_pc().toString().equals("0"))
                            etPricePiece.setText("1");
                    }

                }
            });
            rbNoPrice.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if (isChecked) {
                        rbYesPrice.setChecked(false);
                        rbYesPrice.setButtonDrawable(R.drawable.ic_tick_disable);
                        rbNoPrice.setButtonDrawable(R.drawable.ic_cross_enable);
                        rbYesPrice.setTextColor(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.plano_yes_grey));
                        rbNoPrice.setTextColor(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.plano_no_red));
                        mProductMasterBO.setPriceChanged(0);
                        etPricePiece.setText("0");
                    }

                }
            });

            btnSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

            String strFacingQty = mProductMasterBO.getLocations()
                    .get(mSelectedLocationIndex).getFacingQty()
                    + "";
            facingQty.setText(strFacingQty);

            if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                if (mProductMasterBO.getLocations()
                        .get(mSelectedLocationIndex)
                        .getShelfPiece() > -1 || mProductMasterBO.getLocations()
                        .get(mSelectedLocationIndex).getAvailability() > -1) {
                    mReason.setEnabled(false);
                    mReason.setSelected(false);
                    mReason.setSelection(0);
                } else {
                    mReason.setEnabled(true);
                    mReason.setSelected(true);
                    mReason.setSelection(getReasonIndex(mProductMasterBO
                            .getLocations().get(mSelectedLocationIndex).getReasonId() + ""));
                }
            }



            if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SP)
                if (mProductMasterBO.getLocations()
                        .get(mSelectedLocationIndex).getShelfPiece() > -1) {
                    String strShelfPiece = mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex).getShelfPiece()
                            + "";
                    etShelfPiece.setText(strShelfPiece);
                } else {
                    etShelfPiece.setText("");
                }

            if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SC)
                if (mProductMasterBO.getLocations()
                        .get(mSelectedLocationIndex).getShelfCase() > -1) {
                    String strShelfCase = mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex).getShelfCase()
                            + "";
                    etShelfCase.setText(strShelfCase);
                } else {
                    etShelfCase.setText("");
                }

            if (bmodel.configurationMasterHelper.SHOW_COMB_STOCK_SHELF_OUTER)
                if (mProductMasterBO.getLocations()
                        .get(mSelectedLocationIndex).getShelfOuter() > -1) {
                    String strShelfOuter = mProductMasterBO.getLocations()
                            .get(mSelectedLocationIndex).getShelfOuter()
                            + "";
                    etShelfOuter.setText(strShelfOuter);
                } else {
                    etShelfOuter.setText("");
                }

            etPriceOuter.setText(mProductMasterBO.getPrice_oo());
            etPriceCase.setText(mProductMasterBO.getPrice_ca());
            etPricePiece.setText(mProductMasterBO.getPrice_pc());

            etMrpPriceOuter.setText(mProductMasterBO.getMrp_ou());
            etMrpPriceCase.setText(mProductMasterBO.getMrp_ca());
            etMrpPricePiece.setText(mProductMasterBO.getMrp_pc());


            etExpCase.setText(mProductMasterBO.getLocations()
                    .get(mSelectedLocationIndex).getNearexpiryDate().get(0)
                    .getNearexpCA());
            etExpOuter.setText(mProductMasterBO.getLocations()
                    .get(mSelectedLocationIndex).getNearexpiryDate().get(0)
                    .getNearexpOU());
            etExpPiece.setText(mProductMasterBO.getLocations()
                    .get(mSelectedLocationIndex).getNearexpiryDate().get(0)
                    .getNearexpPC());


        }
    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        spinnerAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("INVT")
                    || temp.getReasonCategory().equalsIgnoreCase("NONE"))
                spinnerAdapter.add(temp);
        }
    }


    /**
     * Load selected reason name in the Screen
     *
     * @param reasonId
     * @return position of the reason id
     */
    private int getReasonIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private final View.OnClickListener mNumperPadListener = new View.OnClickListener() {

        @Override
        public void onClick(View v) {

            int i = v.getId();
            if (i == R.id.calczero || i == R.id.calcone || i == R.id.calctwo || i == R.id.calcthree
                    || i == R.id.calcfour || i == R.id.calcfive || i == R.id.calcsix
                    || i == R.id.calcseven || i == R.id.calceight || i == R.id.calcnine) {
                eff(((Button) v).getText().toString());
            } else if (i == R.id.calcdel) {
                String s = mSelectedET.getText().toString();

                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        if (mSelectedET.getId() == etShelfOuter.getId() || mSelectedET.getId() == etShelfCase.getId() || mSelectedET.getId() == etShelfPiece.getId()) {
                            s = "";
                        } else {
                            s = "0";
                        }

                    }
                }
                mSelectedET.setText(s);

            }

        }
    };

    private void eff(String val) {
        if (mSelectedET != null && mSelectedET.getText() != null) {

            String s = mSelectedET.getText().toString();

            if ("0".equals(s) || "0.0".equals(s) || "0.00".equals(s))
                mSelectedET.setText(val);
            else
                mSelectedET.setText(mSelectedET.getText().append(val));
        }
    }

    private void setNumberPadlistener() {
        findViewById(R.id.calczero)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcone)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calctwo)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcthree)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcfour)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcfive)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcsix)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcseven)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calceight)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcnine)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcdel)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcdot)
                .setOnClickListener(mNumperPadListener);
        findViewById(R.id.calcdot).setVisibility(View.GONE);
    }

    public int getProductTotalValue(ProductMasterBO product) {
        int totalQty = 0;
        Vector<StandardListBO> locationList = bmodel.productHelper
                .getInStoreLocation();

        int size = locationList.size();
        for (int i = 0; i < size; i++) {

            if (product.getLocations().get(i).getShelfPiece() > -1)
                totalQty += product.getLocations().get(i).getShelfPiece();
            if (product.getLocations().get(i).getShelfCase() > -1)
                totalQty += (product.getLocations().get(i).getShelfCase() * product
                        .getCaseSize());
            if (product.getLocations().get(i).getShelfOuter() > -1)
                totalQty += (product.getLocations().get(i).getShelfOuter() * product
                        .getOutersize());
        }
        return totalQty;

    }

    /*
    * update CheckBox color based on Availability
    * Availability =  1 stock available with checked status
    * Availability =  0 stock not available with checked status
    * Availability = -1 Stock not checked  with unchecked status
    */
    private void updateCheckBoxStatus() {
        if (mProductMasterBO.getLocations()
                .get(mSelectedLocationIndex).getAvailability() == 1) {
            CompoundButtonCompat.setButtonTintList(chkAvailability, ColorStateList.valueOf(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.colorAccent)));
            chkAvailability.setChecked(true);
        } else if (mProductMasterBO.getLocations()
                .get(mSelectedLocationIndex).getAvailability() == 0) {
            CompoundButtonCompat.setButtonTintList(chkAvailability, ColorStateList.valueOf(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.RED)));
            chkAvailability.setChecked(true);
        } else if (mProductMasterBO.getLocations()
                .get(mSelectedLocationIndex).getAvailability() == -1) {
            CompoundButtonCompat.setButtonTintList(chkAvailability, ColorStateList.valueOf(ContextCompat.getColor(AvailabiltyCheckActivity.this, R.color.checkbox_default_color)));
            chkAvailability.setChecked(false);
        }

    }
}


