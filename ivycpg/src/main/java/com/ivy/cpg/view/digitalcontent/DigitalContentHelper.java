package com.ivy.cpg.view.digitalcontent;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.DigitalContentBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by Rajkumar.S on 4/12/17.
 */

public class DigitalContentHelper {


    private final Context context;
    private final BusinessModel mBModel;
    private static DigitalContentHelper instance;
    private Vector<DigitalContentBO> digitalMaster;
    private ArrayList<DigitalContentBO> filteredDigitalMaster;
    public String mSelectedActivityName;

    private DigitalContentHelper(Context context) {
        this.context = context;
        mBModel = (BusinessModel) context.getApplicationContext();
    }

    public static DigitalContentHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DigitalContentHelper(context);
        }
        return instance;
    }

    public Vector<DigitalContentBO> getDigitalMaster() {
        return digitalMaster;
    }

    public ArrayList<DigitalContentBO> getFilteredDigitalMaster() {
        return filteredDigitalMaster;
    }

    public void setFilteredDigitalMaster(ArrayList<DigitalContentBO> filteredDigitalMaster) {
        this.filteredDigitalMaster = new ArrayList<>();
        this.filteredDigitalMaster = filteredDigitalMaster;
    }

    /**
     * Get mapping Id based on the mapping type
     *
     * @return Mapping Id
     */
    private String getDigitalContentTaggingDetails() {
        String mappingId = "-1";
        ArrayList<String> mappingIdList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c1 = db
                    .selectSQL("SELECT mappingtype  FROM DigitalContentMapping WHERE mappingtype != 'SELLER'");

            if (c1 != null) {
                mappingIdList = new ArrayList<>();
                while (c1.moveToNext()) {
                    if (c1.getString(0).equals("CHL_L1"))
                        mappingIdList.add("" + mBModel.getRetailerMasterBO().getChannelID());
                    else if (c1.getString(0).equals("CHL_L2"))
                        mappingIdList.add("" + mBModel.getRetailerMasterBO().getSubchannelid());
                    else if (c1.getString(0).equals("RETAILER"))
                        mappingIdList.add("" + mBModel.getRetailerMasterBO().getRetailerID());
                    else if (c1.getString(0).equals("COUNTER"))
                        mappingIdList.add("" + mBModel.getCounterId());
                    else if (c1.getString(0).equals("ACCOUNT"))
                        mappingIdList.add("" + mBModel.getRetailerMasterBO().getAccountid());
                }
                c1.close();
            }
            db.closeDB();

            if (mappingIdList.size() > 0)
                mappingId = addCommaSeparator(mappingIdList);

            return mappingId;
        } catch (Exception e) {
            Commons.printException("" + e);
            return mappingId;
        }
    }


    public String addCommaSeparator(ArrayList<String> array) {
        String result = "";
        if (array.size() > 0) {
            StringBuilder sb = new StringBuilder();
            for (String s : array) {
                sb.append(s).append(",");
            }
            result = sb.deleteCharAt(sb.length() - 1).toString();
        }
        return result;
    }


    /**
     * Download Digital Content details for Seller and retailer wise
     *
     * @param value seller or Retailer
     */
    public void downloadDigitalContent(String value) {
        DigitalContentBO product;
        String mMappingId;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sBuffer = new StringBuilder();
            if ("SELLER".equals(value))
                mMappingId = "0";
            else
                mMappingId = getDigitalContentTaggingDetails();
            if ("SELLER".equals(value))

            {
                sBuffer.append("SELECT DISTINCT DC.Imageid  ,DC.ImageName ,DC.ImageDesc,DC.ImageDate,IFNULL(DCPM.Pid,0),IFNULL(PM.psname,''),IFNULL(SLM.ListName,'NA'),IFNULL(DC.GroupSequence,0) ");
                sBuffer.append(" FROM  DigitalContentMaster DC");
                sBuffer.append(" INNER JOIN DigitalContentMapping DCM ON DC.Imageid = DCM.Imgid  ");
                sBuffer.append(" LEFT JOIN DigitalContentProductMapping DCPM ON DC.Imageid = DCPM .Imgid ");
                sBuffer.append(" LEFT JOIN ProductMaster PM on PM.pid=DCPM.pid LEFT JOIN StandardListMaster SLM ON SLM.ListId = DC.GroupLovID");
                sBuffer.append(" where mappingid=0 and DCM.mappingtype='SELLER'  ORDER BY GroupSequence asc ");

                Cursor c = db.selectSQL(sBuffer.toString());
                if (c != null) {
                    digitalMaster = new Vector<>();
                    while (c.moveToNext()) {
                        product = new DigitalContentBO();
                        product.setImageID(c.getInt(0));
                        product.setFileName(c.getString(1));
                        product.setDescription(c.getString(2));
                        product.setImageDate(c.getString(3));
                        product.setProductID(c.getInt(4));
                        product.setProductName(c.getString(5));
                        product.setGroupName(c.getString(6));
                        product.setSequenceNo(c.getInt(7));
                        digitalMaster.add(product);
                    }
                    c.close();
                }

            } else {
                sBuffer.append("SELECT DISTINCT DC.Imageid  ,DC.ImageName ,DC.ImageDesc,DC.ImageDate,IFNULL(DCPM.Pid,0),PM.psname,IFNULL(SLM.ListName,'NA'),IFNULL(DC.GroupSequence,0) ");
                sBuffer.append(" FROM  DigitalContentMaster DC");
                sBuffer.append(" INNER JOIN DigitalContentMapping DCM ON (DC.Imageid = DCM.Imgid ) ");
                sBuffer.append(" LEFT JOIN DigitalContentProductMapping DCPM ON DC.Imageid = DCPM .Imgid ");
                sBuffer.append(" LEFT JOIN ProductMaster PM on PM.pid=DCPM.pid LEFT JOIN StandardListMaster SLM ON SLM.ListId = DC.GroupLovID");
                sBuffer.append(" where mappingid=");
                sBuffer.append(mMappingId);
                sBuffer.append(" and DCM.mappingtype!='SELLER' ORDER BY GroupSequence asc ");

                Cursor c = db.selectSQL(sBuffer.toString());
                if (c != null) {
                    digitalMaster = new Vector<>();
                    while (c.moveToNext()) {
                        product = new DigitalContentBO();
                        product.setImageID(c.getInt(0));
                        product.setFileName(c.getString(1));
                        product.setDescription(c.getString(2));
                        product.setImageDate(c.getString(3));
                        product.setProductID(c.getInt(4));
                        product.setProductName(c.getString(5));
                        product.setGroupName(c.getString(6));
                        product.setSequenceNo(c.getInt(7));

                        digitalMaster.add(product);
                    }
                    c.close();
                }

            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /**
     * Update digital content availability
     */
    public void setIsDigitalContent() {
        RetailerMasterBO retailer;
        int siz = mBModel.getRetailerMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = mBModel.getRetailerMaster().get(i);
            if (retailer.getRetailerID().equals(
                    mBModel.getRetailerMasterBO().getRetailerID())) {
                retailer.setIsDigitalContent("Y");
                mBModel.getRetailerMaster().setElementAt(retailer, i);
                return;
            }
        }

    }

    /**
     * Update digital content availability in Db
     */
    public void setDigitalContentInDB() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        db.executeQ("update " + DataMembers.tbl_retailerMaster
                + " set isDigitalContent=" + mBModel.QT("Y") + " where retailerid="
                + mBModel.QT(mBModel.getRetailerMasterBO().getRetailerID()));
        db.closeDB();
    }

}
