package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.MerchandisingposmBO;
import com.ivy.sd.png.bo.OrderDetail;
import com.ivy.cpg.view.reports.orderreport.OrderReportBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.print.DemoSleeper;
import com.ivy.sd.print.SettingsHelper;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.printer.PrinterLanguage;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.util.ArrayList;
import java.util.Vector;

@Deprecated
public class DailyReportFragmentNew extends IvyBaseFragment {
    private static final String TAG = "DailyReportFragmentNew";

    private BusinessModel bmodel;
    private GridView dayReportgrid;

    private Vector<ConfigureBO> mDayList;
    private static final String ZEBRA_3INCH = "3";

    boolean hasInititative, hasMerchandising;

    // private ImageView mStatusIV;
    // private ZebraPrinter printer;

    private Connection zebraPrinterConnection;
    private AlertDialog alertDialog;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_daily_report_new, container,
                false);

        dayReportgrid = view.findViewById(R.id.gridview);

        initializeBusinessModel();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.nametv).getTag()) != null)
                ((TextView) view.findViewById(R.id.nametv))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.nametv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.valuetv).getTag()) != null)
                ((TextView) view.findViewById(R.id.valuetv))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(R.id.valuetv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        // loadData();
        bmodel.sbdMerchandisingHelper.loadSbdMerchCoverage();
        mDayList = bmodel.configurationMasterHelper.downloadDayReportList();
        updateDayReportData();

        return view;
    }


    private void initializeBusinessModel() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        getActivity().getMenuInflater().inflate(R.menu.menu_dayreport, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (bmodel.configurationMasterHelper.IS_DAY_REPORT_PRINT)
            menu.findItem(R.id.menu_print).setVisible(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == R.id.menu_print) {
            new Thread(new Runnable() {
                public void run() {
                    Looper.prepare();
                    doConnection(ZEBRA_3INCH);
                    Looper.loop();
                    Looper.myLooper().quit();
                }
            }).start();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, "Printing....");
            alertDialog = builder.create();
            alertDialog.show();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    void updateDayReportData() {
        BeatMasterBO b = getTodayBeat();
        if (b != null) {
            bmodel.beatMasterHealper.setTodayBeatMasterBO(b);
        } else {
            BeatMasterBO tempBeat = new BeatMasterBO();
            tempBeat.setBeatId(0);
            tempBeat.setBeatDescription("Sunday");
            tempBeat.setToday(0);
            bmodel.beatMasterHealper.setTodayBeatMasterBO(tempBeat);
        }

        bmodel.downloadDailyReport();
        DailyReportBO outlet = bmodel.getDailyRep();

        int totalcalls = bmodel.getTotalCallsForTheDay();
        int visitedcalls = bmodel.getVisitedCallsForTheDay();
        ArrayList<ConfigureBO> removable_config = new ArrayList<>();
        for (ConfigureBO con : mDayList) {

            if (con.getConfigCode().equalsIgnoreCase("DAYRT01")) {
                con.setMenuNumber(bmodel.formatValue(SDUtil
                        .convertToDouble(outlet.getTotValues())));

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT02")) {
                String eff = outlet.getEffCoverage();
                con.setMenuNumber(eff);
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT03")) {
                con.setMenuNumber(visitedcalls + "/" + totalcalls);

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT04")) {
                int productivecalls = bmodel.getProductiveCallsForTheDay();
                if (bmodel.configurationMasterHelper.IS_PRODUCTIVE_CALLS_OBJ_PH) {
                    float productiveCallsObj_PH = (float) totalcalls
                            * bmodel.configurationMasterHelper
                            .getProductiveCallPercentage() / 100;

                    int productiveCallsObj_PH_round = Math
                            .round(productiveCallsObj_PH);
                    if (productiveCallsObj_PH > productiveCallsObj_PH_round) {
                        productiveCallsObj_PH = productiveCallsObj_PH + 1;
                    }
                    con.setMenuNumber(productivecalls + "/"
                            + (int) productiveCallsObj_PH);

                } else {
                    con.setMenuNumber(productivecalls + "/" + visitedcalls);
                }

            } /*else if (con.getConfigCode().equalsIgnoreCase("DAYRT05")) {
                //con.setMenuNumber(bmodel.goldStoreValue());

            }*/ else if (con.getConfigCode().equalsIgnoreCase("DAYRT06")) {
                con.setMenuNumber(outlet.getTotLines());

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT07")) {
                float avg1 = 0;
                try {
                    float f1 = SDUtil.convertToFloat(outlet.getTotLines());
                    float f2 = SDUtil.convertToFloat(outlet.getEffCoverage());
                    if (f2 == 0.0) {
                        avg1 = 0;
                    } else {
                        avg1 = f1 / f2;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                con.setMenuNumber(SDUtil.roundIt(avg1, 2));

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT08")) {
                int val[] = bmodel.getSDBDistTargteAndAcheived();
                con.setMenuNumber(val[0] + "/" + val[1]);
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT09")) {
                int val[] = bmodel.getSDBMerchTargteAndAcheived();
                con.setMenuNumber(val[0] + "/" + val[1]);
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT10")) {
                removable_config.add(con);
                con.setMenuNumber("0");
                hasMerchandising = true;
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT11")) {
                removable_config.add(con);
                hasInititative = true;
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT12")) {
                int pre = 0, post = 0;
                ArrayList<OrderReportBO> mylist = bmodel.reportHelper
                        .downloadOrderreport();

                // Calculate the total order value.
                for (OrderReportBO ret : mylist) {
                    try {
                        String str[] = ret.getDist().split("/");
                        pre = pre + SDUtil.convertToInt(str[0]);
                        post = post + SDUtil.convertToInt(str[1]);
                    } catch (Exception e) {
                        // TODO: handle exception
                        Commons.printException(e);
                    }

                }
                float preavg = 0, postavg = 0;
                if (mylist.size() > 0) {
                    if (pre > 0) {
                        preavg = (float) pre / (float) mylist.size();
                    }
                    if (post > 0) {
                        postavg = (float) post / (float) mylist.size();
                    }

                    con.setMenuNumber(SDUtil.format(preavg, 1, 0) + "/"
                            + SDUtil.format(postavg, 1, 0));

                } else {
                    con.setMenuNumber("0/0");
                }

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT13")) {
                int val[] = bmodel.getGoldenPoints();
                if (val[1] != 0)
                    con.setMenuNumber(val[0]
                            + "/"
                            + val[1]
                            + " ("
                            + bmodel.formatPercent((val[0] / (float) val[1]) * 100)
                            + "%)");
                else
                    con.setMenuNumber(val[0] + "/" + val[1] + " (" + "0%)");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT14")) {
                con.setMenuNumber(bmodel.formatValue(bmodel
                        .getStrikeRateValue()));
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT15")) {
                int value = 0;
                Vector<InvoiceReportBO> mylist;
                ArrayList<OrderReportBO> myOrder;
                if (bmodel.configurationMasterHelper.IS_INVOICE) {
                    mylist = bmodel.reportHelper.downloadInvoicereport();
                    for (InvoiceReportBO inv : mylist) {
                        value += inv.getInvoiceAmount();
                    }
                    if (value > 0)
                        con.setMenuNumber(bmodel.formatValue((double) value
                                / (double) mylist.size()));
                    else
                        con.setMenuNumber("0");

                } else {
                    myOrder = bmodel.reportHelper.downloadOrderreport();
                    for (OrderReportBO inv : myOrder) {
                        value += inv.getOrderTotal();
                    }
                    if (value > 0)
                        con.setMenuNumber(bmodel.formatValue((double) value
                                / (double) myOrder.size()));
                    else
                        con.setMenuNumber("0");
                }

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT16")) {
                double FBvalue = 0;
                String productIds = bmodel.productHelper
                        .getTaggingDetails("FCBND");

                ArrayList<OrderDetail> mylist = bmodel.reportHelper
                        .downloadFBOrderDetailForDayReport(productIds);

                for (int i = 0; i < mylist.size(); i++) {
                    FBvalue += mylist.get(i).getTotalAmount();

                }
                con.setMenuNumber(bmodel.formatValue(FBvalue) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT17")) {
                double FB2value = 0;
                String productIds = bmodel.productHelper
                        .getTaggingDetails("FCBND2");
                ArrayList<OrderDetail> mylist = bmodel.reportHelper
                        .downloadFBOrderDetailForDayReport(productIds);

                for (int i = 0; i < mylist.size(); i++) {
                    FB2value += mylist.get(i).getTotalAmount();

                }
                con.setMenuNumber(bmodel.formatValue(FB2value) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT18")) {
                final float totalWeight = bmodel.productHelper.getTotalWeight("");
                con.setMenuNumber(bmodel.formatValue(totalWeight) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT19")) {
                con.setMenuNumber(bmodel.formatValue((SDUtil.convertToDouble(outlet.getTotValues())) - SalesReturnHelper.getInstance(getActivity()).getTotalSalesReturnValue(getContext().getApplicationContext())));
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT20")) {
                final int totalOrderedQty = bmodel.productHelper.getTotalOrderQty();
                con.setMenuNumber(totalOrderedQty + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT21")) {
                con.setMenuNumber(SDUtil.format(bmodel.getFITscoreForAllRetailers(), 2, 0) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT22")) {
                con.setMenuNumber(SDUtil.format((bmodel.getFITscoreForAllRetailers() / bmodel.getTotalCallsForTheDay()), 2, 0) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT23")) {

                con.setMenuNumber(bmodel.getGreenFITscoreRetailersCount() + "/" + bmodel.getTotalCallsForTheDay());
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT24")) {

                con.setMenuNumber(outlet.getNoofOrder() + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT25")) {

                try {

                    StringBuilder sb = new StringBuilder();
                    String op = getResources().getString(R.string.item_piece);
                    String oc = getResources().getString(R.string.item_case);
                    String ou = getResources().getString(R.string.item_outer);
                    //  /**----- update label from label master table based on key value**/
                    if (bmodel.labelsMasterHelper
                            .applyLabels("item_piece") != null)
                        op = bmodel.labelsMasterHelper
                                .applyLabels("item_piece");
                    if (bmodel.labelsMasterHelper
                            .applyLabels("item_case") != null)
                        oc = bmodel.labelsMasterHelper
                                .applyLabels("item_case");

                    if (bmodel.labelsMasterHelper
                            .applyLabels("item_outer") != null)
                        ou = bmodel.labelsMasterHelper
                                .applyLabels("item_outer");

                    ///**-------end of the updated statement-------**/

                    if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {

                        sb.append((outlet.getPcsQty() == null ? 0 : outlet.getPcsQty()) + " " + op + " ");
                    }
                    if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {

                        if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            String s = "\n" + (outlet.getCsQty() == null ? 0 : outlet.getCsQty()) + " "
                                    + oc + " ";
                            sb.append(s);
                        } else {
                            String s = (outlet.getCsQty() == null ? 0 : outlet.getCsQty()) + " "
                                    + oc + " ";
                            sb.append(s);
                        }
                    }
                    if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                        if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS || bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                            String s1 = "\n" + (outlet.getOuQty() == null ? 0 : outlet.getOuQty()) + " " + ou + " ";
                            sb.append(s1);
                        } else {
                            String s1 = (outlet.getOuQty() == null ? 0 : outlet.getOuQty()) + " " + ou + " ";
                            sb.append(s1);
                        }
                    }

                    con.setMenuNumber(sb + "");
                } catch (Exception e) {
                    Commons.printException(e);
                }

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT26")) {

                con.setMenuNumber(outlet.getTotPlannedVisit() + "/" + outlet.getTotPlanned());
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT27")) {

                con.setMenuNumber(outlet.getTotPlannedProductive() + "/" + outlet.getTotPlanned());
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT28")) {

                con.setMenuNumber(outlet.getTotAdhocProductive() + "/" + outlet.getTotAdhoc());
            }
        }

        mDayList.removeElement(removable_config);

        if (hasMerchandising) {
            ConfigureBO con;
            bmodel.sbdMerchandisingHelper.setMerchTypeCodes();

            ArrayList<MerchandisingposmBO> MerchCov;
            MerchCov = bmodel.sbdMerchandisingHelper.getSbdMerchCoverageBO();

            ArrayList<MerchandisingposmBO> merchList = new ArrayList<>();
            ArrayList<MerchandisingposmBO> merchInitList = new ArrayList<>();

            MerchandisingposmBO merch;
            for (int i = 0; i < MerchCov.size(); i++) {
                con = new ConfigureBO();
                merch = MerchCov.get(i);
                con.setMenuName(merch.getPosmdescription());
                con.setMenuNumber(merch.getAchieved() + "/" + merch.getTarget());
                if (bmodel.sbdMerchandisingHelper.merchTypeListCode
                        .equals(merch.getTypeListId())) {
                    merchList.add(merch);
                } else if (bmodel.sbdMerchandisingHelper.merchInitTypeListCode
                        .equals(merch.getTypeListId())) {
                    merchInitList.add(merch);
                }
            }

            con = new ConfigureBO();
            con.setMenuName("Merchandising");
            con.setMenuNumber("Heading");
            mDayList.add(con);
            for (MerchandisingposmBO merchandisingposmBO : merchList) {
                con = new ConfigureBO();
                con.setMenuName(merchandisingposmBO.getPosmdescription());
                con.setMenuNumber(merchandisingposmBO.getPosmValue());
                mDayList.add(con);
            }

            con = new ConfigureBO();
            con.setMenuName("Pricing");
            con.setMenuNumber("Heading");
            mDayList.add(con);

            for (MerchandisingposmBO merchandisingposmBO : merchInitList) {
                con = new ConfigureBO();
                con.setMenuName(merchandisingposmBO.getPosmdescription());
                con.setMenuNumber(merchandisingposmBO.getPosmValue());
                mDayList.add(con);
            }
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_DROPSIZE) {
            for (ConfigureBO config : bmodel
                    .downloadDailyReportDropSize(bmodel.configurationMasterHelper.DROPSIZE_ORDER_TYPE)) {
                ConfigureBO con = new ConfigureBO();
                con.setMenuName(config.getMenuName() + " Drop Size");
                con.setMenuNumber(config.getMenuNumber());
                mDayList.add(con);
            }
        }

        dayReportgrid.setAdapter(new MyAdapter(mDayList));

    }


    class MyAdapter extends BaseAdapter {
        Vector<ConfigureBO> items;

        public MyAdapter(Vector<ConfigureBO> conList) {
            items = conList;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.row_daily_report_fragment, parent, false);
                holder = new ViewHolder();

                holder.name = convertView
                        .findViewById(R.id.name_txt);
                holder.value = convertView
                        .findViewById(R.id.value_txt);
                holder.value1 = convertView
                        .findViewById(R.id.value_txt1);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.name.setText(items.get(position).getMenuName());
            String menunumber = items.get(position).getMenuNumber();
            if (menunumber.contains("/")) {
                String a1 = menunumber.split("/")[0];
                String b1 = menunumber.split("/")[1];
                holder.value.setText(a1);
                holder.value1.setText("/" + b1);

            } else {
                holder.value.setText(menunumber);

            }
            holder.value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.value1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            return convertView;
        }

    }

    class ViewHolder {
        TextView name, value, value1;
    }

    /**
     * Get today beat object by searching the beatmaster vector.
     *
     * @return -  BeatMasterBO object
     */
    private BeatMasterBO getTodayBeat() {
        try {
            int size = bmodel.beatMasterHealper.getBeatMaster().size();
            for (int i = 0; i < size; i++) {
                BeatMasterBO b = bmodel.beatMasterHealper.getBeatMaster()
                        .get(i);
                if (b.getToday() == 1)
                    return b;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void onBackPressed() {
        // do something on back.
        //return;
    }


    private void doConnection(String printername) {
        try {
            bmodel.loadManagementHelper.downloadSubDepots();
            ZebraPrinter printer = connect();
            if (printer != null) {
                // sendTestLabel();
                printInvoice(printername);
            } else {
                disconnect();
                alertDialog.dismiss();
                Toast.makeText(getActivity(), "Printer not connected ..", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    boolean isPrinterLanguageDetected = false;

    public void printInvoice(String printername) {

        try {
            if (printername.equals(ZEBRA_3INCH)) {

                zebraPrinterConnection.write(printDatafor3inchprinter());


                alertDialog.dismiss();


                bmodel.showAlert(
                        getResources().getString(
                                R.string.printed_successfully), 0);

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
            disconnect();
        }
    }

    public ZebraPrinter connect() {
        // setStatus("Connecting...", Color.YELLOW);
        zebraPrinterConnection = null;
        // if (isBluetoothSelected()) {
        zebraPrinterConnection = new BluetoothConnection(
                getMacAddressFieldText());
        SettingsHelper.saveBluetoothAddress(getActivity(), getMacAddressFieldText());

        Commons.print(TAG + "PRINT MAC : " + getMacAddressFieldText());
        try {
            zebraPrinterConnection.open();
            //mStatusIV.setImageResource(R.drawable.greenball);

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

    public String getMacAddressFieldText() {
        String macAddress = null;
        try {

            // String macAddress = "00:22:58:08:1E:37";

            SharedPreferences pref = getActivity().getSharedPreferences("PRINT",
                    Context.MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");

        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    public void disconnect() {
        try {
            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }

        } catch (ConnectionException e) {
            Commons.printException(e);
        }
    }


    public byte[] printDatafor3inchprinter() {
        byte[] printDataBytes = null;
        try {
            StringBuilder sb = new StringBuilder();
            PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320


            //TODO:if the below condition is always true so remove the conditions

            if (PrinterLanguage.CPCL == printerLanguage) {
                ArrayList<SubDepotBo> distributorList = bmodel.loadManagementHelper.getSubDepotList();
                String distributorAddress1 = "";
                String distributorAddress2 = "";
                String distributorContactNo = "";
                if (distributorList != null) {
                    for (SubDepotBo subDepotBo : distributorList) {
                        distributorAddress1 = subDepotBo.getAddress1();
                        distributorAddress2 = subDepotBo.getAddress2();
                        distributorContactNo = subDepotBo.getContactNumber();
                    }

                }


                int height;
                int x = 100;
                height = x + 100
                        + (mDayList.size() * 50) + 80;

                sb.append("! 0 200 200 " + height + " 1\r\n"
                        + "LEFT\r\n");
                sb.append("T 5 0 10 10 ");
                if (distributorAddress1 != null && !distributorAddress1.equals("null"))
                    sb.append(distributorAddress1 + "\r\n");
                else {
                    sb.append("  " + "\r\n");
                }
                sb.append("T 5 0 10 40 ");
                if (distributorAddress2 != null && !distributorAddress2.equals("null"))
                    sb.append(distributorAddress2 + "\r\n");
                else {
                    sb.append("  " + "\r\n");
                }

                sb.append("T 5 0 10 70 ");
                if (distributorContactNo != null && !distributorContactNo.equals("null"))
                    sb.append(distributorContactNo + "\r\n");
                else {
                    sb.append("  " + "\r\n");
                }


				/*sb.append("T 5 1 10 40 ");
                sb.append(getResources().getString(R.string.ramallah_industrial_zone_arabic)+"\r\n");
				sb.append("T 5 1 10 70 ");
				sb.append(getResources().getString(R.string.tel_1_arabic)+"\r\n");
				sb.append("T 5 1 10 100 ");
				sb.append(getResources().getString(R.string.gaza_indus_zone_carbt_arabic)+"\r\n");
				sb.append("T 5 1 10 130 ");
				sb.append(getResources().getString(R.string.tel_2_arabic)+"\r\n");*/


                sb.append("T 5 0 10 180 --------------------------------------------------\r\n");
                sb.append("LEFT \r\n");
                sb.append("T 5 0 10 200 Name \r\n");
                sb.append("T 5 0 300 200 Value  \r\n");

                sb.append("T 5 0 10 220 --------------------------------------------------\r\n");
                x += 120;
                for (ConfigureBO configureBO : mDayList) {
                    x += 40;
                    sb.append("T 5 0 10 " + x + " " + configureBO.getMenuName() + "\r\n");
                    sb.append("T 5 0 300 " + x + " " + configureBO.getMenuNumber() + "\r\n");
                }

                sb.append("PRINT \r\n");
                printDataBytes = sb.toString().getBytes();
            }
            //TODO:if the below condition is always false so it's never execute.  remove the below codes
            else if (printerLanguage == PrinterLanguage.ZPL) {

                printDataBytes = sb.toString().getBytes();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        return printDataBytes;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        bmodel = null;
        mDayList = null;
        zebraPrinterConnection = null;
    }
}