package com.ivy.sd.png.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.InvoiceHeaderBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.print.CommonPrintPreviewActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import javax.activation.CommandMap;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by rajesh.k on 23-02-2016.
 */
public class DeliveryManagementDetail extends IvyBaseActivityNoActionBar implements View.OnClickListener {
    private static final String TAG = "DeliveryManagementDetail";
    private BusinessModel bmodel;
    private TextView mInvoiceNoTV;
    private MaterialSpinner mInvoiceTypeSpin;
    private ListView mProductDetailsLV;
    private LinearLayout mTitleLL, mKeyPadLL;
    private ArrayList<ProductMasterBO> mProductList;

    private static final String DELIVERY_TYPE = "DELIVERY_TYPE";

    private ArrayList<String> mDeliveryTypeList;
    private String mInvoiceNo = "";
    private MyAdapter myAdapter;

    private EditText QUANTITY;
    private String append = "";
    private String mSelectedItem;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private int flag = -1;

    private int mSelectedPrintCount = 0;
    private AlertDialog.Builder builder10;

    private TextView tv_sihTitle, chk_title;
    private Toolbar toolbar;
    private Button saveBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivermanagement_detail);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        mInvoiceNoTV = (TextView) findViewById(R.id.tv_invoice_no);
        mInvoiceTypeSpin = (MaterialSpinner) findViewById(R.id.spin_payment);
        mProductDetailsLV = (ListView) findViewById(R.id.lv_productlist);
        mTitleLL = (LinearLayout) findViewById(R.id.ll_title);
        mKeyPadLL = (LinearLayout) findViewById(R.id.ll_keypad);
        mDeliveryTypeList = new ArrayList<String>();
        mDeliveryTypeList.add(getResources().getString(R.string.fullfilled));
        mDeliveryTypeList.add(getResources().getString(R.string.partially_fullfilled));
        mDeliveryTypeList.add(getResources().getString(R.string.rejected));

        tv_sihTitle = (TextView) findViewById(R.id.sihTitle);
        chk_title = (TextView) findViewById(R.id.chk_title);
        if (!bmodel.configurationMasterHelper.IS_SIH_VALIDATION_ON_DELIVERY)
            tv_sihTitle.setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_PC)
            findViewById(R.id.tv_op).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_CA)
            findViewById(R.id.tv_oc).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_OU)
            findViewById(R.id.tv_oo).setVisibility(View.GONE);

        myAdapter = new MyAdapter();
        if (getIntent().getExtras() != null) {
            mInvoiceNo = getIntent().getExtras().getString("invoiceno");
            //mInvoiceHeaderBO=(InvoiceHeaderBO)getIntent().getParcelableArrayExtra("invoiceBo");
        }

        mInvoiceNoTV.setText(mInvoiceNo);
        setScreenTitle("" + mInvoiceNo);

        // load data
        bmodel.deliveryManagementHelper.downloadDeliveryProductDetails(mInvoiceNo);
        //bmodel.deliveryManagementHelper.downloadInvoiceProductDetails(mInvoiceNo);
        mProductList = bmodel.deliveryManagementHelper.getmInvoiceDetailsList();

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeliveryManagementDetail.this,
                android.R.layout.simple_spinner_item, mDeliveryTypeList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mInvoiceTypeSpin.setAdapter(adapter);

        mInvoiceTypeSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSelectedItem = (String) mInvoiceTypeSpin.getSelectedItem();
                if (mSelectedItem.equals(getResources().getString(R.string.rejected))) {
                    mProductDetailsLV.setVisibility(View.GONE);
                    mTitleLL.setVisibility(View.GONE);
                    mKeyPadLL.setVisibility(View.GONE);

                } else {
                    if (mSelectedItem.equals(getResources().getString(R.string.fullfilled)))
                        chk_title.setVisibility(View.GONE);
                    else
                        chk_title.setVisibility(View.VISIBLE);

                    mProductDetailsLV.setVisibility(View.VISIBLE);
                    mTitleLL.setVisibility(View.VISIBLE);
                    mKeyPadLL.setVisibility(View.VISIBLE);

                    mProductDetailsLV.setAdapter(myAdapter);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        saveBtn = (Button) findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(this);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
    }

    @Override
    protected void onResume() {
        super.onResume();

        bmodel.setContext(this);
    }

    @Override
    public void onClick(View v) {
        Button vw = (Button) v;
        if (vw == saveBtn) {

            if (isDeliveryNotGreaterOrder()) {

                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION_ON_DELIVERY && isStockAvailable().size() > 0) {
                    mProductDetailsLV.invalidateViews();
                    Toast.makeText(DeliveryManagementDetail.this, getResources().getString(R.string.stock_not_available), Toast.LENGTH_SHORT).show();
                } else {
                    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                    CustomFragment dialogFragment = new CustomFragment(this);
                    /*Bundle bundle = new Bundle();
                    bundle.putString("title", "Delivery Management Dialog");
                    bundle.putString("textviewTitle", getResources().getString(R.string.do_u_want_to_save_delivery_management));
                    dialogFragment.setArguments(bundle);*/
                    dialogFragment.show();
                }

            } else {
                Toast.makeText(DeliveryManagementDetail.this, "Qty exceed ", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mProductList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Holder holder;
            if (convertView == null) {
                holder = new Holder();
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.list_delivery_product_details, parent,
                        false);
                holder.deliveryCB = (CheckBox) convertView.findViewById(R.id.cb_delivery);
                holder.productNameTV = (TextView) convertView.findViewById(R.id.tv_productname);
                holder.batchNoTV = (TextView) convertView.findViewById(R.id.tv_batch_no);
                holder.pieceET = (EditText) convertView.findViewById(R.id.edit_pcs_qty);
                holder.caseET = (EditText) convertView.findViewById(R.id.edit_case_qty);
                holder.outerET = (EditText) convertView.findViewById(R.id.edit_outer_qty);
                holder.sih = (TextView) convertView.findViewById(R.id.sih);

                holder.productNameTV.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.batchNoTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.pieceET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.caseET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.outerET.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_PC)
                    holder.pieceET.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_CA)
                    holder.caseET.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_DELIVERY_OU)
                    holder.outerET.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.IS_SIH_VALIDATION_ON_DELIVERY)
                    holder.sih.setVisibility(View.GONE);

                if (mSelectedItem.equals(getResources().getString(R.string.fullfilled)))
                    holder.deliveryCB.setVisibility(View.GONE);

                holder.deliveryCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        holder.productBO.setCheked(isChecked);
                        if (isChecked) {

                            holder.pieceET.setEnabled(false);
                            holder.caseET.setEnabled(false);
                            holder.outerET.setEnabled(false);
                            holder.pieceET.clearFocus();
                            holder.caseET.clearFocus();
                            holder.outerET.clearFocus();
                            holder.productBO.setLocalOrderPieceqty(holder.productBO.getOrderedPcsQty());
                            holder.productBO.setLocalOrderCaseqty(holder.productBO.getOrderedCaseQty());
                            holder.productBO.setLocalOrderOuterQty(holder.productBO.getOrderedOuterQty());

                            holder.pieceET.setText(holder.productBO.getInit_pieceqty() + "");
                            holder.caseET.setText(holder.productBO.getInit_caseqty() + "");
                            holder.outerET.setText(holder.productBO.getInit_OuterQty() + "");
                            QUANTITY = null;
                        } else {
                            holder.pieceET.setEnabled(true);
                            holder.caseET.setEnabled(true);
                            holder.outerET.setEnabled(true);
                        }

                    }
                });
                holder.pieceET.addTextChangedListener(new TextWatcher() {
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
                            int enteredQty = SDUtil
                                    .convertToInt(qty);
                            int totalQty = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
                            int totalEnterdQty = enteredQty + (holder.productBO.getInit_caseqty() * holder.productBO.getCaseSize()) + (holder.productBO.getInit_OuterQty() * holder.productBO.getOutersize());
                            if (totalEnterdQty <= totalQty) {
                                holder.productBO.setLocalOrderPieceqty(enteredQty);
                            } else {
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.productBO.setLocalOrderPieceqty(SDUtil
                                        .convertToInt(qty));

                                holder.pieceET.setText(qty);
                                Toast.makeText(DeliveryManagementDetail.this, "Qty exceed ", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                });
                holder.caseET.addTextChangedListener(new TextWatcher() {
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
                            int enteredQty = SDUtil
                                    .convertToInt(qty);
                            int totalQty = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
                            int totalEnterdQty = holder.productBO.getInit_pieceqty() + (enteredQty * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
                            if (totalEnterdQty <= totalQty) {
                                holder.productBO.setLocalOrderCaseqty(enteredQty);
                            } else {
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.productBO.setLocalOrderCaseqty(SDUtil
                                        .convertToInt(qty));

                                holder.caseET.setText(qty);
                                Toast.makeText(DeliveryManagementDetail.this, "Qty exceed ", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                });
                holder.outerET.addTextChangedListener(new TextWatcher() {
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
                            int enteredQty = SDUtil
                                    .convertToInt(qty);
                            int totalQty = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
                            int totalEnterdQty = holder.productBO.getInit_pieceqty() + (holder.productBO.getInit_caseqty() * holder.productBO.getCaseSize()) + (enteredQty * holder.productBO.getOutersize());
                            if (totalEnterdQty <= totalQty) {
                                holder.productBO.setLocalOrderOuterQty(enteredQty);
                            } else {
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.productBO.setLocalOrderOuterQty(SDUtil
                                        .convertToInt(qty));

                                holder.outerET.setText(qty);
                                Toast.makeText(DeliveryManagementDetail.this, "Qty exceed ", Toast.LENGTH_SHORT).show();
                            }
                        }


                    }
                });

                holder.pieceET.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.pieceET;
                        QUANTITY.setTag(holder.productBO);
                        int inType = holder.pieceET.getInputType();
                        holder.pieceET.setInputType(InputType.TYPE_NULL);
                        holder.pieceET.onTouchEvent(event);
                        holder.pieceET.setInputType(inType);
                        holder.pieceET.selectAll();
                        holder.pieceET.requestFocus();
//                    inputManager.hideSoftInputFromWindow(
//                            mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.caseET.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.caseET;
                        QUANTITY.setTag(holder.productBO);
                        int inType = holder.caseET.getInputType();
                        holder.caseET.setInputType(InputType.TYPE_NULL);
                        holder.caseET.onTouchEvent(event);
                        holder.caseET.setInputType(inType);
                        holder.caseET.selectAll();
                        holder.caseET.requestFocus();
                        return true;
                    }
                });
                holder.outerET.setOnTouchListener(new View.OnTouchListener() {
                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        QUANTITY = holder.outerET;
                        QUANTITY.setTag(holder.productBO);
                        int inType = holder.outerET.getInputType();
                        holder.outerET.setInputType(InputType.TYPE_NULL);
                        holder.outerET.onTouchEvent(event);
                        holder.outerET.setInputType(inType);
                        holder.outerET.selectAll();
                        holder.outerET.requestFocus();
                        return false;
                    }
                });

                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }
            holder.productBO = mProductList.get(position);
            holder.productNameTV.setText(holder.productBO.getProductShortName());

            if (holder.productBO.getBatchNo() != null) {
                holder.batchNoTV.setVisibility(View.VISIBLE);
                holder.batchNoTV.setText(holder.productBO.getBatchNo() + "");
            } else {
                holder.batchNoTV.setVisibility(View.GONE);
            }

            holder.pieceET.setText(holder.productBO.getInit_pieceqty() + "");
            holder.caseET.setText(holder.productBO.getInit_caseqty() + "");
            holder.outerET.setText(holder.productBO.getInit_OuterQty() + "");
            holder.deliveryCB.setChecked(holder.productBO.isCheked());

            holder.sih.setText(holder.productBO.getSIH() + "");//bmodel.productHelper.getProductMasterBOById(holder.productBO.getProductID()).getSIH() + "");
            if (holder.productBO.isCheked()) {

                holder.pieceET.setEnabled(false);
                holder.caseET.setEnabled(false);
                holder.outerET.setEnabled(false);
            } else {
                if (mSelectedItem.equals(getResources().getString(R.string.partially_fullfilled))) {
                    holder.pieceET.setEnabled(true);
                    holder.caseET.setEnabled(true);
                    holder.outerET.setEnabled(true);
                } else {
                    holder.pieceET.setEnabled(false);
                    holder.caseET.setEnabled(false);
                    holder.outerET.setEnabled(false);
                    holder.pieceET.clearFocus();
                    holder.caseET.clearFocus();
                    holder.outerET.clearFocus();
                    holder.productBO.setLocalOrderPieceqty(holder.productBO.getOrderedPcsQty());
                    holder.productBO.setLocalOrderCaseqty(holder.productBO.getOrderedCaseQty());
                    holder.productBO.setLocalOrderOuterQty(holder.productBO.getOrderedOuterQty());

                    holder.pieceET.setText(holder.productBO.getInit_pieceqty() + "");
                    holder.caseET.setText(holder.productBO.getInit_caseqty() + "");
                    holder.outerET.setText(holder.productBO.getInit_OuterQty() + "");
                    QUANTITY = null;
                }
            }

            if (mOutOfStockProducts != null && mOutOfStockProducts.contains(holder.productBO.getProductID()))
                holder.productNameTV.setTextColor(ContextCompat.getColor(DeliveryManagementDetail.this, R.color.RED));
            else
                holder.productNameTV.setTextColor(ContextCompat.getColor(DeliveryManagementDetail.this, R.color.Black));

            if (position % 2 == 0) {
                convertView.setBackgroundColor(getResources().getColor(R.color.list_even_item_bg));
            } else {
                convertView.setBackgroundColor(getResources().getColor(R.color.list_odd_item_bg));
            }

            return convertView;
        }
    }

    class Holder {
        ProductMasterBO productBO;
        TextView productNameTV;
        TextView batchNoTV;
        TextView sih;
        EditText pieceET, caseET, outerET;
        CheckBox deliveryCB;

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_deliverydetails, menu);

        //if (bmodel.configurationMasterHelper.SHOW_SIGNATURE_SCREEN)
        menu.findItem(R.id.menu_signature).setVisible(true);

        menu.findItem(R.id.menu_save).setVisible(false);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {

            if (bmodel.checkForNFilesInFolder(HomeScreenFragment.photoPath, 1, signName))
                bmodel.deleteFiles(HomeScreenFragment.photoPath,
                        signName);

            ArrayList<InvoiceHeaderBO> invoiceList = bmodel.deliveryManagementHelper.getInvoiceList();
            if (invoiceList.size() == 1) {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                if (getIntent().getStringExtra("From") == null) {
                    Intent i = new Intent(DeliveryManagementDetail.this, HomeScreenTwo.class);
                    startActivity(i);
                }
                finish();
            } else {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

                Intent i = new Intent(DeliveryManagementDetail.this, DeliveryManagement.class);
                i.putExtra("screentitle", getIntent().getStringExtra("screentitle"));
                if (getIntent().getStringExtra("From") != null) {
                    i.putExtra("From", getIntent().getStringExtra("From"));
                }
                startActivity(i);
                finish();
            }
        } else if (i1 == R.id.menu_save) {
            if (isDeliveryNotGreaterOrder()) {

                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION_ON_DELIVERY && isStockAvailable().size() > 0) {
                    mProductDetailsLV.invalidateViews();
                    Toast.makeText(DeliveryManagementDetail.this, getResources().getString(R.string.stock_not_available), Toast.LENGTH_SHORT).show();
                    return false;
                } else {
                    android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                    CustomFragment dialogFragment = new CustomFragment(this);
                    /*Bundle bundle = new Bundle();
                    bundle.putString("title", "Delivery Management Dialog");
                    bundle.putString("textviewTitle", getResources().getString(R.string.do_u_want_to_save_delivery_management));
                    dialogFragment.setArguments(bundle);*/
                    dialogFragment.show();
                }

            } else {
                Toast.makeText(DeliveryManagementDetail.this, "Qty exceed ", Toast.LENGTH_SHORT).show();
            }
        } else if (i1 == R.id.menu_signature) {
            if (bmodel.checkForNFilesInFolder(HomeScreenFragment.photoPath, 1, signName)) {
                final CommonDialog commonDialog = new CommonDialog(getApplicationContext(),
                        this,
                        "",
                        getResources().getString(
                                R.string.sign_captured_already),
                        false,
                        getResources().getString(R.string.yes),
                        getResources().getString(R.string.no),
                        false,
                        new CommonDialog.positiveOnClickListener() {
                            @Override
                            public void onPositiveButtonClick() {
                                bmodel.deleteFiles(HomeScreenFragment.photoPath,
                                        signName);
                                Intent i = new Intent(DeliveryManagementDetail.this,
                                        CaptureSignatureActivity.class);
                                i.putExtra("fromModule", "DELIVERY");
                                if (getIntent().getStringExtra("From") != null) {
                                    i.putExtra("From", getIntent().getStringExtra("From"));
                                }
                                startActivityForResult(i, REQUEST_SIGNAATURE_CAPTURE);

                            }
                        }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {

                    }
                });
                commonDialog.show();
                commonDialog.setCancelable(false);

            } else {

                Intent i = new Intent(DeliveryManagementDetail.this,
                        CaptureSignatureActivity.class);
                i.putExtra("fromModule", "DELIVERY");
                if (getIntent().getStringExtra("From") != null) {
                    i.putExtra("From", getIntent().getStringExtra("From"));
                }
                startActivityForResult(i, REQUEST_SIGNAATURE_CAPTURE);
                bmodel.configurationMasterHelper.setSignatureTitle("Signature");
            }
            return true;
        }


        return false;
    }

    private void sendEmail() {
        // Email body content hardcode for Laos.
        String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName + "/" + signName;
        String cid = System.currentTimeMillis() + "";


        String body =

                "<head><style>table{border: 1px solid #dddddd;} th{border: 1px solid black;} div{border: 1px solid black; padding:5px} p.one{text-align:center;}</style></head>" +

                        "<p class=one><font size=5>Lao Brewery co. ltd</font></p>" +
                        "<br>" +
                        "<div>" +

                        "<p style=text-align:center;><font size=3>" + bmodel.getRetailerMasterBO().getRetailerName() + "</font></p>";
        if (bmodel.getRetailerMasterBO().getAddress1() != null && !bmodel.getRetailerMasterBO().getAddress1().equals(""))
            body += "<p style=text-align:center;>" + bmodel.getRetailerMasterBO().getAddress1() + "</p>";
        if (bmodel.getRetailerMasterBO().getAddress2() != null && !bmodel.getRetailerMasterBO().getAddress2().equals(""))
            body += "<p style=text-align:center;>" + bmodel.getRetailerMasterBO().getAddress2() + "</p>";
        if (bmodel.getRetailerMasterBO().getAddress3() != null && !bmodel.getRetailerMasterBO().getAddress3().equals(""))
            body += "<p style=text-align:center;>" + bmodel.getRetailerMasterBO().getAddress3() + "</p>";

        body += "<br>" +
                "<table style=width:100% " +
                "<tr style=font-weight:bold>" +
                "<th align=center width=50>Sno</th>" +
                "<th align=center >Prod.Name</th>" +
                "<th align=center width=70>Trade Offer Qty</th>" +
                "<th align=center width=70>Delivered Qty</th>" +
                "</tr>";

        int sno = 0;
        for (ProductMasterBO bo : bmodel.deliveryManagementHelper.getmInvoiceDetailsList()) {

            int deliverd_qty = bo.getInit_pieceqty() + (bo.getInit_caseqty() * bo.getCaseSize()) + (bo.getInit_OuterQty() * bo.getOutersize());
            int totalQty = bo.getOrderedPcsQty() + (bo.getOrderedCaseQty() * bo.getCaseSize()) + (bo.getOrderedOuterQty() * bo.getOutersize());
            sno += 1;

            body += "<tr>" +
                    "<td align=center width=50>" + sno + "</td>" +
                    "<td>" + bo.getProductShortName() + "</td>" +
                    "<td align=center width=70>" + totalQty + "</td>" +
                    "<td align=center width=70>" + deliverd_qty + "</td>" +
                    "</tr>";
        }

        body += "</table>" +
                "</div>" +
                "<br>" +
                "<br>" +

                "<p style=text-align:left;>Delivered By" +
                "    <span style=float:right;>Signature</span>" +
                "    </p>" +

                "<p style=text-align:left;>" + bmodel.userMasterHelper.getUserMasterBO().getUserName() +
                "    <span style=float:right;><img src=cid:" + cid + " width=100 height=50></span>" +
                "    </p>";


        SendMail sendMail = new SendMail(this, bmodel.getRetailerMasterBO().getEmail(), "Trade Offer Delivery from LBC", body, cid, path);
        sendMail.execute();

    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {

        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt((String) QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");

            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();

            }

        }
    }

    private boolean isDeliveryNotGreaterOrder() {
        for (ProductMasterBO productMasterBO : mProductList) {
            int totalQty = productMasterBO.getOrderedPcsQty() + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()) + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());
            int orderedQty = productMasterBO.getInit_pieceqty() + (productMasterBO.getInit_caseqty() * productMasterBO.getCaseSize()) + (productMasterBO.getInit_OuterQty() * productMasterBO.getOutersize());

            if (orderedQty > totalQty) {
                return false;
            }
        }
        return true;
    }

    ArrayList<String> mOutOfStockProducts;

    private ArrayList<String> isStockAvailable() {
        if (mOutOfStockProducts == null)
            mOutOfStockProducts = new ArrayList<>();
        else
            mOutOfStockProducts.clear();

        for (ProductMasterBO productMasterBO : mProductList) {
            int orderedQty = productMasterBO.getInit_pieceqty() + (productMasterBO.getInit_caseqty() * productMasterBO.getCaseSize()) + (productMasterBO.getInit_OuterQty() * productMasterBO.getOutersize());

            if (orderedQty > bmodel.productHelper.getProductMasterBOById(productMasterBO.getProductID()).getSIH()) {


                mOutOfStockProducts.add(productMasterBO.getProductID());
            }
        }
        return mOutOfStockProducts;
    }

    private class SaveDelivery extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            build = new AlertDialog.Builder(DeliveryManagementDetail.this);

            customProgressDialog(build, getResources().getString(R.string.saving));
            alertDialog = build.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            bmodel.deliveryManagementHelper.saveDeliveryManagement(mInvoiceNo, mSelectedItem, signName, signPath, contactName, contactNo);
            bmodel.saveModuleCompletion("MENU_DELIVERY_MGMT");

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            alertDialog.dismiss();

            bmodel.deliveryManagementHelper.downloadEmailAccountCredentials();
            if (bmodel.configurationMasterHelper.IS_SEND_EMAIL_STATEMENT_FOR_DELIVERY
                    && bmodel.isOnline()
                    && bmodel.getRetailerMasterBO().getEmail() != null
                    && !bmodel.getRetailerMasterBO().getEmail().equals("")
                    && bmodel.deliveryManagementHelper.getUserName() != null && !bmodel.deliveryManagementHelper.getUserName().equals("")
                    && bmodel.deliveryManagementHelper.getUserPassword() != null && !bmodel.deliveryManagementHelper.getUserPassword().equals("")) {
                sendEmail();

            } else {

                if (mSelectedItem.equals(getResources().getString(R.string.partially_fullfilled)) && bmodel.configurationMasterHelper.IS_DELIVERY_PRINT) {

                    for (ProductMasterBO product : mProductList) {
                        product.setOrderedPcsQty(product.getInit_pieceqty());
                        product.setOrderedCaseQty(product.getInit_caseqty());
                        product.setOrderedOuterQty(product.getInit_OuterQty());
                    }

                    Vector<ProductMasterBO> productList = new Vector<ProductMasterBO>(mProductList);
                    //Collections.copy(productList, mProductList);
                    bmodel.mCommonPrintHelper.xmlRead("print_z320_delivery_management.xml", true, productList, null);
                    Intent i = new Intent(DeliveryManagementDetail.this, CommonPrintPreviewActivity.class);
                    i.putExtra("IsFromOrder", true);
                    i.putExtra("isHomeBtnEnable", true);
                    if (getIntent().getStringExtra("From") != null) {
                        i.putExtra("From", getIntent().getStringExtra("From"));
                    }
                    startActivity(i);
                    finish();

                } else {
                    if (bmodel.deliveryManagementHelper.getInvoiceList().size() <= 1) {
                        if (getIntent().getStringExtra("From") == null) {
                            Intent i = new Intent(DeliveryManagementDetail.this, HomeScreenTwo.class);
                            startActivity(i);
                        }
                        finish();
                    } else {
                        Intent i = new Intent(DeliveryManagementDetail.this, DeliveryManagement.class);
                        i.putExtra("screentitle", getIntent().getStringExtra("screentitle"));
                        if (getIntent().getStringExtra("From") != null) {
                            i.putExtra("From", getIntent().getStringExtra("From"));
                        }
                        startActivity(i);
                        finish();
                    }
                }
            }
        }
    }

    public class CustomFragment extends Dialog {
        private String mTitle = "";
        private String mTextviewTitle = "";


        private TextView mTitleTV;
        private Button mOkBtn, mDismisBtn;
        private ListView mCountLV;
        View rootView;

        private String[] mPrintCountArray;
        private Context context;

        public CustomFragment(@NonNull Context context) {
            super(context);
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.context = context;
            rootView = LayoutInflater.from(context).inflate(R.layout.custom_dialog_fragment, null);
            setContentView(rootView);
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            if (bmodel.configurationMasterHelper.MVPTheme == 0) {
                setTheme(bmodel.configurationMasterHelper.getMVPTheme());
            } else {
                setTheme(bmodel.configurationMasterHelper.MVPTheme);
            }
            if (bmodel.configurationMasterHelper.fontSize.equals("")) {
                setFontStyle(bmodel.configurationMasterHelper.getFontSize());
            } else {
                setFontStyle(bmodel.configurationMasterHelper.fontSize);
            }
        }

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            mTitle = "Delivery Management Dialog";
            mTextviewTitle = getResources().getString(R.string.do_u_want_to_save_delivery_management);


        }

        @Override
        public void onStart() {
            super.onStart();
            setTitle(mTitle);
            mTitleTV = (TextView) rootView.findViewById(R.id.title);
            if (mTitleTV != null) {
                mTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }
            mOkBtn = (Button) rootView.findViewById(R.id.btn_ok);
            mDismisBtn = (Button) rootView.findViewById(R.id.btn_dismiss);
            mCountLV = (ListView) rootView.findViewById(R.id.lv_colletion_print);
            mCountLV.setVisibility(View.GONE);
            mTitleTV.setText(mTextviewTitle);
            mPrintCountArray = bmodel.printHelper.getPrintCountArray();
            ArrayList<String> countList = new ArrayList<String>(Arrays.asList(mPrintCountArray));
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(DeliveryManagementDetail.this, android.R.layout.simple_list_item_single_choice, countList);
            if (flag == 0) {
                mCountLV.setVisibility(View.VISIBLE);
                mCountLV.setAdapter(adapter);
            }
            mCountLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mSelectedPrintCount = position;
                    builder10 = new AlertDialog.Builder(context);
                    customProgressDialog(builder10, "Printing....");
                    alertDialog = builder10.create();
                    alertDialog.show();
                    dismiss();
                }
            });

            mOkBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dismiss();
                    if (flag == -1) {
                        mTitleTV.setVisibility(View.GONE);
                        mCountLV.setVisibility(View.VISIBLE);
                        mOkBtn.setVisibility(View.GONE);
                        mDismisBtn.setVisibility(View.GONE);
                        new SaveDelivery().execute();
                    } else if (flag == 0) {
                        Toast.makeText(DeliveryManagementDetail.this, "Print count " + mSelectedPrintCount, Toast.LENGTH_SHORT).show();
                    }

                }
            });
            mDismisBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (flag == -1)
                        dismiss();
                    else {
                        ArrayList<InvoiceHeaderBO> invoiceList = bmodel.deliveryManagementHelper.getInvoiceList();
                        if (invoiceList.size() == 1) {

                            if (getIntent().getStringExtra("From") == null) {
                                Intent i = new Intent(DeliveryManagementDetail.this, HomeScreenTwo.class);
                                startActivity(i);
                            }
                            finish();
                        } else {
                            Intent i = new Intent(DeliveryManagementDetail.this, DeliveryManagement.class);
                            i.putExtra("screentitle", getIntent().getStringExtra("screentitle"));
                            if (getIntent().getStringExtra("From") != null) {
                                i.putExtra("From", getIntent().getStringExtra("From"));
                            }
                            startActivity(i);
                            finish();
                        }
                    }


                }
            });


        }
    }


    private static final int REQUEST_SIGNAATURE_CAPTURE = 100;
    String signName = "";
    String signPath = "";
    String contactName = "";
    String contactNo = "";

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Commons.print(TAG + ",onActivityResult " + resultCode);
        switch (requestCode) {
            case REQUEST_SIGNAATURE_CAPTURE:
                if (resultCode == Activity.RESULT_OK) {
                    signName = data.getStringExtra("IMAGE_NAME");
                    signPath = data.getStringExtra("SERVER_PATH");
                    if (data.getStringExtra("CONTACTNAME") != null) {
                        contactName = data.getStringExtra("CONTACTNAME");
                        contactNo = data.getStringExtra("CONTACTNO");
                    }
                }
                break;

        }
    }


    public class SendMail extends AsyncTask<Void, Void, Boolean> {

        Session session;

        Context mContext;
        private String toEmailId;
        private String subject;
        private String body;
        String photoPath;

        ProgressDialog progressDialog;
        String cid;

        public SendMail(Context ctx, String toEmailId, String subject, String message, String cid, String photoPath) {
            this.mContext = ctx;
            this.toEmailId = toEmailId;
            this.subject = subject;
            this.body = message;
            this.cid = cid;
            this.photoPath = photoPath;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.sending_email), getResources().getString(R.string.please_wait_some_time), false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            Properties props = System.getProperties();// new Properties();

            //Configuring properties for gmail
            props.put("mail.smtp.host", "smtp.gmail.com");
            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");
            //  props.put("mail.debug",true);

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(bmodel.deliveryManagementHelper.getUserName(), bmodel.deliveryManagementHelper.getUserPassword());
                        }
                    });

            try {

                Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(bmodel.deliveryManagementHelper.getUserName()));
                message.setRecipient(Message.RecipientType.TO, new InternetAddress(toEmailId));
                message.setSubject(subject);
                //  mm.setContent(message,"text/html; charset=utf-8");

                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setContent(body, "text/html");//Content(message,"text/html");

                MimeMultipart multiPart = new MimeMultipart();
                multiPart.addBodyPart(bodyPart);

                boolean isImageAvailable = false;
                if (bmodel.checkForNFilesInFolder(HomeScreenFragment.photoPath, 1, signName))
                    isImageAvailable = true;

                if (isImageAvailable) {
                    MimeBodyPart attachmentPart = new MimeBodyPart();
                    attachmentPart.attachFile(photoPath);
                    attachmentPart.setFileName("attachment");
                    attachmentPart.setContentID("<" + cid + ">");
                    attachmentPart.setDisposition(MimeBodyPart.ATTACHMENT);
                    multiPart.addBodyPart(attachmentPart);
                }


                message.setContent(multiPart);

                Thread.currentThread().setContextClassLoader(DeliveryManagementDetail.class.getClassLoader());

                MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

                Transport.send(message);
            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSent) {
            super.onPostExecute(isSent);

            progressDialog.dismiss();

            if (isSent)
                Toast.makeText(mContext, getResources().getString(R.string.email_sent), Toast.LENGTH_LONG).show();
            else
                Toast.makeText(mContext, getResources().getString(R.string.error_in_sending_email), Toast.LENGTH_LONG).show();

            if (mSelectedItem.equals(getResources().getString(R.string.partially_fullfilled))) {

                for (ProductMasterBO product : mProductList) {
                    product.setOrderedPcsQty(product.getInit_pieceqty());
                    product.setOrderedCaseQty(product.getInit_caseqty());
                    product.setOrderedOuterQty(product.getInit_OuterQty());
                }

                Vector<ProductMasterBO> productList = new Vector<ProductMasterBO>(mProductList);
                //Collections.copy(productList, mProductList);
                bmodel.mCommonPrintHelper.xmlRead("print_z320_delivery_management.xml", true, productList, null);
                Intent i = new Intent(DeliveryManagementDetail.this, CommonPrintPreviewActivity.class);
                i.putExtra("IsFromOrder", true);
                i.putExtra("isHomeBtnEnable", true);
                if (getIntent().getStringExtra("From") != null) {
                    i.putExtra("From", getIntent().getStringExtra("From"));
                }
                startActivity(i);
                finish();

            } else {
                if (bmodel.deliveryManagementHelper.getInvoiceList().size() <= 1) {
                    if (getIntent().getStringExtra("From") == null) {
                        Intent i = new Intent(DeliveryManagementDetail.this, HomeScreenTwo.class);
                        startActivity(i);
                    }
                    finish();
                } else {
                    Intent i = new Intent(DeliveryManagementDetail.this, DeliveryManagement.class);
                    i.putExtra("screentitle", getIntent().getStringExtra("screentitle"));
                    if (getIntent().getStringExtra("From") != null) {
                        i.putExtra("From", getIntent().getStringExtra("From"));
                    }
                    startActivity(i);
                    finish();
                }
            }

        }
    }

}
