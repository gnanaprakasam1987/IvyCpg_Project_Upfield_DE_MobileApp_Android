package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class ChannelMasterHelper {

    private Context context;
    private BusinessModel bmodel;
    private Vector<ChannelBO> channelMaster;
    private Vector<RetailerMasterBO> retailerMaster;

    public Vector<RetailerMasterBO> getRetailerMaster() {
        return retailerMaster;
    }

    public void setRetailerMaster(Vector<RetailerMasterBO> retailerMaster) {
        this.retailerMaster = retailerMaster;
    }

    private static ChannelMasterHelper instance = null;

    protected ChannelMasterHelper(Context context) {
        this.context = context;

        this.bmodel = (BusinessModel) context;
    }

    public static ChannelMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ChannelMasterHelper(context);
        }
        return instance;
    }

    public void setChannelMaster(Vector<ChannelBO> channelMaster) {
        this.channelMaster = channelMaster;
    }

    public Vector<ChannelBO> getChannelMaster() {
        return channelMaster;
    }

    public String getChannelName(String channelID) {

        ChannelBO beat;
        int siz = channelMaster.size();
        if (siz == 0)
            return "";

        for (int i = 0; i < siz; ++i) {
            beat = (ChannelBO) channelMaster.get(i);
            if (channelID.equals(beat.getChannelId() + "")) {
                return beat.getChannelName();
            }
        }
        return "";
    }

    // Download Channel details
    public void downloadChannel() {
        try {
            ChannelBO temp;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select levelid from channellevel order by Sequence desc limit 2");
            int leveid = 0;
            if (c.getCount() > 0) {
                if (c.moveToLast()) {
                    leveid = c.getInt(0);
                }
            }


            c = db
                    .selectSQL("SELECT chid, chName FROM ChannelHierarchy where levelid=" + leveid);
            if (c != null) {
                channelMaster = new Vector<ChannelBO>();
                while (c.moveToNext()) {
                    temp = new ChannelBO();
                    temp.setChannelId(c.getInt(0));
                    temp.setChannelName(c.getString(1));
                    channelMaster.add(temp);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public void getRetailers() {
        try {
            RetailerMasterBO temp;
            retailerMaster = new Vector<RetailerMasterBO>();
            int siz = bmodel.getRetailerMaster().size();
            for (int ii = 0; ii < siz; ii++) {
                if (((bmodel
                        .getRetailerMaster().get(ii).getIsToday() == 1)) || bmodel.getRetailerMaster().get(ii).getIsDeviated()
                        .equals("Y")) {
                    temp = new RetailerMasterBO();
                    temp.setTretailerId(Integer.parseInt(bmodel.getRetailerMaster().get(ii).getRetailerID()));
                    temp.setTretailerName(bmodel.getRetailerMaster().get(ii).getRetailerName());
                    retailerMaster.add(temp);
                }
            }

        } catch (Exception e) {

            Commons.printException(e);
        }
    }

    public String getChannelHierarchy(int channelId, Context mContext) {
        String sql, sql1 = "", str = "";
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();
            Cursor c = db.selectSQL("select min(Sequence) as childlevel,(select Sequence from ChannelLevel cl inner join ChannelHierarchy ch on ch.LevelId=cl.LevelId where ch.ChId=" + channelId + ") as contentlevel  from ChannelLevel");
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
            sql = "select " + sql1 + "  from ChannelHierarchy LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ChannelHierarchy LM" + i + " ON LM" + (i - 1)
                        + ".ParentId = LM" + i + ".ChId";
            sql = sql + " where LM1.ChId=" + channelId;
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
        if (str.length() == 0)
            str = "0";
        return str;
    }

    public String getLocationHierarchy(Context mContext) {
        String sql, sql1 = "", str = bmodel.getRetailerMasterBO().getLocationId() + ",";
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            int mChildLevel = 0;
            int mContentLevel = 0;
            db.openDataBase();

            StringBuffer sb = new StringBuffer();
            sb.append("select min(Sequence) as childlevel,(select Sequence from LocationLevel l1 ");
            sb.append("inner join locationmaster lm on l1.id=LM.loclevelid where lm.locid=");
            sb.append(bmodel.getRetailerMasterBO().getLocationId());
            sb.append(") as contentlevel  from LocationLevel");
            Cursor c = db.selectSQL(sb.toString());
            if (c != null) {
                while (c.moveToNext()) {
                    mChildLevel = c.getInt(0);
                    mContentLevel = c.getInt(1);
                }
            }
            c.close();

            int loopEnd = mContentLevel - mChildLevel + 1;

            for (int i = 2; i <= loopEnd; i++) {
                sql1 = sql1 + " LM" + i + ".Locid";
                if (i != loopEnd)
                    sql1 = sql1 + ",";
            }
            sql = "select " + sql1 + "  from LocationMaster LM1";
            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN LocationMaster LM" + i + " ON LM" + (i - 1)
                        + ".LocParentId = LM" + i + ".LocId";
            sql = sql + " where LM1.LocId=" + bmodel.getRetailerMasterBO().getLocationId();
            c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    for (int i = 0; i < c.getColumnCount(); i++) {
                        str = str + c.getString(i);
                        if (c.getColumnCount() > 1 && i != c.getColumnCount())
                            str = str + ",";
                    }
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        //to remove last character if it  ','
        if (str.endsWith(","))
            str = str.substring(0, str.length() - 1);
        return str;
    }

}
