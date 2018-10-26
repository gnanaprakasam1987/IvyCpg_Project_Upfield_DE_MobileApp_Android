package com.ivy.cpg.view.roadactivity;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class RoadActivityHelper {
    private String Loc1 = "3";
    private String Loc2 = "4";

    private Context context;
    private BusinessModel bmodel;
    private static RoadActivityHelper instance = null;
    private Vector<RoadActivityBO> loadType;
    private Vector<RoadActivityBO> loadProduct;
    private Vector<RoadActivityBO> loadloc1;
    private Vector<RoadActivityBO> loadloc2;
    private String ProductName;
    private String LocationName1;
    private String LocationName2;

    private RoadActivityHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static RoadActivityHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RoadActivityHelper(context);
        }
        return instance;
    }

    public void loadTypeSpinnerData() {
        try {
            loadType = new Vector<>();
            RoadActivityBO roadBO = new RoadActivityBO();
            roadBO.setId(0);
            roadBO.setName(context.getResources().getString(R.string.select));
            loadType.add(roadBO);

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId ,ListName  FROM StandardListMaster where  ListType ='ROAD_ACTIVITY_TYPE'");
            if (c != null) {
                while (c.moveToNext()) {
                    roadBO = new RoadActivityBO();
                    roadBO.setId(c.getInt(0));
                    roadBO.setName(c.getString(1));
                    loadType.add(roadBO);
                }
            }
            c.close();
            db.close();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadProductSpinnerData() {
        try {
            int sequenceid = 1;
            loadProduct = new Vector<>();

            RoadActivityBO roadBO = new RoadActivityBO();
            roadBO.setId(0);
            roadBO.setName(context.getResources().getString(R.string.select));
            loadProduct.add(roadBO);

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT PM.PID , PM.PName ,LL.LevelName FROM ProductLevel LL "
                            + " INNER JOIN ProductMaster PM ON PM.PLid  = LL.LevelId  "
                            + " WHERE  LL.Sequence='" + sequenceid + "'");

            if (c != null) {
                while (c.moveToNext()) {
                    roadBO = new RoadActivityBO();
                    roadBO.setId(c.getInt(0));
                    roadBO.setName(c.getString(1));
                    loadProduct.add(roadBO);
                    ProductName = c.getString(2);
                }
            }
            c.close();
            db.close();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }


    public String getProductName() {
        return ProductName;
    }

    String getLocationName1() {
        return LocationName1;
    }

    String getLocationName2() {
        return LocationName2;
    }

    Vector<RoadActivityBO> getProductSpinnerData() {
        return loadProduct;
    }

    Vector<RoadActivityBO> getTypeSpinnerData() {
        return loadType;
    }

    public void loadLocationNames() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT DISTINCT  IFNULL(PL2.NAME,'Location'), IFNULL(PL3.NAME,'Location')  FROM LocationLevel "
                            + " LEFT JOIN LocationLevel  PL2 ON PL2.Sequence ='" + Loc1 + "' "
                            + " LEFT JOIN LocationLevel  PL3 ON PL3.Sequence ='" + Loc2 + "' ");
            if (c != null) {
                while (c.moveToNext()) {
                    LocationName1 = c.getString(0);
                    LocationName2 = c.getString(1);
                }
            }
            c.close();
            db.close();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    public void loadLocation1SpinnerData() {
        try {
            loadloc1 = new Vector<RoadActivityBO>();
            RoadActivityBO roadBO = new RoadActivityBO();
            roadBO.setId(0);
            roadBO.setName(context.getResources().getString(R.string.select));
            loadloc1.add(roadBO);

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT LM.LocId, LM.LocName   FROM LocationLevel LL"
                            + " INNER JOIN  LocationMaster LM ON  LL.ID = LM.LocLevelId"
                            + " WHERE LL.Sequence ='" + Loc1 + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    roadBO = new RoadActivityBO();
                    roadBO.setId(c.getInt(0));
                    roadBO.setName(c.getString(1));
                    loadloc1.add(roadBO);
                }
            }
            c.close();
            db.close();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    Vector<RoadActivityBO> getLocation1SpinnerData() {
        return loadloc1;

    }

    Vector<RoadActivityBO> getLocation2SpinnerData() {
        return loadloc2;

    }

    public void loadLocation2SpinnerData() {
        try {
            loadloc2 = new Vector<>();
            RoadActivityBO roadBO = new RoadActivityBO();
            roadBO.setId(0);
            roadBO.setName(context.getResources().getString(R.string.select));
            loadloc2.add(roadBO);

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT LM.LocId, LM.LocName ,LM.LocParentId  FROM LocationLevel LL"
                            + " INNER JOIN  LocationMaster LM ON  LL.ID = LM.LocLevelId"
                            + " WHERE LL.Sequence ='" + Loc2 + "'");
            if (c != null) {
                while (c.moveToNext()) {
                    roadBO = new RoadActivityBO();
                    roadBO.setId(c.getInt(0));
                    roadBO.setName(c.getString(1));
                    roadBO.setPid(c.getInt(2));
                    loadloc2.add(roadBO);
                }
            }
            c.close();
            db.close();

        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    void saveRoadActivity(String TypeId, String PId, String LocationId,
                                 String Remarks, Vector<String> ImgName) {

        try {
            String imagePath = "";

            String headerColumns = "Uid ,TypeId, PId, LocationId, Remarks";
            String detailColumns = "Uid,ImgName";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String uid = bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));
            String headerValues = uid + "," + TypeId + "," + PId + "," + LocationId + ","
                    + bmodel.QT(Remarks);
            String detailValues = "";

            db.insertSQL(DataMembers.tbl_RoadActivityHeader, headerColumns,
                    headerValues);

            for (int k = 0; k < ImgName.size(); k++) {
                imagePath = "RoadActivity" + "/" + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                        .replace("/", "")
                        + "/" + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + "/" + ImgName.get(k);
                detailValues = uid + "," + bmodel.QT(imagePath);
                db.insertSQL(DataMembers.tbl_RoadActivityDetail, detailColumns,
                        detailValues);
            }

            db.close();
        } catch (Exception e) {
            Commons.printException(e);
        }

    }



}
