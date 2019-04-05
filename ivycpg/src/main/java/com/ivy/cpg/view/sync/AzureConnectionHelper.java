package com.ivy.cpg.view.sync;

import android.content.Context;
import android.database.Cursor;

import com.ivy.core.IvyConstants;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.blob.CloudBlobClient;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

import java.util.Date;
import java.util.EnumSet;

public class AzureConnectionHelper {

    private static AzureConnectionHelper instance;
    private CloudBlobContainer cloudBlobContainer;

    private AzureConnectionHelper() {
    }

    public static AzureConnectionHelper getInstance() {
        if (instance == null) {
            instance = new AzureConnectionHelper();
        }
        return instance;
    }

    public CloudBlobContainer initializeAzureStorageConnection() throws Exception {

        if (cloudBlobContainer == null && !ConfigurationMasterHelper.ACCESS_KEY_ID.equalsIgnoreCase(IvyConstants.SAS_KEY_TYPE)) {
            CloudStorageAccount storageAccount = CloudStorageAccount.parse(DataMembers.AZURE_CONNECTION_STRING);
            CloudBlobClient blobClient = storageAccount.createCloudBlobClient();
            cloudBlobContainer = blobClient.getContainerReference(DataMembers.AZURE_CONTAINER);
        }
        return cloudBlobContainer;
    }

    /**
     * Generate Signed Azure Url with expiration time for 5 hours
     */
    public String getAzureFile(String downloadURL){

        try {

            if (ConfigurationMasterHelper.ACCESS_KEY_ID.equalsIgnoreCase(IvyConstants.SAS_KEY_TYPE)){
                downloadURL = AppUtils.buildAzureUrl(downloadURL);
//                CloudBlockBlob blob = new CloudBlockBlob(new URI(downloadURL));
                return downloadURL;
            }else {

                CloudBlobContainer container = initializeAzureStorageConnection();

                CloudBlockBlob blob = container.getBlockBlobReference(downloadURL);

                String sasToken = blob.generateSharedAccessSignature(getAccessPolicy(), null);

                return String.format("%s?%s", blob.getUri(), sasToken);
            }

        }catch(Exception e){
            Commons.printException(e);
        }

        return "";
    }

    public SharedAccessBlobPolicy getAccessPolicy(){
        SharedAccessBlobPolicy itemPolicy = new SharedAccessBlobPolicy();

        Date expirationTime = new Date(new Date().getTime() + 1000 * 60 * 300);
        itemPolicy.setSharedAccessExpiryTime(expirationTime);
        itemPolicy.setPermissions(EnumSet.of(SharedAccessBlobPermissions.WRITE, SharedAccessBlobPermissions.READ, SharedAccessBlobPermissions.LIST));

        return itemPolicy;
    }

    public void setAzureCredentials(Context context) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
        try {
            db.openDataBase();
            String sql = "Select ListCode,ListName from StandardListMaster where ListType='Amazon_Configuration'";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getString(0).equals("AS_TYPE")) {
                        DataMembers.AZURE_TYPE = c.getString(1);
                    } else if (c.getString(0).equals("AS_BUCKET_NAME")) {
                        DataMembers.AZURE_CONTAINER = c.getString(1);
                    } else if (c.getString(0).equals("AS_END_POINT")) {
                        DataMembers.AZURE_CONNECTION_STRING = c.getString(1);
                    } else if (c.getString(0).equals("AS_SECURITY_KEY")) {
                        DataMembers.AZURE_SAS = c.getString(1);
                    } else if (c.getString(0).equals("AS_ROOT_DIR"))
                        DataMembers.AZURE_ROOT_DIRECTORY = c.getString(1);
                    else if (c.getString(0).equals("AS_ACCESS_KEY"))
                        ConfigurationMasterHelper.ACCESS_KEY_ID = c
                                .getString(1);

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
