package com.ivy.cpg.view.van.vanstockapply;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Vector;

public class VanLoadStockViewFragment extends IvyBaseFragment {
    private static final String TAG = "Vanload Print";

    private ArrayList<VanLoadStockApplyBO> tempData;
    private ListView lvwplist;
    private BusinessModel bmodel;
    private Vector<VanLoadStockApplyBO> mylist;
    private TextView productname;
    private String uid;

    // print vanload

    private static final String ZEBRA_3INCH = "3";

    private Connection zebraPrinterConnection;
    private Bitmap mBmp;
    ;

    private String mSalesdate;


    private AlertDialog alertDialog;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_vanload_stockview, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        lvwplist = view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        productname = view.findViewById(R.id.productName);


        uid = getActivity().getIntent().getExtras().getString("uid");

        mylist = bmodel.stockreportmasterhelper.getStockReportMaster();

        updateVanload(uid);


        if (!bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {
            view.findViewById(R.id.caseTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.caseTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.caseTitle))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.caseTitle).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
            view.findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.pcsTitle).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.totaltitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.totaltitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.totaltitle).getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        if (!bmodel.configurationMasterHelper.SHOW_VAN_STK_OU)
            view.findViewById(R.id.outerTitle).setVisibility(View.GONE);
        else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.outerTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.outerTitle))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.outerTitle).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        setHasOptionsMenu(true);
        return view;

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        getActivity().getMenuInflater()
                .inflate(R.menu.menu_vanload_stockview, menu);

        super.onCreateOptionsMenu(menu, inflater);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bmodel.configurationMasterHelper.SHOW_VANLOAD_STOCK_PRINT)
            menu.findItem(R.id.menu_print).setVisible(true);
    }


    public void updateVanload(String uid) {
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        tempData = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            VanLoadStockApplyBO ret = mylist.get(i);
            if (mylist.get(i).getUid().equals(uid))
                tempData.add(ret);

        }

        MyAdapter mSchedule = new MyAdapter(tempData);
        lvwplist.setAdapter(mSchedule);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {

            getActivity().finish();
            return true;
        } else if (i == R.id.menu_print) {
            if (!uid.equalsIgnoreCase(getResources().getString(R.string.all))) {
                doInitialize();
                new Thread(new Runnable() {
                    public void run() {
                        Looper.prepare();
                        doConnection("3");
                        Looper.loop();
                        Looper.myLooper().quit();
                    }
                }).start();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                customProgressDialog(builder, "Printing....");
                alertDialog = builder.create();
                alertDialog.show();

            } else {
                Toast.makeText(getActivity(),
                        "Please select a particular van load",
                        Toast.LENGTH_LONG).show();
            }
        }
        return false;
    }

    private void doConnection(String printername) {
        ZebraPrinter printer;
        try {
            printer = connect();
            if (printer != null) {
                printInvoice(printername);
            } else {
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Printer not connected ..", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public ZebraPrinter connect() {
        zebraPrinterConnection = null;
        SharedPreferences settings = getActivity().getSharedPreferences(BusinessModel.PREFS_NAME,
                getActivity().MODE_PRIVATE);
        Commons.print(TAG + "PRINT MAC : " + settings.getString("MAC", ""));
        zebraPrinterConnection = new BluetoothConnection(settings.getString(
                "MAC", ""));

        try {
            zebraPrinterConnection.open();

        } catch (ConnectionException e) {

            Commons.printException("" + e);

            DemoSleeper.sleep(1000);
            disconnect();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        ZebraPrinter printer = null;


        if (zebraPrinterConnection.isConnected()) {
            try {
                printer = ZebraPrinterFactory.getInstance(PrinterLanguage.CPCL,
                        zebraPrinterConnection);
                PrinterLanguage pl = printer.getPrinterControlLanguage();
                Commons.print(TAG + "PRINT LANGUAGE : " + pl);
            } catch (ConnectionException e) {

                Commons.print(TAG
                        + "PRINT LANGUAGE : UNKNOWN : PrinterConnectionException");
                Commons.printException("" + e);
            }
        }

        return printer;
    }

    public void disconnect() {
        try {

            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }

        } catch (ConnectionException e) {

            Commons.printException("" + e);
        }
    }

    public byte[] printDatafor3inchprinter() {
        double mCaseTotalValue;
        double mPcTotalValue;
        double mOuterTotalValue;
        byte[] PrintDataBytes = null;
        try {
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            if (printerLanguage == PrinterLanguage.CPCL) {
                File file = new File(getActivity().getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.APP_DIGITAL_CONTENT
                        + "/"
                        + "receiptImg.png");

                mBmp = BitmapFactory.decodeFile(file.getAbsolutePath());

                if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                    return printThai();
                }

                int height;
                int x = 340;

                int mtot;
                mtot = tempData.size();
                System.out.println("mtot=" + mtot);

                height = x + (tempData.size()) * 50 + 380;
                Commons.print(TAG + "Heigt:" + height);

                StringBuilder printItem = new StringBuilder();
                printItem.append("! 0 200 200 ").append(height).append(" 1\r\n CENTER\r\n");
                printItem.append(ExtractGraphicsDataForCPCL(0, 0));

                printItem.append("T 5 1 10 140 ").append(bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorName()).append("\r\n");
                if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() != null) {

                    printItem.append("T 5 0 10 180 ").append("").append(bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber()).append("\r\n");
                }

                printItem.append("T 5 0 10 210 --------------------------------------------------\r\n");
                printItem.append("\r\n");
                printItem.append("LEFT \r\n");
                printItem.append("T 5 0 10 240 ");
                printItem.append("Seller Name" + ":");
                printItem.append(bmodel.userMasterHelper.getUserMasterBO().getUserName()).append("\r\n");

                printItem.append("LEFT \r\n");
                printItem.append("T 5 0 10 270 ");
                printItem.append("Date" + ":");
                printItem.append(mSalesdate).append("\r\n");


                printItem.append("T 5 0 10 330 --------------------------------------------------\r\n");

                printItem.append("T 5 0 220 345 ");
                printItem.append("Load Request" + "\r\n");
                printItem.append("T 5 0 10 375 --------------------------------------------------\r\n");

                printItem.append("\r\n");
                printItem.append("LEFT \r\n");
                printItem.append("T 5 0 10 400 ");
                printItem.append("Product" + "\r\n");
                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {
                    printItem.append("T 5 0 260 400 ");
                    printItem.append(getResources().getString(R.string.case_u)).append("\r\n");
                }
                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                    printItem.append("T 5 0 320 400 ");
                    printItem.append(getResources().getString(R.string.item_outer)).append("\r\n");
                }
                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
                    String pc_text = "Bottles";
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles)) != null)
                            pc_text = bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles));
                    } catch (Exception e) {
                        Commons.printException("" + e);
                        pc_text = "Bottles";
                    }

                    printItem.append("T 5 0 380 400 ");
                    printItem.append(pc_text).append("\r\n");
                }

                printItem.append("T 5 0 10 425 --------------------------------------------------\r\n");
                x += 100;
                mCaseTotalValue = 0;
                mPcTotalValue = 0;
                mOuterTotalValue = 0;
                for (VanLoadStockApplyBO productBO : tempData) {

                    mCaseTotalValue += productBO.getCaseQuantity();
                    mPcTotalValue += productBO.getPieceQuantity();
                    mOuterTotalValue += productBO.getOuterQty();
                    x += 30;

                    printItem.append("T 5 0 10 ").append(x).append(" ");
                    printItem.append("(" + getActivity().getResources().getString(R.string.free) + ")" + productBO.getProductName().toLowerCase()).append("\r\n");

                    x += 30;
                    if (bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {
                        printItem.append("T 5 0 280 ").append(x).append(" ");
                        printItem.append(productBO.getCaseQuantity()).append("\r\n");
                    }
                    if (bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                        printItem.append("T 5 0 330 ").append(x).append(" ");
                        printItem.append(productBO.getOuterQty()).append("\r\n");
                    }
                    if (bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
                        printItem.append("T 5 0 390 ").append(x).append(" ");
                        printItem.append(productBO.getPieceQuantity()).append("\r\n");
                    }


                }
                x += 30;
                printItem.append("T 5 0 10 ").append(x);

                printItem.append(" --------------------------------------------------\r\n");
                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {

                    x += 30;
                    printItem.append("T 5 0 10 ").append(x);
                    printItem.append("Total Case:" + "\r\n");

                    printItem.append("RIGHT \r\n");
                    printItem.append("T 5 0 390 ").append(x).append(" ");
                    printItem.append(bmodel.formatValue(mCaseTotalValue)).append("\r\n");
                    x += 30;
                    printItem.append("T 5 0 10 ").append(x);
                    printItem.append(" --------------------------------------------------\r\n");

                }
                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
                    String pc_text = "Bottles";
                    try {
                        if (bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles)) != null)
                            pc_text = bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles));
                    } catch (Exception e) {
                        Commons.printException("" + e);
                        pc_text = "Bottles";
                    }
                    x += 30;
                    printItem.append("T 5 0 10 ").append(x);
                    printItem.append("Total ").append(pc_text).append(":").append("\r\n");

                    printItem.append("T 5 0 390 ").append(x).append(" ");
                    printItem.append(bmodel.formatValue(mPcTotalValue)).append("\r\n");
                    x += 30;
                    printItem.append("T 5 0 10 ").append(x);
                    printItem.append(" --------------------------------------------------\r\n");

                }
                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                    x += 30;
                    printItem.append("T 5 0 10 ").append(x);
                    printItem.append("Total Outer:").append("\r\n");

                    printItem.append("T 5 0 390 ").append(x).append(" ");
                    printItem.append(bmodel.formatValue(mOuterTotalValue)).append("\r\n");
                    x += 30;
                    printItem.append("T 5 0 10 ").append(x);
                    printItem.append(" --------------------------------------------------\r\n");

                }


                x += 100;

                printItem.append("\r\n");
                printItem.append("\r\n");
                printItem.append("\r\n");

                printItem.append("T 5 0 330 ").append(x).append("Rep. Sign").append("\r\n");
                printItem.append("T 5 0 420 ").append(x).append("--------\r\n");

                printItem.append("PRINT\r\n");

                PrintDataBytes = printItem.toString().getBytes();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return PrintDataBytes;
    }

    public void printInvoice(String printername) {

        try {
            if (printername.equals(ZEBRA_3INCH)) {
                zebraPrinterConnection.write(printDatafor3inchprinter());
                alertDialog.dismiss();

                bmodel.showAlert(getResources().getString(
                        R.string.printed_successfully), 0);

            }

            DemoSleeper.sleep(1500);
            if (zebraPrinterConnection instanceof BluetoothConnection) {
                String friendlyName = ((BluetoothConnection) zebraPrinterConnection)
                        .getFriendlyName();
                Commons.print(TAG + "friendlyName : " + friendlyName);
                DemoSleeper.sleep(500);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            disconnect();
        }
    }

    public String ExtractGraphicsDataForCPCL(int _xpos, int _ypos) {
        String mData;
        int color, bit, currentValue, redValue, blueValue, greenValue;

        try {
            int loopWidth = 8 - (mBmp.getWidth() % 8);
            if (loopWidth == 8)
                loopWidth = mBmp.getWidth();
            else
                loopWidth += mBmp.getWidth();

            mData = "EG" + " " + Integer.toString((loopWidth / 8)) + " "
                    + Integer.toString(mBmp.getHeight()) + " "
                    + Integer.toString(_xpos) + " " + Integer.toString(_ypos)
                    + " ";
            Commons.print(TAG + ",Bitmap height :" + mBmp.getHeight());
            for (int y = 0; y < mBmp.getHeight(); y++) {
                bit = 128;
                currentValue = 0;
                for (int x = 0; x < loopWidth; x++) {
                    int intensity;

                    if (x < mBmp.getWidth()) {
                        color = mBmp.getPixel(x, y);

                        redValue = Color.red(color);
                        blueValue = Color.blue(color);
                        greenValue = Color.green(color);

                        intensity = 255 - ((redValue + greenValue + blueValue) / 3);
                    } else
                        intensity = 0;

                    if (intensity >= 128)
                        currentValue |= bit;
                    bit = bit >> 1;
                    if (bit == 0) {
                        String hex = Integer.toHexString(currentValue);
                        hex = LeftPad(hex);
                        mData = mData + hex.toUpperCase();

                        bit = 128;
                        currentValue = 0;

                    }
                }
            }
            mData = mData + "\r\n";

        } catch (Exception e) {
            Commons.printException("" + e);
            mData = "";
            return mData;
        }

        return mData;
    }

    private String LeftPad(String _num) {

        String str = _num;

        if (_num.length() == 1) {
            str = "0" + _num;
        }

        return str;
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 1:
                return new AlertDialog.Builder(getActivity())
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(
                                R.string.printed_successfully))
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                }).create();


        }
        return null;
    }

    private void doInitialize() {
        try {

            mSalesdate = DateTimeUtils.convertFromServerDateToRequestedFormat(
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                    bmodel.configurationMasterHelper.outDateFormat);


        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    private class MyAdapter extends ArrayAdapter<VanLoadStockApplyBO> {
        VanLoadStockApplyBO product;
        private ArrayList<VanLoadStockApplyBO> items;

        public MyAdapter(ArrayList<VanLoadStockApplyBO> items) {
            super(getActivity(), R.layout.row_stock_report_listview, items);
            this.items = items;
        }

        public VanLoadStockApplyBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            String tv;
            final ViewHolder holder;
            product = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_stock_report_listview,
                        parent, false);
                holder = new ViewHolder();
                holder.listBgLayout = row.findViewById(R.id.header_listlty);
                holder.psname = row.findViewById(R.id.productname);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.productCode = row.findViewById(R.id.product_code);
                holder.caseqty = row.findViewById(R.id.caseqty);
                holder.pcsqty = row.findViewById(R.id.pieceqty);
                holder.unitprice = row.findViewById(R.id.unitprice);
                holder.outerqty = row.findViewById(R.id.outerqty);
                holder.batchid = row.findViewById(R.id.batchid);


                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productname.setText(holder.pname);
                    }
                });

                if (!bmodel.configurationMasterHelper.SHOW_VAN_STK_CS)
                    holder.caseqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_VAN_STK_PS)
                    holder.pcsqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_VAN_STK_OU)
                    holder.outerqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.productCode.setVisibility(View.GONE);
                if (product.getBatchNumber() == null)
                    holder.batchid.setVisibility(View.GONE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            tv = product.getProductShortName();
            holder.psname.setText(tv);

            if (product.getIsFree() == 1)
                holder.psname.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(),
                        R.color.colorAccent));
            else
                holder.psname.setTextColor(ContextCompat.getColor(getActivity().getApplicationContext(),
                        android.R.color.black));


            tv = product.getCaseQuantity() + "";
            holder.caseqty.setText(tv);
            tv = product.getPieceQuantity() + "";
            holder.pcsqty.setText(tv);
            tv = product.getOuterQty() + "";
            holder.outerqty.setText(tv);
            holder.pname = product.getProductName();
            double unitprice = (product.getCaseQuantity() * product.getCaseSize()
                    + product.getPieceQuantity() + product.getOuterQty()
                    * product.getOuterSize())
                    * product.getBasePrice();
            tv = bmodel.formatValue(unitprice) + "";
            holder.unitprice.setText(tv);
            if (product.getBatchNumber() != null) {
                tv = getResources().getString(
                        R.string.batch_no)
                        + ": " + product.getBatchNumber() + "";
                holder.batchid.setText(tv);
            } else {
                holder.batchid.setText(getString(R.string.batch_no));
            }

            if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code)
                        + ": " + product.getProductCode() + " ";
                if (bmodel.labelsMasterHelper.applyLabels(holder.productCode.getTag()) != null)
                    prodCode = bmodel.labelsMasterHelper
                            .applyLabels(holder.productCode.getTag()) + ": " +
                            product.getProductCode() + " ";
                holder.productCode.setText(prodCode);
            }
            return row;
        }
    }

    class ViewHolder {
        LinearLayout listBgLayout;
        TextView psname, productCode;
        TextView caseqty;
        TextView pcsqty;
        TextView unitprice;
        TextView outerqty;
        TextView batchid;
        String pname;

    }

    private byte[] printThai() {
        double mCaseTotalValue;
        double mPcTotalValue;
        double mOuterTotalValue;
        byte[] printDataBytes = null;
        try {
            StringBuilder tempsb = new StringBuilder();
            tempsb.append("! U1 SETLP ANG12PT.CPF 0 34 \n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                    + bmodel.userMasterHelper.getUserMasterBO()
                    .getDistributorName() + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            if (bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() != null) {
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 "
                        + bmodel.userMasterHelper.getUserMasterBO().getDistributorTinNumber() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Seller Name" + ":" +
                    bmodel.userMasterHelper.getUserMasterBO().getUserName() + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Date" + ":" +
                    mSalesdate + "\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "CENTER\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Load Request\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Product\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT");

            if (bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 280 + " 1 " + getResources().getString(R.string.case_u) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");
            }

            if (bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 360 + " 1 " + getResources().getString(R.string.item_outer) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");
            }

            if (bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
                String pc_text = "Bottles";
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles)) != null)
                        pc_text = bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles));
                } catch (Exception e) {
                    Commons.printException("" + e);
                    pc_text = "Bottles";
                }
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 420 + " 1 " + pc_text + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            mCaseTotalValue = 0;
            mPcTotalValue = 0;
            mOuterTotalValue = 0;
            for (VanLoadStockApplyBO productBO : tempData) {

                mCaseTotalValue += productBO.getCaseQuantity();
                mPcTotalValue += productBO.getPieceQuantity();
                mOuterTotalValue += productBO.getOuterQty();

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + productBO.getProductShortName() + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 280 + " 1 " + productBO.getCaseQuantity() + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");
                }

                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 360 + " 1 " + productBO.getOuterQty() + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT");
                }

                if (bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
                    tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                    tempsb.append("SETBOLD 1 \r\n");
                    tempsb.append("TEXT ANG12PT.CPF 0 " + 420 + " 1 " + productBO.getPieceQuantity() + "\r\n");
                    tempsb.append("SETBOLD 0 \r\n");
                    tempsb.append("PRINT\r\n");
                }
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");

            if (bmodel.configurationMasterHelper.SHOW_VAN_STK_CS) {

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Total Case:\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 280 + " 1 " + bmodel.formatValue(mCaseTotalValue) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

            }
            if (bmodel.configurationMasterHelper.SHOW_VAN_STK_PS) {
                String pc_text = "Bottles";
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles)) != null)
                        pc_text = bmodel.labelsMasterHelper.applyLabels(getResources().getString(R.string.van_load_bottles));
                } catch (Exception e) {
                    Commons.printException("" + e);
                    pc_text = "Bottles";
                }

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Total " + pc_text + ":\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 420 + " 1 " + bmodel.formatValue(mPcTotalValue) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

            }
            if (bmodel.configurationMasterHelper.SHOW_VAN_STK_OU) {
                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 " + "Total Outer:" + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 440 + " 1 " + bmodel.formatValue(mOuterTotalValue) + "\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");

                tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
                tempsb.append("SETBOLD 1 \r\n");
                tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "---------------------------------------------------------------------------\r\n");
                tempsb.append("SETBOLD 0 \r\n");
                tempsb.append("PRINT\r\n");
            }

            tempsb.append("! 0 200 200 " + 40 + " 1\r\n" + "LEFT\r\n");
            tempsb.append("SETBOLD 1 \r\n");
            tempsb.append("TEXT ANG12PT.CPF 0 " + 10 + " 1 " + "Rep. Sign--------------------------------------------------------------\r\n");
            tempsb.append("SETBOLD 0 \r\n");
            tempsb.append("PRINT\r\n");
            tempsb.append("\r\n");

            printDataBytes = String.valueOf(tempsb).getBytes("ISO-8859-11");

        } catch (Exception e) {
            Commons.printException(e);
        }
        return printDataBytes;
    }

}
