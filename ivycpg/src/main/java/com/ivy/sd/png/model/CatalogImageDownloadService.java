package com.ivy.sd.png.model;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by dharmapriya.k on 15/12/17.
 */

public class CatalogImageDownloadService extends IntentService {
    public static boolean isServiceRunning;

    public CatalogImageDownloadService() {
        super(CatalogImageDownloadService.class.getName());

    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (getApplicationContext() != null) {
            isServiceRunning = true;
            BusinessModel businessModel = (BusinessModel) getApplicationContext();
            businessModel.configurationMasterHelper.setAmazonS3Credentials();
            BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                    ConfigurationMasterHelper.SECRET_KEY);
            AmazonS3Client s3 = new AmazonS3Client(myCredentials);
            TransferUtility transferUtility = new TransferUtility(s3, getApplicationContext());
            Thread downloaderThread = new DownloaderThreadCatalog(getApplicationContext(),
                    null, (ArrayList<S3ObjectSummary>) businessModel.synchronizationHelper.getImageDetails(),
                    businessModel.userMasterHelper.getUserMasterBO()
                            .getUserid(), transferUtility);
            downloaderThread.start();
        }
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Commons.print("Catalog service starting");
        return super.onStartCommand(intent, flags, startId);
    }

}
