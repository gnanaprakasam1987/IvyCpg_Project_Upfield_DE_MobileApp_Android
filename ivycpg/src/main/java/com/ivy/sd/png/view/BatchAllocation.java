package com.ivy.sd.png.view;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.initiative.InitiativeActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.order.catalog.CatalogOrder;
import com.ivy.cpg.view.order.scheme.SchemeApply;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;
import java.util.Vector;

public class BatchAllocation extends IvyBaseActivityNoActionBar implements OnClickListener {
    private static final String TAG = "BatchAllocation";
    private BusinessModel bmodel;
    private ListView lvwplist;
    /**
     * this list constructs item for list view
     */
    private ArrayList<ProductMasterBO> mylist;
    /**
     * this dialog used to allocate and show batchwise records
     */
    BatchAllocationDialog dialog;
    /**
     * this dialog used to show progress while save
     */
    ProgressDialog pd;
    /**
     * find total value of all batchwise records
     */
    private double mTotalValue = 0.0;
    /**
     * check sign or not
     */


    private String screenCode = "MENU_STK_ORD";

    private Toolbar toolbar;
    private Button mBtnNext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_allocation);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.configurationMasterHelper.getBatchAllocationtitle());
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                screenCode = extras.getString("ScreenCode");
            }
        }

       /* // On/Off order case and pcs
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
            findViewById(R.id.caseTitle).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            findViewById(R.id.outercaseTitle).setVisibility(View.GONE);*/

        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnNext.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        ((LinearLayout) findViewById(R.id.ll_value)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.ll_lpc)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.ll_dist)).setVisibility(View.GONE);
        ((LinearLayout) findViewById(R.id.ll_totqty)).setVisibility(View.GONE);//ll_totqty is not used, hence made invisible


        mBtnNext.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View view) {
                nextButtonClick();
            }
        });


    }


    @Override
    protected void onResume() {
        super.onResume();
        updateOrderTable();

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * refresh listview and update total value
     */
    private void updateOrderTable() {

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        mylist = new ArrayList<ProductMasterBO>();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
            if (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0
                    || ret.getOrderedOuterQty() > 0) {
                mTotalValue = mTotalValue + ret.getBatchwiseTotal();

                mylist.add(ret);

            }
        }

        MyAdapter mSchedule = new MyAdapter();
        lvwplist.setAdapter(mSchedule);

    }

    /**
     * @author rajesh.k set Adapter for listview
     */

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mylist.size();
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

            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.batch_allocation_row, parent,
                        false);
                holder = new ViewHolder();
                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.caseqtyEditText = (TextView) row
                        .findViewById(R.id.orderQTYinCase);
                holder.pieceqty = (TextView) row
                        .findViewById(R.id.orderQTYinpiece);

                holder.total = (TextView) row.findViewById(R.id.total);

                holder.outerQty = (TextView) row
                        .findViewById(R.id.outerorderQTYinCase);

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.caseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    ((LinearLayout) row.findViewById(R.id.llPc)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.pcsTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.pcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.pcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                else {
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.outercaseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.outercaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(R.id.outercaseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e);
                    }

                }

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.caseqtyEditText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.pieceqty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                holder.pieceqty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Commons.print(TAG + ",piece qty on touch listener called ");

                        int inType = holder.pieceqty.getInputType();
                        holder.pieceqty.setInputType(InputType.TYPE_NULL);
                        holder.pieceqty.onTouchEvent(event);
                        holder.pieceqty.setInputType(inType);
                        return true;
                    }
                });

                holder.caseqtyEditText
                        .setOnTouchListener(new OnTouchListener() {

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                Commons.print(TAG + ",case qty on touch listener called ");

                                int inType = holder.caseqtyEditText
                                        .getInputType();
                                holder.caseqtyEditText
                                        .setInputType(InputType.TYPE_NULL);
                                holder.caseqtyEditText.onTouchEvent(event);
                                holder.caseqtyEditText.setInputType(inType);
                                return true;
                            }
                        });

                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        Commons.print(TAG + ",outer qty on touch listener called ");

                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        return true;
                    }
                });
                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.batchAllocationHelper
                                .checkBatchwiseRecordAvailable(holder.productObj
                                        .getProductID())) {
                            dialog = new BatchAllocationDialog(
                                    BatchAllocation.this, holder.productObj,
                                    BatchAllocation.this);
                            dialog.setCancelable(false);
                            dialog.show();
                        } else {
                            Toast.makeText(BatchAllocation.this,
                                    " No Batchwise record available ",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.productObj = mylist.get(position);
            holder.productId = holder.productObj.getProductID();
            holder.productCode = holder.productObj.getProductCode();
            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();

            holder.caseSize = holder.productObj.getCaseSize();
            holder.stockInHand = holder.productObj.getSIH();

            holder.outerQty
                    .setText(holder.productObj.getOrderedOuterQty() + "");
            holder.caseqtyEditText.setText(holder.productObj
                    .getOrderedCaseQty() + "");
            holder.pieceqty.setText(holder.productObj.getOrderedPcsQty() + "");
            holder.total.setText(bmodel.formatValue(holder.productObj
                    .getBatchwiseTotal()));

            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.caseqtyEditText.setEnabled(false);
            } else {
                holder.caseqtyEditText.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.pieceqty.setEnabled(false);
            } else {
                holder.pieceqty.setEnabled(true);
            }

            return (row);

        }

    }

    /*
     * public void onClick(View v) {
     *
     * dialog = new BatchAllocationDialog(this, mSelectedProductBO);
     * dialog.show(); }
     */

    public void numberPressed(View v) {
        Commons.print(TAG + ",Number Pressed call ");
        dialog.numberPressed(v);
    }

    class ViewHolder {

        ProductMasterBO productObj;
        String productId, productCode, pname;
        int caseSize, stockInHand;// product id
        TextView psname;
        TextView caseqtyEditText, pieceqty, outerQty;
        TextView total;
    }

    @Override
    public void onClick(View v) {

    }

    /**
     * go to back screen
     */
    private void backButtonClick() {

        Intent i;
        if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
            i = new Intent(BatchAllocation.this, CatalogOrder.class);
        } else {
            i = new Intent(BatchAllocation.this, StockAndOrder.class);
        }
        i.putExtra("OrderFlag", "Nothing");
        i.putExtra("ScreenCode", screenCode);
        startActivity(i);
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        finish();


    }

    /**
     * save invoice and order
     */

    private void nextButtonClick() {
        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        if (mylist.size() == 0) {
            Toast.makeText(BatchAllocation.this, "please take order ",
                    Toast.LENGTH_SHORT).show();
        } else {
            bmodel.batchAllocationHelper.updateOrderedeBatchCount(mylist);
            if (schemeHelper.IS_SCHEME_ON
                    && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
                Intent init = new Intent(BatchAllocation.this, SchemeApply.class);
                init.putExtra("ScreenCode", screenCode);
                init.putExtra("ForScheme", screenCode);
                startActivity(init);
//                finish();
            } else if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
                Intent init = new Intent(BatchAllocation.this, OrderDiscount.class);
                init.putExtra("ScreenCode", screenCode);
                startActivity(init);
//                finish();
            } else if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                Intent init = new Intent(BatchAllocation.this,
                        InitiativeActivity.class);
                init.putExtra("ScreenCode", screenCode);
                startActivity(init);
//                finish();
            } else if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
                Intent i = new Intent(BatchAllocation.this,
                        DigitalContentActivity.class);
                i.putExtra("ScreenCode", screenCode);
                i.putExtra("FromInit", "Initiative");
                startActivity(i);
//                finish();
            } else {
                Intent i = new Intent(BatchAllocation.this, OrderSummary.class);
                i.putExtra("ScreenCode", screenCode);
                startActivity(i);
//                finish();
            }
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();

        }

        // }

    }

    public Handler getHandler() {
        return handler;
    }

    private final Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {

            if (msg.what == DataMembers.NOTIFY_INVOICE_SAVED) {
                try {
                    pd.dismiss();
                    bmodel = (BusinessModel) getApplicationContext();

                    if (bmodel.configurationMasterHelper.IS_INVOICE) {
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.invoice_created_successfully),
                                DataMembers.NOTIFY_INVOICE_SAVED);
                    } else {
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.order_saved_and_print_preview_created_successfully),
                                DataMembers.NOTIFY_INVOICE_SAVED);
                    }

                } catch (Exception e) {
                }
            }
            return false;
        }
    });


}
