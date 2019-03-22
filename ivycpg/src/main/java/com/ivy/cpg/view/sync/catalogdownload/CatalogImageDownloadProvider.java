package com.ivy.cpg.view.sync.catalogdownload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.downloader.Status;
import com.downloader.core.Core;
import com.ivy.cpg.view.sync.largefiledownload.FileDownloadProvider;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;
import com.microsoft.azure.storage.blob.SharedAccessBlobPermissions;
import com.microsoft.azure.storage.blob.SharedAccessBlobPolicy;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by abbas.a on 15/02/18.
 */

public class CatalogImageDownloadProvider {

    public static CatalogImageDownloadProvider instance;
    private BusinessModel businessModel;
    private DecimalFormat df = new DecimalFormat("0.000");
    private int downloadId = -1;
    public boolean isDownloadInProgress = false;

    private CatalogImageDownloadProvider(Context context) {
        this.businessModel = (BusinessModel) context.getApplicationContext();
    }

    public static CatalogImageDownloadProvider getInstance(Context context) {
        if (instance == null) {
            instance = new CatalogImageDownloadProvider(context);
        }
        return instance;
    }

    public void callCatalogImageDownload() {
        startZipDownload(businessModel.getContext());
    }

    /**
     * Call this method on local login to initiate catalog download if app crashed.
     */
    public void checkCatalogDownload() {

        if (getCatalogDownloadStatus().equals(CatalogDownloadConstants.DOWNLOADING)
                || getCatalogDownloadStatus().isEmpty()) {
            // Set Digital content download path.

            downloadProcess(businessModel.getContext());

        } else if (getCatalogDownloadStatus().equals(CatalogDownloadConstants.UNZIP)) {
            //Call unzip.
            Intent intent = new Intent(businessModel, CatalogImageDownloadService.class);
            businessModel.startService(intent);
        }
    }

    public void storeCatalogDownloadStatus(int id, String status) {
        SharedPreferences.Editor editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(CatalogDownloadConstants.STATUS_KEY, status);
        editor.putInt(CatalogDownloadConstants.STATUS_ID, id);
        editor.apply();
    }

    private void storeCatalogDownloadUrl(String url) {
        SharedPreferences.Editor editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(CatalogDownloadConstants.STATUS_URL, url);
        editor.apply();
    }

    public void storeCatalogDownloadStatusError(String error) {
        SharedPreferences.Editor editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(CatalogDownloadConstants.STATUS_ERROR, error);
        editor.apply();
    }

    private String getCatalogDownloadUrl() {
        SharedPreferences editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE);
        return editor.getString(CatalogDownloadConstants.STATUS_URL, "");
    }

    public String getCatalogDownloadStatus() {
        SharedPreferences editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE);
        return editor.getString(CatalogDownloadConstants.STATUS_KEY, "");
    }

    public void clearCatalogImages() {
        try {
            deleteFiles(getStorageDir(businessModel.getResources().getString(R.string.app_name)));
        } catch (Exception e) {
            Commons.printException("deleteImageDetails" + e);
        }
    }

    private void deleteFiles(File file) {

        if (file.exists()) {
            String deleteCmd = "rm -r " + file.getAbsolutePath();
            Runtime runtime = Runtime.getRuntime();
            try {
                runtime.exec(deleteCmd);
            } catch (IOException e) {
                Commons.printException(e);
            }
        }
    }

    public void setCatalogImageDownloadFinishTime(String count, String time) {
        String filename = "log";
        time = time + "\n";
        FileOutputStream outputStream;
        try {
            Commons.print("FilePath " + getStorageDir(businessModel.getResources().getString(R.string.app_name)) + "/" + filename);
            outputStream = new FileOutputStream(getStorageDir(businessModel.getResources().getString(R.string.app_name)) + "/" + filename);//context.openFileOutput(filename, Context.MODE_PRIVATE);
            outputStream.write(time.getBytes());
            outputStream.write(count.getBytes());

            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get last downloaded date time which is stored in log file.
     *
     * @return date
     */
    public String getLastDownloadedDateTime() {
        //Find the directory for the SD Card using the API
        File sdcard = getStorageDir(businessModel.getResources().getString(R.string.app_name));
        //Get the text file
        File file = new File(sdcard, "log");
        if (file.exists()) {
            //Read text from file
            StringBuilder text = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(new FileReader(file));
                String line;
                int count = 0;
                while ((line = br.readLine()) != null) {
                    if (count == 0) {
                        text.append(line);
                    }
                    count++;
                }
                br.close();
                return text.toString();
            } catch (IOException e) {
                Commons.print("error" + e.getMessage());
            }
        }
        return "";
    }

    public File getStorageDir(String folderName) {

        File docsFolder = new File(Environment.getExternalStorageDirectory(), folderName);
        if (!docsFolder.exists()) {
            docsFolder.mkdir();
        }
        return docsFolder;

    }

    public boolean deleteLogFile() {
        //Find the directory for the SD Card using the API
        File folder = getStorageDir(businessModel.getResources().getString(R.string.app_name));
        //Get the text file
        File file = new File(folder, "log");

        if (file.delete())
            return true;
        else
            return false;
    }

    /**
     * Check the log file whether date is
     * @param ctx
     */
    private void startZipDownload(Context ctx){
        String date = getLastDownloadedDateTime();
        Commons.print("date in log file : " + date);

        //Initiate only if there is not log file.
        if (date.isEmpty()) {

            initializePrDownloader(ctx);
            checkCatalogDownload();
        }
    }

    public void downloadProcess(Context ctx){

        String downloadURL;
        if (getCatalogDownloadUrl() == null || getCatalogDownloadUrl().isEmpty())
        // Prepare download URL path.
            downloadURL = getDownloadUrl();
        else
            downloadURL = getCatalogDownloadUrl();

        // Create location file path to store the downloaded file
        File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" );

        downloadId = PRDownloader.download(downloadURL, file.getAbsolutePath(), CatalogDownloadConstants.FILE_NAME)
                .build()
                .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                    @Override
                    public void onStartOrResume() {
                        storeCatalogDownloadStatus(downloadId,CatalogDownloadConstants.DOWNLOADING);
                        storeCatalogDownloadUrl(downloadURL);
                        isDownloadInProgress = true;
                    }
                })
                .setOnPauseListener(new OnPauseListener() {
                    @Override
                    public void onPause() {
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel() {

                    }
                })
                .setOnProgressListener(new OnProgressListener() {
                    @Override
                    public void onProgress(Progress progress) {

                        double bytesCurrentMB = (double) progress.currentBytes / (double) FileDownloadProvider.MB_IN_BYTES;
                        double bytesTotalMB = (double) progress.totalBytes / (double) FileDownloadProvider.MB_IN_BYTES;

                        String downloadDetail = String.valueOf(df.format(bytesCurrentMB)) + "MB/" + String.valueOf(df.format(bytesTotalMB)) + "MB";

                        int downloadPercentage = (int) (((float) bytesCurrentMB / (float) bytesTotalMB) * 100);

                        Intent intent = new Intent("com.ivy.cpg.view.sync.CatalogDownloadStatus");
                        // You can also include some extra data.
                        intent.putExtra("DownloadDetail", downloadDetail);
                        intent.putExtra("DownloadPercentage", downloadPercentage);
                        intent.putExtra("Status", "InProgress");
                        LocalBroadcastManager.getInstance(businessModel.getContext()).sendBroadcast(intent);

                    }
                })
                .start(new OnDownloadListener() {
                    @Override
                    public void onDownloadComplete() {
                        // update shared preference
                        storeCatalogDownloadStatus(downloadId, CatalogDownloadConstants.UNZIP);

                        try {
                            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + CatalogDownloadConstants.FILE_NAME);
                            int mb = (int) file.length() / 1048576;

                            if (Util.isExternalStorageAvailable(mb * 2)) {
                                //Call unzip.
                                Intent intent = new Intent(ctx, CatalogImageDownloadService.class);
                                ctx.startService(intent);

                            } else {
                                storeCatalogDownloadStatusError(CatalogDownloadConstants.NO_SPACE);
                            }
                        } catch (Exception e) {
                            Commons.printException(e);
                        }

                        Intent intent = new Intent("com.ivy.cpg.view.sync.CatalogDownloadStatus");
                        // You can also include some extra data.
                        intent.putExtra("Status", "Complete");
                        LocalBroadcastManager.getInstance(businessModel.getContext()).sendBroadcast(intent);

                        isDownloadInProgress = false;
                        storeCatalogDownloadUrl("");
                    }

                    @Override
                    public void onError(Error error) {

                        Intent intent = new Intent("com.ivy.cpg.view.sync.CatalogDownloadStatus");
                        // You can also include some extra data.
                        intent.putExtra("Status", "Error");
                        LocalBroadcastManager.getInstance(businessModel.getContext()).sendBroadcast(intent);

                        isDownloadInProgress = false;
                        storeCatalogDownloadUrl("");

                        //storeCatalogDownloadStatus(downloadId,CatalogDownloadConstants.STATUS_ERROR);
                    }
                });

        storeCatalogDownloadStatus(downloadId, "DOWNLOADING");

    }

    private String getDownloadUrl(){
        if (businessModel.configurationMasterHelper.IS_AZURE_UPLOAD) {
            return getAzureFile();
        }else if (businessModel.configurationMasterHelper.ISAMAZON_IMGUPLOAD) {
            return getSignedAwsUrl();
        }

        return "";
    }

    /**
     * Generate Signed Azure Url with expiration time for 5 hours
     */
    private String getAzureFile(){

        // Prepare download URL path.
//        String downloadURL = DataMembers.AZURE_BASE_URL + "/"+DataMembers.AZURE_CONTAINER+"/"+"Product/" + CatalogDownloadConstants.FILE_NAME;
        String downloadURL = "Product/" + CatalogDownloadConstants.FILE_NAME;
        try {
            CloudBlobContainer container = businessModel.initializeAzureStorageConnection();

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

    /**
     * Generate Signed Amazon Url with expiration time for 5 hours
     */
    private String getSignedAwsUrl() {
        try {
            businessModel.configurationMasterHelper.setAmazonS3Credentials();
            businessModel.getimageDownloadURL();
            String downloadKey = DataMembers.IMG_DOWN_URL+ "Product/" + CatalogDownloadConstants.FILE_NAME;

            BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                    ConfigurationMasterHelper.SECRET_KEY);
            AmazonS3Client s3 = new AmazonS3Client(myCredentials);
            s3.setEndpoint(DataMembers.S3_BUCKET_REGION);

            URL url = s3.generatePresignedUrl(DataMembers.S3_BUCKET, downloadKey,
                    new Date(new Date().getTime() + 1000 * 60 * 300));

            Commons.print("Signed Url " + url.toString());

            return url.toString();

        } catch (Exception e) {
            Commons.print("response Code code getting null value");
        }

        return "";
    }

    private void initializePrDownloader(Context context){
        // Initializing PR-DOWNLOADER
        // Enabling database for resume support even after the application is killed:

        if(Core.getInstance() == null) {

            PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                    .setDatabaseEnabled(true)
                    .build();
            PRDownloader.initialize(context.getApplicationContext(), config);
        }
    }

}
