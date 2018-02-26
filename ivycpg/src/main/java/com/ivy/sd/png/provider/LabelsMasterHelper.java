package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.HashMap;
import java.util.Map;

public class LabelsMasterHelper {

    private final Context mContext;
    private final BusinessModel bmodel;

    private static LabelsMasterHelper instance = null;
    private Map<String, String> labelsMap;

    private LabelsMasterHelper(Context context) {
        this.mContext = context;
        this.bmodel = (BusinessModel) context;
    }

    public static LabelsMasterHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LabelsMasterHelper(context);
        }
        return instance;
    }

    public void downloadLabelsMaster() {
        labelsMap = new HashMap<>();

        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT key,value from LabelsMaster where lang = "
                            + bmodel.QT(PreferenceManager
                            .getDefaultSharedPreferences(mContext)
                            .getString("languagePref",
                                    ApplicationConfigs.LANGUAGE)));
            if (c != null) {
                while (c.moveToNext()) {
                    labelsMap.put(c.getString(0), c.getString(1));
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }

    private Map<String, String> getLabelsMap() {
        return labelsMap;
    }

    public String applyLabels(Object tag) {
        try {
            if (tag != null) {
                String str = (String) tag;
                if (getLabelsMap() != null
                        && getLabelsMap().get(str) != null
                        && getLabelsMap().get(str).length() > 0) {
                    return getLabelsMap().get(str);
                }
            }
        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return null;
    }

    //used for dashboard incentive label Month or day or year
    public String applyLabels(String tag) {
        try {
            if (tag != null) {
                if (getLabelsMap() != null
                        && getLabelsMap().get(tag) != null
                        && getLabelsMap().get(tag).length() > 0) {
                    return getLabelsMap().get(tag);
                }
            }
        } catch (Exception e) {
            Commons.printException(""+e);
        }
        return null;
    }

    public String getSyncContentHTML() {
        return SyncContentHTML;
    }

    private String SyncContentHTML = "";

    public void downloadSyncContent() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();

            Cursor c = db
                    .selectSQL("SELECT value from LabelsMaster where lang = "
                            + bmodel.QT(PreferenceManager
                            .getDefaultSharedPreferences(mContext)
                            .getString("languagePref",
                                    ApplicationConfigs.LANGUAGE)) + " and key = 'SYNC_CONTENT'");
            if (c != null) {
                while (c.moveToNext()) {
                    SyncContentHTML = c.getString(0);
                }
                c.close();
            } else {
                SyncContentHTML = "NULL";
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            SyncContentHTML = "NULL";
        }
    }
}
