package com.ivy.cpg.view.stockcheck;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

/**
 * Created by mansoor on 03/10/2018
 */
public class StockCheckHelper {

    private static StockCheckHelper instance = null;
    private final BusinessModel bmodel;

    public boolean SHOW_STOCK_WC;
    public boolean SHOW_STOCK_WP;
    public boolean SHOW_STOCK_SP;
    public boolean SHOW_STOCK_SC;
    public boolean SHOW_STOCK_CB;
    public boolean SHOW_STOCK_RSN;
    public boolean SHOW_WAREHOUSE_OUTER;
    public boolean SHOW_SHELF_OUTER;
    public boolean SHOW_STOCK_TOTAL;
    public boolean SHOW_STOCK_FC;
    public boolean CHANGE_AVAL_FLOW;

    private StockCheckHelper(Context context) {
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static StockCheckHelper getInstance(Context context) {
        if (instance == null) {
            instance = new StockCheckHelper(context);
        }
        return instance;
    }

    public void loadStockCheckConfiguration(Context context, int subChannelID) {


        SHOW_STOCK_WC = false;
        SHOW_STOCK_WP = false;
        SHOW_STOCK_SP = false;
        SHOW_STOCK_SC = false;
        SHOW_STOCK_CB = false;
        SHOW_STOCK_RSN = false;
        SHOW_WAREHOUSE_OUTER = false;
        SHOW_SHELF_OUTER = false;
        SHOW_STOCK_TOTAL = false;
        SHOW_STOCK_FC = false;
        CHANGE_AVAL_FLOW = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        String codeValue = null;
        String sql = "select RField from "
                + DataMembers.tbl_HhtModuleMaster
                + " where hhtCode='CSSTK01' and SubchannelId="
                + subChannelID;
        Cursor c = db.selectSQL(sql);
        if (c != null && c.getCount() != 0) {
            if (c.moveToNext()) {
                codeValue = c.getString(0);
            }
            c.close();
        } else {
            sql = "select RField from " + DataMembers.tbl_HhtModuleMaster
                    + " where hhtCode='CSSTK01' and SubChannelId= 0 and ForSwitchSeller = 0";
            c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                if (c.moveToNext()) {
                    codeValue = c.getString(0);
                }
                c.close();
            }

        }
        if (codeValue != null) {

            String codeSplit[] = codeValue.split(",");
            for (String temp : codeSplit)
                switch (temp) {
                    case "WC":
                        SHOW_STOCK_WC = true;
                        break;
                    case "WP":
                        SHOW_STOCK_WP = true;
                        break;
                    case "WO":
                        SHOW_WAREHOUSE_OUTER = true;
                        break;
                    case "SP":
                        SHOW_STOCK_SP = true;
                        break;
                    case "SC":
                        SHOW_STOCK_SC = true;
                        break;
                    case "SHO":
                        SHOW_SHELF_OUTER = true;
                        break;
                    case "CB":
                        SHOW_STOCK_CB = true;
                        break;
                    case "REASON":
                        SHOW_STOCK_RSN = true;
                        break;
                    case "TOTAL":
                        SHOW_STOCK_TOTAL = true;
                        break;
                    case "FC":
                        SHOW_STOCK_FC = true;
                        break;
                    case "CB01":
                        CHANGE_AVAL_FLOW = true;
                        break;
                }
        }


    }
}
