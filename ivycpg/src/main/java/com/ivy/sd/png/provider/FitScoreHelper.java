package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.FitScoreBO;
import com.ivy.sd.png.bo.FitScoreChartBO;
import com.ivy.sd.png.bo.HHTModuleBO;
import com.ivy.sd.png.bo.WeightageBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by anandasir.v on 9/27/2017.
 */

public class FitScoreHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static FitScoreHelper instance = null;
    private ArrayList<WeightageBO> weightageList = new ArrayList<>();
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

    public ArrayList<WeightageBO> getWeightageList() {
        return weightageList;
    }

    public void setWeightageList(ArrayList<WeightageBO> weightageList) {
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select Module, Weightage from HHTModuleWeightage"); //MENU_STK_ORD
            if (c != null) {
                while (c.moveToNext()) {
                    hhtModuleBO = new HHTModuleBO();
                    hhtModuleBO.setModule(c.getString(0));
                    hhtModuleBO.setWeightage(c.getString(1));
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

        WeightageBO weightageBO;
        weightageList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select A.HeaderID, B.ProductID,B.FromNorm,B.Weightage from WeightageHeader A  " +
                            "inner join WeightageProductDetail B on A.HeaderID  = B.HeaderID " +
                            "where A.CriteriaID = " + criteriaID + " and A.Module ='" + Module + "' and CriteriaType = 'RETAILER'"); //MENU_STK_ORD
            if (c != null) {
                while (c.moveToNext()) {
                    weightageBO = new WeightageBO();
                    weightageBO.setHeaderID(c.getInt(0));
                    weightageBO.setProductID(c.getInt(1));
                    weightageBO.setFromNorm(c.getInt(2));
                    weightageBO.setScore(c.getInt(3));
                    weightageList.add(weightageBO);
                }
            }
            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        setWeightageList(weightageList);
        if (weightageList.size() == 0) {
            getTaggingDetailsforFitScore(Module);
        }
    }

    public void getTaggingDetailsforFitScore(String Module) {
        try {
            String mappingId = "0", moduletypeid = "0", locationId = "0";
            WeightageBO weightageBO;
            weightageList = new ArrayList<>();

            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
//            Cursor c1 = db
//                    .selectSQL("SELECT criteriatype, TaggingTypelovID,criteriaid,locid FROM ProductTaggingCriteriaMapping PCM " +
//                            "INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid WHERE PM.TaggingTypelovID = "
//                            + " (SELECT ListId FROM StandardListMaster WHERE ListCode = '"
//                            + Module + "' AND ListType = 'PRODUCT_TAGGING') AND (PCM.distributorid=0 OR PCM.distributorid=" + bmodel.getRetailerMasterBO().getDistributorId() + ")");
//            if (c1 != null) {
//                if (c1.moveToNext()) {
//                    if (c1.getString(0).equals("CHANNEL")) {
//                        mappingId = bmodel.schemeDetailsMasterHelper.getChannelidForScheme(bmodel.getRetailerMasterBO().getSubchannelid()) + "," + bmodel.getRetailerMasterBO().getSubchannelid();
//                        if (c1.getInt(3) != 0)
//                            locationId = bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "," + bmodel.getRetailerMasterBO().getLocationId();
//                    } else if (c1.getString(0).equals("DISTRIBUTOR"))
//                        mappingId = bmodel.getRetailerMasterBO().getDistributorId() + "";
//                    else if (c1.getString(0).equals("LOCATION")) {
//                        locationId = bmodel.schemeDetailsMasterHelper.getLocationIdsForScheme() + "," + bmodel.getRetailerMasterBO().getLocationId();
//                    } else if (c1.getString(0).equals("USER"))
//                        mappingId = bmodel.userMasterHelper.getUserMasterBO().getUserid() + "";
//                    else if (c1.getString(0).equals("STORE")) {
//                        mappingId = c1.getString(2);
//                    }
//
//                    moduletypeid = c1.getString(1);
//                }
//                c1.close();
//            }

            StringBuilder productIds = new StringBuilder();
//            Cursor c2 = db
//                    .selectSQL("SELECT PM.GroupID, PGM.pid,PGM.FromNorm,PGM.Score FROM ProductTaggingCriteriaMapping PCM " +
//                            "INNER JOIN ProductTaggingMaster PM ON PM.groupid=PCM.groupid" +
//                            " INNER JOIN ProductTaggingGroupMapping PGM ON PGM.groupid=PM.groupid " +
//                            "WHERE PM.TaggingTypelovID = " + moduletypeid +
//                            " AND PCM.criteriaid IN(" + mappingId + ") AND locid IN(" + locationId + ") AND (PCM.distributorid=0 OR PCM.distributorid=" + bmodel.getRetailerMasterBO().getDistributorId() + ")");

            Cursor c2 = db
                    .selectSQL("SELECT PM.GroupID, PGM.pid,PGM.FromNorm,PGM.Weightage FROM ProductTaggingGroupMapping PGM " +
                            "INNER JOIN ProductTaggingMaster PM ON PM.groupid=PGM.groupid " +
                            "inner join StandardListMaster F on F.ListID = PM.TaggingTypelovID " +
                            "WHERE F.ListCode = '" + Module + "'");

            if (c2 != null) {
                while (c2.moveToNext()) {
                    weightageBO = new WeightageBO();
                    weightageBO.setHeaderID(c2.getInt(0));
                    weightageBO.setProductID(c2.getInt(1));
                    weightageBO.setFromNorm(c2.getInt(2));
                    weightageBO.setScore(c2.getInt(3));
                    weightageList.add(weightageBO);
                }
                c2.close();
            }
            db.closeDB();
            setWeightageList(weightageList);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public int getModuleWeightage(String Module) {
        int weightage = 0;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select Weightage from HHTModuleWeightage where Module ='" + Module + "'"); //MENU_STK_ORD
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
            for (WeightageBO weightage : getWeightageList()) {
                if (weightage.getFromNorm() > 0)
                    if (ProductID.equals(String.valueOf(weightage.getProductID())) && Qty >= weightage.getFromNorm()) {
                        return weightage.getScore();
                    }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return 0;
    }

    public int checkWeightage(String ProductID) {
        try {
            for (WeightageBO weightage : getWeightageList()) {
                if (ProductID.equals(String.valueOf(weightage.getProductID()))) {
                    return weightage.getScore();
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
                    .selectSQL("Select A.PName,D.FromNorm,case when (ifnull(D.FromNorm,0)<ifnull(B.Score,0)) then 'Y' else 'N' end,E.Weightage,B.Score from productMaster A " +
                            Qry +
                            "inner join WeightageHeader C on C.CriteriaID = B.RetailerID " +
                            "inner join WeightageProductDetail D on C.HeaderID = D.HeaderID and D.ProductID = B." + PID + " " +
                            "inner join HHTModuleWeightage E on E.Module = C.Module where C.CriteriaID = '" + retailerID + "' and E.Module = '" + Module + "'" +
                            " AND B.Score>0");
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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
//            Cursor c = db
//                    .selectSQL("Select distinct A.PName,E.FromNorm,case when (ifnull(E.FromNorm,0)<ifnull(B.Score,0)) then 'Y' else 'N' end,G.Weightage,B.Score from productMaster A " +
//                            Qry +
//                            "inner join ProductTaggingCriteriaMapping C on C.CriteriaID = B.retailerID " +
//                            "inner join ProductTaggingMaster D ON D.groupid=C.groupid " +
//                            "inner join ProductTaggingGroupMapping E ON E.groupid=D.groupid and E.PID = B." + PID + " " +
//                            "inner join StandardListMaster F on F.ListID = D.TaggingTypelovID " +
//                            "inner join HHTModuleWeightage G on G.Module = F.ListCode " +
//                            "WHERE C.CriteriaID = '" + retailerID + "' and G.Module = '" + Module + "'");
            Cursor c = db
                    .selectSQL("Select A.PName,E.FromNorm,case when (ifnull(E.FromNorm,0)<ifnull(B.Score,0)) then 'Y' else 'N' end,G.Weightage,B.Score from productMaster A " +
                            Qry +
                            "inner join StandardListMaster F on F.ListID = D.TaggingTypelovID " +
                            "inner join ProductTaggingMaster D ON D.TaggingTypelovID  =F.ListID " +
                            "inner join ProductTaggingGroupMapping E ON E.groupid=D.groupid and E.PID = B." + PID + " " +
                            "inner join HHTModuleWeightage G on G.Module = F.ListCode WHERE G.Module = '" + Module + "'" +
                            " AND B.retailerid=" + retailerID +
                            " AND B.Score>0");

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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String ListCode = "";
            if (Module.equals(DataMembers.FIT_ASSET)) {
                ListCode = "MERCH";
            } else {
                ListCode = "MERCH_INIT";
            }
            Cursor c = db
                    .selectSQL("Select A.PosmDesc,0,case when (ifnull(B.Score,0)>0) then 'Y' else 'N' end,E.Weightage,B.Score from PosmMaster A " +
                            "inner join AssetDetail B on A.Posmid = B.AssetID " +
                            "inner join AssetHeader C on C.Uid = B.UID " +
                            "inner join StandardListMaster D on D.ListId = C.TypeLovID " +
                            "inner join HHTModuleWeightage E on E.Module =  '" + Module + "' where B.RetailerID = '" + retailerID + "' and D.ListCode = '" + ListCode + "'" +
                            " AND B.Score>0");
//            Cursor c = db
//                    .selectSQL("Select A.PName,0,case when (ifnull(B.Score,0)>0) then 'Y' else 'N' end,E.Weightage,B.Score from productMaster A " +
//                            "inner join AssetDetail B on A.Pid = B.ProductID " +
//                            "inner join AssetHeader C on C.Uid = B.UID " +
//                            "inner join StandardListMaster D on D.ListId = C.TypeLovID " +
//                            "inner join HHTModuleWeightage E on E.Module =  '" + Module + "' where B.RetailerID = '" + retailerID + "' and D.ListCode = '" + ListCode + "'");
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select A.PName,0,case when (ifnull(B.Score,0)>0) then 'Y' else 'N' end,E.Weightage,B.Score " +
                            "from productMaster A inner join PromotionDetail B on A.Pid = B.BrandID " +
                            "inner join HHTModuleWeightage E on E.Module =  '" + Module + "' where B.RetailerID = '" + retailerID + "'" +
                            " AND B.Score>0");
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("Select Ifnull(sum(0+Score),0),(Select Weightage from HhtModuleWeightage where Module ='" + DataMembers.FIT_STOCK + "' ) " +
                            "from ClosingStockHeader where RetailerID = '" + retailerID + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    FitScoreChartBO fitChart = new FitScoreChartBO();
                    fitChart.setAchieved(SDUtil.roundIt(c.getDouble(0), 2));
                    fitChart.setTarget(SDUtil.roundIt(c.getDouble(1), 2));
                    fitChart.setModule(DataMembers.FIT_STOCK);
                    fitScoreChartList.add(fitChart);
                    weightage = weightage + c.getDouble(0);

                }
            }
            c.close();
            c = db
                    .selectSQL("Select Ifnull(sum(0+Score),0),(Select Weightage from HhtModuleWeightage where Module ='" + DataMembers.FIT_PRICE + "' ) " +
                            "from PriceCheckHeader where RetailerID = '" + retailerID + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    FitScoreChartBO fitChart = new FitScoreChartBO();
                    fitChart.setAchieved(SDUtil.roundIt(c.getDouble(0), 2));
                    fitChart.setTarget(SDUtil.roundIt(c.getDouble(1), 2));
                    fitChart.setModule(DataMembers.FIT_PRICE);
                    fitScoreChartList.add(fitChart);
                    weightage = weightage + c.getDouble(0);
                }
            }
            c.close();
            c = db
                    .selectSQL("Select Ifnull(sum(0+Score),0),(Select Weightage from HhtModuleWeightage where Module ='" + DataMembers.FIT_ASSET + "' ) " +
                            "from AssetHeader inner join StandardListMaster on ListID = TypeLovID " +
                            "where RetailerID = '" + retailerID + "'  and ListCode ='MERCH'");
            if (c != null) {
                while (c.moveToNext()) {
                    FitScoreChartBO fitChart = new FitScoreChartBO();
                    fitChart.setAchieved(SDUtil.roundIt(c.getDouble(0), 2));
                    fitChart.setTarget(SDUtil.roundIt(c.getDouble(1), 2));
                    fitChart.setModule(DataMembers.FIT_ASSET);
                    fitScoreChartList.add(fitChart);
                    weightage = weightage + c.getDouble(0);
                }
            }
            c.close();
            c = db
                    .selectSQL("Select Ifnull(sum(0+Score),0),(Select Weightage from HhtModuleWeightage where Module ='" + DataMembers.FIT_POSM + "' ) " +
                            "from AssetHeader inner join StandardListMaster on ListID = TypeLovID " +
                            "where RetailerID = '" + retailerID + "'  and ListCode ='MERCH_INIT'");
            if (c != null) {
                while (c.moveToNext()) {
                    FitScoreChartBO fitChart = new FitScoreChartBO();
                    fitChart.setAchieved(SDUtil.roundIt(c.getDouble(0), 2));
                    fitChart.setTarget(SDUtil.roundIt(c.getDouble(1), 2));
                    fitChart.setModule(DataMembers.FIT_POSM);
                    fitScoreChartList.add(fitChart);
                    weightage = weightage + c.getDouble(0);
                }
            }
            c.close();
            c = db
                    .selectSQL("Select Ifnull(sum(0+Score),0),(Select Weightage from HhtModuleWeightage where Module ='" + DataMembers.FIT_PROMO + "' ) " +
                            "from PromotionHeader where RetailerID = '" + retailerID + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    FitScoreChartBO fitChart = new FitScoreChartBO();
                    fitChart.setAchieved(SDUtil.roundIt(c.getDouble(0), 2));
                    fitChart.setTarget(SDUtil.roundIt(c.getDouble(1), 2));
                    fitChart.setModule(DataMembers.FIT_PROMO);
                    fitScoreChartList.add(fitChart);
                    weightage = weightage + c.getDouble(0);
                }
            }
            c.close();
            db.closeDB();

            FitScoreChartBO fitChart = new FitScoreChartBO();
            fitChart.setAchieved(SDUtil.roundIt(weightage, 2));
            fitChart.setTarget("100.00");
            fitChart.setModule("ALL");
            fitScoreChartList.add(fitChart);
        } catch (Exception e) {
            Commons.printException(e);
        }
        setFitScoreChartList(fitScoreChartList);
    }

//    public void getFitScore(String Module) {
//
//        FitScoreBO weightageBO;
//        try {
//            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
//                    DataMembers.DB_PATH);
//            db.createDataBase();
//            db.openDataBase();
//            String Qry = "";
//            if (Module.equals("MENU_STK_ORD")) {
//                Qry = "left join ClosingStockDetail D on  D.RetailerID = A.CriteriaID and D.ProductID = B.ProductID ";
//            } else if (Module.equals("MENU_PRICE")) {
//                Qry = "left join PriceCheckDetail D on  D.RetailerID = A.CriteriaID and D.PID = B.ProductID ";
//            }
//            Cursor c = db
//                    .selectSQL("Select A.HeaderID,A.CriteriaID,E.PName,B.FromNorm," +
//                            "case when (ifnull(B.FromNorm,0)<ifnull(D.Score,0)) then 'Y' else 'N' end,C.Weightage,B.Score " +
//                            "from WeightageHeader A inner join WeightageProductDetail B on A.headerID = B.headerID " +
//                            "inner join HHTModuleWeightage C on C.Module = A.Module " +
//                            Qry + " inner join ProductMaster E on E.PID = B.ProductID where A.Module = '" + Module + "'");
//            if (c != null) {
//                while (c.moveToNext()) {
//                    weightageBO = new FitScoreBO();
//                    weightageBO.setHeader(c.getString(2));
//                    weightageBO.setTarget(c.getString(3));
//                    weightageBO.setAchieved(c.getString(4));
//                    weightageBO.setWeightage(c.getString(5));
//                    weightageBO.setScore(c.getString(6));
//                    fitScoreList.add(weightageBO);
//                }
//            }
//            c.close();
//            db.closeDB();
//        } catch (Exception e) {
//            Commons.printException(e);
//        }
//        setFitScoreList(fitScoreList);
//    }

//    public void getFitScoreforAsset(String Module, String Retailer) {
//        FitScoreBO weightageBO;
//        for (StandardListBO standardListBO : bmodel.productHelper.getInStoreLocation()) {
//            ArrayList<AssetTrackingBO> mAssetTrackingList = standardListBO.getAssetTrackingList();
//            if (mAssetTrackingList != null) {
//                for (AssetTrackingBO assetBo : mAssetTrackingList) {
//                    weightageBO = new FitScoreBO();
//                    weightageBO.setHeader(assetBo.getAssetName());
//                    weightageBO.setTarget(String.valueOf(assetBo.getTarget()));
//                    weightageBO.setAchieved(c.getString(4));
//                    weightageBO.setWeightage(c.getString(5));
//                    weightageBO.setScore(c.getString(6));
//                    fitScoreList.add(weightageBO);
//                }
//            }
//        }
//    }
//
//    public boolean isAssetorPosm(String ID, String retailerID) {
//        boolean isHit = false;
//        try {
//            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
//                    DataMembers.DB_PATH);
//            db.createDataBase();
//            db.openDataBase();
//            Cursor c = db
//                    .selectSQL("Select Score from " + tableName + " where RetailerID ='" + retailerID + "'" +
//                            " and ProductID = '" + ID + "'"); //MENU_STK_ORD
//            if (c != null) {
//                while (c.moveToNext()) {
//                    isHit = (c.getInt(0) > 0);
//                }
//            }
//            c.close();
//            db.closeDB();
//        } catch (Exception e) {
//            Commons.printException(e);
//        }
//        return isHit;
//    }
//
//    public boolean getHit(String ID, String retailerID, String Module) {
//        boolean isHit = false;
//        try {
//            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
//                    DataMembers.DB_PATH);
//            db.createDataBase();
//            db.openDataBase();
//            String tableName = "";
//            if (Module.equals("MENU_ASSET") || Module.equals("MENU_POSM")) {
//                tableName = "AssetDetail";
//            } else if (Module.equals("MENU_PROMO")) {
//                tableName = "PromotionDetail";
//            }
//            Cursor c = db
//                    .selectSQL("Select Score from " + tableName + " where RetailerID ='" + retailerID + "'" +
//                            " and ProductID = '" + ID + "'"); //MENU_STK_ORD
//            if (c != null) {
//                while (c.moveToNext()) {
//                    isHit = (c.getInt(0) > 0);
//                }
//            }
//            c.close();
//            db.closeDB();
//        } catch (Exception e) {
//            Commons.printException(e);
//        }
//        return isHit;
//    }
}
