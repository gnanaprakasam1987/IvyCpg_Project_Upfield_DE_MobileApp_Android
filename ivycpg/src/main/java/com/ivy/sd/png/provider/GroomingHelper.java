package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenFragment;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by mayuri.v on 6/8/2017.
 */
public class GroomingHelper {
    private Context context;
    private BusinessModel bmodel;
    private static GroomingHelper instance = null;
    private String uid;
    private ArrayList<String> imageList;

    public GroomingHelper(Context context) {
        this.context = context;
        bmodel = (BusinessModel) context;
    }

    public static GroomingHelper getInstance(Context context) {
        if (instance == null) {
            instance = new GroomingHelper(context);
        }
        return instance;
    }

    public boolean saveGroomingHeader() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            int counterid = 0;
            if (bmodel.configurationMasterHelper.IS_CNT01) {
                userid = bmodel.getSelectedUserId();
            }
            String uid = userid
                    + SDUtil.now(SDUtil.DATE_TIME_ID) + "";
            setUid(uid);

            String columns = "uid,userid,counterid,retailerid,date,time,upload";
            String values = bmodel.QT(uid) + "," + userid + ","
                    + counterid + ", " + bmodel.getRetailerMasterBO().getRetailerID() + ","
                    + bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
                    + bmodel.QT(SDUtil.now(SDUtil.TIME)) + ",'N'";
            db.insertSQL("CS_GroomingHeader", columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("save grooming header" + e);
            return false;
        }

        return true;
    }

    public int loadGroomingHeader() {
        int number = -1;
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            int counterid = 0;
            if (bmodel.configurationMasterHelper.IS_CNT01) {
                userid = bmodel.getSelectedUserId();
            }

            String query = "SELECT uid from CS_GroomingHeader where userid=" + userid+ " and upload='N'";// +
//                    " and retailerid=" + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {
                    number = c.getCount();
                    setUid(c.getString(0));
//                    imgList.add(c.getString(0));
//
//                    if (c.isLast()) {
//                        setImageList(imgList);
//                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("load grooming header" + e);
        }

        return number;
    }

    public ArrayList<String> loadGroomingImage() {
        ArrayList<String> imgList = new ArrayList<>();
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            int counterid = 0;
            if (bmodel.configurationMasterHelper.IS_CNT01) {
                userid = bmodel.getSelectedUserId();
            }

            String query = "SELECT CS_GroomingDetails.localimagepath,CS_GroomingDetails.uid from CS_GroomingDetails inner join CS_GroomingHeader " +
                    "on CS_GroomingDetails.uid=CS_GroomingHeader.uid where CS_GroomingHeader.userid=" + userid + " and CS_GroomingHeader.upload='N'"; //+
//                    " and retailerid=" + bmodel.getRetailerMasterBO().getRetailerID();
            Cursor c = db.selectSQL(query);
            if (c != null) {
                while (c.moveToNext()) {

                    imgList.add(c.getString(0));
                    setUid(c.getString(1));

                    if (c.isLast()) {
//                        Collections.sort(imgList);
                        Collections.reverse(imgList);
                        setImageList(imgList);
                    }
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("load grooming image" + e);
        } finally {
            return imgList;
        }
    }

    public boolean deleteGroomingEntry(String imgPath) {
        try {
            ArrayList<String> imgList = new ArrayList<>();
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            int counterid = 0;
            if (bmodel.configurationMasterHelper.IS_CNT01) {
                userid = bmodel.getSelectedUserId();
            }

            String detailUID = "";
            String imgName = "";


            Cursor c = db.selectSQL("SELECT * from CS_GroomingDetails where localimagepath=" + bmodel.QT(imgPath));
            if (c != null) {
                while (c.moveToNext()) {
                    detailUID = c.getString(0);
                    imgName = c.getString(1);
                }
                c.close();
            }
            db.deleteSQL("CS_GroomingDetails", "localimagepath=" + bmodel.QT(imgPath), false);

            if (!imgName.equals(""))
                bmodel.deleteFiles(HomeScreenFragment.photoPath,
                        imgName.split("/")[3]);

            c = db.selectSQL("SELECT * from CS_GroomingDetails where uid=" + bmodel.QT(detailUID));
            if (c != null) {
                if (!(c.getCount() > 0)) {
                    db.deleteSQL("CS_GroomingHeader", "uid=" + bmodel.QT(detailUID), false);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("delete grooming entry" + e);
            return false;
        }

        return true;
    }

    public boolean saveGroomingDetail(String imgPath, String aws_path) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
            int counterid = 0;
            if (bmodel.configurationMasterHelper.IS_CNT01) {
                userid = bmodel.getSelectedUserId();
            }

            String columns = "uid,imagepath,upload,localimagepath";
            String values = bmodel.QT(getUid()) + "," + bmodel.QT(aws_path) + ",'N'," + bmodel.QT(imgPath);

            db.insertSQL("CS_GroomingDetails", columns, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("save grooming detail" + e);
            return false;
        }

        return true;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public ArrayList<String> getImageList() {
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }
}
