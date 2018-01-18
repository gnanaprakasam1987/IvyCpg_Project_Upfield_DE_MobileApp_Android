package com.ivy.sd.png.view.reports;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.TaxTempBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.BixolonIIPrint;
import com.ivy.sd.png.view.BixolonIPrint;
import com.ivy.sd.png.view.InvoicePrintZebraNew;
import com.ivy.sd.print.BtService;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.GhanaPrintPreviewActivity;
import com.ivy.sd.print.PrintPreviewScreen;
import com.ivy.sd.print.PrintPreviewScreenDiageo;
import com.ivy.sd.print.SettingsHelper;
import com.tremol.zfplibj.ZFPLib;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;
import java.util.Vector;

public class InvoiceReportDetail extends IvyBaseActivityNoActionBar implements
        OnClickListener {
    private Button back;
    private ListView lvwplist;
    private ExpandableListView elv;
    private TextView outletname, txttotal, productName, tvtotalLines, tv_lbl_total_lines, TextView51;
    private BusinessModel bmodel;
    public Vector<ProductMasterBO> mProducts = new Vector<ProductMasterBO>();
    public ArrayList<ProductMasterBO> mProductsForAdapter = new ArrayList<ProductMasterBO>();
    private ArrayList<SchemeProductBO> schemeProductList = new ArrayList<SchemeProductBO>();
    private double tot = 0;

    double vatAmount = 0.0;
    private ArrayList<TaxTempBO> mTax;
    private static final String TAG = "InvoiceReportDetail";
    private static final boolean D = true;
    // Message types sent from the BluetoothChatService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothChatService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    private String mConnectedDeviceName = null;
    private ArrayAdapter<String> mAADisplay;
    private StringBuffer mOutStringBuffer; // String buffer for outgoing
    // messages

    public static Resources mRes = null;
    SharedPreferences msettings;
    private BluetoothAdapter mBluetoothAdapter = null;
    private BtService mChatService = null;
    private String minvoiceid;

    // Unipal printer Instance
    private Connection zebraPrinterConnection;
    ZebraPrinter printer = null;
    private static final String ZEBRA_3INCH = "3";
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private int mSelectedPrintCount = 0;
    private Toolbar toolbar;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // requestWindowFeature(Window.FEATURE_NO_TITLE);

        // getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        // WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_invoice_report_detail);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Bundle extras = getIntent().getExtras();
        if (extras != null)
            if (extras.containsKey("TotalAmount")) {
                tot = extras.getDouble("TotalAmount");
            }
        if (extras.containsKey("lineinvoice")) {
            minvoiceid = extras.getString("lineinvoice");
        }
        try {
            toolbar = (Toolbar) findViewById(R.id.toolbar);
            back = (Button) findViewById(R.id.btnPersBack);
            back.setOnClickListener(this);

            outletname = (TextView) findViewById(R.id.BtnBrandPrev);
            productName = (TextView) findViewById(R.id.productName);
            txttotal = (TextView) findViewById(R.id.txttotal);
            tvtotalLines = (TextView) findViewById(R.id.txttotalqty);
            tv_lbl_total_lines = (TextView) findViewById(R.id.TextView52);
            TextView51 = (TextView) findViewById(R.id.TextView51);
            TextView51.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            lvwplist = (ListView) findViewById(R.id.lvwplistorddet);
            lvwplist.setCacheColorHint(0);
            elv = (ExpandableListView) findViewById(R.id.elv);

            setSupportActionBar(toolbar);
            // Set title to toolbar
            if (getSupportActionBar() != null)
                getSupportActionBar().setTitle(getResources().getString(R.string.invoice_report_details));
            getSupportActionBar().setIcon(null);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            // getSupportActionBar().setDisplayUseLogoEnabled(false);

            /** hide pcs,case,outer **/
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                findViewById(R.id.cqty).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.cqty).getTag()) != null)
                        ((TextView) findViewById(R.id.cqty))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.cqty).getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                findViewById(R.id.outid).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.outid).getTag()) != null)
                        ((TextView) findViewById(R.id.outid))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.outid).getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.outercqty).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.outercqty).getTag()) != null)
                        ((TextView) findViewById(R.id.outercqty))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.outercqty)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
                findViewById(R.id.weighttitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.weighttitle).getTag()) != null)
                        ((TextView) findViewById(R.id.weighttitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.weighttitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }


            mProducts = bmodel.productHelper.getProductMaster();

            msettings = getSharedPreferences(bmodel.PREFS_NAME, MODE_PRIVATE);
            mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            mRes = getResources();
            /*if (mBluetoothAdapter == null)
            {
				//Toast.makeText(this, "Bluetooth not enabled ",Toast.LENGTH_LONG).show();					
				//!!!!!
				finish();
				return;
			}*/
            double totalLines = 0;
            int totalAllQty = 0;
            if (bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {
                // All products not need to load.only invoice products loaded from
                // sqlite and stored in object.Because invoice print file saved in sdcard
                // we can show other details using the text file
                mProductsForAdapter=bmodel.reportHelper.getReportDetails(minvoiceid);
            }else {
                for (ProductMasterBO productBO : mProducts) {
                    // if (productBO.isCheked()
                    // && (productBO.getCaseQty() > 0 || productBO.getPieceQty() >
                    // 0)) {
                    if ((productBO.getOrderedPcsQty() > 0
                            || productBO.getOrderedCaseQty() > 0 || productBO
                            .getOrderedOuterQty() > 0)) {
                        totalLines = totalLines + 1;

                        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && productBO.getBatchwiseProductCount() > 0) {
                            ArrayList<ProductMasterBO> batchList = bmodel.batchAllocationHelper.getBatchlistByProductID().get(productBO.getProductID());
                            if (batchList != null) {
                                for (ProductMasterBO batchproductBo : batchList) {
                                    int totalQty = batchproductBo.getOrderedPcsQty() + (batchproductBo.getOrderedCaseQty() * productBO.getCaseSize())
                                            + (batchproductBo.getOrderedOuterQty() * productBO.getOutersize());
                                    totalAllQty = totalAllQty + totalQty;
                                    if (totalQty > 0) {
                                        batchproductBo.setProductShortName(productBO.getProductShortName());
                                        batchproductBo.setTotalamount(batchproductBo.getDiscount_order_value());
                                        mProductsForAdapter.add(batchproductBo);
                                    }
                                }
                            }

                        } else {
                            productBO.setBatchNo("");
                            int totalQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();
                            totalAllQty = totalAllQty + totalQty;
                            mProductsForAdapter.add(productBO);
                        }

                    }

                }
            }


            if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {
    schemeProductList = bmodel.reportHelper.getSchemeProductDetails(minvoiceid);
}else {
    //load accumulation scheme free products
    schemeProductList = bmodel.schemeDetailsMasterHelper.downLoadAccumulationSchemeDetailReport(minvoiceid, true);
}
            if (schemeProductList != null &&
                    mProductsForAdapter != null) {
                if (mProductsForAdapter.get(mProductsForAdapter.size() - 1).getSchemeProducts() != null)
                    mProductsForAdapter.get(mProductsForAdapter.size() - 1).getSchemeProducts().addAll(schemeProductList);
                else
                    mProductsForAdapter.get(mProductsForAdapter.size() - 1).setSchemeProducts(schemeProductList);
            }


            txttotal.setText(bmodel.formatValue(tot) + "");

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_LINES) {
                if (bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_IN_ORDER_REPORT) {

                    if(mProductsForAdapter!=null) {
                        for (ProductMasterBO productMasterBO : mProductsForAdapter) {
                            totalAllQty = totalAllQty + productMasterBO.getTotalQty();
                        }
                    }
                    tvtotalLines.setText(totalAllQty + "");
                    tv_lbl_total_lines.setText(getResources().getString(R.string.tot_qty));
                } else {
                    totalLines =mProductsForAdapter.size();
                    tvtotalLines.setText(totalLines + "");
                    tv_lbl_total_lines.setText(getResources().getString(R.string.tot_line));
                }

            } else {
                tv_lbl_total_lines.setVisibility(View.GONE);
                tvtotalLines.setVisibility(View.GONE);
            }

            // Show alert if error loading data.
            if (mProductsForAdapter == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.unable_to_load_data),
                        0);
                return;
            }
            // Show alert if no order exist.
            if (mProductsForAdapter.size() == 0) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_orders_available),
                        0);
                return;
            }

            // Load listview.
            elv.setAdapter(new MyAdapter());
            int orderedProductCount = mProductsForAdapter.size();
            for (int i = 0; i < orderedProductCount; i++) {
                ((ExpandableListView) elv).expandGroup(i);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }



    @Override
    public void onClick(View comp) {
        // TODO Auto-generated method stub
        Button btn = (Button) comp;
        if (btn == back) {
            super.onDestroy();
            finish();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_invoice_report, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButtonClick();

        } else if (i == R.id.menu_print) {
            bmodel.invoiceNumber = minvoiceid;
            bmodel.getPrintCount();

            Intent intent = new Intent();

            if (bmodel.configurationMasterHelper.SHOW_BIXOLONI)
                intent.setClass(InvoiceReportDetail.this, BixolonIPrint.class);
            else if (bmodel.configurationMasterHelper.SHOW_BIXOLONII)
                intent.setClass(InvoiceReportDetail.this, BixolonIIPrint.class);
            else if (bmodel.configurationMasterHelper.SHOW_ZEBRA)
                intent.setClass(InvoiceReportDetail.this,
                        InvoicePrintZebraNew.class);
            else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_ATS)
                intent.setClass(InvoiceReportDetail.this,
                        PrintPreviewScreen.class);
            else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_DIAGEO) {
                intent.setClass(InvoiceReportDetail.this,
                        PrintPreviewScreenDiageo.class);
                intent.putExtra("print_count", bmodel.printHelper.getPrintCnt());
            } else if (bmodel.configurationMasterHelper.SHOW_INTERMEC_ATS)
                intent.setClass(InvoiceReportDetail.this, BtPrint4Ivy.class);
            else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_GHANA) {
                intent.setClass(InvoiceReportDetail.this,
                        GhanaPrintPreviewActivity.class);
            } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL || bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                showDialog(2);
            } else if (bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                    || bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {
                // Print file already saved.so not need to reload the object.we can get the object from print text file
                bmodel.mCommonPrintHelper.readBuilder(StandardListMasterConstants.PRINT_FILE_INVOICE+bmodel.invoiceNumber+".txt");
                intent.setClass(InvoiceReportDetail.this,
                        CommonPrintPreviewActivity.class);
                intent.putExtra("IsUpdatePrintCount", true);
                intent.putExtra("isHomeBtnEnable", true);
            } else
                intent.setClass(InvoiceReportDetail.this, BixolonIIPrint.class);

            if (!bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL && !bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                intent.putExtra("IsFromReport", true);
                startActivityForResult(intent, 0);
            }


        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    class MyAdapter extends BaseExpandableListAdapter {

        @Override
        public Object getChild(int arg0, int arg1) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition,
                                 boolean isLastChild, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_invoice_report_detail,
                        parent, false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.PRDNAME);
                holder.tvwpsname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.tvwqty = (TextView) row.findViewById(R.id.PRDPCSQTY);
                holder.tvcaseqty = (TextView) row.findViewById(R.id.PRDQTY);
                holder.tvwval = (TextView) row.findViewById(R.id.PRDVAL);
                holder.tvBatchNo=(TextView)row.findViewById(R.id.batch_no);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerCaseQty);

                row.setOnClickListener(new OnClickListener() {

                    public void onClick(View v) {
                        productName.setText(holder.productName);
                    }
                });

                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tvcaseqty.setVisibility(View.VISIBLE);

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tvwqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.VISIBLE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            SchemeProductBO productBO = mProductsForAdapter.get(groupPosition)
                    .getSchemeProducts().get(childPosition);

            holder.tvwpsname.setText(productBO.getProductName());

            holder.productName = productBO.getProductFullName();
            if(productBO.getBatchId()!=null&&!productBO.getBatchId().equals("null"))
            holder.tvBatchNo.setText(productBO.getBatchId());

            if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {
                if(productBO.getUomDescription().equals("CASE")){
                    holder.tvcaseqty.setText(productBO.getQuantitySelected() + "");
                    holder.tvwqty.setText(0 + "");
                }else if(productBO.getUomDescription().equals("OUTER")){
                    holder.outerQty.setText(productBO.getQuantitySelected()+"");
                    holder.tvwqty.setText(0 + "");
                    holder.tvcaseqty.setText(0 + "");
                }else{
                    holder.tvwqty.setText(productBO.getQuantitySelected() + "");
                    holder.tvcaseqty.setText(0 + "");
                    holder.outerQty.setText(0 + "");
                }
            }else{

            holder.productBO = bmodel.productHelper
                    .getProductMasterBOById(productBO.getProductId());
            if (holder.productBO != null) {
                if (holder.productBO.getCaseUomId() == productBO.getUomID()
                        && holder.productBO.getCaseUomId() != 0) {
                    // case wise free quantity update

                    holder.tvcaseqty.setText(productBO.getQuantitySelected() + "");
                    holder.tvwqty.setText(0 + "");

                } else if (holder.productBO.getOuUomid() == productBO
                        .getUomID() && holder.productBO.getOuUomid() != 0) {
                    // outer wise free quantity update
                    holder.outerQty.setText(productBO.getQuantitySelected()+"");
                    holder.tvwqty.setText(0 + "");
                    holder.tvcaseqty.setText(0 + "");
                } else {
                    holder.tvwqty.setText(productBO.getQuantitySelected() + "");
                    holder.tvcaseqty.setText(0 + "");
                    holder.outerQty.setText(0 + "");
                }
            }
            }

            holder.tvwval.setText("0");
            return row;
        }

        @Override
        public int getChildrenCount(int groupPosition) {


                if (mProductsForAdapter.get(groupPosition).getSchemeProducts() != null) {
                    return mProductsForAdapter.get(groupPosition)
                            .getSchemeProducts().size();

            }

            return 0;
        }

        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public int getGroupCount() {
            if (mProductsForAdapter == null)
                return 0;

            return mProductsForAdapter.size();
        }

        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                                 View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_invoice_report_detail,
                        parent, false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.PRDNAME);
                holder.tvwpsname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.tvBatchNo = (TextView) row.findViewById(R.id.batch_no);
                holder.tvwqty = (TextView) row.findViewById(R.id.PRDPCSQTY);
                holder.tvcaseqty = (TextView) row.findViewById(R.id.PRDQTY);
                holder.tvwval = (TextView) row.findViewById(R.id.PRDVAL);
                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerCaseQty);
                holder.tvWeight = (TextView) row.findViewById(R.id.prdweight);
                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.productName);
                    }
                });

                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.tvcaseqty.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.tvwqty.setVisibility(View.VISIBLE);

                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                    holder.tvWeight.setVisibility(View.VISIBLE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productBO = mProductsForAdapter.get(groupPosition);
            if (!bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                if(holder.productBO.getBatchNo()!=null&&!holder.productBO.getBatchNo().equals("null"))
                    holder.tvBatchNo.setText("12345" + holder.productBO.getBatchNo() + " , ");
            }
            holder.tvBatchNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvwpsname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.tvwpsname.setText(holder.productBO.getProductShortName());
            holder.productName = holder.productBO.getProductName();
            holder.tvwqty.setText(holder.productBO.getOrderedPcsQty() + "");
            holder.tvcaseqty.setText(holder.productBO.getOrderedCaseQty() + "");
            holder.outerQty.setText(holder.productBO.getOrderedOuterQty() + "");
            int totalQty = holder.productBO.getOrderedPcsQty() + (holder.productBO.getOrderedCaseQty() * holder.productBO.getCaseSize()) + (holder.productBO.getOrderedOuterQty() * holder.productBO.getOutersize());
            holder.tvWeight.setText(" WGT : " + totalQty * holder.productBO.getWeight() + "");
            holder.tvWeight.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            /**
             * This line wise total may be wrong is amount discount appied via
             * scheme
             **/
            holder.tvwval.setText(bmodel.formatValue(holder.productBO
                    .getTotalamount()) + "");

            return row;
        }

        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return false;
        }

    }

    class ViewHolder {
        private ProductMasterBO productBO;
        String ref;// product id
        String productName;
        TextView tvwpsname;
        TextView tvBatchNo;
        TextView tvwval, tvwqty, tvcaseqty, outerQty;
        TextView tvWeight;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == BtService.STATE_CONNECTED) {
                Toast.makeText(getApplicationContext(), "Connected",
                        Toast.LENGTH_SHORT).show();
                new Checkandprint().execute();

            }
            if (msg.what == BtService.STATE_CONNECTING) {
                Toast.makeText(getApplicationContext(), "Connecting",
                        Toast.LENGTH_SHORT).show();
            }

            if (msg.what == BtService.STATE_LISTEN)
                if (msg.what == BtService.STATE_NONE) {
                    Toast.makeText(getApplicationContext(), "None",
                            Toast.LENGTH_SHORT).show();

                }
            if (msg.what == MESSAGE_WRITE) {
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                mAADisplay.add("Me:  " + writeMessage);

            }
            if (msg.what == MESSAGE_READ) {
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                mAADisplay.add(mConnectedDeviceName + ":  " + readMessage);

            }
            if (msg.what == MESSAGE_DEVICE_NAME) {
                mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Device Name " + mConnectedDeviceName,
                        Toast.LENGTH_SHORT).show();

            }
            if (msg.what == MESSAGE_TOAST) {
                Toast.makeText(getApplicationContext(),
                        msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                        .show();
                bmodel.productHelper.clearOrderTableChecked();

                finish();
            }
        }

    };


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQUEST_CONNECT_DEVICE == requestCode) {// When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) {
                String address = msettings.getString("MAC", "");
                //String address="00:12:6F:23:47:C8";
//			String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                mChatService.connect(device);
            }
        }
        if (REQUEST_ENABLE_BT == requestCode) {    // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK) {
            } else {
                Commons.print(TAG + ",BT not enabled");
                Toast.makeText(this, "Blue tooth not enable", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (requestCode == 0) {
            if (resultCode == RESULT_OK) {

            }
            if (resultCode == RESULT_CANCELED) {
                finish();
            }
        }
    }// onActivityResult

    private boolean checkBTConn() {
        if (mChatService.getState() != BtService.STATE_CONNECTED)

        {
            Toast.makeText(this, "Not connected", Toast.LENGTH_SHORT).show();

            return false;
        }
        return true;
    }

    @Override
    protected Dialog onCreateDialog(int id) {


        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(InvoiceReportDetail.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.are_you_sure_you_want_to_print))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (mBluetoothAdapter == null) {
                                            Toast.makeText(getApplicationContext(), "Bluetooth not enabled ", Toast.LENGTH_LONG).show();
                                            //!!!!!
                                            finish();
                                            return;
                                        }
                                        bmodel.schemeDetailsMasterHelper.loadSchemeReport(minvoiceid, true);
                                        Checkbluetoothenable();


                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
            case 2:
                AlertDialog.Builder builder9 = new AlertDialog.Builder(InvoiceReportDetail.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle("Do you want to Print?")
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        if (bmodel.configurationMasterHelper.printCount > 0) {
                                            mSelectedPrintCount = bmodel.configurationMasterHelper.printCount;
                                            showDialog(3);
                                        } else {
                                            new Thread(new Runnable() {
                                                public void run() {


                                                    Looper.prepare();
                                                    doConnection(ZEBRA_3INCH);
                                                    Looper.loop();
                                                    Looper.myLooper().quit();
                                                }

                                            }).start();

//
                                            build = new AlertDialog.Builder(InvoiceReportDetail.this);
                                            customProgressDialog(build, "Printing....");
                                            alertDialog = build.create();
                                            alertDialog.show();
                                        }

                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder9);
                break;
            case 3:
                AlertDialog.Builder builder11 = new AlertDialog.Builder(InvoiceReportDetail.this)
                        .setTitle("Print Count")
                        .setSingleChoiceItems(bmodel.printHelper.getPrintCountArray(), 0, null)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                                mSelectedPrintCount = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                new Thread(new Runnable() {
                                    public void run() {
                                        Looper.prepare();
                                        doConnection(ZEBRA_3INCH);
                                        Looper.loop();
                                        Looper.myLooper().quit();
                                    }
                                }).start();

                                build = new AlertDialog.Builder(InvoiceReportDetail.this);
                                customProgressDialog(build, "Printing....");
                                alertDialog = build.create();
                                alertDialog.show();
                                // Do something useful withe the position of the selected radio button
                            }
                        });
                bmodel.applyAlertDialogTheme(builder11);

                break;
        }
        return null;
    }

    public void Checkbluetoothenable() {
        try {
            if (!mBluetoothAdapter.isEnabled()) // If BT is not on, request that it
            // be enabled. setup will then be
            // called during onActivityResult
            {
                Toast.makeText(this, " Bluetooth Not Enabled", Toast.LENGTH_SHORT).show();
                finish();
            } else { // Otherwise, setup the chat session
                if (mChatService == null) {
                    mChatService = new BtService(getApplicationContext(), handler);

                    String address = msettings.getString("MAC", "");
                    //String address = "00:12:6F:23:47:C8";
                    // String address =
                    // data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                    BluetoothDevice device = mBluetoothAdapter
                            .getRemoteDevice(address);
                    mChatService.connect(device);
                    checkBTConn();

                }

            }
        } catch (Exception e) {
            checkmacadd();
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
    }

    public void checkmacadd() {
        Toast.makeText(this, "Please check mac address ", Toast.LENGTH_SHORT).show();

        finish();
    }

    public void Printdata() {
        try {
            mTax = new ArrayList<TaxTempBO>();

            ZFPLib zfp = mChatService.zfplib;
            zfp.openFiscalBon(1, "0000", false, false, false);
            boolean mgoldenstore = false;
            mTax = bmodel.taxHelper.getTaxSumProdList();
            // int siz = bmodel.productHelper.getProductMaster().size();
            for (ProductMasterBO productBO : mProducts) {

                vatAmount = 0.0;
                int taxSize = productBO.getTaxes().size();
                for (TaxTempBO taxBO : mTax) {
                    for (int ii = 0; ii < taxSize; ii++) {

                        if (taxBO.getTaxType().equals(
                                productBO.getTaxes().get(ii).getTaxType())) {

                            vatAmount = vatAmount
                                    + (productBO.getOrderedPcsQty() * ((productBO.getSrp() * productBO
                                    .getTaxes().get(ii).getTaxRate()) / 100))
                                    + (productBO.getOrderedCaseQty() * ((productBO
                                    .getCsrp() * productBO.getTaxes().get(ii)
                                    .getTaxRate()) / 100))
                                    + (productBO.getOrderedOuterQty() * ((productBO
                                    .getOsrp() * productBO.getTaxes().get(ii)
                                    .getTaxRate()) / 100));
                            break;
                        }
                    }

                }


                if ((productBO.getOrderedPcsQty() > 0
                        || productBO.getOrderedCaseQty() > 0 || productBO
                        .getOrderedOuterQty() > 0)) {

                    float pieceCount = (productBO.getOrderedCaseQty() * productBO
                            .getCaseSize())
                            + (productBO.getOrderedPcsQty())
                            + (productBO.getOrderedOuterQty() * productBO
                            .getOutersize());

                    float ptot = pieceCount * productBO.getSrp();


                    float taxdisc = (((float) vatAmount * 100) / ptot);

                    double percent = 0;
                    if (productBO.getIsscheme() == 1) {

                        percent = productBO.getMschemeper();

                    }

                    float Goldenstore = 0;
                    if (bmodel.configurationMasterHelper.SHOW_GOLD_STORE_DISCOUNT
                            && bmodel.productHelper.isGoldenStoreInCurrentandLastVisit()
                            ) {

                        Goldenstore = (float) bmodel.productHelper.applyGoldStoreLineDiscount();
                        mgoldenstore = true;
                    }

                    float discount = (float) percent + Goldenstore;
                    char taxgrp = '0';
                    if (Math.round(taxdisc) == 16) {
                        taxgrp = '1';
                    } else if (Math.round(taxdisc) == 18) {
                        taxgrp = '2';
                    } else {
                        taxgrp = '0';
                    }
                    System.out.println("taxdisc=" + Math.round(taxdisc) + "taxgrp=" + taxgrp + " percent=" + -discount + "sku.getIsscheme()=" + productBO.getIsscheme());


                    zfp.sellFree(productBO.getProductShortName(), taxgrp, productBO.getSrp(), pieceCount, -discount);

                }
            }
            double sum = zfp.calcIntermediateSum(false, false, false, 0.0f, '0');
            zfp.payment(sum, 0, false);
            if (mgoldenstore) {
                zfp.printText("**Golden store applied**", 2);
            }
            zfp.closeFiscalBon();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }
    }

    private class Checkandprint extends AsyncTask<Integer, Integer, Boolean> {
        //private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
          /*  progressDialogue = ProgressDialog.show(InvoiceReportDetail.this,
                    DataMembers.SD, "Printing",
					true, false);*/
            // Shows Progress Bar Dialog and then call doInBackground method
            builder = new AlertDialog.Builder(InvoiceReportDetail.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {


            Commons.print(TAG + ", Asynchrous STATE :" + mChatService.getState());
            if (mChatService.getState() != BtService.STATE_CONNECTED)

            {
                return false;
            } else {

                Printdata();
                return true;
            }


        }

        protected void onProgressUpdate(Integer... progress) {


        }


        @Override
        protected void onPostExecute(Boolean connect) {
            //	progressDialogue.dismiss();
            alertDialog.dismiss();
            if (!connect) {
                Toast.makeText(getApplicationContext(), "Not connected", Toast.LENGTH_SHORT).show();

            }
            bmodel.productHelper.clearOrderTableChecked();

            finish();


        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mChatService != null)
            mChatService.stop();

    }


    public void disconnect() {
        try {


            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }

        } catch (ConnectionException e) {

            Commons.printException(e);
        } finally {

        }
    }

    public ZebraPrinter connect() {
        // setStatus("Connecting...", Color.YELLOW);
        zebraPrinterConnection = null;
        // if (isBluetoothSelected()) {
        String macAddress = getMacAddressFieldText();
        zebraPrinterConnection = new BluetoothConnection(
                macAddress);
        SettingsHelper.saveBluetoothAddress(this, macAddress);


        try {
            zebraPrinterConnection.open();


        } catch (ConnectionException e) {

            Commons.printException(e);

            DemoSleeper.sleep(1000);

            disconnect();
        } catch (Exception e) {


            Commons.printException(e);
        }

        ZebraPrinter printer = null;

        isPrinterLanguageDetected = false;

        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);

                PrinterLanguage pl = printer.getPrinterControlLanguage();

                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
                isPrinterLanguageDetected = true;
            } catch (ConnectionException e) {

                Commons.print(TAG
                        + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException(e);
                // printer = null;
                // DemoSleeper.sleep(1000);
                // disconnect();
                isPrinterLanguageDetected = false;
            } /*
             * catch (ZebraPrinterLanguageUnknownException e) {
			 * setStatus("Unknown Printer Language", Color.RED);
			 * Commons.printException(e);
			 *
			 * isPrinterLanguageDetected = false;
			 *
			 * // printer = null; // DemoSleeper.sleep(1000); // disconnect(); }
			 */
        }

        return printer;
    }

    private void doConnection(String printername) {
        try {
            printer = connect();
            if (printer != null) {
                // sendTestLabel();
                bmodel.vanmodulehelper.downloadSubDepots();
                printInvoice(printername);
            } else {
                bmodel.productHelper.clearOrderTable();
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(this, "Printer not connected .Please check  Mac Address..", Toast.LENGTH_SHORT).show();
                /*Intent i=new Intent(this,HomeScreenTwo.class);
                startActivity(i);
				finish();*/
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {

            // String macAddress = "00:22:58:08:1E:37";

            SharedPreferences pref = getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
//			SharedPreferences.Editor editor = pref.edit();
//			macAddress=editor.
//			editor.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    boolean isPrinterLanguageDetected = false;

    public void printInvoice(String printername) {
        int count = 0;
        try {
            bmodel.getPrintCount();
            bmodel.printHelper.setPrintCnt(bmodel.getPrint_count());
            if (printername.equals(ZEBRA_3INCH)) {


                if (bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {

//					zebraPrinterConnection.write(printDatafor3inchprinter());

                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        count = count + 1;
                        zebraPrinterConnection.write(bmodel.printHelper.printDatafor3inchprinterForUnipal(mProductsForAdapter, false, 1));
                        bmodel.updatePrintCount(1);
                        bmodel.getPrintCount();
                        bmodel.printHelper.setPrintCnt(bmodel.print_count);

                    }

                    ////
                } else if (bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN) {
                    double entryLevelDiscountValue = 0;
                    if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_DIALOG) {
                        entryLevelDiscountValue = bmodel.printHelper.getEntryLevelDiscountValue(mProductsForAdapter);
                    }


                    for (int i = 0; i < mSelectedPrintCount + 1; i++) {
                        zebraPrinterConnection.write(bmodel.printHelper.printDataforTitan3inchprinter(mProductsForAdapter, entryLevelDiscountValue, 0, true));
                        count = count + 1;

                    }

                }


                alertDialog.dismiss();
                bmodel.productHelper.clearOrderTable();


                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 5002);

            }

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) zebraPrinterConnection)
                        .getFriendlyName();

                Commons.print(TAG + "friendlyName : " + friendlyName);
                DemoSleeper.sleep(500);
            }
        } catch (ConnectionException e) {
            Commons.printException(e);

        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            bmodel.updatePrintCount(bmodel.getPrint_count() + count);
            disconnect();
        }
    }

}
