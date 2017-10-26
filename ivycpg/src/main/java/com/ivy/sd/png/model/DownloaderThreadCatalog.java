package com.ivy.sd.png.model;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;

import com.amazonaws.SDKGlobalConfiguration;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.SynchronizationFragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Downloads a file in a thread. Will send messages to the HomeSceen activity to
 * update the progress bar.
 */
public class DownloaderThreadCatalog extends Thread implements Runnable {
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    private File mTranDevicePath, mFolderPath;

    public static final int CONNECTION_TIME_OUT = 10000;

    // instance variables
    private Context parentActivity;
    private ArrayList<S3ObjectSummary> downloadUrls;
    private int userID;
    private Handler activityHandler;

    int responseCount = 0;
    int mTotalSize = 0;
    int downloadPercentage = 0;
    Message msg;
    TransferUtility tm = null;


    private boolean alertshown = false;
    private String TAG_MODIFIED = "S";

    /*S3ObjectSummary oject bucketName = FolderName
    * eTag = isModified
    * key = URL*/

    /**
     * Instantiates a new DownloaderThread object.
     *
     * @param inParentActivity - Reference to activity.
     * @param h                - Reference to handler in activity.
     * @param imgUrls          - String representing the URL of the file to be downloaded.
     * @param Userid           - userid
     * @param transferUtility  - Type of Data download , zip or apk or others
     */
    public DownloaderThreadCatalog(Context inParentActivity, Handler h,
                                   ArrayList<S3ObjectSummary> imgUrls, int Userid, TransferUtility transferUtility) {
        if (imgUrls != null) {
            downloadUrls = imgUrls;
        }
        parentActivity = inParentActivity;
        activityHandler = h;
        userID = Userid;
        tm = transferUtility;
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

                boolean isAmazonUpload = false;
                DBUtil db = new DBUtil(parentActivity, DataMembers.DB_NAME,
                        DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();
                Cursor c = db
                        .selectSQL("SELECT flag FROM HHTModuleMaster where hhtCode = 'ISAMAZON_IMGUPLOAD' and flag = 1");
                if (c != null) {
                    while (c.moveToNext()) {
                        isAmazonUpload = true;
                    }
                }
                c.close();
                db.closeDB();

                mTranDevicePath = new File(
                        parentActivity
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/" + userID + DataMembers.DIGITAL_CONTENT);

                if (!mTranDevicePath.exists())
                    mTranDevicePath.mkdirs();

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
                File mfile;
                // AmazonS3Client s3 = null;

                if (isAmazonUpload) {
                    System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");
                    try {
                        org.xml.sax.XMLReader reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();
                    } catch (org.xml.sax.SAXException e) {
                        Commons.printException("Unable to load XMLReader " + e.getMessage(), e);
                    }
                    System.setProperty(SDKGlobalConfiguration.ENABLE_S3_SIGV4_SYSTEM_PROPERTY, "true");

                }

                for (S3ObjectSummary s3ObjectSummary : downloadUrls) {
                    try {
                        String imagurl = s3ObjectSummary.getKey();
                        String folderName = s3ObjectSummary.getBucketName();
                        String isModified = s3ObjectSummary.getETag();

                        if (isAmazonUpload) {
                            // get the filename
                            mFileName = "file.bin";

                            index = imagurl.lastIndexOf('/');

                            if (index >= 0) {
                                mFileName = imagurl.substring(index + 1);
                            }
                            if (mFileName.equals("")) {
                                mFileName = "file.bin";
                            }

                            mFolderPath = new File(mTranDevicePath, folderName);

                            if (!mFolderPath.exists())
                                mFolderPath.mkdirs();

                            outFile = new File(mFolderPath + "/" + mFileName.replaceAll("%20", " "));

                            mfile = new File(mTranDevicePath + "/" + folderName + "/" + mFileName);

                            if (mfile.exists()) {
                                availe_flag = true;
                                if (isModified.equals(TAG_MODIFIED)) {
                                    mfile.delete();
                                    availe_flag = false;
                                }
                            } else {
                                availe_flag = false;
                            }
                            if (!availe_flag) {
                                new CatalogImagesDownload(outFile, imagurl).execute();

                            } else {
                                responseCount++;
                                downloadPercentage = (int) (((float) responseCount / (float) mTotalSize) * 100);
                                msg = Message.obtain(activityHandler,
                                        DataMembers.MESSAGE_UPDATE_PROGRESS_CATALOG,
                                        responseCount, mTotalSize);
                                activityHandler.sendMessage(msg);
                                if (responseCount >= mTotalSize && !alertshown) {

                                    alertshown = true;
                                    msg = Message.obtain(activityHandler,
                                            DataMembers.MESSAGE_DOWNLOAD_COMPLETE_CATALOG, responseCount, mTotalSize);
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

                            mFolderPath = new File(mTranDevicePath + "/"
                                    + folderName);

                            if (!mFolderPath.exists())
                                mFolderPath.mkdir();

                            outFile = new File(mFolderPath + "/"
                                    + mFileName.replaceAll("%20", " "));

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
                                    DataMembers.MESSAGE_UPDATE_PROGRESS_CATALOG,
                                    i, mTotalSize);
                            activityHandler.sendMessage(msg);

                        }
                    } catch (Exception e) {

                        Commons.printException("Error in URL," + "" + e);
                        continue;
                    }
                }

                if (downloadUrls.size() == 0) {
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_DOWNLOAD_COMPLETE_CATALOG, mTotalSize, mTotalSize);
                    activityHandler.sendMessage(msg);
                }

                if (!isInterrupted() && !isAmazonUpload) {
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_DOWNLOAD_COMPLETE_CATALOG, mTotalSize, mTotalSize);
                    activityHandler.sendMessage(msg);
                }
            } catch (Exception e) {
                Commons.printException(e);
                String errMsg = parentActivity
                        .getString(R.string.error_message_general);
                if (!isInterrupted()) {
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_ENCOUNTERED_ERROR_CATALOG, responseCount, mTotalSize, errMsg);
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

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true && mbAvailable > 10) {
            return true;
        } else {
            return false;
        }
    }

    public class CatalogImagesDownload extends AsyncTask<String, Void, String> {

        File outFile;
        String imgUrl;

        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        FileOutputStream fileStream;

        public CatalogImagesDownload(File outFile, String imgUrl) {
            super();
            this.outFile = outFile;
            this.imgUrl = imgUrl;
        }


        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            try {
                BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                        ConfigurationMasterHelper.SECRET_KEY);
                AmazonS3Client s3 = new AmazonS3Client(myCredentials);

                S3ObjectInputStream content = s3.getObject(DataMembers.S3_BUCKET, imgUrl).getObjectContent();


                inStream = new BufferedInputStream(
                        content, DOWNLOAD_BUFFER_SIZE);


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

                if (isInterrupted()) {
                    outFile.delete();
                }

                responseCount++;
                downloadPercentage = (int) (((float) responseCount / (float) mTotalSize) * 100);

                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_UPDATE_PROGRESS_CATALOG,
                        responseCount, mTotalSize);
                activityHandler.sendMessage(msg);
                if (responseCount >= mTotalSize && !alertshown) {

                    alertshown = true;
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_DOWNLOAD_COMPLETE_CATALOG, responseCount, mTotalSize);
                    activityHandler.sendMessage(msg);
                }


            } catch (Exception e) {
                // TODO Auto-generated catch block
                Commons.printException(e);
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_ENCOUNTERED_ERROR_CATALOG, responseCount, mTotalSize, "Unable to create Folder");
                activityHandler.sendMessage(msg);
            }
            return null;
        }

    }

}
