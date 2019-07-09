package com.ivy.cpg.view.van.odameter;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.cpg.view.van.vanstockapply.VanLoadMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

public class OdameterHelper {

    private Context context;
    private BusinessModel bmodel;
    private static OdameterHelper instance = null;

    public OdameterHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;

    }

    public static OdameterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OdameterHelper(context);
        }
        return instance;
    }


    public void saveOdameter(VanLoadMasterBO mylist) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.createDataBase();
            db.openDataBase();
            db.executeQ("DELETE from Odameter");

            if (!bmodel.configurationMasterHelper.SHOW_PHOTO_ODAMETER) {
                String columns = "uid,start,end,isstarted,startlatitude,startlongitude,starttime,date,tripUid";

                String values = StringUtils.getStringQueryParam(bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID))
                        + ","
                        + mylist.getOdameterstart()
                        + ","
                        + mylist.getOdameterend()
                        + ","
                        + 1
                        + ","
                        + LocationUtil.latitude
                        + ","
                        + LocationUtil.longitude
                        + ","
                        + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))
                        + ","
                        + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate());

                if(bmodel.configurationMasterHelper.IS_ENABLE_TRIP) {
                    values += "," + StringUtils.getStringQueryParam(LoadManagementHelper.getInstance(context.getApplicationContext()).getTripId());
                }
                else {
                    values += "," + StringUtils.getStringQueryParam("0");
                }

                String sql = "insert into " + "Odameter" + "(" + columns
                        + ") values(" + values + ")";
                db.executeQ(sql);
                db.closeDB();

            } else {

                String columns = "uid,start,end,isstarted,startlatitude,startlongitude,starttime,date,startImage,tripUid";

                String values = StringUtils.getStringQueryParam(bmodel.userMasterHelper.getUserMasterBO()
                        .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID))
                        + ","
                        + mylist.getOdameterstart()
                        + ","
                        + mylist.getOdameterend()
                        + ","
                        + 1
                        + ","
                        + LocationUtil.latitude
                        + ","
                        + LocationUtil.longitude
                        + ","
                        + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))
                        + ","
                        + bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
                        .getDownloadDate())
                        + ","
                        + StringUtils.getStringQueryParam(mylist.getStartTripImg());

                if(bmodel.configurationMasterHelper.IS_ENABLE_TRIP) {
                    values += "," + StringUtils.getStringQueryParam(LoadManagementHelper.getInstance(context.getApplicationContext()).getTripId());
                }
                else {
                    values += "," + StringUtils.getStringQueryParam("0");
                }

                String sql = "insert into " + "Odameter" + "(" + columns
                        + ") values(" + values + ")";
                db.executeQ(sql);
                db.closeDB();
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }

    }


    public void UpdateOdaMeter(VanLoadMasterBO mylist) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db.selectSQL("select  count(uid) from Odameter");
            String sql1, sql;
            if (c != null) {
                while (c.moveToNext())

                    if (!bmodel.configurationMasterHelper.SHOW_PHOTO_ODAMETER) {
                        if (c.getInt(0) == 0) {
                            sql1 = "insert into odameter(end,endtime,endlatitude,endlongitude,isended,upload) values("
                                    + mylist.getOdameterend()
                                    + ","
                                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.TIME))
                                    + ","
                                    + LocationUtil.latitude
                                    + ","
                                    + LocationUtil.longitude
                                    + ","
                                    + 1
                                    + ",N)";

                            db.executeQ(sql1);
                        } else {
                            sql = "update Odameter set end="
                                    + mylist.getOdameterend()
                                    + ",endtime="
                                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))
                                    + ",endlatitude=" + LocationUtil.latitude
                                    + ",endlongitude=" + LocationUtil.longitude
                                    + ",isended=" + 1 + ",upload='N'";
                            db.executeQ(sql);
                        }

                        c.close();
                    } else {

                        if (c.getInt(0) == 0) {
                            sql1 = "insert into odameter(end,endtime,endlatitude,endlongitude,endImage,isended,upload) values("
                                    + mylist.getOdameterend()
                                    + ","
                                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.TIME))
                                    + ","
                                    + LocationUtil.latitude
                                    + ","
                                    + LocationUtil.longitude
                                    + ","
                                    + mylist.getEndTripImg()
                                    + ","
                                    + 1
                                    + ",N)";

                            db.executeQ(sql1);
                        } else {
                            sql = "update Odameter set end="
                                    + mylist.getOdameterend()
                                    + ",endtime="
                                    + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW))
                                    + ",endlatitude=" + LocationUtil.latitude
                                    + ",endlongitude=" + LocationUtil.longitude
                                    + ",endImage=" + StringUtils.getStringQueryParam(mylist.getEndTripImg())
                                    + ",isended=" + 1 + ",upload='N'";
                            db.executeQ(sql);
                        }
                        c.close();
                    }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public boolean isOdameterStarted(Context ctx) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select isstarted from odameter");
            if (c != null) {
                if (c.moveToNext()) {
                    boolean isStarted=c.getInt(0)==1;
                    c.close();
                    db.closeDB();
                    return isStarted;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return false;
    }

    public boolean isOdameterEnded(Context ctx) {
        try {
            DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME
            );
            db.openDataBase();
            Cursor c = db.selectSQL("select isEnded from odameter");
            if (c != null) {
                if (c.moveToNext()) {
                    boolean isSEnded=c.getInt(0)==1;
                    c.close();
                    db.closeDB();
                    return isSEnded;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }

        return false;
    }
}
