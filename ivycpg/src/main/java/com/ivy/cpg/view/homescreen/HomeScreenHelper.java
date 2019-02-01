package com.ivy.cpg.view.homescreen;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

public class HomeScreenHelper {

    private Context context;
    public HomeScreenHelper(Context context){
        this.context = context;
    }
    public boolean checkMenusAvailable() {

        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
            db.openDataBase();

            Cursor c = db.selectSQL("select * from "
                    + DataMembers.tbl_HhtMenuMaster
                    + " where  flag = 1 and MenuType= 'HOME_MENU' and lang like"
                    + "'%" + getSelectedLanguage() + "%'");

            if (c != null) {
                if (c.getCount() > 0) {
                    c.close();
                    db.closeDB();
                    return true;
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return false;
    }

    private String getSelectedLanguage(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return sharedPrefs.getString("languagePref", ApplicationConfigs.LANGUAGE);
    }


}
