package com.ivy.sd.png.model;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.provider.Settings.Secure;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Downloads APK in a thread. Will send messages to
 * update progress bar.
 */
public class ApkDownloaderThread extends Thread {
    
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;
    public static final int APK_DOWNLOAD = 152;
    public static final int CONNECTION_TIME_OUT = 20000;

    // instance variables
    private Context parentActivity;
    private String downloadUrl;
    private Handler activityHandler;
    private int type;

    /**
     * Instantiates a new ApkDownloaderThread object.
     *
     * @param inParentActivity   Reference to activity.
     * @param h          Reference to handler in activity.
     * @param inUrl            String representing the URL of the file to be downloaded.
     * @param isDigitalContent is Digital content Download.
     * @param type             Type of Data download , zip or apk or others
     */
    public ApkDownloaderThread(Context inParentActivity, Handler h, String inUrl,
                               boolean isDigitalContent, int type) {
        downloadUrl = "";
        if (inUrl != null) {
            downloadUrl = inUrl;
        }
        parentActivity = inParentActivity;
        activityHandler = h;
        type=type;

    }

    /**
     * Connects to the URL of the file, begins the download, and notifies the
     * HomeScreen activity of changes in state. Writes the file to the root of
     * the SD card.
     */
    @Override
    public void run() {
        URL url;
        URLConnection conn;
        int fileSize, lastSlash;
        String fileName;
        BufferedInputStream inStream;
        BufferedOutputStream outStream;
        File outFile;
        FileOutputStream fileStream;
        Message msg;

        if (!isNonMarketInstallationOptionEnable()) {
            String errMsg = parentActivity
                    .getString(R.string.thirdparty_installation_error);
            msg = Message.obtain(activityHandler,
                    DataMembers.THIRD_PARTY_INSTALLATION_ERROR, 0, 0, errMsg);
            activityHandler.sendMessage(msg);
        } else if (!isExternalStorageAvailable()) {
            String errMsg = parentActivity
                    .getString(R.string.external_storage_not_available);
            msg = Message.obtain(activityHandler,
                    DataMembers.SDCARD_NOT_AVAILABLE, 0, 0, errMsg);
            activityHandler.sendMessage(msg);
        } else {
            // we're going to connect now
            msg = Message.obtain(activityHandler,
                    DataMembers.MESSAGE_CONNECTING_STARTED, 0, 0, downloadUrl);
            activityHandler.sendMessage(msg);

            try {
                url = new URL(downloadUrl);
                conn = url.openConnection();
                conn.setDoInput(true);
                conn.setConnectTimeout(CONNECTION_TIME_OUT);
                conn.setUseCaches(false);
                fileSize = conn.getContentLength();

                // get the filename
                lastSlash = url.toString().lastIndexOf('/');
                fileName = "file.bin";
                if (lastSlash >= 0) {
                    fileName = url.toString().substring(lastSlash + 1);
                }
                if (fileName.equals("")) {
                    fileName = "file.bin";
                }
                DataMembers.fileName = fileName;
                // notify download start
                int fileSizeInKB = fileSize / 1024;

                int fileByDiv = fileSizeInKB > 0 ? (fileSizeInKB / 1000) * 10 : 0;
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_DOWNLOAD_STARTED, fileSizeInKB, 0,
                        fileName);
                activityHandler.sendMessage(msg);

                // start download
                inStream = new BufferedInputStream(conn.getInputStream(),
                        DOWNLOAD_BUFFER_SIZE);
                outFile = new File(
                        parentActivity
                                .getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                                + "/" + fileName);
                fileStream = new FileOutputStream(outFile);
                outStream = new BufferedOutputStream(fileStream,
                        DOWNLOAD_BUFFER_SIZE);
                byte[] data = new byte[DOWNLOAD_BUFFER_SIZE];
                int bytesRead = 0, totalRead = 0;
                while (!isInterrupted()
                        && (bytesRead = inStream.read(data, 0, data.length)) >= 0) {
                    outStream.write(data, 0, bytesRead);

                    // update progress bar
                    totalRead += bytesRead;
                    int totalReadInKB = totalRead / 1024;

                    if (totalReadInKB == fileSizeInKB || fileByDiv == 0 || totalReadInKB % fileByDiv == 0) {
                        msg = Message.obtain(activityHandler,
                                DataMembers.MESSAGE_UPDATE_PROGRESS_BAR,
                                totalReadInKB, 0);
                        activityHandler.sendMessage(msg);
                    }
                }

                outStream.close();
                fileStream.close();
                inStream.close();

                if (isInterrupted()) {
                    outFile.delete();
                } else {
                    msg = Message.obtain(activityHandler,
                            DataMembers.MESSAGE_APK_DOWNLOAD_COMPLETE, type, 0);
                    activityHandler.sendMessage(msg);
                }
            } catch (SocketTimeoutException e) {
                Commons.print("SocketTimeout Exception");
                String errMsg = parentActivity
                        .getString(R.string.socket_time_out_exception);
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_ENCOUNTERED_ERROR_APK, 0, 0, errMsg);
                activityHandler.sendMessage(msg);

            } catch (MalformedURLException e) {
                Commons.print("Malformed URL Exception");
                String errMsg = parentActivity
                        .getString(R.string.error_message_bad_url);
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_ENCOUNTERED_ERROR_APK, 0, 0, errMsg);
                activityHandler.sendMessage(msg);
            } catch (FileNotFoundException e) {
                Commons.print("File Not Found Exception");
                Commons.printException(e);
                String errMsg = parentActivity
                        .getString(R.string.error_message_file_not_found);
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_ENCOUNTERED_ERROR_APK, 0, 0, errMsg);
                activityHandler.sendMessage(msg);
            } catch (Exception e) {
                Commons.printException(e);
                String errMsg = parentActivity
                        .getString(R.string.error_message_general);
                msg = Message.obtain(activityHandler,
                        DataMembers.MESSAGE_ENCOUNTERED_ERROR_APK, type, 0, errMsg);
                activityHandler.sendMessage(msg);
            }

        }
    }

    /**
     * Check APK install from unknown sources enabled or not.
     * @return true | false
     */
    private boolean isNonMarketInstallationOptionEnable() {
        String str = Secure.getString(parentActivity.getContentResolver(),
                Secure.INSTALL_NON_MARKET_APPS);
        return str.equals("1");
    }

    /**
     * Check is external storage availabe with atleast 20 mb space.
     * @return true | false
     */
    private boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable;
        boolean mExternalStorageWriteable;

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
                && mExternalStorageWriteable && mbAvailable > 20;
    }

}
