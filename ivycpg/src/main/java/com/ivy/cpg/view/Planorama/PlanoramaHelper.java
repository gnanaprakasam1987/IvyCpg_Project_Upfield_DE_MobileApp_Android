package com.ivy.cpg.view.Planorama;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.cpg.view.sf.SFLocationBO;
import com.ivy.cpg.view.sf.SalesFundamentalHelper;
import com.ivy.cpg.view.sf.ShelfShareHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.bo.ShelfShareBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class PlanoramaHelper {

    private static PlanoramaHelper instance;
    private BusinessModel mBusinessModel;

    public boolean SHOW_STOCK_SP;
    public boolean SHOW_STOCK_SC;
    public boolean SHOW_STOCK_CB;
    public boolean SHOW_SHELF_OUTER;

    public ArrayList<PlanoramaProductBO> getmProductList() {
        if(mProductList==null)
            mProductList=new ArrayList<>();
        return mProductList;
    }



    private ArrayList<PlanoramaProductBO> mProductList;

    public HashMap<String, PlanoramaProductBO> getmProductBOById() {
        if(mProductBOById==null)
            mProductBOById=new HashMap<>();
        return mProductBOById;
    }

    private HashMap<String,PlanoramaProductBO> mProductBOById;

    public ArrayList<SOSBO> getmSOSList() {
        if(mSOSList==null)
            mSOSList=new ArrayList<>();
        return mSOSList;
    }

    private ArrayList<SOSBO> mSOSList;

    private PlanoramaHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
    }
    public static PlanoramaHelper getInstance(Context context) {
        if (instance == null)
            instance = new PlanoramaHelper(context);

        return instance;
    }

    public void saveVisit(Context mContext, String visitId, String comments, int noOfPhotos, ArrayList<String> imageNameList){

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            String planorama_column = "uid,date,retailerId,comments,NoOfPhotos,ReferenceNo";
            StringBuilder stringBuilder = new StringBuilder();
            String id = StringUtils.QT(mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

            stringBuilder.append(id);
            stringBuilder.append(",");
            stringBuilder.append(StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            stringBuilder.append(",");
            stringBuilder.append(mBusinessModel.getRetailerMasterBO().getRetailerID());
            stringBuilder.append(",");
            stringBuilder.append(StringUtils.QT(comments));
            stringBuilder.append(",");
            stringBuilder.append(noOfPhotos);
            stringBuilder.append(",");
            stringBuilder.append(StringUtils.QT(visitId));



            db.insertSQL("Planorama",
                    planorama_column,
                    stringBuilder.toString());


            String planoramaImageColumn = "uid,imageName,RetailerID";
            StringBuilder imageValues;
            for (String imageName : imageNameList) {
                imageValues = new StringBuilder();
                imageValues.append(id);
                imageValues.append(",");
                imageValues.append(StringUtils.QT(imageName));
                imageValues.append(",");
                imageValues.append(StringUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()));


                db.insertSQL("PlanoramaImages",
                        planoramaImageColumn,
                        imageValues.toString());
            }

        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    public HashMap<String, ArrayList<String>> getImageNameListByVistId() {
        if(imageNameListByVistId==null)
            imageNameListByVistId=new HashMap<>();
        return imageNameListByVistId;
    }

    private HashMap<String,ArrayList<String>> imageNameListByVistId;
    public HashMap<String,ArrayList<String>> getImageNameList(Context mContext){
        imageNameListByVistId=new HashMap<>();

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();


            String query = "select PM.ReferenceNo,imageName from PlanoramaImages PI LEFT JOIN Planorama PM ON PM.uid=PI.uid order by PI.uid";
            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    if(imageNameListByVistId.get(c.getString(0))!=null){
                        imageNameListByVistId.get(c.getString(0)).add(c.getString(1));
                    }
                    else {
                        ArrayList<String> imageNameList = new ArrayList<>();
                        imageNameList.add(c.getString(1));
                        imageNameListByVistId.put(c.getString(0),imageNameList);


                    }
                }
            }

        }
        catch (Exception ex){
            Commons.printException(ex);
        }
        return imageNameListByVistId;

    }


    public void prepareProductList(Context context,String responseOutput){
        try {
            getmProductList().clear();
            ProductHelper productHelper=ProductHelper.getInstance(context);

            if (!responseOutput.equals("")) {
                //list_visits = new ArrayList<>();

                JSONObject jsonObject=new JSONObject(responseOutput);
                JSONObject jsonData = jsonObject.getJSONObject("data");
                JSONObject jsonProductGroup=jsonData.getJSONObject("products");
                Iterator<String> keys=jsonProductGroup.keys();
                PlanoramaProductBO planoramaProductBO;
                while (keys.hasNext()){
                    String key = (String)keys.next();
                    JSONObject jsonProduct = jsonProductGroup.getJSONObject(key);

                    planoramaProductBO=new PlanoramaProductBO();
                    planoramaProductBO.setProductId(jsonProduct.getString("ean"));
                    planoramaProductBO.setProductName(jsonProduct.getString("description"));

                    planoramaProductBO.setLocations(cloneInStoreLocationList(productHelper.locations));

                    getmProductList().add(planoramaProductBO);
                    getmProductBOById().put(planoramaProductBO.getProductId(),planoramaProductBO);
                }




            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    /**
     * Duplicate the Location List
     *
     * @param list list
     * @return clone list
     */
    public static ArrayList<LocationBO> cloneInStoreLocationList(
            ArrayList<LocationBO> list) {
        ArrayList<LocationBO> clone = new ArrayList<LocationBO>(list.size());
        for (LocationBO item : list)
            clone.add(new LocationBO(item));
        return clone;
    }

    public void updateProductAvailability(String jsonResponse){
        try{

            if (getmProductList().size()>0) {

                JSONObject jsonObject=new JSONObject(jsonResponse);
                JSONObject jsonData = jsonObject.getJSONObject("data");
                JSONArray jsonArray=(jsonData.getJSONObject("analysis")).getJSONArray("raw_data");

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = (JSONObject) jsonArray.get(i);
                    if(json.getBoolean("present")){

                        //It can be changed to hashmap later
                        for(PlanoramaProductBO productBO:getmProductList()){
                            if(productBO.getProductId().equals(json.getString("ean"))){
                                productBO.setAvailable(true);
                                productBO.setNumberOfFacings((productBO.getNumberOfFacings()+json.getInt("nb_facing")));
                            }
                        }
                    }
                }





            }

        }
        catch (Exception ex){
            Commons.printException(ex);
        }

    }

    private ArrayList<SFLocationBO> locations;
    public void setLocations(ArrayList<SFLocationBO> locations) {
        this.locations = locations;
    }

    public void preparePlanoramaSOSList(Context context,String jsonResponse){
        try {
            getmSOSList().clear();

            //Load the locations
            SalesFundamentalHelper mSFHelper = SalesFundamentalHelper.getInstance(context);
            mSFHelper.downloadLocations();
            setLocations(mSFHelper.getLocationList());

            if (!jsonResponse.equals("")) {

                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject jsonData = jsonObject.getJSONObject("data");
                JSONArray jsonArray = (jsonData.getJSONObject("analysis")).getJSONArray("sos_by_manufacturer");


                int totalLengthOfShelf = 0;
                SOSBO productBO;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = (JSONObject) jsonArray.get(i);

                    if (i == 0) {
                        totalLengthOfShelf = json.getInt("linear");
                    } else {
                        productBO = new SOSBO();

                        int length = json.getInt("linear");
                        double sos = json.getDouble("sos");
                        double target = (json.has("target")?json.getDouble("target"):0);

                        productBO.setProductName(json.getString("manufacturer_name"));
                        productBO.setNorm((float)target);

                        productBO.setLocations(cloneSOSLocationList(locations));
                        productBO.getLocations().get(0).setActual(String.valueOf(length));
                        productBO.getLocations().get(0).setPercentage(String.valueOf(sos));
                        productBO.getLocations().get(0).setParentTotal(String.valueOf(totalLengthOfShelf));


                        getmSOSList().add(productBO);

                    }
                }

            }


        }
        catch (Exception ex){
            Commons.printException(ex);
        }

    }

    /**
     * Method used to clone given list
     *
     * @param list List to clone
     * @return
     */
    public static ArrayList<SFLocationBO> cloneSOSLocationList(
            ArrayList<SFLocationBO> list) {
        ArrayList<SFLocationBO> clone = new ArrayList<>(list.size());
        for (SFLocationBO item : list)
            clone.add(new SFLocationBO(item));
        return clone;
    }


    public boolean isAnalysisReady(String jsonResponse){
        try {
            JSONObject jsonObject=new JSONObject(jsonResponse);
            if((jsonObject.getJSONObject("data")).getJSONObject("analysis").getJSONArray("raw_data")!=null)
                return true;
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return false;
    }

    public void saveStock(Context mContext,ArrayList<PlanoramaProductBO> productList){

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();

            boolean isData;
            String id = StringUtils.QT(mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));
            if (mBusinessModel.isEditStockCheck()) {
                Cursor closingStockCursor = db
                        .selectSQL("select StockID from ClosingStockHeader where RetailerID="
                                + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + "");

                if (closingStockCursor.getCount() > 0) {
                    closingStockCursor.moveToNext();
                    id = StringUtils.QT(closingStockCursor.getString(0));
                    db.deleteSQL("ClosingStockHeader", "StockID=" + id, false);
                    db.deleteSQL("ClosingStockDetail", "StockID=" + id, false);
                }
                closingStockCursor.close();
            }


            // ClosingStock Detail entry

            String detailColumns = "StockID,Date,ProductID,uomqty,retailerid,uomid,msqqty,Qty,ouomid,ouomqty,"
                    + " Shelfpqty,Shelfcqty,shelfoqty,whpqty,whcqty,whoqty,LocId,reasonID,isDone," +
                    "Facing,IsOwn,PcsUOMId,isAvailable";
            String values="";

            isData = false;
            int siz;

            PlanoramaProductBO product;

            for (int i = 0; i < productList.size(); ++i) {
                    product = productList.get(i);

                int siz1 = product.getLocations().size();
                for (int j = 0; j < siz1; j++) {
                    if ((SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                            || (SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                            || (SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                            || (SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)
                            || product.getLocations().get(j).getFacingQty() > 0
                            || product.getLocations().get(j).getAudit() != 2
                            || product.getLocations().get(j).getReasonId() != 0) {

                        int count = product.getLocations().get(j)
                                .getShelfPiece()
                                + product.getLocations().get(j).getWHPiece();


                        // for demo ID hard coded
                        ProductMasterBO productMasterBO =mBusinessModel.productHelper.getProductMasterBOById("81630");//product.getProductId());
                        if(productMasterBO!=null) {
                            int shelfCase = ((product.getLocations().get(j).getShelfCase() == -1) ? 0 : product.getLocations().get(j).getShelfCase());
                            int shelfPiece = ((product.getLocations().get(j).getShelfPiece() == -1) ? 0 : product.getLocations().get(j).getShelfPiece());
                            int shelfOuter = ((product.getLocations().get(j).getShelfOuter() == -1) ? 0 : product.getLocations().get(j).getShelfOuter());
                            int availability = ((product.getLocations().get(j).getAvailability() == -1) ? 0 : product.getLocations().get(j).getAvailability());
                            values = (id) + ","
                                    + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                    + StringUtils.QT(product.getProductId()) + ","
                                    + productMasterBO.getCaseSize() + ","
                                    + StringUtils.QT(mBusinessModel.retailerMasterBO.getRetailerID()) + ","
                                    + productMasterBO.getCaseUomId() + ","
                                    + productMasterBO.getMSQty() + ","
                                    + count + ","
                                    + productMasterBO.getOuUomid() + ","
                                    + productMasterBO.getOutersize() + ","
                                    + shelfPiece
                                    + ","
                                    + shelfCase
                                    + ","
                                    + shelfOuter
                                    + ","
                                    + product.getLocations().get(j).getWHPiece()
                                    + ","
                                    + product.getLocations().get(j).getWHCase()
                                    + ","
                                    + product.getLocations().get(j).getWHOuter()
                                    + ","
                                    + product.getLocations().get(j).getLocationId()
                                    + ","
                                    + product.getLocations().get(j).getReasonId() + ","
                                    + product.getLocations().get(j).getAudit()
                                    + ","
                                    + product.getLocations().get(j).getFacingQty()
                                    + "," + productMasterBO.getOwn()
                                    + "," + productMasterBO.getPcUomid()
                                    + "," + availability;

                            db.insertSQL(DataMembers.tbl_closingstockdetail,
                                    detailColumns, values);
                            isData = true;
                        }

                    }
                }
            }


            // ClosingStock Header entry
            if (isData) {
              String  columns = "StockID,Date,RetailerID,RetailerCode,remark,DistributorID,AvailabilityShare,ridSF,VisitId";

                values = (id) + ", " + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                        + ", " + StringUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()) + ", "
                        + StringUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerCode()) + ","
                        + StringUtils.QT(mBusinessModel.getStockCheckRemark()) + "," + mBusinessModel.getAppDataProvider().getRetailMaster().getDistributorId();

                if (mBusinessModel.configurationMasterHelper.IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) {
                    String availabilityShare = (mBusinessModel.getAvailablilityShare() == null ||
                            mBusinessModel.getAvailablilityShare().trim().length() == 0) ? "0.0" : mBusinessModel.getAvailablilityShare();
                    values = values + "," + StringUtils.QT(availabilityShare);
                } else {
                    values = values + "," + StringUtils.QT("0.0");
                }

                values = values + "," + StringUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
                        + mBusinessModel.getAppDataProvider().getUniqueId();

                db.insertSQL(DataMembers.tbl_closingstockheader, columns, values);


            }

            db.closeDB();


        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }


    public void loadStockCheckConfiguration(Context context, int subChannelID) {


        SHOW_STOCK_SP = false;
        SHOW_STOCK_SC = false;
        SHOW_SHELF_OUTER = false;

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
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
                    /*case "REASON":
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
                    case "AVGDAYS":
                        SHOW_STOCK_AVGDAYS = true;
                        break;*/


                }
        }
    }

    public boolean hasStockCheck(ArrayList<PlanoramaProductBO> productList) {

        int siz = productList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            PlanoramaProductBO product =productList.get(i);

            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if ((SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                        || (SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                        || (SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                        || (SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)
                        || product.getLocations().get(j).getReasonId() != 0)
                    return true;
            }
        }
        return false;
    }

    /**
     * Load the ClosingStock Details and ClosingStock Header datas into product
     * master to Edit Order.
     */
    public void loadStockCheckedProducts(Context context,String retailerId) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            String stockID = new String();
            // Order Header

            StringBuilder sb = new StringBuilder();
            sb.append("select StockID,ifnull(remark,'') from ");
            sb.append(DataMembers.tbl_closingstockheader + " where RetailerID=");
            sb.append(StringUtils.QT(retailerId));
            sb.append(" AND DistributorID=" + mBusinessModel.getRetailerMasterBO().getDistributorId());
            sb.append(" AND date = " + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));
            sb.append(" and upload= 'N'");


            Cursor stockCheckedHeaderCursor = db.selectSQL(sb.toString());
            if (stockCheckedHeaderCursor != null) {
                if (stockCheckedHeaderCursor.moveToNext()) {
                    stockID = stockCheckedHeaderCursor.getString(0);
                }
            }
            stockCheckedHeaderCursor.close();
            // if (remarksHelper.getRemarksBO().getModuleCode()
            // .equals(StandardListMasterConstants.MENU_STOCK))
            // remarksHelper.getRemarksBO().setTid(stockID);
            String sql1 = "select productId,shelfpqty,shelfcqty,whpqty,whcqty,whoqty,shelfoqty,LocId,isDistributed,isListed,reasonID,isDone,IsOwn,Facing,RField1,RField2,isAvailable from "
                    + DataMembers.tbl_closingstockdetail
                    + " where stockId="
                    + StringUtils.QT(stockID) + "";
            Cursor orderDetailCursor = db.selectSQL(sql1);
            if (orderDetailCursor != null) {
                while (orderDetailCursor.moveToNext()) {
                    String productId = orderDetailCursor.getString(0);
                    int shelfpqty = orderDetailCursor.getInt(1);
                    int shelfcqty = orderDetailCursor.getInt(2);
                    int whpqty = orderDetailCursor.getInt(3);
                    int whcqty = orderDetailCursor.getInt(4);
                    int whoqty = orderDetailCursor.getInt(5);
                    int shelfoqty = orderDetailCursor.getInt(6);
                    int locationId = orderDetailCursor.getInt(7);
                    int isDistributed = orderDetailCursor.getInt(8);
                    int isListed = orderDetailCursor.getInt(9);
                    int reasonID = orderDetailCursor.getInt(10);
                    int audit = orderDetailCursor.getInt(11);
                    int isOwn = orderDetailCursor.getInt(12);
                    int facing = orderDetailCursor.getInt(13);
                    int pouring = orderDetailCursor.getInt(14);
                    int cocktail = orderDetailCursor.getInt(15);
                    int availability = orderDetailCursor.getInt(16);

                    setStockCheckQtyDetails(productId, shelfpqty, shelfcqty,
                            whpqty, whcqty, whoqty, shelfoqty, locationId,
                            isDistributed, isListed, reasonID, audit, isOwn, facing, pouring, cocktail,  availability);

                }
                orderDetailCursor.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    private void setStockCheckQtyDetails(String productid, int shelfpqty,
                                         int shelfcqty, int whpqty, int whcqty, int whoqty, int shelfoqty,
                                         int locationId, int isDistributed, int isListed, int reasonID,
                                         int audit, int isOwn, int facing, int pouring, int cocktail,
                                          int availability) {




            if (!SHOW_STOCK_SP)
                shelfpqty = -1;
            if (!SHOW_STOCK_SC)
                shelfcqty = -1;
            if (!SHOW_SHELF_OUTER)
                shelfoqty = -1;
            if (!SHOW_STOCK_CB)
                availability = -1;



              PlanoramaProductBO  product=getmProductBOById().get(productid);
                if (product != null ) {
                    if(productid.equals(product.getProductId())) {

                        for (int j = 0; j < product.getLocations().size(); j++) {
                            if (product.getLocations().get(j).getLocationId() == locationId) {
                                product.getLocations().get(j).setShelfPiece(shelfpqty);
                                product.getLocations().get(j).setShelfCase(shelfcqty);
                                product.getLocations().get(j).setShelfOuter(shelfoqty);
                                product.getLocations().get(j).setWHPiece(whpqty);
                                product.getLocations().get(j).setWHCase(whcqty);
                                product.getLocations().get(j).setWHOuter(whoqty);
                                product.getLocations().get(j).setReasonId(reasonID);
                                product.getLocations().get(j).setAudit(audit);
                                product.getLocations().get(j).setFacingQty(facing);
                                product.getLocations().get(j).setIsPouring(pouring);
                                product.getLocations().get(j).setCockTailQty(cocktail);
                                product.getLocations().get(j).setAvailability(availability);


                                return;
                            }
                        }

                    }
                }


    }

    public void saveSOS(Context context, ArrayList<SOSBO> mSOSList){

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();

            String modName = "SOS";
            String refId = "0";

            String headerColumns = "Uid,RetailerId,Date,Remark,refid,ridSF,VisitId";
            String detailColumns = "Uid,Pid,RetailerId,Norm,ParentTotal,Required,Actual,Percentage,Gap,ReasonId,ImageName,IsOwn,ParentID,Isdone,MappingId,LocId,imgName";

            String uid = (mBusinessModel.getAppDataProvider().getUser().getUserid() + DateTimeUtils
                    .now(DateTimeUtils.DATE_TIME_ID));

            String query = "select Uid,refid from " + modName
                    + "_Tracking_Header  where RetailerId="
                    + StringUtils.QT(mBusinessModel.retailerMasterBO.getRetailerID());
            query += " and (upload='N' OR refid!=0)";

            Cursor cursor = db.selectSQL(query);

            if (cursor.getCount() > 0) {
                cursor.moveToNext();
                db.deleteSQL(modName + "_Tracking_Header",
                        "Uid=" + StringUtils.QT(cursor.getString(0)), false);
                db.deleteSQL(modName + "_Tracking_Detail",
                        "Uid=" + StringUtils.QT(cursor.getString(0)), false);
                if (modName.equals("SOS")) {
                    db.deleteSQL(modName + "_Tracking_Parent_Detail", "Uid="
                            + StringUtils.QT(cursor.getString(0)), false);
                    db.deleteSQL(DataMembers.tbl_SOS__Block_Tracking_Detail,
                            "Uid=" + StringUtils.QT(cursor.getString(0)), false);
                }
                refId = cursor.getString(1);
                // uid = cursor.getString(0);
            }
            cursor.close();

            // Inserting Header in Tables

            String headerValues = StringUtils.QT(uid)
                    + "," + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()
                    + "," + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + "," + StringUtils.QT(mBusinessModel.getNote())
                    + "," + StringUtils.QT(refId)
                    + "," + StringUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRidSF())
                    + "," + mBusinessModel.getAppDataProvider().getUniqueId();

            db.insertSQL(modName + "_Tracking_Header", headerColumns,
                    headerValues);

            String detailValues;
            if (modName.equals("SOS")) {
                int locid1;
                String tempkey = "";
                String mKey1 = "";
                detailColumns += ",remarks";
                for (SOSBO sosBo : mSOSList) {
                    for (int i = 0; i < sosBo.getLocations().size(); i++) {
                        if ((!sosBo.getLocations().get(i).getParentTotal().equals("0")
                                && !sosBo.getLocations().get(i).getParentTotal().equals("0.0"))
                                || sosBo.getLocations().get(i).getAudit() != 2) {
                            detailValues = StringUtils.QT(uid)
                                    + "," + sosBo.getProductID()
                                    + "," + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()
                                    + "," + sosBo.getNorm()
                                    + "," + sosBo.getLocations().get(i).getParentTotal()
                                    + "," + sosBo.getLocations().get(i).getTarget()
                                    + "," + sosBo.getLocations().get(i).getActual()
                                    + "," + sosBo.getLocations().get(i).getPercentage()
                                    + "," + sosBo.getLocations().get(i).getGap()
                                    + "," + (sosBo.getLocations().get(i).getReasonId() == -1 ? 0 : sosBo.getLocations().get(i).getReasonId())
                                    + "," + StringUtils.QT(sosBo.getLocations().get(i).getImageName())
                                    + "," + sosBo.getIsOwn()
                                    + "," + sosBo.getParentID()
                                    + "," + sosBo.getLocations().get(i).getAudit()
                                    + "," + sosBo.getMappingId()
                                    + "," + sosBo.getLocations().get(i).getLocationId()
                                    + "," + StringUtils.QT(sosBo.getLocations().get(i).getImgName())
                                    + "," + StringUtils.QT(sosBo.getLocations().get(i).getRemarks());

                            db.insertSQL(modName + "_Tracking_Detail",
                                    detailColumns, detailValues);


                        }

                    }


                }






            }

        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    public void saveAnalysisResult(Context context, String result,String visitID){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();
        try {
            db.updateSQL("update planorama set analysisResult="+StringUtils.QT(result)+" where referenceNo="+StringUtils.QT(visitID));
            db.closeDB();

        }
        catch (Exception ex){
            Commons.printException(ex);
            db.closeDB();
        }
    }


    public String fetchLocalAnalysisResult(Context context, String visitId){

        String result="";
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        db.openDataBase();
        try {

            String query = "select analysisResult from planorama where referenceNo="+StringUtils.QT(visitId);

            Cursor cursor = db.selectSQL(query);

            if (cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    result = cursor.getString(0);
                }
            }
            cursor.close();

        }
        catch (Exception ex){
            Commons.printException(ex);
            db.closeDB();
        }
        return result;
    }


}
