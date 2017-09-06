package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by subramanian.r on 19-11-2015.
 */
public class UserFeedBackHelper {

    private Context context;
    private BusinessModel bmodel;
    private static UserFeedBackHelper instance = null;

    private UserFeedBackHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static UserFeedBackHelper getInstance(Context context) {
        if (instance == null) {
            instance = new UserFeedBackHelper(context);
        }
        return instance;
    }

    ArrayList<ReasonMaster> mFeedBackType;

    public ArrayList<ReasonMaster> getFeedBackType() {
        return mFeedBackType;
    }

    public void downloadFeedBackType() {
        try {
            ReasonMaster reason;
            mFeedBackType = new ArrayList<ReasonMaster>();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            reason = new ReasonMaster();
            reason.setReasonID("-1");
            reason.setReasonDesc("Select Type");
            reason.setReasonCategory("Select Type");
            mFeedBackType.add(reason);
            Cursor c = db
                    .selectSQL("Select ListId, ListName, ListCode from StandardListMaster where ListType ='FEEDBACK_TYPE'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    reason = new ReasonMaster();
                    reason.setReasonID(c.getString(0));
                    reason.setReasonDesc(c.getString(1));
                    reason.setReasonCategory(c.getString(2));
                    mFeedBackType.add(reason);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void saveFeedBack(String type, String text, int rank){
        DBUtil db = null;
        try {

            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            String columns = "UId, DateTime, TypeLovId, Feedback, Rating";
            StringBuffer values = new StringBuffer();
            values.append(bmodel.userMasterHelper.getUserMasterBO().getUserid()
                    + "" + SDUtil.now(SDUtil.DATE_TIME_ID));
            values.append(",");
            values.append(bmodel.QT(SDUtil.now(SDUtil.DATE_TIME)));
            values.append(",");
            values.append(bmodel.QT(type));
            values.append(",");
            values.append(bmodel.QT(text));
            values.append(",");
            values.append(bmodel.QT(rank+""));

            db.insertSQL("UserFeedBack", columns, values.toString());
        } catch(Exception e){
            Commons.printException(e);
        } finally {
            db.closeDB();
        }
    }
}
