package com.ivy.ui.profile.data;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.NewOutletBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.LinkedHashMap;

public class ProfileDataManagerImpl implements ProfileDataManager {

    private ArrayList<NewOutletBO> contactTitleList;

    @Override
    public void loadContactTitle(Context context) {

        NewOutletBO contactTitle;
        contactTitleList = new ArrayList<>();
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT ListId,ListCode,ListName from StandardListMaster where ListType='RETAILER_CONTACT_TITLE_TYPE'");
            if (c.getCount() > 0) {
                while (c.moveToNext()) {
                    contactTitle = new NewOutletBO();
                    contactTitle.setListId(c.getInt(0));
                    contactTitle.setListName(c.getString(2));
                    contactTitleList.add(contactTitle);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void loadContactStatus() {

    }

    @Override
    public void downloadLinkRetailer() {

    }

    @Override
    public void downloadLocationMaster() {

    }

    @Override
    public LinkedHashMap<Integer, ArrayList<LocationBO>> getLocationListByLevId() {
        return null;
    }

    @Override
    public void loadContractData() {

    }

    @Override
    public void getChannelMaster() {

    }

    @Override
    public void getPreviousProfileChanges(String RetailerID) {

    }
}
