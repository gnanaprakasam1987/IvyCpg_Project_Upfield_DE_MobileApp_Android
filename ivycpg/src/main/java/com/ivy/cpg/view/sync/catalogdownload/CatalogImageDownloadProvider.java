package com.ivy.cpg.view.sync.catalogdownload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.cpg.view.sync.AzureConnectionHelper;
import com.ivy.cpg.view.sync.largefiledownload.FileDownloadProvider;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.NetworkUtils;
import com.ivy.utils.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;

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
        String date = getLastDownloadedDateTime();
        Commons.print("date in log file : " + date);

        //Initiate only if there is not log file.
        if (date.isEmpty()) {
            checkCatalogDownload();
        }
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
        SharedPreferences.Editor editor = AppUtils.getSharedPreferenceByName(businessModel.getApplicationContext(),CatalogDownloadConstants.CATLOG_PREF_NAME).edit();
        editor.putString(CatalogDownloadConstants.STATUS_KEY, status);
        editor.putInt(CatalogDownloadConstants.STATUS_ID, id);
        editor.apply();
    }

    private void storeCatalogDownloadUrl(String url) {
        SharedPreferences.Editor editor = AppUtils.getSharedPreferenceByName(businessModel.getApplicationContext(),CatalogDownloadConstants.CATLOG_PREF_NAME).edit();
        editor.putString(CatalogDownloadConstants.STATUS_URL, url);
        editor.apply();
    }

    public void storeCatalogDownloadStatusError(String error) {
        SharedPreferences.Editor editor = AppUtils.getSharedPreferenceByName(businessModel.getApplicationContext(),CatalogDownloadConstants.CATLOG_PREF_NAME).edit();
        editor.putString(CatalogDownloadConstants.STATUS_ERROR, error);
        editor.apply();
    }

    private int getDownloadId(){
        SharedPreferences editor = AppUtils.getSharedPreferenceByName(businessModel.getApplicationContext(),CatalogDownloadConstants.CATLOG_PREF_NAME);
        return editor.getInt(CatalogDownloadConstants.STATUS_ID,-1);
    }

    private String getCatalogDownloadUrl() {
        SharedPreferences editor = AppUtils.getSharedPreferenceByName(businessModel.getApplicationContext(),CatalogDownloadConstants.CATLOG_PREF_NAME);
        return editor.getString(CatalogDownloadConstants.STATUS_URL, "");
    }

    public String getCatalogDownloadStatus() {
        SharedPreferences editor = AppUtils.getSharedPreferenceByName(businessModel.getApplicationContext(),CatalogDownloadConstants.CATLOG_PREF_NAME);
        return editor.getString(CatalogDownloadConstants.STATUS_KEY, "");
    }

    public void clearCatalogImages() {
        try {
            deleteFiles(getStorageDir(businessModel.getResources().getString(R.string.app_name)));
            storeCatalogDownloadStatus(getDownloadId(),"");
            storeCatalogDownloadUrl("");
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

    public void downloadProcess(Context ctx){

        if (!NetworkUtils.isNetworkConnected(ctx)){
            Intent intent = new Intent("com.ivy.cpg.view.sync.CatalogDownloadStatus");
            // You can also include some extra data.
            intent.putExtra(CatalogDownloadConstants.STATUS, CatalogDownloadConstants.ERROR);
            LocalBroadcastManager.getInstance(businessModel.getContext()).sendBroadcast(intent);
        }else {

            initializePrDownloader(businessModel.getContext());

            String downloadURL;
            if (StringUtils.isNullOrEmpty(getCatalogDownloadUrl()))
                // Prepare download URL path.
                downloadURL = getDownloadUrl();
            else
                downloadURL = getCatalogDownloadUrl();

            // Create location file path to store the downloaded file
            File file = new File(Environment.getExternalStorageDirectory().getPath() + "/");

            downloadId = PRDownloader.download(downloadURL, file.getAbsolutePath(), CatalogDownloadConstants.FILE_NAME)
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {
                            storeCatalogDownloadStatus(downloadId, CatalogDownloadConstants.DOWNLOADING);
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


                            Commons.print("Percentage =" + downloadPercentage
                                    + " - DownloadId =  " + downloadId);

                            Intent intent = new Intent("com.ivy.cpg.view.sync.CatalogDownloadStatus");
                            // You can also include some extra data.
                            intent.putExtra(CatalogDownloadConstants.DOWNLOAD_DETAIL, downloadDetail);
                            intent.putExtra("DownloadPercentage", downloadPercentage);
                            intent.putExtra(CatalogDownloadConstants.STATUS, CatalogDownloadConstants.IN_PROGRESS);
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

                                if (FileUtils.isExternalStorageAvailable(mb * 2)) {
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
                            intent.putExtra(CatalogDownloadConstants.STATUS, CatalogDownloadConstants.COMPLETE);
                            LocalBroadcastManager.getInstance(businessModel.getContext()).sendBroadcast(intent);

                            isDownloadInProgress = false;
                            storeCatalogDownloadUrl("");
                        }

                        @Override
                        public void onError(Error error) {

                            Intent intent = new Intent("com.ivy.cpg.view.sync.CatalogDownloadStatus");
                            // You can also include some extra data.
                            intent.putExtra(CatalogDownloadConstants.STATUS, CatalogDownloadConstants.ERROR);
                            LocalBroadcastManager.getInstance(businessModel.getContext()).sendBroadcast(intent);

                            isDownloadInProgress = false;
                            if (error.isServerError())
                                storeCatalogDownloadUrl("");

                            //storeCatalogDownloadStatus(downloadId,CatalogDownloadConstants.STATUS_ERROR);
                        }
                    });
            storeCatalogDownloadStatus(downloadId, CatalogDownloadConstants.DOWNLOADING);
        }

    }

    private String getDownloadUrl(){
        if (businessModel.configurationMasterHelper.IS_AZURE_CLOUD_STORAGE) {
            AzureConnectionHelper.getInstance().setAzureCredentials(businessModel.getApplicationContext());
            return AzureConnectionHelper.getInstance().getAzureFile("Product/" + CatalogDownloadConstants.FILE_NAME);
        }else if (businessModel.configurationMasterHelper.IS_S3_CLOUD_STORAGE) {
            AWSConnectionHelper.getInstance().setAWSDBValues(businessModel.getApplicationContext());
            return AWSConnectionHelper.getInstance().getSignedAwsUrl(DataMembers.IMG_DOWN_URL+ "Product/" + CatalogDownloadConstants.FILE_NAME);
        }

        return "";
    }

    private void initializePrDownloader(Context context){
        // Initializing PR-DOWNLOADER
        // Enabling database for resume support even after the application is killed:

//        if(Core.getInstance() == null) {

        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setDatabaseEnabled(true)
                .build();
        PRDownloader.initialize(context.getApplicationContext(), config);
//        }
    }

}
