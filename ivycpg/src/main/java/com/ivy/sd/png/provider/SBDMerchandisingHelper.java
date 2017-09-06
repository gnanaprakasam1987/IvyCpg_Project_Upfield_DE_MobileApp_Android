package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.MerchandisingposmBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SBDMerchandisingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

public class SBDMerchandisingHelper {

    private Context context;
    private BusinessModel bmodel;
    private Vector<SBDMerchandisingBO> sbdMerchandisingBO;
    private ArrayList<MerchandisingposmBO> merchandisingposm;
    private static SBDMerchandisingHelper instance = null;
    private ArrayList<MerchandisingposmBO> sbdMerchCoverageBO;

    public String merchTypeListCode;
    public String merchInitTypeListCode;

    protected SBDMerchandisingHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
        merchandisingposm = new ArrayList<MerchandisingposmBO>();
    }

    public static SBDMerchandisingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SBDMerchandisingHelper(context);
        }
        return instance;
    }

    // ******************************* SBD Merchandising

    public Vector<SBDMerchandisingBO> downloadSBDMerchandising(
            String typeListCode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c;

            if ((bmodel.getRetailerMasterBO().getIsMerchandisingDone()
                    .equals("Y") && typeListCode.equals("MERCH"))
                    || (bmodel.getRetailerMasterBO()
                    .getIsInitMerchandisingDone().equals("Y") && typeListCode
                    .equals("MERCH_INIT"))) {
                c = db.selectSQL(" select A.sbdid,A.brandid,A.VisibilityListid,A.Value,B.ListCode, CASE  WHEN "
                        + " B.ListCode = 'SS' THEN A.Value||' ' || B.ListName ELSE PM.PosmDesc END as valueText,"
                        + " ifnull(BR.PName, 'COMMON'),ifnull(PM.TypeLovId,'Brand'),A.TypeListID , case when ifnull(SMD.sbdid,0)>0 and (SMD.isHit)>0 then 1 "
                        + " else 0  end as IsDone from SbdMerchandisingMaster A inner  join StandardListMaster B  on "
                        + " A.VisibilityListid = B.Listid and A.ChannelId="
                        + bmodel.getRetailerMasterBO().getChannelID()
                        + " LEFT JOIN PosmMaster PM ON "
                        + " A.Value = PM.PosmId "
                        + "LEFT JOIN ProductMaster BR on A.brandid=BR.PID "
                        + " LEFT JOIN SbdMerchandisingDetail SMD on SMD.sbdid=A.sbdid and SMD.brandid=A.brandid"
                        + " and SMD.VisibilityListid=A.VisibilityListid and SMD.value=A.value and SMD.retailerid= "
                        + Utils.QT(bmodel.getRetailerMasterBO().getRetailerID())
                        + " and SMD.TypeListId=(select Listid from StandardListMaster where ListCode='"
                        + typeListCode
                        + "') where A.TypeListId=(select Listid from StandardListMaster where ListCode='"
                        + typeListCode + "')");
            } else {
                c = db.selectSQL(" select A.sbdid,A.brandid,A.VisibilityListid,A.Value,B.ListCode, CASE  WHEN "
                        + " B.ListCode = 'SS' THEN A.Value||' ' || B.ListName ELSE PM.PosmDesc END "
                        + " as valueText,ifnull(BR.PName, 'COMMON'),ifnull(PM.TypeLovId,'Brand'),A.TypeListID , case when "
                        + " ifnull(SMAM.sbdid,0)>0 then 1 else 0 end as IsDone from SbdMerchandisingMaster A "
                        + " inner  join StandardListMaster B  on A.VisibilityListid =B.Listid and "
                        + " A.ChannelId="
                        + bmodel.getRetailerMasterBO().getChannelID()
                        + " LEFT JOIN PosmMaster PM ON A.Value = PM.PosmId "
                        + " LEFT JOIN ProductMaster BR on A.brandid=BR.PID"
                        + " LEFT JOIN SbdMerchandisingAchievedMaster SMAM on SMAM.sbdid=A.sbdid and SMAM.brandid=A.brandid "//
                        + " and SMAM.VisibilityListid=A.VisibilityListid and SMAM.value=A.value and "
                        + " SMAM.retailerid="
                        + Utils.QT(bmodel.getRetailerMasterBO().getRetailerID())
                        + " and SMAM.TypeListId=(select Listid from StandardListMaster where ListCode='"
                        + typeListCode
                        + "') where A.TypeListId=(select Listid from StandardListMaster where ListCode='"
                        + typeListCode + "')");

            }

            sbdMerchandisingBO = new Vector<SBDMerchandisingBO>();
            if (c != null) {
                while (c.moveToNext()) {
                    SBDMerchandisingBO sbdMerchandising = new SBDMerchandisingBO();
                    sbdMerchandising.setSbdid(c.getInt(0));
                    sbdMerchandising.setBrandid(c.getInt(1));
                    sbdMerchandising.setVisibilityListId(c.getInt(2));
                    sbdMerchandising.setValue(c.getString(3));
                    sbdMerchandising.setListCode(c.getString(4));
                    // sbdMerchandising.setWits_flag(c.getString(5));
                    sbdMerchandising.setValueText(c.getString(5) + "");
                    sbdMerchandising.setBrandName(c.getString(6));
                    sbdMerchandising.setTypeListId(c.getInt(8));

                    sbdMerchandising.setDone(c.getInt(9) > 0 ? true : false);

                    sbdMerchandisingBO.add(sbdMerchandising);
                }
            c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return sbdMerchandisingBO;
    }

    public void setMerchTypeCodes() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select Listid from StandardListMaster where ListCode='MERCH'");
        if (c != null) {
            if (c.moveToNext()) {
                merchTypeListCode = c.getString(0);
            }

        }

        c = db.selectSQL("select Listid from StandardListMaster where ListCode='MERCH_INIT'");
        if (c != null) {
            if (c.moveToNext()) {
                merchInitTypeListCode = c.getString(0);
            }

        }
    }

    public void setSBDStatus(SBDMerchandisingBO sbd, boolean flag) {
        SBDMerchandisingBO sBDMerchandisingBO;
        int siz = sbdMerchandisingBO.size();
        if (siz == 0)
            return;
        for (int i = 0; i < siz; ++i) {
            sBDMerchandisingBO = (SBDMerchandisingBO) sbdMerchandisingBO.get(i);
            if (sBDMerchandisingBO.getBrandid() == sbd.getBrandid()
                    && sBDMerchandisingBO.getValue().equals(sbd.getValue())) {
                sBDMerchandisingBO.setDone(flag);
                sbdMerchandisingBO.setElementAt(sBDMerchandisingBO, i);
                return;
            }
        }
        return;
    }

    public void clearMerchandising() {
        SBDMerchandisingBO product;
        int siz = sbdMerchandisingBO.size();
        for (int i = 0; i < siz; ++i) {
            product = (SBDMerchandisingBO) sbdMerchandisingBO.get(i);
            product.setDone(false);
        }
    }

    public String QT(String data) // Quote
    {
        return "'" + data + "'";
    }

    public void saveSBDMerchandising(String Flag) {
        try {
            SBDMerchandisingBO sbdbo;

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String uid = QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));

            deleteSBDMerchEdit(Flag);

            // Header Entry
            String columns = "uid,date,RetailerId,beatid,channelid, flag";
            String values = uid
                    + ","
                    + QT(SDUtil.now(SDUtil.DATE_GLOBAL) + " "
                    + SDUtil.now(SDUtil.TIME)) + ","
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()) + ","
                    + bmodel.getRetailerMasterBO().getBeatID() + ","
                    + bmodel.getRetailerMasterBO().getChannelID() + ","
                    + QT(Flag);
            db.insertSQL(DataMembers.tbl_SbdMerchandisingHeader, columns,
                    values);

            // Details Entry
            columns = "uid,VisibilityListid,value,sbdid,brandid, RetailerId, flag,TypeListId, isHit";
            int siz = sbdMerchandisingBO.size();

            for (int i = 0; i < siz; ++i) {
                sbdbo = (SBDMerchandisingBO) sbdMerchandisingBO.elementAt(i);

                String isHit = sbdbo.isDone() ? "1" : "0";

                values = uid + "," + sbdbo.getVisibilityListId() + ","
                        + QT(sbdbo.getValue()) + "," + sbdbo.getSbdid() + ","
                        + sbdbo.getBrandid() + ","
                        + QT(bmodel.getRetailerMasterBO().getRetailerID())
                        + "," + QT(Flag) + "," + sbdbo.getTypeListId() + ","
                        + isHit;
                db.insertSQL(DataMembers.tbl_SbdMerchandisingDetail, columns,
                        values);

            }

            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public Vector<SBDMerchandisingBO> getSbdMerchandisingBO() {
        return sbdMerchandisingBO;
    }

    public void setSbdMerchandisingBO(
            Vector<SBDMerchandisingBO> sbdMerchandisingBO) {
        this.sbdMerchandisingBO = sbdMerchandisingBO;
    }

    public String[] getDistinctBrandIdOfSBDMerchandising(String typeListCode) {

        String brandIdArray[] = null;

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select distinct A.brandid from SbdMerchandisingMaster A inner join ProductMaster B "
                            + "on A.brandid=B.PID where ChannelId="
                            + bmodel.retailerMasterBO.getChannelID()
                            + " and A.TypeListId=(select Listid from StandardListMaster where ListCode='"
                            + typeListCode + "') order by B.PName");

            int i = 0;
            if (c != null) {
                brandIdArray = new String[c.getCount() + 1];
                while (c.moveToNext()) {
                    brandIdArray[i] = c.getString(0);
                    i++;
                }
            c.close();
            }
            brandIdArray[i] = "0";
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return brandIdArray;
        }
        return brandIdArray;
    }

    private void deleteSBDMerchEdit(String flag) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.executeQ("delete from " + DataMembers.tbl_SbdMerchandisingHeader
                    + " where retailerid="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and flag = " + QT(flag));
            db.executeQ("delete from " + DataMembers.tbl_SbdMerchandisingDetail
                    + " where retailerid="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID())
                    + " and flag = " + QT(flag));
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void setSBDDistributionPercent(String sbd) {
        RetailerMasterBO retailer;
        int siz = bmodel.getRetailerMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = (RetailerMasterBO) bmodel.getRetailerMaster().get(i);
            if (retailer.getRetailerID().equals(
                    bmodel.getRetailerMasterBO().getRetailerID())) {
                retailer.setSbdDistpercent(sbd);
                bmodel.getRetailerMaster().setElementAt(retailer, i);
            }
        }

    }

    /**
     * Calculate the SBD Merchandising Distribution precentage for a particular
     * retailer and update it to RetailerMaster . Also update RPS_Merch_Achieved
     * count in retailer master.
     * <p/>
     * This method will be called after SDB Merchandising save has been done.
     */
    public void updateSBDMerchandisingAchieved() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;
            int acheived = 0;

            c = db.selectSQL(" select B.TypeListId,B.BrandId, value from SbdMerchandisingHeader A inner join "
                    + " SbdMerchandisingdetail B on A.uid=B.uid AND  A.retailerid = '"
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + "' and "
                    + " B.TypeListId=(select ListId from StandardListMaster where Listcode='MERCH')  WHERE "
                    + " TypeListId=(select ListId from StandardListMaster where Listcode='MERCH') and "
                    + " A.RetailerId = '"
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + "' and B.isHit = '1' ");

            if (c != null) {
                acheived = c.getCount();
            c.close();
            }

            /** calculate the precentage value **/
            float precent = 0;
            ;
            if (bmodel.getRetailerMasterBO().getSBDMerchTarget() == 0) {
                precent = 0;
            } else {
                precent = (((float) acheived / (float) bmodel
                        .getRetailerMasterBO().getSBDMerchTarget()) * 100);
            }

            /** set the value in the DB **/
            db.executeQ("update retailermaster set RPS_Merch_Achieved = "
                    + acheived + " where retailerid ="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()));

            db.executeQ("update retailermaster set sbdMerchpercent = "
                    + precent + " where retailerid ="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()));

            /** set the value in the BO **/
            RetailerMasterBO retailer;
            int siz = bmodel.getRetailerMaster().size();
            for (int i = 0; i < siz; ++i) {
                retailer = (RetailerMasterBO) bmodel.getRetailerMaster().get(i);
                if (retailer.getRetailerID().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())) {
                    retailer.setSbdMercPercent(precent + "");
                    retailer.setSBDMerchAchieved(acheived);
                    bmodel.getRetailerMaster().setElementAt(retailer, i);
                }
            }

            bmodel.getRetailerMasterBO().setSBDMerchAchieved(acheived);
            bmodel.getRetailerMasterBO().setSbdMercPercent(precent + "");

            db.closeDB();

        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    /**
     * Calculate the SBD Initiative Merchandising Distribution precentage for a
     * particular retailer and update it to RetailerMaster . Also update
     * RPS_Merch_Achieved count in retailer master.
     * <p/>
     * This method will be called after SDB Merchandising save has been done.
     */
    public void updateSBDInitMerchandisingAchieved() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = null;
            int acheived = 0;

            c = db.selectSQL(" select B.TypeListId,B.BrandId, value from SbdMerchandisingHeader A inner join "
                    + " SbdMerchandisingdetail B on A.uid=B.uid AND  A.retailerid = '"
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + "' and "
                    + " B.TypeListId=(select ListId from StandardListMaster where Listcode='MERCH_INIT')  WHERE "
                    + " TypeListId=(select ListId from StandardListMaster where Listcode='MERCH_INIT') and "
                    + " A.RetailerId = '"
                    + bmodel.getRetailerMasterBO().getRetailerID()
                    + "' and B.isHit = '1' ");

            if (c != null) {
                acheived = c.getCount();
            c.close();
            }

            /** calculate the precentage value **/
            float precent = 0;
            if (bmodel.getRetailerMasterBO().getSbdMerchInitTarget() == 0) {
                precent = 0;
            } else {
                precent = (((float) acheived / (float) bmodel
                        .getRetailerMasterBO().getSbdMerchInitTarget()) * 100);
            }

            /** set the value in the DB **/
            db.executeQ("update retailermaster set sbdMerchInitAcheived = "
                    + acheived + " where retailerid ="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()));

            db.executeQ("update retailermaster set sbdMerchInitPercent = "
                    + precent + " where retailerid ="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()));

            /** set the value in the BO **/
            RetailerMasterBO retailer;
            int siz = bmodel.getRetailerMaster().size();
            for (int i = 0; i < siz; ++i) {
                retailer = (RetailerMasterBO) bmodel.getRetailerMaster().get(i);
                if (retailer.getRetailerID().equals(
                        bmodel.getRetailerMasterBO().getRetailerID())) {
                    retailer.setSbdMerchInitPrecent(precent + "");
                    retailer.setSbdMerchInitAcheived(acheived);
                    bmodel.getRetailerMaster().setElementAt(retailer, i);
                }
            }

            bmodel.getRetailerMasterBO().setSbdMerchInitAcheived(acheived);
            bmodel.getRetailerMasterBO().setSbdMerchInitPrecent(precent + "");

            db.closeDB();

        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public void update_IsMerchandising_Done_Flag() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String query = "UPDATE RetailerMaster SET isMerchandisingDone = 'Y"
                    + "'  WHERE RetailerID = '"
                    + bmodel.retailerMasterBO.getRetailerID() + "'";
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void update_Is_Init_Merchandising_Done_Flag() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String query = "UPDATE RetailerMaster SET isInitMerchandisingDone = 'Y"
                    + "'  WHERE RetailerID = '"
                    + bmodel.retailerMasterBO.getRetailerID() + "'";
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void setIsMerchandisingDone(String flag) {
        RetailerMasterBO retailer;
        int siz = bmodel.getRetailerMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = (RetailerMasterBO) bmodel.getRetailerMaster().get(i);
            if (retailer.getRetailerID().equals(
                    bmodel.getRetailerMasterBO().getRetailerID())) {
                retailer.setIsMerchandisingDone(flag);
                bmodel.getRetailerMaster().setElementAt(retailer, i);
                return;
            }
        }

    }

    public void setIsInitMerchandisingDone(String flag) {
        RetailerMasterBO retailer;
        int siz = bmodel.getRetailerMaster().size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; ++i) {
            retailer = (RetailerMasterBO) bmodel.getRetailerMaster().get(i);
            if (retailer.getRetailerID().equals(
                    bmodel.getRetailerMasterBO().getRetailerID())) {
                retailer.setIsInitMerchandisingDone(flag);
                bmodel.getRetailerMaster().setElementAt(retailer, i);
                return;
            }
        }

    }

    public void loadSbdMerchCoverage() {
        MerchandisingposmBO dash;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select  PosmDesc, SbdMerchTgt,SbdMerchAchd,TypeListId from SbdMerchCoverage");
        if (c != null) {
            sbdMerchCoverageBO = new ArrayList<MerchandisingposmBO>();
            while (c.moveToNext()) {
                dash = new MerchandisingposmBO();
                dash.setPosmdescription(c.getString(0));
                dash.setPosmValue(c.getString(2) + "/" + c.getString(1));
                dash.setTypeListId(c.getString(3));
                sbdMerchCoverageBO.add(dash);
            }
            c.close();
        }
        db.closeDB();
    }

    public ArrayList<MerchandisingposmBO> getSbdMerchCoverageBO() {
        return sbdMerchCoverageBO;
    }

}
