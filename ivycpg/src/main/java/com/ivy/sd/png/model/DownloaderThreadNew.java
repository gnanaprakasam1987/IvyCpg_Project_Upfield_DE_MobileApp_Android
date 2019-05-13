package com.ivy.sd.png.model;

import android.content.Context;
import android.database.Cursor;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.ivy.core.IvyConstants;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;
import com.microsoft.azure.storage.blob.BlobRequestOptions;
import com.microsoft.azure.storage.blob.CloudBlobContainer;
import com.microsoft.azure.storage.blob.CloudBlockBlob;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Downloads a file in a thread. Will send messages to the HomeSceen activity to
 * update the progress bar.
 */
public class DownloaderThreadNew extends Thread {
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    private File mTranDevicePath, mAppDevicePath, mFolderPath, mPrintDevicePath,mPrintFileDevicePath;

    public static final int CONNECTION_TIME_OUT = 10000;

    // instance variables
    private Context parentActivity;
    private HashMap<String, String> downloadUrls;
    private int userID;
    private Handler activityHandler;

    int responseCount = 0;
    boolean isImageDownloadCancelled = false;
    int mTotalSize = 0;
    int downloadPercentage = 0;
    Message msg;
    TransferUtility tm = null;

    private CloudBlobContainer cloudBlobContainer;

    private boolean alertshown = false;
    private BusinessModel bmodel;
    private String start_time = "";
    private int successCount = 0;

    /**
     * Instantiates a new DownloaderThread object.
     *
     * @param inParentActivity - Reference to activity.
     * @param h                - Reference to handler in activity.
     * @param imgUrls          - String representing the URL of the file to be downloaded.
     * @param Userid           - userid
     * @param transferUtility  - Type of Data download , zip or apk or others
     */
    public DownloaderThreadNew(Context inParentActivity, Handler h,
                               HashMap<String, String> imgUrls, int Userid, TransferUtility transferUtility) {
        if (imgUrls != null) {
            downloadUrls = imgUrls;
        }
        parentActivity = inParentActivity;
        activityHandler = h;
        userID = Userid;
        tm = transferUtility;
        bmodel = (BusinessModel) inParentActivity.getApplicationContext();
        if (StringUtils.isEmptyString(start_time))
            start_time = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);
    }

    public DownloaderThreadNew(Context context, Handler handler,  HashMap<String, String> imgUrls,int userID, CloudBlobContainer cloudBlobContainer) {
        parentActivity = context;
        activityHandler = handler;
        this.userID = userID;
        this.cloudBlobContainer = cloudBlobContainer;
        if (imgUrls != null) {
            downloadUrls = imgUrls;
        }
        bmodel = (BusinessModel) context.getApplicationContext();
        if (StringUtils.isEmptyString(start_time))
            start_time = DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW);
    }

    /**
     * Connects to the URL of the file, begins the download, and notifies the
     * HomeScreen activity of changes in state. Writes the file to the root of
     * the SD card.
     */
    @Override
    public void run() {

        if (!isExternalStorageAvailable()) {
            String errMsg = parentActivity
                    .getString(R.string.external_storage_not_available);
            msg = Message.obtain(activityHandler,
                    DataMembers.SDCARD_NOT_AVAILABLE, 0, 0, errMsg);
            activityHandler.sendMessage(msg);
        } else {
            try {

                boolean isAmazonCloud = true;
                boolean isAzureCloud = false;
                boolean isSFDCCloud = false;
                DBUtil db = new DBUtil(parentActivity, DataMembers.DB_NAME
                );
                db.createDataBase();
                db.openDataBase();
                Cursor c = db
                        .selectSQL("SELECT Rfield FROM HHTModuleMaster where hhtCode = 'CLOUD_STORAGE' and flag = 1 and ForSwitchSeller = 0");
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


                db.closeDB();
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_DOWNLOAD_STARTED, downloadUrls.size(), 0,
                        "Digital Contents");
                activityHandler.sendMessage(msg);

                mTranDevicePath = new File(
                        parentActivity
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/" + userID + DataMembers.DIGITAL_CONTENT);

                mAppDevicePath = new File(
                        parentActivity
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + userID
                                + DataMembers.APP_DIGITAL_CONTENT);

                mPrintDevicePath = new File(
                        parentActivity
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + userID
                                + DataMembers.PRINT);

                mPrintFileDevicePath = new File(
                        parentActivity
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + userID
                                + DataMembers.PRINTFILE);


                if (!mTranDevicePath.exists())
                    mTranDevicePath.mkdir();

                if (!mAppDevicePath.exists())
                    mAppDevicePath.mkdir();

                if (!mPrintDevicePath.exists())
                    mPrintDevicePath.mkdir();

                if (!mPrintFileDevicePath.exists())
                    mPrintFileDevicePath.mkdir();

                String mFileName = "";

                int index = 0;

                int i = 0;
                downloadPercentage = 0;

                float a = 0, b = 0;

                mTotalSize = downloadUrls.size();

                URL url;
                URLConnection conn;

                BufferedInputStream inStream;
                BufferedOutputStream outStream;
                File outFile;
                FileOutputStream fileStream;

                boolean availe_flag = false;
                File mfile, appfile, mPrintFile,mPrintFormatFile;
                // AmazonS3Client s3 = null;

                if (isAzureCloud) {
                    for (Entry<String, String> imageurl : downloadUrls.entrySet()) {
                        String imagurl = imageurl.getKey();
                        String folderName = imageurl.getValue();

                        try {
                            // get the filename
                            mFileName = "file.bin";

                            index = imagurl.lastIndexOf('/');

                            if (index >= 0) {
                                mFileName = imagurl.substring(index + 1);
                            }
                            if (mFileName.equals("")) {
                                mFileName = "file.bin";
                            }
                            // read and write the content

                            if (folderName
                                    .equalsIgnoreCase(DataMembers.APP_DIGITAL_CONTENT)) {
                                outFile = new File(mAppDevicePath + "/"
                                        + mFileName.replaceAll("%20", " "));
                            } else if (folderName
                                    .equalsIgnoreCase(DataMembers.PRINT)) {
                                outFile = new File(mPrintDevicePath + "/"
                                        + mFileName.replaceAll("%20", " "));

                            } else if (folderName
                                    .equalsIgnoreCase(DataMembers.PRINTFILE)) {
                                outFile = new File(mPrintFileDevicePath + "/"
                                        + mFileName.replaceAll("%20", " "));

                            } else {

                                mFolderPath = new File(mTranDevicePath + "/"
                                        + folderName);

                                if (!mFolderPath.exists())
                                    mFolderPath.mkdir();

                                outFile = new File(mFolderPath + "/"
                                        + mFileName.replaceAll("%20", " "));
                            }
                            mfile = new File(mTranDevicePath + "/" + folderName + "/" + mFileName);
                            appfile = new File(mAppDevicePath + "/" + mFileName);
                            mPrintFile = new File(mPrintDevicePath + "/" + mFileName);
                            mPrintFormatFile = new File(mPrintFileDevicePath + "/" + mFileName);

                            if (mfile.exists()) {
                                availe_flag = true;
                            } else if (appfile.exists()) {
                                availe_flag = true;
                            } else if (mPrintFile.exists()) {
                                availe_flag = true;
                            } else if (mPrintFormatFile.exists()) {
                                availe_flag = true;
                            } else {
                                availe_flag = false;
                            }

                            if (!availe_flag) {

                                String downloadURL;
                                if (imagurl.equalsIgnoreCase("")) {
                                    downloadURL = mFileName;
                                } else {
                                    downloadURL = imagurl ;
                                }

                                CloudBlockBlob blob;
                                if (ConfigurationMasterHelper.ACCESS_KEY_ID.equalsIgnoreCase(IvyConstants.SAS_KEY_TYPE)){
                                      downloadURL = AppUtils.buildAzureUrl(downloadURL) ;
                                      blob = new CloudBlockBlob(new URI(downloadURL));
                                }  else {
                                    blob = cloudBlobContainer.getBlockBlobReference(downloadURL);
                                }

                                if (blob.exists()) {

                                    if (ConfigurationMasterHelper.ACCESS_KEY_ID.equalsIgnoreCase(IvyConstants.SAS_KEY_TYPE)){
                                        blob.downloadToFile(outFile.getAbsolutePath());
                                    }else {
                                        BlobRequestOptions options = new BlobRequestOptions();
                                        options.setDisableContentMD5Validation(true);
                                        options.setStoreBlobContentMD5(false);
                                        blob.downloadAttributes();
                                        blob.downloadToFile(outFile.getAbsolutePath(), null, options, null);
                                    }
                                    i++;
                                    a = (float) i / (float) mTotalSize;
                                    b = a * 100;
                                    downloadPercentage = (int) b;

                                    msg = Message.obtain(activityHandler,
                                            DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                            downloadPercentage, 0);
                                    activityHandler.sendMessage(msg);
                                }
                                else {
                                    Commons.print(mFileName + " not Present in the Blob");
                                    i++;
                                    a = (float) i / (float) mTotalSize;
                                    b = a * 100;
                                    downloadPercentage = (int) b;

                                    msg = Message.obtain(activityHandler,
                                            DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                            downloadPercentage, 0);
                                    activityHandler.sendMessage(msg);
                                }
                            } else {
                                i++;
                                a = (float) i / (float) mTotalSize;
                                b = a * 100;
                                downloadPercentage = (int) b;

                                msg = Message.obtain(activityHandler,
                                        DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                        downloadPercentage, 0);
                                activityHandler.sendMessage(msg);
                            }
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                    }
                }else {
                    if (isAmazonCloud) {
                        System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
                        try {
                            org.xml.sax.XMLReader reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                        } catch (org.xml.sax.SAXException e) {
                            Commons.printException("Unable to load XMLReader " + e.getMessage(), e);
                        }
                        System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");

//                    Commons.print(ConfigurationMasterHelper.ACCESS_KEY_ID);
//                    Commons.print(ConfigurationMasterHelper.SECRET_KEY);
//                    Commons.print("Buket>>>>" + DataMembers.S3_BUCKET);
                   /* BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                            ConfigurationMasterHelper.SECRET_KEY);
                    s3 = new AmazonS3Client(myCredentials);
                     tm = new TransferUtility(s3,parentActivity.getApplicationContext());*/
                    }

                    for (Entry<String, String> imageurl : downloadUrls.entrySet()) {
                        try {
                            String imagurl = imageurl.getKey();
                            String folderName = imageurl.getValue();

                            if (isAmazonCloud) {
                                // get the filename
                                mFileName = "file.bin";

                                index = imagurl.lastIndexOf('/');

                                if (index >= 0) {
                                    mFileName = imagurl.substring(index + 1);
                                }
                                if (mFileName.equals("")) {
                                    mFileName = "file.bin";
                                }
                                // read and write the content

                                if (folderName
                                        .equalsIgnoreCase(DataMembers.APP_DIGITAL_CONTENT)) {
                                    outFile = new File(mAppDevicePath + "/"
                                            + mFileName.replaceAll("%20", " "));
                                } else if (folderName
                                        .equalsIgnoreCase(DataMembers.PRINT)) {
                                    outFile = new File(mPrintDevicePath + "/"
                                            + mFileName.replaceAll("%20", " "));

                                } else if (folderName
                                        .equalsIgnoreCase(DataMembers.PRINTFILE)) {
                                    outFile = new File(mPrintFileDevicePath + "/"
                                            + mFileName.replaceAll("%20", " "));

                                } else {

                                    mFolderPath = new File(mTranDevicePath + "/"
                                            + folderName);

                                    if (!mFolderPath.exists())
                                        mFolderPath.mkdir();

                                    outFile = new File(mFolderPath + "/"
                                            + mFileName.replaceAll("%20", " "));
                                }
                                mfile = new File(mTranDevicePath + "/" + folderName + "/" + mFileName);
                                appfile = new File(mAppDevicePath + "/" + mFileName);
                                mPrintFile = new File(mPrintDevicePath + "/" + mFileName);
                                mPrintFormatFile = new File(mPrintFileDevicePath + "/" + mFileName);

                                if (mfile.exists()) {
                                    availe_flag = true;
                                } else if (appfile.exists()) {
                                    availe_flag = true;
                                } else if (mPrintFile.exists()) {
                                    availe_flag = true;
                                } else if (mPrintFormatFile.exists()) {
                                    availe_flag = true;
                                } else {
                                    availe_flag = false;
                                }
                                if (!availe_flag) {
                                    TransferObserver observer = tm.download(DataMembers.S3_BUCKET, imagurl, outFile);
                                    observer.setTransferListener(new TransferListener() {
                                        @Override
                                        public void onStateChanged(int i, TransferState transferState) {

                                            if (transferState == TransferState.COMPLETED) {
                                                successCount++;
                                                responseCount++;
                                                downloadPercentage = (int) (((float) responseCount / (float) mTotalSize) * 100);
                                            } else if (transferState == TransferState.FAILED) {
                                                responseCount++;
                                                downloadPercentage = (int) (((float) responseCount / (float) mTotalSize) * 100);
                                            } else if (transferState == TransferState.CANCELED) {
                                                isImageDownloadCancelled = true;
                                                responseCount++;
                                                downloadPercentage = (int) (((float) responseCount / (float) mTotalSize) * 100);
                                            }

                                            msg = Message.obtain(activityHandler,
                                                    DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                                    responseCount, 0);
                                            activityHandler.sendMessage(msg);
                                            if (responseCount >= mTotalSize && !alertshown && !isImageDownloadCancelled) {

                                                alertshown = true;
                                                msg = Message.obtain(activityHandler,
                                                        DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC, 0, 0);
                                                activityHandler.sendMessage(msg);
                                                String status = SynchronizationHelper.SYNC_STATUS_COMPLETED;
                                                 if (successCount == 0)
                                                    status = SynchronizationHelper.SYNC_STATUS_FAILED;
                                                 else if (successCount < mTotalSize)
                                                     status = SynchronizationHelper.SYNC_STATUS_PARTIAL;

                                                bmodel.synchronizationHelper.insertSyncHeader(start_time, DateTimeUtils.now(DateTimeUtils.DATE_TIME_NEW), SynchronizationHelper.SYNC_TYPE_DGT_DOWNLOAD,
                                                        successCount, status, mTotalSize);
                                                successCount = 0;
                                            }

                                        }

                                        @Override
                                        public void onProgressChanged(int i, long l, long l1) {
                                        }

                                        @Override
                                        public void onError(int i, Exception e) {
                                            Commons.printException("onError: ," + e + "");
                                       /* responseCount++;
                                        if (responseCount >= mTotalSize) {
                                           *//* Commons.print("responseCount >= mTotalSize error");
                                            msg = Message.obtain(activityHandler,
                                                    DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC, 0, 0);
                                            activityHandler.sendMessage(msg);*//*

                                        }*/
                                        }
                                    });

                                } else {
                                    responseCount++;
                                    downloadPercentage = (int) (((float) responseCount / (float) mTotalSize) * 100);
                                    msg = Message.obtain(activityHandler,
                                            DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                            responseCount, 0);
                                    activityHandler.sendMessage(msg);
                                    if (responseCount >= mTotalSize && !alertshown && !isImageDownloadCancelled) {

                                        alertshown = true;
                                        msg = Message.obtain(activityHandler,
                                                DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC, 0, 0);
                                        activityHandler.sendMessage(msg);
                                    }
                                }

                            } else {

                                // make connection
                                url = new URL(imagurl.replaceAll(" ", "%20"));
                                conn = url.openConnection();
                                conn.setDoInput(true);
                                conn.setConnectTimeout(CONNECTION_TIME_OUT);
                                conn.setUseCaches(false);

                                // get the filename
                                mFileName = "file.bin";

                                index = url.toString().lastIndexOf('/');

                                if (index >= 0) {
                                    mFileName = url.toString().substring(index + 1);
                                }
                                if (mFileName.equals("")) {
                                    mFileName = "file.bin";
                                }
                                // read and write the content

                                if (folderName
                                        .equalsIgnoreCase(DataMembers.APP_DIGITAL_CONTENT)) {
                                    outFile = new File(mAppDevicePath + "/"
                                            + mFileName.replaceAll("%20", " "));
                                } else {

                                    mFolderPath = new File(mTranDevicePath + "/"
                                            + folderName);

                                    if (!mFolderPath.exists())
                                        mFolderPath.mkdir();

                                    outFile = new File(mFolderPath + "/"
                                            + mFileName.replaceAll("%20", " "));
                                }

                                if (!outFile.exists()) {

                                    inStream = new BufferedInputStream(
                                            conn.getInputStream(), DOWNLOAD_BUFFER_SIZE);

                                    fileStream = new FileOutputStream(outFile);

                                    outStream = new BufferedOutputStream(fileStream,
                                            DOWNLOAD_BUFFER_SIZE);

                                    byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];

                                    int bytesRead = 0;

                                    while (!isInterrupted()
                                            && (bytesRead = inStream.read(data, 0,
                                            data.length)) >= 0) {
                                        outStream.write(data, 0, bytesRead);
                                    }

                                    outStream.close();
                                    fileStream.close();
                                    inStream.close();
                                }
                                if (isInterrupted()) {
                                    outFile.delete();
                                    break;
                                }

                                i++;

                                a = (float) i / (float) mTotalSize;
                                b = a * 100;
                                downloadPercentage = (int) b;

                                msg = Message.obtain(activityHandler,
                                        DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                        i, 0);
                                activityHandler.sendMessage(msg);

                            }
                        } catch (Exception e) {

                            Commons.printException("Error in URL," + "" + e);
                        }

                    }
                }
                if (!isInterrupted() && !isAmazonCloud) {
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_DOWNLOAD_COMPLETE_DC, 0, 0);
                    activityHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Commons.printException(e);
                String errMsg = parentActivity
                        .getString(R.string.error_message_general);
                if (!isInterrupted()) {
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_ENCOUNTERED_ERROR_DC, 0, 0, errMsg);
                    activityHandler.sendMessage(msg);
                }


            }
        }
    }


    private boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        return mExternalStorageAvailable
                && mExternalStorageWriteable && mbAvailable > 10;
    }

}
