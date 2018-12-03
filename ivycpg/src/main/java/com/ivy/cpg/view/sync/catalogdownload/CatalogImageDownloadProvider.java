package com.ivy.cpg.view.sync.catalogdownload;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

/**
 * Created by abbas.a on 15/02/18.
 */

public class CatalogImageDownloadProvider {

    public static CatalogImageDownloadProvider instance;
    BusinessModel businessModel;

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
        callCatalogImageDownload(new DownloadListener(businessModel));
    }

    /**
     * Call this method on first time login to initiate the full download.
     */
    public void callCatalogImageDownload(TransferListener tx) {

        try {
            // Load last downloaed date from SDCard log file.
            String date = getLastDownloadedDateTime();
            Commons.print("date in log file : " + date);

            //Initiate only if there is not log file.
            if (date.isEmpty()) {
                // Set Digital content download path.
                businessModel.getimageDownloadURL();
                // Load credentials
                businessModel.configurationMasterHelper.setAmazonS3Credentials();

                // Initilise transfer utility
                TransferUtility transferUtility = Util.getTransferUtility(businessModel);

                // Create location file path to store the downloaded file
                File file = new File(Environment.getExternalStorageDirectory().getPath() + "/" + CatalogDownloadConstants.FILE_NAME);
                // Prepare download URL path.
                String downloadURL = DataMembers.IMG_DOWN_URL + "Product/" + CatalogDownloadConstants.FILE_NAME;

                // Initiate the download
                TransferObserver observer = transferUtility.download(DataMembers.S3_BUCKET, downloadURL, file);
                observer.setTransferListener(tx);

                // Store download id in shared preference.
                storeCatalogDownloadStatus(observer.getId(), "DOWNLOADING");
            }
        } catch (Exception e) {
            //TODO: clear the text file. Or call at right palce.
            Commons.printException(e);
        }

    }

    /**
     * Call this method on local login to initiate catalog download if app crashed.
     */
    public void checkCatalogDownload() {
        if (getCatalogDownloadStatus().equals(CatalogDownloadConstants.DOWNLOADING)) {
            // Set Digital content download path.
            businessModel.getimageDownloadURL();
            businessModel.configurationMasterHelper.setAmazonS3Credentials();
            TransferUtility transferUtility = Util.getTransferUtility(businessModel.getApplicationContext());
            List<TransferObserver> observers = transferUtility.getTransfersWithType(TransferType.DOWNLOAD);
            TransferListener listener = new DownloadListener(businessModel.getApplicationContext());
            for (TransferObserver observer : observers) {

                if (getCatalogDownloadStatusId() == observer.getId()) {

                    // Sets listeners to in progress transfers
                    if (TransferState.WAITING.equals(observer.getState())
                            || TransferState.WAITING_FOR_NETWORK.equals(observer.getState())
                            || TransferState.IN_PROGRESS.equals(observer.getState()) || TransferState.FAILED.equals(observer.getState())) {

                        observer.cleanTransferListener();
                        observer.setTransferListener(listener);
                        TransferObserver resumed = transferUtility.resume(observer.getId());
                    }

                }

            }
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

    public void storeCatalogDownloadStatusError(String error) {
        SharedPreferences.Editor editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE).edit();
        editor.putString(CatalogDownloadConstants.STATUS_ERROR, error);
        editor.apply();
    }

    public int getCatalogDownloadStatusId() {
        SharedPreferences editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE);
        return editor.getInt(CatalogDownloadConstants.STATUS_ID, 0);
    }

    public String getCatalogDownloadStatus() {
        SharedPreferences editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE);
        return editor.getString(CatalogDownloadConstants.STATUS_KEY, "");
    }

    public String getCatalogDownloadStatusError() {
        SharedPreferences editor = businessModel.getSharedPreferences(CatalogDownloadConstants.CATLOG_PREF_NAME, MODE_PRIVATE);
        return editor.getString(CatalogDownloadConstants.STATUS_ERROR, "");
    }

    /*
     * A TransferListener class that can listen to a download task and be
     * notified when the status changes.
     */
    public class DownloadListener implements TransferListener {

        Context ctx;

        DownloadListener(Context ctx) {
            this.ctx = ctx;
        }

        // Simply updates the list when notified.
        @Override
        public void onError(int id, Exception e) {
            Log.e("IvyCPG", "onError: " + id, e);

        }


        @Override
        public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
            Log.d("IvyCPG", String.format("onProgressChanged: %d, total: %d, current: %d",
                    id, bytesTotal, bytesCurrent));

        }

        @Override
        public void onStateChanged(int id, TransferState state) {
            Log.d("IvyCPG", "onStateChanged: " + id + ", " + state);
            if (state.equals(TransferState.COMPLETED)) {


                // update shared preference
                storeCatalogDownloadStatus(getCatalogDownloadStatusId(), CatalogDownloadConstants.UNZIP);


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


            }

        }


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
}
