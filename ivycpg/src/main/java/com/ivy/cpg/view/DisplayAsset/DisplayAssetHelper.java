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
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

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
                    assetTrackingBO.setDisplayAssetId(c.getString(0));
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

    private static ArrayList<CompanyBO> cloneCompanyList(
            ArrayList<CompanyBO> list) {
        ArrayList<CompanyBO> clone = new ArrayList<>(list.size());
        for (CompanyBO item : list)
            clone.add(new CompanyBO(item));

        return clone;
    }

    public boolean saveDisplayAsset(Context context,String status,double ownCompanyScore,double otherCompanyMaxScore){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        try {

            db.openDataBase();

            String query = "select uid from DisplayAssetTrackingHeader where retailerid ="
                    + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID();

            Cursor c = db.selectSQL(query);
            if (c.getCount() > 0) {
                c.moveToNext();
                db.deleteSQL(DataMembers.tbl_DisplayAssetHeader,
                        "uid=" + StringUtils.QT(c.getString(0)), false);
                db.deleteSQL(DataMembers.tbl_DisplayAssetTDetails,
                        "uid=" + StringUtils.QT(c.getString(0)), false);

            }

            String id = mBusinessModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

            String headerColumns = "Uid,RetailerId,ridSF,visitId,Date,status,ownShare,competitorShare";
            String detailColumns = "Uid,CompetitorId,DisplayAssetId,count,weightage,score";




            boolean isData=false;
            for(AssetTrackingBO assetTrackingBO:getDisplayAssetList()) {
                for (CompanyBO companyBO : assetTrackingBO.getCompanyList()) {

                    String detailValues = StringUtils.QT(id) + ","
                            + companyBO.getCompetitorid() + ","
                            + StringUtils.QT(assetTrackingBO.getDisplayAssetId()) + "," + companyBO.getQuantity() + ","
                            + assetTrackingBO.getWeightage() + ","
                            + (companyBO.getQuantity()*assetTrackingBO.getWeightage());

                    db.insertSQL(DataMembers.tbl_DisplayAssetTDetails, detailColumns,
                            detailValues);
                    isData=true;
                }
            }

            if(isData) {
                String headerValues = StringUtils.QT(id) + ","
                        +  mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + ","
                        + StringUtils.QT(mBusinessModel.getAppDataProvider().getRetailMaster().getRidSF())
                        + "," + mBusinessModel.getAppDataProvider().getUniqueId() + ","
                        + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                        + StringUtils.QT(status)+
                        ","+ownCompanyScore+","+otherCompanyMaxScore;

                db.insertSQL(DataMembers.tbl_DisplayAssetHeader, headerColumns,
                        headerValues);
            }

            mBusinessModel.saveModuleCompletion("MENU_DISPLAY_ASSET", true);
            mBusinessModel.outletTimeStampHelper
                    .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));

            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
            return false;
        }

        return true;
    }


    public void loadDisplayAssetInEditMode(Context context){

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();

            if(true||mBusinessModel.configurationMasterHelper.IS_DISPLAY_ASSET_RETAIN_LAST_VISIT_TRAN) {
                // LastVisit
                String lastVisitQuery = "SELECT CompetitorId,DisplayAssetId,count"
                        + " FROM LastVisitDisplayAsset WHERE retailerId=" + mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID();

                Cursor lastVisitCursor = db.selectSQL(lastVisitQuery);

                if (lastVisitCursor != null) {
                    while (lastVisitCursor.moveToNext()) {
                        for(AssetTrackingBO assetTrackingBO:getDisplayAssetList()) {
                            for (CompanyBO companyBO : assetTrackingBO.getCompanyList()) {
                                if(lastVisitCursor.getString(1).equals(assetTrackingBO.getDisplayAssetId())&&lastVisitCursor.getInt(0)==companyBO.getCompetitorid()){
                                    companyBO.setQuantity(lastVisitCursor.getInt(2));
                                }

                            }
                        }

                    }
                    lastVisitCursor.close();
                }
            }

            String tid = "";
            String sql = "SELECT uid FROM DisplayAssetTrackingHeader WHERE RetailerId = "
                    +  mBusinessModel.getAppDataProvider().getRetailMaster().getRetailerID() + " AND Date = "
                    + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + " and (upload='N')";

            Cursor orderHeaderCursor = db.selectSQL(sql);
            if (orderHeaderCursor != null) {
                if (orderHeaderCursor.moveToNext())
                    tid = orderHeaderCursor.getString(0);
            }

            orderHeaderCursor.close();

            if(!tid.trim().isEmpty()){

                String sql1 = "SELECT CompetitorId, DisplayAssetId, count"
                        + " FROM DisplayAssetTrackingDetails WHERE uid=" + StringUtils.QT(tid);

                Cursor orderDetailCursor = db.selectSQL(sql1);

                if (orderDetailCursor != null) {
                    while (orderDetailCursor.moveToNext()) {
                        for(AssetTrackingBO assetTrackingBO:getDisplayAssetList()) {
                            for (CompanyBO companyBO : assetTrackingBO.getCompanyList()) {
                                if(orderDetailCursor.getString(1).equals(assetTrackingBO.getDisplayAssetId())&&orderDetailCursor.getInt(0)==companyBO.getCompetitorid()){
                                    companyBO.setQuantity(orderDetailCursor.getInt(2));
                                }

                            }
                        }
                    }
                    orderDetailCursor.close();
                }

            }

        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }
}
