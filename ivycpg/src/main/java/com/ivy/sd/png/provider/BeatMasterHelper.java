package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.core.data.beat.BeatDataManagerImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Vector;

/**
 * @See {@link BeatDataManagerImpl}
 * @deprecated
 */
@Deprecated
public class BeatMasterHelper {

    private static BeatMasterHelper instance = null;
    private Context context;
    private Vector<BeatMasterBO> beatMaster;
    private BeatMasterBO todayBeatMasterBO;
    private BusinessModel bmodel;

    protected BeatMasterHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;

    }

    public static BeatMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BeatMasterHelper(context);
        }
        return instance;
    }

    public Vector<BeatMasterBO> getBeatMaster() {
        return beatMaster;
    }

    public void setBeatMaster(Vector<BeatMasterBO> beatMaster) {
        this.beatMaster = beatMaster;
    }

    /**
     * Return string array of all the Beat names downloaded.
     *
     * @return beat[]
     */
    public String[] getAllBeats() {
        BeatMasterBO beat;
        int siz = getBeatMaster().size();

        String data[];

        data = new String[siz];

        if (siz == 0)
            return new String[0];

        for (int i = 0; i < data.length; ++i) {
            beat = getBeatMaster().get(i);
            data[i] = beat.getBeatDescription();

        }
        return data;
    }

    /**
     * @param beatName - beatName
     * @return -BeatsId
     * @See {@link com.ivy.core.data.beat.BeatDataManager#fetchBeatsId(String)}
     * Return beatId of the given beatname.
     * @deprecated
     */
    @Deprecated
    public String getBeatsId(String beatName) {
        BeatMasterBO beat;
        int siz = getBeatMaster().size();
        if (siz == 0)
            return null;

        for (int i = 0; i < siz; ++i) {
            beat = getBeatMaster().get(i);
            if (!StringUtils.isEmptyString(beatName)
                    && !StringUtils.isEmptyString(beat.getBeatDescription())
                    && beatName.equals(beat.getBeatDescription())) {
                return beat.getBeatId() + "";
            }
        }
        return "0";
    }

    /**
     * @See {@link BeatDataManagerImpl#fetchBeats()}
     * Download beatamster and load in verctor
     * @deprecated
     */
    @Deprecated
    public void downloadBeats() {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME);
            String beatCols = "BeatID,BeatDescription,today,UserId,BeatCode";
            BeatMasterBO beat;
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT distinct " + beatCols
                    + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " +
                    "ifnull((SELECT UserId FROM" + DataMembers.tbl_beatMaster +
                    " WHERE UserId=" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "),0) Order by BeatDescription asc");
            if (c != null) {
                setBeatMaster(new Vector<BeatMasterBO>());
                while (c.moveToNext()) {
                    beat = new BeatMasterBO();
                    beat.setBeatId(c.getInt(0));
                    beat.setBeatDescription(c.getString(1));
                    beat.setToday(c.getInt(2));
                    beat.setUserId(c.getInt(3));
                    beat.setBeatCode(c.getString(4));
                    getBeatMaster().add(beat);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            db.closeDB();
            Commons.printException("" + e);
        }
    }

    /*User for Adhoc planning*/

    /**
     * @param userId
     * @return
     * @See {@link BeatDataManagerImpl#fetchBeatsForUser(int)}
     * @deprecated
     */
    @Deprecated
    public ArrayList<BeatMasterBO> downloadBeats(int userId) {
        ArrayList<BeatMasterBO> beatList = new ArrayList<>();
        try {
            BeatMasterBO beat;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT distinct " + DataMembers.tbl_beatMaster_cols
                    + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " + userId);
            if (c != null) {
                beatList = new ArrayList<>();
                while (c.moveToNext()) {
                    beat = new BeatMasterBO();
                    beat.setBeatId(c.getInt(0));
                    beat.setBeatDescription(c.getString(1));
                    beat.setToday(c.getInt(2));
                    beat.setBeatCode(c.getString(3));
                    beatList.add(beat);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return beatList;
    }

    /**
     * @return
     * @See {@link BeatDataManagerImpl#fetchAdhocPlannedBeats()}
     * @deprecated
     */
    @Deprecated
    public ArrayList<BeatMasterBO> downloadBeatsAdhocPlanned() {
        ArrayList<BeatMasterBO> beatList = new ArrayList<>();
        try {
            BeatMasterBO beat;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            Cursor c = db.selectSQL("select distinct RBM.BeatID, BM.BeatDescription,BM.today from RetailerBeatMapping RBM inner join BeatMaster BM on BM.beatID = RBM.BeatID");
            if (c != null) {
                beatList = new ArrayList<>();
                while (c.moveToNext()) {
                    beat = new BeatMasterBO();
                    beat.setBeatId(c.getInt(0));
                    beat.setBeatDescription(c.getString(1));
                    beat.setToday(c.getInt(2));
                    beatList.add(beat);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return beatList;
    }

    /**
     * @return
     * @See {@link AppDataProviderImpl#getBeatMasterBo()}
     * @deprecated
     */
    public BeatMasterBO getTodayBeatMasterBO() {
        return todayBeatMasterBO;
    }


    /**
     * @See {@link com.ivy.core.data.app.AppDataProviderImpl#setTodayBeatMaster(BeatMasterBO)}
     * @deprecated
     */
    @Deprecated
    public void setTodayBeatMasterBO(BeatMasterBO todayBeatMasterBO) {
        bmodel.codeCleanUpUtil.setTodayBeatMaster(todayBeatMasterBO);
        this.todayBeatMasterBO = todayBeatMasterBO;
    }

    /**
     * @param beatid
     * @return
     * @See {@link BeatDataManagerImpl#fetchBeatMaster(String)}
     * @deprecated
     */
    public BeatMasterBO getBeatMasterBOByID(int beatid) {
        for (int i = 0; i < getBeatMaster().size(); i++) {
            if (getBeatMaster().get(i).getBeatId() == beatid)
                return getBeatMaster().get(i);
        }
        return null;
    }
}
