package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.FitScoreBO;
import com.ivy.sd.png.bo.FitScoreChartBO;
import com.ivy.sd.png.bo.HHTModuleBO;
import com.ivy.sd.png.bo.ProductTaggingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by anandasir.v on 9/27/2017.
 */

public class FitScoreHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static FitScoreHelper instance = null;
    private ArrayList<ProductTaggingBO> weightageList = new ArrayList<>();
    private ArrayList<FitScoreBO> fitScoreList = new ArrayList<>();
    private ArrayList<HHTModuleBO> hhtModuleList = new ArrayList<>();

    private ArrayList<FitScoreChartBO> fitScoreChartList = new ArrayList<>();

    protected FitScoreHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static FitScoreHelper getInstance(Context context) {
        if (instance == null) {
            instance = new FitScoreHelper(context);
        }
        return instance;
    }

    public ArrayList<ProductTaggingBO> getWeightageList() {
        return weightageList;
    }

    public void setWeightageList(ArrayList<ProductTaggingBO> weightageList) {
        this.weightageList = weightageList;
    }

    public ArrayList<FitScoreBO> getFitScoreList() {
        return fitScoreList;
    }

    public ArrayList<FitScoreChartBO> getFitScoreChartList() {
        return fitScoreChartList;
    }

    public void setFitScoreChartList(ArrayList<FitScoreChartBO> fitScoreChartList) {
        this.fitScoreChartList = fitScoreChartList;
    }

    public void setFitScoreList(ArrayList<FitScoreBO> fitScoreList) {
        this.fitScoreList = fitScoreList;
    }

    public ArrayList<HHTModuleBO> getHhtModuleList() {
        return hhtModuleList;
    }

    public void setHhtModuleList(ArrayList<HHTModuleBO> hhtModuleList) {
        this.hhtModuleList = hhtModuleList;
    }

    public void getModules() {

        HHTModuleBO hhtModuleBO;
        hhtModuleList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select distinct Module from HHTModuleWeightage"); //MENU_STK_ORD
            if (c != null) {
                while (c.moveToNext()) {
                    hhtModuleBO = new HHTModuleBO();
                    hhtModuleBO.setModule(c.getString(0));
                    hhtModuleList.add(hhtModuleBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setHhtModuleList(hhtModuleList);
    }

    public void getWeightage(String criteriaID, String Module) {

        ProductTaggingBO productTaggingBO;
        weightageList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select A.HeaderID, B.ProductID,B.FromNorm,B.Weightage from WeightageHeader A  " +
                            "inner join WeightageProductDetail B on A.HeaderID  = B.HeaderID " +
                            "where A.CriteriaID = " + criteriaID + " and A.Module ='" + Module + "' and CriteriaType = 'RETAILER'"); //MENU_STK_ORD
            if (c != null) {
                while (c.moveToNext()) {
                    productTaggingBO = new ProductTaggingBO();
                    productTaggingBO.setHeaderID(c.getString(0));
                    productTaggingBO.setPid(c.getString(1));
                    productTaggingBO.setFromNorm(c.getInt(2));
                    productTaggingBO.setWeightage(c.getInt(3));
                    weightageList.add(productTaggingBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setWeightageList(weightageList);
        if (weightageList.size() == 0) {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            int mContentLevelId = bmodel.productHelper.getContentLevel(db, Module);
            ProductTaggingHelper.getInstance(mContext).getTaggedProductIds(mContext,Module, mContentLevelId);
            setWeightageList(ProductTaggingHelper.getInstance(mContext).getProductTaggingList());
            if(!db.isDbNullOrClosed())
                db.closeDB();
        }
    }

    public int getModuleWeightage(String Module) {
        int weightage = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT Weightage FROM HHTModuleWeightage A " +
                            "INNER JOIN HHTModuleWeightageMapping B " +
                            "ON A.GroupID = B.GroupID LEFT JOIN RetailerMaster RM " +
                            "ON B.CriteriaId IN (RM.subchannelid, RM.Channelid) " +
                            "AND B.CriteriaType = 'CHANNEL' " +
                            "LEFT JOIN RetailerAttribute RA " +
                            "ON (RA.AttributeId = B.CriteriaId AND B.CriteriaType = 'RTR_ATTRIBUTE') " +
                            "WHERE A.Module = '" + Module + "' and " +
                            "(RM.RetailerID = '" + bmodel.getRetailerMasterBO().getRetailerID() + "' or " +
                            "RA.RetailerID = '" + bmodel.getRetailerMasterBO().getRetailerID() + "') LIMIT 1");
            if (c != null) {
                while (c.moveToNext()) {
                    weightage = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return weightage;
    }

    public int checkWeightage(String ProductID, int Qty) {
        try {
            for (ProductTaggingBO weightage : getWeightageList()) {
                if (weightage.getFromNorm() > 0)
                    if (ProductID.equals(String.valueOf(weightage.getPid())) && Qty >= weightage.getFromNorm()) {
                        return weightage.getWeightage();
                    }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return 0;
    }

    public int checkWeightage(String ProductID) {
        try {
            for (ProductTaggingBO weightage : getWeightageList()) {
                if (ProductID.equals(String.valueOf(weightage.getPid()))) {
                    return weightage.getWeightage();
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return 0;
    }

    public void getFitScoreforStockandPriceCheck(String retailerID, String Module) {
        fitScoreList = new ArrayList<>();
        FitScoreBO weightageBO;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String Qry = "", PID = "";
            if (Module.equals(DataMembers.FIT_STOCK)) {
                Qry = "inner join ClosingStockDetail B on A.Pid = B.ProductID ";
                PID = "ProductID";
            } else if (Module.equals(DataMembers.FIT_PRICE)) {
                Qry = "inner join PriceCheckDetail B on A.Pid = B.PID ";
                PID = "PID";
            }
            Cursor c = db
                    .selectSQL("Select distinct A.PName,'',case when (ifnull(D.FromNorm,0)<ifnull(B.Score,0)) then 'Y' else 'N' end as Achieved,'','' from ProductMaster A " +
                            Qry +
                            "inner join WeightageHeader C on C.CriteriaID = B.RetailerID " +
                            "inner join WeightageProductDetail D on C.HeaderID = D.HeaderID and D.ProductID = B." + PID + " " +
                            "inner join HHTModuleWeightage E on E.Module = C.Module " +
                            "inner join HHTModuleWeightageMapping HWM ON E.GroupID = HWM.GroupID " +
                            "left join RetailerMaster RM ON HWM.CriteriaId IN (RM.subchannelid, RM.Channelid) AND HWM.CriteriaType = 'CHANNEL' " +
                            "left join RetailerAttribute RA ON (RA.AttributeId = HWM.CriteriaId AND HWM.CriteriaType = 'RTR_ATTRIBUTE') " +
                            "where C.CriteriaID = '" + retailerID + "' and E.Module = '" + Module + "' " +
                            "AND B.Score>0 and (RM.RetailerID = '" + retailerID + "' or RA.RetailerID = '" + retailerID + "')");
            if (c != null) {
                while (c.moveToNext()) {
                    weightageBO = new FitScoreBO();
                    weightageBO.setHeader(c.getString(0));
                    weightageBO.setTarget(c.getString(1));
                    weightageBO.setAchieved(c.getString(2));
                    weightageBO.setWeightage(c.getString(3));
                    weightageBO.setScore(c.getString(4));
                    fitScoreList.add(weightageBO);
                }
            }
            c.close();
            setFitScoreList(fitScoreList);
            if (fitScoreList.size() == 0) {
                getFitScoreforStockandPriceCheckusingProductTagging(retailerID, Module);
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void getFitScoreforStockandPriceCheckusingProductTagging(String retailerID, String Module) {
        fitScoreList = new ArrayList<>();
        FitScoreBO weightageBO;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String Qry = "", PID = "";
            if (Module.equals(DataMembers.FIT_STOCK)) {
                Qry = "inner join ClosingStockDetail B on A.Pid = B.ProductID ";
                PID = "ProductID";
            } else if (Module.equals(DataMembers.FIT_PRICE)) {
                Qry = "inner join PriceCheckDetail B on A.Pid = B.PID ";
                PID = "PID";
            }
            Cursor c = db
                    .selectSQL("Select distinct A.PName,'',case when (ifnull(E.FromNorm,0)<ifnull(B.Score,0)) then 'Y' else 'N' end as achieved,'','' from ProductMaster A " +
                            Qry +
                            "inner join StandardListMaster F on F.ListID = D.TaggingTypelovID " +
                            "inner join ProductTaggingMaster D ON D.TaggingTypelovID  =F.ListID " +
                            "inner join ProductTaggingGroupMapping E ON E.groupid=D.groupid and E.PID = B." + PID + " " +
                            "inner join HHTModuleWeightage G on G.Module = F.ListCode " +
                            "inner join HHTModuleWeightageMapping HWM ON G.GroupID = HWM.GroupID " +
                            "left join RetailerMaster RM ON HWM.CriteriaId IN (RM.subchannelid, RM.Channelid) AND HWM.CriteriaType = 'CHANNEL' " +
                            "left join RetailerAttribute RA ON (RA.AttributeId = HWM.CriteriaId AND HWM.CriteriaType = 'RTR_ATTRIBUTE') " +
                            "WHERE G.Module = '" + Module + "' AND B.retailerid=" + retailerID +
                            " AND B.Score>0 and (RM.RetailerID = '" + retailerID + "' or RA.RetailerID = '" + retailerID + "')");

            if (c != null) {
                while (c.moveToNext()) {
                    weightageBO = new FitScoreBO();
                    weightageBO.setHeader(c.getString(0));
                    weightageBO.setTarget(c.getString(1));
                    weightageBO.setAchieved(c.getString(2));
                    weightageBO.setWeightage(c.getString(3));
                    weightageBO.setScore(c.getString(4));
                    fitScoreList.add(weightageBO);
                }
            }
            c.close();
            setFitScoreList(fitScoreList);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void getFitScoreforAssetandPOSM(String retailerID, String Module) {
        fitScoreList = new ArrayList<>();
        FitScoreBO weightageBO;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String ListCode = "";
            if (Module.equals(DataMembers.FIT_ASSET)) {
                ListCode = "MERCH";
            } else {
                ListCode = "MERCH_INIT";
            }
            Cursor c = db
                    .selectSQL("Select distinct A.PosmDesc,0,case when (ifnull(B.Score,0)>0) then 'Y' else 'N' end,'','' from PosmMaster A " +
                            "inner join AssetDetail B on A.Posmid = B.AssetID " +
                            "inner join AssetHeader C on C.Uid = B.UID " +
                            "inner join StandardListMaster D on D.ListId = C.TypeLovID " +
                            "inner join HHTModuleWeightage E on E.Module =  '" + Module + "' " +
                            "inner join HHTModuleWeightageMapping HWM ON E.GroupID = HWM.GroupID " +
                            "left join RetailerMaster RM ON HWM.CriteriaId IN (RM.subchannelid, RM.Channelid) AND HWM.CriteriaType = 'CHANNEL' " +
                            "left join RetailerAttribute RA ON (RA.AttributeId = HWM.CriteriaId AND HWM.CriteriaType = 'RTR_ATTRIBUTE') " +
                            "where B.RetailerID = '" + retailerID + "' and D.ListCode = '" + ListCode + "' " +
                            "AND B.Score>0 and (RM.RetailerID = '" + retailerID + "' or RA.RetailerID = '" + retailerID + "')");
            if (c != null) {
                while (c.moveToNext()) {
                    weightageBO = new FitScoreBO();
                    weightageBO.setHeader(c.getString(0));
                    weightageBO.setTarget(c.getString(1));
                    weightageBO.setAchieved(c.getString(2));
                    weightageBO.setWeightage(c.getString(3));
                    weightageBO.setScore(c.getString(4));
                    fitScoreList.add(weightageBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setFitScoreList(fitScoreList);
    }

    public void getFitScoreforPromo(String retailerID, String Module) {
        fitScoreList = new ArrayList<>();
        FitScoreBO weightageBO;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select distinct A.PromoName,0,case when (ifnull(B.Score,0)>0) then 'Y' else 'N' end,'','' " +
                            "from PromotionProductMapping A inner join PromotionDetail B on A.PromoId = B.PromotionID " +
                            "inner join HHTModuleWeightage E on E.Module =  '" + Module + "' " +
                            "inner join HHTModuleWeightageMapping HWM ON E.GroupID = HWM.GroupID " +
                            "left join RetailerMaster RM ON HWM.CriteriaId IN (RM.subchannelid, RM.Channelid) AND HWM.CriteriaType = 'CHANNEL' " +
                            "left join RetailerAttribute RA ON (RA.AttributeId = HWM.CriteriaId AND HWM.CriteriaType = 'RTR_ATTRIBUTE') " +
                            "where B.RetailerID = '" + retailerID + "'" +
                            " AND B.Score>0 and (RM.RetailerID = '" + retailerID + "' or RA.RetailerID = '" + retailerID + "')");
            if (c != null) {
                while (c.moveToNext()) {
                    weightageBO = new FitScoreBO();
                    weightageBO.setHeader(c.getString(0));
                    weightageBO.setTarget(c.getString(1));
                    weightageBO.setAchieved(c.getString(2));
                    weightageBO.setWeightage(c.getString(3));
                    weightageBO.setScore(c.getString(4));
                    fitScoreList.add(weightageBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setFitScoreList(fitScoreList);
    }

    public void getFitScoreChartforall(String retailerID) {
        fitScoreChartList = new ArrayList<>();
        double weightage = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Double target = 0.0;

            for (HHTModuleBO hhtModule : hhtModuleList) {
                Cursor c = db
                        .selectSQL("SELECT Ifnull(RS.Score,0) as Score,HMW.Weightage FROM HHTModuleWeightage HMW " +
                                "INNER JOIN HHTModuleWeightageMapping HMWM ON HMW.GroupID = HMWM.GroupID " +
                                "LEFT JOIN RetailerMaster RM ON HMWM.CriteriaId IN (RM.subchannelid, RM.Channelid) AND HMWM.CriteriaType = 'CHANNEL' " +
                                "LEFT JOIN RetailerAttribute RA ON (RA.AttributeId = HMWM.CriteriaId AND HMWM.CriteriaType = 'RTR_ATTRIBUTE') " +
                                "LEFT JOIN RetailerScoreDetails RS ON HMW.Module = RS.ModuleCode AND " +
                                "RS.Tid in (Select RH.Tid from RetailerScoreHeader RH where RS.Tid = RH.Tid and RH.RetailerId = '"+ retailerID +"') " +
                                "WHERE HMW.Module = '" + hhtModule.getModule() + "' AND (RM.RetailerID = '" + retailerID + "' OR RA.RetailerID = '" + retailerID + "') LIMIT 1;");
                if (c != null) {
                    while (c.moveToNext()) {
                        FitScoreChartBO fitChart = new FitScoreChartBO();
                        fitChart.setAchieved(SDUtil.roundIt(c.getDouble(0), 2));
                        fitChart.setTarget(SDUtil.roundIt(c.getDouble(1), 2));
                        fitChart.setModule(hhtModule.getModule());
                        fitScoreChartList.add(fitChart);
                        weightage = weightage + c.getDouble(0);
                    }
                }
                c.close();
            }

            Cursor c = db
                    .selectSQL("SELECT DISTINCT Module, Weightage FROM HHTModuleWeightage A " +
                            "INNER JOIN HHTModuleWeightageMapping B ON A.GroupID = B.GroupID " +
                            "LEFT JOIN RetailerMaster RM ON B.CriteriaId IN (RM.subchannelid, RM.Channelid) AND B.CriteriaType = 'CHANNEL' " +
                            "LEFT JOIN RetailerAttribute RA ON (RA.AttributeId = B.CriteriaId AND B.CriteriaType = 'RTR_ATTRIBUTE') " +
                            "WHERE (RM.RetailerID = '" + retailerID + "' or RA.RetailerID = '" + retailerID + "') LIMIT 5");

            if (c != null) {
                while (c.moveToNext()) {
                    target = target + c.getDouble(1);
                }
            }
            c.close();

            FitScoreChartBO fitChart = new FitScoreChartBO();
            fitChart.setAchieved(SDUtil.roundIt(weightage, 2));
            fitChart.setTarget(target + "");
            fitChart.setModule("ALL");
            fitScoreChartList.add(fitChart);
        } catch (Exception e) {
            Commons.printException(e);
        }
        setFitScoreChartList(fitScoreChartList);
    }

    public double getFitScoreAverage() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("Select sum(0+Score)/count(RetailerID) from RetailerScoreHeader where Date ="
                    + bmodel.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            if (c != null) {
                while (c.moveToNext()) {
                    return c.getDouble(0);
                }
            }
            c.close();

        } catch (Exception e) {
            Commons.printException(e);
        }
        return 0;
    }

}
