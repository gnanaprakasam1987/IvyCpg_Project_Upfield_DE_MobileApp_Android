package com.ivy.cpg.view.profile.otpValidation;

import android.content.Context;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

public class OTPValidationHelper {

    private Context context;

    public OTPValidationHelper(Context ctx){
        context =ctx;
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



}
