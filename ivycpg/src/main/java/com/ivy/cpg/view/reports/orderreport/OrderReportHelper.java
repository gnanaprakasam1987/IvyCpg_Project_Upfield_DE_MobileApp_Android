package com.ivy.cpg.view.reports.orderreport;


import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

public class OrderReportHelper {
    private Context mContext;
    private String userName, userPassword;

    private BusinessModel bmodel;

    public OrderReportHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel)mContext.getApplicationContext();
    }


    public int getavglinesfororderbooking(String tableName) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(linespercall) from "
                    + tableName);
            if (c != null) {
                if (c.moveToNext()) {
                    tot = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return tot;
    }

    public int getorderbookingCount(String tableName) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select count (distinct retailerid) from "
                    + tableName);
            if (c != null) {
                if (c.moveToNext()) {
                    tot = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }

        return tot;
    }

    public int getTotalQtyfororder(String orderId) {
        int tot = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select sum(qty) from OrderDetail where orderId='"
                    + orderId + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    tot = c.getInt(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return tot;
    }


    public double getTotValues(Context mContext) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select ifnull(sum(ordervalue),0) from "
                    + DataMembers.tbl_orderHeader);
            if (c != null) {
                if (c.moveToNext()) {
                    double i = c.getDouble(0);
                    c.close();
                    db.closeDB();
                    return i;
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return 0;
    }

    public void downloadOrderReportToExport() {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct DM.dname," +
                            "RM.retailerCode,RM.retailerName,OH.orderId,OH.orderDate,PM.pCode,PM.pName,OD.pieceQty" +
                            ",OD.caseQty,OD.outerQty,OH.deliveryDate,DM.email from orderHeader OH " +
                            "INNER JOIN orderDetail OD ON OH.orderId=OD.orderId " +
                            "LEFT JOIN ProductMaster PM ON PM.pid=OD.productId " +
                            "LEFT JOIN RetailerMaster RM ON RM.retailerId=OH.retailerId " +
                            "LEFT JOIN DistributorMaster DM ON DM.Did=OH.sid" +
                            " order by OH.sid");
            if (c != null) {
                mOrderDetailsByDistributorName = new HashMap<>();
                mEmailIdByDistributorName = new HashMap<>();
                String mPreviousDistributor = "";
                while (c.moveToNext()) {


                    if (!mPreviousDistributor.equals(c.getString(0))) {
                        // Preparing new list for every distributor
                        rows = new ArrayList<>();
                    }

                    ArrayList<String> row = new ArrayList<>();

                    row.add(c.getString(0));
                    row.add(bmodel.userMasterHelper.getUserMasterBO()
                            .getUserCode() + "");
                    row.add(bmodel.userMasterHelper.getUserMasterBO()
                            .getUserName());
                    row.add(c.getString(1));
                    row.add(c.getString(2));
                    row.add(c.getString(3));
                    row.add(c.getString(4));
                    row.add(c.getString(5));
                    row.add(c.getString(6));
                    if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                        row.add(c.getString(7));
                    if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                        row.add(c.getString(8));
                    if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        row.add(c.getString(9));
                    row.add(c.getString(10));


                    rows.add(row);

                    // distributor wise record
                    mOrderDetailsByDistributorName.put(c.getString(0), rows);

                    mEmailIdByDistributorName.put(c.getString(0), c.getString(11));

                    mPreviousDistributor = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public HashMap<String, ArrayList<ArrayList<String>>> getmOrderDetailsByDistributorName() {
        return mOrderDetailsByDistributorName;
    }


    private HashMap<String, ArrayList<ArrayList<String>>> mOrderDetailsByDistributorName;

    public HashMap<String, String> getmEmailIdByDistributorName() {
        return mEmailIdByDistributorName;
    }

    private HashMap<String, String> mEmailIdByDistributorName;

    public void downloadOrderEmailAccountCredentials() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String s = "SELECT ListName FROM StandardListMaster where listcode='ORDER_EMAIL' and listtype='ORDER_MAIL'";

            Cursor c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userName = c.getString(0);
                }
                c.close();
            }

            s = "SELECT ListName FROM StandardListMaster where listcode='ORDER_PWD' and listtype='ORDER_MAIL'";

            c = db.selectSQL(s);
            if (c != null) {
                if (c.moveToNext()) {
                    userPassword = c.getString(0);
                }
                c.close();
            }

            db.closeDB();
        } catch (SQLException e) {
            Commons.printException(e);
        }
    }

    public String getUserName() {
        return userName;
    }


    public String getUserPassword() {
        return userPassword;
    }


}
