package com.ivy.cpg.view.sync.uploadStatusReport;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

public class UploadStatusHelper {

    private Context context;

    public UploadStatusHelper(Context ctx){
        this.context=ctx;
    }



    public ArrayList<SyncStatusBO> downloadSyncStatusReport() {
        ArrayList<SyncStatusBO> mSyncStatusBOList = new ArrayList<>();
        try {
            SyncStatusBO syncStatusBO;
            String id = "0";
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
            db.openDataBase();

            String sql = "select ID,TableName,LineCount from SyncStatus_Internal order by ID desc";

            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {

                    syncStatusBO = new SyncStatusBO();
                    syncStatusBO.setId(c.getString(0));
                    syncStatusBO.setName(c.getString(1));
                    syncStatusBO.setCount(c.getInt(2));

                    if (!id.equalsIgnoreCase(syncStatusBO.getId())) {

                        if (!id.equals("0")) {
                            syncStatusBO.setShowDateTime(1);
                            mSyncStatusBOList.add(syncStatusBO);
                            id = syncStatusBO.getId();
                        } else {
                            syncStatusBO.setShowDateTime(1);
                            mSyncStatusBOList.add(syncStatusBO);
                            id = syncStatusBO.getId();
                        }

                    } else {
                        syncStatusBO.setShowDateTime(0);
                        mSyncStatusBOList.add(syncStatusBO);
                    }

                }

                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mSyncStatusBOList;
    }
}
