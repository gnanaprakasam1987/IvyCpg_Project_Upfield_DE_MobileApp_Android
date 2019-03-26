package com.ivy.cpg.view.sync;


import android.content.Context;
import android.database.Cursor;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.ivy.cpg.view.sync.catalogdownload.CatalogDownloadConstants;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.net.URL;
import java.util.Date;

public class AWSConnectionHelper {

    private static AWSConnectionHelper instance;

    private AWSConnectionHelper() {
    }

    public static AWSConnectionHelper getInstance() {
        if (instance == null) {
            instance = new AWSConnectionHelper();
        }
        return instance;
    }
    public  void setAWSDBValues(Context context){
        setAmazonS3Credentials(context);
        ((BusinessModel)context).getimageDownloadURL();
    }

    /**
     * Generate Signed Amazon Url with expiration time for 5 hours
     */
    public String getSignedAwsUrl(String downloadKey) {
        try {

            URL url = getS3Connection().generatePresignedUrl(DataMembers.S3_BUCKET, downloadKey,
                    new Date(new Date().getTime() + 1000 * 60 * 300));

            Commons.print("Signed Url " + url.toString());

            return url.toString();

        } catch (Exception e) {
            Commons.print("response Code code getting null value");
        }

        return "";
    }

    public AmazonS3Client getS3Connection(){
        System.setProperty
                (SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        AmazonS3Client s3 = new AmazonS3Client(myCredentials);
        s3.setEndpoint(DataMembers.S3_BUCKET_REGION);
        return s3;
    }

    public void setAmazonS3Credentials(Context context) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String sql = "Select ListCode,ListName from StandardListMaster where ListType='Amazon_Configuration'";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    Commons.print("Check:" + c.getString(0) + " " + c.getString(1));
                    if (c.getString(0).equals("AS_BUCKET_NAME"))
                        DataMembers.S3_BUCKET = c.getString(1);
                    else if (c.getString(0).equals("AS_ACCESS_KEY"))
                        ConfigurationMasterHelper.ACCESS_KEY_ID = c
                                .getString(1);
                    else if (c.getString(0).equals("AS_SECURITY_KEY"))
                        ConfigurationMasterHelper.SECRET_KEY = c.getString(1);
                    else if (c.getString(0).equals("AS_END_POINT"))
                        DataMembers.S3_BUCKET_REGION = c.getString(1);
                    else if (c.getString(0).equals("AS_ROOT_DIR"))
                        DataMembers.S3_ROOT_DIRECTORY = c.getString(1);

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }

    }
}
