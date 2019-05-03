package com.ivy.cpg.view.profile.otpValidation;

import android.content.Context;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

public class OTPValidationHelper {

    private Context context;
    private BusinessModel bModel;
    public String tid;
    private static OTPValidationHelper instance = null;

    public OTPValidationHelper(Context ctx){
        context =ctx;
        bModel = (BusinessModel)ctx.getApplicationContext();
    }

    public static OTPValidationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OTPValidationHelper(context);
        }
        return instance;
    }

    public void saveOTPActivatedDate(String mRetailerId, int mType) {
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();
            String query = "";

            if (mType == 1)
                query = "UPDATE RetailerMaster SET StoreOTPActivated = '"
                        + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)
                        + "'  WHERE RetailerID = '" + mRetailerId + "'";
            else if (mType == 2)
                query = "UPDATE RetailerMaster SET SkipOTPActivated = '"
                        + DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)
                        + "'  WHERE RetailerID = '" + mRetailerId + "'";

            if (!query.equals(""))
                db.updateSQL(query);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("", e);
        }
    }

    public String saveOTPSkipReason(int reasonID, RetailerMasterBO mRetailerBO, String reasonType, int actualRadius) {

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            String values;
            db.createDataBase();
            db.openDataBase();

            String id;

            String columns = "Tid,RetailerID,ReasonID,Date,Type,ExpectedRadius,ActualRadius,upload";

            id = StringUtils.QT(bModel.getAppDataProvider().getUser()
                    .getDistributorid()
                    + ""
                    + bModel.getAppDataProvider().getUser().getUserid()
                    + "" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

            tid = id;

            values = id
                    + ","
                    + StringUtils.QT(mRetailerBO.getRetailerID())
                    + ","
                    + reasonID
                    + ","
                    + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
                    + "," + StringUtils.QT(reasonType)
                    + "," + mRetailerBO.getGpsDistance()
                    + "," + actualRadius
                    + "," + StringUtils.QT("N");

            /*db.deleteSQL(
                    "RetailerLocationDeviation",
                    "RetailerID="
                            + StringUtils.QT(mRetailerBO.getRetailerID())
                            + " and Type="
                            + StringUtils.QT(reasonType)
                            + " and Date="
                            + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)), false);*/

            db.insertSQL("RetailerLocationDeviation", columns, values);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            return "-4";
        }

        return "1";
    }

}
