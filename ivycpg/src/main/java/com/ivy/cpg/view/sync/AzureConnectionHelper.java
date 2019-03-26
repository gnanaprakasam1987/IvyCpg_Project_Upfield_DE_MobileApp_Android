package com.ivy.cpg.view.sync;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.sync.catalogdownload.CatalogDownloadConstants;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
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

        if (cloudBlobContainer == null) {
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

        // Prepare download URL path.
//        String downloadURL = DataMembers.AZURE_BASE_URL + "/"+DataMembers.AZURE_CONTAINER+"/"+"Product/" + CatalogDownloadConstants.FILE_NAME;
        try {
            CloudBlobContainer container = initializeAzureStorageConnection();

            CloudBlockBlob blob = container.getBlockBlobReference(downloadURL);

            String sasToken = blob.generateSharedAccessSignature(getAccessPolicy(), null);

            return String.format("%s?%s", blob.getUri(), sasToken);

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
            String sql = "Select ListCode,ListName from StandardListMaster where ListType='Azure_Configuration'";
            Cursor c = db.selectSQL(sql);
            if (c != null) {
                while (c.moveToNext()) {
                    if (c.getString(0).equals("AS_TYPE")) {
                        DataMembers.AZURE_TYPE = c.getString(1);
                    } else if (c.getString(0).equals("AS_STORAGE_CONTAINER")) {
                        DataMembers.AZURE_CONTAINER = c.getString(1);
                    } else if (c.getString(0).equals("AS_CONNECTION_STRING")) {
                        DataMembers.AZURE_CONNECTION_STRING = c.getString(1);
                    } else if (c.getString(0).equals("AS_STORAGE_SAS")) {
                        DataMembers.AZURE_SAS = c.getString(1);
                    } else if (c.getString(0).equals("AS_STORAGE_ENDPOINT")) {
                        DataMembers.AZURE_ENDPOINT = c.getString(1);
                    } else if (c.getString(0).equals("AS_STORAGE_ACCOUNT_NAME")) {
                        DataMembers.AZURE_ACCOUNT_NAME = c.getString(1);
                    } else if (c.getString(0).equals("AS_STORAGE_KEY")) {
                        DataMembers.AZURE_KEY = c.getString(1);
                    } else if (c.getString(0).equals("AS_STORAGE_BASE_URL")) {
                        DataMembers.AZURE_BASE_URL = c.getString(1);
                    }else if (c.getString(0).equals("AS_ROOT_DIR"))
                        DataMembers.AZURE_ROOT_DIRECTORY = c.getString(1);
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
