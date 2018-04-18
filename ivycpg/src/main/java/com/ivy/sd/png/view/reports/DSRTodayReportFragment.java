package com.ivy.sd.png.view.reports;

import android.database.Cursor;
import android.database.SQLException;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.HashMap;

public class DSRTodayReportFragment extends Fragment {
    private BusinessModel bmodel;
    private View view;
    private TextView sales_mtd, sales_obj, sales_act, sales_index, calls_mtd,
            calls_obj, calls_act, calls_index;
    private TextView pc_mtd, pc_obj, pc_act, pc_index, gs_mtd, gs_obj, gs_act,
            gs_index, dist_mtd, dist_obj, dist_act, dist_index;
    private TextView merch_mtd, merch_obj, merch_act, merch_index, gp_mtd,
            gp_obj, gp_act, gp_index;
    private TextView tl_mtd, tl_obj, tl_act, tl_index, actstore_mtd,
            actstore_obj, actstore_act, actstore_index;
    private HashMap<String, Double> mtdgap;
    private int totalCalls, activeStoreCount;
    int sbdDistTargetAndAchieved[] = null, sbdMerchTargetAndAchieved[];

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Commons.print("oncreate dsr");
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_dsr_report, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        sales_mtd = (TextView) view.findViewById(R.id.sales_mtdgap);
        sales_obj = (TextView) view.findViewById(R.id.sales_obj);
        sales_act = (TextView) view.findViewById(R.id.sales_act);
        sales_index = (TextView) view.findViewById(R.id.sales_index);
        calls_mtd = (TextView) view.findViewById(R.id.calls_mtdgap);
        calls_obj = (TextView) view.findViewById(R.id.calls_obj);
        calls_act = (TextView) view.findViewById(R.id.calls_act);
        calls_index = (TextView) view.findViewById(R.id.calls_index);
        pc_mtd = (TextView) view.findViewById(R.id.pc_mtdgap);
        pc_obj = (TextView) view.findViewById(R.id.pc_obj);
        pc_act = (TextView) view.findViewById(R.id.pc_act);
        pc_index = (TextView) view.findViewById(R.id.pc_index);
        gs_mtd = (TextView) view.findViewById(R.id.gs_mtdgap);
        gs_obj = (TextView) view.findViewById(R.id.gs_obj);
        gs_act = (TextView) view.findViewById(R.id.gs_act);
        gs_index = (TextView) view.findViewById(R.id.gs_index);
        dist_mtd = (TextView) view.findViewById(R.id.dist_mtdgap);
        dist_obj = (TextView) view.findViewById(R.id.dist_obj);
        dist_act = (TextView) view.findViewById(R.id.dist_act);
        dist_index = (TextView) view.findViewById(R.id.dist_index);
        merch_mtd = (TextView) view.findViewById(R.id.merch_mtdgap);
        merch_obj = (TextView) view.findViewById(R.id.merch_obj);
        merch_act = (TextView) view.findViewById(R.id.merch_act);
        merch_index = (TextView) view.findViewById(R.id.merch_index);
        gp_mtd = (TextView) view.findViewById(R.id.godenpoint_mtdgap);
        gp_obj = (TextView) view.findViewById(R.id.goldenpoint_obj);
        gp_act = (TextView) view.findViewById(R.id.goldenpoint_act);
        gp_index = (TextView) view.findViewById(R.id.goldenpoint_index);
        tl_mtd = (TextView) view.findViewById(R.id.totlines_mtdgap);
        tl_obj = (TextView) view.findViewById(R.id.totlines_obj);
        tl_act = (TextView) view.findViewById(R.id.totlines_act);
        tl_index = (TextView) view.findViewById(R.id.totlines_index);
        actstore_mtd = (TextView) view.findViewById(R.id.act_stores_mtdgap);
        actstore_obj = (TextView) view.findViewById(R.id.act_stores_obj);
        actstore_act = (TextView) view.findViewById(R.id.act_stores_act);
        actstore_index = (TextView) view.findViewById(R.id.act_stores_index);

        try {
            downloadMTDGap();

            sbdDistTargetAndAchieved = bmodel.getSDBDistTargteAndAcheived();
            sbdMerchTargetAndAchieved = bmodel.getSDBMerchTargteAndAcheived();

            /** Set values for Sales **/
            sales_mtd.setText(mtdgap.get("SV") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("SV")) + ""); // As per Laxmanan
            // DSR_SALES cahnged
            // into SV
            sales_obj
                    .setText(bmodel.formatValue(bmodel.getSumOfPlannedTarget()));
            sales_act.setText(bmodel.formatValue(bmodel.getAcheived()));
            double index = ((bmodel.getAcheived() == 0) ? 0 : ((bmodel
                    .getSumOfPlannedTarget() == 0) ? 0 : (bmodel.getAcheived()
                    / bmodel.getSumOfPlannedTarget() * 100)));

            sales_index.setText(bmodel.formatPercent(index));

            /** set values for calls */
            calls_mtd.setText(mtdgap.get("DSR_CALL") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("DSR_CALL")) + "");
            calls_obj.setText(totalCalls + "");
            calls_act.setText(bmodel.getVisitedCallsForTheDay() + "");
            double index1 = ((bmodel.getVisitedCallsForTheDay() == 0) ? 0
                    : ((totalCalls == 0) ? 0 : ((double) bmodel
                    .getVisitedCallsForTheDay() / totalCalls * 100)));
            calls_index.setText(bmodel.formatPercent(index1));

            /** set values for productive calls **/
            pc_mtd.setText(mtdgap.get("DSR_PC") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("DSR_PC")) + "");

            /*** Productive Calls Needed New For CR ************/
            float productiveCallsObj_PH;
            productiveCallsObj_PH = bmodel.getTotalCallsForTheDay()
                    * bmodel.configurationMasterHelper
                    .getProductiveCallPercentage() / 100;

            int productiveCallsObj_PH_round = (int) Math
                    .round(productiveCallsObj_PH);
            if (productiveCallsObj_PH > productiveCallsObj_PH_round) {
                productiveCallsObj_PH = productiveCallsObj_PH + 1;
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCTIVE_CALLS_OBJ_PH) {
                if (productiveCallsObj_PH != 0) {
                    index1 = ((bmodel.getProductiveCallsForTheDay() == 0) ? 0
                            : ((productiveCallsObj_PH == 0) ? 0
                            : ((double) bmodel
                            .getProductiveCallsForTheDay()
                            / productiveCallsObj_PH * 100)));
                    pc_index.setText(bmodel.formatPercent(index1));
                } else
                    pc_index.setText("0");
            } else {
                if (bmodel.getVisitedCallsForTheDay() != 0) {
                    if (bmodel.getVisitedCallsForTheDay() != 0) {
                        index1 = ((bmodel.getProductiveCallsForTheDay() == 0) ? 0
                                : ((bmodel.getVisitedCallsForTheDay() == 0) ? 0
                                : ((double) bmodel
                                .getProductiveCallsForTheDay()
                                / totalCalls * 100)));
                        pc_index.setText(bmodel.formatPercent(index1));
                    } else
                        pc_index.setText("0");
                }
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCTIVE_CALLS_OBJ_PH) {
                pc_obj.setText(((int) productiveCallsObj_PH + ""));
            } else {
                pc_obj.setText(totalCalls + "");
            }
            pc_act.setText(bmodel.getProductiveCallsForTheDay() + "");

            /** set values for Gold store **/
            gs_mtd.setText(mtdgap.get("DSR_GOLDSTORE") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("DSR_GOLDSTORE")) + "");
            String[] goldstore = null;
            gs_obj.setText(totalCalls + "");
            gs_act.setText(goldstore[0]);
            if (goldstore.length == 0 || goldstore[0] == null
                    || goldstore[0].length() == 0) {
                gs_index.setText(bmodel.formatPercent(0));
            } else {
                index1 = ((SDUtil.convertToInt(goldstore[0]) == 0) ? 0
                        : ((totalCalls == 0) ? 0 : ((double) SDUtil
                        .convertToInt(goldstore[0]) / totalCalls * 100)));
                gs_index.setText(bmodel.formatPercent(index1));
            }
            /** set values for SBD Dist **/
            dist_mtd.setText(mtdgap.get("DSR_DIST") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("DSR_DIST")) + "");
            dist_obj.setText(totalCalls + "");
            dist_act.setText(sbdDistTargetAndAchieved[0] + "");
            index1 = ((sbdDistTargetAndAchieved[0] == 0) ? 0
                    : ((totalCalls == 0) ? 0
                    : ((double) sbdDistTargetAndAchieved[0]
                    / totalCalls * 100)));
            dist_index.setText(bmodel.formatPercent(index1));

            /** set values for SBD Merch **/
            merch_mtd.setText(mtdgap.get("DSR_MERCH") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("DSR_MERCH")) + "");
            merch_obj.setText(totalCalls + "");
            merch_act.setText(sbdMerchTargetAndAchieved[0] + "");
            index1 = ((sbdMerchTargetAndAchieved[0] == 0) ? 0
                    : ((totalCalls == 0) ? 0
                    : ((double) sbdMerchTargetAndAchieved[0]
                    / totalCalls * 100)));
            merch_index.setText(bmodel.formatPercent(index1));

            /** set values for Golden Points **/
            gp_mtd.setText(mtdgap.get("DSR_GOLDPOINTS") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("DSR_GOLDPOINTS")) + "");
            int[] gptargetandachieve = bmodel.getGoldenPoints();
            gp_obj.setText(gptargetandachieve[1] + "");
            gp_act.setText(gptargetandachieve[0] + "");
            index1 = ((gptargetandachieve[0] == 0) ? 0
                    : ((gptargetandachieve[1] == 0) ? 0
                    : ((double) gptargetandachieve[0]
                    / gptargetandachieve[1] * 100)));
            gp_index.setText(bmodel.formatPercent(index1));

            /** set values for total lines **/
            tl_mtd.setText(mtdgap.get("TLS") == null ? "0" : bmodel
                    .formatValue(mtdgap.get("TLS")) + "");

            tl_obj.setText(getTotalLinesTargetForToday() + "");
            tl_act.setText(bmodel.reportHelper
                    .getavglinesfororderbooking("OrderHeader") + "");
            if (getTotalLinesTargetForToday() != 0) {
                index1 = ((bmodel.reportHelper
                        .getavglinesfororderbooking("OrderHeader") == 0) ? 0
                        : ((getTotalLinesTargetForToday() == 0) ? 0
                        : ((double) bmodel.reportHelper
                        .getavglinesfororderbooking("OrderHeader")
                        / getTotalLinesTargetForToday() * 100)));
                tl_index.setText(bmodel.formatPercent(index1) + "");
            } else
                tl_index.setText("0");
            /** set values for active stores **/
            actstore_mtd.setText(mtdgap.get("DSR_ACTSTORES") == null ? "0"
                    : mtdgap.get("DSR_ACTSTORES") + "");
            actstore_obj.setText(totalCalls + "");
            actstore_act.setText(downloadActivestoreAchieved() + "");
            index1 = ((downloadActivestoreAchieved() == 0) ? 0
                    : ((totalCalls == 0) ? 0
                    : ((double) downloadActivestoreAchieved()
                    / totalCalls * 100)));
            actstore_index.setText(bmodel.formatPercent(index1) + "");
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }

        return view;
    }

    private int downloadActivestoreAchieved() {

        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select  (select count(o.retailerid)  from OrderHeader o inner join RetailerMaster r on o.retailerid=r.retailerid "
                            + " inner join Retailermasterinfo RMI on Rmi.retailerid= R.retailerid"
                            + " where  ((RMI.istoday=1 and r.isDeadStore = 'Y') or (RBM.isdeviated='Y' and r.isDeadStore = 'Y')))"
                            + ",(select count(R.retailerid) from Retailermaster R"
                            + " inner join Retailermasterinfo RMI on Rmi.retailerid= R.retailerid"
                            + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = R.RetailerID"
                            + " where o.upload!='X' and ((RMI.istoday=1 and isDeadStore = 'N') or (RBM.isdeviated='Y'  and isDeadStore = 'N')))");

            if (c != null) {
                while (c.moveToNext()) {
                    return (c.getInt(0) + c.getInt(1));
                }
            }

            c.close();
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
        return 0;
    }

    private void downloadMTDGap() {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select  distinct RM.RetailerID   from RetailerMaster RM "
                            + " inner join Retailermasterinfo RMI on Rmi.retailerid= RM.retailerid"
                            + " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                            + " where (RBM.isdeviated='Y' or RMI.isToday='1' )");
            if (c != null) {
                while (c.moveToNext()) {
                    totalCalls = c.getCount();
                }
            }
            c.close();
            activeStoreCount = 0;
            Cursor c2 = db
                    .selectSQL("select  distinct RM.RetailerID   from RetailerMaster RM"
                            + " inner join Retailermasterinfo RMI on Rmi.retailerid= RM.retailerid "
                            + "LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID"
                            + " where (RBM.isdeviated='Y' or RMI.isToday='1' and isDeadStore = 'N' )");
            if (c2 != null) {
                while (c2.moveToNext()) {
                    activeStoreCount = c2.getCount();
                }
            }
            c2.close();

            Cursor c1 = db
                    .selectSQL("select code,mtdgap from DashboardMaster where type='DAY'");
            if (c1 != null) {
                mtdgap = new HashMap<String, Double>();
                while (c1.moveToNext()) {
                    mtdgap.put(c1.getString(0), c1.getDouble(1));
                }
            }
            c1.close();
            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    public int getTotalLinesTargetForToday() {
        try {
            DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select sum(Lines) from DTPMaster where retailerID in (select RM.RetailerID from RetailerMaster RM inner join Retailermasterinfo RMI on Rmi.retailerid= RM.retailerid " +
                            " LEFT JOIN RetailerBeatMapping RBM ON RBM.RetailerID = RM.RetailerID" +
                            " where RMI.isToday=1 or RBM.isDeviated='Y')");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    int count = c.getInt(0);
                    return count;
                }
            }
            c.close();
            db.closeDB();
            return 0;
        } catch (Exception e) {
            Commons.printException(e);
            return 0;
        }
    }
}
