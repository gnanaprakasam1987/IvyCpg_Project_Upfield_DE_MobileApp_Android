package com.ivy.cpg.view.sync.largefiledownload;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;
import com.ivy.cpg.view.sync.AWSConnectionHelper;
import com.ivy.cpg.view.sync.AzureConnectionHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class FileDownloadIntentService extends IntentService {

    public static boolean isServiceRunning = false;
    private Context context;
    private ArrayList<DigitalContentModel> downloadUrlList = new ArrayList<>();
    private int count = 0;
    private DecimalFormat df = new DecimalFormat("0.000");

    private String downloadType= "AWS";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public FileDownloadIntentService() {
        super(FileDownloadIntentService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        context = getApplicationContext();

        Commons.print("FileDownloadIntentService Started");

        if (intent != null && intent.getParcelableArrayListExtra("DigiContent") != null) {

            isServiceRunning = true;
            downloadUrlList = intent.getParcelableArrayListExtra("DigiContent");

            downloadType = intent.getExtras()!= null?intent.getExtras().getString("DownloadType","AWS"):"AWS";

            if (downloadType.equalsIgnoreCase("AWS"))
                AWSConnectionHelper.getInstance().setAmazonS3Credentials(context);
            else
                AzureConnectionHelper.getInstance().setAzureCredentials(context);

            createFileStartDownload(downloadUrlList.get(count));
        }
    }

    /**
     * One after another download will occur... sequence download process
     */
    public void startProcess() {
        if (count < downloadUrlList.size() - 1) {
            count = count + 1;
            createFileStartDownload(downloadUrlList.get(count));
        } else if (count == downloadUrlList.size() - 1) {
            Commons.print("FileDownloadIntentService isServiceRunning " + isServiceRunning);
            isServiceRunning = false;
            stopSelf();
        }
    }

    /**
     * Updating the download progress to the UI
     */
    private void updateProgress(int id, long bytesCurrent, long bytesTotal,
                                String downloadDetail, int percent,
                                DigitalContentModel digitalContentModel) {
        Intent intent = new Intent("ProgressUpdate");
        intent.putExtra("downloadId", id);
        intent.putExtra("DigitalId", digitalContentModel.getImageID());
        intent.putExtra("bytesCurrent", bytesCurrent);
        intent.putExtra("bytesTotal", bytesTotal);
        intent.putExtra("Percent", percent);
        intent.putExtra("DownloadDetail", downloadDetail);

        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    /**
     * Creates the file and start download the file download using PRdownload
     * If File already found and within the expiry time then it will resume the download.
     */
    private void createFileStartDownload(DigitalContentModel digitalContentBO) {

        Commons.print("FileDownloadIntentService url " + digitalContentBO.getImgUrl());

        if (digitalContentBO.getStatus() != null && digitalContentBO.getStatus().equals(FileDownloadProvider.DONE)) {
            startProcess();
        } else {

            int userId = digitalContentBO.getUserId();

            File mTranDevicePath, mAppDevicePath, mFolderPath, mPrintDevicePath, mPrintFileDevicePath;

            mTranDevicePath = new File(
                    context
                            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/" + userId + DataMembers.DIGITAL_CONTENT);

            mAppDevicePath = new File(
                    context
                            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + userId
                            + DataMembers.APP_DIGITAL_CONTENT);

            mPrintDevicePath = new File(
                    context
                            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + userId
                            + DataMembers.PRINT);

            mPrintFileDevicePath = new File(
                    context
                            .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + userId
                            + DataMembers.PRINTFILE);


            if (!mTranDevicePath.exists())
                mTranDevicePath.mkdir();

            if (!mAppDevicePath.exists())
                mAppDevicePath.mkdir();

            if (!mPrintDevicePath.exists())
                mPrintDevicePath.mkdir();

            if (!mPrintFileDevicePath.exists())
                mPrintFileDevicePath.mkdir();

            String mFileName;
            int index;
            File outFile;

            mFileName = "file.bin";

            index = digitalContentBO.getImgUrl().lastIndexOf('/');

            if (index >= 0) {
                mFileName = digitalContentBO.getImgUrl().substring(index + 1);
            }
            if (mFileName.equals("")) {
                mFileName = "file.bin";
            }
            // read and write the content

            if (digitalContentBO.getContentFrom()
                    .equalsIgnoreCase(DataMembers.APP_DIGITAL_CONTENT)) {
                outFile = mAppDevicePath;
            } else if (digitalContentBO.getContentFrom()
                    .equalsIgnoreCase(DataMembers.PRINT)) {
                outFile = mPrintDevicePath;

            } else if (digitalContentBO.getContentFrom()
                    .equalsIgnoreCase(DataMembers.PRINTFILE)) {
                outFile = mPrintFileDevicePath;

            } else {

                mFolderPath = new File(mTranDevicePath + "/"
                        + digitalContentBO.getContentFrom());

                if (!mFolderPath.exists())
                    mFolderPath.mkdir();

                outFile = mFolderPath;
            }

            String signedUrl;
            if (digitalContentBO.getSignedUrl() != null && digitalContentBO.getSignedUrl().length() > 0) {
                signedUrl = digitalContentBO.getSignedUrl();
            } else {
                if (downloadType.equalsIgnoreCase("AWS"))
                    signedUrl = AWSConnectionHelper.getInstance().getSignedAwsUrl(digitalContentBO.getImgUrl());
                else
                    signedUrl = AzureConnectionHelper.getInstance().getAzureFile(digitalContentBO.getImgUrl());
                digitalContentBO.setSignedUrl(signedUrl);
            }

            int downloadId = PRDownloader.download(signedUrl, outFile.getAbsolutePath(), mFileName)
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {

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

                            digitalContentBO.setDownloadDetail(downloadDetail);
                            digitalContentBO.setPercent(downloadPercentage);
                            digitalContentBO.setStatus("IN-PROGRESS");

                            updateProgress(digitalContentBO.getDownloadId(),
                                    progress.currentBytes,
                                    progress.totalBytes,
                                    downloadDetail,
                                    downloadPercentage,
                                    digitalContentBO);
                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            digitalContentBO.setStatus(FileDownloadProvider.DONE);
                            digitalContentBO.setPercent(100);
                            FileDownloadProvider.getInstance(context).prepareDigitalContentSaveList(digitalContentBO);
                            startProcess();
                        }

                        @Override
                        public void onError(Error error) {

                            if (error.isServerError())
                                digitalContentBO.setSignedUrl("");

                            digitalContentBO.setStatus(FileDownloadProvider.STATUS_ERROR);
                            FileDownloadProvider.getInstance(context).prepareDigitalContentSaveList(digitalContentBO);
                            startProcess();
                        }
                    });

            digitalContentBO.setDownloadId(downloadId);
            digitalContentBO.setStatus("DOWNLOADING");
            digitalContentBO.setFileName(mFileName);

            // Store download id in shared preference.
            FileDownloadProvider.getInstance(context).prepareDigitalContentSaveList(digitalContentBO);

        }
    }

}
