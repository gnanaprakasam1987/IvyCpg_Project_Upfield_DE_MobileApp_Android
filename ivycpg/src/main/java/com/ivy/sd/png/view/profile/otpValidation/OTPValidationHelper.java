package com.ivy.sd.png.view.profile.otpValidation;

import android.content.Context;
import android.widget.Toast;

import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

public class OTPValidationHelper {

    private Context context;

    public OTPValidationHelper(Context ctx){
        context =ctx;
    }

    public void saveOTPActivatedDate(String mRetailerId, int mType) {
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            String query = "";

            if (mType == 1)
                query = "UPDATE RetailerMaster SET StoreOTPActivated = '"
                        + SDUtil.now(SDUtil.DATE_GLOBAL)
                        + "'  WHERE RetailerID = '" + mRetailerId + "'";
            else if (mType == 2)
                query = "UPDATE RetailerMaster SET SkipOTPActivated = '"
                        + SDUtil.now(SDUtil.DATE_GLOBAL)
                        + "'  WHERE RetailerID = '" + mRetailerId + "'";

            if (!query.equals(""))
                db.updateSQL(query);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("", e);
        }
    }



}