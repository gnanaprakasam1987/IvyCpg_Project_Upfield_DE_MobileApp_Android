package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.core.data.channel.ChannelDataManagerImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.SubchannelBO;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class SubChannelMasterHelper {

    private Context context;

    private Vector<SubchannelBO> subchannelMaster;

    private static SubChannelMasterHelper instance = null;

    protected SubChannelMasterHelper(Context context) {
        this.context = context;

    }

    public static SubChannelMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new SubChannelMasterHelper(context);
        }
        return instance;
    }

    public void setSubChannelMaster(Vector<SubchannelBO> subchannelMaster) {
        this.subchannelMaster = subchannelMaster;
    }

    public Vector<SubchannelBO> getSubChannelMaster() {
        return subchannelMaster;
    }

    public String getSubChannelName(String subChannel) {
        SubchannelBO beat;
        int siz = getSubChannelMaster().size();

        if (siz == 0)
            return null;

        for (int i = 0; i < siz; ++i) {
            beat = (SubchannelBO) getSubChannelMaster().get(i);
            if (subChannel.equals(beat.getSubchannelid() + "")) {
                return beat.getSubChannelname() + "";
            }
        }
        return "0";
    }

    public int getSubChannelId(String subchannelName) {
        SubchannelBO beat;
        int siz = subchannelMaster.size();
        if (siz == 0)
            return 0;

        for (int i = 0; i < siz; ++i) {
            beat = (SubchannelBO) subchannelMaster.get(i);
            if (subchannelName.equals(beat.getSubChannelname())) {
                return beat.getSubchannelid();
            }
        }
        return 0;
    }

    /**
     * @See {@link ChannelDataManagerImpl#fetchSubChannels()}
     * @deprecated
     */
    @Deprecated
    public void downloadsubChannel() {
        SubchannelBO scbo;
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        String query = "select levelid from channellevel order by Sequence desc limit 1";
        Cursor c = db.selectSQL(query);
        int subchannellevelid = 0;
        if (c.getCount() > 0) {
            while (c.moveToNext()) {
                subchannellevelid = c.getInt(0);
            }
        }

        c = db
                .selectSQL("SELECT chid, parentid, chName FROM ChannelHierarchy where levelid=" + subchannellevelid);
        if (c != null) {
            subchannelMaster = new Vector<SubchannelBO>();
            while (c.moveToNext()) {
                scbo = new SubchannelBO();
                scbo.setSubchannelid(c.getInt(0));
                scbo.setChannelid(c.getInt(1));
                scbo.setSubChannelname(c.getString(2));
                subchannelMaster.add(scbo);
            }
            c.close();
        }
        db.closeDB();
    }

}
