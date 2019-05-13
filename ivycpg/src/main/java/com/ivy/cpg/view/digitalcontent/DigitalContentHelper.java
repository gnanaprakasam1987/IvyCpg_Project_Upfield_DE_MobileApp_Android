package com.ivy.cpg.view.digitalcontent;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.widget.Toast;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DigitalContentBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
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
    boolean isProductMapped = false;

    public boolean isRetailerWiseDigitalContent() {
        return isRetailerWiseDigitalContent;
    }

    private boolean isRetailerWiseDigitalContent;// flag used to get the type of digital content


    private static final String CODE_FLOAT_DGT_CONTENT = "FUN77";
    public boolean SHOW_FLT_DGT_CONTENT;

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

    public boolean isProductMapped() {
        return isProductMapped;
    }

    public void setProductMapped(boolean productMapped) {
        isProductMapped = productMapped;
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
        String locationHierarchy = mBModel.channelMasterHelper.getLocationHierarchy(mContext);
        if (locationHierarchy != null && !"".equals(locationHierarchy)) {
            locIdScheme = "," + locationHierarchy;
        }

        /* Get channel id and its parent id */
        if (!"".equals(getChannelid(mContext)) &&
                getChannelid(mContext) != null) {
            channelId = "," + getChannelid(mContext);
        }
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
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
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            StringBuilder sBuffer = new StringBuilder();
            if ("SELLER".equals(value)) {
                mMappedImageIds = "0";
                isRetailerWiseDigitalContent=false;
            }
            else {
                mMappedImageIds = getDigitalContentTaggingDetails(mContext);
                isRetailerWiseDigitalContent=true;
            }

            if ("SELLER".equals(value)) {
                sBuffer.append("SELECT DISTINCT DC.Imageid  ,DC.ImageName ,DC.ImageDesc,DC.ImageDate,");
                sBuffer.append(" IFNULL(DCPM.Pid,0),IFNULL(PM.psname,''),IFNULL(SLM.ListName,'NA'),");
                sBuffer.append(" IFNULL(DC.GroupSequence,0),IFNULL(PM.ParentHierarchy,''),DC.allowSharing");
                sBuffer.append(" FROM  DigitalContentMaster DC");
                sBuffer.append(" INNER JOIN DigitalContentMapping DCM ON DC.Imageid = DCM.Imgid");
                sBuffer.append(" LEFT JOIN DigitalContentProductMapping DCPM ON DC.Imageid = DCPM .Imgid");
                sBuffer.append(" LEFT JOIN ProductMaster PM on PM.pid=DCPM.pid");
                sBuffer.append(" LEFT JOIN StandardListMaster SLM ON SLM.ListId = DC.GroupLovID");
                sBuffer.append(" where mappingid=0 and DCM.mappingtype='SELLER'  ORDER BY GroupSequence asc");

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
                        product.setParentHierarchy(c.getString(8));
                        if(c.getInt(9)==1){
                            product.setAllowSharing(true);
                        }
                        else product.setAllowSharing(false);

                        digitalMaster.add(product);
                        if(c.getInt(4) != 0)
                            setProductMapped(true);
                    }
                    c.close();
                }

            } else {

                sBuffer.append("SELECT DISTINCT DC.Imageid  ,DC.ImageName ,DC.ImageDesc,DC.ImageDate,");
                sBuffer.append(" IFNULL(DCPM.Pid,0),PM.psname,IFNULL(SLM.ListName,'NA'),");
                sBuffer.append(" IFNULL(DC.GroupSequence,0),IFNULL(PM.ParentHierarchy,''),DC.allowSharing");
                sBuffer.append(" FROM  DigitalContentMaster DC");
                sBuffer.append(" INNER JOIN DigitalContentMapping DCM ON (DC.Imageid = DCM.Imgid )");
                sBuffer.append(" LEFT JOIN DigitalContentProductMapping DCPM ON DC.Imageid = DCPM .Imgid");
                sBuffer.append(" LEFT JOIN ProductMaster PM on PM.pid=DCPM.pid");
                sBuffer.append(" LEFT JOIN StandardListMaster SLM ON SLM.ListId = DC.GroupLovID");
                sBuffer.append(" where DC.Imageid IN(");
                sBuffer.append(mMappedImageIds);
                sBuffer.append(") and DCM.mappingtype!='SELLER' ORDER BY GroupSequence asc");

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
                        product.setParentHierarchy(c.getString(8));

                        if(c.getInt(9)==1){
                            product.setAllowSharing(true);
                        }
                        else product.setAllowSharing(false);

                        digitalMaster.add(product);
                        if(c.getInt(4) != 0)
                            setProductMapped(true);
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
        DBUtil db = new DBUtil(mAppContext, DataMembers.DB_NAME);
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


            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );

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

    public void saveDigitalContentDetails(Context context, String digiContentId, String productID, String startTime, String endTime,
                                          boolean isFastForward) {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME
        );
        db.openDataBase();

        try {
            String tid = "";
            String content;
            String retailerId = mBModel.getRetailerMasterBO().getRetailerID() != null ? mBModel.getRetailerMasterBO().getRetailerID() : "0";

            Cursor c = db.selectSQL("select UId from DigitalContentTrackingHeader where DId =" +
                    digiContentId + " and RetailerId = " + StringUtils.QT(retailerId) +
                    " and upload='N' and Date=" + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)));

            if (c != null && c.getCount() > 0 && c.moveToNext()) {
                tid = c.getString(0);
                c.close();
            }

            if (tid.equals("")) {
                tid = mBModel.userMasterHelper.getUserMasterBO().getUserid()
                        + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID) + "";

                // UId,DId,RetailerId,Date
                content = StringUtils.QT(tid) + ","
                        + StringUtils.QT(digiContentId) + ","
                        + StringUtils.QT(retailerId) + ","
                        + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                ;

                db.insertSQL(DataMembers.tbl_DigitalContent_Tracking_Header,
                        DataMembers.tbl_DigitalContent_Tracking_Header_cols, content);
            }

            // UId,DId,UserID,RetailerId,StartTime,EndTime,PId,isFastForwarded
            content = StringUtils.QT(tid) + ","
                    + StringUtils.QT(startTime) + ","//startTime
                    + StringUtils.QT(endTime) + ","//EndTime
                    + StringUtils.QT(productID) + ","//Product Id
                    + "'" + isFastForward + "'"
            ;

            db.insertSQL(DataMembers.tbl_DigitalContent_Tracking_Detail,
                    DataMembers.tbl_DigitalContent_Tracking_Detail_cols, content);

            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
            db.closeDB();
        }
    }

    public void loadFloatingDgtConfig(Context context) {
        SHOW_FLT_DGT_CONTENT = false;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            Cursor c = db.selectSQL("select Flag from HhtModuleMaster where hhtcode = " + StringUtils.QT(CODE_FLOAT_DGT_CONTENT) + " and  " +
                    "menu_type= 'FLT_DGT_CNT'and ForSwitchSeller = 0");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    SHOW_FLT_DGT_CONTENT = c.getInt(0) == 1;

                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        } finally {
            db.closeDB();
        }
    }

    public void shareDigitalContent(Context context,String attachmentName,String fileName){

        String toEmailId="";
        String subject;
        if(isRetailerWiseDigitalContent()){
            subject="Retailer Digital content: "+attachmentName;
            toEmailId=mBModel.getAppDataProvider().getRetailMaster().getEmail();
        }
        else {
            subject="Seller Digital Content: "+attachmentName;
        }


        Uri path;
        File file = new File(
                context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.DIGITALCONTENT + "/" + fileName);

        if(file.exists()){
        if (Build.VERSION.SDK_INT >= 24) {
            path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            path = Uri.fromFile(file);
        }


        Intent intent = new Intent(android.content.Intent.ACTION_SEND);
        intent.setType("vnd.android.cursor.dir/email");
        /*final PackageManager pm = context.getPackageManager();
        final List<ResolveInfo> matches = pm.queryIntentActivities(intent, 0);
        ResolveInfo best = null;
        for (final ResolveInfo info : matches)
            if (info.activityInfo.packageName.endsWith(".gm") ||
                    info.activityInfo.name.toLowerCase().contains("gmail")) best = info;
        if (best != null)
            intent.setClassName(best.activityInfo.packageName, best.activityInfo.name);
*/
        intent.putExtra(Intent.EXTRA_EMAIL, new String[]{toEmailId});
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_STREAM, path);
        //context.startActivity(intent);
            context.startActivity(Intent.createChooser(intent , "Send email..."));
        } else {
            Toast.makeText(context,
                    context.getResources().getString(R.string.file_not_found),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
