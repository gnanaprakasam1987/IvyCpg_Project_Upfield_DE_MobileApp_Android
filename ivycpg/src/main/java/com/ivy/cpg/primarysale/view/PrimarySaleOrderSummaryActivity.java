package com.ivy.cpg.primarysale.view;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyThread;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 28-09-2015.
 */
public class PrimarySaleOrderSummaryActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    private Button saveSummary, editSummary, deleteSummary, deliveryDate;
    private TextView valuetv, linestv;
    private ArrayList<ProductMasterBO> ordersummaryList;
    private ListView lvwplist;
    private BusinessModel bmodel;
    private static final int DATE_DIALOG_ID = 0;
    private String nextDate;
    private boolean isClick;
    private AlertDialog.Builder build;
    private AlertDialog alertDialog;
    private List<ProductMasterBO> mOrderedProductList;
    private double totalOrderValue;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        setContentView(R.layout.activity_distributor_ordersummary);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        isClick = false;

        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (getSupportActionBar() != null)
            setScreenTitle(getResources().getString(R.string.summary));

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        saveSummary = (Button) findViewById(R.id.saveSummary);
        editSummary = (Button) findViewById(R.id.editSummary);
        deleteSummary = (Button) findViewById(R.id.deleteSummary);
        deliveryDate = (Button) findViewById(R.id.deliveryDate);
        valuetv = (TextView) findViewById(R.id.valuetv);
        linestv = (TextView) findViewById(R.id.linestv);
        lvwplist = (ListView) findViewById(R.id.list);

        saveSummary.setOnClickListener(this);
        editSummary.setOnClickListener(this);
        deleteSummary.setOnClickListener(this);
        deliveryDate.setOnClickListener(this);

        saveSummary.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        editSummary.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        deleteSummary.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) findViewById(R.id.tv_deliveryDate)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        deliveryDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.lpcLabel)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        linestv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.totalValuelbl)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        valuetv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        getNextDate();
        if (bmodel.distributorMasterHelper.isEditDistributorOrder()) {
            Commons.print("bmodel.getOrderHeaderBO().getDeliveryDate()" + bmodel.distributorMasterHelper.getDeliveryDate());
            deliveryDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(bmodel.distributorMasterHelper.getDeliveryDate(),
                    bmodel.configurationMasterHelper.outDateFormat));
        } else
            deliveryDate.setText(nextDate);
        Vector<ProductMasterBO> productList = bmodel.productHelper
                .getProductMaster();

        if (productList == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (bmodel.distributorMasterHelper.hasDistributorOrder(bmodel.distributorMasterHelper.getDistributor().getDId())) {
            deleteSummary.setVisibility(View.VISIBLE);
        } else {
            deleteSummary.setVisibility(View.GONE);
        }

        int productsCount = productList.size();
        mOrderedProductList = new ArrayList<ProductMasterBO>();
        totalOrderValue = 0;
        ProductMasterBO productBO;
        for (int i = 0; i < productsCount; i++) {

            productBO = productList.elementAt(i);
            if (productBO.getOrderedCaseQty() > 0
                    || productBO.getOrderedPcsQty() > 0
                    || productBO.getOrderedOuterQty() > 0) {

                mOrderedProductList.add(productBO);
                double line_total_price = 0.0;
                line_total_price = (productBO.getOrderedCaseQty() * productBO
                        .getCsrp())
                        + (productBO.getOrderedPcsQty() * productBO
                        .getSrp())
                        + (productBO.getOrderedOuterQty() * productBO
                        .getOsrp());
                productBO.setOrderPricePiece(line_total_price);
                totalOrderValue += line_total_price;


            } // End of Orderproducts condition
        } // End of products loop
        linestv.setText(mOrderedProductList.size() + "");
        valuetv.setText(bmodel.formatValue(totalOrderValue));
        lvwplist.setAdapter(new ListViewAdapter(mOrderedProductList));
    }

    @Override
    public void onClick(View v) {
        int i1 = v.getId();
        if (i1 == R.id.saveSummary) {
            if (!isClick) {
                isClick = true;
                if (mOrderedProductList.size() > 0) {
                    bmodel.getOrderHeaderBO().setOrderValue(totalOrderValue);
                    bmodel.getOrderHeaderBO().setLinesPerCall(
                            SDUtil.convertToInt((String) linestv.getText()));
                    bmodel.getOrderHeaderBO()
                            .setDeliveryDate(
                                    DateTimeUtils.convertToServerDateFormat(
                                            deliveryDate.getText().toString(),
                                            bmodel.configurationMasterHelper.outDateFormat));
                    build = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this);

                    customProgressDialog(build, getResources().getString(R.string.saving_new_order));
                    alertDialog = build.create();
                    alertDialog.show();

                    if (bmodel.hasOrder()) {
                        new MyThread(PrimarySaleOrderSummaryActivity.this,
                                DataMembers.DISTSAVEORDERANDSTOCK).start();
                    } else {
                        isClick = false;
                    }
                } else {
                    isClick = false;
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.no_products_exists),
                            Toast.LENGTH_SHORT);

                }
            }

        } else if (i1 == R.id.editSummary) {
            if (!isClick) {
                isClick = true;
                bmodel.setOrderHeaderBO(null); // Clear Object other wise Data
                Intent i = new Intent(PrimarySaleOrderSummaryActivity.this,
                        PrimarySaleStockAndOrderFragmentActivity.class);
                startActivity(i);
                finish();
            }

        } else if (i1 == R.id.deleteSummary) {
            if (bmodel.distributorMasterHelper.hasDistributorStockCheck())
                showDialog(1);
            else
                showDialog(5);

        } else if (i1 == R.id.deliveryDate) {
            showDialog(DATE_DIALOG_ID);

        }
    }

    class ListViewAdapter extends ArrayAdapter<ProductMasterBO> {
        private List<ProductMasterBO> items;


        public ListViewAdapter(List<ProductMasterBO> items) {
            super(PrimarySaleOrderSummaryActivity.this, R.layout.activity_distributor_ordersummarry_listitem, items);
            this.items = items;
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            try {
                final ViewHolder holder;
                final ProductMasterBO product = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.activity_distributor_ordersummarry_listitem, parent,
                            false);
                    holder = new ViewHolder();

                    holder.pdtnametv = (TextView) row
                            .findViewById(R.id.pdtnametv);
                    holder.casetv = (TextView) row
                            .findViewById(R.id.casetv);
                    holder.outertv = (TextView) row.findViewById(R.id.outertv);
                    holder.piecetv = (TextView) row
                            .findViewById(R.id.piecetv);

                    holder.pricetv = (TextView) row
                            .findViewById(R.id.pricetv);
                    holder.totaltv = (TextView) row
                            .findViewById(R.id.totaltv);
                    ((View) row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);

                    row.setTag(holder);

                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.productObj = product;

                holder.productId = holder.productObj.getProductID();
                holder.productName = holder.productObj.getProductName();

                holder.pdtnametv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.totaltv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                if (bmodel.configurationMasterHelper.SHOW_DIST_ORDER_CASE) {
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.casetv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                }
                if (bmodel.configurationMasterHelper.SHOW_DIST_ORDER_OUTER) {
                    ((LinearLayout) row.findViewById(R.id.llOuter)).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.outertv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                }
                if (bmodel.configurationMasterHelper.SHOW_DIST_ORDER_PIECE) {
                    ((LinearLayout) row.findViewById(R.id.llPiece)).setVisibility(View.VISIBLE);
                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.piecetv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                }

                ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.pricetv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                holder.pricetv.setText("" + bmodel.formatValue(holder.productObj.getSrp()));
                holder.totaltv.setText("" + bmodel.formatValue(holder.productObj.getOrderPricePiece()));
                holder.pdtnametv.setText(holder.productObj.getProductShortName());
                holder.piecetv.setText(holder.productObj.getOrderedPcsQty() + "");
                holder.casetv.setText(holder.productObj.getOrderedCaseQty() + "");
                holder.outertv.setText(holder.productObj.getOrderedOuterQty() + "");

            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return (row);
        }
    }

    public class ViewHolder {
        private String productName, productId;
        private ProductMasterBO productObj;
        private TextView pdtnametv, casetv, outertv, piecetv, pricetv, totaltv;

    }

    private void getNextDate() {
        try {
            Calendar origDay = Calendar.getInstance();
            origDay.add(Calendar.DAY_OF_YEAR, 1);
            nextDate = DateTimeUtils.convertDateObjectToRequestedFormat(origDay.getTime(),
                    bmodel.configurationMasterHelper.outDateFormat);
        } catch (Exception e) {
            Commons.printException(e);// TODO: handle exception
        }
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                    dayOfMonth);
            deliveryDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(
                    selectedDate.getTime(),
                    bmodel.configurationMasterHelper.outDateFormat));

            Calendar currentcal = Calendar.getInstance();
            currentcal.add(Calendar.DAY_OF_YEAR, -1);
            if (currentcal.after(selectedDate)) {
                Toast.makeText(
                        getApplicationContext(),
                        getResources().getString(
                                R.string.Please_select_next_day),
                        Toast.LENGTH_SHORT).show();
                deliveryDate.setText(nextDate);
            }
            view.updateDate(year,monthOfYear,dayOfMonth);
        }
    };

    @Override
    protected Dialog onCreateDialog(int id) {


        switch (id) {
            case DATE_DIALOG_ID:
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DAY_OF_YEAR, 1);
                int cyear = c.get(Calendar.YEAR);
                int cmonth = c.get(Calendar.MONTH);
                int cday = c.get(Calendar.DAY_OF_MONTH);

                // todayDate = cday + "/" + cmonth + "/" + cyear;
                nextDate = DateTimeUtils.convertDateObjectToRequestedFormat(c.getTime(),
                        bmodel.configurationMasterHelper.outDateFormat);
                MyDatePickerDialog d = new MyDatePickerDialog(this,R.style.DatePickerDialogStyle,
                        mDateSetListener, cyear, cmonth, cday);
                d.setPermanentTitle(getResources().getString(R.string.choose_date));
                return d;

            case 1:

                AlertDialog.Builder builder = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_delete_order))
                        .setPositiveButton(getResources().getString(R.string.yes),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        build = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this);
                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();

                                        new MyThread(PrimarySaleOrderSummaryActivity.this,
                                                DataMembers.DIST_DELETE_ORDER).start();
                                    }
                                })
                        .setNeutralButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(
                                        R.string.delete_stock_and_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        build = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this);

                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();

                                        // new deleteStockAndOrder().execute();
                                        new MyThread(PrimarySaleOrderSummaryActivity.this,
                                                DataMembers.DIST_DELETE_STOCK_ORDER).start();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

            case 3:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_saved_locally_order_id_is)
                                        + OrderHelper.getInstance(PrimarySaleOrderSummaryActivity.this).getOrderId())
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.distTimeStampHeaderHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                                        bmodel.productHelper.clearOrderTable();
                                        PrimarySaleOrderSummaryActivity.this.setResult(PrimarySaleOrderSummaryActivity.this.RESULT_OK);
                                        finish();
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder2);
                break;

            case 5:
                AlertDialog.Builder builder4 = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_delete_order))
                        .setPositiveButton(
                                getResources().getString(R.string.only_order),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        build = new AlertDialog.Builder(PrimarySaleOrderSummaryActivity.this);
                                        customProgressDialog(build, getResources().getString(R.string.deleting_order));
                                        alertDialog = build.create();
                                        alertDialog.show();
                                        new MyThread(PrimarySaleOrderSummaryActivity.this,
                                                DataMembers.DIST_DELETE_ORDER).start();
                                    }
                                })

                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder4);
                break;


        }
        return null;
    }

    public Handler getHandler() {
        return handler;
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            isClick = false;

            if (msg.what == DataMembers.NOTIFY_ORDER_SAVED) {
                try {
                    //  pd.dismiss();
                    alertDialog.dismiss();
                    showDialog(3);
                } catch (Exception e) {
                    // TODO: handle exception
                }
            }

            if (msg.what == DataMembers.NOTIFY_ORDER_DELETED) {
                try {

                    alertDialog.dismiss();
                    finish();

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
        }

    };

}
