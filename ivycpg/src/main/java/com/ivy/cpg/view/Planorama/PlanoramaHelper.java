package com.ivy.cpg.view.Planorama;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
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


    public void prepareProductList(String responseOutput){
        try {
            getmProductList().clear();

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

                    getmProductList().add(planoramaProductBO);
                }




            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
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
                        double target = json.getDouble("target");

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
}
