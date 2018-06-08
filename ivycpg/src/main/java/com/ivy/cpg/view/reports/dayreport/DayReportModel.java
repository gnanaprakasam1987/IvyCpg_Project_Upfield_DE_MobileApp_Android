package com.ivy.cpg.view.reports.dayreport;

import android.content.Context;

import com.ivy.cpg.view.reports.OrderReportBO;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.InvoiceReportBO;
import com.ivy.sd.png.bo.MerchandisingposmBO;
import com.ivy.sd.png.bo.OrderDetail;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

import javax.inject.Inject;

public class DayReportModel implements IDayReportModelPresenter {

    private Context mContext;
    private IDayReportView mIDayReportView;
    private BusinessModel mBusinessModel;
    private boolean hasMerchandising;
    private Vector<ConfigureBO> mDayList = null;

    @Inject
    public DayReportHelper dayReportHelper;

    public DayReportModel(Context context, IDayReportView IDayReportView, BusinessModel mBusinessModel) {
        this.mContext = context;
        this.mIDayReportView = IDayReportView;
        this.mBusinessModel = mBusinessModel;
        ReportComponent reportComponent = DaggerReportComponent.builder().reportModule(new ReportModule((BusinessModel) mContext.getApplicationContext())).build();
        reportComponent.inject(this);
        // dayReportHelper = reportComponent.provideDayReportHelper();
    }

    @Inject
    public void initializeHelper(DayReportHelper dayReportHelper) {
        //this.dayReportHelper = dayReportHelper;
    }

    private void updateDayReportData(Vector<ConfigureBO> mDayList) {
        BeatMasterBO b = getTodayBeat();
        if (b != null) {
            mBusinessModel.beatMasterHealper.setTodayBeatMasterBO(b);
        } else {
            BeatMasterBO tempBeat = new BeatMasterBO();
            tempBeat.setBeatId(0);
            tempBeat.setBeatDescription("Sunday");
            tempBeat.setToday(0);
            mBusinessModel.beatMasterHealper.setTodayBeatMasterBO(tempBeat);
        }

        mBusinessModel.downloadDailyReport();
        DailyReportBO outlet = mBusinessModel.getDailyRep();

        int totalcalls = mBusinessModel.getTotalCallsForTheDay();
        int visitedcalls = mBusinessModel.getVisitedCallsForTheDay();
        ArrayList<ConfigureBO> removable_config = new ArrayList<>();
        for (ConfigureBO con : mDayList) {

            if (con.getConfigCode().equalsIgnoreCase("DAYRT01")) {
                con.setMenuNumber(mBusinessModel.formatValue(SDUtil
                        .convertToDouble(outlet.getTotValues())));

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT02")) {
                String eff = outlet.getEffCoverage();
                con.setMenuNumber(eff);
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT03")) {
                con.setMenuNumber(visitedcalls + "/" + totalcalls);

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT04")) {
                int productivecalls = mBusinessModel.getProductiveCallsForTheDay();
                if (mBusinessModel.configurationMasterHelper.IS_PRODUCTIVE_CALLS_OBJ_PH) {
                    float productiveCallsObj_PH = (float) totalcalls
                            * mBusinessModel.configurationMasterHelper
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
                //con.setMenuNumber(mBusinessModel.goldStoreValue());

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
                int val[] = mBusinessModel.getSDBDistTargteAndAcheived();
                con.setMenuNumber(val[0] + "/" + val[1]);
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT09")) {
                int val[] = mBusinessModel.getSDBMerchTargteAndAcheived();
                con.setMenuNumber(val[0] + "/" + val[1]);
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT10")) {
                removable_config.add(con);
                con.setMenuNumber("0");
                hasMerchandising = true;
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT11")) {
                removable_config.add(con);
                //hasInitiative = true;
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT12")) {
                int pre = 0, post = 0;
                //ArrayList<OrderReportBO> mylist = mBusinessModel.reportHelper.downloadOrderreport();

                ArrayList<OrderReportBO> mylist = dayReportHelper.downloadOrderReport();

                // Calculate the total order value.
                for (OrderReportBO ret : mylist) {
                    try {
                        String str[] = ret.getDist().split("/");
                        pre = pre + Integer.parseInt(str[0]);
                        post = post + Integer.parseInt(str[1]);
                    } catch (Exception e) {
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
                int val[] = mBusinessModel.getGoldenPoints();
                if (val[1] != 0)
                    con.setMenuNumber(val[0]
                            + "/"
                            + val[1]
                            + " ("
                            + mBusinessModel.formatPercent((val[0] / (float) val[1]) * 100)
                            + "%)");
                else
                    con.setMenuNumber(val[0] + "/" + val[1] + " (" + "0%)");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT14")) {
                con.setMenuNumber(mBusinessModel.formatValue(mBusinessModel
                        .getStrikeRateValue()));
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT15")) {
                int value = 0;
                Vector<InvoiceReportBO> mylist;
                ArrayList<OrderReportBO> myOrder;
                if (mBusinessModel.configurationMasterHelper.IS_INVOICE) {

                    //TODO:
                    //  mylist = mBusinessModel.reportHelper.downloadInvoicereport();
                    mylist = dayReportHelper.downloadInvoiceReport();

                    for (InvoiceReportBO inv : mylist) {
                        value += inv.getInvoiceAmount();
                    }
                    if (value > 0)
                        con.setMenuNumber(mBusinessModel.formatValue((double) value
                                / (double) mylist.size()));
                    else
                        con.setMenuNumber("0");

                } else {
                    //TODO:
                    // myOrder = mBusinessModel.reportHelper.downloadOrderreport();
                    myOrder = dayReportHelper.downloadOrderReport();
                    for (OrderReportBO inv : myOrder) {
                        value += inv.getOrderTotal();
                    }
                    if (value > 0)
                        con.setMenuNumber(mBusinessModel.formatValue((double) value
                                / (double) myOrder.size()));
                    else
                        con.setMenuNumber("0");
                }

            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT16")) {
                double FBvalue = 0;
                String productIds = mBusinessModel.productHelper
                        .getTaggingDetails("FCBND");


                //TODO:
                //ArrayList<OrderDetail> mylist = mBusinessModel.reportHelper
                //   .downloadFBOrderDetailForDayReport(productIds);

                ArrayList<OrderDetail> mylist = dayReportHelper
                        .downloadFBOrderDetailForDayReport(productIds);

                for (int i = 0; i < mylist.size(); i++) {
                    FBvalue += mylist.get(i).getTotalAmount();

                }
                con.setMenuNumber(mBusinessModel.formatValue(FBvalue) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT17")) {
                double FB2value = 0;
                String productIds = mBusinessModel.productHelper
                        .getTaggingDetails("FCBND2");

                //TODO:

                // ArrayList<OrderDetail> mylist = mBusinessModel.reportHelper
                // .downloadFBOrderDetailForDayReport(productIds);

                ArrayList<OrderDetail> mylist = dayReportHelper
                        .downloadFBOrderDetailForDayReport(productIds);

                for (int i = 0; i < mylist.size(); i++) {
                    FB2value += mylist.get(i).getTotalAmount();

                }
                con.setMenuNumber(mBusinessModel.formatValue(FB2value) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT18")) {
                final float totalWeight = mBusinessModel.productHelper.getTotalWeight("");
                con.setMenuNumber(mBusinessModel.formatValue(totalWeight) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT19")) {
                con.setMenuNumber(mBusinessModel.formatValue((SDUtil.convertToDouble(outlet.getTotValues())) - SalesReturnHelper.getInstance(mContext).getTotalSalesReturnValue(mContext.getApplicationContext())));
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT20")) {
                final int totalOrderedQty = mBusinessModel.productHelper.getTotalOrderQty();
                con.setMenuNumber(totalOrderedQty + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT21")) {
                con.setMenuNumber(SDUtil.format(mBusinessModel.getFITscoreForAllRetailers(), 2, 0) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT22")) {
                con.setMenuNumber(SDUtil.format((mBusinessModel.getFITscoreForAllRetailers() / mBusinessModel.getTotalCallsForTheDay()), 2, 0) + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT23")) {

                con.setMenuNumber(mBusinessModel.getGreenFITscoreRetailersCount() + "/" + mBusinessModel.getTotalCallsForTheDay());
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT24")) {

                con.setMenuNumber(outlet.getNoofOrder() + "");
            } else if (con.getConfigCode().equalsIgnoreCase("DAYRT25")) {

                try {

                    StringBuilder sb = new StringBuilder();
                    String op = mContext.getResources().getString(R.string.item_piece);
                    String oc = mContext.getResources().getString(R.string.item_case);
                    String ou = mContext.getResources().getString(R.string.item_outer);
                    //  /**----- update label from label master table based on key value**/
                    if (mBusinessModel.labelsMasterHelper
                            .applyLabels("item_piece") != null)
                        op = mBusinessModel.labelsMasterHelper
                                .applyLabels("item_piece");
                    if (mBusinessModel.labelsMasterHelper
                            .applyLabels("item_case") != null)
                        oc = mBusinessModel.labelsMasterHelper
                                .applyLabels("item_case");

                    if (mBusinessModel.labelsMasterHelper
                            .applyLabels("item_outer") != null)
                        ou = mBusinessModel.labelsMasterHelper
                                .applyLabels("item_outer");

                    ///**-------end of the updated statement-------**/

                    if (mBusinessModel.configurationMasterHelper.SHOW_ORDER_PCS) {

                        sb.append((outlet.getPcsQty() == null ? 0 : outlet.getPcsQty()) + " " + op + " ");
                    }
                    if (mBusinessModel.configurationMasterHelper.SHOW_ORDER_CASE) {

                        if (mBusinessModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            String s = "\n" + (outlet.getCsQty() == null ? 0 : outlet.getCsQty()) + " "
                                    + oc + " ";
                            sb.append(s);
                        } else {
                            String s = (outlet.getCsQty() == null ? 0 : outlet.getCsQty()) + " "
                                    + oc + " ";
                            sb.append(s);
                        }
                    }
                    if (mBusinessModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                        if (mBusinessModel.configurationMasterHelper.SHOW_ORDER_PCS || mBusinessModel.configurationMasterHelper.SHOW_ORDER_CASE) {
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
            mBusinessModel.sbdMerchandisingHelper.setMerchTypeCodes();

            ArrayList<MerchandisingposmBO> MerchCov;
            MerchCov = mBusinessModel.sbdMerchandisingHelper.getSbdMerchCoverageBO();

            ArrayList<MerchandisingposmBO> merchList = new ArrayList<>();
            ArrayList<MerchandisingposmBO> merchInitList = new ArrayList<>();

            MerchandisingposmBO merch;
            for (int i = 0; i < MerchCov.size(); i++) {
                con = new ConfigureBO();
                merch = MerchCov.get(i);
                con.setMenuName(merch.getPosmdescription());
                con.setMenuNumber(merch.getAchieved() + "/" + merch.getTarget());
                if (mBusinessModel.sbdMerchandisingHelper.merchTypeListCode
                        .equals(merch.getTypeListId())) {
                    merchList.add(merch);
                } else if (mBusinessModel.sbdMerchandisingHelper.merchInitTypeListCode
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

        if (mBusinessModel.configurationMasterHelper.IS_SHOW_DROPSIZE) {
            for (ConfigureBO config : mBusinessModel
                    .downloadDailyReportDropSize(mBusinessModel.configurationMasterHelper.DROPSIZE_ORDER_TYPE)) {
                ConfigureBO con = new ConfigureBO();
                con.setMenuName(config.getMenuName() + " Drop Size");
                con.setMenuNumber(config.getMenuNumber());
                mDayList.add(con);
            }
        }


        MyAdapter myAdapter = new MyAdapter(mDayList, mBusinessModel);
        mIDayReportView.setAdapter(myAdapter);

    }

    @Override
    public void downloadData() {
        // loadData();

        mBusinessModel.sbdMerchandisingHelper.loadSbdMerchCoverage();
        mDayList = mBusinessModel.configurationMasterHelper.downloadDayReportList();
        if (mDayList != null && mDayList.size() > 0)
            updateDayReportData(mDayList);
    }


    /**
     * Get today beat object by searching the beatmaster vector.
     *
     * @return -  BeatMasterBO object
     */
    private BeatMasterBO getTodayBeat() {
        try {
            int size = mBusinessModel.beatMasterHealper.getBeatMaster().size();
            for (int i = 0; i < size; i++) {
                BeatMasterBO b = mBusinessModel.beatMasterHealper.getBeatMaster()
                        .get(i);
                if (b.getToday() == 1)
                    return b;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public byte[] printDataFor3InchPrinter() {
        byte[] printDataBytes = null;
        try {
            StringBuilder sb = new StringBuilder();
            //PrinterLanguage printerLanguage = PrinterLanguage.CPCL;
            // 00:22:58:3D:7E:83 - RW420
            // AC:3F:A4:16:B9:AE - IMZ320


            //TODO:if the below condition is always true so remove the conditions

            ArrayList<SubDepotBo> distributorList = mBusinessModel.vanmodulehelper.getSubDepotList();
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


            //TODO:if the below condition is always false so it's never execute.  remove the below codes
           /* else if (printerLanguage == PrinterLanguage.ZPL) {

                printDataBytes = sb.toString().getBytes();
            }*/
        } catch (Exception e) {
            Commons.printException(e);
        }
        return printDataBytes;
    }

    @Override
    public void destroy() {
        mBusinessModel = null;
    }
}