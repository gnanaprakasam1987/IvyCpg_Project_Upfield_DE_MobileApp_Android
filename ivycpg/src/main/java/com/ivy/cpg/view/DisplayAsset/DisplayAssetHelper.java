package com.ivy.cpg.view.DisplayAsset;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.CompanyBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

public class DisplayAssetHelper {

    private static DisplayAssetHelper instance = null;
    private final BusinessModel mBusinessModel;



    private ArrayList<AssetTrackingBO> mDisplayAssetList;


    private DisplayAssetHelper(Context context) {
        this.mBusinessModel = (BusinessModel) context.getApplicationContext();
    }

    public static DisplayAssetHelper getInstance(Context context) {
        if (instance == null)
            instance = new DisplayAssetHelper(context);

        return instance;
    }


    public ArrayList<AssetTrackingBO> getDisplayAssetList() {
        if(mDisplayAssetList==null)
            mDisplayAssetList=new ArrayList<>();
        return mDisplayAssetList;
    }

    public void setDisplayAssetList(ArrayList<AssetTrackingBO> mDisplayAssetList) {
        this.mDisplayAssetList = mDisplayAssetList;
    }

    public void downloadDisplayAssets(Context context){

        mDisplayAssetList =new ArrayList<>();

        StringBuilder sb = new StringBuilder();
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {
            db.openDataBase();

            ArrayList<CompanyBO> companyList=new ArrayList<>();
            sb.append("select companyId,CompanyName,isOwn from CompanyMaster");
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                CompanyBO companyBO;
                while (c.moveToNext()) {
                    companyBO=new CompanyBO();
                    companyBO.setCompetitorid(c.getInt(0));
                    companyBO.setCompetitorName(c.getString(1));
                    companyBO.setIsOwn(c.getInt(2));

                    companyList.add(companyBO);

                }
            }


            sb=new StringBuilder();
            sb.append("select DisplayAssetId, DisplayAssetName, weightage from DisplayAssetMaster");
            c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                AssetTrackingBO assetTrackingBO;
                while (c.moveToNext()) {
                    assetTrackingBO=new AssetTrackingBO();
                    assetTrackingBO.setAssetID(c.getInt(0));
                    assetTrackingBO.setAssetName(c.getString(1));
                    assetTrackingBO.setWeightage(c.getDouble(2));
                    assetTrackingBO.setCompanyList(cloneCompanyList(companyList));
                    mDisplayAssetList.add(assetTrackingBO);


                }
            }

        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    public static ArrayList<CompanyBO> cloneCompanyList(
            ArrayList<CompanyBO> list) {
        ArrayList<CompanyBO> clone = new ArrayList<>(list.size());
        for (CompanyBO item : list)
            clone.add(new CompanyBO(item));

        return clone;
    }

}
