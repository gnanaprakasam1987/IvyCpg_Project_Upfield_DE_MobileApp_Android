package com.ivy.cpg.view.dashboard.olddashboard;

import android.content.Context;

import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.List;

public class DashBoardPresenterImpl implements DashboardContractor.DashboardPresenter {
    Context context;
    DashBoardHelper dashBoardHelper;
    BusinessModel businessModel;
    DashboardContractor.DashboardView dashboardView;
    private static final String DAY_TYPE = "DAY";
    private static final String JOURNEY_PLAN_CALL = "JPC";
    private static final String PRODUCTIVE_CALL = "PDC";
    private static final String MONTH_TYPE = "MONTH";
    private static final String YEAR_TYPE = "YEAR";
    private static final String QUARTER_TYPE = "QUARTER";
    private static final String ALL_TYPE = "ALL";

    DashBoardPresenterImpl(Context context) {
        this.context = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        dashBoardHelper = DashBoardHelper.getInstance(context);

    }

    @Override
    public void setView(DashboardContractor.DashboardView view) {
        dashboardView = view;
    }

    @Override
    public void loadDownloadMethods(String retailerId, String type) {
        dashBoardHelper.findMinMaxProductLevel(retailerId);
        dashBoardHelper.loadDashBoard(retailerId);

        if (type.equalsIgnoreCase(DAY_TYPE)) {
            dashBoardHelper.downloadDashboardLevelSkip(0);
            dashBoardHelper.downloadTotalValuesAndQty();
        }
        dashboardView.gridListDataLoad(0);

    }

    @Override
    public void updateProductiveAndPlanedCall() {
        for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {
            if (dashBoardBO.getType().equalsIgnoreCase(DAY_TYPE)) {
                if (dashBoardBO.getCode().equals(JOURNEY_PLAN_CALL)) {
                    final int totalcalls = dashBoardHelper.getTotalCallsForTheDay();
                    final int visitedcalls = dashBoardHelper.getVisitedCallsForTheDay();
                    dashBoardBO.setTarget(totalcalls);
                    dashBoardBO.setAcheived(visitedcalls);

                    if (dashBoardBO.getTarget() > 0) {
                        dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(SDUtil.roundIt((dashBoardBO.getAcheived() / dashBoardBO.getTarget() * 100), 2)));
                        if (dashBoardBO.getCalculatedPercentage() >= 100) {
                            dashBoardBO.setConvTargetPercentage(0);
                            dashBoardBO.setConvAcheivedPercentage(100);
                        } else {
                            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                    .getCalculatedPercentage());
                            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                    .getCalculatedPercentage());
                        }
                    } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                        dashBoardBO.setConvTargetPercentage(0);
                        dashBoardBO.setConvAcheivedPercentage(100);
                    }


                } else if (dashBoardBO.getCode().equals(PRODUCTIVE_CALL)) {
                    final int totalcalls = dashBoardHelper.getTotalCallsForTheDay();
                    double targetProductiveCalls = totalcalls * 0.25;
                    final int productivecalls = dashBoardHelper.getProductiveCallsForTheDay();
                    dashBoardBO.setTarget(targetProductiveCalls);
                    dashBoardBO.setAcheived(productivecalls);

                    if (dashBoardBO.getTarget() > 0) {
                        dashBoardBO.setCalculatedPercentage(SDUtil.convertToFloat(SDUtil.roundIt(((dashBoardBO.getAcheived() / dashBoardBO.getTarget()) * 100), 2)));
                        if (dashBoardBO.getCalculatedPercentage() >= 100) {
                            dashBoardBO.setConvTargetPercentage(0);
                            dashBoardBO.setConvAcheivedPercentage(100);
                        } else {
                            dashBoardBO.setConvTargetPercentage(100 - dashBoardBO
                                    .getCalculatedPercentage());
                            dashBoardBO.setConvAcheivedPercentage(dashBoardBO
                                    .getCalculatedPercentage());
                        }
                    } else if (dashBoardBO.getTarget() <= 0 && dashBoardBO.getAcheived() > 0) {
                        dashBoardBO.setConvTargetPercentage(0);
                        dashBoardBO.setConvAcheivedPercentage(100);
                    }
                }
            }
        }
        dashBoardHelper.downloadTotalValuesAndQty();
    }

    @Override
    public void computeDashboardList(String type, String subFilter) {
        if (dashBoardHelper.getDashListViewList() != null) {
            List<DashBoardBO> dashBoardList;
            dashBoardList = new ArrayList<>();
            if (type.equalsIgnoreCase(MONTH_TYPE)) {

                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {
                    if (dashBoardBO.getType().equals(MONTH_TYPE)) {
                        if (dashBoardBO.getMonthName() != null) {
                            if (dashBoardBO.getMonthName().equalsIgnoreCase(subFilter)) {
                                dashBoardList.add(dashBoardBO);
                            }
                        } else {
                            dashBoardList.add(dashBoardBO);
                        }
                    }

                }
            } else if (type.equalsIgnoreCase(YEAR_TYPE)) {
                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {

                    if (dashBoardBO.getType().equalsIgnoreCase(YEAR_TYPE)) {
                        dashBoardList.add(dashBoardBO);

                    }
                }

            } else if (type.equals(ALL_TYPE)) {
                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {
                    dashBoardList.add(dashBoardBO);
                }

            } else if (type.equals(DAY_TYPE)) {
                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {

                    if (dashBoardBO.getType().equalsIgnoreCase(DAY_TYPE)) {
                        dashBoardList.add(dashBoardBO);

                    }
                }

            } else if (type.equalsIgnoreCase(QUARTER_TYPE)) {
                String[] monthLimit = subFilter.split("-");
                int startingMonth = getMonthCount(monthLimit[0]);
                int endingMonth = getMonthCount(monthLimit[1]);

                for (DashBoardBO dashBoardBO : dashBoardHelper.getDashListViewList()) {
                    if (dashBoardBO.getType().equals(QUARTER_TYPE)) {
                        if (dashBoardBO.getMonthName() != null) {
                            if (getMonthCount(dashBoardBO.getMonthName()) >= startingMonth && getMonthCount(dashBoardBO.getMonthName()) <= endingMonth) {
                                dashBoardList.add(dashBoardBO);

                            }
                        } else {
                            dashBoardList.add(dashBoardBO);

                        }
                    }
                }

            }

            dashboardView.updateDashboardList(dashBoardList);
        }
    }

    private static int getMonthCount(String monthName) {
        switch (monthName) {
            case "January":
                return 1;
            case "February":
                return 2;
            case "March":
                return 3;
            case "April":
                return 4;
            case "May":
                return 5;
            case "June":
                return 6;
            case "July":
                return 7;
            case "August":
                return 8;
            case "September":
                return 9;
            case "October":
                return 10;
            case "November":
                return 11;
            case "December":
                return 12;
            default:
                return 1;
        }
    }

}
