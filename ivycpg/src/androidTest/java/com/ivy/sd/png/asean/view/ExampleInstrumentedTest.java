package com.ivy.sd.png.asean.view;

import android.content.Context;
import android.database.Cursor;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Test
    public void useAppContext() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();
        assertEquals("com.ivy.sd.png.asean.view", appContext.getPackageName());
    }

    @Test
    public void ensureDBIsEmpty() throws Exception {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ivy.sd.png.asean.view", appContext.getPackageName());

        DBUtil db = new DBUtil(appContext, DataMembers.DB_NAME);
        db.createDataBase();
        db.openDataBase();
        boolean hasData=false;
        String tableName = "";
        Cursor c = db.selectSQL("select name from sqlite_master where type = 'table'");
        if (c != null) {
            while (c.moveToNext() && !hasData) {
                tableName = c.getString(0);
                if(!tableName.equalsIgnoreCase("android_metadata")) {
                    Cursor c1 = db.selectSQL("select * from " + tableName);
                    hasData = c1.moveToNext();
                }
            }
            c.close();
            Commons.print(tableName);
        }

        assertEquals(false, hasData);


    }
}