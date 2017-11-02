package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

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
     * Return beatId of the given beatname.
     *
     * @param beatName - beatName
     * @return -BeatsId
     */
    public String getBeatsId(String beatName) {
        BeatMasterBO beat;
        int siz = getBeatMaster().size();
        if (siz == 0)
            return null;

        for (int i = 0; i < siz; ++i) {
            beat = getBeatMaster().get(i);
            if (beatName.equals(beat.getBeatDescription())) {
                return beat.getBeatId() + "";
            }
        }
        return "0";
    }

    /**
     * Download beatamster and load in verctor
     */
    public void downloadBeats() {
        try {
            BeatMasterBO beat;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT " + DataMembers.tbl_beatMaster_cols
                    + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " +
                    "ifnull((SELECT UserId FROM" + DataMembers.tbl_beatMaster +
                    " WHERE UserId=" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "),0)");
            if (c != null) {
                setBeatMaster(new Vector<BeatMasterBO>());
                while (c.moveToNext()) {
                    beat = new BeatMasterBO();
                    beat.setBeatId(c.getInt(0));
                    beat.setBeatDescription(c.getString(1));
                    beat.setToday(c.getInt(2));
                    getBeatMaster().add(beat);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    /*User for Adhoc planning*/
    public ArrayList<BeatMasterBO> downloadBeats(int userId) {
        ArrayList<BeatMasterBO> beatList = new ArrayList<>();
        try {
            BeatMasterBO beat;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT " + DataMembers.tbl_beatMaster_cols
                    + " FROM " + DataMembers.tbl_beatMaster + " WHERE UserId = " + userId);
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

    public ArrayList<BeatMasterBO> downloadBeatsAdhocPlanned() {
        ArrayList<BeatMasterBO> beatList = new ArrayList<>();
        try {
            BeatMasterBO beat;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
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

    public BeatMasterBO getTodayBeatMasterBO() {
        return todayBeatMasterBO;
    }

    public void setTodayBeatMasterBO(BeatMasterBO todayBeatMasterBO) {
        this.todayBeatMasterBO = todayBeatMasterBO;
    }

    public BeatMasterBO getBeatMasterBOByID(int beatid) {
        for (int i = 0; i < getBeatMaster().size(); i++) {
            if (getBeatMaster().get(i).getBeatId() == beatid)
                return getBeatMaster().get(i);
        }
        return null;
    }
}
