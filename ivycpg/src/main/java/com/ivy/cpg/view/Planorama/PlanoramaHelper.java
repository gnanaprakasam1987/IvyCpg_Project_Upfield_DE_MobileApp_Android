package com.ivy.cpg.view.Planorama;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ProductHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

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

    public ArrayList<PlanoramaProductBO> getmSOSList() {
        if(mSOSList==null)
            mSOSList=new ArrayList<>();
        return mSOSList;
    }

    private ArrayList<PlanoramaProductBO> mSOSList;

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

            String planorama_column = "visitId,date,retailerId,comments,NoOfPhotos";
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(AppUtils.QT(visitId));
            stringBuilder.append(",");
            stringBuilder.append(AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)));
            stringBuilder.append(",");
            stringBuilder.append(mBusinessModel.getRetailerMasterBO().getRetailerID());
            stringBuilder.append(",");
            stringBuilder.append(AppUtils.QT(comments));
            stringBuilder.append(",");
            stringBuilder.append(noOfPhotos);

            db.insertSQL("Planorama",
                    planorama_column,
                    stringBuilder.toString());


            String planoramaImageColumn = "visitId,imageName";
            StringBuilder imageValues;
            for (String imageName : imageNameList) {
                imageValues = new StringBuilder();
                imageValues.append(AppUtils.QT(visitId));
                imageValues.append(",");
                imageValues.append(AppUtils.QT(imageName));

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


            String query = "select visitId,imageName from PlanoramaImages order by visitId";
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
                                productBO.setNumberOfFacings(json.getInt("nb_facing"));
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

    public void preparePlanoramaSOSList(String jsonResponse){
        try {
            getmSOSList().clear();
            if (!jsonResponse.equals("")) {

                JSONObject jsonObject = new JSONObject(jsonResponse);
                JSONObject jsonData = jsonObject.getJSONObject("data");
                JSONArray jsonArray = (jsonData.getJSONObject("analysis")).getJSONArray("sos_by_manufacturer");

                int totalLengthOfShelf = 0;
                PlanoramaProductBO productBO;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject json = (JSONObject) jsonArray.get(i);

                    if (i == 0) {
                        totalLengthOfShelf = json.getInt("linear");
                    } else {
                        productBO = new PlanoramaProductBO();

                        int length = json.getInt("linear");
                        double sos = json.getDouble("sos");
                        double target = (json.has("target")?json.getDouble("target"):0);

                        productBO.setProductName(json.getString("manufacturer_name"));
                        if(length>0)
                        productBO.setSosActual(sos + "% (" + length + "/" + totalLengthOfShelf+")");
                        else productBO.setSosActual("0/0");
                        productBO.setSosTarget(String.valueOf(target));

                        getmSOSList().add(productBO);

                    }
                }

            }


        }
        catch (Exception ex){
            Commons.printException(ex);
        }

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
            String id = AppUtils.QT(mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + SDUtil.now(SDUtil.DATE_TIME_ID));
            if (mBusinessModel.isEditStockCheck()) {
                Cursor closingStockCursor = db
                        .selectSQL("select StockID from ClosingStockHeader where RetailerID="
                                + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + "");

                if (closingStockCursor.getCount() > 0) {
                    closingStockCursor.moveToNext();
                    id = AppUtils.QT(closingStockCursor.getString(0));
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


                        ProductMasterBO productMasterBO =mBusinessModel.productHelper.getProductMasterBOById(product.getProductId());
                        if(productMasterBO!=null) {
                            int shelfCase = ((product.getLocations().get(j).getShelfCase() == -1) ? 0 : product.getLocations().get(j).getShelfCase());
                            int shelfPiece = ((product.getLocations().get(j).getShelfPiece() == -1) ? 0 : product.getLocations().get(j).getShelfPiece());
                            int shelfOuter = ((product.getLocations().get(j).getShelfOuter() == -1) ? 0 : product.getLocations().get(j).getShelfOuter());
                            int availability = ((product.getLocations().get(j).getAvailability() == -1) ? 0 : product.getLocations().get(j).getAvailability());
                            values = (id) + ","
                                    + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                                    + AppUtils.QT(product.getProductId()) + ","
                                    + productMasterBO.getCaseSize() + ","
                                    + AppUtils.QT(mBusinessModel.retailerMasterBO.getRetailerID()) + ","
                                    + productMasterBO.getCaseUomId() + ","
                                    + productMasterBO.getMSQty() + "," + count + ","
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

                values = (id) + ", " + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
                        + ", " + AppUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID()) + ", "
                        + AppUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerCode()) + ","
                        + AppUtils.QT(mBusinessModel.getStockCheckRemark()) + "," + mBusinessModel.getAppDataProvider().getRetailMaster().getDistributorId();

                if (mBusinessModel.configurationMasterHelper.IS_ENABLE_SHARE_PERCENTAGE_STOCK_CHECK) {
                    String availabilityShare = (mBusinessModel.getAvailablilityShare() == null ||
                            mBusinessModel.getAvailablilityShare().trim().length() == 0) ? "0.0" : mBusinessModel.getAvailablilityShare();
                    values = values + "," + AppUtils.QT(availabilityShare);
                } else {
                    values = values + "," + AppUtils.QT("0.0");
                }

                values = values + "," + AppUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
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

}
