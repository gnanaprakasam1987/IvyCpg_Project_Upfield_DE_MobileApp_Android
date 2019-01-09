package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.NonproductivereasonBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class CloseCallHelper {

    private Context mContext;
    private BusinessModel bmodel;
    private static CloseCallHelper instance = null;
    private int RefId;

    public int getRefId() {
        return RefId;
    }

    public void setRefId(int refId) {
        RefId = refId;
    }

    protected CloseCallHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static CloseCallHelper getInstance(Context context) {
        if (instance == null) {
            instance = new CloseCallHelper(context);
        }
        return instance;
    }

    public String getStandardListId(String listCode) {
        String listID = "";
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListId from StandardListMaster where ListCode='"
                            + listCode + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    listID = c.getString(0);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
        return listID;
    }

    public boolean isValidOtp(String code) {

        Calendar cal = Calendar.getInstance();
        System.out.println("Current time => " + cal.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = df.format(cal.getTime());

        boolean valid = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select  RefId from RetailerVerification where RetailerId = "
                            + bmodel.QT(bmodel.getRetailerMasterBO()
                            .getRetailerID())
                            + " AND  '"
                            + formattedDate
                            + "'  between ValidFrom and  ValidTo "
                            + "AND IsValidated!='1' AND Code ='" + code + "'");
            if (c != null) {
                if (c.moveToNext()) {
                    setRefId(c.getInt(0));
                    valid = true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
        }
        return valid;

    }


    public boolean isAllowOtp() {

        Calendar cal = Calendar.getInstance();
        System.out.println("Current time => " + cal.getTime());

        SimpleDateFormat df = new SimpleDateFormat("yyyy/MM/dd");
        String formattedDate = df.format(cal.getTime());

        boolean valid = false;
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select  count(RefId) from RetailerVerification where RetailerId = "
                            + bmodel.QT(bmodel.getRetailerMasterBO()
                            .getRetailerID())
                            + " AND  '"
                            + formattedDate
                            + "'  between ValidFrom and  ValidTo "
                            + "AND IsValidated='1'");
            if (c != null) {
                if (c.moveToNext()) {
                    if (c.getInt(0) > 0)
                        valid = true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return valid;
        }
        return valid;

    }


    public void updateOtp() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            db.updateSQL("UPDATE RetailerVerification SET IsValidated='1' where RefId = " + RefId);
                        /*	+ bmodel.QT(bmodel.getRetailerMasterBO()
                                    .getRetailerID())
							+ " AND  '"
							+ formattedDate
							+ "'  between ValidFrom and  ValidTo "
							+ " AND Code ='" + code + "'");*/

            db.closeDB();
        } catch (Exception e) {
        }

    }

    public void saveCloseCallreason(NonproductivereasonBO outlet) {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            String values;
            db.createDataBase();
            db.openDataBase();

            String id = AppUtils.QT(bmodel.getAppDataProvider().getUser()
                    .getDistributorid()
                    + ""
                    + bmodel.getAppDataProvider().getUser().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));

            db.deleteSQL(
                    "Nonproductivereasonmaster",
                    "RetailerID="
                            + AppUtils.QT(bmodel.getAppDataProvider().getRetailMaster()
                            .getRetailerID())
                            + " and DistributorID="
                            +bmodel.getAppDataProvider().getRetailMaster()
                            .getDistributorId()
                            + " and ReasonTypes="
                            + AppUtils.QT(getStandardListId(outlet
                            .getReasontype())) + " and RouteID="
                            + bmodel.getAppDataProvider().getRetailMaster().getBeatID(), false);

            String columns = "UID,RetailerID,RouteID,Date,ReasonID,ReasonTypes,upload,DistributorID,ridSF";
            values = id + ","
                    + AppUtils.QT(bmodel.getAppDataProvider().getRetailMaster().getRetailerID())
                    + "," + bmodel.getAppDataProvider().getRetailMaster().getBeatID() + ","
                    + AppUtils.QT(outlet.getDate()) + ","
                    + AppUtils.QT(outlet.getReasonid()) + ","
                    + AppUtils.QT(getStandardListId(outlet.getReasontype()))
                    + "," + AppUtils.QT("N")+ "," +outlet.getDistributorID()
                    + "," + AppUtils.QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF());

            db.insertSQL("Nonproductivereasonmaster", columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public boolean isNonProductiveCloaseCallDone() {
        DBUtil db = null;
        try {
            db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            String values;
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select count(UID) from Nonproductivereasonmaster where retailerid=");
            sb.append(bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID()));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                if (c.moveToNext())
                    c.close();
                db.closeDB();
                return true;
            }


        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            db.closeDB();
        }


        return false;
    }
}
