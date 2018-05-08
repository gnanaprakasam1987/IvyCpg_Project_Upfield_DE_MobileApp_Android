package com.ivy.cpg.view.digitalcontent;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

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


    private final BusinessModel mBModel;
    private static DigitalContentHelper instance;
    private Vector<DigitalContentBO> digitalMaster;
    private ArrayList<DigitalContentBO> filteredDigitalMaster;
    public String mSelectedActivityName;

    private DigitalContentHelper(Context context) {
        mBModel = (BusinessModel) context.getApplicationContext();
    }

    public static DigitalContentHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DigitalContentHelper(context);
        }
        return instance;
    }

    public void clearInstance() {
        instance = null;
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
    private String getDigitalContentTaggingDetails(Context mContext) {
        String mappingId = "-1";
        StringBuilder sb = new StringBuilder();

        String locIdScheme = "";
        String channelId = "";

        ArrayList<String> mappingIdList = new ArrayList<>();

        /* Get location id and its parent id */
        if (!"".equals(mBModel.schemeDetailsMasterHelper.getLocationIdsForScheme()) &&
                mBModel.schemeDetailsMasterHelper.getLocationIdsForScheme() != null) {
            locIdScheme = "," + mBModel.schemeDetailsMasterHelper.getLocationIdsForScheme();
        }

        /* Get channel id and its parent id */
        if (!"".equals(getChannelid(mContext)) &&
                getChannelid(mContext) != null) {
            channelId = "," + getChannelid(mContext);
        }
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();

            sb.append("SELECT Distinct DigitalContent.imgid,DigitalContent.GroupId," +
                    "IfNull(LocationId,0) AS LocationId," +
                    "IfNull(ChannelId,0) AS ChannelId," +
                    "IfNull(RetailerID,0) AS RetailerID," +
                    "IfNull(AccountID,0) AS AccountID," +
                    "IfNull(CounterID,0) AS CounterID " +

                    " FROM (SELECT  DISTINCT imgid,GroupId FROM DigitalContentMapping) AS DigitalContent " +

                    " LEFT JOIN  (SELECT DISTINCT imgid,GroupId,mappingid LocationId  FROM DigitalContentMapping " +
                    " where mappingtype in('LOCATION'))  LS ON DigitalContent.imgid=Ls.imgid and DigitalContent.GroupId=LS.GroupId " +

                    " LEFT JOIN (SELECT imgid,GroupId,mappingid ChannelId FROM DigitalContentMapping " +
                    " where mappingtype in('CHL_L1','CHL_L2')) CS ON  DigitalContent.imgid=CS.imgid and DigitalContent.GroupId=CS.GroupId " +

                    " LEFT JOIN (SELECT DISTINCT imgid,GroupId,mappingid RetailerID FROM DigitalContentMapping " +
                    " where mappingtype in('RETAILER')) RTR ON   DigitalContent.imgid=RTR.imgid and DigitalContent.GroupId=RTR.GroupId " +

                    " LEFT JOIN (SELECT DISTINCT imgid,GroupId,mappingid AccountID FROM DigitalContentMapping " +
                    " where mappingtype in('ACCOUNT')) ACC ON   DigitalContent.imgid=ACC.imgid and DigitalContent.GroupId=ACC.GroupId " +

                    " LEFT JOIN (SELECT DISTINCT imgid,GroupId,mappingid CounterID FROM DigitalContentMapping " +
                    " where mappingtype in('COUNTER')) CUN ON   DigitalContent.imgid=CUN.imgid and DigitalContent.GroupId=CUN.GroupId " +

                    " where ifNull(locationid,0) in(0" + locIdScheme + "," + mBModel.getRetailerMasterBO().getLocationId() + ")" +
                    " And ifnull(channelid,0) in (0" + channelId + "," + mBModel.getRetailerMasterBO().getSubchannelid() + ")" +
                    " And ifnull(RetailerID,0) in (0," + mBModel.getRetailerMasterBO().getRetailerID() + ")" +
                    " And ifnull(AccountID,0) in (0," + mBModel.getRetailerMasterBO().getAccountid() + ")" +
                    " And ifnull(CounterID,0) in (0," + mBModel.getCounterId() + ")");


            Cursor c1 = db.selectSQL(sb.toString());

            if (c1 != null) {
                mappingIdList = new ArrayList<>();
                while (c1.moveToNext()) {
                    mappingIdList.add(c1.getString(0));
                }
                c1.close();
            }
            db.closeDB();

            if (mappingIdList.size() > 0) {
                mappingId = addCommaSeparator(mappingIdList);
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return mappingId;
    }


    private String addCommaSeparator(ArrayList<String> array) {
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
    public void downloadDigitalContent(Context mContext, String value) {
        DigitalContentBO product;
        String mMappedImageIds;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sBuffer = new StringBuilder();
            if ("SELLER".equals(value))
                mMappedImageIds = "0";
            else
                mMappedImageIds = getDigitalContentTaggingDetails(mContext);

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
                sBuffer.append(" where DC.Imageid IN(");
                sBuffer.append(mMappedImageIds);
                sBuffer.append(") and DCM.mappingtype!='SELLER' ORDER BY GroupSequence asc ");

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
    public void setDigitalContentInDB(Context mAppContext) {
        DBUtil db = new DBUtil(mAppContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        db.executeQ("update " + DataMembers.tbl_retailerMaster
                + " set isDigitalContent=" + mBModel.QT("Y") + " where retailerid="
                + mBModel.QT(mBModel.getRetailerMasterBO().getRetailerID()));
        db.closeDB();
    }

    public String getChannelid(Context context) {
        String sql;
        String sql1 = "";
        String str = "";
        int channelid = 0;
        try {
            if (mBModel.getRetailerMasterBO() != null)
                channelid = mBModel.getRetailerMasterBO().getSubchannelid();


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select min(Sequence) as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelid + ") as contentlevel  from ChannelLevel");
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
                c.close();
            }

            int loopEnd = mContentLevel - mChildLevel + 1;

            for (int i = 2; i <= loopEnd; i++) {
                sql1 = sql1 + " LM" + i + ".ChId";
                if (i != loopEnd)
                    sql1 = sql1 + ",";
            }
            sql = "select LM1.ChId," + sql1 + "  from ChannelHierarchy LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                        + ".ParentId = LM" + i + ".ChId";
            sql = sql + " where LM1.ChId=" + channelid;
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }
                    if (str.endsWith(","))
                        str = str.substring(0, str.length() - 1);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return str;
    }

}
