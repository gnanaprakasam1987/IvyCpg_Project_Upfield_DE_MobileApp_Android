package com.ivy.cpg.view.reports.webviewreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.HashMap;

/**
 * Created by Hanifa on 31/7/18.
 */

public class WebViewReportHelper {
    private BusinessModel bModel;
    private Context mContext;
    private static WebViewReportHelper instance = null;

    protected WebViewReportHelper(Context context) {
        mContext = context;
        bModel = (BusinessModel) context.getApplicationContext();
    }

    public static WebViewReportHelper getInstance(Context context) {
        if (instance == null)
            instance = new WebViewReportHelper(context);
        return instance;
    }

    public String getWebViewAuthUrl() {
        return webViewAuthUrl;
    }

    public void setWebViewAuthUrl(String webViewAuthUrl) {
        this.webViewAuthUrl = webViewAuthUrl;
    }

    private String webViewAuthUrl = "";

    public void downloadWebViewArchAuthUrl() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);

            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = 'WEBVIEW_ARCH'");
            if (c != null) {
                if (c.moveToNext()) {
                    webViewAuthUrl = c.getString(0);
                }
                c.close();
            }

            if (!"".equals(webViewAuthUrl)) {
                Cursor c1 = db
                        .selectSQL("select ListName from StandardListMaster where ListCode='AUTH' AND ListType = 'WEBVIEW_ARCH'");
                if (c1 != null) {
                    if (c1.moveToNext()) {
                        webViewAuthUrl += c1.getString(0);
                    }
                    c1.close();
                }
            }
            db.closeDB();

        } catch (Exception e) {
            Commons.printException("" + e);
            webViewAuthUrl = "";
        }
    }



    public void downloadWebViewArchUrl() {
        try {
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = 'WEBVIEW_ARCH'");
            if (c != null) {
                if (c.moveToNext()) {
                    webViewArchUrl = c.getString(0);
                }
                c.close();
            }

            if (!"".equals(webViewArchUrl)) {
                Cursor c1 = db
                        .selectSQL("select ListName from StandardListMaster where ListCode='ACTION' AND ListType = 'WEBVIEW_ARCH'");
                if (c1 != null) {
                    while (c1.moveToNext()) {
                        webViewArchUrl += c1.getString(0);
                    }
                    c1.close();
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            webViewArchUrl = "";
        }
    }


    public String getWebViewArchUrl() {
        return webViewArchUrl;
    }

    public void setWebViewArchUrl(String webViewArchUrl) {
        this.webViewArchUrl = webViewArchUrl;
    }

    private String webViewArchUrl = "";
    public void prepareArchiveFileDownload(String filePath) {
        bModel.setDigitalContentURLS(new HashMap<String, String>());

        boolean isAmazonCloud = true;
        boolean isSFDCCloud=false;
        boolean isAzureCloud=false;

        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME
        );
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT flag FROM HHTModuleMaster where hhtCode = 'CLOUD_STORAGE' and flag = 1 and ForSwitchSeller = 0");
        if (c != null) {
            while (c.moveToNext()) {
                if(c.getInt(0)==0){
                    isAmazonCloud=true;
                }
                else if(c.getInt(0)==1){
                    isSFDCCloud=true;
                }
                else if(c.getInt(0)==2){
                    isAzureCloud=true;
                }
                else {
                    isAmazonCloud=true;
                }
            }
        }
        c.close();

        if (!isAmazonCloud) {
            c = db
                    .selectSQL("SELECT ListName FROM StandardListMaster Where ListCode = 'AS_HOST'");
            if (c != null) {
                while (c.moveToNext()) {
                    DataMembers.IMG_DOWN_URL = c.getString(0);
                }
            }

        } else {
            c = db
                    .selectSQL("SELECT ListName FROM StandardListMaster Where ListCode = 'AS_ROOT_DIR'");
            if (c != null) {
                while (c.moveToNext()) {
                    DataMembers.IMG_DOWN_URL = c.getString(0) + "/";
                }
            }

        }

        bModel.getDigitalContentURLS().put(
                DataMembers.IMG_DOWN_URL + filePath,
                DataMembers.PRINTFILE);

        c.close();
        c = null;
        db.closeDB();
    }

    public void downloadWebViewReportUrl(String menuCode) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME);
        db.openDataBase();
        Cursor c = db
                .selectSQL("select ListName from StandardListMaster where ListCode='URL' AND ListType = 'WEBVIEW_REPORTS'");
        if (c != null) {
            if (c.moveToNext()) {
                webReportUrl = c.getString(0);
            }
            c.close();
        }

        if (!"".equals(webReportUrl)) {
            Cursor c1 = db
                    .selectSQL("select ListName from StandardListMaster where ListCode='" + menuCode + "' AND ListType = 'WEBVIEW_REPORTS'");
            if (c1 != null) {
                while (c1.moveToNext()) {
                    webReportUrl += c1.getString(0);
                }
                c1.close();
            }
        }

        db.closeDB();
    }
    private String webReportUrl = "";
    public String getWebReportUrl() {
        return webReportUrl;
    }

    public void setWebReportUrl(String webReportUrl) {
        this.webReportUrl = webReportUrl;
    }



}
