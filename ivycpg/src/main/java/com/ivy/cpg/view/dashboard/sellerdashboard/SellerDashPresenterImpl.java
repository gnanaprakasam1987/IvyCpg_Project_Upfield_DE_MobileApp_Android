package com.ivy.cpg.view.dashboard.sellerdashboard;

import android.content.Context;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.dashboard.olddashboard.DashboardContractor;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DailyReportBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.KeyPairBoolData;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;

public class SellerDashPresenterImpl implements SellerDashboardContractor.SellerDashPresenter {
    Context context;
    DashBoardHelper dashBoardHelper;
    BusinessModel businessModel;
    SellerDashboardContractor.SellerDashView dashboardView;

    private static final String CODE1 = "VAL";
    private static final String CODE2 = "VIP";
    private static final String CODE3 = "TLS";
    private static final String CODE4 = "PDC";
    private static final String CODE5 = "MSP";
    private static final String CODE6 = "COV";
    private static final String CODE7 = "PRM";
    private static final String CODE8 = "MSL";
    private static final String CODE9 = "TRN";
    private static final String CODE10 = "AUB";
    private static final String CODE11 = "ASP";
    private static final String CODE12 = "ABV";
    private static final String CODE13 = "INV";
    private static final String CODE_EFF_VISIT = "EFV";
    private static final String CODE_EFF_SALE = "EFS";
    private static final String CODE_DROP_SIZE_INV = "DSZ_INVOICE";
    private static final String CODE_DROP_SIZE_ORD = "DSZ_ORDER";
    private static final String CODE_SALES_VS_WEEKLY_OBJ = "SWO";
    private static final String CODE_INIT_VS_WEEKLY_OBJ = "IWO";
    private static final String CODE_RETURN_RATE_INV = "RRA_INVOICE";
    private static final String CODE_RETURN_RATE_ORD = "RRA_ORDER";
    private static final String CODE_FULLFILLMENT = "FULL_FILL";


    SellerDashPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        dashBoardHelper = DashBoardHelper.getInstance(context);
    }

    @Override
    public void setView(SellerDashboardContractor.SellerDashView view) {
        dashboardView = view;
    }

    @Override
    public void gridListDataLoad(int position) {
        if (position == 0) {
            dashBoardHelper.getGridData(0);

        } else {
            dashBoardHelper.getGridData((String) dashBoardHelper
                    .getBeatList().get(position));
        }
    }

    @Override
    public void updateUserData(String distrubutorIds) {
        ArrayList<UserMasterBO> users;
        final List<KeyPairBoolData> userArray = new ArrayList<>();
        String mFilterUser = "";
        if (distrubutorIds.equals("0"))
            users = dashBoardHelper.downloadUserList();
        else
            users = businessModel.userMasterHelper.downloadUserList(distrubutorIds);

        userArray.add(new KeyPairBoolData(0, context.getResources().getString(R.string.all), true));
        int count = 0;
        for (int i = 0; i < users.size(); i++) {
            KeyPairBoolData h = new KeyPairBoolData();
            h.setId(users.get(i).getUserid());
            h.setName(users.get(i).getUserName());
            h.setSelected(true);
            userArray.add(h);
            count++;
            mFilterUser += businessModel.QT(users.get(i).getUserid() + "");
            if (count != users.size())
                mFilterUser += ",";
        }

        dashboardView.loadUserSpinner(mFilterUser,userArray);
    }

    @Override
    public void computeDayAchivements() {
        DailyReportBO outlet = businessModel.getDailyRep();
        int totalcalls = businessModel.getTotalCallsForTheDayExcludingDeviatedVisits();
        //in getNoOfInvoiceAndValue getTotValues refers sum of invoice amt and getTotLines refers num of invoice
        DailyReportBO dailrp = businessModel.getNoOfInvoiceAndValue();
        DailyReportBO dailyrp_order = businessModel.getNoOfOrderAndValue();

        for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {
            if (dashBoardBO.getCode().equalsIgnoreCase(CODE9) | dashBoardBO.getCode().equalsIgnoreCase(CODE10) || dashBoardBO.getCode().equalsIgnoreCase(CODE11) ||
                    dashBoardBO.getCode().equalsIgnoreCase(CODE12)) {
                dashBoardHelper.getCounterSalesDetail();
            }
        }
        for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {
            if (dashBoardBO.getCode().equalsIgnoreCase(CODE1)) {
                dashBoardBO.setKpiAcheived(outlet.getTotValues());

                int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getTotValues());
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE2)) {
                dashBoardBO.setKpiAcheived(outlet.getEffCoverage());
                int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getEffCoverage());
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE3)) {
                dashBoardBO.setKpiAcheived(outlet.getTotLines());
                int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getTotLines());
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE4)) {
                int productivecalls = businessModel.getProductiveCallsForTheDay();

                dashBoardBO.setKpiAcheived(Integer.toString(productivecalls));
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((productivecalls * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE5)) {
                dashBoardBO.setKpiAcheived(outlet.getMspValues());
                int kpiAcheived = (int) SDUtil.convertToDouble(outlet.getMspValues());
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }

                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE6)) {
                int plannedRetailerCount = getRetailerDetail("P");
                int plannedRetailerVisitCount = getRetailerDetail("V");

                dashBoardBO.setKpiAcheived(plannedRetailerVisitCount + "");
                int kpiAcheived = plannedRetailerVisitCount;
                int kpiTarget;

                try {
                    kpiTarget = (plannedRetailerCount);
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }

                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }


            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE7)) {
                int promotionCount = dashBoardHelper.getPromotionDetail("P");
                int promotionAchievedCount = dashBoardHelper.getPromotionDetail("V");

                dashBoardBO.setKpiAcheived(promotionAchievedCount + "");
                int kpiAcheived = promotionAchievedCount;
                int kpiTarget;

                try {
                    kpiTarget = (promotionCount);
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }

                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE8)) {
                int mslCount = dashBoardHelper.getMSLDetail("P");
                int mslAchievedCount = dashBoardHelper.getMSLDetail("V");

                dashBoardBO.setKpiAcheived(mslAchievedCount + "");
                int kpiAcheived = mslAchievedCount;
                int kpiTarget;

                try {
                    kpiTarget = (mslCount);
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((kpiAcheived * 100) / kpiTarget);
                }

                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE9)) {


                dashBoardBO.setKpiAcheived(Integer.toString(dashBoardHelper.transactionPerDay));
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((dashBoardHelper.transactionPerDay * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE10)) {


                dashBoardBO.setKpiAcheived(Integer.toString(dashBoardHelper.avgUnitsPerBill));
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((dashBoardHelper.avgUnitsPerBill * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE11)) {


                dashBoardBO.setKpiAcheived(Integer.toString(dashBoardHelper.avgSellingPrice));
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((dashBoardHelper.avgSellingPrice * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE12)) {


                dashBoardBO.setKpiAcheived(Integer.toString(dashBoardHelper.avgBillValue));
                int kpiTarget;

                try {
                    kpiTarget = (int) Double.parseDouble(dashBoardBO.getKpiTarget());
                } catch (Exception e) {
                    kpiTarget = 0;
                    Commons.printException(e + "");
                }

                if (kpiTarget == 0) {
                    dashBoardBO.setCalculatedPercentage(0);
                } else {
                    dashBoardBO.setCalculatedPercentage((dashBoardHelper.avgBillValue * 100) / kpiTarget);
                }
                if (dashBoardBO.getCalculatedPercentage() >= 100) {
                    dashBoardBO.setConvTargetPercentage(0);
                    dashBoardBO.setConvAcheivedPercentage(100);
                } else {
                    dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                            .getCalculatedPercentage());
                    dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                            .getCalculatedPercentage());
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_EFF_VISIT)) {
                int visitedcalls = businessModel.getVisitedCallsForTheDayExcludingDeviatedVisits();
                if (totalcalls == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived(((visitedcalls / totalcalls) * 100) + "");
                }

            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_EFF_SALE)) {
                int productivecalls = businessModel.getProductiveCallsForTheDayExcludingDeviatedVisits();
                if (totalcalls == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived(((productivecalls / totalcalls) * 100) + "");
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_DROP_SIZE_INV)) {
                if (SDUtil.convertToDouble(dailrp.getTotLines()) == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(dailrp.getTotValues()) / SDUtil.convertToDouble(dailrp.getTotLines())) + "");
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_DROP_SIZE_ORD)) {
                if (SDUtil.convertToDouble(dailyrp_order.getTotLines()) == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(dailyrp_order.getTotValues()) / SDUtil.convertToDouble(dailyrp_order.getTotLines())) + "");
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_SALES_VS_WEEKLY_OBJ)) {
                dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(dailrp.getTotValues())) + "");
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_INIT_VS_WEEKLY_OBJ)) {
                //in getFocusBrandInvoiceAmt getTotValues refers sum of invoice amt of focus brands
                DailyReportBO dailrp_focus_brand = businessModel.getFocusBrandInvoiceAmt();
                dashBoardBO.setKpiAcheived((SDUtil.convertToDouble(dailrp_focus_brand.getTotValues())) + "");
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_RETURN_RATE_INV)) {
                double sales_ret_val = businessModel.getSalesReturnValue();
                if (SDUtil.convertToDouble(dailrp.getTotLines()) == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived(((sales_ret_val / SDUtil.convertToDouble(dailrp.getTotValues())) * 100) + "");
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_RETURN_RATE_ORD)) {
                double sales_ret_val = businessModel.getSalesReturnValue();
                if (SDUtil.convertToDouble(dailyrp_order.getTotValues()) == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived(((sales_ret_val / SDUtil.convertToDouble(dailyrp_order.getTotValues())) * 100) + "");
                }
            } else if (dashBoardBO.getCode().equalsIgnoreCase(CODE_FULLFILLMENT)) {
                DailyReportBO dailyReportBO = businessModel.getFullFillmentValue();
                if (dailyReportBO.getLoaded() == 0) {
                    dashBoardBO.setKpiAcheived("0");
                } else {
                    dashBoardBO.setKpiAcheived(((dailyReportBO.getDelivered() / dailyReportBO.getLoaded()) * 100) + "");
                }
            }

        }
    }
    private int getRetailerDetail(String flag) {
        int size = businessModel.getRetailerMaster().size();
        int count = 0;
        /** Add today's retailers. **/
        if (flag.equals("P")) {
            for (int i = 0; i < size; i++) {
                if (businessModel.getRetailerMaster().get(i).getIsToday() == 1) {
                    count++;
                }

            }
        } else {
            for (int i = 0; i < size; i++) {
                if (businessModel.getRetailerMaster().get(i).getIsVisited().equals("Y")) {
                    count++;
                }
            }
        }

        return count;

    }


}
