package com.ivy.cpg.view.reports.slaesvolumereport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by Hanifa on 31/7/18.
 */

public class SalesVolumeReportHelper {

    private Context mContext;

    public SalesVolumeReportHelper(Context context) {
        mContext = context;
    }

    public ArrayList<SalesVolumeBo> getOrderedProductMaster() {
        return productMaster;
    }

    private ArrayList<SalesVolumeBo> productMaster = new ArrayList<>();
    ArrayList<Integer> parentIds = new ArrayList<>();
    HashMap<Integer, String> parentIdsMap = new HashMap<>();

    public void downloadProductReportsWithFiveLevelFilter() {
        try {
            SalesVolumeBo product;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            int mFiltrtLevel = 0;
            int mContentLevel = 0;


            String sql = "";


            Cursor filterCur = db
                    .selectSQL("SELECT Distinct IFNULL(PL2.Sequence,0), IFNULL(PL3.Sequence,0) FROM ProductLevel CF " +
                            "LEFT JOIN ProductLevel PL2 ON PL2.LevelId =  (Select RField from HhtModuleMaster " +
                            "where hhtCode = 'RPT03' and flag = 1 and ForSwitchSeller = 0) " +
                            "LEFT JOIN ProductLevel PL3 ON PL3.LevelId = (Select LevelId from ProductLevel " +
                            "where Sequence = ( Select Sequence from ProductLevel PL inner join ConfigActivityFilter CF on PL.LevelId = CF.ProductContent where CF.ActivityCode = 'MENU_STK_ORD' ))");

            if (filterCur != null) {
                if (filterCur.moveToNext()) {
                    mFiltrtLevel = filterCur.getInt(0);
                    mContentLevel = filterCur.getInt(1);
                }
                filterCur.close();
            }

            int loopEnd = mContentLevel - mFiltrtLevel + 1;
            sql = "select A"
                    + loopEnd
                    + ".pid,A"
                    + loopEnd
                    + ".pname,sum(OD.Qty),A1.pid,A"
                    + loopEnd
                    + ".psname,A"
                    + loopEnd
                    + ".isSalable," +
                    "A1.pname as brandname,A1.parentid,sum(OD.NetAmount) from ProductMaster A1";

            for (int i = 2; i <= loopEnd; i++)
                sql = sql + " INNER JOIN ProductMaster A" + i + " ON A" + i
                        + ".ParentId = A" + (i - 1) + ".PID";

            sql = sql + " left join OrderDetail OD on OD.ProductID = A" + loopEnd + ".pid WHERE A1.PLid IN " +
                    "(Select RField from HhtModuleMaster where hhtCode = 'RPT03' and flag = 1 and ForSwitchSeller = 0) and "
                    + " A" + loopEnd + ".pid = OD.ProductId"
                    + " group by A" + loopEnd + ".pid ORDER BY "
                    + " A" + loopEnd + ".rowid";


            Cursor c = db.selectSQL(sql);
            productMaster = new ArrayList<>();
            if (c != null) {
                while (c.moveToNext()) {
                    product = new SalesVolumeBo();
                    product.setProductID(c.getString(0));
                    product.setProductName(c.getString(1));
                    product.setTotalQty(c.getInt(2));
                    product.setParentid(c.getInt(3));
                    product.setProductShortName(c.getString(4));
                    product.setIsSaleable(c.getInt(5));
                    product.setBrandname(c.getString(6));
                    product.setcParentid(c.getInt(7));
                    product.setTotalamount(SDUtil.convertToDouble(c.getString(8)));

                    productMaster.add(product);
                    parentIds.add(product.getParentid());
                    parentIdsMap.put(product.getParentid(), product.getProductID());

                }
                c.close();
            }
            db.closeDB();
            if (parentIds != null && parentIds.size() > 0) {
                downloadSellerReportFilter();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public HashMap<Integer, Vector<LevelBO>> getMfilterlevelBo() {
        return mfilterlevelBo;
    }

    private HashMap<Integer, Vector<LevelBO>> mfilterlevelBo = new HashMap<>();

    public Vector<LevelBO> getSequencevalues() {
        return sequencevalues;
    }

    private Vector<LevelBO> sequencevalues;

    public void downloadSellerReportFilter() {
        int LevelID = 0;
        sequencevalues = new Vector<>();
        try {

            LevelBO levelBo;
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);

            db.openDataBase();

            String sql = "select distinct PM.PID,PM.PName," +
                    "PM.ParentId,PM.PLid,PL.LevelName from ProductMaster PM left join ProductLevel PL on PL.LevelId = PM.PLid where PM.PLid = (Select RField from HhtModuleMaster where hhtCode = 'RPT03' and flag = 1 and and ForSwitchSeller = 0)";

            Cursor c = db.selectSQL(sql);
            Vector<LevelBO> levelBOVector = new Vector<>();
            if (c != null) {
                while (c.moveToNext()) {
                    if (parentIdsMap.containsKey(c.getInt(0))) {
                        LevelID = c.getInt(3);
                        levelBo = new LevelBO();
                        levelBo.setProductID(c.getInt(0));
                        levelBo.setLevelName(c.getString(1));
                        levelBo.setParentID(c.getInt(2));
                        //levelBo.setProductLevel(c.getString(4));
                        levelBOVector.add(levelBo);
                        if (sequencevalues.size() == 0) {
                            levelBo = new LevelBO();
                            levelBo.setProductID(LevelID);
                            levelBo.setLevelName(c.getString(4));
                            levelBo.setParentID(c.getInt(2));
                            sequencevalues.add(levelBo);
                        }

                    }
                }
                mfilterlevelBo.put(LevelID, levelBOVector);
                c.close();
            }
            db.closeDB();
            //mfilterlevelBo.put()
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
